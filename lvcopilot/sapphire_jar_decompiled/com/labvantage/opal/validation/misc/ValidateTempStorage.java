/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.validation.misc;

import com.labvantage.sapphire.admin.ddt.StorageUnitSDC;
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

public class ValidateTempStorage
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 54503 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String trackitemid;
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String msg = "";
        String sysuserid = this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()).getSysuserId();
        StringBuilder sql = new StringBuilder();
        sql.append("select count(storageunit.storageunitid) count from storageunit, s_physicalstore");
        sql.append(" where linksdcid='PhysicalStore'");
        sql.append(" and linkkeyid1=s_physicalstore.s_physicalstoreid");
        sql.append(" and s_physicalstore.storageclass='Temporary'");
        sql.append(" and s_physicalstore.departmentid in (select departmentid");
        sql.append(" from departmentsysuser");
        sql.append(" where sysuserid=?)");
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{sysuserid});
        if (ds != null && ds.getInt(0, "count") == 0) {
            msg = "You do not have any Temporary storage locations";
        }
        if ((trackitemid = ajaxResponse.getRequestParameter("trackitemid")) != null && trackitemid.length() > 0) {
            try {
                this.validateTempStorage(trackitemid);
            }
            catch (SapphireException e) {
                msg = e.getMessage();
            }
        } else {
            msg = "Track Item Id not found for the selected item(s)";
        }
        ajaxResponse.addCallbackArgument("data", msg);
        ajaxResponse.addCallbackArgument("url", ajaxResponse.getRequestParameter("url"));
        ajaxResponse.print();
    }

    private void validateTempStorage(String trackitemid) throws SapphireException {
        DataSet ds;
        StringBuffer sql = new StringBuffer();
        if (StringUtil.split(trackitemid = StringUtil.replaceAll(trackitemid, "%3B", ";"), ";").length > 750) {
            String rsetid = this.getDAMProcessor().createRSet("TrackItemSDC", trackitemid, null, null);
            sql.append("select t.currentstorageunitid");
            sql.append(" from trackitem t, rsetitems r");
            sql.append(" where t.trackitemid = r.keyid1");
            sql.append(" and r.rsetid = ?");
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{rsetid});
            this.getDAMProcessor().clearRSet(rsetid);
        } else {
            SafeSQL safeSQL = new SafeSQL();
            sql.append("select currentstorageunitid from trackitem");
            sql.append(" where trackitemid in (").append(safeSQL.addIn(trackitemid, ";")).append(")");
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        }
        if (ds != null) {
            for (int i = 0; i < ds.size(); ++i) {
                String storageNode;
                String currentStorageUnitId = ds.getValue(i, "currentstorageunitid", "");
                if (currentStorageUnitId.length() <= 0 || (storageNode = StorageUnitSDC.getStorageNodeBySDC(this.getQueryProcessor(), currentStorageUnitId, "LV_Box")).length() != 0) continue;
                throw new SapphireException(this.getTranslationProcessor().translate("Selected item(s) can not be moved to temporary storage"));
            }
        }
    }
}

