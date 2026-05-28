/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.adhocbrowser;

import com.labvantage.sapphire.modules.adhocbrowser.AdhocMetaData;
import com.labvantage.sapphire.modules.adhocbrowser.DetailTreeRenderer;
import com.labvantage.sapphire.modules.adhocbrowser.TreeNode;
import java.util.ArrayList;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class WorksheetItemFieldTreeRenderer
extends DetailTreeRenderer {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";
    private String filtertext;
    PropertyList docfieldPL;

    public WorksheetItemFieldTreeRenderer(String sdcid, String detailname, PropertyList pagedata, AdhocMetaData adhocmetadata, QueryProcessor queryProcessor, SDCProcessor sdcProcessor, TranslationProcessor tp) {
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
        ArrayList nodes = null;
        boolean isOracle = new ConnectionProcessor(this.queryProcessor.getConnectionid()).getConnectionInfo(this.queryProcessor.getConnectionid()).isOracle();
        String fieldSelectClause = "select distinct worksheetitemfield.fieldname, worksheetitemfield.fieldtitle, worksheetitemfield.datatype, " + (isOracle ? "dbms_lob.substr( worksheetitemfield.fielddef, 3950, 1 )" : "substring( worksheetitemfield.fielddef, 1, 3950 )") + " fielddef ";
        String fieldOrderByClause = " order by worksheetitemfield.fieldtitle";
        String likevalue = "%" + this.filtertext.toLowerCase() + "%";
        String filterclause = " where lower( worksheetitemfield.fieldname ) like ? or lower( worksheetitemfield.fieldtitle ) like ?";
        if (("worksheetitemfieldroot".equals(this.linkcolumnid) || this.linkcolumnid.indexOf(".worksheetitemfieldroot") > 0) && filterclause.length() > 0) {
            String sql = fieldSelectClause + " from worksheetitemfield " + filterclause + fieldOrderByClause;
            DataSet ds = this.queryProcessor.getPreparedSqlDataSet(sql, new Object[]{likevalue, likevalue}, true);
            nodes = this.getFieldNodes(ds);
        }
        return nodes;
    }

    private ArrayList getFieldNodes(DataSet ds) {
        ArrayList<TreeNode> nodes = new ArrayList<TreeNode>();
        String prefix = this.linkcolumnid;
        if (prefix.equals("worksheetitemfieldroot")) {
            prefix = "";
        } else if (prefix.indexOf("worksheetitemfieldroot.") == 0) {
            prefix = prefix.substring(23);
        } else if (prefix.indexOf(".worksheetitemfieldroot") > 0) {
            prefix = prefix.substring(0, prefix.indexOf("worksheetitemfieldroot"));
        }
        for (int i = 0; i < ds.getRowCount(); ++i) {
            String nodelabel;
            String fieldid = ds.getString(i, "fieldname");
            String fielddesc = ds.getString(i, "fieldtitle");
            String fieldlabel = ds.getString(i, "fieldtitle");
            String fieldtype = ds.getString(i, "datatype");
            String fielddef = ds.getValue(i, "fielddef");
            if (fielddef.length() > 0) {
                PropertyList fieldPL = new PropertyList("fieldPL");
                try {
                    fieldPL.setPropertyList(fielddef);
                    fieldtype = fieldPL.getProperty("type");
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            String nodeid = prefix + "worksheetitemfield[" + fieldid + "]";
            String string = fielddesc != null && fielddesc.length() > 0 ? fielddesc : (nodelabel = fieldlabel != null && fieldlabel.length() > 0 ? fieldlabel : fieldid);
            String datatype = "date".equals(fieldtype) ? "D" : ("dateonly".equals(fieldtype) ? "O" : ("number".equals(fieldtype) ? "N" : ("lookup".equals(fieldtype) ? "S" : ("dropdown".equals(fieldtype) || "radiobutton".equals(fieldtype) || "checkbox".equals(fieldtype) ? "V" : ("string".equals(fieldtype) ? "T" : "T")))));
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

