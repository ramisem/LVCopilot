/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.stability;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.pageelements.PropertyHandler;
import com.labvantage.sapphire.stability.ScheduleGrid;
import java.util.HashMap;
import sapphire.SapphireException;

public class StabilityGridPropertyHandler
extends PropertyHandler {
    public static final String GRID = "grid";

    @Override
    public void processProperties(HashMap props) throws SapphireException {
        ScheduleGrid grid = (ScheduleGrid)props.get(GRID);
        DBUtil db = new DBUtil();
        try {
            grid.initGrid(this.connectionInfo);
            db.setConnection(this.sapphireConnection);
            grid.save(this.sapphireConnection, db);
        }
        catch (Exception e) {
            throw new SapphireException("DB_ACTION_FAILED", "Failed to save ScheduleGrid.", e);
        }
        finally {
            db.reset();
        }
    }
}

