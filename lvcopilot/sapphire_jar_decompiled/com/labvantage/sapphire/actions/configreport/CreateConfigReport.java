/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.configreport;

import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.modules.configreport.ConfigReportController;
import com.labvantage.sapphire.services.SapphireConnection;
import java.io.File;
import java.io.IOException;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class CreateConfigReport
extends BaseAction {
    public static final String ID = "CreateConfigReport";
    public static final String VERSIONID = "1";
    public static final String PROPERTY_POLICYNODE = "policynode";
    public static final String PROPERTY_CATEGORY = "category";
    public static final String PROPERTY_SDCID = "sdcid";
    public static final String PROPERTY_KEYID1 = "keyid1";
    public static final String PROPERTY_KEYID2 = "keyid2";
    public static final String PROPERTY_KEYID3 = "keyid3";
    public static final String PROPERTY_EXCLUDE_SDCID = "excludesdcid";
    public static final String PROPERTY_REPORT_FILENAME = "filename";
    public static final String PROPERTY_REPORT_FOLDER = "folder";
    public static final String PROPERTY_FRAMES = "frames";
    public static final String PROPERTY_APPLICATION_ROOT = "applicationroot";
    public static final String PROPERTY_REPORT_OPTIONS = "options";
    public static final String PROPERTY_CREATEDBY = "createdby";
    public static final String PROPERTY_APPLICATION_URL = "applicationurl";

    @Override
    public void processAction(PropertyList propertyList) throws SapphireException {
        String folder = propertyList.getProperty(PROPERTY_REPORT_FOLDER);
        try {
            File f = new File(folder);
            if (f.exists()) {
                FileUtil.deleteAll(f);
            }
            f.mkdir();
        }
        catch (IOException e) {
            throw new SapphireException("Cannot create report folder ");
        }
        propertyList.setProperty("connection", this.getConnectionId());
        propertyList.setProperty(PROPERTY_APPLICATION_ROOT, propertyList.getProperty(PROPERTY_APPLICATION_ROOT) + "/");
        propertyList.setProperty(PROPERTY_EXCLUDE_SDCID, propertyList.getProperty(PROPERTY_EXCLUDE_SDCID, ""));
        SapphireConnection sapphireConnection = new SapphireConnection(this.database.getConnection(), this.connectionInfo);
        ConfigReportController controller = new ConfigReportController(propertyList, sapphireConnection);
        controller.createReport();
    }
}

