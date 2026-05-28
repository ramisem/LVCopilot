/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.actions;

import com.labvantage.sapphire.DataSetUtil;
import java.util.HashMap;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;

public class DetailActions
extends BaseAction {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";
    private static final String __ModifiableSpecColumns = "decheckflag";

    @Override
    public int processAction(String actionid, String actionversionid, HashMap props) {
        if (actionid.equals("EditSDISpec") || actionid.equals("SetSpecDataEntryFlag")) {
            return this.setSpecDataEntryFlag(props);
        }
        return this.setError("Action " + actionid + " not found.");
    }

    private int setSpecDataEntryFlag(HashMap props) {
        int rc = 1;
        String sdcid = (String)props.get("sdcid");
        String keyid1 = (String)props.get("keyid1");
        String keyid2 = (String)props.get("keyid2");
        String keyid3 = (String)props.get("keyid3");
        String specid = (String)props.get("specid");
        String specversionid = (String)props.get("specversionid");
        String auditreason = (String)props.get("auditreason");
        if (auditreason == null) {
            auditreason = "";
        }
        if (sdcid.length() == 0 || keyid1.length() == 0 || specid.length() == 0 || specversionid.length() == 0) {
            return this.setError("Missing mandatory input");
        }
        if (keyid2 == null || keyid2.length() == 0 || keyid2.equals("null")) {
            keyid2 = "(null)";
        }
        if (keyid3 == null || keyid3.length() == 0 || keyid3.equals("null")) {
            keyid3 = "(null)";
        }
        DataSet dsupdate = new DataSet();
        dsupdate.addColumnValues("sdcid", 0, sdcid, ";");
        dsupdate.addColumnValues("keyid1", 0, keyid1, ";");
        dsupdate.addColumnValues("keyid2", 0, keyid2, ";");
        dsupdate.addColumnValues("keyid3", 0, keyid3, ";");
        dsupdate.addColumnValues("specid", 0, specid, ";");
        dsupdate.addColumnValues("specversionid", 1, specversionid, ";");
        if (props.containsKey(__ModifiableSpecColumns)) {
            dsupdate.addColumnValues(__ModifiableSpecColumns, 0, props.get(__ModifiableSpecColumns).toString(), ";");
        }
        dsupdate.addColumnValues("moddt", 2, "n", ";");
        dsupdate.addColumnValues("modby", 0, this.connectionInfo.getSysuserId(), ";");
        if (dsupdate.size() > 0) {
            try {
                dsupdate.padColumns();
                String[] keycols = new String[]{"sdcid", "keyid1", "keyid2", "keyid3", "specid", "specversionid"};
                DataSetUtil.update(this.database, dsupdate, "sdispec", keycols);
            }
            catch (Exception e) {
                rc = this.setError("DB_ACTION_FAILED", "Error updating database. Exception: " + e.getMessage());
            }
        }
        return rc;
    }
}

