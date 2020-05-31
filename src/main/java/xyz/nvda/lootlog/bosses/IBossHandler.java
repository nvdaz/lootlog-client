package xyz.nvda.lootlog.bosses;

import java.util.function.Consumer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IChatComponent;

public interface IBossHandler<T extends Enum<T>> {
  boolean testChat(IChatComponent chatComponent);

  void testItem(String item, int count);

  void flush();

  ItemStack getItemStack(T rewardType, int count);

  void loadProviders(Consumer<LoadProvidersResult> consumer);

  enum LoadProvidersResult {
    SUCCESS,
    FAILURE
  }
}
