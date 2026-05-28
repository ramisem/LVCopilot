/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.cmt.view;

import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.cmt.PropertyTreeSnapshot;
import com.labvantage.sapphire.cmt.SDISnapshot;
import com.labvantage.sapphire.cmt.view.LV_WorksheetSnapshotViewer;
import com.labvantage.sapphire.cmt.view.PropertyTreeSnapshotViewer;
import com.labvantage.sapphire.cmt.view.SDISnapshotViewer;
import com.labvantage.sapphire.cmt.view.StorageUnitSDCSnapshotViewer;
import com.labvantage.sapphire.services.SapphireConnection;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.xml.cmt.Snapshot;

public class SnapshotViewer
extends BaseCustom {
    protected String sdcid;
    protected SapphireConnection sapphireConnection;
    protected TranslationProcessor translationProcessor;
    protected SDCProcessor sdcProcessor;
    protected QueryProcessor queryProcessor;
    protected boolean diffReport = false;

    public SnapshotViewer() {
    }

    public SnapshotViewer(SapphireConnection sapphireConnection) {
        super.setConnectionId(sapphireConnection.getConnectionId());
        this.diffReport = false;
        this.sapphireConnection = sapphireConnection;
        this.translationProcessor = new TranslationProcessor(sapphireConnection.getConnectionId());
        this.sdcProcessor = new SDCProcessor(sapphireConnection.getConnectionId());
        this.queryProcessor = new QueryProcessor(sapphireConnection.getConnectionId());
    }

    public void initialize(SapphireConnection sapphireConnection) {
        super.setConnectionId(sapphireConnection.getConnectionId());
        this.diffReport = false;
        this.sapphireConnection = sapphireConnection;
        this.translationProcessor = new TranslationProcessor(sapphireConnection.getConnectionId());
        this.sdcProcessor = new SDCProcessor(sapphireConnection.getConnectionId());
        this.queryProcessor = new QueryProcessor(sapphireConnection.getConnectionId());
    }

    public static String getDiffHtml(SapphireConnection sapphireConnection, Snapshot sourceSnapshot, Snapshot targetSnapshot, boolean includeAuditColumns, boolean usecustomrenderer, boolean hideEmptyColumns, boolean hideInheritedProperties) throws SapphireException {
        if (sourceSnapshot instanceof SDISnapshot) {
            if (((SDISnapshot)sourceSnapshot).getSDCId().equals("LV_Worksheet")) {
                if (usecustomrenderer) {
                    return LV_WorksheetSnapshotViewer.getDiffHtml(sapphireConnection, (SDISnapshot)sourceSnapshot, (SDISnapshot)targetSnapshot, includeAuditColumns, hideEmptyColumns, false);
                }
                return SDISnapshotViewer.getDiffHtml(sapphireConnection, (SDISnapshot)sourceSnapshot, (SDISnapshot)targetSnapshot, includeAuditColumns, usecustomrenderer, hideEmptyColumns, hideInheritedProperties);
            }
            if (((SDISnapshot)sourceSnapshot).getSDCId().equals("StorageUnitSDC")) {
                return StorageUnitSDCSnapshotViewer.getDiffHtml(sapphireConnection, (SDISnapshot)sourceSnapshot, (SDISnapshot)targetSnapshot, includeAuditColumns, hideEmptyColumns, false);
            }
            if (((SDISnapshot)sourceSnapshot).getSDCId().equals("PropertyTree")) {
                return PropertyTreeSnapshotViewer.getDiffHtml(sapphireConnection, (PropertyTreeSnapshot)sourceSnapshot, (PropertyTreeSnapshot)targetSnapshot, includeAuditColumns, hideEmptyColumns, hideInheritedProperties);
            }
            return SDISnapshotViewer.getDiffHtml(sapphireConnection, (SDISnapshot)sourceSnapshot, (SDISnapshot)targetSnapshot, includeAuditColumns, usecustomrenderer, hideEmptyColumns, hideInheritedProperties);
        }
        throw new SapphireException("Snapshot viewer not available");
    }
}

