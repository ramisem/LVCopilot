/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import java.util.Iterator;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class LV_CountRule
extends BaseSDCRules {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public void preAdd(SDIData sdidata, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdidata.getDataset("primary");
        for (int i = 0; i < primary.getRowCount(); ++i) {
            String countRuleType;
            String countRule = primary.getValue(i, "countrule", "");
            String summaryText = this.getCountRuleSummary(countRule, countRuleType = primary.getValue(i, "countruletype", ""));
            if (summaryText == null || summaryText.length() <= 0) continue;
            primary.setValue(i, "notes", summaryText.toString());
        }
    }

    @Override
    public void preEdit(SDIData sdidata, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdidata.getDataset("primary");
        for (int i = 0; i < primary.getRowCount(); ++i) {
            String countRuleType;
            String countRule = primary.getValue(i, "countrule", "");
            String summaryText = this.getCountRuleSummary(countRule, countRuleType = primary.getValue(i, "countruletype", ""));
            if (summaryText == null || summaryText.length() <= 0) continue;
            primary.setValue(i, "notes", summaryText.toString());
        }
    }

    private String getCountRuleSummary(String countRule, String countRuleType) throws SapphireException {
        TranslationProcessor tp = this.getTranslationProcessor();
        StringBuffer summaryText = new StringBuffer();
        if (countRule.length() > 0) {
            if (countRuleType.equalsIgnoreCase("Groovy")) {
                summaryText.append(countRule.substring(3, countRule.length() - 1));
            } else {
                try {
                    JSONObject countRuleJSONObj = new JSONObject(countRule);
                    JSONObject rangeArrayObj = countRuleJSONObj.getJSONObject("rangearray");
                    Iterator it = rangeArrayObj.keys();
                    while (it.hasNext()) {
                        String key = (String)it.next();
                        JSONObject param = (JSONObject)rangeArrayObj.get(key);
                        String noofsdi = param.getString("noofsdi");
                        summaryText.append(tp.translate("Number of Samples is") + " ").append(noofsdi);
                        String operator = param.getString("operator");
                        summaryText.append(" " + tp.translate("if batchsize") + " ").append(operator);
                        String batchsize = param.getString("batchsize");
                        summaryText.append(batchsize);
                        summaryText.append(", ");
                        String string = param.getString("batchunits");
                    }
                    String defaultnumberofsdi = countRuleJSONObj.getString("defaultnumberofsdi");
                    summaryText.append(tp.translate("Otherwise") + " ").append(defaultnumberofsdi);
                }
                catch (JSONException e1) {
                    Logger.logError("error in retrieving count rule summary " + e1.getMessage());
                    throw new SapphireException(e1);
                }
            }
        }
        return summaryText.toString();
    }
}

