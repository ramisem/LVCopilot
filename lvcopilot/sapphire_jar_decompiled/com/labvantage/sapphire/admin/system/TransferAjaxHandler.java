/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.admin.system;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.actions.transfer.ProcessTransfer;
import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.xml.AbstractTransferable;
import com.labvantage.sapphire.xml.Column;
import com.labvantage.sapphire.xml.ExportXML;
import com.labvantage.sapphire.xml.ImportXML;
import com.labvantage.sapphire.xml.SDIDetail;
import com.labvantage.sapphire.xml.SDITransfer;
import com.labvantage.sapphire.xml.TableTransfer;
import com.labvantage.sapphire.xml.Transfer;
import com.labvantage.sapphire.xml.TransferPackage;
import com.labvantage.sapphire.xml.Transferable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class TransferAjaxHandler
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 103913 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        TransferPackage transferPackage;
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "handleCallResponse");
        String mode = ajaxResponse.getRequestParameter("mode");
        String transferId = ajaxResponse.getRequestParameter("transferid");
        String exportedFromSDC = ajaxResponse.getRequestParameter("exportedFromSDC");
        if (exportedFromSDC.equals("false")) {
            try {
                if (OpalUtil.isEmpty(transferId)) {
                    throw new SapphireException("TransferId is empty!!");
                }
            }
            catch (SapphireException e) {
                ajaxResponse.setError(e.getMessage());
            }
            transferPackage = (TransferPackage)request.getSession().getAttribute("transferpackage_" + transferId);
        } else {
            transferPackage = (TransferPackage)request.getSession().getAttribute("transferpackage");
        }
        if (transferPackage == null) {
            transferPackage = new TransferPackage();
        }
        String selected = "";
        String message = "&nbsp;";
        try {
            String[] selection;
            Transfer transfer;
            if ("runtransferpackage".equals(mode)) {
                ActionProcessor ap = this.getActionProcessor();
                PropertyList props = new PropertyList();
                props.putAll(ajaxResponse.getRequestParameters());
                ap.processActionClass(ProcessTransfer.class.getName(), props);
            } else if ("newscript".equals(mode)) {
                transferPackage = new TransferPackage();
                transfer = new Transfer(ajaxResponse.getRequestParameter("transferid"));
                ConfigurationProcessor cp = new ConfigurationProcessor(this.getConnectionId());
                if (ajaxResponse.getRequestParameter("transfermode", "E").equals("E")) {
                    if (ajaxResponse.getRequestParameter("tempfileoutput", "N").equalsIgnoreCase("Y")) {
                        ExportXML exportXML = new ExportXML();
                        transfer.addTransfer(exportXML);
                    } else {
                        transferPackage.addProperty("export.dir", cp.getSysConfigProperty("exportdir"));
                        ExportXML exportXML = new ExportXML();
                        exportXML.setDirname("[export.dir]");
                        transfer.addTransfer(exportXML);
                    }
                    transfer.setType("export");
                } else {
                    transferPackage.addProperty("[import.dir]", cp.getSysConfigProperty("importdir"));
                    ImportXML importXML = new ImportXML();
                    transfer.addTransfer(importXML);
                    importXML.setDirname("[import.dir]");
                    transfer.setType("import");
                }
                transferPackage.addTransfer(transfer);
                selected = "transferitem_0_0";
            } else if ("newexportscript".equals(mode)) {
                transferPackage = new TransferPackage(true);
                transferPackage.addProperty("export.sdcid", "");
                transferPackage.addProperty("export.keyid1", "");
                transferPackage.addProperty("export.keyid2", "");
                transferPackage.addProperty("export.keyid3", "");
                transferPackage.addProperty("export.rsetid", "");
                transfer = new Transfer("exportscript");
                ExportXML exportXML = new ExportXML();
                SDITransfer sdi = new SDITransfer(ajaxResponse.getRequestParameter("sdcid"));
                sdi.setRsetid("[export.rsetid]");
                exportXML.addExport(sdi);
                transfer.addTransfer(exportXML);
                transferPackage.addTransfer(transfer);
                selected = "transfer_0";
            } else if ("loadexportscript".equals(mode)) {
                transferPackage.loadXML(StringUtil.unescape(ajaxResponse.getRequestParameter("transferpackagescript")));
                selected = "transfer_0";
            } else if ("newproperty".equals(mode)) {
                transferPackage.addProperty(ajaxResponse.getRequestParameter("propertyid"), ajaxResponse.getRequestParameter("propertyvalue"));
                selected = "property_" + ajaxResponse.getRequestParameter("selected");
            } else if ("editproperty".equals(mode)) {
                transferPackage.addProperty(ajaxResponse.getRequestParameter("propertyid"), ajaxResponse.getRequestParameter("propertyvalue"));
                selected = ajaxResponse.getRequestParameter("selected");
            } else if ("newtransfer".equals(mode)) {
                selection = StringUtil.split(ajaxResponse.getRequestParameter("selected"), "_");
                Transfer transfer2 = new Transfer(ajaxResponse.getRequestParameter("transferid"));
                transfer2.setType(ajaxResponse.getRequestParameter("exporttype"));
                if (ajaxResponse.getRequestParameter("selected").length() > 0 && selection.length > 0) {
                    transferPackage.addTransfer(Integer.parseInt(selection[1]) + 1, transfer2);
                    selected = "transfer_" + String.valueOf(Integer.parseInt(selection[1]) + 1);
                } else {
                    transferPackage.addTransfer(transfer2);
                    selected = "transfer_" + String.valueOf(transferPackage.size() - 1);
                }
            } else if ("edittransfer".equals(mode)) {
                selection = StringUtil.split(ajaxResponse.getRequestParameter("selected"), "_");
                Transfer transfer3 = transferPackage.getTransfer(Integer.parseInt(selection[1]));
                transfer3.setId(ajaxResponse.getRequestParameter("transferid"));
                transfer3.setType(ajaxResponse.getRequestParameter("exporttype"));
                selected = ajaxResponse.getRequestParameter("selected");
            } else if ("newexport".equals(mode)) {
                selection = StringUtil.split(ajaxResponse.getRequestParameter("selected"), "_");
                Transfer transfer4 = transferPackage.getTransfer(Integer.parseInt(selection[1]));
                ExportXML export = new ExportXML();
                export.setDirname(ajaxResponse.getRequestParameter("exportdir"));
                export.setForceUpdate(ajaxResponse.getRequestParameter("forceupdate"));
                export.setForceNullUpdate(ajaxResponse.getRequestParameter("forcenullupdate"));
                transfer4.addTransfer(export);
                selected = "transferitem_" + selection[1] + "_" + String.valueOf(transfer4.getTransfers().size() - 1);
            } else if ("editexport".equals(mode)) {
                selection = StringUtil.split(ajaxResponse.getRequestParameter("selected"), "_");
                Transfer transfer5 = transferPackage.getTransfer(Integer.parseInt(selection[1]));
                ExportXML export = (ExportXML)transfer5.getTransfer(Integer.parseInt(selection[2]));
                export.setDirname(ajaxResponse.getRequestParameter("exportdir"));
                export.setForceUpdate(ajaxResponse.getRequestParameter("forceupdate"));
                export.setForceNullUpdate(ajaxResponse.getRequestParameter("forcenullupdate"));
                selected = ajaxResponse.getRequestParameter("selected");
            } else if ("newimport".equals(mode)) {
                selection = StringUtil.split(ajaxResponse.getRequestParameter("selected"), "_");
                Transfer transfer6 = transferPackage.getTransfer(Integer.parseInt(selection[1]));
                ImportXML importxml = new ImportXML();
                importxml.setDirname(ajaxResponse.getRequestParameter("importdir"));
                importxml.setImportForceUpdate(ajaxResponse.getRequestParameter("forceupdate"));
                transfer6.addTransfer(importxml);
                selected = "transferitem_" + selection[1] + "_" + String.valueOf(transfer6.getTransfers().size() - 1);
            } else if ("editimport".equals(mode)) {
                selection = StringUtil.split(ajaxResponse.getRequestParameter("selected"), "_");
                Transfer transfer7 = transferPackage.getTransfer(Integer.parseInt(selection[1]));
                ImportXML importxml = (ImportXML)transfer7.getTransfer(Integer.parseInt(selection[2]));
                importxml.setDirname(ajaxResponse.getRequestParameter("importdir"));
                importxml.setImportForceUpdate(ajaxResponse.getRequestParameter("forceupdate"));
                selected = ajaxResponse.getRequestParameter("selected");
            } else if ("newimportfile".equals(mode)) {
                selection = StringUtil.split(ajaxResponse.getRequestParameter("selected"), "_");
                Transfer transfer8 = transferPackage.getTransfer(Integer.parseInt(selection[1]));
                ImportXML importxml = (ImportXML)transfer8.getTransfer(Integer.parseInt(selection[2]));
                importxml.addImport(ajaxResponse.getRequestParameter("importfile").length() > 0 ? new File(ajaxResponse.getRequestParameter("importfile")) : null);
                selected = "file_" + selection[1] + "_" + selection[2] + "_" + String.valueOf(importxml.getImportFiles().size() - 1);
            } else if ("editimportfile".equals(mode)) {
                selection = StringUtil.split(ajaxResponse.getRequestParameter("selected"), "_");
                Transfer transfer9 = transferPackage.getTransfer(Integer.parseInt(selection[1]));
                ImportXML importxml = (ImportXML)transfer9.getTransfer(Integer.parseInt(selection[2]));
                importxml.getImportFiles().set(Integer.parseInt(selection[3]), ajaxResponse.getRequestParameter("importfile").length() > 0 ? new File(ajaxResponse.getRequestParameter("importfile")) : null);
                selected = ajaxResponse.getRequestParameter("selected");
            } else if ("newsdiexport".equals(mode)) {
                selection = StringUtil.split(ajaxResponse.getRequestParameter("selected"), "_");
                Transfer transfer10 = transferPackage.getTransfer(Integer.parseInt(selection[1]));
                ExportXML export = (ExportXML)transfer10.getTransfer(Integer.parseInt(selection[2]));
                SDITransfer sdi = new SDITransfer(ajaxResponse.getRequestParameter("sdcid"));
                this.setSDITransferOptions(sdi, ajaxResponse);
                export.addExport(sdi);
                selected = "transferable_" + selection[1] + "_" + selection[2] + "_" + String.valueOf(export.getExports().size() - 1);
            } else if ("editsdiexport".equals(mode)) {
                selection = StringUtil.split(ajaxResponse.getRequestParameter("selected"), "_");
                Transfer transfer11 = transferPackage.getTransfer(Integer.parseInt(selection[1]));
                ExportXML export = (ExportXML)transfer11.getTransfer(Integer.parseInt(selection[2]));
                SDITransfer sdi = (SDITransfer)export.getExport(Integer.parseInt(selection[3]));
                sdi.reset();
                this.setSDITransferOptions(sdi, ajaxResponse);
                selected = ajaxResponse.getRequestParameter("selected");
            } else if ("moveitem".equals(mode)) {
                String direction = ajaxResponse.getRequestParameter("direction");
                String[] selection2 = StringUtil.split(ajaxResponse.getRequestParameter("selected"), "_");
                Transfer transfer12 = transferPackage.getTransfer(Integer.parseInt(selection2[1]));
                ExportXML export = (ExportXML)transfer12.getTransfer(Integer.parseInt(selection2[2]));
                int pos = Integer.parseInt(selection2[3]);
                pos = export.moveExport(pos, "U".equals(direction) ? -1 : 1);
                StringBuffer newselection = new StringBuffer();
                for (int i = 0; i < selection2.length; ++i) {
                    newselection.append("_").append(i == 3 ? String.valueOf(pos) : selection2[i]);
                }
                selected = newselection.substring(1);
            } else if ("newtableexport".equals(mode) || "edittableexport".equals(mode)) {
                TableTransfer table;
                selection = StringUtil.split(ajaxResponse.getRequestParameter("selected"), "_");
                Transfer transfer13 = transferPackage.getTransfer(Integer.parseInt(selection[1]));
                ExportXML export = (ExportXML)transfer13.getTransfer(Integer.parseInt(selection[2]));
                if ("newtableexport".equals(mode)) {
                    table = new TableTransfer(ajaxResponse.getRequestParameter("tableid"));
                    export.addExport(table);
                    selected = "transferable_" + selection[1] + "_" + selection[2] + "_" + String.valueOf(export.getExports().size() - 1);
                } else {
                    table = (TableTransfer)export.getExport(Integer.parseInt(selection[3]));
                    selected = ajaxResponse.getRequestParameter("selected");
                }
                this.setTableTransferOptions(table, ajaxResponse);
            } else if ("newcolumn".equals(mode) || "editcolumn".equals(mode)) {
                selection = StringUtil.split(ajaxResponse.getRequestParameter("selected"), "_");
                Transfer transfer14 = transferPackage.getTransfer(Integer.parseInt(selection[1]));
                ExportXML export = (ExportXML)transfer14.getTransfer(Integer.parseInt(selection[2]));
                Transferable transferable = export.getExport(Integer.parseInt(selection[3]));
                if ("newcolumn".equals(mode)) {
                    Column column = new Column(ajaxResponse.getRequestParameter("columnid"));
                    this.setColumnOptions(column, ajaxResponse);
                    AbstractTransferable sdiOrTable = (AbstractTransferable)transferable;
                    sdiOrTable.addColumn(column);
                    selected = "column_" + selection[1] + "_" + selection[2] + "_" + selection[3] + "_" + String.valueOf(sdiOrTable.getColumns().size() - 1);
                } else {
                    AbstractTransferable sdiOrTable = (AbstractTransferable)transferable;
                    Column column = sdiOrTable.getColumn(ajaxResponse.getRequestParameter("columnid"));
                    this.setColumnOptions(column, ajaxResponse);
                    selected = ajaxResponse.getRequestParameter("selected");
                }
            } else if ("deleteitem".equals(mode)) {
                selection = StringUtil.split(ajaxResponse.getRequestParameter("selected"), "_");
                if (selection.length == 2) {
                    if (ajaxResponse.getRequestParameter("selected").startsWith("property_")) {
                        transferPackage.removeProperty(selection[1]);
                    } else {
                        transferPackage.removeTransfer(Integer.parseInt(selection[1]));
                    }
                } else if (selection.length == 3) {
                    Transfer transfer15 = transferPackage.getTransfer(Integer.parseInt(selection[1]));
                    transfer15.getTransfers().remove(Integer.parseInt(selection[2]));
                } else if (selection.length >= 4) {
                    Transfer transfer16 = transferPackage.getTransfer(Integer.parseInt(selection[1]));
                    Object o = transfer16.getTransfers().get(Integer.parseInt(selection[2]));
                    if (o instanceof ExportXML) {
                        ExportXML export = (ExportXML)o;
                        if (selection.length == 4) {
                            export.getExports().remove(Integer.parseInt(selection[3]));
                        } else {
                            AbstractTransferable transferable = (AbstractTransferable)export.getExports().get(Integer.parseInt(selection[3]));
                            transferable.getColumns().remove(Integer.parseInt(selection[4]));
                        }
                    } else if (o instanceof ImportXML) {
                        ImportXML importxml = (ImportXML)o;
                        importxml.getImportFiles().remove(Integer.parseInt(selection[3]));
                    }
                }
            } else {
                throw new SapphireException("Mode not specified!");
            }
            request.getSession().setAttribute("transferpackage_" + transferId, (Object)transferPackage);
        }
        catch (SapphireException e) {
            this.logError("Failed to transfer properties", e);
            message = e.getMessage();
        }
        try {
            ajaxResponse.addCallbackArgument("html", this.getTransferFileHTML(transferPackage, message, selected, ajaxResponse.getRequestParameter("tempfileoutput", "N").equalsIgnoreCase("Y")));
            ajaxResponse.addCallbackArgument("xml", StringUtil.escape(transferPackage.toXML()));
            ajaxResponse.print();
        }
        catch (SapphireException e) {
            ajaxResponse.setError(e.getMessage());
        }
    }

    private void setSDITransferOptions(SDITransfer sdi, AjaxResponse ajaxResponse) {
        sdi.setSdcid(ajaxResponse.getRequestParameter("sdcid"));
        sdi.setFile(ajaxResponse.getRequestParameter("exportfile").length() > 0 ? new File(ajaxResponse.getRequestParameter("exportfile")) : null);
        sdi.setExportDetails(ajaxResponse.getRequestParameter("adddetail"));
        sdi.setFlushSDI(ajaxResponse.getRequestParameter("flushsdi"));
        sdi.setFlushDetails(ajaxResponse.getRequestParameter("flushdetail"));
        sdi.setExportSDIDetails(ajaxResponse.getRequestParameter("addsdidetail"));
        sdi.setFlushSDIDetails(ajaxResponse.getRequestParameter("flushsdidetail"));
        sdi.setFlushChildSDI(ajaxResponse.getRequestParameter("flushchildsdi"));
        sdi.setExportFKDetails(ajaxResponse.getRequestParameter("addfkdetail"));
        sdi.setExportSecurityDetails(ajaxResponse.getRequestParameter("addsecuritydetail"));
        if (ajaxResponse.getRequestParameter("addroles").equals("true")) {
            SDIDetail role = sdi.getSDIDetail("sdirole");
            if (role == null) {
                role = new SDIDetail("sdirole");
            }
            role.setFlush(ajaxResponse.getRequestParameter("flushroles").equals("true"));
            sdi.addSDIDetail(role);
        } else {
            sdi.removeSDIDetail("sdirole");
        }
        if (ajaxResponse.getRequestParameter("addcategories").equals("true")) {
            SDIDetail categoryitem = sdi.getSDIDetail("categoryitem");
            if (categoryitem == null) {
                categoryitem = new SDIDetail("categoryitem");
            }
            categoryitem.setFlush(ajaxResponse.getRequestParameter("flushcategories").equals("true"));
            sdi.addSDIDetail(categoryitem);
        } else {
            sdi.removeSDIDetail("categoryitem");
        }
        sdi.setExcludeAuditColumns(ajaxResponse.getRequestParameter("excludeauditcolumns").equals("true"));
        sdi.setPrimaryForceUpdate(ajaxResponse.getRequestParameter("forceupdate"));
        sdi.setPrimaryForceNullUpdate(ajaxResponse.getRequestParameter("forcenullupdate"));
        sdi.setDetailForceUpdate(ajaxResponse.getRequestParameter("forceupdate"));
        sdi.setDetailForceNullUpdate(ajaxResponse.getRequestParameter("forcenullupdate"));
        sdi.setSyncDataModel(ajaxResponse.getRequestParameter("syncdatamodel"));
        sdi.setExportid(ajaxResponse.getRequestParameter("exportid"));
        sdi.setAltkeycols(ajaxResponse.getRequestParameter("altkeycols"));
        String type = ajaxResponse.getRequestParameter("type");
        if ("keyid".equals(type)) {
            sdi.setKeyid1(ajaxResponse.getRequestParameter("keyid1"));
            sdi.setKeyid2(ajaxResponse.getRequestParameter("keyid2"));
            sdi.setKeyid3(ajaxResponse.getRequestParameter("keyid3"));
        } else if ("category".equals(type)) {
            sdi.setCategoryid(ajaxResponse.getRequestParameter("categoryid"));
        } else if ("rset".equals(type)) {
            sdi.setRsetid(ajaxResponse.getRequestParameter("rsetid"));
        } else if ("keyfile".equals(type)) {
            sdi.setKeyFilename(ajaxResponse.getRequestParameter("keyfile"));
        } else {
            sdi.setQueryfrom(ajaxResponse.getRequestParameter("queryfrom"));
            sdi.setQuerywhere(ajaxResponse.getRequestParameter("querywhere"));
            sdi.setQueryorderby(ajaxResponse.getRequestParameter("queryorderby"));
        }
    }

    private void setTableTransferOptions(TableTransfer table, AjaxResponse ajaxResponse) {
        table.setTableid(ajaxResponse.getRequestParameter("tableid"));
        table.setFile(ajaxResponse.getRequestParameter("exportfile").length() > 0 ? new File(ajaxResponse.getRequestParameter("exportfile")) : null);
        table.setFrom(ajaxResponse.getRequestParameter("queryfrom"));
        table.setWhere(ajaxResponse.getRequestParameter("querywhere"));
        table.setOrderby(ajaxResponse.getRequestParameter("queryorderby"));
    }

    private void setColumnOptions(Column column, AjaxResponse ajaxResponse) {
        column.setColumnid(ajaxResponse.getRequestParameter("columnid"));
        column.setForceUpdate(ajaxResponse.getRequestParameter("forceupdate"));
        column.setForceNullUpdate(ajaxResponse.getRequestParameter("forcenullupdate"));
        column.setExcluded(ajaxResponse.getRequestParameter("excluded").equals("true"));
        column.setValue(ajaxResponse.getRequestParameter("value"));
        column.setFile(ajaxResponse.getRequestParameter("file"));
    }

    private String getTransferFileHTML(TransferPackage transferPackage, String message, String selected, boolean usetempfile) {
        StringBuffer html = new StringBuffer();
        if (!transferPackage.isExportScript() && !message.equals("&nbsp;")) {
            html.append("<table cellspacing=\"0\" cellpadding=\"3\">");
            html.append("<tr><td>").append(message).append("</td></tr>");
            html.append("</table>");
        }
        html.append("<table border=\"1\" cellspacing=\"0\" cellpadding=\"3\" class=\"gridmaint_table\" id=\"exporttable\" width=\"100%\">");
        this.getPropertyHtml(transferPackage, html, selected);
        for (int i = 0; i < transferPackage.size(); ++i) {
            Transfer transfer = transferPackage.getTransfer(i);
            String transferSelector = "transfer_" + i;
            this.getTransferHtml(transferPackage, html, transferSelector, selected, transfer);
            ArrayList transfers = transfer.getTransfers();
            for (int j = 0; j < transfers.size(); ++j) {
                Object o = transfers.get(j);
                if (o instanceof ExportXML) {
                    this.getExportHtml(transferPackage, o, i, j, html, selected, usetempfile);
                    continue;
                }
                if (!(o instanceof ImportXML)) continue;
                this.getImportHtml(transferPackage, o, i, j, html, selected);
            }
        }
        html.append("</table>");
        return html.toString();
    }

    private void getTransferHtml(TransferPackage transferPackage, StringBuffer html, String transferSelector, String selected, Transfer transfer) {
        html.append("<tr>");
        if (!transferPackage.isExportScript()) {
            html.append("<td colspan=\"5\" valign=\"top\" class=\"gridmaint_fieldtitle\" id=\"td_").append(transferSelector).append("\"").append(" transferid=\"").append(transfer.getId()).append("\"").append(" type=\"").append(transfer.getType()).append("\"").append("><label for=\"").append(transferSelector).append("\">Transfer Package: ").append(transfer.getId()).append(" (<a href=\"Javascript:notes( 'transferpackage' );\">Notes</a>)</label></td>");
        } else {
            html.append("<td colspan=\"5\" valign=\"top\" class=\"gridmaint_fieldtitle\" id=\"td_").append(transferSelector).append("\"").append(" transferid=\"").append(transfer.getId()).append("\"").append(" type=\"").append(transfer.getType()).append("\"").append(">Export Script (<a href=\"Javascript:notes( 'exportscript' );\">Notes</a>)</td>");
        }
        html.append("</tr>");
    }

    private void getPropertyHtml(TransferPackage transferPackage, StringBuffer html, String selected) {
        html.append("<tr>");
        html.append("<td colspan=\"5\" valign=\"top\" class=\"gridmaint_fieldtitle\">");
        html.append("Properties (<a href=\"Javascript:notes( '" + (transferPackage.isExportScript() ? "exportscriptproperties" : "transferpackageproperties") + "' );\">Notes</a>)");
        html.append("</td></tr>");
        PropertyList propertyList = transferPackage.getPropertyList();
        if (propertyList.size() > 0) {
            for (String propertyid : propertyList.keySet()) {
                String propertySelector = "property_" + propertyid;
                html.append("<tr>");
                if (!transferPackage.isExportScript()) {
                    html.append("<td class=\"gridmaint_field\" width=\"15\"><input type=\"radio\" name=\"transfer_selector\" id=\"").append(propertySelector).append("\" ").append(selected.equals(propertySelector) ? "checked" : "").append("></td>");
                    html.append("<td colspan=\"4\" valign=\"top\" class=\"gridmaint_field\" id=\"td_").append(propertySelector).append("\"").append(" propertyid=\"").append(propertyid).append("\"").append(" propertyvalue=\"").append(propertyList.getProperty(propertyid)).append("\"").append("><label for=\"").append(propertySelector).append("\"><a href=\"\" onclick=\"Javascript:editItem( '").append(propertySelector).append("' );sapphire.events.cancelEvent(event, false);\">Property:</a> ").append(propertyid).append(" = ").append(propertyList.getProperty(propertyid)).append("</label></td>");
                } else {
                    html.append("<td class=\"gridmaint_field\" width=\"15\">&nbsp;</td>");
                    html.append("<td colspan=\"4\" valign=\"top\" class=\"gridmaint_field\" id=\"td_").append(propertySelector).append("\"").append(" propertyid=\"").append(propertyid).append("\"").append(" propertyvalue=\"").append(propertyList.getProperty(propertyid)).append("\"").append("><label for=\"").append(propertySelector).append("\">Property: ").append(propertyid).append("</label></td>");
                }
                html.append("</tr>");
            }
        } else {
            html.append("<tr>");
            html.append("<td colspan=\"5\" class=\"gridmaint_field\">No properties defined</td>");
            html.append("</tr>");
        }
    }

    private void getExportHtml(TransferPackage transferPackage, Object o, int i, int j, StringBuffer html, String selected, boolean usetempfile) {
        ExportXML export = (ExportXML)o;
        String transferitemSelector = "transferitem_" + i + "_" + j;
        html.append("<tr>");
        if (!transferPackage.isExportScript()) {
            html.append("<td class=\"gridmaint_field\" width=\"15\"><input type=\"radio\" name=\"transfer_selector\" id=\"").append(transferitemSelector).append("\" ").append(selected.equals(transferitemSelector) ? "checked" : "").append("></td>");
            html.append("<td colspan=\"4\" class=\"gridmaint_field\" id=\"td_").append(transferitemSelector).append("\"").append(" type=\"export\"").append(" exportdir=\"").append(export.getDirname() != null ? export.getDirname() : "").append("\"").append(" forceupdate=\"").append(export.getForceUpdate()).append("\"").append(" forcenullupdate=\"").append(export.getForceNullUpdate()).append("\"").append(!export.isValid() && !usetempfile ? " style=\"color:Red\"" : "").append("><label for=\"").append(transferitemSelector).append("\">&nbsp;<a href=\"\" onclick=\"Javascript:editItem( '").append(transferitemSelector).append("' );sapphire.events.cancelEvent(event, false);\">Export:</a> ").append(usetempfile ? "" : "directory = " + (export.getDirname() != null ? export.getDirname() : "")).append("</label></td>");
        } else {
            html.append("<td colspan=\"5\" class=\"gridmaint_field\" id=\"td_").append(transferitemSelector).append("\"").append(" type=\"export\"").append(" exportdir=\"").append(export.getDirname() != null ? export.getDirname() : "").append("\"").append(" forceupdate=\"").append(export.getForceUpdate()).append("\"").append(" forcenullupdate=\"").append(export.getForceNullUpdate()).append("\">").append("Export Steps (<a href=\"Javascript:notes( 'exportsteps' );\">Notes</a>)</td>");
        }
        html.append("</tr>");
        ArrayList exports = export.getExports();
        for (int k = 0; k < exports.size(); ++k) {
            Transferable transferable = (Transferable)exports.get(k);
            String transferableSelector = "transferable_" + i + "_" + j + "_" + k;
            if (transferable instanceof SDITransfer) {
                this.getSDIHtml(transferPackage, transferable, html, transferableSelector, selected, i, j, k);
                continue;
            }
            if (!(transferable instanceof TableTransfer)) continue;
            this.getTableHtml(transferPackage, transferable, html, transferableSelector, selected, i, j, k);
        }
    }

    private void getSDIHtml(TransferPackage transferPackage, Transferable transferable, StringBuffer html, String transferableSelector, String selected, int i, int j, int k) {
        SDITransfer sdi = (SDITransfer)transferable;
        html.append("<tr>");
        String colspan = "3";
        if (!transferPackage.isExportScript()) {
            html.append("<td class=\"gridmaint_field\">&nbsp;</td>");
        }
        html.append("<td class=\"gridmaint_field\" width=\"15\"><input type=\"radio\" name=\"transfer_selector\" id=\"").append(transferableSelector).append("\" ").append(selected.equals(transferableSelector) ? "checked" : "").append("></td>");
        html.append("<td colspan=\"").append(colspan).append("\" class=\"gridmaint_field\" id=\"td_").append(transferableSelector).append("\"").append(" type=\"sdi\"").append(" sdcid=\"").append(sdi.getSdcid()).append("\"").append(" exportfile=\"").append(sdi.getFile() != null ? sdi.getFile().toString() : "").append("\"").append(" exportby=\"").append(sdi.getTransferType()).append("\"").append(" keyid1=\"").append(sdi.getKeyid1() != null ? sdi.getKeyid1() : "").append("\"").append(" keyid2=\"").append(sdi.getKeyid2() != null ? sdi.getKeyid2() : "").append("\"").append(" keyid3=\"").append(sdi.getKeyid3() != null ? sdi.getKeyid3() : "").append("\"").append(" category=\"").append(sdi.getCategoryid() != null ? sdi.getCategoryid() : "").append("\"").append(" rsetid=\"").append(sdi.getRsetid() != null ? sdi.getRsetid() : "").append("\"").append(" exportid=\"").append(sdi.getExportid() != null ? sdi.getExportid() : "").append("\"").append(" altkeycols=\"").append(sdi.getAltkeycols() != null ? StringUtil.arrayToString(sdi.getAltkeycols().toArray(new String[sdi.getAltkeycols().size()]), ";") : "").append("\"").append(" keyfile=\"").append(sdi.getKeyFilename() != null ? sdi.getKeyFilename() : "").append("\"").append(" queryfrom=\"").append(sdi.getQueryfrom() != null ? sdi.getQueryfrom() : "").append("\"").append(" querywhere=\"").append(sdi.getQuerywhere() != null ? sdi.getQuerywhere() : "").append("\"").append(" queryorderby=\"").append(sdi.getQueryorderby() != null ? sdi.getQueryorderby() : "").append("\"").append(" exportdetail=\"").append(sdi.isExportDetail() ? "true" : "false").append("\"").append(" flushsdi=\"").append(sdi.isFlushSDI() ? "true" : "false").append("\"").append(" flushdetail=\"").append(sdi.isFlushDetail() ? "true" : "false").append("\"").append(" exportsdidetail=\"").append(sdi.isExportSDIDetail() ? "true" : "false").append("\"").append(" flushsdidetail=\"").append(sdi.isFlushSDIDetail() ? "true" : "false").append("\"").append(" flushchildsdi=\"").append(sdi.isFlushChildSDI() ? "true" : "false").append("\"").append(" exportfkdetail=\"").append(sdi.isExportFKDetail() ? "true" : "false").append("\"").append(" exportsecuritydetail=\"").append(sdi.isExportSecurityDetail() ? "true" : "false").append("\"").append(" exportroles=\"").append(sdi.getSDIDetail("sdirole") != null ? "true" : "false").append("\"").append(" flushroles=\"").append(sdi.isFlushRoles() ? "true" : "false").append("\"").append(" exportcategories=\"").append(sdi.getSDIDetail("categoryitem") != null ? "true" : "false").append("\"").append(" flushcategories=\"").append(sdi.isFlushCategories() ? "true" : "false").append("\"").append(" excludeauditcolumns=\"").append(sdi.isExcludeAuditColumns() ? "true" : "false").append("\"").append(" forceupdate=\"").append(sdi.isPrimaryForceUpdate() ? "true" : "false").append("\"").append(" forcenullupdate=\"").append(sdi.isPrimaryForceNullUpdate() ? "true" : "false").append("\"").append(" syncdatamodel=\"").append(sdi.isSyncDataModel() ? "true" : "false").append("\"").append(!sdi.isValid() ? " style=\"color:Red\"" : "").append("><label for=\"").append(transferableSelector).append("\"><a href=\"\" onclick=\"editItem( '").append(transferableSelector).append("' );sapphire.events.cancelEvent(event, false);\">Export SDI:</a>&nbsp;").append(sdi.getSdcid()).append(this.getSDIExportText(sdi)).append("</label></td>");
        html.append("</tr>");
        this.getColumnHtml(transferPackage, sdi, html, selected, i, j, k);
    }

    private void getTableHtml(TransferPackage transferPackage, Transferable transferable, StringBuffer html, String transferableSelector, String selected, int i, int j, int k) {
        TableTransfer table = (TableTransfer)transferable;
        html.append("<tr>");
        String colspan = "3";
        if (!transferPackage.isExportScript()) {
            html.append("<td class=\"gridmaint_field\">&nbsp;</td>");
        }
        html.append("<td class=\"gridmaint_field\" width=\"15\"><input type=\"radio\" name=\"transfer_selector\" id=\"").append(transferableSelector).append("\" ").append(selected.equals(transferableSelector) ? "checked" : "").append("></td>");
        html.append("<td colspan=\"").append(colspan).append("\" class=\"gridmaint_field\" id=\"td_").append(transferableSelector).append("\"").append(" type=\"table\"").append(" tableid=\"").append(table.getTableid()).append("\"").append(" exportfile=\"").append(table.getFile() != null ? table.getFile().toString() : "").append("\"").append(" queryfrom=\"").append(table.getFrom() != null ? table.getFrom() : "").append("\"").append(" querywhere=\"").append(table.getWhere() != null ? table.getWhere() : "").append("\"").append(" queryorderby=\"").append(table.getOrderby() != null ? table.getOrderby() : "").append("\"").append(" tablealias=\"").append(table.getTablealias() != null ? table.getTablealias() : "").append("\"").append(!table.isValid() ? " style=\"color:Red\"" : "").append("><label for=\"").append(transferableSelector).append("\"><a href=\"\" onclick=\"editItem( '").append(transferableSelector).append("' );sapphire.events.cancelEvent(event, false);\">Export Table:</a>&nbsp;").append(table.getTableid()).append(this.getTableExportText(table)).append("</label></td>");
        html.append("</tr>");
        this.getColumnHtml(transferPackage, table, html, selected, i, j, k);
    }

    private void getColumnHtml(TransferPackage transferPackage, AbstractTransferable transferable, StringBuffer html, String selected, int i, int j, int k) {
        List columns = transferable.getColumns();
        if (columns.size() > 0) {
            for (int l = 0; l < columns.size(); ++l) {
                Column column = (Column)columns.get(l);
                String columnSelector = "column_" + i + "_" + j + "_" + k + "_" + l;
                String colspan = "2";
                html.append("<tr>");
                if (!transferPackage.isExportScript()) {
                    html.append("<td class=\"gridmaint_field\">&nbsp;</td>");
                }
                html.append("<td class=\"gridmaint_field\">&nbsp;</td>");
                html.append("<td class=\"gridmaint_field\" width=\"15\"><input type=\"radio\" name=\"transfer_selector\" id=\"").append(columnSelector).append("\" ").append(selected.equals(columnSelector) ? "checked" : "").append("></td>");
                html.append("<td colspan=\"").append(colspan).append("\" class=\"gridmaint_field\" id=\"td_").append(columnSelector).append("\"").append(" type=\"column\"").append(" columnid=\"").append(column.getColumnid()).append("\"").append(" forceupdate=\"").append(column.isForceUpdate()).append("\"").append(" forcenullupdate=\"").append(column.isForceNullUpdate()).append("\"").append(" excluded=\"").append(column.isExcluded()).append("\"").append(" value=\"").append(column.getValue()).append("\"").append(" file=\"").append(column.getFile() != null ? column.getFile() : "").append("\"").append(" parenttype=\"").append(transferable instanceof SDITransfer ? "sdi" : "table").append("\"").append(transferable instanceof SDITransfer ? " sdcid=\"" + ((SDITransfer)transferable).getSdcid() + "\"" : " tableid=\"" + ((TableTransfer)transferable).getTableid() + "\"").append(!column.isValid() ? " style=\"color:Red\"" : "").append("><label for=\"").append(columnSelector).append("\"><a href=\"\" onclick=\"editItem( '").append(columnSelector).append("' );sapphire.events.cancelEvent(event, false);\">Column:</a>&nbsp;").append(column.getColumnid()).append(this.getColumnText(column)).append("</label></td>");
                html.append("</tr>");
            }
        }
    }

    private void getImportHtml(TransferPackage transferPackage, Object o, int i, int j, StringBuffer html, String selected) {
        if (!transferPackage.isExportScript()) {
            ImportXML importxml = (ImportXML)o;
            String transferitemSelector = "transferitem_" + i + "_" + j;
            html.append("<tr>");
            html.append("<td class=\"gridmaint_field\">&nbsp;</td>");
            html.append("<td class=\"gridmaint_field\" width=\"15\"><input type=\"radio\" name=\"transfer_selector\" id=\"").append(transferitemSelector).append("\" ").append(selected.equals(transferitemSelector) ? "checked" : "").append("></td>");
            html.append("<td colspan=\"3\" class=\"gridmaint_field\" id=\"td_").append(transferitemSelector).append("\"").append(" type=\"import\"").append(" importdir=\"").append(importxml.getDirname() != null ? importxml.getDirname() : "").append("\"").append(" forceupdate=\"").append(importxml.isImportForceUpdate() ? "true" : "false").append("\"").append(!importxml.isValid() ? " style=\"color:Red\"" : "").append("><label for=\"").append(transferitemSelector).append("\">&nbsp;<a href=\"\" onclick=\"editItem( '").append(transferitemSelector).append("' );sapphire.events.cancelEvent(event, false);\">Import</a>: directory = ").append(importxml.getDirname() != null ? importxml.getDirname() : "").append("</label></td>");
            html.append("</tr>");
            ArrayList imports = importxml.getImportFiles();
            for (int k = 0; k < imports.size(); ++k) {
                File file = (File)imports.get(k);
                String fileSelector = "file_" + i + "_" + j + "_" + k;
                html.append("<tr>");
                html.append("<td class=\"gridmaint_field\">&nbsp;</td>");
                html.append("<td class=\"gridmaint_field\">&nbsp;</td>");
                html.append("<td class=\"gridmaint_field\"><input type=\"radio\" name=\"transfer_selector\" id=\"").append(fileSelector).append("\" ").append(selected.equals(fileSelector) ? "checked" : "").append("></td>");
                html.append("<td colspan=\"2\" class=\"gridmaint_field\" id=\"td_").append(fileSelector).append("\"").append(" importfile=\"").append(file != null ? file.toString() : "").append("\"").append(file != null && file.length() > 0L ? " style=\"color:Red\"" : "").append("><label for=\"").append(fileSelector).append("\">&nbsp;&nbsp;<a href=\"\" onclick=\"editItem( '").append(fileSelector).append("' );sapphire.events.cancelEvent(event, false);\">Fileset</a>: ").append(file != null ? file.toString() : "").append("</label></td>");
                html.append("</tr>");
            }
        }
    }

    private String getSDIExportText(SDITransfer sdi) {
        StringBuffer out = new StringBuffer();
        String type = sdi.getTransferType();
        if (type.equals("rset")) {
            out.append("<br>- rset: ").append(sdi.getRsetid());
        } else if (type.equals("category")) {
            out.append("<br>- category: ").append(sdi.getCategoryid());
        } else if (type.equals("keyid")) {
            out.append("<br>- keyid1 list: ").append(sdi.getKeyid1());
            if (sdi.getKeyid2() != null && sdi.getKeyid2().length() > 0) {
                out.append("<br>- keyid2 list: ").append(sdi.getKeyid2());
            }
            if (sdi.getKeyid3() != null && sdi.getKeyid3().length() > 0) {
                out.append("<br>- keyid3 list: ").append(sdi.getKeyid3());
            }
        } else if (type.equals("query")) {
            out.append("<br>- queryfrom: ").append(sdi.getQueryfrom()).append("<br>- querywhere: ").append(sdi.getQuerywhere()).append("<br>- orderby: ").append(sdi.getQueryorderby());
        } else if (type.equals("keyfile")) {
            out.append("<br>- keyfile: ").append(sdi.getKeyFilename());
        } else {
            out.append("<br>- undefined selection options");
        }
        out.append(sdi.getColumns().size() == 0 ? "<br>- all columns" : "");
        String options = sdi.getOptionsText();
        out.append(options.length() > 0 ? "<br>- " + options : "");
        ArrayList<String> altkeycols = sdi.getAltkeycols();
        out.append(altkeycols.size() > 0 ? "<br>- altkeycols(" + StringUtil.arrayToString(altkeycols.toArray(new String[altkeycols.size()]), ",") + ")" : "");
        return out.toString();
    }

    private String getTableExportText(TableTransfer table) {
        StringBuffer out = new StringBuffer();
        out.append("<br>- queryfrom: ").append(table.getFrom() != null ? table.getFrom() : "").append("<br>- querywhere: ").append(table.getWhere() != null ? table.getWhere() : "").append("<br>- orderby: ").append(table.getOrderby() != null ? table.getOrderby() : "");
        return out.toString();
    }

    private String getColumnText(Column column) {
        StringBuffer out = new StringBuffer();
        if (!column.isExcluded()) {
            out.append(column.getColumnid().equals("*") ? " All columns" : "");
            StringBuffer options = new StringBuffer();
            options.append(column.isForceUpdate() ? ", forceupdate" : "");
            options.append(column.isForceNullUpdate() ? ", forcenullupdate" : "");
            options.append(column.getValue() != null && column.getValue().length() > 0 ? ", value=" + column.getValue() : "");
            options.append(column.getFile() != null && column.getFile().length() > 0 ? ", file=" + column.getFile() : "");
            out.append(options.length() > 0 ? " - options (" + options.substring(2) + ")" : "");
        } else {
            out.append(" (excluded)");
        }
        return out.toString();
    }
}

