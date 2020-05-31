package xyz.nvda.lootlog.listeners;

import xyz.nvda.lootlog.LocationHandler;
import xyz.nvda.lootlog.LocationHandler.Location;
import xyz.nvda.lootlog.util.ScoreboardUtils;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;

public class PlayerTickHandler {

  private static final Pattern LOCATION_REGEX = Pattern.compile("\u23E3 ([\\w' ]+)");

  private static final LocationHandler locationHandler = LocationHandler.getInstance();

  @SubscribeEvent
  public void onPlayerTick(PlayerTickEvent event) {
    if (event.phase != Phase.START) return;

    Optional<Matcher> matcher =
        Optional.ofNullable(ScoreboardUtils.getUnformattedLine(4)).map(LOCATION_REGEX::matcher);

    if (matcher.isPresent() && matcher.get().find())
      locationHandler.setLocationFromScoreboard(matcher.get().group(1));
    else locationHandler.setLocation(Location.OTHER);
  }
}
