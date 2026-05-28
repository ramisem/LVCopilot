/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.pageelements.gwt.shared.ArrayConstants;
import com.labvantage.sapphire.util.array.ArrayUtil;
import java.util.List;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.SDIProcessor;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class LV_ArrayLayout
extends BaseSDCRules
implements ArrayConstants {
    static final String LABVANTAGE_CVS_ID = "1: 1.1 $";
    private static final String arraylayoutid = "arraylayoutid";
    private static final String arraylayoutversionid = "arraylayoutversionid";
    private static final String xpos = "xpos";
    private static final String ypos = "ypos";
    private static final String sdcid = "sdcid";
    private static final String horizontalLabel = "horizontallabel";
    private static final String verticalLabel = "verticallabel";
    private static final String sdc = "LV_ArrayLayout";
    private static final String link = "Array Layout Items";
    private static final String zonelink = "Array Layout Zones";
    private static final String zonedetaillink = "Zone Items";

    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        if (!this.isCMTImport()) {
            String tempkey1 = actionProps.getProperty("templatekeyid1", "");
            String tempid = actionProps.getProperty("templateid", "");
            if (StringUtil.getLen(tempkey1) == 0L && StringUtil.getLen(tempid) == 0L) {
                this.populateArrayLayoutItem(sdiData);
                this.addDefaultZone(sdiData);
            }
        }
    }

    @Override
    public void postAddDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        String layoutzone;
        DataSet arraylayoutzone = sdiData.getDataset("arraylayoutzone");
        if (arraylayoutzone != null && !arraylayoutzone.isEmpty() && arraylayoutzone.size() == 1 && "(FullArray)".equalsIgnoreCase(layoutzone = arraylayoutzone.getValue(0, "arraylayoutzone"))) {
            String arraylayoutid = arraylayoutzone.getValue(0, arraylayoutid);
            String arraylayoutversionid = arraylayoutzone.getValue(0, arraylayoutversionid);
            DataSet arraylayoutzoneitemDS = new DataSet();
            arraylayoutzoneitemDS.addColumn(xpos, 1);
            arraylayoutzoneitemDS.addColumn(ypos, 1);
            arraylayoutzoneitemDS.addColumn("arraylayoutzone", 0);
            arraylayoutzoneitemDS.addColumn("contentstring", 0);
            String sql = "select arraytypeid, arraytypeversionid from arraylayout where arraylayoutid = ? and arraylayoutversionid = ?";
            DataSet arraytypeDS = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{arraylayoutid, arraylayoutversionid});
            String arraytypeid = arraytypeDS.getValue(0, "arraytypeid");
            String arraytypeversionid = arraytypeDS.getValue(0, "arraytypeversionid");
            SDIProcessor sdiProcessor = this.getSDIProcessor();
            SDIRequest sdiRequest = new SDIRequest();
            sdiRequest.setSDCid("LV_ArrayType");
            sdiRequest.setRequestItem("primary");
            sdiRequest.setKeyid1List(arraytypeid);
            sdiRequest.setKeyid2List(arraytypeversionid);
            SDIData data = sdiProcessor.getSDIData(sdiRequest);
            DataSet dataset = data.getDataset("primary");
            int rows = (int)Float.parseFloat(dataset.getValue(0, "numrows"));
            int cols = (int)Float.parseFloat(dataset.getValue(0, "numcolumns"));
            int count = 0;
            for (int i = 0; i < rows; ++i) {
                for (int j = 0; j < cols; ++j) {
                    ++count;
                    int rowindex = arraylayoutzoneitemDS.addRow();
                    arraylayoutzoneitemDS.setNumber(rowindex, xpos, i);
                    arraylayoutzoneitemDS.setNumber(rowindex, ypos, j);
                    arraylayoutzoneitemDS.setString(rowindex, "arraylayoutzone", layoutzone);
                    arraylayoutzoneitemDS.setString(rowindex, "contentstring", count + ";0;0;0;0");
                    arraylayoutzoneitemDS.setString(rowindex, "usersequence", String.valueOf(count));
                }
            }
            PropertyList arraylayoutzoneitemprop = new PropertyList();
            arraylayoutzoneitemprop.setProperty("keyid1", arraylayoutid);
            arraylayoutzoneitemprop.setProperty("keyid2", arraylayoutversionid);
            arraylayoutzoneitemprop.setProperty(sdcid, sdc);
            arraylayoutzoneitemprop.setProperty("linkid", zonelink);
            arraylayoutzoneitemprop.setProperty("detaillinkid", zonedetaillink);
            arraylayoutzoneitemprop.setProperty("separator", "#");
            arraylayoutzoneitemprop.setProperty(xpos, arraylayoutzoneitemDS.getColumnValues(xpos, "#"));
            arraylayoutzoneitemprop.setProperty(ypos, arraylayoutzoneitemDS.getColumnValues(ypos, "#"));
            arraylayoutzoneitemprop.setProperty("arraylayoutzone", layoutzone);
            arraylayoutzoneitemprop.setProperty("contentstring", arraylayoutzoneitemDS.getColumnValues("contentstring", "#"));
            arraylayoutzoneitemprop.setProperty("usersequence", arraylayoutzoneitemDS.getColumnValues("usersequence", "#"));
            this.getActionProcessor().processAction("AddSDIDetail", "1", arraylayoutzoneitemprop);
        }
    }

    private void addDefaultZone(SDIData sdiData) throws ActionException {
        DataSet primary = sdiData.getDataset("primary");
        String arraylayoutid = primary.getValue(0, arraylayoutid);
        String arraylayoutversionid = primary.getValue(0, arraylayoutversionid);
        PropertyList arraylayoutzoneprop = new PropertyList();
        arraylayoutzoneprop.setProperty("keyid1", arraylayoutid);
        arraylayoutzoneprop.setProperty("keyid2", arraylayoutversionid);
        arraylayoutzoneprop.setProperty(sdcid, sdc);
        arraylayoutzoneprop.setProperty("linkid", zonelink);
        arraylayoutzoneprop.setProperty("arraylayoutzone", "(FullArray)");
        arraylayoutzoneprop.setProperty("usersequence", "1");
        this.getActionProcessor().processAction("AddSDIDetail", "1", arraylayoutzoneprop);
    }

    private void populateArrayLayoutItem(SDIData sdiData) throws ActionException {
        DataSet primary = sdiData.getDataset("primary");
        String arraylayoutid = primary.getValue(0, arraylayoutid);
        String arraylayoutversionid = primary.getValue(0, arraylayoutversionid);
        String arraytypeid = primary.getValue(0, "arraytypeid");
        String arraytypeversionid = primary.getValue(0, "arraytypeversionid");
        SDIProcessor sdiProcessor = this.getSDIProcessor();
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDCid("LV_ArrayType");
        sdiRequest.setRequestItem("primary");
        sdiRequest.setKeyid1List(arraytypeid);
        sdiRequest.setKeyid2List(arraytypeversionid);
        SDIData data = sdiProcessor.getSDIData(sdiRequest);
        DataSet dataset = data.getDataset("primary");
        int rows = (int)Float.parseFloat(dataset.getValue(0, "numrows"));
        int cols = (int)Float.parseFloat(dataset.getValue(0, "numcolumns"));
        String horlbltype = dataset.getValue(0, "horizontallabeltype");
        String horlbldir = dataset.getValue(0, "horizontallabeldirection");
        String horlblstart = dataset.getValue(0, "horizontallabelstart");
        String verlbltype = dataset.getValue(0, "verticallabeltype");
        String verlbldir = dataset.getValue(0, "verticallabeldirection");
        String verlblstart = dataset.getValue(0, "verticallabelstart");
        DataSet arraylayoutitem = new DataSet();
        arraylayoutitem.addColumn(arraylayoutid, 0);
        arraylayoutitem.addColumn(arraylayoutversionid, 0);
        arraylayoutitem.addColumn(sdcid, 0);
        arraylayoutitem.addColumn(xpos, 1);
        arraylayoutitem.addColumn(ypos, 1);
        arraylayoutitem.addColumn(horizontalLabel, 0);
        arraylayoutitem.addColumn(verticalLabel, 0);
        List colLabels = ArrayUtil.generateLabel(horlbltype, horlblstart, horlbldir, cols);
        List rowLabels = ArrayUtil.generateLabel(verlbltype, verlblstart, verlbldir, rows);
        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < cols; ++j) {
                int rowindex = arraylayoutitem.addRow();
                arraylayoutitem.setString(rowindex, arraylayoutid, arraylayoutid);
                arraylayoutitem.setString(rowindex, arraylayoutversionid, arraylayoutversionid);
                arraylayoutitem.setString(rowindex, sdcid, sdc);
                arraylayoutitem.setNumber(rowindex, xpos, i);
                arraylayoutitem.setNumber(rowindex, ypos, j);
                arraylayoutitem.setString(rowindex, horizontalLabel, String.valueOf(colLabels.get(j)));
                arraylayoutitem.setString(rowindex, verticalLabel, String.valueOf(rowLabels.get(i)));
            }
        }
        PropertyList arraylayoutitemprop = new PropertyList();
        arraylayoutitemprop.setProperty("keyid1", arraylayoutid);
        arraylayoutitemprop.setProperty("keyid2", arraylayoutversionid);
        arraylayoutitemprop.setProperty(sdcid, sdc);
        arraylayoutitemprop.setProperty("linkid", link);
        arraylayoutitemprop.setProperty(xpos, arraylayoutitem.getColumnValues(xpos, ";"));
        arraylayoutitemprop.setProperty(ypos, arraylayoutitem.getColumnValues(ypos, ";"));
        arraylayoutitemprop.setProperty(horizontalLabel, arraylayoutitem.getColumnValues(horizontalLabel, ";"));
        arraylayoutitemprop.setProperty(verticalLabel, arraylayoutitem.getColumnValues(verticalLabel, ";"));
        this.getActionProcessor().processAction("AddSDIDetail", "1", arraylayoutitemprop);
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        if (this.database.getPreparedCount("select count(su.storageunitid) from storageunit su, rsetitems r where su.arraylayoutid = r.keyid1 and su.arraylayoutversionid = r.keyid2 and r.rsetid=?", new String[]{rsetid}) > 0) {
            throw new SapphireException(this.getTranslationProcessor().translate("Delete not allowed"), "VALIDATION", this.getTranslationProcessor().translate("Storage units founds for the selected Array Layout"));
        }
    }
}

