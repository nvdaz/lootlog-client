package xyz.nvda.lootlog.handlers;

import xyz.nvda.lootlog.ConfigurationHandler;
import xyz.nvda.lootlog.LocationHandler;
import xyz.nvda.lootlog.hud.HUD;
import xyz.nvda.lootlog.hud.HUDLocationScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class RenderOverlayHandler {

  private static final Minecraft minecraft = Minecraft.getMinecraft();
  private static final ConfigurationHandler configurationHandler =
      ConfigurationHandler.getInstance();

  private HUD lastHUD;

  @SubscribeEvent
  public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
    if (event.type != ElementType.ALL
        || minecraft.gameSettings.showDebugInfo
        || minecraft.currentScreen instanceof HUDLocationScreen
        || !configurationHandler.isHUDEnabled()) return;
    HUD hud = LocationHandler.getInstance().getLocation().associatedHUD;
    if (hud == null) return;
    if (lastHUD != hud) hud.reload();
    hud.render();
    lastHUD = hud;
  }
}
