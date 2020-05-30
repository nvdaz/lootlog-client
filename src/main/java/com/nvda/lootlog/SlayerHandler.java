package com.nvda.lootlog;

import com.apollographql.apollo.ApolloCall.Callback;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.api.cache.http.HttpCachePolicy;
import com.apollographql.apollo.exception.ApolloException;
import com.nvda.lootlog.api.AddSlayerMutation;
import com.nvda.lootlog.api.AddSlayerMutation.AddSlayer;
import com.nvda.lootlog.api.AddSlayerMutation.Data;
import com.nvda.lootlog.api.ItemProvidersQuery;
import com.nvda.lootlog.api.NotableSlayersQuery;
import com.nvda.lootlog.api.NotableSlayersQuery.CurrentUser;
import com.nvda.lootlog.api.NotableSlayersQuery.NotableSlayer;
import com.nvda.lootlog.api.type.SlayerRewardInput;
import com.nvda.lootlog.api.type.SlayerRewardType;
import com.nvda.lootlog.api.type.SlayerType;
import com.nvda.lootlog.util.DelayedTask;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import net.minecraft.util.IChatComponent;

public class SlayerHandler
    extends BossHandler<
        AddSlayerMutation.Data,
        AddSlayerMutation.Variables,
        AddSlayerMutation,
        SlayerRewardInput,
        SlayerRewardType> {
  private static final Pattern START_PATTERN =
      Pattern.compile(
          "^\u00A7r {3}\u00A75\u00A7l\u00BB \u00A77Slay \u00A7c([\\d,]+) Combat XP \u00A77worth of (Zombies|Spiders|Wolves)\u00A77\\.\u00A7r$");
  private static final Pattern END_PATTERN =
      Pattern.compile(
          "\u00A7r {3}\u00A7r\u00A75\u00A7l\u00BB \u00A7r\u00A77Talk to Maddox to claim your (Zombie|Spider|Wolf) Slayer XP!\u00A7r");

  private static final SlayerHandler instance = new SlayerHandler();

  private static final Map<Integer, Integer> slayerXpMap =
      new HashMap<Integer, Integer>() {
        {
          put(1, 5);
          put(2, 25);
          put(3, 100);
          put(4, 500);
        }
      };
  private final Map<SlayerType, NotableSlayersQuery> notableSlayersQueryMap =
      Arrays.stream(SlayerType.values())
          .collect(Collectors.toMap(type -> type, type -> new NotableSlayersQuery(type, 3)));
  private CurrentUser currentUser;
  private DelayedTask delayedTask;
  private SlayerType slayerType;
  private int tier;

  private SlayerHandler() {}

  public static SlayerHandler getInstance() {
    return instance;
  }

  public List<NotableSlayersQuery.NotableSlayer> slayers() {
    return new ArrayList<>(
        Optional.ofNullable(this.currentUser)
            .map(CurrentUser::notableSlayers)
            .orElse(new ArrayList<>()));
  }

  public int trackedXP() {
    return Optional.ofNullable(this.currentUser).map(CurrentUser::slayerXp).orElse(0);
  }

  protected AddSlayerMutation build() {
    return AddSlayerMutation.builder()
        .slayerType(this.slayerType)
        .tier(this.tier)
        .rewards(this.rewards.stream().map(BossReward::toRewardInput).collect(Collectors.toList()))
        .build();
  }

  protected void reset() {
    if (this.delayedTask != null) {
      this.delayedTask.cancel();
      this.delayedTask = null;
    }
    this.slayerType = null;
    this.rewards.clear();
  }

  public boolean testChat(IChatComponent chatComponent) {
    Matcher startMatcher = START_PATTERN.matcher(chatComponent.getFormattedText());
    if (startMatcher.matches()) {
      this.flush();
      int xp = Integer.parseInt(startMatcher.group(1).replaceAll(",", ""));
      switch (startMatcher.group(2).toUpperCase()) {
        case "ZOMBIES":
          this.slayerType = SlayerType.REVENANT;
          if (xp == 150) this.tier = 1;
          else if (xp == 1440) this.tier = 2;
          else if (xp == 2400) this.tier = 3;
          else if (xp == 4800) this.tier = 4;

          break;
        case "SPIDERS":
          this.slayerType = SlayerType.TARANTULA;
          if (xp == 250) this.tier = 1;
          else if (xp == 600) this.tier = 2;
          else if (xp == 1000) this.tier = 3;
          else if (xp == 4800) this.tier = 4;

          break;
        case "WOLVES":
          this.slayerType = SlayerType.WOLF;
          if (xp == 250) this.tier = 1;
          else if (xp == 600) this.tier = 2;
          else if (xp == 1500) this.tier = 3;
          else if (xp == 3000) this.tier = 4;

          break;
      }

      this.rewards.clear();
      Message.START_SLAYER.send(startMatcher.group(2));
      return true;
    }

    Matcher endMatcher = END_PATTERN.matcher(chatComponent.getFormattedText());
    if (endMatcher.matches()) {
      this.delayedTask =
          new DelayedTask(
              () -> {
                this.flush();
                this.delayedTask = null;
              },
              60);
      Message.END_SLAYER.debug();
    }

    Optional<ItemProvider> itemProvider =
        this.findItemProvider(provider -> provider.testChatMessage(chatComponent));
    if (itemProvider.isPresent()) {
      if (this.addReward(itemProvider.get(), 1))
        Message.ITEM_ACQUIRED.debug(itemProvider.get().rewardType.toString());
      return true;
    }

    return false;
  }

  public void testItem(String itemName, int count) {
    if (this.toIgnoreItems() || count <= 0) return;
    Optional<ItemProvider> itemProvider =
        this.findItemProvider(provider -> provider.testItemName(itemName));
    if (itemProvider.isPresent()) {
      if (this.addReward(itemProvider.get(), count))
        Message.ITEM_ACQUIRED.send(itemProvider.get().rewardType.toString());
    }
  }

  protected Stream<ItemProvider> mapDataToItemProviders(ItemProvidersQuery.Data data) {
    return data.slayerItemProviders().stream().map(ItemProvider::new);
  }

  protected void handleAddMutation(Data data) {
    AddSlayer slayer = data.addSlayer();
    Message.SLAYER_POSTED.send(slayer.slayerType(), slayer.gross());
    if (this.currentUser != null) {
      NotableSlayersQuery query = notableSlayersQueryMap.get(slayer.slayerType());
      List<NotableSlayer> newSlayers = new ArrayList<>(currentUser.notableSlayers());

      if (newSlayers.size() >= query.variables().limit()) {
        newSlayers.remove(newSlayers.size() - 1);
      }
      newSlayers.add(
          0,
          new NotableSlayer(
              slayer.__typename(),
              slayer._id(),
              slayer.slayerType(),
              slayer.gross(),
              slayer.rewards().stream()
                  .map(
                      reward ->
                          new NotableSlayersQuery.Reward(
                              reward.__typename(),
                              reward.reward(),
                              reward.count(),
                              reward.appraisal()))
                  .collect(Collectors.toList())));
      NotableSlayersQuery.Data newData =
          new NotableSlayersQuery.Data(
              new CurrentUser(
                  currentUser.__typename(),
                  currentUser._id(),
                  currentUser.slayerXp() + slayerXpMap.get(slayer.tier()),
                  newSlayers));
      try {
        apolloProvider.getClient().apolloStore().writeAndPublish(query, newData).execute();
        this.loadHUDData(slayer.slayerType());
      } catch (ApolloException ex) {
        ex.printStackTrace();
      }
    }
  }

  protected SlayerReward newReward(ItemProvider itemProvider) {
    return new SlayerReward(itemProvider);
  }

  public void loadHUDData(SlayerType slayerType) {
    this.loadHUDData(slayerType, 0);
  }

  private void loadHUDData(SlayerType slayerType, int errorCount) {
    NotableSlayersQuery query = this.notableSlayersQueryMap.get(slayerType);
    apolloProvider
        .getClient()
        .query(query)
        .httpCachePolicy(HttpCachePolicy.CACHE_FIRST)
        .watcher()
        .enqueueAndWatch(
            new Callback<NotableSlayersQuery.Data>() {
              @Override
              public void onResponse(@Nonnull Response<NotableSlayersQuery.Data> response) {
                currentUser =
                    Optional.of(response)
                        .map(Response::data)
                        .map(NotableSlayersQuery.Data::currentUser)
                        .orElse(null);
              }

              @Override
              public void onFailure(@Nonnull ApolloException ex) {
                ex.printStackTrace();
                if (errorCount < 1) loadHUDData(slayerType, errorCount + 1);
              }
            });
  }

  public class SlayerReward extends Reward {
    SlayerReward(ItemProvider itemProvider) {
      super(itemProvider);
    }

    SlayerRewardInput toRewardInput() {
      return SlayerRewardInput.builder().reward(this.type).count(this.count).build();
    }
  }
}
