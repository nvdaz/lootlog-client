package xyz.nvda.lootlog.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.RenderItem;

public abstract class HUD {
  protected static final int REFRESH_TICKS = 20 * 60 * 10;
  protected static final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
  protected static final RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
  protected static final int SPACE_WIDTH = fontRenderer.getStringWidth(" ");

  protected int ticks = REFRESH_TICKS;

  public abstract void render();

  public abstract Size renderPlaceholder();

  public void reload() {
    this.ticks = REFRESH_TICKS;
  }
}
