/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.axis.utils.Messages
 */
package com.labvantage.sapphire.modules.issuemanagement.handlers;

import com.labvantage.sapphire.modules.issuemanagement.handlers.JavaxJSSESocketFactory;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Hashtable;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.axis.utils.Messages;

public class JavaxFakeTrustSocketFactory
extends JavaxJSSESocketFactory {
    static final String LABVANTAGE_CVS_ID = "$Revision: 1.1 $";

    public JavaxFakeTrustSocketFactory(Hashtable attributes) {
        super(attributes);
    }

    @Override
    protected SSLContext getContext() throws Exception {
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, new TrustManager[]{new FakeX509TrustManager()}, new SecureRandom());
            return sc;
        }
        catch (Exception exc) {
            throw new Exception(Messages.getMessage((String)"ftsf02"));
        }
    }

    public static class FakeX509TrustManager
    implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }
}

