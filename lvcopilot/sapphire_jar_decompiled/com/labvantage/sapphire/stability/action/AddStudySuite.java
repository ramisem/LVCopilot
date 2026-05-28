/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.stability.action;

import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseAction;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class AddStudySuite
extends BaseAction
implements sapphire.action.AddStudySuite {
    private static final String STUDYSDC = "StudySDC";
    private static final String STUDYID = "studyid";
    private static final String STUDYSUITEID = "studysuiteid";
    private static final String ACTIONNAME_STUDYSUITE = "createStudySuite";
    private static final String ACTIONNAME_STUDYCOPY = "copyStudy";
    private TranslationProcessor Tp;
    private static final boolean DEBUG = false;

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        this.Tp = this.getTranslationProcessor();
        String sdcid = properties.getProperty("sdcid", "");
        String keyid1 = properties.getProperty("keyid1", "");
        String templateid = properties.getProperty("templateid", "");
        String studysuitestatus = properties.getProperty("studysuitestatus", "");
        String autokeyflagstudysuite = properties.getProperty("autokeyflagstudysuite", "");
        String autokeyflagstudy = properties.getProperty("autokeyflagstudy", "");
        String message = new String("");
        PropertyList propsStudySuite = new PropertyList();
        String newStudySuiteId = new String("");
        int sizeOfStudyDataSet = 0;
        PropertyList propsStudy = new PropertyList();
        if (sdcid.equalsIgnoreCase("")) {
            message = "ERROR:SDCID not specified.";
            throw new SapphireException("INVALID_PROPERTY", this.Tp.translate(message));
        }
        if (autokeyflagstudysuite.equalsIgnoreCase("false") && keyid1.equalsIgnoreCase("")) {
            message = "ERROR:New KeyId not specified as the Auto generation Key is OFF for sdcid";
            throw new SapphireException("INVALID_PROPERTY", this.Tp.translate(message));
        }
        try {
            ActionProcessor actionprocesor = this.getActionProcessor();
            ActionBlock actionblockAdd = new ActionBlock();
            ActionBlock actionblockEdit = new ActionBlock();
            if (templateid.length() > 0) {
                if (autokeyflagstudysuite.equalsIgnoreCase("false")) {
                    propsStudySuite.put("sdcid", sdcid);
                    propsStudySuite.put("keyid1", keyid1);
                    propsStudySuite.put("templateid", templateid);
                    propsStudySuite.put("studysuitestatus", studysuitestatus);
                    actionblockAdd.setAction(ACTIONNAME_STUDYSUITE, "AddSDI", "1");
                    actionblockAdd.setActionProperties(ACTIONNAME_STUDYSUITE, propsStudySuite);
                } else {
                    propsStudySuite.put("sdcid", sdcid);
                    propsStudySuite.put("templateid", templateid);
                    propsStudySuite.put("studysuitestatus", studysuitestatus);
                    actionblockAdd.setAction(ACTIONNAME_STUDYSUITE, "AddSDI", "1");
                    actionblockAdd.setActionProperties(ACTIONNAME_STUDYSUITE, propsStudySuite);
                }
            } else if (autokeyflagstudysuite.equalsIgnoreCase("false")) {
                propsStudySuite.put("sdcid", sdcid);
                propsStudySuite.put("keyid1", keyid1);
                propsStudySuite.put("studysuitestatus", studysuitestatus);
                actionblockAdd.setAction(ACTIONNAME_STUDYSUITE, "AddSDI", "1");
                actionblockAdd.setActionProperties(ACTIONNAME_STUDYSUITE, propsStudySuite);
            } else {
                propsStudySuite.put("sdcid", sdcid);
                propsStudySuite.put("studysuitestatus", studysuitestatus);
                actionblockAdd.setAction(ACTIONNAME_STUDYSUITE, "AddSDI", "1");
                actionblockAdd.setActionProperties(ACTIONNAME_STUDYSUITE, propsStudySuite);
            }
            actionprocesor.processActionBlock(actionblockAdd);
            newStudySuiteId = actionblockAdd.getActionProperty(ACTIONNAME_STUDYSUITE, "newkeyid1");
            if (templateid.length() > 0 && autokeyflagstudy.equalsIgnoreCase("true")) {
                StringBuffer sbSql = new StringBuffer();
                SafeSQL safeSQL = new SafeSQL();
                sbSql.append(" select studyid from study where studysuiteid = ");
                sbSql.append(safeSQL.addVar(templateid));
                QueryProcessor qp = this.getQueryProcessor();
                DataSet dsStudy = qp.getPreparedSqlDataSet(sbSql.toString(), safeSQL.getValues());
                sizeOfStudyDataSet = dsStudy.size();
                String studyid = new String("");
                if (sizeOfStudyDataSet > 0) {
                    for (int counter = 0; counter < sizeOfStudyDataSet; ++counter) {
                        studyid = dsStudy.getValue(counter, STUDYID);
                        propsStudy.clear();
                        propsStudy.put("sdcid", STUDYSDC);
                        propsStudy.put("templateid", studyid);
                        propsStudy.put(STUDYSUITEID, newStudySuiteId);
                        actionblockEdit.setAction(ACTIONNAME_STUDYCOPY + counter, "AddStudy", "1");
                        actionblockEdit.setActionProperties(ACTIONNAME_STUDYCOPY + counter, propsStudy);
                    }
                }
                actionprocesor.processActionBlock(actionblockEdit);
                Object var21_23 = null;
            }
            properties.put("newkeyid1", newStudySuiteId);
        }
        catch (Exception e) {
            message = "ERROR:Exception generated in invoking AddStudySuite=>" + e.getMessage();
            throw new SapphireException("PROCESSACTION_FAILED", this.Tp.translate(message), e);
        }
    }
}

