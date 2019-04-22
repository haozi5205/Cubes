package ethanjones.cubes.core.platform;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import ethanjones.cubes.core.gwt.FakeAtomic.AtomicBoolean;
import ethanjones.cubes.core.gwt.Task;
import ethanjones.cubes.core.localization.Localization;
import ethanjones.cubes.core.logging.Log;
import ethanjones.cubes.core.system.Branding;
import ethanjones.cubes.core.system.CubesException;
import ethanjones.cubes.core.system.Debug;
import ethanjones.cubes.graphics.Graphics;
import ethanjones.cubes.graphics.menu.Menu;
import ethanjones.cubes.graphics.menu.MenuManager;
import ethanjones.cubes.graphics.menus.MainMenu;
import ethanjones.cubes.input.InputChain;
import ethanjones.cubes.side.client.CubesClient;
import ethanjones.cubes.side.common.Cubes;
import ethanjones.cubes.side.common.Side;
import ethanjones.cubes.side.server.CubesServer;
import ethanjones.cubes.side.server.integrated.IntegratedServer;
import ethanjones.cubes.world.client.WorldClient;

public class ClientAdapter implements AdapterInterface {

  private Menu menu;
  private IntegratedServer cubesServer;
  private CubesClient cubesClient;

  private AtomicBoolean setupClient = new AtomicBoolean(false);
  private AtomicBoolean setupMenu = new AtomicBoolean(false);

  public ClientAdapter() {
    Adapter.setInterface(this);
  }

  @Override
  public void create() {
    try {
      Gdx.graphics.setTitle(Branding.DEBUG);
      Cubes.setup(this);
      setMenu(new MainMenu());
      Log.info(Localization.get("client.client_loaded"));
    } catch (StopLoopException e) {
      Log.debug(e);
    } catch (Exception e) {
      Debug.crash(e);
    }
  }

  @Override
  public void setClient(CubesClient cubesClient) throws UnsupportedOperationException {
    //CubesSecurity.checkSetCubes();
    if (cubesClient != null) {
      this.cubesClient = cubesClient;
      Log.debug("Client set");
      setupClient.set(true);
    } else {
      this.cubesClient = null;
      Log.debug("Client set to null");
      setupClient.set(false);
    }
  }

  @Override
  public void resize(int width, int height) {
    if (width == 0 || height == 0) return;
    try {
      Graphics.resize();
      if (menu != null) menu.resize(Graphics.GUI_WIDTH, Graphics.GUI_HEIGHT);
      if (cubesClient != null) cubesClient.resize(width, height);
    } catch (StopLoopException e) {
      Log.debug(e);
    } catch (Exception e) {
      Debug.crash(e);
    }
  }

  @Override
  public void setServer(CubesServer cubesServer) throws UnsupportedOperationException {
    //CubesSecurity.checkSetCubes();
    if (cubesServer != null) {
      if (cubesServer instanceof IntegratedServer) {
        this.cubesServer = (IntegratedServer) cubesServer;
        Log.debug("Server set");
        this.cubesServer.start();
      } else {
        Log.warning("Server can only be set to an IntegratedServer");
      }
    } else {
      this.cubesServer = null;
      Log.debug("Server set to null");
    }
  }

  @Override
  public void render() {
    try {
      if (cubesClient == null && menu == null) { //Nothing to render
        Debug.crash(new CubesException("CubesClient and Menu both null"));
      }
      Compatibility.get().update();
      glClear();
      if (cubesClient != null) {
        if (setupClient.getAndSet(false)) {
          cubesClient.create();
          cubesClient.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
        cubesClient.render();
      }
      if (menu != null) {
        if (setupMenu.getAndSet(false)) {
          menu.resize(Graphics.GUI_WIDTH, Graphics.GUI_HEIGHT);
          InputChain.showMenu(menu);
          menu.show();
        }
        if (menu.shouldRenderBackground()) MenuManager.renderBackground();
        menu.render(); //Render menu over client
      }
      if (menu != null || (cubesClient != null && cubesClient.renderer != null && cubesClient.renderer.noCursorCatching()) || Compatibility.get().isTouchScreen()) {
        Compatibility.get().setCursorCatched(false);
      } else {
        Compatibility.get().setCursorCatched(true);
      }
      Task.runTasks();
    } catch (StopLoopException e) {
      Log.debug(e);
    } catch (Exception e) {
      Debug.crash(e);
    }
  }

  private void glClear() {
    try {
      Color skyColour = ((WorldClient) cubesClient.world).getSkyColour();
      Gdx.gl20.glClearColor(skyColour.r, skyColour.g, skyColour.b, skyColour.a);
    } catch (Exception ignored) {
      Gdx.gl20.glClearColor(0, 0, 0, 1f);
    }
    Gdx.gl20.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
  }

  @Override
  public void setMenu(Menu menu) {
    //CubesSecurity.checkSetMenu();
    Menu old = this.menu;
    if (old != null) {
      old.hide();
      InputChain.hideMenu(old);
    }
    this.menu = menu;
    if (menu != null) {
      Log.debug("Menu set to " + menu.getClass().getSimpleName());
      MenuManager.setMenu(menu);
      setupMenu.set(true);
    } else {
      Log.debug("Menu set to null");
      setupMenu.set(false);
    }
  }

  @Override
  public void pause() {
    try {
      if (cubesClient != null) cubesClient.pause();
    } catch (StopLoopException e) {
      Log.debug(e);
    } catch (Exception e) {
      Debug.crash(e);
    }
  }

  @Override
  public CubesClient getClient() {
    return cubesClient;
  }

  @Override
  public void resume() {
    try {
      if (cubesClient != null) cubesClient.resume();
    } catch (StopLoopException e) {
      Log.debug(e);
    } catch (Exception e) {
      Debug.crash(e);
    }
  }

  @Override
  public CubesServer getServer() {
    return cubesServer;
  }

  @Override
  public Menu getMenu() {
    return menu;
  }

  @Override
  public Side getSide() {
    return Side.Client;
  }

  @Override
  public void dispose() {
    try {
      Adapter.dispose();
    } catch (StopLoopException ignored) {

    }
  }
}
