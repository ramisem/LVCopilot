/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.codec.binary.Base64
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.pageelements.workflow.workflowdefpainter.WorkflowDefImageCreator;
import com.labvantage.sapphire.platform.Configuration;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import org.apache.commons.codec.binary.Base64;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class LV_WorkflowDef
extends BaseSDCRules {
    public static String getText(String workflowdefid, String workflowdefversionid, String workflowdefvariantid) {
        return workflowdefid + "(ver:" + workflowdefversionid + ", var:" + workflowdefvariantid + ")";
    }

    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        if (actionProps.getProperty("skipthumbnail", "N").equalsIgnoreCase("N")) {
            String taskImage = this.getTaskImage(sdiData);
            DataSet primary = sdiData.getDataset("primary");
            String keyid1 = primary.getValue(0, "workflowdefid");
            String keyid2 = primary.getValue(0, "workflowdefversionid");
            String keyid3 = primary.getValue(0, "workflowdefvariantid");
            PropertyList editProps = new PropertyList();
            editProps.setProperty("sdcid", "LV_WorkflowDef");
            editProps.setProperty("keyid1", keyid1);
            editProps.setProperty("keyid2", keyid2);
            editProps.setProperty("keyid3", keyid3);
            editProps.setProperty("thumbnailimagesteps", taskImage);
            this.getActionProcessor().processAction("EditSDI", "1", editProps);
        }
    }

    @Override
    public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        if (actionProps.getProperty("thumbnailimagesteps", "").length() == 0 && actionProps.getProperty("skipthumbnail", "N").equalsIgnoreCase("N")) {
            String taskImage = this.getTaskImage(sdiData);
            DataSet primary = sdiData.getDataset("primary");
            String keyid1 = primary.getValue(0, "workflowdefid");
            String keyid2 = primary.getValue(0, "workflowdefversionid");
            String keyid3 = primary.getValue(0, "workflowdefvariantid");
            PropertyList editProps = new PropertyList();
            editProps.setProperty("sdcid", "LV_WorkflowDef");
            editProps.setProperty("keyid1", keyid1);
            editProps.setProperty("keyid2", keyid2);
            editProps.setProperty("keyid3", keyid3);
            editProps.setProperty("thumbnailimagesteps", taskImage);
            this.getActionProcessor().processAction("EditSDI", "1", editProps);
        }
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        if (actionProps.containsKey("execstatus")) {
            DataSet primary = sdiData.getDataset("primary");
            for (int i = 0; i < primary.size(); ++i) {
                String execstatus = primary.getValue(i, "execstatus", "A");
                if (!execstatus.equals("X")) continue;
                StringBuffer workflowexecid = new StringBuffer();
                this.database.createPreparedResultSet("SELECT workflowexecid FROM workflowexec WHERE workflowdefid = ? AND workflowdefversionid = ? AND workflowdefvariantid = ?", new Object[]{primary.getValue(i, "workflowdefid"), primary.getValue(i, "workflowdefversionid"), primary.getValue(i, "workflowdefvariantid")});
                while (this.database.getNext()) {
                    workflowexecid.append(";").append(this.database.getValue("workflowexecid"));
                }
                if (workflowexecid.length() <= 0) continue;
                PropertyList editWorkflowExec = new PropertyList();
                editWorkflowExec.setProperty("sdcid", "LV_WorkflowExec");
                editWorkflowExec.setProperty("keyid1", workflowexecid.substring(1));
                editWorkflowExec.setProperty("execstatus", "X");
                this.getActionProcessor().processAction("EditSDI", "1", editWorkflowExec);
            }
        }
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        StringBuffer workflowexecid = new StringBuffer();
        this.database.createPreparedResultSet("SELECT workflowexecid FROM workflowexec, rsetitems WHERE rsetitems.rsetid = ? AND rsetitems.sdcid = 'LV_WorkflowDef' AND rsetitems.keyid1 = workflowexec.workflowdefid AND rsetitems.keyid2 = workflowdefversionid AND rsetitems.keyid3 = workflowdefvariantid", new Object[]{rsetid});
        while (this.database.getNext()) {
            workflowexecid.append(";").append(this.database.getValue("workflowexecid"));
        }
        if (workflowexecid.length() > 0) {
            PropertyList deleteProps = new PropertyList();
            deleteProps.setProperty("sdcid", "LV_WorkflowExec");
            deleteProps.setProperty("keyid1", workflowexecid.substring(1));
            this.getActionProcessor().processAction("DeleteSDI", "1", deleteProps);
        }
    }

    private String getTaskImage(SDIData sdiData) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        String keyid1 = primary.getValue(0, "workflowdefid");
        String keyid2 = primary.getValue(0, "workflowdefversionid");
        String keyid3 = primary.getValue(0, "workflowdefvariantid");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Configuration conf = Configuration.getInstance();
        String serverurl = conf.getServerHttpURL();
        WorkflowDefImageCreator.writeImage(keyid1, keyid2, keyid3, WorkflowDefImageCreator.ImageType.PNG, WorkflowDefImageCreator.RenderType.WORKFLOW, (OutputStream)byteArrayOutputStream, serverurl, this.getSDIProcessor(), this.getConnectionProcessor().getSapphireConnection(), this.logger);
        return Base64.encodeBase64String((byte[])byteArrayOutputStream.toByteArray());
    }
}

