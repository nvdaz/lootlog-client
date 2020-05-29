package com.nvda.lootlog.network;

import com.nvda.lootlog.LootLog;
import com.nvda.lootlog.util.ItemUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S0DPacketCollectItem;
import net.minecraftforge.fml.client.FMLClientHandler;

public class ItemCollectHandler extends SimpleChannelInboundHandler<S0DPacketCollectItem> {

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, S0DPacketCollectItem packet) {
    WorldClient world = FMLClientHandler.instance().getWorldClient();
    Entity entity = world.getEntityByID(packet.getCollectedItemEntityID());
    if (entity instanceof EntityItem) {
      ItemStack itemStack = ((EntityItem) entity).getEntityItem();
      LootLog.bossHandlers.forEach(
          bossHandler -> bossHandler.testItem(ItemUtil.id(itemStack), itemStack.stackSize));
    }
  }
}
