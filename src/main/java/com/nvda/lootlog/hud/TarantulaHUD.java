package com.nvda.lootlog.hud;

import com.nvda.lootlog.api.type.SlayerType;

public class TarantulaHUD extends SlayerHUD {
  private static final TarantulaHUD instance = new TarantulaHUD();

  public static TarantulaHUD getInstance() {
    return instance;
  }

  protected void loadHUDData() {
    slayerHandler.loadHUDData(SlayerType.TARANTULA);
  }
}
