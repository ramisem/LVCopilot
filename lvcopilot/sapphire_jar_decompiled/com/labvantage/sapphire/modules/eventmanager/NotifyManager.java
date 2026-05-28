/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eventmanager;

import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.gwt.shared.util.StringUtil;
import com.labvantage.sapphire.modules.dashboard.gizmos.BaseGizmo;
import com.labvantage.sapphire.modules.eventmanager.AsyncNotify;
import com.labvantage.sapphire.modules.eventmanager.Notify;
import com.labvantage.sapphire.modules.eventmanager.NotifyListener;
import com.labvantage.sapphire.modules.eventmanager.gwt.shared.NotificationConstants;
import com.labvantage.sapphire.services.AutomationService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.SapphireService;
import com.labvantage.sapphire.servlet.NotificationController;
import java.text.SimpleDateFormat;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class NotifyManager
implements NotificationConstants {
    private static ArrayList<NotifyListener> notificationListeners = new ArrayList();
    private static HashMap<String, AsyncNotify> subscriberAsyncRequests = new HashMap();
    private static HashMap<String, ArrayList<String>> subscriberElementRequests = new HashMap();
    private static HashMap<String, PropertyListCollection> notificationsMap = new HashMap();
    private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static Runnable sendNotifications = new Runnable(){

        @Override
        public void run() {
            NotifyManager.sendNotifications();
        }
    };
    private static ScheduledFuture<?> deferredSendNotifications;
    private static String systemPassword;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void setSystemPassword(String systemPswd) {
        NotifyManager.logDebug("System password set (" + (systemPswd == null || systemPswd.length() > 0 ? "No password provided" : "Password provided") + ")(NotifyManager)");
        systemPassword = systemPswd;
        ArrayList<NotifyListener> arrayList = notificationListeners;
        synchronized (arrayList) {
            if (notificationListeners != null && notificationListeners.size() > 0) {
                for (NotifyListener listener : notificationListeners) {
                    listener.setSystemPassword(systemPswd);
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void registerListener(NotifyListener listener) {
        ArrayList<NotifyListener> arrayList = notificationListeners;
        synchronized (arrayList) {
            if (!notificationListeners.contains(listener)) {
                NotifyManager.logDebug("registerListener called");
                notificationListeners.add(listener);
                listener.setSystemPassword(systemPassword);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void resetDatabaseFilterOptions(String databaseid) {
        ArrayList<NotifyListener> arrayList = notificationListeners;
        synchronized (arrayList) {
            if (notificationListeners.size() > 0) {
                notificationListeners.get(0).resetDatabaseFilterOptions(databaseid);
            }
        }
    }

    public static void logDebug(String msg) {
        NotificationController.logDebug("NotifyManager - " + msg);
    }

    public static void logInfo(String msg) {
        NotificationController.logInfo("NotifyManager - " + msg);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static synchronized void requestNotifications(String databaseid, String subscriberid, String windowname, AsyncNotify asyncNotify, boolean initialRequest) {
        HashMap<String, Object> hashMap;
        NotifyManager.logDebug("In requestNotifications.");
        if (asyncNotify != null) {
            hashMap = subscriberAsyncRequests;
            synchronized (hashMap) {
                NotifyManager.logDebug("Synchronized requestNotifications.");
                boolean rerequest = false;
                if (subscriberAsyncRequests.containsKey(databaseid + ";" + subscriberid)) {
                    asyncNotify.servletLog("UNREQUEST: " + databaseid + "-" + subscriberid);
                    subscriberAsyncRequests.get(databaseid + ";" + subscriberid).unRequest();
                    rerequest = true;
                }
                asyncNotify.servletLog((rerequest ? "RE-" : "") + "REQUEST NOTIFICATIONS: " + databaseid + "-" + subscriberid);
                subscriberAsyncRequests.put(databaseid + ";" + subscriberid, asyncNotify);
            }
        }
        if (asyncNotify != null) {
            hashMap = notificationsMap;
            synchronized (hashMap) {
                NotifyManager.logDebug("Synchronized requestNotifications - setting up notifications map.");
                PropertyListCollection subscriberNotifications = notificationsMap.get(databaseid + ";" + subscriberid);
                if (subscriberNotifications != null && subscriberNotifications.size() > 0) {
                    for (int i = 0; i < subscriberNotifications.size(); ++i) {
                        NotifyManager.addGizmoNotifications(subscriberNotifications.getPropertyList(i), asyncNotify.getConnectionid());
                    }
                    NotifyManager.respondAsync(databaseid, subscriberid, windowname, subscriberNotifications, asyncNotify, initialRequest, databaseid + "-" + subscriberid + ": Request Notifications - " + subscriberNotifications.size() + " pending notifications");
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static synchronized void requestElementNotifications(String databaseid, String subscriberid, String elementtype, String elementid) {
        NotifyManager.logDebug("In requestElementNotifications.");
        HashMap<String, ArrayList<String>> hashMap = subscriberElementRequests;
        synchronized (hashMap) {
            ArrayList<String> elementRequests = subscriberElementRequests.get(databaseid + ";" + subscriberid);
            if (elementRequests == null) {
                NotifyManager.logDebug("Synchronized requestElementNotifications.");
                elementRequests = new ArrayList();
                subscriberElementRequests.put(databaseid + ";" + subscriberid, elementRequests);
            }
            if (!elementRequests.contains(elementtype + ";" + elementid)) {
                elementRequests.add(elementtype + ";" + elementid);
            }
            NotifyManager.logDebug("- ELEMENT REQUEST: " + databaseid + "-" + subscriberid + " " + elementtype + " (" + elementid + ")");
        }
    }

    public static void notifyCache(String databaseid, String cacheName, String key, boolean startsWith, boolean endsWith) {
        String cid;
        NotifyManager.logDebug("In notifyCache.");
        PropertyList props = new PropertyList();
        props.setProperty("type", "X");
        props.setProperty("__databaseid", databaseid);
        if (cacheName != null && cacheName.length() > 0) {
            props.setProperty("cachename", cacheName);
            if (key != null && key.length() > 0) {
                props.setProperty("cachekey", key);
                if (startsWith) {
                    props.setProperty("startswith", "Y");
                } else if (endsWith) {
                    props.setProperty("endswith", "Y");
                }
            }
        }
        if (databaseid != null && databaseid.length() > 0 && (cid = SapphireService.getInternalConnectionid(databaseid)) != null && cid.length() > 0) {
            NotifyManager.notify(props, true, cid);
        }
    }

    public static void notifyLogoff(SapphireConnection sapphireConnection, boolean logoff) {
        NotifyManager.logDebug("In notifyLogoff.");
        PropertyList props = new PropertyList();
        props.setProperty("__databaseid", sapphireConnection.getDatabaseId());
        props.setProperty("subscriberid", sapphireConnection.getSysuserId() + ";" + sapphireConnection.getConnectionId());
        props.setProperty("connectionid", sapphireConnection.getConnectionId());
        props.setProperty("type", logoff ? "L" : "I");
        NotifyManager.logDebug("Before Notify...");
        NotifyManager.notify(props, false, null);
        NotifyManager.logDebug("After Notify.");
        AutomationService.broadcastServerCommand(sapphireConnection.getDatabaseId(), "Notification", props.toXMLString());
    }

    public static void notifyCancelLogoff(SapphireConnection sapphireConnection) {
        NotifyManager.logDebug("In notifyCancelLogoff.");
        PropertyList props = new PropertyList();
        props.setProperty("__databaseid", sapphireConnection.getDatabaseId());
        props.setProperty("subscriberid", sapphireConnection.getSysuserId() + ";" + sapphireConnection.getConnectionId());
        props.setProperty("connectionid", sapphireConnection.getConnectionId());
        props.setProperty("type", "C");
        NotifyManager.notify(props, false, null);
    }

    public static synchronized boolean notifyListeners(PropertyList properties) {
        return NotifyManager.notifyListeners(properties, false, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static synchronized boolean notifyListeners(PropertyList properties, boolean newTransaction, String connectionId) {
        boolean preCheckok = true;
        try {
            ArrayList<NotifyListener> arrayList = notificationListeners;
            synchronized (arrayList) {
                if (notificationListeners.size() > 0) {
                    for (NotifyListener notifyListener : notificationListeners) {
                        try {
                            if (notifyListener.preNotify(properties)) continue;
                            preCheckok = false;
                            break;
                        }
                        catch (Throwable e) {
                            NotifyManager.logInfo("Failed notification listener precheck. " + e.getMessage());
                        }
                    }
                } else if (properties.getProperty("type").equalsIgnoreCase("X")) {
                    preCheckok = false;
                }
            }
        }
        catch (Throwable e2) {
            NotifyManager.logInfo("Failed to execute notification listeners precheck. " + e2.getMessage());
        }
        boolean continueNotify = true;
        if (preCheckok) {
            if (newTransaction && connectionId != null && connectionId.length() > 0) {
                try {
                    ActionProcessor ap = new ActionProcessor(connectionId);
                    properties.put("listenersonly", "Y");
                    ap.processActionClass(Notify.class.getName(), properties, true);
                    continueNotify = !properties.getProperty("listenerscontinue", "Y").equalsIgnoreCase("N");
                }
                catch (Throwable e) {
                    NotifyManager.logInfo("Failed to notify in new transaction.");
                }
            } else {
                try {
                    ArrayList<NotifyListener> e = notificationListeners;
                    synchronized (e) {
                        if (notificationListeners.size() > 0) {
                            for (NotifyListener notifyListener : notificationListeners) {
                                try {
                                    if (notifyListener.notify(properties)) continue;
                                    continueNotify = false;
                                    break;
                                }
                                catch (Throwable e2) {
                                    NotifyManager.logInfo("Failed notification listener. " + e2.getMessage());
                                }
                            }
                        }
                    }
                }
                catch (Throwable e2) {
                    NotifyManager.logInfo("Failed to execute notification listeners. " + e2.getMessage());
                }
            }
        }
        return continueNotify;
    }

    public static synchronized void notify(PropertyList properties) {
        NotifyManager.notify(properties, false, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static synchronized void notify(PropertyList properties, boolean newTransaction, String connectionId) {
        NotifyManager.logDebug("In notify.");
        if (NotifyManager.notifyListeners(properties, newTransaction, connectionId)) {
            boolean send = true;
            String databaseid = properties.getProperty("__databaseid");
            String subscriberid = properties.getProperty("subscriberid");
            String notifyType = properties.getProperty("type", "T");
            if (notifyType.equals("E")) {
                String elementtype = properties.getProperty("elementtype");
                String elementid = properties.getProperty("elementid", "*");
                String usercontext = properties.getProperty("usercontext");
                PropertyList currentNotification = NotifyManager.createNotification(properties);
                NotifyManager.logDebug("ELEMENT NOTIFY: " + elementtype + ", " + elementid);
                if (subscriberElementRequests.size() > 0) {
                    HashMap<String, ArrayList<String>> hashMap = subscriberElementRequests;
                    synchronized (hashMap) {
                        NotifyManager.logDebug("Synchronized notify - for type element.");
                        for (String requestSubscriberKey : subscriberElementRequests.keySet()) {
                            ArrayList<String> elementRequests;
                            String requestSubscriber = requestSubscriberKey.substring(requestSubscriberKey.indexOf(";") + 1);
                            if (usercontext.length() != 0 && !usercontext.equals("undefined") && (usercontext.length() <= 0 || !usercontext.equals(requestSubscriber) && requestSubscriber.indexOf(usercontext + ";") != 0) || (elementRequests = subscriberElementRequests.get(requestSubscriberKey)) == null || !NotifyManager.matchingElement(elementRequests, elementtype, elementid)) continue;
                            NotifyManager.logDebug("ELEMENT NOTIFY: MATCH for element " + elementtype + " (" + elementid + ")");
                            HashMap<String, PropertyListCollection> hashMap2 = notificationsMap;
                            synchronized (hashMap2) {
                                AsyncNotify asyncNotify = subscriberAsyncRequests.get(requestSubscriberKey);
                                if (asyncNotify != null) {
                                    PropertyListCollection subscriberNotifications = notificationsMap.get(requestSubscriberKey);
                                    if (subscriberNotifications == null) {
                                        subscriberNotifications = new PropertyListCollection();
                                        notificationsMap.put(requestSubscriberKey, subscriberNotifications);
                                    }
                                    subscriberNotifications.add(currentNotification);
                                    NotifyManager.logDebug("ELEMENT NOTIFY: Adding notification to map (" + subscriberNotifications.size() + ")");
                                } else {
                                    subscriberAsyncRequests.remove(requestSubscriberKey);
                                    NotifyManager.logDebug("ELEMENT NOTIFY: Removing " + requestSubscriberKey + " from SubscriberRequests (no async response found)");
                                }
                            }
                        }
                    }
                }
            } else {
                boolean subscriberIsConnId;
                if (notifyType.equals("I") || notifyType.equals("L") || notifyType.equals("C")) {
                    send = false;
                    HashMap<String, PropertyListCollection> elementtype = notificationsMap;
                    synchronized (elementtype) {
                        NotifyManager.logDebug(notifyType.equals("L") ? "LOGOFF NOTIFY" : (notifyType.equals("I") ? "IMINENT LOGOFF NOTIFY" : "CANCEL LOGOFF NOTIFY"));
                        AsyncNotify asyncNotify = subscriberAsyncRequests.get(databaseid + ";" + subscriberid);
                        if (asyncNotify != null && properties.getProperty("connectionid").equals(asyncNotify.getConnectionid())) {
                            if (notifyType.equals("L")) {
                                notificationsMap.remove(databaseid + ";" + subscriberid);
                                subscriberElementRequests.remove(databaseid + ";" + subscriberid);
                            }
                            PropertyListCollection subscriberNotifications = new PropertyListCollection();
                            PropertyList logoffNotification = new PropertyList();
                            logoffNotification.setProperty("type", notifyType);
                            subscriberNotifications.add(logoffNotification);
                            NotifyManager.respondAsync(databaseid, subscriberid, "", subscriberNotifications, asyncNotify, false, "NOTIFYING: " + databaseid + "-" + subscriberid + " - " + (notifyType.equals("L") ? "LOGOFF" : (notifyType.equals("I") ? "IMINENT LOGOFF" : "CANCEL LOGOFF")));
                        }
                    }
                }
                boolean bl = subscriberIsConnId = subscriberid.startsWith(databaseid + "|") || subscriberid.startsWith("#") && subscriberid.contains("?") && subscriberid.contains("-");
                if (subscriberIsConnId && !properties.getProperty("__connection").equals(subscriberid)) {
                    HashMap<String, PropertyListCollection> hashMap = notificationsMap;
                    synchronized (hashMap) {
                        NotifyManager.logDebug("USER NOTIFY: " + NotifyManager.getNotifyTypeText(properties.getProperty("type")) + " - " + properties.getProperty("message"));
                        PropertyListCollection subscriberNotifications = notificationsMap.get(";" + subscriberid);
                        if (subscriberNotifications == null) {
                            subscriberNotifications = new PropertyListCollection();
                            notificationsMap.put(";" + subscriberid, subscriberNotifications);
                        }
                        PropertyList currentNotification = NotifyManager.createNotification(properties);
                        subscriberNotifications.add(currentNotification);
                    }
                }
                if (!properties.getProperty("__createby").equals(subscriberid)) {
                    HashMap<String, PropertyListCollection> hashMap = notificationsMap;
                    synchronized (hashMap) {
                        NotifyManager.logDebug("USER NOTIFY: " + NotifyManager.getNotifyTypeText(properties.getProperty("type")) + " - " + properties.getProperty("message"));
                        PropertyListCollection subscriberNotifications = notificationsMap.get(databaseid + ";" + subscriberid);
                        if (subscriberNotifications == null) {
                            subscriberNotifications = new PropertyListCollection();
                            notificationsMap.put(databaseid + ";" + subscriberid, subscriberNotifications);
                        }
                        PropertyList currentNotification = NotifyManager.createNotification(properties);
                        subscriberNotifications.add(currentNotification);
                    }
                }
            }
            if (send) {
                NotifyManager.logDebug("SEND NOTIFICATIONS: Cancel/Reschedule Send");
                if (deferredSendNotifications != null) {
                    deferredSendNotifications.cancel(false);
                }
                deferredSendNotifications = scheduler.schedule(sendNotifications, 5L, TimeUnit.SECONDS);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static synchronized void sendNotifications() {
        NotifyManager.logDebug("SEND NOTIFICATIONS");
        HashMap<String, PropertyListCollection> hashMap = notificationsMap;
        synchronized (hashMap) {
            HashMap<String, PropertyList> elementValues = new HashMap<String, PropertyList>();
            ArrayList<String> removeList = new ArrayList<String>();
            for (String notificationsKey : notificationsMap.keySet()) {
                String[] subscriberParts = StringUtil.split(notificationsKey, ";");
                PropertyListCollection notifications = notificationsMap.get(notificationsKey);
                if (notifications == null || notifications.size() <= 0) continue;
                String databaseid = subscriberParts[0];
                String subscriberid = subscriberParts[1];
                AsyncNotify asyncNotify = subscriberAsyncRequests.get(notificationsKey);
                if (asyncNotify == null) {
                    for (String key : subscriberAsyncRequests.keySet()) {
                        if (key.length() <= 0 || !key.endsWith(notificationsKey)) continue;
                        asyncNotify = subscriberAsyncRequests.get(key);
                        break;
                    }
                    if (asyncNotify == null) {
                        for (String key : subscriberAsyncRequests.keySet()) {
                            if (key.length() <= 0 || !key.startsWith(notificationsKey + ";")) continue;
                            asyncNotify = subscriberAsyncRequests.get(key);
                            break;
                        }
                    }
                }
                if (asyncNotify != null) {
                    for (int i = 0; i < notifications.size(); ++i) {
                        PropertyList notification = notifications.getPropertyList(i);
                        if (!notification.getProperty("type").equals("E")) continue;
                        String connectionid = asyncNotify.getConnectionid();
                        String elementid = notification.getProperty("elementid");
                        PropertyList values = (PropertyList)elementValues.get(connectionid + ";" + elementid);
                        if (values == null) {
                            values = new PropertyList();
                            values.setProperty("elementid", elementid);
                            NotifyManager.addGizmoNotifications(values, connectionid);
                            elementValues.put(connectionid + ";" + elementid, values);
                        }
                        notification.setProperty("callbackcount", values.getProperty("callbackcount"));
                        notification.setProperty("callbackcountcolor", values.getProperty("callbackcountcolor"));
                        notification.setProperty("callbacktext", values.getProperty("callbacktext"));
                        notification.setProperty("callbacktextcolor", values.getProperty("callbacktextcolor"));
                        notification.setProperty("callbackimagesrc", values.getProperty("callbackimagesrc"));
                    }
                    if (!NotifyManager.respondAsync(databaseid, subscriberid, "", notifications, asyncNotify, false, "NOTIFYING: " + databaseid + "-" + subscriberid + " - " + notifications.size() + " notifications")) continue;
                    removeList.add(notificationsKey);
                    continue;
                }
                removeList.add(notificationsKey);
            }
            for (int i = 0; i < removeList.size(); ++i) {
                String notificationsKey;
                notificationsKey = (String)removeList.get(i);
                notificationsMap.remove(notificationsKey);
            }
        }
    }

    public static String getNotifyTypeText(String type) {
        return type.equals("A") ? "Alert" : (type.equals("T") ? "Transient" : (type.equals("P") ? "Persistent" : (type.equals("E") ? "Element" : (type.equals("L") ? "Logoff" : (type.equals("I") ? "Imminent Logoff" : (type.equals("C") ? "Cancel Logoff" : "Unknown"))))));
    }

    private static boolean matchingElement(ArrayList<String> elements, String elementtype, String elementid) {
        if (elements != null) {
            if (elementid.length() > 0 && !elementid.equals("*")) {
                return elements.contains(elementtype + ";" + elementid);
            }
            Iterator<String> iterator = elements.iterator();
            while (iterator.hasNext()) {
                if (!iterator.next().startsWith(elementtype + ";")) continue;
                return true;
            }
        }
        return false;
    }

    private static void addGizmoNotifications(PropertyList currentNotification, String connectionid) {
        sapphire.pageelements.BaseGizmo gizmo;
        NotifyManager.logDebug("In addGizmoNotifications.");
        currentNotification.setProperty("callbackcount", "-1");
        currentNotification.setProperty("callbackcountcolor", "");
        currentNotification.setProperty("callbacktext", "");
        currentNotification.setProperty("callbacktextcolor", "");
        currentNotification.setProperty("callbackimagesrc", "");
        String elementid = currentNotification.getProperty("elementid");
        if (elementid.length() > 0 && (gizmo = BaseGizmo.getInstance(connectionid, elementid, true)) != null) {
            NotifyManager.logDebug("addGizmoNotifications - " + elementid);
            currentNotification.setProperty("callbackcount", String.valueOf(gizmo.getCount()));
            currentNotification.setProperty("callbackcountcolor", gizmo.getCountColor());
            currentNotification.setProperty("callbacktext", gizmo.getTitle());
            currentNotification.setProperty("callbacktextcolor", gizmo.getTitleColor());
            currentNotification.setProperty("callbackimagesrc", gizmo.getImageSrc());
        }
    }

    private static PropertyList createNotification(PropertyList properties) {
        NotifyManager.logDebug("In  createNotification.");
        Calendar now = DateTimeUtil.getNowCalendar();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        PropertyList currentNotification = new PropertyList();
        currentNotification.setProperty("notificationid", properties.getProperty("__notificationid"));
        currentNotification.setProperty("databaseid", properties.getProperty("databaseid"));
        if (properties.containsKey("elementtype")) {
            currentNotification.setProperty("elementtype", properties.getProperty("elementtype"));
            currentNotification.setProperty("elementid", properties.getProperty("elementid"));
            currentNotification.setProperty("actions", properties.getProperty("actions"));
        }
        currentNotification.setProperty("type", properties.getProperty("type"));
        currentNotification.setProperty("expiry", properties.getProperty("expiry"));
        currentNotification.setProperty("response", properties.getProperty("response"));
        currentNotification.setProperty("message", properties.getProperty("message"));
        currentNotification.setProperty("link", properties.getProperty("link"));
        currentNotification.setProperty("popup", properties.getProperty("popup"));
        currentNotification.setProperty("clientrequests", "0");
        currentNotification.setProperty("createby", properties.getProperty("__createby"));
        currentNotification.setProperty("createdt", sdf.format(now.getTime()));
        return currentNotification;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static boolean respondAsync(String databaseid, String subscriberid, String windowname, PropertyListCollection subscriberNotifications, AsyncNotify asyncNotify, boolean initialRequest, String logMessage) {
        NotifyManager.logDebug("In respondAsync...");
        boolean sent = false;
        if (asyncNotify != null && asyncNotify.canRespond()) {
            PropertyList asyncNotifications = new PropertyList();
            asyncNotifications.setProperty("subscriberid", subscriberid);
            asyncNotifications.setProperty("windowname", windowname);
            asyncNotifications.setProperty("initialrequest", initialRequest ? "Y" : "N");
            asyncNotifications.setProperty("notifications", subscriberNotifications.toJSONString());
            asyncNotifications.setProperty("callbackArgs", "subscriberid,windowname,initialrequest,notifications");
            asyncNotify.servletLog(logMessage);
            asyncNotify.respond(asyncNotifications);
            PropertyListCollection propertyListCollection = subscriberNotifications;
            synchronized (propertyListCollection) {
                NotifyManager.logDebug("Syncronized respondAsync.");
                for (int i = subscriberNotifications.size() - 1; i >= 0; --i) {
                    PropertyList notification = subscriberNotifications.getPropertyList(i);
                    if (notification.getProperty("type").equals("T") || notification.getProperty("type").equals("A") || notification.getProperty("type").equals("P") || notification.getProperty("type").equals("E")) {
                        subscriberNotifications.remove(i);
                        continue;
                    }
                    int clientRequests = 0;
                    try {
                        clientRequests = Integer.parseInt(notification.getProperty("clientrequests", "0"));
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                    notification.setProperty("clientrequests", String.valueOf(Integer.parseInt(notification.getProperty("clientrequests") + 1)));
                }
            }
            sent = true;
        }
        subscriberAsyncRequests.remove(databaseid + ";" + subscriberid);
        return sent;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void clearNotifications(String databaseid, String subscriberid, String notificationid) {
        NotifyManager.logDebug("In clearNotifications...");
        if (notificationid == null || notificationid.length() == 0) {
            if (notificationsMap.containsKey(databaseid + ";" + subscriberid)) {
                notificationsMap.remove(databaseid + ";" + subscriberid);
            }
        } else {
            PropertyListCollection notifications = notificationsMap.get(databaseid + ";" + subscriberid);
            if (notifications != null) {
                PropertyListCollection propertyListCollection = notifications;
                synchronized (propertyListCollection) {
                    NotifyManager.logDebug("Syncronized clearNotifications.");
                    for (int i = 0; i < notifications.size(); ++i) {
                        if (!(";" + notificationid + ";").contains(";" + notifications.getPropertyList(i).getProperty("notificationid") + ";")) continue;
                        notifications.remove(i);
                        break;
                    }
                }
            }
        }
    }

    public static void requestChatNotifications(String databaseid, String subscriberid, AsyncNotify asyncNotify, boolean initialRequest) {
        NotifyManager.requestNotifications(databaseid, "__chat_" + subscriberid, "__chat_" + subscriberid, asyncNotify, initialRequest);
    }

    public static DataSet requestChatUsers(String connectionid) {
        QueryProcessor queryProcessor = new QueryProcessor(connectionid);
        DataSet users = queryProcessor.getSqlDataSet("SELECT DISTINCT sysuser.sysuserid, sysuser.sysuserdesc, CASE WHEN connection.connectionid IS NULL THEN 'N'  ELSE 'Y' END AS connected, sdiattachment.attachmentnum FROM   sysuser LEFT OUTER JOIN connection ON sysuser.sysuserid = connection.sysuserid LEFT OUTER JOIN sdiattachment ON sdiattachment.sdcid = 'User' AND sysuser.sysuserid = sdiattachment.keyid1 AND sdiattachment.attachmentclass = 'Image' WHERE  ( sysuser.activeflag IS NULL OR sysuser.activeflag = 'Y' ) ORDER BY connected DESC, sysuser.sysuserid");
        return users;
    }

    public static void sendChat(String connectionid, String fromSysuserid, String toSysuserid, String message) {
        NotifyManager.logDebug("In sendChat.");
        ConnectionProcessor cp = new ConnectionProcessor(connectionid);
        SapphireConnection sapphireConnection = cp.getSapphireConnection();
        String databaseid = sapphireConnection.getDatabaseId();
        if (sapphireConnection != null) {
            if (subscriberAsyncRequests.containsKey(databaseid + ";" + "__chat_" + toSysuserid) && subscriberAsyncRequests.get(databaseid + ";" + "__chat_" + toSysuserid).canRespond()) {
                PropertyList properties = new PropertyList();
                properties.setProperty("__createby", sapphireConnection.getSysuserId());
                properties.setProperty("__databaseid", sapphireConnection.getDatabaseId());
                properties.setProperty("subscriberid", "__chat_" + toSysuserid);
                properties.setProperty("type", "T");
                properties.setProperty("message", message);
                NotifyManager.notify(properties, false, null);
            } else {
                PropertyList properties = new PropertyList();
                properties.setProperty("__createby", sapphireConnection.getSysuserId());
                properties.setProperty("__databaseid", sapphireConnection.getDatabaseId());
                properties.setProperty("subscriberid", toSysuserid);
                properties.setProperty("type", "P");
                properties.setProperty("expiry", "U");
                properties.setProperty("message", fromSysuserid + " says: " + message);
                properties.setProperty("link", "rc?command=page&page=Chat&tosysuserid=" + fromSysuserid + "&message=" + message);
                properties.setProperty("popup", "Y");
                NotifyManager.notify(properties, false, null);
            }
        }
    }

    public static void endChat(String databaseid, String subscriberid) {
        NotifyManager.logDebug("In endChat.");
        subscriberAsyncRequests.remove(databaseid + ";" + "__chat_" + subscriberid);
    }

    public static void reset(String databaseid) {
        NotifyManager.logDebug("In reset.");
        subscriberAsyncRequests.clear();
        subscriberElementRequests.clear();
        notificationsMap.clear();
    }

    public static String getStats() {
        Object object;
        StringBuilder html = new StringBuilder();
        html.append("<table cellspacing=\"0\" border=\"1\">");
        html.append("<tr class=\"fieldtitle\"><td>Subscriber</td><td>Notification Details</td><tr>");
        for (String subscriber : subscriberAsyncRequests.keySet()) {
            object = subscriberAsyncRequests.get(subscriber);
            html.append("<tr>");
            html.append("<td>" + subscriber + "</td>");
            html.append("<td>" + ((AsyncNotify)object).toString() + "</td>");
            html.append("</tr>");
        }
        for (String subscriber : subscriberElementRequests.keySet()) {
            object = subscriberElementRequests.get(subscriber);
            html.append("<tr>");
            html.append("<td>" + subscriber + "</td>");
            html.append("<td>" + ((AbstractCollection)object).toString() + "</td>");
            html.append("</tr>");
        }
        for (String subscriber : notificationsMap.keySet()) {
            object = notificationsMap.get(subscriber);
            html.append("<tr>");
            html.append("<td>" + subscriber + "</td>");
            html.append("<td>" + ((AbstractCollection)object).toString() + "</td>");
            html.append("</tr>");
        }
        html.append("</table>");
        return html.toString();
    }
}

