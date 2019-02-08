package ethanjones.cubes.block.blocks;

import ethanjones.cubes.block.Block;
import ethanjones.cubes.block.Blocks;
import ethanjones.cubes.core.util.ThreadRandom;
import ethanjones.cubes.entity.living.player.Player;
import ethanjones.cubes.graphics.world.block.BlockRenderType;
import ethanjones.cubes.item.ItemTool.ToolType;
import ethanjones.cubes.world.World;
import ethanjones.cubes.world.collision.BlockIntersection;
import ethanjones.cubes.world.generator.smooth.TreeGenerator;
import ethanjones.cubes.world.storage.Area;

public class BlockSapling extends Block {

  public BlockSapling() {
    super("core:sapling");
    miningTime = 0.4f;
    miningTool = ToolType.none;
    miningOther = true;
  }

  @Override
  public BlockRenderType renderType(int meta) {
    return BlockRenderType.CROSS;
  }

  @Override
  public boolean alwaysTransparent() {
    return true;
  }

  @Override
  public void randomTick(World world, Area area, int x, int y, int z, int meta) {
    if ((world.isDay() && area.getSunlight(x, y + 1, z) >= 10) || area.getLight(x, y + 1, z) >= 10) {
      if (meta >= 7) {
        new TreeGenerator().generateTree(x + area.minBlockX, y, z + area.minBlockZ, 3 + ThreadRandom.get().nextInt(3), area);
      } else {
        area.setBlock(this, x, y, z, meta + 1);
      }
    }
  }

  @Override
  public Integer place(World world, int x, int y, int z, int meta, Player player, BlockIntersection intersection) {
    Block under = world.getBlock(x, y - 1, z);
    if (under == Blocks.grass || under == Blocks.dirt) {
      return super.place(world, x, y, z, meta, player, intersection);
    }
    return null;
  }
}
