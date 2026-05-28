/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sms;

import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.actions.storage.EditTrackItem;
import java.util.Calendar;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.QueryProcessor;
import sapphire.action.BaseAction;
import sapphire.error.ErrorHandler;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class CreateSampleChild
extends BaseAction {
    static final String LABVANTAGE_CVS_ID = "$Revision: 54506 $";
    public static final String ID = "CreateSampleChild";
    public static final String VERSION = "1";
    public static final String PROPERTY_SAMPLEID = "sampleid";
    public static final String PROPERTY_COPIES = "copies";
    public static final String PROPERTY_TEMPLATEID = "templateid";
    public static final String PROPERTY_PARENTQUANTITY = "parentquantity";
    public static final String PROPERTY_UPDATEPARENTQUANTITY = "updateparentquantity";
    public static final String PROPERTY_CHILDQUANTITY = "childquantity";
    public static final String PROPERTY_CHILDUNITS = "childunits";
    public static final String PROPERTY_SAMPLETYPEID = "sampletypeid";
    public static final String PROPERTY_PREPTYPEID = "preptypeid";
    public static final String PROPERTY_TREATMENTID = "treatmentid";
    public static final String PROPERTY_GLPFLAG = "glpflag";
    public static final String PROPERTY_CHILDSAMPLEIDS = "childsampleids";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String parentTrackItemID;
        String childTrackItems;
        String sampleid = properties.getProperty(PROPERTY_SAMPLEID);
        String copies = properties.getProperty(PROPERTY_COPIES, VERSION);
        String templateid = properties.getProperty(PROPERTY_TEMPLATEID);
        String parentquantity = properties.getProperty(PROPERTY_PARENTQUANTITY);
        String updateparentqty = properties.getProperty(PROPERTY_UPDATEPARENTQUANTITY, "N");
        String childquantity = properties.getProperty(PROPERTY_CHILDQUANTITY);
        String childunits = properties.getProperty(PROPERTY_CHILDUNITS);
        String sampletypeid = properties.getProperty(PROPERTY_SAMPLETYPEID);
        String preptypeid = properties.getProperty(PROPERTY_PREPTYPEID);
        String treatmentid = properties.getProperty(PROPERTY_TREATMENTID);
        String glpflag = properties.getProperty(PROPERTY_GLPFLAG, "N");
        PropertyList sampleProps = CreateSampleChild.getSampleProps(this.getQueryProcessor(), sampleid);
        String studyid = sampleProps.getProperty("sstudyid");
        if (StringUtil.getLen(studyid) == 0L) {
            throw new SapphireException("Invalid Parent", "VALIDATION", this.getTranslationProcessor().translate("Unable to create child. Parent sample is not in any study.") + " (" + sampleid + ")");
        }
        HashMap<String, String> props = new HashMap<String, String>();
        props.put("sdcid", "Sample");
        props.put(PROPERTY_COPIES, copies);
        props.put(PROPERTY_TEMPLATEID, templateid);
        props.put("sstudyid", sampleProps.getProperty("sstudyid"));
        props.put("concentration", sampleProps.getProperty("concentration"));
        props.put("concentrationunits", sampleProps.getProperty("concentrationunits"));
        props.put("samplestatus", "Received");
        props.put("storagestatus", "In Prep");
        props.put(PROPERTY_GLPFLAG, glpflag);
        props.put("samplefamilyid", sampleProps.getProperty("samplefamilyid"));
        if ("Y".equals(updateparentqty)) {
            props.put(PROPERTY_SAMPLETYPEID, sampleProps.getProperty(PROPERTY_SAMPLETYPEID));
            props.put(PROPERTY_PREPTYPEID, sampleProps.getProperty(PROPERTY_PREPTYPEID));
        } else {
            props.put(PROPERTY_SAMPLETYPEID, sampletypeid);
            props.put(PROPERTY_PREPTYPEID, preptypeid);
        }
        try {
            this.getActionProcessor().processAction("AddSDI", VERSION, props);
            ErrorHandler errorHandler = this.getActionProcessor().getErrorHandler();
            if (errorHandler != null && errorHandler.hasInfoErrors()) {
                this.setErrors(this.getActionProcessor().getErrorHandler());
            }
        }
        catch (ActionException e) {
            this.setErrors(e.getErrorHandler());
        }
        String childsampleids = (String)props.get("newkeyid1");
        String[] child = StringUtil.split(childsampleids, ";");
        DataSet dsinsert = new DataSet();
        dsinsert.addColumn("sourcesampleid", 0);
        dsinsert.addColumn("destsampleid", 0);
        dsinsert.addColumn("createdt", 2);
        dsinsert.addColumn("createby", 0);
        dsinsert.addColumn("createtool", 0);
        for (int i = 0; i < child.length; ++i) {
            int row = dsinsert.addRow();
            dsinsert.setString(row, "sourcesampleid", sampleid);
            dsinsert.setString(row, "destsampleid", child[i]);
            dsinsert.setString(row, "createby", this.connectionInfo.getSysuserId());
            dsinsert.setString(row, "createtool", "CreateChildSample");
            dsinsert.setDate(row, "createdt", DateTimeUtil.getNowCalendar());
        }
        dsinsert.padColumns();
        DataSetUtil.insert(this.database, dsinsert, "s_samplemap");
        if (treatmentid.length() > 0) {
            Calendar c = DateTimeUtil.getNowCalendar();
            String sequenceid = c.get(2) + "" + c.get(1);
            DataSet dstreatment = new DataSet();
            dstreatment.addColumn("s_sampleid", 0);
            dstreatment.addColumn("s_sampledetailid", 0);
            dstreatment.addColumn("detailtype", 0);
            dstreatment.addColumn("detailvalue", 0);
            dstreatment.addColumn("detailsdcid", 0);
            dstreatment.addColumn("detailkeyid1", 0);
            dstreatment.addColumn("detailkeyid2", 0);
            dstreatment.addColumn("detailkeyid3", 0);
            dstreatment.addColumn("createdt", 2);
            dstreatment.addColumn("createby", 0);
            dstreatment.addColumn("createtool", 0);
            String[] childs = StringUtil.split(childsampleids, ";");
            for (int i = 0; i < childs.length; ++i) {
                String sampledetailid = sequenceid + "-" + this.getSequenceProcessor().getSequence("s_sampledetail", sequenceid);
                String childsampleid = childs[i];
                int row = dstreatment.addRow();
                dstreatment.setString(row, "s_sampleid", childsampleid);
                dstreatment.setString(row, "s_sampledetailid", sampledetailid);
                dstreatment.setString(row, "detailtype", "Treatment");
                dstreatment.setString(row, "detailvalue", "Treatment");
                dstreatment.setString(row, "detailsdcid", "LV_Treatment");
                dstreatment.setString(row, "detailkeyid1", treatmentid);
                dstreatment.setString(row, "detailkeyid2", "(null)");
                dstreatment.setString(row, "detailkeyid3", "(null)");
                dstreatment.setString(row, "createby", this.connectionInfo.getSysuserId());
                dstreatment.setString(row, "createtool", "CreateChildSample");
                dstreatment.setDate(row, "createdt", DateTimeUtil.getNowCalendar());
            }
            dstreatment.padColumns();
            DataSetUtil.insert(this.database, dstreatment, "s_sampledetail");
        }
        if ((childTrackItems = CreateSampleChild.getTrackItemID(this.getQueryProcessor(), childsampleids)) != null && childTrackItems.length() > 0) {
            props.clear();
            props.put("trackitemid", CreateSampleChild.getTrackItemID(this.getQueryProcessor(), childsampleids));
            props.put("qtycurrent", childquantity);
            props.put("qtyunits", childunits);
            props.put("custodialdepartmentid", sampleProps.getProperty("custodialdepartmentid"));
            props.put("custodialuserid", sampleProps.getProperty("custodialuserid"));
            props.put("auditreason", "Y".equals(updateparentqty) ? "Created Aliquotes" : "Created Derivatives");
            try {
                this.getActionProcessor().processActionClass(EditTrackItem.class.getName(), props, false);
                ErrorHandler errorHandler = this.getActionProcessor().getErrorHandler();
                if (errorHandler != null && errorHandler.hasInfoErrors()) {
                    this.setErrors(this.getActionProcessor().getErrorHandler());
                }
            }
            catch (ActionException e) {
                this.setErrors(e.getErrorHandler());
            }
        }
        if ((parentTrackItemID = CreateSampleChild.getTrackItemID(this.getQueryProcessor(), sampleid)) != null && parentTrackItemID.length() > 0 && "Y".equals(updateparentqty)) {
            props.clear();
            props.put("trackitemid", parentTrackItemID);
            props.put("qtycurrent", parentquantity);
            props.put("auditreason", "Created Sample Aliquotes");
            try {
                this.getActionProcessor().processActionClass(EditTrackItem.class.getName(), props, false);
                ErrorHandler errorHandler = this.getActionProcessor().getErrorHandler();
                if (errorHandler != null && errorHandler.hasInfoErrors()) {
                    this.setErrors(this.getActionProcessor().getErrorHandler());
                }
            }
            catch (ActionException e) {
                this.setErrors(e.getErrorHandler());
            }
        }
        properties.setProperty(PROPERTY_CHILDSAMPLEIDS, childsampleids);
    }

    public static PropertyList getSampleProps(QueryProcessor qp, String sampleid) {
        PropertyList props = new PropertyList();
        StringBuilder sql = new StringBuilder();
        sql.append("select s_sampleid, samplefamilyid, preptypeid, sampletypeid, sstudyid, concentration, concentrationunits, glpflag, storagestatus,");
        sql.append(" (select trackitem.custodialdepartmentid from trackitem where trackitem.linksdcid = 'Sample' and trackitem.linkkeyid1 = s_sample.s_sampleid) custodialdepartmentid,");
        sql.append(" (select trackitem.custodialuserid from trackitem where trackitem.linksdcid = 'Sample' and trackitem.linkkeyid1 = s_sample.s_sampleid) custodialuserid,");
        sql.append(" (select trackitem.qtycurrent from trackitem where trackitem.linksdcid = 'Sample' and trackitem.linkkeyid1 = s_sample.s_sampleid) qtycurrent,");
        sql.append(" (select trackitem.qtyunits from trackitem where trackitem.linksdcid = 'Sample' and trackitem.linkkeyid1 = s_sample.s_sampleid) qtyunits");
        sql.append(" from s_sample");
        sql.append(" where s_sampleid = ?");
        DataSet ds = qp.getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{sampleid});
        if (ds != null) {
            for (int i = 0; i < ds.size(); ++i) {
                props.setProperty(PROPERTY_SAMPLEID, ds.getValue(0, "s_sampleid"));
                props.setProperty("samplefamilyid", ds.getValue(0, "samplefamilyid"));
                props.setProperty(PROPERTY_PREPTYPEID, ds.getValue(0, PROPERTY_PREPTYPEID));
                props.setProperty(PROPERTY_SAMPLETYPEID, ds.getValue(0, PROPERTY_SAMPLETYPEID));
                props.setProperty("concentration", ds.getValue(0, "concentration"));
                props.setProperty("concentrationunits", ds.getValue(0, "concentrationunits"));
                props.setProperty(PROPERTY_GLPFLAG, ds.getValue(0, PROPERTY_GLPFLAG));
                props.setProperty("sstudyid", ds.getValue(0, "sstudyid"));
                props.setProperty("storagestatus", ds.getValue(0, "storagestatus"));
                props.setProperty("custodialdepartmentid", ds.getValue(0, "custodialdepartmentid"));
                props.setProperty("custodialuserid", ds.getValue(0, "custodialuserid"));
                props.setProperty("qtycurrent", ds.getValue(0, "qtycurrent"));
                props.setProperty("qtyunits", ds.getValue(0, "qtyunits"));
            }
        }
        return props;
    }

    public static String getTrackItemID(QueryProcessor qp, String sampleid) {
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder sb = new StringBuilder();
        StringBuilder sql = new StringBuilder();
        sql.append("select trackitem.trackitemid from trackitem where trackitem.linksdcid = 'Sample'");
        sql.append(" and trackitem.linkkeyid1 in (").append(safeSQL.addIn(sampleid, ";")).append(")");
        DataSet ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null) {
            for (int i = 0; i < ds.size(); ++i) {
                sb.append(ds.getValue(i, "trackitemid")).append(";");
            }
            if (sb.length() > 0) {
                sb.setLength(sb.length() - 1);
            }
        }
        return sb.toString();
    }
}

