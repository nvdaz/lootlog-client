package xyz.nvda.lootlog.util;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public class DelayedTask {

  private final Runnable runnable;
  private int counter;

  public DelayedTask(Runnable runnable, int ticks) {
    this.counter = ticks;
    this.runnable = runnable;
    MinecraftForge.EVENT_BUS.register(this);
  }

  public void cancel() {
    this.counter = Integer.MAX_VALUE;
    MinecraftForge.EVENT_BUS.register(this);
  }

  @SubscribeEvent
  public void onTick(ClientTickEvent event) {
    if (event.phase != Phase.START) return;

    if (this.counter <= 0) {
      MinecraftForge.EVENT_BUS.unregister(this);
      runnable.run();
    }

    this.counter--;
  }
}
