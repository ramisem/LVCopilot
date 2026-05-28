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

public class DeleteStudySuite
extends BaseAction
implements sapphire.action.DeleteStudySuite {
    private static final String STUDYSDC = "StudySDC";
    private static final String STUDYID = "studyid";
    private static final String STUDYSUITEID = "studysuiteid";
    private static final String ACTIONNAME_DELETESTUDYSUITE = "deleteStudySuite";
    private static final String ACTIONNAME_STUDYOPERATION = "studyOperation";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        TranslationProcessor Tp = this.getTranslationProcessor();
        String sdcid = properties.getProperty("sdcid", "");
        String keyid1 = properties.getProperty("keyid1", "");
        String studydeleteflag = properties.getProperty("studydeleteflag", "Y");
        String message = "";
        PropertyList propsStudySuite = new PropertyList();
        PropertyList propsStudy = new PropertyList();
        QueryProcessor qpStudy = this.getQueryProcessor();
        StringBuffer studyids = new StringBuffer("");
        if (sdcid.equalsIgnoreCase("")) {
            message = "ERROR:SDCID not specified.";
            throw new SapphireException("INVALID_PROPERTY", Tp.translate(message));
        }
        if (keyid1.equalsIgnoreCase("")) {
            message = "ERROR:KeyId not specified for sdcid";
            throw new SapphireException("INVALID_PROPERTY", Tp.translate(message));
        }
        SafeSQL safeSQL = new SafeSQL();
        String sqlStudy = "select studyid from study where studysuiteid = " + safeSQL.addVar(keyid1);
        DataSet dsStudy = qpStudy.getPreparedSqlDataSet(sqlStudy, safeSQL.getValues());
        if (dsStudy.size() > 0) {
            for (int i = 0; i < dsStudy.size(); ++i) {
                studyids.append(dsStudy.getValue(i, STUDYID));
                studyids.append(";");
            }
        }
        try {
            ActionProcessor actionprocesor = this.getActionProcessor();
            ActionBlock actionblockDelete = new ActionBlock();
            propsStudy.put("sdcid", STUDYSDC);
            if (studydeleteflag.equalsIgnoreCase("N")) {
                if (studyids.toString().length() > 0) {
                    propsStudy.put("keyid1", studyids.toString());
                    propsStudy.put(STUDYSUITEID, "(null)");
                    actionblockDelete.setAction(ACTIONNAME_STUDYOPERATION, "EditSDI", "1");
                    actionblockDelete.setActionProperties(ACTIONNAME_STUDYOPERATION, propsStudy);
                }
            } else if (studydeleteflag.equalsIgnoreCase("Y") && studyids.toString().length() > 0) {
                propsStudy.put("keyid1", studyids.toString());
                actionblockDelete.setAction(ACTIONNAME_STUDYOPERATION, "DeleteSDI", "1");
                actionblockDelete.setActionProperties(ACTIONNAME_STUDYOPERATION, propsStudy);
            }
            propsStudySuite.put("sdcid", sdcid);
            propsStudySuite.put("keyid1", keyid1);
            actionblockDelete.setAction(ACTIONNAME_DELETESTUDYSUITE, "DeleteSDI", "1");
            actionblockDelete.setActionProperties(ACTIONNAME_DELETESTUDYSUITE, propsStudySuite);
            actionprocesor.processActionBlock(actionblockDelete);
        }
        catch (Exception e) {
            message = "ERROR:Exception generated in invoking DeleteStudySuite=>" + e.getMessage();
            throw new SapphireException("PROCESSACTION_FAILED", Tp.translate(message), e);
        }
    }
}

