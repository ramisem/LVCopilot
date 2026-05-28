/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.eln;

import com.labvantage.sapphire.actions.eln.BaseELNAction;
import com.labvantage.sapphire.modules.eln.gwt.server.ELNRequest;
import java.util.Arrays;
import sapphire.SapphireException;
import sapphire.util.ActionBlock;
import sapphire.xml.PropertyList;

public class EditWorksheet
extends BaseELNAction
implements sapphire.action.EditWorksheet {
    String[] optionProps = new String[]{"worksheetpolicynode", "showworksheetoptions", "showuserprivs", "showsaveastemplate", "showresetdetails", "allowmodifytitle", "allowworksheetnotes", "allowworksheetattributes", "allowworksheetattachments", "showtocstatuses", "showtocitems", "collapselimstoc", "allowaddsection", "esigaddsection", "worksheetcompletion", "esigworksheetcompletion", "worksheetapprovaltype", "savehtmloncomplete", "lockworksheet", "esigreasonstatus", "esigreasonmodify"};

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String worksheetid = properties.getProperty("worksheetid");
        String worksheetversionid = properties.getProperty("worksheetversionid");
        String activityLog = properties.getProperty("activitylog");
        boolean elnrequest = properties.getProperty("elnrequest", "N").equals("Y");
        properties.remove("worksheetid");
        properties.remove("worksheetversionid");
        properties.remove("elnrequest");
        properties.remove("actionid");
        properties.remove("allow");
        properties.remove("applylock");
        properties.remove("timestamp");
        properties.remove("activitylog");
        if (!elnrequest) {
            for (String propertyid : properties.keySet()) {
                if (properties.getProperty(propertyid).length() <= 0 || propertyid.startsWith("u_") || propertyid.equals("worksheetdesc") || propertyid.equals("worksheetname") || propertyid.equals("authorid") || propertyid.equals("auditactivity") || propertyid.equals("buttonactivity") || propertyid.equals("auditreason") || propertyid.equals("tracelogid") || propertyid.equals("auditsignedflag") || propertyid.equals("auditdt") || Arrays.asList(this.optionProps).contains(propertyid)) continue;
                throw new SapphireException("INVALID_PROPERTY", propertyid + " cannot be set with EditWorksheet");
            }
        }
        if (this.worksheetInProgress(this.database, worksheetid, worksheetversionid)) {
            PropertyList wsProps = new PropertyList();
            wsProps.setProperty("sdcid", "LV_Worksheet");
            wsProps.setProperty("keyid1", worksheetid);
            wsProps.setProperty("keyid2", worksheetversionid);
            wsProps.putAll(properties);
            wsProps.setProperty("worksheet_action", "Y");
            if (properties.containsKey("authorid")) {
                wsProps.setProperty("securityuser", properties.getProperty("authorid"));
                this.database.createPreparedResultSet("SELECT defaultdepartment FROM sysuser WHERE sysuserid = ?", new Object[]{properties.getProperty("authorid")});
                if (this.database.getNext()) {
                    wsProps.setProperty("securitydepartment", this.database.getValue("defaultdepartment"));
                }
            }
            if (properties.containsKey("worksheetname")) {
                wsProps.setProperty("worksheetdesc", properties.getProperty("worksheetname"));
            }
            if (properties.containsKey("worksheetdesc")) {
                wsProps.setProperty("worksheetname", properties.getProperty("worksheetdesc"));
            }
            boolean hasOption = false;
            for (int i = 0; i < this.optionProps.length && !hasOption; ++i) {
                hasOption = properties.containsKey(this.optionProps[i]) && properties.getProperty(this.optionProps[i]).length() > 0;
            }
            PropertyList initialOptions = new PropertyList();
            String options = "";
            this.database.createPreparedResultSet("SELECT options, blockflag, blocksdcid, lesflag FROM worksheet WHERE worksheetid=? AND worksheetversionid=?", new String[]{worksheetid, worksheetversionid});
            if (this.database.getNext()) {
                options = this.database.getString("options");
                initialOptions.setPropertyList(options);
                initialOptions.setProperty("blockflag", this.database.getString("blockflag"));
                initialOptions.setProperty("blocksdcid", this.database.getString("blocksdcid"));
                initialOptions.setProperty("lesflag", this.database.getString("lesflag"));
            }
            if (hasOption) {
                PropertyList newOptions = this.buildOptionProps(this.optionProps, options, properties, wsProps);
                ActionBlock ab = new ActionBlock();
                ELNRequest.resolveWorksheetSDIApprovals(this.getQueryProcessor(), worksheetid, worksheetversionid, newOptions, ab);
                if (ab.getActionCount() > 0) {
                    this.getActionProcessor().processActionBlock(ab);
                }
            }
            String changes = this.propertyValues(wsProps, initialOptions, new String[]{"userprivs"});
            this.getActionProcessor().processAction("EditSDI", "1", wsProps);
            this.addActivityLog(worksheetid, worksheetversionid, "Edit", "LV_Worksheet", worksheetid, worksheetversionid, activityLog.length() > 0 ? activityLog : "Edited worksheet values: " + changes);
        }
    }
}

