package com.nvda.lootlog;

import com.nvda.lootlog.notifications.INotificationProxy;
import com.nvda.lootlog.notifications.NotificationProxyHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

public enum Message {
  INIT_SUCCESS("Connected to LootLog services.", "LootLog initialized"),
  INIT_FAILURE(
      EnumChatFormatting.RED + "Failed to connect to LootLog. Try /lootlog refresh.",
      "LootLog failure\nTry /lootlog refresh"),
  FAILED_PROVIDERS(
      EnumChatFormatting.RED + "Failed to load %1$s providers. Items will not register without this."),
  SUCCESS_PROVIDERS("Loaded %1$s providers."),
  FAILED_BOUNDING(
      EnumChatFormatting.RED
          + "Failed to access required fields in order to draw bounding box. Auto shut-off.",
      "LootLog failed\ndrawing bounds"),
  FAILED_SUBMIT(EnumChatFormatting.RED + "Failed to submit. Try checking your logs."),
  PLACE_EYE("Registered Summoning Eye placement.", "Registered\nSummoning Eye placement"),
  RETRIEVE_EYE("Registered Summoning Eye retrieval.", "Registered\nSummoning Eye retrieval"),
  DRAGON_SPAWN("Registered %1$s Dragon.", "Registered\n%1$s Dragon"),
  LEADERBOARD("Registered leaderboard placement #%1$s.", "Registered\nPlacement #%1$d"),
  ITEM_ACQUIRED("Registered item %1$s.", "Registered\nitem %1$s"),
  DRAGON_POSTED("Posted %1$s Dragon netting %2$d coins.", "Posted %1$s Dragon\nnetting %2$d"),
  MOD_OUTDATED("Your version of LootLog is outdated. To update and view the changelog visit %1$s"),
  DESCRIBE_COMMAND("/lootlog %1$s - %1$s"),
  UNKNOWN_ARGUMENTS(
      EnumChatFormatting.RED + "Unknown arguments. Use /lootlog help to display all sub-commands."),
  INVALID_ARGUMENTS(EnumChatFormatting.RED + "Invalid arguments. Usage: %1$s."),
  REFRESHING("Initiating %1$s refresh."),
  DEBUG_MODE_TOGGLE("Debug mode is now %1$s."),
  BOUNDING_BOX_TOGGLE("Bounding boxes are now %1$s."),
  UPDATED_NICKNAME("Updated your nickname to %1$s."),
  START_SLAYER("Registered %1$s slayer.", "Registered\n%1$s Slayer"),
  END_SLAYER("Registered slayer down.", "Registered\nslayer down"),
  SLAYER_POSTED("Posted %1$s Slayer netting %2$d coins.", "Posted %1$s Slayer\nnetting %2$d"),
  GOLEM_SPAWNED("Registered Golem.", "Registered\n%1$sGolem"),
  GOLEM_POSTED("Posted Golem netting %1$d coins.", "Posted Golem\nnetting %1$d"),
  ;

  private static final INotificationProxy notificationProxy =
      NotificationProxyHandler.getInstance();
  private static final IChatComponent PREFIX =
      new ChatComponentText(
          EnumChatFormatting.WHITE
              + "["
              + EnumChatFormatting.BLUE
              + "LootLog"
              + EnumChatFormatting.WHITE
              + "] ");

  private final String message;
  private String notification;

  Message(String message) {
    this.message = message;
  }

  Message(String message, String notification) {
    this.message = message;
    this.notification = notification;
  }

  public void send(Object... args) {
    if (this.notification != null && notificationProxy.isActive()) this.sendNotification(args);
    else this.sendChatMessage(args);
  }

  public void debug(Object... args) {
    if (ConfigurationHandler.getInstance().isDebugMode()) this.send(args);
  }

  public void sendNotification(Object... args) {
    if (this.notification != null) {
      String[] split = String.format(this.notification, args).split("\n");
      String ln1 = split[0];
      String ln2 = split.length == 1 ? "" : split[1];
      notificationProxy.addNotification(ln1, ln2, 5000);
    }
  }

  public void sendChatMessage(Object... args) {
    if (Minecraft.getMinecraft().thePlayer != null)
      Minecraft.getMinecraft()
          .thePlayer
          .addChatMessage(
              PREFIX
                  .createCopy()
                  .appendSibling(new ChatComponentText(String.format(this.message, args))));
  }
}
