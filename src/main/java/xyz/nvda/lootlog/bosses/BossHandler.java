package xyz.nvda.lootlog.bosses;

import com.apollographql.apollo.ApolloCall.Callback;
import com.apollographql.apollo.api.Mutation;
import com.apollographql.apollo.api.Operation;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.api.cache.http.HttpCachePolicy;
import com.apollographql.apollo.api.cache.http.HttpCachePolicy.ExpirePolicy;
import com.apollographql.apollo.exception.ApolloException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StringUtils;
import xyz.nvda.lootlog.ApolloProvider;
import xyz.nvda.lootlog.Message;
import xyz.nvda.lootlog.api.ItemProvidersQuery;
import xyz.nvda.lootlog.api.type.RewardTestMode;
import xyz.nvda.lootlog.util.ItemUtil;

public abstract class BossHandler<
        D extends Operation.Data,
        V extends Operation.Variables,
        M extends Mutation<D, D, V>,
        I,
        T extends Enum<T>>
    implements IBossHandler<T> {

  protected static final ApolloProvider apolloProvider = ApolloProvider.getInstance();
  protected static final ExpirePolicy itemProvidersCachePolicy =
      HttpCachePolicy.CACHE_FIRST.expireAfter(10L, TimeUnit.MINUTES);
  protected static final ItemProvidersQuery itemProvidersQuery = new ItemProvidersQuery();

  protected final List<ItemProvider> itemProviders = new ArrayList<>();

  protected final List<Reward> rewards = new ArrayList<>();

  //  protected BossHandler(List<R> rewards) {
  //    this.rewards = rewards;
  //  }

  public abstract boolean testChat(IChatComponent chatComponent);

  public abstract void testItem(String itemName, int count);

  protected abstract List<ItemProvider> mapProviders(
      List<ItemProvidersQuery.ItemProvider> itemProviders);

  protected abstract M build();

  protected abstract void reset();

  protected abstract void handleAddMutation(D data);

  protected abstract Reward newReward(ItemProvider itemProvider);

  /** @return whether the item provider is new */
  protected boolean addReward(ItemProvider itemProvider, int count) {
    Optional<Reward> r =
        this.rewards.stream().filter(reward -> reward.type == itemProvider.rewardType).findFirst();

    if (r.isPresent()) {
      Reward reward = r.get();
      reward.count(reward.count + count);
      return false;
    }

    Reward reward = this.newReward(itemProvider);
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
            new Callback<ItemProvidersQuery.Data>() {
              @Override
              public void onResponse(@Nonnull Response<ItemProvidersQuery.Data> response) {
                Optional<List<ItemProvidersQuery.ItemProvider>> providers =
                    Optional.of(response)
                        .map(Response::data)
                        .map(ItemProvidersQuery.Data::itemProviders);

                if (providers.isPresent()) {
                  itemProviders.clear();
                  itemProviders.addAll(mapProviders(providers.get()));
                  consumer.accept(LoadProvidersResult.SUCCESS);
                } else if (errorCount < 1) loadProviders(consumer, errorCount + 1);
                else consumer.accept(LoadProvidersResult.FAILURE);
              }

              @Override
              public void onFailure(@Nonnull ApolloException ex) {}
            });
  }

  static class BossItemProvider<T> {
    final Pattern test;
    final RewardTestMode mode;
    final T rewardType;
    final Item item;
    final String texture;
    final int metadata;

    BossItemProvider(
        Pattern test,
        RewardTestMode mode,
        T rewardType,
        String minecraftItem,
        String texture,
        int metadata) {

      this.test = test;
      this.mode = mode;
      this.rewardType = rewardType;
      this.item = ItemUtil.getItem(minecraftItem);
      this.texture = texture;
      this.metadata = metadata;
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

    abstract I toRewardInput();
  }

  protected class ItemProvider extends BossItemProvider<T> {
    ItemProvider(
        Pattern test,
        RewardTestMode mode,
        T rewardType,
        String minecraftItem,
        String texture,
        int metadata) {
      super(test, mode, rewardType, minecraftItem, texture, metadata);
    }
  }

  protected abstract class Reward extends BossReward<I, T, ItemProvider> {
    Reward(ItemProvider itemProvider) {
      super(itemProvider);
    }
  }
}
