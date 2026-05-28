/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletOutputStream
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.report.nwa;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.report.SapphireReportEvent;
import com.labvantage.sapphire.report.nwa.NWAPolicy;
import com.labvantage.sapphire.report.nwa.NWGHandler;
import com.labvantage.sapphire.xml.SaxUtil;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SapphireNWAUtil {
    private ConnectionInfo connectionInfo;
    private PropertyList qawsServerProps;
    private ActionProcessor actionProcessor;
    private PropertyList stats = null;
    private NWAPolicy policyDef;

    public SapphireNWAUtil(ConnectionInfo connectionInfo) throws SapphireException {
        this.connectionInfo = connectionInfo;
        String node = "Sapphire Custom";
        PropertyList policy = new ConfigurationProcessor(connectionInfo.getConnectionId()).getPolicy("NWAPolicy", node);
        if (policy == null) {
            throw new SapphireException("Failed to get NWA policy");
        }
        this.policyDef = new NWAPolicy(policy);
        this.qawsServerProps = this.policyDef.getQAWSServerProps();
        this.actionProcessor = new ActionProcessor(connectionInfo.getConnectionId());
    }

    public byte[] runReport(HashMap paramMap, DataSet paramds, String location, HttpServletResponse response) throws SapphireException {
        try {
            String url = this.buildNWAUrl(paramMap, paramds, location);
            Trace.log("URL to connect to QAWS is:" + url);
            byte[] image = this.downloadURL(url);
            response.setContentLength(image.length);
            response.setContentType("image/png");
            ServletOutputStream ouputStream = response.getOutputStream();
            ouputStream.write(image, 0, image.length);
            ouputStream.flush();
            ouputStream.close();
            return image;
        }
        catch (MalformedURLException e) {
            throw new SapphireException("Malformed URL. Check report configuration");
        }
        catch (ProtocolException e) {
            throw new SapphireException("ProtocolException:" + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())));
        }
        catch (IOException e) {
            throw new SapphireException("IOException:" + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())));
        }
    }

    public byte[] sendToFile(HashMap paramMap, DataSet paramds, String location, String filename) throws SapphireException {
        try {
            String url = this.buildNWAUrl(paramMap, paramds, location);
            byte[] image = this.downloadURL(url);
            FileOutputStream outputStream = new FileOutputStream(filename);
            outputStream.write(image, 0, image.length);
            outputStream.flush();
            outputStream.close();
            return image;
        }
        catch (MalformedURLException e) {
            throw new SapphireException("Malformed URL. Check report configuration");
        }
        catch (ProtocolException e) {
            throw new SapphireException("ProtocolException:" + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())));
        }
        catch (IOException e) {
            throw new SapphireException("IOException:" + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())));
        }
    }

    public PropertyList getStats() {
        return this.stats;
    }

    private void saveDataSetAsCsv(String filename) {
        try {
            String data = FileUtil.getFileString(new File(this.getReportFolder() + "/" + filename));
            String replacedelimiter = data.replaceAll(" ", ",");
            String csvfilename = this.getReportFolder() + "/" + filename.replace("DAT", "csv");
            FileOutputStream fos = new FileOutputStream(csvfilename);
            fos.write(replacedelimiter.getBytes());
            fos.flush();
            fos.close();
        }
        catch (IOException e) {
            Trace.logError("Failed to save the dat file as csv:" + filename, e);
        }
    }

    public PropertyListCollection downloadGeneratedFiles(String reference, SapphireReportEvent event) throws SapphireException {
        String reportfolder = this.getReportFolder();
        PropertyListCollection generatedFiles = this.policyDef.getGeneratedFileNames(reference, event.getReporteventid());
        if (generatedFiles != null) {
            try {
                for (int i = 0; i < generatedFiles.size(); ++i) {
                    String filename = generatedFiles.getPropertyList(i).getProperty("filename", "");
                    if (filename.length() <= 0) continue;
                    String url = this.buildFileUrl(filename);
                    byte[] currBytes = this.downloadURL(url);
                    String filepath = reportfolder + "/" + filename;
                    FileOutputStream out = new FileOutputStream(filepath);
                    out.write(currBytes);
                    out.close();
                    if (filename.toLowerCase().indexOf(".nwg") > 0) {
                        this.stats = this.parseStats(filepath, this.connectionInfo.getConnectionId());
                        continue;
                    }
                    if (filename.toLowerCase().indexOf(".dat") <= 0 || !generatedFiles.getPropertyList(i).getProperty("saveascsv", "").equals("Y")) continue;
                    this.saveDataSetAsCsv(filename);
                }
            }
            catch (IOException e) {
                throw new SapphireException("Failed to read the generated file in the specified report folder", e);
            }
        }
        return generatedFiles;
    }

    private byte[] downloadURL(String url) throws SapphireException {
        try {
            URL nwaURL = new URL(url);
            HttpURLConnection conn = (HttpURLConnection)nwaURL.openConnection();
            conn.setRequestMethod("POST");
            conn.connect();
            InputStream in = conn.getInputStream();
            ArrayList<Byte> byteList = new ArrayList<Byte>();
            byte[] buffer = new byte[1];
            while (in.read(buffer) != -1) {
                byteList.add(new Byte(buffer[0]));
            }
            byte[] allBytes = new byte[byteList.size()];
            for (int i = 0; i < byteList.size(); ++i) {
                allBytes[i] = (Byte)byteList.get(i);
            }
            conn.disconnect();
            return allBytes;
        }
        catch (MalformedURLException e) {
            throw new SapphireException("Malformed URL. Check report configuration");
        }
        catch (ProtocolException e) {
            throw new SapphireException("ProtocolException:" + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())));
        }
        catch (IOException e) {
            throw new SapphireException("IOException:" + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())));
        }
    }

    private String replacePreviousParams(String paramVal, int index, DataSet paramds, HashMap paramMap) {
        if (paramVal.indexOf("[") > -1 && paramVal.indexOf("]") > 0) {
            for (int i = 0; i < index; ++i) {
                Object substituteVal;
                String pattern = "[" + paramds.getString(i, "paramid") + "]";
                if (paramVal.indexOf(pattern) <= -1 || (substituteVal = paramMap.get(paramds.getString(i, "paramid"))) == null) continue;
                paramVal = paramVal.replace(pattern, substituteVal.toString());
            }
        }
        return paramVal;
    }

    public String buildNWAUrl(HashMap paramMap, DataSet paramds, String reference) {
        String url = "http://";
        url = url + this.getServerName();
        url = url + ":" + this.getPortNum() + "/";
        url = url + reference + ".png";
        String paramStr = "";
        for (int i = 0; i < paramds.getRowCount(); ++i) {
            String paramKey = paramds.getString(i, "paramid");
            if (paramMap.get(paramKey) == null) continue;
            String paramVal = paramMap.get(paramKey).toString();
            paramVal = this.replacePreviousParams(paramVal, i, paramds, paramMap);
            if (paramKey.equals("keyid1")) {
                String[] keyid1List = StringUtil.split(paramVal, ";");
                String formatKeyList = "";
                for (int j = 0; j < keyid1List.length; ++j) {
                    if (formatKeyList.length() > 0) {
                        formatKeyList = formatKeyList + ",";
                    }
                    formatKeyList = formatKeyList + "'" + keyid1List[j] + "'";
                }
                paramVal = formatKeyList;
            }
            paramStr = paramStr.length() == 0 ? paramStr + "?" : paramStr + "&";
            paramVal = HttpUtil.encodeURIComponent(paramVal, "Windows-1252");
            paramStr = paramStr + "^" + paramKey + "=" + paramVal;
        }
        paramStr = paramStr + "&WIDTH=1000&HEIGHT=600";
        if (paramMap.get("SAPPHIRE_ReportEventID") != null) {
            if (paramStr != null && paramStr.length() > 0) {
                paramStr = paramStr + "&";
            }
            paramStr = paramStr + "^reporteventid=" + paramMap.get("SAPPHIRE_ReportEventID");
        }
        return url + paramStr;
    }

    public String buildFileUrl(String filename) {
        String url = "http://";
        url = url + this.getServerName();
        url = url + ":" + this.getPageServerPortNum() + "/";
        url = url + this.getPageServerLocation() + "/";
        url = url + filename;
        return url;
    }

    private String getServerName() {
        return this.qawsServerProps.getProperty("QAWSServerName");
    }

    private String getPortNum() {
        return this.qawsServerProps.getProperty("QAWSPortNum");
    }

    private String getReportFolder() {
        return this.policyDef.getReportFolder();
    }

    private String getPageServerPortNum() {
        return this.qawsServerProps.getProperty("PageServerPortNum");
    }

    private String getPageServerLocation() {
        return this.qawsServerProps.getProperty("PageServerLocation");
    }

    private String getReportEventId(HashMap paramMap) {
        if (paramMap.get("SAPPHIRE_ReportEventID") != null) {
            return paramMap.get("SAPPHIRE_ReportEventID").toString();
        }
        return "";
    }

    private String getExt(String filename) {
        return filename.substring(filename.indexOf("."));
    }

    public void addToReportEvent(String reporteventid, PropertyListCollection generatedFiles) throws SapphireException {
        for (int i = 0; i < generatedFiles.size(); ++i) {
            PropertyList fileProps = generatedFiles.getPropertyList(i);
            String filename = fileProps.getProperty("filename", "");
            if (filename.length() <= 0) continue;
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "ReportEvent");
            props.setProperty("keyid1", reporteventid);
            props.setProperty("filename", this.getReportFolder() + "/" + filename);
            props.setProperty("description", fileProps.getProperty("description", ""));
            props.setProperty("type", "R");
            this.actionProcessor.processAction("AddSDIAttachment", "1", props);
            if (!this.getExt(filename).equalsIgnoreCase(".DAT") || !generatedFiles.getPropertyList(i).getProperty("saveascsv", "").equals("Y")) continue;
            PropertyList props2 = new PropertyList();
            props2.setProperty("sdcid", "ReportEvent");
            props2.setProperty("keyid1", reporteventid);
            props2.setProperty("filename", this.getReportFolder() + "/" + filename.replace("DAT", "csv"));
            props2.setProperty("description", "(CSV Format)" + fileProps.getProperty("description", ""));
            props2.setProperty("type", "R");
            this.actionProcessor.processAction("AddSDIAttachment", "1", props2);
        }
    }

    public PropertyList parseStats(String nwgXmlFileName, String connectionid) throws SapphireException {
        NWGHandler handler = new NWGHandler();
        try {
            String nwgXml = FileUtil.getFileString(new File(nwgXmlFileName));
            handler.setConnectionid(connectionid);
            handler.setXMLString(nwgXml);
            SaxUtil.parseString(handler);
        }
        catch (ActionException e) {
            throw new SapphireException(ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(connectionid)));
        }
        catch (SapphireException e) {
            throw e;
        }
        catch (Exception e) {
            throw new SapphireException("Unexpected exception: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(connectionid)));
        }
        return handler.getStats();
    }
}

