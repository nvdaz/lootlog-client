package com.nvda.lootlog.util;

import com.google.common.base.CharMatcher;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StringUtils;
import net.minecraft.world.World;

public class ScoreboardUtils {
  @SuppressWarnings("UnstableApiUsage")
  private static final CharMatcher INVALID_CHARACTERS =
      CharMatcher.is('\ud83c').or(CharMatcher.inRange('\uDF00',  '\uDFFF')).or(CharMatcher.inRange('\uDC00', '\uDDFF'));

  @SuppressWarnings("UnstableApiUsage")
  public static List<IChatComponent> getSidebarScores() {
    Scoreboard scoreboard =
        Optional.ofNullable(Minecraft.getMinecraft())
            .map((mc) -> mc.theWorld)
            .map(World::getScoreboard)
            .orElse(null);

    if (scoreboard == null) return Lists.newArrayList();

    ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);

    if (objective == null) return Lists.newArrayList();

    Collection<Score> scores = scoreboard.getSortedScores(objective);
    List<Score> list =
        scores.stream()
            .filter(
                input ->
                    input != null
                        && input.getPlayerName() != null
                        && !input.getPlayerName().startsWith("#"))
            .collect(Collectors.toList());
    Collections.reverse(list);

    scores = list.size() > 15 ? Lists.newArrayList(Iterables.skip(list, scores.size() - 15)) : list;

    return scores.stream()
        .map(
            score ->
                new ChatComponentText(
                INVALID_CHARACTERS.removeFrom(
                        ScorePlayerTeam.formatPlayerName(
                            scoreboard.getPlayersTeam(score.getPlayerName()),
                            score.getPlayerName()))))
        .collect(Collectors.toList());
  }

  public static String getUnformattedLine(int line) {
    List<IChatComponent> scores = getSidebarScores();
    return scores.size() > line
        ? StringUtils.stripControlCodes(getSidebarScores().get(line).getUnformattedText())
        : null;
  }
}