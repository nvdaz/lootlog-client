package com.nvda.lootlog.notifications;

import net.minecraftforge.fml.common.Loader;

public class NotificationProxyHandler {

  private static INotificationProxy instance;

  public static INotificationProxy getInstance() {
    if (instance != null) return instance;
    if (Loader.isModLoaded("sbnotifications")) {
      try {
        instance =
            Class.forName("com.nvda.lootlog.notifications.SBNotificationsProxy")
                .asSubclass(INotificationProxy.class)
                .newInstance();
      } catch (IllegalAccessException | ClassNotFoundException | InstantiationException ex) {
        ex.printStackTrace();
      }
    }
    if (instance == null) instance = new InactiveNotificationProxy();

    return instance;
  }
}
