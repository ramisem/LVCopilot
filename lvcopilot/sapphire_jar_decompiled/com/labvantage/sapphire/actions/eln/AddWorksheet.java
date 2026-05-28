/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.eln;

import com.labvantage.sapphire.actions.eln.AddWorksheetItem;
import com.labvantage.sapphire.actions.eln.BaseELNAction;
import com.labvantage.sapphire.platform.Configuration;
import sapphire.SapphireException;
import sapphire.util.ActionBlock;
import sapphire.xml.PropertyList;

public class AddWorksheet
extends BaseELNAction {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String templateid = properties.getProperty("templateid");
        if (templateid.length() == 0) {
            ActionBlock ab = new ActionBlock();
            String templatetypeflag = properties.getProperty("templatetypeflag", "W");
            String worksheetid = properties.getProperty("worksheetid");
            if (worksheetid.length() > 0) {
                this.database.createPreparedResultSet("SELECT worksheetid, worksheetversionid, worksheetname FROM worksheet WHERE worksheetid = ?", new Object[]{worksheetid + "_" + templatetypeflag});
                if (this.database.getNext()) {
                    throw new SapphireException("Template id '" + worksheetid + "' already exists");
                }
            }
            this.database.createPreparedResultSet("SELECT worksheetid, worksheetversionid, worksheetname FROM worksheet WHERE worksheetname = ? AND templatetypeflag = ?", new Object[]{properties.getProperty("worksheetname"), "W"});
            if (this.database.getNext()) {
                throw new SapphireException("Template name '" + properties.getProperty("worksheetname") + "' already exists");
            }
            String keygenrule = "";
            PropertyList wsProps = new PropertyList();
            PropertyList wsOptions = new PropertyList();
            wsProps.setProperty("sdcid", "LV_Worksheet");
            if (worksheetid.length() > 0 && properties.getProperty("templateprivacyflag", "G").equals("G")) {
                wsProps.setProperty("keyid1", worksheetid + "_" + (Configuration.isDevmode(this.connectionInfo.getDatabaseId()) ? "LV" : "") + templatetypeflag);
                wsProps.setProperty("coreflag", Configuration.isDevmode(this.connectionInfo.getDatabaseId()) ? "Y" : "N");
                wsProps.setProperty("overrideautokey", "Y");
                keygenrule = TEMPLATE_KEYGENRULE;
                wsOptions.setProperty("keygenrule", keygenrule);
                wsProps.setProperty("options", wsOptions.toXMLString());
            }
            wsProps.setProperty("worksheetversionid", "1");
            wsProps.setProperty("worksheetdesc", properties.getProperty("worksheetname"));
            wsProps.setProperty("worksheetname", properties.getProperty("worksheetname"));
            wsProps.setProperty("templateprivacyflag", properties.getProperty("templateprivacyflag", "G"));
            wsProps.setProperty("templatetypeflag", properties.getProperty("templatetypeflag", "W"));
            wsProps.setProperty("authorid", properties.getProperty("authorid", this.connectionInfo.getSysuserId()));
            wsProps.setProperty("authordt", "now");
            if (!properties.getProperty("templateflag", "N").equals("Y")) {
                throw new SapphireException("Non template worksheets must be created from a template");
            }
            wsProps.setProperty("autokeytemplate", "Y");
            wsProps.setProperty("templateflag", "Y");
            wsProps.setProperty("worksheetstatus", properties.getProperty("worksheetstatus", "InProgress"));
            wsProps.setProperty("worksheet_action", "Y");
            ab.setAction("AddWorksheet", "AddSDI", "1", wsProps);
            PropertyList wssProps = new PropertyList();
            wssProps.setProperty("sdcid", "LV_WorksheetSection");
            wssProps.setProperty("keygenerationrule", keygenrule);
            wssProps.setProperty("worksheetsectionversionid", "1");
            if (wsOptions.getProperty("keygenrule").equals(TEMPLATE_KEYGENRULE)) {
                wssProps.setProperty("worksheetsectionid", this.getTemplateDetailKey(wsProps.getProperty("keyid1")));
                wssProps.setProperty("overrideautokey", "Y");
            }
            wssProps.setProperty("worksheetid", "[$G{AddWorksheet.newkeyid1}]");
            wssProps.setProperty("worksheetversionid", "[$G{AddWorksheet.newkeyid2}]");
            wssProps.setProperty("sectionlevel", "0");
            wssProps.setProperty("worksheetdefaultsection", "Y");
            wssProps.setProperty("sectionstatus", "InProgress");
            wssProps.setProperty("usersequence", "0");
            wssProps.setProperty("worksheet_action", "Y");
            ab.setAction("AddWorksheetSection", "AddSDI", "1", wssProps);
            if (properties.getProperty("templatetypeflag", "W").equals("I") && properties.getProperty("propertytreeid").length() > 0) {
                PropertyList wsiProps = new PropertyList();
                wsiProps.setProperty("keygenerationrule", keygenrule);
                wsiProps.setProperty("worksheetid", "[$G{AddWorksheet.newkeyid1}]");
                wsiProps.setProperty("worksheetversionid", "[$G{AddWorksheet.newkeyid2}]");
                wsiProps.setProperty("worksheetsectionid", "[$G{AddWorksheetSection.newkeyid1}]");
                wsiProps.setProperty("worksheetsectionversionid", "[$G{AddWorksheetSection.newkeyid2}]");
                wsiProps.setProperty("propertytreeid", properties.getProperty("propertytreeid"));
                wsiProps.setProperty("sourcenodeid", properties.getProperty("sourcenodeid", "Sapphire Custom"));
                ab.setActionClass("AddWorksheetItem", AddWorksheetItem.class.getName(), wsiProps);
            }
            this.getActionProcessor().processActionBlock(ab);
            properties.setProperty("worksheetid", ab.getActionProperty("AddWorksheet", "newkeyid1"));
            properties.setProperty("worksheetversionid", ab.getActionProperty("AddWorksheet", "newkeyid2"));
            this.addActivityLog(properties.getProperty("worksheetid"), properties.getProperty("worksheetversionid"), "Add", "LV_Worksheet", properties.getProperty("worksheetid"), properties.getProperty("worksheetversionid"), properties.getProperty("templateflag", "N").equals("Y") ? "Added new worksheet template" : "Added new worksheet");
        } else {
            String[] worksheet = this.copyWorksheet(templateid, properties.getProperty("templateversionid", "1"), properties.getProperty("workbookid"), properties.getProperty("workbookversionid"), properties);
            properties.setProperty("worksheetid", worksheet[0]);
            properties.setProperty("worksheetversionid", worksheet[1]);
            this.addActivityLog(properties.getProperty("worksheetid"), properties.getProperty("worksheetversionid"), "Add", "LV_Worksheet", properties.getProperty("worksheetid"), properties.getProperty("worksheetversionid"), "Added new worksheet from template '" + templateid + "' v(" + properties.getProperty("templateversionid", "1") + ")");
        }
    }
}

