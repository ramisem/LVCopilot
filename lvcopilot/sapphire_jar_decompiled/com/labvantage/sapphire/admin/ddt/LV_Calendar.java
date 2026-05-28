/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  biweekly.ICalendar
 *  biweekly.io.TimezoneAssignment
 */
package com.labvantage.sapphire.admin.ddt;

import biweekly.ICalendar;
import biweekly.io.TimezoneAssignment;
import com.labvantage.sapphire.modules.wap.calendar.LVCalendar;
import java.util.TimeZone;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class LV_Calendar
extends BaseSDCRules {
    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.size(); ++i) {
            if (!primary.getValue(i, "sourceflag").equals("E")) continue;
            String externalOptions = primary.getValue(i, "externaloptions");
            ICalendar iCalendar = LVCalendar.getICalendar(externalOptions);
            TimeZone timeZone = TimeZone.getDefault();
            if (iCalendar != null) {
                if (iCalendar.getTimezoneInfo().getDefaultTimezone() != null) {
                    timeZone = iCalendar.getTimezoneInfo().getDefaultTimezone().getTimeZone();
                } else if (iCalendar.getTimezoneInfo().getTimezones() != null && iCalendar.getTimezoneInfo().getTimezones().size() > 0) {
                    timeZone = ((TimezoneAssignment)iCalendar.getTimezoneInfo().getTimezones().toArray()[0]).getTimeZone();
                }
            }
            primary.setString(i, "timezone", timeZone.getID());
        }
    }
}

