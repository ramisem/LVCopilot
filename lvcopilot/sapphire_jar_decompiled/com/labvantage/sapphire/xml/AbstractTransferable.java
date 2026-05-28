/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.xml;

import com.labvantage.sapphire.xml.Column;
import com.labvantage.sapphire.xml.ImportDirective;
import com.labvantage.sapphire.xml.NodeList;
import com.labvantage.sapphire.xml.PropertyTreeTransfer;
import com.labvantage.sapphire.xml.SDITransfer;
import com.labvantage.sapphire.xml.TransferConstants;
import com.labvantage.sapphire.xml.Transferable;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import sapphire.xml.PropertyList;

public abstract class AbstractTransferable
implements Transferable,
TransferConstants {
    public static final String DEFAULT_KEYSEPARATOR = "\t";
    protected String filename;
    protected File file;
    protected File zipFile;
    protected String keyFilename;
    protected String keyseparator = "\t";
    protected String zipFileEntry;
    protected boolean verbose;
    protected boolean parseOnly;
    protected String commitScope = "table";
    protected boolean ignoreMissingObjects = false;
    protected boolean excludeAuditColumns = true;
    protected boolean propagateAuditColumns = false;
    protected boolean excludeExportAttributes = false;
    protected boolean exportTableDefinition = true;
    protected boolean forceLOBExport;
    protected boolean importForceUpdate = false;
    protected List referencedItems = new ArrayList();
    protected ArrayList columns = new ArrayList();
    protected ArrayList<ImportDirective> importDirectives = new ArrayList();
    private String connectionid;
    private PropertyList transferOptions = new PropertyList();
    protected int importTarget = 0;
    protected Object importObject;

    @Override
    public final File getFile() {
        return this.file;
    }

    public final void setFilename(String filename) {
        this.filename = filename;
    }

    @Override
    public final void setFile(File file) {
        this.file = file;
    }

    @Override
    public void setZipFile(File zipFile) {
        this.zipFile = zipFile;
    }

    @Override
    public void setZipFileEntry(String zipFileEntry) {
        this.zipFileEntry = zipFileEntry;
    }

    public String getKeyFilename() {
        return this.keyFilename;
    }

    public String getKeyseparator() {
        return this.keyseparator;
    }

    public void setKeyFilename(String keyFilename) {
        this.keyFilename = keyFilename;
    }

    public void setKeyseparator(String keyseparator) {
        this.keyseparator = keyseparator;
    }

    @Override
    public final void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public final void setParseOnly(boolean parseOnly) {
        this.parseOnly = parseOnly;
    }

    @Override
    public final void setCommitScope(String commitScope) {
        this.commitScope = commitScope;
    }

    @Override
    public void setIgnoreMissingObjects(boolean ignoreMissingObjects) {
        this.ignoreMissingObjects = ignoreMissingObjects;
    }

    public void setExcludeAuditColumns(boolean excludeAuditColumns) {
        this.excludeAuditColumns = excludeAuditColumns;
    }

    public boolean isExcludeAuditColumns() {
        return this.excludeAuditColumns;
    }

    public void setPropagateAuditColumns(boolean propagateAuditColumns) {
        this.propagateAuditColumns = propagateAuditColumns;
    }

    public void setExcludeExportAttributes(boolean excludeExportAttributes) {
        this.excludeExportAttributes = excludeExportAttributes;
    }

    public void setExportTableDefinition(boolean exportTableDefinition) {
        this.exportTableDefinition = exportTableDefinition;
    }

    public void setForceLOBExport(boolean forceLOBExport) {
        this.forceLOBExport = forceLOBExport;
    }

    @Override
    public void setImportForceUpdate(boolean importForceUpdate) {
        this.importForceUpdate = importForceUpdate;
    }

    public void setConnectionid(String connectionid) {
        this.connectionid = connectionid;
    }

    public String getConnectionid() {
        return this.connectionid;
    }

    protected boolean hasConnection() {
        return this.connectionid != null && this.connectionid.length() > 0;
    }

    @Override
    public void setImportDirectives(ArrayList<ImportDirective> importDirectives) {
        this.importDirectives = importDirectives;
    }

    public void addImportDirective(ImportDirective importDirective) {
        this.importDirectives.add(importDirective);
    }

    public ArrayList<ImportDirective> getImportDirectives() {
        return this.importDirectives;
    }

    public void setReferencedItems(List referencedItems) {
        this.referencedItems = referencedItems;
    }

    @Override
    public final List getReferencedItems() {
        return this.referencedItems;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public final void setTransferOption(String propertyid, String value) {
        this.transferOptions.setProperty(propertyid, value);
    }

    @Override
    public final String getTransferOption(String propertyid) {
        return this.transferOptions.getProperty(propertyid);
    }

    public final String getTransferOption(String propertyid, String defaultvalue) {
        return this.transferOptions.getProperty(propertyid, defaultvalue);
    }

    public final PropertyList getTransferOptions() {
        return this.transferOptions;
    }

    public void addReferencedItem(Transferable item) {
        if (item instanceof PropertyTreeTransfer) {
            PropertyTreeTransfer newitem = (PropertyTreeTransfer)item;
            String propertytreeid = newitem.getId();
            boolean found = false;
            Iterator iterator = this.referencedItems.iterator();
            while (iterator.hasNext() && !found) {
                PropertyTreeTransfer currentItem;
                Transferable transferable = (Transferable)iterator.next();
                if (!(transferable instanceof PropertyTreeTransfer) || !(currentItem = (PropertyTreeTransfer)transferable).getId().equals(propertytreeid)) continue;
                found = true;
                NodeList currentNodeList = currentItem.getNodeList();
                if (currentNodeList == null) {
                    currentNodeList = new NodeList();
                    currentItem.setNodeList(currentNodeList);
                }
                NodeList newNodeList = newitem.getNodeList();
                currentNodeList.mergeNodes(newNodeList);
            }
            if (!found) {
                this.referencedItems.add(item);
            }
        } else if (item instanceof SDITransfer) {
            String newitem = item.toString();
            boolean found = false;
            Iterator iterator = this.referencedItems.iterator();
            while (iterator.hasNext() && !found) {
                SDITransfer currentItem;
                Transferable transferable = (Transferable)iterator.next();
                if (!(transferable instanceof SDITransfer) || !(currentItem = (SDITransfer)transferable).toString().equals(newitem)) continue;
                found = true;
            }
            if (!found) {
                this.referencedItems.add(item);
            }
        } else {
            this.referencedItems.add(item);
        }
    }

    @Override
    public void setImportTarget(int importTarget) {
        this.importTarget = importTarget;
    }

    @Override
    public void setImportObject(Object importObject) {
        this.importObject = importObject;
    }

    public void addColumn(Column column) {
        this.columns.add(column);
    }

    public Column getColumn(String columnid) {
        int n = this.columns.size();
        for (int i = 0; i < n; ++i) {
            if (!columnid.equals(((Column)this.columns.get(i)).getColumnid())) continue;
            return (Column)this.columns.get(i);
        }
        return null;
    }

    public void setColumns(ArrayList columns) {
        this.columns = columns;
    }

    public List getColumns() {
        return this.columns;
    }
}

