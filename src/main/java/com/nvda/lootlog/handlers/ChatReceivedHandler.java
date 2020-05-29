package com.nvda.lootlog.handlers;

import com.nvda.lootlog.ConfigurationHandler;
import com.nvda.lootlog.DragonHandler;
import com.nvda.lootlog.GolemHandler;
import com.nvda.lootlog.SlayerHandler;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ChatReceivedHandler {
  private static final Pattern NICK_REGEX =
      Pattern.compile("^You are now nicked as (\\w{3,16})!", Pattern.CASE_INSENSITIVE);
  private static final ConfigurationHandler config = ConfigurationHandler.getInstance();
  private static final DragonHandler dragonHandler = DragonHandler.getInstance();
  private static final SlayerHandler slayerHandler = SlayerHandler.getInstance();
  private static final GolemHandler golemHandler = GolemHandler.getInstance();

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void onChat(ClientChatReceivedEvent e) {
    if (e.type == 2) return;

    String message = StringUtils.stripControlCodes(e.message.getUnformattedText());
    Matcher nickMatcher = NICK_REGEX.matcher(message);

    if (nickMatcher.matches()) {
      config.setNickname(nickMatcher.group(1));
    } else if (dragonHandler.testChat(e.message) || slayerHandler.testChat(e.message) || golemHandler.testChat(e.message)) {
      // do nothing
    }
  }
}
