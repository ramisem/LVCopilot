/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.xml;

import com.labvantage.sapphire.services.ConnectionInfo;
import com.labvantage.sapphire.xml.SapphireSaxHandler;
import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.util.DataSet;
import sapphire.util.SDIData;

public class SDIDataHandler
extends SapphireSaxHandler {
    private StringBuffer value = new StringBuffer();
    private StringBuffer currentElementChars = new StringBuffer();
    private boolean CDATAEncountered;
    private SDIData sdidata;
    private String datasetName = "";
    private String datasetXML = "";
    private String currDSCDataEscape = "";
    private ArrayDeque<SDIData> sdiDataStack = new ArrayDeque();
    private String currLinkedSDIDataName = "";
    private String currMetaDataVarName = "";
    private int currMetaDataVarDimension = 0;
    private int currMetaDataValRowNum = -1;
    private int currMetaDataValColNum = -1;
    private Map<Integer, String> currMetaDataVar1D = null;
    private Map<Integer, Map<Integer, String>> currMetaDataVar2D = null;

    public SDIDataHandler(SDIData sdidata) {
        this.sdidata = sdidata;
    }

    public SDIData getSDIData() {
        return this.sdidata;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        Properties attr = this.getAttributes(attributes);
        this.currentElementChars.delete(0, this.currentElementChars.length());
        if (qName.equalsIgnoreCase("sdidata")) {
            if (this.sdiDataStack.size() == 0) {
                this.sdidata.setSdcid(attr.getProperty("sdcid"));
                this.sdiDataStack.push(this.sdidata);
            } else {
                this.sdiDataStack.push(new SDIData(attr.getProperty("sdcid")));
            }
        } else if (qName.equalsIgnoreCase("datasetitem")) {
            this.datasetXML = "";
            this.currDSCDataEscape = "";
        } else if (qName.equalsIgnoreCase("datasetname")) {
            this.datasetName = "";
        } else if (qName.equalsIgnoreCase("linkedsdidataname")) {
            this.currLinkedSDIDataName = "";
        } else if (qName.equalsIgnoreCase("datasetval")) {
            this.currDSCDataEscape = attr.getProperty("cdataescape", "!]!]!>");
        } else if (qName.equalsIgnoreCase("metadataitem") || qName.equalsIgnoreCase("mdi")) {
            this.currMetaDataVarName = attr.getProperty("varname");
            this.currMetaDataVarDimension = Integer.parseInt(attr.getProperty("dimension"));
            this.currMetaDataValRowNum = -1;
            this.currMetaDataValColNum = -1;
            if (this.currMetaDataVarDimension == 1) {
                this.currMetaDataVar1D = new HashMap<Integer, String>();
            } else if (this.currMetaDataVarDimension == 2) {
                this.currMetaDataVar2D = new HashMap<Integer, Map<Integer, String>>();
            }
        } else if (qName.equalsIgnoreCase("metadataitemvalue") || qName.equalsIgnoreCase("mdiv")) {
            this.currMetaDataValRowNum = attr.containsKey("rowindex") ? Integer.parseInt(attr.getProperty("rowindex")) : Integer.parseInt(attr.getProperty("ri"));
            if (this.currMetaDataVarDimension == 2) {
                this.currMetaDataValColNum = attr.containsKey("colindex") ? Integer.parseInt(attr.getProperty("colindex")) : Integer.parseInt(attr.getProperty("ci"));
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("datasetitem")) {
            this.datasetXML = this.datasetXML.replaceAll(this.currDSCDataEscape, "]]>");
            DataSet ds = null;
            ds = this.connectionid != null && this.connectionid.length() > 0 ? new DataSet(this.datasetXML, (ConnectionInfo)(this.rakFile != null ? new ConnectionProcessor(this.rakFile, this.connectionid) : new ConnectionProcessor(this.connectionid)).getConnectionInfo(this.connectionid)) : new DataSet(this.datasetXML);
            this.sdiDataStack.peek().setDataset(this.datasetName, ds);
        } else if (qName.equalsIgnoreCase("datasetname")) {
            this.datasetName = this.currentElementChars.toString();
        } else if (qName.equalsIgnoreCase("datasetval")) {
            this.datasetXML = this.currentElementChars.toString();
        } else if (qName.equalsIgnoreCase("linkedsdidataname")) {
            this.currLinkedSDIDataName = this.currentElementChars.toString();
        } else if (qName.equalsIgnoreCase("metadataitemvalue") || qName.equalsIgnoreCase("mdiv")) {
            if (this.currMetaDataVarDimension == 1) {
                this.currMetaDataVar1D.put(this.currMetaDataValRowNum, this.currentElementChars.toString());
            } else if (this.currMetaDataVarDimension == 2) {
                if (!this.currMetaDataVar2D.containsKey(this.currMetaDataValRowNum)) {
                    this.currMetaDataVar2D.put(this.currMetaDataValRowNum, new HashMap());
                }
                if (this.currMetaDataValColNum != -1) {
                    this.currMetaDataVar2D.get(this.currMetaDataValRowNum).put(this.currMetaDataValColNum, this.currentElementChars.toString());
                }
            }
        } else if (qName.equalsIgnoreCase("metadataitem") || qName.equalsIgnoreCase("mdi")) {
            SDIData currSDIData = this.sdiDataStack.peek();
            if (this.currMetaDataVarDimension == 1) {
                String[] var = new String[this.currMetaDataVar1D.size()];
                for (Integer rowNum : this.currMetaDataVar1D.keySet()) {
                    var[rowNum.intValue()] = this.currMetaDataVar1D.get(rowNum);
                }
                this.setVariableValue(currSDIData, var);
            } else if (this.currMetaDataVarDimension == 2) {
                String[][] var = new String[this.currMetaDataVar2D.size()][];
                for (Integer rowNum : this.currMetaDataVar2D.keySet()) {
                    Map<Integer, String> rowMap = this.currMetaDataVar2D.get(rowNum);
                    if (rowMap.size() <= 0) continue;
                    var[rowNum.intValue()] = new String[rowMap.size()];
                    for (Integer colNum : rowMap.keySet()) {
                        var[rowNum.intValue()][colNum.intValue()] = rowMap.get(colNum);
                    }
                }
                this.setVariableValue(currSDIData, var);
            }
        } else if (qName.equalsIgnoreCase("sdidata")) {
            if (this.sdiDataStack.size() > 1) {
                SDIData sdiDataTemp = this.sdiDataStack.pop();
                this.sdiDataStack.peek().setSDIData(this.currLinkedSDIDataName, sdiDataTemp);
            } else {
                this.returnHandler();
            }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (this.currentElementChars != null) {
            this.currentElementChars.append(this.getCharacters(ch, start, length));
        }
    }

    private void setVariableValue(SDIData currSDIData, Object var) throws SAXException {
        try {
            Field field = currSDIData.getClass().getDeclaredField(this.currMetaDataVarName);
            boolean accessible = field.isAccessible();
            field.setAccessible(true);
            field.set(currSDIData, var);
            field.setAccessible(accessible);
        }
        catch (Exception e) {
            throw new SAXException(e);
        }
    }
}

