/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pages.prodvariantruleeditor;

import com.labvantage.sapphire.pages.prodvariantruleeditor.RuleTypeProcessor;
import java.util.Calendar;
import java.util.HashMap;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;

public class OtherwiseLevelRuleProcessor
implements RuleTypeProcessor {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public String getEditor(QueryProcessor qp, TranslationProcessor tp) {
        return "";
    }

    @Override
    public String getSummary(String ruledefinition, QueryProcessor qp, TranslationProcessor tp) throws SapphireException {
        StringBuffer summarytext = new StringBuffer();
        try {
            JSONObject json = new JSONObject(ruledefinition);
            String ruleno = json.getString("ruleno");
            String level = json.getString("level");
            SafeSQL safeSQL = new SafeSQL();
            String sql = "SELECT  refdisplayvalue  FROM refvalue where ( activeflag='Y' OR activeflag is null) AND reftypeid = 'ProdVariantRuleLevel' and refvalueid = " + safeSQL.addVar(level);
            DataSet ds = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
            String levelDisplayValue = ds.getValue(0, "refdisplayvalue");
            summarytext.append(tp.translate(". otherwise Set Level to ")).append("<font size=\"1\" face=\"Verdana\" color=\"blue\">").append(tp.translate(levelDisplayValue)).append(" ").append("</font>");
        }
        catch (JSONException e) {
            throw new SapphireException(e);
        }
        return summarytext.toString();
    }

    @Override
    public boolean evaluateRule(String levelid, String ruledefinition, String sortingColumn, String sdcid, QueryProcessor qp, TranslationProcessor tp, HashMap prodVarColValMap, Calendar lastTransitionDate, String dbms) throws SapphireException {
        return true;
    }

    private static class UserMessages {
        static final String OTHERWISESETLEVELTO = ". otherwise Set Level to ";

        private UserMessages() {
        }
    }
}

