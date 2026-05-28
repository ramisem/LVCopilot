/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.email;

import com.labvantage.sapphire.util.email.EmailCaptureOptions;
import com.labvantage.sapphire.util.email.FetchMail;

public interface ReadAndParseMail {
    public FetchMail.Email readEmail(EmailCaptureOptions var1) throws Exception;
}

