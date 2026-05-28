/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.sdms.actions;

import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.gwt.shared.util.StringUtil;
import com.labvantage.sapphire.modules.sdms.SDMSConstants;
import java.util.List;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.M18NUtil;
import sapphire.xml.PropertyList;

public class AddSDIDataCapture
extends BaseAction
implements SDMSConstants {
    public static void linkSDI(List<String> sdilist, String datacaptureid, M18NUtil m18n, DBAccess dbUtil, Logger logger) throws SapphireException {
        for (String sdi : sdilist) {
            String[] parts = StringUtil.split(sdi, ";");
            if (parts.length <= 1) continue;
            AddSDIDataCapture.linkSDI(parts[0], parts[1], parts.length > 2 ? parts[2] : null, parts.length > 3 ? parts[3] : null, datacaptureid, m18n, dbUtil, logger);
        }
    }

    public static void linkSDI(String sdcid, String keyid1, String keyid2, String keyid3, String datacaptureid, M18NUtil m18n, DBAccess dbUtil, Logger logger) throws SapphireException {
        if (sdcid.equalsIgnoreCase("DataItem")) {
            dbUtil.executePreparedUpdate("UPDATE sdidataitem SET datacaptureid=? WHERE sdidataitemid=?", new Object[]{datacaptureid, keyid1});
        } else {
            DataSet toinsert = new DataSet();
            toinsert.addColumn("sdcid", 0);
            toinsert.addColumn("keyid1", 0);
            toinsert.addColumn("keyid2", 0);
            toinsert.addColumn("keyid3", 0);
            toinsert.addColumn("datacaptureid", 0);
            toinsert.addColumn("__rowstatus", 0);
            toinsert.setM18NUtil(m18n);
            toinsert.addRow();
            toinsert.setValue(0, "sdcid", sdcid);
            toinsert.setValue(0, "keyid1", keyid1);
            if (keyid2 != null && keyid2.length() > 0) {
                toinsert.setValue(0, "keyid2", keyid2);
            }
            if (keyid3 != null && keyid3.length() > 0) {
                toinsert.setValue(0, "keyid3", keyid3);
            }
            toinsert.setValue(0, "datacaptureid", datacaptureid);
            toinsert.setValue(0, "__rowstatus", "I");
            AddSDIDataCapture.saveSDIDataCapture(sdcid, toinsert, m18n, dbUtil, logger);
        }
    }

    public static void saveSDIDataCapture(String sdcid, DataSet capturedata, M18NUtil m18n, DBAccess dbUtil, Logger logger) throws SapphireException {
        DataSet toinsert = new DataSet();
        toinsert.addColumn("sdcid", 0);
        toinsert.addColumn("keyid1", 0);
        toinsert.addColumn("keyid2", 0);
        toinsert.addColumn("keyid3", 0);
        toinsert.addColumn("datacaptureid", 0);
        DataSet toupdate = (DataSet)toinsert.clone();
        for (int i = 0; i < capturedata.getRowCount(); ++i) {
            String keyid1 = capturedata.getValue(i, "keyid1", "");
            String keyid2 = capturedata.getValue(i, "keyid2", "(null)");
            String keyid3 = capturedata.getValue(i, "keyid3", "(null)");
            String datacaptureid = capturedata.getValue(i, "datacaptureid", "");
            String rowstatus = capturedata.getValue(i, "__rowstatus", "S");
            if (datacaptureid.length() > 0) {
                int r;
                if (rowstatus.equalsIgnoreCase("I")) {
                    r = toinsert.addRow();
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
                    toinsert.setValue(r, "datacaptureid", datacaptureid);
                    continue;
                }
                if (rowstatus.equalsIgnoreCase("U")) {
                    r = toupdate.addRow();
                    toupdate.setValue(r, "sdcid", sdcid);
                    toupdate.setValue(r, "keyid1", keyid1);
                    if (keyid2.length() > 0 && !keyid2.equalsIgnoreCase("(null)")) {
                        toupdate.setValue(r, "keyid2", keyid2);
                        if (keyid3.length() > 0 && !keyid3.equalsIgnoreCase("(null)")) {
                            toupdate.setValue(r, "keyid3", keyid3);
                        } else {
                            toupdate.setValue(r, "keyid3", "(null)");
                        }
                    } else {
                        toupdate.setValue(r, "keyid2", "(null)");
                        toupdate.setValue(r, "keyid3", "(null)");
                    }
                    toupdate.setValue(r, "datacaptureid", datacaptureid);
                    continue;
                }
                if (rowstatus.equalsIgnoreCase("D")) {
                    Object[] objects;
                    StringBuilder sql = new StringBuilder("DELETE sdidatacapture WHERE sdcid=? AND keyid1=? ");
                    if (keyid2.length() > 0 && !keyid2.equalsIgnoreCase("(null)")) {
                        sql.append(" AND keyid2=? ");
                        if (keyid3.length() > 0 && !keyid3.equalsIgnoreCase("(null)")) {
                            sql.append(" AND keyid3=? ");
                            objects = new Object[]{sdcid, keyid1, keyid2, keyid3, datacaptureid};
                        } else {
                            objects = new Object[]{sdcid, keyid1, keyid2, datacaptureid};
                        }
                    } else {
                        objects = new Object[]{sdcid, keyid1, datacaptureid};
                    }
                    sql.append(" AND attachmentoperationid=?");
                    try {
                        dbUtil.executePreparedUpdate(sql.toString(), objects);
                        continue;
                    }
                    catch (Exception e) {
                        logger.warn("Failed to remove attachment operation. Error - " + e.getMessage());
                        throw new SapphireException("Failed to remove SDI data capture.", e);
                    }
                }
                logger.debug("Row status not D or I thus ignore.");
                continue;
            }
            logger.debug("No capture found");
        }
        if (toinsert.getRowCount() > 0) {
            try {
                DataSetUtil.insert(dbUtil, toinsert, "sdidatacapture");
            }
            catch (Exception e) {
                logger.warn("Failed to add capture operation. Error - " + e.getMessage());
            }
        }
        if (toupdate.getRowCount() > 0) {
            try {
                DataSetUtil.update(dbUtil, toupdate, "sdidatacapture", new String[]{"sdcid", "keyid1", "keyid2", "keyid3"});
            }
            catch (Exception e) {
                logger.warn("Failed to update capture. Error - " + e.getMessage());
            }
        }
    }

    @Override
    public void processAction(PropertyList propertyList) throws SapphireException {
        M18NUtil m18n = new M18NUtil(this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()));
        AddSDIDataCapture.linkSDI(propertyList.getProperty("sdcid"), propertyList.getProperty("keyid1"), propertyList.getProperty("keyid2"), propertyList.getProperty("keyid3"), propertyList.getProperty("datacaptureid"), m18n, this.database, this.logger);
    }
}

