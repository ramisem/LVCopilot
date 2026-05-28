/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.maint;

import com.labvantage.sapphire.pageelements.maint.MaintColumn;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.tagext.QueryData;
import com.labvantage.sapphire.util.cache.CacheUtil;
import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.servlet.RequestContext;
import sapphire.tagext.SDITagInfo;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class EditorStyleField
extends MaintColumn {
    private String editorstyleid = "";
    private String customValue = "";
    private String customId = "";
    private String customChange = "";
    private String customDataset = "";
    private String customPrefix = "";
    private int customRow = -1;
    private boolean useNoName = false;
    private Boolean readonly = null;
    public static final String CUSTOMID = "__CUSTOM";
    public static final String DEFAULTID = "__DEFAULT";

    public EditorStyleField(PageContext pageContext, SDITagInfo sdiInfo, String connectionid) {
        super(pageContext, sdiInfo, connectionid);
        this.setRowStatus("S");
    }

    public EditorStyleField(PageContext pageContext) {
        super(pageContext, null, RequestContext.getRequestContext(pageContext).getConnectionId());
        this.setRowStatus("S");
    }

    public String getColumnProperty(String propertyId) {
        PropertyList column = super.getColumn();
        if (column != null) {
            return column.getProperty(propertyId);
        }
        return "";
    }

    public void setFieldName(String name) {
        this.customId = name;
        super.setColumnProperty("columnid", name);
    }

    public String getFieldName() {
        if (this.customId.length() > 0) {
            return this.customId;
        }
        return super.getColumn().getProperty("columnid");
    }

    @Override
    public void setPrefix(String prefix) {
        this.customPrefix = prefix;
    }

    @Override
    public void setDatasetname(String dataset) {
        this.customDataset = dataset;
        super.setDatasetname(dataset);
    }

    public void setRowNumber(int rowNumber) {
        this.customRow = rowNumber;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = new Boolean(readonly);
    }

    public void setUseNoNameAttribute(boolean use) {
        this.useNoName = use;
    }

    public void setFieldValue(String value) {
        this.customValue = value;
    }

    public String getFieldValue() {
        if (this.customValue != null) {
            return this.customValue;
        }
        return "";
    }

    public void setColumnDefinition(String description, String datatype, int length, boolean pk) {
        this.setColumnDefinition(description, datatype, length, pk, false);
    }

    public void setColumnDefinition(String description, String editorStyleDatatype, int length, boolean pk, boolean timezonIndependent) {
        PropertyList columnDefinition = super.getColumnDefinition();
        if (columnDefinition == null) {
            columnDefinition = new PropertyList();
            super.setColumnDefinition(columnDefinition);
        }
        columnDefinition.setProperty("columndesc", description);
        columnDefinition.setProperty("datatype", "" + EditorStyleField.getColumnDataType(editorStyleDatatype));
        columnDefinition.setProperty("columnlength", "" + length);
        columnDefinition.setProperty("pkflag", pk ? "Y" : "N");
        columnDefinition.setProperty("timezoneindependent", timezonIndependent ? "Y" : "N");
    }

    public static String getEditorStyleDataType(String dataType) {
        if (dataType.equalsIgnoreCase("C") || dataType.equalsIgnoreCase("string")) {
            return "S";
        }
        if (dataType.equalsIgnoreCase("T") || dataType.equalsIgnoreCase("clob")) {
            return "C";
        }
        if (dataType.equalsIgnoreCase("N") || dataType.equalsIgnoreCase("number")) {
            return "N";
        }
        if (dataType.equalsIgnoreCase("D") || dataType.equalsIgnoreCase("date")) {
            return "D";
        }
        if (dataType.equalsIgnoreCase("o") || dataType.equalsIgnoreCase("dateonly")) {
            return "O";
        }
        return dataType;
    }

    public static String getColumnDataType(String dataType) {
        if (dataType.equalsIgnoreCase("S")) {
            return "C";
        }
        if (dataType.equalsIgnoreCase("C")) {
            return "T";
        }
        if (dataType.equalsIgnoreCase("N")) {
            return "N";
        }
        if (dataType.equalsIgnoreCase("D") || dataType.equalsIgnoreCase("O")) {
            return "D";
        }
        return dataType;
    }

    public void setLinkDefinition(String reftype, boolean validated) {
        PropertyList columnDefinition = super.getColumnDefinition();
        if (columnDefinition == null) {
            columnDefinition = new PropertyList();
            super.setColumnDefinition(columnDefinition);
        }
        PropertyList columnLink = new PropertyList();
        columnDefinition.setProperty("link", columnLink);
        columnLink.setProperty("linksdcid", "");
        columnLink.setProperty("type", validated ? "V" : "R");
        columnLink.setProperty("reftypeid", reftype);
        columnLink.setProperty("sdccolumnid2", "");
        columnLink.setProperty("versionedflag", "N");
    }

    public void setLinkDefinition(String linksdcid, char linktype, String sdccolumnid2, boolean versioned) {
        PropertyList columnDefinition = super.getColumnDefinition();
        if (columnDefinition == null) {
            columnDefinition = new PropertyList();
            super.setColumnDefinition(columnDefinition);
        }
        PropertyList columnLink = new PropertyList();
        columnDefinition.setProperty("link", columnLink);
        columnLink.setProperty("linksdcid", linksdcid);
        columnLink.setProperty("type", "" + linktype);
        columnLink.setProperty("reftypeid", "");
        columnLink.setProperty("sdccolumnid2", sdccolumnid2);
        columnLink.setProperty("versionedflag", versioned ? "Y" : "N");
    }

    @Override
    public String getHtml() {
        PropertyList column = this.getColumn();
        boolean nodata = false;
        if (this.sdiInfo == null || this.sdiInfo.getDataSet(this.getDatasetname()) == null) {
            nodata = true;
            HashMap<String, QueryData> querymap = new HashMap<String, QueryData>();
            DataSet dsempty = new DataSet();
            querymap.put(this.getDatasetname(), new QueryData(this.getDatasetname(), dsempty));
            this.sdiInfo = new SDITagInfo(querymap);
            if (this.pageContext != null) {
                this.sdiInfo.setPageContext(this.pageContext);
            }
        }
        if (super.getAttributes() == null || super.getInputAttributes().size() == 0) {
            super.setAttributes(super.getInputAttributes());
        }
        super.getAttributes().setProperty("editorstyleid", this.editorstyleid.length() > 0 ? this.editorstyleid : CUSTOMID);
        if (this.customValue.length() > 0) {
            super.getAttributes().setProperty("value", this.customValue);
        }
        if (this.customRow > -1 && nodata) {
            super.getAttributes().setProperty("rowindex", "" + this.customRow);
        }
        if (this.customPrefix.length() > -1 && nodata) {
            super.getAttributes().setProperty("_prefix", this.customPrefix);
        }
        if (this.customId.length() > 0) {
            super.getAttributes().setProperty("name", this.customId);
        } else if (column.getProperty("columnid").length() > 0 && nodata) {
            super.getAttributes().setProperty("name", super.getAttributes().getProperty("_prefix") + SDIData.getDatasetCode(this.getDatasetname()) + this.customRow + "_" + column.getProperty("columnid"));
        }
        if (this.readonly != null) {
            if (this.readonly.booleanValue()) {
                super.getAttributes().setProperty("readonly", "true");
            } else if (nodata && !this.readonly.booleanValue()) {
                super.getAttributes().setProperty("readonly", "false");
            }
        }
        if (this.customChange.length() > 0) {
            super.getAttributes().setProperty("onchange", this.customChange);
        }
        if (column.getProperty("oninput").length() > 0) {
            super.getAttributes().setProperty("oninput", super.getColumn().getProperty("oninput"));
        }
        if (this.useNoName) {
            super.getAttributes().setProperty("nonameattribute", "Y");
        }
        if (column.getProperty("sdcid", "").length() > 0) {
            super.getAttributes().setProperty("sdcid", column.getProperty("sdcid"));
            PropertyList dropdowndef = new PropertyList();
            dropdowndef.setProperty("sdcid", column.getProperty("sdcid"));
            dropdowndef.setProperty("queryfrom", column.getProperty("queryfrom"));
            dropdowndef.setProperty("querywhere", column.getProperty("querywhere"));
            dropdowndef.setProperty("queryorderby", column.getProperty("queryorderby"));
            dropdowndef.setProperty("valuecolumn", column.getProperty("valuecolumn"));
            dropdowndef.setProperty("displaycolumn", column.getProperty("displaycolumn"));
            super.getAttributes().setProperty("dropdowndefinition", dropdowndef);
            super.getAttributes().setProperty("reftypeid", "");
            super.getAttributes().setProperty("sql", "");
        } else if (column.getProperty("reftypeid", "").length() > 0) {
            super.getAttributes().setProperty("sdcid", "");
            if (super.getAttributes().containsKey("dropdowndefinition")) {
                super.getAttributes().remove("dropdowndefinition");
            }
            super.getAttributes().setProperty("sql", "");
            super.getAttributes().setProperty("reftypeid", column.getProperty("reftypeid"));
        }
        PropertyList coldef = super.getColumnDefinition();
        if (coldef != null) {
            String dt = coldef.getProperty("datatype");
            if (dt.equalsIgnoreCase("T")) {
                super.getAttributes().setProperty("size", super.getAttributes().getProperty("size", "50"));
            } else if (!super.getAttributes().getProperty("mode").equalsIgnoreCase("dropdownlist")) {
                super.getAttributes().setProperty("size", "25");
            } else {
                super.getAttributes().setProperty("size", "1");
            }
        }
        if (column.getProperty("editorstyledatatype", "S").equalsIgnoreCase("O")) {
            super.getAttributes().setProperty("format", "O");
        }
        if (column.getProperty("mode").equalsIgnoreCase("inputarea")) {
            String r = column.getProperty("rows");
            String c = column.getProperty("columns");
            if (r.length() > 0) {
                try {
                    r = "" + Integer.parseInt(r);
                    super.getAttributes().setProperty("rows", r);
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            if (c.length() > 0) {
                try {
                    c = "" + Integer.parseInt(c);
                    super.getAttributes().setProperty("cols", c);
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }
        if (column.getProperty("css", "").length() > 0) {
            String style = super.getAttributes().getProperty("style", "");
            style = style + (style.length() > 0 ? ";" : "") + column.getProperty("css", "");
            super.getAttributes().setProperty("style", style);
        }
        return super.getHtml();
    }

    public void setDefaultEditorStyleProperties(String editorStyleDatatype, String sdcid, String reftypeid) {
        this.setEditorStyleProperties(DEFAULTID, EditorStyleField.getDefaultProperties(editorStyleDatatype, sdcid, reftypeid));
    }

    public void setEditorStyleProperties(String editorStyldId, PropertyList pl) {
        this.editorstyleid = editorStyldId;
        super.setColumn(pl);
    }

    @Override
    public PropertyList getColumn() {
        PropertyList col = super.getColumn();
        if (col.getProperty("displayvalue", "").length() == 0 && (col.getProperty("checkedvalue", "").length() > 0 || col.getProperty("uncheckedvalue", "").length() > 0)) {
            if (col.getProperty("checkedvalue", "").length() == 0 || col.getProperty("uncheckedvalue", "").length() == 0) {
                this.logger.warn("Found either checked or unchecked value without matching unchecked or checked value.");
            } else {
                col.setProperty("displayvalue", col.getProperty("checkedvalue", "") + ";" + col.getProperty("uncheckedvalue", ""));
            }
        }
        return col;
    }

    public static PropertyList getDefaultProperties(String editorStyleDatatype, String sdcid, String reftypeid) {
        PropertyList pl = new PropertyList();
        String datatype = editorStyleDatatype;
        if (datatype.equalsIgnoreCase("D") || datatype.equalsIgnoreCase("O")) {
            pl.setProperty("mode", "datelookup");
            pl.setProperty("validation", "Date");
            pl.setProperty("editorstyledatatype", datatype.toUpperCase());
        } else if (datatype.equalsIgnoreCase("N")) {
            pl.setProperty("mode", "input");
            pl.setProperty("validation", "Number( to )");
            pl.setProperty("editorstyledatatype", "N");
        } else if (datatype.equalsIgnoreCase("C")) {
            pl.setProperty("mode", "inputarea");
            pl.setProperty("editorstyledatatype", "C");
        } else {
            if (sdcid != null && sdcid.length() > 0) {
                pl.setProperty("mode", "lookup");
                PropertyList lll = new PropertyList();
                lll.setProperty("sdcid", sdcid);
                pl.setProperty("lookuplink", lll);
            } else if (reftypeid != null && reftypeid.length() > 0) {
                pl.setProperty("mode", "dropdownlist");
                pl.setProperty("reftypeid", reftypeid);
            } else {
                pl.setProperty("mode", "input");
            }
            pl.setProperty("editorstyledatatype", "S");
        }
        return pl;
    }

    public String getEditorStyleDataType() {
        String s = this.getColumnProperty("editorstyledatatype");
        if (s.length() == 0) {
            s = "S";
        }
        return s;
    }

    public void setEditorStyleId(String editorstyleid) throws SapphireException {
        this.setEditorStyleId(editorstyleid, null, null);
    }

    public void setEditorStyleId(String editorstyleid, PropertyList map) throws SapphireException {
        this.setEditorStyleId(editorstyleid, null, null, map);
    }

    public static PropertyList getEditorStyleProperties(String editorstyleid, String sdcid, String reftypeid, SapphireConnection sapphireConnection) throws SapphireException {
        return EditorStyleField.getEditorStyleProperties(editorstyleid, sdcid, reftypeid, sapphireConnection, new QueryProcessor(sapphireConnection.getConnectionId()));
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static PropertyList getEditorStyleProperties(String editorstyleid, String sdcid, String reftypeid, SapphireConnection sapphireConnection, QueryProcessor qp) throws SapphireException {
        String datatype;
        PropertyList pl = (PropertyList)CacheUtil.get(sapphireConnection.getDatabaseId(), "EditorStyle", editorstyleid.toLowerCase());
        if (pl == null) {
            SafeSQL safeSQL = new SafeSQL();
            DataSet ds = qp.getPreparedSqlDataSet("SELECT editorstyle.* FROM editorstyle WHERE editorstyleid = " + safeSQL.addVar(editorstyleid), safeSQL.getValues(), true);
            if (ds == null || ds.getRowCount() != 1) throw new SapphireException("Editor style could not be found.");
            String xml = ds.getClob(0, "editordefinition");
            if (xml.length() <= 0) throw new SapphireException("Editor XML is empty.");
            pl = new PropertyList();
            try {
                pl.setPropertyList(xml);
                pl.setProperty("editorstyledatatype", ds.getValue(0, "datatype", "S"));
                CacheUtil.put(sapphireConnection.getDatabaseId(), "EditorStyle", editorstyleid.toLowerCase(), pl);
                pl = pl.copy();
            }
            catch (Exception e) {
                throw new SapphireException("Could not load editor xml.", e);
            }
        } else {
            pl = pl.copy();
        }
        if (((datatype = pl.getProperty("editorstyledatatype", "S")).equalsIgnoreCase("D") || datatype.equalsIgnoreCase("O")) && pl.getProperty("validation").length() == 0) {
            pl.setProperty("validation", "Date");
        } else if (datatype.equalsIgnoreCase("N") && pl.getProperty("validation").length() == 0) {
            pl.setProperty("validation", "Number( to )");
        }
        if (pl.getProperty("mode", "").equalsIgnoreCase("lookup")) {
            pl.setProperty("reftypeid", "");
            if (pl.containsKey("lookuplink") && pl.getPropertyList("lookuplink").getProperty("reftypeid", "").length() > 0) {
                pl.setProperty("reftypeid", pl.getPropertyList("lookuplink").getProperty("reftypeid", ""));
                pl.getPropertyList("lookuplink").setProperty("reftypeid", "");
            }
        } else {
            if (pl.containsKey("lookuplink")) {
                pl.remove("lookuplink");
            }
            if (pl.getProperty("mode", "").equalsIgnoreCase("checkbox")) {
                pl.setProperty("sdcid", "");
                pl.setProperty("sql", "");
            }
        }
        if (sdcid != null && sdcid.length() > 0) {
            PropertyList lll;
            if (pl.getProperty("mode", "").equalsIgnoreCase("dropdownlist") || pl.getProperty("mode", "").equalsIgnoreCase("dropdowncombo") || pl.getProperty("mode", "").startsWith("radiobutton")) {
                if (pl.getProperty("sdcid", "").length() != 0 || pl.getProperty("reftypeid", "").length() != 0) return pl;
                pl.setProperty("sdcid", sdcid);
                pl.setProperty("reftypeid", "");
                pl.setProperty("sql", "");
                return pl;
            } else if (pl.containsKey("lookuplink")) {
                lll = pl.getPropertyList("lookuplink");
                if (lll.getProperty("sdcid", "").length() != 0 || pl.getProperty("reftypeid", "").length() != 0) return pl;
                lll.setProperty("sdcid", sdcid);
                pl.setProperty("sdcid", "");
                pl.setProperty("reftypeid", "");
                pl.setProperty("sql", "");
                return pl;
            } else {
                lll = new PropertyList();
                pl.setProperty("lookuplink", lll);
                lll.setProperty("sdcid", sdcid);
                pl.setProperty("sdcid", "");
                pl.setProperty("reftypeid", "");
                pl.setProperty("sql", "");
            }
            return pl;
        } else {
            if (reftypeid == null || reftypeid.length() <= 0) return pl;
            if (pl.getProperty("mode", "").equalsIgnoreCase("lookup") && pl.containsKey("lookuplink")) {
                if (pl.getProperty("reftypeid").length() != 0 || pl.getPropertyList("lookuplink").getProperty("sdcid").length() != 0) return pl;
                pl.getPropertyList("lookuplink").setProperty("sdcid", "");
                pl.setProperty("reftypeid", reftypeid);
                pl.setProperty("sdcid", "");
                pl.setProperty("sql", "");
                return pl;
            } else {
                if (pl.getProperty("reftypeid").length() != 0 || pl.getProperty("sdcid").length() != 0) return pl;
                pl.setProperty("reftypeid", reftypeid);
                pl.setProperty("sdcid", "");
                pl.setProperty("sql", "");
            }
        }
        return pl;
    }

    public void setEditorStyleId(String editorstyleid, String sdcid, String reftypeid, PropertyList map) throws SapphireException {
        PropertyList pl = EditorStyleField.getEditorStyleProperties(editorstyleid, sdcid, reftypeid, this.getConnectionProcessor().getSapphireConnection());
        String sql = pl.getProperty("sql", "");
        if (sql.length() > 0 && sql.indexOf("[") > -1 && sql.indexOf("]") >= 0) {
            String[] tokens = StringUtil.getTokens(sql);
            for (int i = 0; i < tokens.length; ++i) {
                if (map.getProperty(tokens[i]) == null) continue;
                sql = StringUtil.replaceAll(sql, "[" + tokens[i] + "]", map.getProperty(tokens[i]));
            }
            pl.setProperty("sql", sql);
        }
        this.setEditorStyleProperties(editorstyleid, pl);
    }

    public void setEditorStyleId(String editorstyleid, String sdcid, String reftypeid) throws SapphireException {
        PropertyList pl = EditorStyleField.getEditorStyleProperties(editorstyleid, sdcid, reftypeid, this.getConnectionProcessor().getSapphireConnection());
        this.setEditorStyleProperties(editorstyleid, pl);
    }

    @Override
    public void setColumn(PropertyList column) {
        if (this.editorstyleid.length() > 0) {
            super.getColumn().putAll(column);
        } else {
            super.setColumn(column);
        }
    }

    public void setChangeEvent(String change) {
        this.customChange = change;
    }
}

