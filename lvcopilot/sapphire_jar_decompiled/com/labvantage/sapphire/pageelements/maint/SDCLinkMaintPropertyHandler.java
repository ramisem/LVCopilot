/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.maint;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.pageelements.PropertyHandler;
import com.labvantage.sapphire.services.ActionService;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.util.MiscUtil;
import com.labvantage.sapphire.util.evaluator.ExpressionUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.error.ErrorDetail;
import sapphire.error.ErrorHandler;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SDCLinkMaintPropertyHandler
extends PropertyHandler {
    public static final String PREFIX = "___";
    public static final String TYPE_POSTFIX = "_type";
    public static final String DATASET_POSTFIX = "_dataset";
    public static final String LINKTYPE_POSTFIX = "_linktype";
    public static final String LINKID_POSTFIX = "_linkid";
    public static final String LINKELEMENTID_POSTFIX = "_linkelement_";
    public static final String FROMSDC_POSTFIX = "_fromsdc";
    public static final String TOSDC_POSTFIX = "_tosdc";
    public static final String TOCOL_POSTFIX = "_tocol";
    public static final String LINKKEYID_POSTFIX = "_linkkeyid";
    public static final String ADDFROMLOOKUP_POSTFIX = "_addfromlookup";
    private TranslationProcessor translationProcessor;

    @Override
    public void processProperties(HashMap props) throws SapphireException {
        String elementid;
        this.translationProcessor = new TranslationProcessor(this.getConnectionInfo().getConnectionId());
        ArrayList<String> elementlist = new ArrayList<String>();
        if (props.containsKey("__elementlist")) {
            String[] elements = StringUtil.split(props.get("__elementlist").toString(), ";");
            for (int i = 0; i < elements.length; ++i) {
                elementid = elements[i];
                if (!props.containsKey("__propertyhandler_" + elementid) || !props.get("__propertyhandler_" + elementid).toString().equalsIgnoreCase(this.getClass().getName())) continue;
                elementlist.add(elementid);
                props.put("__propertyhandler_" + elementid, "");
            }
        }
        Iterator it = props.keySet().iterator();
        while (it.hasNext()) {
            String elementid2;
            String value;
            String key = it.next().toString();
            if (!key.toLowerCase().startsWith("__propertyhandler_") || !(value = props.get(key).toString()).equalsIgnoreCase(this.getClass().getName()) || elementlist.contains(elementid2 = key.substring(18))) continue;
            elementlist.add(elementid2);
            props.put(key, "");
        }
        for (int elcount = 0; elcount < elementlist.size(); ++elcount) {
            elementid = (String)elementlist.get(elcount);
            this.logDebug("elementid = " + elementid);
            if (elementid.length() > 0) {
                String dataset = props.get("__" + elementid + DATASET_POSTFIX).toString();
                if (dataset.length() > 0) {
                    try {
                        int linktype = Integer.parseInt(props.get(PREFIX + elementid + LINKTYPE_POSTFIX).toString());
                        String fromSDC = props.get(PREFIX + elementid + FROMSDC_POSTFIX).toString();
                        String toSDC = props.get(PREFIX + elementid + TOSDC_POSTFIX).toString();
                        if (fromSDC.length() > 0 && toSDC.length() > 0) {
                            this.logDebug("fromSDC = " + fromSDC);
                            this.logDebug("toSDC = " + toSDC);
                            String toCol = props.get(PREFIX + elementid + TOCOL_POSTFIX).toString();
                            this.logDebug("toCol = " + toCol);
                            String linkVal = props.get(PREFIX + elementid + LINKKEYID_POSTFIX).toString();
                            this.logDebug("linkVal = " + linkVal);
                            String[] linkcols = StringUtil.split(toCol, ";");
                            String[] linkvals = StringUtil.split(linkVal, ";");
                            switch (linktype) {
                                case 1: {
                                    ErrorHandler eh;
                                    String linkId = props.get(PREFIX + elementid + LINKID_POSTFIX).toString();
                                    this.logDebug("linkId = " + linkId);
                                    if (this.saveData(props, elementid, dataset, toSDC, linkcols, linkvals)) {
                                        this.logDebug("Data saved(1).");
                                        break;
                                    }
                                    String sMsg = "Could not save linked FK data.";
                                    String errorcode = "GENERAL_ERROR";
                                    String errortype = "FAILURE";
                                    if (props.containsKey("ERRORHANDLER") && (eh = (ErrorHandler)props.get("ERRORHANDLER")).size() > 0 && eh.get(eh.size() - 1) instanceof ErrorDetail) {
                                        ErrorDetail ed = (ErrorDetail)eh.get(eh.size() - 1);
                                        sMsg = sMsg + " " + ed.getMessage();
                                        errortype = ed.getErrorType();
                                        errorcode = ed.getErrorid();
                                    }
                                    throw new SapphireException(this.translationProcessor.translate(errorcode), errortype, this.translationProcessor.translate(sMsg));
                                }
                                case 2: {
                                    ErrorHandler eh;
                                    if (this.saveData(props, elementid, dataset, toSDC, linkcols, linkvals)) {
                                        this.logDebug("Data saved(2).");
                                        break;
                                    }
                                    String sMsg = "Could not save data.";
                                    if (props.containsKey("ERRORHANDLER") && (eh = (ErrorHandler)props.get("ERRORHANDLER")).size() > 0 && eh.get(eh.size() - 1) instanceof ErrorDetail) {
                                        ErrorDetail ed = (ErrorDetail)eh.get(eh.size() - 1);
                                        sMsg = sMsg + " " + ed.getMessage();
                                    }
                                    throw new SapphireException(sMsg);
                                }
                                default: {
                                    throw new SapphireException("Invalid linktype provided(1).");
                                }
                            }
                            continue;
                        }
                        throw new SapphireException("From SDC or to SDC not provided.");
                    }
                    catch (NumberFormatException e) {
                        throw new SapphireException("Invalid linktype provided(2).");
                    }
                }
                throw new SapphireException("Could not find dataset name.");
            }
            throw new SapphireException("Could not find element id.");
        }
    }

    private void setUpLinkKeyProps(String[] linkcols, String[] linkvals, HashMap props) {
        for (int i = 0; i < linkcols.length; ++i) {
            String totallinkvalue;
            if (props.containsKey(linkcols[i])) {
                totallinkvalue = props.get(linkcols[i]).toString() + ";" + linkvals[i];
                props.remove(linkcols[i]);
            } else {
                totallinkvalue = linkvals[i];
            }
            props.put(linkcols[i], totallinkvalue);
        }
    }

    private void setUpEdit(HashMap props, String keyidvalue, String[] linkcols, String[] linkvals, String datasetName, int row, HashMap editSDIProps, String[] columns, PropertyListCollection sdccolumnscollection) {
        if (linkcols.length == linkvals.length) {
            if (keyidvalue.length() > 0 && !keyidvalue.contains("(null)")) {
                String totalkeyvalue1;
                String[] keys = StringUtil.split(keyidvalue, ";");
                if (editSDIProps.containsKey("keyid1")) {
                    totalkeyvalue1 = editSDIProps.get("keyid1").toString() + ";" + keys[0];
                    editSDIProps.remove("keyid1");
                } else {
                    totalkeyvalue1 = keys[0];
                }
                editSDIProps.put("keyid1", totalkeyvalue1);
                if (keys.length > 1) {
                    String totalkeyvalue2;
                    if (editSDIProps.containsKey("keyid2")) {
                        totalkeyvalue2 = editSDIProps.get("keyid2").toString() + ";" + keys[1];
                        editSDIProps.remove("keyid2");
                    } else {
                        totalkeyvalue2 = keys[1];
                    }
                    editSDIProps.put("keyid2", totalkeyvalue2);
                    if (keys.length > 2) {
                        String totalkeyvalue3;
                        if (editSDIProps.containsKey("keyid3")) {
                            totalkeyvalue3 = editSDIProps.get("keyid3").toString() + ";" + keys[2];
                            editSDIProps.remove("keyid3");
                        } else {
                            totalkeyvalue3 = keys[2];
                        }
                        editSDIProps.put("keyid3", totalkeyvalue3);
                    }
                }
                this.setUpLinkKeyProps(linkcols, linkvals, editSDIProps);
                if (linkvals.length > 0 && linkvals[0].length() > 0) {
                    String cv;
                    for (int col = 0; col < columns.length; ++col) {
                        String totalcolumnvalue;
                        String columnvalue;
                        Object columnob;
                        if (sdccolumnscollection.find("columnid", columns[col]) == null || (columnob = props.get(datasetName + row + "_" + columns[col])) == null || columns[col].equalsIgnoreCase("usersequence") || (columnvalue = props.get(datasetName + row + "_" + columns[col]).toString()).equals(keyidvalue)) continue;
                        if (columnvalue.length() == 0) {
                            columnvalue = "(null)";
                        } else if (columnvalue.startsWith("[") && columnvalue.endsWith("]")) {
                            try {
                                String t = ExpressionUtil.evaluate(columnvalue, props);
                                if (t.length() > 0) {
                                    columnvalue = t;
                                }
                            }
                            catch (Exception t) {
                                // empty catch block
                            }
                        }
                        columnvalue = StringUtil.replaceAll(columnvalue, ";", "#semicolon#");
                        if (editSDIProps.containsKey(columns[col]) && MiscUtil.MiscArray.stringInArray(linkcols, columns[col], true, true) == 0) {
                            totalcolumnvalue = editSDIProps.get(columns[col]).toString() + ";" + columnvalue;
                            editSDIProps.remove(columns[col]);
                        } else {
                            totalcolumnvalue = columnvalue;
                        }
                        editSDIProps.put(columns[col], totalcolumnvalue);
                    }
                    if (sdccolumnscollection.find("columnid", "usersequence") != null && (cv = props.get("__" + datasetName + row + "_sequence").toString()) != null) {
                        String totalcolumnvalue;
                        String columnvalue = cv.toString();
                        if (editSDIProps.containsKey("usersequence")) {
                            totalcolumnvalue = editSDIProps.get("usersequence").toString() + ";" + columnvalue;
                            editSDIProps.remove("usersequence");
                        } else {
                            totalcolumnvalue = columnvalue;
                        }
                        editSDIProps.put("usersequence", totalcolumnvalue);
                    }
                }
            }
        } else {
            this.logError("Incorrect link columns and values provided.");
        }
    }

    private boolean saveData(HashMap props, String elementId, String datasetName, String sdcId, String[] linkcols, String[] linkvals) {
        int rows;
        boolean out = false;
        PropertyList addSDIProps = new PropertyList();
        PropertyList editSDIProps = new PropertyList();
        PropertyList deleteSDIProps = new PropertyList();
        PropertyList unlinkSDIProps = new PropertyList();
        int copies = 0;
        try {
            rows = Integer.parseInt(props.get("__" + datasetName + "_rows").toString());
        }
        catch (Exception e) {
            this.logWarn("Could not find row count.");
            rows = 0;
        }
        this.logDebug("rows = " + rows);
        String[] columns = props.get("__" + datasetName + "_cols").toString().split(";");
        if (columns.length > 0) {
            SDCProcessor sdcproc = new SDCProcessor(this.getConnectionInfo().getConnectionId());
            PropertyListCollection sdccolumnscollection = sdcproc.getColumns(sdcId);
            Iterator it = props.keySet().iterator();
            String lowds = datasetName.toLowerCase();
            while (it.hasNext()) {
                String rs;
                String key = it.next().toString();
                String lowkey = key.toLowerCase();
                if (!lowkey.endsWith("_rs") || !lowkey.startsWith("__" + lowds) || (rs = props.get(key).toString()).length() <= 0 || rs.equalsIgnoreCase("s")) continue;
                try {
                    String totalkeyvalue3;
                    String totalkeyvalue2;
                    String totalkeyvalue1;
                    String[] keyidvalue;
                    int row = Integer.parseInt(key.substring(datasetName.length() + 2, key.lastIndexOf("_rs")));
                    if (rs.equalsIgnoreCase("u")) {
                        keyidvalue = props.get("__" + datasetName + row + "_key").toString();
                        this.setUpEdit(props, (String)keyidvalue, linkcols, linkvals, datasetName, row, editSDIProps, columns, sdccolumnscollection);
                        continue;
                    }
                    if (rs.equalsIgnoreCase("i")) {
                        boolean newsdi;
                        boolean addFromLookup = props.get(PREFIX + elementId + ADDFROMLOOKUP_POSTFIX).toString().equalsIgnoreCase("Y");
                        String linkedkeyid1 = sdcproc.getProperty(sdcId, "keycolid1", "");
                        String linkedkeyid1value = "";
                        if (props.containsKey(datasetName + row + "_" + linkedkeyid1)) {
                            linkedkeyid1value = props.get(datasetName + row + "_" + linkedkeyid1).toString();
                        }
                        String linkedkeyid2 = sdcproc.getProperty(sdcId, "keycolid2", "");
                        String linkedkeyid2value = "";
                        if (linkedkeyid2.length() > 0 && props.containsKey(datasetName + row + "_" + linkedkeyid2)) {
                            linkedkeyid2value = props.get(datasetName + row + "_" + linkedkeyid2).toString();
                        }
                        String linkedkeyid3 = sdcproc.getProperty(sdcId, "keycolid3", "");
                        String linkedkeyid3value = "";
                        if (linkedkeyid3.length() > 0 && props.containsKey(datasetName + row + "_" + linkedkeyid3)) {
                            linkedkeyid3value = props.get(datasetName + row + "_" + linkedkeyid3).toString();
                        }
                        boolean bl = newsdi = linkedkeyid1value.length() == 0 || linkedkeyid1value.equalsIgnoreCase("(auto)") || !addFromLookup;
                        if (!newsdi) {
                            this.logDebug("About to check for existing...");
                            Iterator it2 = props.keySet().iterator();
                            while (it2.hasNext()) {
                                String linkedkeyid1value2;
                                String key2 = it2.next().toString();
                                if (key2.equals("__" + datasetName + row + "_key") || !key2.startsWith("__" + datasetName) || !key2.endsWith("_key") || !(linkedkeyid1value2 = props.get(key2).toString()).equals(linkedkeyid1value)) continue;
                                int row2 = Integer.parseInt(key2.substring(datasetName.length() + 2, key2.indexOf("_key")));
                                String rs2 = props.get("__" + datasetName + row2 + "_rs").toString();
                                if (!rs2.equalsIgnoreCase("d")) continue;
                                newsdi = true;
                                this.logDebug("Found delete for same key " + linkedkeyid1);
                            }
                            if (!newsdi) {
                                SafeSQL safeSQL = new SafeSQL();
                                StringBuffer sql = new StringBuffer("SELECT COUNT(").append(linkedkeyid1).append(") FROM ").append(sdcproc.getProperty(sdcId, "tableid")).append(" WHERE ").append(linkedkeyid1).append(" = ").append(safeSQL.addVar(linkedkeyid1value));
                                QueryProcessor qp = new QueryProcessor(this.getConnectionInfo().getConnectionId());
                                if (qp != null) {
                                    try {
                                        if (qp.getPreparedCount(sql.toString(), safeSQL.getValues()) == 0) {
                                            newsdi = true;
                                        }
                                    }
                                    catch (SapphireException e2) {
                                        this.logWarn("Could not execute add sdi precheck.");
                                    }
                                } else {
                                    this.logWarn("Query processor could not be created for add sdi precheck.");
                                }
                            }
                        }
                        if (newsdi) {
                            String cv;
                            ++copies;
                            this.setUpLinkKeyProps(linkcols, linkvals, addSDIProps);
                            String __rowmap = addSDIProps.getProperty("__rowmap");
                            if (__rowmap.length() > 0) {
                                __rowmap = __rowmap + ";";
                            }
                            __rowmap = __rowmap + datasetName + row + "_";
                            addSDIProps.setProperty("__rowmap", __rowmap);
                            for (int col = 0; col < columns.length; ++col) {
                                String totalcolumnvalue;
                                Object columnob;
                                if (sdccolumnscollection.find("columnid", columns[col]) == null || (columnob = props.get(datasetName + row + "_" + columns[col])) == null || MiscUtil.MiscArray.stringInArray(linkcols, columns[col], true, true) != 0 || columns[col].equalsIgnoreCase("usersequence")) continue;
                                String columnvalue = props.get(datasetName + row + "_" + columns[col]).toString();
                                if (columnvalue.length() == 0) {
                                    columnvalue = "(null)";
                                } else if (columnvalue.startsWith("[") && columnvalue.endsWith("]")) {
                                    try {
                                        String t = ExpressionUtil.evaluate(columnvalue, props);
                                        if (t.length() > 0) {
                                            columnvalue = t;
                                        }
                                    }
                                    catch (Exception t) {
                                        // empty catch block
                                    }
                                }
                                columnvalue = StringUtil.replaceAll(columnvalue, ";", "#semicolon#");
                                if (addSDIProps.containsKey(columns[col])) {
                                    totalcolumnvalue = addSDIProps.get(columns[col]).toString() + ";" + columnvalue;
                                    addSDIProps.remove(columns[col]);
                                } else {
                                    totalcolumnvalue = columnvalue;
                                }
                                addSDIProps.put(columns[col], totalcolumnvalue);
                            }
                            String templatePropsAsColumn = (String)props.get(datasetName + row + "_templatepropsascolumn");
                            if (templatePropsAsColumn != null) {
                                addSDIProps.setProperty("templatepropsascolumn", templatePropsAsColumn);
                            }
                            if (sdccolumnscollection.find("columnid", "usersequence") == null || (cv = props.get("__" + datasetName + row + "_sequence").toString()) == null) continue;
                            String columnvalue = cv.toString();
                            String tempUsrSeq = addSDIProps.getProperty("usersequence", "");
                            if (tempUsrSeq.length() > 0) {
                                addSDIProps.put("usersequence", tempUsrSeq + ";" + columnvalue);
                                continue;
                            }
                            addSDIProps.put("usersequence", columnvalue);
                            continue;
                        }
                        this.setUpEdit(props, linkedkeyid1value + (linkedkeyid2.length() > 0 ? ";" + linkedkeyid2value + (linkedkeyid3.length() > 0 ? ";" + linkedkeyid3value : "") : ""), linkcols, linkvals, datasetName, row, editSDIProps, columns, sdccolumnscollection);
                        continue;
                    }
                    if (rs.equalsIgnoreCase("r")) {
                        keyidvalue = props.get("__" + datasetName + row + "_key").toString();
                        String[] temp = new String[linkcols.length];
                        for (int i = 0; i < linkcols.length; ++i) {
                            temp[i] = "";
                        }
                        this.setUpEdit(props, (String)keyidvalue, linkcols, temp, datasetName, row, unlinkSDIProps, columns, sdccolumnscollection);
                        continue;
                    }
                    if (!rs.equalsIgnoreCase("d") || (keyidvalue = StringUtil.split(props.get("__" + datasetName + row + "_key").toString(), ";")).length <= 0 || keyidvalue[0].equals("(null)")) continue;
                    if (deleteSDIProps.containsKey("keyid1")) {
                        totalkeyvalue1 = deleteSDIProps.get("keyid1").toString() + ";" + keyidvalue[0];
                        deleteSDIProps.remove("keyid1");
                    } else {
                        totalkeyvalue1 = keyidvalue[0];
                    }
                    deleteSDIProps.put("keyid1", totalkeyvalue1);
                    if (keyidvalue.length <= 1 || keyidvalue[1].equals("(null)")) continue;
                    if (deleteSDIProps.containsKey("keyid2")) {
                        totalkeyvalue2 = deleteSDIProps.get("keyid2").toString() + ";" + keyidvalue[1];
                        deleteSDIProps.remove("keyid2");
                    } else {
                        totalkeyvalue2 = keyidvalue[1];
                    }
                    deleteSDIProps.put("keyid2", totalkeyvalue2);
                    if (keyidvalue.length <= 2 || keyidvalue[2].equals("(null)")) continue;
                    if (deleteSDIProps.containsKey("keyid3")) {
                        totalkeyvalue3 = deleteSDIProps.get("keyid3").toString() + ";" + keyidvalue[2];
                        deleteSDIProps.remove("keyid3");
                    } else {
                        totalkeyvalue3 = keyidvalue[2];
                    }
                    deleteSDIProps.put("keyid3", totalkeyvalue3);
                }
                catch (NumberFormatException e) {
                    this.logWarn("Invalid row encoutered");
                }
            }
            if (editSDIProps.size() > 0 || addSDIProps.size() > 0 || deleteSDIProps.size() > 0 || unlinkSDIProps.size() > 0) {
                ErrorHandler eh = new ErrorHandler();
                try {
                    String traceLogId = (String)props.get("tracelogid");
                    ActionService as = new ActionService(this.sapphireConnection);
                    if (editSDIProps.size() > 0) {
                        editSDIProps.put("sdcid", sdcId);
                        if (traceLogId != null && traceLogId.length() > 0) {
                            editSDIProps.put("tracelogid", traceLogId);
                        }
                        try {
                            as.processAction("EditSDI", "1", editSDIProps, eh);
                        }
                        catch (ServiceException e) {
                            if (eh.size() > 0 && ErrorHandler.isErrorHandlerFormat(e.getMessage())) {
                                throw e;
                            }
                            throw new SapphireException(e.getErrorid(), ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
                        }
                    }
                    if (unlinkSDIProps.size() > 0) {
                        unlinkSDIProps.put("sdcid", sdcId);
                        if (traceLogId != null && traceLogId.length() > 0) {
                            unlinkSDIProps.put("tracelogid", traceLogId);
                        }
                        try {
                            as.processAction("EditSDI", "1", unlinkSDIProps, eh);
                        }
                        catch (ServiceException e) {
                            if (eh.size() > 0 && ErrorHandler.isErrorHandlerFormat(e.getMessage())) {
                                throw e;
                            }
                            throw new SapphireException(e.getErrorid(), ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
                        }
                    }
                    if (deleteSDIProps.size() > 0) {
                        deleteSDIProps.put("sdcid", sdcId);
                        if (traceLogId != null && traceLogId.length() > 0) {
                            deleteSDIProps.put("tracelogid", traceLogId);
                        }
                        try {
                            as.processAction("DeleteSDI", "1", deleteSDIProps, eh);
                            out = true;
                        }
                        catch (ServiceException e) {
                            if (eh.size() > 0 && ErrorHandler.isErrorHandlerFormat(e.getMessage())) {
                                throw e;
                            }
                            throw new SapphireException(e.getErrorid(), ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
                        }
                    }
                    if (addSDIProps.size() > 0) {
                        this.sortProps(addSDIProps);
                        addSDIProps.put("sdcid", sdcId);
                        addSDIProps.put("copies", "" + copies);
                        if (traceLogId != null && traceLogId.length() > 0) {
                            addSDIProps.put("tracelogid", traceLogId);
                        }
                        try {
                            String k3;
                            String k2;
                            as.processAction("AddSDI", "1", addSDIProps, eh);
                            String[] rowmap = StringUtil.split(addSDIProps.getProperty("__rowmap"), ";");
                            props.put("gary123", "hello");
                            String k1 = sdcproc.getProperty(sdcId, "keycolid1");
                            if (k1.length() > 0) {
                                String[] key1 = StringUtil.split(addSDIProps.getProperty("newkeyid1"), ";");
                                block23: for (int k = 0; k < key1.length; ++k) {
                                    Iterator kit = props.keySet().iterator();
                                    while (kit.hasNext()) {
                                        String kkey = kit.next().toString();
                                        if (!kkey.equalsIgnoreCase(rowmap[k] + k1)) continue;
                                        props.put(kkey, key1[k]);
                                        continue block23;
                                    }
                                }
                            }
                            if ((k2 = sdcproc.getProperty(sdcId, "keycolid2")).length() > 0) {
                                String[] key2 = StringUtil.split(addSDIProps.getProperty("newkeyid2"), ";");
                                block25: for (int k = 0; k < key2.length; ++k) {
                                    Iterator kit = props.keySet().iterator();
                                    while (kit.hasNext()) {
                                        String kkey = kit.next().toString();
                                        if (!kkey.equalsIgnoreCase(rowmap[k] + k2)) continue;
                                        props.put(kkey, key2[k]);
                                        continue block25;
                                    }
                                }
                            }
                            if ((k3 = sdcproc.getProperty(sdcId, "keycolid3")).length() > 0) {
                                String[] key3 = StringUtil.split(addSDIProps.getProperty("newkeyid3"), ";");
                                block27: for (int k = 0; k < key3.length; ++k) {
                                    Iterator kit = props.keySet().iterator();
                                    while (kit.hasNext()) {
                                        String kkey = kit.next().toString();
                                        if (!kkey.equalsIgnoreCase(rowmap[k] + k3)) continue;
                                        props.put(kkey, key3[k]);
                                        continue block27;
                                    }
                                }
                            }
                        }
                        catch (ServiceException e) {
                            if (eh.size() > 0 && ErrorHandler.isErrorHandlerFormat(e.getMessage())) {
                                throw e;
                            }
                            throw new SapphireException(e.getErrorid(), ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
                        }
                    }
                    out = true;
                }
                catch (SapphireException e) {
                    this.logError("Error raised processing SDITag properties. Reason: " + e.getMessage(), e);
                    eh = new ErrorHandler(e.getErrorid(), e.getErrorType(), e.getMessage());
                    props.put("ERRORHANDLER", eh);
                    out = false;
                }
                catch (ServiceException e) {
                    this.logError("Error raised processing SDITag properties. Reason: " + e.getMessage(), e);
                    props.put("ERRORHANDLER", eh);
                    out = false;
                }
            } else {
                out = true;
            }
        } else {
            this.logInfo("No columns to save.");
            out = true;
        }
        return out;
    }

    private void sortProps(PropertyList props) {
        if (props.containsKey("usersequence") && props.getProperty("usersequence", "").contains(";")) {
            DataSet sortds = new DataSet();
            for (Object key : props.keySet()) {
                if (!(props.get(key) instanceof String) || !props.getProperty(key.toString(), "").contains(";")) continue;
                sortds.addColumnValues(key.toString(), key.toString().equalsIgnoreCase("usersequence") ? 1 : 0, props.getProperty(key.toString(), ""), ";", "");
            }
            sortds.sort("usersequence");
            for (String columnid : sortds.getColumns()) {
                props.setProperty(columnid, sortds.getColumnValues(columnid, ";"));
            }
        }
    }
}

