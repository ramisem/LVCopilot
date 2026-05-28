/*
 * Decompiled with CFR 0.152.
 */
package sapphire.ext;

import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.instrument.InstrumentTelnetClient;
import com.labvantage.sapphire.instrument.textparser.GenericTextParser;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import sapphire.accessor.ConnectionProcessor;
import sapphire.xml.PropertyList;

public class BaseInstrumentProvider
extends BaseCustom {
    private ExecutorService executeService = Executors.newCachedThreadPool();
    private static HashMap<String, Future> resultCache = new HashMap();
    private static HashMap<String, InstrumentTelnetClient> instrumentClientCache = new HashMap();
    private static HashMap<String, String> instrumentConnectByCache = new HashMap();

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String executeCommand(PropertyList commandProps) throws Exception {
        String returnflag;
        String commandcode;
        final String host = commandProps.getProperty("host");
        int port = 23;
        if (commandProps.getProperty("port").length() > 0) {
            port = Integer.parseInt(commandProps.getProperty("port"));
        }
        if ((commandcode = commandProps.getProperty("commandcode")).length() == 0 && commandProps.getProperty("commandid").length() > 0) {
            commandcode = commandProps.getProperty("commandid");
        }
        int timeout = 60000;
        if (commandProps.getProperty("timeout").length() > 0) {
            try {
                timeout = Integer.parseInt(commandProps.getProperty("timeout"));
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        final boolean hasResponse = "Y".equals(returnflag = commandProps.getProperty("returnflag")) || "P".equals(returnflag);
        final String comcode = commandcode;
        final int pt = port;
        String hostport = host + ":" + port;
        final int tout = timeout;
        final String responseEndString = commandProps.getProperty("responseterminationstring");
        String result = "";
        if (resultCache.get(hostport) == null) {
            Future<String> results = this.executeService.submit(new Callable<String>(){

                @Override
                public String call() throws Exception {
                    return BaseInstrumentProvider.this.callInstrumentTelnet(host, pt, comcode, hasResponse, responseEndString, tout);
                }
            });
            resultCache.put(hostport, results);
            result = this.waitForResult(host, pt, timeout + 5000);
        } else if (instrumentConnectByCache.get(hostport) != null && !instrumentConnectByCache.get(hostport).equals(this.getConnectionId())) {
            String otherconnectionid = instrumentConnectByCache.get(hostport);
            String otheruser = new ConnectionProcessor(otherconnectionid).getConnectionInfo(otherconnectionid).getSysuserId();
            result = "Error:The Instrument at " + hostport + " is currently connected by " + otheruser;
        } else {
            result = this.waitForResult(host, pt, timeout + 5000);
            HashMap<String, String> hashMap = instrumentConnectByCache;
            synchronized (hashMap) {
                instrumentConnectByCache.remove(hostport);
            }
        }
        if (result.indexOf("Error:") == 0 || result.equals(this.getTranslationProcessor().translate("(Waiting for response timed out with nothing received)"))) {
            throw new Exception(result);
        }
        return result;
    }

    public HashMap parseResponse(String responseStr, PropertyList commandProps) throws Exception {
        String parseRule = commandProps.getProperty("parsingrule");
        if (parseRule.length() > 0 && responseStr.length() > 0) {
            return GenericTextParser.parse(responseStr, parseRule);
        }
        return new HashMap();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void cancelCommand(PropertyList commandProps) {
        String host = commandProps.getProperty("host");
        int port = 23;
        if (commandProps.getProperty("port").length() > 0) {
            port = Integer.parseInt(commandProps.getProperty("port"));
        }
        String hostport = host + ":" + port;
        HashMap<String, Future> hashMap = resultCache;
        synchronized (hashMap) {
            if (resultCache.get(hostport) != null) {
                resultCache.get(hostport).cancel(true);
                resultCache.remove(hostport);
            }
            if (instrumentClientCache.get(hostport) != null) {
                try {
                    instrumentClientCache.get(hostport).disconnect();
                }
                catch (Exception exception) {
                }
                finally {
                    if (instrumentClientCache.get(hostport) != null) {
                        instrumentClientCache.remove(hostport);
                    }
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private String callInstrumentTelnet(String host, int port, String comcode, boolean hasResponse, String responseEndStr, int timeout) throws Exception {
        String response = "";
        String hostport = host + ":" + port;
        HashMap<String, InstrumentTelnetClient> hashMap = instrumentClientCache;
        synchronized (hashMap) {
            if (instrumentClientCache.get(hostport) != null) {
                try {
                    instrumentClientCache.get(hostport).disconnect();
                }
                catch (Exception exception) {
                }
                finally {
                    if (instrumentClientCache.get(hostport) != null) {
                        instrumentClientCache.remove(hostport);
                    }
                }
            }
        }
        InstrumentTelnetClient instrclient = null;
        try {
            instrclient = new InstrumentTelnetClient(host, port);
            instrclient.setEndMessageToken(responseEndStr);
            instrclient.setTranslationProcessor(this.getTranslationProcessor());
            instrclient.connect();
            HashMap<String, InstrumentTelnetClient> hashMap2 = instrumentClientCache;
            synchronized (hashMap2) {
                instrumentClientCache.put(hostport, instrclient);
                instrumentConnectByCache.put(hostport, this.getConnectionId());
            }
            response = instrclient.sendMessage(comcode, hasResponse, timeout);
        }
        catch (Exception e) {
            response = "Error:" + e.getMessage();
        }
        finally {
            if (instrclient != null) {
                instrclient.disconnect();
            }
        }
        return response;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private String waitForResult(String host, int port, int waitformilliSecond) throws Exception {
        String hostport = host + ":" + port;
        int loopcount = waitformilliSecond / 100;
        String result = "";
        try {
            for (int i = 0; !resultCache.get(hostport).isDone() && i < loopcount; ++i) {
                Thread.sleep(100L);
            }
        }
        catch (NullPointerException e) {
            throw new Exception("Error:Command Cancelled!");
        }
        finally {
            HashMap<String, Future> hashMap = resultCache;
            synchronized (hashMap) {
                if (resultCache.get(hostport) != null && resultCache.get(hostport).isDone()) {
                    result = (String)resultCache.get(hostport).get();
                    resultCache.remove(hostport);
                }
            }
        }
        return result;
    }
}

