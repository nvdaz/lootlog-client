package xyz.nvda.lootlog;

import xyz.nvda.lootlog.IBossHandler.LoadProvidersResult;
import xyz.nvda.lootlog.hud.DragonHUD;
import xyz.nvda.lootlog.hud.HUDLocationScreen;
import xyz.nvda.lootlog.util.DelayedTask;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;

public class Command extends CommandBase {

  public static final String commandName = "lootlog";
  public static final Map<String, String> subCommands =
      new HashMap<String, String>() {
        {
          put("refresh", "refresh loot log token");
          put("toggle", "toggle dragon logging");
          put("debug", "toggle debugging");
          put("bounds", "toggle dragon bounding boxes");
          put("nick", "update your nickname");
        }
      };
  private static final ConfigurationHandler config = ConfigurationHandler.getInstance();

  @Override
  public String getCommandName() {
    return commandName;
  }

  @Override
  public String getCommandUsage(ICommandSender sender) {
    return "/" + this.getCommandName();
  }

  @Override
  public boolean canCommandSenderUseCommand(ICommandSender sender) {
    return true;
  }

  @Override
  public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
    if (args.length == 1) {
      return new ArrayList<>(subCommands.keySet());
    }
    return new ArrayList<>();
  }

  @Override
  public void processCommand(ICommandSender sender, String[] args) {
    if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
      for (Map.Entry<String, String> entry : subCommands.entrySet())
        Message.DESCRIBE_COMMAND.send(entry.getKey(), entry.getValue());
    } else {
      if (args[0].equalsIgnoreCase("hud")) {
        new DelayedTask(
            () ->
                Minecraft.getMinecraft()
                    .displayGuiScreen(new HUDLocationScreen().setHUD(DragonHUD.getInstance())),
            1);
      } else if (args[0].equalsIgnoreCase("refresh")) {
        if (args.length == 1 || args[1].equalsIgnoreCase("token")) {
          Message.REFRESHING.send("access token");
          ApolloProvider.getInstance()
              .refreshAccessToken(
                  (result) ->
                      (result.isSuccess() ? Message.INIT_SUCCESS : Message.INIT_FAILURE).send());
        } else if (args[1].equalsIgnoreCase("providers")) {
          Message.REFRESHING.send("providers");
          LootLog.bossHandlers.forEach(
              bossHandler ->
                  bossHandler.loadProviders(
                      result ->
                          (result == LoadProvidersResult.SUCCESS
                                  ? Message.SUCCESS_PROVIDERS
                                  : Message.FAILED_PROVIDERS)
                              .send(bossHandler.getClass().getName())));
        }
      } else if (args[0].equalsIgnoreCase("debug")) {
        config.setDebugMode(!config.isDebugMode());
        Message.DEBUG_MODE_TOGGLE.send(config.isDebugMode() ? "enabled" : "disabled");
      } else if (args[0].equalsIgnoreCase("bounds")) {
        config.setBoundingEnabled(!config.isBoundingEnabled());
        Message.BOUNDING_BOX_TOGGLE.send(config.isBoundingEnabled() ? "enabled" : "disabled");
      } else if (args[0].equalsIgnoreCase("nick")) {
        if (args.length > 1) {
          config.setNickname(args[1]);
          Message.UPDATED_NICKNAME.send(config.getNickname());
        } else {
          Message.INVALID_ARGUMENTS.send("/lootlog nick <nick>");
        }
      } else {
        Message.UNKNOWN_ARGUMENTS.send();
      }
    }
  }
}
