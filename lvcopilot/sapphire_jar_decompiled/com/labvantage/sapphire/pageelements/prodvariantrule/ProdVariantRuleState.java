/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.prodvariantrule;

import com.labvantage.sapphire.pageelements.controls.Button;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ProdVariantRuleState
extends BaseElement {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";
    private static final String PROPERTYHANDLERCLASS = "com.labvantage.sapphire.pageelements.prodvariantrule.ProdVariantRuleStatePropertyHandler";
    private static final String JSFILE = "WEB-CORE/elements/prodvariantrule/scripts/prodvariantrulestate.js";
    private static final String JSOBJECT = "pvrstate";
    private static final String JSADDROW = "addNewState";
    private static final String JSREMOVEROW = "removeState";
    static final String IMG_SRC_ADD = "WEB-CORE/images/gif/AddRow.gif";
    private String keyid1;
    private String keyid2;
    private String keyid3;
    private String mode;
    private TranslationProcessor tp = null;
    private static final String IMG_SRC_REMOVE = "WEB-CORE/images/gif/RemoveRow.gif";
    int lastUserSequence = 0;
    int lastUserSeq4LevelRules = 0;
    int lastUserSeq4TransitionRules = 0;

    @Override
    public String getHtml() {
        boolean isLocked;
        Logger.logInfo("ProdVariantRuleState: getHtml(): entered");
        this.tp = this.getTranslationProcessor();
        StringBuffer html = new StringBuffer();
        QueryProcessor qp = this.getQueryProcessor();
        JSONObject prodVariantStates = null;
        this.keyid1 = this.requestContext.getProperty("keyid1");
        this.keyid2 = this.requestContext.getProperty("keyid2");
        this.keyid3 = this.requestContext.getProperty("keyid3");
        this.mode = this.requestContext.getProperty("mode");
        String lockedBy = this.sdiInfo.getDataSet("primary").getString(0, "__lockedby", "");
        boolean bl = isLocked = lockedBy.length() > 0;
        if (isLocked) {
            this.mode = "View";
        }
        html.append("<script language=\"JavaScript\" src=\"").append(JSFILE).append("\"></script>\n");
        html.append("\n<input type=\"hidden\" name=\"__propertyhandler_" + this.elementid + "\" value=\"" + PROPERTYHANDLERCLASS + "\" />");
        html.append("\n<input type=\"hidden\" name=\"__prodvariantstaterule_elementid\" id=\"prodvariantstaterule_elementid\" value=\"" + this.elementid + "\" />");
        html.append("\n<input type=\"hidden\" name=\"__" + this.elementid + "_keyid1\" value=\"" + this.keyid1 + "\" />");
        html.append("\n<input type=\"hidden\" name=\"__" + this.elementid + "_keyid2\" value=\"" + this.keyid2 + "\" />");
        html.append("\n<input type=\"hidden\" name=\"__" + this.elementid + "_keyid3\" value=\"" + this.keyid3 + "\" />");
        html.append("<table class='gridmaint_table' cellspacing=0 width=\"100%\" id=\"").append(this.elementid).append("_table\" border=1 >");
        html.append("<tr  class='gridmaint_tablehead'>");
        html.append("<td  class='gridmaint_fieldtitle'><input type=\"checkbox\" onclick=\"pvrstate.selectAllRows(this)\"></td>");
        html.append("<td class='gridmaint_fieldtitle'><b>").append(this.tp.translate("State")).append("</b></td>");
        html.append("<td  class='gridmaint_fieldtitle'><b>").append(this.tp.translate("Level Rule")).append("</b></td>");
        html.append("<td  class='gridmaint_fieldtitle'><b>").append(this.tp.translate("Transition Rule")).append("</b></td>");
        html.append("<td  class='gridmaint_fieldtitle'><b>").append(this.tp.translate("Initial State")).append("</b></td>");
        html.append("</tr>");
        try {
            prodVariantStates = this.loadData(this.keyid1, html, qp);
        }
        catch (SapphireException e) {
            this.logger.error("Error in retrieving the states and level data from database for the prodvariant rule: " + e.getMessage());
            html.append("<font color=\"red\">").append(this.tp.translate("Error in retrieving the element data from database") + " ").append("</font>");
        }
        html.append("</table>");
        if (!this.mode.equals("View")) {
            this.renderButtons(html);
        }
        html.append("<script language=\"JavaScript\">\n");
        html.append(JSOBJECT).append(".sElementId = '").append(this.elementid).append("';\n");
        html.append(" var " + this.elementid + "_jsonObj = " + prodVariantStates.toString() + "; \n");
        html.append(" var jsonObjInDB = " + prodVariantStates.toString() + "; \n");
        html.append(" var lastUserSequence = " + this.lastUserSequence + "; \n");
        html.append("</script>");
        html.append("\n<input type=\"hidden\" name=\"__" + this.elementid + "_jsonString\" id=\"__" + this.elementid + "_jsonString\" value='' />");
        return html.toString();
    }

    private JSONObject loadData(String prodvariantruleid, StringBuffer html, QueryProcessor qp) throws SapphireException {
        String intialStateId = this.getInitialStateForGivenPVRId(prodvariantruleid);
        this.logger.info("Getting states for the prodvariantruleid : " + prodvariantruleid);
        JSONObject prodVariantStates = new JSONObject();
        JSONArray prodVarStatesArr = new JSONArray();
        try {
            DataSet ds = this.getStatesForGivenPVRId(prodvariantruleid);
            SafeSQL safeSQL = new SafeSQL();
            for (int i = 0; i < ds.size(); ++i) {
                JSONObject prodVariantRule = new JSONObject();
                String stateforPVR = ds.getValue(i, "s_stateid");
                String usersequence = ds.getValue(i, "usersequence");
                prodVariantRule.put("Rowid", i);
                prodVariantRule.put("State", stateforPVR);
                String state = stateforPVR;
                String sql4St = "SELECT  refdisplayvalue  FROM refvalue where ( activeflag='Y' OR activeflag is null) AND reftypeid = 'ProdVariantState' and refvalueid = " + safeSQL.addVar(stateforPVR.replaceAll("'", "''"));
                DataSet stDS = qp.getPreparedSqlDataSet(sql4St, safeSQL.getValues());
                safeSQL.reset();
                if (stDS.size() > 0 && stDS.getValue(0, "refdisplayvalue", "").length() > 0) {
                    state = stDS.getValue(0, "refdisplayvalue", "");
                }
                html.append("<tr >");
                String cbId = this.elementid + "_cb_" + i;
                html.append("<td><input type=\"checkbox\" id=\"").append(cbId).append("\"></td>");
                String stateFieldCol = this.elementid + "_stateCol_" + i;
                html.append("<td >");
                String stateFieldId = this.elementid + "_state_" + i;
                html.append("<font size=\"1\" face=\"Verdana\" id=\"").append(stateFieldCol).append("\">").append(this.tp.translate(state)).append("</font>");
                html.append("</td > ");
                this.renderLevelRules4State(prodvariantruleid, stateforPVR, qp, html, i, prodVariantRule);
                this.renderTransitionRules4State(prodvariantruleid, stateforPVR, qp, html, i, prodVariantRule);
                String rbId = this.elementid + "_rb_" + i;
                if (!this.mode.equals("View")) {
                    if (stateforPVR.equals(intialStateId)) {
                        html.append("<td><input type=\"radio\" checked name=\"initialStateRB\" id=\"").append(rbId).append("\" onclick=\"pvrstate.updateJSONString(this)\"></td>");
                        prodVariantRule.put("IsInitial", "true");
                    } else {
                        html.append("<td><input type=\"radio\" name=\"initialStateRB\" id=\"").append(rbId).append("\" onclick=\"pvrstate.updateJSONString(this)\"></td>");
                        prodVariantRule.put("IsInitial", "false");
                    }
                } else if (stateforPVR.equals(intialStateId)) {
                    html.append("<td><input type=\"radio\" checked name=\"initialStateRB\" disabled id=\"").append(rbId).append("\" onclick=\"pvrstate.updateJSONString(this)\"></td>");
                    prodVariantRule.put("IsInitial", "true");
                } else {
                    html.append("<td><input type=\"radio\" name=\"initialStateRB\" disabled  id=\"").append(rbId).append("\" onclick=\"pvrstate.updateJSONString(this)\"></td>");
                    prodVariantRule.put("IsInitial", "false");
                }
                prodVariantRule.put("ActionFlag", "U");
                prodVariantRule.put("UserSequence", usersequence);
                this.lastUserSequence = Integer.parseInt(usersequence);
                html.append("</tr>");
                prodVarStatesArr.put(prodVariantRule);
            }
            prodVariantStates.put("prodVariantStates", prodVarStatesArr);
        }
        catch (JSONException e) {
            Logger.logError("loadData: " + e.getMessage());
            throw new SapphireException(e);
        }
        return prodVariantStates;
    }

    private void renderLevelRules4State(String prodvariantruleid, String stateforPVR, QueryProcessor qp, StringBuffer html, int rowIndex, JSONObject prodVariantRule) throws SapphireException {
        String cellid = this.elementid + "_Level_" + rowIndex;
        html.append("<td id=\"").append(cellid).append("\" width=\"300px\">");
        String summaryTextId = this.elementid + "_summarytext_" + rowIndex;
        html.append("<span id=\"").append(summaryTextId).append("\" style=\"width:260px; float:left\" >");
        DataSet rulesDS = this.getLevlRulesForGivenPVRState(stateforPVR, prodvariantruleid, qp);
        JSONArray rulesArr = new JSONArray();
        JSONObject allrules = new JSONObject();
        StringBuffer temp = new StringBuffer();
        try {
            int counter = 0;
            for (int i = 0; i < rulesDS.size(); ++i) {
                JSONObject ruleobj = new JSONObject();
                String ruletype = rulesDS.getValue(i, "levelruletype");
                String ruledef = rulesDS.getValue(i, "levelrule");
                String level = rulesDS.getValue(i, "levellabel");
                String ruleno = rulesDS.getValue(i, "s_levelruleid");
                ruleobj.put("level", level);
                ruleobj.put("ruleno", ruleno);
                ruleobj.put("ruletype", ruletype);
                ruleobj.put("data", new JSONObject(ruledef));
                ruleobj.put("usersequence", ruleno);
                ruleobj.put("actionflag", "U");
                rulesArr.put(ruleobj);
                String className = ruletype + "LevelRuleProcessor";
                Method summaryMethod = (Method)this.getSummaryMethod(className, ruletype, "Level").get("summaryMethod");
                if (ruletype.equalsIgnoreCase("Otherwise")) {
                    temp.append((String)summaryMethod.invoke(this.getSummaryMethod(className, ruletype, "Level").get("object"), ruleobj.toString(), qp, this.tp));
                    continue;
                }
                if (i > 0) {
                    html.append("<br>");
                }
                html.append(++counter).append((String)summaryMethod.invoke(this.getSummaryMethod(className, ruletype, "Level").get("object"), ruleobj.toString(), qp, this.tp));
            }
            if (counter > 0) {
                html.append("<br>");
            }
            if (temp.length() > 0) {
                html.append(++counter).append(temp);
            }
            allrules.put("rules", rulesArr);
            prodVariantRule.put("LevelRule", allrules);
        }
        catch (Exception e) {
            Logger.logError("renderLevelRules4State(): " + e.getMessage());
            throw new SapphireException(e);
        }
        html.append("</span>");
        if (!this.mode.equals("View")) {
            html.append("<input type=\"button\" title=\"Define Rule\" style=\"vertical-align: top; float:right\" value=\"...\" onclick=\"pvrstate.openRuleEditor(this, 'Level')\" >");
        }
        html.append("</td>");
    }

    private void renderTransitionRules4State(String prodvariantruleid, String stateforPVR, QueryProcessor qp, StringBuffer html, int rowIndex, JSONObject prodVariantRule) throws SapphireException {
        String cellid = this.elementid + "_Transition_" + rowIndex;
        html.append("<td id=\"").append(cellid).append("\" width=\"300px\">");
        String summaryTextId = this.elementid + "_trsummarytext_" + rowIndex;
        html.append("<span id=\"").append(summaryTextId).append("\" style=\"width:260px; float:left\" >");
        DataSet rulesDS = this.getTransitionRulesForGivenPVRState(stateforPVR, prodvariantruleid, qp);
        JSONArray rulesArr = new JSONArray();
        JSONObject allrules = new JSONObject();
        try {
            for (int i = 0; i < rulesDS.size(); ++i) {
                JSONObject ruleobj = new JSONObject();
                String ruletype = rulesDS.getValue(i, "transitionruletype");
                String ruledef = rulesDS.getValue(i, "transitionrule");
                String transitionId = rulesDS.getValue(i, "nextstateid");
                String ruleno = rulesDS.getValue(i, "s_transitionruleid");
                ruleobj.put("state", transitionId);
                ruleobj.put("ruleno", ruleno);
                ruleobj.put("ruletype", ruletype);
                ruleobj.put("autoflag", rulesDS.getValue(i, "autoflag"));
                ruleobj.put("usersequence", ruleno);
                ruleobj.put("actionflag", "U");
                ruleobj.put("data", new JSONObject(ruledef));
                rulesArr.put(ruleobj);
                String className = ruletype + "TransitionRuleProcessor";
                Method summaryMethod = (Method)this.getSummaryMethod(className, ruletype, "Transition").get("summaryMethod");
                if (i > 0) {
                    html.append("<br>");
                }
                html.append(i + 1 + (String)summaryMethod.invoke(this.getSummaryMethod(className, ruletype, "Transition").get("object"), ruleobj.toString(), qp, this.tp));
            }
            allrules.put("rules", rulesArr);
            prodVariantRule.put("TransitionRule", allrules);
        }
        catch (Exception e) {
            Logger.logError("renderTransitionRules4State(): " + e.getMessage());
            throw new SapphireException(e);
        }
        html.append("</span>");
        if (!this.mode.equals("View")) {
            html.append("<input type=\"button\" title=\"Define Rule\" style=\"vertical-align: top; float:right\" value=\"...\" onclick=\"pvrstate.openRuleEditor(this, 'Transition')\" >");
        }
        html.append("</td>");
    }

    private DataSet getStatesForGivenPVRId(String prodvariantruleid) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT s_stateid, usersequence  FROM s_pvrstate WHERE s_prodvariantruleid = ? order by usersequence");
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{prodvariantruleid});
        return ds;
    }

    private String getInitialStateForGivenPVRId(String prodvariantruleid) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT initialstateid  FROM s_prodvariantrule WHERE s_prodvariantruleid = ? order by usersequence");
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{prodvariantruleid});
        return ds.getValue(0, "initialstateid");
    }

    private DataSet getLevlRulesForGivenPVRState(String stateid, String prodvariantruleid, QueryProcessor qp) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT levellabel, s_levelruleid, levelruletype, levelrule FROM s_pvrlevelrule WHERE s_prodvariantruleid = ? and s_stateid = ? order by usersequence");
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{prodvariantruleid, stateid});
        return ds;
    }

    private DataSet getTransitionRulesForGivenPVRState(String stateid, String prodvariantruleid, QueryProcessor qp) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT nextstateid, s_transitionruleid, transitionruletype, transitionrule, autoflag FROM s_pvrtransitionrule WHERE s_prodvariantruleid = ? and s_stateid = ? order by usersequence");
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{prodvariantruleid, stateid});
        return ds;
    }

    private void renderButtons(StringBuffer html) {
        String img = IMG_SRC_ADD;
        String tip = "Add";
        String appearance = "standard";
        String margin = "";
        String style = "";
        String width = "";
        this.logger.debug("renderButtons called...");
        html.append("<table cellspacing=\"0\" cellpadding=\"1\" border=\"0\" align=right>\n<tr>\n");
        html.append("<td nowrap>\n");
        Button button = new Button(this.pageContext);
        button.setId("add");
        button.setText(this.tp.translate("Add"));
        button.setImg(img);
        button.setTip(tip);
        button.setAppearance(appearance);
        button.setMargin(margin);
        button.setStyle(style);
        button.setWidth(width);
        button.setAction("pvrstate.addNewState()");
        html.append(button.getHtml());
        html.append("</td>\n");
        html.append("<td nowrap>\n");
        button = new Button(this.pageContext);
        button.setId("remove");
        button.setText(this.tp.translate("Remove"));
        button.setImg(IMG_SRC_REMOVE);
        button.setTip("Remove Rule");
        button.setAppearance(appearance);
        button.setMargin(margin);
        button.setStyle(style);
        button.setWidth(width);
        button.setAction("pvrstate.removeState()");
        html.append(button.getHtml());
        html.append("</td>\n");
        html.append("</tr>\n</table>\n");
    }

    private String getCustomRuleClassName(String rule, String ruletype) throws SapphireException {
        String className = "";
        PropertyList policy = this.getConfigurationProcessor().getPolicy("SamplingPlanPolicy", "Sapphire Custom");
        if (policy != null) {
            PropertyListCollection customRules = policy.getCollectionNotNull("customrules");
            for (int i = 0; i < customRules.size(); ++i) {
                PropertyList ruleDef = customRules.getPropertyList(i);
                String ruleTypeValueFromPolicy = ruleDef.getProperty("ruletype");
                String ruleFromPolicy = ruleDef.getProperty("rule");
                if (!ruleTypeValueFromPolicy.equals(ruletype) || ruleFromPolicy.indexOf(rule) == -1) continue;
                className = ruleDef.getProperty("classname");
                break;
            }
        }
        return className;
    }

    private HashMap getSummaryMethod(String className, String ruletype, String rule) throws SapphireException {
        Class<?> processorclass = null;
        HashMap<String, Method> summaryMethodObjHM = new HashMap<String, Method>();
        try {
            try {
                processorclass = Class.forName("com.labvantage.sapphire.pages.prodvariantruleeditor." + className);
            }
            catch (ClassNotFoundException e) {
                try {
                    processorclass = Class.forName(this.getCustomRuleClassName(rule, ruletype));
                }
                catch (ClassNotFoundException e1) {
                    Logger.logError("Could not retrieve the custom class for the rule type " + ruletype, e.getMessage());
                    throw new SapphireException(e1);
                }
                catch (SapphireException e1) {
                    Logger.logError(e1.getMessage());
                    throw e1;
                }
            }
            Constructor<?> c = processorclass.getConstructor(new Class[0]);
            Object o = c.newInstance(new Object[0]);
            Class[] partypes = new Class[]{String.class, QueryProcessor.class, TranslationProcessor.class};
            summaryMethodObjHM.put("summaryMethod", processorclass.getMethod("getSummary", partypes));
            summaryMethodObjHM.put("object", (Method)o);
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
        return summaryMethodObjHM;
    }

    @Override
    public boolean isVisibleInAddMode() {
        return true;
    }

    private static class UserMessages {
        static final String STATEHEADER = "State";
        static final String LEVELRULEHEADER = "Level Rule";
        static final String TRANSITIONRULEHEADER = "Transition Rule";
        static final String INITIALSTATEHEADER = "Initial State";
        static final String ADDBUTTON = "Add";
        static final String REMOVEBUTTON = "Remove";

        private UserMessages() {
        }
    }
}

