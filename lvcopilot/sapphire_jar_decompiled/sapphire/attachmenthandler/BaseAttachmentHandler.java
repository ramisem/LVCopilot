/*
 * Decompiled with CFR 0.152.
 */
package sapphire.attachmenthandler;

import com.labvantage.sapphire.modules.sdms.util.ResultDataGrid;
import java.util.List;
import sapphire.SapphireException;
import sapphire.attachment.Attachment;
import sapphire.attachmenthandler.AttachmentHandler;
import sapphire.attachmenthandler.HandlerType;
import sapphire.attachmenthandler.SDILinks;
import sapphire.util.ActionBlock;
import sapphire.xml.PropertyList;

public abstract class BaseAttachmentHandler
extends com.labvantage.sapphire.modules.sdms.handlers.BaseAttachmentHandler
implements AttachmentHandler {
    @Override
    public boolean isDebugMode() {
        return super.isDebugMode();
    }

    @Override
    public String getHelperURL() {
        return super.getHelperURL();
    }

    @Override
    public String getSDCId() {
        return super.getSDCId();
    }

    @Override
    public String getKeyId1() {
        return super.getKeyId1();
    }

    @Override
    public String getKeyId2() {
        return super.getKeyId2();
    }

    @Override
    public String getKeyId3() {
        return super.getKeyId3();
    }

    @Override
    public String getAtachmentHandlerId() {
        return super.getAtachmentHandlerId();
    }

    @Override
    public void logMessage(String message) {
        super.logMessage(message);
    }

    @Override
    public void addFile(String filepath, String aliasFileName, String attachmentClass) {
        super.addFile(filepath, aliasFileName, attachmentClass);
    }

    @Override
    public void setResultGrid(ResultDataGrid resultGrid) {
        super.setResultGrid(resultGrid);
    }

    @Override
    public int getResultResultGridCount() {
        return super.getResultResultGridCount();
    }

    @Override
    public ResultDataGrid getResultGrid(int resultgridindex) {
        return super.getResultGrid(resultgridindex);
    }

    @Override
    public ResultDataGrid getResultGrid() {
        return super.getResultGrid();
    }

    @Override
    public void addMetaData(String name, String value) {
        super.addMetaData(name, value);
    }

    @Override
    public void addFileMetaData(PropertyList propertyList, Attachment attachment) {
        super.addFileMetaData(propertyList, attachment);
    }

    @Override
    public void setActionBlock(ActionBlock actionBlock) {
        super.setActionBlock(actionBlock);
    }

    @Override
    public void addLinkSDI(String sdcid, String keyid1, String keyid2, String keyid3) {
        super.addLinkSDI(sdcid, keyid1, keyid2, keyid3);
    }

    @Override
    public boolean isDatabaseRequired() {
        return super.isDatabaseRequired();
    }

    @Override
    public PropertyList getFileMetaData(Attachment attachment) {
        return super.getFileMetaData(attachment);
    }

    @Override
    public SDILinks getLinkSDI() {
        return super.getLinkSDI();
    }

    @Override
    public String getHandlerId() {
        return super.getHandlerId();
    }

    @Override
    public PropertyList getMetaData() {
        return super.getMetaData();
    }

    @Override
    public abstract void handleData(List<Attachment> var1, PropertyList var2) throws SapphireException;

    @Override
    public HandlerType getHandlerType() {
        return super.getHandlerType();
    }
}

