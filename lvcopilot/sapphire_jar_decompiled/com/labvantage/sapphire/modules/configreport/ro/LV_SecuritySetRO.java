/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.ro;

import com.labvantage.sapphire.services.SapphireConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;
import sapphire.SapphireException;
import sapphire.ext.BaseSDCRO;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;

public class LV_SecuritySetRO
extends BaseSDCRO {
    public void initialize(SapphireConnection connection) throws SapphireException {
        super.initialize("LV_SecuritySet", connection);
    }

    public String getSecuritySetId() {
        return this.getKeyid1();
    }

    public String getSecuritySetDesc() {
        return this.getDescription();
    }

    public String getOwnerSdcId() {
        return this.getPrimaryValue("ownersdcid");
    }

    public String getOwnerKeyid1() {
        return this.getPrimaryValue("ownerkeyid1");
    }

    public String getOwnerKeyid2() {
        return this.getPrimaryValue("ownerkeyid2");
    }

    public String getOwnerKeyid3() {
        return this.getPrimaryValue("ownerkeyid3");
    }

    public String getOwnerCheckFlag() {
        return this.getPrimaryValue("ownercheckflag");
    }

    public String getOwnerSDI() {
        String sdi = "";
        String sdcid = this.getPrimaryValue("ownersdcid");
        if (sdcid != null && sdcid.length() > 0) {
            sdi = sdcid;
            sdi = sdi + " ( " + this.getOwnerKeyid1();
        }
        String keyid2 = this.getOwnerKeyid2();
        String keyid3 = this.getOwnerKeyid3();
        if (keyid2 != null && keyid2.length() > 0) {
            sdi = sdi + "," + keyid2;
        }
        if (keyid3 != null && keyid3.length() > 0) {
            sdi = sdi + "," + keyid3;
        }
        if (sdi.length() > 0) {
            sdi = sdi + ")";
        }
        return sdi;
    }

    public DataSet getSecuritySetSDCs() {
        return this.getDataSet("securitysetsdc");
    }

    public DataSet getSecuritySetItems() {
        DataSet s = this.getDataSet("securitysetitem");
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("securitysetitemid", "All");
        DataSet match = s.getFilteredDataSet(filter);
        if (match == null || match.getRowCount() == 0) {
            // empty if block
        }
        return s;
    }

    public String[] getDistinctSecuritySetItemIds() {
        DataSet ds = this.getDataSet("securitysetitem");
        String ssids = ds.getColumnValues("securitysetitemid", ";");
        String[] ids = StringUtil.split(ssids, ";");
        HashSet<String> set = new HashSet<String>();
        for (int i = 0; i < ids.length; ++i) {
            if (set.contains(ids[i])) continue;
            set.add(ids[i]);
        }
        String[] ret = new String[set.size()];
        Iterator iter = set.iterator();
        int i = 0;
        while (iter.hasNext()) {
            ret[i++] = iter.next().toString();
        }
        return ret;
    }

    public String getSDCOperations(String sdcid) {
        DataSet ds = this.getSecuritySetItems();
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("securitysetsdcid", sdcid);
        ds = ds.getFilteredDataSet(filter);
        ds.sort("operationid");
        TreeSet<String> set = new TreeSet<String>();
        for (int i = 0; i < ds.getRowCount(); ++i) {
            if (set.contains(ds.getString(i, "operationid"))) continue;
            set.add(ds.getString(i, "operationid"));
        }
        Object[] arr = set.toArray();
        String ops = "";
        for (int i = 0; i < arr.length; ++i) {
            if (i != 0) {
                ops = ops + ";";
            }
            ops = ops + arr[i].toString();
        }
        return ops;
    }
}

