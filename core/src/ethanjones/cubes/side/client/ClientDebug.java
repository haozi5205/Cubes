package ethanjones.cubes.side.client;

import ethanjones.cubes.core.platform.Compatibility;
import ethanjones.cubes.core.settings.Settings;
import ethanjones.cubes.core.system.Branding;
import ethanjones.cubes.core.util.PerSecond;
import ethanjones.cubes.entity.living.player.Player;
import ethanjones.cubes.graphics.rendering.WorldRenderer;
import ethanjones.cubes.graphics.world.ao.AmbientOcclusion;
import ethanjones.cubes.graphics.world.area.AreaRenderer;
import ethanjones.cubes.side.common.Cubes;
import ethanjones.cubes.world.CoordinateConverter;
import ethanjones.cubes.world.collision.BlockIntersection;
import ethanjones.cubes.world.storage.Area;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.WindowedMean;

import java.text.DecimalFormat;

import static ethanjones.cubes.graphics.Graphics.glProfiler;

public class ClientDebug {

  private static final String lineSeparator = System.getProperty("line.separator");
  private static WindowedMean frameMS = new WindowedMean(50);
  private static PerSecond fps = new PerSecond(10);
  private static StringBuilder builder = new StringBuilder(250).append(Branding.DEBUG).append(lineSeparator);
  private static int brandingDebugLength = builder.length();
  private static DecimalFormat twoDP = new DecimalFormat("0.00");
  private static DecimalFormat oneDP = new DecimalFormat("0.0");
  private static boolean glProfilerEnabled = false;

  protected static void setup() {
    glProfilerEnabled = Settings.getBooleanSettingValue(Settings.DEBUG_GL_PROFILER);
    if (glProfilerEnabled) {
      glProfiler.enable();
    } else {
      glProfiler.disable();
    }
  }

  protected static void frameStart() {
    fps.tick();
    if (glProfilerEnabled && Cubes.getClient().renderer.guiRenderer.debugEnabled) {
      LastFrame.totalCalls = glProfiler.getCalls();
      LastFrame.drawCalls = glProfiler.getDrawCalls();
      LastFrame.textureBindings = glProfiler.getTextureBindings();
      LastFrame.shaderSwitches = glProfiler.getShaderSwitches();
      LastFrame.vertexCount = glProfiler.getVertexCount().total;

      glProfiler.reset();
    }
  }

  public static String getDebugString() {
    Vector3 p = Cubes.getClient().player.position;
    WorldRenderer worldRenderer = Cubes.getClient().renderer.worldRenderer;
    int i = Settings.getIntegerSetting(Settings.GRAPHICS_VIEW_DISTANCE).get();
    frameMS.addValue(Gdx.graphics.getRawDeltaTime() * 1000f);

    builder.setLength(brandingDebugLength);
    builder.append("FPS:").append(Gdx.graphics.getFramesPerSecond()).append(" MS:").append(twoDP.format(frameMS.getMean())).append(" MEM:").append(Compatibility.get().getFreeMemory()).append("MB").append(lineSeparator);
    builder.append("TPS C:").append(Cubes.getClient().ticksPerSecond.last()).append(" A:").append(oneDP.format(Cubes.getClient().ticksPerSecond.average()));
    if (Cubes.getServer() != null) builder.append(" S:").append(Cubes.getServer().ticksPerSecond.last()).append(" A:").append(oneDP.format(Cubes.getServer().ticksPerSecond.average())).append(" MS:").append(twoDP.format(Cubes.getServer().meanUpdateMS.getMean()));
    builder.append(lineSeparator);
    builder.append("POS X:").append(twoDP.format(p.x)).append("(").append(CoordinateConverter.area(p.x)).append(")").append(" Y:").append(twoDP.format(p.y)).append("(").append(CoordinateConverter.area(p.y)).append(")").append(" Z:").append(twoDP.format(p.z)).append("(").append(CoordinateConverter.area(p.z)).append(")").append(lineSeparator);
    builder.append("DIR X:").append(twoDP.format(Cubes.getClient().player.angle.x)).append(" Y:").append(twoDP.format(Cubes.getClient().player.angle.y)).append(" Z:").append(twoDP.format(Cubes.getClient().player.angle.z)).append(lineSeparator);

    builder.append("REN A:").append(AreaRenderer.renderedThisFrame).append(" Q:").append(AreaRenderer.refreshQueueLength).append(" M:").append(AreaRenderer.renderedMeshesThisFrame).append(" E:").append(worldRenderer.getEntitiesDrawn()).append("/").append(worldRenderer.getTotalEntities()).append(lineSeparator);

    builder.append("GFX D:").append(i);
    if (i > 16) builder.append("(").append(worldRenderer.getEffectiveViewDistance()).append(")");
    if (Settings.getBooleanSettingValue(Settings.GRAPHICS_FOG)) builder.append(" FOG");
    if (AmbientOcclusion.isEnabled()) builder.append(" AO");
    builder.append(lineSeparator);

    //builder.append("REF A:").append(AreaRenderer.refreshedThisFrame).append(" M:").append(AreaRenderer.refreshedMeshesThisFrame).append(lineSeparator);
    if (glProfilerEnabled) {
      builder.append("TC:").append(LastFrame.totalCalls).append(" DG:").append(LastFrame.drawCalls).append(" TB:").append(LastFrame.textureBindings).append(" SS:").append(LastFrame.shaderSwitches).append(" VC:").append(LastFrame.vertexCount()).append(lineSeparator);
    }
    builder.append("W B:").append(getBlockLight()).append(" S:").append(getSunlight()).append(" T:").append(Cubes.getClient().world.getTime());
    BlockIntersection blockIntersection = BlockIntersection.getBlockIntersection(Cubes.getClient().player.position, Cubes.getClient().player.angle, Cubes.getClient().world);
    if (blockIntersection != null && blockIntersection.getBlock() != null) {
      builder.append(lineSeparator).append("B ID:").append(blockIntersection.getBlock().id).append(" M:").append(blockIntersection.getBlockMeta());
    }
    return builder.toString();
  }

  private static int getBlockLight() {
    Player player = Cubes.getClient().player;
    Area area = Cubes.getClient().world.getArea(CoordinateConverter.area(player.position.x), CoordinateConverter.area(player.position.z));
    if (area != null) {
      int x = CoordinateConverter.block(player.position.x);
      int y = CoordinateConverter.block(player.position.y - player.height);
      int z = CoordinateConverter.block(player.position.z);
      if (y > area.maxY || y < 0) return 0;
      return area.getLight(x - area.minBlockX, y, z - area.minBlockZ);
    }
    return 0;
  }

  private static int getSunlight() {
    Player player = Cubes.getClient().player;
    Area area = Cubes.getClient().world.getArea(CoordinateConverter.area(player.position.x), CoordinateConverter.area(player.position.z));
    if (area != null) {
      int x = CoordinateConverter.block(player.position.x);
      int y = CoordinateConverter.block(player.position.y - player.height);
      int z = CoordinateConverter.block(player.position.z);
      if (y > area.maxY || y < 0) return 0;
      return area.getSunlight(x - area.minBlockX, y, z - area.minBlockZ);
    }
    return 0;
  }

  private static class LastFrame {

    private static final DecimalFormat vertexFormat = new DecimalFormat("0.##E0");

    static int totalCalls;
    static int drawCalls;
    static int textureBindings;
    static int shaderSwitches;
    static float vertexCount;

    static String vertexCount() {
      if (vertexCount < 100000) return "" + ((int) vertexCount);
      return vertexFormat.format(vertexCount);
    }
  }
}
