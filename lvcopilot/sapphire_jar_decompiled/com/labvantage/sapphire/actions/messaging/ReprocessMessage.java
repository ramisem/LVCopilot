/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.messaging;

import com.labvantage.sapphire.Trace;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ReprocessMessage
extends BaseAction
implements sapphire.action.ReprocessMessage {
    @Override
    public void processAction(PropertyList propertyList) throws SapphireException {
        String msgLogIds = propertyList.getProperty("messagelogid");
        String lastReprocessedBy = propertyList.getProperty("lastreprocessedby");
        String successList = "";
        String failList = "";
        if (msgLogIds == null || msgLogIds.length() == 0) {
            throw new ActionException("MessageLogId is empty or null");
        }
        String[] msgLogId = StringUtil.split(msgLogIds, ";");
        for (int i = 0; i < msgLogId.length; ++i) {
            String status;
            PropertyList props;
            String sql = "SELECT messagetype.directionflag, allowreprocessflag, processstatus, messagetype.messagetypeid FROM messagetype, messagelog where messagelogid = ? and messagelog.messagetypeid = messagetype.messagetypeid";
            DataSet results = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{msgLogId[i]});
            if (results.getRowCount() == 0) {
                this.setError("Invalid msglogid: " + msgLogId[i]);
                continue;
            }
            String direction = results.getString(0, "directionflag");
            String allowreprocessflag = results.getString(0, "allowreprocessflag");
            String processstatus = results.getString(0, "processstatus");
            String messageType = results.getString(0, "messagetypeid");
            if ("N".equals(allowreprocessflag)) {
                Trace.log("Cannot reprocess messages of type:  " + messageType);
                this.setError(msgLogId[i] + ": Cannot reprocess messages of type:  " + messageType);
                if (failList.length() == 0) {
                    failList = failList + msgLogId[i];
                    continue;
                }
                failList = failList + "," + msgLogId[i];
                continue;
            }
            if ("F".equals(allowreprocessflag)) {
                if (!"ERROR".equals(processstatus)) {
                    Trace.log(" Cannot reprocess message with ProcessStatus as: " + processstatus);
                    this.setError(msgLogId[i] + ": Cannot reprocess message " + msgLogId[i] + "with ProcessStatus as: " + processstatus);
                    if (failList.length() == 0) {
                        failList = failList + msgLogId[i];
                        continue;
                    }
                    failList = failList + "," + msgLogId[i];
                    continue;
                }
            } else if ("WAITING".equals(processstatus)) {
                Trace.log(" Cannot reprocess message with ProcessStatus WAITING ");
                this.setError(msgLogId[i] + ": Cannot reprocess message with ProcessStatus as: " + processstatus);
                if (failList.length() == 0) {
                    failList = failList + msgLogId[i];
                    continue;
                }
                failList = failList + "," + msgLogId[i];
                continue;
            }
            if ("O".equalsIgnoreCase(direction)) {
                props = new PropertyList();
                props.setProperty("messagelogid", msgLogId[i]);
                props.setProperty("lastreprocessedby", lastReprocessedBy);
                this.getActionProcessor().processAction("ProcessOutMessage", "1", props, true);
                status = props.getProperty("status");
                if (!"SUCCESS".equals(status)) {
                    if (failList.length() == 0) {
                        failList = failList + msgLogId[i];
                        continue;
                    }
                    failList = failList + "," + msgLogId[i];
                    continue;
                }
                if (successList.length() == 0) {
                    successList = successList + msgLogId[i];
                    continue;
                }
                successList = successList + "," + msgLogId[i];
                continue;
            }
            if ("I".equalsIgnoreCase(direction)) {
                props = new PropertyList();
                props.setProperty("messagelogid", msgLogId[i]);
                props.setProperty("lastreprocessedby", lastReprocessedBy);
                this.getActionProcessor().processAction("ProcessInMessage", "1", props, true);
                status = props.getProperty("status");
                if (!"SUCCESS".equals(status)) {
                    if (failList.length() == 0) {
                        failList = failList + msgLogId[i];
                        continue;
                    }
                    failList = failList + "," + msgLogId[i];
                    continue;
                }
                if (successList.length() == 0) {
                    successList = successList + msgLogId[i];
                    continue;
                }
                successList = successList + "," + msgLogId[i];
                continue;
            }
            this.setError("Direction is not known for msglogid: " + msgLogId[i]);
        }
        if (failList.length() > 0) {
            String info = "Reprocess Failed for item(s): " + failList;
            this.setError(info, "INFORMATION");
        }
        propertyList.setProperty("successlist", successList);
        propertyList.setProperty("faillist", failList);
    }
}

