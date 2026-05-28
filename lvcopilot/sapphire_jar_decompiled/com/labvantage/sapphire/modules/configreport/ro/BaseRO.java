/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.ro;

import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.servlet.RequestProcessor;
import sapphire.SapphireException;
import sapphire.util.DBAccess;

public abstract class BaseRO
extends BaseCustom {
    public static final String DATASOURCE_DATABASE = "DATABASE";
    public static final String DATASOURCE_XMLREPORT = "XMLREPORT";
    public static final String DATASOURCE_INPUT = "INPUT";
    protected String folder;
    protected DBAccess database;
    protected SapphireConnection sapphireConnection;
    protected String createdBy;
    protected String chapterName;
    protected WebAdminProcessor webAdminProcessor;
    protected RequestProcessor requestProcessor;
    public String dataSource = "";
    protected String refReportFolder = "";

    public void initialize(String sdcid, SapphireConnection sapphireConnection) throws SapphireException {
        this.setConnectionId(sapphireConnection.getConnectionId());
        this.dataSource = DATASOURCE_INPUT;
        this.chapterName = sdcid;
        this.sapphireConnection = sapphireConnection;
        this.webAdminProcessor = new WebAdminProcessor(sapphireConnection.getConnectionId());
    }

    public void initialize(String chapterName, String folder, String createdBy, String refReportFolder, SapphireConnection sapphireConnection) throws SapphireException {
        this.setConnectionId(sapphireConnection.getConnectionId());
        this.dataSource = DATASOURCE_XMLREPORT;
        this.folder = folder;
        this.createdBy = createdBy;
        this.chapterName = chapterName;
        this.refReportFolder = refReportFolder;
        this.sapphireConnection = sapphireConnection;
        this.webAdminProcessor = new WebAdminProcessor(sapphireConnection.getConnectionId());
    }

    public void initialize(String chapterName, SapphireConnection sapphireConnection, String folder, String createdBy) throws SapphireException {
        this.dataSource = DATASOURCE_DATABASE;
        this.setConnectionId(sapphireConnection.getConnectionId());
        this.sapphireConnection = sapphireConnection;
        DBUtil dbUtil = new DBUtil(sapphireConnection.getConnectionId());
        dbUtil.setConnection(sapphireConnection);
        this.database = dbUtil;
        this.folder = folder;
        this.createdBy = createdBy;
        this.webAdminProcessor = new WebAdminProcessor(sapphireConnection.getConnectionId());
        this.requestProcessor = new RequestProcessor(sapphireConnection.getConnectionId());
        this.chapterName = chapterName;
    }

    public void startChapter() throws SapphireException {
    }

    public void startSection() throws SapphireException {
    }
}

