package ethanjones.cubes.world.light;

import ethanjones.cubes.core.id.TransparencyManager;
import ethanjones.cubes.world.CoordinateConverter;
import ethanjones.cubes.world.storage.Area;

import java.util.ArrayDeque;
import java.util.concurrent.locks.ReentrantLock;

import static ethanjones.cubes.world.storage.Area.*;

public class SunLight {
  public static final int MAX_SUNLIGHT = 0xF0;

  public static void initialSunlight(Area area) {
    try (LightWorldSection worldSection = new LightWorldSection(area)) {
      ArrayDeque<LightNode> lightQueue = new ArrayDeque<>();
      int max = 15;
      for (int x = 0; x < SIZE_BLOCKS; x++) {
        for (int z = 0; z < SIZE_BLOCKS; z++) {
          int hmRef = getHeightMapRef(x, z);
          int h = area.heightmap[hmRef] + 1;

          int ref = getRef(x, h, z);
          for (int y = 0; y <= (area.maxY - h); y++) {
            int r = ref + (y * MAX_Y_OFFSET);
            area.light[r] = (byte) ((area.light[r] & 0xF) | (max << 4));
          }

          lightQueue.add(new LightNode(x + area.minBlockX, h, z + area.minBlockZ, max));
        }
      }
      propagateAdd(lightQueue, worldSection);
    }
  }

  public static void addSunlight(int x, int y, int z, Area area, LightWorldSection w) {
    if (y > 0 && y <= area.maxY) {
      ArrayDeque<LightNode> lightQueue = new ArrayDeque<LightNode>(1000);

      if (y <= w.maxY(x + 1, z) && w.transparent(x + 1, y, z))
        lightQueue.add(new LightNode(x + 1, y, z, w.getSunlight(x + 1, y, z)));
      if (y <= w.maxY(x - 1, z) && w.transparent(x - 1, y, z))
        lightQueue.add(new LightNode(x - 1, y, z, w.getSunlight(x - 1, y, z)));
      if (y < w.maxY(x, z) && w.transparent(x, y + 1, z))
        lightQueue.add(new LightNode(x, y + 1, z, w.getSunlight(x, y + 1, z)));
      if (y > 0 && w.transparent(x, y - 1, z)) lightQueue.add(new LightNode(x, y - 1, z, w.getSunlight(x, y - 1, z)));
      if (y <= w.maxY(x, z + 1) && w.transparent(x, y, z + 1))
        lightQueue.add(new LightNode(x, y, z + 1, w.getSunlight(x, y, z + 1)));
      if (y <= w.maxY(x, z - 1) && w.transparent(x, y, z - 1))
        lightQueue.add(new LightNode(x, y, z - 1, w.getSunlight(x, y, z - 1)));

      propagateAdd(lightQueue, w);
    }
  }

  private static void propagateAdd(ArrayDeque<LightNode> lightQueue, LightWorldSection w) {
    if (lightQueue.isEmpty()) return;

    while (!lightQueue.isEmpty()) {
      LightNode n = lightQueue.pop();
      int x = n.x;
      int y = n.y;
      int z = n.z;
      int l = n.l;

      if (l <= 1) continue;

      tryPropagateAdd(lightQueue, w, x - 1, y, z, l - 1);
      tryPropagateAdd(lightQueue, w, x + 1, y, z, l - 1);
      tryPropagateAdd(lightQueue, w, x, y, z - 1, l - 1);
      tryPropagateAdd(lightQueue, w, x, y, z + 1, l - 1);
      if (y > 0) tryPropagateAdd(lightQueue, w, x, y - 1, z, l); // go down without loss in strength
      tryPropagateAdd(lightQueue, w, x, y + 1, z, l - 1);
    }
  }

  // ln has already been subtracted by one
  private static void tryPropagateAdd(ArrayDeque<LightNode> lightQueue, LightWorldSection w, int x, int y, int z, int ln) {
    Area a = w.getArea(CoordinateConverter.area(x), CoordinateConverter.area(z));
    int ref = getRef(x - a.minBlockX, y, z - a.minBlockZ);
    if (!a.isReady() || y > a.maxY || !TransparencyManager.isTransparent(a.blocks[ref])) return;
    int i = ((a.light[ref] >> 4) & 0xF);
    if (i + 1 <= ln) { // DIFFERENT + 1 instead of + 2
      a.light[ref] = (byte) ((a.light[ref] & 0xF) | (ln << 4));
      a.updateRender(y / SIZE_BLOCKS);
      a.modify();
      lightQueue.add(new LightNode(x, y, z, ln));
    }
  }

  public static void removeSunlight(int x, int y, int z, Area area, LightWorldSection lws) {
    if (y > 0 && y <= area.maxY) {
      ArrayDeque<LightNode> removeQueue = new ArrayDeque<LightNode>(1000);
      ArrayDeque<LightNode> addQueue = new ArrayDeque<LightNode>(1000);

      int prev = area.getSunlight(x - area.minBlockX, y, z - area.minBlockZ);
      area.setSunlight(x - area.minBlockX, y, z - area.minBlockZ, 0);
      removeQueue.add(new LightNode(x, y, z, prev));
      propagateRemove(removeQueue, addQueue, lws);
      propagateAdd(addQueue, lws);
    }
  }

  private static void propagateRemove(ArrayDeque<LightNode> removeQueue, ArrayDeque<LightNode> addQueue, LightWorldSection w) {
    if (removeQueue.isEmpty()) return;

    while (!removeQueue.isEmpty()) {
      LightNode n = removeQueue.pop();
      int x = n.x;
      int y = n.y;
      int z = n.z;
      int l = n.l;

      if (l <= 1) continue;

      tryPropagateRemove(removeQueue, addQueue, w, x - 1, y, z, l);
      tryPropagateRemove(removeQueue, addQueue, w, x + 1, y, z, l);
      tryPropagateRemove(removeQueue, addQueue, w, x, y, z - 1, l);
      tryPropagateRemove(removeQueue, addQueue, w, x, y, z + 1, l);
      if (y > 0)
        tryPropagateRemove(removeQueue, addQueue, w, x, y - 1, z, 16); //16 is higher than maximum light, therefore the sunlight is always removed
      tryPropagateRemove(removeQueue, addQueue, w, x, y + 1, z, l);
    }
  }

  private static void tryPropagateRemove(ArrayDeque<LightNode> removeQueue, ArrayDeque<LightNode> addQueue, LightWorldSection w, int x, int y, int z, int l) {
    Area a = w.getArea(CoordinateConverter.area(x), CoordinateConverter.area(z));
    int ref = getRef(x - a.minBlockX, y, z - a.minBlockZ);
    if (!a.isReady() || y > a.maxY || !TransparencyManager.isTransparent(a.blocks[ref])) return;
    int p = ((a.light[ref] >> 4) & 0xF);
    if (p != 0 && p < l) {
      a.light[ref] = (byte) (a.light[ref] & 0xF); // same as ((a.light[ref] & 0xF0) | (0 << 4))
      a.updateRender(y / SIZE_BLOCKS);
      a.modify();
      removeQueue.add(new LightNode(x, y, z, p));
    } else if (p >= l) {
      addQueue.add(new LightNode(x, y, z, p));
    }
  }
}
