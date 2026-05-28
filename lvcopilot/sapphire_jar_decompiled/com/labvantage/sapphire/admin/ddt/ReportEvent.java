/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.admin.system.AttachmentProcessor;
import com.labvantage.sapphire.report.SapphireReport;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.attachment.Attachment;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class ReportEvent
extends BaseSDCRules {
    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.size(); ++i) {
            String reportType;
            String keyid1 = primary.getString(i, "keyid1", primary.getString(i, "reporteventid"));
            String disposition = primary.getString(i, "disposition");
            String previousDisposition = this.getBeforeEditImage().getDataset("primary").getString(i, "disposition");
            if ((previousDisposition == null || "Pending".equals(previousDisposition)) && disposition != null && (disposition.equalsIgnoreCase("Confirmed") || disposition.equals("Rejected"))) {
                DataSet dsSigning;
                primary.addColumn("reviewedby", 0);
                primary.setValue(i, "reviewedby", this.connectionInfo.getSysuserId());
                primary.addColumn("revieweddt", 2);
                primary.setValue(i, "revieweddt", "NOW");
                if (disposition.equalsIgnoreCase("Confirmed") && !(dsSigning = this.getQueryProcessor().getPreparedSqlDataSet("SELECT r.signingmode, re.digitallysignedflag, a.attachmentnum FROM reportevent re INNER JOIN report r ON re.reportid = r.reportid AND re.reportversionid = r.reportversionid INNER JOIN sdiattachment a ON a.sdcid  = 'ReportEvent' AND re.reporteventid = a.keyid1 WHERE re.reporteventid = ? and re.displaytype = 'pdf'", (Object[])new String[]{keyid1})).isEmpty() && !dsSigning.getString(0, "digitallysignedflag", "").equalsIgnoreCase("Y") && dsSigning.getString(0, "signingmode", this.getConnectionProcessor().getSysConfigProperty("signingmode", "")).equals("With Report Confirmation")) {
                    PropertyList pl = new PropertyList();
                    pl.setProperty("reporteventid", keyid1);
                    pl.setProperty("attachmentnum", dsSigning.getValue(0, "attachmentnum"));
                    this.getActionProcessor().processAction("SignPDFReport", "1", pl);
                    primary.addColumn("digitallysignedflag", 0);
                    primary.setValue(i, "digitallysignedflag", "Y");
                }
            }
            if (OpalUtil.isEmpty(previousDisposition) || !previousDisposition.equalsIgnoreCase("Confirmed") && (!previousDisposition.equalsIgnoreCase("Rejected") || !disposition.equalsIgnoreCase("Pending"))) continue;
            primary.addColumn("reviewedby", 0);
            primary.setValue(i, "reviewedby", null);
            primary.addColumn("revieweddt", 2);
            primary.setValue(i, "revieweddt", null);
            String sql = "SELECT r.initialdisposition, r.watermarkflag FROM reportevent re, report r where re.reportid = r.reportid and re.reportversionid = r.reportversionid and re.reporteventid = ?";
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{keyid1});
            String initialdisposition = ds.getString(0, "initialdisposition");
            String watermarkflag = ds.getString(0, "watermarkflag");
            if (!initialdisposition.equalsIgnoreCase("Confirmed") && (!initialdisposition.equalsIgnoreCase("Pending") || !watermarkflag.equalsIgnoreCase("Y"))) continue;
            Attachment reportAttachment = null;
            byte[] reportBytes = null;
            sql = "SELECT attachmentnum FROM sdiattachment WHERE sdcid = 'ReportEvent' AND keyid1 = ? AND attachmentclass = 'ReportEvent'";
            ds.clear();
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{keyid1});
            if (ds.getRowCount() > 0) {
                reportAttachment = Attachment.getAttachment("ReportEvent", keyid1, null, null, ds.getInt(0, "attachmentnum"));
                AttachmentProcessor attachmentProcessor = new AttachmentProcessor(this.getConnectionid());
                reportAttachment = attachmentProcessor.getSDIAttachment(reportAttachment, Attachment.ThumbnailGeneration.DISABLED);
            }
            if (reportAttachment == null || !(reportType = reportAttachment.getSourceFilename().substring(reportAttachment.getSourceFilename().indexOf(".") + 1)).equalsIgnoreCase("pdf")) continue;
            reportBytes = SapphireReport.addWatermark(reportAttachment.getData(), "Pending Confirmation");
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", reportAttachment.getSDCId());
            props.setProperty("keyid1", reportAttachment.getKeyId1());
            props.setProperty("keyid2", reportAttachment.getKeyId2());
            props.setProperty("keyid3", reportAttachment.getKeyId3());
            props.setProperty("attachmentnum", Integer.toString(reportAttachment.getAttachmentNum()));
            props.setProperty("filename", reportAttachment.getFilename());
            props.setProperty("attachmentclass", reportAttachment.getAttachmentClass());
            props.setProperty("attachmentclass", reportAttachment.getAttachmentClass());
            props.setProperty("datahash", "");
            new sapphire.accessor.AttachmentProcessor(this.getConnectionId()).editSDIAttachment((HashMap)props, reportBytes);
        }
    }
}

