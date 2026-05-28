/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.prodvariantrule;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.pageelements.PropertyHandler;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.xml.PropertyList;

public class ProdVariantRuleStatePropertyHandler
extends PropertyHandler {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";
    private static final String SDCID = "LV_ProdVariantRule";
    DataSet levelRuleToBeAdded = new DataSet();
    DataSet levelRuleToBeDeleted = new DataSet();
    DataSet levelRuleToBeUpdated = new DataSet();
    DataSet transitionRuleToBeAdded = new DataSet();
    DataSet transitionRuleToBeDeleted = new DataSet();
    DataSet transitionRuleToBeUpdated = new DataSet();

    public DataSet getLevelRuleToBeAdded() {
        return this.levelRuleToBeAdded;
    }

    public void setLevelRuleToBeAdded(DataSet levelRuleToBeAdded) {
        this.levelRuleToBeAdded = levelRuleToBeAdded;
    }

    public DataSet getLevelRuleToBeDeleted() {
        return this.levelRuleToBeDeleted;
    }

    public DataSet getTransitionRuleToBeAdded() {
        return this.transitionRuleToBeAdded;
    }

    public void setTransitionRuleToBeAdded(DataSet transitionRuleToBeAdded) {
        this.transitionRuleToBeAdded = transitionRuleToBeAdded;
    }

    public DataSet getTransitionRuleToBeDeleted() {
        return this.transitionRuleToBeDeleted;
    }

    public void setTransitionRuleToBeDeleted(DataSet transitionRuleToBeDeleted) {
        this.transitionRuleToBeDeleted = transitionRuleToBeDeleted;
    }

    public DataSet getTransitionRuleToBeUpdated() {
        return this.transitionRuleToBeUpdated;
    }

    public void setTransitionRuleToBeUpdated(DataSet transitionRuleToBeUpdated) {
        this.transitionRuleToBeUpdated = transitionRuleToBeUpdated;
    }

    public void setLevelRuleToBeDeleted(DataSet levelRuleToBeDeleted) {
        this.levelRuleToBeDeleted = levelRuleToBeDeleted;
    }

    public DataSet getLevelRuleToBeUpdated() {
        return this.levelRuleToBeUpdated;
    }

    public void setLevelRuleToBeUpdated(DataSet levelRuleToBeUpdated) {
        this.levelRuleToBeUpdated = levelRuleToBeUpdated;
    }

    @Override
    public void processProperties(HashMap props) throws SapphireException {
        Logger.logInfo(this.getClass().getName(), "ProdVariantRuleStatePropertyHandler: processProperties(): ");
        PropertyList pl = new PropertyList(props);
        TranslationProcessor tp = new TranslationProcessor(this.sapphireConnection.getConnectionId());
        ActionProcessor ap = new ActionProcessor(this.getConnectionInfo().getConnectionId());
        String elementId = pl.getProperty("__prodvariantstaterule_elementid", "");
        String jsonStr = pl.getProperty("__" + elementId + "_jsonString");
        if (jsonStr.length() > 0) {
            DBUtil db = new DBUtil();
            db.setConnection(this.sapphireConnection);
            String keyid1 = pl.getProperty("__" + elementId + "_keyid1", "");
            try {
                JSONObject json = new JSONObject(jsonStr);
                JSONArray prodVariantStates = json.getJSONArray("prodVariantStates");
                DataSet statesToBeAddded = new DataSet();
                this.createDataSetColumns(statesToBeAddded);
                DataSet statesToBeDeleted = new DataSet();
                this.createDataSetColumns(statesToBeDeleted);
                try {
                    HashMap addDeleteDSHM = this.getStatesDataSetFromJSONString(prodVariantStates, statesToBeAddded, statesToBeDeleted, tp);
                    statesToBeAddded = (DataSet)addDeleteDSHM.get("add");
                    statesToBeDeleted = (DataSet)addDeleteDSHM.get("delete");
                }
                catch (Exception e) {
                    Logger.logStackTrace(e);
                    throw new SapphireException("ProdVariantRuleStatePropertyHandler", tp.translate("Failed to get required datasets from json string.") + " " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())));
                }
                if (statesToBeAddded.size() > 0) {
                    Logger.logInfo(this.getClass().getName(), " Adding the following States DataSet : " + statesToBeAddded.toString() + " for prodvariantruleid:  " + keyid1);
                    this.addStatesToDB(statesToBeAddded, keyid1, ap, tp);
                }
                if (this.getLevelRuleToBeAdded().size() > 0) {
                    Logger.logInfo(this.getClass().getName(), " Adding the following Level Rules DataSet : " + this.getLevelRuleToBeAdded().toString() + " for prodvariantruleid:  " + keyid1);
                    this.addLevelRulesToDB(this.getLevelRuleToBeAdded(), keyid1, db, ap, tp);
                }
                if (this.getTransitionRuleToBeAdded().size() > 0) {
                    Logger.logInfo(this.getClass().getName(), " Adding the following Transition Rules DataSet : " + this.getTransitionRuleToBeAdded().toString() + " for prodvariantruleid:  " + keyid1);
                    this.addTransitionRulesToDB(this.getTransitionRuleToBeAdded(), keyid1, ap, tp, db);
                }
                if (this.getLevelRuleToBeDeleted().size() > 0) {
                    Logger.logInfo(this.getClass().getName(), " Deleting the following Level Rules DataSet : " + this.getLevelRuleToBeDeleted().toString() + " for prodvariantruleid:  " + keyid1);
                    this.deleteLevelRulesFromDB(this.getLevelRuleToBeDeleted(), keyid1, db, tp);
                }
                if (this.getTransitionRuleToBeDeleted().size() > 0) {
                    Logger.logInfo(this.getClass().getName(), " Deleting the following Transition Rules DataSet : " + this.getTransitionRuleToBeDeleted().toString() + " for prodvariantruleid:  " + keyid1);
                    this.deleteTransitionRulesFromDB(this.getTransitionRuleToBeDeleted(), keyid1, db, tp);
                }
                if (statesToBeDeleted.size() > 0) {
                    Logger.logInfo(this.getClass().getName(), " Deleting the following States DataSet : " + statesToBeDeleted.toString() + " for prodvariantruleid:  " + keyid1);
                    this.deleteStatesNRulesFromDB(statesToBeDeleted, keyid1, db, ap);
                }
                if (this.getLevelRuleToBeUpdated().size() > 0) {
                    Logger.logInfo(this.getClass().getName(), " Updating the following Level Rules DataSet : " + this.getLevelRuleToBeUpdated().toString() + " for prodvariantruleid:  " + keyid1);
                    this.updateLevelRulesInDB(this.getLevelRuleToBeUpdated(), keyid1, db, tp);
                }
                if (this.getTransitionRuleToBeUpdated().size() > 0) {
                    Logger.logInfo(this.getClass().getName(), " Updating the following Transition Rules DataSet : " + this.getLevelRuleToBeUpdated().toString() + " for prodvariantruleid:  " + keyid1);
                    this.updateTransitionRulesInDB(this.getTransitionRuleToBeUpdated(), keyid1, db, tp);
                }
                this.findNUpdateInitialStateInPVR(keyid1, prodVariantStates, db, statesToBeDeleted);
            }
            catch (Exception e) {
                Logger.logStackTrace(e);
                throw new SapphireException(e);
            }
        }
    }

    private void updateTransitionRulesInDB(DataSet transitionRuleToBeUpdated, String prodVariantRuleId, DBUtil db, TranslationProcessor tp) throws SapphireException {
        try {
            StringBuffer query = new StringBuffer();
            query.append("UPDATE s_pvrtransitionrule SET ");
            query.append("transitionruletype=? , ");
            query.append("transitionrule= ? , ");
            query.append("nextstateid= ? , ");
            query.append("autoflag= ? WHERE  ");
            query.append("s_prodvariantruleid =? AND ").append("s_stateid =? AND ").append("s_transitionruleid =? ");
            PreparedStatement updateRuleState = db.prepareStatement("updateRules", query.toString());
            for (int i = 0; i < transitionRuleToBeUpdated.size(); ++i) {
                updateRuleState.setString(1, transitionRuleToBeUpdated.getValue(i, "transitionruletype"));
                updateRuleState.setString(2, transitionRuleToBeUpdated.getValue(i, "transitionrule"));
                updateRuleState.setString(3, transitionRuleToBeUpdated.getValue(i, "nextstateid"));
                updateRuleState.setString(4, transitionRuleToBeUpdated.getValue(i, "autoflag"));
                updateRuleState.setString(5, prodVariantRuleId);
                updateRuleState.setString(6, transitionRuleToBeUpdated.getValue(i, "s_stateid"));
                updateRuleState.setString(7, transitionRuleToBeUpdated.getValue(i, "s_transitionruleid"));
                updateRuleState.executeUpdate();
            }
        }
        catch (Exception e) {
            Logger.logStackTrace(e);
            throw new SapphireException("ProdVariantRuleStatePropertyHandler", tp.translate("Failed to update level rules") + " " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())));
        }
    }

    private void deleteTransitionRulesFromDB(DataSet transitionRuleToBeDeleted, String prodVariantRuleId, DBUtil db, TranslationProcessor tp) throws SapphireException {
        try {
            StringBuffer query = new StringBuffer();
            query.append("DELETE FROM s_pvrtransitionrule WHERE ");
            query.append("s_prodvariantruleid =? AND ").append("s_stateid =?  AND ").append("s_transitionruleid = ?");
            PreparedStatement deleteState = db.prepareStatement("deleteTRules", query.toString());
            for (int i = 0; i < transitionRuleToBeDeleted.size(); ++i) {
                deleteState.setString(1, prodVariantRuleId);
                deleteState.setString(2, transitionRuleToBeDeleted.getValue(i, "s_stateid"));
                deleteState.setString(3, transitionRuleToBeDeleted.getValue(i, "s_transitionruleid"));
                deleteState.executeUpdate();
            }
        }
        catch (Exception e) {
            Logger.logStackTrace(e);
            throw new SapphireException("ProdVariantRuleStatePropertyHandler", tp.translate("Failed to delete transition rules") + " " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())));
        }
    }

    private void addTransitionRulesToDB(DataSet transitionRuleToBeAdded, String prodVariantRuleId, ActionProcessor ap, TranslationProcessor tp, DBUtil db) throws SapphireException {
        transitionRuleToBeAdded.sort("s_stateid");
        ArrayList<DataSet> groupedDS = transitionRuleToBeAdded.getGroupedDataSets("s_stateid");
        for (int i = 0; i < groupedDS.size(); ++i) {
            DataSet stateDS = groupedDS.get(i);
            String stateid = stateDS.getValue(0, "s_stateid");
            try {
                HashMap<String, Object> props = new HashMap<String, Object>();
                props.put("sdcid", SDCID);
                props.put("linkid", "state");
                props.put("detaillinkid", "transition");
                props.put("keyid1", prodVariantRuleId);
                props.put("usersequence", stateDS.getColumnValues("usersequence", ";"));
                props.put("keyid2", stateid);
                props.put("s_transitionruleid", stateDS.getColumnValues("usersequence", ";"));
                props.put("nextstateid", stateDS.getColumnValues("nextstateid", ";"));
                props.put("transitionruletype", stateDS.getColumnValues("transitionruletype", ";"));
                props.put("transitionrule", stateDS.getColumnValues("transitionrule", ";"));
                props.put("autoflag", stateDS.getColumnValues("autoflag", ";"));
                props.put("createdt", DateTimeUtil.getNowTimestamp());
                props.put("createtool", this.connectionInfo.getTool());
                props.put("createby", this.connectionInfo.getSysuserId());
                ap.processAction("AddSDIDetail", "1", props);
                continue;
            }
            catch (ActionException e) {
                throw new SapphireException("ProdVariantRuleStatePropertyHandler", tp.translate("Failed to insert transition rules.") + " " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())));
            }
        }
    }

    private void updateLevelRulesInDB(DataSet levelRuleToBeUpdated, String prodVariantRuleId, DBUtil db, TranslationProcessor tp) throws SapphireException {
        try {
            StringBuffer query = new StringBuffer();
            query.append("UPDATE s_pvrlevelrule SET ");
            query.append("levelruletype=? , ");
            query.append("levellabel=? , ");
            query.append("levelrule= ? WHERE ");
            query.append("s_prodvariantruleid =? AND ").append("s_stateid =? AND ").append("s_levelruleid =?  ");
            PreparedStatement updateRuleState = db.prepareStatement("updateRules", query.toString());
            for (int i = 0; i < levelRuleToBeUpdated.size(); ++i) {
                updateRuleState.setString(1, levelRuleToBeUpdated.getValue(i, "levelruletype"));
                updateRuleState.setString(2, levelRuleToBeUpdated.getValue(i, "levellabel"));
                updateRuleState.setString(3, levelRuleToBeUpdated.getValue(i, "levelrule"));
                updateRuleState.setString(4, prodVariantRuleId);
                updateRuleState.setString(5, levelRuleToBeUpdated.getValue(i, "s_stateid"));
                updateRuleState.setString(6, levelRuleToBeUpdated.getValue(i, "s_levelruleid"));
                updateRuleState.executeUpdate();
            }
        }
        catch (Exception e) {
            Logger.logStackTrace(e);
            throw new SapphireException("ProdVariantRuleStatePropertyHandler", tp.translate("Failed to update level rules") + " " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())));
        }
    }

    private void deleteLevelRulesFromDB(DataSet levelRuleToBeDeleted, String prodVariantRuleId, DBUtil db, TranslationProcessor tp) throws SapphireException {
        try {
            StringBuffer query = new StringBuffer();
            query.append("DELETE FROM s_pvrlevelrule WHERE ");
            query.append("s_prodvariantruleid =? AND ").append("s_stateid =?  AND ").append("s_levelruleid = ?");
            PreparedStatement deleteState = db.prepareStatement("deleteTRules", query.toString());
            for (int i = 0; i < levelRuleToBeDeleted.size(); ++i) {
                deleteState.setString(1, prodVariantRuleId);
                deleteState.setString(2, levelRuleToBeDeleted.getValue(i, "s_stateid"));
                deleteState.setString(3, levelRuleToBeDeleted.getValue(i, "s_levelruleid"));
                deleteState.executeUpdate();
            }
        }
        catch (Exception e) {
            Logger.logStackTrace(e);
            throw new SapphireException("ProdVariantRuleStatePropertyHandler", tp.translate("Failed to delete level rules") + " " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())));
        }
    }

    private void addLevelRulesToDB(DataSet levelRuleToBeAdded, String prodVariantRuleId, DBUtil db, ActionProcessor ap, TranslationProcessor tp) throws SapphireException {
        levelRuleToBeAdded.sort("s_stateid");
        ArrayList<DataSet> groupedDS = levelRuleToBeAdded.getGroupedDataSets("s_stateid");
        for (int i = 0; i < groupedDS.size(); ++i) {
            DataSet stateDS = groupedDS.get(i);
            String stateid = stateDS.getValue(0, "s_stateid");
            try {
                HashMap<String, Object> props = new HashMap<String, Object>();
                props.put("sdcid", SDCID);
                props.put("linkid", "state");
                props.put("detaillinkid", "level");
                props.put("keyid1", prodVariantRuleId);
                props.put("usersequence", stateDS.getColumnValues("usersequence", ";"));
                props.put("keyid2", stateid);
                props.put("s_levelruleid", stateDS.getColumnValues("usersequence", ";"));
                props.put("levellabel", stateDS.getColumnValues("levellabel", ";"));
                props.put("levelruletype", stateDS.getColumnValues("levelruletype", ";"));
                props.put("levelrule", stateDS.getColumnValues("levelrule", ";"));
                props.put("createdt", DateTimeUtil.getNowTimestamp());
                props.put("createtool", this.connectionInfo.getTool());
                props.put("createby", this.connectionInfo.getSysuserId());
                ap.processAction("AddSDIDetail", "1", props);
                continue;
            }
            catch (ActionException e) {
                throw new SapphireException("ProdVariantRuleStatePropertyHandler", tp.translate("Failed to insert states") + " " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())));
            }
        }
    }

    private DataSet createDataSetColumns4LevelRules(DataSet ds) {
        ds.addColumn("s_levelruleid", 0);
        ds.addColumn("levellabel", 0);
        ds.addColumn("levelruletype", 0);
        ds.addColumn("levelrule", 0);
        ds.addColumn("usersequence", 0);
        ds.addColumn("s_stateid", 0);
        return ds;
    }

    private void addStatesToDB(DataSet statesToBeAddded, String prodVariantRuleId, ActionProcessor ap, TranslationProcessor tp) throws SapphireException {
        try {
            HashMap<String, Object> props = new HashMap<String, Object>();
            props.put("sdcid", SDCID);
            props.put("linkid", "state");
            props.put("keyid1", prodVariantRuleId);
            props.put("usersequence", statesToBeAddded.getColumnValues("usersequence", ";"));
            props.put("s_stateid", statesToBeAddded.getColumnValues("s_stateid", ";"));
            props.put("createdt", DateTimeUtil.getNowTimestamp());
            props.put("createtool", this.connectionInfo.getTool());
            props.put("createby", this.connectionInfo.getSysuserId());
            ap.processAction("AddSDIDetail", "1", props);
        }
        catch (ActionException e) {
            Logger.logStackTrace(e);
            throw new SapphireException("ProdVariantRuleStatePropertyHandler", tp.translate("Failed to insert states") + " " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())));
        }
    }

    private DataSet createDataSetColumns(DataSet ds) {
        ds.addColumn("s_stateid", 0);
        ds.addColumn("usersequence", 0);
        return ds;
    }

    private HashMap getStatesDataSetFromJSONString(JSONArray prodVariantStatesArr, DataSet statesToBeAdded, DataSet statesToBeDeleted, TranslationProcessor tp) throws Exception {
        HashMap<String, DataSet> addDeleteDataSets = new HashMap<String, DataSet>();
        for (int i = 0; i < prodVariantStatesArr.length(); ++i) {
            JSONObject row = (JSONObject)prodVariantStatesArr.get(i);
            String state = row.getString("State");
            String actionFlag = row.getString("ActionFlag");
            if (actionFlag.equals("A")) {
                int newRow = statesToBeAdded.addRow();
                statesToBeAdded.setValue(newRow, "s_stateid", state);
                statesToBeAdded.setValue(newRow, "usersequence", row.getString("UserSequence"));
            } else if (actionFlag.equals("D")) {
                int newRow = statesToBeDeleted.addRow();
                statesToBeDeleted.setValue(newRow, "s_stateid", state);
                statesToBeDeleted.setValue(newRow, "usersequence", row.getString("UserSequence"));
            }
            JSONObject levelrule = row.getJSONObject("LevelRule");
            JSONArray levelRulesArr = levelrule.getJSONArray("rules");
            this.filterLevelRulesArrByActionFlag(levelRulesArr, state, tp);
            JSONObject transitionrule = row.getJSONObject("TransitionRule");
            JSONArray transitionRulesArr = transitionrule.getJSONArray("rules");
            this.filterTransitionRulesArrByActionFlag(transitionRulesArr, state, tp);
        }
        addDeleteDataSets.put("add", statesToBeAdded);
        addDeleteDataSets.put("delete", statesToBeDeleted);
        return addDeleteDataSets;
    }

    private void filterTransitionRulesArrByActionFlag(JSONArray transitionRulesArr, String state, TranslationProcessor tp) throws SapphireException {
        try {
            this.createDataSetColumns4TransitionRules(this.getTransitionRuleToBeAdded());
            this.createDataSetColumns4TransitionRules(this.getTransitionRuleToBeUpdated());
            this.createDataSetColumns4TransitionRules(this.getTransitionRuleToBeDeleted());
            for (int i = 0; i < transitionRulesArr.length(); ++i) {
                int newRow;
                DataSet ds;
                JSONObject row = (JSONObject)transitionRulesArr.get(i);
                if (!row.keys().hasNext()) continue;
                String actionflag = row.getString("actionflag");
                if (actionflag.equals("A")) {
                    ds = this.getTransitionRuleToBeAdded();
                    newRow = ds.addRow();
                    ds.setValue(newRow, "nextstateid", row.getString("state"));
                    ds.setValue(newRow, "s_transitionruleid", row.getString("ruleno"));
                    ds.setValue(newRow, "transitionruletype", row.getString("ruletype"));
                    ds.setValue(newRow, "transitionrule", row.getString("data"));
                    ds.setValue(newRow, "autoflag", row.getString("autoflag"));
                    ds.setValue(newRow, "usersequence", row.getString("usersequence"));
                    ds.setValue(newRow, "s_stateid", state);
                    continue;
                }
                if (actionflag.equals("D")) {
                    ds = this.getTransitionRuleToBeDeleted();
                    newRow = ds.addRow();
                    ds.setValue(newRow, "nextstateid", row.getString("state"));
                    ds.setValue(newRow, "s_transitionruleid", row.getString("ruleno"));
                    ds.setValue(newRow, "transitionruletype", row.getString("ruletype"));
                    ds.setValue(newRow, "transitionrule", row.getString("data"));
                    ds.setValue(newRow, "autoflag", row.getString("autoflag"));
                    ds.setValue(newRow, "usersequence", row.getString("usersequence"));
                    ds.setValue(newRow, "s_stateid", state);
                    continue;
                }
                if (!actionflag.equals("E")) continue;
                ds = this.getTransitionRuleToBeUpdated();
                newRow = ds.addRow();
                ds.setValue(newRow, "nextstateid", row.getString("state"));
                ds.setValue(newRow, "s_transitionruleid", row.getString("ruleno"));
                ds.setValue(newRow, "transitionruletype", row.getString("ruletype"));
                ds.setValue(newRow, "transitionrule", row.getString("data"));
                ds.setValue(newRow, "autoflag", row.getString("autoflag"));
                ds.setValue(newRow, "usersequence", row.getString("usersequence"));
                ds.setValue(newRow, "s_stateid", state);
            }
        }
        catch (JSONException e) {
            Logger.logStackTrace(e);
            throw new SapphireException("ProdVariantRuleStatePropertyHandler", tp.translate("Failed to get required datasets from json string.") + " " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())));
        }
    }

    private DataSet createDataSetColumns4TransitionRules(DataSet ds) {
        ds.addColumn("s_transitionruleid", 0);
        ds.addColumn("nextstateid", 0);
        ds.addColumn("transitionruletype", 0);
        ds.addColumn("transitionrule", 0);
        ds.addColumn("usersequence", 0);
        ds.addColumn("s_stateid", 0);
        ds.addColumn("autoflag", 0);
        return ds;
    }

    private void filterLevelRulesArrByActionFlag(JSONArray levelRulesArr, String state, TranslationProcessor tp) throws SapphireException {
        try {
            this.createDataSetColumns4LevelRules(this.getLevelRuleToBeAdded());
            this.createDataSetColumns4LevelRules(this.getLevelRuleToBeUpdated());
            this.createDataSetColumns4LevelRules(this.getLevelRuleToBeDeleted());
            for (int i = 0; i < levelRulesArr.length(); ++i) {
                int newRow;
                DataSet ds;
                JSONObject row = (JSONObject)levelRulesArr.get(i);
                if (!row.keys().hasNext()) continue;
                String actionflag = row.getString("actionflag");
                if (actionflag.equals("A")) {
                    ds = this.getLevelRuleToBeAdded();
                    newRow = ds.addRow();
                    ds.setValue(newRow, "levellabel", row.getString("level"));
                    ds.setValue(newRow, "s_levelruleid", row.getString("ruleno"));
                    ds.setValue(newRow, "levelruletype", row.getString("ruletype"));
                    ds.setValue(newRow, "levelrule", row.getString("data"));
                    ds.setValue(newRow, "usersequence", row.getString("usersequence"));
                    ds.setValue(newRow, "s_stateid", state);
                    continue;
                }
                if (actionflag.equals("D")) {
                    ds = this.getLevelRuleToBeDeleted();
                    newRow = ds.addRow();
                    ds.setValue(newRow, "levellabel", row.getString("level"));
                    ds.setValue(newRow, "s_levelruleid", row.getString("ruleno"));
                    ds.setValue(newRow, "levelruletype", row.getString("ruletype"));
                    ds.setValue(newRow, "levelrule", row.getString("data"));
                    ds.setValue(newRow, "usersequence", row.getString("usersequence"));
                    ds.setValue(newRow, "s_stateid", state);
                    continue;
                }
                if (!actionflag.equals("E")) continue;
                ds = this.getLevelRuleToBeUpdated();
                newRow = ds.addRow();
                ds.setValue(newRow, "levellabel", row.getString("level"));
                ds.setValue(newRow, "s_levelruleid", row.getString("ruleno"));
                ds.setValue(newRow, "levelruletype", row.getString("ruletype"));
                ds.setValue(newRow, "levelrule", row.getString("data"));
                ds.setValue(newRow, "usersequence", row.getString("usersequence"));
                ds.setValue(newRow, "s_stateid", state);
            }
        }
        catch (JSONException e) {
            Logger.logStackTrace(e);
            throw new SapphireException("ProdVariantRuleStatePropertyHandler", tp.translate("Failed to get required datasets from json string.") + " " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())));
        }
    }

    private JSONArray convertToJSONArray(JSONObject obj) throws SapphireException {
        JSONArray array = new JSONArray();
        try {
            Iterator it = obj.keys();
            TreeMap<String, Object> map = new TreeMap<String, Object>();
            while (it.hasNext()) {
                String key = (String)it.next();
                int index = Integer.parseInt(key);
                map.put(key, obj.get(key));
            }
            for (String key : map.keySet()) {
                array.put(map.get(key));
            }
        }
        catch (JSONException e) {
            Logger.logStackTrace(e);
            throw new SapphireException(e);
        }
        return array;
    }

    private void findNUpdateInitialStateInPVR(String prodVariantRuleId, JSONArray prodVariantStatesArr, DBUtil db, DataSet statesToBeDeleted) throws SapphireException {
        try {
            boolean donotSetStateToNull = false;
            for (int i = 0; i < prodVariantStatesArr.length(); ++i) {
                JSONObject row = (JSONObject)prodVariantStatesArr.get(i);
                if (statesToBeDeleted.getColumnValues("s_stateid", ";").indexOf(row.getString("State")) != -1 || !row.getString("IsInitial").equals("true")) continue;
                donotSetStateToNull = true;
                String intialState = row.getString("State");
                Logger.logInfo(this.getClass().getName(), " Updating the initial state of the following prodvariantrule : " + prodVariantRuleId + " to state:  " + intialState);
                String sql = "UPDATE s_prodvariantrule SET initialstateid=? WHERE s_prodvariantruleid = ?";
                db.executePreparedUpdate(sql, new Object[]{intialState, prodVariantRuleId});
                break;
            }
            if (!donotSetStateToNull) {
                String sql = "UPDATE s_prodvariantrule SET initialstateid= null WHERE s_prodvariantruleid = ?";
                db.executePreparedUpdate(sql, new Object[]{prodVariantRuleId});
            }
        }
        catch (JSONException e) {
            Logger.logStackTrace(e);
            throw new SapphireException(e);
        }
    }

    private void deleteStatesNRulesFromDB(DataSet statesToBeRemovedFromDB, String prodVariantRuleId, DBUtil db, ActionProcessor ap) throws SapphireException {
        try {
            StringBuffer query = new StringBuffer();
            query.append("DELETE FROM s_pvrlevelrule WHERE ");
            query.append("s_prodvariantruleid =? AND ").append("s_stateid =?   ");
            PreparedStatement deleteLR = db.prepareStatement("deleteLRules", query.toString());
            for (int i = 0; i < statesToBeRemovedFromDB.size(); ++i) {
                deleteLR.setString(1, prodVariantRuleId);
                deleteLR.setString(2, statesToBeRemovedFromDB.getValue(i, "s_stateid"));
                deleteLR.executeUpdate();
            }
            query = new StringBuffer();
            query.append("DELETE FROM s_pvrtransitionrule WHERE ");
            query.append("s_prodvariantruleid =? AND ").append("s_stateid =?  ");
            PreparedStatement deleteTRules = db.prepareStatement("deleteTRules", query.toString());
            for (int i = 0; i < statesToBeRemovedFromDB.size(); ++i) {
                deleteTRules.setString(1, prodVariantRuleId);
                deleteTRules.setString(2, statesToBeRemovedFromDB.getValue(i, "s_stateid"));
                deleteTRules.executeUpdate();
            }
            StringBuffer stateIds = new StringBuffer();
            for (int i = 0; i < statesToBeRemovedFromDB.size(); ++i) {
                stateIds.append(";" + statesToBeRemovedFromDB.getValue(i, "s_stateid"));
            }
            if (stateIds.length() > 0) {
                PropertyList deleteStateProps = new PropertyList();
                deleteStateProps.setProperty("linkid", "state");
                deleteStateProps.setProperty("sdcid", SDCID);
                deleteStateProps.setProperty("keyid1", prodVariantRuleId);
                deleteStateProps.setProperty("s_stateid", stateIds.substring(1));
                ap.processAction("DeleteSDIDetail", "1", deleteStateProps);
            }
        }
        catch (Exception e) {
            Logger.logStackTrace(e);
            throw new SapphireException(e);
        }
    }
}

