/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.admin.propertytree;

import com.labvantage.sapphire.admin.propertytree.EditorUtil;
import com.labvantage.sapphire.admin.propertytree.TypeSimple;
import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyValue;

public class SDCColumnEditor
implements TypeSimple {
    @Override
    public String getEditor(String fieldName, PropertyValue propertyValue, PropertyList topPropertylist, boolean ancestorValue, HashMap attributes, PageContext pageContext, boolean debug) {
        boolean definitionFound = false;
        String sdcid = null;
        Object tableid = null;
        if (topPropertylist != null && ((sdcid = topPropertylist.getProperty("sdcid")) == null || sdcid.length() == 0)) {
            try {
                sdcid = propertyValue.getParentPropertyList().getParentPropertyValue().getParentPropertyList().getProperty("sdcid");
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        StringBuffer output = new StringBuffer();
        DataSet columns = (DataSet)pageContext.getAttribute("cachedsdccolumns_" + sdcid);
        String displayColTypes = (String)attributes.get("displaycoltypes");
        String[] displayColTypeArr = null;
        if (displayColTypes != null && displayColTypes.trim().length() > 0) {
            displayColTypeArr = StringUtil.split(displayColTypes, ",");
        }
        boolean bl = definitionFound = sdcid != null && sdcid.length() > 0;
        if (columns == null && definitionFound) {
            SDCProcessor sdc = EditorUtil.getSDCProcessor(pageContext);
            columns = sdc.getColumnData(sdcid);
            columns.sort("pkflag d,columnid");
            definitionFound = columns != null;
            pageContext.setAttribute("cachedsdccolumns_" + sdcid, (Object)columns);
        }
        if (!definitionFound) {
            output.append("<input onchange=\"propertyChange()\" onkeyup=\"propertyChange()\" type=\"text\" name=\"" + fieldName + "\" id=\"" + fieldName + "\" style=\"width:200px " + (ancestorValue ? "; color:blue" : "") + "\" onchange=\"this.style.color='black';checkEvent( this )\" value=\"" + propertyValue + "\"/>");
        } else {
            output.append("<select name=\"" + fieldName + "\" id=\"" + fieldName + "\" style=\"" + (ancestorValue ? "; color:blue" : "") + "\" onchange=\"propertyChange();this.style.color='black';checkEvent( this )\">");
            output.append("<option value=\"" + (ancestorValue ? propertyValue.value : "") + "\">" + (ancestorValue ? propertyValue.value : "") + "</option>");
            for (int i = 0; i < columns.getRowCount(); ++i) {
                String colDataType = columns.getString(i, "datatype", "");
                if (displayColTypeArr != null) {
                    boolean colTypeFound = false;
                    for (String dispColType : displayColTypeArr) {
                        if (!colDataType.equalsIgnoreCase(dispColType)) continue;
                        colTypeFound = true;
                        break;
                    }
                    if (!colTypeFound) continue;
                }
                output.append("<option value=\"" + columns.getString(i, "columnid") + "\"" + (columns.getString(i, "columnid").equals(propertyValue.value) ? " selected" : "") + ">" + columns.getString(i, "columnid") + "</option>");
            }
            if (topPropertylist.getCollection("searchablesdcs") != null && sdcid.length() > 0) {
                int rows;
                SafeSQL safeSQL = new SafeSQL();
                String sql = "select columnid from sysextendedcolumn where tableid in (select tableid from sdc where sdcid=" + safeSQL.addVar(sdcid) + ") order by columnid";
                DataSet extcolumns = (DataSet)pageContext.getAttribute("cachedDs_" + sdcid);
                if (extcolumns == null) {
                    QueryProcessor queryProcessor = EditorUtil.getQueryProcessor(pageContext);
                    extcolumns = queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
                    pageContext.setAttribute("cachedDs_" + sdcid, (Object)extcolumns);
                }
                if ((rows = extcolumns.getRowCount()) > 0) {
                    for (int j = 0; j < rows; ++j) {
                        String value = extcolumns.getString(j, "columnid");
                        output.append("<option value=\"" + value + "\"" + (value.equals(propertyValue.value) ? " selected" : "") + ">" + value + "(ext)</option>");
                    }
                }
            }
            output.append("</select>");
        }
        return output.toString();
    }
}

