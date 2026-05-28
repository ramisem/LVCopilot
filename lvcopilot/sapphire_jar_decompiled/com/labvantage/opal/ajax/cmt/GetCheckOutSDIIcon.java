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

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.util.policy.CMTPolicy;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;

public class GetCheckOutSDIIcon
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        CMTPolicy cmtPolicy;
        String cmtenabled = "N";
        DataSet ds = null;
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String sdcid = ajaxResponse.getRequestParameter("sdcid", "");
        String keyid1 = ajaxResponse.getRequestParameter("keyid1", "");
        String keyid2 = ajaxResponse.getRequestParameter("keyid2", "");
        String keyid3 = ajaxResponse.getRequestParameter("keyid3", "");
        if (sdcid.length() > 0 && keyid1.length() > 0 && ("Y".equals((cmtPolicy = CMTPolicy.getPolicy(this.getConnectionid(), sdcid)).getChangeControlledFlag()) || "T".equals(cmtPolicy.getChangeControlledFlag()))) {
            cmtenabled = "Y";
            try {
                String sysuserid = this.getConnectionProcessor().getSapphireConnection().getSysuserId();
                List<String> userDepartmentList = OpalUtil.toList(this.getConnectionProcessor().getSapphireConnection().getDepartmentList(), ";");
                String rsetid = this.getDAMProcessor().createRSet(sdcid, keyid1, keyid2, keyid3);
                String sql = "select changelog.linksdcid sdcid, changelog.linkkeyid1 keyid1, changelog.linkkeyid2 keyid2, changelog.linkkeyid3 keyid3, changelog.checkedoutbydepartmentid, changelog.checkedoutbyuserid, '' imgsrc, '' imgtitle";
                sql = sql + " from changelog, rsetitems";
                sql = sql + " where changelog.linksdcid = rsetitems.sdcid";
                sql = sql + " and changelog.linkkeyid1 = rsetitems.keyid1";
                sql = sql + " and changelog.linkkeyid2 = rsetitems.keyid2";
                sql = sql + " and changelog.linkkeyid3 = rsetitems.keyid3";
                sql = sql + " and rsetitems.rsetid = ?";
                sql = sql + " and changelog.changelogstatus = 'Checked Out'";
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{rsetid});
                this.getDAMProcessor().clearRSet(rsetid);
                if (ds != null) {
                    for (int i = 0; i < ds.size(); ++i) {
                        String checkedoutbyuserid = ds.getString(0, "checkedoutbyuserid", "");
                        String checkedoutbydepartmentid = ds.getString(0, "checkedoutbydepartmentid", "");
                        String imgSrc = "";
                        String imgTitle = "";
                        if (checkedoutbydepartmentid.length() > 0) {
                            imgTitle = this.getTranslationProcessor().translate("Checked out to department") + " " + checkedoutbydepartmentid;
                            imgSrc = userDepartmentList.contains(checkedoutbydepartmentid) ? "WEB-CORE/images/svg/checkout_dept.svg" : "img src='WEB-CORE/images/svg/checkout_others_dept.svg";
                        } else if (checkedoutbyuserid.length() > 0) {
                            imgTitle = this.getTranslationProcessor().translate("Checked out by") + " " + checkedoutbyuserid;
                            imgSrc = sysuserid.equals(checkedoutbyuserid) ? "WEB-CORE/images/svg/checkout_user.svg" : "WEB-CORE/images/svg/checkout_others.svg";
                        }
                        if (imgSrc.length() <= 0) continue;
                        ds.setString(i, "imgsrc", imgSrc);
                        ds.setString(i, "imgtitle", imgTitle);
                    }
                }
            }
            catch (SapphireException e) {
                e.printStackTrace();
            }
        }
        ajaxResponse.addCallbackArgument("cmtenabled", cmtenabled);
        ajaxResponse.addCallbackArgument("data", ds != null ? ds : new DataSet());
        ajaxResponse.addCallbackArgument("keycolid1", this.getSDCProcessor().getProperty(sdcid, "keycolid1"));
        ajaxResponse.addCallbackArgument("keycolid2", this.getSDCProcessor().getProperty(sdcid, "keycolid2"));
        ajaxResponse.addCallbackArgument("keycolid3", this.getSDCProcessor().getProperty(sdcid, "keycolid3"));
        ajaxResponse.print();
    }
}

