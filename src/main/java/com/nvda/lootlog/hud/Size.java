package com.nvda.lootlog.hud;

public class Size {

  private int width;
  private int height;

  public Size() {
    this.width = 0;
    this.height = 0;
  }

  public Size(int width, int height) {
    this.width = width;
    this.height = height;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public void maxWidth(int width) {
    this.width = Math.max(this.width, width);
  }

  public void maxHeight(int height) {
    this.height = Math.max(this.height, height);
  }

  public void max(int width, int height) {
    this.maxWidth(width);
    this.maxHeight(height);
  }

  public void addWidth(int width) {
    this.width += width;
  }

  public void addHeight(int height) {
    this.height += height;
  }

  public boolean includes(Anchor anchor, int x, int y) {
    return x >= anchor.getX() && x <= anchor.getX() + this.width && y >= anchor.getY() && y <= anchor.getY() + this.height;
  }
}
