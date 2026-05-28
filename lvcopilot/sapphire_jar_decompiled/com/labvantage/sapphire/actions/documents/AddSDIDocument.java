/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.documents;

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

public class AddSDIDocument
extends BaseAction
implements sapphire.action.AddSDIDocument {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String sdcid = properties.getProperty("sdcid");
        String rsetid = properties.getProperty("rsetid");
        if (rsetid == null) {
            rsetid = "";
        }
        boolean deleterset = false;
        DAMProcessor dam = this.getDAMProcessor();
        if (rsetid.length() == 0) {
            boolean applylock = properties.getProperty("applylock").equals("Y");
            rsetid = applylock ? dam.createLockedRSet(sdcid, properties.getProperty("keyid1"), properties.getProperty("keyid2"), properties.getProperty("keyid3")) : dam.createRSet(sdcid, properties.getProperty("keyid1"), properties.getProperty("keyid2"), properties.getProperty("keyid3"));
            if (rsetid.length() == 0) {
                throw new SapphireException("CREATE_RSET_FAILURE", "Failed to create RSET for edit");
            }
            deleterset = true;
        }
        String select = "SELECT\tsdidocument.sdcid, sdidocument.keyid1, sdidocument.keyid2, sdidocument.keyid3, sdidocument.documentid, sdidocument.documentversionid, sdidocument.usersequence FROM\tsdidocument, rsetitems WHERE\trsetitems.sdcid = ? AND \t\trsetitems.rsetid = ? AND \t\trsetitems.sdcid = sdidocument.sdcid AND \t\trsetitems.keyid1 = sdidocument.keyid1 AND \t\trsetitems.keyid2 = sdidocument.keyid2 AND \t\trsetitems.keyid3 = sdidocument.keyid3 ORDER BY sdidocument.keyid1, sdidocument.keyid2, sdidocument.keyid3, sdidocument.documentid, sdidocument.documentversionid";
        this.database.createPreparedResultSet(select, new Object[]{sdcid, rsetid});
        DataSet sdidocument = new DataSet();
        sdidocument.setResultSet(this.database.getResultSet());
        DataSet insert = new DataSet();
        insert.addColumn("sdcid", 0);
        insert.addColumn("keyid1", 0);
        insert.addColumn("keyid2", 0);
        insert.addColumn("keyid3", 0);
        insert.addColumn("documentid", 0);
        insert.addColumn("documentversionid", 0);
        insert.addColumn("createby", 0);
        insert.addColumn("createdt", 2);
        insert.addColumn("createtool", 0);
        insert.addColumn("modby", 0);
        insert.addColumn("moddt", 2);
        insert.addColumn("modtool", 0);
        String[] keyid1prop = StringUtil.split(properties.getProperty("keyid1"), ";");
        String[] keyid2prop = StringUtil.split(properties.getProperty("keyid2"), ";");
        String[] keyid3prop = StringUtil.split(properties.getProperty("keyid3"), ";");
        String[] documentidprop = StringUtil.split(properties.getProperty("documentid"), ";");
        String[] documentversionidprop = StringUtil.split(properties.getProperty("documentversionid"), ";");
        if (keyid1prop.length == documentidprop.length && documentidprop.length == documentversionidprop.length) {
            Calendar now = DateTimeUtil.getNowCalendar();
            HashMap<String, String> findMap = new HashMap<String, String>();
            for (int i = 0; i < keyid1prop.length; ++i) {
                findMap.put("keyid1", keyid1prop[i]);
                if (keyid2prop.length > i && keyid2prop[i].length() > 0) {
                    findMap.put("keyid2", keyid2prop[i]);
                }
                if (keyid3prop.length > i && keyid3prop[i].length() > 0) {
                    findMap.put("keyid3", keyid3prop[i]);
                }
                findMap.put("documentid", documentidprop[i]);
                findMap.put("documentversionid", documentversionidprop[i]);
                int row = sdidocument.findRow(findMap);
                if (row >= 0) continue;
                row = insert.addRow();
                insert.setString(row, "sdcid", sdcid);
                insert.setString(row, "keyid1", keyid1prop[i]);
                insert.setString(row, "keyid2", keyid2prop.length > i && keyid2prop[i].length() > 0 ? keyid2prop[i] : "(null)");
                insert.setString(row, "keyid3", keyid3prop.length > i && keyid3prop[i].length() > 0 ? keyid3prop[i] : "(null)");
                insert.setString(row, "documentid", documentidprop[i]);
                insert.setString(row, "documentversionid", documentversionidprop[i]);
                insert.setString(row, "createby", this.connectionInfo.getSysuserId());
                insert.setDate(row, "createdt", now);
                insert.setString(row, "createtool", this.connectionInfo.getTool());
                insert.setString(row, "modby", this.connectionInfo.getSysuserId());
                insert.setDate(row, "moddt", now);
                insert.setString(row, "modtool", this.connectionInfo.getTool());
            }
            DataSetUtil.insert(this.database, insert, "sdidocument");
        }
        if (deleterset) {
            dam.clearRSet(rsetid);
        }
    }
}

