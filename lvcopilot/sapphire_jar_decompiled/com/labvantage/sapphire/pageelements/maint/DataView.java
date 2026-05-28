/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.maint;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.pageelements.ElementUtil;
import com.labvantage.sapphire.pageelements.list.List;
import com.labvantage.sapphire.pageelements.maint.Maint;
import com.labvantage.sapphire.tagext.QueryData;
import com.labvantage.sapphire.tagext.SDITagUtil;
import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.tagext.SDITagInfo;
import sapphire.util.DataSet;
import sapphire.util.FormatUtil;
import sapphire.util.JstlUtil;
import sapphire.util.SDIData;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class DataView
extends BaseElement {
    private String datasetName = "primary";
    private QueryData queryData;
    private String propertyHandler;
    private String[] keyCols;
    private boolean renderTagsJS = true;
    private String sdcid = null;

    public DataView(PageContext pageContext, String datasetName, SDITagInfo sdiinfo, String propertyHandler, String connectionid) {
        this.init(pageContext, datasetName, sdiinfo, propertyHandler, connectionid);
    }

    public DataView(PageContext pageContext, SDITagInfo sdiinfo, String connectionid) {
        this.init(pageContext, this.datasetName, sdiinfo, "", connectionid);
    }

    public DataView(PageContext pageContext, DataSet dataset, String connectionid) {
        this.init(pageContext, this.datasetName, dataset, "", connectionid);
    }

    public DataView(PageContext pageContext, QueryData querydata, String connectionid) {
        this.init(pageContext, querydata, "", connectionid);
    }

    public DataView(PageContext pageContext, String datasetName, DataSet dataset, String propertyHandler, String connectionid) {
        this.init(pageContext, datasetName, dataset, propertyHandler, connectionid);
    }

    public DataView(PageContext pageContext, QueryData querydata, String propertyHandler, String connectionid) {
        this.init(pageContext, querydata, propertyHandler, connectionid);
    }

    private void init(PageContext pageContext, String datasetName, DataSet dataset, String propertyHandler, String connectionid) {
        this.init(pageContext, new QueryData(datasetName, dataset), propertyHandler, connectionid);
    }

    private void init(PageContext pageContext, QueryData querydata, String propertyHandler, String connectionid) {
        this.init(pageContext, connectionid);
        this.queryData = querydata;
        this.datasetName = querydata.getDatasetName();
        this.propertyHandler = propertyHandler;
        this.setSDIInfo(this.buildSDITagInfo(querydata));
        if (pageContext != null) {
            this.sdiInfo.setPageContext(pageContext);
        }
    }

    private void init(PageContext pageContext, String datasetName, SDITagInfo sdiInfo, String propertyHandler, String connectionid) {
        this.init(pageContext, connectionid);
        this.queryData = sdiInfo.getQueryData(datasetName);
        this.datasetName = datasetName;
        this.propertyHandler = propertyHandler;
        this.setSDIInfo(sdiInfo);
    }

    private void init(PageContext pageContext, String connectionid) {
        this.setPageContext(pageContext);
        this.setConnectionId(connectionid);
    }

    public DataView() {
    }

    public void setKeyCols(String[] keyCols) {
        this.keyCols = keyCols;
    }

    public void setSDCId(String sdcId) {
        this.sdcid = sdcId;
    }

    private SDITagInfo buildSDITagInfo(QueryData queryData) {
        DataSet dataset = queryData.getQuerydata();
        if (!dataset.isValidColumn("__rowstatus")) {
            dataset.addColumn("__rowstatus", 0);
            dataset.setString(-1, "__rowstatus", "S");
        }
        if (!dataset.isValidColumn("__rowid")) {
            dataset.addColumn("__rowid", 0);
            for (int row = 0; row < dataset.getRowCount(); ++row) {
                dataset.setString(row, "__rowid", "" + row);
            }
        }
        HashMap<String, QueryData> datamap = new HashMap<String, QueryData>();
        datamap.put(this.datasetName, queryData);
        return new SDITagInfo(datamap);
    }

    @Override
    public String getHtml() {
        DataSet paramlistitem;
        if (this.element == null) {
            return "No element data found for the " + this.elementid + " element.";
        }
        if (this.queryData == null) {
            String dataset = this.element.getProperty("dataset");
            String datasetName = this.element.getProperty("datasetname", this.datasetName);
            String propertyHandler = this.element.getProperty("propertyhandler");
            Object dataObject = null;
            if (dataset.length() > 0) {
                if (dataset.indexOf("${") < 0) {
                    dataset = "${" + dataset + "}";
                }
                dataObject = JstlUtil.evaluateExpression(dataset, this.pageContext);
            } else {
                String sql = this.element.getProperty("sql");
                if (sql.length() > 0) {
                    try {
                        dataObject = this.getDataSet(sql);
                    }
                    catch (Exception e) {
                        return e.getMessage();
                    }
                }
            }
            if (dataObject == null) {
                return "No data found for the " + this.elementid + " element";
            }
            if (dataObject instanceof QueryData) {
                this.init(this.pageContext, datasetName, ((QueryData)dataObject).getQuerydata(), propertyHandler, this.getConnectionId());
            } else if (dataObject instanceof DataSet) {
                this.init(this.pageContext, datasetName, (DataSet)dataObject, propertyHandler, this.getConnectionId());
            } else {
                return "Element " + this.elementid + " Cannot handle the data object type:" + dataObject.getClass().getName();
            }
        }
        boolean renderUpdateFields = this.propertyHandler != null && this.propertyHandler.length() > 0;
        StringBuffer html = new StringBuffer();
        if ("paramlistitem".equals(this.datasetName) && (paramlistitem = this.queryData.getQuerydata()) != null) {
            for (int i = 0; i < paramlistitem.getRowCount(); ++i) {
                if ((paramlistitem.getValue(i, "defaultvalue").length() <= 0 || !"N".equals(paramlistitem.getValue(i, "datatypes"))) && !"NC".equals(paramlistitem.getValue(i, "datatypes"))) continue;
                paramlistitem.setValue(i, "defaultvalue", StringUtil.replaceAll(paramlistitem.getValue(i, "defaultvalue"), FormatUtil.getInstance().getDecimalSeparator() + "", FormatUtil.getInstance(this.connectionInfo).getDecimalSeparator() + ""));
            }
        }
        if (this.renderTagsJS) {
            html.append("<script language=\"JavaScript\" src=\"WEB-CORE/scripts/tags.js\"></script>\n");
        }
        html.append("<script language=\"JavaScript\" src=\"WEB-CORE/elements/scripts/dataview.js\"></script>\n");
        PropertyListCollection columns = this.element.getCollection("columns");
        if (columns != null && columns.size() > 0) {
            if (this.element.containsKey("sortby") || "list".equals(this.element.getProperty("mode"))) {
                if (this.element.getProperty("showgroupby").length() == 0) {
                    this.element.setProperty("showgroupby", "N");
                }
                if (this.element.getProperty("showcollapseall").length() == 0) {
                    this.element.setProperty("showcollapseall", "N");
                }
                List list = new List(this.pageContext, this.sdiInfo, this.getSDCProcessor());
                list.setElementProperties(this.element);
                html.append(list.getHtml());
            } else {
                Maint maint = new Maint(this.pageContext, this.sdiInfo, this.getConnectionId());
                maint.setElementProperties(this.element);
                maint.setDatasetName(this.datasetName);
                if (this.sdcid != null && this.sdcid.length() > 0) {
                    maint.setSDCId(this.sdcid);
                }
                if (this.elementid.length() > 0) {
                    html.append("<input type=\"hidden\" name=\"__").append(this.elementid).append("_dataset\" id=\"__").append(this.elementid).append("_dataset\" value=\"").append(this.datasetName).append("\">\n");
                    if (this.sdcid != null && this.sdcid.length() > 0) {
                        String tableid = this.getSDCProcessor().getProperty(this.sdcid, "tableid");
                        html.append("<input type=\"hidden\" name=\"__").append(this.elementid).append("_tableid\" id=\"__").append(this.elementid).append("_tableid\" value=\"").append(tableid).append("\">\n");
                    }
                    if (this.keyCols != null && this.keyCols.length > 0) {
                        StringBuffer keys = new StringBuffer();
                        for (int i = 0; i < this.keyCols.length; ++i) {
                            if (i > 0) {
                                keys.append(";");
                            }
                            keys.append(this.keyCols[i]);
                        }
                        html.append("<input type=\"hidden\" name=\"__").append(this.elementid).append("_keycols\" id=\"__").append(this.elementid).append("_keycols\" value=\"").append(keys.toString()).append("\">\n");
                    } else {
                        html.append("<input type=\"hidden\" name=\"__").append(this.elementid).append("_keycols\" id=\"__").append(this.elementid).append("_keycols\" value=\"").append("").append("\">\n");
                    }
                }
                if (renderUpdateFields) {
                    String customseperator = this.element.getProperty("customseperator");
                    String[] cols = this.queryData.getQuerydata().getColumns();
                    if (customseperator == null || customseperator.length() == 0) {
                        html.append(SDITagUtil.getFixedRowInputs(this.datasetName, cols, this.queryData.getRowCount(), this.prefix == null ? "" : this.prefix));
                    } else {
                        html.append(SDITagUtil.getFixedRowInputs(this.datasetName, cols, this.queryData.getRowCount(), this.prefix == null ? "" : this.prefix, customseperator));
                    }
                }
                if (this.queryData.getRowCount() > 0) {
                    for (int row = 0; row < this.queryData.getRowCount(); ++row) {
                        this.queryData.nextRow(this.queryData.getRowCount());
                        html.append(maint.getHtml());
                        if (!renderUpdateFields) continue;
                        html.append(SDITagUtil.getRepeatedRowInputs(this.datasetName, this.keyCols, this.queryData, this.prefix == null ? "" : this.prefix, "", 0));
                        int cr = row;
                        try {
                            cr = Integer.parseInt(this.queryData.getRowId(this.queryData.getCurrentRow()));
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                        html.append("<input type=\"hidden\" name=\"__").append(this.datasetName).append(cr).append("_sequence\" id=\"__").append(this.datasetName).append(cr).append("_sequence\" value=\"").append(cr + 1).append("\"/>");
                    }
                } else {
                    this.queryData.setHeaderGenerate();
                    html.append(maint.getHtml());
                }
                if (renderUpdateFields) {
                    this.queryData.setTemplateGenerate();
                    DataSet tempDs = null;
                    for (int i = 0; i < columns.size(); ++i) {
                        PropertyList column = columns.getPropertyList(i);
                        if (column.getProperty("defaultvalue").length() <= 0) continue;
                        String columnid = column.getProperty("columnid");
                        if (tempDs == null) {
                            tempDs = new DataSet(this.connectionInfo);
                        }
                        tempDs.addRow();
                        tempDs.addColumn(columnid, 0);
                        tempDs.setValue(0, columnid, ElementUtil.evaluateExpression(this.datasetName, 0, columnid, column.getProperty("defaultvalue"), this.sdiInfo, null));
                        this.queryData.setTemplateData(tempDs);
                    }
                    html.append("<table  style=\"display:none\" id=\"__").append(SDIData.getDatasetCode(this.datasetName)).append("_templatetable\">\n");
                    html.append(maint.getHtml());
                    html.append("</table>\n");
                    if (!this.propertyHandler.equals("[default]")) {
                        html.append("<input type=\"hidden\" name=\"").append("__propertyhandler_").append(this.elementid).append("\" value=\"").append(this.propertyHandler).append("\"/>");
                        html.append("<input type=\"hidden\" name=\"").append("__propertyhandler_").append("datasetname\" value=\"").append(this.datasetName).append("\"/>");
                    }
                }
            }
        } else {
            TranslationProcessor tp = this.getTranslationProcessor();
            if (tp != null) {
                html.append("No columns defined");
            } else {
                html.append(tp.translate("No columns defined"));
            }
        }
        return html.toString();
    }

    public void setRenderTagsJS(boolean value) {
        this.renderTagsJS = value;
    }

    protected DataSet getDataSet(String sql) throws SapphireException {
        DataSet ds;
        String[] tokens = StringUtil.getTokens(sql);
        if (tokens != null && tokens.length > 0) {
            for (String key : tokens) {
                String value = "sysuserid".equals(key) || "currentuser".equals(key) ? this.getConnectionProcessor().getSapphireConnection().getSysuserId() : this.requestContext.getProperty(key);
                sql = StringUtil.replaceAll(sql, "[" + key + "]", value);
            }
        }
        if ((ds = this.getQueryProcessor().getSqlDataSet(sql)) == null) {
            throw new SapphireException(ErrorUtil.extractMessage("Exception caught while rendering this Element. Please contact your Administrator. Exception: Dataset failure for query:<br>" + sql, ErrorUtil.isUserAdmin(this.getConnectionId())));
        }
        return ds;
    }
}

