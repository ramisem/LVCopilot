/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdi;

import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class BaseGetSDI
extends BaseAction {
    public static final String COLUMNID = "columnid";
    public static final String ATTRIBUTEID = "attributeid";
    public static final String VALUE = "value";
    public static final String APPLYMASKING = "applymasking";

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    protected void getSDIValue(PropertyList properties, int datatype) throws SapphireException {
        String sdcid = properties.getProperty("sdcid");
        String columnid = properties.getProperty(COLUMNID);
        String attributeid = properties.getProperty(ATTRIBUTEID);
        boolean applyMasking = "Y".equals(StringUtil.getYN(properties.getProperty(APPLYMASKING, "N"), "N"));
        if (sdcid.length() > 0 && columnid.length() > 0) {
            SDIData sdidata;
            String keyid1 = properties.getProperty("keyid1");
            String keyid2 = properties.getProperty("keyid2");
            String keyid3 = properties.getProperty("keyid3");
            SDIRequest sdirequest = new SDIRequest();
            sdirequest.setSDIList(sdcid, keyid1, keyid2, keyid3);
            sdirequest.setRequestItem("primary");
            if (applyMasking) {
                sdirequest.setReturnMaskedData(true);
            }
            try {
                sdidata = this.getSDIProcessor().getSDIData(sdirequest);
            }
            catch (Exception e) {
                throw new SapphireException("GET_SDIDATA_FAILED", "Failed to get SDI data", e);
            }
            this.logger.info("Retrieving the SDI information");
            if (sdidata == null) throw new SapphireException("INVALID_PROPERTY", "Invalid SDI Request.");
            DataSet samples = sdidata.getDataset("primary");
            this.logger.info("Retreived " + String.valueOf(samples.getRowCount()) + " row(s) and column type:" + String.valueOf(samples.getColumnType(columnid)));
            if (samples.getColumnType(columnid) != datatype) throw new SapphireException("INVALID_PROPERTY", "Invalid column data type.");
            if (datatype == 2 && "Y".equals(this.getSDCProcessor().getSDCColumnProperty(sdcid, columnid, "timezoneindependent"))) {
                samples.setTimeZoneInsensitive(columnid);
            }
            int rows = samples.getRowCount();
            StringBuffer result = new StringBuffer("");
            for (int i = 0; i < rows; ++i) {
                result.append(samples.getValue(i, columnid));
                if (i >= rows - 1) continue;
                result.append(";");
            }
            properties.setProperty(VALUE, result.toString());
            return;
        }
        if (sdcid.length() <= 0 || attributeid.length() <= 0) return;
        DataSet props = new DataSet();
        props.addColumnValues("keyid1", 0, properties.getProperty("keyid1"), ";");
        props.addColumnValues("keyid2", 0, properties.getProperty("keyid2"), ";", "(null)");
        props.addColumnValues("keyid3", 0, properties.getProperty("keyid3"), ";", "(null)");
        props.padColumns();
        String rsetid = this.getDAMProcessor().createRSet(sdcid, props.getColumnValues("keyid1", ";"), props.getColumnValues("keyid2", ";"), props.getColumnValues("keyid3", ";"));
        try {
            String sql = "SELECT sdiattribute.keyid1, sdiattribute.keyid2, sdiattribute.keyid3, sdiattribute.datatype, textvalue, numericvalue, datevalue, clobvalue FROM sdiattribute, rsetitems WHERE sdiattribute.sdcid=rsetitems.sdcid AND sdiattribute.keyid1=rsetitems.keyid1 AND sdiattribute.keyid2=rsetitems.keyid2 AND sdiattribute.keyid3=rsetitems.keyid3 AND sdiattribute.attributeid=? AND attributesdcid=rsetitems.sdcid AND rsetitems.rsetid=? ORDER BY attributeinstance";
            DataSet attributes = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{attributeid, rsetid}, true);
            attributes.setConnectionInfo(this.connectionInfo);
            StringBuffer returnValue = new StringBuffer();
            HashMap<String, String> findfilter = new HashMap<String, String>();
            for (int i = 0; i < props.size(); ++i) {
                findfilter.put("keyid1", props.getString(i, "keyid1"));
                findfilter.put("keyid2", props.getString(i, "keyid2"));
                findfilter.put("keyid3", props.getString(i, "keyid3"));
                int row = attributes.findRow(findfilter);
                returnValue.append(i > 0 ? ";" : "");
                String attributeDatatype = attributes.getValue(row, "datatype");
                if (attributeDatatype.equals("N")) {
                    if (datatype != 1) {
                        throw new SapphireException("INVALID_PROPERTY", "Invalid attribute data type.");
                    }
                    returnValue.append(attributes.getValue(row, "numericvalue"));
                    continue;
                }
                if (attributeDatatype.equals("D")) {
                    if (datatype != 2) {
                        throw new SapphireException("INVALID_PROPERTY", "Invalid attribute data type.");
                    }
                    returnValue.append(attributes.getValue(row, "datevalue"));
                    continue;
                }
                if (attributeDatatype.equals("O")) {
                    if (datatype != 2) {
                        throw new SapphireException("INVALID_PROPERTY", "Invalid attribute data type.");
                    }
                    attributes.setTimeZoneInsensitive("datevalue");
                    returnValue.append(attributes.getValue(row, "datevalue"));
                    continue;
                }
                if (attributeDatatype.equals("C")) {
                    if (datatype != 0) {
                        throw new SapphireException("INVALID_PROPERTY", "Invalid attribute data type.");
                    }
                    returnValue.append(attributes.getValue(row, "clobvalue"));
                    continue;
                }
                if (datatype != 0) {
                    throw new SapphireException("INVALID_PROPERTY", "Invalid attribute data type.");
                }
                returnValue.append(attributes.getValue(row, "textvalue"));
            }
            properties.setProperty(VALUE, returnValue.toString());
            return;
        }
        finally {
            this.getDAMProcessor().clearRSet(rsetid);
        }
    }
}

