/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.util;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.actions.sdi.AddSDI;
import java.util.ArrayList;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class GetTrackItemID
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String message = "";
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String trackitemid = ajaxResponse.getRequestParameter("trackitemid");
        String sdcid = ajaxResponse.getRequestParameter("sdcid");
        String keyid1 = ajaxResponse.getRequestParameter("keyid1", "");
        boolean validatecustody = "Y".equals(ajaxResponse.getRequestParameter("validatecustody", "N"));
        boolean addtrackitem = "Y".equals(ajaxResponse.getRequestParameter("addtrackitem", "Y"));
        String sysuserid = this.getConnectionProcessor().getSapphireConnection().getSysuserId();
        StringBuilder sql = new StringBuilder();
        ArrayList<String> trackitemList = new ArrayList<String>();
        if (OpalUtil.isNotEmpty(trackitemid)) {
            SafeSQL safeSQL = new SafeSQL();
            sql.append("select trackitemid, custodialuserid, (select su.linkkeyid1 from storageunit su where su.storageunitid = trackitem.CURRENTSTORAGEUNITID and su.linksdcid = 'LV_Package') packageid from trackitem");
            ArrayList ds = null;
            if (StringUtil.split(trackitemid, ";").length > 1000) {
                try {
                    String rsetid = this.getDAMProcessor().createRSet("TrackItemSDC", trackitemid, null, null);
                    sql.append(" where trackitemid in (select r.keyid1 from rsetitems r where r.rsetid = ").append(safeSQL.addVar(rsetid)).append(")");
                    ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                    this.getDAMProcessor().clearRSet(rsetid);
                }
                catch (SapphireException e) {
                    e.printStackTrace();
                }
            } else {
                sql.append(" where trackitemid in (").append(safeSQL.addIn(trackitemid, ";")).append(")");
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            }
            if (ds != null && ds.size() > 0) {
                for (int i = 0; i < ds.size(); ++i) {
                    String packageid = ((DataSet)ds).getString(i, "packageid", "");
                    if (validatecustody) {
                        if (!sysuserid.equals(((DataSet)ds).getString(i, "custodialuserid"))) {
                            message = this.getTranslationProcessor().translate("One or more of the selected items are not in your Custody");
                            break;
                        }
                        if (OpalUtil.isNotEmpty(packageid)) {
                            message = this.getTranslationProcessor().translate("One or more of the selected items are in another Package") + " (" + packageid + ")";
                            break;
                        }
                    }
                    if (packageid.length() != 0) continue;
                    trackitemList.add(((DataSet)ds).getString(i, "trackitemid"));
                }
            }
        } else if (OpalUtil.isNotEmpty(sdcid) && OpalUtil.isNotEmpty(keyid1)) {
            keyid1 = StringUtil.replaceAll(keyid1, "%3B", ";");
            String tableid = this.getSDCProcessor().getProperty(sdcid, "tableid");
            String keycolid1 = this.getSDCProcessor().getProperty(sdcid, "keycolid1");
            SafeSQL safeSQL = new SafeSQL();
            if ("Sample".equals(sdcid)) {
                sql.append("select s.").append(keycolid1).append(", s.sstudyid, s.samplestatus, t.trackitemid, t.custodialuserid");
            } else {
                sql.append("select s.").append(keycolid1).append(", t.trackitemid, t.custodialuserid");
            }
            sql.append(" ,(select su.linkkeyid1 from storageunit su where su.storageunitid = t.currentstorageunitid and su.linksdcid = 'LV_Package') packageid");
            sql.append(" from ").append(tableid).append(" s left outer join trackitem t on t.linksdcid = ").append(safeSQL.addVar(sdcid));
            sql.append(" and t.linkkeyid1 = s.").append(keycolid1);
            ArrayList ds = null;
            if (StringUtil.split(keyid1, ";").length > 1000) {
                try {
                    String rsetid = this.getDAMProcessor().createRSet(sdcid, keyid1, null, null);
                    sql.append(" where s.").append(keycolid1).append(" in (select r.keyid1 from rsetitems r where r.rsetid = ").append(safeSQL.addVar(rsetid)).append(")");
                    ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                    this.getDAMProcessor().clearRSet(rsetid);
                }
                catch (SapphireException e) {
                    e.printStackTrace();
                }
            } else {
                sql.append(" where s.").append(keycolid1).append(" in (").append(safeSQL.addIn(keyid1, ";")).append(")");
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            }
            ArrayList<String> addList = new ArrayList<String>();
            if (ds != null && ds.size() > 0) {
                for (int i = 0; i < ds.size(); ++i) {
                    String dstrackitemid = ((DataSet)ds).getString(i, "trackitemid");
                    String packageid = ((DataSet)ds).getString(i, "packageid");
                    if (OpalUtil.isNotEmpty(dstrackitemid)) {
                        if ("Sample".equals(sdcid) && ((DataSet)ds).getString(i, "sstudyid", "").length() == 0 && "Initial".equals(((DataSet)ds).getString(i, "samplestatus", ""))) {
                            message = this.getTranslationProcessor().translate("Samples with status of Initial are not allowed to be shipped");
                            break;
                        }
                        if (validatecustody) {
                            if (!sysuserid.equals(((DataSet)ds).getString(i, "custodialuserid"))) {
                                message = this.getTranslationProcessor().translate("One or more of the selected items are not in your Custody");
                                break;
                            }
                            trackitemList.add(dstrackitemid);
                            if (!OpalUtil.isNotEmpty(packageid)) continue;
                            message = this.getTranslationProcessor().translate("One or more of the selected items are in another Package") + " (" + packageid + ")";
                            break;
                        }
                        if (packageid.length() != 0) continue;
                        trackitemList.add(dstrackitemid);
                        continue;
                    }
                    addList.add(((DataSet)ds).getString(i, keycolid1));
                }
            }
            if (OpalUtil.isEmpty(message) && addList.size() > 0 && addtrackitem) {
                try {
                    PropertyList props = new PropertyList();
                    props.setProperty("sdcid", "TrackItemSDC");
                    props.setProperty("linksdcid", sdcid);
                    props.setProperty("linkkeyid1", OpalUtil.toDelimitedString(addList, ";"));
                    props.setProperty("custodialuserid", sysuserid);
                    props.setProperty("custodialdepartmentid", this.getConnectionProcessor().getSapphireConnection().getDefaultDepartment());
                    props.setProperty("copies", String.valueOf(addList.size()));
                    this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
                    trackitemList.addAll(OpalUtil.toList(props.getProperty("newkeyid1"), ";"));
                }
                catch (ActionException e) {
                    message = this.getTranslationProcessor().translate("Error while adding trackitem records for selected items");
                }
            }
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.addCallbackArgument("trackitemid", OpalUtil.toDelimitedString(trackitemList, ";"));
        ajaxResponse.addCallbackArgument("sdcid", sdcid);
        ajaxResponse.addCallbackArgument("keyid1", keyid1);
        ajaxResponse.print();
    }
}

