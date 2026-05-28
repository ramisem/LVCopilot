/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.console.diagnostics.storageunit;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.console.diagnostics.BaseDiagnostic;
import com.labvantage.sapphire.console.diagnostics.DiagnosticException;
import com.labvantage.sapphire.xml.PropertyTree;
import java.util.HashMap;
import java.util.Map;
import sapphire.SapphireException;
import sapphire.util.ConnectionInfo;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class UpdateStorageUnitRowColumnData
extends BaseDiagnostic {
    private Map<String, PropertyList> defMap = new HashMap<String, PropertyList>();

    public UpdateStorageUnitRowColumnData(DBAccess database, ConnectionInfo connectionInfo) {
        super(database, connectionInfo);
    }

    public UpdateStorageUnitRowColumnData(String webappid, ConnectionInfo connectionInfo) {
        super(webappid, connectionInfo);
    }

    @Override
    public String getTitle() {
        return "Populate values in NUMROWS, NUMCOL, LABELROW and LABELCOL values in existing storage units.";
    }

    @Override
    public String getDescription() {
        return "This diagnostics populates the values in columns NUMROWS, NUMCOL, LABELROW and LABELCOL in storageunit that have been introduced in LV 8.3.0";
    }

    @Override
    public String runDiagnostic(PropertyList properties) throws DiagnosticException {
        return "";
    }

    @Override
    public String runRepair(PropertyList properties) throws DiagnosticException {
        try {
            DataSet update = new DataSet();
            this.database.createPreparedResultSet("select storageunitid, storageunittype, propertytreeid from storageunit where propertytreeid = 'Grid' and numrows is null", new String[0]);
            DataSet ds = new DataSet();
            ds.setResultSet(this.database.getResultSet());
            this.database.closeResultSet();
            for (int i = 0; i < ds.size(); ++i) {
                String storageunitid = ds.getString(i, "storageunitid");
                String storageunittype = ds.getString(i, "storageunittype");
                String propertytreeid = ds.getString(i, "propertytreeid");
                if (!OpalUtil.isNotEmpty(storageunittype) || !OpalUtil.isNotEmpty(propertytreeid)) continue;
                PropertyList propertyList = this.getStorageUnitTypeDefinition(storageunittype, propertytreeid);
                int row = update.addRow();
                update.setString(row, "storageunitid", storageunitid);
                update.setNumber(row, "numrows", propertyList.getProperty("rows"));
                update.setNumber(row, "numcol", propertyList.getProperty("columns"));
            }
            if (update.size() > 0) {
                DataSetUtil.update(this.database, update, "storageunit", new String[]{"storageunitid"});
            }
        }
        catch (SapphireException e) {
            e.printStackTrace();
        }
        String sql = "select csu.storageunitid, csu.parentid, csu.storageunitlabel, psu.storageunittype parentstorageunittype, psu.propertytreeid parentpropertytreeid from storageunit csu, storageunit psu where csu.propertytreeid = 'No Layout' and csu.labelrow is null and csu.labelcol is null and csu.linksdcid is null and psu.propertytreeid in ( 'Grid', 'Linear' ) and psu.storageunitid = csu.parentid";
        try {
            DataSet update = new DataSet();
            this.database.createResultSet("labelrow", sql);
            DataSet ds = new DataSet();
            ds.setResultSet(this.database.getResultSet("labelrow"));
            this.database.closeResultSet("labelrow");
            for (int i = 0; i < ds.size(); ++i) {
                String storageunitlabel = ds.getString(i, "storageunitlabel");
                String parentstorageunittype = ds.getString(i, "parentstorageunittype");
                String parentpropertytreeid = ds.getString(i, "parentpropertytreeid");
                String labelrow = "";
                String labelcol = "";
                if (OpalUtil.isNotEmpty(storageunitlabel)) {
                    if ("Linear".equals(parentpropertytreeid)) {
                        if ("Vertical".equals(this.getStorageUnitTypeDefinition(parentstorageunittype, parentpropertytreeid).getProperty("orientation"))) {
                            labelrow = storageunitlabel;
                        } else {
                            labelcol = storageunitlabel;
                        }
                    } else if ("Grid".equals(parentpropertytreeid)) {
                        PropertyList parentStorageUnitTypeDefinition = this.getStorageUnitTypeDefinition(parentstorageunittype, parentpropertytreeid);
                        String[] labels = UpdateStorageUnitRowColumnData.splitAlphaNumeric(storageunitlabel);
                        if (labels.length == 2) {
                            String orientation = parentStorageUnitTypeDefinition.getProperty("orientation");
                            if ("Row Major".equals(orientation)) {
                                labelrow = String.valueOf(labels[0]);
                                labelcol = String.valueOf(labels[1]);
                            } else if ("Column Major".equals(orientation)) {
                                labelrow = String.valueOf(labels[1]);
                                labelcol = String.valueOf(labels[0]);
                            }
                        }
                    }
                }
                if (!OpalUtil.isNotEmpty(labelrow) || !OpalUtil.isNotEmpty(labelcol)) continue;
                int row = update.addRow();
                update.setString(row, "storageunitid", ds.getString(i, "storageunitid"));
                update.setString(row, "labelrow", labelrow);
                update.setString(row, "labelcol", labelcol);
            }
            if (update.size() > 0) {
                DataSetUtil.update(this.database, update, "storageunit", new String[]{"storageunitid"});
            }
        }
        catch (SapphireException e) {
            e.printStackTrace();
        }
        return "Updated labelrow and labelcol values in storageunit successfully";
    }

    private static String[] splitAlphaNumeric(String str) {
        return str.split("((?<=[a-zA-Z])(?=[0-9]))|((?<=[0-9])(?=[a-zA-Z]))");
    }

    private PropertyList getStorageUnitTypeDefinition(String storageunittype, String propertytreeid) {
        String key = storageunittype + propertytreeid;
        if (!this.defMap.containsKey(key)) {
            PropertyList propertyList = null;
            try {
                String valuetree = "";
                String defTree = "";
                this.database.createPreparedResultSet("select valuetree, definitiontree from PROPERTYTREE where PROPERTYTREETYPE = 'StorageUnitType' and propertytreeid = ?", new String[]{propertytreeid});
                if (this.database.getNext()) {
                    valuetree = this.database.getClob("valuetree");
                    defTree = this.database.getClob("definitiontree");
                }
                this.database.closeResultSet();
                if (valuetree != null && valuetree.length() > 0) {
                    PropertyTree propertyTree = new PropertyTree();
                    propertyTree.setValueXML(valuetree);
                    propertyTree.setDefinitionXML(defTree);
                    propertyList = propertyTree.getNodePropertyList(storageunittype, true);
                }
            }
            catch (SapphireException e) {
                e.printStackTrace();
            }
            this.defMap.put(key, propertyList == null ? new PropertyList() : propertyList);
        }
        return this.defMap.get(key);
    }

    @Override
    public boolean canBeRepaired() {
        return true;
    }

    @Override
    public boolean canAutoRepair() {
        return true;
    }
}

