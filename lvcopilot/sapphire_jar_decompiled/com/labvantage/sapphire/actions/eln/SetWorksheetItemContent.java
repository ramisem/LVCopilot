/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.eln;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.actions.eln.AddWorksheetItemRef;
import com.labvantage.sapphire.actions.eln.BaseELNAction;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItem;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemFactory;
import com.labvantage.sapphire.pageelements.gwt.server.command.CommandRequest;
import com.labvantage.sapphire.pageelements.gwt.server.command.CommandResponse;
import com.labvantage.sapphire.pageelements.gwt.shared.CommandConstants;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.servlet.RequestProcessor;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class SetWorksheetItemContent
extends BaseELNAction {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String worksheetid = properties.getProperty("worksheetid");
        String worksheetversionid = properties.getProperty("worksheetversionid");
        String worksheetitemid = properties.getProperty("worksheetitemid");
        String worksheetitemversionid = properties.getProperty("worksheetitemversionid", "1");
        String activitylog = properties.getProperty("activitylog", "Set worksheet item content");
        DataSet item = this.getWorksheetItem(worksheetitemid, worksheetitemversionid);
        if (this.sectionInProgress(this.database, item.getValue(0, "worksheetsectionid"), item.getValue(0, "worksheetsectionversionid"))) {
            String contents = properties.getProperty("contents");
            if (worksheetitemid.length() == 0) {
                throw new SapphireException("INVALID_PROPERTY", "No worksheetitemid specified");
            }
            PropertyList editProps = new PropertyList();
            editProps.setProperty("sdcid", "LV_WorksheetItem");
            editProps.setProperty("keyid1", worksheetitemid);
            editProps.setProperty("keyid2", worksheetitemversionid);
            editProps.setProperty("auditreason", properties.getProperty("auditreason"));
            editProps.setProperty("auditsignedflag", properties.getProperty("auditsignedflag"));
            editProps.setProperty("auditactivity", properties.getProperty("auditactivity"));
            WorksheetItem worksheetItem = WorksheetItemFactory.getInstance(new SapphireConnection(this.database.getConnection(), this.connectionInfo), (DBUtil)this.database, (HashMap)item.get(0));
            worksheetItem.setTemplate(properties.getProperty("template").equals("Y"));
            contents = worksheetItem.validateContents(contents);
            editProps.setProperty("contents", contents);
            item.setValue(0, "contents", contents);
            properties.put("worksheetitem", worksheetItem);
            editProps.setProperty("worksheet_action", "Y");
            this.getActionProcessor().processAction("EditSDI", "1", editProps);
            DataSet fields = null;
            RequestProcessor requestProcessor = new RequestProcessor(this.getConnectionId());
            ((DBUtil)this.database).executePreparedUpdate("DELETE worksheetitemreference WHERE refsdcid = ? AND worksheetitemid = ? AND worksheetitemversionid = ? AND reffunction = ?", new Object[]{"LV_WorksheetItem", worksheetitemid, worksheetitemversionid, "field"});
            if (contents.contains("$F{")) {
                String[] tokens;
                for (String field : tokens = StringUtil.getTokens(contents, "$F{", "}")) {
                    String refworksheetitemid = "";
                    String refworksheetitemversionid = "";
                    String[] bits = StringUtil.split(field, ";");
                    if (bits.length >= 3) {
                        refworksheetitemid = bits[0];
                        refworksheetitemversionid = bits[1];
                    } else if (bits.length == 1) {
                        int row;
                        if (fields == null) {
                            CommandRequest commandRequest = new CommandRequest("lf");
                            commandRequest.set("worksheetid", worksheetid);
                            commandRequest.set("worksheetversionid", worksheetversionid);
                            CommandResponse commandResponse = new CommandResponse();
                            HashMap<String, CommandConstants> requestMap = new HashMap<String, CommandConstants>();
                            requestMap.put("commandrequest", commandRequest);
                            requestMap.put("commandresponse", commandResponse);
                            HashMap returnMap = requestProcessor.processRequest("com.labvantage.sapphire.modules.eln.gwt.server.ELNRequest", requestMap);
                            if (commandResponse.containsKey("fields") && commandResponse.get("fields") instanceof DataSet) {
                                fields = (DataSet)commandResponse.get("fields");
                            }
                        }
                        if (fields != null && (row = fields.findRow("fieldname", field)) > -1) {
                            refworksheetitemid = fields.getValue(row, "worksheetitemid");
                            refworksheetitemversionid = fields.getValue(row, "worksheetitemversionid");
                        }
                    }
                    if (refworksheetitemid.length() <= 0 || refworksheetitemversionid.length() <= 0) continue;
                    PropertyList refProps = new PropertyList();
                    refProps.setProperty("worksheetid", worksheetid);
                    refProps.setProperty("worksheetversionid", worksheetversionid);
                    refProps.setProperty("worksheetitemid", worksheetitemid);
                    refProps.setProperty("worksheetitemversionid", worksheetitemversionid);
                    refProps.setProperty("refworksheetid", worksheetid);
                    refProps.setProperty("refworksheetversionid", worksheetversionid);
                    refProps.setProperty("refsdcid", "LV_WorksheetItem");
                    refProps.setProperty("refkeyid1", refworksheetitemid);
                    refProps.setProperty("refkeyid2", refworksheetitemversionid);
                    refProps.setProperty("reffunction", "field");
                    this.getActionProcessor().processActionClass(AddWorksheetItemRef.class.getName(), refProps);
                }
            }
            worksheetItem.postContents();
            this.addActivityLog(worksheetid, worksheetversionid, "SetContent", "LV_WorksheetItem", worksheetitemid, worksheetitemversionid, activitylog);
        }
    }
}

