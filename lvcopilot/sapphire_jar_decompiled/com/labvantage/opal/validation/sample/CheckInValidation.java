/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.validation.sample;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.opal.validation.BaseAjaxValidation;
import com.labvantage.sapphire.actions.sdi.AddSDI;
import java.util.HashSet;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.error.ErrorHandler;
import sapphire.servlet.AjaxResponse;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class CheckInValidation
extends BaseAjaxValidation {
    protected static final String LABVANTAGE_CVS_ID = "$Revision: 84703 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        ErrorHandler errorHandler = new ErrorHandler();
        String message = "";
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String sdcid = ajaxResponse.getRequestParameter("sdcid");
        if ("Sample".equals(sdcid)) {
            String sysuserid = this.getConnectionProcessor().getSapphireConnection().getSysuserId();
            String sampleid = ajaxResponse.getRequestParameter("keyid1");
            DataSet sampleDs = this.getSampleInfo(sampleid);
            boolean isRepositoryUser = this.isUserRepository(this.getSysUserId());
            StringBuilder userNotInCd = new StringBuilder();
            StringBuilder disposedSample = new StringBuilder();
            StringBuilder noRestrictClass = new StringBuilder();
            StringBuilder inStorage = new StringBuilder();
            StringBuilder noRepositoryRole = new StringBuilder();
            for (int i = 0; i < sampleDs.size(); ++i) {
                boolean isBBSample;
                String tempSampleId = sampleDs.getString(i, "s_sampleid");
                boolean bl = isBBSample = sampleDs.getString(i, "sstudyid", "").length() > 0;
                if (sampleDs.getValue(i, "custodialdepartmentid").length() > 0 && !this.isDepartmentMember(sampleDs.getValue(i, "custodialdepartmentid"))) {
                    userNotInCd.append(";").append(tempSampleId);
                }
                if (isBBSample) {
                    if ("Allocated".equals(sampleDs.getValue(i, "storagestatus")) && !isRepositoryUser) {
                        noRepositoryRole.append(";").append(tempSampleId);
                    }
                    if ("Disposed".equals(sampleDs.getValue(i, "storagestatus"))) {
                        disposedSample.append(";").append(tempSampleId);
                    }
                    try {
                        if (this.isBBRuleActive("Active RC Rule") && sampleDs.getValue(i, "confirmeddt").length() < 1 && !this.studyHasActiveRestrictClass(sampleDs.getValue(i, "sstudyid"))) {
                            noRestrictClass.append(";").append(tempSampleId);
                        }
                    }
                    catch (SapphireException e) {
                        this.logger.error("CheckInValidation", e);
                    }
                } else if ("Disposed".equals(sampleDs.getString(i, "samplestatus"))) {
                    disposedSample.append(";").append(tempSampleId);
                }
                if (sampleDs.getString(i, "custodialuserid", "").equals(sysuserid) || sampleDs.getValue(i, "currentstorageunitid").length() <= 0) continue;
                inStorage.append(";").append(tempSampleId);
            }
            TranslationProcessor tp = this.getTranslationProcessor();
            if (userNotInCd.length() > 0) {
                errorHandler.add("CheckInValidation", "", tp.translate("Validation failure"), "VALIDATION", tp.translate("User is not a member of Sample's Custodial Department") + "<br><br>" + StringUtil.replaceAll(userNotInCd.substring(1), ";", ", "));
            }
            if (noRepositoryRole.length() > 0) {
                errorHandler.add("CheckInValidation", "", tp.translate("Validation failure"), "VALIDATION", tp.translate("User do not have enough roles to perform this operation on samples") + "<br><br>" + StringUtil.replaceAll(noRepositoryRole.substring(1), ";", ", "));
            }
            if (disposedSample.length() > 0) {
                errorHandler.add("CheckInValidation", "", tp.translate("Validation failure"), "VALIDATION", tp.translate("Following sample has been Disposed") + "<br><br>" + StringUtil.replaceAll(disposedSample.substring(1), ";", ", "));
            }
            if (noRestrictClass.length() > 0) {
                errorHandler.add("CheckInValidation", "", tp.translate("Validation failure"), "VALIDATION", tp.translate("Sample is not confirmed and its study does not have an Active Restriction Class") + "<br><br>" + StringUtil.replaceAll(noRestrictClass.substring(1), ";", ", "));
            }
            if (inStorage.length() > 0) {
                errorHandler.add("CheckInValidation", "", tp.translate("Validation failure"), "VALIDATION", tp.translate("Following samples are stored in storage") + "<br><br>" + StringUtil.replaceAll(inStorage.substring(1), ";", ", "));
            }
        } else if ("LV_Array".equals(sdcid)) {
            String arrayid = ajaxResponse.getRequestParameter("keyid1");
            String sysuserid = this.getConnectionProcessor().getSapphireConnection().getSysuserId();
            arrayid = StringUtil.replaceAll(arrayid, "%3B", ";");
            HashSet<String> addSet = new HashSet<String>();
            SafeSQL safeSQL = new SafeSQL();
            String lazysql = "select array.arrayid, trackitem.trackitemid from array left outer join trackitem on trackitem.linksdcid = 'LV_Array' and trackitem.linkkeyid1 = array.arrayid where arrayid in (" + safeSQL.addIn(arrayid, ";") + ")";
            DataSet lazyds = this.getQueryProcessor().getPreparedSqlDataSet(lazysql, safeSQL.getValues());
            if (lazyds != null) {
                for (int i = 0; i < lazyds.size(); ++i) {
                    if (!OpalUtil.isEmpty(lazyds.getString(i, "trackitemid"))) continue;
                    addSet.add(lazyds.getString(i, "arrayid"));
                }
                if (addSet.size() > 0) {
                    PropertyList props = new PropertyList();
                    props.setProperty("sdcid", "TrackItemSDC");
                    props.setProperty("copies", String.valueOf(addSet.size()));
                    props.setProperty("linksdcid", sdcid);
                    props.setProperty("linkkeyid1", OpalUtil.toDelimitedString(addSet, ";"));
                    props.setProperty("custodialuserid", sysuserid);
                    props.setProperty("custodialdepartmentid", this.getConnectionProcessor().getSapphireConnection().getDefaultDepartment());
                    try {
                        this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
                    }
                    catch (ActionException e) {
                        message = this.getTranslationProcessor().translate("Unable to lazy create Trackitem for Array. If problem persist, please contact your Administrator.");
                    }
                }
            }
            safeSQL.reset();
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select trackitemid, linkkeyid1, custodialuserid, custodialdepartmentid from trackitem where linksdcid = 'LV_Array' and linkkeyid1 in (" + safeSQL.addIn(arrayid, ";") + ")", safeSQL.getValues());
            if (ds != null) {
                List<String> departmentList = OpalUtil.toList(this.getConnectionProcessor().getSapphireConnection().getDepartmentList(), ";");
                for (int i = 0; i < ds.size(); ++i) {
                    String custodialuserid = ds.getString(i, "custodialuserid");
                    if (OpalUtil.isEmpty(custodialuserid) || OpalUtil.isNotEmpty(custodialuserid) && !sysuserid.equals(custodialuserid)) {
                        message = this.getTranslationProcessor().translate("One of more of the selected Plate(s) is not in your custody.");
                    } else {
                        String custodialdepartmentid = ds.getString(i, "custodialdepartmentid", "");
                        if (!OpalUtil.isNotEmpty(custodialdepartmentid) || departmentList.contains(custodialdepartmentid)) continue;
                        message = this.getTranslationProcessor().translate("One of more of the selected Plate(s) is not in your custodial department.");
                    }
                    break;
                }
            }
        } else {
            String trackItemIds = ajaxResponse.getRequestParameter("trackitemids");
            if (trackItemIds != null && trackItemIds.length() > 0) {
                try {
                    this.validateCheckIn(trackItemIds);
                }
                catch (SapphireException e) {
                    errorHandler.add("CheckInValidation", "", "Operation Failed", "VALIDATION", e.getMessage());
                }
            } else {
                errorHandler.add("CheckInValidation", "", "Operation Failed", "VALIDATION", "Error: Track Item Id not found for the selected item(s).");
            }
        }
        if (errorHandler.size() > 0) {
            message = ErrorUtil.formatErrorMessage(errorHandler);
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.print();
    }

    private void validateCheckIn(String trackItemIds) throws SapphireException {
        DataSet ds;
        String sysuserid = this.getConnectionProcessor().getSapphireConnection().getSysuserId();
        QueryProcessor qp = this.getQueryProcessor();
        StringBuilder sql = new StringBuilder();
        TranslationProcessor tp = this.getTranslationProcessor();
        SafeSQL safeSQL = new SafeSQL();
        if (StringUtil.getLen(trackItemIds) <= 2000L) {
            sql.append("SELECT custodialuserid, currentstorageunitid FROM trackitem");
            sql.append(" WHERE trackitemid IN (").append(safeSQL.addIn(trackItemIds, ";")).append(" )");
            ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        } else {
            String rsetid = this.getDAMProcessor().createRSet("TrackItemSDC", trackItemIds, null, null);
            sql.append("SELECT custodialuserid, currentstorageunitid FROM trackitem");
            sql.append(" WHERE trackitemid IN ( select rsetitems.keyid1 from rsetitems where rsetitems.rsetid = ").append(safeSQL.addVar(rsetid)).append(" )");
            ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            this.getDAMProcessor().clearRSet(rsetid);
        }
        if (ds != null) {
            for (int i = 0; i < ds.size(); ++i) {
                String custodialuserid = ds.getString(i, "custodialuserid", "");
                String currentStorageUnitId = ds.getString(i, "currentstorageunitid", "");
                if (custodialuserid.equals(sysuserid) || currentStorageUnitId.length() <= 0) continue;
                throw new SapphireException(tp.translate("Selected item(s) are currently in storage. You must take custody of these item(s)."));
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public DataSet getSampleInfo(String sampleId) {
        DataSet ds;
        StringBuilder sql = new StringBuilder();
        if (StringUtil.getLen(sampleId) <= 2000L) {
            SafeSQL safeSQL = new SafeSQL();
            sql.append("select s.s_sampleid, s.sstudyid, s.samplestatus, s.storagestatus, t.trackitemid, t.custodialuserid, t.custodialdepartmentid, s.confirmeddt, s.confirmedby, s.sstudyid, t.currentstorageunitid").append(" from s_sample s left outer join trackitem t on t.linksdcid = 'Sample' and t.linkkeyid1 = s.s_sampleid").append(" where s.s_sampleid in (").append(safeSQL.addIn(sampleId, ";")).append(")");
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        } else {
            String rsetid = null;
            try {
                rsetid = this.getDAMProcessor().createRSet("Sample", sampleId, null, null);
                sql.append("select s.s_sampleid, s.sstudyid, s.samplestatus, s.storagestatus, t.trackitemid, t.custodialuserid, t.custodialdepartmentid, s.confirmeddt, s.confirmedby, s.sstudyid, t.currentstorageunitid").append(" from s_sample s left outer join trackitem t on t.linksdcid = 'Sample' and t.linkkeyid1 = s.s_sampleid").append(" where s.s_sampleid in (select rsetitems.keyid1 from rsetitems where rsetitems.rsetid = ?)");
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{rsetid});
            }
            catch (SapphireException e) {
                ds = new DataSet();
                e.printStackTrace();
            }
            finally {
                if (rsetid != null) {
                    this.getDAMProcessor().clearRSet(rsetid);
                }
            }
        }
        if (OpalUtil.isNotEmpty(ds)) {
            DataSet addTrackItemDS = new DataSet();
            for (int i = 0; i < ds.size(); ++i) {
                String trackitemid = ds.getString(i, "trackitemid", "");
                if (trackitemid.length() != 0) continue;
                int row = addTrackItemDS.addRow();
                addTrackItemDS.setString(row, "s_sampleid", ds.getString(i, "s_sampleid"));
            }
            if (addTrackItemDS.size() > 0) {
                String sysuserid = this.getSapphireConnection().getSysuserId();
                String defaultdepartmentid = this.getSapphireConnection().getDefaultDepartment();
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", "TrackItemSDC");
                props.setProperty("linksdcid", "Sample");
                props.setProperty("linkkeyid1", addTrackItemDS.getColumnValues("s_sampleid", ";"));
                props.setProperty("copies", String.valueOf(addTrackItemDS.size()));
                props.setProperty("custodialuserid", sysuserid);
                props.setProperty("custodialdepartmentid", defaultdepartmentid);
                try {
                    this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
                    addTrackItemDS.addColumnValues("trackitemid", 0, props.getProperty("newkeyid1"), ";");
                    for (int i = 0; i < addTrackItemDS.size(); ++i) {
                        String sampleid = addTrackItemDS.getString(i, "s_sampleid");
                        String trackitemid = addTrackItemDS.getString(i, "trackitemid");
                        int row = ds.findRow("s_sampleid", sampleid);
                        if (row == -1) continue;
                        ds.setString(row, "trackitemid", trackitemid);
                        ds.setString(row, "custodialuserid", sysuserid);
                        ds.setString(row, "custodialdepartmentid", defaultdepartmentid);
                    }
                }
                catch (ActionException e) {
                    e.printStackTrace();
                }
            }
        }
        return ds;
    }

    private boolean isUserRepository(String userid) {
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT ROLEID FROM SYSUSERROLE WHERE SYSUSERID = ?", (Object[])new String[]{userid});
        for (int i = 0; i < ds.size(); ++i) {
            if (!"Repository User".equals(ds.getString(i, "ROLEID", ""))) continue;
            return true;
        }
        return false;
    }

    private boolean studyHasActiveRestrictClass(String studyid) {
        return this.getQueryProcessor().getPreparedSqlDataSet("select s_restrictclassid from s_restrictclass where sstudyid = ?", (Object[])new String[]{studyid}).getRowCount() > 0;
    }
}

