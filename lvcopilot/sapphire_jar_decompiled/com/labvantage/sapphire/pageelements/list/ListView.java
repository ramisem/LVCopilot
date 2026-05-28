/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.list;

import com.labvantage.sapphire.pageelements.list.List;
import com.labvantage.sapphire.tagext.QueryData;
import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import sapphire.accessor.SDCProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.tagext.SDITagInfo;
import sapphire.util.DataSet;
import sapphire.util.JstlUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ListView
extends BaseElement {
    static final String LABVANTAGE_CVS_ID = "$Revision: 51120 $";
    public static final String PROPERTY_COLUMNS = "columns";
    public static final String PROPERTY_DATASETNAME = "datasetname";
    public static final String PROPERTY_DATASET = "dataset";
    public static final String PROPERTY_SHOWGROUPBY = "showgroupby";
    public static final String PROPERTY_SHOWCOLLAPSEALL = "showcollapseall";
    public static final String PROPERTY_SORTBY = "sortby";
    public static final String PROPERTY_GROUPBY = "groupby";
    public static final String VALUE_NO = "N";
    private String datasetName = "primary";
    private QueryData queryData;
    private String listmode = "list";

    public ListView(PageContext pageContext, DataSet dataset, String connectionid) {
        this.logger.info("ListView (overload 4) created...");
        this.init(pageContext, this.datasetName, new QueryData(this.datasetName, dataset), connectionid);
    }

    public ListView(PageContext pageContext, QueryData querydata, String connectionid) {
        this.logger.info("ListView (overload 3) created...");
        this.init(pageContext, this.datasetName, querydata, connectionid);
    }

    public ListView(PageContext pageContext, String datasetName, DataSet dataset, String connectionid) {
        this.logger.info("ListView (overload 2) created...");
        this.init(pageContext, datasetName, new QueryData(datasetName, dataset), connectionid);
    }

    public ListView() {
        this.logger.info("ListView (overload 1) created...");
    }

    public void setListmode(String listmode) {
        this.listmode = listmode;
    }

    private void init(PageContext pageContext, String datasetName, QueryData querydata, String connectionid) {
        this.logger.info("init called...");
        this.pageContext = pageContext;
        this.queryData = querydata;
        this.datasetName = datasetName;
        HashMap<String, QueryData> datamap = new HashMap<String, QueryData>();
        datamap.put(datasetName, this.queryData);
        this.sdiInfo = new SDITagInfo(datamap);
        this.setConnectionId(connectionid);
    }

    public void setDataSet(String datasetName, DataSet dataSet) {
        HashMap<String, QueryData> datamap = new HashMap<String, QueryData>();
        datamap.put(datasetName, this.queryData);
        this.sdiInfo = new SDITagInfo(datamap);
    }

    private void createQueryData() {
        this.logger.info("createQueryData called...");
        String dataset = this.element.getProperty(PROPERTY_DATASET);
        String datasetName = this.element.getProperty(PROPERTY_DATASETNAME, this.datasetName);
        this.logger.debug("dataset (1) = " + dataset);
        this.logger.debug("datasetName = " + datasetName);
        if (dataset.indexOf("${") < 0) {
            dataset = "${" + dataset + "}";
        }
        this.logger.debug("dataset (2) = " + dataset);
        Object dataObject = JstlUtil.evaluateExpression(dataset, this.pageContext);
        if (dataObject != null) {
            if (dataObject instanceof QueryData) {
                this.queryData = (QueryData)dataObject;
                this.init(this.pageContext, datasetName, this.queryData, this.getConnectionId());
            } else if (dataObject instanceof DataSet) {
                this.queryData = new QueryData((DataSet)dataObject);
                this.init(this.pageContext, datasetName, this.queryData, this.getConnectionId());
            } else {
                this.logger.error("Element " + this.elementid + " Cannot handle the data object type:" + dataObject.getClass().getName());
                this.queryData = null;
            }
        } else {
            this.logger.error("No data found for the " + this.elementid + " element");
            this.queryData = null;
        }
    }

    @Override
    public SDCProcessor getSDCProcessor() {
        SDCProcessor sdcproc = this.pageContext != null ? new SDCProcessor(this.pageContext) : super.getSDCProcessor();
        return sdcproc;
    }

    @Override
    public String getHtml() {
        this.logger.info("getHtml called...");
        StringBuffer html = new StringBuffer();
        String send = "";
        if (this.element != null) {
            if (this.queryData == null && this.sdiInfo == null) {
                this.createQueryData();
            }
            if (this.sdiInfo != null) {
                PropertyListCollection columns = this.element.getCollection(PROPERTY_COLUMNS);
                if (columns != null && columns.size() > 0) {
                    this.correctProperties();
                    this.logger.debug("About to create list element...");
                    List list = new List(this.pageContext, this.sdiInfo, this.getSDCProcessor());
                    if (this.elementid != null && this.elementid.length() > 0) {
                        list.setElementid(this.elementid + "_list");
                        if (this.element.getId().length() == 0) {
                            this.element.setId(this.elementid + "_list");
                        }
                    }
                    list.setElementProperties(this.element);
                    list.setDatasetName(this.datasetName);
                    list.setListmode(this.listmode);
                    this.logger.debug("List element created.");
                    html.append(list.getHtml());
                    this.logger.debug("HTML generated.");
                } else {
                    this.logger.error("No columns defined in the element properties.");
                }
            }
        } else {
            this.logger.error("No element properties found for element " + this.elementid + ".");
        }
        if (this.debugErrorMsg != null && this.debugErrorMsg.length() > 0) {
            send = this.getError();
        } else if (html.length() > 0) {
            send = html.toString();
        }
        return send;
    }

    private void correctProperties() {
        this.logger.info("correctProperties called...");
        String temp = this.element.getProperty(PROPERTY_SHOWGROUPBY);
        if (temp == null || temp.length() == 0) {
            this.element.setProperty(PROPERTY_SHOWGROUPBY, VALUE_NO);
        }
        if ((temp = this.element.getProperty(PROPERTY_SHOWCOLLAPSEALL)) == null || temp.length() == 0) {
            this.element.setProperty(PROPERTY_SHOWCOLLAPSEALL, VALUE_NO);
        }
        if ((temp = this.element.getProperty("selectortype")) == null || temp.length() == 0) {
            this.element.setProperty("selectortype", "checkbox");
        }
        if ((temp = this.element.getProperty("initselectall")) == null || temp.length() == 0) {
            this.element.setProperty("initselectall", VALUE_NO);
        }
        if ((temp = this.element.getProperty("initexpandall")) == null || temp.length() == 0) {
            this.element.setProperty("initexpandall", "Y");
        }
        if ((temp = this.element.getProperty("initialgrouped")) == null || temp.length() == 0) {
            this.element.setProperty("initialgrouped", "Y");
        }
        if ((temp = this.element.getProperty("rowsperpage")) == null || temp.length() == 0) {
            this.element.setProperty("rowsperpage", "");
        }
        if ((temp = this.element.getProperty("retrievelimit")) == null || temp.length() == 0) {
            this.element.setProperty("retrievelimit", "");
        }
        if (this.element.getCollection(PROPERTY_SORTBY) == null) {
            PropertyListCollection tempCol1 = new PropertyListCollection();
            PropertyList tempItem1 = new PropertyList();
            tempItem1.setProperty("id", "");
            tempItem1.setProperty("columnid", "");
            tempItem1.setProperty("asc_desc", "");
            tempItem1.setProperty("callback", "");
            tempCol1.add(tempItem1);
            this.element.setProperty(PROPERTY_SORTBY, tempCol1);
        }
    }
}

