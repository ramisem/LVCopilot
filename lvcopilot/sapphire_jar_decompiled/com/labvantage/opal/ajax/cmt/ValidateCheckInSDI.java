/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.cmt;

import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class ValidateCheckInSDI
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String message = "";
        String sdcid = ajaxResponse.getRequestParameter("sdcid", "");
        String keyid1 = StringUtil.replaceAll(ajaxResponse.getRequestParameter("keyid1", ""), "%3B", ";");
        String keyid2 = StringUtil.replaceAll(ajaxResponse.getRequestParameter("keyid2", ""), "%3B", ";");
        String keyid3 = StringUtil.replaceAll(ajaxResponse.getRequestParameter("keyid3", ""), "%3B", ";");
        String pTreeNodeIds = StringUtil.replaceAll(ajaxResponse.getRequestParameter("propertytreenodeid", ""), "%3B", ";");
        DataSet inputs = new DataSet();
        inputs.addColumnValues("sdcid", 0, sdcid, ";");
        inputs.addColumnValues("keyid1", 0, keyid1, ";");
        inputs.addColumnValues("keyid2", 0, keyid2, ";");
        inputs.addColumnValues("keyid3", 0, keyid3, ";");
        inputs.addColumnValues("propertytreenodeid", 0, pTreeNodeIds, ";");
        inputs.padColumn("sdcid");
        if (sdcid.length() > 0 && keyid1.length() > 0) {
            DataSet ds = null;
            String sql = "select changelog.changelogid, changelog.changelogstatus, changelog.checkedoutbyuserid, changelog.checkedoutbydepartmentid,";
            sql = sql + "changelog.linksdcid, changelog.linkkeyid1, changelog.linkkeyid2, changelog.linkkeyid3, changelog.propertytreenodeid";
            if ("LV_ChangeLog".equals(sdcid)) {
                SafeSQL safeSQL = new SafeSQL();
                sql = sql + " from changelog where changelogid in (" + safeSQL.addIn(keyid1, ";") + ") and changelogstatus='Checked Out'";
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            } else {
                int keycolumns = Integer.parseInt(this.getSDCProcessor().getProperty(sdcid, "keycolumns"));
                try {
                    String rsetid = this.getDAMProcessor().createRSet(sdcid, keyid1, keyid2, keyid3);
                    sql = sql + " from changelog, rsetitems";
                    sql = sql + " where changelogstatus='Checked Out' and rsetitems.sdcid = changelog.linksdcid";
                    sql = sql + " and rsetitems.keyid1 = changelog.linkkeyid1";
                    if (keycolumns > 1) {
                        sql = sql + " and rsetitems.keyid2 = changelog.linkkeyid2";
                    }
                    if (keycolumns > 2) {
                        sql = sql + " and rsetitems.keyid3 = changelog.linkkeyid3";
                    }
                    sql = sql + " and rsetitems.rsetid = ?";
                    ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{rsetid});
                    this.getDAMProcessor().clearRSet(rsetid);
                }
                catch (SapphireException e) {
                    e.printStackTrace();
                }
            }
            if (ds != null) {
                if (ds.getRowCount() == 0) {
                    message = this.getTranslationProcessor().translate("Failed to Check In. Selected item is not Checked Out.");
                } else if (!"LV_ChangeLog".equals(sdcid)) {
                    HashMap<String, String> filterMap = new HashMap<String, String>();
                    for (int i = 0; i < inputs.getRowCount(); ++i) {
                        int findRow;
                        String linksdcid = inputs.getString(i, "sdcid", "");
                        filterMap.clear();
                        filterMap.put("linksdcid", linksdcid);
                        String key = "";
                        if (linksdcid.length() > 0) {
                            int keycolumns = Integer.parseInt(this.getSDCProcessor().getProperty(linksdcid, "keycolumns"));
                            key = inputs.getString(i, "keyid1", "");
                            filterMap.put("linkkeyid1", inputs.getString(i, "keyid1"));
                            if (keycolumns > 1) {
                                key = key + ", " + inputs.getString(i, "keyid2", "");
                                filterMap.put("linkkeyid2", inputs.getString(i, "keyid2"));
                            }
                            if (keycolumns > 2) {
                                key = key + ", " + inputs.getString(i, "keyid3", "");
                                filterMap.put("linkkeyid3", inputs.getString(i, "keyid3"));
                            }
                            if ("PropertyTree".equals(linksdcid) && inputs.getString(i, "propertytreenodeid", "").length() > 0) {
                                key = key + ", Node: " + inputs.getString(i, "propertytreenodeid");
                                filterMap.put("propertytreenodeid", inputs.getString(i, "propertytreenodeid"));
                            }
                        }
                        if ((findRow = ds.findRow(filterMap)) == -1) {
                            message = this.getTranslationProcessor().translate("Failed to Check In. Selected item is not Checked Out.") + " [" + key + "]";
                        } else {
                            String checkedoutbyuserid = ds.getString(findRow, "checkedoutbyuserid", "");
                            if (this.getConnectionProcessor().getSapphireConnection().getSysuserId().equals(checkedoutbyuserid)) continue;
                            String checkedoutbydepartmentid = ds.getString(findRow, "checkedoutbydepartmentid", "");
                            if (checkedoutbydepartmentid.length() > 0) {
                                if ((";" + this.getConnectionProcessor().getSapphireConnection().getDepartmentList() + ";").contains(";" + checkedoutbydepartmentid + ";")) continue;
                                message = this.getTranslationProcessor().translate("User is not allowed to check in selected item") + " [" + key + "]";
                            } else {
                                message = this.getTranslationProcessor().translate("User is not allowed to check in selected item") + " [" + key + "]";
                            }
                        }
                        break;
                    }
                } else {
                    for (int i = 0; i < ds.size(); ++i) {
                        String checkedoutbyuserid = ds.getString(i, "checkedoutbyuserid", "");
                        if (this.getConnectionProcessor().getSapphireConnection().getSysuserId().equals(checkedoutbyuserid)) continue;
                        String checkedoutbydepartmentid = ds.getString(i, "checkedoutbydepartmentid", "");
                        if (checkedoutbydepartmentid.length() > 0) {
                            if ((";" + this.getConnectionProcessor().getSapphireConnection().getDepartmentList() + ";").contains(";" + checkedoutbydepartmentid + ";")) continue;
                            String key = ds.getString(i, "linkkeyid1") + ", " + ds.getString(i, "linkkeyid2", "(null)") + ", " + ds.getString(i, "linkkeyid3", "(null)");
                            message = this.getTranslationProcessor().translate("User is not allowed to check in selected item") + " [" + key + "]";
                        } else {
                            String key = ds.getString(i, "linkkeyid1") + ", " + ds.getString(i, "linkkeyid2", "(null)") + ", " + ds.getString(i, "linkkeyid3", "(null)");
                            message = this.getTranslationProcessor().translate("User is not allowed to check in selected item") + " [" + key + "]";
                        }
                        break;
                    }
                }
            } else {
                message = this.getTranslationProcessor().translate("Failed fetching change log data. If problem persists, contant your Administrator.");
            }
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.print();
    }
}

