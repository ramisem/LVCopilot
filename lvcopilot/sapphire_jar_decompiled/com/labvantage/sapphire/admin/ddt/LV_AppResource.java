/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.util.LabVantageClassLoader;
import com.labvantage.sapphire.util.cache.CacheNames;
import com.labvantage.sapphire.util.cache.CacheUtil;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import sapphire.SapphireException;
import sapphire.accessor.AttachmentProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.action.BaseSDCRules;
import sapphire.attachment.Attachment;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class LV_AppResource
extends BaseSDCRules
implements CacheNames {
    public static final String SDCID = "LV_AppResource";
    public static final String KEYID1 = "appresourceid";
    public static final String ATTACHMENT_CLASS = "AppResource";
    public static final String ATTACHMENT_CLASS_MINIFY = "AppResourceMinify";
    private static final String COLUMN_ATTACHMENTNUM = "attachmentnum";
    private static final String COLUMN_ATTACHMENTCLASS = "attachmentclass";
    private static final String MESSAGE_ONLY_ONE_ATTACHMENT_ALLOWED = "Only one attachment allowed per Application Resource";
    private static final String ERROR_ID = "Invalid attachment";

    @Override
    public void postDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        HashSet<String> sdiSet = new HashSet<String>(Arrays.asList(StringUtil.split(actionProps.getProperty("keyid1"), ";")));
        this.flushCaches(sdiSet);
    }

    @Override
    public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        HashSet<String> sdiSet = new HashSet<String>(Arrays.asList(StringUtil.split(actionProps.getProperty("keyid1"), ";")));
        this.flushCaches(sdiSet);
    }

    @Override
    public void preAddSDIAttachment(Attachment attachment) throws SapphireException {
    }

    @Override
    public void postAddSDIAttachment(Attachment attachment) {
        HashSet<String> sdiSet = new HashSet<String>();
        sdiSet.add(attachment.getKeyId1());
        this.flushCaches(sdiSet);
    }

    @Override
    public void postEditSDIAttachment(Attachment attachment) throws SapphireException {
        HashSet<String> sdiSet = new HashSet<String>();
        sdiSet.add(attachment.getKeyId1());
        this.flushCaches(sdiSet);
        if (attachment.getAttachmentClass().equals(ATTACHMENT_CLASS)) {
            SDIProcessor sdiProcessor = this.getSDIProcessor();
            SDIRequest sdiRequest = new SDIRequest();
            sdiRequest.setSDCid(SDCID);
            sdiRequest.setKeyid1List(attachment.getKeyId1());
            sdiRequest.setRequestItem("attachment");
            SDIData data = sdiProcessor.getSDIData(sdiRequest);
            DataSet attachments = data.getDataset("attachment");
            AttachmentProcessor attachmentProcessor = new AttachmentProcessor(this.getConnectionId());
            for (int i = 0; i < attachments.getRowCount(); ++i) {
                int attachmentnum = attachments.getInt(i, COLUMN_ATTACHMENTNUM);
                String attachmentClass = attachments.getString(i, COLUMN_ATTACHMENTCLASS);
                if (!attachmentClass.equals(ATTACHMENT_CLASS_MINIFY)) continue;
                Attachment attachment2 = Attachment.getAttachment(SDCID, attachment.getKeyId1(), null, null, attachmentnum);
                attachmentProcessor.deleteSDIAttachment(attachment2);
            }
        }
    }

    @Override
    public void postDeleteSDIAttachment(Attachment attachment) throws SapphireException {
        HashSet<String> sdiSet = new HashSet<String>();
        sdiSet.add(attachment.getKeyId1());
        this.flushCaches(sdiSet);
    }

    public void flushCaches(Set<String> sdiSet) {
        for (String keyid1 : sdiSet) {
            CacheUtil.remove(this.getDatabaseid(), "AppResourceTags", keyid1);
            CacheUtil.remove(this.getDatabaseid(), "AppResourceSdis", keyid1);
            LabVantageClassLoader.reset(this.getDatabaseid());
        }
    }
}

