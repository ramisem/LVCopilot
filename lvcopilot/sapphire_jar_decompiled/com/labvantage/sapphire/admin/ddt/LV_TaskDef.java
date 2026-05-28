/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.codec.binary.Base64
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.admin.ddt.LV_WorkflowDef;
import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.modules.eventmanager.EventManager;
import com.labvantage.sapphire.pageelements.workflow.workflowdefpainter.WorkflowDefImageCreator;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.cache.CacheUtil;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import org.apache.commons.codec.binary.Base64;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class LV_TaskDef
extends BaseSDCRules {
    public static String getText(String taskdefid, String taskdefversionid, String taskdefvariantid) {
        return taskdefid + "(ver:" + taskdefversionid + ", var:" + taskdefvariantid + ")";
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        this.checkCoreType(sdiData);
    }

    private String getTaskStepsImage(SDIData sdiData) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        String keyid1 = primary.getValue(0, "taskdefid");
        String keyid2 = primary.getValue(0, "taskdefversionid");
        String keyid3 = primary.getValue(0, "taskdefvariantid");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Configuration conf = Configuration.getInstance();
        String serverurl = conf.getServerHttpURL();
        WorkflowDefImageCreator.writeImage(keyid1, keyid2, keyid3, WorkflowDefImageCreator.ImageType.PNG, WorkflowDefImageCreator.RenderType.STEPS, (OutputStream)byteArrayOutputStream, serverurl, this.getSDIProcessor(), this.getConnectionProcessor().getSapphireConnection(), this.logger);
        return Base64.encodeBase64String((byte[])byteArrayOutputStream.toByteArray());
    }

    private String getTaskImage(SDIData sdiData) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        String keyid1 = primary.getValue(0, "taskdefid");
        String keyid2 = primary.getValue(0, "taskdefversionid");
        String keyid3 = primary.getValue(0, "taskdefvariantid");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Configuration conf = Configuration.getInstance();
        String serverurl = conf.getServerHttpURL();
        WorkflowDefImageCreator.writeImage(keyid1, keyid2, keyid3, WorkflowDefImageCreator.ImageType.PNG, WorkflowDefImageCreator.RenderType.TASK, (OutputStream)byteArrayOutputStream, serverurl, this.getSDIProcessor(), this.getConnectionProcessor().getSapphireConnection(), this.logger);
        return Base64.encodeBase64String((byte[])byteArrayOutputStream.toByteArray());
    }

    private void saveThumbnails(SDIData sdiData) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        String keyid1 = primary.getValue(0, "taskdefid");
        String keyid2 = primary.getValue(0, "taskdefversionid");
        String keyid3 = primary.getValue(0, "taskdefvariantid");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Configuration conf = Configuration.getInstance();
        String serverurl = conf.getServerHttpURL();
        WorkflowDefImageCreator.writeImage(keyid1, keyid2, keyid3, WorkflowDefImageCreator.ImageType.PNG, WorkflowDefImageCreator.RenderType.TASK, (OutputStream)byteArrayOutputStream, serverurl, this.getSDIProcessor(), this.getConnectionProcessor().getSapphireConnection(), this.logger);
        String taskimage = Base64.encodeBase64String((byte[])byteArrayOutputStream.toByteArray());
        byteArrayOutputStream = new ByteArrayOutputStream();
        WorkflowDefImageCreator.writeImage(keyid1, keyid2, keyid3, WorkflowDefImageCreator.ImageType.PNG, WorkflowDefImageCreator.RenderType.STEPS, (OutputStream)byteArrayOutputStream, serverurl, this.getSDIProcessor(), this.getConnectionProcessor().getSapphireConnection(), this.logger);
        String taskstepsimage = Base64.encodeBase64String((byte[])byteArrayOutputStream.toByteArray());
        if (taskimage.length() > 0) {
            primary.setClob(0, "thumbnailimageappearance", taskimage);
        }
        if (taskstepsimage.length() > 0) {
            primary.setClob(0, "thumbnailimagesteps", taskstepsimage);
        }
    }

    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        boolean devMode;
        DataSet primary = sdiData.getDataset("primary");
        ConfigurationProcessor config = new ConfigurationProcessor(this.getConnectionId());
        try {
            devMode = config.getSysConfigProperty("devmode", "N").equalsIgnoreCase("Y");
        }
        catch (Exception e) {
            devMode = false;
        }
        if (!devMode) {
            primary.setValue(-1, "coreflag", "U");
        } else {
            primary.setValue(-1, "coreflag", "C");
        }
    }

    @Override
    public boolean requiresBeforeEditImage() {
        return false;
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        String workflowCheck = "SELECT DISTINCT workflowdefid, workflowdefversionid, workflowdefvariantid FROM   workflowdeftask, rsetitems WHERE  rsetitems.rsetid = ? AND    workflowdeftask.taskdefid = rsetitems.keyid1 AND workflowdeftask.taskdefversionid = rsetitems.keyid2 AND workflowdeftask.taskdefvariantid = rsetitems.keyid3 ORDER BY 1, 2, 3";
        this.database.createPreparedResultSet(workflowCheck, new Object[]{rsetid});
        StringBuffer workflowRefs = new StringBuffer();
        for (int i = 0; i < 10 && this.database.getNext(); ++i) {
            String workflow = LV_WorkflowDef.getText(this.database.getString("workflowdefid"), this.database.getString("workflowdefversionid"), this.database.getString("workflowdefvariantid"));
            workflowRefs.append("<br/>").append(workflow);
        }
        if (workflowRefs.length() > 0) {
            boolean more = this.database.getNext();
            this.throwError("TaskDefUsed", "VALIDATION", "Task(s) cannot be deleted because it exists in " + (more ? "at least" : "") + " the following Workflows:" + workflowRefs + (more ? "<br/>..." : ""));
        }
        this.checkCoreType(rsetid, "You cannot delete 'Core' tasks");
    }

    @Override
    public boolean requiresEditDetailPrimary() {
        return true;
    }

    @Override
    public void preAddDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        this.checkCoreType(sdiData);
    }

    @Override
    public void preEditDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        this.checkCoreType(sdiData);
    }

    @Override
    public void preDeleteDetail(String rsetid, PropertyList actionProps) throws SapphireException {
        this.checkCoreType(rsetid, "You cannot delete 'Core' task details");
    }

    private void checkCoreType(SDIData sdiData) throws SapphireException {
        boolean devMode;
        if (this.isCMTImport()) {
            return;
        }
        ConfigurationProcessor config = new ConfigurationProcessor(this.getConnectionId());
        try {
            devMode = config.getSysConfigProperty("devmode", "N").equalsIgnoreCase("Y");
        }
        catch (Exception e) {
            devMode = false;
        }
        if (!devMode) {
            DataSet primary = sdiData.getDataset("primary");
            if (primary == null) {
                throw new SapphireException("Primary dataset not available");
            }
            for (int i = 0; i < primary.size(); ++i) {
                if (!"C".equals(primary.getString(i, "coreflag"))) continue;
                throw new SapphireException("You cannot modify 'Core' tasks");
            }
        }
    }

    private void checkCoreType(String rsetid, String message) throws SapphireException {
        boolean devMode;
        ConfigurationProcessor config = new ConfigurationProcessor(this.getConnectionId());
        try {
            devMode = config.getSysConfigProperty("devmode", "N").equalsIgnoreCase("Y");
        }
        catch (Exception e) {
            devMode = false;
        }
        if (!devMode) {
            this.database.createPreparedResultSet("SELECT coreflag FROM taskdef, rsetitems WHERE taskdef.taskdefid = rsetitems.keyid1 AND taskdef.taskdefversionid = rsetitems.keyid2 AND taskdef.taskdefvariantid = rsetitems.keyid3 AND rsetid = ?", new Object[]{rsetid});
            while (this.database.getNext()) {
                String csuflag = this.database.getString("coreflag");
                if (csuflag == null || !csuflag.equals("C")) continue;
                throw new SapphireException(message);
            }
        }
    }

    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        if (this.hasEventInputs(sdiData)) {
            EventManager.loadEventPlans(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
        }
        CacheUtil.clear(this.getConnectionProcessor().getSapphireConnection().getDatabaseId(), "TaskDef");
        if (!this.isCMTImport() && actionProps.getProperty("skipthumbnail", "N").equalsIgnoreCase("N")) {
            String taskImage = this.getTaskImage(sdiData);
            String taskStepsImage = this.getTaskStepsImage(sdiData);
            DataSet primary = sdiData.getDataset("primary");
            String keyid1 = primary.getValue(0, "taskdefid");
            String keyid2 = primary.getValue(0, "taskdefversionid");
            String keyid3 = primary.getValue(0, "taskdefvariantid");
            PropertyList editProps = new PropertyList();
            editProps.setProperty("sdcid", "LV_TaskDef");
            editProps.setProperty("keyid1", keyid1);
            editProps.setProperty("keyid2", keyid2);
            editProps.setProperty("keyid3", keyid3);
            editProps.setProperty("thumbnailimageappearance", taskImage);
            editProps.setProperty("thumbnailimagesteps", taskStepsImage);
            this.getActionProcessor().processAction("EditSDI", "1", editProps);
        }
    }

    @Override
    public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        if (this.hasEventInputs(sdiData)) {
            EventManager.loadEventPlans(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
        }
        CacheUtil.clear(this.getConnectionProcessor().getSapphireConnection().getDatabaseId(), "TaskDef");
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.size(); ++i) {
            String cacheKey = primary.getString(i, "taskdefid") + ";" + primary.getString(i, "taskdefversionid") + ";" + primary.getString(i, "taskdefvariantid");
            CacheUtil.removeAllStartWith(this.getConnectionProcessor().getSapphireConnection().getDatabaseId(), "TaskStepProps", cacheKey);
            CacheUtil.removeAllStartWith(this.getConnectionProcessor().getSapphireConnection().getDatabaseId(), "TaskStepTypeProps", cacheKey);
        }
        if (!this.isCMTImport() && actionProps.getProperty("thumbnailimagesteps", "").length() == 0 && actionProps.getProperty("skipthumbnail", "N").equalsIgnoreCase("N")) {
            String taskImage = this.getTaskImage(sdiData);
            String taskStepsImage = this.getTaskStepsImage(sdiData);
            String keyid1 = primary.getValue(0, "taskdefid");
            String keyid2 = primary.getValue(0, "taskdefversionid");
            String keyid3 = primary.getValue(0, "taskdefvariantid");
            PropertyList editProps = new PropertyList();
            editProps.setProperty("sdcid", "LV_TaskDef");
            editProps.setProperty("keyid1", keyid1);
            editProps.setProperty("keyid2", keyid2);
            editProps.setProperty("keyid3", keyid3);
            editProps.setProperty("thumbnailimageappearance", taskImage);
            editProps.setProperty("thumbnailimagesteps", taskStepsImage);
            this.getActionProcessor().processAction("EditSDI", "1", editProps);
        }
    }

    @Override
    public void postDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        CacheUtil.clear(this.getConnectionProcessor().getSapphireConnection().getDatabaseId(), "TaskDef");
        CacheUtil.clear(this.getConnectionProcessor().getSapphireConnection().getDatabaseId(), "TaskStepTypeProps");
        CacheUtil.clear(this.getConnectionProcessor().getSapphireConnection().getDatabaseId(), "TaskStepProps");
    }

    private boolean hasEventInputs(SDIData sdiData) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        return this.database.getPreparedCount("SELECT count( taskdefio.ioid ) FROM   taskdefio WHERE  taskdefio.taskdefid = ? AND    taskdefio.taskdefversionid = ? AND    taskdefio.taskdefvariantid = ? AND    taskdefio.waittype = 'event' ", new Object[]{primary.getValue(0, "taskdefid"), primary.getValue(0, "taskdefversionid"), primary.getValue(0, "taskdefvariantid")}) > 0;
    }
}

