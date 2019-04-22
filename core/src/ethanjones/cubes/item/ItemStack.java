package ethanjones.cubes.item;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import ethanjones.cubes.core.id.IDManager;
import ethanjones.cubes.core.logging.Log;
import ethanjones.data.DataGroup;
import ethanjones.data.DataParser;

public class ItemStack implements DataParser {
  public Item item;
  public int count;
  public int meta;

  public ItemStack() {
  }

  public ItemStack(Item item) {
    this.item = item;
    this.count = 1;
    this.meta = 0;
  }

  public ItemStack(Item item, int count) {
    this.item = item;
    this.count = count;
    this.meta = 0;
  }

  public ItemStack(Item item, int count, int meta) {
    this.item = item;
    this.count = count;
    this.meta = meta;
  }

  @Override
  public DataGroup write() {
    DataGroup dataGroup = new DataGroup();
    dataGroup.put("item", IDManager.toInt(item));
    dataGroup.put("count", count);
    if (meta != 0) dataGroup.put("meta", meta);
    return dataGroup;
  }

  @Override
  public void read(DataGroup dataGroup) {
    item = IDManager.toItem(dataGroup.getInteger("item"));
    count = dataGroup.getInteger("count");
    meta = dataGroup.containsKey("meta") ? dataGroup.getInteger("meta") : 0;
  }

  public ItemStack copy() {
    return new ItemStack(item, count, meta);
  }

  public static ItemStack readItemStack(DataGroup dataGroup) {
    ItemStack stack = new ItemStack();
    stack.read(dataGroup);
    if (stack.item == null) {
      Log.debug("Null item after reading itemstack");
      return null;
    }
    return stack;
  }

  public TextureRegion getTextureRegion() {
    return item.getTextureRegion(meta);
  }

  public String getName() {
    return item.getName(meta);
  }
}
