/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.eln;

import com.labvantage.sapphire.actions.eln.BaseELNAction;
import com.labvantage.sapphire.modules.eln.gwt.server.ELNRequest;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.util.ActionBlock;
import sapphire.xml.PropertyList;

public class EditWorksheetSection
extends BaseELNAction
implements sapphire.action.EditWorksheetSection {
    String[] optionProps = new String[]{"showsectionoptions", "allowmodifysection", "allowdeletesection", "esigdeletesection", "allowsectionnotes", "allowsectionattributes", "allowsectionattachments", "excludesectionfromexport", "sectioncompletion", "sectionapprovaltype", "esigsectioncompletion", "allowadditem", "esigadditem"};

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String worksheetsectionid = properties.getProperty("worksheetsectionid");
        String worksheetsectionversionid = properties.getProperty("worksheetsectionversionid");
        String worksheetid = properties.getProperty("worksheetid");
        String worksheetversionid = properties.getProperty("worksheetversionid");
        String activityLog = properties.getProperty("activitylog");
        if (worksheetid.length() == 0) {
            this.database.createPreparedResultSet("SELECT worksheetid, worksheetversionid FROM worksheetsection WHERE worksheetsectionid=? AND worksheetsectionversionid=?", new String[]{worksheetsectionid, worksheetsectionversionid});
            if (this.database.getNext()) {
                worksheetid = this.database.getString("worksheetid");
                worksheetversionid = this.database.getString("worksheetversionid");
            } else {
                throw new ActionException("Unable to locate worksheet section " + worksheetsectionid);
            }
        }
        if (this.worksheetInProgress(this.database, worksheetid, worksheetversionid)) {
            properties.remove("worksheetsectionid");
            properties.remove("worksheetsectionversionid");
            if (properties.containsKey("sectiondesc")) {
                properties.setProperty("worksheetsectiondesc", properties.getProperty("sectiondesc"));
                properties.remove("sectiondesc");
            }
            PropertyList wssProps = new PropertyList();
            wssProps.setProperty("sdcid", "LV_WorksheetSection");
            wssProps.setProperty("keyid1", worksheetsectionid);
            wssProps.setProperty("keyid2", worksheetsectionversionid);
            wssProps.putAll(properties);
            boolean hasOption = false;
            for (int i = 0; i < this.optionProps.length && !hasOption; ++i) {
                hasOption = properties.containsKey(this.optionProps[i]) && properties.getProperty(this.optionProps[i]).length() > 0;
            }
            PropertyList initialOptions = new PropertyList();
            String options = "";
            this.database.createPreparedResultSet("SELECT options FROM worksheetsection WHERE worksheetsectionid=? AND worksheetsectionversionid=?", new String[]{worksheetsectionid, worksheetsectionversionid});
            if (this.database.getNext()) {
                options = this.database.getString("options");
                initialOptions.setPropertyList(options);
            }
            if (hasOption) {
                PropertyList newOptions = this.buildOptionProps(this.optionProps, options, properties, wssProps);
                ActionBlock ab = new ActionBlock();
                ELNRequest.resolveSectionSDIApprovals(this.getQueryProcessor(), worksheetid, worksheetversionid, worksheetsectionid, worksheetsectionversionid, newOptions, ab);
                if (ab.getActionCount() > 0) {
                    this.getActionProcessor().processActionBlock(ab);
                }
            }
            String changes = this.propertyValues(wssProps, initialOptions, new String[0]);
            wssProps.setProperty("worksheet_action", "Y");
            this.getActionProcessor().processAction("EditSDI", "1", wssProps);
            this.addActivityLog(worksheetid, worksheetversionid, "Edit", "LV_WorksheetSection", worksheetsectionid, worksheetsectionversionid, activityLog.length() > 0 ? activityLog : "Edited section values: " + changes);
        }
    }
}

