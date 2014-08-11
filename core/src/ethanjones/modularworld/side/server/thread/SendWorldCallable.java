package ethanjones.modularworld.side.server.thread;

import ethanjones.modularworld.networking.common.packet.PacketQueue;
import ethanjones.modularworld.networking.packets.PacketArea;
import ethanjones.modularworld.world.storage.Area;
import ethanjones.modularworld.world.thread.GenerateWorld;

import java.util.concurrent.Callable;

public class SendWorldCallable implements Callable {

  private final GenerateWorld generateWorld;
  private final Area area;
  private final PacketQueue packetQueue;

  public SendWorldCallable(Area area, PacketQueue packetQueue) {
    this.generateWorld = null;
    this.area = area;
    this.packetQueue = packetQueue;
  }

  public SendWorldCallable(GenerateWorld generateWorld, PacketQueue packetQueue) {
    this.generateWorld = generateWorld;
    this.area = null;
    this.packetQueue = packetQueue;
  }

  @Override
  public Object call() throws Exception {
    Area area = generateWorld != null ? generateWorld.call() : this.area;
    PacketArea packetArea = new PacketArea();
    packetArea.areaX = area.x;
    packetArea.areaY = area.y;
    packetArea.areaZ = area.z;
    packetArea.area = area.write();
    packetQueue.addPacket(packetArea);
    return area;
  }
}