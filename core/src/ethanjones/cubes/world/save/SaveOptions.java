package ethanjones.cubes.world.save;

import com.badlogic.gdx.math.MathUtils;
import ethanjones.cubes.core.system.Branding;
import ethanjones.cubes.core.system.CubesException;
import ethanjones.cubes.networking.socket.SocketMonitor;
import ethanjones.cubes.side.common.Cubes;
import ethanjones.cubes.world.World;
import ethanjones.cubes.world.generator.RainStatus;
import ethanjones.data.DataGroup;
import ethanjones.data.DataParser;

public class SaveOptions implements DataParser {

  public long worldSeed = MathUtils.random.nextLong();
  public String worldSeedString = String.valueOf(worldSeed);
  public int worldTime = World.MAX_TIME / 4;
  public long worldPlayingTime = 0;
  public String worldType = "core:smooth";
  public Gamemode worldGamemode = Gamemode.survival;
  public DataGroup idManager = new DataGroup();
  public DataGroup player = new DataGroup();

  public long lastOpenedTime = 0;
  public int lastVersionMajor, lastVersionMinor, lastVersionPoint, lastVersionBuild;
  public String lastVersionHash;

  public RainStatus worldRainOverride = null;
  public long worldRainOverrideTime = 0;

  @Override
  public DataGroup write() {
    DataGroup dataGroup = new DataGroup();
    dataGroup.put("saveVersion", 0);
    dataGroup.put("worldSeed", worldSeed);
    dataGroup.put("worldSeedStr", worldSeedString);
    dataGroup.put("worldTime", worldTime);
    dataGroup.put("worldPlayingTime", worldPlayingTime);
    dataGroup.put("worldType", worldType);
    dataGroup.put("worldGamemode", worldGamemode.name());
    dataGroup.put("idManager", idManager);

    if (Cubes.getServer() != null && Cubes.getServer().world != null && Cubes.getServer().world.save.getSaveOptions() == this) {
      dataGroup.put("player", player = Cubes.getServer().getClient((SocketMonitor) null).getPlayer().write());
    } else {
      dataGroup.put("player", player);
    }

    if (worldRainOverride != null) {
      DataGroup rain = new DataGroup();
      rain.put("time", worldRainOverrideTime);
      rain.put("raining", worldRainOverride.raining);
      rain.put("rate", worldRainOverride.rainRate);
      dataGroup.put("worldRainOverride", rain);
    }
  
    dataGroup.put("lastOpenedTime", System.currentTimeMillis());
  
    DataGroup version = dataGroup.getGroup("lastVersion");
    version.put("major", Branding.VERSION_MAJOR);
    version.put("minor", Branding.VERSION_MINOR);
    version.put("point", Branding.VERSION_POINT);
    version.put("build", Branding.VERSION_BUILD);
    version.put("hash", Branding.VERSION_HASH);
    
    return dataGroup;
  }

  @Override
  public void read(DataGroup dataGroup) {
    if (dataGroup.getInteger("saveVersion") != 0) throw new CubesException("Invalid save version");
    worldSeed = dataGroup.getLong("worldSeed");
    worldSeedString = dataGroup.getString("worldSeedStr");
    worldTime = dataGroup.getInteger("worldTime");
    worldPlayingTime = dataGroup.getLong("worldPlayingTime");
    worldType = dataGroup.getString("worldType");
    worldGamemode = Gamemode.valueOf(dataGroup.getString("worldGamemode"));
    idManager = dataGroup.getGroup("idManager");

    player = dataGroup.getGroup("player");

    if (dataGroup.containsKey("worldRainOverride")) {
      DataGroup rain = dataGroup.getGroup("worldRainOverride");
      worldRainOverrideTime = rain.getLong("time");
      worldRainOverride = new RainStatus(rain.getBoolean("raining"), rain.getFloat("rate"));
    } else {
      worldRainOverrideTime = 0;
      worldRainOverride = null;
    }

    lastOpenedTime = dataGroup.getLong("lastOpenedTime");
  
    DataGroup version = dataGroup.getGroup("lastVersion");
    lastVersionMajor = version.getInteger("major");
    lastVersionMinor = version.getInteger("minor");
    lastVersionPoint = version.getInteger("point");
    lastVersionBuild = version.getInteger("build");
    lastVersionHash = version.getString("hash");
  }
  
  public void setWorldSeed(String seedString) {
    long seed = 0;
    try {
      seed = Long.parseLong(seedString);
    } catch (NumberFormatException e) {
      if (seedString.isEmpty()) {
        seed = MathUtils.random.nextLong();
      } else {
        seed = seedString.hashCode();
      }
    }
    this.worldSeed = seed;
    this.worldSeedString = seedString;
  }
}
