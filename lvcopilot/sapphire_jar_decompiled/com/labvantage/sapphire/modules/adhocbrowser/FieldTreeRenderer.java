/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.adhocbrowser;

import com.labvantage.sapphire.modules.adhocbrowser.AdhocMetaData;
import com.labvantage.sapphire.modules.adhocbrowser.DetailTreeRenderer;
import com.labvantage.sapphire.modules.adhocbrowser.TreeNode;
import java.util.ArrayList;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class FieldTreeRenderer
extends DetailTreeRenderer {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";
    private String filtertext;
    private String fieldSelectClause = "select field.fieldid, field.fielddesc, field.fieldlabel, field.fieldtype, field.defaulteditor ";
    private String fieldOrderByClause = " order by field.fieldlabel";
    PropertyList docfieldPL;

    public FieldTreeRenderer(String sdcid, String detailname, PropertyList pagedata, AdhocMetaData adhocmetadata, QueryProcessor queryProcessor, SDCProcessor sdcProcessor, TranslationProcessor tp) {
        super(sdcid, detailname, pagedata, adhocmetadata, queryProcessor, sdcProcessor, tp);
        PropertyListCollection searchablesdcs = null;
        if (pagedata != null) {
            searchablesdcs = pagedata.getCollection("searchablesdcs");
        }
        if (searchablesdcs != null) {
            for (int i = 0; i < searchablesdcs.size(); ++i) {
                if (!sdcid.equals(searchablesdcs.getPropertyList(i).getProperty("sdcid"))) continue;
                this.docfieldPL = searchablesdcs.getPropertyList(i).getPropertyList("docfieldsearch");
                break;
            }
        }
    }

    public void init(String filtertext) {
        this.filtertext = filtertext;
    }

    @Override
    protected ArrayList getChildNodes() {
        String filterclause;
        ArrayList nodes = null;
        String string = filterclause = this.filtertext != null && this.filtertext.length() > 0 ? " where lower( field.fieldid ) like '%" + this.filtertext.toLowerCase().replaceAll("'", "''") + "%' or lower( field.fieldlabel ) like '%" + this.filtertext.toLowerCase().replaceAll("'", "''") + "%'" : "";
        if (this.docfieldPL != null) {
            ArrayList cats = null;
            if (this.docfieldPL != null) {
                cats = this.docfieldPL.getCollection("docfieldcategories");
            }
            if (cats == null || cats.size() > 0) {
                // empty if block
            }
        }
        if (this.linkcolumnid.indexOf("fieldcategory[") >= 0) {
            String categoryid = StringUtil.getTokens(this.linkcolumnid)[0];
            nodes = this.getFieldsInCategoryNodes(categoryid);
        } else if ("fieldroot".equals(this.linkcolumnid) || this.linkcolumnid.indexOf(".fieldroot") > 0) {
            if (filterclause.length() > 0) {
                DataSet ds = this.queryProcessor.getSqlDataSet(this.fieldSelectClause + " from field " + filterclause + this.fieldOrderByClause);
                nodes = this.getFieldNodes(ds);
            }
            ArrayList categoryNodes = this.getCategoryNodes();
            if (nodes != null) {
                nodes.addAll(categoryNodes);
            } else {
                nodes = categoryNodes;
            }
        }
        return nodes;
    }

    private ArrayList getFieldsInCategoryNodes(String categoryid) {
        DataSet ds = this.queryProcessor.getSqlDataSet(this.fieldSelectClause + "from field, categoryitem where field.fieldid=categoryitem.keyid1 and sdcid='LV_Field' and categoryitem.categoryid='" + categoryid.replaceAll("'", "''") + "'" + this.fieldOrderByClause);
        return this.getFieldNodes(ds);
    }

    private ArrayList getCategoryNodes() {
        int i;
        ArrayList<TreeNode> nodes = new ArrayList<TreeNode>();
        ArrayList ds = null;
        ArrayList cats = null;
        if (this.docfieldPL != null) {
            cats = this.docfieldPL.getCollection("docfieldcategories");
        }
        if (cats != null && cats.size() > 0) {
            ds = new DataSet();
            ((DataSet)ds).addColumn("categoryid", 0);
            ((DataSet)ds).addColumn("categorydesc", 0);
            for (i = 0; i < cats.size(); ++i) {
                String catid = ((PropertyListCollection)cats).getPropertyList(i).getProperty("categoryid");
                if (catid.length() <= 0) continue;
                int row = ((DataSet)ds).addRow();
                ((DataSet)ds).setValue(row, "categoryid", catid);
                ((DataSet)ds).setValue(row, "categorydesc", ((PropertyListCollection)cats).getPropertyList(i).getProperty("title"));
            }
        }
        if (ds == null || ds.size() == 0) {
            String sql = "select categoryid, categorydesc from category where sdcid='LV_Field'";
            ds = this.queryProcessor.getSqlDataSet(sql);
        }
        for (i = 0; i < ((DataSet)ds).getRowCount(); ++i) {
            String categoryid = ((DataSet)ds).getString(i, "categoryid");
            String nodeid = this.linkcolumnid + ".fieldcategory[" + categoryid + "]";
            String nodeimage = "WEB-CORE/imageref/flat/16/flat_black_folder2_closed.svg";
            TreeNode node = new TreeNode(nodeid, categoryid, nodeimage, true);
            node.setNodelabel(((DataSet)ds).getValue(i, "categorydesc").length() == 0 ? categoryid : ((DataSet)ds).getValue(i, "categorydesc"));
            node.setDragable(false);
            nodes.add(node);
        }
        return nodes;
    }

    private ArrayList getFieldNodes(DataSet ds) {
        ArrayList<TreeNode> nodes = new ArrayList<TreeNode>();
        String prefix = this.linkcolumnid;
        if (prefix.indexOf(".fieldcategory[") > 0) {
            prefix = this.linkcolumnid.substring(0, this.linkcolumnid.indexOf(".fieldcategory["));
        }
        if (prefix.equals("fieldroot")) {
            prefix = "";
        } else if (prefix.indexOf("fieldroot.") == 0) {
            prefix = prefix.substring(10);
        } else if (prefix.indexOf(".fieldroot") > 0) {
            prefix = prefix.substring(0, prefix.indexOf("fieldroot"));
        }
        for (int i = 0; i < ds.getRowCount(); ++i) {
            String nodelabel;
            String fieldid = ds.getString(i, "fieldid");
            String fielddesc = ds.getString(i, "fielddesc");
            String fieldlabel = ds.getString(i, "fieldlabel");
            String fieldtype = ds.getString(i, "fieldtype");
            String defaulteditor = ds.getString(i, "defaulteditor");
            String nodeid = prefix + "documentfield[" + fieldid + "]";
            String string = fielddesc != null && fielddesc.length() > 0 ? fielddesc : (nodelabel = fieldlabel != null && fieldlabel.length() > 0 ? fieldlabel : fieldid);
            String datatype = "date".equals(fieldtype) ? "D" : ("dateonly".equals(fieldtype) ? "O" : ("number".equals(fieldtype) ? "N" : ("lookup".equals(defaulteditor) ? "S" : ("dropdown".equals(defaulteditor) || "radiobutton".equals(defaulteditor) || "checkbox".equals(defaulteditor) ? "V" : ("string".equals(fieldtype) ? "T" : "T")))));
            String nodeimage = "WEB-CORE/imageref/flat/16/flat_black_type_bit.svg";
            if ("V".equals(datatype) || "R".equals(datatype)) {
                nodeimage = "WEB-CORE/imageref/flat/16/flat_black_sort_down_dropdown.svg";
            } else if ("D".equals(datatype) || "O".equals(datatype)) {
                nodeimage = "WEB-CORE/imageref/flat/16/flat_black_calendar2.svg";
            } else if ("S".equals(datatype)) {
                nodeimage = "WEB-CORE/imageref/flat/16/flat_black_external_lookup1.svg";
            } else if ("T".equals(datatype) || "A".equals(datatype)) {
                nodeimage = "WEB-CORE/imageref/flat/16/flat_black_page.svg";
            }
            TreeNode node = new TreeNode(nodeid, nodelabel, nodeimage, false);
            nodes.add(node);
            node.setColumntitle(nodelabel);
        }
        return nodes;
    }
}

