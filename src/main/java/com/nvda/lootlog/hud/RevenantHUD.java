package com.nvda.lootlog.hud;

import com.nvda.lootlog.api.type.SlayerType;

public class RevenantHUD extends SlayerHUD {
  private static final RevenantHUD instance = new RevenantHUD();

  public static RevenantHUD getInstance() {
    return instance;
  }

  protected void loadHUDData() {
    slayerHandler.loadHUDData(SlayerType.REVENANT);
  }
}
