package xyz.nvda.lootlog.listeners;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xyz.nvda.lootlog.ConfigurationHandler;
import xyz.nvda.lootlog.LootLog;

public class ChatReceivedHandler {
  private static final Pattern NICK_REGEX =
      Pattern.compile("^You are now nicked as (\\w{3,16})!", Pattern.CASE_INSENSITIVE);
  private static final ConfigurationHandler config = ConfigurationHandler.getInstance();

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void onChat(ClientChatReceivedEvent e) {
    if (e.type == 2) return;

    String message = StringUtils.stripControlCodes(e.message.getUnformattedText());
    Matcher nickMatcher = NICK_REGEX.matcher(message);

    if (nickMatcher.matches()) config.setNickname(nickMatcher.group(1));
    else LootLog.bossHandlers.forEach(bossHandler -> bossHandler.testChat(e.message));
  }
}
