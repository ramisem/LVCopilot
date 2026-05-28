/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.wap.activity;

import com.labvantage.opal.elements.advancedtoolbar.AdvancedToolbar;
import com.labvantage.sapphire.modules.wap.activity.Activity;
import com.labvantage.sapphire.modules.wap.activity.ActivityClassHandler;
import com.labvantage.sapphire.modules.wap.activity.AssignmentPageUtil;
import com.labvantage.sapphire.modules.wap.activity.BaseActivityElement;
import com.labvantage.sapphire.modules.wap.activity.WAPCommands;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ActivityWorkList
extends BaseActivityElement {
    public String getToolbar() {
        AdvancedToolbar tb = new AdvancedToolbar();
        tb.setPageContext(this.pageContext);
        tb.setElementid("activitywork_advancedtoolbar");
        PropertyList properties = new PropertyList();
        properties.setProperty("displaystyle", "");
        properties.setProperty("showtitle", "N");
        PropertyListCollection buttons = new PropertyListCollection();
        PropertyListCollection pagebuttons = this.getElementProperties().getCollection("buttons");
        if (pagebuttons != null) {
            for (int i = 0; i < pagebuttons.size(); ++i) {
                PropertyList button = ButtonType.getButton(pagebuttons.getPropertyList(i), this.getTranslationProcessor());
                button.setProperty("buttontype", "User");
                buttons.add(button);
                if (button.getProperty("text").length() != 0) continue;
                button.getPropertyListNotNull("commonprops").setProperty("showtext", "N");
            }
        }
        properties.setProperty("buttons", buttons);
        properties.setProperty("rendermode", "Button");
        tb.setElementProperties(properties);
        return tb.getHtml();
    }

    @Override
    public String getHtml() {
        StringBuilder html = new StringBuilder();
        if (this.getSDIInfo() != null && this.getSDIInfo().getDataSet("primary") != null) {
            DataSet pri = this.getSDIInfo().getDataSet("primary");
            if (pri.isValidColumn("activityid") && pri.getRowCount() > 0 && pri.getValue(0, "activityid", "").length() > 0) {
                WAPCommands wapCommands = new WAPCommands(this.getConnectionId());
                try {
                    DataSet worksdis = wapCommands.getActivityWorkSDIs(pri.getValue(0, "activityid", ""));
                    if (worksdis != null) {
                        Activity activity = wapCommands.getActivityDetails(pri.getValue(0, "activityid", ""));
                        String activityClass = activity.getActivityClass();
                        String sdcid = worksdis.getValue(0, "sdcid", "");
                        ActivityClassHandler activityClassHandler = ActivityClassHandler.getInstance(this.getConnectionId(), AssignmentPageUtil.getWapPolicy(this.getConnectionId()), activityClass, sdcid);
                        if (activityClassHandler != null) {
                            String iframesrc = "rc?command=file&file=WEB-OPAL/pagetypes/list/maintenance_list.jsp";
                            PropertyList lookUpPageDirectives = activityClassHandler.getListPageDirectives(activity.getTestingDepartmentid(), "", "", false, false, this.getSDCProcessor(), this.getTranslationProcessor(), this.pageContext);
                            lookUpPageDirectives.setProperty("lookupcallback", "activityWork.addDialogCallback");
                            PropertyList pagedirectives = activityClassHandler.getListPageDirectives(worksdis.getColumnValues("workkeyid1", ";"), this.getSDCProcessor(), this.getTranslationProcessor(), this.pageContext);
                            html.append("<iframe name=\"wsdilistframe\" id=\"wsdilistframe\" onload=\"\" style=\"border:solid 1px #CCCCCC;width:").append(this.element.getProperty("width", "600")).append("px;height:").append(this.element.getProperty("height", "300")).append("px;\" ></iframe>");
                            html.append("<script src=\"WEB-CORE/modules/wap/scripts/activityworklist.js\" type=\"text/javascript\"></script>");
                            html.append("<script>");
                            html.append("var activityWork = new ActivityWork('").append(activity.getActivityid()).append("',").append(pagedirectives.toJSONString()).append(",'").append(iframesrc).append("',").append(lookUpPageDirectives.toJSONString()).append(");");
                            html.append("</script>");
                        } else {
                            html.append("<div id=\"error\">").append(this.getTranslationProcessor().translate("Unable to load activity class")).append(".</div>");
                        }
                    } else {
                        html.append("<div id=\"norows\">").append("No Work").append("</div>");
                    }
                    html.append("<div id=\"").append(this.elementid).append("toolbar\">");
                    html.append(this.getToolbar());
                    html.append("</div>");
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
        REMOVE("Remove", "FlatBlackDelete", "activityWork.remove()", "Remove Selected Work", ""),
        ADD("Add", "FlatBlackAdd", "activityWork.add()", "Add new work", ""),
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

