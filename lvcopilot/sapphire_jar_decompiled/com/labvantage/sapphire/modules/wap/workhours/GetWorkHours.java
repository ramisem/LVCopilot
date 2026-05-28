/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.modules.wap.workhours;

import com.labvantage.sapphire.modules.wap.workhours.WorkHours;
import java.util.ArrayList;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;

public class GetWorkHours
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "1: 1.1 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String shiftId = ajaxResponse.getRequestParameter("shiftid");
        String row = ajaxResponse.getRequestParameter("rowindex");
        String departmentid = ajaxResponse.getRequestParameter("departmentid");
        String coreHours = "";
        JSONObject job = new JSONObject();
        SafeSQL safeSQL = new SafeSQL();
        String sql = "select corehours from calendar where calendarid in (select calendarid from departmentshift where shiftid=" + safeSQL.addVar(shiftId) + " and departmentid=" + safeSQL.addVar(departmentid) + ")";
        DataSet dataSet = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (dataSet.getRowCount() > 0) {
            coreHours = dataSet.getString(0, "corehours");
            try {
                WorkHours workHours = WorkHours.getInstance(coreHours);
                job.put("coreHours", GetWorkHours.getShifts(workHours));
                job.put("coreHoursJson", workHours.toJSONString());
                job.put("rowindex", row);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        ajaxResponse.addCallbackArgument("retvalue", job.toString());
        ajaxResponse.print();
    }

    public static String getShifts(WorkHours workHours) {
        String shift = null;
        if (workHours.getStartHour() < 0 && workHours.getEndHour() < 0) {
            shift = "";
        } else {
            ArrayList<String> workingDayOfWeek = new ArrayList<String>();
            String[] strDays = new String[]{"Sunday", "Monday", "Tuesday", "Wednesday", "Thusday", "Friday", "Saturday"};
            for (int i = 1; i <= 7; ++i) {
                if (!workHours.isWorkingDayOfWeek(i)) continue;
                workingDayOfWeek.add(strDays[i - 1]);
            }
            String workingdays = workingDayOfWeek.toString().replaceAll("\\[", "").replaceAll("\\]", "");
            int start = workingdays.lastIndexOf(",");
            StringBuffer strBuffer = new StringBuffer();
            if (start > -1) {
                strBuffer.append(workingdays.substring(0, start));
                strBuffer.append(" and ");
                strBuffer.append(workingdays.substring(start + 1));
            } else {
                strBuffer.append(workingdays);
            }
            shift = "Shift starts at " + String.format("%02d", workHours.getStartHour()) + ":" + String.format("%02d", workHours.getStartMinute()) + " and ends at " + String.format("%02d", workHours.getEndHour()) + ":" + String.format("%02d", workHours.getEndMinute()) + " on " + strBuffer.toString();
        }
        return shift;
    }
}

