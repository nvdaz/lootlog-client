package com.nvda.lootlog.notifications;

public class InactiveNotificationProxy implements INotificationProxy {
    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public void addNotification(String ln1, String ln2, long duration) {}
}
