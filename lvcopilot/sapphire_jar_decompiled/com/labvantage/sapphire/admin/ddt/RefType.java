/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.services.ConfigService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.cache.CacheNames;
import com.labvantage.sapphire.util.cache.CacheUtil;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class RefType
extends BaseSDCRules
implements CacheNames {
    static final String LABVANTAGE_CVS_ID = "$Revision: 84852 $";

    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        String typeflag;
        String compcode = Configuration.getCompcode(this.connectionInfo.getDatabaseId());
        String string = typeflag = Configuration.isDevmode(this.connectionInfo.getDatabaseId()) ? "S" : "U";
        if (compcode.length() > 0) {
            typeflag = "S";
        }
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.size(); ++i) {
            primary.setString(i, "typeflag", typeflag);
        }
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        if (!Configuration.isDevmode(this.connectionInfo.getDatabaseId()) && Configuration.getCompcode(this.connectionInfo.getDatabaseId()).length() == 0) {
            this.checkCoreType(rsetid, "You cannot delete 'Core' or 'System' reference types", false);
        }
        String sdclinkCheck = "SELECT sdclink.sdcid, sdclink.reftypeid FROM   sdclink, rsetitems WHERE  rsetitems.rsetid = ? AND    sdclink.reftypeid = rsetitems.keyid1 ORDER BY 1";
        this.database.createPreparedResultSet(sdclinkCheck, new Object[]{rsetid});
        StringBuffer sdclinkRefs = new StringBuffer();
        for (int i = 0; i < 10 && this.database.getNext(); ++i) {
            sdclinkRefs.append("<br/>").append(this.database.getString("sdcid"));
        }
        if (sdclinkRefs.length() > 0) {
            boolean more = this.database.getNext();
            this.throwError("RefTypeUsed", "VALIDATION", "Reftype(s) cannot be deleted because of " + (more ? "at least" : "") + " the following SDC link references:" + sdclinkRefs + (more ? "<br/>..." : ""));
        }
        String sdcdetaillinkCheck = "SELECT sdcdetaillink.sdcid, sdcdetaillink.reftypeid FROM   sdcdetaillink, rsetitems WHERE  rsetitems.rsetid = ? AND    sdcdetaillink.reftypeid = rsetitems.keyid1 ORDER BY 1";
        this.database.createPreparedResultSet(sdcdetaillinkCheck, new Object[]{rsetid});
        StringBuffer sdcdetaillinkRefs = new StringBuffer();
        for (int i = 0; i < 10 && this.database.getNext(); ++i) {
            sdcdetaillinkRefs.append("<br/>").append(this.database.getString("sdcid"));
        }
        if (sdcdetaillinkRefs.length() > 0) {
            boolean more = this.database.getNext();
            this.throwError("RefTypeUsed", "VALIDATION", "Reftype(s) cannot be deleted because of " + (more ? "at least" : "") + " the following SDC detail link references:" + sdcdetaillinkRefs + (more ? "<br/>..." : ""));
        }
        String paramlistitemCheck = "SELECT DISTINCT paramlistitem.paramlistid, paramlistitem.paramlistversionid  FROM   paramlistitem, rsetitems  WHERE  rsetitems.rsetid = ? AND rsetitems.sdcid = 'RefType' AND    paramlistitem.entryreftypeid = rsetitems.keyid1 ORDER BY 1";
        this.database.createPreparedResultSet("paramlistitemrefs", paramlistitemCheck, new Object[]{rsetid});
        StringBuffer paramlistitemRefs = new StringBuffer();
        for (int i = 0; i < 10 && this.database.getNext("paramlistitemrefs"); ++i) {
            paramlistitemRefs.append("<br/>").append(this.database.getString("paramlistitemrefs", "paramlistid")).append(" (").append(this.database.getString("paramlistitemrefs", "paramlistversionid")).append(")");
        }
        if (paramlistitemRefs.length() > 0) {
            boolean more = this.database.getNext("paramlistitemrefs");
            this.throwError("RefTypeUsed", "VALIDATION", "Reftype(s) cannot be deleted because of " + (more ? "at least" : "") + " the following ParamList references:" + paramlistitemRefs + (more ? "<br/>..." : ""));
        }
        this.database.closeResultSet("paramlistitemrefs");
    }

    @Override
    public boolean requiresEditDetailPrimary() {
        return true;
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.size(); ++i) {
            String reftypeid = primary.getValue(i, "reftypeid");
            this.clearCache(reftypeid);
        }
    }

    @Override
    public void preAddDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        if (!Configuration.isDevmode(this.connectionInfo.getDatabaseId())) {
            this.checkCoreType(sdiData);
        }
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.size(); ++i) {
            String reftypeid = primary.getValue(i, "reftypeid");
            this.clearCache(reftypeid);
        }
    }

    @Override
    public void preEditDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        ConfigService config = new ConfigService(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
        if (!Configuration.isDevmode(this.connectionInfo.getDatabaseId())) {
            this.checkCoreType(sdiData);
        }
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.size(); ++i) {
            String reftypeid = primary.getValue(i, "reftypeid");
            this.clearCache(reftypeid);
        }
    }

    @Override
    public void preDeleteDetail(String rsetid, PropertyList actionProps) throws SapphireException {
        this.database.createPreparedResultSet("SELECT distinct keyid1 from rsetitems WHERE rsetid = ?", new Object[]{rsetid});
        while (this.database.getNext()) {
            String reftypeid = this.database.getString("keyid1");
            this.clearCache(reftypeid);
        }
    }

    private void checkCoreType(SDIData sdiData) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        if (primary == null) {
            throw new SapphireException("Primary dataset not available");
        }
        for (int i = 0; i < primary.size(); ++i) {
            if (!primary.getString(i, "typeflag").equals("C")) continue;
            throw new SapphireException("You cannot modify 'Core' reference types");
        }
    }

    private void checkCoreType(String rsetid, String message, boolean allowSystem) throws SapphireException {
        this.database.createPreparedResultSet("SELECT typeflag FROM reftype, rsetitems WHERE reftype.reftypeid = rsetitems.keyid1 AND rsetid = ?", new Object[]{rsetid});
        while (this.database.getNext()) {
            if (this.database.getString("typeflag") == null || (allowSystem || !this.database.getString("typeflag").equals("S")) && !this.database.getString("typeflag").equals("C")) continue;
            throw new SapphireException(message);
        }
    }

    private void clearCache(String reftypeid) {
        CacheUtil.remove(this.connectionInfo.getDatabaseId(), "RefValues", reftypeid);
        CacheUtil.remove(this.connectionInfo.getDatabaseId(), "ClientRefTypeCache", reftypeid);
    }
}

