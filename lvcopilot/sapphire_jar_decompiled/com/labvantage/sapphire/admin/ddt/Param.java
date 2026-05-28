/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.xml.PropertyList;

public class Param
extends BaseSDCRules {
    private String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        String paramlistCheck = "SELECT paramlistitem.paramlistid, paramlistitem.paramlistversionid, paramlistitem.variantid FROM   paramlistitem, rsetitems WHERE  rsetitems.rsetid = ? AND    paramlistitem.paramid = rsetitems.keyid1 ORDER BY 1, 2, 3";
        this.database.createPreparedResultSet(paramlistCheck, new Object[]{rsetid});
        StringBuffer paramlistRefs = new StringBuffer();
        for (int i = 0; i < 10 && this.database.getNext(); ++i) {
            paramlistRefs.append("<br/>").append(this.database.getString("paramlistid")).append(": ").append(this.database.getString("paramlistversionid")).append(": ").append(this.database.getString("variantid"));
        }
        if (paramlistRefs.length() > 0) {
            boolean more = this.database.getNext();
            this.throwError("ParamUsed", "VALIDATION", "Parameter(s) cannot be deleted because of " + (more ? "at least" : "") + " the following ParamList references:" + paramlistRefs + (more ? "<br/>..." : ""));
        }
        String sdidataCheck = "SELECT sdidataitem.sdcid, sdidataitem.keyid1, sdidataitem.keyid2, sdidataitem.keyid3 FROM   sdidataitem, rsetitems WHERE  rsetitems.rsetid = ? AND    sdidataitem.paramid = rsetitems.keyid1 ORDER BY 1, 2, 3, 4";
        this.database.createPreparedResultSet(sdidataCheck, new Object[]{rsetid});
        StringBuffer sdidataRefs = new StringBuffer();
        for (int i = 0; i < 10 && this.database.getNext(); ++i) {
            sdidataRefs.append("<br/>").append(this.database.getString("sdcid")).append(": ").append(this.database.getString("keyid1"));
        }
        if (sdidataRefs.length() > 0) {
            boolean more = this.database.getNext();
            this.throwError("ParamUsed", "VALIDATION", "Parameter(s) cannot be deleted because of " + (more ? "at least" : "") + " the following data references:" + sdidataRefs + (more ? "<br/>..." : ""));
        }
        String qcbatchparamsetCheck = "SELECT s_qcbatchparamset.s_qcbatchsampletypeid, s_qcbatchparamset.s_qcbatchparamsetid FROM   s_qcbatchparamset, rsetitems WHERE  rsetitems.rsetid = ? AND    s_qcbatchparamset.paramid = rsetitems.keyid1 ORDER BY 1, 2";
        this.database.createPreparedResultSet(qcbatchparamsetCheck, new Object[]{rsetid});
        StringBuffer qcbatchparamsetRefs = new StringBuffer();
        for (int i = 0; i < 10 && this.database.getNext(); ++i) {
            qcbatchparamsetRefs.append("<br/>").append(this.database.getString("sdcid")).append(": ").append(this.database.getString("keyid1"));
        }
        if (qcbatchparamsetRefs.length() > 0) {
            boolean more = this.database.getNext();
            this.throwError("ParamUsed", "VALIDATION", "Parameter(s) cannot be deleted because of " + (more ? "at least" : "") + " the following QC Batch references:" + qcbatchparamsetRefs + (more ? "<br/>..." : ""));
        }
        String qcmethodsampletypeCheck = "SELECT s_qcmethodsampletypelimit.s_qcmethodsampletypeid, s_qcmethodsampletypelimit.s_qcmethodsampletypelimitid FROM   s_qcmethodsampletypelimit, rsetitems WHERE  rsetitems.rsetid = ? AND    s_qcmethodsampletypelimit.paramid = rsetitems.keyid1 ORDER BY 1, 2";
        this.database.createPreparedResultSet(qcmethodsampletypeCheck, new Object[]{rsetid});
        StringBuffer qcmethodsampletypeRefs = new StringBuffer();
        for (int i = 0; i < 10 && this.database.getNext(); ++i) {
            qcmethodsampletypeRefs.append("<br/>").append(this.database.getString("sdcid")).append(": ").append(this.database.getString("keyid1"));
        }
        if (qcmethodsampletypeRefs.length() > 0) {
            boolean more = this.database.getNext();
            this.throwError("ParamUsed", "VALIDATION", "Parameter(s) cannot be deleted because of " + (more ? "at least" : "") + " the following QC Batch references:" + qcmethodsampletypeRefs + (more ? "<br/>..." : ""));
        }
    }
}

