package com.nvda.lootlog.hud;

public class Anchor {

  private final int x;
  private final int y;

  public Anchor(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public Anchor offset(int x, int y) {
    return new Anchor(this.x + x, this.y + y);
  }
}
