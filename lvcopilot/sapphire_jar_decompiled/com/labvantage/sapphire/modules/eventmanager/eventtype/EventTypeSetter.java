/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eventmanager.eventtype;

import com.labvantage.sapphire.modules.eventmanager.eventtype.BaseEventType;
import com.labvantage.sapphire.services.SapphireConnection;
import sapphire.SapphireException;
import sapphire.error.ErrorHandler;

public class EventTypeSetter {
    public static void startEvent(BaseEventType eventType, SapphireConnection sapphireConnection, String loggerName) {
        eventType.startEventType(sapphireConnection, loggerName);
    }

    public static void startEvent(BaseEventType eventType, SapphireConnection sapphireConnection, String loggerName, ErrorHandler errorHandler) {
        eventType.startEventType(sapphireConnection, loggerName, errorHandler);
    }

    public static void endEvent(BaseEventType eventType) throws SapphireException {
        eventType.endEventType();
    }
}

