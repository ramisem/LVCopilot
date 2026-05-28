/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletRequest
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.scheduler;

import com.labvantage.sapphire.BaseAccessor;
import com.labvantage.sapphire.scheduler.SchedulerAdminPropertyHandler;
import com.labvantage.sapphire.stability.ScheduleGrid;
import com.labvantage.sapphire.stability.StabilityGridPropertyHandler;
import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import javax.servlet.ServletRequest;
import javax.servlet.jsp.PageContext;
import org.w3c.dom.Node;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.xml.DOMUtil;
import sapphire.xml.PropertyList;

public class SchedulerAdminProcessor
extends BaseAccessor {
    private ServletRequest request = null;

    public SchedulerAdminProcessor(PageContext pageContext) {
        super(pageContext);
        this.request = pageContext.getRequest();
    }

    public SchedulerAdminProcessor(String connectionid) {
        super(connectionid);
    }

    public SchedulerAdminProcessor(File rakFile, String connectionid) {
        super(rakFile, connectionid);
    }

    public void handleWebRequest() throws Exception {
        PropertyList props = HttpUtil.getRequestPropertyList(this.request);
        String refsdcid = this.request.getParameter("sdcid") != null ? this.request.getParameter("sdcid") : "";
        String refkeyid1 = this.request.getParameter("keyid1") != null ? this.request.getParameter("keyid1") : "";
        String refkeyid2 = this.request.getParameter("keyid2") != null ? this.request.getParameter("keyid2") : "";
        String refkeyid3 = this.request.getParameter("keyid3") != null ? this.request.getParameter("keyid3") : "";
        String scheduleplannodedesc = this.request.getParameter("nodename") != null ? this.request.getParameter("nodename") : "";
        props.setProperty("refsdcid", refsdcid);
        props.setProperty("refkeyid1", refkeyid1);
        props.setProperty("refkeyid2", refkeyid2);
        props.setProperty("refkeyid3", refkeyid3);
        props.setProperty("scheduleplannodedesc", scheduleplannodedesc);
        this.getRequestManager().processRequest(this.getConnectionid(), SchedulerAdminPropertyHandler.class.getName(), props);
    }

    public void processScheduleAdmin(PropertyList props) throws Exception {
        try {
            if (local) {
                this.getLocalAccessManager().processRequest(this.getConnectionid(), SchedulerAdminPropertyHandler.class.getName(), props);
            } else {
                this.getRemoteAccessManager().processRequest(this.getConnectionid(), SchedulerAdminPropertyHandler.class.getName(), props);
            }
        }
        catch (Exception e) {
            this.setError("Failed to process schedule admin. Exception: " + e.getMessage(), e);
        }
    }

    public String loadItemValueTree(String scheduleplanid, String scheduleplanitemid) throws Exception {
        PropertyList props = new PropertyList();
        props.setProperty("scheduleplanid", scheduleplanid);
        props.setProperty("scheduleplanitemid", scheduleplanitemid);
        props.setProperty("mode", "getvaluetree");
        PropertyList returnProps = (PropertyList)this.getRequestManager().processRequest(this.getConnectionid(), SchedulerAdminPropertyHandler.class.getName(), props);
        return returnProps.getProperty("valuetree");
    }

    public void saveItemValueTree(String scheduleplanid, String scheduleplanitemid, Node propertylist) throws Exception {
        StringBuffer output = new StringBuffer();
        output.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append(DOMUtil.toString(propertylist));
        PropertyList props = new PropertyList();
        props.setProperty("scheduleplanid", scheduleplanid);
        props.setProperty("scheduleplanitemid", scheduleplanitemid);
        props.setProperty("mode", "setvaluetree");
        props.setProperty("valuetree", output.toString());
        this.getRequestManager().processRequest(this.getConnectionid(), SchedulerAdminPropertyHandler.class.getName(), props);
    }

    public void saveItemValueTree(String scheduleplanid, String scheduleplanitemid, String propertyList, String propertytreeid) throws Exception {
        StringBuffer output = new StringBuffer();
        output.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append(propertyList);
        PropertyList props = new PropertyList();
        props.setProperty("scheduleplanid", scheduleplanid);
        props.setProperty("scheduleplanitemid", scheduleplanitemid);
        props.setProperty("mode", "setvaluetree");
        props.setProperty("valuetree", output.toString());
        this.getRequestManager().processRequest(this.getConnectionid(), SchedulerAdminPropertyHandler.class.getName(), props);
        String sql = "UPDATE scheduleplanitem SET propertytreeid=? WHERE scheduleplanid=? AND scheduleplanitemid=?";
        this.getQueryManager().execPreparedUpdate(this.getConnectionid(), sql, new Object[]{propertytreeid, scheduleplanid, scheduleplanitemid});
    }

    public String loadConditionValueTree(String scheduleplanid, String conditionid, String propertytreeid) throws Exception {
        PropertyList props = new PropertyList();
        props.setProperty("scheduleplanid", scheduleplanid);
        props.setProperty("conditionid", conditionid);
        props.setProperty("propertytreeid", propertytreeid);
        props.setProperty("mode", "getconditionvaluetree");
        PropertyList returnProps = (PropertyList)this.getRequestManager().processRequest(this.getConnectionid(), SchedulerAdminPropertyHandler.class.getName(), props);
        return returnProps.getProperty("valuetree");
    }

    public String loadPlanValueTree(String scheduleplanid, String propertytreeid) throws Exception {
        PropertyList props = new PropertyList();
        props.setProperty("scheduleplanid", scheduleplanid);
        props.setProperty("propertytreeid", propertytreeid);
        props.setProperty("mode", "getplanvaluetree");
        PropertyList returnProps = (PropertyList)this.getRequestManager().processRequest(this.getConnectionid(), SchedulerAdminPropertyHandler.class.getName(), props);
        return returnProps.getProperty("valuetree");
    }

    public String getPlanValueTreeExtendNode(String scheduleplanid, String propertytreeid) throws Exception {
        PropertyList props = new PropertyList();
        props.setProperty("scheduleplanid", scheduleplanid);
        props.setProperty("propertytreeid", propertytreeid);
        props.setProperty("mode", "getplanvaluetreeextendnode");
        PropertyList returnProps = (PropertyList)this.getRequestManager().processRequest(this.getConnectionid(), SchedulerAdminPropertyHandler.class.getName(), props);
        return returnProps.getProperty("extendnodeid");
    }

    public int saveGrid(ScheduleGrid grid) throws Exception {
        HashMap<String, ScheduleGrid> props = new HashMap<String, ScheduleGrid>();
        props.put("grid", grid);
        try {
            HashMap hashMap = this.getRequestManager().processRequest(this.getConnectionid(), StabilityGridPropertyHandler.class.getName(), props);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    public DataSet getEvents(PropertyList props) throws Exception {
        props.setProperty("mode", "getscheduleevents");
        HashMap returnProps = this.getRequestManager().processRequest(this.getConnectionid(), SchedulerAdminPropertyHandler.class.getName(), props);
        DataSet eventDs = (DataSet)returnProps.get("eventlist");
        return eventDs;
    }

    public DataSet getExcludes(PropertyList props) throws Exception {
        props.setProperty("mode", "getscheduleexcludes");
        HashMap returnProps = this.getRequestManager().processRequest(this.getConnectionid(), SchedulerAdminPropertyHandler.class.getName(), props);
        DataSet eventDs = (DataSet)returnProps.get("eventlist");
        return eventDs;
    }

    public String moveEvent(PropertyList props) throws Exception {
        HashMap returnProps = this.getRequestManager().processRequest(this.getConnectionid(), SchedulerAdminPropertyHandler.class.getName(), props);
        String result = (String)returnProps.get("result");
        return result;
    }

    public String executeEvent(PropertyList props) throws Exception {
        props.setProperty("mode", "executenow");
        HashMap returnProps = this.getRequestManager().processRequest(this.getConnectionid(), SchedulerAdminPropertyHandler.class.getName(), props);
        String result = (String)returnProps.get("result");
        return result;
    }

    public String changeEventStatus(PropertyList props) throws Exception {
        props.setProperty("mode", "changeeventstatus");
        HashMap returnProps = this.getRequestManager().processRequest(this.getConnectionid(), SchedulerAdminPropertyHandler.class.getName(), props);
        String result = (String)returnProps.get("result");
        return result;
    }

    public HashMap<String, Calendar> getPlanItemGracePerioid(PropertyList props) throws Exception {
        props.put("mode", "getplanitemgrcperiod");
        return this.getRequestManager().processRequest(this.getConnectionid(), SchedulerAdminPropertyHandler.class.getName(), props);
    }
}

