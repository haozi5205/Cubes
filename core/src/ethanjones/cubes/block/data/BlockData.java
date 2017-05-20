package ethanjones.cubes.block.data;

import ethanjones.cubes.networking.NetworkingManager;
import ethanjones.cubes.networking.packets.PacketBlockData;
import ethanjones.cubes.side.common.Side;
import ethanjones.cubes.world.storage.Area;
import ethanjones.cubes.world.storage.WorldStorageInterface.ChangedBlock;
import ethanjones.data.DataGroup;
import ethanjones.data.DataParser;

public abstract class BlockData implements DataParser {
  private Area area;
  private int x, y, z;

  public BlockData(Area area, int x, int y, int z) {
    this.area = area;
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public Area getArea() {
    return area;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public int getZ() {
    return z;
  }

  public void sync() {
    DataGroup d = write();
    for (ChangedBlock changedBlock : area.changedBlockList) {
      if (changedBlock.ref == Area.getRef(x, y, z)) {
        changedBlock.data = d;
        break;
      }
    }
    area.modify();
    
    if (Area.isShared()) return;
    PacketBlockData packet = new PacketBlockData();
    packet.areaX = area.areaX;
    packet.areaZ = area.areaZ;
    packet.blockX = x;
    packet.blockY = y;
    packet.blockZ = z;
    packet.dataGroup = d;
    if (Side.isClient()) {
      NetworkingManager.sendPacketToServer(packet);
    } else {
      NetworkingManager.sendPacketToAllClients(packet);
    }
  }

  public void update() {

  }
}
