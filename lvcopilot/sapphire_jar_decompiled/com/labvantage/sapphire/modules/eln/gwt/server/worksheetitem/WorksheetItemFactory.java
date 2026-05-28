/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItem;
import com.labvantage.sapphire.services.SapphireConnection;
import java.util.HashMap;
import sapphire.SapphireException;

public class WorksheetItemFactory {
    public static WorksheetItem getIncludesInstance(SapphireConnection sapphireConnection, String classname) throws SapphireException {
        return WorksheetItem.getInstance(sapphireConnection, classname);
    }

    public static WorksheetItem getIndexingInstance(DBUtil database, HashMap rowdata) throws SapphireException {
        return WorksheetItem.getInstance(null, database, rowdata, "-1");
    }

    public static WorksheetItem getInstance(SapphireConnection sapphireConnection, DBUtil database, HashMap rowdata) throws SapphireException {
        return WorksheetItem.getInstance(sapphireConnection, database, rowdata, "-1");
    }

    public static WorksheetItem getRenderingInstance(SapphireConnection sapphireConnection, HashMap rowdata, String width) throws SapphireException {
        return WorksheetItem.getInstance(sapphireConnection, null, rowdata, width);
    }
}

