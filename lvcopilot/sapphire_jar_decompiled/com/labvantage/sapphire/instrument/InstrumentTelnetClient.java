/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.instrument;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.instrument.InstrumentClient;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import javax.net.SocketFactory;
import sapphire.accessor.TranslationProcessor;

public class InstrumentTelnetClient
implements InstrumentClient {
    protected String host;
    protected int port = 23;
    private String endMessageToken = "\r\n";
    private SocketFactory factory = SocketFactory.getDefault();
    private Socket socket = null;
    private TranslationProcessor translationProcessor;

    public InstrumentTelnetClient(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
    }

    @Override
    public void connect() throws Exception {
        this.socket = this.factory.createSocket(this.host, this.port);
    }

    @Override
    public void disconnect() throws Exception {
        if (this.socket != null && this.socket.isConnected()) {
            this.socket.close();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public String sendMessage(String message, boolean hasReturn, int timeout) throws Exception {
        OutputStream outstr = this.socket.getOutputStream();
        InputStream instr = this.socket.getInputStream();
        try {
            this.socket.setSoTimeout(100);
            instr.read(new byte[1024]);
        }
        catch (Exception exception) {
            // empty catch block
        }
        outstr.write(message.getBytes());
        outstr.flush();
        boolean responseTimedout = false;
        if (hasReturn) {
            String response;
            this.socket.setSoTimeout(timeout);
            StringBuffer rx = new StringBuffer();
            try {
                byte[] buff;
                int ret_read = 0;
                String strictEndToken = "";
                while ((ret_read = instr.read(buff = new byte[1024])) > 0) {
                    if (this.endMessageToken != null && this.endMessageToken.indexOf("!") == 0) {
                        strictEndToken = this.endMessageToken.substring(1);
                    } else {
                        this.socket.setSoTimeout(5000);
                    }
                    rx.append(new String(buff, 0, ret_read, Charset.forName("ISO-8859-1")));
                    Trace.logDebug("Read Response:" + rx);
                    if (!(strictEndToken.length() > 0 ? rx.indexOf(strictEndToken) > 0 : !"\r\n".equals(this.endMessageToken) && rx.indexOf(this.endMessageToken) > 0) && ret_read >= 0) continue;
                    break;
                }
            }
            catch (SocketTimeoutException ste) {
                if (this.socket.getSoTimeout() == timeout) {
                    responseTimedout = true;
                } else {
                    Trace.logWarn("End of Response String" + this.endMessageToken + " not found in instrument response. Stop reading after 5000ms.");
                }
            }
            catch (Exception e) {
                throw new IOException(e.getMessage());
            }
            finally {
                outstr.close();
                instr.close();
            }
            int index = 0;
            for (int i = 0; i < rx.length(); ++i) {
                char c = rx.charAt(i);
                if (c == '\u00ff') {
                    ++index;
                    continue;
                }
                if (c < '\u00f0' || c == '\u00f8') continue;
                ++index;
                ++index;
            }
            String string = response = rx.length() > index ? rx.substring(index) : rx.toString();
            if (responseTimedout) {
                response = "(Waiting for response timed out with nothing received)";
                if (this.translationProcessor != null) {
                    response = this.translationProcessor.translate("(Waiting for response timed out with nothing received)");
                }
                throw new Exception(response);
            }
            return response;
        }
        this.socket.setSoTimeout(500);
        try {
            instr.read();
        }
        catch (Exception exception) {
        }
        finally {
            instr.close();
        }
        return "";
    }

    public String getEndMessageToken() {
        return this.endMessageToken;
    }

    public void setEndMessageToken(String endMessageToken) {
        if (endMessageToken != null && endMessageToken.length() > 0) {
            this.endMessageToken = endMessageToken;
        }
    }

    public void setTranslationProcessor(TranslationProcessor translationProcessor) {
        this.translationProcessor = translationProcessor;
    }
}

