/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdi;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import java.util.Calendar;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.DAMProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class AddCategoryItem
extends BaseAction
implements sapphire.action.AddCategoryItem {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        block17: {
            boolean deleterset = false;
            String rsetid = properties.getProperty("rsetid");
            DAMProcessor dam = null;
            DataSet categoryitems = null;
            DataSet newadd = null;
            String sdcid = properties.getProperty("sdcid");
            if (rsetid.length() == 0) {
                boolean applylock = properties.getProperty("applylock").equals("Y");
                dam = this.getDAMProcessor();
                rsetid = applylock ? dam.createLockedRSet(sdcid, properties.getProperty("keyid1"), "", "") : dam.createRSet(sdcid, properties.getProperty("keyid1"), "", "");
                deleterset = true;
            }
            if (rsetid.length() > 0) {
                try {
                    newadd = new DataSet();
                    categoryitems = new DataSet();
                    String selectCategoryItems = "SELECT categoryitem.sdcid, categoryitem.keyid1, categoryitem.categoryid FROM categoryitem, rsetitems WHERE rsetitems.sdcid = ? AND rsetitems.rsetid = ? AND rsetitems.sdcid = categoryitem.sdcid AND rsetitems.keyid1 = categoryitem.keyid1 ORDER BY categoryitem.sdcid, categoryitem.keyid1, categoryitem.categoryid";
                    try {
                        this.database.createPreparedResultSet(selectCategoryItems, new Object[]{sdcid, rsetid});
                        categoryitems.setResultSet(this.database.getResultSet());
                    }
                    catch (SapphireException se) {
                        throw new SapphireException("CREATE_RESULTSET_FAILED", "Failed to get result set for: " + selectCategoryItems, se);
                    }
                    String separator = properties.getProperty("separator", ";");
                    boolean propsmatch = StringUtil.getYN(properties.getProperty("propsmatch"), "N").equals("Y");
                    HashMap<String, String> findmap = new HashMap<String, String>();
                    String[] keyid1prop = StringUtil.split(properties.getProperty("keyid1"), separator);
                    String categoryprop = properties.getProperty("categoryid").trim();
                    if (categoryprop == null || categoryprop.length() == 0) {
                        throw new SapphireException("INVALID_PROPERTIES", "There must be a categoryid for each item");
                    }
                    String[] categoryidprop = StringUtil.split(categoryprop, separator);
                    if (categoryidprop.length > 0) {
                        String keyid1 = "";
                        String categoryid = "";
                        Calendar now = DateTimeUtil.getNowCalendar();
                        for (int sdi = 0; sdi < (propsmatch ? 1 : keyid1prop.length); ++sdi) {
                            for (int category = 0; category < categoryidprop.length; ++category) {
                                keyid1 = propsmatch ? keyid1prop[category] : keyid1prop[sdi];
                                findmap.put("keyid1", keyid1);
                                categoryid = categoryidprop[category];
                                findmap.put("categoryid", categoryid);
                                int findrow = categoryitems.findRow(findmap);
                                if (findrow != -1) continue;
                                int newrow = newadd.addRow();
                                newadd.setString(newrow, "sdcid", sdcid);
                                newadd.setString(newrow, "keyid1", keyid1);
                                newadd.setString(newrow, "categoryid", categoryid);
                                newadd.setString(newrow, "createby", this.connectionInfo.getSysuserId());
                                newadd.setString(newrow, "createtool", "AddCategoryItem");
                                newadd.setDate(newrow, "createdt", now);
                                newadd.setString(newrow, "modby", this.connectionInfo.getSysuserId());
                                newadd.setString(newrow, "modtool", "AddCategoryItem");
                                newadd.setDate(newrow, "moddt", now);
                            }
                        }
                    } else {
                        throw new SapphireException("INVALID_PROPERTIES", "There must be a categoryid for each item");
                    }
                    this.logger.info("Processing the categoryitem inserts: " + newadd);
                    try {
                        DataSetUtil.insert(this.database, newadd, "categoryitem");
                        break block17;
                    }
                    catch (SapphireException se) {
                        throw new SapphireException("DB_INSERT_FAILED", "Failed to update categoryitem: " + se.getMessage(), se);
                    }
                }
                catch (Exception e) {
                    throw new SapphireException("Error: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
                }
                finally {
                    if (deleterset) {
                        dam.clearRSet(rsetid);
                    }
                    newadd.reset();
                    categoryitems.reset();
                }
            }
            throw new SapphireException("CREATE_RSET_FAILURE", "Failed to create RSET.");
        }
    }
}

