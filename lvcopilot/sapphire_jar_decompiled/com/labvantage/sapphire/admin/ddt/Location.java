/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.scheduler.SchedulerUtil;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class Location
extends BaseSDCRules {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        this.checkValidParentLocation(primary);
        this.checkSiteLabWorkAreaHierarchy(primary);
        this.createLocationHotspotSDI(primary);
        if (!actionProps.getProperty("operation").equals("synchronizeonly") && actionProps.getProperty("auditactivity").equals("Clear Excursion")) {
            this.checkValidClearExcursion(primary);
        }
    }

    private void checkValidClearExcursion(DataSet primary) throws SapphireException {
        for (int i = 0; i < primary.getRowCount(); ++i) {
            String locationid = primary.getValue(i, "s_locationid");
            String sql = "select s.EXCURSIONFLAG from s_location s where s.S_LOCATIONID = ?";
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{locationid});
            if (ds == null || ds.getRowCount() <= 0 || !ds.getValue(0, "excursionflag").equalsIgnoreCase("N") && !ds.getValue(0, "excursionflag").equalsIgnoreCase("")) continue;
            throw new SapphireException("Please clear the excursion in the correct level");
        }
    }

    void checkValidParentLocation(DataSet primary) throws SapphireException {
        for (int i = 0; i < primary.getRowCount(); ++i) {
            String parentlocationid;
            String locationid = primary.getValue(i, "s_locationid");
            if (!this.hasPrimaryValueChanged(primary, i, "parentlocationid") || !(parentlocationid = primary.getValue(i, "parentlocationid")).equals(locationid)) continue;
            throw new SapphireException("Location can't have parent location reference to itself!");
        }
    }

    void createLocationHotspotSDI(DataSet primary) throws SapphireException {
        for (int i = 0; i < primary.getRowCount(); ++i) {
            String locationid = primary.getValue(i, "s_locationid");
            String hotspotProp = primary.getValue(i, "hotspotpropertyclob");
            try {
                JSONArray hotspotsPropArray = new JSONArray(hotspotProp);
                JSONObject hotspotsProp = new JSONObject(hotspotsPropArray.get(0).toString());
                JSONArray hotspotsArray = new JSONArray(hotspotsProp.has("hotspot") ? hotspotsProp.getString("hotspot") : "[]");
                if (hotspotsArray.length() <= 0) continue;
                for (int hotspot = 0; hotspot < hotspotsArray.length(); ++hotspot) {
                    JSONObject hotspots = new JSONObject(hotspotsArray.get(hotspot).toString());
                    if (!hotspots.has("create") || !hotspots.get("create").toString().equalsIgnoreCase("Y")) continue;
                    if (hotspots.has("sdcid") && hotspots.has("label") && (!hotspots.has("keyid1") || hotspots.get("keyid1").toString().length() == 0)) {
                        String sdcid = hotspots.get("sdcid").toString();
                        String label = hotspots.get("label").toString();
                        if (sdcid != null && sdcid.trim().length() > 0) {
                            HashMap<String, String> actionProps = new HashMap<String, String>();
                            actionProps.put("sdcid", sdcid);
                            if (sdcid.equalsIgnoreCase("SamplePoint")) {
                                actionProps.put("locationid", locationid);
                            }
                            actionProps.put("parentlocationid", locationid);
                            actionProps.put("locationlabel", label);
                            try {
                                this.getActionProcessor().processAction("AddSDI", "1", actionProps);
                                String value = (String)actionProps.get("newkeyid1");
                                hotspots.remove("create");
                                hotspots.put("keyid1", value);
                            }
                            catch (ActionException e) {
                                this.logger.error(e.getMessage(), e);
                            }
                        }
                        hotspotsArray.put(hotspot, hotspots);
                    }
                    JSONObject o = hotspotsArray.getJSONObject(0);
                    hotspotsProp.remove("hotspot");
                    JSONArray ab = ((JSONObject)hotspotsPropArray.get(0)).getJSONArray("hotspot");
                    ((JSONObject)hotspotsPropArray.get(0)).put("hotspot", hotspotsArray);
                    hotspotsProp.put("hotspot", o);
                    JSONObject oi = hotspotsProp;
                    primary.setValue(i, "hotspotpropertyclob", hotspotsPropArray.toString());
                }
                continue;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        this.checkValidParentLocation(primary);
        this.checkSiteLabWorkAreaHierarchy(primary);
    }

    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        int i;
        String newLocations;
        int copies;
        DataSet ds;
        String sql;
        String templateKeyid1;
        String keygenerationrule;
        DataSet primary = sdiData.getDataset("primary");
        boolean isFromTemplate = !actionProps.getProperty("templateid", "").isEmpty() || !actionProps.getProperty("templatekeyid1", "").isEmpty();
        boolean deepCopyPlanItems = actionProps.getProperty("copyplanitems", "N").startsWith("Y");
        boolean deepCopySamplePoints = actionProps.getProperty("copysamplepoints", "N").startsWith("Y");
        boolean deepCopyLocations = actionProps.getProperty("copylocations", "N").startsWith("Y");
        if (isFromTemplate && deepCopyPlanItems) {
            String templateKeyid12 = actionProps.getProperty("templateid", "");
            if (templateKeyid12.isEmpty()) {
                templateKeyid12 = actionProps.getProperty("templatekeyid1", "");
            }
            String newKeyid1 = primary.getColumnValues("s_locationid", ";");
            SchedulerUtil schedulerUtil = new SchedulerUtil(this.getConnectionId());
            schedulerUtil.copySchedulePlanItemsOnSource(this.getSdcid(), templateKeyid12, null, null, this.getSdcid(), newKeyid1, null, null, true);
        }
        if (isFromTemplate && deepCopySamplePoints) {
            keygenerationrule = this.getSDCProcessor().getProperty("SamplePoint", "keygenerationrule");
            boolean autokeygen = StringUtil.getLen(keygenerationrule) > 0L && keygenerationrule.charAt(0) == 'A';
            templateKeyid1 = actionProps.getProperty("templateid", "");
            if (templateKeyid1.isEmpty()) {
                templateKeyid1 = actionProps.getProperty("templatekeyid1", "");
            }
            sql = "select s_samplepointid from s_samplepoint where locationid = ?";
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{templateKeyid1});
            if (!autokeygen && ds.getRowCount() > 0) {
                this.setError("CopySamplePoints", "INFORMATION", "SamplePoints under location cannot be copied because SamplePoint SDC does not have automatical keygeneration turned on");
            } else {
                copies = primary.getRowCount();
                newLocations = primary.getColumnValues("s_locationid", ";");
                for (i = 0; i < ds.getRowCount(); ++i) {
                    String templateSPkeyid1 = ds.getString(i, "s_samplepointid");
                    PropertyList addSamplePointProps = new PropertyList();
                    addSamplePointProps.setProperty("sdcid", "SamplePoint");
                    addSamplePointProps.setProperty("templateid", templateSPkeyid1);
                    addSamplePointProps.setProperty("copyplanitems", "Y");
                    addSamplePointProps.setProperty("locationid", newLocations);
                    addSamplePointProps.setProperty("copies", String.valueOf(copies));
                    this.getActionProcessor().processAction("AddSDI", "1", addSamplePointProps);
                }
            }
        }
        if (isFromTemplate && deepCopyLocations) {
            keygenerationrule = this.getSDCProcessor().getProperty(this.getSdcid(), "keygenerationrule");
            boolean autokeygen = StringUtil.getLen(keygenerationrule) > 0L && keygenerationrule.charAt(0) == 'A';
            templateKeyid1 = actionProps.getProperty("templateid", "");
            if (templateKeyid1.isEmpty()) {
                templateKeyid1 = actionProps.getProperty("templatekeyid1", "");
            }
            sql = "select s_locationid from s_location where parentlocationid = ?";
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{templateKeyid1});
            copies = primary.getRowCount();
            newLocations = primary.getColumnValues("s_locationid", ";");
            if (!autokeygen && ds.getRowCount() > 0) {
                this.setError("CopyChildLocations", "INFORMATION", "Child locations under this location cannot be copied because Location SDC does not have automatical keygeneration turned on");
            } else {
                for (i = 0; i < ds.getRowCount(); ++i) {
                    String templateLocationKeyid1 = ds.getString(i, "s_locationid");
                    PropertyList addChildLocationProps = new PropertyList();
                    addChildLocationProps.setProperty("sdcid", "Location");
                    addChildLocationProps.setProperty("templateid", templateLocationKeyid1);
                    addChildLocationProps.setProperty("copyplanitems", "Y");
                    addChildLocationProps.setProperty("copylocations", deepCopyLocations ? "Y" : "N");
                    addChildLocationProps.setProperty("copysamplepoints", deepCopySamplePoints ? "Y" : "N");
                    addChildLocationProps.setProperty("parentlocationid", newLocations);
                    addChildLocationProps.setProperty("copies", String.valueOf(copies));
                    this.getActionProcessor().processAction("AddSDI", "1", addChildLocationProps);
                }
            }
        }
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        String keyid1 = actionProps.getProperty("keyid1", "");
        SchedulerUtil util = new SchedulerUtil(this.getConnectionId());
        util.checkForExistingSchedulePlanItemOnSource(this.getSdcid(), keyid1);
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
}

