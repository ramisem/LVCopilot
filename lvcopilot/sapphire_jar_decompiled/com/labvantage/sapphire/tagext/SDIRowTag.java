/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.JspTagException
 *  javax.servlet.jsp.tagext.Tag
 *  javax.servlet.jsp.tagext.TagSupport
 */
package com.labvantage.sapphire.tagext;

import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.tagext.QueryData;
import com.labvantage.sapphire.tagext.SDIGroupTag;
import com.labvantage.sapphire.tagext.SDITag;
import com.labvantage.sapphire.tagext.SDITagUtil;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;
import sapphire.accessor.SDIProcessor;
import sapphire.tagext.BaseBodyTagSupport;
import sapphire.util.JstlUtil;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;

public class SDIRowTag
extends BaseBodyTagSupport {
    private SDITag _sditag = null;
    private QueryData _querydata = null;
    private String _data = "primary";
    private String _error = "";
    private String _sort = "";
    private String _filter = "";
    private String _filterseparator = ",";
    private String[] _keycols = null;
    private String[] _columns = null;
    private String _fkcolumns = null;
    private String _customcolumns = null;
    private String _templateid = "";
    private String _templateKeyId1 = "";
    private String _templateKeyId2 = "";
    private String _templateKeyId3 = "";
    private String _templateoption = "save";
    private int _startrow = -1;
    private int _endrow = -1;
    private String _initrowsstring = "";
    private int _initrows = 0;
    private int _copies = 1;
    private boolean _templategenerated = false;
    private String _dynamictable = "false";
    private String _var = "currentRow";
    private int _rowCount;

    public String getCustomcolumns() {
        return this._customcolumns;
    }

    public void setCustomcolumns(String _customcolumns) {
        this._customcolumns = _customcolumns;
    }

    public String getFkcolumns() {
        return this._fkcolumns;
    }

    public void setFkcolumns(String _fkcolumns) {
        this._fkcolumns = _fkcolumns;
    }

    public String getTemplatekeyid1() {
        return this._templateKeyId1;
    }

    public void setTemplatekeyid1(String _templateid1) {
        this._templateKeyId1 = _templateid1;
    }

    public String getTemplatekeyid2() {
        return this._templateKeyId2;
    }

    public void setTemplatekeyid2(String _templateid2) {
        this._templateKeyId2 = _templateid2;
    }

    public String getTemplatekeyid3() {
        return this._templateKeyId3;
    }

    public void setTemplatekeyid3(String _templateid3) {
        this._templateKeyId3 = _templateid3;
    }

    public void setData(String data) {
        this._data = data;
    }

    public void setSort(String sort) {
        this._sort = sort;
    }

    public void setFilter(String filter) {
        this._filter = filter;
    }

    public void setFilterseparator(String filterseparator) {
        this._filterseparator = filterseparator;
    }

    public void setStartrow(String startrow) {
        try {
            this._startrow = Integer.parseInt(startrow) - 1;
        }
        catch (Exception e) {
            this._startrow = 0;
        }
    }

    public void setEndrow(String endrow) {
        try {
            this._endrow = Integer.parseInt(endrow) + 1;
        }
        catch (Exception e) {
            this._endrow = 1;
        }
    }

    public void setInitrows(String initrows) {
        this._initrowsstring = initrows;
    }

    public void setCopies(String copies) {
        try {
            this._copies = Integer.parseInt(copies);
        }
        catch (Exception e) {
            this._copies = 0;
        }
    }

    public void setTemplateid(String templateid) {
        this._templateid = templateid;
    }

    public void setTemplateoption(String templateoption) {
        this._templateoption = templateoption;
    }

    public void setVar(String var) {
        this._var = var;
    }

    public void setDynamictable(String dynamictable) {
        this._dynamictable = dynamictable;
    }

    public int doStartTag() throws JspTagException {
        int rc = 2;
        this.doInit();
        this.evaluateExpressions();
        this._sditag = (SDITag)TagSupport.findAncestorWithClass((Tag)this, SDITag.class);
        if (this._sditag != null) {
            SDIGroupTag sdigrouptag = (SDIGroupTag)TagSupport.findAncestorWithClass((Tag)this, SDIGroupTag.class);
            String groupdata = "";
            do {
                groupdata = sdigrouptag != null ? sdigrouptag.getData() : this._data;
                sdigrouptag = (SDIGroupTag)TagSupport.findAncestorWithClass((Tag)sdigrouptag, SDIGroupTag.class);
            } while (!groupdata.equals(this._data));
            if (this._filter != null && this._filter.length() > 0 && sdigrouptag != null && sdigrouptag.getFilter().length() > 0) {
                this._error = this._error + "TAG ERROR: sdirow tag cannot have a filter attribute as well as the group tag";
                rc = 0;
            } else {
                this._rowCount = this._sditag.getData(this._data, "").getRowCount() + this._initrows;
                this._querydata = this._sditag.getData(this._data, this._filter, this._filterseparator);
                if (this._querydata != null) {
                    this._keycols = this._sditag.getKeyCols(this._data);
                    this._columns = this._sditag.getColumns(this._data);
                    if ((this._templateid.length() > 0 || this._templateKeyId1.length() > 0) && (this._templateoption.equals("load") || this._templateoption.equals("loadsave"))) {
                        SDIRequest sdiRequest = new SDIRequest();
                        if (this._templateid.length() > 0) {
                            sdiRequest.setSDIList(this._sditag.getSdcid(), this._templateid, "", "");
                        } else {
                            sdiRequest.setSDIList(this._sditag.getSdcid(), this._templateKeyId1, this._templateKeyId2, this._templateKeyId3);
                        }
                        sdiRequest.setRequestItem("primary");
                        sdiRequest.setExtendedDataTypes(true);
                        SDIProcessor sdiProcessor = new SDIProcessor(this.getConnectionId());
                        SDIData sdiData = sdiProcessor.getSDIData(sdiRequest);
                        if (sdiData != null) {
                            sdiData.sanitizeDataset("primary", this.requestContext.getProperty("sysuserid"), "SDITag", DateTimeUtil.getNowCalendar());
                            sdiData.setKeys("primary", "", "", "");
                            this._querydata.setTemplateData(sdiData.getDataset("primary"));
                            this._querydata.addRows(this._initrows, sdiData.getDataset("primary"));
                        } else {
                            this._error = this._error + "TAG ERROR: Failed to load template data for templateid '" + this._templateid + "'";
                            rc = 0;
                        }
                    } else if (this.requestContext.getProperty("__formcommand").length() == 0) {
                        this._querydata.addRows(this._initrows);
                    }
                } else {
                    this.logError("QueryData object null for data '" + this._data + "' and filter '" + this._filter + "'");
                    rc = 0;
                }
            }
        } else {
            this._error = this._error + "TAG ERROR: sdirow tag must be nested in a sdi tag";
            rc = 0;
        }
        return rc;
    }

    public void doInitBody() throws JspTagException {
        this._querydata.resetRow(this._startrow);
        if (this._sort != null && this._sort.length() > 0) {
            this._querydata.sort(this._sort);
        }
        if (this._dynamictable.equalsIgnoreCase("true") && this._querydata.getRowCount() == 0) {
            this._querydata.setTemplateGenerate();
            this._templategenerated = true;
            this.write("<tr id=\"__" + SDIData.getDatasetCode(this._data) + "_templaterow\" style=\"display:none\"><td><table  style=\"display:none\" id=\"__" + SDIData.getDatasetCode(this._data) + "_templatetable\">\n");
        }
        this._querydata.nextRow(this._endrow);
        this._querydata.addGridRow();
        if (this._sditag.isSDIForm()) {
            this.write(SDITagUtil.getFixedRowInputs(this._data, this._columns, this._rowCount, this._sditag.getId(), null, this._fkcolumns, this._customcolumns));
            if (this._templateoption.equals("save") || this._templateoption.equals("loadsave")) {
                this.write(SDITagUtil.getRepeatedRowInputs(this._data, this._keycols, this._querydata, this._sditag.getId(), this._templateid, this._templateKeyId1, this._templateKeyId2, this._templateKeyId3, this._copies));
            } else {
                this.write(SDITagUtil.getRepeatedRowInputs(this._data, this._keycols, this._querydata, this._sditag.getId(), "", this._copies));
            }
        }
        if (this._querydata.getCurrentRow() >= 0 && this._querydata.getCurrentRow() < this._querydata.getRowCount()) {
            this.pageContext.setAttribute(this._var, this._querydata.get(this._querydata.getCurrentRow()));
        }
    }

    public int doAfterBody() throws JspTagException {
        int rc = 2;
        if (this._querydata.nextRow(this._endrow)) {
            this._querydata.addGridRow();
            if (this._sditag.isSDIForm()) {
                if (this._templateoption.equals("save") || this._templateoption.equals("loadsave")) {
                    this.write(SDITagUtil.getRepeatedRowInputs(this._data, this._keycols, this._querydata, this._sditag.getId(), this._templateid, this._templateKeyId1, this._templateKeyId2, this._templateKeyId3, this._copies));
                } else {
                    this.write(SDITagUtil.getRepeatedRowInputs(this._data, this._keycols, this._querydata, this._sditag.getId(), "", this._copies));
                }
            }
            if (this._querydata.getCurrentRow() >= 0 && this._querydata.getCurrentRow() < this._querydata.getRowCount()) {
                this.pageContext.setAttribute(this._var, this._querydata.get(this._querydata.getCurrentRow()));
            }
        } else if (this._dynamictable.equalsIgnoreCase("true") && this._sditag.isSDIForm()) {
            if (!this._templategenerated) {
                this._templategenerated = true;
                this._querydata.setTemplateGenerate();
                this.write("<tr id=\"__" + this._sditag.getId() + SDIData.getDatasetCode(this._data) + "_templaterow\" style=\"display:none\"><td><table  style=\"display:none\" id=\"__" + this._sditag.getId() + SDIData.getDatasetCode(this._data) + "_templatetable\">\n");
            } else {
                this.write("</table></td></tr>\n");
                this.writeBodyContent();
                rc = 0;
            }
        } else {
            this.writeBodyContent();
            rc = 0;
        }
        return rc;
    }

    @Override
    public int doEndTag() throws JspTagException {
        int rc = 6;
        if (this._error.length() > 0) {
            this.write(this._error);
        }
        this._sditag = null;
        this._querydata = null;
        this._data = "primary";
        this._error = "";
        this._sort = "";
        this._filter = "";
        this._filterseparator = ",";
        this._keycols = null;
        this._columns = null;
        this._fkcolumns = null;
        this._customcolumns = null;
        this._templateid = "";
        this._templateKeyId1 = "";
        this._templateKeyId2 = "";
        this._templateKeyId3 = "";
        this._templateoption = "save";
        this._startrow = -1;
        this._endrow = -1;
        this._initrowsstring = "";
        this._initrows = 0;
        this._copies = 1;
        this._templategenerated = false;
        this._dynamictable = "false";
        this._var = "currentRow";
        super.doEndTag();
        return rc;
    }

    private void evaluateExpressions() {
        this._data = JstlUtil.evaluateExpression(this._data, this.pageContext, "").toString();
        this._templateid = JstlUtil.evaluateExpression(this._templateid, this.pageContext, "").toString();
        this._templateKeyId1 = JstlUtil.evaluateExpression(this._templateKeyId1, this.pageContext, "").toString();
        this._templateKeyId2 = JstlUtil.evaluateExpression(this._templateKeyId2, this.pageContext, "").toString();
        this._templateKeyId3 = JstlUtil.evaluateExpression(this._templateKeyId3, this.pageContext, "").toString();
        this._templateoption = JstlUtil.evaluateExpression(this._templateoption, this.pageContext, "save").toString();
        this._sort = JstlUtil.evaluateExpression(this._sort, this.pageContext, "").toString();
        this._filter = JstlUtil.evaluateExpression(this._filter, this.pageContext, "").toString();
        this._dynamictable = JstlUtil.evaluateExpression(this._dynamictable, this.pageContext, "false").toString();
        this._startrow = Integer.valueOf(JstlUtil.evaluateExpression(String.valueOf(this._startrow), this.pageContext, "-1").toString());
        this._endrow = Integer.valueOf(JstlUtil.evaluateExpression(String.valueOf(this._endrow), this.pageContext, "-1").toString());
        this._initrows = Integer.valueOf(JstlUtil.evaluateExpression(String.valueOf(this._initrowsstring), this.pageContext, "0").toString());
        this._copies = Integer.valueOf(JstlUtil.evaluateExpression(String.valueOf(this._copies), this.pageContext, "0").toString());
    }
}

