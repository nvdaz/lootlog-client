package com.nvda.lootlog;

import com.nvda.lootlog.hud.DragonHUD;
import com.nvda.lootlog.hud.HUD;
import com.nvda.lootlog.hud.RevenantHUD;
import com.nvda.lootlog.hud.TarantulaHUD;
import com.nvda.lootlog.hud.WolfHUD;

public class LocationHandler {

  private static final LocationHandler instance = new LocationHandler();

  private Location location = Location.OTHER;

  public static LocationHandler getInstance() {
    return instance;
  }

  public Location getLocation() {
    return this.location;
  }

  public void setLocation(Location location) {
    this.location = location;
  }

  public enum Location {
    THE_CATACOMBS("The Catacombs", null),
    THE_END("The End", DragonHUD.getInstance()),
    DRAGON_NEST("Dragon's Nest", DragonHUD.getInstance()),
    HOWLING_CAVE("Howling Cave", WolfHUD.getInstance()),
    RUINS("Ruins", WolfHUD.getInstance()),
    GRAVEYARD("Graveyard", RevenantHUD.getInstance()),
    COAL_MINE("Coal Mine", RevenantHUD.getInstance()),
    SPIDERS_DEN("Spider's Den", TarantulaHUD.getInstance()),
    OTHER(null, null),
    ;

    public final HUD associatedHUD;
    private final String scoreboardName;

    Location(String scoreboardName, HUD associatedHUD) {
      this.scoreboardName = scoreboardName;
      this.associatedHUD = associatedHUD;
    }

    public static Location fromScoreboardName(String name) {
      for (Location location : Location.values())
        if (location.scoreboardName != null && location.scoreboardName.equals(name))
          return location;
      return Location.OTHER;
    }
  }
}
