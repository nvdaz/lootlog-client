package xyz.nvda.lootlog.hud;

import xyz.nvda.lootlog.api.type.SlayerType;

public class WolfHUD extends SlayerHUD {
  private static final WolfHUD instance = new WolfHUD();

  public static WolfHUD getInstance() {
    return instance;
  }

  protected void loadHUDData() {
    slayerHandler.loadHUDData(SlayerType.WOLF);
  }
}
