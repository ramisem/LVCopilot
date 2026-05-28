/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.wap.actions;

import com.labvantage.sapphire.modules.wap.activity.Activity;
import com.labvantage.sapphire.modules.wap.activity.ContextMap;
import com.labvantage.sapphire.modules.wap.activity.WAPCommands;
import com.labvantage.sapphire.modules.wap.activity.WAPConstants;
import com.labvantage.sapphire.modules.wap.activity.WAPSelector;
import java.util.ArrayList;
import java.util.List;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class AddReservationActivityWork
extends BaseAction
implements WAPConstants {
    public static final String PROPERTY_TYPE = "type";
    public static final String PROPERTY_TYPE_REQUEST = "request";
    public static final String PROPERTY_REQUESTID = "requestid";
    public static final String PROPERTY_REQUESTITEMID = "requestitemid";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String type = properties.getProperty(PROPERTY_TYPE);
        if (type.equals(PROPERTY_TYPE_REQUEST)) {
            String requestid = properties.getProperty(PROPERTY_REQUESTID);
            String fetchRequestItemid = properties.getProperty(PROPERTY_REQUESTITEMID);
            if (requestid.length() == 0) {
                throw new ActionException("No request provided");
            }
            WAPCommands commands = new WAPCommands(this.getConnectionid());
            WAPSelector selector = new WAPSelector(this.getConnectionid());
            List<Activity> activities = selector.getRequestReservations(requestid, fetchRequestItemid, "", "", "", "");
            DataSet activitiyTypes = new DataSet();
            for (Activity activity : activities) {
                int row = activitiyTypes.addRow();
                activitiyTypes.setNumber(row, "_index", row);
                activitiyTypes.setString(row, "activityid", activity.getActivityid());
                activitiyTypes.setString(row, "reservationtype", activity.getReservationType());
                activitiyTypes.setString(row, "reservationcontext", activity.getReservationContext());
            }
            activitiyTypes.sort("reservationtype,reservationcontext");
            ArrayList<DataSet> activityTypeGroups = activitiyTypes.getGroupedDataSets("reservationtype,reservationcontext");
            for (DataSet groupitem : activityTypeGroups) {
                String workitemversionid;
                String workitemid;
                String reservationType = groupitem.getString(0, "reservationtype");
                String reservationContext = groupitem.getString(0, "reservationcontext");
                ArrayList<String> activitylist = new ArrayList<String>();
                ArrayList workSDIs = null;
                String worksdcid = "";
                ContextMap map = new ContextMap(reservationContext);
                if (reservationType.equals("RequestItemSample")) {
                    String requestitemid = map.get("RI");
                    workSDIs = this.getQueryProcessor().getPreparedSqlDataSet("SELECT linkkeyid1 workkeyid1 FROM s_requestitemdetail WHERE requestitemid=?", (Object[])new String[]{requestitemid});
                    worksdcid = "Sample";
                } else if (reservationType.equals("RequestItemWorkItem")) {
                    String requestitemid = map.get("RI");
                    workitemid = map.get("WI");
                    workitemversionid = map.get("WIV");
                    workSDIs = this.getQueryProcessor().getPreparedSqlDataSet("SELECT sdiworkitemid workkeyid1 FROM sdiworkitem, s_requestitemdetail WHERE s_requestitemdetail.requestitemid=? AND sdiworkitem.sdcid = s_requestitemdetail.linksdcid AND sdiworkitem.keyid1 = s_requestitemdetail.linkkeyid1 AND sdiworkitem.workitemid=? AND sdiworkitem.workitemversionid=?", (Object[])new String[]{requestitemid, workitemid, workitemversionid});
                    worksdcid = "SDIWorkItem";
                } else if (reservationType.equals("RequestItemWorkItemItem")) {
                    String requestitemid = map.get("RI");
                    workitemid = map.get("WI");
                    workitemversionid = map.get("WIV");
                    String paramlistid = map.get("PL");
                    String paramlistversionid = map.get("PLV");
                    String variantid = map.get("V");
                    workSDIs = this.getQueryProcessor().getPreparedSqlDataSet("SELECT sdidata.sdidataid workkeyid1 FROM sdidata, s_requestitemdetail WHERE s_requestitemdetail.requestitemid=? AND sdidata.sdcid = s_requestitemdetail.linksdcid AND sdidata.keyid1 = s_requestitemdetail.linkkeyid1 AND sdidata.sourceworkitemid=? AND sdidata.paramlistid=? AND sdidata.variantid=?", (Object[])new String[]{requestitemid, workitemid, paramlistid, variantid});
                    worksdcid = "DataSet";
                }
                if (workSDIs == null || workSDIs.size() <= 0) continue;
                int fromcount = 0;
                for (int i = 0; i < groupitem.size(); ++i) {
                    Activity activity = activities.get(groupitem.getInt(i, "_index"));
                    activitylist.add(activity.getActivityid());
                    int size = activity.getActivitySize();
                    for (int j = fromcount; j < fromcount + size; ++j) {
                        ((DataSet)workSDIs).setString(j, "activityid", activity.getActivityid());
                    }
                    fromcount += size;
                }
                PropertyList actionProps = new PropertyList();
                actionProps.setProperty("sdcid", "LV_Activity");
                actionProps.setProperty("linkid", "Activity Work List");
                actionProps.setProperty("keyid1", ((DataSet)workSDIs).getColumnValues("activityid", ";"));
                actionProps.setProperty("worksdcid", StringUtil.repeat(worksdcid, workSDIs.size(), ";"));
                actionProps.setProperty("workkeyid1", ((DataSet)workSDIs).getColumnValues("workkeyid1", ";"));
                actionProps.setProperty("workkeyid2", StringUtil.repeat("__null", workSDIs.size(), ";"));
                actionProps.setProperty("workkeyid3", StringUtil.repeat("__null", workSDIs.size(), ";"));
                this.getActionProcessor().processAction("AddSDIDetail", "1", actionProps);
                for (String activityid : activitylist) {
                    Activity editActivity = new Activity();
                    editActivity.setActivityid(activityid);
                    editActivity.setIsReservation(false);
                    commands.editActivity(editActivity, false);
                }
            }
        } else {
            throw new ActionException("Unrecognized reservation type");
        }
    }
}

