/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.validation.tism;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.admin.ddt.StorageUnitSDC;
import com.labvantage.sapphire.modules.storage.StorageUnitUtil;
import com.labvantage.sapphire.services.SapphireConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;

public class ValidateAutoFileSDI
extends BaseAjaxRequest {
    public static final String LABVANTAGE_CVS_ID = "$Revision: 90000 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        DataSet ds;
        String msg;
        DataSet sortedDS;
        String confirm;
        AjaxResponse ajaxResponse;
        block45: {
            ajaxResponse = new AjaxResponse(request, response);
            String trackitemid = ajaxResponse.getRequestParameter("trackitemid", "");
            String targetstorageunitid = ajaxResponse.getRequestParameter("targetstorageunitid", "");
            String confirmed = ajaxResponse.getRequestParameter("confirmed", "N");
            confirm = "N";
            SapphireConnection sapphireConnection = this.getConnectionProcessor().getSapphireConnection();
            String sysuserid = sapphireConnection.getSysuserId();
            sortedDS = null;
            msg = "";
            StringBuilder sql = new StringBuilder();
            ds = new DataSet();
            String rsetid = null;
            if ("0".equals(OpalUtil.getColumnValue(this.getQueryProcessor(), "storageunit", "maxtiallowed", "storageunitid=? and not exists (select 1 from storageunit su where su.parentid = storageunit.storageunitid)", new String[]{targetstorageunitid}))) {
                msg = this.getTranslationProcessor().translate("Target storage location does not allow filing trackitems");
            } else {
                try {
                    rsetid = this.getDAMProcessor().createRSet("TrackItemSDC", trackitemid, "", "");
                    sql.append("select t.trackitemid, t.linksdcid sdcid, t.linkkeyid1 keyid1, t.custodialdepartmentid, t.custodialuserid, t.freezethawflag, t.freezethawcount, t.freezethawcountmax, t.freezethawcountwarn");
                    sql.append(", t.currentstorageunitid, (select su.labelpath from storageunit su where su.storageunitid = t.currentstorageunitid) labelpath,");
                    sql.append(" (select su.linksdcid from storageunit su where su.storageunitid = t.currentstorageunitid) susdcid,");
                    sql.append(" (select department.externalflag from department where department.departmentid = t.custodialdepartmentid) externaldeptflag");
                    sql.append(" from trackitem t, rsetitems r");
                    sql.append(" where t.trackitemid = r.keyid1 and r.rsetid = ?");
                    sql.append(" order by r.rsetseq");
                    ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{rsetid});
                    if (ds != null && ds.size() > 0) {
                        ds.addColumn("reservelocation", 0);
                        ds.addColumn("__ftstatusimg", 0);
                        ds.addColumn("__ftflag", 0);
                        for (int i = 0; i < ds.size(); ++i) {
                            int row;
                            boolean isTargetFTCandidate;
                            String currentstorageunitid;
                            String string = ds.getValue(i, "sdcid");
                            String string2 = ds.getValue(i, "keyid1");
                            String custodian = ds.getValue(i, "custodialuserid");
                            if (StringUtil.getLen(custodian) > 0L && !sysuserid.equals(custodian)) {
                                msg = this.getTranslationProcessor().translate("Item being filed is not in user's custody") + " (" + OpalUtil.getSDCName(string) + " " + string2 + ")";
                                break;
                            }
                            if (!"Y".equals(confirmed) && (currentstorageunitid = ds.getString(i, "currentstorageunitid", "")).length() > 0 && !"LV_Package".equals(ds.getString(i, "susdcid"))) {
                                msg = "One or more if the selected item(s) is already in Storage";
                                confirm = "Y";
                                break;
                            }
                            String cd = ds.getValue(i, "custodialdepartmentid");
                            if (StringUtil.getLen(cd) > 0L && !"Y".equals(ds.getValue(i, "externaldeptflag")) && !sapphireConnection.isDepartmentMember(cd)) {
                                msg = this.getTranslationProcessor().translate("Item being filed is not in user's custodial department") + " (" + OpalUtil.getSDCName(string) + " " + string2 + ")";
                                break;
                            }
                            ds.setValue(i, "labelpath", ds.getValue(i, "labelpath", ""));
                            ds.setValue(i, "custodialuserid", ds.getValue(i, "custodialuserid", ""));
                            ds.setValue(i, "freezethawflag", ds.getValue(i, "freezethawflag", "N"));
                            ds.setNumber(i, "freezethawcount", ds.getInt(i, "freezethawcount", -1));
                            ds.setNumber(i, "freezethawcountmax", ds.getInt(i, "freezethawcountmax", 0));
                            ds.setNumber(i, "freezethawcountwarn", ds.getInt(i, "freezethawcountwarn", 0));
                            ds.setValue(i, "reservelocation", "");
                            int count = ds.getInt(i, "freezethawcount");
                            int warn = ds.getInt(i, "freezethawcountwarn", 0);
                            int max = ds.getInt(i, "freezethawcountmax", 0);
                            String ftimg = "FreezeThawPass";
                            if (max != 0) {
                                if (count > max) {
                                    ftimg = "FreezeThawFail";
                                } else if (count == max) {
                                    ftimg = "FreezeThawMax";
                                } else if (warn != 0 && count >= warn) {
                                    ftimg = "FreezeThawWarn";
                                }
                            } else if (warn != 0 && count >= warn) {
                                ftimg = "FreezeThawWarn";
                            }
                            ds.setValue(i, "__ftstatusimg", ftimg);
                            ds.setValue(i, "__ftflag", ds.getValue(i, "freezethawflag"));
                            if ("Sample".equals(string) || !(isTargetFTCandidate = StorageUnitSDC.isFreezeThawCandidate(this.getQueryProcessor(), sapphireConnection.isOracle(), targetstorageunitid))) continue;
                            sql.setLength(0);
                            DataSet dsFreezeThaw = new DataSet();
                            DataSet dataSet = this.getQueryProcessor().getPreparedSqlDataSet("select s.s_sampleid, t.trackitemid, t.freezethawcountwarn, t.freezethawcountmax, t.freezethawcount, t.currentstorageunitid, '' storageunitlabel from s_sample s, trackitem t where t.linksdcid = 'Sample' and t.linkkeyid1 = s.s_sampleid and t.freezethawflag = 'Y' and t.currentstorageunitid = (select su.storageunitid from storageunit su where su.linksdcid = ? and su.linkkeyid1 = ?) order by s.s_sampleid", (Object[])new String[]{string, string2});
                            if (dataSet != null) {
                                for (row = 0; row < dataSet.size(); ++row) {
                                    dsFreezeThaw.copyRow(dataSet, row, 1);
                                }
                            }
                            if ((dataSet = this.getQueryProcessor().getPreparedSqlDataSet("select c.storageunitid, c.storageunitlabel, s.s_sampleid, t.trackitemid, t.freezethawcountwarn, t.freezethawcountmax, t.freezethawcount from storageunit p, storageunit c, trackitem t, s_sample s where p.linksdcid = ? and p.linkkeyid1 = ? and c.parentid = p.storageunitid and t.currentstorageunitid = c.storageunitid and t.freezethawflag = 'Y' and t.linksdcid = 'Sample' and s.s_sampleid = t.linkkeyid1 order by c.storageunitindex", (Object[])new String[]{string, string2})) != null) {
                                for (row = 0; row < dataSet.size(); ++row) {
                                    dsFreezeThaw.copyRow(dataSet, row, 1);
                                }
                            }
                            if (dsFreezeThaw.size() <= 0) continue;
                            for (row = 0; row < dsFreezeThaw.size(); ++row) {
                                ftimg = "greenled.gif";
                                int __count = dsFreezeThaw.getInt(row, "freezethawcount", 0);
                                int __warn = dsFreezeThaw.getInt(row, "freezethawcountwarn", 0);
                                int __max = dsFreezeThaw.getInt(row, "freezethawcountmax", 0);
                                if (__max != 0) {
                                    if (__count > __max) {
                                        ftimg = "redled.gif";
                                    } else if (__count == __max) {
                                        ftimg = "orangeled.gif";
                                    } else if (__warn != 0 && __count >= __warn) {
                                        ftimg = "yellowled.gif";
                                    }
                                } else if (__warn != 0 && __count >= __warn) {
                                    ftimg = "yellowled.gif";
                                }
                                dsFreezeThaw.setString(row, "__ftstatusimg", ftimg);
                                if ("Y".equals(ajaxResponse.getRequestParameter("thawbydefault", "N"))) {
                                    dsFreezeThaw.setString(row, "thawed", "Y");
                                    dsFreezeThaw.setNumber(row, "freezethawcount", __count + 1);
                                    continue;
                                }
                                dsFreezeThaw.setString(row, "thawed", "N");
                                dsFreezeThaw.setNumber(row, "freezethawcount", __count);
                            }
                            ds.setString(i, "contentftflag", "Y");
                            ds.setString(i, "contentsampleid", dsFreezeThaw.getColumnValues("s_sampleid", ";"));
                            ds.setString(i, "contenttrackitemid", dsFreezeThaw.getColumnValues("trackitemid", ";"));
                            ds.setString(i, "storageunitlabel", dsFreezeThaw.getColumnValues("storageunitlabel", ";"));
                            ds.setString(i, "contentftcount", dsFreezeThaw.getColumnValues("freezethawcount", ";"));
                            ds.setString(i, "contentftcountwarn", dsFreezeThaw.getColumnValues("freezethawcountwarn", ";"));
                            ds.setString(i, "contentftcountmax", dsFreezeThaw.getColumnValues("freezethawcountmax", ";"));
                            ds.setString(i, "contentftstatusimg", dsFreezeThaw.getColumnValues("__ftstatusimg", ";"));
                            ds.setString(i, "contentthawed", dsFreezeThaw.getColumnValues("thawed", ";"));
                        }
                    }
                    if (trackitemid.contains("trackitemid_")) {
                        String[] s;
                        for (String value : s = StringUtil.split(trackitemid, ";")) {
                            if (!value.startsWith("trackitemid_")) continue;
                            int row = ds.addRow();
                            ds.setValue(row, "trackitemid", value);
                            ds.setValue(row, "labelpath", "");
                            ds.setValue(row, "custodialuserid", "");
                            ds.setValue(row, "freezethawflag", "");
                            ds.setNumber(row, "freezethawcount", "");
                            ds.setNumber(row, "freezethawcountmax", "");
                            ds.setNumber(row, "freezethawcountwarn", "");
                            ds.setValue(row, "reservelocation", "");
                        }
                    }
                    if (!ds.getColumnValues("trackitemid", ";").equals(trackitemid)) {
                        sortedDS = new DataSet();
                        if (ds != null && ds.size() > 0) {
                            String[] stringArray;
                            HashMap<String, String> filterMap = new HashMap<String, String>();
                            for (String trackitem : stringArray = StringUtil.split(trackitemid, ";")) {
                                filterMap.put("trackitemid", trackitem);
                                int row = ds.findRow(filterMap);
                                if (row == -1) continue;
                                sortedDS.copyRow(ds, row, 1);
                            }
                            ds.clear();
                        }
                    }
                    List<String> trackitemlist = OpalUtil.toList(trackitemid, ":");
                    DataSet dataSet = StorageUnitUtil.getStorageRestrictions(this.getQueryProcessor(), targetstorageunitid, this.getConnectionProcessor().isOra());
                    if (dataSet == null || dataSet.size() <= 0) break block45;
                    ArrayList<String> arrayList = new ArrayList<String>();
                    DataSet suds = this.getQueryProcessor().getPreparedSqlDataSet("select su.storageunitid from storageunit su, trackitem ti where su.linksdcid = ti.linksdcid and su.linkkeyid1 = ti.linkkeyid1 and ti.trackitemid in (select r.keyid1 from rsetitems r where r.rsetid = ?)", (Object[])new String[]{rsetid});
                    if (suds != null && suds.size() > 0) {
                        try {
                            for (int i = 0; i < suds.size(); ++i) {
                                DataSet trackitems = StorageUnitSDC.getAllTrackitemsInStorageUnitHeirarchy(this.getQueryProcessor(), suds.getString(i, "storageunitid"), this.getConnectionProcessor().isOra());
                                if (trackitems == null || trackitems.size() <= 0) continue;
                                arrayList.addAll(OpalUtil.toList(trackitems.getColumnValues("trackitemid", ";"), ";"));
                            }
                        }
                        catch (SapphireException e) {
                            e.printStackTrace(System.out);
                        }
                    }
                    trackitemlist.addAll(arrayList);
                    List<String> errors = StorageUnitUtil.validateStorageRestrictions(this.getQueryProcessor(), this.getDAMProcessor(), targetstorageunitid, OpalUtil.toDelimitedString(trackitemlist, ";"), this.getConnectionProcessor().getSapphireConnection());
                    if (errors != null && errors.size() > 0) {
                        msg = "<p>";
                        msg = msg + OpalUtil.toDelimitedString(errors, "</p><p>");
                        msg = msg + "</p>";
                    }
                }
                catch (Exception e) {
                    msg = this.getTranslationProcessor().translate("Exception caught while validating Trackitems. If problem persists, please contact your Adminstrator.") + " [" + e.getMessage() + "]";
                    throw new ServletException((Throwable)e);
                }
                finally {
                    if (StringUtil.getLen(rsetid) > 0L) {
                        this.getDAMProcessor().clearRSet(rsetid);
                    }
                }
            }
        }
        ajaxResponse.addCallbackArgument("msg", msg);
        ajaxResponse.addCallbackArgument("ds", sortedDS != null ? sortedDS : ds);
        ajaxResponse.addCallbackArgument("confirm", confirm);
        ajaxResponse.print();
    }
}

