package Controller;


import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("FieldMayBeFinal")
public class NotificationsController {

    private static NotificationsController notificationsController;
    private Map<String,Integer>notificationIDs;
    public interface onNotificationRemoveListener{
        void onNotificationRemoved(int notificationID);
    }
    private List<onNotificationRemoveListener>listeners;

    public static NotificationsController getInstance() {
        if (notificationsController == null)
            notificationsController = new NotificationsController();
        return notificationsController;
    }

    private NotificationsController()
    {
        notificationIDs = new HashMap<>();
        listeners = new ArrayList<>();
    }

    public void addOnRemoveListener(onNotificationRemoveListener listener)
    {
        listeners.add(listener);
    }

    public void addNotification(String conversationID)
    {
        if (notificationIDs.isEmpty())
        {
            notificationIDs.put(conversationID,0);
        }
        else if (!notificationIDs.containsKey(conversationID))
        {
            Set<String>keys = notificationIDs.keySet();
            Iterator<String>iterator = keys.iterator();
            String lastKey = null;
            while (iterator.hasNext())
            {
                lastKey = iterator.next();
            }
            int lastValue = notificationIDs.get(lastKey);
            int newValue = lastValue + 1;
            notificationIDs.put(conversationID,newValue);
        }
        else
            Log.d("notifications","notification for conversation already exist");
    }

    public void removeNotification(String conversationID)
    {
        for (onNotificationRemoveListener listener : listeners)
            listener.onNotificationRemoved(getNotificationID(conversationID));
        notificationIDs.remove(conversationID);
    }

    public int getNotificationID(String conversationID)
    {
        if (notificationIDs.containsKey(conversationID))
            return notificationIDs.get(conversationID);
        else
        {
            addNotification(conversationID);
            return getNotificationID(conversationID);
        }
    }
}
