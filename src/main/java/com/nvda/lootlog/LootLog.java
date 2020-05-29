package com.nvda.lootlog;

import com.nvda.lootlog.IBossHandler.LoadProvidersResult;
import com.nvda.lootlog.handlers.ArmorStandRewardHandler;
import com.nvda.lootlog.handlers.ChatReceivedHandler;
import com.nvda.lootlog.handlers.ClientConnectedHandler;
import com.nvda.lootlog.handlers.DrawScreenEventHandler;
import com.nvda.lootlog.handlers.EntityJoinEvent;
import com.nvda.lootlog.handlers.PlayerTickHandler;
import com.nvda.lootlog.handlers.RenderOverlayHandler;
import com.nvda.lootlog.handlers.RenderWorldLastCheck;
import com.nvda.lootlog.handlers.WorldLoadEvent;
import com.nvda.lootlog.notifications.NotificationProxyHandler;
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

    MinecraftForge.EVENT_BUS.register(WorldLoadEvent.getInstance());
    MinecraftForge.EVENT_BUS.register(new DrawScreenEventHandler());
    MinecraftForge.EVENT_BUS.register(new ChatReceivedHandler());
    MinecraftForge.EVENT_BUS.register(new EntityJoinEvent());
    MinecraftForge.EVENT_BUS.register(new RenderWorldLastCheck());
    MinecraftForge.EVENT_BUS.register(new PlayerTickHandler());
    MinecraftForge.EVENT_BUS.register(new RenderOverlayHandler());
    MinecraftForge.EVENT_BUS.register(new ClientConnectedHandler());
    MinecraftForge.EVENT_BUS.register(new ArmorStandRewardHandler());
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
