/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.ajax.operations;

import com.labvantage.sapphire.servlet.RequestProcessor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public abstract class BaseLabelMethodRequest
extends BaseAjaxRequest {
    static final String SDCID = "sdcid";
    static final String DEFAULT_SDCID = "Sample";
    static final String DEFAULT_VERSION = "1";
    static final String PROFILEPROPERTY_LABELPRINTER = "lastusedlabelprinter";
    static final String PROFILEPROPERTY_LABELMETHOD = "lastusedlabelmethod";
    static final String PROFILEPROPERTY_LABELMETHOD_VERSION = "lastusedlabelmethodversion";
    static final String PRINTERADDRESSID = "printeraddressid";
    static final String LABELMETHODID = "labelmethodid";
    static final String LABELMETHODVERSIONID = "labelmethodversionid";
    static final String LABELMETHODTYPE = "labelmethodtype";
    static final String DISPLAYVALUE = "displayvalue";
    static final String LABELSOURCE = "labelsource";
    static final String PRINTERS = "printers";
    static final String LABELS = "labels";
    static final String SCREENPRINTER = "***SCREENPRINTER***";
    static final String SCREENPRINTER_NAME = "Open PDF";
    static final String MESSAGE = "message";
    static final String MESSAGE_COULD_NOT_STORE_METHOD = "Could not store method as last used label method";
    static final String MESSAGE_COULD_NOT_STORE_PRINTER = "Could not store printer as last used printer";

    DataSet getLabelMethodPrinters(String userId, String method, String versionId, HttpServletRequest req, HttpServletResponse resp) {
        RequestProcessor requestProcessor = new RequestProcessor(this.getConnectionProcessor().getConnectionid());
        AjaxResponse ar = new AjaxResponse(req, resp);
        String webPageId = ar.getRequestParameter("pageid", "LV_LabelMethodPrompt");
        PropertyListCollection columns = null;
        try {
            columns = requestProcessor.getWebPageProperties(webPageId, this.getRequestContext()).getPropertyListNotNull("pagedata").getCollectionNotNull("columns");
        }
        catch (SapphireException e) {
            throw new RuntimeException(e);
        }
        PropertyList dropdowndefinition = columns.find("id", PRINTERADDRESSID).getPropertyList("dropdowndefinition");
        String querywhere = dropdowndefinition.getProperty("querywhere");
        String sql = " SELECT distinct addressid as value, coalesce(addressdesc, addressid) as displayvalue FROM labelmethod lm  JOIN ADDRESS ON address.addresstype = 'Device' AND coalesce(address.activeflag,'Y')='Y' AND (address.printertype = lm.printertype  OR lm.printertype is null OR address.printertype is null ) " + this.getUserJoinClause() + " WHERE  lm.labelmethodid = ? AND lm.labelmethodversionid = ? AND " + querywhere + this.getUserWhereClause() + this.getSecurityWhere("Address");
        sql = sql.replace("[labelmethodid]", method);
        Object[] params = new String[]{userId, method, versionId};
        return this.getQueryProcessor().getPreparedSqlDataSet(sql, params);
    }

    protected String getUserJoinClause() {
        return " LEFT JOIN departmentsysuser u ON u.sysuserid = ? AND address.printerdepartmentid = u.departmentid ";
    }

    protected String getUserWhereClause() {
        return " AND (address.printerdepartmentid = u.departmentid OR address.printerdepartmentid is null) ";
    }

    protected String getSecurityWhere(String sdcId) {
        String securitywhere = "";
        try {
            PropertyList inprops = new PropertyList();
            inprops.setProperty(SDCID, sdcId);
            new ActionProcessor(this.getSDCProcessor().getConnectionid()).processActionClass("com.labvantage.sapphire.modules.dashboard.util.DashboardSecurityWhereClause", inprops);
            securitywhere = inprops.getProperty("whereclause");
            if (securitywhere.length() > 0) {
                securitywhere = " AND " + securitywhere;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return securitywhere;
    }

    protected String getSDCIdSpecificProfileProperty(String propertyid, String sdcid) {
        return propertyid + "|" + sdcid;
    }
}

