package com.nvda.lootlog;

import com.nvda.lootlog.hud.Anchor;
import java.io.File;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Loader;

public class ConfigurationHandler {

  private static final ConfigurationHandler instance = new ConfigurationHandler();

  private final Configuration config;

  private final Property nickProperty;
  private final Property debugProperty;
  private final Property boundingProperty;
  private final Property tokenProperty;
  private final Property hudXProperty;
  private final Property hudYProperty;
  private final Property hudEnabledProperty;

  public static ConfigurationHandler getInstance() {
    return instance;
  }

  public ConfigurationHandler() {
    File file = new File(Loader.instance().getConfigDir(), "lootlog.cfg");
    boolean first = !file.exists();
    this.config = new Configuration(file);
    this.config.load();

    this.nickProperty = this.config.get(Configuration.CATEGORY_CLIENT, "nick", "");
    this.debugProperty = this.config.get(Configuration.CATEGORY_CLIENT, "debug", false);
    this.boundingProperty = this.config.get(Configuration.CATEGORY_CLIENT, "bounding", false);
    this.tokenProperty = this.config.get(Configuration.CATEGORY_CLIENT, "token", "");
    this.hudXProperty = this.config.get(Configuration.CATEGORY_CLIENT, "hud.x", 2);
    this.hudYProperty = this.config.get(Configuration.CATEGORY_CLIENT, "hud.y", 50);
    this.hudEnabledProperty = this.config.get(Configuration.CATEGORY_CLIENT, "hud.enabled", true);

    if (first) this.attemptMigrate();
    if (this.config.hasChanged()) this.config.save();
  }

  private void attemptMigrate() {
    File oldFile = new File(Loader.instance().getConfigDir(), "lootLogger.cfg");
    if (!oldFile.exists()) return;
    Configuration oldConfig = new Configuration(oldFile);

    this.setNickname(oldConfig.get("lootlog", "nick", "").getString());
    this.setDebugMode(oldConfig.get("lootlog", "debug", false).getBoolean());
    this.setBoundingEnabled(oldConfig.get("lootlog", "bounding", false).getBoolean());
    this.setAccessToken(oldConfig.get("lootlog", "token", "").getString());

    oldFile.delete();
  }

  public String getNickname() {
    return this.nickProperty.getString();
  }

  public void setNickname(String nickname) {
    this.nickProperty.set(nickname);
    if (this.config.hasChanged()) this.config.save();
  }

  public boolean isDebugMode() {
    return this.debugProperty.getBoolean();
  }

  public void setDebugMode(boolean debugMode) {
    this.debugProperty.set(debugMode);
    if (this.config.hasChanged()) this.config.save();
  }

  public boolean isBoundingEnabled() {
    return this.boundingProperty.getBoolean();
  }

  public void setBoundingEnabled(boolean boundingEnabled) {
    this.boundingProperty.set(boundingEnabled);
    if (this.config.hasChanged()) this.config.save();
  }

  public String getAccessToken() {
    return this.tokenProperty.getString();
  }

  public void setAccessToken(String accessToken) {
    this.tokenProperty.set(accessToken);
    if (this.config.hasChanged()) this.config.save();
  }

  public boolean isHUDEnabled() {
    return this.hudEnabledProperty.getBoolean();
  }

  public void setHUDEnabled(boolean enabled) {
    this.hudEnabledProperty.set(enabled);
    if (this.config.hasChanged()) this.config.save();
  }

  public Anchor getHUDAnchor() {
    return new Anchor(this.hudXProperty.getInt(), this.hudYProperty.getInt());
  }

  public void setAnchor(Anchor anchor) {
    this.hudXProperty.set(anchor.getX());
    this.hudYProperty.set(anchor.getY());
    if (this.config.hasChanged()) this.config.save();
  }
}
