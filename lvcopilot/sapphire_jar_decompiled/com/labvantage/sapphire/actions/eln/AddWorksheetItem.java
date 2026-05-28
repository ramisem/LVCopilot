/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.eln;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.actions.eln.AddWorksheetItemRef;
import com.labvantage.sapphire.actions.eln.BaseELNAction;
import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItem;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemFactory;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.xml.PropertyTree;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class AddWorksheetItem
extends BaseELNAction {
    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        PropertyList wsOptions;
        String worksheetsectionversionid;
        String worksheetid = properties.getProperty("worksheetid");
        String worksheetversionid = properties.getProperty("worksheetversionid");
        String worksheetsectionid = properties.getProperty("worksheetsectionid");
        if (!this.sectionInProgress(this.database, worksheetsectionid, worksheetsectionversionid = properties.getProperty("worksheetsectionversionid"), wsOptions = new PropertyList(), null, properties.getProperty("bypassstatuscheck").equals("Y"))) return;
        String propertytreeid = properties.getProperty("propertytreeid");
        String sourcenodeid = properties.getProperty("sourcenodeid", "Sapphire Custom");
        String templateid = properties.getProperty("templateid");
        String templateversionid = properties.getProperty("templateversionid");
        String itemtemplateid = properties.getProperty("itemtemplateid");
        String itemtemplateversionid = properties.getProperty("itemtemplateversionid");
        this.database.createPreparedResultSet("loaditems", "SELECT worksheetitemid, worksheetitemversionid, usersequence FROM worksheetitem WHERE worksheetsectionid=? AND worksheetsectionversionid=? ORDER BY usersequence", new String[]{worksheetsectionid, worksheetsectionversionid});
        DataSet allItems = new DataSet(this.database.getResultSet("loaditems"));
        this.database.closeResultSet("loaditems");
        int itemrow = -1;
        try {
            itemrow = Integer.parseInt(properties.getProperty("usersequence", "-1"));
        }
        catch (Exception exception) {
            // empty catch block
        }
        int newRow = itemrow >= 0 ? allItems.addRow(itemrow) : allItems.addRow();
        allItems.setSequence("newsequence");
        int userSequence = allItems.getInt(newRow, "newsequence");
        for (int i = 0; i < allItems.size(); ++i) {
            int newseq;
            int seq = allItems.getInt(i, "usersequence", i);
            if (seq == (newseq = allItems.getInt(i, "newsequence", i)) || i == newRow) continue;
            this.database.executePreparedUpdate("UPDATE worksheetitem SET usersequence = ? WHERE worksheetitemid = ? AND worksheetitemversionid = ?", new Object[]{newseq, allItems.getString(i, "worksheetitemid"), allItems.getString(i, "worksheetitemversionid")});
            properties.setProperty("usersequenceupdate", "Y");
        }
        PropertyList config = null;
        if (propertytreeid.length() > 0) {
            WebAdminProcessor webAdminProcessor = new WebAdminProcessor(this.getConnectionId());
            try {
                PropertyTree propertyTree = webAdminProcessor.getPropertyTree(propertytreeid);
                config = propertyTree.getNodePropertyList(sourcenodeid, true);
            }
            catch (Exception e) {
                throw new SapphireException("Failed to get propertytree for control '" + propertytreeid + "'. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
            }
        }
        WorksheetItem worksheetItem = null;
        boolean addRef = false;
        PropertyList addProps = new PropertyList();
        addProps.setProperty("sdcid", "LV_WorksheetItem");
        addProps.setProperty("worksheetitemversionid", "1");
        if (wsOptions.getProperty("keygenrule").equals(TEMPLATE_KEYGENRULE)) {
            addProps.setProperty("worksheetitemid", this.getTemplateDetailKey(worksheetid));
            addProps.setProperty("overrideautokey", "Y");
        }
        addProps.setProperty("worksheetid", worksheetid);
        addProps.setProperty("worksheetversionid", worksheetversionid);
        addProps.setProperty("worksheetsectionid", worksheetsectionid);
        addProps.setProperty("worksheetsectionversionid", worksheetsectionversionid);
        addProps.setProperty("itemstatus", properties.getProperty("itemstatus", "InProgress"));
        addProps.setProperty("availabilityflag", "Y");
        addProps.setProperty("usersequence", String.valueOf(userSequence));
        if (propertytreeid.length() > 0) {
            addProps.setProperty("propertytreeid", propertytreeid);
            addProps.setProperty("sourcenodeid", sourcenodeid);
            addProps.setProperty("config", config != null ? config.toXMLString() : new PropertyList().toXMLString());
            PropertyList itemDefaults = wsOptions.getPropertyList("itemdefaults");
            addProps.setProperty("options", itemDefaults != null ? new PropertyList(itemDefaults).toXMLString() : "");
            addProps.setProperty("contents", properties.getProperty("contents"));
            HashMap data = new HashMap();
            data.putAll(addProps);
            worksheetItem = WorksheetItemFactory.getInstance(new SapphireConnection(this.database.getConnection(), this.connectionInfo), (DBUtil)this.database, data);
            worksheetItem.validateAdd(addProps);
        } else if (templateid.length() > 0) {
            if (properties.getProperty("fromworksheetid").length() > 0) {
                templateversionid = AddWorksheetItem.resolveVersion(this.getQueryProcessor(), templateid, templateversionid, "worksheet");
                this.database.createPreparedResultSet("SELECT worksheetitemid, worksheetitemversionid FROM worksheetitem, worksheet WHERE worksheetitem.worksheetid = worksheet.worksheetid AND worksheetitem.worksheetversionid = worksheet.worksheetversionid AND worksheet.worksheetid = ? AND worksheet.worksheetversionid = ?", new Object[]{templateid, templateversionid});
                if (!this.database.getNext()) throw new SapphireException("Failed to find templateid " + AddWorksheetItem.getIdVersionText(templateid, templateversionid));
                addProps.setProperty("templatekeyid1", this.database.getValue("worksheetitemid"));
                addProps.setProperty("templatekeyid2", this.database.getValue("worksheetitemversionid"));
                addProps.setProperty("templateid", templateid);
                addProps.setProperty("templateversionid", templateversionid);
                addProps.setProperty("addfromtemplate", "Y");
                addProps.setProperty("copyattachment", "Y");
            } else {
                addProps.setProperty("templatekeyid1", templateid);
                addProps.setProperty("templatekeyid2", templateversionid);
            }
        } else {
            if (itemtemplateid.length() <= 0) throw new SapphireException("Missing properties - must supply a propertytreeid, templateid or itemtemplateid");
            this.database.createPreparedResultSet("SELECT worksheetitemid, worksheetitemversionid FROM worksheetitem WHERE worksheetitemid = ? AND worksheetitemversionid = ?", new Object[]{itemtemplateid, itemtemplateversionid});
            if (!this.database.getNext()) throw new SapphireException("Failed to find item templateid " + AddWorksheetItem.getIdVersionText(itemtemplateid, itemtemplateversionid));
            addProps.setProperty("templatekeyid1", itemtemplateid);
            addProps.setProperty("templatekeyid2", itemtemplateversionid);
            addProps.setProperty("addfromtemplate", "Y");
            addProps.setProperty("copyattachment", "Y");
            addRef = true;
        }
        addProps.setProperty("worksheet_action", "Y");
        this.getActionProcessor().processAction("AddSDI", "1", addProps);
        properties.setProperty("worksheetitemid", addProps.getProperty("newkeyid1"));
        properties.setProperty("worksheetitemversionid", addProps.getProperty("newkeyid2"));
        if (worksheetItem != null) {
            worksheetItem.setWorksheetItemId(addProps.getProperty("newkeyid1"));
            worksheetItem.setWorksheetItemVersionId(addProps.getProperty("newkeyid2"));
            worksheetItem.postAdd();
        }
        if (addRef) {
            PropertyList refProps = new PropertyList();
            refProps.setProperty("worksheetid", worksheetid);
            refProps.setProperty("worksheetversionid", worksheetversionid);
            refProps.setProperty("worksheetitemid", addProps.getProperty("newkeyid1"));
            refProps.setProperty("worksheetitemversionid", addProps.getProperty("newkeyid2"));
            refProps.setProperty("refworksheetid", properties.getProperty("fromworksheetid"));
            refProps.setProperty("refworksheetversionid", properties.getProperty("fromworksheetversionid"));
            refProps.setProperty("refsdcid", "LV_WorksheetItem");
            refProps.setProperty("refkeyid1", itemtemplateid);
            refProps.setProperty("refkeyid2", itemtemplateversionid);
            refProps.setProperty("reffunction", "copy");
            this.getActionProcessor().processActionClass(AddWorksheetItemRef.class.getName(), refProps);
        }
        this.addActivityLog(worksheetid, worksheetversionid, "Add", "LV_WorksheetItem", properties.getProperty("worksheetitemid"), properties.getProperty("worksheetitemversionid"), propertytreeid.length() > 0 ? "Added new " + propertytreeid + " worksheet item" : (templateid.length() > 0 ? "Added new worksheetitem from template " + AddWorksheetItem.getIdVersionText(templateid, templateversionid) : "Added new worksheetitem copying worksheetitem " + AddWorksheetItem.getIdVersionText(itemtemplateid, itemtemplateversionid)));
    }
}

