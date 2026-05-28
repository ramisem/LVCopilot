/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.modules.wap.activity;

import com.labvantage.sapphire.I18nUtil;
import com.labvantage.sapphire.modules.wap.activity.Activity;
import com.labvantage.sapphire.modules.wap.activity.ActivityClassHandler;
import com.labvantage.sapphire.modules.wap.activity.AssignmentPage;
import com.labvantage.sapphire.modules.wap.activity.AssignmentPageResourceContainer;
import com.labvantage.sapphire.modules.wap.activity.AssignmentPageResourceData;
import com.labvantage.sapphire.modules.wap.activity.AssignmentPageUtil;
import com.labvantage.sapphire.modules.wap.activity.BaseActivityElement;
import com.labvantage.sapphire.modules.wap.activity.WAPCommands;
import com.labvantage.sapphire.pageelements.controls.Button;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.TimeZone;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.accessor.SDIProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SafeHTML;
import sapphire.xml.PropertyList;

public class ActivityResources
extends BaseActivityElement {
    public static String getToolbar(PageContext pageContext, boolean canAdd, boolean canRemove, AssignmentPageResourceData resourceData, TranslationProcessor translationProcessor) {
        StringBuilder out = new StringBuilder();
        boolean b = false;
        int allocated = 0;
        if (canAdd && resourceData != null && resourceData.getResourceRequirements() != null && resourceData.getResourceRequirements().size() > 0) {
            for (int i = 0; i < resourceData.getResourceRequirements().getRowCount(); ++i) {
                boolean isAssigned;
                AssignmentPage.ResourceSDC resourceSDC = resourceData.getResourceRequirements().getValue(i, "resourcetypeflag", "A").equalsIgnoreCase("I") ? AssignmentPage.ResourceSDC.INSTRUMENT : AssignmentPage.ResourceSDC.USER;
                boolean bl = isAssigned = resourceData.getResourceRequirements().getValue(i, resourceSDC == AssignmentPage.ResourceSDC.USER ? "analystid" : "instrumentid", "").length() > 0 || resourceData.getResourceRequirements().getValue(i, "workareadepartmentid", "").length() > 0;
                if (!isAssigned) {
                    String label;
                    String type;
                    if (resourceSDC == AssignmentPage.ResourceSDC.INSTRUMENT) {
                        type = resourceData.getResourceRequirements().getValue(i, "instrumenttypeid", "");
                        String model = resourceData.getResourceRequirements().getValue(i, "instrumenttypeid", "");
                        label = type.length() > 0 ? type : (model.length() > 0 ? model : resourceSDC.singulartext + " " + resourceData.getResourceRequirements().getValue(i, "resourcenum", ""));
                    } else {
                        type = resourceData.getResourceRequirements().getValue(i, "analysttype", "");
                        label = type.length() > 0 ? type : resourceSDC.singulartext + " " + resourceData.getResourceRequirements().getValue(i, "resourcenum", "");
                    }
                    if (b) {
                        out.append("&nbsp;");
                    }
                    Button add = new Button(pageContext);
                    add.setId("btRemove");
                    add.setText(SafeHTML.encodeForHTML(translationProcessor.translate("Add " + label)));
                    add.setTip(translationProcessor.translate("Add " + label + " resource"));
                    add.setImg("rc?command=image&image=FlatBlackAdd");
                    add.setAction("activityResources.add('" + (Object)((Object)resourceSDC) + "'," + resourceData.getResourceRequirements().getValue(i, "resourcenum", "0") + ")");
                    out.append(add.getHtml());
                    b = true;
                    continue;
                }
                ++allocated;
            }
        }
        if (canRemove && resourceData != null && resourceData.getResources() != null && resourceData.getResources().size() > 0 && allocated > 0) {
            if (b) {
                out.append("&nbsp;");
            }
            Button remove = new Button(pageContext);
            remove.setId("btRemove");
            remove.setText(translationProcessor.translate("Remove"));
            remove.setTip(translationProcessor.translate("Remove selected resource"));
            remove.setImg("rc?command=image&image=FlatBlackDelete");
            remove.setAction("activityResources.remove()");
            out.append(remove.getHtml());
        }
        return out.toString();
    }

    public static void renderHtml(StringBuilder html, String activityid, String elementid, boolean fullRender, boolean canAdd, boolean canRemove, WAPCommands wapCommands, ZoneId displayTimeZone, String connectionId, TranslationProcessor tp, SDIProcessor sdiProcessor, Logger logger, PageContext pageContext) throws SapphireException {
        DataSet resources = wapCommands.getActivityResources(activityid);
        if (resources != null) {
            String eid;
            Activity activity = wapCommands.getActivityDetails(activityid);
            String string = eid = elementid != null && elementid.length() > 0 ? elementid : "activityresources";
            if (fullRender) {
                html.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"WEB-CORE/modules/wap/style/activityresources.css\">");
                html.append("<script src=\"WEB-CORE/modules/wap/scripts/activityresources.js\" type=\"text/javascript\"></script>");
                html.append("<div id=\"").append(eid).append("\">");
            }
            ActivityClassHandler activityClassHandler = new ActivityClassHandler(connectionId, "_dummy", "Activity", "LV_Activity");
            AssignmentPage.AssignmentPageResourceRequirement resourceRequirement = AssignmentPageUtil.loadResources(activityClassHandler, activityid, null, activity.getTestingDepartmentid(), AssignmentPage.ResourceMode.ASSIGNED, connectionId, sdiProcessor, tp, false, null);
            AssignmentPageResourceData resourceData = resourceRequirement.getResourceData();
            resourceData.setResourceRequirements(resources);
            boolean found = false;
            for (int i = 0; i < resourceData.getResources().size(); ++i) {
                AssignmentPageResourceContainer resourceContainer = resourceData.getResources().get(i);
                String classN = resourceContainer.getResourceSDC() == AssignmentPage.ResourceSDC.INSTRUMENT ? "resource_instrument" : "resource_user";
                if (resourceContainer.getData() != null && resourceContainer.getData().getRowCount() > 0) {
                    html.append("<div class=\"").append(classN).append("\">");
                    AssignmentPageUtil.drawResourceHtml(html, null, null, null, null, null, displayTimeZone, resourceContainer.resourceData, resourceContainer.attachmentData, resourceContainer.getResourceSDC().getName(), resourceContainer, null, 64, "activityResources", AssignmentPage.ColorScheme.DEFAULT, logger, tp, pageContext);
                    html.append("</div>");
                    found = true;
                }
                if (resourceContainer.getWorkareas() == null || resourceContainer.getWorkareas().getRowCount() <= 0) continue;
                html.append("<div class=\"").append("resource_department").append("\">");
                AssignmentPageUtil.drawResourceHtml(html, null, null, null, null, null, displayTimeZone, resourceContainer.workareaData, resourceContainer.attachmentData, "Department", resourceContainer, null, 64, "activityResources", AssignmentPage.ColorScheme.DEFAULT, logger, tp, pageContext);
                html.append("</div>");
                found = true;
            }
            if (!found) {
                html.append("<div id=\"error\">").append(tp.translate("No Resources Found")).append(".</div>");
            }
            html.append("<div id=\"").append(elementid).append("toolbar\">");
            html.append(ActivityResources.getToolbar(pageContext, canAdd, canRemove, resourceData, tp));
            html.append("</div>");
            if (fullRender) {
                html.append("</div>");
                html.append("<script>");
                html.append("var activityResources = new ActivityResources('").append(eid).append("','").append(activity.getActivityid()).append("'").append("," + canAdd + "," + canRemove + ");");
                html.append("</script>");
            }
        }
    }

    @Override
    public String getHtml() {
        StringBuilder html = new StringBuilder();
        ZoneId displayTimeZone = ZoneOffset.UTC;
        if (this.connectionInfo.getTimeZone() != null && this.connectionInfo.getTimeZone().length() > 0) {
            try {
                try {
                    displayTimeZone = TimeZone.getTimeZone(this.connectionInfo.getTimeZone()).toZoneId();
                }
                catch (Exception e1) {
                    displayTimeZone = I18nUtil.getZoneIdFromString(this.connectionInfo.getTimeZone());
                }
            }
            catch (Exception e) {
                this.logger.error("Failed to find timezone", e);
            }
        } else {
            displayTimeZone = TimeZone.getDefault().toZoneId();
        }
        if (this.getSDIInfo() != null && this.getSDIInfo().getDataSet("primary") != null) {
            DataSet pri = this.getSDIInfo().getDataSet("primary");
            if (pri.isValidColumn("activityid") && pri.getRowCount() > 0 && pri.getValue(0, "activityid", "").length() > 0) {
                WAPCommands wapCommands = new WAPCommands(this.getConnectionId());
                try {
                    boolean canAdd = this.element.getProperty("canadd", "Y").equalsIgnoreCase("Y");
                    boolean canRemove = this.element.getProperty("canremove", "Y").equalsIgnoreCase("Y");
                    ActivityResources.renderHtml(html, pri.getValue(0, "activityid", ""), this.elementid, true, canAdd, canRemove, wapCommands, displayTimeZone, this.getConnectionId(), this.getTranslationProcessor(), this.getSDIProcessor(), this.logger, this.pageContext);
                }
                catch (Exception e) {
                    html.append("<div id=\"error\">").append(this.getTranslationProcessor().translate("Could not load activity details")).append(".</div>");
                }
            } else {
                html.append("<div id=\"error\">").append(this.getTranslationProcessor().translate("No activity found")).append(".</div>");
            }
        } else {
            html.append("<div id=\"error\">").append(this.getTranslationProcessor().translate("No activity data found")).append(".</div>");
        }
        return html.toString();
    }

    public static enum ButtonType {
        REMOVE("Remove", "FlatBlackDelete", "activityResources.remove()", "Remove Selected Work", ""),
        ADD("Add", "FlatBlackAdd", "activityResources.add()", "Add new work", ""),
        CUSTOM("", "", "", "", "");

        String tip;
        String text;
        String group;
        String image;
        String function;

        private ButtonType(String text, String image, String function, String tip, String group) {
            this.text = text;
            this.group = group;
            this.tip = tip;
            this.image = image;
            this.function = function;
        }

        public static PropertyList getButton(PropertyList pagebutton, TranslationProcessor translationProcessor) {
            PropertyList button = new PropertyList();
            PropertyList commonprops = new PropertyList();
            PropertyList userbuttonprops = new PropertyList();
            button.setProperty("id", pagebutton.getProperty("id"));
            button.setProperty("type", "User");
            ButtonType buttonType = CUSTOM;
            try {
                buttonType = ButtonType.valueOf(pagebutton.getProperty("function", "Custom").toUpperCase());
            }
            catch (Exception exception) {
                // empty catch block
            }
            commonprops.setProperty("text", translationProcessor.translate(pagebutton.getProperty("text", buttonType.text)));
            String im = pagebutton.getProperty("image", buttonType.image);
            commonprops.setProperty("image", (!im.startsWith("rc?command=image&image=") ? "rc?command=image&image=" : "") + im);
            commonprops.setProperty("imagelarge", "rc?command=image&image=" + pagebutton.getProperty("image", buttonType.image));
            commonprops.setProperty("show", pagebutton.getProperty("show"));
            commonprops.setProperty("ribbonstyle", "Large");
            commonprops.setProperty("appearance", "standard");
            commonprops.setProperty("group", buttonType.group);
            commonprops.setProperty("tip", translationProcessor.translate(pagebutton.getProperty("tip", buttonType.tip)));
            commonprops.setProperty("mode", "Button");
            if (buttonType == CUSTOM) {
                userbuttonprops.setProperty("action", pagebutton.getProperty("custom"));
            } else {
                userbuttonprops.setProperty("action", buttonType.function);
            }
            button.setProperty("commonprops", commonprops);
            button.setProperty("userbuttonprops", userbuttonprops);
            return button;
        }
    }
}

