/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.maint;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.pageelements.PropertyHandler;
import java.util.HashMap;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.error.ErrorHandler;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;

public class MaintCalendarPropertyHandler
extends PropertyHandler {
    @Override
    public void processProperties(HashMap sdiprops) throws SapphireException {
        ErrorHandler errorHandler = sdiprops.containsKey("ERRORHANDLER") && sdiprops.get("ERRORHANDLER") instanceof ErrorHandler ? (ErrorHandler)sdiprops.get("ERRORHANDLER") : new ErrorHandler();
        String dataset = sdiprops.containsKey("__calendar_data") ? sdiprops.get("__calendar_data").toString() : "";
        DataSet calendardata = null;
        if (dataset.length() > 0) {
            try {
                calendardata = new DataSet(new JSONObject(dataset));
            }
            catch (Exception e) {
                this.logDebug("Could not create attachment data.");
            }
        }
        if (calendardata != null) {
            sdiprops.remove("__calendar_data");
            String prefix = (String)sdiprops.get("__prefix");
            String sdcid = (String)sdiprops.get("__" + prefix + "sdcid");
            M18NUtil m18n = new M18NUtil(this.getConnectionInfo());
            ActionProcessor ap = new ActionProcessor(this.sapphireConnection.getConnectionId());
            SDCProcessor sdcProcessor = new SDCProcessor(this.sapphireConnection.getConnectionId());
            DataSet toinsert = new DataSet();
            toinsert.addColumn("sdcid", 0);
            toinsert.addColumn("keyid1", 0);
            toinsert.addColumn("keyid2", 0);
            toinsert.addColumn("keyid3", 0);
            toinsert.addColumn("calendarid", 0);
            DBUtil dbUtil = new DBUtil();
            dbUtil.setConnection(this.sapphireConnection);
            for (int i = 0; i < calendardata.getRowCount(); ++i) {
                String keyid1 = calendardata.getValue(i, "keyid1", "");
                String keyid2 = calendardata.getValue(i, "keyid2", "(null)");
                String keyid3 = calendardata.getValue(i, "keyid3", "(null)");
                String calendarid = calendardata.getValue(i, "calendarid", "");
                if (calendarid.length() > 0) {
                    String rowstatus = calendardata.getValue(i, "__rowstatus", "S");
                    if (rowstatus.equalsIgnoreCase("I")) {
                        int r = toinsert.addRow();
                        toinsert.setValue(r, "sdcid", sdcid);
                        toinsert.setValue(r, "keyid1", keyid1);
                        if (keyid2.length() > 0 && !keyid2.equalsIgnoreCase("(null)")) {
                            toinsert.setValue(r, "keyid2", keyid2);
                            if (keyid3.length() > 0 && !keyid3.equalsIgnoreCase("(null)")) {
                                toinsert.setValue(r, "keyid3", keyid3);
                            } else {
                                toinsert.setValue(r, "keyid3", "(null)");
                            }
                        } else {
                            toinsert.setValue(r, "keyid2", "(null)");
                            toinsert.setValue(r, "keyid3", "(null)");
                        }
                        toinsert.setValue(r, "calendarid", calendarid);
                        continue;
                    }
                    if (rowstatus.equalsIgnoreCase("D")) {
                        Object[] objects;
                        StringBuilder sql = new StringBuilder("DELETE sdicalendar WHERE sdcid=? AND keyid1=? ");
                        if (keyid2.length() > 0 && !keyid2.equalsIgnoreCase("(null)")) {
                            sql.append(" AND keyid2=? ");
                            if (keyid3.length() > 0 && !keyid3.equalsIgnoreCase("(null)")) {
                                sql.append(" AND keyid3=? ");
                                objects = new Object[]{sdcid, keyid1, keyid2, keyid3, calendarid};
                            } else {
                                objects = new Object[]{sdcid, keyid1, keyid2, calendarid};
                            }
                        } else {
                            objects = new Object[]{sdcid, keyid1, calendarid};
                        }
                        sql.append(" AND calendarid=?");
                        try {
                            dbUtil.executePreparedUpdate(sql.toString(), objects);
                            continue;
                        }
                        catch (Exception e) {
                            this.logWarn("Failed to remove calendar item. Error - " + e.getMessage());
                            throw new SapphireException("Failed to remove calendar item.", e);
                        }
                    }
                    this.logDebug("Row status not D or I thus ignore.");
                    continue;
                }
                this.logWarn("To calendar id found for calendar item");
            }
            if (toinsert.getRowCount() > 0) {
                try {
                    DataSetUtil.insert(dbUtil, toinsert, "sdicalendar");
                }
                catch (Exception e) {
                    this.logWarn("Failed to add calendar item. Error - " + e.getMessage());
                    throw new SapphireException("Failed to add calendar item.", e);
                }
            }
        }
    }
}

