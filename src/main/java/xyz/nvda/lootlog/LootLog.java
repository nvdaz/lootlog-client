package xyz.nvda.lootlog;

import xyz.nvda.lootlog.IBossHandler.LoadProvidersResult;
import xyz.nvda.lootlog.handlers.ClientConnectionHandler;
import xyz.nvda.lootlog.notifications.NotificationProxyHandler;
import java.util.Arrays;
import java.util.List;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = LootLog.MODID, version = LootLog.VERSION)
public class LootLog {

  public static final String MODID = "lootlog";
  public static final String VERSION = "__VERSION__";

  public static final List<BossHandler<?, ?, ?, ?, ?>> bossHandlers =
      Arrays.asList(
          new BossHandler<?, ?, ?, ?, ?>[] {
            DragonHandler.getInstance(), SlayerHandler.getInstance(), GolemHandler.getInstance()
          });

  @EventHandler
  public void init(FMLInitializationEvent event) {
    System.out.println("Initializing " + MODID + " " + VERSION);

    MinecraftForge.EVENT_BUS.register(new ClientConnectionHandler());
    ClientCommandHandler.instance.registerCommand(new Command());

    ApolloProvider.getInstance()
        .refreshAccessToken(
            (refreshTokenResult) -> {
              System.out.println(refreshTokenResult.getDescription());
              bossHandlers.forEach(
                  bossHandler ->
                      bossHandler.loadProviders(
                          result ->
                              System.out.println(
                                  (result == LoadProvidersResult.SUCCESS
                                          ? "Loaded "
                                          : "Could not load ")
                                      + bossHandler.getClass().getName()
                                      + " item providers")));
            });

    NotificationProxyHandler.getInstance();
  }
}
