package com.nvda.lootlog.handlers;

import com.nvda.lootlog.ApolloProvider;
import com.nvda.lootlog.Message;
import com.nvda.lootlog.UpdateChecker;
import com.nvda.lootlog.UpdateChecker.UpdateCheckResult.UpdateStatus;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EntityJoinEvent {

  private boolean stated = false;

  @SubscribeEvent
  public void onEntityJoin(EntityJoinWorldEvent e) {
    if (!this.stated) {
      this.stated = true;
      MinecraftForge.EVENT_BUS.unregister(this);
      (ApolloProvider.getInstance().hasToken() ? Message.INIT_SUCCESS : Message.INIT_FAILURE)
          .send();
      UpdateChecker.getUpdates(
          (result) -> {
            if (result.updateStatus != UpdateStatus.CURRENT)
              Message.MOD_OUTDATED.send(result.changelog);
          });
    }
  }
}
