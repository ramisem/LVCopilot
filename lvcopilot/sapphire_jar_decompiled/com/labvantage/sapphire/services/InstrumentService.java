/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.services;

import com.labvantage.sapphire.instrument.textparser.GenericTextParser;
import com.labvantage.sapphire.services.BaseService;
import com.labvantage.sapphire.services.QueryService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import java.util.HashMap;
import sapphire.error.ErrorHandler;
import sapphire.ext.BaseInstrumentProvider;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class InstrumentService
extends BaseService {
    public static final String LOGNAME = "InstrumentService";

    public InstrumentService(SapphireConnection sapphireConnection) {
        super(sapphireConnection);
        this.logName = LOGNAME;
    }

    public HashMap executeCommand(PropertyList commandProps) throws ServiceException {
        ErrorHandler errorHandler = new ErrorHandler();
        String traceLogIdStr = "";
        QueryService queryService = new QueryService(this.sapphireConnection);
        String commandid = commandProps.getProperty("commandid");
        String instrumentid = this.getInstrumentId(commandProps, queryService);
        String host = commandProps.getProperty("hostname");
        String port = commandProps.getProperty("hostport");
        String commandcode = commandProps.getProperty("commandcode");
        String responseTerminationString = commandProps.getProperty("responseterminationstring");
        String instrumentmodelid = commandProps.getProperty("instrumentmodelid");
        String instrumenttypeid = commandProps.getProperty("instrumenttypeid");
        String driverClass = "";
        String response = "";
        String returnflag = "Y";
        boolean isStopPoll = false;
        if ("stoppoll".equals(commandid)) {
            isStopPoll = true;
            commandid = "";
        }
        if (instrumentid != null && instrumentid.length() > 0) {
            String instrumentSQL = "SELECT i.hostname, i.hostport, i.instrumenttype, i.instrumentmodelid, m.timeout, c.commandcode, c.returnflag, c.responseparsingrule, c.responseexample, c.responseterminationstring  FROM instrument i, instrumentmodel m, instrumentmodelcommand c WHERE i.instrumentmodelid = m.instrumentmodelid AND m.instrumentmodelid=c.instrumentmodelid AND i.instrumentid=? " + (commandid.length() > 0 ? "AND c.commandid=? " : "AND c.defaultcommandflag=?");
            DataSet ds = new QueryService(this.sapphireConnection).getPreparedSqlDataSet(instrumentSQL, new Object[]{instrumentid, commandid.length() > 0 ? commandid : "Y"}, true);
            instrumentmodelid = ds.getValue(0, "instrumentmodelid");
            instrumenttypeid = ds.getValue(0, "instrumenttype");
            host = host.length() == 0 ? ds.getValue(0, "hostname") : host;
            port = port.length() == 0 ? ds.getValue(0, "hostport") : port;
            returnflag = ds.getValue(0, "returnflag");
            response = ds.getValue(0, "responseexample");
            String responseParsingRule = ds.getValue(0, "responseparsingrule");
            responseTerminationString = ds.getValue(0, "responseterminationstring");
            commandcode = ds.getValue(0, "commandcode");
            String timeout = ds.getValue(0, "timeout");
            commandcode = GenericTextParser.unescapeASCIIChars(commandcode, "{", "}");
            commandcode = GenericTextParser.unescapeASCIIChars(commandcode, "<", ">");
            responseTerminationString = GenericTextParser.unescapeASCIIChars(responseTerminationString, "{", "}");
            responseTerminationString = GenericTextParser.unescapeASCIIChars(responseTerminationString, "<", ">");
            commandProps.put("host", host);
            commandProps.put("port", port);
            commandProps.put("timeout", timeout);
            commandProps.put("parsingrule", responseParsingRule);
            commandProps.put("commandcode", commandcode);
            commandProps.put("responseterminationstring", responseTerminationString);
            commandProps.put("returnflag", returnflag);
        } else {
            commandProps.put("host", host);
            commandProps.put("port", port);
            commandcode = GenericTextParser.unescapeASCIIChars(commandcode, "{", "}");
            commandcode = GenericTextParser.unescapeASCIIChars(commandcode, "<", ">");
            responseTerminationString = GenericTextParser.unescapeASCIIChars(responseTerminationString, "{", "}");
            responseTerminationString = GenericTextParser.unescapeASCIIChars(responseTerminationString, "<", ">");
            commandProps.put("commandcode", commandcode);
            commandProps.put("responseterminationstring", responseTerminationString);
            commandProps.put("returnflag", returnflag);
        }
        BaseInstrumentProvider instrument = null;
        SafeSQL safeSQL = new SafeSQL();
        String sql = "select protocolproviderclass from instrumentprotocolprovider where instrumentprotocolproviderid in (select instrumentprotocolproviderid from instrumentmodel where instrumentmodelid=" + safeSQL.addVar(instrumentmodelid) + " and instrumenttypeid=" + safeSQL.addVar(instrumenttypeid) + ")";
        driverClass = new QueryService(this.sapphireConnection).getPreparedSqlDataSet(sql, safeSQL.getValues()).getValue(0, "protocolproviderclass");
        if (driverClass.length() > 0) {
            try {
                this.logInfo("Use instrument protocol provider class " + driverClass);
                Class<?> c = Class.forName(driverClass);
                instrument = (BaseInstrumentProvider)c.newInstance();
            }
            catch (Exception e) {
                throw new ServiceException(e.getMessage());
            }
        } else {
            this.logInfo("Use default instrument protocol provider class ");
            instrument = new BaseInstrumentProvider();
        }
        instrument.setConnectionId(this.sapphireConnection.getConnectionId());
        HashMap<String, Object> returnProps = new HashMap<String, Object>();
        try {
            if (isStopPoll) {
                this.logInfo("Cancelling command " + commandProps);
                instrument.cancelCommand(commandProps);
                response = "";
            } else if (host.length() > 0 || driverClass.length() > 0) {
                this.logInfo("Sending command " + commandProps);
                response = instrument.executeCommand(commandProps);
                String dataitemkey = commandProps.getProperty("dataitemkey");
                if (instrumentid.length() > 0) {
                    String[] keys = StringUtil.split(dataitemkey, ";");
                    PropertyList props = new PropertyList();
                    props.setProperty("instrumentid", instrumentid);
                    if (keys.length >= 7) {
                        props.setProperty("paramlistid", keys[4]);
                        props.setProperty("versionid", keys[5]);
                        props.setProperty("variant", keys[6]);
                    }
                    props.setProperty("usagetype", "Simple Instrument Reading");
                    this.getActionProcessor().processAction("InstrumentUsed", "1", props);
                }
                this.logInfo("Received response " + response);
            } else {
                this.logInfo("No host specified for " + instrumentid + " use example response:" + response);
            }
            if (!isStopPoll && ("Y".equals(returnflag) || "P".equals(returnflag)) && response != null && response.length() > 0) {
                try {
                    returnProps.put("result", instrument.parseResponse(response, commandProps));
                }
                catch (Exception e) {
                    throw new ServiceException("Failed to parse response");
                }
            } else {
                returnProps.put("result", new HashMap());
            }
            returnProps.put("instrumentid", instrumentid);
            returnProps.put("commandid", commandid);
            returnProps.put("response", response);
            this.logInfo("Done parsing response " + returnProps);
        }
        catch (Exception e) {
            throw new ServiceException(e);
        }
        catch (Throwable t) {
            throw new ServiceException(t.getMessage());
        }
        return returnProps;
    }

    private String getInstrumentId(PropertyList commandProps, QueryService queryService) throws ServiceException {
        String dataitemkey;
        String instrumentid = commandProps.getProperty("instrumentid");
        if (instrumentid.length() == 0 && (dataitemkey = commandProps.getProperty("dataitemkey")).length() > 0) {
            String[] keys = StringUtil.split(dataitemkey, ";");
            DataSet ds = queryService.getPreparedSqlDataSet("SELECT s_instrumentid FROM sdidata WHERE sdcid=? AND keyid1=? AND keyid2=? AND keyid3=? AND paramlistid=? AND paramlistversionid=? AND variantid=? AND dataset=?", new Object[]{keys[0], keys[1], keys[2], keys[3], keys[4], keys[5], keys[6], keys[7]});
            instrumentid = ds.getValue(0, "s_instrumentid");
        }
        return instrumentid;
    }
}

