package ethanjones.cubes.core.json;

import com.eclipsesource.json.JsonValue;
import ethanjones.cubes.block.BlockJson;
import ethanjones.cubes.item.ItemJson;
import ethanjones.cubes.item.crafting.RecipeJson;

public enum JsonStage {
  BLOCK {
    @Override
    public void load(JsonValue jsonValue) {
      BlockJson.json(jsonValue.asArray());
    }
  },
  ITEM {
    @Override
    public void load(JsonValue jsonValue) {
      ItemJson.json(jsonValue.asArray());
    }
  },
  RECIPE {
    @Override
    public void load(JsonValue jsonValue) {
      RecipeJson.json(jsonValue.asArray());
    }
  };

  public abstract void load(JsonValue jsonValue);
}
