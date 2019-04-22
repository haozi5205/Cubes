package ethanjones.cubes.graphics.world.ao;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import ethanjones.cubes.core.id.TransparencyManager;
import ethanjones.cubes.core.logging.Log;
import ethanjones.cubes.core.settings.Setting;
import ethanjones.cubes.core.settings.Settings;
import ethanjones.cubes.core.settings.type.DropDownSetting;
import ethanjones.cubes.core.system.Pools;
import ethanjones.cubes.graphics.assets.Asset;
import ethanjones.cubes.graphics.assets.Assets;
import ethanjones.cubes.graphics.world.WorldGraphicsPools;
import ethanjones.cubes.graphics.world.area.AreaMesh;
import ethanjones.cubes.world.CoordinateConverter;
import ethanjones.cubes.world.storage.Area;

public final class AmbientOcclusion {

  // ABC
  // D-E
  // FGH

  static final int A = 1;
  static final int B = 1 << 1;
  static final int C = 1 << 2;
  static final int D = 1 << 3;
  static final int E = 1 << 4;
  static final int F = 1 << 5;
  static final int G = 1 << 6;
  static final int H = 1 << 7;

  static final int SQRT_TOTAL = 16;
  static final int TOTAL = SQRT_TOTAL * SQRT_TOTAL;
  static final int INDIVIDUAL_SIZE = 256;
  static final int TEXTURE_SIZE = SQRT_TOTAL * INDIVIDUAL_SIZE;

  static final Pixmap.Format FORMAT = Pixmap.Format.RGBA8888;

  private static Texture loadedTexture = null;
  private static TextureAttribute loadedTextureAttribute = null;
  private static TextureRegion[] loadedRegions = new TextureRegion[TOTAL];

  private static boolean exists(Area base, int x, int y, int z) {
    Area a = base.neighbour(CoordinateConverter.area(x), CoordinateConverter.area(z));
    if (a == null || a.blocks == null) return false; //TODO fix
    if (y > a.maxY || y < 0) return false;
    x -= a.minBlockX;
    z -= a.minBlockZ;
    return !TransparencyManager.isTransparent(a.blocks[Area.getRef(x, y, z)]);
  }

  private static TextureRegion y(Area area, int x, int y, int z) {
    int i = 0;
    if (exists(area, x + 1, y, z - 1)) i |= A;
    if (exists(area, x + 1, y, z)) i |= B;
    if (exists(area, x + 1, y, z + 1)) i |= C;

    if (exists(area, x, y, z - 1)) i |= D;
    if (exists(area, x, y, z + 1)) i |= E;

    if (exists(area, x - 1, y, z - 1)) i |= F;
    if (exists(area, x - 1, y, z)) i |= G;
    if (exists(area, x - 1, y, z + 1)) i |= H;

    return loadedRegions[i];
  }

  public static TextureRegion posY(Area area, int x, int y, int z, boolean ao) {
    if (!ao) return null;
    return y(area, x + area.minBlockX, y + 1, z + area.minBlockZ);
  }

  public static TextureRegion negY(Area area, int x, int y, int z, boolean ao) {
    if (!ao) return null;
    return y(area, x + area.minBlockX, y - 1, z + area.minBlockZ);
  }

  private static TextureRegion x(Area area, int x, int y, int z) {
    int i = 0;
    if (exists(area, x, y + 1, z + 1)) i |= A;
    if (exists(area, x, y + 1, z)) i |= B;
    if (exists(area, x, y + 1, z - 1)) i |= C;

    if (exists(area, x, y, z + 1)) i |= D;
    if (exists(area, x, y, z - 1)) i |= E;

    if (exists(area, x, y - 1, z + 1)) i |= F;
    if (exists(area, x, y - 1, z)) i |= G;
    if (exists(area, x, y - 1, z - 1)) i |= H;
    return loadedRegions[i];
  }

  public static TextureRegion posX(Area area, int x, int y, int z, boolean ao) {
    if (!ao) return null;
    return x(area, x + area.minBlockX + 1, y, z + area.minBlockZ);
  }

  public static TextureRegion negX(Area area, int x, int y, int z, boolean ao) {
    if (!ao) return null;
    return x(area, x + area.minBlockX - 1, y, z + area.minBlockZ);
  }

  private static TextureRegion z(Area area, int x, int y, int z) {
    int i = 0;
    if (exists(area, x + 1, y + 1, z)) i |= A;
    if (exists(area, x, y + 1, z)) i |= B;
    if (exists(area, x - 1, y + 1, z)) i |= C;

    if (exists(area, x + 1, y, z)) i |= D;
    if (exists(area, x - 1, y, z)) i |= E;

    if (exists(area, x + 1, y - 1, z)) i |= F;
    if (exists(area, x, y - 1, z)) i |= G;
    if (exists(area, x - 1, y - 1, z)) i |= H;
    return loadedRegions[i];
  }

  public static TextureRegion posZ(Area area, int x, int y, int z, boolean ao) {
    if (!ao) return null;
    return z(area, x + area.minBlockX, y, z + area.minBlockZ + 1);
  }

  public static TextureRegion negZ(Area area, int x, int y, int z, boolean ao) {
    if (!ao) return null;
    return z(area, x + area.minBlockX, y, z + area.minBlockZ - 1);
  }

  public static boolean load() {
    Strength strength = getStrength();
    if (strength == null) return false;
    if (loadedTexture != null) return true;

    Asset asset = Assets.getAsset("core:ao/ao-texture.png");
    if (asset == null) throw new IllegalStateException("Failed to load AO Texture");
    loadedTexture = new Texture(asset.getFileHandle(), FORMAT, false);
    loadedTextureAttribute = TextureAttribute.createDiffuse(loadedTexture);

    for (int i = 0; i < TOTAL; i++) {
      int x = (i % SQRT_TOTAL) * INDIVIDUAL_SIZE;
      int y = (i / SQRT_TOTAL) * INDIVIDUAL_SIZE;
      loadedRegions[i] = new TextureRegion(loadedTexture, x, y, INDIVIDUAL_SIZE, INDIVIDUAL_SIZE);
    }
    return true;
  }

  public static Texture getTexture() {
    if (!load()) return null;
    return loadedTexture;
  }

  public static TextureAttribute getTextureAttribute() {
    if (!load()) return null;
    return loadedTextureAttribute;
  }

  public static TextureRegion noAO() {
    if (!load()) return null;
    return loadedRegions[0];
  }

  protected static String name(int i) {
    char[] c = new char[]{'-', '-', '-', '-', '-', '-', '-', '-'};

    if ((i & A) == A) c[0] = 'A';
    if ((i & B) == B) c[1] = 'B';
    if ((i & C) == C) c[2] = 'C';
    if ((i & D) == D) c[3] = 'D';
    if ((i & E) == E) c[4] = 'E';
    if ((i & F) == F) c[5] = 'F';
    if ((i & G) == G) c[6] = 'G';
    if ((i & H) == H) c[7] = 'H';

    return String.valueOf(c);
  }

  public static boolean isEnabled() {
    DropDownSetting dropDown = (DropDownSetting) Settings.getSetting(Settings.GRAPHICS_AO);
    return !dropDown.getSelected().equals("disabled");
  }

  public static Strength getStrength() {
    DropDownSetting dropDown = (DropDownSetting) Settings.getSetting(Settings.GRAPHICS_AO);
    if (dropDown.getSelected().equals("disabled")) return null;
    return Strength.valueOf(dropDown.getSelected());
  }

  public static Setting getSetting() {
    String[] strings = new String[Strength.values().length + 1];
    strings[0] = "disabled";
    for (int i = 1; i < strings.length; i++) {
      strings[i] = Strength.values()[i - 1].name();
    }
    return new DropDownSetting(strings) {
      {
        selected = "weak";
      }

      @Override
      public void onChange() {
        super.onChange();
        if (Pools.poolExists(AreaMesh.class)) {
          Log.debug("Clearing AreaMesh pool on AO setting change");
          WorldGraphicsPools.free();
          Pools.clearPool(AreaMesh.class);
        }
      }
    };
  }

  public enum Strength {
    strongest(1f),
    strong(0.75f),
    normal(0.5f),
    weak(0.25f),
    weakest(0f);

    public final float strength;

    Strength(float strength) {
      this.strength = strength;
    }
  }

}
