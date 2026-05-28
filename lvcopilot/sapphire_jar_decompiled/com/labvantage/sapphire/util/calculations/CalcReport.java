/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.calculations;

import com.labvantage.sapphire.I18nUtil;
import com.labvantage.sapphire.services.ConnectionInfo;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.util.FormatUtil;
import sapphire.util.M18NUtil;
import sapphire.util.SafeHTML;

public class CalcReport {
    public String sdcid;
    public String keyid1;
    public String keyid2;
    public String keyid3;
    public String paramlistid;
    public String paramlistversionid;
    public String variantid;
    public BigDecimal dataset;
    public String paramid;
    public String paramtype;
    public BigDecimal replicateid;
    public String attributeid;
    public String calcRule;
    public String result = "Undetermined";
    public String errorText;
    public String infoText;
    private Token[] params;

    public CalcReport(int length) {
        this.params = new Token[length];
    }

    public String toHTML(ConnectionInfo connectionInfo) {
        String username = connectionInfo.getSysuserName() == null ? connectionInfo.getSysuserId() : connectionInfo.getSysuserName();
        TranslationProcessor tp = new TranslationProcessor(connectionInfo.getConnectionId());
        StringBuffer out = new StringBuffer();
        out.append("<h1 align=center><u>Calculation Report</u></h1>");
        String expression = this.calcRule;
        if (expression.startsWith("$G{")) {
            expression = this.calcRule.substring(3, this.calcRule.length() - 1);
        }
        out.append("<br>");
        out.append("<table border=\"1\" cellspacing=\"0\" cellpadding=\"4\">");
        out.append("<tr><td style=\"background-color: wheat\">Expression </td><td>" + SafeHTML.encodeForHTML(expression) + "</td></tr>");
        out.append("<tr><td style=\"background-color: wheat\">Data Item </td><td>" + this.getLabel(this.sdcid, this.keyid1, this.keyid2, this.keyid3, this.paramlistid, this.paramlistversionid, this.variantid, this.dataset.intValue(), this.paramid, this.paramtype, this.replicateid.intValue(), this.attributeid) + "</td></tr>");
        if (this.errorText != null && this.errorText.length() > 0) {
            out.append("<tr><td style=\"background-color: wheat\">Error </td><td>");
            out.append("<div style=\"color: red\">" + this.errorText + "</div>");
            out.append("</td></tr>");
        } else {
            out.append("<tr><td style=\"background-color: wheat\">Result </td><td>");
            out.append("Undetermined".equals(this.result) ? "<span style=\"color:red\">Undetermined</span>" : (this.infoText != null && this.infoText.length() > 0 ? this.result + "&nbsp;&nbsp;( <span style=\"color:red\">" + this.infoText + "</span> )" : this.result));
            out.append("</td></tr>");
        }
        out.append("</table>");
        out.append("<br>");
        out.append("<table width=\"100%\" border=\"1\" cellspacing=\"0\" cellpadding=\"2\">");
        out.append("<tr style=\"background-color: wheat\" ><td>Token</td><td>Description</td><td>Value</td></tr>");
        for (int j = 0; j < this.params.length; ++j) {
            Token token = this.params[j];
            boolean hasComment = token.comment != null && token.comment.length() > 0;
            out.append("<tr>");
            out.append("<td rowspan=\"" + (token.paramDataItems.size() + (hasComment ? 1 : 0)) + "\">" + token.tokenid + "</td>");
            if (hasComment) {
                out.append("<td colspan=\"2\"><table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"4\">");
                out.append("<tr><td style=\"color: red\">");
                out.append(token.comment);
                out.append("</td></tr>");
                out.append("</table></td></tr><tr>");
            }
            for (int i = 0; i < token.paramDataItems.size(); ++i) {
                if (i > 0) {
                    out.append("<tr>");
                }
                if (token.paramDataItems.get(i) instanceof CalcParamDataItem) {
                    CalcParamDataItem calcParamDataItem = (CalcParamDataItem)token.paramDataItems.get(i);
                    M18NUtil m18nUtil = new M18NUtil(connectionInfo);
                    String enteredvalue = calcParamDataItem.value;
                    String datatype = calcParamDataItem.datatypes;
                    BigDecimal transformvalue = null;
                    Calendar transformdt = null;
                    Locale userLocale = I18nUtil.getConnectionLocale(connectionInfo);
                    TimeZone userTimeZone = I18nUtil.getConnectionTimeZone(connectionInfo);
                    if (datatype != null) {
                        if (calcParamDataItem.columnType == 1 && (datatype.equalsIgnoreCase("N") || datatype.equalsIgnoreCase("NC")) && enteredvalue.length() > 0) {
                            transformvalue = FormatUtil.getInstance().parseBigDecimal(enteredvalue);
                            enteredvalue = FormatUtil.getInstance().format(transformvalue, false, true);
                        } else if (datatype.equalsIgnoreCase("D") || datatype.equalsIgnoreCase("O")) {
                            transformdt = new M18NUtil().parseCalendar(enteredvalue, !datatype.equals("O"));
                        }
                    }
                    String value = I18nUtil.formatDataEntryDisplay(enteredvalue, datatype, transformvalue, transformdt, "", "", userLocale, userTimeZone, m18nUtil);
                    out.append("<td>" + this.getParamLabel(calcParamDataItem) + "</td>");
                    out.append("<td>");
                    out.append(calcParamDataItem.value == null || calcParamDataItem.value.length() == 0 ? "&nbsp;" : value);
                } else if (token.paramDataItems.get(i) instanceof CalcParamDataSet) {
                    CalcParamDataSet calcParamDataSet = (CalcParamDataSet)token.paramDataItems.get(i);
                    out.append("<td>" + this.getLabel(calcParamDataSet.sdcid, calcParamDataSet.keyid1, calcParamDataSet.keyid2, calcParamDataSet.keyid3, calcParamDataSet.paramlistid, calcParamDataSet.paramlistversionid, calcParamDataSet.variantid, calcParamDataSet.dataset, null, null, null, calcParamDataSet.attributeid) + "</td>");
                    out.append("<td>");
                    out.append(calcParamDataSet.value == null || calcParamDataSet.value.length() == 0 ? "&nbsp;" : calcParamDataSet.value);
                } else if (token.paramDataItems.get(i) instanceof CalcParamAlt) {
                    CalcParamAlt calcParamAlt = (CalcParamAlt)token.paramDataItems.get(i);
                    out.append("<td><span style=\"color:red\">" + tp.translate("Data Item not found. Using Alt Value") + ".</span></td>");
                    out.append("<td>");
                    out.append(calcParamAlt.value == null || calcParamAlt.value.length() == 0 ? "&nbsp;" : calcParamAlt.value);
                } else if (token.paramDataItems.get(i) instanceof CalcParamSDIWorkItem) {
                    CalcParamSDIWorkItem calcParamSDIWorkItem = (CalcParamSDIWorkItem)token.paramDataItems.get(i);
                    out.append("<td>" + this.getWorkItemLabel(calcParamSDIWorkItem.sdcid, calcParamSDIWorkItem.keyid1, calcParamSDIWorkItem.keyid2, calcParamSDIWorkItem.keyid3, calcParamSDIWorkItem.workitemid, calcParamSDIWorkItem.workiteminstance, calcParamSDIWorkItem.attributeid) + "</td>");
                    out.append("<td>");
                    out.append(calcParamSDIWorkItem.value == null || calcParamSDIWorkItem.value.length() == 0 ? "&nbsp;" : calcParamSDIWorkItem.value);
                } else if (token.paramDataItems.get(i) instanceof CalcParamSDI) {
                    CalcParamSDI calcParamSDI = (CalcParamSDI)token.paramDataItems.get(i);
                    out.append("<td>" + this.getLabel(calcParamSDI.sdcid, calcParamSDI.keyid1, calcParamSDI.keyid2, calcParamSDI.keyid3, null, null, null, null, null, null, null, calcParamSDI.attributeid) + "</td>");
                    out.append("<td>");
                    out.append(calcParamSDI.value == null || calcParamSDI.value.length() == 0 ? "&nbsp;" : calcParamSDI.value);
                }
                out.append("</td>");
                out.append("</tr>");
            }
        }
        out.append("</table><br>");
        out.append("<br><br>");
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("dd-MMM-yyyy 'at' HH:mm z");
        String date = sdf.format(Calendar.getInstance().getTime());
        out.append("Report generated on " + date + " by " + username);
        out.append("&nbsp;&nbsp;&nbsp;&nbsp;<span style=\"cursor: pointer; color:blue\" onclick=\"this.style.visibility='hidden';window.print();this.style.visibility='visible'\">(Print Report)</span>");
        return out.toString();
    }

    public StringBuffer getParamLabel(CalcParamDataItem calcParamDataItem) {
        StringBuffer out = new StringBuffer();
        out.append(this.getSDILabel(calcParamDataItem.sdcid, calcParamDataItem.keyid1, calcParamDataItem.keyid2, calcParamDataItem.keyid3));
        out.append("<br/>");
        if (calcParamDataItem.paramlistid != null && calcParamDataItem.paramlistid.length() > 0) {
            out.append(this.getParamListLabel(calcParamDataItem.paramlistid, calcParamDataItem.paramlistversionid, calcParamDataItem.variantid, calcParamDataItem.dataset, ""));
            out.append("<br/>");
        }
        if (calcParamDataItem.workitemid != null && calcParamDataItem.workitemid.length() > 0) {
            out.append(this.getWorkItemLabel(calcParamDataItem.sdcid, calcParamDataItem.keyid1, calcParamDataItem.keyid2, calcParamDataItem.keyid3, calcParamDataItem.workitemid, calcParamDataItem.workiteminstance, this.attributeid));
            out.append("<br/>");
        }
        if (calcParamDataItem.paramid != null && calcParamDataItem.paramid.length() > 0) {
            out.append("Parameter: " + calcParamDataItem.paramid + " (" + calcParamDataItem.paramtype + "), Replicate: " + calcParamDataItem.replicateid);
            if (calcParamDataItem.attributeid != null && calcParamDataItem.attributeid.length() > 0) {
                out.append(" </br>(Attribute:" + calcParamDataItem.attributeid + ")");
            } else if (calcParamDataItem.limittypeid != null && calcParamDataItem.limittypeid.length() > 0) {
                out.append(" <br/>(Parameter Limit: " + calcParamDataItem.limittypeid + "; " + calcParamDataItem.limitcolumn + ")");
                if (calcParamDataItem.limitcolumn.equals("Status Flag")) {
                    out.append(" [Returns 1 when Limit Met, 0 when Not Met, else Undefined]");
                }
            }
            return out;
        }
        return out;
    }

    public StringBuffer getLabel(String sdcid, String keyid1, String keyid2, String keyid3, String paramlistid, String paramlistversionid, String variantid, Integer dataset, String paramid, String paramtype, Integer replicateid, String attributeid) {
        StringBuffer out = new StringBuffer();
        out.append(this.getSDILabel(sdcid, keyid1, keyid2, keyid3));
        out.append("<br/>");
        if (paramlistid != null && paramlistid.length() > 0) {
            out.append(this.getParamListLabel(paramlistid, paramlistversionid, variantid, dataset, attributeid));
            out.append("<br/>");
        }
        if (paramid != null && paramid.length() > 0) {
            out.append(this.getParamLabel(paramid, paramtype, replicateid, attributeid));
        }
        return out;
    }

    public StringBuffer getSDILabel(String sdcid, String keyid1, String keyid2, String keyid3) {
        StringBuffer out = new StringBuffer();
        out.append(sdcid + ": " + keyid1);
        if (!keyid2.equals("(null)")) {
            out.append(" ( " + keyid2);
            if (!keyid3.equals("(null)")) {
                out.append(", " + keyid3);
            }
            out.append(" )");
        }
        return out;
    }

    public StringBuffer getParamListLabel(String paramlistid, String paramlistversionid, String variantid, int dataset, String attributeid) {
        StringBuffer out = new StringBuffer();
        out.append("Parameter List: " + paramlistid + " ( Version: " + paramlistversionid + ", Variant: " + variantid + "), DataSet: " + dataset);
        if (attributeid != null && attributeid.length() > 0) {
            out.append(" (Attribute:" + attributeid + ")");
        }
        return out;
    }

    public StringBuffer getWorkItemLabel(String sdcid, String keyid1, String keyid2, String keyid3, String workitemid, int workiteminstance, String attributeid) {
        StringBuffer out = new StringBuffer();
        out.append(this.getSDILabel(sdcid, keyid1, keyid2, keyid3));
        out.append("<br/>");
        if (workitemid != null && workitemid.length() > 0) {
            out.append("WorkItem: " + workitemid + ", Instance: " + workiteminstance);
            if (attributeid != null && attributeid.length() > 0) {
                out.append(" (Attribute:" + attributeid + ")");
            }
        }
        return out;
    }

    public StringBuffer getParamLabel(String paramid, String paramtype, int replicateid, String attributeid) {
        StringBuffer out = new StringBuffer();
        out.append("Parameter: " + paramid + " (" + paramtype + "), Replicate: " + replicateid);
        if (attributeid != null && attributeid.length() > 0) {
            out.append(" (Attribute:" + attributeid + ")");
        }
        return out;
    }

    public Token createToken(int index) {
        Token record;
        this.params[index] = record = new Token();
        return record;
    }

    class CalcParamSDI {
        String sdcid;
        String keyid1;
        String keyid2;
        String keyid3;
        String attributeid;
        String value;

        CalcParamSDI() {
        }
    }

    class CalcParamSDIWorkItem {
        String sdcid;
        String keyid1;
        String keyid2;
        String keyid3;
        String workitemid;
        int workiteminstance;
        String attributeid;
        String value;

        CalcParamSDIWorkItem() {
        }
    }

    class CalcParamAlt {
        String value;

        CalcParamAlt() {
        }
    }

    class CalcParamDataSet {
        String sdcid;
        String keyid1;
        String keyid2;
        String keyid3;
        String paramlistid;
        String paramlistversionid;
        String variantid;
        int dataset;
        String attributeid;
        String value;

        CalcParamDataSet() {
        }
    }

    class CalcParamDataItem {
        String sdcid;
        String keyid1;
        String keyid2;
        String keyid3;
        String paramlistid;
        String paramlistversionid;
        String variantid;
        int dataset;
        String workitemid;
        int workiteminstance;
        String paramid;
        String paramtype;
        int replicateid;
        String attributeid;
        String limittypeid;
        String limitcolumn;
        String datatypes;
        String value;
        int columnType;

        CalcParamDataItem() {
        }
    }

    public class Token {
        public String tokenid;
        public String comment;
        public List paramDataItems = new ArrayList();

        public String getComment() {
            return this.comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public void addDataItem(String sdcid, String keyid1, String keyid2, String keyid3, String paramlistid, String paramlistversionid, String variantid, int dataset, String paramid, String paramtype, int replicateid, String value) {
            CalcParamDataItem item = new CalcParamDataItem();
            item.sdcid = sdcid;
            item.keyid1 = keyid1;
            item.keyid2 = keyid2;
            item.keyid3 = keyid3;
            item.paramlistid = paramlistid;
            item.paramlistversionid = paramlistversionid;
            item.variantid = variantid;
            item.dataset = dataset;
            item.paramid = paramid;
            item.paramtype = paramtype;
            item.replicateid = replicateid;
            item.value = value;
            this.paramDataItems.add(item);
        }

        public void addDataItems(DataSet dataitems, String columnid) {
            for (int i = 0; i < dataitems.size(); ++i) {
                this.addDataItemRow(dataitems, columnid, i);
            }
        }

        public void addDataItemRow(DataSet dataitems, String columnid, int row) {
            CalcParamDataItem item = new CalcParamDataItem();
            item.sdcid = dataitems.getString(row, "sdcid");
            item.keyid1 = dataitems.getString(row, "keyid1");
            item.keyid2 = dataitems.getString(row, "keyid2");
            item.keyid3 = dataitems.getString(row, "keyid3");
            item.paramlistid = dataitems.getString(row, "paramlistid");
            item.paramlistversionid = dataitems.getString(row, "paramlistversionid");
            item.variantid = dataitems.getString(row, "variantid");
            item.dataset = dataitems.getInt(row, "dataset");
            item.paramid = dataitems.getString(row, "paramid");
            item.paramtype = dataitems.getString(row, "paramtype");
            item.replicateid = dataitems.getInt(row, "replicateid");
            if ("(all)".equalsIgnoreCase(columnid)) {
                item.value = "(all)";
            } else if (columnid != null && columnid.length() > 0) {
                item.value = dataitems.getValue(row, columnid);
            }
            item.datatypes = dataitems.getValue(row, "datatypes");
            item.columnType = dataitems.getColumnType(columnid);
            this.paramDataItems.add(item);
        }

        public void addDataItemAttributeRow(DataSet dataitems, int row, String attributeid, String value) {
            CalcParamDataItem item = new CalcParamDataItem();
            item.sdcid = dataitems.getString(row, "sdcid");
            item.keyid1 = dataitems.getString(row, "keyid1");
            item.keyid2 = dataitems.getString(row, "keyid2");
            item.keyid3 = dataitems.getString(row, "keyid3");
            item.paramlistid = dataitems.getString(row, "paramlistid");
            item.paramlistversionid = dataitems.getString(row, "paramlistversionid");
            item.variantid = dataitems.getString(row, "variantid");
            item.dataset = dataitems.getInt(row, "dataset");
            item.paramid = dataitems.getString(row, "paramid");
            item.paramtype = dataitems.getString(row, "paramtype");
            item.replicateid = dataitems.getInt(row, "replicateid");
            item.attributeid = attributeid;
            item.value = value;
            this.paramDataItems.add(item);
        }

        public void addDataItemLimitRow(DataSet dataitems, int row, String limittypeid, String valueColumnId, String value) {
            CalcParamDataItem item = new CalcParamDataItem();
            item.sdcid = dataitems.getString(row, "sdcid");
            item.keyid1 = dataitems.getString(row, "keyid1");
            item.keyid2 = dataitems.getString(row, "keyid2");
            item.keyid3 = dataitems.getString(row, "keyid3");
            item.paramlistid = dataitems.getString(row, "paramlistid");
            item.paramlistversionid = dataitems.getString(row, "paramlistversionid");
            item.variantid = dataitems.getString(row, "variantid");
            item.dataset = dataitems.getInt(row, "dataset");
            item.paramid = dataitems.getString(row, "paramid");
            item.paramtype = dataitems.getString(row, "paramtype");
            item.replicateid = dataitems.getInt(row, "replicateid");
            item.limittypeid = limittypeid;
            item.limitcolumn = valueColumnId.equals("statusflag") ? "Status Flag" : (valueColumnId.equals("value1") ? "Value 1" : (valueColumnId.equals("value2") ? "Value 2" : valueColumnId));
            item.value = value;
            this.paramDataItems.add(item);
        }

        public void addDataItemAttributeRows(DataSet dataitems, int row, String attributeid, String[] values) {
            for (int i = 0; i < values.length; ++i) {
                CalcParamDataItem item = new CalcParamDataItem();
                item.sdcid = dataitems.getString(row, "sdcid");
                item.keyid1 = dataitems.getString(row, "keyid1");
                item.keyid2 = dataitems.getString(row, "keyid2");
                item.keyid3 = dataitems.getString(row, "keyid3");
                item.paramlistid = dataitems.getString(row, "paramlistid");
                item.paramlistversionid = dataitems.getString(row, "paramlistversionid");
                item.variantid = dataitems.getString(row, "variantid");
                item.dataset = dataitems.getInt(row, "dataset");
                item.paramid = dataitems.getString(row, "paramid");
                item.paramtype = dataitems.getString(row, "paramtype");
                item.replicateid = dataitems.getInt(row, "replicateid");
                item.attributeid = attributeid;
                item.value = values[i];
                this.paramDataItems.add(item);
            }
        }

        public void addDataSets(DataSet datasets, String columnid) {
            for (int i = 0; i < datasets.size(); ++i) {
                this.addDataSetRow(datasets, columnid, i);
            }
        }

        public void addDataSetRow(DataSet datasets, String columnid, int row) {
            CalcParamDataSet item = new CalcParamDataSet();
            item.sdcid = datasets.getString(row, "sdcid");
            item.keyid1 = datasets.getString(row, "keyid1");
            item.keyid2 = datasets.getString(row, "keyid2");
            item.keyid3 = datasets.getString(row, "keyid3");
            item.paramlistid = datasets.getString(row, "paramlistid");
            item.paramlistversionid = datasets.getString(row, "paramlistversionid");
            item.variantid = datasets.getString(row, "variantid");
            item.dataset = datasets.getInt(row, "dataset");
            if (columnid != null && columnid.length() > 0) {
                item.value = datasets.getValue(row, columnid);
            }
            this.paramDataItems.add(item);
        }

        public void addAltRow(String altValue) {
            CalcParamAlt item = new CalcParamAlt();
            item.value = altValue;
            this.paramDataItems.add(item);
        }

        public void addDataSetAttributeRow(DataSet dataset, int row, String attributeid, String value) {
            CalcParamDataSet item = new CalcParamDataSet();
            item.sdcid = dataset.getString(row, "sdcid");
            item.keyid1 = dataset.getString(row, "keyid1");
            item.keyid2 = dataset.getString(row, "keyid2");
            item.keyid3 = dataset.getString(row, "keyid3");
            item.paramlistid = dataset.getString(row, "paramlistid");
            item.paramlistversionid = dataset.getString(row, "paramlistversionid");
            item.variantid = dataset.getString(row, "variantid");
            item.dataset = dataset.getInt(row, "dataset");
            item.attributeid = attributeid;
            item.value = value;
            this.paramDataItems.add(item);
        }

        public void addSDIWorkItemAttributeRow(DataSet dataset, int row, String attributeid, String value) {
            CalcParamSDIWorkItem item = new CalcParamSDIWorkItem();
            item.sdcid = dataset.getString(row, "sdcid");
            item.keyid1 = dataset.getString(row, "keyid1");
            item.keyid2 = dataset.getString(row, "keyid2");
            item.keyid3 = dataset.getString(row, "keyid3");
            item.workitemid = dataset.getString(row, "sourceworkitemid");
            item.workiteminstance = dataset.getInt(row, "sourceworkiteminstance");
            item.attributeid = attributeid;
            item.value = value;
            this.paramDataItems.add(item);
        }

        public void addDataSetAttributeRows(DataSet dataset, int row, String attributeid, String[] values) {
            for (int i = 0; i < values.length; ++i) {
                CalcParamDataSet item = new CalcParamDataSet();
                item.sdcid = dataset.getString(row, "sdcid");
                item.keyid1 = dataset.getString(row, "keyid1");
                item.keyid2 = dataset.getString(row, "keyid2");
                item.keyid3 = dataset.getString(row, "keyid3");
                item.paramlistid = dataset.getString(row, "paramlistid");
                item.paramlistversionid = dataset.getString(row, "paramlistversionid");
                item.variantid = dataset.getString(row, "variantid");
                item.dataset = dataset.getInt(row, "dataset");
                item.attributeid = attributeid;
                item.value = values[i];
                this.paramDataItems.add(item);
            }
        }

        public void addSDIWorkItemAttributeRows(DataSet dataset, int row, String attributeid, String[] values) {
            for (int i = 0; i < values.length; ++i) {
                CalcParamSDIWorkItem item = new CalcParamSDIWorkItem();
                item.sdcid = dataset.getString(row, "sdcid");
                item.keyid1 = dataset.getString(row, "keyid1");
                item.keyid2 = dataset.getString(row, "keyid2");
                item.keyid3 = dataset.getString(row, "keyid3");
                item.workitemid = dataset.getString(row, "sourceworkitemid");
                item.workiteminstance = dataset.getInt(row, "sourceworkiteminstance");
                item.attributeid = attributeid;
                item.value = values[i];
                this.paramDataItems.add(item);
            }
        }

        public void addSDIRowColumn(DataSet datasets, String columnid, String sdcId, String keyId1, String keyId2, String keyId3) {
            CalcParamSDI item = new CalcParamSDI();
            item.sdcid = sdcId;
            item.keyid1 = keyId1;
            item.keyid2 = keyId2;
            item.keyid3 = keyId3;
            if (columnid != null && columnid.length() > 0) {
                item.value = datasets.getValue(0, columnid);
            }
            this.paramDataItems.add(item);
        }

        public void addSDIAttributeRow(String attributeid, String value, String sdcId, String keyId1, String keyId2, String keyId3) {
            CalcParamSDI item = new CalcParamSDI();
            item.sdcid = sdcId;
            item.keyid1 = keyId1;
            item.keyid2 = keyId2;
            item.keyid3 = keyId3;
            item.attributeid = attributeid;
            item.value = value;
            this.paramDataItems.add(item);
        }

        public void addSDIAttributeRows(String attributeid, String[] values, String sdcId, String keyId1, String keyId2, String keyId3) {
            for (int i = 0; i < values.length; ++i) {
                CalcParamSDI item = new CalcParamSDI();
                item.sdcid = sdcId;
                item.keyid1 = keyId1;
                item.keyid2 = keyId2;
                item.keyid3 = keyId3;
                item.attributeid = attributeid;
                item.value = values[i];
                this.paramDataItems.add(item);
            }
        }
    }
}

