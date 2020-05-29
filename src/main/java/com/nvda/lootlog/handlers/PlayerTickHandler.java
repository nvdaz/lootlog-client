package com.nvda.lootlog.handlers;

import com.nvda.lootlog.LocationHandler;
import com.nvda.lootlog.LocationHandler.Location;
import com.nvda.lootlog.LootLog;
import com.nvda.lootlog.util.ItemUtil;
import com.nvda.lootlog.util.ScoreboardUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;

public class PlayerTickHandler {

  private static final Pattern LOCATION_REGEX = Pattern.compile("\u23E3 ([\\w' ]+)");

//  private Map<NormalizedItem, Integer> last = null;

  @SubscribeEvent
  public void onPlayerTick(PlayerTickEvent event) {
    if (event.phase != Phase.START) return;

//    List<ItemStack> inventory =
//        new ArrayList<>(Arrays.asList(Minecraft.getMinecraft().thePlayer.inventory.mainInventory));
//    if (Minecraft.getMinecraft().thePlayer.inventory.getItemStack() != null)
//      inventory.add(Minecraft.getMinecraft().thePlayer.inventory.getItemStack());
//    Map<NormalizedItem, Integer> normalized = new HashMap<>();
//    for (ItemStack itemStack : inventory) {
//      NormalizedItem item = new NormalizedItem(itemStack);
//      if (item.id != null)
//        normalized.put(item, normalized.getOrDefault(item, 0) + itemStack.stackSize);
//    }
//    if (this.last != null) {
//      Set<NormalizedItem> keys = new HashSet<>();
//      keys.addAll(normalized.keySet());
//      keys.addAll(this.last.keySet());
//
//      Map<NormalizedItem, Integer> delta = new HashMap<>();
//
//      for (NormalizedItem item : keys) {
//        if (normalized.containsKey(item)
//            && !normalized.get(item).equals(this.last.get(item))
//            && item.getLastPickup() > System.currentTimeMillis() - 1000) {
//          delta.put(
//              item,
//              delta.getOrDefault(item, 0)
//                  + normalized.getOrDefault(item, 0)
//                  - this.last.getOrDefault(item, 0));
//        }
//      }
//
//      for (Entry<NormalizedItem, Integer> entry : delta.entrySet()) {
//        LootLog.bossHandlers.forEach(
//            handler -> handler.testItem(entry.getKey().id, entry.getValue()));
//      }
//    }
//    this.last = normalized;
    //    if (this.last.size() >= 1) {
    //      Map<String, Integer> delta = new HashMap<>();
    //      int index = 0;
    //
    //      for (ItemStack itemStack : inventory) {
    //        if (this.last.size() <= index) continue;
    //        ItemStack lastStack = this.last.get(index);
    //        if (itemStack == null) continue;
    //        String id = ItemUtil.id(itemStack);
    //
    //        if (id != null && !ItemStack.areItemStacksEqual(itemStack, lastStack))
    //          delta.put(
    //              id,
    //              itemStack.stackSize
    //                  - (lastStack != null ? lastStack.stackSize : 0)
    //                  + (delta.getOrDefault(id, 0)));
    //        index++;
    //      }
    //      for (String name : new HashSet<>(delta.keySet())) {
    //        if (delta.get(name) == 0) delta.remove(name);
    //      }
    //      for (String name : delta.keySet()) {
    //        int count = delta.get(name);
    //        DragonHandler.getInstance().testItem(name, count);
    //        SlayerHandler.getInstance().testItem(name, count);
    //        GolemHandler.getInstance().testItem(name, count);
    //      }
    //    }
    //    this.last = inventory;

    Optional<Matcher> matcher =
        Optional.ofNullable(ScoreboardUtils.getUnformattedLine(4)).map(LOCATION_REGEX::matcher);
    LocationHandler.getInstance()
        .setLocation(
            matcher.isPresent() && matcher.get().find()
                ? Location.fromScoreboardName(matcher.get().group(1))
                : Location.OTHER);
  }
}
