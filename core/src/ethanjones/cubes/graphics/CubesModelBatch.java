package ethanjones.cubes.graphics;

import ethanjones.cubes.core.logging.Log;
import ethanjones.cubes.core.performance.Performance;
import ethanjones.cubes.core.performance.PerformanceTags;
import ethanjones.cubes.core.system.CubesException;
import ethanjones.cubes.core.system.Debug;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.utils.RenderableSorter;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import java.lang.reflect.Field;
import java.util.Comparator;

public class CubesModelBatch extends ModelBatch {

  protected static class CubesRenderableSorter implements RenderableSorter, Comparator<Renderable> {
    // taken from DefaultRenderableSorter and modified

    private Camera camera;
    private final Vector3 translation1 = new Vector3();
    private final Vector3 translation2 = new Vector3();

    @Override
    public void sort(final Camera camera, final Array<Renderable> renderables) {
      this.camera = camera;
      Performance.start(PerformanceTags.CLIENT_RENDER_WORLD_FLUSH_SORT);
      renderables.sort(this);
      Performance.stop(PerformanceTags.CLIENT_RENDER_WORLD_FLUSH_SORT);
    }

    @Override
    public int compare(final Renderable o1, final Renderable o2) {
      if (o1.material == o2.material) {
        // when materials are the same, use this quicker path
        translation1.set(o1.meshPart.center).mul(o1.worldTransform);
        translation2.set(o2.meshPart.center).mul(o2.worldTransform);
        final float dst = (int) (1000f * camera.position.dst2(translation1)) - (int) (1000f * camera.position.dst2(translation2));
        return dst < 0 ? 1 : (dst > 0 ? -1 : 0);
      }

      final boolean b1 = o1.material.has(BlendingAttribute.Type) && ((BlendingAttribute) o1.material.get(BlendingAttribute.Type)).blended;
      final boolean b2 = o2.material.has(BlendingAttribute.Type) && ((BlendingAttribute) o2.material.get(BlendingAttribute.Type)).blended;
      if (b1 != b2) return b1 ? 1 : -1;

      translation1.set(o1.meshPart.center).mul(o1.worldTransform);
      translation2.set(o2.meshPart.center).mul(o2.worldTransform);
      final float dst = (int) (1000f * camera.position.dst2(translation1)) - (int) (1000f * camera.position.dst2(translation2));
      final int result = dst < 0 ? -1 : (dst > 0 ? 1 : 0);
      return b1 ? -result : result;
    }
  }

  protected static class CubesRenderablePool extends RenderablePool {

    @Override
    protected Renderable newObject() {
      return new CubesRenderable();
    }

    @Override
    public Renderable obtain() {
      Renderable renderable = super.obtain();
      if (!(renderable instanceof CubesRenderable)) throw new IllegalStateException();
      return ((CubesRenderable) renderable).reset();
    }

    @Override
    protected void reset(Renderable object) {
      if (!(object instanceof CubesRenderable)) {
        Log.error("Resetting non-Cubes renderable! Flushing pool");
        flush();
      }
    }

    @Override
    public void free(Renderable object) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void freeAll(Array<Renderable> objects) {
      throw new UnsupportedOperationException();
    }
  }

  public CubesModelBatch() {
    super(new CubesShaderProvider(), new CubesRenderableSorter());

    try {
      Field field = ModelBatch.class.getDeclaredField("renderablesPool");
      field.setAccessible(true);
      field.set(this, new CubesRenderablePool());
      field.setAccessible(false);
      if (!(renderablesPool instanceof CubesRenderablePool)) throw new CubesException("Not instance of CubesRenderablePool");
    } catch (Exception e) {
      Debug.crash(new CubesException("Failed to setup CubesRenderablePool", e));
    }
  }
}