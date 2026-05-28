/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.webservices.messages;

import com.labvantage.sapphire.webservices.messages.BaseSECMessage;
import com.labvantage.sapphire.webservices.transport.DataSetTransportBean;
import com.labvantage.sapphire.webservices.transport.PropertyListTransportBean;
import org.json.JSONObject;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.xml.PropertyList;

public class EmpowerMessage
extends BaseSECMessage {
    private PropertyList metaData;
    private DataSet batchData;
    private DataSet sampleData;
    private DataSet resultData;
    private DataSet peakData;
    private DataSet calibrationData;
    private DataSet curveData;
    private String policyNode;
    private String empowerProject;
    private String empowerDatabase;

    public EmpowerMessage() {
        this.metaData = new PropertyList();
    }

    public EmpowerMessage(String msg) {
        this.fromMessage(msg);
    }

    public String getProperty(String propertyid) {
        return this.metaData.getProperty(propertyid);
    }

    public String getProperty(String propertyid, String def) {
        return this.metaData.getProperty(propertyid, def);
    }

    public void setProperty(String propertyid, String value) {
        this.metaData.setProperty(propertyid, value);
    }

    public void setDataSet(DataLevel level, DataSet data) {
        switch (level) {
            case BATCH: {
                this.batchData = data;
                this.batchData.setId("batch");
                break;
            }
            case SAMPLE: {
                this.sampleData = data;
                this.sampleData.setId("sample");
                break;
            }
            case RESULT: {
                this.resultData = data;
                this.resultData.setId("result");
                break;
            }
            case PEAK: {
                this.peakData = data;
                this.peakData.setId("peak");
                break;
            }
            case CALIBRATION: {
                this.calibrationData = data;
                this.calibrationData.setId("calibration");
                break;
            }
            case CURVE: {
                this.curveData = data;
                this.curveData.setId("curve");
            }
        }
    }

    public DataSet getDataSet(DataLevel level) {
        switch (level) {
            case BATCH: {
                return this.batchData;
            }
            case SAMPLE: {
                return this.sampleData;
            }
            case RESULT: {
                return this.resultData;
            }
            case PEAK: {
                return this.peakData;
            }
            case CALIBRATION: {
                return this.calibrationData;
            }
            case CURVE: {
                return this.curveData;
            }
        }
        return null;
    }

    public PropertyListTransportBean getMetaData() {
        return new PropertyListTransportBean(this.metaData);
    }

    public void setMetaData(PropertyListTransportBean metaData) {
        this.metaData = metaData.toPropertyList();
        this.metaData.setId("meta");
    }

    public DataSetTransportBean getBatchData() {
        return new DataSetTransportBean(this.batchData, true, true);
    }

    public String getPolicyNode() {
        return this.policyNode;
    }

    public String getEmpowerProject() {
        return this.empowerProject;
    }

    public String getEmpowerDatabase() {
        return this.empowerDatabase;
    }

    public void setBatchData(DataSetTransportBean batchData) {
        this.batchData = batchData.toDataSet();
        this.batchData.setId("batch");
    }

    public DataSetTransportBean getSampleData() {
        return new DataSetTransportBean(this.sampleData, true, true);
    }

    public void setSampleData(DataSetTransportBean sampleData) {
        this.sampleData = sampleData.toDataSet();
        this.batchData.setId("sample");
    }

    public void setPolicyNode(String node) {
        this.policyNode = node;
    }

    public void setEmpowerProject(String project) {
        this.empowerProject = project;
    }

    public void setEmpowerDatabase(String database) {
        this.empowerDatabase = database;
    }

    public DataSetTransportBean getResultData() {
        return new DataSetTransportBean(this.resultData, true, true);
    }

    public void setResultData(DataSetTransportBean resultData) {
        this.resultData = resultData.toDataSet();
        this.batchData.setId("result");
    }

    public DataSetTransportBean getPeakData() {
        return new DataSetTransportBean(this.peakData, true, true);
    }

    public void setPeakData(DataSetTransportBean peakData) {
        this.peakData = peakData.toDataSet();
        this.batchData.setId("peak");
    }

    public DataSetTransportBean getCalibrationData() {
        return new DataSetTransportBean(this.calibrationData, true, true);
    }

    public void setCalibrationData(DataSetTransportBean calibrationData) {
        this.calibrationData = calibrationData.toDataSet();
        this.batchData.setId("calibration");
    }

    public DataSetTransportBean getCurveData() {
        return new DataSetTransportBean(this.curveData, true, true);
    }

    public void setCurveData(DataSetTransportBean curveData) {
        this.curveData = curveData.toDataSet();
        this.batchData.setId("curve");
    }

    @Override
    public void fromMessage(String message) {
        Logger.logInfo("From Message called.");
        if (Logger.isDebugEnabled()) {
            Logger.logDebug(message);
        }
        if (message.length() > 0) {
            try {
                JSONObject job = new JSONObject(message);
                if (job != null) {
                    DataSet ds;
                    if (job.has("id")) {
                        this.setId(job.get("id").toString());
                    } else {
                        Logger.logInfo("No message id provided.");
                    }
                    if (job.has("version")) {
                        this.setVersion(job.get("version").toString());
                    } else {
                        Logger.logInfo("No message version provided.");
                    }
                    if (job.has("flow")) {
                        try {
                            this.setMsgFlow(Integer.parseInt(job.get("flow").toString()));
                        }
                        catch (Exception e) {
                            Logger.logWarn("Invalid message flow provided.");
                        }
                    } else {
                        Logger.logInfo("No message flow provided.");
                    }
                    if (job.has("policynode")) {
                        this.setPolicyNode(job.get("policynode").toString());
                    }
                    if (job.has("empowerproject")) {
                        this.setEmpowerProject(job.get("empowerproject").toString());
                    }
                    if (job.has("empowerdatabase")) {
                        this.setEmpowerDatabase(job.get("empowerdatabase").toString());
                    }
                    if (job.has("meta")) {
                        try {
                            PropertyList pl = new PropertyList(job.getJSONObject("meta"));
                            this.setMetaData(new PropertyListTransportBean(pl));
                        }
                        catch (Exception e) {
                            Logger.logWarn("Invalid meta data provided.");
                        }
                    } else {
                        Logger.logInfo("No meta data provided.");
                    }
                    if (job.has("batch")) {
                        try {
                            ds = new DataSet(job.getJSONObject("batch"));
                            this.setBatchData(new DataSetTransportBean(ds, true, true));
                        }
                        catch (Exception e) {
                            Logger.logWarn("Invalid batch data provided.");
                        }
                    } else {
                        Logger.logInfo("No batch data provided.");
                    }
                    if (job.has("sample")) {
                        try {
                            ds = new DataSet(job.getJSONObject("sample"));
                            this.setSampleData(new DataSetTransportBean(ds, true, true));
                        }
                        catch (Exception e) {
                            Logger.logWarn("Invalid sample data provided.");
                        }
                    } else {
                        Logger.logInfo("No sample data provided.");
                    }
                    if (job.has("result")) {
                        try {
                            ds = new DataSet(job.getJSONObject("result"));
                            this.setResultData(new DataSetTransportBean(ds, true, true));
                        }
                        catch (Exception e) {
                            Logger.logWarn("Invalid result data provided.");
                        }
                    } else {
                        Logger.logInfo("No result data provided.");
                    }
                    if (job.has("peak")) {
                        try {
                            ds = new DataSet(job.getJSONObject("peak"));
                            this.setPeakData(new DataSetTransportBean(ds, true, true));
                        }
                        catch (Exception e) {
                            Logger.logWarn("Invalid peak data provided.");
                        }
                    } else {
                        Logger.logInfo("No peak data provided.");
                    }
                    if (job.has("calibration")) {
                        try {
                            ds = new DataSet(job.getJSONObject("calibration"));
                            this.setCalibrationData(new DataSetTransportBean(ds, true, true));
                        }
                        catch (Exception e) {
                            Logger.logWarn("Invalid calibration data provided.");
                        }
                    } else {
                        Logger.logInfo("No calibration data provided.");
                    }
                    if (job.has("curve")) {
                        try {
                            ds = new DataSet(job.getJSONObject("curve"));
                            this.setCurveData(new DataSetTransportBean(ds, true, true));
                        }
                        catch (Exception e) {
                            Logger.logWarn("Invalid curve data provided.");
                        }
                    } else {
                        Logger.logInfo("No curve data provided.");
                    }
                }
            }
            catch (Exception e2) {
                Logger.logError("Could not read message.", e2);
            }
        }
    }

    @Override
    public String toMessage() {
        try {
            JSONObject job = new JSONObject();
            job.put("id", this.getId());
            job.put("version", this.getVersion());
            job.put("flow", this.getMsgFlow());
            job.put("meta", this.getMetaData().toPropertyList().toJSONObject(true));
            job.put("batch", this.getBatchData().toDataSet().toJSONObject(true, true));
            job.put("sample", this.getSampleData().toDataSet().toJSONObject(true, true));
            job.put("result", this.getResultData().toDataSet().toJSONObject(true, true));
            job.put("peak", this.getPeakData().toDataSet().toJSONObject(true, true));
            job.put("calibration", this.getCalibrationData().toDataSet().toJSONObject(true, true));
            job.put("curve", this.getCurveData().toDataSet().toJSONObject(true, true));
            job.put("policynode", this.getPolicyNode());
            job.put("empowerproject", this.getEmpowerProject());
            job.put("empowerdatabase", this.getEmpowerDatabase());
            return job.toString();
        }
        catch (Exception e) {
            Logger.logError("Could not build string message.", e);
            return "";
        }
    }

    public static enum DataLevel {
        BATCH,
        SAMPLE,
        RESULT,
        PEAK,
        CALIBRATION,
        CURVE;

    }
}

