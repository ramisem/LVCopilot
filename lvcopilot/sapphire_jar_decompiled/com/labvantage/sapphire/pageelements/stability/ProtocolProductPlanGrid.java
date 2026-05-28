/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.stability;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.pageelements.ElementUtil;
import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.stability.ScheduleGrid;
import com.labvantage.sapphire.tagext.QueryData;
import com.labvantage.sapphire.tagext.SDITagUtil;
import java.math.BigDecimal;
import sapphire.SapphireException;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.tagext.SDITagInfo;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeHTML;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ProtocolProductPlanGrid
extends BaseElement {
    @Override
    public String getHtml() {
        SDITagInfo sdiInfo;
        if (Trace.on) {
            this.logTrace("Initailizing protocol product plan grid from properties...");
        }
        if ((sdiInfo = this.getSDIInfo()) == null || sdiInfo.getQueryData("protocolproduct") == null || sdiInfo.getQueryData("protocolproduct").getQuerydata() == null || sdiInfo.getQueryData("protocol_scheduleplan") == null || sdiInfo.getQueryData("protocol_scheduleplan").getQuerydata() == null || sdiInfo.getQueryData("protocolprod_scheduleplan") == null || sdiInfo.getQueryData("protocolprod_scheduleplan").getQuerydata() == null || sdiInfo.getQueryData("primary") == null || sdiInfo.getQueryData("primary").getQuerydata() == null) {
            return "Protocol data not found. ProtocolProductPlan grid must be inside an SDI tag and include primary, protocolproduct, protocol_scheduleplan and protocolprod_scheduleplan in the request attribute.";
        }
        QueryData protocolQD = sdiInfo.getQueryData("primary");
        DataSet protocol = protocolQD.getQuerydata();
        if (protocol.size() != 1) {
            return "No protocol found.";
        }
        String formname = this.getSDIFormId();
        StringBuffer html = new StringBuffer();
        boolean readonly = this.element.getProperty("readonly") != null && this.element.getProperty("readonly").equals("Y");
        Button button = new Button(this.pageContext);
        PropertyList addPlanButton = this.element.getPropertyList("addplanbutton");
        PropertyList addProductButton = this.element.getPropertyList("addproductbutton");
        TranslationProcessor tp = new TranslationProcessor(this.pageContext);
        PropertyList pagedata = (PropertyList)this.pageContext.getRequest().getAttribute("pagedata");
        String enableChangeControl = "";
        try {
            PropertyList policy = this.getConfigurationProcessor().getPolicy("CMTPolicy", "ProtocolSDC Custom");
            if (policy.size() == 0) {
                policy = this.getConfigurationProcessor().getPolicy("CMTPolicy", "Sapphire Custom");
            }
            enableChangeControl = policy.getProperty("enablechangecontrol");
        }
        catch (SapphireException e) {
            this.logger.error(e.getMessage());
        }
        if ("Y".equals(enableChangeControl)) {
            String code;
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT c.checkedoutbydepartmentid,c.checkedoutbyuserid,CASE WHEN c.checkedoutbydepartmentid IS NOT NULL AND c.checkedoutbydepartmentid IN (SELECT departmentid FROM departmentsysuser WHERE sysuserid = ? ) THEN '3' ");
            sql.append("WHEN c.checkedoutbydepartmentid IS NOT NULL AND c.checkedoutbydepartmentid NOT IN (SELECT departmentid FROM departmentsysuser WHERE sysuserid = ? ) THEN '4'  ");
            sql.append("WHEN c.checkedoutbyuserid IS NOT NULL AND c.checkedoutbyuserid = ? THEN '1'  ");
            sql.append("WHEN c.checkedoutbyuserid IS NOT NULL AND c.checkedoutbyuserid != ? THEN '2'  ");
            sql.append("ELSE '' END checkinallowed  ");
            sql.append("FROM changelog c WHERE c.changelogstatus = 'Checked Out' and c.linksdcid='ProtocolSDC' and c.linkkeyid1= ? and c.linkkeyid2= ?  ");
            String currentUser = pagedata.getProperty("currentuser");
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), new Object[]{currentUser, currentUser, currentUser, currentUser, protocol.getString(0, "protocolid"), protocol.getString(0, "protocolversionid")});
            if (ds != null && ds.getRowCount() > 0 && ("2".equals(code = ds.getValue(0, "checkinallowed")) || "4".equals(code))) {
                readonly = true;
            }
        }
        html.append("<script language=\"JavaScript\" src=\"WEB-CORE/elements/scripts/protocolgrid.js\"></script>\n");
        html.append("<input type=\"hidden\" id=\"pppmode\" value=\"" + (readonly ? "Y" : "N") + "\"/>");
        html.append("<input type=\"hidden\" name=\"__propertyhandler_" + this.elementid + "\" value=\"com.labvantage.sapphire.pageelements.stability.PPPGridPropertyHandler\"/>");
        html.append("<input type=\"hidden\" id=\"__linksdcid\" name=\"__linksdcid\" value=\"" + this.element.getProperty("sdcid") + "\"/>");
        html.append("<input type=\"hidden\" id=\"linksdcid\" name=\"linksdcid\" value=\"" + this.element.getProperty("sdcid") + "\"/>");
        html.append("<table><tr>");
        html.append("<td valign=\"top\"><div id=\"PPPGridDiv\">");
        html.append("</div></td>");
        button.setText(addPlanButton != null ? tp.translate(addPlanButton.getProperty("text", "Add Plan")) : tp.translate("Add Plan"));
        button.setAction(addPlanButton != null ? addPlanButton.getProperty("js", "alert( 'JavaScript not defined for Add Plan button' );") : "alert( 'JavaScript not defined for Add Plan button' );");
        html.append("<td valign=\"top\">" + (readonly ? "&nbsp;" : button.getHtml()) + "</td>");
        html.append("</tr><tr>");
        button.setText(addProductButton != null ? tp.translate(addProductButton.getProperty("text", "Add Product")) : tp.translate("Add Product"));
        button.setAction(addProductButton != null ? addProductButton.getProperty("js", "alert( 'JavaScript not defined for Add Product button' );") : "alert( 'JavaScript not defined for Add Product button' );");
        html.append("<td>" + (readonly ? "&nbsp;" : button.getHtml()) + "</td><td>&nbsp;</td>");
        html.append("</tr></table>\n");
        String protocolid = protocol.getString(0, "protocolid");
        String protocolversionid = protocol.getString(0, "protocolversionid");
        boolean showDataDivs = this.pageContext.getRequest().getParameter("debug") != null && this.pageContext.getRequest().getParameter("debug").equals("Y");
        html.append("<script>var __currentsequence = new Array();</script>");
        html.append("<div style=\"display:" + (showDataDivs ? "block" : "none") + "\">");
        html.append(ElementUtil.getDetailHtml(new String[]{protocolid, protocolversionid}, new String[]{"protocolproductid", "usersequence", "containertypeid", "trackingtypeflag", "partialpullflag", "linksdcid", "linkkeyid1"}, "protocolproduct", sdiInfo, SDITagUtil.getInstance(this.pageContext), true, true));
        html.append("</div>\n");
        html.append("<div style=\"display:" + (showDataDivs ? "block" : "none") + "\">");
        html.append(ElementUtil.getDetailHtml(new String[]{protocolid, protocolversionid}, new String[]{"scheduleplanid", "usersequence"}, "protocol_scheduleplan", sdiInfo, SDITagUtil.getInstance(this.pageContext), true, true));
        html.append("</div>\n");
        html.append("<div style=\"display:" + (showDataDivs ? "block" : "none") + "\">");
        html.append(ElementUtil.getDetailHtml(new String[]{protocolid, protocolversionid}, new String[]{"protocolproductid", "scheduleplanid", "qtyneeded", "qtyunits", "qtyneededtype"}, "protocolprod_scheduleplan", sdiInfo, SDITagUtil.getInstance(this.pageContext), true, true));
        html.append("</div>\n");
        html.append("<script>\n");
        html.append("var nexturl = \"rc?command=" + pagedata.getProperty("command") + "&" + pagedata.getProperty("command") + "=" + pagedata.getProperty("page") + "&keyid1=" + HttpUtil.encodeURIComponent(pagedata.getProperty("keyid1")) + "&keyid2=" + HttpUtil.encodeURIComponent(pagedata.getProperty("keyid2")) + "\";\n");
        SDCProcessor sdcProcessor = new SDCProcessor(this.pageContext);
        if (this.element.getProperty("sdcid") == null || this.element.getProperty("sdcid").length() == 0) {
            this.element.setProperty("sdcid", "Product");
        }
        String sdcid = this.element.getProperty("sdcid");
        String keyid1 = sdcProcessor.getProperty(sdcid, "keycolid1");
        String desc = sdcProcessor.getProperty(sdcid, "desccol");
        DataSet productData = this.getQueryProcessor().getPreparedSqlDataSet("SELECT " + sdcProcessor.getProperty(sdcid, "tableid") + ".* FROM\tprotocolproduct, " + sdcProcessor.getProperty(sdcid, "tableid") + " WHERE\tprotocolproduct.linkkeyid1 = " + sdcProcessor.getProperty(sdcid, "tableid") + "." + sdcProcessor.getProperty(sdcid, "keycolid1") + " AND\tprotocolproduct.protocolid = ? AND\tprotocolproduct.protocolversionid = ?", new Object[]{protocolid, protocolversionid});
        html.append("var productHeader = new Array();");
        PropertyListCollection productHeader = this.element.getCollection("columns");
        for (int i = 0; i < productData.size(); ++i) {
            html.append("productHeader[\"" + SafeHTML.encodeForJavaScript(productData.getValue(i, sdcProcessor.getProperty(sdcid, "keycolid1"))) + "\"] = \"");
            if (productHeader != null && productHeader.size() > 0) {
                for (int j = 0; j < productHeader.size(); ++j) {
                    html.append(StringUtil.replaceAll(SafeHTML.encodeForJavaScript(productData.getValue(i, productHeader.getPropertyList(j).getProperty("columnid"))), "\"", "&quot") + "<br/>");
                }
            } else {
                html.append(productData.getValue(i, keyid1) + "<br/>" + StringUtil.replaceAll(SafeHTML.encodeForJavaScript(productData.getValue(i, desc)), "\"", "&quot") + "<br/>");
            }
            html.append("\";");
        }
        DataSet planData = this.getQueryProcessor().getPreparedSqlDataSet("SELECT scheduleplan.scheduleplanid, scheduleplan.scheduleplandesc FROM\tprotocol_scheduleplan, scheduleplan WHERE\tprotocol_scheduleplan.scheduleplanid = scheduleplan.scheduleplanid AND\tprotocol_scheduleplan.protocolid = ? AND    protocol_scheduleplan.protocolversionid = ?", new Object[]{protocolid, protocolversionid});
        html.append("var planHeader = new Array();");
        for (int i = 0; i < planData.size(); ++i) {
            html.append("planHeader[\"" + SafeHTML.encodeForJavaScript(planData.getValue(i, "scheduleplanid")) + "\"] = \"" + SafeHTML.encodeForJavaScript(planData.getValue(i, "scheduleplandesc")) + "\";");
        }
        SDIProcessor sdiProcessor = this.getSDIProcessor();
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDCid("Units");
        sdiRequest.setQueryFrom("units");
        sdiRequest.setRequestItem("primary");
        SDIData sdiData = sdiProcessor.getSDIData(sdiRequest);
        DataSet units = sdiData.getDataset("primary");
        html.append("function getUnitsOptions() {");
        html.append("var options = new Array();");
        html.append("options[0] = \"(Containers)\";");
        for (int i = 0; i < units.size(); ++i) {
            html.append("options[" + (i + 1) + "] = \"" + units.getValue(i, "unitsid") + "\";");
        }
        html.append("return options;}\n");
        sdiRequest.setSDCid("ContainerType");
        sdiRequest.setQueryFrom("containertype");
        sdiRequest.setQueryOrderBy("containertypeid");
        sdiRequest.setRequestItem("primary");
        sdiData = sdiProcessor.getSDIData(sdiRequest);
        DataSet containertypes = sdiData.getDataset("primary");
        html.append("var containerTypeSizeValue = new Array();\n");
        html.append("var containerTypeSizeUnits = new Array();\n");
        StringBuffer containerTypeFunction = new StringBuffer("function getContainerTypeOptions() { var options = new Array();");
        int options = 0;
        for (int i = 0; i < containertypes.size(); ++i) {
            if (containertypes.getValue(i, "sizevalue").length() <= 0 || containertypes.getValue(i, "sizeunits").length() <= 0) continue;
            html.append("containerTypeSizeValue[\"" + containertypes.getValue(i, "containertypeid") + "\"] = \"" + containertypes.getValue(i, "sizevalue") + "\"; ");
            html.append("containerTypeSizeUnits[\"" + containertypes.getValue(i, "containertypeid") + "\"] = \"" + containertypes.getValue(i, "sizeunits") + "\";\n");
            containerTypeFunction.append("options[" + options++ + "] = \"" + containertypes.getValue(i, "containertypeid") + "\";");
        }
        html.append(containerTypeFunction.append("return options;}\n"));
        DataSet planquantities = this.getQueryProcessor().getPreparedSqlDataSet("SELECT schedulecondition.scheduleplanid, qtypull, qtypullunits, qtypulltype FROM\tschedulecondition, protocol_scheduleplan WHERE\tschedulecondition.scheduleplanid = protocol_scheduleplan.scheduleplanid AND\tprotocol_scheduleplan.protocolid = ? AND\tprotocol_scheduleplan.protocolversionid = ? ORDER BY schedulecondition.scheduleplanid", new Object[]{protocolid, protocolversionid});
        html.append("var planQty = new Array();\n");
        html.append("var planUnits = new Array();\n");
        String currentPlanid = "";
        String planUnits = "";
        BigDecimal planTotal = null;
        boolean unitsMatch = true;
        boolean stdUnits = true;
        for (int i = 0; i < planquantities.size(); ++i) {
            BigDecimal qtyPull = planquantities.getBigDecimal(i, "qtypull");
            String qtyPullUnits = planquantities.getString(i, "qtypullunits");
            String qtyPullType = planquantities.getString(i, "qtypulltype");
            if (!planquantities.getString(i, "scheduleplanid").equals(currentPlanid)) {
                unitsMatch = true;
                currentPlanid = planquantities.getString(i, "scheduleplanid");
                planUnits = qtyPullUnits;
                stdUnits = qtyPullType == null || qtyPullType.equals("U");
                planTotal = null;
                if (stdUnits) {
                    if (planUnits != null && planUnits.length() > 0) {
                        planTotal = qtyPull;
                    }
                } else {
                    planTotal = qtyPull != null ? qtyPull : new BigDecimal(0);
                }
            } else {
                if (stdUnits && planUnits == null && qtyPullUnits != null) {
                    planUnits = qtyPullUnits;
                }
                if (!stdUnits || qtyPullUnits != null) {
                    if (!stdUnits || qtyPullUnits.equals(planUnits)) {
                        planTotal = stdUnits ? (planTotal != null ? planTotal.add(qtyPull != null ? qtyPull : new BigDecimal(0)) : qtyPull) : (planTotal != null ? planTotal.add(qtyPull != null ? qtyPull : new BigDecimal(0)) : (qtyPull != null ? qtyPull : new BigDecimal(0)));
                    } else {
                        unitsMatch = false;
                    }
                }
            }
            if (i != planquantities.size() - 1 && planquantities.getString(i + 1, "scheduleplanid").equals(currentPlanid)) continue;
            html.append("planQty[\"" + currentPlanid + "\"] = " + (unitsMatch && planTotal != null ? planTotal.doubleValue() : -1.0) + ";\n");
            html.append("planUnits[\"" + currentPlanid + "\"] = \"" + (unitsMatch ? (stdUnits ? planUnits : "Containers") : "") + "\";\n");
        }
        html.append("\n");
        html.append("//2 dimensional array -> productPlanContainers[planid][protocolproductid]=containers\n");
        html.append("var productPlanContainers = new Array();\n");
        ScheduleGrid grid = new ScheduleGrid(this.getConnectionId());
        DataSet dsProtocolProduct = sdiInfo.getDataSet("protocolproduct");
        StringBuffer log = new StringBuffer();
        for (int i = 0; i < planData.size(); ++i) {
            try {
                String planId = planData.getValue(i, "scheduleplanid");
                grid.planItems.clear();
                grid.retrieve(planId);
                html.append("productPlanContainers[\"" + planId + "\"] = new Array();\n");
                for (int j = 0; j < dsProtocolProduct.size(); ++j) {
                    String protocolProductId = dsProtocolProduct.getValue(j, "protocolproductid", "");
                    log.append("<table border=1 cellpadding=5 cellspacing=0 width=600>\n<tr style=\"background-color:DarkKhaki\"><td><b>Plan: " + planId + "<br>Product: " + protocolProductId + "</b></td></tr>\n</table>");
                }
                continue;
            }
            catch (Exception ex) {
                this.logger.error("Error", ex);
            }
        }
        html.append("renderGrid( '" + formname + "' );\n");
        html.append("</script>\n");
        html.append("<div id=\"inventorylog\" style=\"display:" + (showDataDivs ? "block" : "none") + "\">" + log + "</div>\n");
        return html.toString();
    }
}

