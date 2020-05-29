package com.nvda.lootlog.notifications;

import xyz.nvda.sbnotifications.Notification;
import xyz.nvda.sbnotifications.NotificationHandler;

public class SBNotificationsProxy implements INotificationProxy {

    private static final NotificationHandler notificationHandler = NotificationHandler.getInstance();

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public void addNotification(String ln1, String ln2,  long duration) {
        notificationHandler.add(new Notification(ln1, ln2, duration));
    }
}
