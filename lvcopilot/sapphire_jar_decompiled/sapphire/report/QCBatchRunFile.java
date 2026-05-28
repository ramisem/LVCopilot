/*
 * Decompiled with CFR 0.152.
 */
package sapphire.report;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import sapphire.SapphireException;
import sapphire.report.BaseJavaReport;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;

public class QCBatchRunFile
extends BaseJavaReport {
    private HashMap paramsMap;

    @Override
    public void init(String reportid, String reportVersionid, HashMap paramsMap, ConnectionInfo connectionInfo) {
        this.paramsMap = paramsMap;
    }

    @Override
    public String getLogicalFileName(String defaultFileName) {
        return defaultFileName.endsWith(".txt") ? defaultFileName : "sequence.txt";
    }

    @Override
    public String[] getReportParameters() {
        return new String[]{"keyid1"};
    }

    @Override
    public void runReport(OutputStream outputStream) throws SapphireException {
        String qcbatchid = (String)this.paramsMap.get("keyid1");
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer();
        sql.append("select sdi.s_qcbatchid, qcbi.usersequence,sdi.keyid1,qcbi.batchitemtype,sdi.paramlistid,sdi.paramlistversionid,sdi.variantid,sdi.dataset from sdidata sdi,s_qcbatchitem qcbi ");
        sql.append(" where sdi.s_qcbatchid in (").append(safeSQL.addIn(qcbatchid, ";")).append(")");
        sql.append(" and sdi.s_qcbatchid=qcbi.s_qcbatchid");
        sql.append(" and sdi.s_qcbatchitemid=qcbi.s_qcbatchitemid");
        sql.append(" and exists(select pl.paramlistid from  paramlist pl where pl.paramlistid=sdi.paramlistid and pl.paramlistversionid=sdi.paramlistversionid and pl.variantid=sdi.variantid and pl.s_paramlisttype='Procedural')");
        sql.append(" order by sdi.s_qcbatchid,qcbi.usersequence");
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        String[] titles = new String[]{"QC Batch", "Sample Id", "QC Sample Type"};
        String[] columnids = new String[]{"s_qcbatchid", "keyid1", "batchitemtype"};
        StringBuffer runfilecontent = this.getTextFromDataSet(ds, Arrays.asList(titles), Arrays.asList(columnids));
        try {
            outputStream.write(runfilecontent.toString().getBytes());
        }
        catch (IOException e) {
            throw new SapphireException("Unable to stream the report back");
        }
    }

    protected StringBuffer getTextFromDataSet(DataSet ds, List<String> titles, List<String> columnIds) {
        StringBuffer sbOutputData = new StringBuffer();
        StringBuffer sbHeaderData = new StringBuffer();
        for (String title : titles) {
            sbHeaderData.append("\t").append(title);
        }
        sbOutputData.append(sbHeaderData.substring(1));
        sbOutputData.append("\r\n");
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < ds.getRowCount(); ++i) {
            int col = 0;
            for (String columnId : columnIds) {
                String value = ds.getValue(i, columnId);
                if (col == 0) {
                    if (list.contains(value)) {
                        value = "\t";
                    } else {
                        list.add(value);
                    }
                }
                sbOutputData.append(value).append("\t");
                ++col;
            }
            sbOutputData.append("\r\n");
        }
        return sbOutputData;
    }
}

