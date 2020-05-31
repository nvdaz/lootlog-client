package xyz.nvda.lootlog.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;

public class ItemUtil {

  public static ItemStack createTexturedSkull(String texture, int count) {
    NBTTagCompound textureTag = new NBTTagCompound();
    textureTag.setString("Value", texture);

    NBTTagList texturesList = new NBTTagList();
    texturesList.appendTag(textureTag);

    NBTTagCompound propertiesTag = new NBTTagCompound();
    propertiesTag.setTag("textures", texturesList);

    NBTTagCompound skullOwnerTag = new NBTTagCompound();
    skullOwnerTag.setTag("Properties", propertiesTag);
    skullOwnerTag.setString("Id", UUID.randomUUID().toString());

    NBTTagCompound tag = new NBTTagCompound();
    tag.setTag("SkullOwner", skullOwnerTag);

    ItemStack item = new ItemStack(Items.skull, count, 3);
    item.setTagCompound(tag);

    return item;
  }

  public static Item getItem(String name) {
    return Item.itemRegistry.getObject(new ResourceLocation(name));
  }

  public static String id(ItemStack itemStack) {
    if (itemStack == null) return null;

    String id = itemStack.getDisplayName();

    NBTTagCompound extras =
        Optional.of(itemStack)
            .map(ItemStack::serializeNBT)
            .map((nbt) -> nbt.getCompoundTag("tag"))
            .map((tag) -> tag.getCompoundTag("ExtraAttributes"))
            .orElse(null);

    if (extras == null) return id;

    if (extras.hasKey("id")) id = extras.getString("id");

    if (id.equals("PET") && extras.hasKey("petInfo")) {
      JsonObject petInfo = new JsonParser().parse(extras.getString("petInfo")).getAsJsonObject();
      id = petInfo.get("tier").getAsString() + "_" + petInfo.get("type").getAsString() + "_PET";
    }

    if (id.equals("ENCHANTED_BOOK") && extras.hasKey("enchantments")) {
      NBTTagCompound enchantments = extras.getCompoundTag("enchantments");
      if (enchantments.getKeySet().size() != 1) return null;
      else {
        String enchantment = enchantments.getKeySet().iterator().next();
        id =
            enchantment.toUpperCase()
                + "_"
                + enchantments.getInteger(enchantment)
                + "_ENCHANTED_BOOK";
      }
    }

    if (id.equals("RUNE") && extras.hasKey("runes")) {
      NBTTagCompound runes = extras.getCompoundTag("runes");
      if (runes.getKeySet().size() != 1) return null;
      else {
        String rune = runes.getKeySet().iterator().next();
        id = rune + "_" + runes.getInteger(rune) + "_RUNE";
      }
    }

    return id;
  }

}
