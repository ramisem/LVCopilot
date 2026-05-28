/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.attachment;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.services.Attachment;
import java.io.IOException;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;

public class HashedAttachmentInputStream
extends DigestInputStream {
    private sapphire.attachment.Attachment attachment = null;

    public HashedAttachmentInputStream(sapphire.attachment.Attachment attachment, MessageDigest digest) {
        super(attachment.getInputStream(), digest);
        this.attachment = attachment;
    }

    @Override
    public void close() throws IOException {
        if (this.attachment != null && !this.attachment.isEncrypted()) {
            byte[] md5sum = this.digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            long hash = bigInt.longValue();
            Trace.logDebug("Generated New Hash: " + hash);
            ((Attachment)this.attachment).checkHash(hash);
        }
        super.close();
    }
}

