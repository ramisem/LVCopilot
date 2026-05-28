/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.elements.sdidetailmaint;

import com.labvantage.opal.elements.detailmaint.BaseItem;
import com.labvantage.opal.elements.sdidetailmaint.BaseSDIDetail;
import com.labvantage.opal.elements.sdidetailmaint.SDIAddress;
import com.labvantage.opal.elements.sdidetailmaint.SDIAlias;
import com.labvantage.opal.elements.sdidetailmaint.SDIApproval;
import com.labvantage.opal.elements.sdidetailmaint.SDIDataitem;
import com.labvantage.opal.elements.sdidetailmaint.SDIDataset;
import com.labvantage.opal.elements.sdidetailmaint.SDIDocument;
import com.labvantage.opal.elements.sdidetailmaint.SDIFormRule;
import com.labvantage.opal.elements.sdidetailmaint.SDIResourceRequirement;
import com.labvantage.opal.elements.sdidetailmaint.SDISecurityDepartment;
import com.labvantage.opal.elements.sdidetailmaint.SDISecuritySet;
import com.labvantage.opal.elements.sdidetailmaint.SDISpec;
import com.labvantage.opal.elements.sdidetailmaint.SDIWorkflowRule;
import com.labvantage.opal.elements.sdidetailmaint.SDIWorkitem;
import com.labvantage.opal.elements.sdidetailmaint.SDIWorksheetRule;
import sapphire.pageelements.BaseElement;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SDIDetailElement
extends BaseElement {
    static final String LABVANTAGE_CVS_ID = "$Revision: 65879 $";
    public static final String ELEMENTTYPE_SDIDATA = "SDIData";
    public static final String ELEMENTTYPE_SDIDATAITEM = "SDIDataItem";
    public static final String ELEMENTTYPE_SDIWORKITEM = "SDIWorkItem";
    public static final String ELEMENTTYPE_SDISPEC = "SDISpec";
    public static final String ELEMENTTYPE_SDIADDRESS = "SDIAddress";
    public static final String ELEMENTTYPE_SDIAPPROVAL = "SDIApproval";
    public static final String ELEMENTTYPE_SDIALIAS = "SDIAlias";
    public static final String ELEMENTTYPE_SDIDOCUMENT = "SDIDocument";
    public static final String ELEMENTTYPE_SDIFORMRULE = "SDIFormRule";
    public static final String ELEMENTTYPE_SDIWORKSHEETRULE = "SDIWorksheetRule";
    public static final String ELEMENTTYPE_SDIWORKFLOWRULE = "SDIWorkflowRule";
    public static final String ELEMENTTYPE_SDISECURITYSET = "SDISecuritySet";
    public static final String ELEMENTTYPE_SDISECURITYDEPARTMENT = "SDISecurityDepartment";
    public static final String ELEMENTTYPE_SDIRESOURCEREQUIREMENT = "SDIResourceRequirement";
    public String tableId = "";

    @Override
    public String getHtml() {
        BaseSDIDetail sdidetail = null;
        String elementtype = this.element.getProperty("elementtype");
        if (elementtype.equals(ELEMENTTYPE_SDIDATAITEM)) {
            sdidetail = new SDIDataitem(this.element, this.pageContext, this.requestContext, this.getConnectionId());
            this.tableId = "sdidataitem";
        } else if (elementtype.equals(ELEMENTTYPE_SDIDATA)) {
            sdidetail = new SDIDataset(this.element, this.pageContext, this.requestContext, this.getConnectionId());
            this.tableId = "sdidata";
        } else if (elementtype.equals(ELEMENTTYPE_SDIWORKITEM)) {
            sdidetail = new SDIWorkitem(this.element, this.pageContext, this.requestContext, this.getConnectionId());
            this.tableId = "sdiworkitem";
        } else if (elementtype.equals(ELEMENTTYPE_SDISPEC)) {
            sdidetail = new SDISpec(this.element, this.pageContext, this.requestContext, this.getConnectionId());
            this.tableId = "sdispec";
        } else if (elementtype.equals(ELEMENTTYPE_SDIADDRESS)) {
            sdidetail = new SDIAddress(this.element, this.pageContext, this.requestContext, this.getConnectionId());
            this.tableId = "sdiaddress";
        } else if (elementtype.equals(ELEMENTTYPE_SDIAPPROVAL)) {
            sdidetail = new SDIApproval(this.element, this.pageContext, this.requestContext, this.getConnectionId());
            this.tableId = "sdiapprovalstep";
        } else if (elementtype.equals(ELEMENTTYPE_SDIALIAS)) {
            sdidetail = new SDIAlias(this.element, this.pageContext, this.requestContext, this.getConnectionId());
            this.tableId = "sdialias";
        } else if (elementtype.equals(ELEMENTTYPE_SDIDOCUMENT)) {
            sdidetail = new SDIDocument(this.element, this.pageContext, this.requestContext, this.getConnectionId());
            this.tableId = "sdidocument";
        } else if (elementtype.equals(ELEMENTTYPE_SDIFORMRULE)) {
            sdidetail = new SDIFormRule(this.element, this.pageContext, this.requestContext, this.getConnectionId());
            this.tableId = "sdiformrule";
        } else if (elementtype.equals(ELEMENTTYPE_SDIWORKSHEETRULE)) {
            sdidetail = new SDIWorksheetRule(this.element, this.pageContext, this.requestContext, this.getConnectionId());
            this.tableId = "sdiworksheetrule";
        } else if (elementtype.equals(ELEMENTTYPE_SDIWORKFLOWRULE)) {
            sdidetail = new SDIWorkflowRule(this.element, this.pageContext, this.requestContext, this.getConnectionId());
            this.tableId = "sdiworkflowrule";
        } else if (elementtype.equals(ELEMENTTYPE_SDISECURITYSET)) {
            sdidetail = new SDISecuritySet(this.element, this.pageContext, this.requestContext, this.getConnectionId());
            this.tableId = "sdisecurityset";
        } else if (elementtype.equals(ELEMENTTYPE_SDISECURITYDEPARTMENT)) {
            sdidetail = new SDISecurityDepartment(this.element, this.pageContext, this.requestContext, this.getConnectionId());
            this.tableId = "sdisecuritydepartment";
        } else if (elementtype.equals(ELEMENTTYPE_SDIRESOURCEREQUIREMENT)) {
            sdidetail = new SDIResourceRequirement(this.element, this.pageContext, this.requestContext, this.getConnectionId());
            this.tableId = "sdiresourcerequirement";
        }
        if (sdidetail != null) {
            if (this.tableId != null && this.tableId.length() > 0) {
                sdidetail.createTimeZoneIndependentColumnList(this.tableId);
            }
            return this.getDynamicLookupVariableHtml(this.element) + (this.element.getProperty("dynamicaudit").length() > 0 ? "<script type=\"text/javascript\">sapphire.page.data.dynamicaudit_" + this.elementid + "='" + this.element.getProperty("dynamicaudit") + "';</script>" + sdidetail.getHtml() : sdidetail.getHtml());
        }
        return BaseItem.toErrorString("Element Configuration Error", "Detail Type not selected");
    }

    @Override
    public boolean isVisibleInAddMode() {
        return true;
    }

    private String getDynamicLookupVariableHtml(PropertyList element) {
        StringBuilder html = new StringBuilder();
        PropertyListCollection columns = element.getCollection("columns");
        html.append("<script>\n");
        for (int i = 0; i < columns.size(); ++i) {
            PropertyList column = columns.getPropertyList(i);
            if (!"lookup".equals(column.getProperty("mode")) || column.getPropertyList("lookuplink") == null || !"Y".equals(column.getPropertyList("lookuplink").getProperty("enablesuggest"))) continue;
            PropertyList lookuplink = column.getPropertyList("lookuplink");
            html.append("var oLUPD_" + this.elementid + "_" + column.getProperty("column") + "=" + lookuplink.toJSONString(false, false));
        }
        html.append("</script>");
        return html.toString();
    }
}

