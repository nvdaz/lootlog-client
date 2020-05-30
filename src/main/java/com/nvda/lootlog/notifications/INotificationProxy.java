package com.nvda.lootlog.notifications;

public interface INotificationProxy {

  boolean isActive();

  void addNotification(String ln1, String ln2, long duration);
}
