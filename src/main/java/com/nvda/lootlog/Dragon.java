package com.nvda.lootlog;

import com.nvda.lootlog.api.GetMyDragonsQuery;
import java.time.LocalDate;
import java.time.ZoneId;

public class Dragon {

  private final int gross;
  private final DragonType dragonType;

  public Dragon(GetMyDragonsQuery.Dragon dragon) {
    this(dragon.gross(), DragonType.of(dragon.dragonType()));
  }

  public Dragon(int gross, DragonType dragonType) {
    this.gross = gross;
    this.dragonType = dragonType;
  }

  public int gross() {
    return gross;
  }

  public DragonType dragonType() {
    return dragonType;
  }

  public enum DragonType {
    SUPERIOR("Superior", 0xeded02),
    STRONG("Strong", 0xb01835),
    UNSTABLE("Unstable", 0x7d0d9f),
    WISE("Wise", 0x1da8a4),
    YOUNG("Young", 0x9ba0a8),
    OLD("Old", 0xa8a177),
    PROTECTOR("Protector", 0x6b6a62);

    private final String name;
    private final int color;

    DragonType(String name, int color) {
      this.name = name;
      this.color = color;
    }

    public static DragonType of(com.nvda.lootlog.api.type.DragonType dragonType) {
      switch (dragonType) {
        case SUPERIOR:
          return SUPERIOR;
        case STRONG:
          return STRONG;
        case UNSTABLE:
          return UNSTABLE;
        case WISE:
          return WISE;
        case YOUNG:
          return YOUNG;
        case OLD:
          return OLD;
        case PROTECTOR:
          return PROTECTOR;
      }
      return null;
    }

    public String getName() {
      return name;
    }

    public int getColor() {
      return color;
    }
  }

  public static class DragonOverview {

    private final LocalDate date;
    private final double gross;

    public DragonOverview(GetMyDragonsQuery.DragonOverview dragonOverview) {
      this(
          dragonOverview.day().atZone(ZoneId.systemDefault()).toLocalDate(),
          dragonOverview.gross());
    }

    public DragonOverview(LocalDate date, double gross) {
      this.date = date;
      this.gross = gross;
    }

    public double gross() {
      return gross;
    }

    public LocalDate date() {
      return date;
    }
  }
}
