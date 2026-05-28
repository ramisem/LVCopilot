/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.misc;

import com.labvantage.sapphire.instrument.csv.CSVWriter;
import com.labvantage.sapphire.util.groovy.GroovyUtil;
import java.io.File;
import java.text.DecimalFormat;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.SDIProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ExportDataToCSV
extends BaseAction {
    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String filename = properties.getProperty("filename", properties.getProperty("filepath", properties.getProperty("file", "")));
        if (filename.length() <= 0) throw new SapphireException("No filename provided.");
        File file = new File(filename);
        if (file == null) throw new SapphireException("Filename is invalid.");
        DataSet data = null;
        if (properties.getProperty("sql", "").length() > 0) {
            data = this.getQueryProcessor().getSqlDataSet(properties.getProperty("sql"));
        } else {
            if (properties.getProperty("sdcid", "").length() <= 0 || properties.getProperty("keyid1", "").length() <= 0 && properties.getProperty("queryfrom", "").length() <= 0 && properties.getProperty("queryid", "").length() <= 0) throw new SapphireException("No SQL or SDI details provided.");
            SDIRequest sdiRequest = new SDIRequest();
            sdiRequest.setSDCid(properties.getProperty("sdcid"));
            if (properties.getProperty("keyid1", "").length() > 0) {
                sdiRequest.setSDCid(properties.getProperty("keyid1"));
                if (properties.getProperty("keyid2", "").length() > 0) {
                    sdiRequest.setSDCid(properties.getProperty("keyid2"));
                    if (properties.getProperty("keyid3", "").length() > 0) {
                        sdiRequest.setSDCid(properties.getProperty("keyid3"));
                    }
                }
            } else if (properties.getProperty("queryid", "").length() > 0) {
                sdiRequest.setQueryid(properties.getProperty("queryid"));
                if (properties.getProperty("queryparams", "").length() > 0) {
                    sdiRequest.setQueryParams(StringUtil.split(properties.getProperty("queryparams"), ";"));
                }
            }
            if (properties.getProperty("queryfrom", "").length() > 0) {
                sdiRequest.setQueryFrom(properties.getProperty("queryfrom"));
            }
            if (properties.getProperty("querywhere", "").length() > 0) {
                sdiRequest.setQueryWhere(properties.getProperty("querywhere"));
            }
            if (properties.getProperty("queryorderby", "").length() > 0) {
                sdiRequest.setQueryOrderBy(properties.getProperty("queryorderby"));
            }
            String retrievelimit = properties.getProperty("retrievelimit", "100");
            try {
                sdiRequest.setRetrieveLimit(Integer.parseInt(retrievelimit));
            }
            catch (Exception e) {
                sdiRequest.setRetrieveLimit(100);
            }
            sdiRequest.setShowTemplates(properties.getProperty("showtemplates", "N").equalsIgnoreCase("Y"));
            sdiRequest.setRequestItem("primary");
            SDIProcessor sdi = this.getSDIProcessor();
            SDIData sdiData = sdi.getSDIData(sdiRequest);
            if (sdiData == null || sdiData.getDataset("primary") == null) throw new SapphireException("No data could be obtained. Check SDI Properties.");
            data = sdiData.getDataset("primary");
        }
        if (data == null) throw new SapphireException("No data could be obtained. Check SQL or SDI Properties.");
        data.setColidCaseSensitive(true);
        String pseudocolumn = properties.getProperty("pseudocolumn", "");
        if (pseudocolumn.length() > 0) {
            String[] pseudocolumnParts = StringUtil.split(pseudocolumn, ";", true);
            for (int p = 0; p < pseudocolumnParts.length; ++p) {
                int ei = pseudocolumnParts[p].indexOf("=");
                if (ei <= -1) continue;
                String col = pseudocolumnParts[p].substring(0, ei);
                String v = pseudocolumnParts[p].substring(ei + 1);
                if (!data.isValidColumn(col)) {
                    data.addColumn(col, 0);
                }
                DecimalFormat df = new DecimalFormat("#.##");
                for (int r = 0; r < data.getRowCount(); ++r) {
                    String uv;
                    if (v.startsWith("[{") && v.endsWith("}]")) {
                        HashMap<String, Object> bind = new HashMap<String, Object>();
                        for (int c = 0; c < data.getColumnCount(); ++c) {
                            String cid = data.getColumnId(c);
                            bind.put(cid, data.getValue(r, cid, ""));
                        }
                        bind.put("format", df);
                        try {
                            uv = GroovyUtil.getInstance(this.connectionInfo).evaluateSecure(v.substring(2, v.length() - 2), bind);
                        }
                        catch (Exception e) {
                            this.logger.warn(e.getMessage());
                            uv = "Could not evaluate groovy.";
                        }
                    } else {
                        uv = v;
                    }
                    if (uv.equalsIgnoreCase("[rownum]") || uv.equalsIgnoreCase("[rownum+1]")) {
                        data.setValue(r, col, "" + (uv.equalsIgnoreCase("[rownum+1]") ? r + 1 : r));
                        continue;
                    }
                    if (uv.startsWith("[random<") || uv.endsWith("]")) {
                        Integer n;
                        String t = uv.substring("[random<".length());
                        t = t.substring(0, t.length() - 1);
                        try {
                            n = Integer.parseInt(t) - 1;
                        }
                        catch (Exception e) {
                            n = 0;
                        }
                        data.setValue(r, col, df.format(Math.random() + (double)n.intValue()));
                        continue;
                    }
                    data.setValue(r, col, uv);
                }
            }
        }
        CSVWriter csv = new CSVWriter(properties.getProperty("csvheaderrow", "Y").equalsIgnoreCase("Y"), properties.getProperty("csvseparatorchar", ",").charAt(0), properties.getProperty("csvquotechar", "\"").charAt(0), properties.getProperty("csvescapechar", "\\").charAt(0));
        if (properties.getProperty("csvcolumnmapping", "").length() > 0) {
            csv.setColumnMapping(properties.getProperty("csvcolumnmapping"));
        }
        csv.writeCSVToFile(data, file);
    }
}

