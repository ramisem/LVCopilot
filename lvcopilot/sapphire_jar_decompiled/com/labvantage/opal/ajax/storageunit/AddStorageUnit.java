/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.storageunit;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.actions.sdi.AddSDIDetail;
import com.labvantage.sapphire.actions.sdi.EditSDI;
import com.labvantage.sapphire.modules.storage.StorageUnitUtil;
import com.labvantage.sapphire.services.SapphireConnection;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class AddStorageUnit
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String message = "";
        String storageunitid = "";
        String linkkeyid1 = "";
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String storageunittype = ajaxResponse.getRequestParameter("storageunittype");
        PropertyList props = new PropertyList();
        if (OpalUtil.isNotEmpty(storageunittype)) {
            props.setProperty("storageunittype", storageunittype);
            try {
                String desccolumn;
                PropertyList propertyList;
                String sdcid;
                int maxtiallowed;
                try {
                    maxtiallowed = Integer.parseInt(ajaxResponse.getRequestParameter("maxtiallowed", "0"));
                }
                catch (NumberFormatException e) {
                    maxtiallowed = 0;
                }
                SapphireConnection sapphireConnection = this.getConnectionProcessor().getSapphireConnection();
                String departmentid = ajaxResponse.getRequestParameter("departmentid", sapphireConnection.getDefaultDepartment());
                props.setProperty("maxtiallowed", String.valueOf(maxtiallowed));
                props.setProperty("trackitem_custodialuserid", sapphireConnection.getSysuserId());
                props.setProperty("trackitem_custodialdepartmentid", departmentid);
                String description = ajaxResponse.getRequestParameter("description", "");
                if (OpalUtil.isNotEmpty(description) && OpalUtil.isNotEmpty(sdcid = (propertyList = StorageUnitUtil.getDefinition(this.getQueryProcessor(), storageunittype)).getPropertyListNotNull("template").getProperty("sdcid")) && OpalUtil.isNotEmpty(desccolumn = this.getSDCProcessor().getProperty(sdcid, "desccol"))) {
                    props.setProperty("primary_" + desccolumn.toLowerCase(), description);
                }
                this.getActionProcessor().processActionClass(com.labvantage.opal.actions.storageunit.AddStorageUnit.class.getName(), props);
                storageunitid = props.getProperty("storageunitid");
                linkkeyid1 = props.getProperty("linkkeyid1");
            }
            catch (SapphireException e) {
                message = this.getTranslationProcessor().translate("Error adding Storage Unit") + ": " + e.getMessage();
                this.logger.stackTrace(e);
            }
        } else {
            String json = ajaxResponse.getRequestParameter("props", "");
            if (OpalUtil.isNotEmpty(json)) {
                try {
                    props.setJSONString(json);
                    String sdcid = props.getProperty("primary_sdcid");
                    if (OpalUtil.isNotEmpty(sdcid)) {
                        String keycolid1 = this.getSDCProcessor().getProperty(sdcid, "keycolid1");
                        props.remove("primary_" + keycolid1);
                        this.getActionProcessor().processActionClass(com.labvantage.opal.actions.storageunit.AddStorageUnit.class.getName(), props);
                        storageunitid = props.getProperty("storageunitid");
                        linkkeyid1 = props.getProperty("linkkeyid1");
                        String boxtemplateid = props.getProperty("boxtemplateid");
                        if (OpalUtil.isNotEmpty(boxtemplateid)) {
                            String boxtemplatestorageunitid = OpalUtil.getColumnValue(this.getQueryProcessor(), "storageunit", "storageunitid", "linksdcid = 'LV_Box' and linkkeyid1 = ?", new String[]{boxtemplateid});
                            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select arraylayoutid, arraylayoutversionid from storageunit where storageunitid = ? and arraylayoutid is not null and arraylayoutversionid is not null", (Object[])new String[]{boxtemplatestorageunitid});
                            if (OpalUtil.isNotEmpty(ds)) {
                                props.clear();
                                props.setProperty("sdcid", "StorageUnitSDC");
                                props.setProperty("keyid1", storageunitid);
                                props.setProperty("arraylayoutid", ds.getString(0, "arraylayoutid"));
                                props.setProperty("arraylayoutversionid", ds.getString(0, "arraylayoutversionid"));
                                this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
                            }
                            if (OpalUtil.isNotEmpty(ds = this.getQueryProcessor().getPreparedSqlDataSet("select restrictionbasedon, propertyid, propertyvalue, operator, failuremessage, usersequence from storagerestriction where storageunitid = ? and activeflag = 'Y'", (Object[])new String[]{boxtemplatestorageunitid}))) {
                                for (int i = 0; i < ds.size(); ++i) {
                                    props.clear();
                                    props.setProperty("sdcid", "StorageUnitSDC");
                                    props.setProperty("keyid1", storageunitid);
                                    props.setProperty("linkid", "Storage Restrictions");
                                    props.setProperty("restrictionbasedon", ds.getString(i, "restrictionbasedon", ""));
                                    props.setProperty("propertyid", ds.getString(i, "propertyid", ""));
                                    props.setProperty("propertyvalue", ds.getString(i, "propertyvalue", ""));
                                    props.setProperty("operator", ds.getString(i, "operator", ""));
                                    props.setProperty("failuremessage", ds.getString(i, "failuremessage", ""));
                                    props.setProperty("activeflag", "Y");
                                    props.setProperty("usersequence", ds.getValue(i, "usersequence", ""));
                                    this.getActionProcessor().processActionClass(AddSDIDetail.class.getName(), props);
                                }
                            }
                        }
                    }
                }
                catch (JSONException | ActionException e) {
                    message = this.getTranslationProcessor().translate("Error adding Storage Unit") + ": " + e.getMessage();
                    this.logger.stackTrace(e);
                }
            }
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.addCallbackArgument("storageunitid", storageunitid);
        ajaxResponse.addCallbackArgument("linkkeyid1", linkkeyid1);
        ajaxResponse.print();
    }
}

