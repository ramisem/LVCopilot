/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.system;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.pageelements.PropertyHandler;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;

public class UserSDCSecurityMaintPropertyHandler
extends PropertyHandler {
    static final String LABVANTAGE_CVS_ID = "$Revision: 77330 $";

    @Override
    public void processProperties(HashMap props) throws SapphireException {
        boolean isUser = "User".equals(props.get("sdcid"));
        String primarycolid = isUser ? "sysuserid" : "jobtypeid";
        try {
            String addrow = (String)props.get("addrow");
            String deleterow = (String)props.get("deleterow");
            JSONArray addArray = new JSONArray(addrow);
            JSONArray deleteArray = new JSONArray(deleterow);
            JSONObject deleteobj = (JSONObject)deleteArray.get(0);
            Iterator itr = deleteobj.keys();
            DBUtil db = new DBUtil();
            db.setConnection(this.sapphireConnection);
            PreparedStatement delete = db.prepareStatement("DELETE FROM " + (isUser ? "sdcsecurity" : "sdcjobtypesecurity") + " WHERE sdcid=? AND operationid=? AND " + (isUser ? "sysuserid" : "jobtypeid") + "=? AND accesstype=?");
            while (itr.hasNext()) {
                String k = (String)itr.next();
                String[] key = StringUtil.split(k, ";");
                delete.setString(1, key[0]);
                delete.setString(2, key[1]);
                delete.setString(3, key[2]);
                delete.setString(4, key[3].equals("W") ? "world" : (key[3].equals("O") ? "owner" : (key[3].equals("M") ? "member" : key[3])));
                delete.execute();
            }
            JSONObject addobj = (JSONObject)addArray.get(0);
            itr = addobj.keys();
            DataSet addDataSet = new DataSet();
            addDataSet.addColumn("sdcid", 0);
            addDataSet.addColumn("operationid", 0);
            addDataSet.addColumn(primarycolid, 0);
            addDataSet.addColumn("accesstype", 0);
            addDataSet.addColumn("createdt", 2);
            addDataSet.addColumn("createby", 0);
            addDataSet.addColumn("createtool", 0);
            while (itr.hasNext()) {
                String k = (String)itr.next();
                String[] key = StringUtil.split(k, ";");
                int row = addDataSet.addRow();
                addDataSet.setString(row, "sdcid", key[0]);
                addDataSet.setString(row, "operationid", key[1]);
                addDataSet.setString(row, primarycolid, key[2]);
                addDataSet.setString(row, "accesstype", key[3].equals("W") ? "world" : (key[3].equals("O") ? "owner" : (key[3].equals("M") ? "member" : key[3])));
                addDataSet.setDate(row, "createdt", DateTimeUtil.getNowCalendar());
                addDataSet.setString(row, "createby", this.sapphireConnection.getSysuserId());
                addDataSet.setString(row, "createtool", "SDCSecurityMaint");
            }
            DataSetUtil.insert(db, addDataSet, isUser ? "sdcsecurity" : "sdcjobtypesecurity");
        }
        catch (Exception e) {
            throw new SapphireException("Failed to save user sdc security maint grid. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.sapphireConnection.getConnectionId())), e);
        }
    }
}

