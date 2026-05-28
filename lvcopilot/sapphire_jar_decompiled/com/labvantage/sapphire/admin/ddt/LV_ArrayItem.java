/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.actions.sdidata.DataEntryLimitsUtil;
import com.labvantage.sapphire.util.array.ArrayUtil;
import sapphire.SapphireException;
import sapphire.action.AddArrayContent;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class LV_ArrayItem
extends BaseSDCRules
implements AddArrayContent {
    static final String LABVANTAGE_CVS_ID = "1: 1.1 $";
    static final String COL_CALCKEYSET = "_calckeyset";
    static final String COL_APPARENTDATATYPE = "_apparentdatatype";

    @Override
    public void preAddDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataEntryLimitsUtil limitsUtil = new DataEntryLimitsUtil(this.getSDCProcessor(), this.getTranslationProcessor(), this.database, this.logger, this.connectionInfo);
        limitsUtil.manipulateLimits("arrayparamitem", sdiData, actionProps);
    }

    @Override
    public void preEditDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataEntryLimitsUtil limitsUtil = new DataEntryLimitsUtil(this.getSDCProcessor(), this.getTranslationProcessor(), this.database, this.logger, this.connectionInfo);
        limitsUtil.manipulateLimits("arrayparamitem", sdiData, actionProps);
    }

    @Override
    public boolean requiresDataEntryPrimary() {
        return true;
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        ArrayUtil.checkExistenceInArray(rsetid, actionProps, this.getQueryProcessor(), this.getTranslationProcessor(), true);
    }

    @Override
    public void postDeleteDetail(String rsetid, PropertyList actionProps) throws SapphireException {
        String arrayitemids = actionProps.getProperty("arrayitemid", "");
        if (arrayitemids.length() > 0) {
            String status;
            String[] arrayitemidlist = StringUtil.split(arrayitemids, ";");
            String arrayid = this.getQueryProcessor().getPreparedSqlDataSet("SELECT arrayid FROM arrayitem WHERE arrayitemid=?", new Object[]{arrayitemidlist[0]}).getValue(0, "arrayid");
            if (arrayid != null && arrayid.length() > 0 && (status = com.labvantage.sapphire.actions.array.ArrayUtil.getArrayStatus(this.getQueryProcessor(), arrayid)).equals("Loaded")) {
                int countitemcontent = this.getQueryProcessor().getPreparedCount("select count(*) from arrayitemcontent where arrayitemid in ( select arrayitemid from arrayitem where arrayid = ? )", new Object[]{arrayid});
                int countzonecontent = this.getQueryProcessor().getPreparedCount("select count(*) from arrayzonecontent where arrayzoneid in ( select arrayzoneid from arrayzone where arrayid = ? and contentkeyid1 is not null)", new Object[]{arrayid});
                if (countitemcontent == 0 && countzonecontent == 0) {
                    this.updateArrayArrayMethodItemStatus(actionProps, arrayid, "Initial");
                    this.updateArrayStatus(arrayid, "Applied");
                }
            }
        }
    }

    private void updateArrayStatus(String arrayid, String arrayStatus) throws SapphireException {
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "LV_Array");
        props.setProperty("keyid1", arrayid);
        props.setProperty("arraystatus", arrayStatus);
        this.getActionProcessor().processAction("EditSDI", "1", props);
    }

    private void updateArrayArrayMethodItemStatus(PropertyList properties, String arrayid, String status) throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        safeSQL.addVar(arrayid);
        String sql = "SELECT arraymethodid, arraymethodversionid, arraymethodinstance, arraymethoditemstatus FROM arrayarraymethoditem WHERE arrayid=? order by usersequence desc";
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (ds != null && ds.getRowCount() > 0 && !ds.getString(0, "arraymethoditemstatus").equals(status)) {
            PropertyList arrayarraymethoditemprops = new PropertyList();
            arrayarraymethoditemprops.setProperty("sdcid", "LV_Array");
            arrayarraymethoditemprops.setProperty("linkid", "Array ArrayMethod Item");
            arrayarraymethoditemprops.setProperty("arrayid", arrayid);
            arrayarraymethoditemprops.setProperty("arraymethodid", ds.getString(0, "arraymethodid"));
            arrayarraymethoditemprops.setProperty("arraymethodversionid", ds.getString(0, "arraymethodversionid"));
            arrayarraymethoditemprops.setProperty("arraymethodinstance", "" + ds.getInt(0, "arraymethodinstance"));
            arrayarraymethoditemprops.setProperty("arraymethoditemstatus", status);
            arrayarraymethoditemprops.setProperty("auditsignedflag", properties.getProperty("auditsignedflag"));
            arrayarraymethoditemprops.setProperty("auditactivity", properties.getProperty("auditactivity"));
            arrayarraymethoditemprops.setProperty("auditreason", properties.getProperty("auditreason"));
            this.getActionProcessor().processAction("EditSDIDetail", "1", arrayarraymethoditemprops);
        }
    }
}

