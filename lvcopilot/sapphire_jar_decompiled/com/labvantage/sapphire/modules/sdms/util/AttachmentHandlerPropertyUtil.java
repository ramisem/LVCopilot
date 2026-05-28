/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.modules.sdms.util;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.pageelements.maint.EditorStyleField;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.tagext.PageTagInfo;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class AttachmentHandlerPropertyUtil {
    private String sdcid;
    private String keyid1;
    private String keyid2;
    private String keyid3;
    private String attachmentoperationid;
    private QueryProcessor qp;
    private TranslationProcessor tp;
    private PropertyList pagedata;
    private PageContext pageContext;
    private boolean viewonly = false;

    public AttachmentHandlerPropertyUtil(PageContext pageContext, PageTagInfo pageinfo, HttpServletRequest request) {
        this.pageContext = pageContext;
        this.qp = pageinfo.getQueryProcessor();
        this.tp = new TranslationProcessor(pageContext);
        this.pagedata = pageinfo.getPropertyList("pagedata");
        this.sdcid = this.pagedata.getProperty("sdcid", "");
        this.keyid1 = this.pagedata.getProperty("keyid1", "");
        this.keyid2 = this.pagedata.getProperty("keyid2", "");
        this.keyid3 = this.pagedata.getProperty("keyid3", "");
        this.attachmentoperationid = this.pagedata.getProperty("attachmentoperationid", "");
        this.viewonly = this.pagedata.getProperty("viewonly", "N").equalsIgnoreCase("Y");
    }

    public String getHtml(String mode) {
        StringBuffer htmlData = new StringBuffer();
        String noVarFound = this.tp.translate("No variable found");
        htmlData.append("<div style=\"border-collapse:collapse;\" id=dataentry_grid_container>");
        htmlData.append("<table id=\"dataEntryTable\" class=\"maintform_table\" border=\"0\" cellpadding=\"2\" cellspacing=\"0\">");
        PropertyList properties = new PropertyList();
        PropertyList values = new PropertyList();
        String firstvariable = "";
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer();
        PropertyListCollection plc = new PropertyListCollection();
        boolean allpropsoverriden = false;
        try {
            if (this.sdcid.equalsIgnoreCase("Instrument")) {
                sql.setLength(0);
                safeSQL.reset();
                sql.append("select sdaoInstr.propertyclob instrSVs,sdaoModel.propertyclob modelSVs,attachmenthandler.propertyclob ahSVs from sdiattachmentoperation sdaoInstr ");
                sql.append(" left join instrument on");
                sql.append(" instrument.instrumentid=sdaoInstr.keyid1");
                sql.append(" left join sdiattachmentoperation sdaoModel on");
                sql.append(" sdaoModel.attachmentoperationid=").append(safeSQL.addVar(this.attachmentoperationid));
                sql.append(" and sdaoModel.sdcid='LV_InstrumentModel'");
                sql.append(" and sdaoModel.keyid1=instrument.instrumentmodelid");
                sql.append(" and sdaoModel.keyid2=instrument.instrumenttype");
                sql.append(" left join attachmenthandler on attachmenthandler.attachmenthandlerid=sdaoInstr.operationkeyid1");
                sql.append(" where sdaoInstr.attachmentoperationid=").append(safeSQL.addVar(this.attachmentoperationid));
                sql.append(" and sdaoInstr.sdcid='Instrument'");
                sql.append(" and sdaoInstr.keyid1=").append(safeSQL.addVar(this.keyid1));
                DataSet instrAttachOp = this.qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues(), true);
                if (instrAttachOp != null && instrAttachOp.size() > 0) {
                    String instrSVs = instrAttachOp.getClob(0, "instrSVs", "");
                    String modelSVs = instrAttachOp.getClob(0, "modelSVs", "");
                    String ahSVs = instrAttachOp.getClob(0, "ahSVs", "");
                    properties = this.getPropsFromJSONString(ahSVs);
                    plc = properties.getCollectionNotNull("variables");
                    if (instrSVs.length() > 0 || modelSVs.length() > 0) {
                        values = this.getPropsFromJSONString(instrSVs);
                        PropertyList modelSVsPL = this.getPropsFromJSONString(modelSVs);
                        for (int i = 0; i < plc.size(); ++i) {
                            String value;
                            PropertyList varProps = (PropertyList)plc.get(i);
                            String variableid = varProps.getProperty("variableid", "");
                            if (values.containsKey(variableid)) {
                                value = values.getProperty(variableid, "");
                                varProps.setProperty("value", value.length() == 0 ? " " : value);
                                varProps.setProperty("inherited", "N");
                                continue;
                            }
                            if (!modelSVsPL.containsKey(variableid)) continue;
                            value = modelSVsPL.getProperty(variableid, "");
                            varProps.setProperty("value", value.length() == 0 ? " " : value);
                            varProps.setProperty("inheritedfrom", "Instrument Model");
                        }
                    }
                }
            } else if (this.sdcid.equalsIgnoreCase("LV_InstrumentModel")) {
                sql.setLength(0);
                safeSQL.reset();
                sql.append("select sdaoModel.propertyclob modelSVs,attachmenthandler.propertyclob ahSVs from sdiattachmentoperation sdaoModel");
                sql.append(" left join attachmenthandler on attachmenthandler.attachmenthandlerid=sdaoModel.operationkeyid1");
                sql.append(" where sdaoModel.sdcid='LV_InstrumentModel'");
                sql.append(" and sdaoModel.keyid1=").append(safeSQL.addVar(this.keyid1));
                sql.append(" and sdaoModel.keyid2=").append(safeSQL.addVar(this.keyid2));
                sql.append(" and sdaoModel.attachmentoperationid=").append(safeSQL.addVar(this.attachmentoperationid));
                DataSet modelAttachOp = this.qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues(), true);
                if (modelAttachOp != null && modelAttachOp.size() > 0) {
                    String modelSVs = modelAttachOp.getClob(0, "modelSVs", "");
                    String ahSVs = modelAttachOp.getClob(0, "ahSVs", "");
                    properties = this.getPropsFromJSONString(ahSVs);
                    plc = properties.getCollectionNotNull("variables");
                    if (modelSVs.length() > 0) {
                        values = this.getPropsFromJSONString(modelSVs);
                        for (int i = 0; i < plc.size(); ++i) {
                            PropertyList varProps = (PropertyList)plc.get(i);
                            String variableid = varProps.getProperty("variableid", "");
                            if (!values.containsKey(variableid)) continue;
                            String value = values.getProperty(variableid, "");
                            varProps.setProperty("value", value.length() == 0 ? " " : value);
                            varProps.setProperty("inherited", "N");
                        }
                    }
                }
            } else {
                sql.setLength(0);
                safeSQL.reset();
                sql.append("select propertyclob from sdiattachmentoperation ");
                sql.append(" where sdcid=").append(safeSQL.addVar(this.sdcid));
                sql.append(" and keyid1=").append(safeSQL.addVar(this.keyid1));
                if (this.keyid2.length() > 0) {
                    sql.append(" and keyid2=").append(safeSQL.addVar(this.keyid2));
                }
                if (this.keyid3.length() > 0) {
                    sql.append(" and keyid3=").append(safeSQL.addVar(this.keyid3));
                }
                sql.append(" and attachmentoperationid=").append(safeSQL.addVar(this.attachmentoperationid));
                DataSet attachOp = this.qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues(), true);
                if (attachOp != null && attachOp.size() > 0) {
                    String propertyclob = attachOp.getClob(0, "propertyclob", "");
                    properties = this.getPropsFromJSONString(propertyclob);
                    plc = properties.getCollectionNotNull("variables");
                }
                allpropsoverriden = true;
            }
            if (plc.size() > 0) {
                htmlData.append(this.getHeaderHtml());
                for (int i = 0; i < plc.size(); ++i) {
                    PropertyList varProps = (PropertyList)plc.get(i);
                    String variableid = varProps.getProperty("variableid", "");
                    boolean inherited = !allpropsoverriden && varProps.getProperty("inherited", "Y").equalsIgnoreCase("Y");
                    String inheritedFrom = varProps.getProperty("inheritedfrom", "Attachment Handler");
                    if (variableid.equalsIgnoreCase("sdcid") || variableid.equalsIgnoreCase("keyid1") || variableid.equalsIgnoreCase("keyid2") || variableid.equalsIgnoreCase("keyid3")) {
                        variableid = "setupvar_" + variableid;
                    }
                    String editorstyleid = varProps.getProperty("editorstyleid", "");
                    String defaultvalue = varProps.getProperty("value", "");
                    String prompt = varProps.getProperty("prompt", StringUtil.initCaps(variableid));
                    htmlData.append(this.getRowHtml(prompt, defaultvalue, editorstyleid, variableid, inherited, inheritedFrom));
                    if (firstvariable.length() != 0) continue;
                    firstvariable = variableid;
                    htmlData.append(this.addHiddenFieldHtml("firstvariable", firstvariable));
                }
            } else {
                htmlData.append("<tr><td nowrap>" + noVarFound + "</td></tr>");
            }
        }
        catch (SapphireException e) {
            Trace.log("Unable to render setup variable::" + e.getMessage());
        }
        htmlData.append("</table>");
        htmlData.append("</div>");
        if (properties.size() > 0) {
            htmlData.append("<script>");
            htmlData.append("attachmentHandlerPropsContent.properties=sapphire.util.dataSet.create(").append(properties.toJSONObject()).append(");");
            htmlData.append("attachmentHandlerPropsContent.values=sapphire.util.dataSet.create(").append(values.toJSONObject()).append(");");
            htmlData.append("</script>");
        }
        return htmlData.toString();
    }

    public void saveData(String data) {
        StringBuffer updateSql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        updateSql.append("update sdiattachmentoperation");
        updateSql.append(" set propertyclob=").append(safeSQL.addVar(data));
        updateSql.append(" where sdcid=").append(safeSQL.addVar(this.sdcid));
        updateSql.append(" and keyid1=").append(safeSQL.addVar(this.keyid1));
        if (this.keyid2.length() > 0) {
            updateSql.append(" and keyid2=").append(safeSQL.addVar(this.keyid2));
        }
        if (this.keyid3.length() > 0) {
            updateSql.append(" and keyid3=").append(safeSQL.addVar(this.keyid3));
        }
        updateSql.append(" and attachmentoperationid=").append(safeSQL.addVar(this.attachmentoperationid));
        this.qp.execPreparedUpdate(updateSql.toString(), safeSQL.getValues());
    }

    private String getHeaderHtml() {
        StringBuffer headerHtml = new StringBuffer();
        headerHtml.append("<tr>");
        headerHtml.append("<th nowrap class=\"gridmaint_fieldtitle gridmaint_ftmed\">" + this.tp.translate("Variable") + "</th>");
        headerHtml.append("<th nowrap class=\"gridmaint_fieldtitle gridmaint_ftmed\">" + this.tp.translate("Value") + "</th>");
        headerHtml.append("</tr>");
        return headerHtml.toString();
    }

    private String addHiddenFieldHtml(String id, String value) {
        return "<input  id=\"" + id + "\" name=\"" + id + "\" type=\"hidden\" value=\"" + value + "\">";
    }

    private String getRowHtml(String prompt, String value, String editorstyleid, String variableid, boolean inherited, String inheritedFrom) {
        StringBuffer rowHtml = new StringBuffer();
        rowHtml.append("<tr>");
        rowHtml.append(this.getRowTitleHtml(prompt));
        rowHtml.append(this.getRowInputHtml(value, editorstyleid, variableid, inherited, inheritedFrom));
        rowHtml.append("</tr>");
        return rowHtml.toString();
    }

    private String getRowTitleHtml(String prompt) {
        return "<td  class=\"maintform_fieldtitle\"  align=left nowrap>" + prompt + "</td>";
    }

    private String getRowInputHtml(String value, String editorstyleid, String variableid, boolean inherited, String inheritedFrom) {
        String inheritedImage = "rc?command=image&amp;image=FlatBlackArrow4Down&amp;size=16&amp;height=16&amp;width=16";
        StringBuffer rowHtml = new StringBuffer();
        rowHtml.append("<td style=\"border:1px solid #BDCCD4;background-color:white;\" valign='top'>");
        rowHtml.append("<table><tr><td>");
        if (editorstyleid != null && editorstyleid.length() > 0) {
            try {
                EditorStyleField editorStyleField = new EditorStyleField(this.pageContext);
                editorStyleField.setEditorStyleId(editorstyleid);
                editorStyleField.setFieldName(variableid);
                PropertyList column = new PropertyList();
                column.setProperty("columnid", variableid);
                if (this.viewonly) {
                    column.setProperty("mode", "readonly");
                }
                editorStyleField.setColumn(column);
                editorStyleField.setFieldValue(value);
                rowHtml.append(editorStyleField.getHtml());
            }
            catch (SapphireException e) {
                e.printStackTrace();
            }
        } else {
            rowHtml.append("<input " + (this.viewonly ? "style=\";border:0;\" readonly" : "") + " onchange=\"sdiSetRowUpdate(event)\" oninput=\"sdiSetRowUpdate(event)\" id=\"" + variableid + "\" name=\"" + variableid + "\" type=\"text\" class=\"input_field\"  value=\"" + value + "\">");
        }
        rowHtml.append("</td>");
        rowHtml.append("<td>");
        rowHtml.append(inherited ? "<img src='" + inheritedImage + "' border=0 title='" + this.tp.translate("Inherited from " + inheritedFrom) + "'>" : "");
        rowHtml.append("</td>");
        rowHtml.append("</tr></table>");
        rowHtml.append("</td>");
        return rowHtml.toString();
    }

    private PropertyList getPropsFromJSONString(String json) throws SapphireException {
        PropertyList props = new PropertyList();
        if (json.length() > 0) {
            try {
                JSONObject jsonObj = new JSONObject(json);
                props = new PropertyList(jsonObj);
            }
            catch (Exception e) {
                throw new SapphireException("Failed to parse JSON." + e.getMessage());
            }
        }
        return props;
    }

    public void updatePageData(PageTagInfo pageinfo) {
        this.pagedata.setProperty("sdcid", pageinfo.getProperty("sdcid"));
        this.pagedata.setProperty("keyid1", pageinfo.getProperty("keyid1"));
        this.pagedata.setProperty("keyid2", pageinfo.getProperty("keyid2"));
        this.pagedata.setProperty("keyid3", pageinfo.getProperty("keyid3"));
        this.pagedata.setProperty("attachmentoperationid", pageinfo.getProperty("attachmentoperationid"));
    }
}

