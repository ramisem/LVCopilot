/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.modules.wap.activity.Activity;
import com.labvantage.sapphire.modules.wap.activity.WAPCommands;
import java.util.Calendar;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class LV_Activity
extends BaseSDCRules {
    static final String LABVANTAGE_CVS_ID = "$Revision: 57021 $";

    @Override
    public void preEdit(SDIData sdidata, PropertyList actionProps) throws SapphireException {
        boolean recalcdates;
        DataSet dsPrimary = sdidata.getDataset("primary");
        boolean bl = actionProps.containsKey("_recalcdates") ? actionProps.getProperty("_recalcdates", "Y").equalsIgnoreCase("Y") || actionProps.getProperty("_recalcdates", "Y").equalsIgnoreCase("(null)") : (recalcdates = false);
        if (recalcdates && dsPrimary.getRowCount() == 1) {
            int d;
            DateTimeUtil dtu = new DateTimeUtil(this.getConnectionInfo());
            Activity toedit = new Activity();
            toedit.setStartDateInstantUTC(dtu.getInstant(actionProps.getProperty("startdt", "(null)")));
            toedit.setIsEndDateFixed(actionProps.getProperty("enddtfixedflag", dsPrimary.getValue(0, "enddtfixedflag", "N")).equalsIgnoreCase("Y"));
            toedit.setEndDateInstantUTC(dtu.getInstant(toedit.isEndDateFixed() ? actionProps.getProperty("enddt", "(null)") : "(null)"));
            toedit.setStartRangeInstantUTC(dtu.getInstant(actionProps.getProperty("startrangedt", "(null)")));
            toedit.setEndRangeInstantUTC(dtu.getInstant(actionProps.getProperty("endrangedt", "(null)")));
            WAPCommands wapCommands = new WAPCommands(this.getConnectionId());
            Activity current = wapCommands.getActivityDetails(dsPrimary.getValue(0, "activityid", ""));
            boolean calcdates = wapCommands.prepareEditActivityDates(toedit, current, actionProps);
            actionProps.setProperty("_recalcdates", calcdates ? "Y" : "N");
            Calendar start = null;
            Calendar end = null;
            if (actionProps.getProperty("startdt").length() > 0 && !actionProps.getProperty("startdt").equalsIgnoreCase("(null)")) {
                start = dtu.getCalendar(actionProps.getProperty("startdt"));
            } else if (actionProps.getProperty("enddtfixedflag").equalsIgnoreCase("Y") || actionProps.getProperty("timemode").equalsIgnoreCase("Fixed")) {
                throw new SapphireException(this.getTranslationProcessor().translate("No fixed start date supplied."));
            }
            if (!calcdates) {
                if (actionProps.getProperty("enddt").length() > 0 && !actionProps.getProperty("enddt").equalsIgnoreCase("(null)")) {
                    end = dtu.getCalendar(actionProps.getProperty("enddt"));
                } else if (actionProps.getProperty("enddtfixedflag").equalsIgnoreCase("Y") || actionProps.getProperty("timemode").equalsIgnoreCase("Fixed")) {
                    throw new SapphireException(this.getTranslationProcessor().translate("No fixed end date supplied."));
                }
                if (end != null && start != null) {
                    d = end.compareTo(start);
                    if (d < 0) {
                        throw new SapphireException(this.getTranslationProcessor().translate("Fixed start date cannot be after fixed end date."));
                    }
                    if (d == 0) {
                        throw new SapphireException(this.getTranslationProcessor().translate("Fixed start date cannot be equal to fixed end date."));
                    }
                }
            }
            start = null;
            end = null;
            if (actionProps.getProperty("startrangedt").length() > 0 && !actionProps.getProperty("startrangedt").equalsIgnoreCase("(null)")) {
                start = dtu.getCalendar(actionProps.getProperty("startrangedt"));
            } else if (actionProps.getProperty("timemode").equalsIgnoreCase("Floating")) {
                throw new SapphireException(this.getTranslationProcessor().translate("No start date supplied."));
            }
            if (!calcdates) {
                if (actionProps.getProperty("endrangedt").length() > 0 && !actionProps.getProperty("endrangedt").equalsIgnoreCase("(null)")) {
                    end = dtu.getCalendar(actionProps.getProperty("endrangedt"));
                } else if (actionProps.getProperty("timemode").equalsIgnoreCase("Floating")) {
                    throw new SapphireException(this.getTranslationProcessor().translate("No end date supplied."));
                }
                if (end != null && start != null) {
                    d = end.compareTo(start);
                    if (d < 0) {
                        throw new SapphireException(this.getTranslationProcessor().translate("Start date of range cannot be after end date."));
                    }
                    if (d == 0) {
                        throw new SapphireException(this.getTranslationProcessor().translate("Start date of range cannot be equal to end date."));
                    }
                }
            }
            dsPrimary.setValue(0, "startdt", actionProps.getProperty("startdt"));
            dsPrimary.setValue(0, "enddt", actionProps.getProperty("enddt", "(null)"));
            dsPrimary.setValue(0, "enddtfixedflag", actionProps.getProperty("enddtfixedflag").equalsIgnoreCase("(null)") ? "" : actionProps.getProperty("enddtfixedflag"));
            dsPrimary.setValue(0, "startrangedt", actionProps.getProperty("startrangedt"));
            dsPrimary.setValue(0, "endrangedt", actionProps.getProperty("endrangedt"));
        }
    }

    @Override
    public void postEdit(SDIData sdidata, PropertyList actionProps) throws SapphireException {
        if (actionProps.getProperty("_recalcdates", "").equalsIgnoreCase("Y")) {
            DataSet dsPrimary = sdidata.getDataset("primary");
            WAPCommands wapCommands = new WAPCommands(this.getConnectionId());
            Activity activity = wapCommands.getActivityDetails(dsPrimary, 0);
            wapCommands.syncMaxDurationEndDateDueDateCompleteCount(activity);
        }
    }
}

