/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem;

import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItem;
import com.labvantage.sapphire.pageelements.list.ListColumn;
import com.labvantage.sapphire.tagext.SDITagUtil;
import java.util.ArrayList;
import java.util.HashSet;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.ext.BaseWorksheetItem;
import sapphire.util.DataSet;
import sapphire.util.SafeHTML;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public abstract class BaseSDIRelationControl
extends BaseWorksheetItem {
    private static final String cFNS = "{fn concat(";
    private static final String cFNE = ")}";

    public void renderDetailColumns(TranslationProcessor tp, PropertyListCollection plDetailColumns, StringBuilder restOfRow, DataSet cells, int i, String prefix) {
        if (plDetailColumns.size() > 0) {
            for (int j = 0; j < plDetailColumns.size(); ++j) {
                PropertyList column = plDetailColumns.getPropertyList(j);
                if (!column.getProperty("show", "Y").equals("Y")) continue;
                boolean keepWithPrior = column.getProperty("keepwithpreviousline", "N").equals("Y");
                String coltitle = column.getProperty("title");
                String displayValue = this.getColumnDisplayValue(cells, i, column, prefix, tp);
                restOfRow.append(!keepWithPrior ? "<br>" : "");
                restOfRow.append("<span style=\"white-space:nowrap" + (keepWithPrior ? ";float:right" : "") + "\">").append(coltitle.length() > 0 ? coltitle + ": " : "").append(displayValue).append("</span>");
            }
        }
    }

    public DataSet fetchControlData(String relationFunction, String extraColumnsForParamlist, String extraColumnsForWorkitem, String detailColumns, String detailJoin, String detailTIJoin, String source, String sourcesdcid, String sourcerelation, String workitemid, String paramlistid, String paramlistversionid, String variantid) throws SapphireException {
        String dicols = (detailColumns.length() > 0 ? detailColumns + "," : "") + " 'P' sourcetype " + (extraColumnsForParamlist.length() > 0 ? ", " + extraColumnsForParamlist : "") + ", sdidatarelation.keyid1, sdidatarelation.relationtype, sdidatarelation.usersequence, sdidatarelation.sourcekeyid1, sdidatarelation.tokeyid1, sdidatarelation.refkeyid1, sdidatarelation.amount, sdidatarelation.amountunits, sdidatarelation.amountunitstype, sdidatarelation.mandatoryflag, sdidatarelation.requiredamount, sdidatarelation.requiredamountunits, sdidatarelation.requiredamountunitstype, sdidata.availabilityflag, " + BaseSDIRelationControl.concatFields(this.getSapphireConnection().isOracle(), "sdidatarelation.keyid1", "sdidatarelation.paramlistid", "sdidatarelation.paramlistversionid", "sdidatarelation.variantid", "sdidatarelation.dataset") + " title";
        String wicols = (detailColumns.length() > 0 ? detailColumns + "," : "") + " 'W' sourcetype " + (extraColumnsForWorkitem.length() > 0 ? "," + extraColumnsForWorkitem : "") + ", sdiworkitemrelation.keyid1, sdiworkitemrelation.relationtype, sdiworkitemrelation.usersequence, sdiworkitemrelation.sourcekeyid1, sdiworkitemrelation.tokeyid1, sdiworkitemrelation.refkeyid1, sdiworkitemrelation.amount, sdiworkitemrelation.amountunits, sdiworkitemrelation.amountunitstype,sdiworkitemrelation.mandatoryflag, sdiworkitemrelation.requiredamount, sdiworkitemrelation.requiredamountunits, sdiworkitemrelation.requiredamountunitstype,  sdiworkitem.appliedflag, " + BaseSDIRelationControl.concatFields(this.getSapphireConnection().isOracle(), "sdiworkitemrelation.keyid1", "sdiworkitemrelation.workitemid", "sdiworkitemrelation.workiteminstance") + " title";
        String wstable = source.equalsIgnoreCase("Worksheet") ? "worksheetsdi" : "worksheetitemsdi";
        StringBuilder sql = new StringBuilder();
        ArrayList<String> args = new ArrayList<String>();
        if (sourcerelation.equalsIgnoreCase("paramlist") || sourcerelation.equalsIgnoreCase("both")) {
            sql.append("SELECT " + dicols);
            sql.append(" FROM sdidatarelation ");
            if (detailJoin.length() > 0) {
                sql.append("left outer join " + detailJoin + " = sdidatarelation.tokeyid1 ");
            }
            if (detailTIJoin.length() > 0) {
                sql.append(" left outer join " + detailTIJoin + " = sdidatarelation.refkeyid1 ");
            }
            sql.append(", sdiworkitem, sdiworkitemitem, sdidata");
            sql.append((CharSequence)this.getItemJoins(sourcesdcid, wstable, args));
            sql.append(" AND sdiworkitem.sdcid = sdiworkitemitem.sdcid AND sdiworkitem.keyid1 = sdiworkitemitem.keyid1 AND sdiworkitem.keyid2 = sdiworkitemitem.keyid2 AND sdiworkitem.keyid3 = sdiworkitemitem.keyid3   AND sdiworkitem.workitemid = sdiworkitemitem.workitemid AND sdiworkitem.workiteminstance = sdiworkitemitem.workiteminstance   AND sdiworkitemitem.sdcid = sdidata.sdcid AND sdiworkitemitem.keyid1 = sdidata.keyid1 AND sdiworkitemitem.keyid2 = sdidata.keyid2 AND sdiworkitemitem.keyid3 = sdidata.keyid3 AND sdiworkitemitem.itemsdcid = 'ParamList'   AND sdiworkitemitem.itemkeyid1 = sdidata.paramlistid AND sdiworkitemitem.itemkeyid2 = sdidata.paramlistversionid AND sdiworkitemitem.itemkeyid3 = sdidata.variantid AND sdiworkitemitem.iteminstance = sdidata.dataset   AND sdidatarelation.sdcid = sdiworkitem.sdcid AND sdidatarelation.keyid1 = sdiworkitem.keyid1 AND sdidatarelation.keyid2 = sdiworkitem.keyid2 AND sdidatarelation.keyid3 = sdiworkitem.keyid3   AND sdidatarelation.paramlistid = sdidata.paramlistid AND sdidatarelation.paramlistversionid = sdidata.paramlistversionid AND sdidatarelation.variantid = sdidata.variantid AND sdidatarelation.dataset = sdidata.dataset   AND sdidatarelation.relationfunction = '" + relationFunction + "'");
            if (workitemid.length() > 0) {
                sql.append(" AND sdiworkitem.workitemid = ?");
                args.add(workitemid);
            }
            if (paramlistid.length() > 0) {
                sql.append(" AND sdidata.paramlistid=?");
                args.add(paramlistid);
            }
            if (paramlistversionid.length() > 0) {
                sql.append(" AND sdidata.paramlistversionid=?");
                args.add(paramlistversionid);
            }
            if (variantid.length() > 0) {
                sql.append(" AND sdidata.variantid=?");
                args.add(variantid);
            }
        }
        if (sourcerelation.equalsIgnoreCase("both")) {
            sql.append(" UNION ALL ");
        }
        if (sourcerelation.equalsIgnoreCase("workitem") || sourcerelation.equalsIgnoreCase("both")) {
            sql.append("SELECT " + wicols);
            sql.append(" FROM sdiworkitemrelation ");
            if (detailJoin.length() > 0) {
                sql.append("left outer join " + detailJoin + " = sdiworkitemrelation.tokeyid1 ");
            }
            if (detailTIJoin.length() > 0) {
                sql.append(" left outer join " + detailTIJoin + " = sdiworkitemrelation.refkeyid1 ");
            }
            sql.append(", sdiworkitem");
            sql.append((CharSequence)this.getItemJoins(sourcesdcid, wstable, args));
            sql.append(" AND sdiworkitemrelation.sdcid = sdiworkitem.sdcid AND sdiworkitemrelation.keyid1 = sdiworkitem.keyid1 AND sdiworkitemrelation.keyid2 = sdiworkitem.keyid2 AND sdiworkitemrelation.keyid3 = sdiworkitem.keyid3   AND sdiworkitemrelation.workitemid = sdiworkitem.workitemid AND sdiworkitemrelation.workiteminstance = sdiworkitem.workiteminstance   AND sdiworkitemrelation.relationfunction = '" + relationFunction + "'");
            if (workitemid.length() > 0) {
                sql.append(" AND sdiworkitem.workitemid = ?");
                args.add(workitemid);
            }
        }
        DataSet controldata = this.getQueryProcessor().getPreparedSqlDataSet("sdirelation_noexception", sql.toString(), args.toArray());
        return controldata;
    }

    public StringBuilder getItemJoins(String sourcesdcid, String wstable, ArrayList args) {
        StringBuilder sql = new StringBuilder();
        if (wstable.equalsIgnoreCase("worksheetsdi")) {
            sql.append(",worksheetsdi");
            sql.append(" WHERE worksheetsdi.worksheetid = ? AND worksheetsdi.worksheetversionid = ? AND worksheetsdi.sdcid = ? ");
            args.add(this.getWorksheetId());
            args.add(this.getWorksheetVersionId());
            args.add(sourcesdcid);
        } else {
            sql.append(", worksheetitemsdi ");
            sql.append(" WHERE worksheetitemsdi.worksheetitemid = ? AND worksheetitemsdi.worksheetitemversionid = ? AND worksheetitemsdi.sdcid = ? ");
            args.add(this.getWorksheetItemId());
            args.add(this.getWorksheetItemVersionId());
            args.add(sourcesdcid);
        }
        if (sourcesdcid.equalsIgnoreCase("sdiworkitem")) {
            sql.append(" AND " + wstable + ".keyid1 = sdiworkitem.sdiworkitemid ");
        } else {
            sql.append(" AND " + wstable + ".sdcid=sdiworkitem.sdcid AND " + wstable + ".keyid1 = sdiworkitem.keyid1 AND " + wstable + ".keyid2 = sdiworkitem.keyid2 AND " + wstable + ".keyid3 = sdiworkitem.keyid3 ");
        }
        return sql;
    }

    @Override
    protected String getColumnDisplayValue(DataSet controlData, int i, PropertyList column, TranslationProcessor translationProcessor) {
        return this.getColumnDisplayValue(controlData, i, column, "", translationProcessor);
    }

    protected String getColumnDisplayValue(DataSet controlData, int i, PropertyList column, String columnPrefix, TranslationProcessor translationProcessor) {
        String columnid = column.getProperty("columnid");
        columnid = columnid.contains("trackitem.") && columnid.contains("reagentlot_") ? columnid.substring(columnid.lastIndexOf("reagentlot_")) : columnPrefix + columnid;
        String value = i >= 0 ? controlData.getValue(i, columnid) : columnid;
        value = ListColumn.sanitizeHTMLValue(value);
        String displayValue = column.getProperty("displayvalue");
        if ((displayValue = WorksheetItem.replaceSubstitutionTokens(displayValue, controlData, i)).length() > 0) {
            value = SDITagUtil.getDisplayValue(value, displayValue);
        }
        if ("Y".equals(column.getProperty("translatevalue"))) {
            value = translationProcessor.translate(value);
        }
        return value;
    }

    public StringBuilder renderNoAvailability(String primarysdcid, PropertyListCollection primaryColumns, String titleSDC, String titlesdc, String tableStyling, boolean fullwidth, TranslationProcessor tp) {
        StringBuilder html = new StringBuilder();
        int colspan = 1;
        html.append("<table class=\"" + tableStyling + "\" " + (fullwidth ? "width=\"100%\"" : "") + ">");
        html.append("<tr>");
        if (primaryColumns.size() > 0) {
            for (int i = 0; i < primaryColumns.size(); ++i) {
                PropertyList column = primaryColumns.getPropertyList(i);
                String title = column.getProperty("title");
                if (!column.getProperty("show", "Y").equals("Y")) continue;
                html.append("<td class=\"title\">" + title + "</td>");
            }
            colspan = primaryColumns.size();
        } else {
            html.append("<td class=\"title\">" + primarysdcid + " " + titleSDC + "s</td>");
        }
        if (this.isTemplate()) {
            html.append("<td class=\"title\">" + titleSDC + " 1</td>");
            html.append("<td class=\"title\">" + titleSDC + " 2</td>");
            html.append("</tr>");
            html.append("<tr><td class=\"value\" colspan=\"" + (colspan + 2) + "\"><i>List of samples and their " + titlesdc + "</i></td></tr>");
        } else {
            html.append("</tr>");
            String noSDIMsg = this.config.getProperty("nosdiavailablemessage");
            String msg = noSDIMsg.length() > 0 ? SafeHTML.encodeForHTML(noSDIMsg, true) : tp.translate("No " + titleSDC + " found");
            html.append("<tr><td class=\"value\" colspan=\"" + (colspan + 2) + "\"><i>" + msg + "</i></td></tr>");
        }
        html.append("</table>");
        return html;
    }

    public void createPopupDiv(StringBuffer html, String sourcerelation, String sourcename, DataSet controldata, DataSet extra) throws SapphireException {
        if (sourcerelation.equalsIgnoreCase("workitem")) {
            HashSet<String> setOfWorkitems = new HashSet<String>();
            for (int i = 0; i < controldata.size(); ++i) {
                if (!controldata.getString(i, "sourcetype").equals("W")) continue;
                setOfWorkitems.add(controldata.getValue(i, "title"));
            }
            StringBuilder keyidlist = new StringBuilder();
            StringBuilder workitemlist = new StringBuilder();
            StringBuilder workiteminstance = new StringBuilder();
            for (String data : setOfWorkitems) {
                String[] parts = StringUtil.split(data, ";");
                keyidlist.append(";").append(parts[0]);
                workitemlist.append(";").append(parts[1]);
                workiteminstance.append(";").append(parts[2]);
            }
            if (keyidlist.length() > 0) {
                html.append("<div id=\"relationlist_" + this.getElementId() + "\" style=\"display:none\">{");
                html.append("\"keyidlist\":\"" + keyidlist.substring(1) + "\",");
                html.append("\"workitemid\":\"" + workitemlist.substring(1) + "\",");
                html.append("\"workiteminstance\":\"" + workiteminstance.substring(1) + "\",");
                html.append("\"worksheetid\":\"" + this.getWorksheetId() + "\",");
                html.append("\"worksheetversionid\":\"" + this.getWorksheetVersionId() + "\",");
                html.append("\"worksheetitemid\":\"" + this.getWorksheetItemId() + "\",");
                html.append("\"worksheetitemversionid\":\"" + this.getWorksheetItemVersionId() + "\",");
                html.append("\"" + sourcename + "\":\"sdiwirelation\"");
                html.append("}</div>");
            }
        } else {
            int i;
            HashSet<String> setOfDatasets = new HashSet<String>();
            if (sourcerelation.equalsIgnoreCase("both")) {
                for (i = 0; i < controldata.size(); ++i) {
                    setOfDatasets.add(controldata.getValue(i, "title"));
                }
            } else {
                for (i = 0; i < controldata.size(); ++i) {
                    if (!controldata.getString(i, "sourcetype").equals("P")) continue;
                    setOfDatasets.add(controldata.getValue(i, "title"));
                }
            }
            if (extra != null) {
                for (i = 0; i < extra.size(); ++i) {
                    setOfDatasets.add(extra.getValue(i, "title"));
                }
            }
            StringBuilder keyidlist = new StringBuilder();
            StringBuilder paramlistidlist = new StringBuilder();
            StringBuilder paramlistversionidlist = new StringBuilder();
            StringBuilder variantidlist = new StringBuilder();
            StringBuilder datasetlist = new StringBuilder();
            for (String data : setOfDatasets) {
                String[] parts = StringUtil.split(data, ";");
                if (parts.length == 3) {
                    String partsPL = this.getParamListOfWI(data);
                    parts = StringUtil.split(partsPL, "%3B");
                }
                keyidlist.append(";").append(parts[0]);
                paramlistidlist.append(";").append(parts[1]);
                paramlistversionidlist.append(";").append(parts[2]);
                variantidlist.append(";").append(parts[3]);
                datasetlist.append(";").append(parts[4]);
            }
            if (keyidlist.length() > 0) {
                html.append("<div id=\"relationlist_" + this.getElementId() + "\" style=\"display:none\">{");
                html.append("\"keyidlist\":\"" + keyidlist.substring(1) + "\",");
                html.append("\"paramlistidlist\":\"" + paramlistidlist.substring(1) + "\",");
                html.append("\"paramlistversionidlist\":\"" + paramlistversionidlist.substring(1) + "\",");
                html.append("\"variantidlist\":\"" + variantidlist.substring(1) + "\",");
                html.append("\"datasetlist\":\"" + datasetlist.substring(1) + "\",");
                html.append("\"worksheetid\":\"" + this.getWorksheetId() + "\",");
                html.append("\"worksheetversionid\":\"" + this.getWorksheetVersionId() + "\",");
                html.append("\"worksheetitemid\":\"" + this.getWorksheetItemId() + "\",");
                html.append("\"worksheetitemversionid\":\"" + this.getWorksheetItemVersionId() + "\",");
                html.append("\"" + sourcename + "\":\"" + (sourcerelation.equalsIgnoreCase("paramlist") ? "sdidatarelation" : "") + "\"");
                html.append("}</div>");
            }
        }
    }

    private String getParamListOfWI(String data) throws SapphireException {
        String partsPL = "";
        String[] parts = StringUtil.split(data, ";");
        String keyid1 = parts[0];
        String workitemid = parts[1];
        String workiteminstance = parts[2];
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT distinct itemkeyid1,itemkeyid2,itemkeyid3,iteminstance FROM sdiworkitemitem");
        sql.append(" WHERE keyid1=" + safeSQL.addVar(keyid1));
        sql.append(" AND workitemid=" + safeSQL.addVar(workitemid));
        sql.append(" AND workiteminstance=" + safeSQL.addVar(workiteminstance));
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        StringBuilder keyidlist = new StringBuilder();
        StringBuilder paramlistidlist = new StringBuilder();
        StringBuilder paramlistversionidlist = new StringBuilder();
        StringBuilder variantidlist = new StringBuilder();
        StringBuilder datasetlist = new StringBuilder();
        for (int i = 0; i < ds.size(); ++i) {
            keyidlist.append(";").append(keyid1);
            paramlistidlist.append(";").append(ds.getString(i, "itemkeyid1", ""));
            paramlistversionidlist.append(";").append(ds.getValue(i, "itemkeyid2", ""));
            variantidlist.append(";").append(ds.getString(i, "itemkeyid3", ""));
            datasetlist.append(";").append(ds.getValue(i, "iteminstance", ""));
        }
        if (paramlistidlist.length() > 0) {
            partsPL = keyidlist.substring(1) + "%3B" + paramlistidlist.substring(1) + "%3B" + paramlistversionidlist.substring(1) + "%3B" + variantidlist.substring(1) + "%3B" + datasetlist.substring(1);
        }
        return partsPL;
    }

    protected static String concatFields(boolean isOracle, String ... fields) {
        String str = "";
        boolean firstItem = true;
        for (String f : fields) {
            if (firstItem) {
                str = isOracle ? f : "cast(" + f + " as nvarchar(100))";
                firstItem = false;
                continue;
            }
            str = "{fn concat({fn concat(" + str + ",';'" + cFNE + "," + (isOracle ? f : "cast(" + f + " as nvarchar(100))") + cFNE;
        }
        return str;
    }
}

