/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.vc;

import com.labvantage.sapphire.admin.vc.LicenseAPI;
import java.io.File;
import java.text.SimpleDateFormat;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class LicenseKey
extends BaseSDCRules {
    protected String licenseRootDir = "C:/license";

    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.getRowCount(); ++i) {
            String licensekeyid = primary.getValue(i, "licensekeyid");
            actionProps.setProperty("licensekeyid", licensekeyid);
            String licensedto = primary.getValue(i, "licensedto");
            File fileDir = new File(this.licenseRootDir + "/" + licensedto);
            if (!fileDir.exists() && !fileDir.mkdir()) {
                throw new SapphireException("Cannot create directory:" + fileDir.getAbsolutePath());
            }
            fileDir = new File(this.licenseRootDir + "/" + licensedto + "/" + licensekeyid);
            if (!fileDir.exists() && !fileDir.mkdir()) {
                throw new SapphireException("Cannot create directory:" + fileDir.getAbsolutePath());
            }
            actionProps.setProperty("command", "generate");
            actionProps.setProperty("dir", fileDir.getAbsolutePath());
            if (primary.getValue(i, "expirydate").length() > 0) {
                actionProps.setProperty("expirydate", new SimpleDateFormat("dd-MM-yyyy").format(primary.getCalendar(i, "expirydate").getTime()));
            } else {
                actionProps.setProperty("expirydate", "(none)");
            }
            LicenseAPI.generate(actionProps);
            PropertyList addAttachProps = new PropertyList();
            addAttachProps.setProperty("sdcid", "LicenseKey");
            addAttachProps.setProperty("keyid1", licensekeyid);
            addAttachProps.setProperty("type", "R");
            addAttachProps.setProperty("filename", fileDir.getAbsolutePath() + "/labvantage.lic");
            this.getActionProcessor().processAction("AddSDIAttachment", "1", addAttachProps);
            addAttachProps = new PropertyList();
            addAttachProps.setProperty("sdcid", "LicenseKey");
            addAttachProps.setProperty("keyid1", licensekeyid);
            addAttachProps.setProperty("type", "R");
            addAttachProps.setProperty("filename", fileDir.getAbsolutePath() + "/labvantage.txt");
            this.getActionProcessor().processAction("AddSDIAttachment", "1", addAttachProps);
        }
    }
}

