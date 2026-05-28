/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.xml;

import com.labvantage.sapphire.xml.ImportDirective;
import com.labvantage.sapphire.xml.Logger;
import com.labvantage.sapphire.xml.TransferConstants;
import com.labvantage.sapphire.xml.Transferable;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipOutputStream;
import sapphire.SapphireException;
import sapphire.util.DBAccess;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class PropertyListTransfer
extends PropertyList
implements Transferable,
TransferConstants,
Cloneable {
    private String exists = "replace";
    private String notexists = "add";
    private String propertyListXML;
    private File file;
    private PropertyList transferOptions = new PropertyList();
    private boolean verbose;
    private boolean parseOnly;
    private String commitScope = "table";
    private boolean ignoreMissingObjects = false;
    protected ArrayList<ImportDirective> importDirectives = new ArrayList();

    public PropertyListTransfer() {
    }

    public PropertyListTransfer(String id) {
        super(id);
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void setFile(File file) {
        this.file = file;
    }

    @Override
    public File getFile() {
        return this.file;
    }

    @Override
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public void setParseOnly(boolean parseOnly) {
        this.parseOnly = parseOnly;
    }

    @Override
    public void setCommitScope(String commitScope) {
        this.commitScope = commitScope;
    }

    @Override
    public void setIgnoreMissingObjects(boolean ignoreMissingObjects) {
        this.ignoreMissingObjects = ignoreMissingObjects;
    }

    public String getExists() {
        return this.exists;
    }

    public void setExists(String exists) {
        this.exists = exists;
    }

    public String getNotexists() {
        return this.notexists;
    }

    public void setNotexists(String notexists) {
        this.notexists = notexists;
    }

    @Override
    public void setImportDirectives(ArrayList<ImportDirective> importDirectives) {
        this.importDirectives = importDirectives;
    }

    @Override
    public List getReferencedItems() {
        return null;
    }

    @Override
    public void evalProperties(PropertyList props) {
    }

    public void export(File exportFile, PrintStream out, ZipOutputStream zipOut, DBAccess database, int level, Logger logger, Map exported, String propertyListXML) throws SapphireException {
        this.propertyListXML = propertyListXML;
        this.export(exportFile, out, zipOut, database, level, logger, exported);
    }

    @Override
    public void export(File exportFile, PrintStream out, ZipOutputStream zipOut, DBAccess database, int level, Logger logger, Map exported) throws SapphireException {
        String level0 = StringUtil.repeat("\t", level);
        String level1 = StringUtil.repeat("\t", level + 1);
        PropertyList loadedPropertyList = new PropertyList();
        loadedPropertyList.setPropertyList(this.propertyListXML);
        logger.log(level0 + "Exporting PROPERTYLIST");
        logger.log(level1 + "Import options: " + this.exists + " existing properties, " + this.notexists + " missing properties.");
        this.exportPropertyList(this, loadedPropertyList, out, logger, level + 1, true);
    }

    private void exportPropertyList(PropertyList exportPropertyList, PropertyList loadedPropertyList, PrintStream out, Logger logger, int level, boolean topLevel) {
        String level0 = StringUtil.repeat("\t", level);
        String level1 = StringUtil.repeat("\t", level + 1);
        String level2 = StringUtil.repeat("\t", level + 2);
        if (loadedPropertyList != null && loadedPropertyList.size() > 0) {
            if (exportPropertyList != null && exportPropertyList.size() > 0) {
                String sequence = loadedPropertyList.getAttribute("sequence");
                String rolelist = loadedPropertyList.getAttribute("rolelist");
                String modulelist = loadedPropertyList.getAttribute("modulelist");
                out.println(level0 + "<propertylist exists=\"" + this.exists + "\" notexists=\"" + this.notexists + "\" id=\"" + exportPropertyList.getId() + "\"" + (sequence.length() > 0 ? " sequence=\"" + sequence + "\"" : "") + (rolelist.length() > 0 ? " rolelist=\"" + rolelist + "\"" : "") + (modulelist.length() > 0 ? " modulelist=\"" + modulelist + "\"" : "") + ">");
                for (String propertyid : exportPropertyList.keySet()) {
                    Object loadedPropertyValue = loadedPropertyList.get(propertyid);
                    if (loadedPropertyValue == null) continue;
                    if (loadedPropertyValue instanceof PropertyListCollection) {
                        PropertyListCollection loadedCollection = (PropertyListCollection)loadedPropertyValue;
                        PropertyListCollection exportCollection = exportPropertyList.getCollection(propertyid);
                        if (exportCollection != null) {
                            out.println(level1 + "<property id=\"" + propertyid + "\" type=\"collection\">");
                            out.println(level2 + "<collection>");
                            for (int i = 0; i < exportCollection.size(); ++i) {
                                String id;
                                PropertyList recursiveExportPropertyList = exportCollection.getPropertyList(i);
                                PropertyList recursiveLoadedPropertyList = loadedCollection.getPropertyList(recursiveExportPropertyList.getId());
                                String string = id = recursiveLoadedPropertyList == null ? "" : recursiveLoadedPropertyList.getProperty("id");
                                if (id.length() == 0) {
                                    id = recursiveExportPropertyList.getId();
                                }
                                logger.log(level1 + propertyid + " collection-item: " + id);
                                this.exportPropertyList(recursiveExportPropertyList, recursiveLoadedPropertyList, out, logger, level + 1, false);
                            }
                            out.println(level2 + "</collection>");
                            out.println(level1 + "</property>");
                            continue;
                        }
                        logger.log(level1 + "Exporting ALL collection-items for: " + propertyid);
                        StringBuffer xml = new StringBuffer();
                        this.propertyToXML(xml, propertyid, loadedPropertyValue, null, "", "", level + 1);
                        out.println(xml.toString());
                        continue;
                    }
                    if (loadedPropertyValue instanceof PropertyListTransfer || loadedPropertyValue instanceof PropertyList) {
                        PropertyList recursiveExportPropertyList = exportPropertyList.getPropertyList(propertyid);
                        PropertyList recursiveLoadedPropertyList = loadedPropertyList.getPropertyList(propertyid);
                        if (topLevel) {
                            logger.log(level1 + "Properties for: " + propertyid);
                        }
                        out.println(level1 + "<property id=\"" + propertyid + "\" type=\"propertylist\">");
                        this.exportPropertyList(recursiveExportPropertyList, recursiveLoadedPropertyList, out, logger, level + 1, false);
                        out.println(level1 + "</property>");
                        continue;
                    }
                    StringBuffer xml = new StringBuffer();
                    this.propertyToXML(xml, propertyid, loadedPropertyValue, null, "", "", level + 1);
                    out.println(xml.toString());
                    if (!topLevel) continue;
                    logger.log(level1 + "Simple: " + propertyid + "=" + loadedPropertyValue);
                }
                out.println(level0 + "</propertylist>");
            } else {
                loadedPropertyList.setAttribute("exists", this.exists, true);
                loadedPropertyList.setAttribute("notexists", this.notexists, true);
                String xml = loadedPropertyList.toXMLString(level);
                out.println(xml);
                logger.log(level0 + "Exporting ALL properties");
            }
        } else {
            out.println(level0 + "<propertylist/>");
            logger.log(level0 + "No properties found to export.");
        }
    }

    @Override
    public boolean startElementImport(DBAccess database, String elementName, Properties attributes, Logger logger) throws SapphireException {
        return false;
    }

    @Override
    public boolean endElementImport(DBAccess database, String elementName, String elementCharacters, boolean isCDATA, Logger logger) throws SapphireException {
        return false;
    }

    @Override
    public void generateAntTask(PrintStream out, int level) {
        String level0 = StringUtil.repeat("\t", level);
        out.println(level0 + "<propertylist id=\"" + this.id + "\" exists=\"" + this.exists + "\" notexists=\"" + this.notexists + "\" " + (this.size() == 0 ? "/>" : ">"));
        if (this.size() > 0) {
            out.println(level0 + "</propertylist>");
        }
    }

    @Override
    public Object getParsedData() {
        return null;
    }

    @Override
    public void setTransferOption(String propertyid, String value) {
        this.transferOptions.setProperty(propertyid, value);
    }

    @Override
    public String getTransferOption(String propertyid) {
        return this.transferOptions.getProperty(propertyid);
    }

    @Override
    public void setImportTarget(int importTarget) {
    }

    @Override
    public void setImportObject(Object importObject) {
    }

    public String getTransferOption(String propertyid, String defaultvalue) {
        return this.transferOptions.getProperty(propertyid, defaultvalue);
    }

    @Override
    public void setZipFile(File zipFile) {
    }

    @Override
    public void setZipFileEntry(String zipFileEntry) {
    }

    @Override
    public void setImportForceUpdate(boolean importForceUpdate) {
    }
}

