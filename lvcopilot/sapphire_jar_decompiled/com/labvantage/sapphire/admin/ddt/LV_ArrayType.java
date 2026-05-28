/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class LV_ArrayType
extends BaseSDCRules {
    @Override
    public void preAdd(SDIData sdidata, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdidata.getDataset("primary");
        for (int i = 0; i < primary.size(); ++i) {
            if (!"Y".equals(primary.getString(i, "aslflag")) || "(system)".equals(primary.getString(i, "createby"))) continue;
            throw new SapphireException(this.getTranslationProcessor().translate("Invalid Array Type Operation"), "VALIDATION", this.getTranslationProcessor().translate("User is not allowed to create ASL Array Types. ASL Array Types are managed internally by Application."));
        }
    }

    @Override
    public void preEdit(SDIData sdidata, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdidata.getDataset("primary");
        for (int i = 0; i < primary.size(); ++i) {
            if (!"Y".equals(primary.getString(i, "aslflag")) || "(system)".equals(primary.getString(i, "modby"))) continue;
            throw new SapphireException(this.getTranslationProcessor().translate("Invalid Array Type Operation"), "VALIDATION", this.getTranslationProcessor().translate("User is not allowed to edit ASL Array Types. ASL Array Types are managed internally by Application."));
        }
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        if (this.database.getPreparedCount("select count(arraytypeid) from arraytype where aslflag = 'Y' and arraytypeid in (select r.keyid1 from rsetitems r where r.rsetid = ?)", new String[]{rsetid}) > 0 && !this.connectionInfo.getSysuserId().equals("(system)")) {
            throw new SapphireException(this.getTranslationProcessor().translate("Invalid Array Type Operation"), "VALIDATION", this.getTranslationProcessor().translate("User is not allowed to delete ASL Array Types.") + "<br>" + this.getTranslationProcessor().translate("ASL Array Types are managed internally by Application."));
        }
    }
}

