/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.stability;

import com.labvantage.sapphire.xml.SapphireSaxHandler;
import java.util.ArrayList;
import java.util.Properties;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ConditionAxisAuditHandler
extends SapphireSaxHandler {
    private DataSet currentDataSet;
    private String currentDataSetColumnId = "";
    private int currentDataSetRow = -1;
    private boolean CDATAEncountered;
    private boolean inDetails = false;
    private boolean inWorkitems = false;
    private boolean inTrackitems = false;
    private DataSet detailsDataSet;
    private DataSet trackitemsDataSet;
    private int propertyListIndex = 0;
    private StringBuffer propertyListBuffer = new StringBuffer();
    private String currentPropertyType;
    private StringBuffer currentElementChars = new StringBuffer();
    private ArrayList planItems = new ArrayList();
    private PlanItem currentPlanItem;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        Properties attr = this.getAttributes(attributes);
        this.log("Begin parsing " + qName);
        if (qName.equalsIgnoreCase("DETAILS")) {
            this.inDetails = true;
        }
        if (qName.equalsIgnoreCase("WORKITEMS")) {
            this.inWorkitems = true;
        }
        if (qName.equalsIgnoreCase("TRACKITEMS")) {
            this.inTrackitems = true;
        }
        if (qName.equalsIgnoreCase("PLANITEM")) {
            this.currentPlanItem = new PlanItem(attr.getProperty("id"), attr.getProperty("timepoint"), attr.getProperty("propertytreeid"));
            this.planItems.add(this.currentPlanItem);
        } else if (qName.equalsIgnoreCase("propertylist")) {
            String id;
            String string = attr.getProperty("id") != null ? attr.getProperty("id") : (id = attr.getProperty("propertylistid") != null ? attr.getProperty("propertylistid") : "");
            if (this.propertyListIndex == 0) {
                PropertyList propertyList = new PropertyList(id);
                propertyList.setUsePropertyValues(true);
                this.propertyListBuffer.setLength(0);
                this.propertyListBuffer.append("<propertylist ").append(this.getAttributesText(attr)).append(">\n");
                this.propertyListIndex = 1;
            } else {
                this.propertyListBuffer.append("\n<propertylist ").append(this.getAttributesText(attr)).append(">\n");
                ++this.propertyListIndex;
            }
        } else if (qName.equalsIgnoreCase("property")) {
            this.propertyListBuffer.append("<property ").append(this.getAttributesText(attr)).append(">");
            this.currentPropertyType = attr.getProperty("type") != null ? attr.getProperty("type") : "simple";
        } else if (qName.equalsIgnoreCase("collection")) {
            this.propertyListBuffer.append("\n<collection>\n");
        } else if (qName.equalsIgnoreCase("dataset")) {
            this.currentDataSet = new DataSet();
        } else if (qName.equalsIgnoreCase("COL")) {
            this.currentDataSetColumnId = attr.getProperty("id");
        } else if (qName.equalsIgnoreCase("ROW")) {
            this.currentDataSetRow = this.currentDataSet.addRow();
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        this.log("End parsing " + qName);
        if (qName.equalsIgnoreCase("DETAILS")) {
            this.inDetails = false;
        }
        if (qName.equalsIgnoreCase("WORKITEMS")) {
            this.inWorkitems = true;
        }
        if (qName.equalsIgnoreCase("TRACKITEMS")) {
            this.inTrackitems = true;
        } else if (qName.equalsIgnoreCase("propertylist")) {
            this.propertyListBuffer.append("</propertylist>\n");
            --this.propertyListIndex;
            if (this.propertyListIndex == 0) {
                this.currentPlanItem.propertylist = new PropertyList();
                try {
                    this.currentPlanItem.propertylist.setPropertyList(this.propertyListBuffer.toString());
                }
                catch (SapphireException e) {
                    Logger.logStackTrace(e);
                }
            }
        } else if (qName.equalsIgnoreCase("property")) {
            String propertyValue = this.currentElementChars.toString().trim();
            if (this.currentElementChars.toString().trim().length() == 0 && this.currentElementChars.toString().length() > 0) {
                propertyValue = StringUtil.repeat(" ", this.currentElementChars.length());
            }
            this.propertyListBuffer.append(this.currentPropertyType.equals("simple") && propertyValue.length() > 0 ? "<![CDATA[" + propertyValue + "]]>" : "").append("</property>\n");
        } else if (qName.equalsIgnoreCase("collection")) {
            this.propertyListBuffer.append("</collection>\n");
        } else if (qName.equalsIgnoreCase("COL")) {
            this.currentDataSet.setString(this.currentDataSetRow, this.currentDataSetColumnId, this.currentElementChars.toString().trim());
        } else if (qName.equalsIgnoreCase("DATASET")) {
            if (this.inDetails) {
                this.detailsDataSet = this.currentDataSet;
            } else if (this.inWorkitems) {
                this.currentPlanItem.workitems = this.currentDataSet;
            } else if (this.inTrackitems) {
                this.trackitemsDataSet = this.currentDataSet;
            }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (this.CDATAEncountered) {
            this.currentElementChars.append(this.getCharacters(ch, start, length));
        }
    }

    @Override
    public void startCDATA() throws SAXException {
        this.currentElementChars.setLength(0);
        this.CDATAEncountered = true;
    }

    @Override
    public void endCDATA() throws SAXException {
        this.CDATAEncountered = false;
    }

    public DataSet getDetailsDataSet() {
        return this.detailsDataSet;
    }

    public DataSet getTrackitemsDataSet() {
        return this.trackitemsDataSet;
    }

    public int getPlanItemsSize() {
        return this.planItems.size();
    }

    public String getPlanItemId(int index) {
        PlanItem item = (PlanItem)this.planItems.get(index);
        return item.id;
    }

    public String getPlanItemTimepoint(int index) {
        PlanItem item = (PlanItem)this.planItems.get(index);
        return item.timepoint;
    }

    public String getPlanItemPropertyTree(int index) {
        PlanItem item = (PlanItem)this.planItems.get(index);
        return item.propertytree;
    }

    public PropertyList getPlanItemPropertyList(int index) {
        PlanItem item = (PlanItem)this.planItems.get(index);
        return item.propertylist;
    }

    public DataSet getPlanItemWorkItems(int index) {
        PlanItem item = (PlanItem)this.planItems.get(index);
        return item.workitems;
    }

    public int findPlanItem(String planitemid) {
        for (int i = 0; i < this.planItems.size(); ++i) {
            if (!((PlanItem)this.planItems.get((int)i)).id.equals(planitemid)) continue;
            return i;
        }
        return -1;
    }

    public void removePlanItem(String planItemid) {
        int index = this.findPlanItem(planItemid);
        if (index > -1) {
            this.planItems.remove(index);
        }
    }

    class PlanItem {
        String id;
        String timepoint;
        String propertytree;
        PropertyList propertylist;
        DataSet workitems;

        public PlanItem(String id, String timepoint, String propertytree) {
            this.id = id;
            this.timepoint = timepoint;
            this.propertytree = propertytree;
        }
    }
}

