package ethanjones.cubes.block;

import ethanjones.cubes.block.data.BlockData;
import ethanjones.cubes.core.id.TransparencyManager;
import ethanjones.cubes.core.localization.Localization;
import ethanjones.cubes.core.util.BlockFace;
import ethanjones.cubes.entity.ItemEntity;
import ethanjones.cubes.entity.living.player.Player;
import ethanjones.cubes.graphics.world.block.BlockRenderType;
import ethanjones.cubes.graphics.world.block.BlockTextureHandler;
import ethanjones.cubes.input.ClickType;
import ethanjones.cubes.item.ItemBlock;
import ethanjones.cubes.item.ItemStack;
import ethanjones.cubes.item.ItemTool;
import ethanjones.cubes.side.common.Side;
import ethanjones.cubes.world.World;
import ethanjones.cubes.world.collision.BlockIntersection;
import ethanjones.cubes.world.storage.Area;
import ethanjones.data.DataGroup;

public class Block {

  private static final int[] ONE_ZERO = new int[]{0};

  public String id;
  public int intID;
  protected ItemBlock itemBlock;
  protected BlockTextureHandler[] textureHandlers;
  // block mining
  protected float miningTime = 0.5f;
  protected ItemTool.ToolType miningTool = ItemTool.ToolType.pickaxe;
  protected int miningToolLevel = 1;
  protected boolean miningOther = true;

  public Block(String id) {
    if (!id.contains(":")) throw new IllegalArgumentException(id + " is not in the correct format");
    this.id = id.toLowerCase();
    this.itemBlock = new ItemBlock(this);
  }

  public void loadGraphics() {
    textureHandlers = new BlockTextureHandler[]{new BlockTextureHandler(id)};
  }

  public BlockTextureHandler getTextureHandler(int meta) {
    if (meta < 0 || meta >= textureHandlers.length) meta = 0;
    return textureHandlers[meta];
  }

  public ItemBlock getItemBlock() {
    return itemBlock;
  }

  public String getName(int meta) {
    String s = "block." + id.replaceFirst(":", ".");
    return Localization.getFirstAvailable(s + "." + meta, s);
  }

  public int getLightLevel(int meta) {
    return 0;
  }

  public boolean alwaysTransparent() {
    return false; // should be true if isTransparent always return true
  }

  public boolean canBeTransparent() {
    return alwaysTransparent(); // should be true if it is possible for isTransparent to return true
  }

  public boolean isTransparent(int meta) {
    return alwaysTransparent();
  }

  public int[] displayMetaValues() {
    return ONE_ZERO;
  }

  @Override
  public String toString() {
    return id;
  }

  // block mining
  public boolean canMine(ItemStack itemStack) {
    if (itemStack == null || !(itemStack.item instanceof ItemTool)) return miningOther;
    ItemTool itemTool = ((ItemTool) itemStack.item);
    if (itemTool.getToolType() != miningTool) return miningOther;
    return miningOther || miningToolLevel >= itemTool.getToolLevel();
  }

  public float getMiningTime() {
    return miningTime;
  }

  public float getMiningSpeed(ItemStack itemStack) {
    if (itemStack == null || !(itemStack.item instanceof ItemTool)) return 1f;
    ItemTool itemTool = ((ItemTool) itemStack.item);
    if (itemTool.getToolType() != miningTool) return 1f;
    return itemTool.getToolLevel() * 2;
  }

  public boolean onButtonPress(ClickType type, Player player, int blockX, int blockY, int blockZ) {
    return false;
  }

  // coordinates inside area
  public BlockData createBlockData(Area area, int x, int y, int z, int meta, DataGroup dataGroup) {
    return null;
  }

  public boolean blockData() {
    return false; // true if in any state the block has blockdata
  }

  // coordinates inside area
  public void randomTick(World world, Area area, int x, int y, int z, int meta) { }

  public void dropItems(World world, int x, int y, int z, int meta) {
    if (Side.isClient()) return;
    ItemStack[] drops = drops(world, x, y, z, meta);
    for (ItemStack drop : drops) {
      ItemEntity itemEntity = new ItemEntity();
      itemEntity.itemStack = drop;
      itemEntity.position.set(x + 0.5f, y, z + 0.5f);
      world.addEntity(itemEntity);
    }
  }

  public Integer place(World world, int x, int y, int z, int meta, Player player, BlockIntersection intersection) {
    return meta;
  }

  public ItemStack[] drops(World world, int x, int y, int z, int meta) {
    return new ItemStack[]{new ItemStack(getItemBlock(), 1, meta)};
  }

  public BlockRenderType renderType(int meta) {
    return BlockRenderType.DEFAULT;
  }

  public boolean renderFace(BlockFace blockFace, int neighbourIDAndMeta) {
    return TransparencyManager.isTransparent(neighbourIDAndMeta);
  }
}
