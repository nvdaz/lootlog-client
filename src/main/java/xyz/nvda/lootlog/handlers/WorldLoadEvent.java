package xyz.nvda.lootlog.handlers;

import xyz.nvda.lootlog.DragonHandler;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class WorldLoadEvent {

  private static final DragonHandler dragonHandler = DragonHandler.getInstance();
  private static final WorldLoadEvent instance = new WorldLoadEvent();

  public long lastWorldLoad;

  public static WorldLoadEvent getInstance() {
    return instance;
  }

  private WorldLoadEvent() {}

  @SubscribeEvent
  public void onWorldLoad(WorldEvent.Load e) {
    dragonHandler.flush();
    this.lastWorldLoad = System.currentTimeMillis();
  }
}