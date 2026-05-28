/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.io.IOUtils
 */
package com.labvantage.sapphire.modules.sdms.collector.collectortypes;

import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.modules.sdms.SDMSUtil;
import com.labvantage.sapphire.modules.sdms.collector.storagemodes.BaseFileSender;
import com.labvantage.sapphire.modules.sdms.collector.storagemodes.FileSenderFactory;
import com.labvantage.sapphire.util.file.FileManager;
import com.labvantage.sapphire.util.file.FileTransfer;
import com.labvantage.sapphire.util.file.FileTransferOptions;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.io.IOUtils;
import sapphire.SapphireException;
import sapphire.ext.BaseCollectorType;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class NetworkCollectorType
extends BaseCollectorType {
    private final int TIMEOUT = 30000;
    boolean isCollectionEnabled = false;
    boolean isDeliveryEnbabled = false;
    boolean isEmulatorEnabled = false;
    private final String IDNAME_EMULATOR_ADDRESS = "emulatoraddress";
    private final String IDNAME_EMULATOR_PORT = "emulatorport";
    private final String IDNAME_FREQUENCY = "frequency";
    private final String IDNAME_RANDOMDELTA = "randomdelta";
    private final String IDNAME_STREMDATA = "streamdata";
    private final String IDNAME_ERRORS = "errors";
    private final String IDNAME_MODE = "mode";
    private final String IDNAME_ERRORTYPE = "errortype";
    private final String IDNAME_SIZE = "size";
    private final String IDNAME_TIME = "time";
    private final String IDNAME_STARTCHAR = "startchar";
    private final String IDNAME_ENDCHAR = "endchar";
    private final String IDNAME_DATACONTENT = "datacontent";
    private String emulatorAddress = "";
    private Integer emulatorPort;
    private int emulatorFrequency = 0;
    private int emulatorDelta = 0;
    private String emulatorStreamDataStartChar;
    private String emulatorStreamDataEndChar;
    private String emulatorDataContent = "";
    private ErrorType emulatorErrorType = ErrorType.NONE;
    private int emulatorErrorFrequency = 0;
    private final String IDNAME_COLLECTOR_ADDRESS = "collectoraddress";
    private final String IDNAME_COLLECTOR_PORT = "collectorport";
    private final String CONTINUOUS_ATTACHMENT_TIME = "Time";
    private final String CONTINUOUS_ATTACHMENT_SIZE = "Size";
    private String collectorAddress = "";
    private Integer collectorPort;
    private Mode collectormode = Mode.CONTINUOUS;
    private long continuousMaxCaptureTime = 0L;
    private ContinuousAttachmentMode continuousCreateAttachment = ContinuousAttachmentMode.SIZE;
    private long continuousCreateAttachmentTime_sec = 0L;
    private int continuousCreateAttachmentSize_byte = 0;
    private String trimResult = "";
    private String excludeStartTrigger = "";
    private String excludeEndTrigger = "";
    private String filename = "";
    private List<String> tempFilePaths = new ArrayList<String>();
    private List<String> finalFilePaths = new ArrayList<String>();
    private Integer attachmentCount = 1;
    private Path workarea = null;
    private String discreteStartTrigger = "";
    private DiscreteEndTrigger discreteEndTrigger = DiscreteEndTrigger.CHAR;
    private String discreteEndChar = "";
    private int discreteEndDuration = 0;
    private int discreteEndSize = 0;
    private int discreteEndSilence = 0;
    private String attachmentClass = "";
    String lastStoreDescription = "";
    Path lastTriggerFile = null;
    Calendar lastTriggerDt = null;
    private boolean isDeliveryRunFileName = false;
    private static List<Socket> socketList = new ArrayList<Socket>();

    public List<String> getTempFilePaths() {
        return this.tempFilePaths;
    }

    @Override
    public void configure(PropertyList collectorTypeProps) throws SapphireException {
        this.isCollectionEnabled = collectorTypeProps.getProperty("enablecollection").equals("Y");
        this.isDeliveryEnbabled = collectorTypeProps.getProperty("enablerunfiledelivery").equals("Y");
        this.isEmulatorEnabled = collectorTypeProps.getProperty("enableemulator").equals("Y");
        String msg = "";
        if (this.isCollectionEnabled) {
            PropertyList collectorProps = collectorTypeProps.getPropertyListNotNull("collectorprops");
            PropertyList triggerProps = collectorProps.getPropertyListNotNull("trigger");
            PropertyList collectProps = collectorProps.getPropertyListNotNull("collect");
            this.collectorAddress = collectorProps.getProperty("collectoraddress", "");
            try {
                this.collectorPort = Integer.parseInt(collectorProps.getProperty("collectorport", ""));
            }
            catch (Exception e) {
                this.collectorPort = 0;
                msg = "Collector port should be numeric";
                this.raiseInstrumentAlert("SDMS Startup", "Failure", "Failed to configure Collector", msg, false, true);
            }
            if (this.collectorAddress.trim().length() == 0 || this.collectorPort == 0) {
                msg = "Invalid collector port or address";
                this.raiseInstrumentAlert("SDMS Startup", "Failure", "Failed to configure Collector", msg, false, true);
            }
            try {
                this.collectormode = Mode.valueOf(triggerProps.getProperty("collectmode", Mode.CONTINUOUS.toString()).toUpperCase());
            }
            catch (Exception e) {
                msg = "Invalid collector mode";
                this.raiseInstrumentAlert("SDMS Startup", "Failure", "Failed to configure Collector", msg, false, true);
            }
            try {
                this.continuousMaxCaptureTime = Long.parseLong(triggerProps.getProperty("continuousmaxcapturetime", "10"));
            }
            catch (Exception e) {
                msg = "Invalid max capture time";
                this.raiseInstrumentAlert("SDMS Startup", "Failure", "Failed to configure Collector", msg, false, true);
            }
            try {
                this.continuousCreateAttachment = ContinuousAttachmentMode.valueOf(triggerProps.getProperty("createattachment", ContinuousAttachmentMode.SIZE.toString()).toUpperCase());
            }
            catch (Exception e) {
                msg = "Invalid continuous mode";
                this.raiseInstrumentAlert("SDMS Startup", "Failure", "Failed to configure Collector", msg, false, true);
            }
            this.continuousCreateAttachmentTime_sec = Long.parseLong(triggerProps.getProperty("createattachmenttime", "30"));
            this.continuousCreateAttachmentSize_byte = Integer.parseInt(triggerProps.getProperty("createattachmentsize", "1024"));
            if (this.collectormode == Mode.DISCRETE) {
                this.discreteStartTrigger = triggerProps.getProperty("discretestart", "");
                if (this.discreteStartTrigger.trim().isEmpty()) {
                    msg = "No discrete start character (s) provided";
                    this.raiseInstrumentAlert("SDMS Startup", "Failure", "Failed to configure Collector", msg, false, true);
                }
                try {
                    this.discreteEndTrigger = DiscreteEndTrigger.valueOf(triggerProps.getProperty("discreteend", "Char").toUpperCase());
                }
                catch (Exception e) {
                    msg = "Invalid discrete end trigger";
                    this.raiseInstrumentAlert("SDMS Startup", "Failure", "Failed to configure Collector", msg, false, true);
                }
                switch (this.discreteEndTrigger) {
                    case CHAR: {
                        this.discreteEndChar = triggerProps.getProperty("discreteendchar", "");
                        if (!this.discreteEndChar.trim().isEmpty()) break;
                        msg = "No discrete end character (s) provided";
                        this.raiseInstrumentAlert("SDMS Startup", "Failure", "Failed to configure Collector", msg, false, true);
                        break;
                    }
                    case DURATION: {
                        try {
                            this.discreteEndDuration = Integer.parseInt(triggerProps.getProperty("discreteendduration", "30"));
                        }
                        catch (Exception e) {
                            msg = "Invalid end duration provided";
                            this.raiseInstrumentAlert("SDMS Startup", "Failure", "Failed to configure Collector", msg, false, true);
                        }
                        break;
                    }
                    case SIZE: {
                        try {
                            this.discreteEndSize = Integer.parseInt(triggerProps.getProperty("discreteendsize", "1024"));
                        }
                        catch (Exception e) {
                            msg = "Invalid end size provided";
                            this.raiseInstrumentAlert("SDMS Startup", "Failure", "Failed to configure Collector", msg, false, true);
                        }
                        break;
                    }
                    case SILENCE: {
                        try {
                            this.discreteEndSilence = Integer.parseInt(triggerProps.getProperty("discreteendsilence", "0"));
                            break;
                        }
                        catch (Exception e) {
                            msg = "Invalid end silence provided";
                            this.raiseInstrumentAlert("SDMS Startup", "Failure", "Failed to configure Collector", msg, false, true);
                        }
                    }
                }
            }
            this.trimResult = collectProps.getProperty("trimresult", "N");
            this.excludeStartTrigger = collectProps.getProperty("excludestarttrigger", "N");
            this.excludeEndTrigger = collectProps.getProperty("excludeendtrigger", "N");
            this.filename = collectProps.getProperty("filename", "");
            if (this.filename.length() == 0) {
                if (this.isDeliveryEnbabled) {
                    this.isDeliveryRunFileName = true;
                }
                this.filename = "networkcollection.txt";
            }
            this.attachmentClass = collectProps.getProperty("attachmentclass", "");
            this.logStartup("Acting as client on IP=" + this.collectorAddress + " and Port=" + this.collectorPort);
        }
        if (this.isEmulatorEnabled) {
            PropertyList emulatorProps = collectorTypeProps.getPropertyListNotNull("emulatorprops");
            PropertyList collectorProps = collectorTypeProps.getPropertyListNotNull("collectorprops");
            this.emulatorAddress = collectorProps.getProperty("collectoraddress", "");
            try {
                this.emulatorPort = Integer.parseInt(collectorProps.getProperty("collectorport", ""));
            }
            catch (Exception e) {
                this.emulatorPort = 0;
                msg = "Collector port should be numeric";
                this.raiseInstrumentAlert("SDMS Startup", "Failure", "Failed to configure Collector", msg, false, true);
            }
            if (this.emulatorAddress.trim().length() == 0 || this.emulatorPort == 0) {
                msg = "Invalid collector port or address";
                this.raiseInstrumentAlert("SDMS Startup", "Failure", "Failed to configure Collector", msg, false, true);
            }
            try {
                this.emulatorFrequency = Integer.parseInt(emulatorProps.getProperty("frequency", "0"));
            }
            catch (Exception e) {
                msg = "Invalid emulator frequency";
                this.raiseInstrumentAlert("SDMS Startup", "Failure", "Failed to configure Collector", msg, false, true);
            }
            try {
                this.emulatorDelta = Integer.parseInt(emulatorProps.getProperty("randomdelta", "0"));
            }
            catch (Exception e) {
                msg = "Invalid emulator delta";
                this.raiseInstrumentAlert("SDMS Startup", "Failure", "Failed to configure Collector", msg, false, true);
            }
            PropertyList streamData = emulatorProps.getPropertyListNotNull("streamdata");
            this.emulatorStreamDataStartChar = streamData.getProperty("startchar", "");
            this.emulatorStreamDataEndChar = streamData.getProperty("endchar", "");
            this.emulatorDataContent = streamData.getProperty("datacontent", "");
            if (this.emulatorDataContent.length() == 0) {
                msg = "No emulator data provided";
                this.raiseInstrumentAlert("SDMS Startup", "Failure", "Failed to configure Collector", msg, false, true);
            }
            PropertyList errors = emulatorProps.getPropertyListNotNull("errors");
            try {
                this.emulatorErrorType = ErrorType.valueOf(errors.getProperty("errortype", ErrorType.NONE.toString()).toUpperCase());
            }
            catch (Exception e) {
                msg = "Invalid emulator error type";
                this.raiseInstrumentAlert("SDMS Startup", "Failure", "Failed to configure Collector", msg, false, true);
            }
            this.emulatorErrorFrequency = 0;
            try {
                this.emulatorErrorFrequency = Integer.parseInt(errors.getProperty("frequency", "0"));
            }
            catch (Exception e) {
                msg = "Invalid emulator error frequency";
                this.raiseInstrumentAlert("SDMS Startup", "Failure", "Failed to configure Collector", msg, false, true);
            }
            this.logStartup("Acting as server on IP=" + this.emulatorAddress + " and Port=" + this.emulatorPort);
        }
    }

    @Override
    public int getCollectionPollInterval() {
        return -1;
    }

    @Override
    public int getEmulatorPollInterval() {
        return -1;
    }

    @Override
    public boolean isCollectionEnabled() {
        return this.isCollectionEnabled;
    }

    @Override
    public boolean isContinuousOperation() {
        return true;
    }

    @Override
    public boolean isRunfileDeliveryEnabled() {
        return this.isDeliveryEnbabled;
    }

    @Override
    public boolean isEmulatorEnabled() {
        return this.isEmulatorEnabled;
    }

    @Override
    public boolean doRunCollector(FileSenderFactory fileSenderFactory) throws Exception {
        this.recieveData(fileSenderFactory);
        return true;
    }

    @Override
    public boolean doRunEmulator() throws SapphireException {
        try {
            this.sendData();
        }
        catch (Exception e) {
            String msg = e.getMessage();
            this.raiseInstrumentAlert("SDMS Startup", "Failure", "Failed to run network emulator", msg, false, true);
        }
        return true;
    }

    public Path getSandboxArea() {
        return this.workarea;
    }

    public void setSandboxArea(Path workarea) {
        this.workarea = workarea;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void sendData() throws SapphireException {
        block73: {
            this.appendEmulatorLog("Configured to emulate address " + this.collectorAddress + " and port " + this.collectorPort + ".");
            int errorFreq = this.emulatorErrorFrequency * 1000;
            try {
                ArrayList<byte[]> dataArray;
                block71: {
                    dataArray = new ArrayList<byte[]>();
                    if (this.emulatorDataContent.length() > 0) {
                        try {
                            if (this.emulatorDataContent.startsWith("data:") && this.emulatorDataContent.contains(";base64,")) {
                                FileManager.FileData fileData = new FileManager.FileData(this.emulatorDataContent);
                                dataArray.add(fileData.getData());
                                this.appendEmulatorLog("Configured with base64 data.");
                                break block71;
                            }
                            if (!this.emulatorDataContent.contains(";") && this.emulatorDataContent.contains(File.separator) && Files.exists(Paths.get(this.emulatorDataContent, new String[0]), new LinkOption[0])) {
                                try (InputStream is = Files.newInputStream(Paths.get(this.emulatorDataContent, new String[0]), new OpenOption[0]);
                                     ByteArrayOutputStream bos = new ByteArrayOutputStream();){
                                    FileTransfer.safeDataTransfer(is, bos, new FileTransferOptions());
                                    dataArray.add(bos.toByteArray());
                                    this.appendEmulatorLog("Configured with file data.");
                                    break block71;
                                }
                                catch (Exception e) {
                                    this.raiseInstrumentAlert("SDMS Emulator", "Failure", "Invalid file for emulation data", e.getMessage(), false, true);
                                }
                                break block71;
                            }
                            String[] dataStrings = StringUtil.split(this.emulatorDataContent, ";");
                            for (String dataString : dataStrings) {
                                byte[] data = dataString.getBytes();
                                dataArray.add(data);
                            }
                            this.appendEmulatorLog("Configured with " + dataStrings.length + " data string(s).");
                        }
                        catch (Exception e) {
                            this.raiseInstrumentAlert("SDMS Emulator", "Failure", "Invalid emulation data", e.getMessage(), false, true);
                        }
                    }
                }
                if (dataArray.size() > 0) {
                    try (ServerSocket serverSocket = new ServerSocket();){
                        InetAddress addr = InetAddress.getByName(this.emulatorAddress);
                        InetSocketAddress sockaddr = new InetSocketAddress(addr, (int)this.emulatorPort);
                        serverSocket.setReuseAddress(true);
                        serverSocket.bind(sockaddr);
                        Socket socket = null;
                        OutputStream out = null;
                        try {
                            int dataPoint = 0;
                            long errorTimePoint = System.currentTimeMillis();
                            boolean errorMode = false;
                            while (!(this.sdmsCollector.isDisabled() || this.sdmsCollector.isPaused() || this.isInstrumentPaused() || this.sdmsCollector.isShuttingDownCollectors() || this.isShuttingDown())) {
                                try {
                                    if (serverSocket.isClosed()) {
                                        if (socket != null) {
                                            try {
                                                socket.close();
                                            }
                                            catch (Exception exception) {
                                                // empty catch block
                                            }
                                            socket = null;
                                        }
                                        serverSocket = new ServerSocket();
                                        serverSocket.setReuseAddress(true);
                                        serverSocket.bind(sockaddr);
                                    }
                                    if (socket == null) {
                                        socket = serverSocket.accept();
                                        socket.setSoTimeout(0);
                                        socket.setKeepAlive(true);
                                    } else if (socket.isClosed()) {
                                        socket = serverSocket.accept();
                                        socket.setSoTimeout(0);
                                        socket.setKeepAlive(true);
                                    }
                                    if (out == null) {
                                        out = socket.getOutputStream();
                                    }
                                    if (errorMode) {
                                        errorTimePoint = System.currentTimeMillis();
                                        errorMode = false;
                                    }
                                    ByteArrayInputStream bis = null;
                                    if (this.emulatorStreamDataStartChar.length() == 0 && this.emulatorStreamDataEndChar.length() == 0) {
                                        bis = new ByteArrayInputStream((byte[])dataArray.get(dataPoint));
                                    } else {
                                        String data = new String((byte[])dataArray.get(dataPoint));
                                        if (this.emulatorStreamDataStartChar.length() > 0) {
                                            data = this.emulatorStreamDataStartChar + data;
                                        }
                                        if (this.emulatorStreamDataEndChar.length() > 0) {
                                            data = data + this.emulatorStreamDataEndChar;
                                        }
                                        bis = new ByteArrayInputStream(data.getBytes());
                                    }
                                    try {
                                        int b = bis.read();
                                        while (!(errorMode || this.sdmsCollector.isDisabled() || this.sdmsCollector.isPaused() || this.isInstrumentPaused() || this.sdmsCollector.isShuttingDownCollectors() || this.isShuttingDown() || b <= -1)) {
                                            out.write(b);
                                            b = bis.read();
                                            if (this.emulatorErrorType != ErrorType.CLOSE && this.emulatorErrorType != ErrorType.INTERRUPT || System.currentTimeMillis() < errorTimePoint + (long)errorFreq) continue;
                                            switch (this.emulatorErrorType) {
                                                case CLOSE: {
                                                    this.appendEmulatorLog("Close socket error test triggered.");
                                                    try {
                                                        out.flush();
                                                        out.close();
                                                    }
                                                    catch (Exception exception) {
                                                        // empty catch block
                                                    }
                                                    out = null;
                                                    try {
                                                        socket.close();
                                                    }
                                                    catch (Exception exception) {
                                                        // empty catch block
                                                    }
                                                    socket = null;
                                                    try {
                                                        serverSocket.close();
                                                    }
                                                    catch (Exception exception) {}
                                                    break;
                                                }
                                                case INTERRUPT: {
                                                    this.appendEmulatorLog("Interrupt output stream error test triggered.");
                                                    try {
                                                        out.flush();
                                                        out.close();
                                                    }
                                                    catch (Exception exception) {
                                                        // empty catch block
                                                    }
                                                    out = null;
                                                }
                                            }
                                            errorMode = true;
                                        }
                                    }
                                    finally {
                                        bis.close();
                                        bis = null;
                                    }
                                    if (out != null && !errorMode) {
                                        out.flush();
                                    }
                                    if (dataArray.size() > 1 && !errorMode && ++dataPoint >= dataArray.size()) {
                                        dataPoint = 0;
                                    }
                                }
                                catch (SocketException e1) {
                                    this.raiseInstrumentAlert("SDMS Emulator", "Warning", "Socket connection interrupt", "Could not send continuous data from server socket", false, true);
                                }
                                catch (Exception e2) {
                                    this.raiseInstrumentAlert("SDMS Emulator", "Failure", "Failed to send data", "Could not send continuous data from server socket", false, true);
                                }
                                int eFreq = this.emulatorFrequency;
                                if (this.emulatorDelta > 0) {
                                    int maximum = this.emulatorFrequency + this.emulatorDelta;
                                    int minimum = this.emulatorFrequency - this.emulatorDelta;
                                    eFreq = SDMSUtil.getRandomInteger(maximum, minimum);
                                }
                                if (eFreq <= 0) continue;
                                try {
                                    Thread.sleep((eFreq < 1 ? 1 : eFreq) * 1000);
                                }
                                catch (Exception exception) {}
                            }
                            break block73;
                        }
                        finally {
                            if (out != null) {
                                try {
                                    out.close();
                                }
                                catch (Exception exception) {}
                            }
                            if (socket != null && !socket.isClosed()) {
                                try {
                                    socket.close();
                                }
                                catch (Exception exception) {}
                            }
                        }
                    }
                }
                this.raiseInstrumentAlert("SDMS Emulator", "Failure", "Failed to run Network Emulator", "No emulator data to stream could be obtained", false, true);
            }
            catch (IOException e) {
                String msg = e.getMessage();
                this.raiseInstrumentAlert("SDMS Emulator", "Failure", "Failed to run Network Emulator", msg, false, true);
            }
        }
    }

    @Override
    public List<String> getReportsForSDC(PropertyList collectorTypeProps, String sdcid) {
        ArrayList<String> reports = new ArrayList<String>();
        PropertyList deliveryProps = collectorTypeProps.getPropertyListNotNull("deliveryprops");
        String reportid = deliveryProps.getProperty("reportid");
        if (reportid.length() > 0) {
            reports.add(reportid + ";C");
        }
        return reports;
    }

    @Override
    public Calendar getLastCaptureDt() {
        return this.lastTriggerDt;
    }

    @Override
    public String getLastCaptureDescription() {
        return this.lastTriggerFile == null ? "None" : this.lastTriggerFile.toFile().getName();
    }

    @Override
    public String getLastStoreDescription() {
        return this.lastStoreDescription == null ? "None" : this.lastStoreDescription;
    }

    private void streamDataForGivenTime(InputStream is, OutputStream out, int timeoutMillis) throws IOException {
        int len;
        long maxTimeMillis = System.currentTimeMillis() + (long)timeoutMillis;
        byte[] buffer = new byte[1024];
        while (System.currentTimeMillis() < maxTimeMillis && (len = is.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
    }

    private void streamDataWithStartAndEndChar(InputStream is, OutputStream out) throws IOException {
        boolean foundStartingChars = false;
        StringBuilder startChars = new StringBuilder("");
        StringBuilder endChars = new StringBuilder("");
        int lenChar = 0;
        while ((lenChar = is.read()) != -1) {
            if (!foundStartingChars) {
                foundStartingChars = this.checkForStartChars(startChars, lenChar, this.emulatorStreamDataStartChar);
                if (!foundStartingChars) continue;
                for (int i = 0; i < startChars.length(); ++i) {
                    out.write(startChars.charAt(i));
                }
                continue;
            }
            out.write((char)lenChar);
            if (!this.checkForEndChars(endChars, lenChar, this.emulatorStreamDataEndChar)) continue;
            startChars = new StringBuilder("");
            endChars = new StringBuilder("");
            foundStartingChars = false;
            break;
        }
    }

    private void streamDataForGivenSize(InputStream is, OutputStream out, int sizeInKB) throws IOException {
        int len;
        byte[] buffer = new byte[1024];
        for (int readByte = 0; readByte < sizeInKB && (len = is.read(buffer)) != -1; ++readByte) {
            out.write(buffer, 0, len);
        }
    }

    private InputStream getStreamDataContent() throws IOException {
        InputStream inputStream = null;
        inputStream = this.isStreamDataFilePath() ? new FileInputStream(this.emulatorDataContent) : IOUtils.toInputStream((String)this.emulatorDataContent);
        return inputStream;
    }

    private boolean isStreamDataFilePath() {
        File file = new File(this.emulatorDataContent);
        return file.exists();
    }

    private void recieveData(FileSenderFactory fileSenderFactory) throws SapphireException {
        try {
            this.storeStreamData(fileSenderFactory);
        }
        catch (IOException e) {
            String msg = e.getMessage();
            this.raiseInstrumentAlert("SDMS Startup", "Failure", "Failed to run network collector", msg, false, true);
        }
        catch (Exception e) {
            String msg = e.getMessage();
            this.raiseInstrumentAlert("SDMS Startup", "Failure", "Failed to run network collector", msg, false, true);
        }
    }

    private FileOutputStream getFileOutputStream(Path tempworkspace, String filename) throws IOException {
        Calendar now = DateTimeUtil.getNowCalendar();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddhhmm");
        String name = "";
        String extension = "";
        if (filename.contains("[date]")) {
            filename = filename.replace("[date]", simpleDateFormat.format(now.getTime()));
        }
        if (filename.contains("[attachment_count]")) {
            Integer n = this.attachmentCount;
            Integer n2 = this.attachmentCount = Integer.valueOf(this.attachmentCount + 1);
            filename = filename.replace("[attachment_count]", Integer.toString(n));
        } else if (this.attachmentCount > -1) {
            name = FileUtil.getFileName(filename, false);
            extension = FileUtil.getExtension(filename);
            filename = name + this.attachmentCount + "." + extension;
        }
        name = FileUtil.getFileName(filename, false);
        extension = FileUtil.getExtension(filename);
        Path tempPath = Files.createFile(tempworkspace.resolve(filename), new FileAttribute[0]);
        String filePath = tempPath.toFile().getAbsolutePath();
        this.tempFilePaths.add(filePath);
        FileOutputStream fileOutputStream = new FileOutputStream(new File(filePath));
        return fileOutputStream;
    }

    private void storeStreamData(FileSenderFactory fileSenderFactory) throws Exception {
        this.appendCollectionLog("Configured to collect on address " + this.collectorAddress + " and port " + this.collectorPort + ".");
        try {
            switch (this.collectormode) {
                case CONTINUOUS: {
                    this.collectContinuously(fileSenderFactory);
                    break;
                }
                case DISCRETE: {
                    this.collectDiscrete(fileSenderFactory);
                }
            }
        }
        catch (Exception e) {
            String msg = e.getMessage();
            this.raiseInstrumentAlert("SDMS Startup", "Failure", "Failed to run the network Collector", msg, false, true);
        }
    }

    private boolean checkForStartChars(StringBuilder startChars, int data, String startCharTrigger) {
        char startChar = startCharTrigger.charAt(0);
        char endChar = startCharTrigger.charAt(startCharTrigger.length() - 1);
        if (data != 9 && data != 32) {
            char c = (char)data;
            if (startChar == c) {
                if (startChar == endChar && (startChars.length() == startCharTrigger.length() - 1 || startCharTrigger.length() == 1)) {
                    startChars.append(c);
                    return startChars.toString().equals(startCharTrigger);
                }
                if (!startCharTrigger.startsWith(startChars.toString())) {
                    startChars.delete(0, startChars.length());
                }
                startChars.append(c);
                return false;
            }
            startChars.append(c);
            if (!startCharTrigger.startsWith(startChars.toString())) {
                startChars.delete(0, startChars.length());
            }
            if (startChars.toString().equals(startCharTrigger)) {
                return true;
            }
        } else {
            startChars.delete(0, startChars.length());
        }
        return false;
    }

    private boolean checkForEndChars(StringBuilder endChars, int data, String discreteEndChar) {
        char startChar = discreteEndChar.charAt(0);
        char endChar = discreteEndChar.charAt(discreteEndChar.length() - 1);
        if (data != 9 && data != 32) {
            char c = (char)data;
            if (startChar == c && discreteEndChar.length() > 1) {
                if (startChar == endChar && endChars.length() == discreteEndChar.length() - 1) {
                    endChars.append(c);
                    return endChars.toString().equals(discreteEndChar);
                }
                if (!discreteEndChar.startsWith(endChars.toString())) {
                    endChars.delete(0, endChars.length());
                }
                endChars.append(c);
                return false;
            }
            if (startChar == c && discreteEndChar.length() == 1) {
                endChars.delete(0, endChars.length());
                endChars.append(c);
                return true;
            }
            endChars.append(c);
            if (endChars.toString().equals(discreteEndChar)) {
                return true;
            }
        } else {
            endChars.delete(0, endChars.length());
        }
        return false;
    }

    private Path getTempWorkarea() throws IOException {
        Path tempworkspace = null;
        tempworkspace = this.workarea != null ? Files.createDirectories(this.workarea.resolve("sandbox" + StringUtil.getRandomString(5)), new FileAttribute[0]) : FileUtil.createTempDirectory(null, false);
        return tempworkspace;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void collectDiscrete(FileSenderFactory fileSenderFactory) throws SapphireException, IOException {
        int maxBytesPerVolume = this.discreteEndSize;
        long maxMilliSecondsPerVolume = this.discreteEndDuration * 1000;
        long maxMilliSecondsSilence = this.discreteEndSilence * 1000;
        while (!(this.sdmsCollector.isDisabled() || this.sdmsCollector.isPaused() || this.isInstrumentPaused() || this.sdmsCollector.isShuttingDownCollectors() || this.isShuttingDown())) {
            Socket socket = null;
            try {
                socket = new Socket(this.collectorAddress, (int)this.collectorPort);
            }
            catch (Exception e1) {
                this.logger.log("COLLECTING", "Failed to create socket. Will try again in 10 seconds.");
                try {
                    Thread.sleep(10000L);
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            if (socket == null) continue;
            try {
                BaseFileSender fileSender = fileSenderFactory.getInstance(this.sdmsCollector, this);
                socket.setSoTimeout(0);
                socket.setKeepAlive(true);
                boolean connected = true;
                try (final InputStream in = socket.getInputStream();){
                    String sendId = "";
                    long currentCaptureTimePoint = System.currentTimeMillis();
                    long silenceTimePoint = -1L;
                    long trueSilenceTimePoint = -1L;
                    AtomicLong noDataTimePoint = new AtomicLong(-1L);
                    OutputStream currentOut = null;
                    long bytesInCurrentVolume = 0L;
                    long volumeRecordingStartedAt = currentCaptureTimePoint;
                    boolean capturing = false;
                    Path tempworkspace = null;
                    StringBuilder startChars = new StringBuilder();
                    StringBuilder endChars = new StringBuilder();
                    this.attachmentCount = -1;
                    while (!(!connected || this.sdmsCollector.isPaused() || this.isInstrumentPaused() || this.sdmsCollector.isDisabled() || this.sdmsCollector.isShuttingDownCollectors() || this.isShuttingDown())) {
                        final AtomicInteger dataIn = new AtomicInteger(-1);
                        final AtomicBoolean dataRead = new AtomicBoolean(false);
                        new Thread(){

                            @Override
                            public void run() {
                                try {
                                    dataIn.set(in.read());
                                    dataRead.set(true);
                                }
                                catch (Exception exception) {
                                    // empty catch block
                                }
                            }
                        }.start();
                        silenceTimePoint = -1L;
                        while (!dataRead.get()) {
                            try {
                                Thread.sleep(100L);
                                if (this.discreteEndTrigger != DiscreteEndTrigger.SILENCE || !capturing) continue;
                                if (silenceTimePoint == -1L) {
                                    silenceTimePoint = System.currentTimeMillis();
                                    continue;
                                }
                                if (System.currentTimeMillis() < silenceTimePoint + maxMilliSecondsSilence || silenceTimePoint <= 0L) continue;
                                silenceTimePoint = -1L;
                                capturing = false;
                                this.collectFilesThreadded(sendId, fileSender, this.tempFilePaths, this.lastTriggerDt, BaseFileSender.ActionOnOrginal.LEAVE, this.attachmentClass, tempworkspace);
                                this.tempFilePaths = new ArrayList<String>();
                                endChars = new StringBuilder();
                                startChars = new StringBuilder();
                                bytesInCurrentVolume = 0L;
                                currentCaptureTimePoint = System.currentTimeMillis();
                                this.cleanupOutput(currentOut);
                                this.resetCollectionLog();
                                currentOut = null;
                            }
                            catch (Exception exception) {}
                        }
                        if (dataRead.get() && dataIn.get() > -1) {
                            if (!capturing) {
                                if (this.checkForStartChars(startChars, dataIn.get(), this.discreteStartTrigger)) {
                                    this.lastTriggerDt = Calendar.getInstance();
                                    tempworkspace = this.getTempWorkarea();
                                    this.tempFilePaths = new ArrayList<String>();
                                    currentCaptureTimePoint = System.currentTimeMillis();
                                    bytesInCurrentVolume = 0L;
                                    endChars = new StringBuilder();
                                    startChars = new StringBuilder();
                                    trueSilenceTimePoint = -1L;
                                    if (currentOut != null) {
                                        this.cleanupOutput(currentOut);
                                        currentOut = null;
                                    }
                                    currentOut = this.getFileOutputStream(tempworkspace, this.filename);
                                    for (int i = 0; i < this.discreteStartTrigger.length(); ++i) {
                                        char character = this.discreteStartTrigger.charAt(i);
                                        currentOut.write(character);
                                        ++bytesInCurrentVolume;
                                    }
                                    capturing = true;
                                }
                            } else {
                                currentOut.write(dataIn.get());
                                ++bytesInCurrentVolume;
                                trueSilenceTimePoint = -1L;
                                noDataTimePoint.set(System.currentTimeMillis());
                            }
                        } else if (trueSilenceTimePoint == -1L) {
                            trueSilenceTimePoint = System.currentTimeMillis();
                        }
                        boolean doCollectFiles = false;
                        if (capturing && this.tempFilePaths.size() > 0) {
                            switch (this.discreteEndTrigger) {
                                case CHAR: {
                                    if (dataIn.get() <= -1 || !this.checkForEndChars(endChars, dataIn.get(), this.discreteEndChar)) break;
                                    this.appendCollectionLog("Collection trigged by end char.");
                                    doCollectFiles = true;
                                    break;
                                }
                                case SIZE: {
                                    if (bytesInCurrentVolume < (long)maxBytesPerVolume) break;
                                    this.appendCollectionLog("Collection trigged by size.");
                                    doCollectFiles = true;
                                    break;
                                }
                                case DURATION: {
                                    long diff = System.currentTimeMillis() - currentCaptureTimePoint;
                                    if (diff <= maxMilliSecondsPerVolume || currentCaptureTimePoint <= 0L) break;
                                    this.appendCollectionLog("Collection trigged by duration.");
                                    doCollectFiles = true;
                                }
                            }
                            if (trueSilenceTimePoint > 0L && System.currentTimeMillis() - trueSilenceTimePoint > 30000L) {
                                this.appendCollectionLog("Collection trigged by silence.");
                                doCollectFiles = true;
                                connected = false;
                            }
                        } else if (trueSilenceTimePoint > 0L && System.currentTimeMillis() - trueSilenceTimePoint > 30000L) {
                            this.appendCollectionLog("Silence triggered outside of collection. Cleaning up.");
                            connected = false;
                        }
                        if (!doCollectFiles) continue;
                        doCollectFiles = false;
                        capturing = false;
                        this.appendCollectionLog("Collecting data.");
                        this.collectFilesThreadded(sendId, fileSender, this.tempFilePaths, this.lastTriggerDt, BaseFileSender.ActionOnOrginal.LEAVE, this.attachmentClass, tempworkspace);
                        this.tempFilePaths = new ArrayList<String>();
                        endChars = new StringBuilder();
                        startChars = new StringBuilder();
                        bytesInCurrentVolume = 0L;
                        currentCaptureTimePoint = System.currentTimeMillis();
                        this.cleanupOutput(currentOut);
                        this.resetCollectionLog();
                        currentOut = null;
                    }
                    if (currentOut != null) {
                        this.cleanupOutput(currentOut);
                        currentOut = null;
                    }
                    if (this.tempFilePaths.size() <= 0) continue;
                    this.appendCollectionLog("Collecting any left over data and Cleaning up.");
                    this.collectFilesThreadded(sendId, fileSender, this.tempFilePaths, this.lastTriggerDt, BaseFileSender.ActionOnOrginal.LEAVE, this.attachmentClass, tempworkspace);
                    this.tempFilePaths = new ArrayList<String>();
                    this.resetCollectionLog();
                }
            }
            finally {
                if (socket == null) continue;
                try {
                    socket.close();
                    socket = null;
                }
                catch (Exception exception) {}
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void collectContinuously(FileSenderFactory fileSenderFactory) throws SapphireException, IOException {
        int maxBytesPerVolume = this.continuousCreateAttachmentSize_byte;
        long maxMilliSecondsPerVolume = this.continuousCreateAttachmentTime_sec * 1000L;
        while (!(this.sdmsCollector.isPaused() || this.isInstrumentPaused() || this.sdmsCollector.isDisabled() || this.sdmsCollector.isShuttingDownCollectors() || this.isShuttingDown())) {
            Socket socket = null;
            try {
                socket = new Socket(this.collectorAddress, (int)this.collectorPort);
            }
            catch (Exception e1) {
                this.appendCollectionLog("Failed to create socket. Will try again in 10 seconds.");
                try {
                    Thread.sleep(10000L);
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            if (socket == null) continue;
            try {
                BaseFileSender fileSender = fileSenderFactory.getInstance(this.sdmsCollector, this);
                socket.setSoTimeout(0);
                socket.setKeepAlive(true);
                InputStream in = null;
                try {
                    String sendId = "";
                    long currentCaptureTimePoint = System.currentTimeMillis();
                    OutputStream currentOut = null;
                    long bytesInCurrentVolume = 0L;
                    long volumeRecordingStartedAt = currentCaptureTimePoint;
                    Path tempworkspace = this.getTempWorkarea();
                    this.attachmentCount = 1;
                    boolean connected = true;
                    long silenceTimePoint = -1L;
                    while (!(!connected || this.sdmsCollector.isPaused() || this.isInstrumentPaused() || this.sdmsCollector.isDisabled() || this.sdmsCollector.isShuttingDownCollectors() || this.isShuttingDown())) {
                        long timeDiff;
                        if (!connected) continue;
                        in = socket.getInputStream();
                        int dataIn = in.read();
                        if (dataIn > -1) {
                            long timeInCurrentvolume;
                            silenceTimePoint = -1L;
                            if (this.tempFilePaths.size() == 0) {
                                this.lastTriggerDt = Calendar.getInstance();
                            }
                            if (currentOut == null) {
                                currentOut = this.getFileOutputStream(tempworkspace, this.filename);
                                bytesInCurrentVolume = 0L;
                                volumeRecordingStartedAt = System.currentTimeMillis();
                            }
                            currentOut.write(dataIn);
                            ++bytesInCurrentVolume;
                            if (this.continuousCreateAttachment == ContinuousAttachmentMode.SIZE) {
                                if (bytesInCurrentVolume >= (long)maxBytesPerVolume) {
                                    this.appendCollectionLog("Attachment size met, about to output attachment.");
                                    this.cleanupOutput(currentOut);
                                    currentOut = null;
                                    Integer n = this.attachmentCount;
                                    Integer n2 = this.attachmentCount = Integer.valueOf(this.attachmentCount + 1);
                                }
                            } else if (this.continuousCreateAttachment == ContinuousAttachmentMode.TIME && (timeInCurrentvolume = System.currentTimeMillis() - volumeRecordingStartedAt) >= maxMilliSecondsPerVolume) {
                                this.appendCollectionLog("Attachment time met, about to output attachment.");
                                this.cleanupOutput(currentOut);
                                currentOut = null;
                                Integer n = this.attachmentCount;
                                Integer n3 = this.attachmentCount = Integer.valueOf(this.attachmentCount + 1);
                            }
                        } else if (silenceTimePoint == -1L) {
                            silenceTimePoint = System.currentTimeMillis();
                        } else if (System.currentTimeMillis() - silenceTimePoint >= 30000L) {
                            this.appendCollectionLog("Connection timed out on silence and will clean up.");
                            connected = false;
                        }
                        if ((timeDiff = System.currentTimeMillis() - currentCaptureTimePoint) < this.continuousMaxCaptureTime * 1000L || currentCaptureTimePoint <= 0L) continue;
                        this.cleanupOutput(currentOut);
                        currentOut = null;
                        this.appendCollectionLog("Collection of attachments spawned off.");
                        this.collectFilesThreadded(sendId, fileSender, this.tempFilePaths, this.lastTriggerDt, BaseFileSender.ActionOnOrginal.LEAVE, this.attachmentClass, tempworkspace);
                        this.resetCollectionLog();
                        this.tempFilePaths = new ArrayList<String>();
                        tempworkspace = this.getTempWorkarea();
                        this.attachmentCount = 1;
                        new DateTimeUtil();
                        currentCaptureTimePoint = DateTimeUtil.getNowCalendar().getTimeInMillis();
                    }
                    if (currentOut != null) {
                        this.cleanupOutput(currentOut);
                        currentOut = null;
                    }
                    if (this.tempFilePaths.size() <= 0) continue;
                    this.appendCollectionLog("Collection of attachments spawned off (clean up).");
                    this.collectFilesThreadded(sendId, fileSender, this.tempFilePaths, this.lastTriggerDt, BaseFileSender.ActionOnOrginal.LEAVE, this.attachmentClass, tempworkspace);
                    this.resetCollectionLog();
                    this.tempFilePaths = new ArrayList<String>();
                }
                finally {
                    if (in == null) continue;
                    try {
                        in.close();
                    }
                    catch (Exception exception) {}
                }
            }
            finally {
                if (socket == null) continue;
                try {
                    socket.close();
                    socket = null;
                }
                catch (Exception exception) {}
            }
        }
    }

    private void cleanupOutput(OutputStream out) throws IOException {
        if (out != null) {
            try {
                try {
                    out.flush();
                }
                finally {
                    try {
                        out.close();
                    }
                    catch (Exception exception) {}
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    private void collectFilesThreadded(final String sendId, final BaseFileSender fileSender, final List<String> tempFilePaths, final Calendar lastTriggerDt, final BaseFileSender.ActionOnOrginal actionOnOrginal, final String attachmentClass, final Path tempworkspace) throws SapphireException, IOException {
        new Thread("capturefiles"){
            final String $sendId;
            final BaseFileSender $fileSender;
            final List<String> $tempFilePaths;
            final Calendar $lastTriggerDt;
            final BaseFileSender.ActionOnOrginal $actionOnOrginal;
            final String $attachmentClass;
            final Path $tempworkspace;
            {
                super(x0);
                this.$sendId = sendId;
                this.$fileSender = fileSender;
                this.$tempFilePaths = (ArrayList)((ArrayList)tempFilePaths).clone();
                this.$lastTriggerDt = (Calendar)lastTriggerDt.clone();
                this.$actionOnOrginal = actionOnOrginal;
                this.$attachmentClass = attachmentClass;
                this.$tempworkspace = tempworkspace;
            }

            @Override
            public void run() {
                try {
                    try {
                        NetworkCollectorType.this.collectFiles(this.$sendId, this.$fileSender, this.$tempFilePaths, this.$lastTriggerDt, this.$actionOnOrginal, this.$attachmentClass);
                    }
                    finally {
                        try {
                            if (NetworkCollectorType.this.getSandboxArea() == null) {
                                FileUtil.deleteDirectory(this.$tempworkspace.toFile());
                            }
                        }
                        catch (Exception exception) {}
                    }
                }
                catch (Exception e) {
                    NetworkCollectorType.this.logger.log("COLLECTING", "Failed to collect files");
                }
            }
        }.start();
    }

    private void collectFiles(String sendId, BaseFileSender fileSender, List<String> tempFilePaths, Calendar lastTriggerDt, BaseFileSender.ActionOnOrginal actionOnOrginal, String actionClass) throws SapphireException, IOException {
        try {
            sendId = fileSender.init(lastTriggerDt, new PropertyList());
        }
        catch (Exception e) {
            this.raiseInstrumentAlert("SDMS Collection", "Failure", "Failed to initialise sender", "Failed to initialise the sender.", false, false);
        }
        if (sendId != null && sendId.length() > 0) {
            for (String tempFilePath : tempFilePaths) {
                String fileContent = null;
                if (this.excludeStartTrigger.trim().equalsIgnoreCase("y") && !this.discreteStartTrigger.trim().isEmpty()) {
                    if (fileContent == null) {
                        fileContent = FileUtil.getFileString(new File(tempFilePath));
                    }
                    fileContent = NetworkCollectorType.excludeStartTrigger(this.discreteStartTrigger, fileContent);
                }
                if (this.excludeEndTrigger.trim().equalsIgnoreCase("y") && !this.discreteEndChar.trim().isEmpty()) {
                    if (fileContent == null) {
                        fileContent = FileUtil.getFileString(new File(tempFilePath));
                    }
                    fileContent = NetworkCollectorType.excludeEndTrigger(this.discreteEndChar, fileContent);
                }
                if (this.trimResult.trim().equalsIgnoreCase("y")) {
                    if (fileContent == null) {
                        fileContent = FileUtil.getFileString(new File(tempFilePath));
                    }
                    fileContent = fileContent.trim();
                }
                if (fileContent != null) {
                    FileUtil.writeFileString(new File(tempFilePath), fileContent);
                }
                PropertyList fileMetadata = new PropertyList();
                fileMetadata.setProperty("filename", FileUtil.getFileName(tempFilePath, true));
                fileMetadata.setProperty("filesize", Long.toString(FileUtil.fileSize(new File(tempFilePath))));
                try {
                    fileSender.store(sendId, Paths.get(tempFilePath, new String[0]), actionOnOrginal, actionClass, false, null, null, fileMetadata);
                }
                catch (Exception e) {
                    this.raiseInstrumentAlert("SDMS Collection", "Failure", "Failed to store data", "Failed to send the file to LIMS.", false, false);
                }
            }
            try {
                tempFilePaths.clear();
                fileSender.complete(sendId);
            }
            catch (Exception e) {
                String msg = e.getMessage();
                this.raiseInstrumentAlert("SDMS Startup", "Failure", "Exception while storing files in network collector", msg, false, true);
            }
        }
    }

    private static String excludeStartTrigger(String excludeStartTriggerChars, String fileContent) {
        int i = fileContent.indexOf(excludeStartTriggerChars);
        if (i > -1) {
            String before = fileContent.substring(0, i).toString();
            String after = fileContent.substring(i + excludeStartTriggerChars.length(), fileContent.length()).toString();
            String result = before + after;
            return result;
        }
        return fileContent;
    }

    private static String excludeEndTrigger(String excludeEndTriggerChars, String fileContent) {
        int lastIndex = fileContent.lastIndexOf(excludeEndTriggerChars);
        if (lastIndex > -1) {
            String before = fileContent.substring(0, lastIndex).toString();
            String after = fileContent.substring(lastIndex + excludeEndTriggerChars.length(), fileContent.length()).toString();
            String result = before + after;
            return result;
        }
        return fileContent;
    }

    @Override
    public String doDeliverRunFile(String filename, byte[] bytes) throws SapphireException {
        String message = "";
        try {
            if (this.isDeliveryRunFileName) {
                this.filename = FileUtil.getExtension(filename).length() > 0 ? filename : filename + ".txt";
            }
            ServerSocket serverSocket = new ServerSocket();
            InetAddress addr = InetAddress.getByName(this.collectorAddress);
            InetSocketAddress sockaddr = new InetSocketAddress(addr, (int)this.collectorPort);
            serverSocket.setReuseAddress(true);
            serverSocket.bind(sockaddr);
            Socket socket = serverSocket.accept();
            OutputStream out = socket.getOutputStream();
            InputStream inputStream = IOUtils.toInputStream((String)new String(bytes));
            int lenChar = 0;
            while ((lenChar = inputStream.read()) != -1) {
                out.write((char)lenChar);
            }
            inputStream.close();
            out.flush();
            out.close();
            message = "RunFile has been successfully delivered.";
        }
        catch (Exception e) {
            String msg = e.getMessage();
            this.raiseInstrumentAlert("SDMS Startup", "Failure", "Failed to deliver RunFile", msg, false, true);
        }
        return message;
    }

    public static enum DiscreteEndTrigger {
        CHAR,
        DURATION,
        SILENCE,
        SIZE;

    }

    public static enum ContinuousAttachmentMode {
        TIME,
        SIZE;

    }

    public static enum ErrorType {
        NONE,
        CLOSE,
        INTERRUPT;

    }

    public static enum Mode {
        DISCRETE,
        CONTINUOUS;

    }
}

