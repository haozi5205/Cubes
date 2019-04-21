package ethanjones.cubes.world.generator.smooth;

import ethanjones.cubes.block.Block;
import ethanjones.cubes.block.Blocks;
import ethanjones.cubes.core.logging.Log;
import ethanjones.cubes.core.util.locks.Locked;
import ethanjones.cubes.world.generator.RainStatus;
import ethanjones.cubes.world.generator.TerrainGenerator;
import ethanjones.cubes.world.reference.BlockReference;
import ethanjones.cubes.world.server.WorldServer;
import ethanjones.cubes.world.storage.Area;

import java.util.Random;

public class SmoothWorld extends TerrainGenerator {
  public static final int minSurfaceHeight = 48;
  private static Random randomSeed = new Random();

  public final long baseSeed;
  private final Feature height;
  private final Feature heightVariation;
  private final Feature trees;
  private final Feature rain;
  private final CaveManager caves;

  public SmoothWorld() {
    this(randomSeed.nextLong());
  }

  public SmoothWorld(long baseSeed) {
    this.baseSeed = murmurHash3(baseSeed);
    Log.info("Smooth World Seed: " + baseSeed + " [" + this.baseSeed + "]");

    height = new Feature(murmurHash3(this.baseSeed + 1), 4, 1);
    heightVariation = new Feature(murmurHash3(this.baseSeed + 2), 4, 2);
    trees = new Feature(murmurHash3(this.baseSeed + 3), 1, 3);
    rain = new Feature(murmurHash3(this.baseSeed + 4), 1, 3);

    caves = new CaveManager(this);
  }

  @Override
  public void generate(Area area) {
    for (int x = 0; x < Area.SIZE_BLOCKS; x++) {
      for (int z = 0; z < Area.SIZE_BLOCKS; z++) {
        int g = getSurfaceHeight(x + area.minBlockX, z + area.minBlockZ);
        int d = getDirtHeight(x + area.minBlockX, z + area.minBlockZ);

        if ((x == 0 && z == 0) || g > area.maxY) area.setupArrays(g);

        area.blocks[Area.getRef(x, 0, z)] = Blocks.bedrock.intID;
        for (int y = 1; y < g; y++) {
          if (y < (g - d))
            area.blocks[Area.getRef(x, y, z)] = Blocks.stone.intID;
          else
            area.blocks[Area.getRef(x, y, z)] = Blocks.dirt.intID;
        }
        area.blocks[Area.getRef(x, g, z)] = Blocks.grass.intID;
      }
    }
    caves.apply(area);
  }

  @Override
  public void features(Area area, WorldServer world) {
    if (area.areaX == 0 && area.areaZ == 0) return; // no trees on spawnpoint
    for (int x = 0; x < Area.SIZE_BLOCKS; x++) {
      for (int z = 0; z < Area.SIZE_BLOCKS; z++) {
        double t = trees.eval(area.areaX + x, area.areaZ + z);
        if (t > 0.5d) {
          int trees = 100 - ((int) ((t - 0.4d) / 0.05d));
          if (pseudorandomInt(x + area.minBlockX, z + area.minBlockZ, trees) == 0) {
            genTree(area, x, z);
          }
        }
      }
    }
  }

  @Override
  public BlockReference spawnPoint(WorldServer world) {
    return new BlockReference().setFromBlockCoordinates(0, getSurfaceHeight(0, 0) + 1, 0);
  }

  public void genTree(Area area, int aX, int aZ) {
    int x = aX + area.minBlockX;
    int z = aZ + area.minBlockZ;
    int ground = getSurfaceHeight(x, z);
    if (area.getBlock(aX, ground, aZ) == Blocks.grass) {
      set(area, Blocks.dirt, aX, ground, aZ, 0);
    } else {
      return;
    }

    int y = ground + 1;
    int h = getTreeHeight(x, z) + 1;

    new TreeGenerator() {
      @Override
      protected void setBlock(Area area, Block block, int x, int y, int z, int meta) {
        setVisibleNeighbour(area, block, x, y, z, meta);
      }
    }.generateTree(x, y, z, h, area);
  }

  @Override
  public RainStatus getRainStatus(float x, float z, float rainTime) {
    double r = rain.eval(x / 100, z / 100, rainTime / 1000);
    if (r < 0.6) return RainStatus.NOT_RAINING;
    return new RainStatus(true, (float) ((r - 0.6d) / 0.4d));
  }

  public int getSurfaceHeight(int x, int z) {
    double h = height.eval(x, z) * 20;
    double hv = (heightVariation.eval(x, z) * 6) - 3;
    return minSurfaceHeight + (int) Math.exp(Math.log(h) + hv);
  }

  public int getDirtHeight(int x, int z) {
    double h = height.eval(x, z) * 100;
    return 1 + ((int) Math.floor(h % 3));
  }

  public int getTreeHeight(int x, int z) {
    double h = heightVariation.eval(x, z) * 100;
    return 2 + ((int) Math.floor(h % 3));
  }

  public long pseudorandomBits(long x, long z, int bits, boolean murmurHash3) {
    long l = x + z + (x * (x - 1)) + (z * (z + 1)) + (long) Math.pow(x, z > 0 ? z : (z < 0 ? -z : 1));
    if (murmurHash3) l = murmurHash3(l);
    l += baseSeed;

    long multiplier = 0x5DEECE66DL;
    long addend = 0xBL;

    long l1 = l * multiplier + addend;
    long l2 = l1 * multiplier + addend;
    long lo = (l1 << 32) + l2;
    return lo >>> (64 - bits);
  }

  public int pseudorandomInt(long x, long z, int inclusiveBound) {
    float f = pseudorandomBits(x, z, 24, false) / ((float) (1 << 24));
    return (int) Math.floor(f * (inclusiveBound + 1));
  }

  public static long murmurHash3(long x) {
    x ^= x >>> 33;
    x *= 0xff51afd7ed558ccdL;
    x ^= x >>> 33;
    x *= 0xc4ceb9fe1a85ec53L;
    x ^= x >>> 33;

    return x;
  }
}
