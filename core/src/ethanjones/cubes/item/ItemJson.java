package ethanjones.cubes.item;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import ethanjones.cubes.core.id.IDManager;
import ethanjones.cubes.core.json.JsonException;
import ethanjones.cubes.graphics.assets.Assets;

public class ItemJson {

  public static void json(JsonArray json) {
    for (JsonValue value : json) {
      addItem(value.asObject());
    }
  }

  public static void addItem(JsonObject json) {
    if (json.get("tool") != null) {
      addItemTool(json);
      return;
    }
    String id = json.getString("id", null);
    if (id == null) throw new JsonException("No item id");
    JItem item = new JItem(id);

    JsonValue prop;

    prop = json.get("texture");
    if (prop != null) {
      item.textureString = prop.asString();
    } else {
      item.textureString = id;
    }

    for (JsonObject.Member member : json) {
      switch (member.getName()) {
        case "id":
        case "texture":
          break;
        default:
          throw new JsonException("Unexpected block member \"" + member.getName() + "\"");
      }
    }

    IDManager.register(item);
  }

  public static void addItemTool(JsonObject json) {
    String id = json.getString("id", null);
    if (id == null) throw new JsonException("No item id");
    JItemTool item = new JItemTool(id);

    JsonObject tool = json.get("tool").asObject();
    for (JsonObject.Member member : tool) {
      switch (member.getName()) {
        case "type":
          item.setToolType(toolType(member.getValue().asString()));
          break;
        case "level":
          item.setToolLevel(member.getValue().asInt());
          break;
        default:
          throw new JsonException("Unexpected item tool member \"" + member.getName() + "\"");
      }
    }

    JsonValue prop;

    prop = json.get("texture");
    if (prop != null) {
      item.textureString = prop.asString();
    } else {
      item.textureString = id;
    }

    for (JsonObject.Member member : json) {
      switch (member.getName()) {
        case "id":
        case "texture":
        case "tool":
          break;
        default:
          throw new JsonException("Unexpected block member \"" + member.getName() + "\"");
      }
    }

    IDManager.register(item);
  }

  public static ItemTool.ToolType toolType(String s) {
    switch (s) {
      case "pickaxe":
        return ItemTool.ToolType.pickaxe;
      case "axe":
        return ItemTool.ToolType.axe;
      case "shovel":
        return ItemTool.ToolType.shovel;
      case "none":
        return ItemTool.ToolType.none;
      default:
        throw new JsonException("No such tool: \"" + s + "\"");
    }
  }

  public static boolean isJsonItem(Item i) {
    return i instanceof JItem || i instanceof JItemTool;
  }

  private static class JItem extends Item {
    protected String textureString;

    public JItem(String id) {
      super(id);
    }

    @Override
    public void loadGraphics() {
      this.texture = Assets.getBlockItemTextureRegion(textureString, "item");
    }
  }

  private static class JItemTool extends ItemTool {
    protected String textureString;

    public JItemTool(String id) {
      super(id);
    }

    @Override
    public void loadGraphics() {
      this.texture = Assets.getBlockItemTextureRegion(textureString, "item");
    }

    protected void setToolType(ToolType type) {
      this.toolType = type;
    }

    protected void setToolLevel(int level) {
      this.toolLevel = level;
    }
  }
}
