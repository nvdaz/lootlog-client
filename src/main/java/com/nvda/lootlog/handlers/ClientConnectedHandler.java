package com.nvda.lootlog.handlers;

import com.nvda.lootlog.network.ItemCollectHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;

public class ClientConnectedHandler {

  @SubscribeEvent
  public void onClientConnected(ClientConnectedToServerEvent event) {
    event
        .manager
        .channel()
        .pipeline()
        .addAfter("fml:packet_handler", "lootlog_item_collect_handler", new ItemCollectHandler());
  }
}
