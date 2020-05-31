package xyz.nvda.lootlog;

import xyz.nvda.lootlog.hud.DragonHUD;
import xyz.nvda.lootlog.hud.HUD;
import xyz.nvda.lootlog.hud.RevenantHUD;
import xyz.nvda.lootlog.hud.TarantulaHUD;
import xyz.nvda.lootlog.hud.WolfHUD;

public class LocationHandler {

  private static final LocationHandler instance = new LocationHandler();

  private long lastUpdated = System.currentTimeMillis();
  private Location location = Location.OTHER;

  public static LocationHandler getInstance() {
    return instance;
  }

  public Location getLocation() {
    return this.location;
  }

  public void setLocation(Location location) {
    this.lastUpdated = System.currentTimeMillis();
    this.location = location;
  }

  public void setLocationFromScoreboard(String name) {
    if (!name.equals("None")) this.setLocation(Location.fromScoreboardName(name));
    else if (System.currentTimeMillis() - this.lastUpdated < 10000)
      this.setLocation(Location.OTHER);
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
