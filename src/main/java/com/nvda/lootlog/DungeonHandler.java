package com.nvda.lootlog;

import com.nvda.lootlog.api.type.MatchType;
import java.util.regex.Matcher;
import net.minecraft.util.IChatComponent;

public class DungeonHandler extends BossHandler<?, ?, ?, ?, ?> {

  private static final ChatPattern ENTER_PATTERN =
      new ChatPattern("^Dungeon starts in 1 second.$", MatchType.UNFORMATTED);
  private static final ChatPattern CLASS_PATTERN =
      new ChatPattern(
          "^(\\w{3,16}) selected the (Healer|Mage|Archer|Berserk|Tank) Dungeon Class.$",
          MatchType.UNFORMATTED);
  private static final ChatPattern SCORE_PATTERN =
      new ChatPattern("^ +Team Score: (\\d+) \\(([SA-DF]+?)\\)", MatchType.UNFORMATTED);

  private boolean inDungeon;
  private int score = 0;
  private String grade = "";

  @Override
  public boolean testChat(IChatComponent chatComponent) {
    if (ENTER_PATTERN.matcher(chatComponent).matches()) {
      inDungeon = true;
      return true;
    }

    Matcher scoreMatcher = SCORE_PATTERN.matcher(chatComponent);
    if (scoreMatcher.matches()) {
      this.score = Integer.parseInt(scoreMatcher.group(1));
      this.grade = scoreMatcher.group(2);
    }

    return false;
  }
}
