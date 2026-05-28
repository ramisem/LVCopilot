/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdi;

import com.labvantage.sapphire.actions.sdi.BaseSDIAction;
import com.labvantage.sapphire.services.Attachment;
import com.labvantage.sapphire.util.LabVantageClassLoader;
import sapphire.SapphireException;
import sapphire.attachment.BaseAttachmentRepository;
import sapphire.xml.PropertyList;

public class CleanUpRepository
extends BaseSDIAction
implements sapphire.action.CleanUpRepository {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String sdcId = properties.getProperty("sdcid");
        String keyId1 = properties.getProperty("keyid1");
        String keyId2 = properties.getProperty("keyid2");
        String keyId3 = properties.getProperty("keyid3");
        int attachmentnum = Integer.parseInt(properties.getProperty("attachmentnum"));
        String attachmentRepositoryID = properties.getProperty("attachmentrepositoryid");
        String attachmentRepositoryNodeID = properties.getProperty("attachmentrepositorynodeid", "Sapphire Custom");
        String externalRepository = properties.getProperty("externalrepository");
        String fileName = properties.getProperty("filename");
        BaseAttachmentRepository attachmentRepository = BaseAttachmentRepository.getRepository(attachmentRepositoryID, attachmentRepositoryNodeID, this.getConnectionProcessor().getSapphireConnection());
        Attachment attachment = new Attachment();
        attachment.setSDCId(sdcId);
        attachment.setKeyId1(keyId1);
        attachment.setKeyId2(keyId2);
        attachment.setKeyId3(keyId3);
        attachment.setAttachmentNum(attachmentnum);
        attachment.setAttachmentRepositoryId(attachmentRepositoryID);
        attachment.setAttachmentRepositoryNodeId(attachmentRepositoryNodeID);
        attachment.setRepositoryId(externalRepository);
        attachment.setFilename(fileName);
        if (attachmentRepository != null) {
            LabVantageClassLoader.executeCode(attachmentRepository.getClassLoader(), () -> {
                attachmentRepository.postDeleteSDIAttachment(attachment);
                attachmentRepository.cleanUpRepository(attachment);
            }, false);
        }
    }
}

