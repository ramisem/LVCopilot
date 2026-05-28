/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.actions.paramlist;

import com.labvantage.opal.actions.paramlist.SyncCrossSDICalcInfoForParamList;
import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class SyncSDIDataCrossSDICalc
extends BaseAction {
    public static final String ID = "SyncSDIDataCrossSDICalc";
    public static final String VERSIONID = "1";
    List<String> paramlistids = new ArrayList<String>();
    List<String> paramlistversionids = new ArrayList<String>();
    List<String> variantids = new ArrayList<String>();
    List<String> fromsdcs = new ArrayList<String>();
    List<String> datasetnums = new ArrayList<String>();

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String separator = properties.getProperty("separator", properties.getProperty("delimeter", ";"));
        String sdcid = properties.getProperty("sdcid", "Sample");
        String templates = properties.getProperty("keyid1");
        try {
            String[] templateVals = StringUtil.split(templates, separator);
            String[] sdcVals = StringUtil.split(sdcid, separator);
            for (int i = 0; i < templateVals.length; ++i) {
                SDIRequest sdiRequest = new SDIRequest();
                sdiRequest.setRequestItem("primary");
                sdiRequest.setRequestItem("dataset");
                sdiRequest.setSDCid("Sample");
                sdiRequest.setShowTemplates(true);
                sdiRequest.setKeyid1List(templateVals[i]);
                sdiRequest.setQueryWhere(" s_sampleid = '" + templateVals[i] + "'");
                sdiRequest.setQueryFrom("s_sample");
                SDIData sdidata = this.getSDIProcessor().getSDIData(sdiRequest);
                DataSet dataset = sdidata.getDataset("dataset");
                DataSet paramlistcrosssdicalcs = null;
                DataSet sdidatacrosssdicalcs = null;
                for (int count = 0; count < dataset.size(); ++count) {
                    String paramlistid = dataset.getString(count, "paramlistid");
                    String paramlistversionid = dataset.getString(count, "paramlistversionid");
                    String variantid = dataset.getString(count, "variantid");
                    int datasetnum = dataset.getInt(count, "dataset");
                    DataSet paramlist = this.getParamListData(paramlistid, paramlistversionid, variantid);
                    paramlistcrosssdicalcs = this.fetchParamlistCrossSDICalc(paramlistid, paramlistversionid, variantid);
                    sdidatacrosssdicalcs = this.fetchSDIDataCrossSDICalc(sdcVals[i], templateVals[i], "(null)", "(null)", paramlistid, paramlistversionid, variantid, datasetnum);
                    if (!OpalUtil.isEmpty(sdidatacrosssdicalcs) || !paramlist.getValue(0, "enableautoredocalcflag", "").equalsIgnoreCase("Y")) continue;
                    if (OpalUtil.isNotEmpty(paramlistcrosssdicalcs)) {
                        this.addDataset(sdcVals[i], templateVals[i], "(null)", "(null)", paramlistid, paramlistversionid, variantid, String.valueOf(datasetnum), paramlistcrosssdicalcs);
                        continue;
                    }
                    this.paramlistids.add(paramlistid);
                    this.paramlistversionids.add(paramlistversionid);
                    this.variantids.add(variantid);
                    this.fromsdcs.add(paramlist.getValue(0, "targetsdcid", "Sample"));
                    this.datasetnums.add(String.valueOf(datasetnum));
                }
                if (!this.needToSync()) continue;
                PropertyList props = new PropertyList();
                props.setProperty("paramlistid", String.join((CharSequence)"~", this.paramlistids));
                props.setProperty("paramlistversionid", String.join((CharSequence)"~", this.paramlistversionids));
                props.setProperty("variantid", String.join((CharSequence)"~", this.variantids));
                props.setProperty("fromsdc", String.join((CharSequence)"~", this.fromsdcs));
                props.setProperty("separator", "~");
                this.getActionProcessor().processActionClass(SyncCrossSDICalcInfoForParamList.class.getName(), props, true);
                for (int j = 0; j < this.paramlistids.size(); ++j) {
                    paramlistcrosssdicalcs = this.fetchParamlistCrossSDICalc(this.paramlistids.get(j), this.paramlistversionids.get(j), this.variantids.get(j));
                    this.addDataset(sdcid, templateVals[i], "(null)", "(null)", this.paramlistids.get(j), this.paramlistversionids.get(j), this.variantids.get(j), this.datasetnums.get(j), paramlistcrosssdicalcs);
                    paramlistcrosssdicalcs.clear();
                }
            }
        }
        catch (Exception e) {
            throw new SapphireException("PROCESSACTION_FAILED", "SyncSDIDataCrossSDICalc " + this.getTranslationProcessor().translate("Action failed:") + " " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
    }

    private DataSet getParamListData(String paramlistid, String paramlistversionid, String variantid) {
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setRequestItem("primary");
        sdiRequest.setSDCid("ParamList");
        sdiRequest.setKeyid1List(paramlistid);
        sdiRequest.setKeyid2List(paramlistversionid);
        sdiRequest.setKeyid3List(variantid);
        SDIData sdidata = this.getSDIProcessor().getSDIData(sdiRequest);
        return sdidata.getDataset("primary");
    }

    private DataSet fetchSDIDataCrossSDICalc(String sdcid, String keyid1, String keyid2, String keyid3, String paramlistid, String paramlistversionid, String variantid, int datasetnum) {
        DataSet sdidatacrosssdicalcs = new DataSet();
        String selectsdidatacrosssdicalc = "SELECT\t* FROM sdidatacrosssdicalc WHERE\tsdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND paramlistid = ? AND paramlistversionid = ? AND variantid = ? AND dataset = ?";
        Object[] paramlistBindVars = new String[]{sdcid, keyid1, keyid2, keyid3, paramlistid, paramlistversionid, variantid, String.valueOf(datasetnum)};
        sdidatacrosssdicalcs = this.getQueryProcessor().getPreparedSqlDataSet("SelectsdidataCrosssdiCalcRules", selectsdidatacrosssdicalc, paramlistBindVars);
        return sdidatacrosssdicalcs;
    }

    private DataSet fetchParamlistCrossSDICalc(String paramlistid, String paramlistversionid, String variantid) throws SapphireException {
        DataSet paramlistcrosssdicalcs = new DataSet();
        String selectparamlistcrosssdicalc = "SELECT\t* FROM paramlistcrosssdicalc WHERE\tparamlistid = ? AND \t\tparamlistversionid = ? AND \t\tvariantid = ?";
        Object[] paramlistBindVars = new String[]{paramlistid, paramlistversionid, variantid};
        paramlistcrosssdicalcs = this.getQueryProcessor().getPreparedSqlDataSet("SelectparamlistCrosssdiCalcRules", selectparamlistcrosssdicalc, paramlistBindVars);
        return paramlistcrosssdicalcs;
    }

    private void addDataset(String sdcid, String keyid1, String keyid2, String keyid3, String paramlistid, String paramlistversionid, String variantid, String datasetnum, DataSet paramlistcrosssdicalcs) throws SapphireException {
        DataSet sdidatacrosssdicalc = new DataSet(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()));
        this.addSDIDataCrossSDICalcRules(sdidatacrosssdicalc, paramlistcrosssdicalcs, sdcid, keyid1, keyid2, keyid3, paramlistid, paramlistversionid, variantid, datasetnum, DateTimeUtil.getNowCalendar());
        DataSetUtil.insert(this.database, sdidatacrosssdicalc, "sdidatacrosssdicalc");
        this.logger.info("Done insert sdidatacrosssdicalc");
    }

    private boolean needToSync() {
        return OpalUtil.isNotEmpty(this.paramlistids) && OpalUtil.isNotEmpty(this.paramlistversionids) && OpalUtil.isNotEmpty(this.variantids) && OpalUtil.isNotEmpty(this.fromsdcs);
    }

    private void addSDIDataCrossSDICalcRules(DataSet sdidatacrosssdicalc, DataSet paramlistcrosssdicalcs, String sdcid, String keyid1, String keyid2, String keyid3, String paramlistid, String paramlistversionid, String variantid, String datasetnum, Calendar now) {
        this.logger.info("Adding SDI Data CrossSDI Calculation rule  details...");
        for (int i = 0; i < paramlistcrosssdicalcs.size(); ++i) {
            int newRow = sdidatacrosssdicalc.addRow();
            sdidatacrosssdicalc.setString(newRow, "sdcid", sdcid);
            sdidatacrosssdicalc.setString(newRow, "keyid1", keyid1);
            sdidatacrosssdicalc.setString(newRow, "keyid2", keyid2);
            sdidatacrosssdicalc.setString(newRow, "keyid3", keyid3);
            sdidatacrosssdicalc.setString(newRow, "paramlistid", paramlistid);
            sdidatacrosssdicalc.setString(newRow, "paramlistversionid", paramlistversionid);
            sdidatacrosssdicalc.setString(newRow, "variantid", variantid);
            sdidatacrosssdicalc.setNumber(newRow, "dataset", Integer.valueOf(datasetnum));
            sdidatacrosssdicalc.setString(newRow, "crosssdicalcdefid", paramlistcrosssdicalcs.getValue(i, "crosssdicalcdefid"));
            sdidatacrosssdicalc.setDate(newRow, "createdt", now);
            sdidatacrosssdicalc.setString(newRow, "createby", this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()).getSysuserId());
            sdidatacrosssdicalc.setString(newRow, "createtool", this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()).getTool());
            sdidatacrosssdicalc.setDate(newRow, "moddt", now);
            sdidatacrosssdicalc.setString(newRow, "modby", this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()).getSysuserId());
            sdidatacrosssdicalc.setString(newRow, "modtool", this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()).getTool());
        }
    }
}

