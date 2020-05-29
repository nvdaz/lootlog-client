package com.nvda.lootlog.handlers;

import com.nvda.lootlog.ConfigurationHandler;
import com.nvda.lootlog.Message;
import java.lang.reflect.Field;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class RenderWorldLastCheck {

  private static final Field renderPosXField =
      ReflectionHelper.findField(RenderManager.class, "renderPosX", "field_78725_b");
  private static final Field renderPosYField =
      ReflectionHelper.findField(RenderManager.class, "renderPosY", "field_78726_c");
  private static final Field renderPosZField =
      ReflectionHelper.findField(RenderManager.class, "renderPosZ", "field_78723_d");
  private static final ConfigurationHandler config = ConfigurationHandler.getInstance();

  public RenderWorldLastCheck() {
    renderPosXField.setAccessible(true);
    renderPosYField.setAccessible(true);
    renderPosZField.setAccessible(true);
  }

  @SubscribeEvent
  public void onRenderWorldLast(RenderWorldLastEvent e) {
    if (!config.isBoundingEnabled()) return;

    List<EntityDragon> dragons =
        Minecraft.getMinecraft().theWorld.getEntities(EntityDragon.class, p -> true);

    for (EntityDragon dragon : dragons) {
      double lastTickPosX = dragon.ticksExisted == 0 ? dragon.posX : dragon.lastTickPosX;
      double lastTickPosY = dragon.ticksExisted == 0 ? dragon.posY : dragon.lastTickPosY;
      double lastTickPosZ = dragon.ticksExisted == 0 ? dragon.posZ : dragon.lastTickPosZ;

      double fX, fY, fZ;

      try {
        fX =
            lastTickPosX
                + ((dragon.posX - dragon.lastTickPosX) * e.partialTicks)
                - (double) renderPosXField.get(Minecraft.getMinecraft().getRenderManager())
                - dragon.posX;
        fY =
            lastTickPosY
                + ((dragon.posY - dragon.lastTickPosY) * e.partialTicks)
                - (double) renderPosYField.get(Minecraft.getMinecraft().getRenderManager())
                - dragon.posY;
        fZ =
            lastTickPosZ
                + ((dragon.posZ - dragon.lastTickPosZ) * e.partialTicks)
                - (double) renderPosZField.get(Minecraft.getMinecraft().getRenderManager())
                - dragon.posZ;
      } catch (IllegalAccessException ex) {
        ex.printStackTrace();
        config.setBoundingEnabled(false);
        Message.FAILED_BOUNDING.send();
        return;
      }

      AxisAlignedBB axisAlignedBB = dragon.getEntityBoundingBox();

      GlStateManager.pushMatrix();
      GlStateManager.disableTexture2D();
      GlStateManager.translate(fX, fY, fZ);
      GlStateManager.disableLighting();
      GlStateManager.disableCull();
      RenderGlobal.drawOutlinedBoundingBox(axisAlignedBB, 255, 255, 255, 255);
      GlStateManager.enableLighting();
      GlStateManager.enableCull();
      GlStateManager.enableTexture2D();
      GlStateManager.popMatrix();
    }
  }
}
