package com.nvda.lootlog.handlers;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityItem;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;

public class ArmorStandRewardHandler {

  private final List<Integer> trackedEntities = new ArrayList<>();

  @SubscribeEvent
  public void onTick(PlayerTickEvent event) {
    Minecraft.getMinecraft().theWorld.getEntities(EntityItem.class, p -> true).stream()
        .filter(entity -> trackedEntities.contains(entity.getEntityId()))
        .forEach(entity -> System.out.println(entity.getEntityId() + "=" + entity.getEntityItem()));
  }
}
