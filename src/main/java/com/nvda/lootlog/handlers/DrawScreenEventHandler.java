package com.nvda.lootlog.handlers;

import com.nvda.lootlog.ConfigurationHandler;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenBook;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DrawScreenEventHandler {
  private static final Pattern NICK_REGEX =
      Pattern.compile("nicked as (\\[(VIP|MVP)\\+?\\+?] )?(\\w{3,16}).");

  private static final Field currPageField =
      ReflectionHelper.findField(GuiScreenBook.class, "currPage", "field_146484_x");
  private static final Field bookPagesField =
      ReflectionHelper.findField(GuiScreenBook.class, "bookPages", "field_146483_y");
  private static final ConfigurationHandler config = ConfigurationHandler.getInstance();

  public DrawScreenEventHandler() {
    currPageField.setAccessible(true);
    bookPagesField.setAccessible(true);
  }

  @SubscribeEvent
  public void bookCheck(GuiScreenEvent.DrawScreenEvent e) {
    GuiScreen currentScreen = e.gui;
    if (!(currentScreen instanceof GuiScreenBook)) return;

    try {
      NBTTagList bookPages = (NBTTagList) bookPagesField.get(currentScreen);
      int currPage = (int) currPageField.get(currentScreen);

      if (currPage >= bookPages.tagCount()) return;

      IChatComponent componentPage =
          IChatComponent.Serializer.jsonToComponent(bookPages.getStringTagAt(currPage));
      if (componentPage == null) return;

      String text = StringUtils.stripControlCodes(componentPage.getUnformattedText());
      Matcher nickMatcher = NICK_REGEX.matcher(text);

      if (!nickMatcher.find()) return;

      String nick = nickMatcher.group(3).trim();
      config.setNickname(nick);
    } catch (IllegalAccessException ex) {
      ex.printStackTrace();
    }
  }
}
