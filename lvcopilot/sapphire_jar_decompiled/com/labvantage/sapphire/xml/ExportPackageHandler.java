/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.xml;

import com.labvantage.sapphire.xml.ExportXML;
import com.labvantage.sapphire.xml.ImportDirective;
import com.labvantage.sapphire.xml.Logger;
import com.labvantage.sapphire.xml.PropertyTreeTransfer;
import com.labvantage.sapphire.xml.SDCTransfer;
import com.labvantage.sapphire.xml.SDITransfer;
import com.labvantage.sapphire.xml.SapphireSaxHandler;
import com.labvantage.sapphire.xml.Table;
import com.labvantage.sapphire.xml.TableTransfer;
import com.labvantage.sapphire.xml.TransferConstants;
import com.labvantage.sapphire.xml.Transferable;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import sapphire.SapphireException;
import sapphire.util.SDIData;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ExportPackageHandler
extends SapphireSaxHandler
implements TransferConstants,
Logger {
    private StringBuffer currentElementChars = new StringBuffer();
    private boolean CDATAEncountered;
    private boolean checksumFound;
    private boolean checksumCheck;
    private String checksumProcessing = "I";
    private boolean checksumWarned;
    private boolean importForceUpdate;
    private boolean ignoreMissingObjects = false;
    private boolean ignoreSequenceCheck = false;
    private boolean regenKeys = false;
    private File zipFile;
    private String zipFileEntry;
    private Transferable currentItem;
    private Logger logger;
    private String commitScope = "table";
    private List importObjects;
    private Object currentImportObject;
    private int importTarget = 0;
    private Hashtable antProperties;
    private ArrayList<ImportDirective> importDirectives = new ArrayList();
    private HashMap<String, HashMap<String, String>> regeneratedKeys = new HashMap();

    public List getImportObjects() {
        return this.importObjects;
    }

    public void setCommitScope(String commitScope) {
        this.commitScope = commitScope;
    }

    public void setIgnoreMissingObjects(boolean ignoreMissingObjects) {
        this.ignoreMissingObjects = ignoreMissingObjects;
    }

    public void setIgnoreSequenceCheck(boolean ignoreSequenceCheck) {
        this.ignoreSequenceCheck = ignoreSequenceCheck;
    }

    public void setRegenKeys(boolean regenKeys) {
        this.regenKeys = regenKeys;
    }

    public void setImportDirectives(ArrayList<ImportDirective> importDirectives) {
        this.importDirectives = importDirectives;
    }

    public void setImportTarget(int importTarget) {
        this.importTarget = importTarget;
    }

    public void setChecksumProcessing(String checksumProcessing) {
        this.checksumProcessing = checksumProcessing;
    }

    public void setZipFile(File zipFile) {
        this.zipFile = zipFile;
    }

    public void setZipFileEntry(String zipFileEntry) {
        this.zipFileEntry = zipFileEntry;
    }

    public void setImportForceUpdate(boolean importForceUpdate) {
        this.importForceUpdate = importForceUpdate;
    }

    public void setAntProperties(Hashtable antProperties) {
        this.antProperties = antProperties;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        this.currentElementChars.delete(0, this.currentElementChars.length());
        Properties attr = this.getAttributes(attributes);
        try {
            if (qName.equalsIgnoreCase("exportpackage")) {
                this.log((this.importTarget == 2 ? "Parsing" : "Importing") + " EXPORTPACKAGE: " + (this.zipFile != null ? this.zipFileEntry : this.getXMLFile().getName()) + "...");
                this.checksumCheck = attr.getProperty("checksum", "N").equals("Y");
                if (this.importTarget == 1 || this.importTarget == 2) {
                    this.importObjects = new ArrayList();
                }
                if (this.importTarget == 2) {
                    PropertyList props = new PropertyList(new HashMap<Object, Object>(attr));
                    props.setProperty("importfilename", this.getXMLFile().getName());
                    if (this.zipFile != null) {
                        props.setProperty("zipfile", this.zipFile.getName());
                        props.setProperty("zipfileentry", this.zipFileEntry);
                    }
                    props.setProperty("checksumprocessing", this.checksumProcessing);
                    this.importObjects.add(props);
                }
            } else if (qName.equalsIgnoreCase("checksum")) {
                this.checksumFound = true;
            } else if (qName.equalsIgnoreCase("export")) {
                if (this.currentItem instanceof SDITransfer) {
                    ((SDITransfer)this.currentItem).setConnectionid(this.getConnectionid());
                    ((SDITransfer)this.currentItem).setRegenKeys(this.regenKeys);
                    ((SDITransfer)this.currentItem).setRegeneratedKeys(this.regeneratedKeys);
                    ((SDITransfer)this.currentItem).setFlushSDI(attr.getProperty("flushsdi"));
                    ((SDITransfer)this.currentItem).setFlushDetails(attr.getProperty("flushdetails"));
                    ((SDITransfer)this.currentItem).setFlushSDIDetails(attr.getProperty("flushsdidetails"));
                    ((SDITransfer)this.currentItem).setFlushChildSDI(attr.getProperty("flushchildsdi"));
                    ((SDITransfer)this.currentItem).setFlushRoles(attr.getProperty("flushroles"));
                    ((SDITransfer)this.currentItem).setFlushCategories(attr.getProperty("flushcategories"));
                }
            } else if (!qName.equalsIgnoreCase("exportitem")) {
                if (qName.equalsIgnoreCase("sdc") || qName.equalsIgnoreCase("sdi") || qName.equalsIgnoreCase("table") || qName.equalsIgnoreCase("propertytree")) {
                    if (!this.checksumFound) {
                        if ((this.checksumCheck || this.checksumProcessing.equals("B")) && this.importTarget == 0) {
                            throw new SAXException("Checksum missing from file - import blocked!");
                        }
                        if (!this.checksumWarned && this.checksumProcessing.equals("W")) {
                            this.log("WARNING: Checksum validation failed - file has been changed since generation time (or does not contain checksum information)!");
                            this.checksumWarned = true;
                        }
                    }
                    if (qName.equalsIgnoreCase("sdc")) {
                        this.currentItem = new SDCTransfer(attr.getProperty("sdcid"));
                    } else if (qName.equalsIgnoreCase("sdi")) {
                        this.currentItem = new SDITransfer(attr.getProperty("sdcid"));
                        this.currentItem.setImportTarget(this.importTarget);
                        if (this.importTarget == 1) {
                            this.currentImportObject = new SDIData(attr.getProperty("sdcid"));
                            this.currentItem.setImportObject(this.currentImportObject);
                            this.importObjects.add(this.currentImportObject);
                        }
                        ((SDITransfer)this.currentItem).setIgnoreSequenceCheck(this.ignoreSequenceCheck);
                        if (this.antProperties != null && this.antProperties.get("devmode.importxml.ignoresequencecheck") != null && ((String)this.antProperties.get("devmode.importxml.ignoresequencecheck")).equals("true")) {
                            ((SDITransfer)this.currentItem).setIgnoreSequenceCheck(true);
                            ((SDITransfer)this.currentItem).setIgnoreFlush(true);
                        }
                    } else if (qName.equalsIgnoreCase("table") && (this.currentItem == null || !(this.currentItem instanceof SDCTransfer) && !(this.currentItem instanceof SDITransfer))) {
                        this.currentItem = new TableTransfer(attr.getProperty("tableid"));
                        this.currentItem.setImportTarget(this.importTarget);
                        ((TableTransfer)this.currentItem).setImportForceUpdate(this.importForceUpdate);
                        if (this.importTarget == 1) {
                            this.currentImportObject = new Table(attr.getProperty("tableid"));
                            this.currentItem.setImportObject(this.currentImportObject);
                            this.importObjects.add(this.currentImportObject);
                        }
                    } else if (qName.equalsIgnoreCase("propertytree")) {
                        this.currentItem = new PropertyTreeTransfer(attr.getProperty("propertytreeid") != null ? attr.getProperty("propertytreeid") : (attr.getProperty("id") != null ? attr.getProperty("id") : ""));
                    }
                    this.currentItem.setParseOnly(this.importTarget == 2 || attr.getProperty("parseonly") != null && attr.getProperty("parseonly").equals("true"));
                    this.currentItem.setVerbose(this.verbose || attr.getProperty("verbose") != null && attr.getProperty("verbose").equals("true"));
                    this.currentItem.setImportForceUpdate(this.importForceUpdate);
                    this.currentItem.setImportDirectives(this.importDirectives);
                    this.currentItem.setCommitScope(this.commitScope);
                    this.currentItem.setIgnoreMissingObjects(this.ignoreMissingObjects);
                    this.currentItem.setFile(this.getXMLFile());
                    this.currentItem.setZipFile(this.zipFile);
                    this.currentItem.setZipFileEntry(this.zipFileEntry);
                    if (this.antProperties != null && this.antProperties.get("sapphire.debug") != null && ((String)this.antProperties.get("sapphire.debug")).equals("true")) {
                        for (String property : this.antProperties.keySet()) {
                            String[] parts;
                            if (!property.startsWith("sapphire.debug") || (parts = StringUtil.split(property, ".")).length != 3 || !parts[2].equals(this.currentItem.getId()) || !((String)this.antProperties.get(property)).equals("true")) continue;
                            this.currentItem.setVerbose(true);
                            this.currentItem.setCommitScope("row");
                        }
                    }
                    this.currentItem.startElementImport(this.getDBUtil(), qName, attr, this);
                } else if (this.currentItem == null || !this.currentItem.startElementImport(this.getDBUtil(), qName, attr, this)) {
                    String err = "Unrecognized element " + qName + " found in document " + this._xmlFile.getName();
                    this.log(err);
                    this.println(err);
                    throw new SAXException(err);
                }
            }
        }
        catch (SapphireException se) {
            throw new SAXException(se);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        try {
            if (qName.equals("sdi")) {
                if (this.importTarget == 0 && this.commitScope.equals("sdi")) {
                    try {
                        this.log("Committing SDI: " + this.currentItem.getId());
                        this.getDBUtil().getConnection().commit();
                    }
                    catch (SQLException e) {
                        throw new SAXException("Failed to commit SDI data. Reason: " + e.getMessage(), e);
                    }
                }
                if (this.importTarget == 1) {
                    int i;
                    SDIData sdiData = (SDIData)this.currentImportObject;
                    ArrayList<Table> linkTables = new ArrayList<Table>();
                    for (String datasetname : sdiData.getDatasets()) {
                        Table table = (Table)sdiData.getDataset(datasetname);
                        String linkid = table.getLinkid();
                        if (linkid == null || linkid.length() <= 0) continue;
                        linkTables.add(table);
                    }
                    String[] linkids = new String[linkTables.size()];
                    String[] linktables = new String[linkTables.size()];
                    String[][] linktablekeys = new String[linkTables.size()][];
                    for (i = 0; i < linkTables.size(); ++i) {
                        Table table = (Table)linkTables.get(i);
                        linkids[i] = table.getLinkid();
                        linktables[i] = table.getTableid();
                        linktablekeys[i] = table.getKeyColumns();
                    }
                    sdiData.setLinks(linkids, linktables);
                    for (i = 0; i < linktables.length; ++i) {
                        sdiData.setLinkTableKeys(linktables[i], linktablekeys[i]);
                    }
                }
            }
            if (qName.equalsIgnoreCase("exportpackage")) {
                this.log((this.importTarget == 2 ? "Parse" : "Import") + " complete");
            } else if (qName.equalsIgnoreCase("checksum")) {
                String parsedChecksum = this.currentElementChars.toString();
                String fileChecksum = StringUtil.padRight(this.zipFile != null && this.zipFileEntry != null ? ExportXML.generateCheckSum(this.zipFile, this.zipFileEntry) : ExportXML.generateCheckSum(this._xmlFile, false), 100, '*');
                if (this.importTarget == 2) {
                    ((PropertyList)this.importObjects.get(0)).setProperty("parsedchecksum", parsedChecksum);
                    ((PropertyList)this.importObjects.get(0)).setProperty("filechecksum", fileChecksum);
                } else if (parsedChecksum == null || !parsedChecksum.equals(fileChecksum)) {
                    if (this.checksumCheck) {
                        throw new SAXException("Checksum validation failed - file has been changed since generation time!");
                    }
                    if (this.checksumProcessing.equals("B")) {
                        throw new SAXException("Checksum validation failed - file has been changed since generation time!");
                    }
                    if (this.checksumProcessing.equals("W")) {
                        this.log("WARNING: Checksum validation failed - file has been changed since generation time!");
                    }
                }
            } else if (!qName.equalsIgnoreCase("export")) {
                if (qName.equalsIgnoreCase("exportitem")) {
                    if (this.currentItem instanceof SDITransfer) {
                        ((SDITransfer)this.currentItem).addExportItem(this.currentElementChars.toString());
                    }
                } else if (this.currentItem != null && this.currentItem.endElementImport(this.getDBUtil(), qName, this.currentElementChars.toString(), this.CDATAEncountered, this) && this.importTarget == 2 && this.currentItem != null && this.currentItem.getParsedData() != null) {
                    if (qName.equalsIgnoreCase("sdc") && this.currentItem instanceof SDCTransfer) {
                        this.importObjects.add(this.currentItem.clone());
                    } else if (qName.equalsIgnoreCase("sdi") && this.currentItem instanceof SDITransfer) {
                        this.importObjects.add(this.currentItem.clone());
                    } else if (qName.equalsIgnoreCase("table") && this.currentItem instanceof TableTransfer) {
                        this.importObjects.add(this.currentItem.clone());
                    } else if (qName.equalsIgnoreCase("propertytree") && this.currentItem instanceof PropertyTreeTransfer && this.currentItem.getId() != null && this.currentItem.getId().length() > 0) {
                        this.importObjects.add(this.currentItem.clone());
                    }
                }
            }
        }
        catch (SapphireException se) {
            throw new SAXException(se);
        }
        catch (CloneNotSupportedException cnse) {
            throw new SAXException("here" + cnse);
        }
    }

    @Override
    public void startDocument() throws SAXException {
    }

    @Override
    public void endDocument() throws SAXException {
    }

    @Override
    public void startCDATA() throws SAXException {
        this.CDATAEncountered = true;
    }

    @Override
    public void endCDATA() throws SAXException {
        this.CDATAEncountered = false;
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (this.currentElementChars != null) {
            this.currentElementChars.append(this.getCharacters(ch, start, length));
        }
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void log(String message) {
        if (this.logger != null) {
            this.logger.log(message);
        }
    }
}

