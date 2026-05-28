/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.misc;

import com.labvantage.sapphire.instrument.csv.CSVParser;
import com.labvantage.sapphire.util.MiscUtil;
import java.io.File;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ImportResultsFromCSV
extends BaseAction {
    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        DataSet data;
        String filename = properties.getProperty("filename", properties.getProperty("filepath", properties.getProperty("file", "")));
        if (filename.length() <= 0) throw new SapphireException("No filename provided.");
        File file = new File(filename);
        if (file == null || !file.exists()) throw new SapphireException("Filename is invalid.");
        if (properties.getProperty("sdcid", "").length() <= 0 || properties.getProperty("paramlistid", "").length() <= 0 || properties.getProperty("paramlistversionid", "").length() <= 0 || properties.getProperty("variantid", "").length() <= 0) throw new SapphireException("No sdi & parameter list details provided.");
        CSVParser csv = new CSVParser(properties.getProperty("csvheaderrow", "Y").equalsIgnoreCase("Y"), properties.getProperty("csvseparatorchar", ",").charAt(0), properties.getProperty("csvquotechar", "\"").charAt(0), properties.getProperty("csvescapechar", "\\").charAt(0));
        if (properties.getProperty("csvcolumnmapping", "").length() > 0) {
            csv.setColumnMapping(properties.getProperty("csvcolumnmapping"));
        }
        if ((data = csv.parseCSVFile(file)) == null) throw new SapphireException("No data imported");
        String[] excludeColumns = StringUtil.split(properties.getProperty("excludecolumn", ""), ";");
        if (properties.getProperty("keyid1", "").length() > 0) {
            if (!data.isValidColumn("keyid1")) {
                data.addColumn("keyid1", 0);
            }
            data.setValue(-1, "keyid1", properties.getProperty("keyid1"));
        }
        if (properties.getProperty("keyid2", "").length() > 0) {
            if (!data.isValidColumn("keyid2")) {
                data.addColumn("keyid2", 0);
            }
            data.setValue(-1, "keyid2", properties.getProperty("keyid2"));
        }
        if (properties.getProperty("keyid3", "").length() > 0) {
            if (!data.isValidColumn("keyid3")) {
                data.addColumn("keyid3", 0);
            }
            data.setValue(-1, "keyid3", properties.getProperty("keyid3"));
        }
        if (properties.getProperty("paramtype", "").length() > 0) {
            if (!data.isValidColumn("paramtype")) {
                data.addColumn("paramtype", 0);
            }
            data.setValue(-1, "paramtype", properties.getProperty("paramtype", ""));
        }
        if (properties.getProperty("replicateid", "").length() > 0) {
            if (!data.isValidColumn("replicateid")) {
                data.addColumn("replicateid", 0);
            }
            data.setValue(-1, "replicateid", properties.getProperty("replicateid"));
        }
        if (properties.getProperty("dataset", "").length() > 0) {
            if (!data.isValidColumn("dataset")) {
                data.addColumn("dataset", 0);
            }
            data.setValue(-1, "dataset", properties.getProperty("dataset"));
        }
        StringBuffer valuesBuf = new StringBuffer();
        StringBuffer paramidsBuf = new StringBuffer();
        StringBuffer keyid1Buf = new StringBuffer();
        StringBuffer keyid2Buf = new StringBuffer();
        StringBuffer keyid3Buf = new StringBuffer();
        StringBuffer paramtypeBuf = new StringBuffer();
        StringBuffer replicateidBuf = new StringBuffer();
        StringBuffer datasetBuf = new StringBuffer();
        for (int i = 0; i < data.getColumnCount(); ++i) {
            String col = data.getColumnId(i);
            if (col.equalsIgnoreCase("keyid1") || col.equalsIgnoreCase("paramtype") || col.equalsIgnoreCase("replicateid") || col.equalsIgnoreCase("dataset") || MiscUtil.MiscArray.isStringInArray(excludeColumns, col, true)) continue;
            for (int r = 0; r < data.getRowCount(); ++r) {
                if (r > 0 || paramidsBuf.length() > 0) {
                    paramidsBuf.append(";");
                    valuesBuf.append(";");
                    keyid1Buf.append(";");
                    if (data.isValidColumn("keyid2")) {
                        keyid2Buf.append(";");
                        if (data.isValidColumn("keyid3")) {
                            keyid3Buf.append(";");
                        }
                    }
                    replicateidBuf.append(";");
                    paramtypeBuf.append(";");
                    datasetBuf.append(";");
                }
                paramidsBuf.append(col);
                valuesBuf.append(data.getValue(r, data.getColumnId(i), ""));
                keyid1Buf.append(data.getValue(r, "keyid1", ""));
                if (data.isValidColumn("keyid2")) {
                    keyid2Buf.append(data.getValue(r, "keyid2", ""));
                    if (data.isValidColumn("keyid3")) {
                        keyid3Buf.append(data.getValue(r, "keyid3", ""));
                    }
                }
                paramtypeBuf.append(data.getValue(r, "paramtype", ""));
                replicateidBuf.append(data.getValue(r, "replicateid", ""));
                datasetBuf.append(data.getValue(r, "dataset", ""));
            }
        }
        PropertyList enterdataProps = new PropertyList();
        enterdataProps.setProperty("sdcid", properties.getProperty("sdcid"));
        enterdataProps.setProperty("paramlistid", properties.getProperty("paramlistid"));
        enterdataProps.setProperty("paramlistversionid", properties.getProperty("paramlistversionid"));
        enterdataProps.setProperty("variantid", properties.getProperty("variantid"));
        enterdataProps.setProperty("dataset", datasetBuf.toString());
        enterdataProps.setProperty("keyid1", keyid1Buf.toString());
        if (keyid2Buf.length() > 0) {
            enterdataProps.setProperty("keyid2", keyid2Buf.toString());
            if (keyid3Buf.length() > 0) {
                enterdataProps.setProperty("keyid3", keyid3Buf.toString());
            }
        }
        enterdataProps.setProperty("paramid", paramidsBuf.toString());
        enterdataProps.setProperty("paramtype", paramtypeBuf.toString());
        enterdataProps.setProperty("replicateid", replicateidBuf.toString());
        enterdataProps.setProperty("enteredtext", valuesBuf.toString());
        this.getActionProcessor().processAction("EnterDataItem", "1", enterdataProps);
    }
}

