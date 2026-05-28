/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.adhocbrowser;

import com.labvantage.sapphire.modules.adhocbrowser.AdhocMetaData;
import com.labvantage.sapphire.modules.adhocbrowser.DetailTreeRenderer;
import com.labvantage.sapphire.modules.adhocbrowser.TreeNode;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class AttributeTreeRenderer
extends DetailTreeRenderer {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";
    private String filtertext;
    private PropertyList attributePL = null;

    public AttributeTreeRenderer(String sdcid, String detailname, PropertyList pagedata, AdhocMetaData adhocmetadata, QueryProcessor queryProcessor, SDCProcessor sdcProcessor, TranslationProcessor tp) {
        super(sdcid, detailname, pagedata, adhocmetadata, queryProcessor, sdcProcessor, tp);
        PropertyListCollection searchablesdcs = null;
        if (pagedata != null) {
            searchablesdcs = pagedata.getCollection("searchablesdcs");
        }
        if (searchablesdcs != null) {
            for (int i = 0; i < searchablesdcs.size(); ++i) {
                if (!sdcid.equals(searchablesdcs.getPropertyList(i).getProperty("sdcid"))) continue;
                this.attributePL = searchablesdcs.getPropertyList(i).getPropertyList("attributesearch");
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
        if (this.linkcolumnid.indexOf("attributegroup[") >= 0) {
            String attributegroupid = StringUtil.getTokens(this.linkcolumnid)[0];
            nodes = this.getAttributeNodesInGroup(this.sdcid, attributegroupid);
        } else if ("attributeroot".equals(this.linkcolumnid) || this.linkcolumnid.indexOf(".attributeroot") > 0) {
            Object[] objectArray;
            String attributeSdcid = this.sdcid;
            if (this.linkcolumnid.indexOf(".attributeroot") > 0) {
                try {
                    String reftableid = AdhocMetaData.getReferenceEntityName(this.sdcProcessor.getConnectionid(), this.adhocmetadata.getTableid(this.sdcid), this.linkcolumnid.substring(0, this.linkcolumnid.lastIndexOf(".")));
                    attributeSdcid = this.adhocmetadata.getSdcId(reftableid);
                }
                catch (Exception reftableid) {
                    // empty catch block
                }
            }
            if ("LV_Worksheet".equals(attributeSdcid)) {
                attributeSdcid = "LV_Worksheet','LV_WorksheetSection','LV_WorksheetItem";
            }
            String sql = "SELECT distinct d.attributetitle, d.editsdcid, d.editreftypeid, d.editorstyleid, d.datatype, a.attributeid  FROM sdiattribute a left outer join editorstyle ed on a.editorstyleid=ed.editorstyleid, attributedef d WHERE a.attributeid=d.attributedefid AND a.sdcid in ('" + attributeSdcid + "') AND a.datatype!='C'" + (this.filtertext == null || this.filtertext.length() == 0 ? " AND ( d.attributegroup is null OR d.attributegroup='' )" : "") + (this.filtertext != null && this.filtertext.length() > 0 ? " AND ( lower( d.attributetitle ) like ? or lower( d.attributedefid ) like ?)" : "") + " ORDER BY 1";
            String likeparam = "%" + this.filtertext.toLowerCase() + "%";
            if (this.filtertext == null || this.filtertext.length() == 0) {
                objectArray = new Object[]{};
            } else {
                Object[] objectArray2 = new Object[2];
                objectArray2[0] = likeparam;
                objectArray = objectArray2;
                objectArray2[1] = likeparam;
            }
            DataSet ds = this.queryProcessor.getPreparedSqlDataSet(sql, objectArray, true);
            nodes = this.getAttributeNodes(ds);
            ArrayList groupNodes = this.getAttributeGroupNodes();
            if (nodes != null) {
                nodes.addAll(groupNodes);
            } else {
                nodes = groupNodes;
            }
        }
        return nodes;
    }

    private ArrayList getAttributeNodesInGroup(String attributeSdcid, String attributegroup) {
        String sql = "SELECT distinct d.attributetitle, d.editsdcid, d.editreftypeid, d.editorstyleid, d.datatype, a.attributeid  FROM sdiattribute a left outer join editorstyle ed on a.editorstyleid=ed.editorstyleid, attributedef d WHERE a.attributeid=d.attributedefid AND a.sdcid=? AND a.datatype!='C' AND d.attributegroup=? ORDER BY 1";
        DataSet ds = this.queryProcessor.getPreparedSqlDataSet(sql, new Object[]{attributeSdcid, attributegroup});
        return this.getAttributeNodes(ds);
    }

    private ArrayList getAttributeGroupNodes() {
        int i;
        ArrayList<TreeNode> nodes = new ArrayList<TreeNode>();
        ArrayList ds = null;
        ArrayList groups = null;
        if (this.attributePL != null) {
            groups = this.attributePL.getCollection("attributegroups");
        }
        if (groups != null && groups.size() > 0) {
            ds = new DataSet();
            ((DataSet)ds).addColumn("attributegroup", 0);
            ((DataSet)ds).addColumn("title", 0);
            for (i = 0; i < groups.size(); ++i) {
                String group = ((PropertyListCollection)groups).getPropertyList(i).getProperty("attributegroup");
                if (group.length() <= 0) continue;
                int row = ((DataSet)ds).addRow();
                ((DataSet)ds).setValue(row, "attributegroup", group);
                ((DataSet)ds).setValue(row, "title", ((PropertyListCollection)groups).getPropertyList(i).getProperty("title"));
            }
        }
        if (ds == null || ds.size() == 0) {
            String sql = "select distinct attributegroup, attributegroup title from attributedef where basedonid=? order by 1";
            ds = this.queryProcessor.getPreparedSqlDataSet(sql, new Object[]{this.sdcid});
        }
        for (i = 0; i < ((DataSet)ds).getRowCount(); ++i) {
            String attributegroup = ((DataSet)ds).getValue(i, "attributegroup");
            if (attributegroup.length() <= 0) continue;
            String nodeid = this.linkcolumnid + ".attributegroup[" + attributegroup + "]";
            String nodeimage = "WEB-CORE/imageref/flat/16/flat_black_folder2_closed.svg";
            TreeNode node = new TreeNode(nodeid, attributegroup, nodeimage, true);
            node.setNodelabel(((DataSet)ds).getValue(i, "title").length() == 0 ? attributegroup : ((DataSet)ds).getValue(i, "title"));
            node.setDragable(false);
            nodes.add(node);
        }
        return nodes;
    }

    private ArrayList getAttributeNodes(DataSet ds) {
        int i;
        ArrayList<TreeNode> nodes = new ArrayList<TreeNode>();
        String prefix = this.linkcolumnid;
        if (prefix.indexOf(".attributegroup[") > 0) {
            prefix = this.linkcolumnid.substring(0, this.linkcolumnid.indexOf(".attributegroup["));
        }
        if (prefix.equals("attributeroot")) {
            prefix = "";
        } else if (prefix.indexOf("attributeroot.") == 0) {
            prefix = prefix.substring(14);
        } else if (prefix.indexOf(".attributeroot") > 0) {
            prefix = prefix.substring(0, prefix.indexOf("attributeroot"));
        }
        String editorstyleids = ds.getColumnValues("editorstyleid", "','");
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT editorstyleid, editordefinition FROM editorstyle WHERE editorstyleid IN (" + safeSQL.addIn(editorstyleids) + ")";
        DataSet editorstyleds = this.queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues(), true);
        HashMap<String, String> editorStyleModeMap = new HashMap<String, String>();
        for (i = 0; i < editorstyleds.getRowCount(); ++i) {
            PropertyList editorpl = new PropertyList("fieldpl");
            try {
                String editorstyleid = editorstyleds.getValue(i, "editorstyleid");
                editorpl.setPropertyList(editorstyleds.getValue(i, "editordefinition"));
                String mode = editorpl.getProperty("mode");
                editorStyleModeMap.put(editorstyleid, mode);
                continue;
            }
            catch (Exception e) {
                throw new RuntimeException("Editor definition Error.");
            }
        }
        for (i = 0; i < ds.getRowCount(); ++i) {
            String attributeid = ds.getValue(i, "attributeid");
            String attributetitle = ds.getValue(i, "attributetitle");
            String datatype = ds.getValue(i, "datatype");
            String editsdcid = ds.getValue(i, "editsdcid");
            String editreftypeid = ds.getValue(i, "editreftypeid");
            String mode = (String)editorStyleModeMap.get(ds.getValue(i, "editorstyleid"));
            String nodeid = prefix + "sdiattribute[" + attributeid + "]";
            String nodelabel = attributetitle.length() == 0 ? attributeid : attributetitle;
            String nodeimage = "";
            nodeimage = "dropdownlist".equals(mode) || "dropdowncombo".equals(mode) || editreftypeid.length() > 0 ? "WEB-CORE/imageref/flat/16/flat_black_sort_down_dropdown.svg" : ("datelookup".equals(mode) || "D".equals(datatype) || "O".equals(datatype) ? "WEB-CORE/imageref/flat/16/flat_black_calendar2.svg" : ("lookup".equals(mode) || editsdcid.length() > 0 ? "WEB-CORE/imageref/flat/16/flat_black_external_lookup1.svg" : ("N".equals(datatype) ? "WEB-CORE/imageref/flat/16/flat_black_type_bit.svg" : "WEB-CORE/imageref/flat/16/flat_black_page.svg")));
            TreeNode node = new TreeNode(nodeid, nodelabel, nodeimage, false);
            nodes.add(node);
            node.setColumntitle(nodelabel);
        }
        return nodes;
    }
}

