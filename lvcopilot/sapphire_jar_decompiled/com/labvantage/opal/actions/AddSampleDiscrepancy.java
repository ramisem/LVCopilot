/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.actions;

import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import java.util.Calendar;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class AddSampleDiscrepancy
extends BaseAction {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public void processAction(PropertyList props) throws SapphireException {
        DataSet ds;
        String sysuserid;
        String sampleid = props.getProperty("sampleid");
        String discrepancy = props.getProperty("discrepancy");
        if (StringUtil.getLen(sampleid) > 0L && StringUtil.getLen(discrepancy) > 0L) {
            sysuserid = this.getConnectionProcessor().getSapphireConnection().getSysuserId();
            Calendar c = Calendar.getInstance();
            String sequenceid = c.get(5) + "" + c.get(2) + "" + c.get(1);
            PropertyList sequence = new PropertyList();
            StringBuilder sql = new StringBuilder();
            SafeSQL safeSQL = new SafeSQL();
            if (StringUtil.split(sampleid, ";").length > 750) {
                String rsetid = this.getDAMProcessor().createRSet("Sample", sampleid, null, null);
                if (StringUtil.getLen(rsetid) <= 0L) {
                    throw new SapphireException("Unable to create RSET");
                }
                sql.append("select s_sampleid, max(usersequence) + 1 usersequence");
                sql.append(" from s_sampledetail");
                sql.append(" where s_sampleid in ( select r.keyid1 from rsetitems r where r.rsetid = ").append(safeSQL.addVar(rsetid)).append(" )");
                sql.append(" and detailtype = 'Deviation'");
                sql.append(" group by s_sampleid");
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                this.getDAMProcessor().clearRSet(rsetid);
            } else {
                sql.append("select s_sampleid, max(usersequence) + 1 usersequence");
                sql.append(" from s_sampledetail");
                sql.append(" where s_sampleid in ( ").append(safeSQL.addIn(sampleid, ";")).append(" )");
                sql.append(" and detailtype = 'Deviation'");
                sql.append(" group by s_sampleid");
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            }
            if (ds != null && ds.size() > 0) {
                for (int i = 0; i < ds.size(); ++i) {
                    sequence.setProperty(ds.getValue(i, "s_sampleid"), ds.getValue(i, "usersequence"));
                }
            }
            ds = new DataSet();
            ds.addColumn("s_sampleid", 0);
            ds.addColumn("s_sampledetailid", 0);
            ds.addColumn("detailtype", 0);
            ds.addColumn("detailvalue", 0);
            ds.addColumn("createdt", 2);
            ds.addColumn("createby", 0);
            ds.addColumn("createtool", 0);
            ds.addColumn("usersequence", 0);
            String[] samples = StringUtil.split(sampleid, ";");
            for (int i = 0; i < samples.length; ++i) {
                String sampledetailid = sequenceid + "-" + this.getSequenceProcessor().getSequence("s_sampledetail", sequenceid);
                int row = ds.addRow();
                ds.setString(row, "s_sampleid", samples[i]);
                ds.setString(row, "s_sampledetailid", sampledetailid);
                ds.setString(row, "detailvalue", discrepancy);
                ds.setValue(row, "usersequence", sequence.getProperty(samples[i], "1"));
            }
        } else {
            throw new SapphireException(this.getTranslationProcessor().translate("Invalid action input for adding Discrepancies"));
        }
        ds.setString(-1, "detailtype", "Deviation");
        ds.setDate(-1, "createdt", DateTimeUtil.getNowCalendar());
        ds.setString(-1, "createby", sysuserid);
        ds.setString(-1, "createtool", "AJAX");
        DataSetUtil.insert(this.database, ds, "s_sampledetail");
    }
}

