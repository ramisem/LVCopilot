/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.modules.wap.activity;

import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.wap.AddActivityWorkSDI;
import com.labvantage.sapphire.actions.wap.CreateActivity;
import com.labvantage.sapphire.actions.wap.SetActivityResource;
import com.labvantage.sapphire.modules.wap.CalendarConverter;
import com.labvantage.sapphire.modules.wap.activity.Activity;
import com.labvantage.sapphire.modules.wap.activity.ActivityClassHandler;
import com.labvantage.sapphire.modules.wap.activity.AssignmentPage;
import com.labvantage.sapphire.modules.wap.activity.AssignmentPageResourceContainer;
import com.labvantage.sapphire.modules.wap.activity.AssignmentPageResourceData;
import com.labvantage.sapphire.modules.wap.activity.ContextMap;
import com.labvantage.sapphire.modules.wap.activity.NewWorkDetails;
import com.labvantage.sapphire.modules.wap.activity.WAPAvailability;
import com.labvantage.sapphire.modules.wap.activity.WAPAvailabilityOptions;
import com.labvantage.sapphire.modules.wap.activity.WAPAvailabilitySelector;
import com.labvantage.sapphire.modules.wap.activity.WAPCommands;
import com.labvantage.sapphire.modules.wap.activity.WAPSelector;
import com.labvantage.sapphire.modules.wap.calendar.CalendarFactory;
import com.labvantage.sapphire.modules.wap.calendar.CalendarPage;
import com.labvantage.sapphire.modules.wap.calendar.LVCalendar;
import com.labvantage.sapphire.modules.wap.workhours.WorkHours;
import com.labvantage.sapphire.pageelements.controls.Image;
import com.labvantage.sapphire.pageelements.controls.Tab;
import com.labvantage.sapphire.pageelements.controls.TabGroup;
import com.labvantage.sapphire.services.SapphireConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import javax.servlet.jsp.PageContext;
import org.json.JSONArray;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeHTML;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class AssignmentPageUtil {
    public static String getShortDesc(String desc) {
        String out = desc;
        if (desc.length() > 18) {
            int m = desc.length() % 2;
            String p1 = desc.substring(0, m == 0 ? desc.length() / 2 : desc.length() / 2 + 1);
            String p2 = desc.substring(m == 0 ? desc.length() / 2 + 1 : desc.length() / 2 + 2);
            if (p1.length() > 8) {
                p1 = p1.substring(0, 7).trim();
            }
            if (p2.length() > 8) {
                p2 = p2.substring(p2.length() - 7).trim();
            }
            out = p1 + "..." + p2;
        }
        return out;
    }

    static String getEventDisplayLabel(WAPCommands wapCommands, SDCProcessor sdcProcessor, String activityLabelDisplayOp, Activity activity, ZoneId displayTimeZone, M18NUtil m18) {
        int row;
        HashMap<String, String> find;
        DataSet resources;
        String text = activityLabelDisplayOp;
        text = StringUtil.replaceAll(text, "[label]", activity.getLabel());
        text = StringUtil.replaceAll(text, "[activitylabel]", activity.getLabel());
        text = StringUtil.replaceAll(text, "[id]", activity.getActivityid());
        text = StringUtil.replaceAll(text, "[activityid]", activity.getActivityid());
        text = StringUtil.replaceAll(text, "[class]", activity.getActivityClass());
        text = StringUtil.replaceAll(text, "[activityclass]", activity.getActivityClass());
        text = StringUtil.replaceAll(text, "[worksdcid]", activity.getWorksdcid());
        text = StringUtil.replaceAll(text, "[timemode]", activity.getTimeMode());
        text = StringUtil.replaceAll(text, "[status]", activity.getStatus());
        text = StringUtil.replaceAll(text, "[activitystatus]", activity.getStatus());
        text = StringUtil.replaceAll(text, "[size]", activity.getActivitySize() + "");
        text = StringUtil.replaceAll(text, "[activitysize]", activity.getActivitySize() + "");
        text = StringUtil.replaceAll(text, "[maxactivitysize]", activity.getMaxActivitySize() + "");
        text = StringUtil.replaceAll(text, "[maxsize]", activity.getMaxActivitySize() + "");
        text = StringUtil.replaceAll(text, "[completecount]", activity.getWorkCompleteCount() + "");
        text = StringUtil.replaceAll(text, "[workduedt]", activity.getWorkDuedt() == null ? "" : m18.getDateFormatter(displayTimeZone).format(activity.getWorkDuedt()));
        DateFormat defaultDateFormat = m18.getDefaultDateFormat();
        if (text.contains("[maxduration]")) {
            text = StringUtil.replaceAll(text, "[maxduration]", WAPCommands.getDurationDisplay(activity.getMaxDurationMinutes()));
        }
        if (text.contains("[statusdate]")) {
            String status = activity.getStatus();
            String statusdate = "";
            if (status.equals("Draft")) {
                statusdate = activity.getCreateDate() == null ? "" : defaultDateFormat.format(activity.getCreateDate().getTime());
            } else if (status.equals("Activated")) {
                statusdate = activity.getActivatedDate() == null ? "" : defaultDateFormat.format(activity.getActivatedDate().getTime());
            } else if (status.equals("In Progress")) {
                statusdate = activity.getActualStartDate() == null ? "" : defaultDateFormat.format(activity.getActualStartDate().getTime());
            } else if (status.equals("Completed")) {
                statusdate = activity.getActualEndDate() == null ? "" : defaultDateFormat.format(activity.getActualEndDate().getTime());
            } else if (status.equals("Cancelled")) {
                statusdate = activity.getCancelledDate() == null ? "" : defaultDateFormat.format(activity.getCancelledDate().getTime());
            }
            text = StringUtil.replaceAll(text, "[statusdate]", statusdate);
        }
        if (text.contains("[assignedanalyst]") || text.contains("[assignedanalystid]")) {
            resources = wapCommands.getActivityResources(activity.getActivityid());
            find = new HashMap<String, String>();
            find.put("resourcetypeflag", "A");
            find.put("analysttype", "Analyst");
            row = resources.findRow(find);
            if (row >= 0) {
                text = StringUtil.replaceAll(text, "[assignedanalystid]", resources.getValue(row, "analystid", "Not Assigned"));
                text = StringUtil.replaceAll(text, "[assignedanalyst]", resources.getValue(row, "analystid", "Not Assigned"));
            } else {
                text = StringUtil.replaceAll(text, "[assignedanalystid]", "None required");
                text = StringUtil.replaceAll(text, "[assignedanalyst]", "None required");
            }
        }
        if (text.contains("[analystduration]")) {
            resources = wapCommands.getActivityResources(activity.getActivityid());
            find = new HashMap();
            find.put("resourcetypeflag", "A");
            find.put("analysttype", "Analyst");
            row = resources.findRow(find);
            text = row >= 0 ? StringUtil.replaceAll(text, "[analystduration]", WAPCommands.getDurationDisplay(resources.getInt(row, "duration"))) : StringUtil.replaceAll(text, "[analystduration]", "No analyst required");
        }
        if (text.contains("[worksdcid.singular]")) {
            text = StringUtil.replaceAll(text, "[worksdcid.singular]", sdcProcessor.getProperty(activity.getWorksdcid(), "singular"));
        }
        if (text.contains("[worksdcid.plural]")) {
            text = StringUtil.replaceAll(text, "[worksdcid.plural]", sdcProcessor.getProperty(activity.getWorksdcid(), "plural"));
        }
        if (text.contains("[%complete]")) {
            int percent = activity.getActivitySize() == 0 ? 0 : 100 * activity.getWorkCompleteCount() / activity.getActivitySize();
            text = StringUtil.replaceAll(text, "[%complete]", percent + "%");
        }
        if (text.contains("[timeremaining]")) {
            int percentremaining = 100 - (activity.getActivitySize() == 0 ? 0 : 100 * activity.getWorkCompleteCount() / activity.getActivitySize());
            int minutesremaining = activity.getMaxDurationMinutes() * percentremaining / 100;
            text = StringUtil.replaceAll(text, "[timeremaining]", WAPCommands.getDurationDisplay(minutesremaining));
        }
        return text;
    }

    public static String getTestingLabType(String testinglab, QueryProcessor queryProcessor) {
        SafeSQL safeSQL = new SafeSQL();
        safeSQL.addVar(testinglab);
        DataSet ds = queryProcessor.getPreparedSqlDataSet("SELECT testinglabtype FROM department WHERE departmentid=?", safeSQL.getValues());
        return ds != null && ds.getRowCount() > 0 ? ds.getValue(0, "testinglabtype", "") : "";
    }

    public static PropertyList getWapPolicy(String connectionId) {
        PropertyList wapPolicy;
        ConfigurationProcessor cp = new ConfigurationProcessor(connectionId);
        try {
            wapPolicy = cp.getPolicy("WAPPolicy", "Sapphire Custom");
        }
        catch (Exception e) {
            wapPolicy = new PropertyList();
        }
        return wapPolicy;
    }

    /*
     * Could not resolve type clashes
     */
    public static boolean drawResourceHtml(StringBuilder html, WAPAvailabilitySelector wapAvailabilitySelector, WAPAvailabilityOptions wapAvailabilityOptions, WAPSelector wapSelector, Instant fromDate, Instant toDate, ZoneId displayTimeZone, DataSet data, DataSet attachments, String sdcid, AssignmentPageResourceContainer focusedResource, JSONArray selectedResource, int imagesize, String jsClass, AssignmentPage.ColorScheme colorScheme, Logger logger, TranslationProcessor translationProcessor, PageContext pageContext) {
        String[] preferred;
        if (attachments == null) {
            DataSet dataSet = attachments = focusedResource.getAttachment() != null ? focusedResource.getAttachment() : null;
        }
        String[] stringArray = sdcid.equalsIgnoreCase("Department") ? (focusedResource.preferredWorkarea.length() > 0 ? StringUtil.split(focusedResource.preferredWorkarea, ";") : new String[]{}) : (preferred = focusedResource.preferredSDI.length() > 0 ? StringUtil.split(focusedResource.preferredSDI, ";") : new String[]{});
        String[] trans = sdcid.equalsIgnoreCase("Department") ? new String[]{} : (focusedResource.transientSDI.length() > 0 ? StringUtil.split(focusedResource.transientSDI, ";") : new String[]{});
        boolean rendered = false;
        for (int i = 0; i < data.getRowCount(); ++i) {
            boolean continueRender;
            boolean transientItem;
            String autoclass;
            List<WAPAvailability> availability;
            String itemdesc;
            String itemid;
            block54: {
                itemid = "";
                itemdesc = "";
                availability = null;
                autoclass = "";
                transientItem = false;
                continueRender = true;
                try {
                    if (sdcid.equalsIgnoreCase(AssignmentPage.ResourceSDC.USER.getName())) {
                        itemid = data.getValue(i, "sysuserid");
                        itemdesc = data.getValue(i, "sysuserdesc");
                        for (Object tran : trans) {
                            if (!itemid.equalsIgnoreCase((String)tran)) continue;
                            transientItem = true;
                            break;
                        }
                        if (transientItem && !focusedResource.isShowAll()) {
                            continueRender = false;
                            Map<String, Set<String>> schedWorkareas = null;
                            try {
                                schedWorkareas = wapSelector != null ? wapSelector.getScheduledUserWorkAreas(itemid, fromDate, toDate) : null;
                            }
                            catch (Exception exception) {
                                // empty catch block
                            }
                            if (schedWorkareas != null && focusedResource.getWorkareas() != null && focusedResource.getWorkareas().getRowCount() > 0) {
                                block8: for (Set<String> value : schedWorkareas.values()) {
                                    Object tran;
                                    tran = value.iterator();
                                    while (tran.hasNext()) {
                                        String workareaid = (String)tran.next();
                                        if (focusedResource.getWorkareas().findRow("departmentid", workareaid) <= -1) continue;
                                        continueRender = true;
                                        break block8;
                                    }
                                }
                            }
                        }
                        if (continueRender) {
                            availability = wapAvailabilitySelector != null ? wapAvailabilitySelector.getUserAvailabilityBetween(itemid, fromDate, toDate, wapAvailabilityOptions, displayTimeZone) : null;
                        }
                        autoclass = focusedResource.autoresource.length() > 0 && focusedResource.autoresource.equals(itemid) ? " icon_auto" : "";
                        break block54;
                    }
                    if (sdcid.equalsIgnoreCase(AssignmentPage.ResourceSDC.INSTRUMENT.getName())) {
                        itemid = data.getValue(i, "instrumentid");
                        itemdesc = data.getValue(i, "instrumentdesc");
                        availability = wapAvailabilitySelector != null ? wapAvailabilitySelector.getInstrumentAvailabilityBetween(itemid, fromDate, toDate, wapAvailabilityOptions, displayTimeZone) : null;
                        autoclass = focusedResource.autoresource.length() > 0 && focusedResource.autoresource.equals(itemid) ? " icon_auto" : "";
                    } else {
                        itemid = data.getValue(i, "departmentid");
                        itemdesc = data.getValue(i, "departmentdesc");
                        String string = autoclass = focusedResource.autoworkarea.length() > 0 && focusedResource.autoworkarea.equals(itemid) ? " icon_auto" : "";
                        availability = focusedResource.getResourceSDC() == AssignmentPage.ResourceSDC.INSTRUMENT ? (wapAvailabilitySelector != null ? wapAvailabilitySelector.getInstrumentWorkareaAvailabilityBetween(itemid, fromDate, toDate, wapAvailabilityOptions, focusedResource.resourceType, focusedResource.resourceModel, displayTimeZone) : null) : (wapAvailabilitySelector != null ? wapAvailabilitySelector.getUserWorkareaAvailabilityBetween(itemid, fromDate, toDate, wapAvailabilityOptions, displayTimeZone) : null);
                    }
                }
                catch (Exception e) {
                    logger.warn("Failed to find availability.");
                }
            }
            if (!continueRender) continue;
            boolean suggested = false;
            for (String s : preferred) {
                if (!itemid.equalsIgnoreCase(s)) continue;
                suggested = true;
                break;
            }
            boolean selected = false;
            if (selectedResource != null) {
                for (int a = 0; a < selectedResource.length(); ++a) {
                    try {
                        if (!selectedResource.getString(a).equals(itemid)) continue;
                        selected = true;
                        break;
                    }
                    catch (Exception tran) {
                        // empty catch block
                    }
                }
            }
            html.append("<div class=\"icon").append(selected ? " icon_sel" : "").append(autoclass).append("\" onclick=\"").append(jsClass).append(".itemClick(event,this)\" data-sdcid=\"").append(sdcid).append("\" data-resourcesdc=\"").append(focusedResource != null ? focusedResource.getResourceSDC() : "").append("\" data-resourcenum=\"").append(focusedResource.getNum()).append("\" data-itemid=\"").append(itemid).append("\" data-suggested=\"").append(suggested ? "Y" : "N").append("\">");
            if (suggested) {
                html.append("<div class=\"icon_fav\">");
                Image favImage = new Image(pageContext);
                favImage.setDimensions(16, 16);
                favImage.setTitle(transientItem ? translationProcessor.translate("Transient resource") : translationProcessor.translate("Suggested resource"));
                favImage.setImageId(transientItem ? "FlatBlackTimer" : "FlatBlackStar1");
                favImage.setColor("Orange");
                html.append(favImage.getHtml());
                html.append("</div>");
            }
            html.append("<div class=\"icon_img\">");
            Image image = new Image(pageContext);
            image.setHeight(imagesize);
            if (sdcid.equalsIgnoreCase(AssignmentPage.ResourceSDC.USER.getName())) {
                image.setImageId("FlatBlackPeople");
            } else if (sdcid.equalsIgnoreCase(AssignmentPage.ResourceSDC.INSTRUMENT.getName())) {
                image.setImageId("FlatBlackThermometer2");
            } else if (focusedResource.getResourceSDC() == AssignmentPage.ResourceSDC.INSTRUMENT) {
                image.setImageId("FlatBlackThermometerGroup");
            } else {
                image.setImageId("FlatBlackGroup");
            }
            int percent = availability != null ? AssignmentPageUtil.getPercentAvailability(availability) : -1;
            String textclass = "";
            String textCol = "#000000";
            if (percent > -1 && colorScheme != AssignmentPage.ColorScheme.NONE) {
                if (percent > 100) {
                    image.setColor("Red");
                    textCol = "#eefa04";
                    textclass = "lv_exception";
                } else if (percent > 80) {
                    image.setColor("Red");
                    textCol = "#000000";
                } else if (percent > 60) {
                    image.setColor("Orange");
                    textCol = "#FFFFFF";
                } else if (percent > 40) {
                    image.setColor("Yellow");
                    textCol = "#000000";
                } else if (percent > 20) {
                    image.setColor("Blue");
                    textCol = "#FFFFFF";
                } else {
                    image.setColor("Green");
                    textCol = "#FFFFFF";
                }
            } else {
                image.setColor("Gray");
            }
            String title = itemdesc.length() > 0 ? itemdesc : itemid;
            String hint = (focusedResource != null ? focusedResource.getResourceSDC().singulartext + " " : "") + title;
            if (autoclass.length() > 0 && hint.length() > 0) {
                hint = hint + " (" + translationProcessor.translate("preferred resource") + ")";
            } else if (transientItem && hint.length() > 0) {
                hint = hint + " (" + translationProcessor.translate("transient resource") + ")";
            }
            image.setTitle(hint);
            html.append(image.getHtml());
            html.append("</div>");
            if (attachments != null && attachments.getRowCount() > 0 && focusedResource.getResourceSDC() == AssignmentPage.ResourceSDC.USER) {
                if (sdcid.equalsIgnoreCase(focusedResource.getResourceSDC().getName())) {
                    html.append("<div class=\"icon_face\"");
                    int r = new Random().nextInt(100) + 1;
                    HashMap<String, String> find = new HashMap<String, String>();
                    find.put("keyid1", itemid);
                    find.put("attachmentclass", "ProfilePicture");
                    if (attachments.findRow(find) > -1) {
                        html.append(" style=\"");
                        html.append("background-image:url('").append("rc?command=image&attachment=User;").append(itemid).append(";;;ProfilePicture&width=48&height=48&_r=").append(r).append("');");
                        html.append("\"");
                    }
                    html.append(" title=\"").append(hint).append("\">");
                    html.append("</div>");
                } else {
                    String users;
                    int c = 0;
                    String string = users = data.isValidColumn("_sysusers") ? data.getValue(i, "_sysusers", "") : "";
                    if (users.length() > 0) {
                        String[] usersarray;
                        for (String currentUser : usersarray = StringUtil.split(users, ";")) {
                            for (int j = 0; j < attachments.getRowCount() && c < 3; ++j) {
                                if (!attachments.getValue(j, "keyid1", "").equalsIgnoreCase(currentUser) || !attachments.getValue(j, "attachmentclass", "").equalsIgnoreCase("ProfilePicture")) continue;
                                int r = new Random().nextInt(100) + 1;
                                html.append("<div class=\"icon_face").append(c == 0 ? " icon_facecen" : (c == 1 ? " icon_facelhs" : " icon_facerhs")).append("\"");
                                html.append(" style=\"");
                                html.append("background-image:url('").append("rc?command=image&attachment=User;").append(attachments.getValue(j, "keyid1", "")).append(";;;ProfilePicture&width=").append(c == 0 ? "50" : "32").append("&height=").append(c == 0 ? "50" : "32").append("&_r=").append(r).append("');");
                                html.append("\"");
                                html.append(">");
                                html.append("</div>");
                                ++c;
                            }
                        }
                    }
                }
            }
            if (percent > -1) {
                html.append("<div class=\"icon_num").append(textclass.length() > 0 ? " " + textclass : "").append("\" style=\"color:").append(textCol).append(";\" title=\"").append(hint).append("\" onmousemove=\"assignment.workloadMouseMove(event)\" onmouseout=\"assignment.workloadMouseOut(event)\">");
                html.append(percent).append("%");
                html.append("</div>");
            }
            if (fromDate != null) {
                html.append("<div class=\"icon_cal\" onclick=\"").append(jsClass).append(".itemCalClick('").append(focusedResource.getId()).append("','").append(itemid).append("','").append(sdcid).append("', event)\">");
                Image calImage = new Image(pageContext);
                calImage.setDimensions(16, 16);
                calImage.setTitle(translationProcessor.translate("See") + " " + translationProcessor.translate(focusedResource.getResourceSDC().singulartext) + " " + translationProcessor.translate("calendar"));
                calImage.setImageId("FlatBlackCalendarWeek");
                html.append(calImage.getHtml());
                html.append("</div>");
            }
            String t = title.substring(0, 1).toUpperCase() + title.substring(1);
            html.append("<div class=\"icon_txt").append(autoclass.length() > 0 ? " " + autoclass : "").append("\" title=\"").append(hint).append("\">");
            html.append(AssignmentPageUtil.getShortDesc(t));
            if (autoclass.length() > 0) {
                html.append(" (P)");
            } else if (transientItem) {
                html.append(" (T)");
            }
            html.append("</div>");
            html.append("</div>");
            rendered = true;
        }
        return rendered;
    }

    public static JSONArray getSelectedResource(String resourceid, JSONObject selection) {
        if (selection != null) {
            if (selection.has(resourceid)) {
                try {
                    return selection.getJSONArray(resourceid);
                }
                catch (Exception e) {
                    return null;
                }
            }
            return null;
        }
        return null;
    }

    public static AssignmentPageResourceContainer getFocusedResource(String resourceid, AssignmentPageResourceData resourceData) {
        AssignmentPageResourceContainer focusedResource = null;
        if (resourceData != null && resourceData.getResources() != null) {
            for (int i = 0; i < resourceData.getResources().size(); ++i) {
                if (!resourceData.getResources().get(i).getId().equals(resourceid)) continue;
                focusedResource = resourceData.getResources().get(i);
                break;
            }
        }
        return focusedResource;
    }

    private static void updateFocusedResource(AssignmentPageResourceData resourceData, AssignmentPageResourceContainer toUpdate) {
        String resourceid = toUpdate.getId();
        if (resourceData != null && resourceData.getResources() != null) {
            for (int i = 0; i < resourceData.getResources().size(); ++i) {
                if (!resourceData.getResources().get(i).getId().equals(resourceid)) continue;
                resourceData.getResources().set(i, toUpdate);
                break;
            }
        }
    }

    public static AssignmentPageResourceContainer getResourceData(String resourceId, AssignmentPageResourceData resourceData) {
        if (resourceData != null) {
            for (int i = 0; i < resourceData.resources.size(); ++i) {
                if (!resourceData.resources.get(i).getId().equals(resourceId)) continue;
                return resourceData.resources.get(i);
            }
            return null;
        }
        return null;
    }

    public static void refreshFocusedResource(String focusedResource, AssignmentPageResourceData resourceData, String departmentid, AssignmentPage.ResourceMode resourceMode, SDIProcessor sdiProcessor, String connectionId) {
        AssignmentPageResourceContainer focus = AssignmentPageUtil.getFocusedResource(focusedResource, resourceData);
        if (focus != null) {
            String sdis = focus.preferredSDI;
            String workareas = focus.preferredWorkarea;
            if (resourceMode == AssignmentPage.ResourceMode.ALL) {
                WAPCommands wapCommands = new WAPCommands(connectionId);
                if (focus.getResourceSDC() == AssignmentPage.ResourceSDC.USER) {
                    sdis = wapCommands.getDepartmentUsers(departmentid);
                    workareas = wapCommands.getDepartmentUserWorkareas(departmentid);
                } else {
                    sdis = wapCommands.getDepartmentInstruments(departmentid, "", focus.resourceType, focus.resourceModel);
                    workareas = wapCommands.getDepartmentInstrumentWorkareas(departmentid, focus.resourceType, focus.resourceModel);
                }
            }
            AssignmentPageResourceContainer resource = AssignmentPageUtil.getResourceData(focus.getResourceSDC(), focus.getNum(), focus.getId(), focus.getLabel(), focus.getType(), focus.getModel(), focus.getAutoAssignResource(), focus.getAutoAssignWorkarea(), sdis, "", "", workareas, departmentid, true, sdiProcessor);
            resource.showAll = resourceMode == AssignmentPage.ResourceMode.ALL;
            resource.preferredSDI = focus.preferredSDI;
            resource.transientSDI = focus.transientSDI;
            resource.preferredWorkarea = focus.preferredWorkarea;
            AssignmentPageUtil.updateFocusedResource(resourceData, resource);
        }
    }

    public static AssignmentPageResourceContainer getResourceData(AssignmentPage.ResourceSDC resourceSDC, int resourcenum, String resourceLabel, String resourceType, String resourceModel, String autoresource, String autoworkarea, int typeCount, String keyid1, String keyid2, String keyid3, String workareaid, String departmentid, boolean useKeyid1, SDIProcessor sdiProcessor) {
        return AssignmentPageUtil.getResourceData(resourceSDC, resourcenum, resourceLabel, resourceType, resourceModel, autoresource, autoworkarea, typeCount, keyid1, keyid2, keyid3, workareaid, keyid1, workareaid, departmentid, useKeyid1, sdiProcessor);
    }

    public static AssignmentPageResourceContainer getResourceData(AssignmentPage.ResourceSDC resourceSDC, int resourcenum, String resourceLabel, String resourceType, String resourceModel, String autoresource, String autoworkarea, int typeCount, String keyid1, String keyid2, String keyid3, String workareaid, String prefferedKeyid1, String preferredWorkareaid, String departmentid, boolean useKeyid1, SDIProcessor sdiProcessor) {
        return AssignmentPageUtil.getResourceData(resourceSDC, resourcenum, resourceLabel, resourceType, resourceModel, autoresource, autoworkarea, typeCount, keyid1, keyid2, keyid3, workareaid, prefferedKeyid1, preferredWorkareaid, "", departmentid, useKeyid1, sdiProcessor);
    }

    public static AssignmentPageResourceContainer getResourceData(AssignmentPage.ResourceSDC resourceSDC, int resourcenum, String resourceLabel, String resourceType, String resourceModel, String autoresource, String autoworkarea, int typeCount, String keyid1, String keyid2, String keyid3, String workareaid, String prefferedKeyid1, String preferredWorkareaid, String transientKeyid1, String departmentid, boolean useKeyid1, SDIProcessor sdiProcessor) {
        String label = resourceLabel == null || resourceLabel.length() == 0 ? resourceSDC.singulartext + " " + (typeCount + 1) : resourceLabel;
        String id = resourceSDC.toString() + "-" + typeCount;
        return AssignmentPageUtil.getResourceData(resourceSDC, resourcenum, id, label, resourceType, resourceModel, autoresource, autoworkarea, keyid1, keyid2, keyid3, workareaid, prefferedKeyid1, preferredWorkareaid, transientKeyid1, departmentid, useKeyid1, sdiProcessor);
    }

    public static AssignmentPageResourceContainer getResourceData(AssignmentPage.ResourceSDC resourceSDC, int resourcenum, String resourceId, String resourceLabel, String resourceType, String resourceModel, String autoresource, String autoworkarea, String keyid1, String keyid2, String keyid3, String workareaid, String departmentid, boolean useKeyid1, SDIProcessor sdiProcessor) {
        return AssignmentPageUtil.getResourceData(resourceSDC, resourcenum, resourceId, resourceLabel, resourceType, resourceModel, autoresource, autoworkarea, keyid1, keyid2, keyid3, workareaid, keyid1, workareaid, departmentid, useKeyid1, sdiProcessor);
    }

    public static AssignmentPageResourceContainer getResourceData(AssignmentPage.ResourceSDC resourceSDC, int resourcenum, String resourceId, String resourceLabel, String resourceType, String resourceModel, String autoresource, String autoworkarea, String keyid1, String keyid2, String keyid3, String workareaid, String prefferedKeyid1, String preferredWorkareaid, String departmentid, boolean useKeyid1, SDIProcessor sdiProcessor) {
        return AssignmentPageUtil.getResourceData(resourceSDC, resourcenum, resourceId, resourceLabel, resourceType, resourceModel, autoresource, autoworkarea, keyid1, keyid2, keyid3, workareaid, prefferedKeyid1, preferredWorkareaid, "", departmentid, useKeyid1, sdiProcessor);
    }

    public static AssignmentPageResourceContainer getResourceData(AssignmentPage.ResourceSDC resourceSDC, int resourcenum, String resourceId, String resourceLabel, String resourceType, String resourceModel, String autoresource, String autoworkarea, String keyid1, String keyid2, String keyid3, String workareaid, String prefferedKeyid1, String preferredWorkareaid, String transientKeyid1, String departmentid, boolean useKeyid1, SDIProcessor sdiProcessor) {
        TimeZone t;
        SDIData temp;
        StringBuilder where;
        AssignmentPageResourceContainer out = null;
        DataSet workareaData = null;
        if (workareaid != null && workareaid.length() > 0) {
            SDIRequest depRequest = new SDIRequest();
            depRequest.setSDCid("Department");
            depRequest.setRequestItem("primary");
            depRequest.setRequestItem("departmentsysuser");
            depRequest.setRetainRsetid(false);
            depRequest.setKeyid1List(workareaid);
            depRequest.setQueryOrderBy("departmentid");
            SDIData temp2 = sdiProcessor.getSDIData(depRequest);
            if (temp2 != null && temp2.getDataset("primary") != null) {
                DataSet depusers;
                workareaData = temp2.getDataset("primary");
                if (!workareaData.isValidColumn("_sysusers")) {
                    workareaData.addColumn("_sysusers", 0);
                }
                if ((depusers = temp2.getDataset("departmentsysuser")) != null) {
                    for (int d = 0; d < workareaData.getRowCount(); ++d) {
                        String depid = workareaData.getValue(d, "departmentid", "");
                        StringBuilder users = new StringBuilder();
                        for (int r = 0; r < depusers.getRowCount(); ++r) {
                            if (!depusers.getValue(r, "departmentid", "").equalsIgnoreCase(depid)) continue;
                            if (users.length() > 0) {
                                users.append(";");
                            }
                            if (depusers.getValue(r, "sysuserid", "").length() <= 0) continue;
                            users.append(depusers.getValue(r, "sysuserid", ""));
                        }
                        workareaData.setValue(d, "_sysusers", users.toString());
                    }
                }
            }
        }
        boolean isOracle = new ConnectionProcessor(sdiProcessor.getConnectionid()).isOra();
        if (resourceSDC == AssignmentPage.ResourceSDC.USER) {
            SDIRequest userRequest = new SDIRequest();
            userRequest.setSDCid("User");
            userRequest.setRequestItem("primary");
            userRequest.setRequestItem("attachment");
            userRequest.setRequestItem("departmentsysuser");
            userRequest.setRetainRsetid(false);
            if (useKeyid1) {
                userRequest.setKeyid1List(keyid1);
            } else {
                userRequest.setQueryFrom("sysuser");
                where = new StringBuilder();
                StringBuilder from = new StringBuilder();
                where.append("( sysuser.disabledflag is null or sysuser.disabledflag = 'N' )");
                if (departmentid != null && departmentid.length() > 0) {
                    if (where.length() > 0) {
                        where.append(" AND");
                    }
                    where.append(" departmentsysuser.sysuserid = sysuser.sysuserid");
                    where.append(" AND departmentsysuser.departmentid = '" + SafeSQL.encodeForSQL(departmentid, isOracle) + "'");
                    from.append("departmentsysuser, sysuser");
                }
                if (keyid1.length() > 0) {
                    if (where.length() > 0) {
                        where.append(" AND");
                    }
                    where.append(" sysuser.sysuserid IN ('").append(SafeSQL.convertToSQLInClause(keyid1, ";", isOracle)).append("')");
                }
                if (where.length() > 0) {
                    userRequest.setQueryWhere(where.toString());
                }
                if (from.length() > 0) {
                    userRequest.setQueryFrom(from.toString());
                }
            }
            userRequest.setQueryOrderBy("sysuserid");
            temp = sdiProcessor.getSDIData(userRequest);
            if (temp != null && temp.getDataset("primary") != null) {
                int r;
                HashMap<String, ZoneId> timezones = new HashMap<String, ZoneId>();
                for (r = 0; r < temp.getDataset("primary").getRowCount(); ++r) {
                    t = TimeZone.getTimeZone(temp.getDataset("primary").getValue(r, "timezone", TimeZone.getDefault().getID()));
                    String id = temp.getDataset("primary").getValue(r, "sysuserid", "");
                    timezones.put(id + "|User", t.toZoneId());
                }
                if (workareaData != null) {
                    for (r = 0; r < workareaData.getRowCount(); ++r) {
                        t = TimeZone.getTimeZone(workareaData.getValue(r, "timezone", TimeZone.getDefault().getID()));
                        String id = workareaData.getValue(r, "departmentid", "");
                        timezones.put(id + "|Department", t.toZoneId());
                    }
                }
                out = new AssignmentPageResourceContainer(resourceId, resourcenum, prefferedKeyid1, preferredWorkareaid, transientKeyid1, resourceLabel, resourceSDC, resourceType, resourceModel, autoresource, autoworkarea, false, temp.getDataset("primary"), workareaData, temp.getDataset("departmentsysuser"), temp.getDataset("attachment"), timezones);
            }
        } else if (resourceSDC == AssignmentPage.ResourceSDC.INSTRUMENT) {
            SDIRequest instRequest = new SDIRequest();
            instRequest.setSDCid("Instrument");
            instRequest.setRetainRsetid(false);
            instRequest.setRequestItem("primary[*, testingdepartmentid.timezone]");
            if (useKeyid1) {
                instRequest.setKeyid1List(keyid1);
            } else {
                instRequest.setQueryFrom("instrument");
                where = new StringBuilder();
                StringBuilder from = new StringBuilder();
                if (departmentid != null && departmentid.length() > 0) {
                    if (where.length() > 0) {
                        where.append(" AND");
                    }
                    where.append(" testingdepartmentid = '" + SafeSQL.encodeForSQL(departmentid, isOracle) + "'");
                }
                if (keyid1.length() > 0) {
                    if (where.length() > 0) {
                        where.append(" AND");
                    }
                    where.append(" instrumentid IN ('").append(SafeSQL.convertToSQLInClause(keyid1, ";", isOracle)).append("')");
                }
                if (resourceModel.length() > 0) {
                    if (where.length() > 0) {
                        where.append(" AND");
                    }
                    where.append(" instrumentmodelid = '").append(SafeSQL.encodeForSQL(resourceModel, isOracle)).append("'");
                }
                if (resourceType.length() > 0) {
                    if (where.length() > 0) {
                        where.append(" AND");
                    }
                    where.append(" instrumenttype = '").append(SafeSQL.encodeForSQL(resourceType, isOracle)).append("'");
                }
                if (where.length() > 0) {
                    instRequest.setQueryWhere(where.toString());
                }
                if (from.length() > 0) {
                    instRequest.setQueryFrom(from.toString());
                }
            }
            instRequest.setQueryOrderBy("instrumentid");
            temp = sdiProcessor.getSDIData(instRequest);
            if (temp != null && temp.getDataset("primary") != null) {
                int r;
                HashMap<String, ZoneId> timezones = new HashMap<String, ZoneId>();
                for (r = 0; r < temp.getDataset("primary").getRowCount(); ++r) {
                    t = TimeZone.getTimeZone(temp.getDataset("primary").getValue(r, "testingdepartmentid.timezone", TimeZone.getDefault().getID()));
                    String id = temp.getDataset("primary").getValue(r, "instrumentid", "");
                    timezones.put(id + "|Instrument", t.toZoneId());
                }
                if (workareaData != null) {
                    for (r = 0; r < workareaData.getRowCount(); ++r) {
                        t = TimeZone.getTimeZone(workareaData.getValue(r, "timezone", TimeZone.getDefault().getID()));
                        String id = workareaData.getValue(r, "departmentid", "");
                        timezones.put(id + "|Department", t.toZoneId());
                    }
                }
                out = new AssignmentPageResourceContainer(resourceId, resourcenum, prefferedKeyid1, preferredWorkareaid, transientKeyid1, resourceLabel, resourceSDC, resourceType, resourceModel, autoresource, autoworkarea, false, temp.getDataset("primary"), workareaData, null, null, timezones);
            }
        }
        return out;
    }

    protected static String getAssignmentHTML(ActivityClassHandler activityClassHandler, String selectedsdis, CalendarPage.View dateRange, Instant[] selectedDates, String departmentid, String shiftid, JSONObject selection, AssignmentPageResourceData resourceData, SDIProcessor sdiProcessor, TranslationProcessor tp, ZoneId displayTimeZone, M18NUtil m18) throws SapphireException {
        String[] selectedSDIs;
        StringBuilder html = new StringBuilder();
        String sdcid = activityClassHandler.getSDC();
        boolean isActivitySelection = activityClassHandler.getSDC().equals("LV_Activity");
        boolean isWorkAssignment = false;
        try {
            isWorkAssignment = selection != null && selection.has("activity") && selection.getJSONArray("activity").length() > 0;
        }
        catch (Exception exception) {
            // empty catch block
        }
        html.append("<input type=\"hidden\" name=\"activityclass\" value=\"").append(activityClassHandler.getId()).append("\">");
        html.append("<input type=\"hidden\" name=\"sdcid\" value=\"").append(sdcid).append("\">");
        html.append("<input type=\"hidden\" name=\"selectedsdis\" value=\"").append(selectedsdis).append("\">");
        html.append("<input type=\"hidden\" name=\"daterange\" value=\"").append(dateRange.toString()).append("\">");
        html.append("<input type=\"hidden\" name=\"selecteddates\" value=\"").append(selectedDates[0].toEpochMilli() + (selectedDates.length > 1 ? ";" + selectedDates[1].toEpochMilli() : "")).append("\">");
        html.append("<input type=\"hidden\" name=\"departmentid\" value=\"").append(departmentid).append("\">");
        html.append("<input type=\"hidden\" name=\"shiftid\" value=\"").append(shiftid).append("\">");
        StringBuilder insert = new StringBuilder();
        if (!isActivitySelection) {
            insert.append("<strong>").append(tp.translate("Time Assignment")).append("</strong>");
            String warn = displayTimeZone.getId().equals(m18.getTimezone().toZoneId().getId()) ? "" : " background-color: orange";
            insert.append("<span style=\"padding-left: 5px;padding-right:5px;margin-left: 20px; " + warn + "\">").append("(" + displayTimeZone + ")").append("</span>");
        }
        insert.append("<br>");
        String[] stringArray = selectedSDIs = selectedsdis.length() > 0 ? StringUtil.split(selectedsdis, ";") : new String[]{};
        if (selectedSDIs.length > 0) {
            if (isWorkAssignment && isActivitySelection) {
                throw new SapphireException(tp.translate("You can not assign an activity as work to an existing activity."));
            }
            if (isWorkAssignment) {
                html.append("<table cellspacing=\"10\"><tr>");
                html.append("</tr><tr>");
                html.append("<td style=\"vertical-align:top;\">");
                try {
                    JSONArray acts = selection.getJSONArray("activity");
                    if (selectedSDIs.length > 1) {
                        html.append(tp.translate("Adding")).append(" ").append(selectedSDIs.length).append(" SDIs ").append(tp.translate("to")).append(" ").append(tp.translate("Activity")).append(" ").append(acts.getString(0)).append("<br>");
                    } else {
                        html.append(tp.translate("Adding ")).append(selectedSDIs[0]).append(" ").append(tp.translate("to")).append(" ").append(tp.translate("Activity")).append(" ").append(acts.getString(0)).append("<br>");
                    }
                }
                catch (Exception exception) {
                    // empty catch block
                }
                html.append("</td>");
                html.append("</tr>");
                html.append("</table>");
            } else {
                int selectedresources = AssignmentPageUtil.getAndCheckSelectedResourceCount(selection, resourceData);
                if (isActivitySelection && selectedresources == 0) {
                    throw new SapphireException(tp.translate("No resources selected."));
                }
                if (selectedresources < 0) {
                    throw new SapphireException(tp.translate("Invalid resources selected. Please select either 1 or the same number of resources for each resource requirement."));
                }
                if (isActivitySelection && selectedresources != selectedSDIs.length && selectedresources != 1) {
                    throw new SapphireException(tp.translate("Invalid resources selected. Please select either 1 or the same number of resources for the number of activities selected."));
                }
                if (!isActivitySelection) {
                    if (selectedresources > selectedSDIs.length) {
                        throw new SapphireException(tp.translate("Cannot assign as resources selected do not match the amount of work selected.") + tp.translate("You need to select") + " " + selectedresources + " " + tp.translate("or more Work SDI's") + ".");
                    }
                    DateTimeFormatter dateTimeFormatter = m18.getDateTimeFormatter(displayTimeZone);
                    DateTimeFormatter timeFormatter = m18.getTimeFormatter(displayTimeZone);
                    DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM").withZone(displayTimeZone).withLocale(m18.getLocale());
                    DateTimeFormatter dayMonthFormatter = DateTimeFormatter.ofPattern("d MMM").withZone(displayTimeZone).withLocale(m18.getLocale());
                    if (selectedDates.length == 0) {
                        insert.append("<input name=\"dateassignment\" type=\"radio\" value=\"now\">").append(tp.translate("Starting Immediately")).append("<br>");
                        insert.append("<input name=\"dateassignment\" type=\"radio\" value=\"tomorrow\">").append(tp.translate("Starting Tomorrow")).append("<br>");
                        insert.append("<input name=\"dateassignment\" type=\"radio\" value=\"week\">").append(tp.translate("Starting Next Week")).append("<br>");
                        insert.append("<input name=\"dateassignment\" type=\"radio\" value=\"time\">").append(tp.translate("Choose Time"));
                    } else {
                        ZonedDateTime zonedFrom = ZonedDateTime.ofInstant(selectedDates[0], displayTimeZone);
                        ZonedDateTime zonedTo = selectedDates.length == 1 ? null : ZonedDateTime.ofInstant(selectedDates[1], displayTimeZone);
                        insert.append("<input name=\"dateassignment\" type=\"radio\" value=\"floating\" checked>");
                        if (dateRange == CalendarPage.View.DAY) {
                            if (selectedDates.length == 1) {
                                try {
                                    insert.append(tp.translate("Assigning to")).append(" ").append(dateTimeFormatter.format(zonedFrom));
                                }
                                catch (Exception e) {
                                    throw new SapphireException(tp.translate("Invalid single date."));
                                }
                            } else if (selectedDates.length == 2) {
                                try {
                                    insert.append(tp.translate("Assigning to time range")).append(" ").append(dayMonthFormatter.format(zonedFrom)).append(" ").append(timeFormatter.format(zonedFrom)).append(" - ").append(timeFormatter.format(zonedTo));
                                }
                                catch (Exception e) {
                                    throw new SapphireException(tp.translate("Invalid time range."));
                                }
                            } else {
                                insert.append(tp.translate("Assigning to ")).append(selectedDates.length).append(" ").append(tp.translate("selected dates"));
                            }
                        } else if (selectedDates.length == 1) {
                            try {
                                if (dateRange == CalendarPage.View.WEEK) {
                                    int weekOfYear = zonedFrom.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
                                    ZonedDateTime startWeek = CalendarConverter.getStartOfWeek(zonedFrom, m18.getLocale());
                                    ZonedDateTime endWeek = startWeek.plus(7L, ChronoUnit.DAYS);
                                    insert.append(tp.translate("Assigning to ")).append(tp.translate("week")).append(" ").append(weekOfYear).append(" (").append(dayMonthFormatter.format(startWeek)).append(" - ").append(dayMonthFormatter.format(endWeek)).append(")");
                                }
                                insert.append(tp.translate("Assigning to month of")).append(" ").append(monthFormatter.format(zonedFrom));
                            }
                            catch (Exception e) {
                                throw new SapphireException(tp.translate("Invalid date range."));
                            }
                        } else if (selectedDates.length == 2) {
                            try {
                                insert.append(tp.translate("Assigning to ")).append(tp.translate("date range")).append(" ").append(dayMonthFormatter.format(zonedFrom)).append(" - ").append(dayMonthFormatter.format(zonedTo));
                            }
                            catch (Exception e) {
                                throw new SapphireException(tp.translate("Invalid date range."));
                            }
                        } else {
                            throw new SapphireException(tp.translate("No valid date range selected"));
                        }
                        insert.append("<br>");
                        insert.append("<input name=\"dateassignment\" type=\"radio\" value=\"fixed\">");
                        insert.append(tp.translate("Fix start time to")).append(" ").append(dayMonthFormatter.format(zonedFrom)).append(" ");
                        insert.append("<select name=\"fixeddate_time\" class=\"\">");
                        boolean found = false;
                        for (int i = 0; i < 24; ++i) {
                            int baseMin = i * 60;
                            for (int k = 0; k < 4; ++k) {
                                boolean selected2;
                                boolean bl = selected2 = i == zonedFrom.getHour() && k == zonedFrom.getMinute();
                                if (selected2) {
                                    found = true;
                                }
                                int min = k * 15;
                                String text = "" + i;
                                if (text.length() == 1) {
                                    text = "0" + text;
                                }
                                text = text + ":";
                                String m = "" + min;
                                if (m.length() == 1) {
                                    m = "0" + m;
                                }
                                text = text + m;
                                insert.append("<option value=\"").append(baseMin + k * 15).append("\"").append(selected2 ? " selected" : "").append(">").append(text).append("</option>");
                            }
                        }
                        if (!found) {
                            String text = "" + zonedFrom.getHour();
                            if (text.length() == 1) {
                                text = "0" + text;
                            }
                            text = text + ":";
                            String m = "" + zonedFrom.getMinute();
                            if (m.length() == 1) {
                                m = "0" + m;
                            }
                            text = text + m;
                            insert.append("<option value=\"").append(zonedFrom.getHour() * 60 + zonedFrom.getMinute()).append("\"").append(" selected").append(">").append(text).append("</option>");
                        }
                        insert.append("</select>");
                    }
                }
                html.append("<table cellspacing=\"10\"><tr>");
                html.append("</tr><tr>");
                html.append("<td style=\"vertical-align:top;\">");
                if (!isActivitySelection) {
                    html.append("<strong>").append(tp.translate("SDI Assignment")).append("</strong><br>");
                } else {
                    html.append("<strong>").append(tp.translate("Selected Activities")).append("</strong><br>");
                }
                AssignmentPage.AssignmentPageResourceRequirement resourceRequirement = AssignmentPageUtil.getResourceRequirement(activityClassHandler, selectedsdis, departmentid, resourceData, tp.getConnectionid(), sdiProcessor, tp, false);
                String duration = resourceRequirement.getDuration();
                String worklabel = resourceRequirement.getDescription();
                int maxASize = resourceRequirement.getMaxSize();
                String activityDesc = activityClassHandler.getActivityLabel();
                if (activityDesc.length() == 0) {
                    activityDesc = worklabel;
                }
                activityDesc = StringUtil.replaceAll(activityDesc, "[label]", worklabel, false);
                activityDesc = StringUtil.replaceAll(activityDesc, "[testingdepartmentid]", departmentid, false);
                activityDesc = StringUtil.replaceAll(activityDesc, "[departmentid]", departmentid, false);
                activityDesc = activityClassHandler.replaceDateTokens(activityDesc);
                activityDesc = activityClassHandler.replaceTokensFromWorkSDI(activityDesc, selectedSDIs[0]);
                if (isActivitySelection) {
                    if (selectedSDIs.length > 1) {
                        html.append(tp.translate("Assigning to")).append(" ").append(selectedSDIs.length).append(" ").append(tp.translate("selected activities")).append(" ");
                        if (duration.length() > 0) {
                            html.append(tp.translate("totalling")).append(" ").append(duration);
                        }
                    } else {
                        html.append(tp.translate("Assigning to")).append(" ").append(tp.translate("activity")).append(" ").append(" ").append(selectedSDIs[0]).append(" ");
                        if (duration.length() > 0) {
                            html.append(tp.translate("taking")).append(" ").append(duration);
                        }
                    }
                } else if (selectedSDIs.length > 1) {
                    html.append(tp.translate("Assigning")).append(" ").append(selectedSDIs.length).append(" ").append(tp.translate("selected items")).append(" ");
                    if (duration.length() > 0) {
                        html.append(tp.translate("totalling")).append(" ").append(duration);
                    }
                    int definedMAS = maxASize > 0 ? maxASize : 1000;
                    HashMap<String, String> token = new HashMap<String, String>();
                    token.put("definedMAS", Integer.toString(definedMAS));
                    if (selectedSDIs.length > definedMAS) {
                        html.append("<div style=\"color:red;\">").append(tp.translate("The maximum size ([definedMAS]) exceeded, so will be split across multiple activities.", token)).append("</div>");
                    }
                } else {
                    html.append(tp.translate("Assigning")).append(" ").append(tp.translate("one")).append(" ").append(sdcid).append(" ");
                    if (duration.length() > 0) {
                        html.append(tp.translate("taking")).append(" ").append(duration);
                    }
                }
                html.append("</td>");
                html.append("</tr><tr>");
                if (!isActivitySelection) {
                    html.append("<td style=\"vertical-align:top;\">");
                    html.append((CharSequence)insert);
                    html.append("</td>");
                    html.append("</tr><tr>");
                }
                html.append("<td style=\"vertical-align:top;\">");
                html.append("<strong>").append(tp.translate("Resource Allocation Rules")).append("</strong><br>");
                if (selection != null && resourceData != null && resourceData.getResources() != null && resourceData.getResources().size() > 0) {
                    TabGroup tabGroup = new TabGroup();
                    tabGroup.setBodywidth("100%");
                    tabGroup.setBodyheight("150px");
                    tabGroup.setAppearance("modern");
                    tabGroup.setId("ass__tabgroup");
                    tabGroup.setMultiTab(true);
                    tabGroup.setUseChangeTab(false);
                    for (int r = 0; r < resourceData.getResources().size(); ++r) {
                        HashMap<String, String> token;
                        AssignmentPageResourceContainer resource = resourceData.getResources().get(r);
                        Tab tab = new Tab();
                        tab.setBodyheight("100px");
                        tab.setBodywidth("100%");
                        tab.setExpandable("false");
                        tab.setExpanded("false");
                        tab.setHighlight("false");
                        tab.setAppearance("modern");
                        tab.setId("ass__tab" + r);
                        tab.setAction("sapphire.page.getTop().assignment.changeAssignmentTab(" + r + ")");
                        JSONArray selected = null;
                        if (selection.has(resource.getId())) {
                            try {
                                selected = selection.getJSONArray(resource.getId());
                            }
                            catch (Exception selected2) {
                                // empty catch block
                            }
                        }
                        String selSDCId = "";
                        if (selection.has(resource.getId() + "_sdcid")) {
                            try {
                                selSDCId = selection.getString(resource.getId() + "_sdcid");
                            }
                            catch (Exception min) {
                                // empty catch block
                            }
                        }
                        int len = selected != null ? selected.length() : 0;
                        String label = resource.getType().length() > 0 ? resource.getType() : resource.getLabel();
                        if (len > 0) {
                            token = new HashMap<String, String>();
                            token.put("len", Integer.toString(len));
                            tab.setText(tp.translate(label + " ([len])", token));
                        } else {
                            token = new HashMap();
                            token.put("len", Integer.toString(len));
                            tab.setText("<span style=\"color:red;\">" + tp.translate(label + " ([len])", token) + "</span>");
                        }
                        StringBuilder tabhtml = new StringBuilder();
                        tabhtml.append("<span style=\"font-weight:normal;\">");
                        if (selected == null || selected.length() == 0) {
                            tabhtml.append(tp.translate("No")).append(" ").append(tp.translate(resource.getResourceSDC().pluraltext)).append(" ").append(tp.translate("selected")).append(".<br>");
                            tabhtml.append(tp.translate("Selecting Assign will create an activity without an")).append(" ").append(resource.getResourceSDC().singulartext).append(" ").append(tp.translate("assigned")).append(".");
                        } else if (selected.length() > 1) {
                            tabhtml.append(tp.translate("Assigning to")).append(" ").append(selected.length()).append(" ").append(tp.translate("selected")).append(" ").append(resource.getResourceSDC().pluraltext).append(":<br>");
                            tabhtml.append("&emsp;");
                            for (int t = 0; t < selected.length(); ++t) {
                                try {
                                    tabhtml.append(t > 0 ? (t < selected.length() - 1 ? ", " : " " + tp.translate("and") + " ") : "").append(selected.get(t));
                                    continue;
                                }
                                catch (Exception exception) {
                                    // empty catch block
                                }
                            }
                        } else {
                            try {
                                if (selSDCId.equalsIgnoreCase(resource.getResourceSDC().getName())) {
                                    tabhtml.append(tp.translate("Assigning to")).append(" ").append(resource.getResourceSDC().singulartext).append(" ").append(selected.get(0));
                                }
                                tabhtml.append(tp.translate("Assigning to")).append(" ").append(tp.translate("Workarea")).append(" ").append(selected.get(0));
                            }
                            catch (Exception exception) {
                                // empty catch block
                            }
                        }
                        tabhtml.append("</span>");
                        tab.setContent(tabhtml.toString());
                        tabGroup.setTab(tab);
                    }
                    html.append(tabGroup.getHtml());
                }
                html.append("</td>");
                if (!isActivitySelection) {
                    html.append("</tr><tr>");
                    html.append("<td style=\"vertical-align:top;\">");
                    html.append("<strong>").append("Activity Details").append("</strong><br>");
                    html.append(tp.translate("Activity description: "));
                    html.append("<input type=\"input\" name=\"activitydesc\" value=\"").append(activityDesc).append("\">");
                    html.append("&nbsp;");
                    html.append("&nbsp;");
                    html.append(tp.translate("Auto Activate: "));
                    html.append("<input type=\"checkbox\" name=\"autoactivate\">");
                    html.append("</td>");
                }
                html.append("</tr>");
                html.append("</table>");
            }
        } else {
            throw new SapphireException(tp.translate("No SDI's selected."));
        }
        return html.toString();
    }

    public static AssignmentPageResourceData generateResources(String sdcid, String keyid1, String activityQuery, String departmentid, AssignmentPage.ResourceSDC resourceSDC, Instant from, Instant to, String resourcetype, String resourcemodel, SDIProcessor sdiProcessor, QueryProcessor qp, String connectionId) {
        AssignmentPageResourceData out = new AssignmentPageResourceData("", "");
        String workareaids = "";
        String resourceKeyid1 = keyid1;
        if (sdcid.length() == 0) {
            sdcid = resourceSDC.getName();
        }
        if (sdcid.equalsIgnoreCase(AssignmentPage.ResourceSDC.INSTRUMENT.getName()) || sdcid.equalsIgnoreCase(AssignmentPage.ResourceSDC.USER.getName())) {
            AssignmentPageResourceContainer resource;
            try {
                resourceSDC = AssignmentPage.ResourceSDC.valueOf(sdcid.toUpperCase());
            }
            catch (Exception exception) {
                // empty catch block
            }
            if (keyid1.length() == 0 && departmentid.length() > 0) {
                WAPCommands wapCommands = new WAPCommands(connectionId);
                workareaids = wapCommands.getDepartmentUserWorkareas(departmentid);
            } else if (keyid1.length() > 0 && !keyid1.contains(";")) {
                WAPCommands wapCommands = new WAPCommands(connectionId);
                workareaids = sdcid.equalsIgnoreCase(AssignmentPage.ResourceSDC.INSTRUMENT.getName()) ? wapCommands.getInstrumentWorkarea(keyid1) : wapCommands.getUserWorkareas(keyid1);
            }
            if (resourceSDC == AssignmentPage.ResourceSDC.USER) {
                resourcetype = "Analyst";
            }
            if ((resource = AssignmentPageUtil.getResourceData(resourceSDC, 0, "", resourcetype, resourcemodel, "", "", 0, resourceKeyid1, "", "", workareaids, departmentid, false, sdiProcessor)) != null) {
                out.resources.add(resource);
            }
        } else if (sdcid.equalsIgnoreCase("Department")) {
            AssignmentPageResourceContainer resource;
            WAPCommands wapCommands = new WAPCommands(connectionId);
            List<String> users = wapCommands.getWorkareaUsers(keyid1, from, to);
            StringBuilder sb = new StringBuilder();
            for (String user : users) {
                if (sb.length() > 0) {
                    sb.append(";");
                }
                sb.append(user);
            }
            resourceKeyid1 = sb.toString();
            workareaids = keyid1;
            if (resourceSDC == AssignmentPage.ResourceSDC.USER) {
                resourcetype = "Analyst";
            }
            if ((resource = AssignmentPageUtil.getResourceData(resourceSDC, 0, "", resourcetype, resourcemodel, "", "", 0, resourceKeyid1, "", "", workareaids, departmentid, false, sdiProcessor)) != null) {
                out.resources.add(resource);
            }
        } else if (sdcid.equalsIgnoreCase("LV_Activity") && (keyid1.length() > 0 || activityQuery.length() > 0)) {
            AssignmentPageResourceContainer resource;
            SafeSQL safeSQL = new SafeSQL();
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT activityresource.analystid, activityresource.instrumentid, activityresource.workareadepartmentid, activityresource.analysttype, activityresource.instrumenttypeid, activityresource.instrumentmodelid FROM activityresource WHERE ");
            if (activityQuery.length() > 0) {
                sql.append("activityresource.activityid in (" + activityQuery + ") ");
            } else {
                sql.append("activityresource.activityid in (" + safeSQL.addIn(keyid1, ";") + ") ");
            }
            sql.append("AND activityresource.resourcetypeflag = ?");
            safeSQL.addVar(resourceSDC == AssignmentPage.ResourceSDC.USER ? "A" : "I");
            DataSet resources = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (resources != null && resources.getRowCount() > 0) {
                StringBuilder rKeyid1 = new StringBuilder();
                StringBuilder wa = new StringBuilder();
                ArrayList<String> fkeyid1 = new ArrayList<String>();
                ArrayList<String> fwa = new ArrayList<String>();
                for (int i = 0; i < resources.getRowCount(); ++i) {
                    String was;
                    String type;
                    String cur;
                    String model = "";
                    if (resourceSDC == AssignmentPage.ResourceSDC.USER) {
                        cur = resources.getValue(i, "analystid", "");
                        type = resources.getValue(i, "analysttype", "");
                    } else {
                        cur = resources.getValue(i, "instrumentid", "");
                        type = resources.getValue(i, "instrumenttypeid", "");
                        model = resources.getValue(i, "instrumentmodelid", "");
                    }
                    if (resourcetype.length() != 0 && !resourcetype.equalsIgnoreCase(type) || resourcemodel.length() != 0 && !resourcemodel.equalsIgnoreCase(model)) continue;
                    if (!(cur.length() <= 0 || fkeyid1.size() != 0 && fkeyid1.contains(cur))) {
                        if (rKeyid1.length() > 0) {
                            rKeyid1.append(";");
                        }
                        rKeyid1.append(cur);
                        fkeyid1.add(cur);
                    }
                    if ((was = resources.getValue(i, "workareadepartmentid", "")).length() <= 0 || fwa.size() != 0 && fwa.contains(was)) continue;
                    if (wa.length() > 0) {
                        wa.append(";");
                    }
                    wa.append(was);
                    fwa.add(was);
                }
                resourceKeyid1 = rKeyid1.toString();
                workareaids = wa.toString();
            }
            if (resourceSDC == AssignmentPage.ResourceSDC.USER) {
                resourcetype = "Analyst";
            }
            if ((resource = AssignmentPageUtil.getResourceData(resourceSDC, 0, "", resourcetype, resourcemodel, "", "", 0, resourceKeyid1, "", "", workareaids, departmentid, false, sdiProcessor)) != null) {
                out.resources.add(resource);
            }
        }
        return out;
    }

    public static AssignmentPage.AssignmentPageResourceRequirement loadResources(ActivityClassHandler activityClassHandler, String keyid1, AssignmentPageResourceData currentResources, String departmentid, AssignmentPage.ResourceMode dataMode, String connectionId, SDIProcessor sdiProcessor, TranslationProcessor tp, boolean showSelected, JSONObject selection) throws SapphireException {
        AssignmentPageResourceData resourceData;
        int maxActivity;
        String foundDesc;
        String out;
        block45: {
            out = "";
            if (keyid1.contains("%3B")) {
                keyid1 = StringUtil.replaceAll(keyid1, "%3B", ";");
            }
            foundDesc = "";
            String[] keys = StringUtil.split(keyid1, ";");
            if (keys.length > 1000) {
                keys = Arrays.copyOfRange(keys, 0, 1000);
            }
            maxActivity = 1000;
            String sdcid = activityClassHandler.getSDC();
            WAPCommands wapCommands = new WAPCommands(connectionId);
            try {
                boolean loadData;
                DataSet data;
                if (activityClassHandler.getSDC().equals("LV_Activity")) {
                    data = wapCommands.getActivityResourceDetails(keyid1);
                    loadData = true;
                    resourceData = new AssignmentPageResourceData(data.getRowCount() > 0 ? data.getValue(0, "testingdepartmentid", "") : "", data.getRowCount() > 0 ? data.getValue(0, "workcontext", "") : "");
                    if (dataMode == AssignmentPage.ResourceMode.ASSIGNED) {
                        wapCommands.populateResourceSDIs(data, departmentid, new ContextMap(resourceData.getWorkContext()));
                        resourceData.setResourceRequirements(data);
                    }
                } else {
                    NewWorkDetails newWorkDetails = wapCommands.getNewWorkDetails(activityClassHandler, sdcid, keyid1);
                    maxActivity = newWorkDetails.getMaxActivitySize();
                    boolean bl = loadData = currentResources == null || !currentResources.getTestingDepartmentId().equals(newWorkDetails.getTestingDepartmentid()) || !currentResources.getWorkContext().equals(newWorkDetails.getWorkContext());
                    if (loadData) {
                        resourceData = new AssignmentPageResourceData(newWorkDetails.getTestingDepartmentid(), newWorkDetails.getWorkContext());
                        data = newWorkDetails.getResourceRequirements();
                        wapCommands.populateResourceSDIs(data, departmentid, new ContextMap(newWorkDetails.getWorkContext()));
                        resourceData.setResourceRequirements(data);
                    } else {
                        resourceData = currentResources;
                        data = resourceData.getResourceRequirements();
                    }
                }
                if (data == null || data.getRowCount() <= 0) break block45;
                int maxDuraction = 0;
                HashMap<AssignmentPage.ResourceSDC, Integer> typecount = new HashMap<AssignmentPage.ResourceSDC, Integer>();
                for (int r = 0; r < data.getRowCount(); ++r) {
                    String selresourcesdc;
                    AssignmentPageResourceContainer resource;
                    List<String> resourcesdiArray;
                    String preferedworkareas;
                    String resourceworkareas;
                    String preferred;
                    String sdis;
                    String rule = data.getValue(r, "durationrule", "");
                    if (rule.length() > 0) {
                        int duration = 0;
                        try {
                            duration = wapCommands.getDurationMinutes(rule, keys.length);
                        }
                        catch (Exception e) {
                            Trace.logWarn("Invalid duration rule found. " + e.getMessage());
                        }
                        maxDuraction = Math.max(maxDuraction, duration);
                    }
                    if (foundDesc.length() == 0) {
                        String string = foundDesc = data.isValidColumn("keyid1") ? data.getValue(r, "keyid1", "") : "";
                    }
                    if (!loadData) continue;
                    String transientSdis = "";
                    AssignmentPage.ResourceSDC resourceSDC = AssignmentPage.ResourceSDC.USER;
                    String type = data.getValue(r, "resourcetypeflag", "A");
                    String resourceType = "";
                    String resourceModel = "";
                    if (type.equalsIgnoreCase("A")) {
                        resourceSDC = AssignmentPage.ResourceSDC.USER;
                        resourceType = data.getValue(r, "analysttype", "");
                    } else if (type.equalsIgnoreCase("I")) {
                        resourceSDC = AssignmentPage.ResourceSDC.INSTRUMENT;
                        resourceModel = data.getValue(r, "instrumentmodelid", "");
                        resourceType = data.getValue(r, "instrumenttypeid", "");
                    }
                    if (dataMode == AssignmentPage.ResourceMode.SUGGESTED) {
                        sdis = data.getValue(r, "_resourcesdis", "");
                        if (data.getValue(r, "_tempresourcesdis", "").length() > 0) {
                            transientSdis = data.getValue(r, "_tempresourcesdis", "");
                            sdis = sdis + (sdis.length() > 0 ? ";" : "") + transientSdis;
                        }
                        preferred = sdis;
                    } else if (dataMode == AssignmentPage.ResourceMode.ALL) {
                        preferred = data.getValue(r, "_resourcesdis", "");
                        if (data.getValue(r, "_tempresourcesdis", "").length() > 0) {
                            transientSdis = data.getValue(r, "_tempresourcesdis", "");
                            preferred = preferred + (preferred.length() > 0 ? ";" : "") + transientSdis;
                        }
                        sdis = resourceSDC == AssignmentPage.ResourceSDC.USER ? wapCommands.getDepartmentUsers(departmentid) : wapCommands.getDepartmentInstruments(departmentid, "", resourceType, resourceModel);
                    } else {
                        preferred = data.getValue(r, "_resourcesdis", "");
                        if (data.getValue(r, "_tempresourcesdis", "").length() > 0) {
                            transientSdis = data.getValue(r, "_tempresourcesdis", "");
                            preferred = preferred + (preferred.length() > 0 ? ";" : "") + transientSdis;
                        }
                        sdis = "";
                    }
                    if (dataMode == AssignmentPage.ResourceMode.SUGGESTED) {
                        preferedworkareas = resourceworkareas = data.getValue(r, "_resourcesworkareas", "");
                    } else if (dataMode == AssignmentPage.ResourceMode.ALL) {
                        preferedworkareas = data.getValue(r, "_resourcesworkareas", "");
                        resourceworkareas = resourceSDC == AssignmentPage.ResourceSDC.USER ? wapCommands.getDepartmentUserWorkareas(departmentid) : wapCommands.getDepartmentInstrumentWorkareas(departmentid, resourceType, resourceModel);
                    } else {
                        preferedworkareas = data.getValue(r, "_resourcesworkareas", "");
                        resourceworkareas = "";
                    }
                    boolean alreadyAssigned = false;
                    if (dataMode == AssignmentPage.ResourceMode.SUGGESTED && sdis.length() == 0 && resourceworkareas.length() == 0) {
                        boolean bl = alreadyAssigned = type.equalsIgnoreCase("A") && (data.getValue(r, "analystid", "").length() > 0 || data.getValue(r, "_resourcesworkareas", "").length() > 0) || type.equalsIgnoreCase("I") && (data.getValue(r, "instrumentid", "").length() > 0 || data.getValue(r, "_resourcesworkareas", "").length() > 0);
                    }
                    if (alreadyAssigned && (alreadyAssigned || sdis.length() != 0) || !type.equalsIgnoreCase("A") && !type.equalsIgnoreCase("I")) continue;
                    if (resourceSDC == AssignmentPage.ResourceSDC.USER) {
                        if (dataMode == AssignmentPage.ResourceMode.ASSIGNED && (sdis = data.getValue(r, "analystid", "")).length() == 0) {
                            resourceworkareas = data.getValue(r, "workareadepartmentid", "");
                        }
                    } else if (resourceSDC == AssignmentPage.ResourceSDC.INSTRUMENT && dataMode == AssignmentPage.ResourceMode.ASSIGNED && (sdis = data.getValue(r, "instrumentid")).length() == 0) {
                        resourceworkareas = data.getValue(r, "workareadepartmentid", "");
                    }
                    if (!typecount.containsKey((Object)resourceSDC)) {
                        typecount.put(resourceSDC, 0);
                    }
                    String autoresource = data.getValue(r, "autoassignflag").equalsIgnoreCase("I") ? (resourceSDC == AssignmentPage.ResourceSDC.USER ? data.getValue(r, "autoassignanalystid", "") : data.getValue(r, "autoassigninstrumentid", "")) : "";
                    String autoworkarea = data.getValue(r, "autoassignflag").equalsIgnoreCase("W") && autoresource.length() == 0 ? data.getValue(r, "autoassigndepartmentid", "") : "";
                    List<String> resourceworkareasArray = Arrays.asList(resourceworkareas.length() > 0 ? StringUtil.split(resourceworkareas, ";") : new String[]{});
                    if (!resourceworkareasArray.contains(autoworkarea)) {
                        autoworkarea = "";
                    }
                    if (!(resourcesdiArray = Arrays.asList(sdis.length() > 0 ? StringUtil.split(sdis, ";") : new String[]{})).contains(autoresource)) {
                        autoresource = "";
                    }
                    if ((resource = AssignmentPageUtil.getResourceData(resourceSDC, data.getInt(r, "resourcenum", 0), data.getValue(r, "resourcelabel", ""), resourceType, resourceModel, autoresource, autoworkarea, (Integer)typecount.get((Object)resourceSDC), sdis, "", "", resourceworkareas, preferred, preferedworkareas, transientSdis, departmentid, true, sdiProcessor)) == null) continue;
                    if (dataMode == AssignmentPage.ResourceMode.ALL) {
                        resource.setShowAll(true);
                    }
                    Integer count = (Integer)typecount.get((Object)resourceSDC);
                    typecount.put(resourceSDC, count + 1);
                    resourceData.getResources().add(resource);
                    if (selection == null || resource.autoworkarea.length() <= 0 && resource.autoresource.length() <= 0) continue;
                    String selresource = resource.autoresource.length() > 0 ? resource.autoresource : resource.autoworkarea;
                    JSONArray selresourceArray = new JSONArray();
                    selresourceArray.put(selresource);
                    String string = selresourcesdc = resource.autoresource.length() > 0 ? resource.getResourceSDC().getName() : "Department";
                    if (!selection.has(resource.resourceid)) {
                        selection.put(resource.resourceid, selresourceArray);
                        if (selection.has(resource.resourceid + "_sdcid")) {
                            selection.remove(resource.resourceid + "_sdcid");
                        }
                        selection.put(resource.resourceid + "_sdcid", selresourcesdc);
                        continue;
                    }
                    if (selection.getString(resource.resourceid).length() != 0) continue;
                    selection.remove(resource.resourceid);
                    selection.put(resource.resourceid, selresourceArray);
                    if (selection.has(resource.resourceid + "_sdcid")) {
                        selection.remove(resource.resourceid + "_sdcid");
                    }
                    selection.put(resource.resourceid + "_sdcid", selresourcesdc);
                }
                if (maxDuraction <= 0) break block45;
                String dur = "";
                try {
                    dur = WAPCommands.getDurationDisplay(maxDuraction).trim();
                }
                catch (Exception exception) {
                    // empty catch block
                }
                if (dur.length() > 0) {
                    if (showSelected) {
                        out = tp.translate("Selected") + " " + keys.length + " " + (keys.length > 1 ? tp.translate("items") : tp.translate("item"));
                        out = out + " " + (keys.length > 1 ? tp.translate("totaling") : tp.translate("taking")) + " " + dur;
                    } else {
                        out = dur;
                    }
                }
            }
            catch (Exception e) {
                throw new SapphireException(e.getMessage(), e);
            }
        }
        return new AssignmentPage.AssignmentPageResourceRequirement(resourceData, out, foundDesc, maxActivity);
    }

    public static AssignmentPage.AssignmentPageResourceRequirement getResourceRequirement(ActivityClassHandler activityClassHandler, String keyid1, String departmentid, AssignmentPageResourceData resourceData, String connectionId, SDIProcessor sdiProcessor, TranslationProcessor tp, boolean showSelected) throws SapphireException {
        return AssignmentPageUtil.loadResources(activityClassHandler, keyid1, resourceData, departmentid, AssignmentPage.ResourceMode.SUGGESTED, connectionId, sdiProcessor, tp, showSelected, null);
    }

    public static String getDuration(ActivityClassHandler activityClassHandler, String keyid1, String departmentid, AssignmentPageResourceData resourceData, String connectionId, SDIProcessor sdiProcessor, TranslationProcessor tp, boolean showSelected) throws SapphireException {
        AssignmentPage.AssignmentPageResourceRequirement rr = AssignmentPageUtil.loadResources(activityClassHandler, keyid1, resourceData, departmentid, AssignmentPage.ResourceMode.SUGGESTED, connectionId, sdiProcessor, tp, showSelected, null);
        return rr.getDuration();
    }

    public static ZonedDateTime[] getTrueDateRange(AssignmentPage.CalendarView calendarView, ZonedDateTime outFrom, ZonedDateTime outTo) {
        switch (calendarView) {
            case DAY: 
            case WEEK: 
            case AGENDA: 
            case RESOURCE_TIMELINE: {
                outFrom = outFrom.plusDays(1L);
                outTo = outTo.minusDays(1L);
                break;
            }
            case MONTH: {
                outFrom = outFrom.plusDays(7L);
                outTo = outTo.minusDays(7L);
                outFrom = outFrom.plusDays(1L);
                outTo = outTo.minusDays(1L);
                break;
            }
            case RESOURCE_WEEK: {
                outFrom = outFrom.plusDays(1L);
                outTo = outTo.minusDays(1L);
            }
        }
        return new ZonedDateTime[]{outFrom, outTo};
    }

    public static void adjustRangeToCalendarView(AssignmentPage.CalendarView calendarView, AssignmentPage.AssignmentPageTimeRange timeRange, M18NUtil m18) {
        AssignmentPageUtil.adjustRangeToCalendarView(calendarView, timeRange, false, m18);
    }

    public static void adjustRangeToCalendarView(AssignmentPage.CalendarView calendarView, AssignmentPage.AssignmentPageTimeRange timeRange, boolean avoidBoundary, M18NUtil m18) {
        if (timeRange == null || timeRange.from == null) {
            return;
        }
        switch (calendarView) {
            case DAY: {
                timeRange.from = timeRange.from.truncatedTo(ChronoUnit.DAYS);
                timeRange.to = timeRange.from.plus(1L, ChronoUnit.DAYS);
                break;
            }
            case WEEK: 
            case AGENDA: 
            case RESOURCE_WEEK: {
                timeRange.from = CalendarConverter.getStartOfWeek(timeRange.from, m18.getLocale());
                timeRange.to = timeRange.from.plus(7L, ChronoUnit.DAYS);
                break;
            }
            case MONTH: {
                timeRange.from = timeRange.from.truncatedTo(ChronoUnit.DAYS).withDayOfMonth(1);
                timeRange.to = timeRange.from.plusDays(YearMonth.of(timeRange.from.getYear(), timeRange.from.getMonth()).lengthOfMonth());
                break;
            }
            case RESOURCE_TIMELINE: {
                timeRange.from = CalendarConverter.getStartOfWeek(timeRange.from, m18.getLocale());
                timeRange.to = timeRange.from.plus(14L, ChronoUnit.DAYS);
            }
        }
    }

    public static String getAreaSelector(AssignmentPage.ViewMode viewMode, ZonedDateTime clientFromDate, ZonedDateTime clientToDate, ZoneId displayTimeZone, String departmentid, String shiftid, AssignmentPageResourceContainer focusedResource, AssignmentPageResourceData resourceData, String focusid, JSONArray selection, String selectedSDCId, String sdcid, PropertyList userConfig, String userConfigPrefix, PropertyList element, AssignmentPage.OperatingMode operatingMode, AssignmentPage.ColorScheme colorScheme, SDIProcessor sdiProcessor, TranslationProcessor translationProcessor, SapphireConnection sapphireConnection, PageContext pageContext, ZoneId segmentTimeZone) {
        StringBuilder html = new StringBuilder();
        if (viewMode != null) {
            String label;
            String wid;
            DataSet workareas;
            String depid;
            String ddlabel;
            String fSdc;
            String fId;
            if (viewMode.showFocusSelector) {
                html.append(translationProcessor.translate("Focus")).append(":");
                html.append("&nbsp;");
                html.append("<select name=\"focussel\" id=\"focussel\" style=\"width:130px;\" onchange=\"assignment.selectorsChange(this)\">");
                if (resourceData != null && resourceData.getResources() != null) {
                    for (int i = 0; i < resourceData.getResources().size(); ++i) {
                        int count;
                        AssignmentPageResourceContainer resourceContainer = resourceData.getResources().get(i);
                        int n = count = resourceContainer.getData() != null ? resourceContainer.getData().getRowCount() : 0;
                        String l = resourceContainer.getType().length() > 0 ? resourceContainer.getType() : (resourceContainer.getModel().length() > 0 ? resourceContainer.getModel() : resourceContainer.getLabel());
                        html.append("<option value=\"").append(resourceContainer.getId()).append("\"").append(resourceContainer.getId().equals(focusedResource.getId()) ? " SELECTED" : "").append(">").append(SafeHTML.encodeForHTML(translationProcessor.translate(l))).append(" (").append(count).append(")</option>");
                    }
                }
                html.append("</select>");
                html.append("&nbsp;");
                html.append("&nbsp;");
            }
            if (focusid.contains("|")) {
                String[] t = StringUtil.split(focusid, "|");
                fId = t[0];
                fSdc = t[1];
            } else {
                fId = focusid;
                String string = fSdc = focusedResource != null ? focusedResource.getResourceSDC().getName() : "";
            }
            if (focusedResource != null && focusedResource.getResourceSDC() == AssignmentPage.ResourceSDC.USER && viewMode.showWorkSelector) {
                boolean foundUser = false;
                if (viewMode.showWorkSelector) {
                    int r;
                    ddlabel = focusedResource.getType().length() > 0 ? focusedResource.getType() : focusedResource.getLabel();
                    html.append(SafeHTML.encodeForHTML(ddlabel)).append(":");
                    html.append("&nbsp;");
                    html.append("<select name=\"usersel\" id=\"usersel\" style=\"width:115px;\" onchange=\"assignment.selectorsChange(this)\">");
                    html.append("<option value=\"\"").append(fId == null || fId.length() == 0 ? " selected" : "").append(">").append(translationProcessor.translate("All Analysts")).append("</option>");
                    DataSet users = focusedResource.getData();
                    if (users != null && users.getRowCount() > 0) {
                        String[] trans = sdcid.equalsIgnoreCase("Department") ? new String[]{} : (focusedResource.transientSDI.length() > 0 ? StringUtil.split(focusedResource.transientSDI, ";") : new String[]{});
                        for (r = 0; r < users.getRowCount(); ++r) {
                            depid = users.getValue(r, "basedepartment", "");
                            if (departmentid != null && departmentid.length() != 0 && depid.length() != 0 && !departmentid.equals(depid)) continue;
                            String uid = users.getValue(r, "sysuserid", "");
                            boolean transientItem = false;
                            for (String tran : trans) {
                                if (!uid.equalsIgnoreCase(tran)) continue;
                                transientItem = true;
                                break;
                            }
                            String value = uid + "|" + AssignmentPage.ResourceSDC.USER.getName();
                            String label2 = users.getValue(r, "sysuserdesc", uid);
                            if (transientItem) {
                                label2 = label2 + " (T)";
                            } else if (focusedResource.autoresource.length() > 0 && focusedResource.autoresource.equals(uid)) {
                                label2 = label2 + " (P)";
                            }
                            html.append("<option value=\"").append(value).append("\"").append(fId != null && fId.length() > 0 && fSdc.equalsIgnoreCase(focusedResource.getResourceSDC().getName()) && fId.equals(uid) ? " selected" : "").append(">");
                            if (fId.equals(uid) && fSdc.equalsIgnoreCase(focusedResource.getResourceSDC().getName())) {
                                foundUser = true;
                            }
                            html.append(SafeHTML.encodeForHTML(label2));
                            html.append("</option>");
                        }
                    }
                    workareas = focusedResource.getWorkareas();
                    if (fId != null && workareas != null && workareas.getRowCount() > 0) {
                        for (r = 0; r < workareas.getRowCount(); ++r) {
                            depid = workareas.getValue(r, "parentdepartmentid", "");
                            if (departmentid != null && departmentid.length() != 0 && !departmentid.equals(depid)) continue;
                            wid = workareas.getValue(r, "departmentid", "");
                            String value = wid + "|" + "Department";
                            label = workareas.getValue(r, "departmentdesc", wid);
                            html.append("<option value=\"").append(value).append("\"").append(fId != null && fId.length() > 0 && fSdc.equalsIgnoreCase("Department") && fId.equals(wid) ? " selected" : "").append(">");
                            if (fId.equals(wid) && fSdc.equalsIgnoreCase("Department")) {
                                foundUser = true;
                            }
                            html.append(SafeHTML.encodeForHTML(label));
                            html.append("</option>");
                        }
                    }
                    html.append("</select>");
                }
                html.append("&nbsp;");
                AssignmentPageUtil.renderWorkloadIcon(html, viewMode, focusedResource, focusid, foundUser, clientFromDate, clientToDate, null, colorScheme, sdiProcessor.getConnectionid(), translationProcessor, pageContext, segmentTimeZone);
            } else if (focusedResource != null && focusedResource.getResourceSDC() == AssignmentPage.ResourceSDC.INSTRUMENT && viewMode.showWorkSelector) {
                boolean foundInstrument = false;
                ddlabel = focusedResource.getType().length() > 0 ? focusedResource.getType() : (focusedResource.getModel().length() > 0 ? focusedResource.getModel() : focusedResource.getLabel());
                html.append(ddlabel).append(":");
                html.append("&nbsp;");
                html.append("<select name=\"instrumentsel\" id=\"instrumentsel\" style=\"width:115px;\" onchange=\"assignment.selectorsChange(this)\">");
                html.append("<option value=\"\"").append(focusid == null || focusid.length() == 0 ? " selected" : "").append(">").append(translationProcessor.translate("All Instruments")).append("</option>");
                DataSet instruments = focusedResource.getData();
                if (instruments != null) {
                    for (int r = 0; r < instruments.getRowCount(); ++r) {
                        String instrumentid = instruments.getValue(r, "instrumentid", "");
                        String value = instrumentid + "|" + AssignmentPage.ResourceSDC.INSTRUMENT.getName();
                        String label3 = instruments.getValue(r, "instrumentdesc", instrumentid);
                        html.append("<option value=\"").append(value).append("\"").append(focusid != null && focusid.length() > 0 && fSdc.equalsIgnoreCase(focusedResource.getResourceSDC().getName()) && fId.equals(instrumentid) ? " selected" : "").append(">");
                        if (fId.equals(instrumentid) && fSdc.equalsIgnoreCase(focusedResource.getResourceSDC().getName())) {
                            foundInstrument = true;
                        }
                        html.append(SafeHTML.encodeForHTML(label3));
                        html.append("</option>");
                    }
                }
                workareas = focusedResource.getWorkareas();
                if (fId != null && workareas != null && workareas.getRowCount() > 0) {
                    for (int r = 0; r < workareas.getRowCount(); ++r) {
                        depid = workareas.getValue(r, "parentdepartmentid", "");
                        if (departmentid != null && departmentid.length() != 0 && !departmentid.equals(depid)) continue;
                        wid = workareas.getValue(r, "departmentid", "");
                        String value = wid + "|" + "Department";
                        label = workareas.getValue(r, "departmentdesc", wid);
                        html.append("<option value=\"").append(value).append("\"").append(fId != null && fId.length() > 0 && fSdc.equalsIgnoreCase("Department") && fId.equals(wid) ? " selected" : "").append(">");
                        if (fId.equals(wid) && fSdc.equalsIgnoreCase("Department")) {
                            foundInstrument = true;
                        }
                        html.append(SafeHTML.encodeForHTML(label));
                        html.append("</option>");
                    }
                }
                html.append("</select>");
                html.append("&nbsp;");
                AssignmentPageUtil.renderWorkloadIcon(html, viewMode, focusedResource, focusid, foundInstrument, clientFromDate, clientToDate, null, colorScheme, sdiProcessor.getConnectionid(), translationProcessor, pageContext, segmentTimeZone);
            }
            if (viewMode.showWorkSelector && focusid.length() > 0 && focusedResource != null) {
                html.append("&nbsp;");
                boolean selected = false;
                if (selection != null) {
                    String[] t = StringUtil.split(focusid, "|");
                    String fid = t[0];
                    String fsdc = "";
                    if (t.length > 1) {
                        fsdc = t[1];
                    }
                    if (fsdc.length() == 0) {
                        fsdc = focusedResource.getResourceSDC().getName();
                    }
                    if (selectedSDCId.length() == 0) {
                        selectedSDCId = focusedResource.getResourceSDC().getName();
                    }
                    if (fsdc.equalsIgnoreCase(selectedSDCId)) {
                        for (int a = 0; a < selection.length(); ++a) {
                            try {
                                if (!selection.getString(a).equals(fid)) continue;
                                selected = true;
                                break;
                            }
                            catch (Exception exception) {
                                // empty catch block
                            }
                        }
                    }
                }
                if (focusedResource != null) {
                    html.append("<div id=\"selectresourcecontainer\"><div>Select ").append(focusedResource.getResourceSDC().singulartext).append("</div><div><input type=\"checkbox\"").append(selected ? " checked" : "").append(" id=\"selectresourcecheckbox\" onclick=\"assignment.selectResource('" + focusedResource.getId() + "','" + fId + "','").append(fSdc).append("')\"></div></div>");
                    html.append("&nbsp;");
                }
            }
            html.append("<div id=\"assignment_timezoneDIV\">");
            Image image = null;
            try {
                QueryProcessor queryProcessor = new QueryProcessor(sapphireConnection.getConnectionId());
                TimeZone departmentTimezone = (TimeZone)CalendarFactory.getDepartmentCalendarDetails(queryProcessor, departmentid)[1];
                DataSet plannerDS = queryProcessor.getPreparedSqlDataSet("SELECT sysuserid, timezone FROM sysuser WHERE sysuserid=?", (Object[])new String[]{sapphireConnection.getSysuserId()});
                TimeZone plannerTimeZone = plannerDS.getValue(0, "timezone").length() > 0 ? TimeZone.getTimeZone(plannerDS.getValue(0, "timezone")) : TimeZone.getDefault();
                html.append(translationProcessor.translate("Timezone")).append(":");
                html.append("<select name=\"assignment_timezone\" id=\"assignment_timezone\" value=\"").append(TimeZone.getTimeZone(displayTimeZone).getDisplayName()).append("\" onChange=\"assignment.toggleTimewarp(this)\">");
                if (plannerTimeZone.toZoneId().getRules().equals(departmentTimezone.toZoneId().getRules())) {
                    html.append("<option ").append(plannerTimeZone.toZoneId().getRules().equals(displayTimeZone.getRules()) ? "selected " : "").append("value=\"").append(plannerTimeZone.getID()).append("\">").append(plannerTimeZone.toZoneId().getId()).append(translationProcessor.translate(" (You & Lab)")).append("</option>");
                } else {
                    html.append("<option ").append(plannerTimeZone.toZoneId().getRules().equals(displayTimeZone.getRules()) ? "selected " : "").append("value=\"").append(plannerTimeZone.getID()).append("\">").append(plannerTimeZone.toZoneId().getId()).append(translationProcessor.translate(" (You)")).append("</option>");
                    html.append("<option ").append(departmentTimezone.toZoneId().getRules().equals(displayTimeZone.getRules()) ? "selected " : "").append("value=\"").append(departmentTimezone.getID()).append("\">").append(departmentTimezone.toZoneId().getId()).append(translationProcessor.translate(" (Lab)")).append("</option>");
                }
                html.append("</select>");
                html.append("</div>");
                html.append("<div id=\"legendIcon\" class=\"legendicon\">");
                image = new Image(pageContext);
                image.setImageId("FlatBlackStack2");
                image.setDimensions(16, 16);
                image.setElementid("imLegend");
                image.setTitle("Legend");
                html.append(image.getHtml());
                html.append("</div>");
            }
            catch (SapphireException queryProcessor) {
                // empty catch block
            }
            html.append("<div id=\"legendContent\" style=\"display:none;\">");
            if (colorScheme != AssignmentPage.ColorScheme.NONE) {
                if (viewMode != AssignmentPage.ViewMode.RESOURCE) {
                    html.append(translationProcessor.translate("Event Colors"));
                }
                html.append("<div id=\"eventColors\" class=\"eventColors\" ").append(viewMode == AssignmentPage.ViewMode.RESOURCE ? " style=\"display:none\"" : "").append(">");
                html.append(AssignmentPageUtil.getEventColorsKey("", null, translationProcessor));
                html.append("</div>");
            }
            if (colorScheme != AssignmentPage.ColorScheme.NONE) {
                html.append(translationProcessor.translate("Workload Colors"));
                html.append("<div>");
                html.append(AssignmentPageUtil.getWorkloadColorsKey(translationProcessor));
                html.append("</div>");
            }
            html.append(translationProcessor.translate("Icons"));
            html.append("<div>");
            html.append(AssignmentPageUtil.getEventIconsKey(translationProcessor));
            html.append("</div>");
            html.append("</div>");
            html.append("<div id=\"displayIcon\" class=\"displayicon\" onclick=\"assignment.changeDisplayOptions()\">");
            image = new Image(pageContext);
            image.setImageId("FlatBlackCog");
            image.setDimensions(16, 16);
            image.setElementid("imgDisplay");
            image.setTitle("Display Options");
            html.append(image.getHtml());
            html.append("</div>");
            PropertyList displayoptions = null;
            if (element != null && element.getPropertyList("pagedata") != null && element.getPropertyList("pagedata").getPropertyList("displayoptions") != null) {
                displayoptions = element.getPropertyList("pagedata").getPropertyList("displayoptions");
            }
            html.append("<div id=\"displayOptionsContent\"").append(" style=\"display:none\"").append(">");
            html.append(AssignmentPageUtil.getDisplayOptions(displayoptions, sdcid, userConfig, userConfigPrefix, operatingMode, colorScheme, false, translationProcessor));
            html.append("</div>");
            if (viewMode.showWorkSelector && focusid.length() > 0 && focusedResource != null) {
                html.append("<div class=\"resourceicon\" onclick=\"assignment.nextResource(this)\">");
                Image image2 = new Image(pageContext);
                image2.setImageId("FlatBlackArrowRight");
                image2.setDimensions(24, 24);
                image2.setElementid("NextItem");
                image2.setTitle(translationProcessor.translate("Next") + " " + focusedResource.getResourceSDC().singulartext);
                html.append(image2.getHtml());
                html.append("</div>");
                html.append("&nbsp;");
                html.append("<div class=\"resourceicon\" onclick=\"assignment.prevResource(this)\">");
                image2 = new Image(pageContext);
                image2.setImageId("FlatBlackArrowLeft");
                image2.setDimensions(24, 24);
                image2.setElementid("PrevItem");
                image2.setTitle(translationProcessor.translate("Previous") + " " + focusedResource.getResourceSDC().singulartext);
                html.append(image2.getHtml());
                html.append("</div>");
            }
            if (viewMode == AssignmentPage.ViewMode.RESOURCE) {
                html.append("<div id=\"showallcontainer\">");
                if (focusedResource.isShowAll()) {
                    html.append("<a href=\"javascript:assignment.refreshFocusedResource(false);\">").append(translationProcessor.translate("Show Suggested")).append("</a>");
                } else {
                    html.append("<a href=\"javascript:assignment.refreshFocusedResource(true);\">").append(translationProcessor.translate("Show All")).append("</a>");
                }
                html.append("</div>");
            }
            html.append(viewMode.getAreaSelector(translationProcessor));
        }
        return html.toString();
    }

    public static void renderWorkloadIcon(StringBuilder html, AssignmentPage.ViewMode viewMode, AssignmentPageResourceContainer selectedResource, String focusid, boolean foundItem, ZonedDateTime clientFromDate, ZonedDateTime clientToDate, List<WAPAvailability> existingAvailability, AssignmentPage.ColorScheme colorScheme, String connectionid, TranslationProcessor translationProcessor, PageContext pageContext, ZoneId segmentTimeZone) {
        if (viewMode != null) {
            String fSdc;
            String fId;
            if (focusid.contains("|")) {
                String[] t = StringUtil.split(focusid, "|");
                fId = t[0];
                fSdc = t[1];
            } else {
                fId = focusid;
                fSdc = selectedResource != null ? selectedResource.getResourceSDC().getName() : "";
            }
            boolean isWorkarea = false;
            if (selectedResource != null && fId != null && fId.length() > 0 && viewMode.showWorkSelector && foundItem) {
                List<WAPAvailability> availability = null;
                if (existingAvailability != null) {
                    availability = existingAvailability;
                } else {
                    WAPAvailabilitySelector wapAvailabilitySelector = new WAPAvailabilitySelector(connectionid);
                    WAPAvailabilityOptions wapAvailabilityOptions = new WAPAvailabilityOptions();
                    try {
                        if (selectedResource.getResourceSDC() == AssignmentPage.ResourceSDC.USER && !fSdc.equalsIgnoreCase("Department")) {
                            availability = wapAvailabilitySelector.getUserAvailabilityBetween(fId, clientFromDate.toInstant(), clientToDate.toInstant(), wapAvailabilityOptions, segmentTimeZone);
                        } else if (selectedResource.getResourceSDC() == AssignmentPage.ResourceSDC.INSTRUMENT && !fSdc.equalsIgnoreCase("Department")) {
                            availability = wapAvailabilitySelector.getInstrumentAvailabilityBetween(fId, clientFromDate.toInstant(), clientToDate.toInstant(), wapAvailabilityOptions, segmentTimeZone);
                        } else if (fSdc.equalsIgnoreCase("Department")) {
                            isWorkarea = true;
                            availability = selectedResource.getResourceSDC() == AssignmentPage.ResourceSDC.INSTRUMENT ? wapAvailabilitySelector.getInstrumentWorkareaAvailabilityBetween(fId, clientFromDate.toInstant(), clientToDate.toInstant(), wapAvailabilityOptions, selectedResource.resourceType, selectedResource.resourceModel, segmentTimeZone) : wapAvailabilitySelector.getUserWorkareaAvailabilityBetween(fId, clientFromDate.toInstant(), clientToDate.toInstant(), wapAvailabilityOptions, segmentTimeZone);
                        }
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
                Image image = new Image(pageContext);
                image.setHeight(24);
                image.setWidth(24);
                if (fSdc.equalsIgnoreCase(selectedResource.getResourceSDC().getName())) {
                    image.setImageId(selectedResource.getResourceSDC().imageid);
                } else {
                    isWorkarea = true;
                    if (selectedResource.getResourceSDC() == AssignmentPage.ResourceSDC.USER) {
                        image.setImageId("FlatBlackGroup");
                    } else {
                        image.setImageId("FlatBlackThermometerGroup");
                    }
                }
                html.append("<div class=\"icon\" onclick=\"\" data-workarea=\"").append(isWorkarea ? "Y" : "N").append("\" data-").append(selectedResource.getResourceSDC().keycolid).append("=\"").append(fId).append("\" onmousemove=\"assignment.workloadMouseMove(event)\" onmouseout=\"assignment.workloadMouseOut(event)\">");
                html.append("<div class=\"icon_img\">");
                int percent = availability != null ? AssignmentPageUtil.getPercentAvailability(availability) : 0;
                String textclass = "";
                String textc = "#000000";
                if (colorScheme != AssignmentPage.ColorScheme.NONE) {
                    if (percent > 100) {
                        image.setColor("Red");
                        textclass = "lv_exception";
                        textc = "#ff0000";
                    } else if (percent > 80) {
                        image.setColor("Red");
                    } else if (percent > 60) {
                        image.setColor("Orange");
                    } else if (percent > 40) {
                        image.setColor("Yellow");
                    } else if (percent > 20) {
                        image.setColor("Blue");
                    } else {
                        image.setColor("Green");
                    }
                } else {
                    image.setColor("Gray");
                }
                String title = translationProcessor.translate("Workload") + " " + percent + "%";
                image.setTitle(title);
                html.append(image.getHtml());
                html.append("</div>");
                html.append("&nbsp;");
                html.append("<div class=\"icon_num").append(textclass.length() > 0 ? " " + textclass : "").append("\" style=\"color:").append(textc).append(";\">");
                html.append(percent).append("%");
                html.append("</div>");
                html.append("</div>");
            }
        }
    }

    public static String getResourceSidebarSection(AssignmentPageResourceContainer focusedResource, PropertyList element, TranslationProcessor tp, QueryProcessor qp) {
        StringBuilder html = new StringBuilder();
        html.append("<div>");
        html.append(tp.translate("Resource")).append(" ").append("<br>");
        html.append("<select name=\"resource\" onchange=\"assignment.sidebarChange(this);\">");
        html.append("<option value=\"USER\"").append(focusedResource.getResourceSDC() == AssignmentPage.ResourceSDC.USER ? " selected" : "").append(">").append("User").append("</option>");
        html.append("<option value=\"INSTRUMENT\"").append(focusedResource.getResourceSDC() == AssignmentPage.ResourceSDC.INSTRUMENT ? " selected" : "").append(">").append("Instrument").append("</option>");
        html.append("</select>");
        html.append("</div>");
        if (focusedResource.getResourceSDC() == AssignmentPage.ResourceSDC.INSTRUMENT) {
            DataSet instrumentmodels;
            DataSet instrumenttypes = qp.getSqlDataSet("SELECT DISTINCT instrumenttypeid FROM activityresource WHERE instrumenttypeid IS NOT null");
            if (instrumenttypes != null && instrumenttypes.getRowCount() > 0) {
                String rt = element.getPropertyList("pagedata").getProperty("instrumenttype", element.getPropertyList("pagedata").getProperty("instrumenttypeid", ""));
                html.append("<div>");
                html.append(tp.translate("Instrument Type")).append(" ").append("<br>");
                html.append("<select name=\"resourcetype\" onchange=\"assignment.sidebarChange(this);\">");
                html.append("<option value=\"\"></option>");
                for (int i = 0; i < instrumenttypes.getRowCount(); ++i) {
                    String it = instrumenttypes.getValue(i, "instrumenttypeid", "");
                    if (it.length() <= 0) continue;
                    html.append("<option value=\"").append(it).append("\"").append(it.equalsIgnoreCase(rt) ? " selected" : "").append(">").append(it).append("</option>");
                }
                html.append("</select>");
                html.append("</div>");
            }
            if ((instrumentmodels = qp.getSqlDataSet("SELECT DISTINCT instrumentmodelid FROM activityresource WHERE instrumentmodelid IS NOT null")) != null && instrumentmodels.getRowCount() > 0) {
                String rm = element.getPropertyList("pagedata").getProperty("instrumentmodel", element.getPropertyList("pagedata").getProperty("instrumentmodelid", ""));
                html.append("<div>");
                html.append(tp.translate("Instrument Model")).append(" ").append("<br>");
                html.append("<select name=\"resourcemodel\" onchange=\"assignment.sidebarChange(this);\">");
                html.append("<option value=\"\"></option>");
                for (int i = 0; i < instrumentmodels.getRowCount(); ++i) {
                    String im = instrumentmodels.getValue(i, "instrumentmodelid", "");
                    if (im.length() <= 0) continue;
                    html.append("<option value=\"").append(im).append("\"").append(im.equalsIgnoreCase(rm) ? " selected" : "").append(">").append(im).append("</option>");
                }
                html.append("</select>");
                html.append("</div>");
            }
        } else {
            DataSet analysttypes = qp.getSqlDataSet("SELECT DISTINCT analysttype FROM activityresource WHERE analysttype IS NOT null");
            if (analysttypes != null && analysttypes.getRowCount() > 0) {
                String string = element.getPropertyList("pagedata").getProperty("analysttype", element.getPropertyList("pagedata").getProperty("analysttypeid", ""));
            }
        }
        return html.toString();
    }

    public static String getDisplayOptions(PropertyList displayoptions, String sdcid, PropertyList userConfig, String userConfigPrefix, AssignmentPage.OperatingMode operatingmode, AssignmentPage.ColorScheme colorScheme, boolean sidebar, TranslationProcessor translationProcessor) {
        PropertyListCollection labels;
        StringBuilder html = new StringBuilder();
        String change = sidebar ? "assignment.sidebarChange(this);" : "sapphire.page.getTop().assignment.displayOptionsChange(this);";
        String colorbyDisplayOp = userConfig.getProperty(userConfigPrefix + "sidebar_colorby");
        if (colorbyDisplayOp.length() == 0) {
            String string = colorbyDisplayOp = displayoptions != null ? displayoptions.getProperty("colorby", "Resource") : "Resource";
        }
        if (colorScheme != AssignmentPage.ColorScheme.NONE) {
            html.append("<div").append(sidebar ? "" : " style=\"padding:5px;\"").append(">");
            html.append(translationProcessor.translate("Color By")).append(sidebar ? " " : ": ").append(sidebar ? "<br>" : "");
            html.append("<select name=\"colorby\" onchange=\"").append(change).append("\">");
            html.append("<option value=\"Resource\"").append(colorbyDisplayOp.equalsIgnoreCase("Resource") ? " selected" : "").append(">").append(translationProcessor.translate("Resource")).append("</option>");
            html.append("<option value=\"Status\"").append(colorbyDisplayOp.equalsIgnoreCase("Status") ? " selected" : "").append(">").append(translationProcessor.translate("Status")).append("</option>");
            html.append("<option value=\"Timemode\"").append(colorbyDisplayOp.equalsIgnoreCase("Timemode") ? " selected" : "").append(">").append(translationProcessor.translate("Timemode")).append("</option>");
            html.append("<option value=\"Work SDC\"").append(colorbyDisplayOp.equalsIgnoreCase("Work SDC") ? " selected" : "").append(">").append(translationProcessor.translate("Work SDC")).append("</option>");
            html.append("<option value=\"Due Date\"").append(colorbyDisplayOp.equalsIgnoreCase("Due Date") ? " selected" : "").append(">").append(translationProcessor.translate("Due Date")).append("</option>");
            html.append("<option value=\"Nothing\"").append(colorbyDisplayOp.equalsIgnoreCase("Nothing") ? " selected" : "").append(">").append(translationProcessor.translate("Nothing")).append("</option>");
            html.append("</select>");
            html.append("</div>");
        }
        html.append("<div").append(sidebar ? "" : " style=\"padding:5px;\"").append(">");
        String temp = userConfig.getProperty(userConfigPrefix + "sidebar_appointments");
        boolean appointmentsDisplayOp = temp.length() == 0 ? displayoptions != null && displayoptions.getProperty("appointments", "Y").equalsIgnoreCase("Y") : temp.equalsIgnoreCase("Y");
        html.append(translationProcessor.translate("Show Appointments")).append(" ");
        html.append("<input type=\"checkbox\" name=\"appointments\"").append(appointmentsDisplayOp ? " checked" : "").append(" onclick=\"").append(change).append("\">");
        html.append("</div>");
        html.append("<div").append(sidebar ? "" : " style=\"padding:5px;\"").append(">");
        String workloadDisplayOp = userConfig.getProperty(userConfigPrefix + "sidebar_workload");
        if (workloadDisplayOp.length() == 0) {
            workloadDisplayOp = displayoptions != null ? displayoptions.getProperty("workload", "") : "";
        }
        html.append(translationProcessor.translate("Workload")).append(sidebar ? " " : ": ").append(sidebar ? "<br>" : "");
        html.append("<select name=\"workload\" onchange=\"").append(change).append("\">");
        html.append("<option value=\"None\"").append(workloadDisplayOp.equalsIgnoreCase("None") ? " selected" : "").append(">").append(translationProcessor.translate("None")).append("</option>");
        html.append("<option value=\"Percentage Workload\"").append(workloadDisplayOp.equalsIgnoreCase("Percentage Workload") ? " selected" : "").append(">").append(translationProcessor.translate("Percentage Workload")).append("</option>");
        html.append("<option value=\"Available Time\"").append(workloadDisplayOp.equalsIgnoreCase("Available Time") ? " selected" : "").append(">").append(translationProcessor.translate("Available Time")).append("</option>");
        html.append("</select>");
        html.append("</div>");
        html.append("<div").append(sidebar ? "" : " style=\"padding:5px;\"").append(">");
        html.append(translationProcessor.translate("Activity Label")).append(sidebar ? " " : ": ").append(sidebar ? "<br>" : "");
        html.append("<select name=\"activitylabel\" onchange=\"").append(change).append("\">");
        PropertyListCollection propertyListCollection = labels = displayoptions != null ? displayoptions.getCollection("activitylabels") : null;
        if (labels != null) {
            String selected = userConfig.getProperty(userConfigPrefix + "sidebar_activitylabel");
            if (selected.length() == 0) {
                selected = displayoptions.getProperty("activitylabel");
            }
            if (selected.length() == 0 && labels.size() > 0) {
                selected = labels.getPropertyList(0).getProperty("id");
            }
            for (int i = 0; i < labels.size(); ++i) {
                String labelTitle = labels.getPropertyList(i).getProperty("title");
                String labelid = labels.getPropertyList(i).getProperty("id");
                if (labelTitle.length() == 0) {
                    labelTitle = labelid;
                }
                html.append("<option value=\"").append(labelid).append("\"").append(selected.equalsIgnoreCase(labelid) ? " selected" : "").append(">").append(translationProcessor.translate(labelTitle)).append("</option>");
            }
        }
        html.append("</select>");
        html.append("</div>");
        if (sdcid.equalsIgnoreCase("LV_Activity") && operatingmode == AssignmentPage.OperatingMode.WORK) {
            html.append("<div").append(sidebar ? "" : " style=\"padding:5px;\"").append(">");
            temp = userConfig.getProperty(userConfigPrefix + "sidebar_unassigned");
            boolean unassignedDisplayOp = temp.length() == 0 ? displayoptions != null && displayoptions.getProperty("unassigned", "Y").equalsIgnoreCase("Y") : temp.equalsIgnoreCase("Y");
            html.append(translationProcessor.translate("Show Unassigned")).append(" ");
            html.append("<input type=\"checkbox\" name=\"unassigned\"").append(unassignedDisplayOp ? " checked" : "").append(" onclick=\"").append(change).append("\">");
            html.append("</div>");
        }
        return html.toString();
    }

    public static String getEventColorsKey(String colorbyDisplayOp, AssignmentPageResourceContainer focusedResource, TranslationProcessor translationProcessor) {
        StringBuilder html = new StringBuilder();
        html.append("<div class=\"lv_key_eventcolors lv_type_calendaritem\"></div><div class=\"lv_key_eventcolors_text\">").append(" = ").append(translationProcessor.translate("Calendar Item")).append("</div><br>");
        if (colorbyDisplayOp.equalsIgnoreCase("Status")) {
            html.append("<div class=\"lv_key_eventcolors\"></div><div class=\"lv_key_eventcolors_text\">").append(" = ").append(translationProcessor.translate("Draft")).append("</div><br>");
            html.append("<div class=\"lv_key_eventcolors activity_status_activated\"></div><div class=\"lv_key_eventcolors_text\">").append(" = ").append(translationProcessor.translate("Activated")).append("</div><br>");
            html.append("<div class=\"lv_key_eventcolors activity_status_in_progress\"></div><div class=\"lv_key_eventcolors_text\">").append(" = ").append(translationProcessor.translate("In Progress")).append("</div><br>");
            html.append("<div class=\"lv_key_eventcolors activity_status_completed\"></div><div class=\"lv_key_eventcolors_text\">").append(" = ").append(translationProcessor.translate("Completed")).append("</div><br>");
            html.append("<div class=\"lv_key_eventcolors activity_status_cancelled\"></div><div class=\"lv_key_eventcolors_text\">").append(" = ").append(translationProcessor.translate("Cancelled")).append("</div><br>");
        } else if (colorbyDisplayOp.equalsIgnoreCase("Timemode")) {
            html.append("<div class=\"lv_key_eventcolors\"></div><div class=\"lv_key_eventcolors_text\">").append(" = ").append(translationProcessor.translate("Fixed")).append("</div><br>");
            html.append("<div class=\"lv_key_eventcolors activity_timemode_floating\"></div><div class=\"lv_key_eventcolors_text\">").append(" = ").append(translationProcessor.translate("Floating")).append("</div><br>");
        } else if (colorbyDisplayOp.equalsIgnoreCase("Reservation")) {
            html.append("<div class=\"lv_key_eventcolors\"></div><div class=\"lv_key_eventcolors_text\">").append(" = ").append(translationProcessor.translate("Standard")).append("</div><br>");
            html.append("<div class=\"lv_key_eventcolors activity_reservation\"></div><div class=\"lv_key_eventcolors_text\">").append(" = ").append(translationProcessor.translate("Reservation")).append("</div><br>");
        } else if (colorbyDisplayOp.equalsIgnoreCase("Work SDC")) {
            html.append("<div class=\"lv_key_eventcolors\"></div><div class=\"lv_key_eventcolors_text\">").append(" = ").append(translationProcessor.translate("SDI WorkItem")).append("</div><br>");
            html.append("<div class=\"lv_key_eventcolors activity_worksdc_sample\"></div><div class=\"lv_key_eventcolors_text\">").append(" = ").append(translationProcessor.translate("Sample")).append("</div><br>");
            html.append("<div class=\"lv_key_eventcolors activity_worksdc_dataset\"></div><div class=\"lv_key_eventcolors_text\">").append(" = ").append(translationProcessor.translate("DataSet")).append("</div><br>");
            html.append("<div class=\"lv_key_eventcolors activity_worksdc_workordersdc\"></div><div class=\"lv_key_eventcolors_text\">").append(" = ").append(translationProcessor.translate("WorkOrder")).append("</div><br>");
            html.append("<div class=\"lv_key_eventcolors activity_worksdc_noworksdcid\"></div><div class=\"lv_key_eventcolors_text\">").append(" = ").append(translationProcessor.translate("No Work")).append("</div><br>");
        } else if (colorbyDisplayOp.equalsIgnoreCase("Due Date")) {
            html.append("<div class=\"lv_key_eventcolors activity_duedt_none\"></div><div class=\"lv_key_eventcolors_text\">").append(" = ").append(translationProcessor.translate("No Due Date")).append("</div><br>");
            html.append("<div class=\"lv_key_eventcolors activity_duedt_in\"></div><div class=\"lv_key_eventcolors_text\">").append(" = ").append(translationProcessor.translate("In Due Date")).append("</div><br>");
            html.append("<div class=\"lv_key_eventcolors activity_duedt_out\"></div><div class=\"lv_key_eventcolors_text\">").append(" = ").append(translationProcessor.translate("Outside Due Date")).append("</div><br>");
        } else if (!colorbyDisplayOp.equalsIgnoreCase("Nothing")) {
            if (focusedResource == null || focusedResource.getData().getRowCount() == 0 && focusedResource.getWorkareas().getRowCount() == 0) {
                html.append("<div class=\"lv_key_eventcolors\"></div><div class=\"lv_key_eventcolors_text\">").append(" = ").append(translationProcessor.translate("Resource")).append(" 1").append("</div><br>");
                html.append("<div class=\"lv_key_eventcolors activity_user_1\"></div><div class=\"lv_key_eventcolors_text\">").append(" = ").append(translationProcessor.translate("Resource")).append(" 2").append("</div><br>");
                html.append("<div class=\"lv_key_eventcolors activity_user_2\"></div><div class=\"lv_key_eventcolors_text\">").append(" = ").append(translationProcessor.translate("Resource")).append(" 3").append("</div><br>");
                html.append("<div class=\"lv_key_eventcolors activity_user_3\"></div><div class=\"lv_key_eventcolors_text\">").append(" = ").append(translationProcessor.translate("Resource")).append(" 4").append("</div><br>");
                html.append("<div class=\"lv_key_eventcolors activity_user_4\"></div><div class=\"lv_key_eventcolors_text\">").append(" = ").append(translationProcessor.translate("Resource")).append(" 5").append("</div><br>");
                html.append("<div class=\"lv_key_eventcolors activity_user_5\"></div><div class=\"lv_key_eventcolors_text\">").append(" = ").append(translationProcessor.translate("Resource")).append(" 6").append("</div><br>");
            } else {
                String t;
                String cl;
                int i;
                int c = 0;
                for (i = 0; i < focusedResource.getData().getRowCount(); ++i) {
                    cl = focusedResource.getResourceSDC() == AssignmentPage.ResourceSDC.INSTRUMENT ? (c < 1 ? "" : " activity_instrument_" + c) : (c < 1 ? "" : " activity_user_" + c);
                    t = focusedResource.getResourceSDC() == AssignmentPage.ResourceSDC.INSTRUMENT ? focusedResource.getData().getValue(i, "instrumentid", "") : focusedResource.getData().getValue(i, "sysuserid", "");
                    html.append("<div class=\"lv_key_eventcolors").append(cl).append("\"></div><div class=\"lv_key_eventcolors_text\">").append(" = ").append(t).append("</div><br>");
                    if (c < 6) {
                        ++c;
                        continue;
                    }
                    c = 0;
                }
                if (focusedResource.getWorkareas() != null) {
                    c = 0;
                    for (i = 0; i < focusedResource.getWorkareas().getRowCount(); ++i) {
                        cl = focusedResource.getResourceSDC() == AssignmentPage.ResourceSDC.INSTRUMENT ? " activity_workarea_" + c : " activity_workarea_" + c;
                        t = focusedResource.getWorkareas().getValue(i, "departmentid", "");
                        html.append("<div class=\"lv_key_eventcolors").append(cl).append("\"></div><div class=\"lv_key_eventcolors_text\">").append(" = ").append(t).append("</div><br>");
                        if (c < 4) {
                            ++c;
                            continue;
                        }
                        c = 0;
                    }
                }
            }
        }
        return html.toString();
    }

    public static String getWorkloadColorsKey(TranslationProcessor translationProcessor) {
        StringBuilder html = new StringBuilder();
        html.append("<div class=\"lv_key_workload lv_green\"></div><div class=\"lv_key_workload_text\">").append(" = ").append("0% to 20% ").append(translationProcessor.translate("Workload")).append("</div><br>");
        html.append("<div class=\"lv_key_workload lv_blue\"></div><div class=\"lv_key_workload_text\">").append(" = ").append("21% to 40% ").append(translationProcessor.translate("Workload")).append("</div><br>");
        html.append("<div class=\"lv_key_workload lv_yellow\"></div><div class=\"lv_key_workload_text\">").append(" = ").append("41% to 60% ").append(translationProcessor.translate("Workload")).append("</div><br>");
        html.append("<div class=\"lv_key_workload lv_orange\"></div><div class=\"lv_key_workload_text\">").append(" = ").append("61% to 80% ").append(translationProcessor.translate("Workload")).append("</div><br>");
        html.append("<div class=\"lv_key_workload lv_red\"></div><div class=\"lv_key_workload_text\">").append(" = ").append("81% to 100% ").append(translationProcessor.translate("Workload")).append("</div><br>");
        html.append("<div class=\"lv_key_workload lv_exception\"></div><div class=\"lv_key_workload_text\">").append(" = ").append(translationProcessor.translate("Above")).append(" 100% ").append(translationProcessor.translate("Workload")).append("</div><br>");
        return html.toString();
    }

    public static String getEventIconsKey(TranslationProcessor translationProcessor) {
        StringBuilder html = new StringBuilder();
        html.append("<div class=\"lv_key_icons warnunassigned\"></div><div class=\"lv_key_icons_text\">").append(" = ").append(translationProcessor.translate("Unassigned Resources")).append("</div><br>");
        html.append("<div class=\"lv_key_icons okduedate\"></div><div class=\"lv_key_icons_text\">").append(" = ").append(translationProcessor.translate("In Due Date")).append("</div><br>");
        html.append("<div class=\"lv_key_icons warnduedate\"></div><div class=\"lv_key_icons_text\">").append(" = ").append(translationProcessor.translate("Outside Due Date")).append("</div><br>");
        html.append("<div class=\"lv_key_icons draft\"></div><div class=\"lv_key_icons_text\">").append(" = ").append(translationProcessor.translate("Draft Status")).append("</div><br>");
        html.append("<div class=\"lv_key_icons activated\"></div><div class=\"lv_key_icons_text\">").append(" = ").append(translationProcessor.translate("Activated Status")).append("</div><br>");
        html.append("<div class=\"lv_key_icons inprogress\"></div><div class=\"lv_key_icons_text\">").append(" = ").append(translationProcessor.translate("In Progress Status")).append("</div><br>");
        html.append("<div class=\"lv_key_icons completed\"></div><div class=\"lv_key_icons_text\">").append(" = ").append(translationProcessor.translate("Completed Status")).append("</div><br>");
        html.append("<div class=\"lv_key_icons cancelled\"></div><div class=\"lv_key_icons_text\">").append(" = ").append(translationProcessor.translate("Cancelled Status")).append("</div><br>");
        html.append("<div class=\"lv_key_icons workcount\"></div><div class=\"lv_key_icons_text\">").append(" = ").append(translationProcessor.translate("Has Work Assigned")).append("</div><br>");
        return html.toString();
    }

    public static int getPercentAvailability(int availmin, int workingmin) {
        int percent = availmin > 0 ? (workingmin > 0 ? (availmin == workingmin ? 0 : (int)Math.round((double)(workingmin - availmin) / (double)workingmin * 100.0)) : 0) : (workingmin > 0 ? (int)Math.round((double)(workingmin - availmin) / (double)workingmin * 100.0) : 0);
        return percent;
    }

    private static int getPercentAvailability(List<WAPAvailability> availability) {
        int availmin = 0;
        int workingmin = 0;
        if (availability != null && availability.size() > 0) {
            for (WAPAvailability avail : availability) {
                availmin += avail.getAvailableMinutes();
                workingmin += avail.getWorkingMinutes();
            }
        }
        return AssignmentPageUtil.getPercentAvailability(availmin, workingmin);
    }

    private static int getAndCheckSelectedResourceCount(JSONObject selection, AssignmentPageResourceData resourceData) {
        int selectedresources = 0;
        if (selection != null && resourceData != null && resourceData.getResources().size() > 0) {
            for (int i = 0; i < resourceData.getResources().size(); ++i) {
                try {
                    AssignmentPageResourceContainer resource = resourceData.getResources().get(i);
                    if (!selection.has(resource.getId()) || selection.getJSONArray(resource.getId()).length() <= 0) continue;
                    JSONArray current = selection.getJSONArray(resource.getId());
                    if (current.length() > selectedresources && selectedresources == 0) {
                        selectedresources = current.length();
                        continue;
                    }
                    if (current.length() == selectedresources || current.length() <= 1 || selectedresources <= 1) continue;
                    selectedresources = -1;
                    break;
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }
        return selectedresources;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static String assignWork(String sdcid, String[] selectedsdis, String departmentid, String shiftid, JSONObject selection, JSONObject resourceassignment, String timemode, CalendarPage.View dateRange, ZonedDateTime[] selecteddates, int fixedmins, String activitydesc, boolean autoactivate, ActivityClassHandler activityClass, AssignmentPageResourceData resourceData, ZoneId displayTimeZone, SDIProcessor sdiP, TranslationProcessor tp, ActionProcessor ap, M18NUtil m18) throws SapphireException {
        boolean isActivitySelection = sdcid.equalsIgnoreCase("LV_Activity");
        boolean isWorkAssignment = false;
        try {
            isWorkAssignment = selection != null && selection.has("activity") && selection.getJSONArray("activity").length() > 0;
        }
        catch (Exception exception) {
            // empty catch block
        }
        StringBuilder activityid = new StringBuilder();
        if (selectedsdis.length <= 0) throw new SapphireException(tp.translate("No items selected to assign."));
        if (isWorkAssignment && isActivitySelection) {
            throw new SapphireException(tp.translate("You can not assign an activity as work to an existing activity."));
        }
        if (isWorkAssignment) {
            PropertyList props = new PropertyList();
            StringBuilder ac = new StringBuilder();
            try {
                JSONArray acts = selection.getJSONArray("activity");
                for (int i = 0; i < acts.length(); ++i) {
                    if (ac.length() > 0) {
                        ac.append(";");
                    }
                    ac.append(acts.getString(i));
                }
            }
            catch (Exception acts) {
                // empty catch block
            }
            if (ac.length() <= 0) throw new SapphireException(tp.translate("No activities to assign work to."));
            props.setProperty("activityid", ac.toString());
            props.setProperty("worksdcid", sdcid);
            props.setProperty("workkeyid1", StringUtil.arrayToString(selectedsdis, ";"));
            try {
                ap.processActionClass(AddActivityWorkSDI.class.getName(), props);
                return activityid.toString();
            }
            catch (SapphireException e) {
                throw new SapphireException(e.getMessage(), e);
            }
        }
        int selectedresources = AssignmentPageUtil.getAndCheckSelectedResourceCount(selection, resourceData);
        if (isActivitySelection && selectedresources == 0) {
            throw new SapphireException(tp.translate("Cannot assign as no resources selected"));
        }
        if (selectedresources < 0) {
            throw new SapphireException(tp.translate("Cannot assign as resources selected do not match. Need to be either the same number of selected or 1."));
        }
        if (isActivitySelection && selectedresources != selectedsdis.length && selectedresources != 1) {
            throw new SapphireException(tp.translate("Cannot assign as resources selected do not match the number of activities selected. Need to be either the same number of selected or 1."));
        }
        if (selectedresources < 0 || selection == null) throw new SapphireException(tp.translate("No resources selected to assign work to."));
        boolean cont = false;
        PropertyList props = new PropertyList();
        if (isActivitySelection) {
            props.setProperty("activityid", StringUtil.arrayToString(selectedsdis, ";"));
            StringBuilder analystids = new StringBuilder();
            StringBuilder instrumentids = new StringBuilder();
            StringBuilder analystworkareaids = new StringBuilder();
            StringBuilder instrumentworkareaids = new StringBuilder();
            StringBuilder instrumenttype = new StringBuilder();
            StringBuilder analysttype = new StringBuilder();
            StringBuilder instrumentmodel = new StringBuilder();
            for (int i = 0; i < selectedsdis.length; ++i) {
                for (int r = 0; r < resourceData.getResources().size(); ++r) {
                    try {
                        String imodel;
                        String itype;
                        String atype;
                        AssignmentPageResourceContainer resource = resourceData.getResources().get(r);
                        String selsdcid = selection.has(resource.getId() + "_sdcid") && selection.getString(resource.getId() + "_sdcid").length() > 0 ? selection.getString(resource.getId() + "_sdcid") : resource.getResourceSDC().getName();
                        if (!selection.has(resource.getId()) || selection.getJSONArray(resource.getId()).length() <= 0) continue;
                        JSONArray current = selection.getJSONArray(resource.getId());
                        if (selsdcid.equalsIgnoreCase(AssignmentPage.ResourceSDC.USER.getName()) && resource.getResourceSDC() == AssignmentPage.ResourceSDC.USER) {
                            if (analystids.length() > 0) {
                                analystids.append(";");
                            }
                            if (selectedresources > 1 && current.length() > i) {
                                analystids.append(current.get(i));
                            } else {
                                analystids.append(current.get(0));
                            }
                            atype = resource.getType();
                            if (atype.length() <= 0) continue;
                            if (analysttype.length() > 0) {
                                analysttype.append(";");
                            }
                            analysttype.append(atype);
                            continue;
                        }
                        if (selsdcid.equalsIgnoreCase(AssignmentPage.ResourceSDC.INSTRUMENT.getName()) && resource.getResourceSDC() == AssignmentPage.ResourceSDC.INSTRUMENT) {
                            if (instrumentids.length() > 0) {
                                instrumentids.append(";");
                            }
                            if (selectedresources > 1 && current.length() > i) {
                                instrumentids.append(current.get(i));
                            } else {
                                instrumentids.append(current.get(0));
                            }
                            itype = resource.getType();
                            if (itype.length() > 0) {
                                if (instrumenttype.length() > 0) {
                                    instrumenttype.append(";");
                                }
                                instrumenttype.append(itype);
                            }
                            if ((imodel = resource.getModel()).length() <= 0) continue;
                            if (instrumentmodel.length() > 0) {
                                instrumentmodel.append(";");
                            }
                            instrumentmodel.append(imodel);
                            continue;
                        }
                        if (!selsdcid.equalsIgnoreCase("Department")) continue;
                        if (resource.getResourceSDC() == AssignmentPage.ResourceSDC.INSTRUMENT) {
                            if (instrumentworkareaids.length() > 0) {
                                instrumentworkareaids.append(";");
                            }
                            if (selectedresources > 1 && current.length() > i) {
                                instrumentworkareaids.append(current.get(i));
                            } else {
                                instrumentworkareaids.append(current.get(0));
                            }
                            itype = resource.getType();
                            if (itype.length() > 0) {
                                if (instrumenttype.length() > 0) {
                                    instrumenttype.append(";");
                                }
                                instrumenttype.append(itype);
                            }
                            if ((imodel = resource.getModel()).length() <= 0) continue;
                            if (instrumentmodel.length() > 0) {
                                instrumentmodel.append(";");
                            }
                            instrumentmodel.append(imodel);
                            continue;
                        }
                        if (analystworkareaids.length() > 0) {
                            analystworkareaids.append(";");
                        }
                        if (selectedresources > 1 && current.length() > i) {
                            analystworkareaids.append(current.get(i));
                        } else {
                            analystworkareaids.append(current.get(0));
                        }
                        atype = resource.getType();
                        if (atype.length() <= 0) continue;
                        if (analysttype.length() > 0) {
                            analysttype.append(";");
                        }
                        analysttype.append(atype);
                        continue;
                    }
                    catch (Exception resource) {
                        // empty catch block
                    }
                }
            }
            if (analystids.length() > 0) {
                props.setProperty("analystid", analystids.toString());
            }
            if (analystworkareaids.length() > 0) {
                props.setProperty("analystworkareaid", analystworkareaids.toString());
            }
            if (instrumentids.length() > 0) {
                props.setProperty("instrumentid", instrumentids.toString());
            }
            if (instrumentworkareaids.length() > 0) {
                props.setProperty("instrumentworkareaid", instrumentworkareaids.toString());
            }
            if (instrumentmodel.length() > 0) {
                props.setProperty("instrumentmodelid", instrumentmodel.toString());
            }
            if (instrumenttype.length() > 0) {
                props.setProperty("instrumenttypeid", instrumenttype.toString());
            }
            if (analysttype.length() > 0) {
                props.setProperty("analysttype", analysttype.toString());
            }
            ap.processActionClass(SetActivityResource.class.getName(), props);
            return activityid.toString();
        } else {
            DateFormat sdf;
            SimpleDateFormat test;
            if (m18 == null) {
                ConnectionProcessor cp = new ConnectionProcessor(ap.getConnectionid());
                m18 = new M18NUtil(cp.getConnectionInfo(ap.getConnectionid()));
            }
            if (!((test = (SimpleDateFormat)(sdf = m18.getDefaultDateFormat())).toPattern().contains("mm") || test.toPattern().contains("H") || test.toPattern().contains("h"))) {
                Logger.logError("Date Format from policy has no time element. Thus ranges would fail.");
                throw new SapphireException("Date Format does not contain time element and thus assignment cannot continue.");
            }
            ArrayList<String[]> worklists = new ArrayList<String[]>();
            if (selectedresources > 1) {
                if (selectedresources > selectedsdis.length) {
                    throw new SapphireException(tp.translate("Cannot assign as resources selected do not match the amount of work selected.") + tp.translate("You need to select") + " " + selectedresources + " " + tp.translate("or more Work SDI's") + ".");
                }
                int totalwork = selectedsdis.length;
                if (totalwork >= selectedresources) {
                    int workperresource = totalwork / selectedresources;
                    int extra = totalwork % selectedresources;
                    int c = 0;
                    for (int k = 0; k < selectedresources; ++k) {
                        String[] curr = extra > 0 ? new String[workperresource + extra] : new String[workperresource];
                        int w = c + workperresource + extra;
                        int a = 0;
                        for (int t = c; t < w; ++t) {
                            curr[a] = selectedsdis[t];
                            ++a;
                            ++c;
                        }
                        if (extra > 0) {
                            extra = 0;
                        }
                        worklists.add(curr);
                    }
                } else {
                    for (String selectedsdi : selectedsdis) {
                        worklists.add(new String[]{selectedsdi});
                    }
                }
            } else {
                worklists.add(selectedsdis);
            }
            if (selectedresources > 0) {
                for (int i = 0; i < selectedresources; ++i) {
                    String[] work;
                    String[] stringArray = work = worklists.size() > i ? (String[])worklists.get(i) : null;
                    if (work != null) {
                        if (selecteddates.length > 0) {
                            StringBuilder resourcenum = new StringBuilder();
                            StringBuilder resourceid = new StringBuilder();
                            StringBuilder resourcesdc = new StringBuilder();
                            for (int r = 0; r < resourceData.getResources().size(); ++r) {
                                try {
                                    String selsdcid;
                                    AssignmentPageResourceContainer resource = resourceData.getResources().get(r);
                                    selsdcid = selection.has(resource.getId() + "_sdcid") && selection.getString(resource.getId() + "_sdcid").length() > 0 ? ((selsdcid = selection.getString(resource.getId() + "_sdcid")).equalsIgnoreCase("Department") ? "Department" : resource.getResourceSDC().getName()) : resource.getResourceSDC().getName();
                                    if (!selection.has(resource.getId()) || selection.getJSONArray(resource.getId()).length() <= 0) continue;
                                    JSONArray current = selection.getJSONArray(resource.getId());
                                    if (current.length() == selectedresources) {
                                        if (resourcenum.length() > 0) {
                                            resourcenum.append(";");
                                            resourceid.append(";");
                                            resourcesdc.append(";");
                                        }
                                        resourcenum.append(resource.getNum());
                                        resourceid.append(current.get(i));
                                        resourcesdc.append(selsdcid);
                                        continue;
                                    }
                                    if (current.length() != 1) continue;
                                    if (resourcenum.length() > 0) {
                                        resourcenum.append(";");
                                        resourceid.append(";");
                                        resourcesdc.append(";");
                                    }
                                    resourcenum.append(resource.getNum());
                                    resourceid.append(current.get(0));
                                    resourcesdc.append(selsdcid);
                                    continue;
                                }
                                catch (Exception exception) {
                                    // empty catch block
                                }
                            }
                            props.setProperty("resourcenum", resourcenum.toString());
                            props.setProperty("resourcekeyid1", resourceid.toString());
                            props.setProperty("resourcesdcid", resourcesdc.toString());
                            props.setProperty("label", activitydesc != null ? activitydesc : "");
                            props.setProperty("worksdcid", sdcid);
                            props.setProperty("workkeyid1", StringUtil.arrayToString(work, ";"));
                            if (activityClass != null) {
                                props.setProperty("activityclass", activityClass.getId());
                            }
                            if (AssignmentPageUtil.getAssignmentDates(timemode, selecteddates, dateRange, displayTimeZone, fixedmins, props, tp, m18)) {
                                cont = true;
                            }
                        } else {
                            new SapphireException(tp.translate("No dates chosen."));
                        }
                    }
                    if (!cont) continue;
                    try {
                        if (autoactivate) {
                            props.setProperty("autoactivate", "Y");
                        }
                        ap.processActionClass(CreateActivity.class.getName(), props);
                        activityid.append(activityid.length() > 0 ? ";" + props.getProperty("activityid") : props.getProperty("activityid"));
                        continue;
                    }
                    catch (SapphireException e) {
                        throw new SapphireException(e.getMessage(), e);
                    }
                }
                return activityid.toString();
            } else {
                String[] work = (String[])worklists.get(0);
                if (work == null) return activityid.toString();
                props.setProperty("label", activitydesc != null ? activitydesc : "");
                props.setProperty("worksdcid", sdcid);
                props.setProperty("workkeyid1", StringUtil.arrayToString(work, ";"));
                if (activityClass != null) {
                    props.setProperty("activityclass", activityClass.getId());
                }
                if (AssignmentPageUtil.getAssignmentDates(timemode, selecteddates, dateRange, displayTimeZone, fixedmins, props, tp, m18)) {
                    cont = true;
                }
                if (!cont) return activityid.toString();
                try {
                    if (autoactivate) {
                        props.setProperty("autoactivate", "Y");
                    }
                    ap.processActionClass(CreateActivity.class.getName(), props);
                    activityid.append(activityid.length() > 0 ? ";" + props.getProperty("activityid") : props.getProperty("activityid"));
                    return activityid.toString();
                }
                catch (SapphireException e) {
                    throw new SapphireException(e.getMessage(), e);
                }
            }
        }
    }

    private static boolean getAssignmentDates(String timemode, ZonedDateTime[] selectedDates, CalendarPage.View dateRange, ZoneId displayTimeZone, int fixedmins, PropertyList props, TranslationProcessor tp, M18NUtil m18) throws SapphireException {
        ZonedDateTime endZoned;
        boolean cont = false;
        CalendarConverter calendarConverter = new CalendarConverter(new DateTimeUtil(m18.getTimezone(), m18.getLocale()));
        ZonedDateTime startZoned = selectedDates[0];
        ZonedDateTime zonedDateTime = endZoned = selectedDates.length == 2 ? selectedDates[1] : null;
        if (timemode.equalsIgnoreCase("Fixed")) {
            try {
                if (selectedDates.length == 1 && dateRange != CalendarPage.View.DAY) {
                    startZoned = dateRange == CalendarPage.View.WEEK ? startZoned.with(WeekFields.of(Locale.US).dayOfWeek(), 1L) : startZoned.withDayOfMonth(1);
                }
                if (fixedmins > 0) {
                    startZoned = startZoned.truncatedTo(ChronoUnit.DAYS).plus(fixedmins, ChronoUnit.MINUTES);
                }
                props.setProperty("timemode", "Fixed");
                props.setProperty("startdt", calendarConverter.convertInstantUtcToUserActionDateString(startZoned.toInstant()));
                cont = true;
            }
            catch (Exception e) {
                throw new SapphireException(e.getMessage(), e);
            }
        } else if (dateRange == CalendarPage.View.DAY && selectedDates.length == 2) {
            try {
                props.setProperty("timemode", "Floating");
                props.setProperty("startrangedt", calendarConverter.convertInstantUtcToUserActionDateString(startZoned.toInstant()));
                props.setProperty("endrangedt", calendarConverter.convertInstantUtcToUserActionDateString(endZoned.toInstant()));
                cont = true;
            }
            catch (Exception e) {
                throw new SapphireException(e.getMessage(), e);
            }
        } else if (selectedDates.length == 1) {
            if (dateRange == CalendarPage.View.DAY) {
                startZoned = startZoned.truncatedTo(ChronoUnit.DAYS);
                endZoned = startZoned.plus(1L, ChronoUnit.DAYS);
                props.setProperty("startrangedt", calendarConverter.convertInstantUtcToUserActionDateString(startZoned.toInstant()));
                props.setProperty("endrangedt", calendarConverter.convertInstantUtcToUserActionDateString(endZoned.toInstant()));
                cont = true;
            } else if (dateRange == CalendarPage.View.WEEK) {
                startZoned = startZoned.with(WeekFields.of(Locale.US).dayOfWeek(), 1L);
                endZoned = startZoned.plus(7L, ChronoUnit.DAYS);
                props.setProperty("startrangedt", calendarConverter.convertInstantUtcToUserActionDateString(startZoned.toInstant()));
                props.setProperty("endrangedt", calendarConverter.convertInstantUtcToUserActionDateString(endZoned.toInstant()));
                cont = true;
            } else {
                startZoned = startZoned.withDayOfMonth(1);
                endZoned = startZoned.plus(1L, ChronoUnit.MONTHS);
                props.setProperty("startrangedt", calendarConverter.convertInstantUtcToUserActionDateString(startZoned.toInstant()));
                props.setProperty("endrangedt", calendarConverter.convertInstantUtcToUserActionDateString(endZoned.toInstant()));
                cont = true;
            }
            props.setProperty("timemode", "Floating");
        } else if (selectedDates.length == 2) {
            props.setProperty("timemode", "Floating");
            props.setProperty("startrangedt", calendarConverter.convertInstantUtcToUserActionDateString(startZoned.toInstant()));
            props.setProperty("endrangedt", calendarConverter.convertInstantUtcToUserActionDateString(endZoned.toInstant()));
            cont = true;
        } else {
            throw new SapphireException(tp.translate("Invalid dates chosen."));
        }
        return cont;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static void toggleTimemode(String activityId, String connectionId) throws SapphireException {
        WAPCommands wapCommands = new WAPCommands(connectionId);
        Activity activity = wapCommands.getActivityDetails(activityId);
        if (activity == null) throw new SapphireException("Unable to obtain activity details.");
        if (activity.getTimeMode().equalsIgnoreCase("Fixed")) {
            throw new SapphireException("You can only toggle floating activitites.");
        }
        if (!activity.getStatus().equalsIgnoreCase("Draft") && !activity.getStatus().equalsIgnoreCase("Activated")) throw new SapphireException("Cannot toggle fixed times for activities which are in-progress, cancelled or completed.");
        Activity editActivity = new Activity();
        editActivity.setActivityid(activityId);
        editActivity.setStatus("");
        if (activity.getStartDateInstantUTC() != null) {
            editActivity.setStartRangeInstantUTC(activity.getStartDateInstantUTC());
            editActivity.setEndRangeInstantUTC(activity.getEndRangeInstantUTC());
            editActivity.setStartDateInstantUTC(null);
            editActivity.setEndDateInstantUTC(null);
            editActivity.setIsEndDateFixed(false);
            wapCommands.editActivity(editActivity, true);
            return;
        } else {
            if (activity.getStartRangeInstantUTC() == null) throw new SapphireException("Cannot toggle fixed times.");
            DataSet resources = wapCommands.getActivityResources(activityId);
            String userid = "";
            for (int i = 0; i < resources.getRowCount(); ++i) {
                if (!resources.getValue(i, "resourcetypeflag", "").equalsIgnoreCase("A") || resources.getValue(i, "analystid", "").length() <= 0) continue;
                userid = resources.getValue(i, "analystid", "");
                break;
            }
            if (userid.length() > 0) {
                CalendarFactory calendarFactory = new CalendarFactory(connectionId);
                LVCalendar cal = calendarFactory.getUserCalendar(userid, true, true);
                WorkHours workHours = cal.getCoreHours();
                if (workHours != null) {
                    ZonedDateTime rangeStart = activity.getStartRangeInstantUTC().atZone(cal.getTimeZone() != null ? cal.getTimeZone() : ZoneId.systemDefault());
                    if (workHours.isWorkingDayOfWeek(rangeStart.getDayOfWeek())) {
                        editActivity.setStartDateInstantUTC(activity.getStartRangeInstantUTC());
                    } else {
                        editActivity.setStartDateInstantUTC(workHours.getNextWorkdayStart(activity.getStartRangeInstantUTC()).toInstant());
                    }
                } else {
                    editActivity.setStartDateInstantUTC(activity.getStartRangeInstantUTC());
                }
            } else {
                editActivity.setStartDateInstantUTC(activity.getStartRangeInstantUTC());
            }
            wapCommands.editActivity(editActivity, true);
        }
    }

    public static String moveWork(String activityid, String timemode, Instant fromDate, Instant toDate, TimeZone displayTimeZone, ActionProcessor ap, TranslationProcessor tp, M18NUtil m18) throws SapphireException {
        return AssignmentPageUtil.moveWork(activityid, timemode, fromDate, toDate, null, null, null, "", displayTimeZone, ap, tp, m18);
    }

    public static String moveWork(String activityid, String timemode, Instant newStartDate, Instant newEndDate, AssignmentPageResourceData resourceData, AssignmentPageResourceContainer focusedResource, String newResourceId, String newResourceSDC, TimeZone displayTimeZone, ActionProcessor ap, TranslationProcessor tp, M18NUtil m18) throws SapphireException {
        Instant duedt;
        String outmsg = "";
        boolean checkDueDate = true;
        WAPCommands wapCommands = new WAPCommands(ap.getConnectionid());
        Activity activity = wapCommands.getActivityDetails(activityid);
        if (newStartDate != null && newEndDate != null) {
            if (newStartDate.compareTo(newEndDate) > 0) {
                Instant temp = newStartDate;
                newStartDate = newEndDate;
                newEndDate = temp;
            }
            Activity editActivity = new Activity();
            editActivity.setActivityid(activity.getActivityid());
            editActivity.setTimeMode(activity.getTimeMode());
            if (activity.getStartDateInstantUTC() != null) {
                editActivity.setStartDateInstantUTC(newStartDate);
                editActivity.setEndDateInstantUTC(newEndDate);
            } else {
                int daysBetween = (int)ChronoUnit.DAYS.between(newStartDate, newEndDate);
                if (daysBetween > 0) {
                    editActivity.setStartRangeInstantUTC(newStartDate);
                    editActivity.setEndRangeInstantUTC(newEndDate);
                    if (newStartDate.compareTo(activity.getStartRangeInstantUTC()) != 0) {
                        int hours = newStartDate.atZone(ZoneOffset.UTC).toLocalTime().getHour();
                        newStartDate = newStartDate.truncatedTo(ChronoUnit.DAYS);
                        if (hours > 20) {
                            newStartDate = newStartDate.plus(1L, ChronoUnit.DAYS);
                        }
                        int duration = (int)ChronoUnit.DAYS.between(activity.getStartRangeInstantUTC(), activity.getEndRangeInstantUTC());
                        editActivity.setStartRangeInstantUTC(newStartDate);
                        editActivity.setEndRangeInstantUTC(newStartDate.plus((long)duration, ChronoUnit.SECONDS));
                    } else {
                        editActivity.setStartRangeInstantUTC(activity.getStartRangeInstantUTC());
                        int hours = newEndDate.atZone(ZoneOffset.UTC).toLocalTime().getHour();
                        newEndDate = newEndDate.truncatedTo(ChronoUnit.DAYS).plus(1L, ChronoUnit.DAYS);
                        if (hours < 4) {
                            newEndDate.minus(1L, ChronoUnit.DAYS);
                        }
                        editActivity.setStartRangeInstantUTC(newStartDate);
                        editActivity.setEndRangeInstantUTC(newEndDate);
                    }
                } else {
                    editActivity.setStartRangeInstantUTC(newStartDate);
                    editActivity.setEndRangeInstantUTC(newEndDate);
                }
            }
            wapCommands.editActivity(editActivity, true);
            if (activity.getWorkDuedt() == null) {
                checkDueDate = false;
            }
        }
        if (resourceData != null && focusedResource != null && newResourceId != null && newResourceId.length() > 0) {
            PropertyList resourceprops = new PropertyList();
            resourceprops.setProperty("activityid", activityid);
            if (newResourceSDC.length() == 0) {
                newResourceSDC = focusedResource.getResourceSDC().getName();
            }
            if (focusedResource.getResourceSDC() == AssignmentPage.ResourceSDC.USER) {
                if (newResourceSDC.equalsIgnoreCase(AssignmentPage.ResourceSDC.USER.getName())) {
                    resourceprops.setProperty("analystid", newResourceId);
                } else {
                    resourceprops.setProperty("analystworkareaid", newResourceId);
                }
                if (focusedResource.getType().length() > 0) {
                    resourceprops.setProperty("analysttype", focusedResource.getType());
                }
            } else if (focusedResource.getResourceSDC() == AssignmentPage.ResourceSDC.INSTRUMENT) {
                if (newResourceSDC.equalsIgnoreCase(AssignmentPage.ResourceSDC.INSTRUMENT.getName())) {
                    resourceprops.setProperty("instrumentid", newResourceId);
                } else {
                    resourceprops.setProperty("instrumentworkareaid", newResourceId);
                }
                if (focusedResource.getModel().length() > 0) {
                    resourceprops.setProperty("instrumentmodelid", focusedResource.getModel());
                }
                if (focusedResource.getType().length() > 0) {
                    resourceprops.setProperty("instrumenttypeid", focusedResource.getType());
                }
            }
            try {
                ap.processActionClass(SetActivityResource.class.getName(), resourceprops);
            }
            catch (SapphireException e) {
                throw new SapphireException(e.getMessage(), e);
            }
        }
        if (checkDueDate && (duedt = (activity = wapCommands.getActivityDetails(activityid)).getWorkDuedt()) != null) {
            Instant enddt;
            Instant instant = enddt = activity.getEndDateInstantUTC() != null ? activity.getEndDateInstantUTC() : activity.getEndRangeInstantUTC();
            if (enddt != null && enddt.compareTo(duedt) > 0) {
                outmsg = tp.translate("Due date of " + m18.getDateTimeFormatter(displayTimeZone.toZoneId()).format(duedt) + " is before end date of activity.");
            }
        }
        return outmsg;
    }

    public static String getOperationsMenu(String activityId, ActivityClassHandler activityClass, PropertyList wapPolicy, TranslationProcessor tp, String connectionId) {
        StringBuilder html = new StringBuilder();
        if (activityId.length() > 0 && activityClass != null && wapPolicy != null) {
            WAPCommands wapCommands = new WAPCommands(connectionId);
            Activity activity = null;
            try {
                activity = wapCommands.getActivityDetails(activityId);
            }
            catch (Exception exception) {
                // empty catch block
            }
            if (activity != null) {
                DataSet work = wapCommands.getActivityWorkSDIs(activityId);
                DataSet worksdis = work != null ? activityClass.getWork(work.getColumnValues("workkeyid1", ";"), activity.getTestingDepartmentid()) : null;
                html.append("<ul class=\"menu\">");
                if (activityClass != null && activityClass.getOperations().size() > 0) {
                    for (int i = 0; i < activityClass.getOperations().size(); ++i) {
                        PropertyList operation = activityClass.getOperations().getPropertyList(i);
                        String label = operation.getProperty("label");
                        String url = operation.getProperty("url");
                        boolean show = operation.getProperty("show", "Y").equalsIgnoreCase("Y");
                        if (!show || label.length() <= 0 || url.length() <= 0) continue;
                        url = activityClass.replaceTokensFromActivity(url, activity, true);
                        if (work != null) {
                            url = activityClass.replaceTokensFromActivityWorkSDI(url, work, true);
                            if (worksdis != null) {
                                url = activityClass.replaceTokensFromActivityWorkSDI(url, worksdis, false);
                                url = activityClass.replaceTokensFromWorkSDI(url, worksdis);
                            }
                        }
                        String target = operation.getProperty("target", "");
                        url = !url.toLowerCase().startsWith("javascript:") ? "sapphire.page.navigate('" + StringUtil.replaceAll(url.replaceAll("[\\p{Cf}]", ""), "'", "\\'") + "'" + (target.length() > 0 ? ",'N','" + target + "'" : "") + ")" : url.substring(11);
                        html.append("<li><div onclick=\"").append(url).append("\">").append(tp.translate(label)).append("</div></li>");
                    }
                } else {
                    html.append("<li><div onclick=\"sapphire.navigate('rc?command=page&page=LV_ActivityMaint&sdcid=LV_Activity&mode=Edit&keyid1=").append(activityId).append("')\">").append(tp.translate("Edit Activity")).append("</div></li>");
                }
                html.append("</ul>");
            }
        }
        return html.toString();
    }
}

