/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.maint;

import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.ajax.operations.AddSDI;
import com.labvantage.sapphire.pageelements.ElementUtil;
import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.pageelements.controls.Tab;
import com.labvantage.sapphire.pageelements.maint.MaintElement;
import com.labvantage.sapphire.tagext.QueryData;
import com.labvantage.sapphire.tagext.SDITagUtil;
import javax.servlet.jsp.PageContext;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.tagext.SDITagInfo;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeHTML;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class MaintDetail
extends MaintElement {
    private String type;
    private String prKeyid1;
    private String datasetcode;
    private PropertyList sdc;
    private PropertyList linksdc;
    private PropertyList link;

    public MaintDetail() {
    }

    public MaintDetail(PageContext pageContext, SDITagInfo sdiInfo, String connectionid) {
        this.init(pageContext, sdiInfo, connectionid, this.datasetname);
    }

    @Override
    public String getHtml() {
        TranslationProcessor tp = this.getTranslationProcessor();
        this.type = this.elementType.toLowerCase();
        if (this.type.equalsIgnoreCase("role") || this.type.equalsIgnoreCase("category")) {
            this.datasetname = this.type;
        }
        ElementUtil.setSdcPropertyCache(this.pageContext, this.getConnectionId(), this.sdiInfo.getSdcid(), "sdc");
        this.sdc = (PropertyList)this.pageContext.getAttribute("sdc");
        if (this.sdc == null) {
            return "SDC properties not found for sdcid: " + this.element.getProperty("sdcid");
        }
        PropertyListCollection links = this.sdc.getCollection("links");
        if ((links == null || links.size() == 0) && this.type.equals("manytomany")) {
            return "No link data found for sdcid: " + this.element.getProperty("sdcid");
        }
        DataSet primary = this.sdiInfo.getDataSet("primary");
        if (primary != null && primary.size() > 0) {
            this.prKeyid1 = primary.getString(0, this.sdc.getProperty("keycolid1"));
            if (this.prKeyid1 == null && "true".equalsIgnoreCase(this.requestContext.getProperty("newAddMode"))) {
                this.prKeyid1 = this.requestContext.getProperty("keyid1");
            }
        } else {
            return "Primary data must be specified in request";
        }
        if (primary.getValue(0, "__lockedby") != null && primary.getValue(0, "__lockedby").length() > 0) {
            this.element.setProperty("readonly", "Y");
        }
        String whereClause = this.element.getProperty("appflagonly").equals("Y") ? "appflag='Y'" : (this.element.getProperty("appflag").equals("Y") ? "appflag = 'Y'" : (this.element.getProperty("appflag").equals("N") ? "coalesce(appflag, 'N') = 'N'" : ""));
        if (this.type.equals("manytomany")) {
            boolean found = false;
            for (int i = 0; i < links.size() && !found; ++i) {
                this.link = links.getPropertyList(i);
                if (!this.link.getProperty("linkid").equals(this.element.getProperty("linkid"))) continue;
                this.datasetcode = this.datasetname = this.link.getProperty("linktableid");
                SDCProcessor sdcProcessor = new SDCProcessor(this.pageContext);
                this.linksdc = sdcProcessor.getPropertyList(this.link.getProperty("linksdcid"));
                found = true;
            }
            if (!found) {
                return "linkid property does not match an sdclink for sdc: " + this.element.getProperty("sdcid");
            }
        } else if (this.type.equals("role")) {
            this.datasetcode = "rl";
            this.link = new PropertyList();
            this.link.setProperty("linktype", "M");
            this.linksdc = new PropertyList();
            this.linksdc.setProperty("sdcid", "Role");
            this.linksdc.setProperty("tableid", "role");
            this.linksdc.setProperty("keycolid1", "roleid");
            this.linksdc.setProperty("singular", "role");
        } else if (this.type.equals("category")) {
            this.datasetcode = "ct";
            this.link = new PropertyList();
            this.link.setProperty("linktype", "M");
            this.linksdc = new PropertyList();
            this.linksdc.setProperty("sdcid", "Category");
            this.linksdc.setProperty("tableid", "category");
            this.linksdc.setProperty("keycolid1", "categoryid");
            this.linksdc.setProperty("singular", "category");
            whereClause = "sdcid = '" + this.sdiInfo.getSdcid() + "'";
        } else {
            return this.type + " type details are not supported";
        }
        if (this.link.getProperty("linktype").equals("D")) {
            return "Detail table links are not supported";
        }
        if (this.link.getProperty("linktype").equals("M")) {
            String click;
            DataSet linkData = new DataSet();
            StringBuffer html = new StringBuffer();
            try {
                SDIRequest sdiRequest = new SDIRequest();
                sdiRequest.setSDCid(this.linksdc.getProperty("sdcid"));
                sdiRequest.setQueryFrom(this.linksdc.getProperty("tableid"));
                sdiRequest.setQueryOrderBy(this.linksdc.getProperty("keycolid1"));
                if (whereClause.length() > 0) {
                    sdiRequest.setQueryWhere(whereClause);
                }
                sdiRequest.setRequestItem("primary");
                SDIData sdiData = new SDIProcessor(this.pageContext).getSDIData(sdiRequest);
                if (sdiData != null) {
                    linkData = sdiData.getDataset("primary");
                }
            }
            catch (Exception e) {
                this.logger.warn("Error while fetching many2many data with SDIProcessor: " + e.getMessage());
                QueryProcessor qp = new QueryProcessor(this.pageContext);
                linkData = qp.getSqlDataSet("SELECT * FROM " + this.linksdc.getProperty("tableid") + (whereClause.length() > 0 ? " WHERE " + whereClause : "") + " ORDER BY " + this.linksdc.getProperty("keycolid1"));
            }
            int checkboxCols = 1;
            try {
                checkboxCols = Integer.parseInt(this.element.getProperty("checkboxcols"));
            }
            catch (NumberFormatException nfe) {
                this.logger.warn(nfe.getMessage());
            }
            boolean readonly = this.element.getProperty("readonly").equals("Y");
            if (this.element.getProperty("showselectall", "N").equalsIgnoreCase("Y")) {
                html.append("<div style=\"padding-left:2px;\">");
                html.append("<input type=\"checkbox\"").append(readonly ? " disabled" : "").append(" name=\"").append(this.datasetname).append("selectall\" id=\"cbx_").append(this.datasetname).append("_selectall\" onclick=\"").append("selectAll" + this.datasetname).append("( this )\">").append(this.getTranslationProcessor().translate("Select/Unselect All"));
                html.append("</div>");
            }
            html.append("\n<table class=\"").append("maintdetail_table").append("\"").append(this.element.getProperty("customtablestyle", "").length() > 0 ? " style=\"" + this.element.getProperty("customtablestyle") + "\"" : (this.browser.isWebkit() ? " style=\"border-collapse: separate;\"" : "")).append(" cellpadding=\"3\" cellspacing=\"0\" id=\"").append(this.datasetname).append("_cbxtable\">\n");
            int i = 0;
            String string = click = this.element.getProperty("customclick", "").length() > 0 ? this.element.getProperty("customclick", "") : "update" + this.datasetname + "Table";
            while (i < linkData.size()) {
                html.append("<tr>");
                for (int j = 0; j < checkboxCols && i < linkData.size(); ++i, ++j) {
                    html.append("<td><input type=\"checkbox\"").append(readonly ? " disabled" : "").append(" name=\"").append(this.datasetname).append("selector\" id=\"cbx_").append(this.datasetname).append(";").append(SafeHTML.encodeForHTMLAttribute(linkData.getString(i, this.linksdc.getProperty("keycolid1")))).append("\" ").append(this.linksdc.getProperty("keycolid1")).append("=\"").append(SafeHTML.encodeForHTMLAttribute(linkData.getString(i, this.linksdc.getProperty("keycolid1")))).append("\" rowid=\"\" onclick=\"").append(click).append("( this )\" />");
                    html.append("<label for=\"cbx_").append(this.datasetname).append(";").append(SafeHTML.encodeForHTMLAttribute(linkData.getString(i, this.linksdc.getProperty("keycolid1")))).append("\">").append(SafeHTML.encodeForHTML(tp.translate(linkData.getString(i, this.linksdc.getProperty("keycolid1"))))).append(!this.element.getProperty("appflagonly").equals("Y") && "Y".equals(linkData.getString(i, "appflag")) ? " (App)" : "").append("</label>&nbsp;&nbsp;&nbsp;</td>");
                }
                html.append("</tr>");
            }
            html.append("<tr id=\"").append(this.datasetname).append("_newcbxrow\"></tr>");
            html.append("</table><br>");
            if (!this.element.getProperty("readonly").equals("Y") && this.element.getProperty("allowadd").equals("Y")) {
                Button addButton = new Button(this.pageContext);
                addButton.setId("add" + this.datasetname);
                addButton.setImg("WEB-CORE/images/gif/Add.gif");
                addButton.setText(tp.translate("Add " + this.linksdc.getProperty("singular")));
                if (this.element.getProperty("customadd", "").length() > 0) {
                    addButton.setAction(this.element.getProperty("customadd", "") + "( '" + tp.translate("Add " + this.linksdc.getProperty("singular")) + "', '" + tp.translate("Enter a name for the new") + " " + this.linksdc.getProperty("singular") + ":', '" + this.linksdc.getProperty("sdcid") + "' )");
                } else {
                    addButton.setAction("add" + this.datasetname + "Item( '" + tp.translate("Add " + this.linksdc.getProperty("singular")) + "', '" + tp.translate("Enter a name for the new") + " " + this.linksdc.getProperty("singular") + ":', '" + EncryptDecrypt.obfsql(this.linksdc.getProperty("sdcid")) + "' )");
                }
                html.append(addButton.getHtml());
            }
            html.append("<div style=\"display:none\">");
            html.append("<table id=\"").append(this.datasetname).append("table\">");
            QueryData queryData = this.sdiInfo.getQueryData(this.datasetname);
            queryData.resetRow(-1);
            html.append(SDITagUtil.getFixedRowInputs(this.datasetname, this.sdiInfo.getDataSet(this.datasetname).getColumns(), this.sdiInfo.getRowCount(this.datasetname), ""));
            while (queryData.nextRow(-1)) {
                html.append(this.getManyToManyRowHtml(queryData, this.linksdc));
            }
            queryData.setTemplateGenerate();
            html.append(SDITagUtil.getTemplateRowStart(this.datasetname));
            html.append(this.getManyToManyRowHtml(queryData, this.linksdc));
            html.append(SDITagUtil.getTemplateRowEnd());
            html.append("</table></div>");
            html.append(this.getJavaScript());
            PropertyList tabprops = this.element.getPropertyList("tab");
            if (tabprops != null && !tabprops.getProperty("show").equals("N")) {
                this.tab = new Tab();
                ElementUtil.setTabProperties(this.tab, tabprops, this.datasetname, this.getTranslationProcessor());
                this.tab.setContent(html.toString());
                return this.tab.getHtml();
            }
            return html.toString();
        }
        return "Links of type " + this.link.getProperty("linktype") + " cannot be rendered";
    }

    private String getManyToManyRowHtml(QueryData queryData, PropertyList linksdc) {
        this.setRowKeyCols();
        this.setRowKeyids();
        StringBuffer html = new StringBuffer();
        String rowid = queryData.getRowId(queryData.getCurrentRow());
        html.append(SDITagUtil.getRepeatedRowInputs(this.datasetname, this.keycols, this.sdiInfo.getQueryData(this.datasetname), "", "", 1));
        html.append("<tr id=\"").append(this.datasetname).append(rowid).append("\">");
        html.append("<td id=\"").append(this.datasetname).append(";").append(SafeHTML.encodeForHTMLAttribute(queryData.getValue(linksdc.getProperty("keycolid1"), ""))).append("\" rowid=\"").append(rowid).append("\">&nbsp;</td>");
        if (this.type.equals("manytomany")) {
            html.append("<td><input type=\"text\" name=\"").append(this.datasetname).append(rowid).append("_").append(this.sdc.getProperty("keycolid1")).append("\" id=\"").append(this.datasetname).append(rowid).append("_").append(this.sdc.getProperty("keycolid1")).append("\" value=\"").append(this.prKeyid1).append("\"/></td>");
            html.append("<td><input type=\"text\" name=\"").append(this.datasetname).append(rowid).append("_").append(linksdc.getProperty("keycolid1")).append("\" id=\"").append(this.datasetname).append(rowid).append("_").append(linksdc.getProperty("keycolid1")).append("\" value=\"").append(SafeHTML.encodeForHTMLAttribute(queryData.getValue(linksdc.getProperty("keycolid1"), ""))).append("\"/></td>");
        } else if (this.datasetname.equals("role")) {
            html.append("<td><input type=\"text\" name=\"rl").append(rowid).append("_roleid\" id=\"rl").append(rowid).append("_roleid\" value=\"").append(SafeHTML.encodeForHTMLAttribute(queryData.getValue("roleid", ""))).append("\"/></td>");
            html.append("<td><input type=\"text\" name=\"rl").append(rowid).append("_keyid1\" id=\"rl").append(rowid).append("_keyid1\" value=\"").append(this.prKeyid1).append("\"/></td>");
            html.append("<td><input type=\"text\" name=\"rl").append(rowid).append("_privid\" id=\"rl").append(rowid).append("_privid\" value=\"list\"/></td>");
        } else if (this.datasetname.equals("category")) {
            html.append("<td><input type=\"text\" name=\"ct").append(rowid).append("_categoryid\" id=\"ct").append(rowid).append("_categoryid\" value=\"").append(SafeHTML.encodeForHTMLAttribute(queryData.getValue("categoryid", ""))).append("\"/></td>");
            html.append("<td><input type=\"text\" name=\"ct").append(rowid).append("_keyid1\" id=\"ct").append(rowid).append("_keyid1\" value=\"").append(this.prKeyid1).append("\"/></td>");
        }
        html.append("</tr>");
        return html.toString();
    }

    private String getJavaScript() {
        StringBuffer html = new StringBuffer();
        html.append("<script type=\"text/javascript\">");
        html.append("var sanitizeHTML = function (str) {\n\tvar temp = document.createElement('div');\n\ttemp.textContent = str;\n\treturn temp.innerHTML;\n};");
        if (this.element.getProperty("customadd", "").length() == 0) {
            html.append("\n");
            html.append("function add").append(this.datasetname).append("Item(title, label, sdcid){\n");
            html.append("if (typeof(sapphire.ui) != 'undefined'){\n");
            html.append("if(top.maint_iframe==undefined && parent.maint_iframe!=undefined){\n");
            html.append("var oDialog = sapphire.ui.dialog.prompt(title, label, '', 'parent.maint_iframe.add").append(this.datasetname).append("Item_Callback', true);\n");
            html.append("}else{\n");
            html.append("var oDialog = sapphire.ui.dialog.prompt(title, label, '', window.frameElement.name + '.add").append(this.datasetname).append("Item_Callback', true);\n");
            html.append("}\n");
            html.append("oDialog.sdcid = sdcid;\n");
            html.append("}\n");
            html.append("else{\n");
            html.append("var sValue = window.prompt(label, '');\n");
            html.append("add").append(this.datasetname).append("Item_Callback({'sdcid': sdcid}, sValue);\n");
            html.append("}\n");
            html.append("}\n");
            html.append("function add").append(this.datasetname).append("Item_Callback(oDialog, sValue){\n");
            html.append("if (sValue != null && sValue.length > 0){\n");
            html.append("var callprops = {'add': 'Y', 'sdcid': oDialog.sdcid, 'keyid1': sValue" + (this.element.getProperty("appflag").equals("Y") ? ",'appflag':'Y' " : "") + "};\n");
            html.append("if (oDialog.sdcid == '" + EncryptDecrypt.obfsql("Category") + "'){\n");
            html.append("callprops.keyid2 = '").append(this.sdiInfo.getSdcid()).append("';\n");
            html.append("}\n");
            html.append("sapphire.ajax.callClass('").append(AddSDI.class.getName()).append("', 'handle").append(this.datasetname).append("Response', callprops);\n");
            html.append("}\n");
            html.append("else {\n");
            html.append("sapphire.alert( 'You need to enter a value!' );\n");
            html.append("}\n");
            html.append("}\n");
            String click = this.element.getProperty("customclick", "").length() > 0 ? this.element.getProperty("customclick", "") : "update" + this.datasetname + "Table";
            html.append("function handle").append(this.datasetname).append("Response(newkeyid){\n");
            html.append("if ( newkeyid != null && newkeyid.length > 0){\n");
            html.append("var cbxRow = document.getElementById('").append(this.datasetname).append("_newcbxrow');\n");
            html.append("var newTD = document.createElement('td');\n");
            html.append("newTD.innerHTML = '<input type=\"checkbox\" name=\"").append(this.datasetname).append("selector\" id=\"cbx_").append(this.datasetname).append(";' + sanitizeHTML( newkeyid ) +'\" ").append(this.linksdc.getProperty("keycolid1")).append("=\"' + sanitizeHTML( newkeyid ) + '\" rowid=\"\" onclick=\"").append(click).append("(this)\" /><label for=\"cbx_").append(this.datasetname).append(";' + sanitizeHTML( newkeyid ) + '\">' + sanitizeHTML( newkeyid ) + '</label>';\n");
            html.append("cbxRow.appendChild( newTD );\n");
            html.append("}\n");
            html.append("else {\n");
            html.append("sapphire.alert('Failed to add new ' + sdcid);\n");
            html.append("}\n");
            html.append("}\n");
        }
        if (this.element.getProperty("customclick", "").length() == 0) {
            html.append("function update").append(this.datasetname).append("Table(checkBox){\n");
            html.append("if (checkBox.checked){\n");
            html.append("sdiAddTableRow('sdiedit', '").append(this.datasetname).append("', '").append(this.datasetname).append("table');\n");
            html.append("document.getElementById('").append(this.datasetcode).append("' + __currentindex['").append(this.datasetname).append("'] + '_").append(this.linksdc.getProperty("keycolid1")).append("' ).value = sapphire.util.dom.getAttribute(checkBox,'").append(this.linksdc.getProperty("keycolid1")).append("');\n");
            html.append("checkBox.setAttribute('rowid', __currentindex['").append(this.datasetname).append("']);\n");
            html.append("}\n");
            html.append("else{\n");
            html.append("sdiDeleteTableRow('").append(this.datasetname).append("', '").append(this.datasetname).append("table', '").append(this.datasetname).append("' + checkBox.getAttribute( 'rowid' ), checkBox.getAttribute('rowid' ));\n");
            html.append("}\n");
            if (this.type.equals("manytomany")) {
                html.append("var keyid1 = '';\n");
                html.append("for (i = 0; i < __currentindex['").append(this.datasetname).append("'] + 1; i++){\n");
                html.append("if (document.getElementById( '__").append(this.datasetcode).append("' + i + '_rs' ).value == 'I'){\n");
                html.append("keyid1 += ';' + document.getElementById('").append(this.datasetcode).append("' + i + '_").append(this.sdc.getProperty("keycolid1")).append("').value;\n");
                html.append("}\n");
                html.append("}\n");
                html.append("document.getElementById('keyid1').value = keyid1.length > 0 ? keyid1.substring(1) : '';\n");
            }
            html.append("}\n");
        }
        html.append("function selectAll").append(this.datasetname).append("(checkBox){\n");
        html.append("var cbs = document.getElementsByName('").append(this.datasetname).append("selector');\n");
        html.append("var l = cbs.length;\n");
        html.append("for(var i=0;i<l;i++){\n");
        html.append("var el = cbs[i];\n");
        html.append("if((checkBox.checked && !el.checked) || (!checkBox.checked && el.checked)){\n");
        html.append("if(checkBox.checked){\n");
        html.append("cbs[i].checked = true;\n");
        html.append("}else{\n");
        html.append("cbs[i].checked = false;\n");
        html.append("}\n");
        if (this.element.getProperty("customclick", "").length() == 0) {
            html.append("update").append(this.datasetname).append("Table(cbs[i]);\n");
        } else {
            html.append(this.element.getProperty("customclick", "")).append("(cbs[i]);\n");
        }
        html.append("}\n");
        html.append("}\n");
        html.append("}\n");
        html.append("function init").append(this.datasetname).append("CheckBoxes(){\n");
        html.append("var checkBoxes = document.getElementsByName('").append(this.datasetname).append("selector');\n");
        html.append("var c = 0;\n");
        html.append("var l = checkBoxes.length;\n");
        html.append("for (var i = 0; i < l; i++){\n");
        html.append("if (document.getElementById('").append(this.datasetname).append(";' + sapphire.util.dom.getAttribute(checkBoxes[i],'").append(this.linksdc.getProperty("keycolid1")).append("')) != null ) {\n");
        html.append("checkBoxes[i].checked = true;\n");
        html.append("c++;\n");
        html.append("checkBoxes[i].setAttribute('rowid', document.getElementById('").append(this.datasetname).append(";' + sapphire.util.dom.getAttribute(checkBoxes[i],'").append(this.linksdc.getProperty("keycolid1")).append("')).getAttribute('rowid'));\n");
        html.append("}\n");
        html.append("}\n");
        if (this.element.getProperty("showselectall", "N").equalsIgnoreCase("Y")) {
            html.append("if (c==l){\n");
            html.append("document.getElementById('cbx_").append(this.datasetname).append("_selectall').checked = true;");
            html.append("}\n");
        }
        html.append("}\n");
        html.append("init").append(this.datasetname).append("CheckBoxes();\n");
        html.append("</script>");
        return html.toString();
    }

    @Override
    protected String getDefaultHeaderHtml() {
        StringBuffer html = new StringBuffer();
        html.append("<tr height=\"10\" class=\"maintdetail_headerrow\">\n");
        html.append(this.getHeaderSelector());
        html.append("<td class=\"maintdetail_header\">Detail Columns Undefined</td>\n");
        html.append("</tr>");
        return html.toString();
    }

    @Override
    protected String getDefaultRowHtml() {
        StringBuffer html = new StringBuffer();
        html.append("<tr height=\"10\" class=\"maintdetail_row\" id=\"").append(this.prefix).append("_row_").append(this.rownum).append("\">\n");
        html.append(this.getRowSelector());
        html.append("<td class=\"maintdetail_field\">&nbsp;</td>\n");
        html.append("</tr>");
        return html.toString();
    }

    @Override
    protected void setRowKeyids() {
        if (this.link.getProperty("linktype").equals("D")) {
            this.id = this.sdiInfo.getString(this.datasetname, "reftypeid") + "|" + this.sdiInfo.getString(this.datasetname, "refvalueid");
        }
    }

    @Override
    protected void setRowKeyCols() {
        if (this.type.equals("manytomany")) {
            if (this.link.getProperty("linktype").equals("D")) {
                this.keycols = new String[2];
                this.keycols[0] = "reftypeid";
                this.keycols[1] = "refvalueid";
            } else if (this.link.getProperty("linktype").equals("M")) {
                this.keycols = new String[2];
                this.keycols[0] = this.sdc.getProperty("keycolid1");
                this.keycols[1] = this.linksdc.getProperty("keycolid1");
            }
        } else {
            SDIData sdidata = new SDIData();
            this.keycols = sdidata.getKeys(this.datasetname);
        }
    }

    @Override
    public boolean isVisibleInAddMode() {
        return true;
    }
}

