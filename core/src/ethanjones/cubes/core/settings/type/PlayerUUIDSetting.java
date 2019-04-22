package ethanjones.cubes.core.settings.type;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonValue;
import ethanjones.cubes.core.gwt.UUID;
import ethanjones.cubes.core.settings.Setting;
import ethanjones.cubes.core.settings.VisualSettingManager;

import static ethanjones.cubes.side.client.CubesClient.uuid;

public class PlayerUUIDSetting extends Setting {
  static {
    uuid = UUID.randomUUID();
  }

  @Override
  public boolean shouldDisplay() {
    return false;
  }

  @Override
  public Actor getActor(String settingName, VisualSettingManager visualSettingManager) {
    return null;
  }

  @Override
  public String toString() {
    return uuid.toString();
  }
  
  @Override
  public JsonValue toJson() {
    return Json.value(uuid.toString());
  }
  
  @Override
  public void readJson(JsonValue json) {
    try {
      uuid = UUID.fromString(json.asString());
    } catch (IllegalArgumentException e) {
      throw new UnsupportedOperationException(e);
    }
  }
}
