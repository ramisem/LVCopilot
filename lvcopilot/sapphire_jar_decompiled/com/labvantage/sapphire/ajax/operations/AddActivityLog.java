/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.ajax.operations;

import com.labvantage.sapphire.servlet.RequestProcessor;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;

public class AddActivityLog
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        try {
            String auditreason = request.getParameter("auditreason");
            String jsonString = request.getParameter("activityData");
            String clienttime = request.getParameter("clienttime");
            long serverclienttimeoffset = clienttime != null ? System.currentTimeMillis() - Long.parseLong(clienttime) : 0L;
            DataSet activityDataSet = new DataSet(new JSONObject(jsonString));
            HashMap<String, Object> props = new HashMap<String, Object>();
            props.put("reason", auditreason);
            props.put("activityDataSet", activityDataSet);
            props.put("serverclienttimeoffset", serverclienttimeoffset);
            new RequestProcessor(this.getConnectionid()).processRequest("com.labvantage.sapphire.ajax.operations.AddActivityLogPropertyHandler", props);
            this.write("Activity Audit Succeeded.");
        }
        catch (Exception e) {
            this.write("Failed to audit activity: " + e.getMessage());
        }
    }

    public static class ActivityLogData {
        private DataSet activityDataSet = new DataSet();
        private static final String[] activityColumns = new String[]{"__activitydt", "activitygroup", "activitytype", "sdcid", "keyid1", "keyid2", "keyid3", "detailtableid", "detailkeyvalues", "columnid", "fieldid", "oldvalue", "newvalue", "webpageid", "savetransaction"};

        public ActivityLogData() {
            for (int i = 0; i < activityColumns.length; ++i) {
                this.activityDataSet.addColumn(activityColumns[i], 0);
            }
        }

        public void addActivity(HashMap activityMap) {
            this.activityDataSet.add(activityMap);
        }

        public DataSet getActivityDataSet() {
            return this.activityDataSet;
        }
    }
}

