package com.nvda.lootlog;

import com.apollographql.apollo.ApolloCall.Callback;
import com.apollographql.apollo.api.Mutation;
import com.apollographql.apollo.api.Operation;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.api.cache.http.HttpCachePolicy;
import com.apollographql.apollo.api.cache.http.HttpCachePolicy.ExpirePolicy;
import com.apollographql.apollo.exception.ApolloException;
import com.nvda.lootlog.IBossHandler.LoadProvidersResult;
import com.nvda.lootlog.api.ItemProvidersQuery;
import com.nvda.lootlog.api.ItemProvidersQuery.Data;
import com.nvda.lootlog.api.type.RewardTestMode;
import com.nvda.lootlog.handlers.WorldLoadEvent;
import com.nvda.lootlog.util.ItemUtil;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StringUtils;

public abstract class BossHandler<
        D extends Operation.Data,
        V extends Operation.Variables,
        M extends Mutation<D, D, V>,
        R extends BossHandler<?, ?, ?, ?, T>.Reward<?>,
        T extends Enum<T>> {

  protected static final ApolloProvider apolloProvider = ApolloProvider.getInstance();
  protected static final ExpirePolicy itemProvidersCachePolicy =
      HttpCachePolicy.CACHE_FIRST.expireAfter(10L, TimeUnit.MINUTES);
  protected static final ItemProvidersQuery itemProvidersQuery = new ItemProvidersQuery();

  protected final List<ItemProvider> itemProviders = new ArrayList<>();

  protected final List<R> rewards;

  protected BossHandler(List<R> rewards) {
    this.rewards = rewards;
  }

  public abstract boolean testChat(IChatComponent chatComponent);

  public abstract void testItem(String itemName, int count);

  protected abstract Stream<ItemProvider> mapDataToItemProviders(ItemProvidersQuery.Data data);

  protected abstract M build();

  protected abstract void reset();

  protected abstract void handleAddMutation(D data);

  protected abstract R newReward(ItemProvider itemProvider);

  /** @return whether the item provider is new */
  protected boolean addReward(ItemProvider itemProvider, int count) {
    Optional<R> r =
        this.rewards.stream().filter(reward -> reward.type == itemProvider.rewardType).findFirst();

    if (r.isPresent()) {
      R reward = r.get();
      reward.count(reward.count + count);
      return false;
    }

    R reward = this.newReward(itemProvider);
    reward.count(count);
    this.rewards.add(reward);
    return true;
  }

  public void flush() {
    try {
      M mutation = this.build();
      this.reset();
      this.executeMutation(mutation, 0);
    } catch (IllegalStateException | NullPointerException ignored) {
    }
  }

  private void executeMutation(M mutation, int errorCount) {
    apolloProvider
        .getClient()
        .mutate(mutation)
        .enqueue(
            new Callback<D>() {
              @Override
              public void onResponse(@Nonnull Response<D> response) {
                D data = response.data();
                if (data != null) handleAddMutation(data);
              }

              @Override
              public void onFailure(@Nonnull ApolloException ex) {
                if (errorCount < 1)
                  apolloProvider.refreshAccessToken(
                      result -> {
                        if (result.isSuccess()) executeMutation(mutation, errorCount + 1);
                      });
                else {
                  ex.printStackTrace();
                  Message.FAILED_SUBMIT.send();
                }
              }
            });
  }

  protected Optional<ItemProvider> findItemProvider(Predicate<? super ItemProvider> predicate) {
    return this.itemProviders.stream().filter(predicate).findFirst();
  }

  protected boolean toIgnoreItems() {
    return System.currentTimeMillis() - WorldLoadEvent.getInstance().lastWorldLoad < 3000;
  }

  public ItemStack getItemStack(T rewardType, int count) {
    Optional<ItemProvider> itemProvider =
        itemProviders.stream().filter(provider -> provider.rewardType == rewardType).findFirst();
    if (itemProvider.isPresent()) {
      if (itemProvider.get().item == Items.skull && itemProvider.get().texture != null)
        return ItemUtil.createTexturedSkull(itemProvider.get().texture, count);
      else if (itemProvider.get().item != null)
        return new ItemStack(itemProvider.get().item, count, itemProvider.get().metadata);
    }

    return new ItemStack(Item.getItemFromBlock(Blocks.barrier), count);
  }

  public void loadProviders(Consumer<LoadProvidersResult> consumer) {
    this.loadProviders(consumer, 0);
  }

  protected void loadProviders(Consumer<LoadProvidersResult> consumer, int errorCount) {
    apolloProvider
        .getClient()
        .query(itemProvidersQuery)
        .httpCachePolicy(itemProvidersCachePolicy)
        .enqueue(
            new Callback<Data>() {
              @Override
              public void onResponse(@Nonnull Response<Data> response) {
                if (response.data() == null) {
                  if (errorCount < 1) loadProviders(consumer, errorCount + 1);
                  else consumer.accept(LoadProvidersResult.FAILURE);
                  return;
                }
                itemProviders.clear();

                mapDataToItemProviders(response.data()).forEach(itemProviders::add);
                consumer.accept(LoadProvidersResult.SUCCESS);
              }

              @Override
              public void onFailure(@Nonnull ApolloException ex) {
                ex.printStackTrace();
                if (errorCount < 1) loadProviders(consumer, errorCount + 1);
                else consumer.accept(LoadProvidersResult.FAILURE);
              }
            });
  }

  interface GeneratedItemProvider<RewardType> {
    Pattern test();

    RewardTestMode mode();

    RewardType item();

    String minecraftItem();

    String texture();

    int metadata();
  }

  static class BossItemProvider<T> {
    final Pattern test;
    final RewardTestMode mode;
    final T rewardType;
    final Item item;
    final String texture;
    final int metadata;

    @SuppressWarnings("unchecked")
    public <P> BossItemProvider(P generatedItemProvider) {
      this(
          (GeneratedItemProvider<T>)
              Proxy.newProxyInstance(
                  GeneratedItemProvider.class.getClassLoader(),
                  new Class[] {GeneratedItemProvider.class},
                  (proxy, method, args) -> {
                    try {
                      Class<?> proxiedClass = generatedItemProvider.getClass();
                      switch (method.getName()) {
                        case "test":
                          return proxiedClass
                              .getDeclaredMethod("test")
                              .invoke(generatedItemProvider);
                        case "mode":
                          return proxiedClass
                              .getDeclaredMethod("mode")
                              .invoke(generatedItemProvider);
                        case "item":
                          return proxiedClass
                              .getDeclaredMethod("item")
                              .invoke(generatedItemProvider);
                        case "minecraftItem":
                          return proxiedClass
                              .getDeclaredMethod("minecraftItem")
                              .invoke(generatedItemProvider);
                        case "texture":
                          return proxiedClass
                              .getDeclaredMethod("texture")
                              .invoke(generatedItemProvider);
                        case "metadata":
                          return Optional.ofNullable(
                                  proxiedClass
                                      .getDeclaredMethod("metadata")
                                      .invoke(generatedItemProvider))
                              .orElse(0);
                      }
                    } catch (Exception ex) {
                      ex.printStackTrace();
                    }
                    return null;
                  }));
    }

    public BossItemProvider(GeneratedItemProvider<T> itemProvider) {
      this.test = itemProvider.test();
      this.mode = itemProvider.mode();
      this.rewardType = itemProvider.item();
      this.item = ItemUtil.getItem(itemProvider.minecraftItem());
      this.texture = itemProvider.texture();
      this.metadata = itemProvider.metadata();
    }

    public boolean testChatMessage(IChatComponent chatComponent) {
      return this.mode == RewardTestMode.CHAT_FORMATTED
          ? this.test.matcher(chatComponent.getFormattedText()).matches()
          : this.mode == RewardTestMode.CHAT_UNFORMATTED
              && this.test
                  .matcher(StringUtils.stripControlCodes(chatComponent.getUnformattedText()))
                  .matches();
    }

    public boolean testItemName(String itemName) {
      return (this.mode == RewardTestMode.ITEM_DELTA || this.mode == RewardTestMode.ITEM_PACKET)
          && this.test.matcher(itemName).matches();
    }
  }

  abstract static class BossReward<I, T, P extends BossItemProvider<T>> {
    protected final T type;
    protected int count = 1;

    BossReward(P itemProvider) {
      this.type = itemProvider.rewardType;
    }

    void count(int count) {
      this.count = count;
    }

    boolean matches() {
      return this.count > 0;
    }

    abstract I toRewardInput();
  }

  protected class ItemProvider extends BossItemProvider<T> {
    public <P> ItemProvider(P generatedItemProvider) {
      super(generatedItemProvider);
    }
  }

  protected abstract class Reward<I> extends BossReward<I, T, ItemProvider> {
    Reward(ItemProvider itemProvider) {
      super(itemProvider);
    }
  }
}
