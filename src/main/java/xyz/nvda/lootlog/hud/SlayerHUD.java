package xyz.nvda.lootlog.hud;

import xyz.nvda.lootlog.ConfigurationHandler;
import xyz.nvda.lootlog.SlayerHandler;
import xyz.nvda.lootlog.api.NotableSlayersQuery.NotableSlayer;
import xyz.nvda.lootlog.util.NumberUtil;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

public abstract class SlayerHUD extends HUD {
  protected static final SlayerHandler slayerHandler = SlayerHandler.getInstance();

  private int ticks = REFRESH_TICKS;

  protected SlayerHUD() {}

  protected abstract void loadHUDData();

  private Size render(List<NotableSlayer> slayers, int trackedXP) {
    Anchor anchor = ConfigurationHandler.getInstance().getHUDAnchor();
    Size size = new Size();

    String trackedString = "Tracked XP: " + NumberUtil.formatNumberCommas(trackedXP);

    fontRenderer.drawStringWithShadow(trackedString, anchor.getX(), anchor.getY(), 0x6391a6);

    size.addHeight(fontRenderer.FONT_HEIGHT + 2);
    size.maxWidth(fontRenderer.getStringWidth(trackedString));

    if (slayers.size() > 0) {
      String title = "Recent Drops";

      fontRenderer.drawStringWithShadow(
          title, anchor.getX(), anchor.getY() + size.getHeight(), 0x6391a6);

      size.addHeight(fontRenderer.FONT_HEIGHT + 2);
      size.maxWidth(fontRenderer.getStringWidth(title));
    }

    GlStateManager.pushMatrix();
    for (NotableSlayer slayer : slayers) {
      List<ItemStack> items =
          slayer.rewards().stream()
              .map(reward -> slayerHandler.getItemStack(reward.reward(), reward.count()))
              .collect(Collectors.toList());
      int xOffset = SPACE_WIDTH;

      for (ItemStack itemStack : items) {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderHelper.enableGUIStandardItemLighting();
        renderItem.renderItemAndEffectIntoGUI(
            itemStack, anchor.getX() + xOffset, anchor.getY() + size.getHeight());
        renderItem.renderItemOverlays(
            fontRenderer, itemStack, anchor.getX() + xOffset, anchor.getY() + size.getHeight());
        GlStateManager.disableLighting();
        GlStateManager.disableBlend();
        GlStateManager.color(1F, 1F, 1F, 1F);
        xOffset += 16;
      }

      String deltaString = " (" + NumberUtil.formatNumberShort(slayer.gross()) + ")";
      fontRenderer.drawStringWithShadow(
          deltaString, anchor.getX() + xOffset, anchor.getY() + size.getHeight(), 0x6391a6);
      xOffset += fontRenderer.getStringWidth(deltaString);

      size.addHeight(Math.max(16, fontRenderer.FONT_HEIGHT + 2));
      size.maxWidth(xOffset);
    }
    GlStateManager.popMatrix();

    return size;
  }

  public void render() {
    if (this.ticks >= REFRESH_TICKS) {
      this.ticks = 0;
      this.loadHUDData();
    }
    ticks++;
    this.render(slayerHandler.slayers(), slayerHandler.trackedXP());
  }

  public Size renderPlaceholder() {
    return null;
  }
}
