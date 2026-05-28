/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.sdms.actions;

import com.labvantage.sapphire.modules.sdms.SDMSConstants;
import com.labvantage.sapphire.modules.sdms.SDMSUtil;
import com.labvantage.sapphire.modules.sdms.collector.SDMSCommandHandler;
import java.util.HashMap;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class CheckSDMSRebootFlag
extends BaseAction
implements SDMSConstants {
    HashMap<String, PropertyList> collectorStartupStates = new HashMap();

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        block8: {
            SDMSCommandHandler sdmsCommandHandler;
            String instrumentidlist;
            block7: {
                String collectoridlist = properties.getProperty("collectorid");
                instrumentidlist = properties.getProperty("instrumentid");
                String propertytreeid = properties.getProperty("propertytreeid");
                if (propertytreeid.length() > 0) {
                    DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT instrumentid FROM instrument WHERE sdmscollectorid is not null AND collectorpropertytreeid=?", (Object[])new String[]{propertytreeid});
                    instrumentidlist = ds.getColumnValues("instrumentid", ";");
                }
                sdmsCommandHandler = new SDMSCommandHandler();
                sdmsCommandHandler.setConnectionId(this.getConnectionId());
                if (collectoridlist.length() <= 0) break block7;
                String[] collectorid = StringUtil.split(collectoridlist, ";");
                for (int i = 0; i < collectorid.length; ++i) {
                    PropertyList commandRequest = new PropertyList();
                    commandRequest.setProperty("collectorid", collectorid[i]);
                    try {
                        PropertyList configProps = sdmsCommandHandler.processCommand("COMMAND_GETCONFIGPROPS", commandRequest);
                        String currentConfigHash = configProps.getProperty("confighash");
                        PropertyList startupState = this.getCollectorStartupState(collectorid[i]);
                        String startConfigHash = startupState.getProperty("confighash");
                        if (startConfigHash.equals(currentConfigHash)) continue;
                        PropertyList props = new PropertyList();
                        props.setProperty("sdcid", "LV_SDMSCollector");
                        props.setProperty("keyid1", collectorid[i]);
                        props.setProperty("rebootrequiredflag", "Y");
                        props.setProperty("skipconfighash", "Y");
                        this.getActionProcessor().processAction("EditSDI", "1", props);
                        continue;
                    }
                    catch (Exception configProps) {
                        // empty catch block
                    }
                }
                break block8;
            }
            if (instrumentidlist.length() <= 0) break block8;
            String[] instrumentid = StringUtil.split(instrumentidlist, ";");
            for (int i = 0; i < instrumentid.length; ++i) {
                try {
                    String startConfigHash;
                    PropertyList commandRequest = new PropertyList();
                    commandRequest.setProperty("instrumentid", instrumentid[i]);
                    PropertyList configProps = sdmsCommandHandler.processCommand("COMMAND_GETINSTRUMENTCONFIGPROPS", commandRequest);
                    DataSet ds = new DataSet(new JSONObject(configProps.getProperty("instruments_dataset")));
                    String configHash = ds.getValue(0, "_confighash");
                    String collectorid = ds.getValue(0, "sdmscollectorid");
                    PropertyList startupState = this.getCollectorStartupState(collectorid);
                    PropertyListCollection instruments = startupState.getCollectionNotNull("instruments");
                    PropertyList instPL = instruments.find("instrumentid", instrumentid[i]);
                    String string = startConfigHash = instPL == null ? "" : instPL.getProperty("confighash");
                    if (startConfigHash.equals(configHash)) continue;
                    PropertyList props = new PropertyList();
                    props.setProperty("sdcid", "Instrument");
                    props.setProperty("keyid1", instrumentid[i]);
                    props.setProperty("rebootrequiredflag", "Y");
                    props.setProperty("skipconfighash", "Y");
                    this.getActionProcessor().processAction("EditSDI", "1", props);
                    continue;
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }
    }

    private PropertyList getCollectorStartupState(String collectorid) {
        PropertyList startupState = this.collectorStartupStates.get(collectorid);
        if (startupState == null) {
            startupState = SDMSUtil.getCollectorStartupState(this.getQueryProcessor(), collectorid);
            this.collectorStartupStates.put(collectorid, startupState);
        }
        return startupState;
    }
}

