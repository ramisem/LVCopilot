/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem;

import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemIncludes;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemOptions;
import java.util.HashSet;
import sapphire.SapphireException;
import sapphire.ext.BaseWorksheetItem;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ReagentLotRecipe
extends BaseWorksheetItem {
    @Override
    public void setupOptions(WorksheetItemOptions worksheetItemOptions) {
        worksheetItemOptions.setViewOnly(true);
        worksheetItemOptions.setSupportsSDIs(true, this.config.getProperty("source"), "LV_ReagentLot");
        worksheetItemOptions.addOperations(this.config.getCollection("operations"));
        worksheetItemOptions.setSupportsDataAvailablity(true);
    }

    @Override
    public void setupIncludes(WorksheetItemIncludes worksheetItemIncludes) {
        worksheetItemIncludes.addScriptInclude("WEB-CORE/modules/eln/worksheetitem/scripts/reagentlotrecipe.js");
        worksheetItemIncludes.setJSObjectName("reagentLotRecipeEditor");
    }

    @Override
    public String getExportHTML(PropertyList exportOptions) throws SapphireException {
        return this.getHTML(true);
    }

    @Override
    public String getViewHTML() throws SapphireException {
        return this.getHTML(false);
    }

    private String getHTML(boolean export) throws SapphireException {
        String source = this.config.getProperty("source", "Control");
        String reagentLotid = this.config.getProperty("reagentlotid", "");
        String recipeitemtype = this.config.getProperty("recipeitemtype", "Reagent");
        boolean filteredbystage = this.config.getProperty("filteredbystage", "N").equalsIgnoreCase("Y");
        String reagentlotstageid = this.config.getProperty("stageid", "");
        StringBuffer html = new StringBuffer();
        if (source.length() > 0) {
            boolean hidecolumnifnull = "Y".equals(this.config.getProperty("hidecolumnifnull"));
            PropertyListCollection columns = this.config.getCollection("columns");
            PropertyListCollection operations = this.config.getCollection("operations");
            if (columns == null) {
                columns = new PropertyListCollection();
            }
            HashSet<String> skipColumns = new HashSet<String>();
            if (columns.size() > 0) {
                SafeSQL safeSQL = new SafeSQL();
                StringBuffer sql = new StringBuffer();
                sql.append("SELECT ");
                for (int col = 0; col < columns.size(); ++col) {
                    PropertyList colProps = columns.getPropertyList(col);
                    String columnid = colProps.getProperty("columnid");
                    if (columnid.indexOf("(") > -1 && columnid.indexOf(")") > -1) {
                        sql.append(columnid);
                        columnid = columnid.substring(columnid.lastIndexOf(")") + 1).trim();
                        colProps.setProperty("columnid", columnid);
                    } else if (!columnid.contains(".")) {
                        sql.append(columnid);
                    }
                    sql.append(",");
                }
                sql.deleteCharAt(sql.length() - 1);
                sql.append(" FROM reagentlotrecipe");
                sql.append(" WHERE reagentlotid =").append(safeSQL.addVar(reagentLotid));
                sql.append(" and recipeitemtype =").append(safeSQL.addVar(recipeitemtype));
                sql.append(filteredbystage ? " and reagentlotstageid = " + safeSQL.addVar(reagentlotstageid) : "");
                sql.append(" order by usersequence");
                DataSet lotRecipeDS = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                for (int col = 0; col < columns.size(); ++col) {
                    PropertyList colProps = columns.getPropertyList(col);
                    String columnid = colProps.getProperty("columnid");
                    if (!colProps.getProperty("show", "Y").equals("N") && (!hidecolumnifnull || lotRecipeDS.getColumnValues(columnid, "").length() != 0)) continue;
                    skipColumns.add(columnid);
                }
                this.createPopupDiv(html, lotRecipeDS, reagentLotid);
                html.append(this.getTableHTML(export, columns, skipColumns, operations, lotRecipeDS, recipeitemtype.equalsIgnoreCase("Instrument") ? "Consumable Lot Equipment" : "Consumable Lot Recipe", "reagentlotid", "", ""));
            }
            return html.toString();
        }
        this.worksheetItemOptions.setRequiresConfig(true, "Reagent Lot Recipe Control requires configuration - click to configure");
        return "";
    }

    @Override
    public String getEditorHTML() throws SapphireException {
        return this.getViewHTML();
    }

    public void createPopupDiv(StringBuffer html, DataSet controldata, String reagentLotid) throws SapphireException {
        html.append("<div id=\"relationlist_" + this.getElementId() + "\" style=\"display:none\">{");
        html.append("\"reagentlotid\":\"" + reagentLotid + "\"");
        html.append("}</div>");
    }
}

