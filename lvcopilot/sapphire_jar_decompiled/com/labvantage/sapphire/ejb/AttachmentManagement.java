/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.ejb;

import com.labvantage.sapphire.ejb.ManagerException;
import java.nio.file.Path;
import java.util.HashMap;
import sapphire.attachment.Attachment;

public interface AttachmentManagement {
    public HashMap addSDIAttachment(String var1, HashMap var2, byte[] var3) throws ManagerException;

    public Attachment addSDIAttachment(String var1, Attachment var2, boolean var3, boolean var4, String var5) throws ManagerException;

    public HashMap editSDIAttachment(String var1, HashMap var2, byte[] var3) throws ManagerException;

    public Attachment editSDIAttachment(String var1, Attachment var2, boolean var3, boolean var4, String var5) throws ManagerException;

    public void deleteSDIAttachment(String var1, String var2, String var3, String var4, String var5, String var6, boolean var7) throws ManagerException;

    public void deleteSDIAttachment(String var1, Attachment var2, boolean var3, String var4) throws ManagerException;

    public Path getSDIAttachmentLocalFile(String var1, Attachment var2, boolean var3) throws ManagerException;

    public Attachment getSDIAttachment(String var1, Attachment var2, Attachment.ThumbnailGeneration var3) throws ManagerException;

    public Attachment getSDIAttachment(String var1, Attachment var2, int var3, Attachment.ThumbnailGeneration var4) throws ManagerException;

    public Attachment getTempAttachment(String var1, Attachment var2, Attachment.ThumbnailGeneration var3) throws ManagerException;

    public Attachment getSDIAttachment(String var1, String var2, String var3, String var4, String var5, int var6) throws ManagerException;

    public Attachment getSDIAttachment(String var1, String var2, String var3, String var4, String var5, int var6, Attachment.ThumbnailGeneration var7) throws ManagerException;

    public Attachment getSDIAttachment(String var1, String var2, String var3, String var4, String var5, String var6) throws ManagerException;

    public Attachment getSDIAttachment(String var1, String var2, String var3, String var4, String var5, String var6, Attachment.ThumbnailGeneration var7) throws ManagerException;

    public Attachment getTempAttachment(String var1, String var2, String var3, String var4, String var5, String var6) throws ManagerException;

    public Attachment getTempAttachment(String var1, String var2, String var3, String var4, String var5, String var6, Attachment.ThumbnailGeneration var7) throws ManagerException;

    public Attachment getSDIAttachment(String var1, String var2, String var3, String var4, String var5, int var6, int var7) throws ManagerException;

    public Attachment getSDIAttachment(String var1, String var2, String var3, String var4, String var5, int var6, int var7, Attachment.ThumbnailGeneration var8) throws ManagerException;
}

