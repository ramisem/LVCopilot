/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.workflow.workflowdefpainter;

import com.labvantage.sapphire.pageelements.controls.Image;
import com.labvantage.sapphire.pageelements.workflow.taskdefpainter.TaskDefMaint;
import com.labvantage.sapphire.pageelements.workflow.workflowdefpainter.WorkflowDefMaint;
import org.json.JSONObject;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class WorkflowDefTaskLookup
extends BaseElement {
    @Override
    public String getHtml() {
        PropertyListCollection tasks;
        int count = 0;
        StringBuffer out = new StringBuffer();
        TranslationProcessor translationProcessor = this.getTranslationProcessor();
        PropertyList tasksData = TaskDefMaint.getTasksData(false, this.getSDIProcessor(), this.getConnectionProcessor().getSapphireConnection(), this.logger);
        if (tasksData != null && (tasks = tasksData.getCollection("tasks")) != null) {
            PropertyList workflowprops = null;
            String sjs = this.element.getProperty("properties");
            if (sjs.length() > 0) {
                try {
                    workflowprops = new PropertyList(new JSONObject(sjs));
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            String taskdefitemid = this.element.getProperty("taskdefitemid");
            PropertyList passedIn = workflowprops != null && taskdefitemid.length() > 0 ? (workflowprops.getCollection("tasks") != null ? workflowprops.getCollection("tasks").find("taskdefitemid", taskdefitemid) : null) : null;
            for (int i = 0; i < tasks.size(); ++i) {
                boolean current;
                PropertyList task = tasks.getPropertyList(i);
                boolean bl = passedIn != null ? passedIn.getProperty("taskdefid").equals(task.getProperty("taskdefid")) && passedIn.getProperty("taskdefversionid", "1").equals(task.getProperty("taskdefversionid", "1")) && passedIn.getProperty("taskdefvariantid", "1").equals(task.getProperty("taskdefvariantid", "1")) : (current = false);
                if (passedIn == null || current || !WorkflowDefMaint.isValidTaskSwap(passedIn, task, workflowprops != null ? workflowprops.getCollection("tasks") : null, false)) continue;
                String returnFunc = "oOpener." + this.element.getProperty("lookupcallback", "sapphire.alert") + "(oDialog,'" + this.element.getProperty("taskdefitemid", "") + "','" + task.getProperty("taskdefid") + "','" + task.getProperty("taskdefversionid") + "','" + task.getProperty("taskdefvariantid") + "');void(0);";
                out.append("<div class=\"rowdiv\">");
                Image image = new Image(this.pageContext);
                image.setImageSrc(task.getProperty("icon"));
                out.append(image.getHtml());
                out.append("&nbsp;");
                out.append("<a href=\"javascript:" + returnFunc + "\">");
                out.append(task.getProperty("taskdefid"));
                out.append(" (" + task.getProperty("taskdefversionid") + " - " + task.getProperty("taskdefvariantid") + ")");
                out.append("</a>");
                out.append("&nbsp;");
                out.append(task.getProperty("shorttitle", task.getProperty("longtitle", "")));
                out.append("</div>");
                ++count;
            }
        }
        if (count == 0) {
            out.append("<p>" + translationProcessor.translate("No Compatible Tasks Available") + "</p>");
        }
        return out.toString();
    }
}

