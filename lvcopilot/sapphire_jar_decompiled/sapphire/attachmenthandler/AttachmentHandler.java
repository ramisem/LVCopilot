/*
 * Decompiled with CFR 0.152.
 */
package sapphire.attachmenthandler;

import com.labvantage.sapphire.modules.sdms.util.ResultDataGrid;
import java.util.List;
import sapphire.SapphireException;
import sapphire.attachment.Attachment;
import sapphire.attachmenthandler.HandlerType;
import sapphire.attachmenthandler.SDILinks;
import sapphire.util.ActionBlock;
import sapphire.xml.PropertyList;

public interface AttachmentHandler {
    public String getSDCId();

    public String getKeyId1();

    public String getKeyId2();

    public String getKeyId3();

    public String getAtachmentHandlerId();

    public void logMessage(String var1);

    public void addFile(String var1, String var2, String var3);

    public void setResultGrid(ResultDataGrid var1);

    public int getResultResultGridCount();

    public ResultDataGrid getResultGrid(int var1);

    public ResultDataGrid getResultGrid();

    public void addMetaData(String var1, String var2);

    public void addFileMetaData(PropertyList var1, Attachment var2);

    public void setActionBlock(ActionBlock var1);

    public void addLinkSDI(String var1, String var2, String var3, String var4);

    public boolean isDatabaseRequired();

    public PropertyList getFileMetaData(Attachment var1);

    public SDILinks getLinkSDI();

    public String getHandlerId();

    public PropertyList getMetaData();

    public void handleData(List<Attachment> var1, PropertyList var2) throws SapphireException;

    public HandlerType getHandlerType();

    public ResultDataGrid addResultGrid();
}

