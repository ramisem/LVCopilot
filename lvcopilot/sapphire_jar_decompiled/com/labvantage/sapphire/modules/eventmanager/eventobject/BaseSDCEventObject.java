/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eventmanager.eventobject;

import com.labvantage.sapphire.modules.eventmanager.eventobject.BaseEventItem;
import com.labvantage.sapphire.modules.eventmanager.eventobject.BaseEventObject;
import com.labvantage.sapphire.modules.eventmanager.eventobject.DataItemEventItem;
import com.labvantage.sapphire.modules.eventmanager.eventobject.DataSetEventItem;
import com.labvantage.sapphire.modules.eventmanager.eventobject.NoteEventItem;
import com.labvantage.sapphire.modules.eventmanager.eventobject.SDIEventItem;
import com.labvantage.sapphire.modules.eventmanager.eventobject.WorkItemEventItem;
import com.labvantage.sapphire.services.DDTConstants;
import com.labvantage.sapphire.services.SapphireConnection;
import java.util.ArrayList;
import sapphire.accessor.SDIProcessor;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public abstract class BaseSDCEventObject
extends BaseEventObject
implements DDTConstants {
    private String sdcid;
    private PropertyList sdcProps;
    private SDIData sdiData;
    private String[] supplementalRequestItems;
    private String rsetid;
    private PropertyList properties;
    private StringBuffer rsetkeyid1 = new StringBuffer();
    private StringBuffer rsetkeyid2 = new StringBuffer();
    private StringBuffer rsetkeyid3 = new StringBuffer();

    public BaseSDCEventObject(String eventid, Object[] eventData, String sdcid, PropertyList sdcProps, PropertyList properties, SDIData sdiData) {
        super(eventid, eventData);
        this.sdcid = sdcid;
        this.sdcProps = sdcProps;
        this.properties = properties;
        this.sdiData = sdiData;
    }

    public String getSdcid() {
        return this.sdcid;
    }

    public PropertyList getSdcProps() {
        return this.sdcProps;
    }

    public void setSdcProps(PropertyList sdcProps) {
        this.sdcProps = sdcProps;
    }

    public SDIData getSDIData() {
        return this.sdiData;
    }

    public void setSDIData(SDIData sdiData) {
        this.sdiData = sdiData;
    }

    public String[] getSupplementalRequestItems() {
        return this.supplementalRequestItems;
    }

    public void setSupplementalRequestItems(String[] supplementalRequestItems) {
        this.supplementalRequestItems = supplementalRequestItems;
    }

    public void addRequestItems(SDIRequest sdiRequest) {
        if (this.getSupplementalRequestItems() != null) {
            for (String requestItem : this.getSupplementalRequestItems()) {
                sdiRequest.setRequestItem(requestItem);
            }
        }
    }

    public void setProperties(PropertyList properties) {
        this.properties = properties;
    }

    public PropertyList getProperties() {
        return this.properties;
    }

    public String getRsetid() {
        return this.rsetid;
    }

    public void setRsetid(String rsetid) {
        this.rsetid = rsetid;
    }

    public DataSet getDataSet(String dataset) {
        SDIData sdiData = this.getSDIData();
        return sdiData != null ? sdiData.getDataset(dataset) : null;
    }

    public DataSet getDataSet(String dataset, boolean loadIfMissing, SapphireConnection sapphireConnection) {
        DataSet data = this.getDataSet(dataset);
        if (data == null && loadIfMissing && sapphireConnection != null) {
            SDIRequest sdiRequest = new SDIRequest();
            if (this.rsetid != null && this.rsetid.length() > 0) {
                sdiRequest.setSDCid(this.sdcid);
                sdiRequest.setRsetid(this.rsetid);
                sdiRequest.setRetainRsetid(true);
            } else {
                sdiRequest.setSDIList(this.sdcid, this.rsetkeyid1.length() > 0 ? this.rsetkeyid1.substring(1) : "", this.rsetkeyid2.length() > 0 ? this.rsetkeyid2.substring(1) : "", this.rsetkeyid3.length() > 0 ? this.rsetkeyid3.substring(1) : "");
            }
            sdiRequest.setRequestItem(dataset);
            sdiRequest.setExtendedDataTypes(true);
            SDIData datasetData = new SDIProcessor(sapphireConnection.getConnectionId()).getSDIData(sdiRequest);
            return datasetData != null ? datasetData.getDataset(dataset) : null;
        }
        return data;
    }

    public DataSet getSDIEventPlanData(SapphireConnection sapphireConnection) {
        DataSet sdieventplan;
        SDIData sdiData = this.getSDIData();
        Object o = this.getSupplementalData();
        SDIData supplementalData = o instanceof SDIData ? (SDIData)o : null;
        DataSet dataSet = sdieventplan = sdiData != null ? sdiData.getDataset("sdieventplan") : null;
        if (sdieventplan == null) {
            SDIRequest sdiRequest = new SDIRequest();
            if (this.rsetid != null && this.rsetid.length() > 0) {
                sdiRequest.setSDCid(this.sdcid);
                sdiRequest.setRsetid(this.rsetid);
                sdiRequest.setRetainRsetid(true);
            } else {
                sdiRequest.setSDIList(this.sdcid, this.rsetkeyid1.length() > 0 ? this.rsetkeyid1.substring(1) : "", this.rsetkeyid2.length() > 0 ? this.rsetkeyid2.substring(1) : "", this.rsetkeyid3.length() > 0 ? this.rsetkeyid3.substring(1) : "");
            }
            sdiRequest.setRequestItem("sdieventplan");
            sdiRequest.setRequestItem("sdieventplanitem");
            sdiRequest.setRequestItem("sdieventplanitemproperty");
            sdiRequest.setExtendedDataTypes(false);
            SDIData sdieventplanData = new SDIProcessor(sapphireConnection.getConnectionId()).getSDIData(sdiRequest);
            sdieventplan = sdieventplanData.getDataset("sdieventplan");
            if (sdiData == null) {
                sdiData = new SDIData();
                this.setSDIData(sdiData);
            }
            sdiData.setDataset("sdieventplan", sdieventplan);
            sdiData.setDataset("sdieventplanitem", sdieventplanData.getDataset("sdieventplanitem"));
            sdiData.setDataset("sdieventplanitemproperty", sdieventplanData.getDataset("sdieventplanitemproperty"));
        } else if (sdieventplan.size() == 0 && supplementalData != null && supplementalData.getDataset("sdieventplan") != null) {
            sdieventplan = supplementalData.getDataset("sdieventplan");
        }
        return sdieventplan;
    }

    public int addEventItemsFromPrimary() {
        return this.addEventItemsFromPrimary(null);
    }

    public int addEventItemsFromPrimary(ArrayList<BaseEventItem> origEventItems) {
        DataSet primary = this.getDataSet("primary");
        this.setEventItemType(1);
        if (primary != null && primary.size() > 0) {
            int keyCols = Integer.parseInt(this.sdcProps.getProperty("keycolumns"));
            for (int i = 0; i < primary.size(); ++i) {
                SDIEventItem eventItem;
                if (!primary.getValue(i, "templateflag", "N").equals("N")) continue;
                String keyid1 = primary.getValue(i, this.sdcProps.getProperty("keycolid1"));
                this.rsetkeyid1.append(";").append(keyid1);
                if (keyCols == 1) {
                    eventItem = new SDIEventItem(this.getSdcid(), keyid1);
                } else if (keyCols == 2) {
                    String keyid2 = primary.getValue(i, this.sdcProps.getProperty("keycolid2"));
                    this.rsetkeyid2.append(";").append(keyid2);
                    eventItem = new SDIEventItem(this.getSdcid(), primary.getValue(i, this.sdcProps.getProperty("keycolid1")), primary.getValue(i, this.sdcProps.getProperty("keycolid2")));
                } else {
                    String keyid3 = primary.getValue(i, this.sdcProps.getProperty("keycolid3"));
                    this.rsetkeyid3.append(";").append(keyid3);
                    eventItem = new SDIEventItem(this.getSdcid(), primary.getValue(i, this.sdcProps.getProperty("keycolid1")), primary.getValue(i, this.sdcProps.getProperty("keycolid2")), primary.getValue(i, this.sdcProps.getProperty("keycolid3")));
                }
                eventItem.setDataIndex(i);
                if (origEventItems != null) {
                    eventItem.addEventPlan(origEventItems);
                }
                this.addEventItem(eventItem);
            }
        }
        return this.getEventItems().size();
    }

    public int addEventItemsFromNotes() {
        DataSet sdidata = this.getDataSet("notes");
        this.setEventItemType(5);
        if (sdidata != null && sdidata.size() > 0) {
            int keyCols = Integer.parseInt(this.sdcProps.getProperty("keycolumns"));
            for (int i = 0; i < sdidata.size(); ++i) {
                NoteEventItem eventItem = new NoteEventItem(this.getSdcid(), sdidata.getValue(i, "keyid1"), sdidata.getValue(i, "keyid2"), sdidata.getValue(i, "keyid3"), sdidata.getInt(i, "notenum"));
                this.addRsetkeyids(sdidata, i, keyCols);
                eventItem.setDataIndex(i);
                this.addEventItem(eventItem);
            }
        }
        return this.getEventItems().size();
    }

    public int addEventItemsFromDatasets() {
        DataSet sdidata = this.getDataSet("dataset");
        this.setEventItemType(2);
        if (sdidata != null && sdidata.size() > 0) {
            int keyCols = Integer.parseInt(this.sdcProps.getProperty("keycolumns"));
            for (int i = 0; i < sdidata.size(); ++i) {
                DataSetEventItem eventItem = new DataSetEventItem(this.getSdcid(), sdidata.getValue(i, "keyid1"), sdidata.getValue(i, "keyid2"), sdidata.getValue(i, "keyid3"), sdidata.getValue(i, "paramlistid"), sdidata.getValue(i, "paramlistversionid"), sdidata.getValue(i, "variantid"), sdidata.getInt(i, "dataset"));
                this.addRsetkeyids(sdidata, i, keyCols);
                eventItem.setDataIndex(i);
                this.addEventItem(eventItem);
            }
        }
        return this.getEventItems().size();
    }

    public int addEventItemsFromDataitems() {
        DataSet sdidataitem = this.getDataSet("dataitem");
        this.setEventItemType(3);
        if (sdidataitem != null && sdidataitem.size() > 0) {
            int keyCols = Integer.parseInt(this.sdcProps.getProperty("keycolumns"));
            for (int i = 0; i < sdidataitem.size(); ++i) {
                DataItemEventItem eventItem = new DataItemEventItem(this.getSdcid(), sdidataitem.getValue(i, "keyid1"), sdidataitem.getValue(i, "keyid2"), sdidataitem.getValue(i, "keyid3"), sdidataitem.getValue(i, "paramlistid"), sdidataitem.getValue(i, "paramlistversionid"), sdidataitem.getValue(i, "variantid"), sdidataitem.getInt(i, "dataset"), sdidataitem.getValue(i, "paramid"), sdidataitem.getValue(i, "paramtype"), sdidataitem.getInt(i, "replicateid"));
                this.addRsetkeyids(sdidataitem, i, keyCols);
                eventItem.setDataIndex(i);
                this.addEventItem(eventItem);
            }
        }
        return this.getEventItems().size();
    }

    public int addEventItemsFromWorkitems() {
        DataSet sdiworkitem = this.getDataSet("sdiworkitem");
        this.setEventItemType(4);
        if (sdiworkitem != null && sdiworkitem.size() > 0) {
            int keyCols = Integer.parseInt(this.sdcProps.getProperty("keycolumns"));
            for (int i = 0; i < sdiworkitem.size(); ++i) {
                WorkItemEventItem eventItem = new WorkItemEventItem(this.getSdcid(), sdiworkitem.getValue(i, "keyid1"), sdiworkitem.getValue(i, "keyid2"), sdiworkitem.getValue(i, "keyid3"), sdiworkitem.getValue(i, "workitemid"), sdiworkitem.getValue(i, "workitemversionid"), sdiworkitem.getInt(i, "workiteminstance"));
                this.addRsetkeyids(sdiworkitem, i, keyCols);
                eventItem.setDataIndex(i);
                this.addEventItem(eventItem);
            }
        }
        return this.getEventItems().size();
    }

    private void addRsetkeyids(DataSet dataset, int row, int keyCols) {
        if (keyCols >= 1) {
            String keyid1 = dataset.getValue(row, "keyid1");
            if (this.rsetkeyid1.indexOf(";" + keyid1) == -1) {
                this.rsetkeyid1.append(";").append(keyid1);
            }
        }
        if (keyCols >= 2) {
            String keyid2 = dataset.getValue(row, "keyid2");
            if (this.rsetkeyid2.indexOf(";" + keyid2) == -1) {
                this.rsetkeyid2.append(";").append(keyid2);
            }
        }
        if (keyCols >= 3) {
            String keyid3 = dataset.getValue(row, "keyid3");
            if (this.rsetkeyid3.indexOf(";" + keyid3) == -1) {
                this.rsetkeyid3.append(";").append(keyid3);
            }
        }
    }

    public int addEventItemsFromProperties() {
        this.setEventItemType(1);
        PropertyList properties = this.getProperties();
        String[] keyid1list = StringUtil.split(properties.getProperty("keyid1"), ";");
        String[] keyid2list = StringUtil.split(properties.getProperty("keyid2"), ";");
        String[] keyid3list = StringUtil.split(properties.getProperty("keyid3"), ";");
        PropertyList sdcProps = this.getSdcProps();
        int keyCols = Integer.parseInt(sdcProps.getProperty("keycolumns"));
        for (int i = 0; i < keyid1list.length; ++i) {
            SDIEventItem eventItem;
            this.rsetkeyid1.append(";").append(keyid1list[i]);
            if (keyCols == 1) {
                eventItem = new SDIEventItem(this.getSdcid(), keyid1list[i]);
            } else if (keyCols == 2) {
                this.rsetkeyid2.append(";").append(keyid2list[i]);
                eventItem = new SDIEventItem(this.getSdcid(), keyid1list[i], keyid2list[i]);
            } else {
                this.rsetkeyid3.append(";").append(keyid3list[i]);
                eventItem = new SDIEventItem(this.getSdcid(), keyid1list[i], keyid2list[i], keyid3list[i]);
            }
            this.addEventItem(eventItem);
        }
        return this.getEventItems().size();
    }

    public void addSDIEventPlan(String eventplanid, String eventplanversionid, String keyid1, String keyid2, String keyid3) {
        ArrayList<BaseEventItem> eventItems = this.getEventItems();
        for (int i = 0; i < eventItems.size(); ++i) {
            SDIEventItem sdiEventItem = (SDIEventItem)eventItems.get(i);
            if (!sdiEventItem.matches(keyid1, keyid2, keyid3)) continue;
            sdiEventItem.addEventPlan(eventplanid, eventplanversionid);
            if (!(sdiEventItem instanceof DataItemEventItem) && !(sdiEventItem instanceof WorkItemEventItem)) break;
        }
    }
}

