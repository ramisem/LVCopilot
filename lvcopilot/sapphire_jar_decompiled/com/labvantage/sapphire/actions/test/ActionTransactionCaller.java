/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.test;

import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.action.BaseAction;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class ActionTransactionCaller
extends BaseAction {
    public static final String NOTHING = "nothing";
    public static final String PROPERTY_MODE = "mode";
    public static final String UPDATESAMPLE1 = "updatesample1";
    public static final String UPDATESAMPLE1_THROWEXCEPTION = "updatesample1_throwexception";
    public static final String UPDATESAMPLE1_NESTEDUPDATESAMPLE2 = "updatesample1_nestedupdatesample2";
    public static final String UPDATESAMPLE1_NESTEDUPDATESAMPLE2_EXCEPTIONSAMPLE1 = "updatesample1_nestedupdatesample2_exceptionsample1";
    public static final String UPDATESAMPLE1_NESTEDUPDATESAMPLE2_THROWEXCEPTION_SUPPRESSEXCEPTION = "updatesample1_nestedupdatesample2_throwexception_suppressexception";
    public static final String UPDATESAMPLE1_NESTEDUPDATESAMPLE2_THROWEXCEPTION_RETHROWEXCEPTION = "updatesample1_nestedupdatesample2_throwexception_rethrowexception";
    public static final String DEFERREDCONSTRAINTSAMPLE1 = "deferredconstraintsample1";
    public static final String NESTEDDEFERREDCONSTRAINTSUPRESSED = "nesteddeferredconstraintsupressed";
    public static final String NESTEDDEFERREDCONSTRAINTRETHROWN = "nesteddeferredconstraintrethrown";
    public static final String DEFERREDCONSTRAINTSAMPLE1_WITHNESTEDUPDATE = "deferredconstraintsample1_withnestedupdate";
    public static final String PROPERTY_SAMPLEID = "sample1";
    public static final String PROPERTY_SAMPLEDESC = "sampledesc";
    public static final String PROPERTY_NESTEDSAMPLEID = "nestedsampleid";
    public static final String PROPERTY_NESTEDSAMPLEDESC = "nestedsampledesc";
    public static final String PROPERTY_PROJECTID = "projectid";
    public static final String PROPERTY_NEWTRANS = "newtrans";
    public static final String RETURN_NEWSAMPLEDESC = "newsampledesc";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String mode = properties.getProperty(PROPERTY_MODE);
        String sampleid = properties.getProperty(PROPERTY_SAMPLEID);
        String nestedSampleid = properties.getProperty(PROPERTY_NESTEDSAMPLEID);
        String sampleDesc = properties.getProperty(PROPERTY_SAMPLEDESC);
        String nestedSampleDesc = properties.getProperty(PROPERTY_NESTEDSAMPLEDESC);
        String projectid = properties.getProperty(PROPERTY_PROJECTID);
        boolean newTrans = properties.getProperty(PROPERTY_NEWTRANS).equals("Y");
        String sql = "UPDATE s_sample SET sampledesc = ? WHERE s_sampleid=?";
        this.database.executePreparedUpdate(sql, new Object[]{sampleDesc, sampleid});
        if (!mode.equals(UPDATESAMPLE1)) {
            if (mode.equals(UPDATESAMPLE1_THROWEXCEPTION)) {
                throw new ActionException("oops");
            }
            if (mode.equals(UPDATESAMPLE1_NESTEDUPDATESAMPLE2)) {
                PropertyList nestedProps = new PropertyList();
                nestedProps.setProperty(PROPERTY_SAMPLEID, nestedSampleid);
                nestedProps.setProperty(PROPERTY_SAMPLEDESC, nestedSampleDesc);
                nestedProps.setProperty(PROPERTY_MODE, UPDATESAMPLE1);
                this.getActionProcessor().processActionClass(ActionTransactionCaller.class.getName(), nestedProps, newTrans);
            } else {
                if (mode.equals(UPDATESAMPLE1_NESTEDUPDATESAMPLE2_EXCEPTIONSAMPLE1)) {
                    PropertyList nestedProps = new PropertyList();
                    nestedProps.setProperty(PROPERTY_SAMPLEID, nestedSampleid);
                    nestedProps.setProperty(PROPERTY_SAMPLEDESC, nestedSampleDesc);
                    nestedProps.setProperty(PROPERTY_MODE, UPDATESAMPLE1);
                    this.getActionProcessor().processActionClass(ActionTransactionCaller.class.getName(), nestedProps, newTrans);
                    throw new ActionException("oops");
                }
                if (mode.equals(UPDATESAMPLE1_NESTEDUPDATESAMPLE2_THROWEXCEPTION_SUPPRESSEXCEPTION)) {
                    PropertyList nestedProps = new PropertyList();
                    nestedProps.setProperty(PROPERTY_SAMPLEID, nestedSampleid);
                    nestedProps.setProperty(PROPERTY_SAMPLEDESC, nestedSampleDesc);
                    nestedProps.setProperty(PROPERTY_MODE, UPDATESAMPLE1_THROWEXCEPTION);
                    try {
                        this.getActionProcessor().processActionClass(ActionTransactionCaller.class.getName(), nestedProps, newTrans);
                    }
                    catch (ActionException actionException) {}
                } else if (mode.equals(UPDATESAMPLE1_NESTEDUPDATESAMPLE2_THROWEXCEPTION_RETHROWEXCEPTION)) {
                    PropertyList nestedProps = new PropertyList();
                    nestedProps.setProperty(PROPERTY_SAMPLEID, nestedSampleid);
                    nestedProps.setProperty(PROPERTY_SAMPLEDESC, nestedSampleDesc);
                    nestedProps.setProperty(PROPERTY_MODE, UPDATESAMPLE1_THROWEXCEPTION);
                    this.getActionProcessor().processActionClass(ActionTransactionCaller.class.getName(), nestedProps, newTrans);
                } else if (mode.equals(DEFERREDCONSTRAINTSAMPLE1)) {
                    this.database.executePreparedUpdate("UPDATE s_sample SET s_projectid = ? WHERE s_sampleid=?", new Object[]{projectid, sampleid});
                } else if (mode.equals(DEFERREDCONSTRAINTSAMPLE1_WITHNESTEDUPDATE)) {
                    PropertyList nestedProps = new PropertyList();
                    nestedProps.setProperty(PROPERTY_SAMPLEID, nestedSampleid);
                    nestedProps.setProperty(PROPERTY_SAMPLEDESC, nestedSampleDesc);
                    nestedProps.setProperty(PROPERTY_MODE, UPDATESAMPLE1);
                    this.getActionProcessor().processActionClass(ActionTransactionCaller.class.getName(), nestedProps, newTrans);
                    this.database.executePreparedUpdate("UPDATE s_sample SET s_projectid = ? WHERE s_sampleid=?", new Object[]{projectid, sampleid});
                } else if (mode.equals(NESTEDDEFERREDCONSTRAINTRETHROWN)) {
                    PropertyList nestedProps = new PropertyList();
                    nestedProps.setProperty(PROPERTY_SAMPLEID, nestedSampleid);
                    nestedProps.setProperty(PROPERTY_SAMPLEDESC, nestedSampleDesc);
                    nestedProps.setProperty(PROPERTY_MODE, DEFERREDCONSTRAINTSAMPLE1);
                    this.getActionProcessor().processActionClass(ActionTransactionCaller.class.getName(), nestedProps, newTrans);
                } else if (mode.equals(NESTEDDEFERREDCONSTRAINTSUPRESSED)) {
                    PropertyList nestedProps = new PropertyList();
                    nestedProps.setProperty(PROPERTY_SAMPLEID, nestedSampleid);
                    nestedProps.setProperty(PROPERTY_SAMPLEDESC, nestedSampleDesc);
                    nestedProps.setProperty(PROPERTY_MODE, DEFERREDCONSTRAINTSAMPLE1);
                    try {
                        this.getActionProcessor().processActionClass(ActionTransactionCaller.class.getName(), nestedProps, newTrans);
                    }
                    catch (ActionException actionException) {
                        // empty catch block
                    }
                }
            }
        }
        properties.setProperty(RETURN_NEWSAMPLEDESC, this.getSampleDesc(sampleid));
    }

    private String getSampleDesc(String sampleid) {
        SafeSQL safeSQL = new SafeSQL();
        return this.getQueryProcessor().getPreparedSqlDataSet("SELECT sampledesc FROM s_sample WHERE s_sampleid=" + safeSQL.addVar(sampleid), safeSQL.getValues()).getValue(0, PROPERTY_SAMPLEDESC);
    }
}

