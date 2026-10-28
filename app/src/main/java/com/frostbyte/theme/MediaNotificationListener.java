package com.frostbyte.theme;

import android.service.notification.NotificationListenerService;

public final class MediaNotificationListener extends NotificationListenerService {
    @Override public void onListenerConnected() { MediaBridge.get(this).refreshSessions(); }
    @Override public void onNotificationPosted(android.service.notification.StatusBarNotification sbn) { MediaBridge.get(this).refreshSessions(); }
    @Override public void onNotificationRemoved(android.service.notification.StatusBarNotification sbn) { MediaBridge.get(this).refreshSessions(); }
}
