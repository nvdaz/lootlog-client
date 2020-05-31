package xyz.nvda.lootlog.listeners;

import com.google.common.collect.ImmutableSet;
import xyz.nvda.lootlog.network.ItemCollectHandler;
import java.util.Optional;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;

public class ClientConnectionHandler {

  private static final EventBus EVENT_BUS = MinecraftForge.EVENT_BUS;
  private static final Minecraft minecraft = Minecraft.getMinecraft();

  private final Set<Object> listeners =
      ImmutableSet.of(
          new DrawScreenEventHandler(),
          new ChatReceivedHandler(),
          new EntityJoinEvent(),
          new RenderWorldLastCheck(),
          new PlayerTickHandler(),
          new RenderOverlayHandler());
  private boolean isConnectedToHypixel = false;

  @SubscribeEvent
  public void onClientConnected(ClientConnectedToServerEvent event) {
    Optional<String> serverIP =
        Optional.of(minecraft).map(Minecraft::getCurrentServerData).map(data -> data.serverIP);
    this.isConnectedToHypixel = serverIP.isPresent() && serverIP.get().endsWith("hypixel.net");
    if (this.isConnectedToHypixel) {
      event
          .manager
          .channel()
          .pipeline()
          .addAfter("fml:packet_handler", "lootlog_item_collect_handler", new ItemCollectHandler());
      this.listeners.forEach(EVENT_BUS::register);
    }
  }

  @SubscribeEvent
  public void onClientDisconnected(ClientDisconnectionFromServerEvent event) {
    if (this.isConnectedToHypixel) {
      this.isConnectedToHypixel = false;
      this.listeners.forEach(EVENT_BUS::unregister);
    }
  }
}
