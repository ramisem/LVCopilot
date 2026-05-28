/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.stability;

import com.labvantage.sapphire.stability.ScheduleGrid;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.FormatUtil;
import sapphire.util.SafeSQL;

public class GetStudyContainers
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 55322 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String studyid = ajaxResponse.getRequestParameter("studyid");
        FormatUtil fmtUtil = FormatUtil.getInstance(this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()));
        String strContainerSize = ajaxResponse.getRequestParameter("containersize");
        if (strContainerSize == null || strContainerSize.length() == 0) {
            strContainerSize = "0";
        }
        double containerSize = fmtUtil.parseBigDecimal(strContainerSize).doubleValue();
        String containerUnits = ajaxResponse.getRequestParameter("containerunits");
        String ppFlag = ajaxResponse.getRequestParameter("partialpullflag");
        boolean partialPullFlag = ppFlag != null && !"".equals(ppFlag) && !ppFlag.equals("N") && !ppFlag.equals("X");
        StringBuffer log = new StringBuffer();
        JSONArray jsonDataSet = new JSONArray();
        if (studyid.length() > 0) {
            SafeSQL safeSQL = new SafeSQL();
            String sql = "SELECT scheduleplanid FROM study_scheduleplan WHERE studyid = " + safeSQL.addVar(studyid);
            DataSet plans = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            for (int plan = 0; plan < plans.size(); ++plan) {
                String planid = plans.getString(plan, "scheduleplanid");
                try {
                    ScheduleGrid grid = new ScheduleGrid(this.getConnectionId());
                    grid.retrieve(planid);
                    grid.setPartialDistribution("X".equalsIgnoreCase(ppFlag));
                    for (int i = 0; i < grid.conditionAxis.items.size(); ++i) {
                        String conditionid = grid.conditionAxis.getId(i);
                        int containers = grid.conditionAxis.getContainersForCondition(conditionid, containerSize, containerUnits, partialPullFlag, false, log);
                        int totalContainers = grid.conditionAxis.getContainersForCondition(conditionid, containerSize, containerUnits, partialPullFlag, true, log);
                        JSONObject jo = new JSONObject();
                        jo.put("planid", planid);
                        jo.put("conditionid", conditionid);
                        jo.put("containers", "" + containers);
                        jo.put("totalcontainers", "" + totalContainers);
                        jsonDataSet.put(jo);
                    }
                    continue;
                }
                catch (Exception e) {
                    ajaxResponse.setError("Unable to load details for plan " + planid, e);
                }
            }
            ajaxResponse.addCallbackArgument("containers", jsonDataSet);
        } else {
            ajaxResponse.setError("Expression property not defined for service!");
        }
        ajaxResponse.print();
    }
}

