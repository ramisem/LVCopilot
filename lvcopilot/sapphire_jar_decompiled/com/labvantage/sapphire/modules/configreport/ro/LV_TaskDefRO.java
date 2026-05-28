/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.ro;

import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.SDI;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.pageelements.workflow.taskdefpainter.TaskDefMaint;
import com.labvantage.sapphire.pageelements.workflow.workflowdefpainter.WorkflowDefImageCreator;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.xml.Node;
import com.labvantage.sapphire.xml.PropertyTree;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import sapphire.SapphireException;
import sapphire.ext.BaseSDCRO;
import sapphire.ext.ConfigReportContent;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class LV_TaskDefRO
extends BaseSDCRO {
    PropertyList currentTaskDef = null;
    static final int STEPWIDTH = 125;
    static final int STEPHEIGHT = 45;

    public void initialize(SapphireConnection connection) throws SapphireException {
        super.initialize("LV_TaskDef", connection);
    }

    @Override
    public void setCurrentSDIData(SDIData sdiData) throws SapphireException {
        super.setCurrentSDIData(sdiData);
        try {
            this.currentTaskDef = TaskDefMaint.getTaskData(this.getConnectionId(), sdiData);
        }
        catch (SapphireException e) {
            this.currentTaskDef = new PropertyList();
            Trace.logError("Failed to fetch task data.", e);
        }
    }

    @Override
    public int gotoSection(SDI sdi) {
        super.gotoSection(sdi);
        for (int i = 0; i < this.sdiList.size(); ++i) {
            SDI curr = (SDI)this.sdiList.get(i);
            if (!curr.getKeyid1().equalsIgnoreCase(sdi.getKeyid1())) continue;
            try {
                if (!this.dataSource.equals("XMLREPORT")) {
                    this.currentTaskDef = this.getTaskData();
                } else {
                    this.currentTaskDef = this.getTaskDataFromXMLReport(this.refReportFolder);
                    if (this.currentTaskDef == null) {
                        this.currentSDI = null;
                        this.currentSDIData = null;
                        return -1;
                    }
                }
                return i;
            }
            catch (Exception e) {
                Trace.log("Failed to fetch taskdef for:" + sdi.getKeyid1());
                e.printStackTrace();
            }
        }
        this.currentSDI = null;
        this.currentSDIData = null;
        return -1;
    }

    public String getSystemTask() {
        String colValue = this.getPrimaryValue("coreflag");
        if ("S".equals(colValue)) {
            return "Yes";
        }
        return "No";
    }

    public PropertyList getTaskData() {
        PropertyList outProps = new PropertyList();
        TaskDefMaint.getTaskData("LV_TaskDef", this.getKeyid1(), this.getKeyid2(), this.getKeyid3(), false, this.getSDIProcessor(), this.logger, outProps);
        return outProps;
    }

    public String getAutoExec() {
        String colValue = this.getPrimaryValue("autoexecflag");
        if ("Y".equals(colValue)) {
            return "Yes";
        }
        return "No";
    }

    public String getInstructions() {
        if (this.currentTaskDef != null) {
            return this.currentTaskDef.getProperty("instructions");
        }
        return "";
    }

    public String getSummaryStatement() {
        if (this.currentTaskDef != null) {
            return this.currentTaskDef.getProperty("summary");
        }
        return "";
    }

    public String getAllowPause() {
        String colValue = this.getPrimaryValue("allowdraft");
        if ("Y".equals(colValue)) {
            return "Yes";
        }
        return "No";
    }

    public String getStandalone() {
        String colValue = this.getPrimaryValue("standaloneflag");
        if ("Y".equals(colValue)) {
            return "Yes";
        }
        return "No";
    }

    public String getAllowCancel() {
        String colValue = this.getPrimaryValue("allowcancel");
        if ("Y".equals(colValue)) {
            return "Yes";
        }
        return "No";
    }

    public String getCancelScript() {
        if (this.currentTaskDef != null) {
            return this.currentTaskDef.getProperty("cancelscript", "");
        }
        return "";
    }

    public DataSet getTaskVariables() {
        DataSet ds = new DataSet();
        if (this.currentTaskDef != null) {
            PropertyListCollection variablesCollection = this.currentTaskDef.getCollectionNotNull("variables");
            ds.setColidCaseSensitive(true);
            for (int i = 0; i < variablesCollection.size(); ++i) {
                PropertyList currentVar = variablesCollection.getPropertyList(i);
                int currRow = ds.addRow();
                ds.setString(currRow, "Variable", currentVar.getProperty("variableid"));
                ds.setString(currRow, "Type", currentVar.getProperty("type"));
                ds.setString(currRow, "Default Value", currentVar.getProperty("defaultvalue"));
                ds.setString(currRow, "Setup Variable", currentVar.getProperty("setup"));
                ds.setString(currRow, "Exposed", currentVar.getProperty("exposed"));
                ds.setString(currRow, "Prompt", currentVar.getProperty("prompt"));
                ds.setString(currRow, "Modifiable", currentVar.getProperty("modifiable", "Y"));
                ds.setString(currRow, "Mandatory", currentVar.getProperty("mandatory"));
                ds.setString(currRow, "Hidden", currentVar.getProperty("hidden"));
                ds.setString(currRow, "Help Text", currentVar.getProperty("help"));
                ds.setString(currRow, "Editor Style", currentVar.getProperty("editorstyleid"));
                ds.setString(currRow, "Related Variabled", currentVar.getProperty("relatedvariableid"));
                ds.setString(currRow, "Description", currentVar.getProperty("description"));
            }
        }
        return ds;
    }

    public PropertyListCollection getTaskQueueInfo() {
        if (this.currentTaskDef != null) {
            return this.currentTaskDef.getCollection("taskio");
        }
        return new PropertyListCollection();
    }

    public String getTaskDefProperty(String propertyid) {
        if (this.currentTaskDef != null) {
            return this.currentTaskDef.getProperty(propertyid);
        }
        return "";
    }

    public DataSet getStages() {
        if (this.currentTaskDef != null) {
            PropertyListCollection variablesCollection = this.currentTaskDef.getCollectionNotNull("stages");
            DataSet ds = new DataSet();
            ds.setColidCaseSensitive(true);
            for (int i = 0; i < variablesCollection.size(); ++i) {
                PropertyList currentVar = variablesCollection.getPropertyList(i);
                int currRow = ds.addRow();
                ds.setString(currRow, "Stage", currentVar.getProperty("stageid"));
            }
            return ds;
        }
        return new DataSet();
    }

    public DataSet getSteps() {
        DataSet ds = new DataSet();
        if (this.currentTaskDef != null) {
            PropertyListCollection variablesCollection = this.currentTaskDef.getCollectionNotNull("steps");
            ds.setColidCaseSensitive(true);
            for (int i = 0; i < variablesCollection.size(); ++i) {
                PropertyList currentVar = variablesCollection.getPropertyList(i);
                int currRow = ds.addRow();
                ds.setString(currRow, "Step Id", currentVar.getProperty("stepid"));
                ds.setString(currRow, "Execution Title", currentVar.getProperty("title"));
                ds.setString(currRow, "Icon Text", currentVar.getProperty("shorttitle"));
                ds.setString(currRow, "Stage Id", currentVar.getProperty("stepgroupid"));
                ds.setString(currRow, "Instructions", currentVar.getProperty("instructions"));
                ds.setString(currRow, "Summary Statement", currentVar.getProperty("summary"));
                ds.setString(currRow, "Loaded Script", currentVar.getProperty("loadedscript"));
            }
        }
        return ds;
    }

    public PropertyList getAppearanceInfo() {
        PropertyList props = new PropertyList();
        if (this.currentTaskDef != null) {
            props.setProperty("shorttitle", this.currentTaskDef.getProperty("shorttitle"));
            props.setProperty("icon", this.currentTaskDef.getProperty("icon"));
            props.setProperty("appearance", this.currentTaskDef.getProperty("appearance"));
            props.setProperty("taskcolor", this.currentTaskDef.getProperty("taskcolor1"));
        }
        return props;
    }

    public PropertyList getStepConfiguration(String stepid) {
        PropertyList ret = new PropertyList();
        if (this.currentTaskDef != null) {
            PropertyListCollection variablesCollection = this.currentTaskDef.getCollectionNotNull("steps");
            for (int i = 0; i < variablesCollection.size(); ++i) {
                PropertyList currentVar = variablesCollection.getPropertyList(i);
                if (!currentVar.getProperty("stepid").equals(stepid)) continue;
                ret = currentVar.getPropertyList("steptypemerged");
                break;
            }
        }
        return ret;
    }

    public String getFirstStepId() {
        if (this.currentTaskDef != null) {
            return this.currentTaskDef.getProperty("startstepid");
        }
        return "";
    }

    public DataSet getNextSteps(String stepid) {
        DataSet ret = new DataSet();
        if (this.currentTaskDef != null) {
            PropertyListCollection variablesCollection = this.currentTaskDef.getCollectionNotNull("steps");
            for (int i = 0; i < variablesCollection.size(); ++i) {
                PropertyList currentVar = variablesCollection.getPropertyList(i);
                if (!currentVar.getProperty("stepid").equals(stepid)) continue;
                PropertyList next = currentVar.getPropertyListNotNull("next");
                PropertyListCollection coll = next.getCollectionNotNull("transitions");
                for (int b = 0; b < coll.size(); ++b) {
                    PropertyList curr = coll.getPropertyList(b);
                    ret.addRow();
                    ret.setString(b, "stepid", curr.getProperty("stepid"));
                }
            }
        }
        return ret;
    }

    public String getStepPropertyTreeId(String stepid) {
        if (this.currentTaskDef != null) {
            PropertyListCollection variablesCollection = this.currentTaskDef.getCollectionNotNull("steps");
            for (int i = 0; i < variablesCollection.size(); ++i) {
                PropertyList currentVar = variablesCollection.getPropertyList(i);
                if (!currentVar.getProperty("stepid").equals(stepid)) continue;
                return currentVar.getProperty("propertytreeid");
            }
        }
        return "";
    }

    public PropertyList getStepQueueSelectorDetails(String stepid) {
        PropertyList ret = new PropertyList();
        if (this.currentTaskDef != null) {
            PropertyListCollection variablesCollection = this.currentTaskDef.getCollectionNotNull("steps");
            for (int i = 0; i < variablesCollection.size(); ++i) {
                PropertyList currentVar = variablesCollection.getPropertyList(i);
                if (!currentVar.getProperty("stepid").equals(stepid)) continue;
                ret.setProperty("Step Id", stepid);
                ret.setProperty("Execution Title", currentVar.getProperty("title"));
                ret.setProperty("Icon Text", currentVar.getProperty("shorttitle"));
                ret.setProperty("Stage Id", currentVar.getProperty("stepgroupid"));
                ret.setProperty("Instructions", currentVar.getProperty("instructions"));
                ret.setProperty("Summary Statement", currentVar.getProperty("summary"));
                ret.setProperty("Loaded Script", currentVar.getProperty("loadedscript"));
            }
        }
        return ret;
    }

    public DataSet getToolbar(String stepid) {
        DataSet ret = new DataSet();
        ret.setColidCaseSensitive(true);
        ret.addColumn("Text", 0);
        ret.addColumn("Id", 0);
        ret.addColumn("Image", 0);
        ret.addColumn("Tip", 0);
        ret.addColumn("Callbefore Action", 0);
        ret.addColumn("Action", 0);
        ret.addColumn("Callback Action", 0);
        ret.addColumn("Callback Operation", 0);
        if (this.currentTaskDef != null) {
            PropertyListCollection variablesCollection = this.currentTaskDef.getCollectionNotNull("steps");
            for (int i = 0; i < variablesCollection.size(); ++i) {
                PropertyList currentVar = variablesCollection.getPropertyList(i);
                if (!currentVar.getProperty("stepid").equals(stepid)) continue;
                PropertyListCollection coll = currentVar.getCollectionNotNull("buttons");
                for (int b = 0; b < coll.size(); ++b) {
                    PropertyList curr = coll.getPropertyList(b);
                    ret.addRow();
                    ret.setString(b, "Text", curr.getProperty("text"));
                    ret.setString(b, "Id", curr.getProperty("buttonid"));
                    ret.setString(b, "Image", curr.getProperty("image"));
                    ret.setString(b, "Tip", curr.getProperty("tip"));
                    ret.setString(b, "Callbefore Action", curr.getProperty("callbeforeaction"));
                    ret.setString(b, "Action", curr.getProperty("action"));
                    ret.setString(b, "Callback Action", curr.getProperty("callbackaction"));
                    ret.setString(b, "Callback Operation", curr.getProperty("callbackoperation"));
                }
            }
        }
        return ret;
    }

    public DataSet getTransitions(String stepid) {
        DataSet ret = new DataSet();
        ret.setColidCaseSensitive(true);
        ret.addColumn("Target Step", 0);
        ret.addColumn("Case", 0);
        ret.addColumn("Label", 0);
        if (this.currentTaskDef != null) {
            PropertyListCollection variablesCollection = this.currentTaskDef.getCollectionNotNull("steps");
            for (int i = 0; i < variablesCollection.size(); ++i) {
                PropertyList currentVar = variablesCollection.getPropertyList(i);
                if (!currentVar.getProperty("stepid").equals(stepid)) continue;
                PropertyList next = currentVar.getPropertyListNotNull("next");
                PropertyListCollection coll = next.getCollectionNotNull("transitions");
                for (int b = 0; b < coll.size(); ++b) {
                    PropertyList curr = coll.getPropertyList(b);
                    ret.addRow();
                    ret.setString(b, "Target Step", curr.getProperty("stepid"));
                    ret.setString(b, "Case", curr.getProperty("case"));
                    ret.setString(b, "Label", curr.getProperty("text"));
                }
            }
        }
        return ret;
    }

    public String getAutoShowInfo() {
        if (this.currentTaskDef != null) {
            String colValue = this.currentTaskDef.getProperty("autoshowinfo");
            if ("S".equals(colValue)) {
                return "Step Instructions";
            }
            if ("D".equals(colValue)) {
                return "Diagram";
            }
            if ("T".equals(colValue)) {
                return "Task Instructions";
            }
            if ("L".equals(colValue)) {
                return "Task History";
            }
        }
        return "";
    }

    public String getCompleteAction() {
        if (this.currentTaskDef != null) {
            return this.currentTaskDef.getProperty("complete");
        }
        return "";
    }

    public String getCompletePage() {
        if (this.currentTaskDef != null) {
            return this.currentTaskDef.getProperty("completepage");
        }
        return "";
    }

    public String getCancelAction() {
        if (this.currentTaskDef != null) {
            return this.currentTaskDef.getProperty("cancel");
        }
        return "";
    }

    public String getCancelPage() {
        if (this.currentTaskDef != null) {
            return this.currentTaskDef.getProperty("cancelpage");
        }
        return "";
    }

    public String getConfirmCancel() {
        if (this.currentTaskDef != null) {
            return this.currentTaskDef.getProperty("cancelconfirm");
        }
        return "";
    }

    public void getRefTaskDefImage(String taskimagefilename) {
        if (this.dataSource.equals("XMLREPORT")) {
            String xmlSdiFileName = ConfigReportContent.generateSDISectionXMLFileName(this.currentSDI);
            String taskImageDefFileName = this.refReportFolder + "/xmlreport/" + xmlSdiFileName.replace(".xml", "_taskdefimage.png");
            try {
                ConfigReportContent.copyFile(new File(taskImageDefFileName), new File(this.folder + File.separator + "images" + File.separator + taskimagefilename));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getRefTaskStepsImage() {
        if (this.dataSource.equals("XMLREPORT")) {
            String xmlSdiFileName = ConfigReportContent.generateSDISectionXMLFileName(this.currentSDI);
            String taskImageDefFileName = this.refReportFolder + "/xmlreport/" + xmlSdiFileName.replace(".xml", "_taskstepsimage.png");
            return taskImageDefFileName;
        }
        return "";
    }

    public String getImageMapForSteps(String mapname) {
        String mapStr = "<map name=\"" + mapname + "\">\n";
        PropertyListCollection steps = this.currentTaskDef.getCollection("steps");
        if (steps != null) {
            for (int t = 0; t < steps.size(); ++t) {
                PropertyList step = steps.getPropertyList(t);
                int x = 0;
                int y = 0;
                try {
                    x = (int)Math.round(Double.parseDouble(step.getProperty("x")));
                    y = (int)Math.round(Double.parseDouble(step.getProperty("y")));
                    String stepid = step.getProperty("stepid");
                    PropertyList queueSelector = this.getStepQueueSelectorDetails(stepid);
                    String steptitle = "Step: " + queueSelector.getProperty("Execution Title") + "(" + stepid + ")";
                    String href = ConfigReportContent.generateSectionAnchor(steptitle);
                    mapStr = mapStr + "  <area shape=\"rect\" coords=\"" + x + "," + y + "," + (x + 125) + "," + (y + 45) + "\" href=\"#" + href + "\" alt=\"" + stepid + "\">\n";
                    continue;
                }
                catch (NumberFormatException e) {
                    Logger.logWarn("Could not obtain coordinates.");
                }
            }
        }
        mapStr = mapStr + "</map>\n";
        return mapStr;
    }

    public void getTaskDefImage(String url, String taskimagefilename, String connectionid, String taskOrSteps) {
        try {
            FileOutputStream os = new FileOutputStream(taskimagefilename);
            Configuration conf = Configuration.getInstance();
            String serverurl = conf.getServerHttpURL();
            WorkflowDefImageCreator.writeImage(this.getKeyid1(), this.getKeyid2(), this.getKeyid3(), WorkflowDefImageCreator.ImageType.PNG, taskOrSteps.equals("TASK") ? WorkflowDefImageCreator.RenderType.TASK : WorkflowDefImageCreator.RenderType.STEPS, (OutputStream)os, serverurl, this.getSDIProcessor(), this.getConnectionProcessor().getSapphireConnection(), this.logger);
        }
        catch (Exception e) {
            Trace.logError("Failed to get taskdefimage", e);
        }
    }

    private static PropertyListCollection getCategories(String keyid1, String keyid2, String keyid3, DataSet cats) {
        PropertyListCollection categories = new PropertyListCollection();
        if (cats != null && cats.getRowCount() > 0) {
            for (int i = 0; i < cats.getRowCount(); ++i) {
                String catid = cats.getValue(i, "categoryid", "");
                String taskdefId = cats.getValue(i, "keyid1", "");
                if (catid.length() <= 0 || !taskdefId.equalsIgnoreCase(keyid1)) continue;
                PropertyList cat = new PropertyList();
                cat.setProperty("categoryid", catid);
                cat.setProperty("mode", "S");
                categories.add(cat);
            }
        }
        return categories;
    }

    static PropertyTree getTree(PropertyList overrides, String stepid, String steptypeid, String steptypenode, String connectionId) throws SapphireException {
        PropertyTree tree = null;
        Node stepTypeNode = null;
        WebAdminProcessor webadminProcessor = new WebAdminProcessor(connectionId);
        try {
            tree = webadminProcessor.getPropertyTree(steptypeid);
            tree.setId(steptypeid);
            Node e = tree.getNode(steptypenode);
            stepTypeNode = tree.createNode("_steptypeoverrides", e);
            if (overrides != null) {
                stepTypeNode.setPropertyList(overrides);
            }
            return tree;
        }
        catch (Exception var9) {
            throw new SapphireException("Failed to obtain step node.", var9);
        }
    }

    private PropertyList getTaskDataFromXMLReport(String folder) throws SapphireException {
        String xmlSdiFileName = this.generateSDISectionXMLFileName(this.currentSDI);
        String xmlDataFileObjectsFileName = folder + "/xmlreport/" + xmlSdiFileName.replace(".xml", "_taskdata.xml");
        File f = new File(xmlDataFileObjectsFileName);
        try {
            if (f.exists()) {
                String xml = FileUtil.getFileString(f);
                PropertyList ret = new PropertyList();
                ret.setPropertyList(xml);
                return ret;
            }
        }
        catch (Exception e) {
            Trace.log("Failed to read taskdef:" + e.getMessage());
        }
        return null;
    }
}

