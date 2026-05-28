/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class LV_FormulationMethod
extends BaseSDCRules {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    @Override
    public void preAddDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet paramlists = sdiData.getDataset("formulationmethodparamlist");
        if (paramlists != null) {
            this.setCurrentVersion(paramlists);
        }
    }

    private void setCurrentVersion(DataSet paramlists) {
        for (int i = 0; i < paramlists.size(); ++i) {
            if (!"C".equalsIgnoreCase(paramlists.getValue(i, "paramlistversionid", "C"))) continue;
            paramlists.setValue(i, "paramlistversionid", null);
        }
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        TranslationProcessor tp = this.getTranslationProcessor();
        for (int i = 0; i < primary.getRowCount(); ++i) {
            String formulationMethodId = primary.getValue(i, "formulationmethodid", "");
            if (!this.hasPrimaryValueChanged(primary, i, "recipetypeflag")) continue;
            String oldRecipeType = this.getOldPrimaryValue(primary, i, "recipetypeflag");
            this.database.createPreparedResultSet("getprojects", "select distinct p.formulationprojectid  from formulationproject p, formulationprojectmethod fpm where p.formulationprojectid = fpm.formulationprojectid and fpm.formulationmethodid = ? and p.recipetypeflag = ?", new String[]{formulationMethodId, oldRecipeType});
            DataSet ds = new DataSet(this.database.getResultSet("getprojects"));
            if (ds.getRowCount() <= 0) continue;
            String projects = ds.getColumnValues("formulationprojectid", ", ");
            HashMap<String, String> token = new HashMap<String, String>();
            token.put("methodid", "'" + formulationMethodId + "'");
            token.put("projects", projects);
            token.put("recipetype", "A".equalsIgnoreCase(oldRecipeType) ? "'Absolute'" : "'Proportional'");
            String msg = tp.translate("Formulation method [methodid] is used by [recipetype] Recipe Type Formulation Project(s): [projects].", token);
            this.throwError("Validation Failed", "VALIDATION", msg);
        }
    }

    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }
}

