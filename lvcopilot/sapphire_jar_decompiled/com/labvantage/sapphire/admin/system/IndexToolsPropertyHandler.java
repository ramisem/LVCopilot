/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.system;

import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.actions.search.ProcessBacklog;
import com.labvantage.sapphire.modules.search.IndexRequest;
import com.labvantage.sapphire.modules.search.Indexer;
import com.labvantage.sapphire.pageelements.PropertyHandler;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class IndexToolsPropertyHandler
extends PropertyHandler {
    public static final String COMMAND = "dothis";
    public static final String COMMAND_INDEX = "index";
    public static final String COMMAND_INDEXPENDING = "indexpending";
    public static final String COMMAND_INDEXEXISTINGBACKLOG = "indexexistingbacklog";
    public static final String COMMAND_INDEXBACKLOG = "indexbacklog";
    public static final String COMMAND_STOP = "stop";
    public static final String COMMAND_STOPBACKLOG = "stopbacklog";
    public static final String COMMAND_START = "start";
    public static final String COMMAND_RESET = "reset";
    public static final String COMMAND_RESETMAP = "resetmap";
    public static final String COMMAND_RESETPENDING = "resetpending";
    public static final String COMMAND_RESETLASTINDEXDT = "resetlastindexdt";

    @Override
    public void processProperties(HashMap props) throws SapphireException {
        String command = (String)props.get(COMMAND);
        if (command.equals(COMMAND_INDEX)) {
            String indexitem;
            IndexRequest indexRequest = new IndexRequest();
            indexRequest.setUnlock("Y".equals(props.get("unlock")));
            indexRequest.setReset("Y".equals(props.get("resetindex")));
            indexRequest.setProcessImmediate("Y".equals(props.get("processimmediate")));
            String scope = (String)props.get("scope");
            if (!scope.equals("all")) {
                if (scope.equals("sdc")) {
                    indexRequest.setSdcid((String)props.get("sdcid"));
                } else if (scope.equals("sdi")) {
                    indexRequest.setSdcid((String)props.get("sdcid"));
                    String[] sdiparts = StringUtil.split((String)props.get("sdi"), ";");
                    indexRequest.setKeyid1(sdiparts.length > 0 ? sdiparts[0] : "");
                    indexRequest.setKeyid2(sdiparts.length > 1 ? sdiparts[1] : "");
                    indexRequest.setKeyid3(sdiparts.length > 2 ? sdiparts[2] : "");
                }
            }
            if ((indexitem = (String)props.get("indexitem")).equals("sdi")) {
                indexRequest.addAllDatasets();
            } else {
                String[] itemparts = StringUtil.split(indexitem, ";");
                for (int i = 0; i < itemparts.length; ++i) {
                    indexRequest.addDataset(itemparts[i]);
                }
            }
            Indexer.index(this.connectionInfo, indexRequest);
        } else if (command.equals(COMMAND_INDEXPENDING)) {
            IndexRequest indexRequest = new IndexRequest();
            indexRequest.setProcessImmediate(true);
            Indexer.index(this.connectionInfo, indexRequest);
        } else if (command.equals(COMMAND_INDEXBACKLOG) || command.equals(COMMAND_INDEXEXISTINGBACKLOG)) {
            Indexer indexer = Indexer.getInstance(this.connectionInfo.getDatabaseId());
            if (indexer != null) {
                if (Indexer.startBacklogIndexing(this.connectionInfo)) {
                    try {
                        PropertyList backlogProps = new PropertyList();
                        if (command.equals(COMMAND_INDEXBACKLOG)) {
                            String backlogcount = (String)props.get("backlogcount");
                            int rowcount = Integer.parseInt(backlogcount != null && backlogcount.length() > 0 ? backlogcount : "-1");
                            backlogProps.setProperty("rowcount", String.valueOf(rowcount));
                            backlogProps.setProperty("sdcid", props.get("backlogsdcid") != null ? (String)props.get("backlogsdcid") : "");
                            backlogProps.setProperty("aftercreatedt", props.get("backlogaftercreatedt") != null ? (String)props.get("backlogaftercreatedt") : "");
                            backlogProps.setProperty("extendedwhere", props.get("backlogextendedwhere") != null ? (String)props.get("backlogextendedwhere") : "");
                            backlogProps.setProperty("orderby", props.get("backlogseq") != null ? (String)props.get("backlogseq") : "");
                            backlogProps.setProperty("createbacklog", "Y");
                        }
                        ProcessBacklog.addToTDL(this.sapphireConnection, 1, backlogProps);
                    }
                    catch (Exception e) {
                        props.put("error", "Failed to start backlog indexing. Reason: " + e.getMessage());
                    }
                } else {
                    props.put("error", "Failed to start backlog indexing. This is most likely due to other indexing activity locking the index.");
                }
            }
        } else if (command.equals(COMMAND_STOPBACKLOG)) {
            Indexer.stopBacklogIndexing(this.connectionInfo);
        } else if (command.equals(COMMAND_STOP)) {
            Indexer.stopIndexing(this.connectionInfo);
        } else if (command.equals(COMMAND_START)) {
            Indexer.startIndexing(this.connectionInfo);
        } else if (command.equals(COMMAND_RESET)) {
            Indexer.reset(this.connectionInfo, true, true);
        } else if (command.equals(COMMAND_RESETMAP)) {
            Indexer.reset(this.connectionInfo, false, true);
        } else if (command.equals(COMMAND_RESETPENDING)) {
            String indexflag = (String)props.get("indexflag");
            if (indexflag != null && (indexflag.equals("U") || indexflag.equals("D") || indexflag.equals("B"))) {
                Indexer.resetPending(this.connectionInfo, indexflag);
            }
        } else if (command.equals(COMMAND_RESETLASTINDEXDT)) {
            Indexer.setLastIndexDt(this.connectionInfo, DateTimeUtil.getNowCalendar());
        }
    }
}

