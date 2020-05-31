package xyz.nvda.lootlog.bosses;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import net.minecraft.util.IChatComponent;
import xyz.nvda.lootlog.ChatPattern;
import xyz.nvda.lootlog.ChatPattern.MatchType;
import xyz.nvda.lootlog.Message;
import xyz.nvda.lootlog.api.AddGolemMutation;
import xyz.nvda.lootlog.api.AddGolemMutation.AddGolem;
import xyz.nvda.lootlog.api.AddGolemMutation.Data;
import xyz.nvda.lootlog.api.AddGolemMutation.Variables;
import xyz.nvda.lootlog.api.ItemProvidersQuery;
import xyz.nvda.lootlog.api.type.GolemRewardInput;
import xyz.nvda.lootlog.api.type.GolemRewardType;
import xyz.nvda.lootlog.util.DelayedTask;

public class GolemHandler
    extends BossHandler<Data, Variables, AddGolemMutation, GolemRewardInput, GolemRewardType> {
  private static final ChatPattern SPAWN_PATTERN =
      new ChatPattern("^An Endstone Protector is spawning!$", MatchType.UNFORMATTED);
  private static final ChatPattern LEADERBOARD_PATTERN =
      new ChatPattern(
          "^ +Your Damage: [\\d,]+ (?:\\(NEW RECORD!\\) )?\\(Position #(\\d{1,2})\\)$",
          MatchType.UNFORMATTED);
  private static final GolemHandler instance = new GolemHandler();
  private DelayedTask delayedTask;
  private int leaderboardPlacement = 1;
  private long endedAt;
  private boolean spawned = false;

  private GolemHandler() {}

  public static GolemHandler getInstance() {
    return instance;
  }

  public boolean testChat(IChatComponent chatComponent) {
    Matcher spawnMatcher = SPAWN_PATTERN.matcher(chatComponent);
    if (spawnMatcher.matches()) {
      this.spawned = true;
      Message.GOLEM_SPAWNED.debug();
      return true;
    }

    Matcher leaderboardMatcher = LEADERBOARD_PATTERN.matcher(chatComponent);
    if (leaderboardMatcher.matches()) {
      if (!this.spawned) return false;
      this.leaderboardPlacement = Integer.parseInt(leaderboardMatcher.group(1));
      this.endedAt = System.currentTimeMillis();
      Message.LEADERBOARD.debug(this.leaderboardPlacement);
      this.delayedTask =
          new DelayedTask(
              () -> {
                this.flush();
                this.delayedTask = null;
              },
              1200);
      return true;
    }

    if (System.currentTimeMillis() - this.endedAt > 60000 || !this.spawned) return false;
    Optional<ItemProvider> itemProvider =
        this.itemProviders.stream()
            .filter(golemItemProvider -> golemItemProvider.testChatMessage(chatComponent))
            .findFirst();
    if (itemProvider.isPresent()) {
      if (this.addReward(itemProvider.get(), 1))
        Message.ITEM_ACQUIRED.debug(itemProvider.get().rewardType.toString());
      return true;
    }

    return false;
  }

  public void testItem(String itemName, int count) {
    if (System.currentTimeMillis() - this.endedAt > 10000 || !this.spawned) return;
    Optional<ItemProvider> itemProvider =
        this.itemProviders.stream()
            .filter(golemItemProvider -> golemItemProvider.testItemName(itemName))
            .findFirst();
    if (itemProvider.isPresent()) {
      if (this.addReward(itemProvider.get(), count))
        Message.ITEM_ACQUIRED.debug(itemProvider.get().rewardType.toString());
    }
  }

  protected AddGolemMutation build() {
    return AddGolemMutation.builder()
        .leaderboardPlacement(leaderboardPlacement)
        .rewards(rewards.stream().map(BossReward::toRewardInput).collect(Collectors.toList()))
        .build();
  }

  protected void reset() {
    if (this.delayedTask != null) {
      this.delayedTask.cancel();
      this.delayedTask = null;
    }
    this.spawned = false;
    this.leaderboardPlacement = 1;
    this.rewards.clear();
  }

  protected void handleAddMutation(Data data) {
    AddGolem golem = data.addGolem();
    Message.GOLEM_POSTED.send(golem.revenue());
  }

  protected GolemReward newReward(ItemProvider itemProvider) {
    return new GolemReward(itemProvider);
  }

  public void loadProviders(Consumer<LoadProvidersResult> consumer) {
    this.loadProviders(consumer, 0);
  }

  @Override
  protected List<ItemProvider> mapProviders(List<ItemProvidersQuery.ItemProvider> itemProviders) {
    return itemProviders.stream()
        .map(ItemProvidersQuery.ItemProvider::asGolemItemProvider)
        .filter(Objects::nonNull)
        .map(
            itemProvider ->
                new ItemProvider(
                    itemProvider.test(),
                    itemProvider.mode(),
                    itemProvider.golemItem(),
                    itemProvider.minecraftItem(),
                    itemProvider.texture(),
                    Optional.ofNullable(itemProvider.metadata()).orElse(0)))
        .collect(Collectors.toList());
  }

  public class GolemReward extends Reward {
    GolemReward(ItemProvider itemProvider) {
      super(itemProvider);
    }

    GolemRewardInput toRewardInput() {
      return GolemRewardInput.builder().reward(this.type).count(this.count).build();
    }
  }
}
