/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.actions.qcactions;

import com.labvantage.sapphire.DataSetUtil;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class FillQCSampleTypes
extends BaseAction {
    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String qcbatchid = properties.getProperty("qcbatchid", "");
        if (qcbatchid.length() <= 0) throw new SapphireException("No QC Batch provided.");
        String qcsampletype = properties.getProperty("qcsampletype", "");
        String s_qcbatchsampletypeid = properties.getProperty("s_qcbatchsampletypeid", "");
        if (qcsampletype.length() > 0) {
            String trackItemId = properties.getProperty("trackitemid", "");
            String reagentlotid = properties.getProperty("reagentlotid", "");
            if (trackItemId.length() <= 0 || reagentlotid.length() <= 0) throw new SapphireException("No track item or reagent lot provided.");
            String[] qcsampletype_a = StringUtil.split(qcsampletype, ";");
            String[] trackItemId_a = StringUtil.split(trackItemId, ";");
            String[] reagentlotid_a = StringUtil.split(reagentlotid, ";");
            if (qcsampletype_a.length != trackItemId_a.length || qcsampletype_a.length != reagentlotid_a.length) throw new SapphireException("Track Item Id and/or Reagent Lot Id does not match.");
            for (int i = 0; i < qcsampletype_a.length; ++i) {
                int r = this.getQueryProcessor().execPreparedUpdate("UPDATE s_qcbatchsampletype SET trackitemid=?, reagentlotid=? WHERE qcbatchid=? AND qcsampletype=?", new Object[]{trackItemId_a[i], reagentlotid_a[i], qcbatchid, qcsampletype_a[i]});
                if (r == 1) continue;
                throw new SapphireException("Failed to update qcsampletype " + qcsampletype_a[i] + ".");
            }
            return;
        } else {
            if (s_qcbatchsampletypeid.length() <= 0) throw new SapphireException("No QC Sample Type provided.");
            String trackItemId = properties.getProperty("trackitemid", "");
            String reagentlotid = properties.getProperty("reagentlotid", "");
            if (trackItemId.length() <= 0 || reagentlotid.length() <= 0) throw new SapphireException("No track item or reagent lot provided.");
            String[] s_qcbatchsampletypeid_a = StringUtil.split(s_qcbatchsampletypeid, ";");
            String[] trackItemId_a = StringUtil.split(trackItemId, ";");
            String[] reagentlotid_a = StringUtil.split(reagentlotid, ";");
            if (s_qcbatchsampletypeid_a.length != trackItemId_a.length || s_qcbatchsampletypeid_a.length != reagentlotid_a.length) throw new SapphireException("Track Item Id and/or Reagent Lot Id does not match.");
            DataSet updateDS = new DataSet();
            updateDS.addColumnValues("s_qcbatchsampletypeid", 0, s_qcbatchsampletypeid, ";", "");
            updateDS.addColumnValues("trackitemid", 0, trackItemId, ";", "");
            updateDS.addColumnValues("reagentlotid", 0, reagentlotid, ";", "");
            try {
                DataSetUtil.update(this.database, updateDS, "s_qcbatchsampletype", new String[]{"s_qcbatchsampletypeid"});
                return;
            }
            catch (Exception e) {
                throw new SapphireException("Failed to update s_qcbatchsampletype.", e);
            }
        }
    }
}

