package com.nvda.lootlog.util;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public class DelayedTask {

  private final Runnable runnable;
  private int counter;

  public DelayedTask(Runnable runnable, int ticks) {
    counter = ticks;
    this.runnable = runnable;
    MinecraftForge.EVENT_BUS.register(this);
  }

  public void cancel() {
    this.counter = Integer.MAX_VALUE;
    MinecraftForge.EVENT_BUS.register(this);
  }

  @SubscribeEvent
  public void onTick(TickEvent.ClientTickEvent event) {
    if (event.phase != Phase.START) return;

    if (counter <= 0) {
      MinecraftForge.EVENT_BUS.unregister(this);
      runnable.run();
    }

    counter--;
  }
}
