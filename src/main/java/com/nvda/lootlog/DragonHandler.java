package com.nvda.lootlog;

import com.apollographql.apollo.ApolloCall.Callback;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.api.cache.http.HttpCachePolicy;
import com.apollographql.apollo.exception.ApolloException;
import com.nvda.lootlog.ChatPattern.MatchType;
import com.nvda.lootlog.Dragon.DragonOverview;
import com.nvda.lootlog.api.AddDragonMutation;
import com.nvda.lootlog.api.AddDragonMutation.AddDragon;
import com.nvda.lootlog.api.GetMyDragonsQuery;
import com.nvda.lootlog.api.GetMyDragonsQuery.CurrentUser;
import com.nvda.lootlog.api.GetMyDragonsQuery.Dragon;
import com.nvda.lootlog.api.ItemProvidersQuery;
import com.nvda.lootlog.api.type.DragonRewardInput;
import com.nvda.lootlog.api.type.DragonRewardType;
import com.nvda.lootlog.api.type.DragonType;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraft.util.IChatComponent;

public class DragonHandler
    extends BossHandler<
        AddDragonMutation.Data,
        AddDragonMutation.Variables,
        AddDragonMutation,
        DragonRewardInput,
        //        DragonReward,
        DragonRewardType> {

  private static final ChatPattern DRAGON_PATTERN =
      new ChatPattern(
          "^.? The (Superior|Strong|Wise|Unstable|Protector|Young|Old) Dragon has spawned!$",
          MatchType.UNFORMATTED);
  private static final ChatPattern PLACE_PATTERN =
      new ChatPattern(
          "^.? You placed a Summoning Eye! (Brace yourselves! )?\\(\\d\\/8\\)$",
          MatchType.UNFORMATTED);
  private static final ChatPattern PICKUP_PATTERN =
      new ChatPattern("^You recovered a Summoning Eye!$", MatchType.UNFORMATTED);
  private static final ChatPattern LEADERBOARD_PATTERN =
      new ChatPattern(
          "^ +Your Damage: [\\d,]+ (?:\\(NEW RECORD!\\) )?\\(Position #(\\d{1,2})\\)$",
          MatchType.UNFORMATTED);
  private static final ChatPattern EGG_PATTERN =
      new ChatPattern("^.? The Dragon Egg has spawned!$", MatchType.UNFORMATTED);

  private static final DragonHandler instance = new DragonHandler();
  private final GetMyDragonsQuery getMyDragonsQuery =
      new GetMyDragonsQuery(
          -(ZoneOffset.systemDefault().getRules().getOffset(Instant.now()).getTotalSeconds()) / 60,
          3);
  private CurrentUser currentUser;
  private DragonType dragonType;
  private int eyesPlaced = 0;
  private int day = 1;
  private int leaderboardPlacement = 1;

  private DragonHandler() {}

  public static DragonHandler getInstance() {
    return instance;
  }

  protected Reward newReward(ItemProvider itemProvider) {
    return new DragonReward(itemProvider);
  }

  public List<com.nvda.lootlog.Dragon> dragons() {
    return Optional.ofNullable(this.currentUser).map(CurrentUser::dragons).orElse(new ArrayList<>())
        .stream()
        .map(com.nvda.lootlog.Dragon::new)
        .collect(Collectors.toList());
  }

  public List<DragonOverview> dragonOverviews() {
    return Optional.ofNullable(this.currentUser).map(CurrentUser::dragonOverviews)
        .orElse(new ArrayList<>()).stream()
        .map(DragonOverview::new)
        .collect(Collectors.toList());
  }

  protected AddDragonMutation build() {
    return AddDragonMutation.builder()
        .dragonType(this.dragonType)
        .eyesPlaced(this.eyesPlaced)
        .rewards(this.rewards.stream().map(BossReward::toRewardInput).collect(Collectors.toList()))
        .day(this.day)
        .leaderboardPlacement(this.leaderboardPlacement)
        .build();
  }

  protected void reset() {
    this.dragonType = null;
    this.rewards.clear();
    this.eyesPlaced = 0;
  }

  public boolean testChat(IChatComponent chatComponent) {
    Matcher dragonTypeMatcher = DRAGON_PATTERN.matcher(chatComponent);
    if (dragonTypeMatcher.matches()) {
      this.dragonType = DragonType.valueOf(dragonTypeMatcher.group(1).toUpperCase());
      this.day = (int) Minecraft.getMinecraft().theWorld.getWorldTime() / 24000;
      Message.DRAGON_SPAWN.debug(dragonTypeMatcher.group(1));
      return true;
    }

    Matcher pickupMatcher = PICKUP_PATTERN.matcher(chatComponent);
    if (pickupMatcher.matches()) {
      this.eyesPlaced--;
      Message.RETRIEVE_EYE.debug();
      return true;
    }

    Matcher placeMatcher = PLACE_PATTERN.matcher(chatComponent);
    if (placeMatcher.matches()) {
      this.eyesPlaced++;
      Message.PLACE_EYE.debug();
      return true;
    }

    Matcher leaderboardMatcher = LEADERBOARD_PATTERN.matcher(chatComponent);
    if (leaderboardMatcher.matches()) {
      if (this.dragonType == null) return false;
      this.leaderboardPlacement = Integer.parseInt(leaderboardMatcher.group(1));
      Message.LEADERBOARD.debug(this.leaderboardPlacement);
      return true;
    }

    Matcher eggMatcher = EGG_PATTERN.matcher(chatComponent);
    if (eggMatcher.matches()) {
      this.flush();
      return true;
    }

    if (this.dragonType == null) return false;
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
    if (this.toIgnoreItems() || this.dragonType == null) return;
    Optional<ItemProvider> itemProvider =
        this.findItemProvider(provider -> provider.testItemName(itemName));
    if (itemProvider.isPresent()) {
      if (this.addReward(itemProvider.get(), count))
        Message.ITEM_ACQUIRED.debug(itemProvider.get().rewardType.toString());
    }
  }

  protected void handleAddMutation(AddDragonMutation.Data data) {
    AddDragon dragon = data.addDragon();
    Message.DRAGON_POSTED.send(dragon.dragonType(), dragon.gross());

    if (this.currentUser != null) {
      List<Dragon> newDragons = new ArrayList<>(currentUser.dragons());

      if (newDragons.size() >= getMyDragonsQuery.variables().limit())
        newDragons.remove(newDragons.size() - 1);
      newDragons.add(
          0, new Dragon(dragon.__typename(), dragon._id(), dragon.dragonType(), dragon.gross()));
      List<GetMyDragonsQuery.DragonOverview> newDragonOverviews =
          new ArrayList<>(currentUser.dragonOverviews());
      Optional<GetMyDragonsQuery.DragonOverview> overviewToday =
          newDragonOverviews.stream()
              .filter(
                  (dragonOverview ->
                      new DragonOverview(dragonOverview).date().isEqual(LocalDate.now())))
              .findFirst();

      if (overviewToday.isPresent()) {
        newDragonOverviews.remove(overviewToday.get());
        newDragonOverviews.add(
            new GetMyDragonsQuery.DragonOverview(
                overviewToday.get().__typename(),
                overviewToday.get()._id(),
                overviewToday.get().day(),
                overviewToday.get().gross() + dragon.gross()));
      }

      Optional<GetMyDragonsQuery.DragonOverview> overviewAllTime =
          newDragonOverviews.stream()
              .filter(
                  (dragonOverview) ->
                      new DragonOverview(dragonOverview)
                              .date()
                              .atStartOfDay()
                              .atZone(ZoneId.systemDefault())
                              .toInstant()
                              .toEpochMilli()
                          < 0)
              .findFirst();

      if (overviewAllTime.isPresent()) {
        newDragonOverviews.remove(overviewAllTime.get());
        newDragonOverviews.add(
            new GetMyDragonsQuery.DragonOverview(
                overviewAllTime.get().__typename(),
                overviewAllTime.get()._id(),
                overviewAllTime.get().day(),
                overviewAllTime.get().gross() + dragon.gross()));
      }

      GetMyDragonsQuery.Data newData =
          new GetMyDragonsQuery.Data(
              new CurrentUser(
                  currentUser.__typename(),
                  currentUser._id(),
                  currentUser.displayName(),
                  newDragons,
                  newDragonOverviews));

      try {
        apolloProvider
            .getClient()
            .apolloStore()
            .writeAndPublish(getMyDragonsQuery, newData)
            .execute();
        this.loadHUDData();
      } catch (ApolloException ex) {
        ex.printStackTrace();
      }
    }
  }

  protected Stream<ItemProvider> mapDataToItemProviders(ItemProvidersQuery.Data data) {
    return data.dragonItemProviders().stream().map(ItemProvider::new);
  }

  public void loadHUDData() {
    this.loadHUDData(0);
  }

  private void loadHUDData(int errorCount) {
    apolloProvider
        .getClient()
        .query(getMyDragonsQuery)
        .httpCachePolicy(HttpCachePolicy.CACHE_FIRST)
        .watcher()
        .enqueueAndWatch(
            new Callback<GetMyDragonsQuery.Data>() {
              @Override
              public void onResponse(@Nonnull Response<GetMyDragonsQuery.Data> response) {
                currentUser =
                    Optional.of(response)
                        .map(Response::data)
                        .map(GetMyDragonsQuery.Data::currentUser)
                        .orElse(null);
              }

              @Override
              public void onFailure(@Nonnull ApolloException ex) {
                ex.printStackTrace();
                if (errorCount < 1) loadHUDData(errorCount + 1);
              }
            });
  }

  class DragonReward extends Reward {

    public DragonReward(ItemProvider itemProvider) {
      super(itemProvider);
    }

    @Override
    DragonRewardInput toRewardInput() {
      return DragonRewardInput.builder().reward(this.type).count(this.count).build();
    }
  }
}
