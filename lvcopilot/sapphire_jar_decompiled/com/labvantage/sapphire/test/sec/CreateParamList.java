/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.test.sec;

import com.labvantage.sapphire.Trace;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.action.BaseAction;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class CreateParamList
extends BaseAction {
    public static final String PROPERTY_PARAMLISTID = "paramlistid";
    public static final String PROPERTY_PARAMNAME = "paramname";

    @Override
    public void processAction(PropertyList propertyList) throws SapphireException {
        String paramNames = propertyList.getProperty(PROPERTY_PARAMNAME, "");
        String paramListIds = propertyList.getProperty(PROPERTY_PARAMLISTID, "");
        if (paramNames.length() == 0 || paramListIds.length() == 0) {
            throw new ActionException("Invalid paramname or paramlistid");
        }
        String[] paramListId = StringUtil.split(paramListIds, ";");
        String[] paramName = StringUtil.split(paramNames, ";");
        String response = "";
        for (int i = 0; i < paramListId.length; ++i) {
            String sql = "SELECT count(*) FROM param where paramid='" + paramName[i] + "'";
            int count = this.database.getCount(sql);
            if (count == 0) {
                HashMap<String, String> paramProps = new HashMap<String, String>();
                paramProps.put("sdcid", "Param");
                paramProps.put("keyid1", paramName[i]);
                Trace.logInfo("Adding param with paramprops: " + paramProps.toString());
                try {
                    this.getActionProcessor().processAction("AddSDI", "1", paramProps);
                }
                catch (Exception e) {
                    Trace.logInfo("Failed to add param " + e.getMessage());
                    throw new ActionException("Failed to add Param:" + paramName[i]);
                }
            }
            if ((count = this.database.getCount(sql = "SELECT count(*) FROM paramlist where paramlistid='" + paramListId[i] + "'")) > 0) {
                throw new ActionException("ParamList already exists");
            }
            HashMap<String, String> paramListProps = new HashMap<String, String>();
            paramListProps.put("sdcid", "ParamList");
            paramListProps.put("keyid1", paramListId[i]);
            paramListProps.put("keyid2", "1");
            paramListProps.put("keyid3", "1");
            paramListProps.put("modifiableflag", "Y");
            this.getActionProcessor().processAction("AddSDI", "1", paramListProps);
            HashMap<String, String> detailProps = new HashMap<String, String>();
            detailProps.put("sdcid", "ParamList");
            detailProps.put("keyid1", paramListId[i]);
            detailProps.put("keyid2", "1");
            detailProps.put("keyid3", "1");
            detailProps.put("datatypes", "T");
            detailProps.put("linkid", "param list items");
            detailProps.put("mandatoryflag", "Y");
            detailProps.put("numreplicates", "1");
            detailProps.put("paramid", paramName[i]);
            detailProps.put("paramtype", "Standard");
            this.getActionProcessor().processAction("AddSDIDetail", "1", detailProps);
            response = response + "\n Created ParamList with id : " + paramListId[i] + " and added param " + paramName[i] + "\n";
        }
        propertyList.setProperty("responsemessage", response);
    }
}

