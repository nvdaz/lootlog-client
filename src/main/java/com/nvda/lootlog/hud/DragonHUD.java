package com.nvda.lootlog.hud;

import com.nvda.lootlog.ConfigurationHandler;
import com.nvda.lootlog.Dragon;
import com.nvda.lootlog.Dragon.DragonOverview;
import com.nvda.lootlog.Dragon.DragonType;
import com.nvda.lootlog.DragonHandler;
import com.nvda.lootlog.util.NumberUtil;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

public class DragonHUD extends HUD {

  private static final DragonHandler dragonHandler = DragonHandler.getInstance();
  private static final DragonHUD instance = new DragonHUD();
  private int ticks = REFRESH_TICKS;

  public static DragonHUD getInstance() {
    return instance;
  }

  private Size render(
      List<Dragon> dragons, DragonOverview overviewToday, DragonOverview overviewAllTime) {

    Anchor anchor = ConfigurationHandler.getInstance().getHUDAnchor();
    Size size = new Size();

    if (dragons.size() > 0) {
      String title = "Recent Dragons";

      fontRenderer.drawStringWithShadow(title, anchor.getX(), anchor.getY(), 0x6391a6);

      size.addHeight(fontRenderer.FONT_HEIGHT + 2);
      size.maxWidth(fontRenderer.getStringWidth(title));
    }
    for (Dragon dragon : dragons) {
      String namePart = " " + dragon.dragonType().getName();
      int namePartWidth = fontRenderer.getStringWidth(namePart);

      String parentheticalPart = " (" + NumberUtil.formatNumberShort(dragon.gross()) + ")";
      int parentheticalPartWidth = fontRenderer.getStringWidth(parentheticalPart);

      fontRenderer.drawStringWithShadow(
          namePart,
          anchor.getX(),
          anchor.getY() + size.getHeight(),
          dragon.dragonType().getColor());
      fontRenderer.drawStringWithShadow(
          parentheticalPart,
          anchor.getX() + namePartWidth,
          anchor.getY() + size.getHeight(),
          0x6391a6);

      size.addHeight(fontRenderer.FONT_HEIGHT + 2);
      size.maxWidth(namePartWidth + parentheticalPartWidth);
    }

    if (overviewToday != null) {
      String today = "Today: " + NumberUtil.formatNumberShort((long) overviewToday.gross());
      int todayWidth = fontRenderer.getStringWidth(today);

      fontRenderer.drawStringWithShadow(
          today, anchor.getX(), anchor.getY() + size.getHeight(), 0x6391a6);

      size.maxWidth(todayWidth);
      size.addHeight(fontRenderer.FONT_HEIGHT + 2);
    }

    if (overviewAllTime != null) {
      String today = "All Time: " + NumberUtil.formatNumberShort((long) overviewAllTime.gross());
      int todayWidth = fontRenderer.getStringWidth(today);

      fontRenderer.drawStringWithShadow(
          today, anchor.getX(), anchor.getY() + size.getHeight(), 0x6391a6);

      size.maxWidth(todayWidth);
      size.addHeight(fontRenderer.FONT_HEIGHT + 2);
    }

    return size;
  }

  public void render() {
    if (ticks >= REFRESH_TICKS) {
      ticks = 0;
      dragonHandler.loadHUDData();
    }
    ticks++;

    this.render(
        dragonHandler.dragons(),
        dragonHandler.dragonOverviews().stream()
            .filter((dragonOverview -> dragonOverview.date().isEqual(LocalDate.now())))
            .findFirst()
            .orElse(null),
        dragonHandler.dragonOverviews().stream()
            .filter(
                (dragonOverview) ->
                    dragonOverview
                            .date()
                            .atStartOfDay()
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli()
                        < 0)
            .findFirst()
            .orElse(null));
  }

  public Size renderPlaceholder() {
    return this.render(
        Collections.nCopies(3, new Dragon(1000000, DragonType.SUPERIOR)),
        new DragonOverview(null, 1000000),
        new DragonOverview(null, 1000000));
  }
}
