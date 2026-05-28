/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.xml;

import com.labvantage.sapphire.xml.AbstractTransferable;
import com.labvantage.sapphire.xml.Column;
import com.labvantage.sapphire.xml.ExportXML;
import com.labvantage.sapphire.xml.ImportXML;
import com.labvantage.sapphire.xml.Logger;
import com.labvantage.sapphire.xml.SDCTransfer;
import com.labvantage.sapphire.xml.SDIDetail;
import com.labvantage.sapphire.xml.SDITransfer;
import com.labvantage.sapphire.xml.SapphireSaxHandler;
import com.labvantage.sapphire.xml.TableTransfer;
import com.labvantage.sapphire.xml.Transfer;
import com.labvantage.sapphire.xml.TransferPackage;
import com.labvantage.sapphire.xml.Transferable;
import java.io.File;
import java.util.Properties;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import sapphire.util.StringUtil;

public class TransferPackageHandler
extends SapphireSaxHandler
implements Logger {
    private StringBuffer currentElementChars = new StringBuffer();
    private boolean CDATAEncountered;
    private Logger logger;
    private boolean exportScript;
    private TransferPackage transferPackage;
    private String currentParentElement;
    private String currentPropertyId;
    private boolean processedPropertyList = false;

    public TransferPackage getTransferPackage() {
        return this.transferPackage;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        this.currentElementChars.delete(0, this.currentElementChars.length());
        Properties attr = this.getAttributes(attributes);
        if (qName.equalsIgnoreCase("transferpackage")) {
            this.log("Processing TRANSFERPACKAGE: " + (this.getXMLFile() != null ? this.getXMLFile().getAbsolutePath() : "") + "...");
            this.exportScript = attr.getProperty("exportscript") != null && (attr.getProperty("exportscript").equals("Y") || attr.getProperty("exportscript").equals("true"));
            return;
        } else if (qName.equalsIgnoreCase("propertylist")) {
            if (this.processedPropertyList) {
                throw new SAXException("Incorrect XML format - multiple propertylist elements!");
            }
            if (this.transferPackage != null) return;
            this.transferPackage = new TransferPackage(this.exportScript);
            return;
        } else if (qName.equalsIgnoreCase("property")) {
            if (this.transferPackage == null) {
                throw new SAXException("Incorrect XML format - property element found without propertylist element!");
            }
            this.currentPropertyId = attr.getProperty("id");
            return;
        } else if (qName.equalsIgnoreCase("transfer")) {
            if (this.transferPackage == null) {
                this.transferPackage = new TransferPackage(this.exportScript);
            }
            if (this.exportScript && this.transferPackage.size() > 0) {
                throw new SAXException("Invalid script - export script can only contain one transfer element");
            }
            Transfer transfer = new Transfer(attr.getProperty("id"));
            if (attr.getProperty("type") != null) {
                transfer.setType(attr.getProperty("type"));
            }
            this.transferPackage.addTransfer(transfer);
            this.currentPropertyId = attr.getProperty("id");
            return;
        } else if (qName.equalsIgnoreCase("exportXML")) {
            if (this.transferPackage == null) {
                throw new SAXException("Incorrect XML format - exportXML element found without transfer element!");
            }
            if (this.exportScript && this.transferPackage.getTransfer(this.transferPackage.size() - 1).getTransfers().size() > 0) {
                throw new SAXException("Invalid script - export script can only contain one export element");
            }
            Transfer transfer = this.transferPackage.getTransfer(this.transferPackage.size() - 1);
            ExportXML export = new ExportXML();
            if (!this.exportScript) {
                if (attr.getProperty("dir") != null && attr.getProperty("dir").length() > 0) {
                    export.setDirname(attr.getProperty("dir"));
                }
                if (attr.getProperty("file") != null && attr.getProperty("file").length() > 0) {
                    export.setFilename(attr.getProperty("file"));
                }
                if (attr.getProperty("multifile") != null && attr.getProperty("multifile").length() > 0) {
                    export.setMultifile("Y".equals(attr.getProperty("multifile")) || "true".equals(attr.getProperty("multifile")));
                }
                if (attr.getProperty("forceupdate") != null) {
                    export.setForceUpdate(attr.getProperty("forceupdate"));
                }
                if (attr.getProperty("forcenullupdate") != null) {
                    export.setForceNullUpdate(attr.getProperty("forcenullupdate"));
                }
            }
            transfer.addTransfer(export);
            return;
        } else if (qName.equalsIgnoreCase("sdi")) {
            if (this.transferPackage == null) {
                throw new SAXException("Incorrect XML format - sdi element found without transfer element!");
            }
            Transfer transfer = this.transferPackage.getTransfer(this.transferPackage.size() - 1);
            Object o = transfer.getLatestTransfer();
            if (!(o instanceof ExportXML)) throw new SAXException("Incorrect XML format - sdi element found without an export element!");
            ExportXML export = (ExportXML)o;
            SDITransfer sdi = new SDITransfer(attr.getProperty("sdcid"));
            if (attr.getProperty("file") != null) {
                sdi.setFilename(attr.getProperty("file"));
                sdi.setFile(new File(attr.getProperty("file")));
            }
            if (attr.getProperty("keyfile") != null) {
                sdi.setKeyFilename(attr.getProperty("keyfile"));
            }
            sdi.setKeyid1(attr.getProperty("keyid1"));
            sdi.setKeyid2(attr.getProperty("keyid2"));
            sdi.setKeyid3(attr.getProperty("keyid3"));
            sdi.setCategoryid(attr.getProperty("categoryid"));
            sdi.setQueryfrom(attr.getProperty("queryfrom"));
            sdi.setQuerywhere(attr.getProperty("querywhere"));
            sdi.setQueryorderby(attr.getProperty("queryorderby"));
            sdi.setRsetid(attr.getProperty("rsetid"));
            sdi.setExportid(attr.getProperty("exportid"));
            if (attr.getProperty("altkeycols") != null) {
                sdi.setAltkeycols(attr.getProperty("altkeycols"));
            }
            if (attr.getProperty("keyseparator") != null) {
                sdi.setKeyseparator(attr.getProperty("keyseparator"));
            }
            sdi.setSyncDataModel(attr.getProperty("syncdatamodel"));
            if (attr.getProperty("excludeauditcolumns") != null) {
                sdi.setExcludeAuditColumns(attr.getProperty("excludeauditcolumns").equals("Y") || attr.getProperty("excludeauditcolumns").equals("true"));
            }
            sdi.setPrimaryForceUpdate(attr.getProperty("primaryforceupdate") != null ? attr.getProperty("primaryforceupdate") : export.getForceUpdate());
            sdi.setPrimaryForceNullUpdate(attr.getProperty("primaryforcenullupdate") != null ? attr.getProperty("primaryforcenullupdate") : export.getForceNullUpdate());
            sdi.setDetailForceUpdate(attr.getProperty("detailforceupdate") != null ? attr.getProperty("detailforceupdate") : export.getForceUpdate());
            sdi.setDetailForceNullUpdate(attr.getProperty("detailforcenullupdate") != null ? attr.getProperty("detailforcenullupdate") : export.getForceNullUpdate());
            if (attr.getProperty("exportdetails") != null) {
                sdi.setExportDetails(attr.getProperty("exportdetails"));
            }
            if (attr.getProperty("flushsdi") != null) {
                sdi.setFlushSDI(attr.getProperty("flushsdi"));
            }
            if (attr.getProperty("flushdetails") != null) {
                sdi.setFlushDetails(attr.getProperty("flushdetails"));
            }
            if (attr.getProperty("exportsdidetails") != null) {
                sdi.setExportSDIDetails(attr.getProperty("exportsdidetails"));
            }
            if (attr.getProperty("flushsdidetails") != null) {
                sdi.setFlushSDIDetails(attr.getProperty("flushsdidetails"));
            }
            if (attr.getProperty("flushchildsdi") != null) {
                sdi.setFlushChildSDI(attr.getProperty("flushchildsdi"));
            }
            if (attr.getProperty("exportfkdetails") != null) {
                sdi.setExportFKDetails(attr.getProperty("exportfkdetails"));
            }
            if (attr.getProperty("exportsecuritydetails") != null) {
                sdi.setExportSecurityDetails(attr.getProperty("exportsecuritydetails"));
            }
            if (attr.getProperty("exportroles") == null || attr.getProperty("exportroles").equals("Y") || attr.getProperty("exportroles").equals("true")) {
                SDIDetail role = new SDIDetail("sdirole");
                role.setFlush(attr.getProperty("flushroles") == null || attr.getProperty("flushroles").equals("Y") || attr.getProperty("flushroles").equals("true"));
                sdi.addSDIDetail(role);
            }
            if (attr.getProperty("exportcategories") == null || attr.getProperty("exportcategories").equals("Y") || attr.getProperty("exportcategories").equals("true")) {
                SDIDetail categoryitem = new SDIDetail("categoryitem");
                categoryitem.setFlush(attr.getProperty("flushcategories") == null || attr.getProperty("flushcategories").equals("Y") || attr.getProperty("flushcategories").equals("true"));
                sdi.addSDIDetail(categoryitem);
            }
            export.addExport(sdi);
            this.currentParentElement = "sdi";
            return;
        } else if (qName.equalsIgnoreCase("sdc")) {
            if (this.transferPackage == null) {
                throw new SAXException("Incorrect XML format - sdc element found without transfer element!");
            }
            Transfer transfer = this.transferPackage.getTransfer(this.transferPackage.size() - 1);
            Object o = transfer.getLatestTransfer();
            if (!(o instanceof ExportXML)) throw new SAXException("Incorrect XML format - sdi element found without an export element!");
            ExportXML export = (ExportXML)o;
            SDCTransfer sdc = new SDCTransfer(attr.getProperty("sdcid"));
            if (attr.getProperty("file") != null) {
                sdc.setFilename(attr.getProperty("file"));
            }
            sdc.setCategoryid(attr.getProperty("categoryid"));
            if (attr.getProperty("excludeauditcolumns") != null) {
                sdc.setExcludeAuditColumns(attr.getProperty("excludeauditcolumns").equals("Y") || attr.getProperty("excludeauditcolumns").equals("true"));
            }
            if (attr.getProperty("excludelinktables") != null) {
                sdc.setExcludelinktables(attr.getProperty("excludelinktables").equals("Y") || attr.getProperty("excludelinktables").equals("true"));
            }
            sdc.setForceUpdate(attr.getProperty("forceupdate") != null ? attr.getProperty("forceupdate") : export.getForceUpdate());
            sdc.setForceNullUpdate(attr.getProperty("forcenullupdate") != null ? attr.getProperty("forcenullupdate") : export.getForceNullUpdate());
            if (attr.getProperty("flushupdtables") != null) {
                sdc.setFlushupdtables(attr.getProperty("flushupdtables").equals("Y") || attr.getProperty("flushupdtables").equals("true"));
            }
            export.addExport(sdc);
            this.currentParentElement = "sdi";
            return;
        } else if (qName.equalsIgnoreCase("table")) {
            if (this.transferPackage == null) {
                throw new SAXException("Incorrect XML format - table element found without transfer element!");
            }
            Transfer transfer = this.transferPackage.getTransfer(this.transferPackage.size() - 1);
            Object o = transfer.getLatestTransfer();
            if (!(o instanceof ExportXML)) throw new SAXException("Incorrect XML format - table element found without an export element!");
            ExportXML export = (ExportXML)o;
            TableTransfer table = new TableTransfer(attr.getProperty("tableid"));
            if (attr.getProperty("file") != null) {
                table.setFile(new File(attr.getProperty("file")));
            }
            if (attr.getProperty("keyfile") != null) {
                table.setKeyFilename(attr.getProperty("keyfile"));
            }
            table.setKeyseparator(attr.getProperty("keyseparator"));
            table.setFrom(attr.getProperty("from"));
            table.setWhere(attr.getProperty("where"));
            table.setOrderby(attr.getProperty("orderby"));
            table.setTablealias(attr.getProperty("tablealias"));
            table.setDefaultForceUpdate(export.getForceUpdate());
            table.setDefaultForceNullUpdate(export.getForceNullUpdate());
            table.setForceLOBExport(attr.getProperty("forcelobexport", "false").equalsIgnoreCase("true"));
            export.addExport(table);
            this.currentParentElement = "table";
            return;
        } else if (qName.equalsIgnoreCase("sdidetail")) {
            if (this.transferPackage == null) {
                throw new SAXException("Incorrect XML format - sdidetail element found without transfer element!");
            }
            Transfer transfer = this.transferPackage.getTransfer(this.transferPackage.size() - 1);
            Object o = transfer.getLatestTransfer();
            if (!(o instanceof ExportXML)) throw new SAXException("Incorrect XML format - sdidetail element found without an export element!");
            ExportXML export = (ExportXML)o;
            Transferable transferable = export.getLatestExport();
            SDIDetail sdidetail = new SDIDetail(attr.getProperty("detailid"));
            sdidetail.setExtendedwhere(attr.getProperty("extendedwhere"));
            if (!(transferable instanceof SDITransfer)) {
                throw new SAXException("Incorrect XML format - detail element found without an SDI element!");
            }
            SDITransfer sdi = (SDITransfer)transferable;
            sdi.addSDIDetail(sdidetail);
            this.currentParentElement = "sdidetail";
            return;
        } else if (qName.equalsIgnoreCase("column")) {
            if (this.transferPackage == null) {
                throw new SAXException("Incorrect XML format - column element found without transfer element!");
            }
            Transfer transfer = this.transferPackage.getTransfer(this.transferPackage.size() - 1);
            Object o = transfer.getLatestTransfer();
            if (!(o instanceof ExportXML)) throw new SAXException("Incorrect XML format - column element found without an export element!");
            ExportXML export = (ExportXML)o;
            Transferable transferable = export.getLatestExport();
            Column column = new Column(attr.getProperty("columnid"));
            column.setForceUpdate(attr.getProperty("forceupdate") != null ? attr.getProperty("forceupdate") : export.getForceUpdate());
            column.setForceNullUpdate(attr.getProperty("forcenullupdate") != null ? attr.getProperty("forcenullupdate") : export.getForceNullUpdate());
            column.setValue(attr.getProperty("value"));
            column.setFile(attr.getProperty("file"));
            column.setExcluded(attr.getProperty("excluded", "").equals("true"));
            if (this.currentParentElement.equals("sdi") || this.currentParentElement.equals("table")) {
                if (!(transferable instanceof TableTransfer) && !(transferable instanceof SDITransfer) && !(transferable instanceof SDCTransfer)) throw new SAXException("Incorrect XML format - column element found without an SDI or table element!");
                ((AbstractTransferable)transferable).addColumn(column);
                return;
            } else {
                if (!this.currentParentElement.equals("sdidetail")) throw new SAXException("Incorrect XML format - sdielement found within an unrecognized element!");
                if (!(transferable instanceof SDITransfer)) throw new SAXException("Incorrect XML format - sdielement found within an unrecognized transferable!");
                SDITransfer sdi = (SDITransfer)transferable;
                SDIDetail sdiDetail = sdi.getLatestSDIDetail();
                if (sdiDetail == null) {
                    throw new SAXException("SDIDetail parent element for column '" + column.getColumnid() + "' not found!");
                }
                sdiDetail.addColumn(column);
            }
            return;
        } else {
            if (qName.equalsIgnoreCase("data")) {
                if (this.transferPackage == null) {
                    throw new SAXException("Incorrect XML format - data element found without transfer element!");
                }
                Transfer transfer = this.transferPackage.getTransfer(this.transferPackage.size() - 1);
                Object o = transfer.getLatestTransfer();
                if (!(o instanceof ExportXML)) throw new SAXException("Incorrect XML format - data element found without an export element!");
                ExportXML export = (ExportXML)o;
                Transferable transferable = export.getLatestExport();
                if (transferable instanceof TableTransfer || transferable instanceof SDITransfer) return;
                throw new SAXException("Incorrect XML format - data element found without an SDI or table element!");
            }
            if (qName.equalsIgnoreCase("importXML")) {
                if (this.transferPackage == null) {
                    throw new SAXException("Incorrect XML format - importXML element found without transfer element!");
                }
                Transfer transfer = this.transferPackage.getTransfer(this.transferPackage.size() - 1);
                ImportXML importxml = new ImportXML();
                if (attr.getProperty("dir") != null && attr.getProperty("dir").length() > 0) {
                    importxml.setDirname(attr.getProperty("dir"));
                }
                if (attr.getProperty("forceupdate") != null) {
                    importxml.setImportForceUpdate(attr.getProperty("forceupdate"));
                }
                transfer.addTransfer(importxml);
                return;
            } else if (qName.equalsIgnoreCase("file")) {
                if (this.transferPackage == null) {
                    throw new SAXException("Incorrect XML format - file element found without transfer element!");
                }
                Transfer transfer = this.transferPackage.getTransfer(this.transferPackage.size() - 1);
                Object o = transfer.getLatestTransfer();
                if (!(o instanceof ImportXML)) throw new SAXException("Incorrect XML format - file element found without an import element!");
                ImportXML importxml = (ImportXML)o;
                importxml.addImport(new File(attr.getProperty("file")));
                return;
            } else {
                String err = "Unrecognized element '" + qName + "' found in document " + this._xmlFile.getName();
                this.log(err);
                this.println(err);
                throw new SAXException(err);
            }
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("transferpackage")) {
            this.log("Load complete");
            return;
        } else if (qName.equalsIgnoreCase("propertylist")) {
            this.processedPropertyList = true;
            return;
        } else if (qName.equalsIgnoreCase("property")) {
            String propertyValue = this.currentElementChars.toString().trim();
            if (this.currentElementChars.toString().trim().length() == 0 && this.currentElementChars.toString().length() > 0) {
                propertyValue = StringUtil.repeat(" ", this.currentElementChars.length());
            }
            this.transferPackage.addProperty(this.currentPropertyId, propertyValue);
            return;
        } else {
            if (!qName.equalsIgnoreCase("data")) return;
            Transfer transfer = this.transferPackage.getTransfer(this.transferPackage.size() - 1);
            Object o = transfer.getLatestTransfer();
            ExportXML export = (ExportXML)o;
            Transferable transferable = export.getLatestExport();
            if (this.currentParentElement.equals("sdi") || this.currentParentElement.equals("table")) {
                if (transferable instanceof TableTransfer) {
                    TableTransfer table = (TableTransfer)transferable;
                    table.setData(this.currentElementChars.toString());
                    return;
                } else {
                    if (!(transferable instanceof SDITransfer)) return;
                    SDITransfer sdi = (SDITransfer)transferable;
                    sdi.setData(this.currentElementChars.toString());
                }
                return;
            } else {
                if (!this.currentParentElement.equals("sdidetail")) throw new SAXException("Incorrect XML format - data element found within an unrecognized element!");
                if (!(transferable instanceof SDITransfer)) throw new SAXException("Incorrect XML format - data element found within an unrecognized transferable!");
                SDITransfer sdi = (SDITransfer)transferable;
                SDIDetail sdiDetail = sdi.getLatestSDIDetail();
                if (sdiDetail == null) {
                    throw new SAXException("SDIDetail not found!");
                }
                sdiDetail.setData(this.currentElementChars.toString());
            }
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
        super.log(message);
        if (this.logger != null) {
            this.logger.log(message);
        }
    }
}

