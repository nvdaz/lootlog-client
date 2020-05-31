package xyz.nvda.lootlog.hud;

import xyz.nvda.lootlog.ConfigurationHandler;
import net.minecraft.client.gui.GuiScreen;

public class HUDLocationScreen extends GuiScreen {

  private HUD hud;

  private Size size;
  private int startX = -1;
  private int startY = -1;

  public HUDLocationScreen setHUD(HUD hud) {
    this.hud = hud;
    return this;
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    super.drawDefaultBackground();
    if (this.hud != null) this.size = this.hud.renderPlaceholder();
  }

  @Override
  protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
    if (this.size != null
        && this.size.includes(ConfigurationHandler.getInstance().getHUDAnchor(), mouseX, mouseY)) {
      this.startX = mouseX;
      this.startY = mouseY;
    } else {
      this.startX = -1;
      this.startY = -1;
    }
  }

  @Override
  protected void mouseClickMove(
      int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
    if (this.startX >= 0 && this.startY >= 0) {
      int deltaX = mouseX - startX;
      int deltaY = mouseY - startY;
      this.startX = mouseX;
      this.startY = mouseY;
      Anchor newAnchor = ConfigurationHandler.getInstance().getHUDAnchor().offset(deltaX, deltaY);
      ConfigurationHandler.getInstance().setAnchor(newAnchor);
    }
  }
}
