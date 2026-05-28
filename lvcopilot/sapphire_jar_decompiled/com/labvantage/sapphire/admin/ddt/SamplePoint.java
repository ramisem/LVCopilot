/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.scheduler.SchedulerUtil;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class SamplePoint
extends BaseSDCRules {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        boolean isFromTemplate = !actionProps.getProperty("templateid", "").isEmpty() || !actionProps.getProperty("templatekeyid1", "").isEmpty();
        boolean deepCopyPlanItems = actionProps.getProperty("copyplanitems", "N").startsWith("Y");
        if (isFromTemplate && deepCopyPlanItems) {
            String templateKeyid1 = actionProps.getProperty("templateid", "");
            if (templateKeyid1.isEmpty()) {
                templateKeyid1 = actionProps.getProperty("templatekeyid1", "");
            }
            String newKeyid1 = primary.getColumnValues("s_samplepointid", ";");
            SchedulerUtil schedulerUtil = new SchedulerUtil(this.getConnectionId());
            schedulerUtil.copySchedulePlanItemsOnSource(this.getSdcid(), templateKeyid1, null, null, this.getSdcid(), newKeyid1, null, null, true);
        }
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        String keyid1 = actionProps.getProperty("keyid1", "");
        SchedulerUtil util = new SchedulerUtil(this.getConnectionId());
        util.checkForExistingSchedulePlanItemOnSource(this.getSdcid(), keyid1);
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        this.checkSiteLabWorkAreaHierarchy(primary);
    }

    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        this.checkSiteLabWorkAreaHierarchy(primary);
    }

    private void checkSiteLabWorkAreaHierarchy(DataSet primary) throws SapphireException {
        for (int indx = 0; indx < primary.size(); ++indx) {
            DataSet ds;
            String siteid = this.getColumnValue(primary, indx, "sitedepartmentid");
            String labid = this.getColumnValue(primary, indx, "testingdepartmentid");
            String workarea = this.getColumnValue(primary, indx, "workareadepartmentid");
            StringBuffer sql = new StringBuffer();
            SafeSQL safeSQL = new SafeSQL();
            String exceptionMSG = "";
            if ((this.hasPrimaryValueChanged(primary, indx, "sitedepartmentid") || this.hasPrimaryValueChanged(primary, indx, "testingdepartmentid")) && siteid.length() > 0 && labid.length() > 0 && !labid.equals(siteid)) {
                sql.append("select departmentid from department where departmentid = ").append(safeSQL.addVar(labid)).append(" and parentdepartmentid=").append(safeSQL.addVar(siteid));
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                if (ds == null || ds.size() == 0) {
                    exceptionMSG = "Testing Lab (" + labid + ") is not in the Site (" + siteid + ").";
                }
            }
            if ((this.hasPrimaryValueChanged(primary, indx, "workareadepartmentid") || this.hasPrimaryValueChanged(primary, indx, "testingdepartmentid")) && workarea.length() > 0 && labid.length() > 0 && !labid.equals(workarea)) {
                safeSQL.reset();
                sql.delete(0, sql.length());
                sql.append("select departmentid from department where departmentid = ").append(safeSQL.addVar(workarea)).append(" and parentdepartmentid=").append(safeSQL.addVar(labid));
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                if (ds == null || ds.size() == 0) {
                    exceptionMSG = (exceptionMSG.length() > 0 ? exceptionMSG + " And " : "") + "Work Area (" + workarea + ") is not in the Testing Lab (" + labid + ").";
                }
            }
            if (exceptionMSG.length() <= 0) continue;
            throw new SapphireException(this.getTranslationProcessor().translate(exceptionMSG));
        }
    }

    private String getColumnValue(DataSet primary, int indx, String columnid) {
        String value = "";
        value = primary.isValidColumn(columnid) ? primary.getString(indx, columnid, "") : this.getOldPrimaryValue(primary, indx, columnid);
        return value;
    }

    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }
}

