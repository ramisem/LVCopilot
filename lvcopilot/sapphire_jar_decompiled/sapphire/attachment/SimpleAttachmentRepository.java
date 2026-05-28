/*
 * Decompiled with CFR 0.152.
 */
package sapphire.attachment;

import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.sql.Blob;
import sapphire.SapphireException;
import sapphire.attachment.Attachment;
import sapphire.attachment.BaseAttachmentRepository;
import sapphire.util.Logger;

public class SimpleAttachmentRepository
extends BaseAttachmentRepository {
    @Override
    public boolean checkHash(Attachment attachment) {
        return false;
    }

    @Override
    public boolean canGenerateThumbnail() {
        return false;
    }

    @Override
    public boolean canBrowseRepository() {
        return false;
    }

    @Override
    public String[] getBrowseIncludes() {
        return null;
    }

    @Override
    public String getBrowseScript(String attachmentElementId) {
        return "";
    }

    @Override
    public String getBrowseButtonText() {
        return "";
    }

    @Override
    public boolean enableCaching() {
        return false;
    }

    @Override
    public boolean canEncrypt() {
        return false;
    }

    @Override
    public boolean canCompress() {
        return false;
    }

    @Override
    public boolean canHash() {
        return false;
    }

    @Override
    public void getSDIAttachment(Attachment attachment, Attachment.ThumbnailGeneration thumbnailGeneration) throws SapphireException {
        try {
            attachment.setInputStream(attachment.getBlob().getBinaryStream());
        }
        catch (Exception e) {
            Logger.logError("Could not obtain blob.", e);
        }
    }

    @Override
    public void preAddSDIAttachment(Attachment attachment) throws SapphireException {
        if (attachment.getInputStream() != null) {
            try {
                Blob blob = this.getSapphireConnection().getConnection().createBlob();
                ByteBuffer buffer = ByteBuffer.allocateDirect(16384);
                ReadableByteChannel inputChannel = Channels.newChannel(attachment.getInputStream());
                WritableByteChannel outputChannel = Channels.newChannel(blob.setBinaryStream(1L));
                while (inputChannel.read(buffer) != -1) {
                    buffer.flip();
                    outputChannel.write(buffer);
                    buffer.compact();
                }
                buffer.flip();
                while (buffer.hasRemaining()) {
                    outputChannel.write(buffer);
                }
                attachment.setBlob(blob);
            }
            catch (Exception e) {
                throw new SapphireException(e);
            }
        } else {
            attachment.setBlob(null);
        }
    }

    @Override
    public void postAddSDIAttachment(Attachment attachment) throws SapphireException {
    }

    @Override
    public boolean preEditSDIAttachment(Attachment attachment, Attachment oldAttachment) throws SapphireException {
        if (attachment.getInputStream() != null) {
            try {
                Blob blob = this.getSapphireConnection().getConnection().createBlob();
                ByteBuffer buffer = ByteBuffer.allocateDirect(16384);
                ReadableByteChannel inputChannel = Channels.newChannel(attachment.getInputStream());
                WritableByteChannel outputChannel = Channels.newChannel(blob.setBinaryStream(1L));
                while (inputChannel.read(buffer) != -1) {
                    buffer.flip();
                    outputChannel.write(buffer);
                    buffer.compact();
                }
                buffer.flip();
                while (buffer.hasRemaining()) {
                    outputChannel.write(buffer);
                }
                attachment.setBlob(blob);
                return true;
            }
            catch (Exception e) {
                throw new SapphireException(e);
            }
        }
        return false;
    }

    @Override
    public void postEditSDIAttachment(Attachment attachment, Attachment oldAttachment) throws SapphireException {
    }

    @Override
    public void preDeleteSDIAttachment(Attachment attachment) throws SapphireException {
    }

    @Override
    public void postDeleteSDIAttachment(Attachment oldAttachment) throws SapphireException {
    }

    @Override
    public void cleanUpRepository(Attachment oldAttachment) throws SapphireException {
    }
}

