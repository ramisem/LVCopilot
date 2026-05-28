/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.JspWriter
 */
package com.labvantage.sapphire.stability;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Set;
import javax.servlet.jsp.JspWriter;
import sapphire.util.DataSet;

public class EventTaskMapper {
    static final String PLAN_ID = "scheduleplanid";
    static final String PLAN_ITEM_ID = "scheduleplanitemid";
    static final String PLAN_ITEM_DESC = "scheduleplanitemdesc";
    static final String EVENT_DT = "eventdt";
    static final String EVENT_STATUS = "evetnstatus";
    static final String STUDY_ID = "studyid";
    private int maxCount = 0;
    private HashMap map = new HashMap();
    private HashMap allCountMap = new HashMap();
    private HashMap studyCountMap = new HashMap();
    DateFormat sdf = DateFormat.getDateInstance(3);
    DateFormat sdfTime = DateFormat.getTimeInstance(3);
    public int first = 1;
    public int second = 2;
    public int third = 3;
    public int fourth = 4;
    public int fifth = 5;
    private boolean isSingleStudy = false;
    private Calendar firstEventDt = null;
    private Calendar lastEventDt = null;

    public EventTaskMapper(DataSet ds) {
        int count = ds.getRowCount();
        ds.sort(EVENT_DT);
        for (int i = 0; i < count; ++i) {
            String keyCal;
            ArrayList list;
            Calendar tempCal = ds.getCalendar(i, EVENT_DT);
            if (i == 0) {
                this.firstEventDt = (Calendar)tempCal.clone();
            }
            if (i == count - 1) {
                this.lastEventDt = (Calendar)tempCal.clone();
            }
            if ((list = (ArrayList)this.map.get(keyCal = this.sdf.format(tempCal.getTime()))) == null) {
                list = new ArrayList();
                list.add(ds.get(i));
                this.map.put(keyCal, list);
            } else {
                list.add(ds.get(i));
            }
            if (list.size() > this.maxCount) {
                this.maxCount = list.size();
            }
            this.allCountMap.put(keyCal, new Integer(list.size()));
        }
    }

    public int getMaxCount() {
        return this.maxCount;
    }

    public int getCount(String eventDt) {
        Integer count = (Integer)this.allCountMap.get(eventDt);
        if (count == null) {
            return 0;
        }
        return count;
    }

    public HashMap getCountMap() {
        return this.allCountMap;
    }

    public String getDetail(String eventDt) {
        ArrayList list = (ArrayList)this.map.get(eventDt);
        if (list != null) {
            StringBuffer detail = new StringBuffer();
            for (int i = 0; i < list.size(); ++i) {
                HashMap event = (HashMap)list.get(i);
                detail.append(this.sdfTime.format(((Calendar)event.get(EVENT_DT)).getTime()) + " ");
                String status = (String)event.get("eventstatus");
                String display = status.equals("S") ? "Scheduled" : (status.equals("D") ? "Done" : (status.equals("T") ? "Test" : (status.equals("E") ? "Error" : "")));
                detail.append(display);
                detail.append("\n");
            }
            return detail.toString();
        }
        return "";
    }

    public void getCountJSArray(JspWriter out, String studyid) throws IOException {
        out.print("var t = new Array();\n");
        Set keySet = this.allCountMap.keySet();
        for (String key : keySet) {
            out.print("t['" + key + "']=" + this.allCountMap.get(key) + ";");
        }
    }

    public void generateGetColorJs(JspWriter out) throws IOException {
        if (this.maxCount > 5) {
            this.fifth = this.maxCount % 5 == 0 ? this.maxCount : (this.maxCount / 5 + 1) * 5;
            this.fourth *= this.fifth / 5;
            this.third *= this.fifth / 5;
            this.second *= this.fifth / 5;
            this.first *= this.fifth / 5;
        }
        out.write("function getColor ( count, isAllert ) {\n");
        out.write("if ( count > 0 && count <= " + this.first + " ) return 'yellow';\n");
        out.write("else if ( count > " + this.first + " && count <= " + this.second + " ) return '#FFCC00';\n");
        out.write("else if ( count > " + this.second + " && count <= " + this.third + " ) return '#FF9900';\n");
        out.write("else if ( count > " + this.third + " && count <= " + this.fourth + " ) return '#FF6600';\n");
        out.write("else if ( count > " + this.fourth + " && count <= " + this.fifth + " ) return 'red'\n;");
        out.write("}");
    }

    public String getEventYearList() {
        StringBuffer sb = new StringBuffer();
        if (this.firstEventDt != null && this.lastEventDt != null) {
            sb.append(this.firstEventDt.get(1));
            Calendar tempEventDt = (Calendar)this.firstEventDt.clone();
            while (tempEventDt.get(1) < this.lastEventDt.get(1)) {
                tempEventDt.add(1, 1);
                sb.append(";" + tempEventDt.get(1));
            }
        } else {
            sb.append("" + Calendar.getInstance().get(1));
        }
        return sb.toString();
    }
}

