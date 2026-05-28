/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.actions.paramlist;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.util.OpalUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class SyncCrossSDICalcInfoForParamList
extends BaseAction {
    public static final String ID = "SyncCrossSDICalcInfoForParamList";
    public static final String VERSIONID = "1";
    public static final String PROPERTY_PARAMLISTID = "paramlistid";
    public static final String PROPERTY_PARAMLISTVERSIONID = "paramlistversionid";
    public static final String PROPERTY_PARAMLISTVARAINTID = "variantid";
    private String default_seperator = "";
    private String cmt_changerequestid = "";
    private String cmt_departmentid = "";
    private String cmt_checkedoutby = "";
    private String cmt_changelogstatus = "";
    private Map<String, String> ruleStatusFlagMap;

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        this.default_seperator = properties.getProperty("separator", "~");
        String[] fromSDCprop = StringUtil.split(properties.getProperty("fromsdc"), this.default_seperator);
        String[] paramlistidprop = StringUtil.split(properties.getProperty(PROPERTY_PARAMLISTID), this.default_seperator);
        String[] paramlistversionidprop = StringUtil.split(properties.getProperty(PROPERTY_PARAMLISTVERSIONID), this.default_seperator);
        String[] variantidprop = StringUtil.split(properties.getProperty(PROPERTY_PARAMLISTVARAINTID), this.default_seperator);
        this.loadRuleStatusFlagMap();
        try {
            for (int paramlistcount = 0; paramlistcount < paramlistidprop.length; ++paramlistcount) {
                String paramlistid = paramlistidprop[paramlistcount];
                String paramlistversionid = paramlistversionidprop[paramlistcount];
                String variantid = variantidprop[paramlistcount];
                String fromSDC = fromSDCprop[paramlistcount];
                this.performCMT(paramlistid, paramlistversionid, variantid);
                List<String> calcrule = this.prepareCalcRule(this.fetchAllExistingParamlistItemsForParameterlist(paramlistid, paramlistversionid, variantid));
                if (!OpalUtil.isNotEmpty(calcrule)) continue;
                this.deleteAllExistingParamlistCrossSdiCalc(paramlistid, paramlistversionid, variantid);
                this.populateCrossSdiCalc(fromSDC, paramlistid, paramlistversionid, variantid, calcrule);
            }
        }
        catch (Exception e) {
            throw new SapphireException("PROCESSACTION_FAILED", "SyncCrossSDICalcInfoForParamList " + this.getTranslationProcessor().translate("Action failed:") + " " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
    }

    private void performCMT(String paramlistid, String paramlistversionid, String variantid) {
        String sql = "select * from changelog where linksdcid = 'ParamList' and linkkeyid1 = ? and linkkeyid2 = ? and linkkeyid3 = ?";
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{paramlistid, paramlistversionid, variantid});
        if (OpalUtil.isNotEmpty(ds)) {
            this.cmt_changerequestid = ds.getValue(0, "changerequestid");
            this.cmt_departmentid = ds.getValue(0, "checkedoutbydepartmentid");
            this.cmt_checkedoutby = ds.getValue(0, "checkedoutbyuserid");
            this.cmt_changelogstatus = ds.getValue(0, "changelogstatus");
        }
    }

    private List<String> prepareCalcRule(DataSet paramlistItems) {
        ArrayList<String> newCalcRule = new ArrayList<String>();
        for (int count = 0; count < paramlistItems.size(); ++count) {
            if (!OpalUtil.isNotEmpty(paramlistItems.getValue(count, "calcrule"))) continue;
            newCalcRule.add(paramlistItems.getValue(count, "calcrule"));
        }
        return newCalcRule;
    }

    private void populateCrossSdiCalc(String fromSDC, String paramlistid, String paramlistversionid, String variantid, List<String> listOfCalcRule) throws SapphireException {
        ArrayList<String> calctokens = new ArrayList<String>();
        for (String calrule : listOfCalcRule) {
            calctokens.addAll(Arrays.asList(StringUtil.getExpressionTokens(calrule)));
        }
        Object[] calcrule = calctokens.toArray();
        HashMap<String, List<String>> crossSDICalcMap = new HashMap<String, List<String>>();
        for (int len = 0; len < calcrule.length; ++len) {
            this.populateCrossSDIRuleWithType((String)calcrule[len], crossSDICalcMap);
        }
        if (!crossSDICalcMap.isEmpty()) {
            for (Map.Entry entry : crossSDICalcMap.entrySet()) {
                String crossSDICalcType = (String)entry.getKey();
                List calcrules = (List)entry.getValue();
                switch (crossSDICalcType) {
                    case "link": {
                        this.handleFKRelation(paramlistid, paramlistversionid, variantid, fromSDC, calcrules);
                        break;
                    }
                    case "sdirelation": {
                        this.handleSDIRelation(paramlistid, paramlistversionid, variantid, fromSDC, calcrules);
                        break;
                    }
                    case "reversesdirelation": {
                        this.handleReverseSDIRelation(paramlistid, paramlistversionid, variantid, fromSDC, calcrules);
                        break;
                    }
                    case "sdidatarelation": {
                        this.handleSDIDataRelation(paramlistid, paramlistversionid, variantid, fromSDC, calcrules);
                        break;
                    }
                    case "sample": {
                        this.handleSampleParentChild(paramlistid, paramlistversionid, variantid, fromSDC, calcrules);
                        break;
                    }
                    case "aqc": {
                        this.handleAQC(paramlistid, paramlistversionid, variantid, fromSDC, calcrules);
                        break;
                    }
                    case "sdi": {
                        this.handleAbsolute(paramlistid, paramlistversionid, variantid, fromSDC, calcrules);
                        break;
                    }
                }
            }
        }
    }

    private void populateCrossSDIRuleWithType(String calcrule, Map<String, List<String>> crossSDICalcMap) {
        String[] tokenParts = StringUtil.split(calcrule, "|");
        if (this.isaCrossSDIRule(tokenParts)) {
            String crossSDICalcType = "";
            crossSDICalcType = tokenParts[0].trim().contains(":") ? StringUtil.split(tokenParts[0].trim(), ":")[0].trim().toLowerCase() : "sdidatarelation";
            if (crossSDICalcMap.containsKey(crossSDICalcType)) {
                crossSDICalcMap.get(crossSDICalcType).add(calcrule);
            } else {
                ArrayList<String> calcRules = new ArrayList<String>();
                calcRules.add(calcrule);
                crossSDICalcMap.put(crossSDICalcType, calcRules);
            }
        }
    }

    private boolean isaCrossSDIRule(String[] tokenParts) {
        return tokenParts.length == 3;
    }

    private void handleFKRelation(String paramlistid, String paramlistversionid, String variantid, String fromSDC, List<String> calcrules) throws SapphireException {
        HashMap<CrossSDIInfo, List<String>> crossInfoMap = new HashMap<CrossSDIInfo, List<String>>();
        for (int len = 0; len < calcrules.size(); ++len) {
            CrossSDIInfo crossSDIInfo;
            String[] calcInfo = StringUtil.split(calcrules.get(len), "|");
            String[] linkInfo = StringUtil.split(calcInfo[0], ":");
            if (linkInfo[1].trim().contains(";")) {
                crossSDIInfo = this.getCrossSDIInfoForReverseFK(fromSDC, linkInfo[1]);
                this.populateMap(crossInfoMap, calcInfo, crossSDIInfo);
                continue;
            }
            crossSDIInfo = this.getCrossSDIInfoForFK(fromSDC, linkInfo[1]);
            this.populateMap(crossInfoMap, calcInfo, crossSDIInfo);
        }
        this.callDatabase(paramlistid, paramlistversionid, variantid, crossInfoMap);
    }

    private CrossSDIInfo getCrossSDIInfoForReverseFK(String fromSDC, String linkinfo) {
        String toSDCId = linkinfo.split(";")[0];
        String toSDCColumnId = linkinfo.split(";")[1];
        return new CrossSDIInfo(fromSDC, toSDCId, null, toSDCColumnId, null, "REVERSE_FK", null, null, null);
    }

    private CrossSDIInfo getCrossSDIInfoForFK(String fromSDC, String linkid) throws SapphireException {
        String toSDCId = "";
        String fromSDCColumnId = "";
        CrossSDIInfo crossSDIInfo = null;
        DataSet linksData = this.getSDCProcessor().getLinksData(fromSDC);
        if (OpalUtil.isNotEmpty(linksData)) {
            int foundLinkRow = linksData.findRow("sdccolumnid", linkid);
            if (foundLinkRow < 0) {
                foundLinkRow = linksData.findRow("linkid", linkid);
            }
            if (foundLinkRow < 0) {
                throw new SapphireException("Link:" + linkid + " is not found for ForSDC: " + fromSDC);
            }
            toSDCId = linksData.getValue(foundLinkRow, "linksdcid");
            fromSDCColumnId = linksData.getValue(foundLinkRow, "sdccolumnid");
        } else {
            throw new SapphireException("Link:" + linkid + " is not found for ForSDC: " + fromSDC);
        }
        crossSDIInfo = new CrossSDIInfo(fromSDC, toSDCId, fromSDCColumnId, null, null, "FK", null, null, null);
        return crossSDIInfo;
    }

    private void populateMap(Map<CrossSDIInfo, List<String>> crossInfoMap, String[] calcInfo, CrossSDIInfo crossSDIInfo) {
        if (null != crossInfoMap) {
            if (crossInfoMap.containsKey(crossSDIInfo)) {
                crossInfoMap.get(crossSDIInfo).add("[" + calcInfo[1] + "|" + calcInfo[2] + "]");
            } else {
                ArrayList<String> calcRuleList = new ArrayList<String>();
                calcRuleList.add("[" + calcInfo[1] + "|" + calcInfo[2] + "]");
                crossInfoMap.put(crossSDIInfo, calcRuleList);
            }
        }
    }

    private void handleSDIRelation(String paramlistid, String paramlistversionid, String variantid, String fromSDC, List<String> calcrules) throws SapphireException {
        HashMap<CrossSDIInfo, List<String>> crossInfoMap = new HashMap<CrossSDIInfo, List<String>>();
        for (int len = 0; len < calcrules.size(); ++len) {
            String[] calcInfo = StringUtil.split(calcrules.get(len), "|");
            String[] linkInfo = StringUtil.split(calcInfo[0], ":");
            CrossSDIInfo crossSDIInfo = new CrossSDIInfo(fromSDC, null, null, null, linkInfo[1], linkInfo[0], null, null, null);
            this.populateMap(crossInfoMap, calcInfo, crossSDIInfo);
        }
        this.callDatabase(paramlistid, paramlistversionid, variantid, crossInfoMap);
    }

    private void callDatabase(String paramlistid, String paramlistversionid, String variantid, Map<CrossSDIInfo, List<String>> crossInfoMap) throws SapphireException {
        for (Map.Entry<CrossSDIInfo, List<String>> entry : crossInfoMap.entrySet()) {
            CrossSDIInfo crossSDIInfo = entry.getKey();
            PropertyList actionpropsForCrossSDICalcDef = this.populateActionPropsForCrossSDICalcDef(crossSDIInfo);
            PropertyList propsForParamListInfo = this.populatePropsForParamListInfo(paramlistid, paramlistversionid, variantid, String.join((CharSequence)",", (Iterable<? extends CharSequence>)entry.getValue()));
            this.handleDatabaseOperation(propsForParamListInfo, actionpropsForCrossSDICalcDef);
        }
    }

    private void handleReverseSDIRelation(String paramlistid, String paramlistversionid, String variantid, String fromSDC, List<String> calcrules) throws SapphireException {
        HashMap<CrossSDIInfo, List<String>> crossInfoMap = new HashMap<CrossSDIInfo, List<String>>();
        for (int len = 0; len < calcrules.size(); ++len) {
            String[] calcInfo = StringUtil.split(calcrules.get(len), "|");
            String[] linkInfo = StringUtil.split(calcInfo[0], ":");
            CrossSDIInfo crossSDIInfo = new CrossSDIInfo(fromSDC, null, null, null, linkInfo[1], linkInfo[0], null, null, null);
            this.populateMap(crossInfoMap, calcInfo, crossSDIInfo);
        }
        this.callDatabase(paramlistid, paramlistversionid, variantid, crossInfoMap);
    }

    private void handleSDIDataRelation(String paramlistid, String paramlistversionid, String variantid, String fromSDC, List<String> calcrules) throws SapphireException {
        HashMap<CrossSDIInfo, List<String>> crossInfoMap = new HashMap<CrossSDIInfo, List<String>>();
        for (int len = 0; len < calcrules.size(); ++len) {
            String[] calcInfo = StringUtil.split(calcrules.get(len), "|");
            String[] linkInfo = StringUtil.split(calcInfo[0], ":");
            CrossSDIInfo crossSDIInfo = new CrossSDIInfo(fromSDC, null, null, null, linkInfo[0], "sdidatarelation", null, null, null);
            this.populateMap(crossInfoMap, calcInfo, crossSDIInfo);
        }
        this.callDatabase(paramlistid, paramlistversionid, variantid, crossInfoMap);
    }

    private void handleSampleParentChild(String paramlistid, String paramlistversionid, String variantid, String fromSDC, List<String> calcrules) throws SapphireException {
        HashMap<CrossSDIInfo, List<String>> crossInfoMap = new HashMap<CrossSDIInfo, List<String>>();
        for (int len = 0; len < calcrules.size(); ++len) {
            String[] calcInfo = StringUtil.split(calcrules.get(len), "|");
            String[] linkInfo = StringUtil.split(calcInfo[0], ":");
            CrossSDIInfo crossSDIInfo = new CrossSDIInfo(fromSDC, fromSDC, null, null, "", linkInfo[1], null, null, null);
            this.populateMap(crossInfoMap, calcInfo, crossSDIInfo);
        }
        this.callDatabase(paramlistid, paramlistversionid, variantid, crossInfoMap);
    }

    private void handleAQC(String paramlistid, String paramlistversionid, String variantid, String fromSDC, List<String> calcrules) throws SapphireException {
        HashMap<CrossSDIInfo, List<String>> crossInfoMap = new HashMap<CrossSDIInfo, List<String>>();
        for (int len = 0; len < calcrules.size(); ++len) {
            String[] calcInfo = StringUtil.split(calcrules.get(len), "|");
            String[] linkInfo = StringUtil.split(calcInfo[0], ":");
            String relationtype = "";
            if (linkInfo[1].trim().contains(";")) {
                relationtype = linkInfo[1].split(";")[0];
            }
            CrossSDIInfo crossSDIInfo = new CrossSDIInfo(fromSDC, fromSDC, null, null, relationtype, calcInfo[0], null, null, null);
            this.populateMap(crossInfoMap, calcInfo, crossSDIInfo);
        }
        this.callDatabase(paramlistid, paramlistversionid, variantid, crossInfoMap);
    }

    private void handleAbsolute(String paramlistid, String paramlistversionid, String variantid, String fromSDC, List<String> calcrules) throws SapphireException {
        HashMap<CrossSDIInfo, List<String>> crossInfoMap = new HashMap<CrossSDIInfo, List<String>>();
        for (int len = 0; len < calcrules.size(); ++len) {
            String[] calcInfo = StringUtil.split(calcrules.get(len), "|");
            String[] linkInfo = StringUtil.split(calcInfo[0], ":");
            if (linkInfo[1].trim().contains(";")) {
                String[] sdiinfo = StringUtil.split(linkInfo[1].trim(), ";");
                CrossSDIInfo crossSDIInfo = this.getCrossSDIInfoForAbsolute(fromSDC, sdiinfo);
                this.populateMap(crossInfoMap, calcInfo, crossSDIInfo);
                continue;
            }
            CrossSDIInfo crossSDIInfo = new CrossSDIInfo(fromSDC, fromSDC, null, null, null, "absolute", linkInfo[1], null, null);
            this.populateMap(crossInfoMap, calcInfo, crossSDIInfo);
        }
        this.callDatabase(paramlistid, paramlistversionid, variantid, crossInfoMap);
    }

    private CrossSDIInfo getCrossSDIInfoForAbsolute(String fromSDC, String[] sdiinfo) throws SapphireException {
        String toSDCId = "";
        String tosdc_keyid1 = "";
        String tosdc_keyid2 = "";
        String tosdc_keyid3 = "";
        String sql = "SELECT sdcid FROM sdc WHERE dataentryflag='Y' and sdctype in ('S','U')";
        DataSet dsavailablesdcs = this.getQueryProcessor().getSqlDataSet(sql);
        ArrayList<String> sdclist = new ArrayList<String>();
        if (OpalUtil.isNotEmpty(dsavailablesdcs)) {
            for (int i = 0; i < dsavailablesdcs.size(); ++i) {
                sdclist.add(dsavailablesdcs.getString(i, "sdcid"));
            }
        }
        if (sdclist.contains(sdiinfo[0])) {
            toSDCId = sdiinfo[0];
            if (sdiinfo.length == 4) {
                tosdc_keyid1 = sdiinfo[1];
                tosdc_keyid2 = sdiinfo[2];
                tosdc_keyid3 = sdiinfo[3];
            } else if (sdiinfo.length == 3) {
                tosdc_keyid1 = sdiinfo[1];
                tosdc_keyid2 = sdiinfo[2];
            } else if (sdiinfo.length == 2) {
                tosdc_keyid1 = sdiinfo[1];
            }
        } else {
            toSDCId = fromSDC;
            if (sdiinfo.length == 3) {
                tosdc_keyid1 = sdiinfo[0];
                tosdc_keyid2 = sdiinfo[1];
                tosdc_keyid3 = sdiinfo[2];
            } else if (sdiinfo.length == 2) {
                tosdc_keyid1 = sdiinfo[0];
                tosdc_keyid2 = sdiinfo[1];
            }
        }
        return new CrossSDIInfo(fromSDC, toSDCId, null, null, null, "absolute", tosdc_keyid1, tosdc_keyid2, tosdc_keyid3);
    }

    private PropertyList populatePropsForParamListInfo(String paramlistid, String paramlistversionid, String variantid, String calcruleparams) {
        PropertyList propsForParamInfo = new PropertyList();
        propsForParamInfo.setProperty(PROPERTY_PARAMLISTID, paramlistid);
        propsForParamInfo.setProperty(PROPERTY_PARAMLISTVERSIONID, paramlistversionid);
        propsForParamInfo.setProperty(PROPERTY_PARAMLISTVARAINTID, variantid);
        propsForParamInfo.setProperty("calcruleparams", calcruleparams);
        return propsForParamInfo;
    }

    private PropertyList populateActionPropsForCrossSDICalcDef(CrossSDIInfo crossSDIInfo) throws SapphireException {
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "LV_CrossSDICalcDef");
        props.setProperty("fromsdcid", crossSDIInfo.getFromSDCID());
        props.setProperty("tosdcid", crossSDIInfo.getToSDCID());
        props.setProperty("relationtype", crossSDIInfo.getRelationtype());
        props.setProperty("crosssdicalctype", crossSDIInfo.getCrossSDICalcType());
        props.setProperty("fromsdccolumnid", crossSDIInfo.getFromsdccolumnid());
        props.setProperty("tosdccolumnid", crossSDIInfo.getTosdccolumnid());
        props.setProperty("tokeyid1", crossSDIInfo.getTokeyid1());
        props.setProperty("tokeyid2", crossSDIInfo.getTokeyid2());
        props.setProperty("tokeyid3", crossSDIInfo.getTokeyid3());
        props.setProperty("rulestatusflag", this.determineRuleStatusFlag(crossSDIInfo.getCrossSDICalcType()));
        props.setProperty("crosssdicalcdefdesc", crossSDIInfo.toString());
        props.setProperty("cmtchangerequestid", this.cmt_changerequestid);
        props.setProperty("cmtdepartmentid", this.cmt_departmentid);
        return props;
    }

    private void loadRuleStatusFlagMap() {
        this.ruleStatusFlagMap = new HashMap<String, String>();
        this.ruleStatusFlagMap.put("FK", "fk");
        this.ruleStatusFlagMap.put("REVERSE_FK", "reversefk");
        this.ruleStatusFlagMap.put("sdidatarelation", "sdidatarelation");
        this.ruleStatusFlagMap.put("sdirelation", "sdirelation");
        this.ruleStatusFlagMap.put("reversesdirelation", "reversesdirelation");
        this.ruleStatusFlagMap.put("Parent", "parentchild");
        this.ruleStatusFlagMap.put("Child", "parentchild");
        this.ruleStatusFlagMap.put("Ancestor", "parentchild");
        this.ruleStatusFlagMap.put("Descendant", "parentchild");
        this.ruleStatusFlagMap.put("absolute", "absolute");
        this.ruleStatusFlagMap.put("AQC", "aqc");
    }

    private String determineRuleStatusFlag(String crossSDICalcType) throws SapphireException {
        String rulestatusflag = "";
        PropertyList policy = this.getConfigurationProcessor().getPolicy("DataEntryPolicy", "Sapphire Custom");
        PropertyList crosssdicalcruledefaultsPL = policy.getPropertyList("crosssdicalcruledefaults");
        if (crosssdicalcruledefaultsPL != null) {
            if (crossSDICalcType.startsWith("AQC")) {
                crossSDICalcType = "AQC";
            }
            rulestatusflag = crosssdicalcruledefaultsPL.getProperty(this.ruleStatusFlagMap.get(crossSDICalcType));
        }
        return rulestatusflag;
    }

    private void handleDatabaseOperation(PropertyList propsForParamInfo, PropertyList actionpropsForCrossSDICalcDef) throws SapphireException {
        this.databaseOperationOnCrossCalcDef(propsForParamInfo, actionpropsForCrossSDICalcDef);
        this.databaseOperationOnParamlistCrossSDiCalc(propsForParamInfo);
    }

    private DataSet fetchAllExistingParamlistItemsForParameterlist(String paramlistid, String paramlistversionid, String variantid) {
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("Select paramid, calcrule from paramlistitem ");
        sql.append(" where paramlistid = ").append(safeSQL.addVar(paramlistid));
        sql.append(" and paramlistversionid = ").append(safeSQL.addVar(paramlistversionid));
        sql.append(" and variantid = ").append(safeSQL.addVar(variantid));
        return this.getQueryProcessor().getPreparedSqlDataSet("getAllParamlistItems", sql.toString(), safeSQL.getValues(), true);
    }

    private void deleteAllExistingParamlistCrossSdiCalc(String paramlistid, String paramlistversionid, String variantid) throws SapphireException {
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("Delete from paramlistcrosssdicalc ");
        sql.append(" where paramlistid = ").append(safeSQL.addVar(paramlistid));
        sql.append(" and paramlistversionid = ").append(safeSQL.addVar(paramlistversionid));
        sql.append(" and variantid = ").append(safeSQL.addVar(variantid));
        this.database.executePreparedUpdate(sql.toString(), safeSQL.getValues());
    }

    private void databaseOperationOnCrossCalcDef(PropertyList propsForParamInfo, PropertyList actionpropsForCrossSDICalcDef) throws SapphireException {
        String crosssdicalcdefid = "";
        boolean crossSDICalcpresent = false;
        Map<String, String> existingCrossInfo = this.checkIfSameCrossSDIInformationPresent(actionpropsForCrossSDICalcDef);
        if (existingCrossInfo != null) {
            for (Map.Entry<String, String> entry : existingCrossInfo.entrySet()) {
                crossSDICalcpresent = entry.getKey().equalsIgnoreCase("Y");
                crosssdicalcdefid = entry.getValue();
            }
        }
        if (!crossSDICalcpresent) {
            this.getActionProcessor().processAction("AddSDI", VERSIONID, actionpropsForCrossSDICalcDef);
            crosssdicalcdefid = actionpropsForCrossSDICalcDef.getProperty("newkeyid1");
            PropertyList checkInProps = new PropertyList();
            checkInProps.setProperty("sdcid", "LV_CrossSDICalcDef");
            checkInProps.setProperty("keyid1", crosssdicalcdefid);
            checkInProps.setProperty("notes", "Checking in");
            this.getActionProcessor().processAction("CheckInSDI", VERSIONID, checkInProps);
        }
        propsForParamInfo.setProperty("crosssdicalcdefid", crosssdicalcdefid);
    }

    private Map<String, String> checkIfSameCrossSDIInformationPresent(PropertyList actionpropsForCrossSDICalcDef) throws SapphireException {
        StringBuilder checksql = new StringBuilder();
        HashMap<String, String> crossSDIInfo = null;
        boolean same = false;
        SafeSQL safeSQL = new SafeSQL();
        checksql.append("Select crosssdicalcdefid, crosssdicalctype, fromsdcid, tosdcid, fromsdccolumnid, tosdccolumnid,relationtype,tokeyid1, tokeyid2, tokeyid3 from crosssdicalcdef").append(" WHERE crosssdicalctype = " + safeSQL.addVar(actionpropsForCrossSDICalcDef.getProperty("crosssdicalctype"))).append(" AND fromsdcid ").append(null == actionpropsForCrossSDICalcDef.getProperty("fromsdcid", null) ? " IS NULL " : " = " + safeSQL.addVar(actionpropsForCrossSDICalcDef.getProperty("fromsdcid"))).append(" AND tosdcid ").append(null == actionpropsForCrossSDICalcDef.getProperty("tosdcid", null) ? " IS NULL " : " = " + safeSQL.addVar(actionpropsForCrossSDICalcDef.getProperty("tosdcid"))).append(" AND fromsdccolumnid ").append(null == actionpropsForCrossSDICalcDef.getProperty("fromsdccolumnid", null) ? " IS NULL " : " = " + safeSQL.addVar(actionpropsForCrossSDICalcDef.getProperty("fromsdccolumnid"))).append(" AND tosdccolumnid ").append(null == actionpropsForCrossSDICalcDef.getProperty("tosdccolumnid", null) ? " IS NULL " : " = " + safeSQL.addVar(actionpropsForCrossSDICalcDef.getProperty("tosdccolumnid"))).append(" AND tokeyid1 ").append(null == actionpropsForCrossSDICalcDef.getProperty("tokeyid1", null) ? " IS NULL " : " = " + safeSQL.addVar(actionpropsForCrossSDICalcDef.getProperty("tokeyid1"))).append(" AND tokeyid2 ").append(null == actionpropsForCrossSDICalcDef.getProperty("tokeyid2", null) ? " IS NULL " : " = " + safeSQL.addVar(actionpropsForCrossSDICalcDef.getProperty("tokeyid2"))).append(" AND tokeyid3 ").append(null == actionpropsForCrossSDICalcDef.getProperty("tokeyid3", null) ? " IS NULL " : " = " + safeSQL.addVar(actionpropsForCrossSDICalcDef.getProperty("tokeyid3"))).append(" AND relationtype ").append(null == actionpropsForCrossSDICalcDef.getProperty("relationtype", null) ? " IS NULL " : " = " + safeSQL.addVar(actionpropsForCrossSDICalcDef.getProperty("relationtype")));
        DataSet crossSDIInfoDS = this.getQueryProcessor().getPreparedSqlDataSet("checkCrossSDIInfo", checksql.toString(), safeSQL.getValues());
        if (OpalUtil.isNotEmpty(crossSDIInfoDS)) {
            crossSDIInfo = new HashMap<String, String>();
            crossSDIInfo.put("Y", crossSDIInfoDS.getValue(0, "crosssdicalcdefid"));
        }
        return crossSDIInfo;
    }

    private void databaseOperationOnParamlistCrossSDiCalc(PropertyList propsForParamInfo) throws SapphireException {
        PropertyList actionprops = new PropertyList();
        actionprops.setProperty("sdcid", "ParamList");
        actionprops.setProperty("linkid", "Cross SDI Calc Definitions");
        actionprops.setProperty("keyid1", propsForParamInfo.getProperty(PROPERTY_PARAMLISTID));
        actionprops.setProperty("keyid2", propsForParamInfo.getProperty(PROPERTY_PARAMLISTVERSIONID));
        actionprops.setProperty("keyid3", propsForParamInfo.getProperty(PROPERTY_PARAMLISTVARAINTID));
        actionprops.setProperty("calcruleparams", propsForParamInfo.getProperty("calcruleparams"));
        actionprops.setProperty("crosssdicalcdefid", propsForParamInfo.getProperty("crosssdicalcdefid"));
        actionprops.setProperty("separator", this.default_seperator);
        this.getActionProcessor().processAction("AddSDIDetail", VERSIONID, actionprops);
    }

    class CrossSDIInfo {
        private String fromSDCID;
        private String toSDCID;
        private String fromsdccolumnid;
        private String tosdccolumnid;
        private String relationtype;
        private String crossSDICalcType;
        private String tosdc_keyid1;
        private String tosdc_keyid2;
        private String tosdc_keyid3;

        public CrossSDIInfo(String fromSDCID, String toSDCID, String fromsdccolumnid, String tosdccolumnid, String relationtype, String crossSDICalcType, String tosdc_keyid1, String tosdc_keyid2, String tosdc_keyid3) {
            this.fromSDCID = fromSDCID;
            this.toSDCID = toSDCID;
            this.fromsdccolumnid = fromsdccolumnid;
            this.tosdccolumnid = tosdccolumnid;
            this.relationtype = relationtype;
            this.crossSDICalcType = crossSDICalcType;
            this.tosdc_keyid1 = tosdc_keyid1;
            this.tosdc_keyid2 = tosdc_keyid2;
            this.tosdc_keyid3 = tosdc_keyid3;
        }

        public CrossSDIInfo() {
        }

        public String getFromSDCID() {
            return this.fromSDCID;
        }

        public void setFromSDCID(String fromSDCID) {
            this.fromSDCID = fromSDCID;
        }

        public String getToSDCID() {
            return this.toSDCID;
        }

        public void setToSDCID(String toSDCID) {
            this.toSDCID = toSDCID;
        }

        public String getFromsdccolumnid() {
            return this.fromsdccolumnid;
        }

        public void setFromsdccolumnid(String fromsdccolumnid) {
            this.fromsdccolumnid = fromsdccolumnid;
        }

        public String getTosdccolumnid() {
            return this.tosdccolumnid;
        }

        public void setTosdccolumnid(String tosdccolumnid) {
            this.tosdccolumnid = tosdccolumnid;
        }

        public String getRelationtype() {
            return this.relationtype;
        }

        public void setRelationtype(String relationtype) {
            this.relationtype = relationtype;
        }

        public String getCrossSDICalcType() {
            return this.crossSDICalcType;
        }

        public void setCrossSDICalcType(String crossSDICalcType) {
            this.crossSDICalcType = crossSDICalcType;
        }

        public String getTokeyid1() {
            return this.tosdc_keyid1;
        }

        public void setTokeyid1(String tosdc_keyid1) {
            this.tosdc_keyid1 = tosdc_keyid1;
        }

        public String getTokeyid2() {
            return this.tosdc_keyid2;
        }

        public void setTokeyid2(String tosdc_keyid2) {
            this.tosdc_keyid2 = tosdc_keyid2;
        }

        public String getTokeyid3() {
            return this.tosdc_keyid3;
        }

        public void setTokeyid3(String tosdc_keyid3) {
            this.tosdc_keyid3 = tosdc_keyid3;
        }

        public String toString() {
            StringBuilder val = new StringBuilder();
            val.append("crossSDICalcType='" + this.crossSDICalcType + "'");
            if (OpalUtil.isNotEmpty(this.fromSDCID)) {
                val.append(", fromSDCID='" + this.fromSDCID + "'");
            }
            if (OpalUtil.isNotEmpty(this.toSDCID)) {
                val.append(", toSDCID='" + this.toSDCID + "'");
            }
            if (OpalUtil.isNotEmpty(this.fromsdccolumnid)) {
                val.append(", fromsdccolumnid='" + this.fromsdccolumnid + "'");
            }
            if (OpalUtil.isNotEmpty(this.tosdccolumnid)) {
                val.append(", tosdccolumnid='" + this.tosdccolumnid + "'");
            }
            if (OpalUtil.isNotEmpty(this.relationtype)) {
                val.append(", relationtype='" + this.relationtype + "'");
            }
            if (OpalUtil.isNotEmpty(this.tosdc_keyid1)) {
                val.append(", tosdc_keyid1='" + this.tosdc_keyid1 + "'");
            }
            if (OpalUtil.isNotEmpty(this.tosdc_keyid2)) {
                val.append(", tosdc_keyid2='" + this.tosdc_keyid2 + "'");
            }
            if (OpalUtil.isNotEmpty(this.tosdc_keyid3)) {
                val.append(", tosdc_keyid3='" + this.tosdc_keyid3 + "'");
            }
            return val.toString();
        }

        public int hashCode() {
            int result = 31;
            result = 31 * result + (this.fromSDCID != null ? this.fromSDCID.hashCode() : 0);
            result = 31 * result + (this.toSDCID != null ? this.toSDCID.hashCode() : 0);
            result = 31 * result + (this.fromsdccolumnid != null ? this.fromsdccolumnid.hashCode() : 0);
            result = 31 * result + (this.tosdccolumnid != null ? this.tosdccolumnid.hashCode() : 0);
            result = 31 * result + (this.relationtype != null ? this.relationtype.hashCode() : 0);
            result = 31 * result + (this.crossSDICalcType != null ? this.crossSDICalcType.hashCode() : 0);
            result = 31 * result + (this.tosdc_keyid1 != null ? this.tosdc_keyid1.hashCode() : 0);
            result = 31 * result + (this.tosdc_keyid2 != null ? this.tosdc_keyid2.hashCode() : 0);
            result = 31 * result + (this.tosdc_keyid3 != null ? this.tosdc_keyid3.hashCode() : 0);
            return result;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            CrossSDIInfo crossSDIInfo = (CrossSDIInfo)o;
            if (this.fromSDCID != null ? !this.fromSDCID.equals(crossSDIInfo.fromSDCID) : crossSDIInfo.fromSDCID != null) {
                return false;
            }
            if (this.toSDCID != null ? !this.toSDCID.equals(crossSDIInfo.toSDCID) : crossSDIInfo.toSDCID != null) {
                return false;
            }
            if (this.fromsdccolumnid != null ? !this.fromsdccolumnid.equals(crossSDIInfo.fromsdccolumnid) : crossSDIInfo.fromsdccolumnid != null) {
                return false;
            }
            if (this.tosdccolumnid != null ? !this.tosdccolumnid.equals(crossSDIInfo.tosdccolumnid) : crossSDIInfo.tosdccolumnid != null) {
                return false;
            }
            if (this.crossSDICalcType != null ? !this.crossSDICalcType.equals(crossSDIInfo.crossSDICalcType) : crossSDIInfo.crossSDICalcType != null) {
                return false;
            }
            if (this.tosdc_keyid1 != null ? !this.tosdc_keyid1.equals(crossSDIInfo.tosdc_keyid1) : crossSDIInfo.tosdc_keyid1 != null) {
                return false;
            }
            if (this.tosdc_keyid2 != null ? !this.tosdc_keyid2.equals(crossSDIInfo.tosdc_keyid2) : crossSDIInfo.tosdc_keyid2 != null) {
                return false;
            }
            if (this.tosdc_keyid3 != null ? !this.tosdc_keyid3.equals(crossSDIInfo.tosdc_keyid3) : crossSDIInfo.tosdc_keyid3 != null) {
                return false;
            }
            return this.relationtype != null ? this.relationtype.equals(crossSDIInfo.relationtype) : crossSDIInfo.relationtype == null;
        }
    }
}

