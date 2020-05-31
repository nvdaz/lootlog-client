package xyz.nvda.lootlog;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StringUtils;

public class ChatPattern {

  private final Pattern pattern;
  private final MatchType matchType;

  public ChatPattern(String pattern, MatchType matchType) {
    this.pattern = Pattern.compile(pattern);
    this.matchType = matchType;
  }

  public Matcher matcher(IChatComponent chatComponent) {
    return this.pattern.matcher(
        matchType == MatchType.FORMATTED
            ? chatComponent.getFormattedText()
            : StringUtils.stripControlCodes(chatComponent.getUnformattedText()));
  }

  public enum MatchType {
    FORMATTED,
    UNFORMATTED
  }

}
