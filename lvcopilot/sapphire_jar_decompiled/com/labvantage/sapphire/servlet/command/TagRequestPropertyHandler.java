/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.servlet.command;

import com.labvantage.opal.elements.BasePropertyHandler;
import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.RequestParser;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.cmt.CheckInSDI;
import com.labvantage.sapphire.actions.cmt.RepositoryOperations;
import com.labvantage.sapphire.actions.ddt.DDTPropertyHandler;
import com.labvantage.sapphire.pageelements.PropertyHandler;
import com.labvantage.sapphire.pageelements.maint.Maint;
import com.labvantage.sapphire.services.ActionService;
import com.labvantage.sapphire.services.AuditService;
import com.labvantage.sapphire.services.QueryService;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.servlet.RequestProcessor;
import com.labvantage.sapphire.util.policy.CMTPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.error.ErrorHandler;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class TagRequestPropertyHandler
extends BasePropertyHandler {
    public static final String KEYLIST = "__keylist";
    public static final String DEFAULT_SEPARATOR = ";";
    public static final String PROPERTY_BLOCKEDCOLUMNS = "blockedcolumns";
    public static final String PROPERTY_BLOCKCOLUMNUPDATES_MODE = "blockcolumnupdatesmode";
    public static final String PROPERTY_BLOCKCOLUMNUPDATES_MODE_YES = "Y";
    public static final String PROPERTY_BLOCKCOLUMNUPDATES_MODE_YESERROR = "E";
    public static final String PROPERTY_BLOCKCOLUMNUPDATES_MODE_NO = "N";
    public static final String PROPERTY_BLOCKCOLUMNUPDATES_MODE_INHERIT = "I";

    private void processCustomPropertyHandlers(HashMap sdiprops, HashMap handlers, ErrorHandler errorHandler) throws SapphireException {
        for (String elementid : handlers.keySet()) {
            String currentUserid;
            String handlerClass = (String)handlers.get(elementid);
            sdiprops.put("__propertyhandler_elementid", elementid);
            Trace.log("Instanciating property handler " + handlerClass);
            if (("com.labvantage.opal.elements.sdidetailmaint.handler.SDIAddressPropertyHandler".equals(handlerClass) || "com.labvantage.sapphire.admin.system.SysToolsPropertyHandler".equals(handlerClass)) && TagRequestPropertyHandler.isUserPreferencePageSave(sdiprops) && !(currentUserid = this.connectionInfo.getSysuserId()).equals(sdiprops.get("pr0_sysuserid"))) {
                throw new SapphireException("Not authorized!");
            }
            try {
                Class<?> c = Class.forName(handlerClass);
                PropertyHandler handler = (PropertyHandler)c.newInstance();
                handler.setSapphireConnection(this.sapphireConnection);
                handler.setErrorHandler(errorHandler);
                handler.processProperties(sdiprops);
            }
            catch (SapphireException e) {
                throw e;
            }
            catch (Exception e) {
                throw new SapphireException("GENERAL_ERROR", "Failed to process property handler " + handlerClass + ". Exception: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.sapphireConnection.getConnectionId())), e);
            }
        }
        sdiprops.remove("__propertyhandler_elementid");
    }

    private void resolveKeyid(HashMap sdiprops, PropertyList keylist) {
        for (Object key : sdiprops.keySet()) {
            Object val = sdiprops.get(key);
            if (val == null || !(val instanceof String)) continue;
            String value = (String)val;
            String newVal = StringUtil.replaceAll(StringUtil.replaceAll(StringUtil.replaceAll(value, "(auto_keyid1_0)", keylist.getProperty("newkeyid1")), "(auto_keyid2_0)", keylist.getProperty("newkeyid2")), "(auto_keyid3_0)", keylist.getProperty("newkeyid3"));
            sdiprops.put(key, newVal);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processProperties(HashMap sdiprops) throws SapphireException {
        block149: {
            this.logName = "SDITagRequestHandler";
            ErrorHandler errorHandler = new ErrorHandler();
            sdiprops.put("ERRORHANDLER", errorHandler);
            String traceLogIdStr = "";
            AuditService audit = new AuditService(this.sapphireConnection);
            try {
                String cmtcheckinprops;
                SDIData sdidef;
                if (sdiprops.containsKey("__htmleditors") && sdiprops.get("__htmleditors").toString().length() > 0) {
                    String[] htmleditors = StringUtil.split((String)sdiprops.get("__htmleditors"), DEFAULT_SEPARATOR);
                    for (int i = 0; i < htmleditors.length; ++i) {
                        String editor = htmleditors[i];
                        if (!sdiprops.containsKey(editor)) continue;
                        String content = sdiprops.get(editor).toString();
                        sdiprops.put(editor, HttpUtil.decodeURIComponent(content));
                    }
                }
                PropertyList keylist = new PropertyList();
                PropertyList props = new PropertyList();
                PropertyList editprops = new PropertyList();
                PropertyList delprops = new PropertyList();
                PropertyList addprops = new PropertyList();
                PropertyList deprops = null;
                HashMap<String, DataSet> fkeditdata = new HashMap<String, DataSet>();
                HashSet<String> keyset = new HashSet<String>();
                String prefix = (String)sdiprops.get("__prefix");
                String sdcid = (String)sdiprops.get("__" + prefix + "sdcid");
                SDCProcessor sdcProcessor = new SDCProcessor(this.sapphireConnection.getConnectionId());
                PropertyList sdcProps = sdcProcessor.getProperties(sdcid);
                if (sdcProps == null) {
                    throw new SapphireException("INVALID_PROPERTY", "Unrecognized SDC: " + sdcid);
                }
                DataSet linksdata = null;
                ActionService actionService = new ActionService(this.sapphireConnection);
                try {
                    QueryService queryService = new QueryService(this.sapphireConnection);
                    SDIRequest sdiRequest = new SDIRequest();
                    sdiRequest.setSDCid(sdcid);
                    sdiRequest.setRetrieve(false);
                    sdidef = queryService.getSDIData(sdiRequest);
                }
                catch (ServiceException e) {
                    throw new SapphireException("QUERY_SERVICE_FAILED", "Failed to get sdi definition", e);
                }
                this.logInfo("Reconciling data for sdc: " + sdcid);
                String[] request = RequestParser.parseRequestItem(EncryptDecrypt.unobfsql((String)sdiprops.get("__" + prefix + "request")));
                String[] datasetcodes = new String[request.length];
                Set keySet = sdiprops.keySet();
                HashMap handlers_pre = new HashMap();
                HashMap<String, String> handlers_post = new HashMap<String, String>();
                if (sdcid.equals("SDC")) {
                    handlers_post.put("sdcexport", DDTPropertyHandler.class.getName());
                }
                for (Object aKeySet : keySet) {
                    String propertyid = (String)aKeySet;
                    if (propertyid.startsWith("__propertyhandler_pre_")) {
                        handlers_pre.put(propertyid.substring("__propertyhandler_pre_".length()), sdiprops.get(propertyid));
                        continue;
                    }
                    if (propertyid.startsWith("__postpropertyhandler_post_")) {
                        handlers_post.put(propertyid.substring("__postpropertyhandler_post_".length()), (String)sdiprops.get(propertyid));
                        continue;
                    }
                    if (!propertyid.startsWith("__propertyhandler_")) continue;
                    handlers_post.put(propertyid.substring("__propertyhandler_".length()), (String)sdiprops.get(propertyid));
                }
                this.processCustomPropertyHandlers(sdiprops, handlers_pre, errorHandler);
                for (int i = 0; i < request.length; ++i) {
                    int datasetrows;
                    String[] customcolumns;
                    String custcols;
                    String datasetname;
                    if (request[i].equals("notes")) continue;
                    int plusindex = request[i].indexOf(43);
                    String string = datasetname = plusindex == -1 ? request[i].trim() : request[i].trim().substring(0, plusindex);
                    if (request[i].indexOf("[") >= 0) {
                        datasetname = datasetname.substring(0, request[i].indexOf("[")).trim();
                    }
                    datasetcodes[i] = SDIData.getDatasetCode(datasetname);
                    String[] keycols = sdidef.getKeys(datasetname);
                    if (keycols == null) {
                        throw new SapphireException("INVALID_PROPERTY", "Failed to find keys for dataset '" + datasetname + "'. You might need to exclude this dataset from the request.");
                    }
                    keyset.clear();
                    for (int col = 0; col < keycols.length; ++col) {
                        keyset.add(keycols[col]);
                    }
                    ArrayList<String> columns = new ArrayList<String>();
                    columns.addAll(Arrays.asList(StringUtil.split((String)sdiprops.get("__" + prefix + datasetcodes[i] + "_cols"), DEFAULT_SEPARATOR)));
                    ArrayList<String> fkcolumns = new ArrayList<String>();
                    if (sdiprops.get("__" + prefix + datasetcodes[i] + "_fkcols") != null) {
                        fkcolumns.addAll(Arrays.asList(StringUtil.split((String)sdiprops.get("__" + prefix + datasetcodes[i] + "_fkcols"), DEFAULT_SEPARATOR)));
                    }
                    String blockColumnUpdatesMode = PROPERTY_BLOCKCOLUMNUPDATES_MODE_NO;
                    String blockedColumnsList = "";
                    String attributesVals = (String)sdiprops.get("__" + prefix + "attributes");
                    String[] attributes = RequestParser.parseFormAttributes(attributesVals);
                    if (attributes.length > 30) {
                        String pageid = attributes[29];
                        String pageedition = attributes[30];
                        if (pageid != null && pageedition != null && pageid.length() > 0 && pageedition.length() > 0) {
                            if (pageedition.contains("|")) {
                                pageedition = pageedition.substring(0, pageedition.indexOf("|"));
                            }
                            if (pageid.length() <= 40 && pageedition.length() <= 40) {
                                try {
                                    PropertyList maint;
                                    RequestProcessor rp = new RequestProcessor(this.connectionInfo.getConnectionId());
                                    PropertyList webPageProperties = rp.getWebPageProperties(pageid, pageedition, new PropertyList(), false);
                                    if (datasetname.equalsIgnoreCase("primary") && (maint = webPageProperties.getPropertyList("maint")) != null) {
                                        blockColumnUpdatesMode = maint.getProperty("enablecolumnupdateblocking", PROPERTY_BLOCKCOLUMNUPDATES_MODE_INHERIT);
                                        if (blockColumnUpdatesMode.equals(PROPERTY_BLOCKCOLUMNUPDATES_MODE_INHERIT)) {
                                            blockColumnUpdatesMode = webPageProperties.getPropertyListNotNull("pagedata").getProperty("enablecolumnupdateblocking", PROPERTY_BLOCKCOLUMNUPDATES_MODE_NO);
                                        }
                                        blockedColumnsList = Maint.getBlockColumns(maint, blockColumnUpdatesMode.equals(PROPERTY_BLOCKCOLUMNUPDATES_MODE_YES) || blockColumnUpdatesMode.equals(PROPERTY_BLOCKCOLUMNUPDATES_MODE_YESERROR));
                                    }
                                }
                                catch (SapphireException e) {
                                    this.logWarn("Failed to load page details for " + pageid + ". Unable to build block-column list.");
                                }
                            }
                        }
                    }
                    String string2 = custcols = sdiprops.get("__" + prefix + datasetcodes[i] + "_custcols") != null ? (String)sdiprops.get("__" + prefix + datasetcodes[i] + "_custcols") : "";
                    if (EncryptDecrypt.isObfuscated(custcols)) {
                        custcols = EncryptDecrypt.unobfsql(custcols);
                    }
                    String[] stringArray = customcolumns = custcols.length() > 0 ? StringUtil.split(custcols, DEFAULT_SEPARATOR) : null;
                    if (customcolumns != null) {
                        for (String custcol : customcolumns) {
                            int j = custcol.indexOf(".");
                            if (custcol.startsWith("(")) {
                                String alias = RequestParser.parseAlias(custcol);
                                int k = alias.indexOf(".");
                                if (alias.indexOf(".") > -1) {
                                    if (!alias.substring(k + 1).startsWith("_") || fkcolumns.contains(alias)) continue;
                                    fkcolumns.add(alias);
                                    continue;
                                }
                                if (!alias.startsWith("_") || columns.contains(alias)) continue;
                                columns.add(alias);
                                continue;
                            }
                            if (j > -1) {
                                String realcol = RequestParser.parseColumn(custcol).substring(j + 1);
                                if (realcol.length() <= 0 || !realcol.startsWith("_") || fkcolumns.contains(custcol)) continue;
                                fkcolumns.add(custcol);
                                continue;
                            }
                            if (!custcol.startsWith("_") || columns.contains(custcol)) continue;
                            columns.add(custcol);
                        }
                    }
                    this.logInfo("Reconciling dataset: " + datasetname + " [" + datasetcodes[i] + "] with prefix '" + prefix + "'");
                    addprops.clear();
                    editprops.clear();
                    delprops.clear();
                    fkeditdata.clear();
                    boolean linktable = false;
                    int copies = 1;
                    if (datasetname.equals(datasetcodes[i])) {
                        linktable = true;
                    }
                    try {
                        datasetrows = Integer.parseInt((String)sdiprops.get("__" + prefix + datasetcodes[i] + "_rows"));
                    }
                    catch (NumberFormatException nfe) {
                        datasetrows = 0;
                    }
                    String separator = DEFAULT_SEPARATOR;
                    try {
                        separator = (String)sdiprops.get("__" + prefix + datasetcodes[i] + "_separator");
                        if (separator == null || separator.length() == 0) {
                            separator = DEFAULT_SEPARATOR;
                        }
                    }
                    catch (Exception e) {
                        separator = DEFAULT_SEPARATOR;
                    }
                    boolean sdclinkMaint = false;
                    TreeSet<Integer> sortedRowSet = new TreeSet<Integer>();
                    for (String propertyid : sdiprops.keySet()) {
                        if (propertyid.startsWith("__" + prefix + datasetcodes[i]) && propertyid.endsWith("_rs") && propertyid.indexOf("[__row]") == -1) {
                            try {
                                sortedRowSet.add(new Integer(propertyid.substring(("__" + prefix + datasetcodes[i]).length(), propertyid.lastIndexOf("_rs"))));
                            }
                            catch (NumberFormatException k) {}
                            continue;
                        }
                        if (!propertyid.startsWith("__" + prefix + datasetcodes[i]) || !propertyid.endsWith("_sequence") || propertyid.indexOf("[__row]") != -1) continue;
                        sdclinkMaint = true;
                    }
                    ArrayList rows = new ArrayList();
                    Iterator itr = sortedRowSet.iterator();
                    while (itr.hasNext()) {
                        rows.add(itr.next());
                    }
                    for (int j = 0; j < rows.size(); ++j) {
                        int row = (Integer)rows.get(j);
                        boolean derow = false;
                        String rowstatus = (String)sdiprops.get("__" + prefix + datasetcodes[i] + String.valueOf(row) + "_rs");
                        if (rowstatus != null && rowstatus.length() > 0 && !rowstatus.equalsIgnoreCase("A")) {
                            String[] keyvals;
                            String keyvalue = (String)sdiprops.get("__" + prefix + datasetcodes[i] + String.valueOf(row) + "_key");
                            this.logInfo("Keyvalue" + String.valueOf(row) + ": " + keyvalue + " Row Status: " + rowstatus);
                            if (keyvalue != null && keyvalue.length() > 0) {
                                keyvals = StringUtil.split(keyvalue, DEFAULT_SEPARATOR);
                            } else {
                                keyvals = new String[keycols.length];
                                for (int col = 0; col < keycols.length; ++col) {
                                    keyvals[col] = "(null)";
                                }
                            }
                            switch (rowstatus.charAt(0)) {
                                case 'I': {
                                    props = addprops;
                                    break;
                                }
                                case 'U': {
                                    props = editprops;
                                    break;
                                }
                                case 'D': {
                                    props = delprops;
                                    break;
                                }
                                default: {
                                    props = keylist;
                                }
                            }
                            if (datasetcodes[i].equals("di")) {
                                if (deprops == null) {
                                    deprops = new PropertyList();
                                }
                                if (sdiprops.get("__" + prefix + "de" + String.valueOf(row)).equals("D")) {
                                    derow = true;
                                }
                            }
                            if (keycols.length == keyvals.length) {
                                int col;
                                boolean keyChange = false;
                                for (col = 0; col < keycols.length; ++col) {
                                    if (sdiprops.containsKey(prefix + datasetcodes[i] + String.valueOf(row) + "_" + keycols[col])) {
                                        String keyval = (String)sdiprops.get(prefix + datasetcodes[i] + String.valueOf(row) + "_" + keycols[col]);
                                        if (!(keyvals[col].equals("(null)") || keyvals[col].equals(keyval) || rowstatus.equals(PROPERTY_BLOCKCOLUMNUPDATES_MODE_INHERIT))) {
                                            rowstatus = PROPERTY_BLOCKCOLUMNUPDATES_MODE_INHERIT;
                                            props = addprops;
                                            keyChange = true;
                                        }
                                        keyvals[col] = keyval;
                                    }
                                    if (!keyvals[col].equals("[newkeyid1]")) continue;
                                    keyvals[col] = (String)keylist.get("newkeyid1");
                                }
                                for (col = 0; col < keycols.length; ++col) {
                                    String keycol;
                                    String string3 = keycol = rowstatus.equalsIgnoreCase("S") ? "allkeyid" + String.valueOf(col + 1) : keycols[col];
                                    if (keyvals[col].equals("(null)")) continue;
                                    if (datasetname.equals("primary")) {
                                        keycol = "keyid" + String.valueOf(col + 1);
                                    }
                                    if (props.containsKey(keycol)) {
                                        props.put(keycol, props.get(keycol) + separator + keyvals[col]);
                                    } else {
                                        props.put(keycol, keyvals[col]);
                                    }
                                    if (!derow) continue;
                                    if (deprops.containsKey(keycol)) {
                                        deprops.put(keycol, deprops.get(keycol) + separator + keyvals[col]);
                                        continue;
                                    }
                                    deprops.put(keycol, keyvals[col]);
                                }
                                if (keyChange) {
                                    String[] origkeyvals = StringUtil.split((String)sdiprops.get("__" + prefix + datasetcodes[i] + String.valueOf(row) + "_key"), DEFAULT_SEPARATOR);
                                    for (int col2 = 0; col2 < keycols.length; ++col2) {
                                        if (delprops.containsKey(keycols[col2])) {
                                            delprops.put(keycols[col2], delprops.get(keycols[col2]) + separator + origkeyvals[col2]);
                                            continue;
                                        }
                                        delprops.put(keycols[col2], origkeyvals[col2]);
                                    }
                                }
                                if (rowstatus.equalsIgnoreCase("U") || rowstatus.equalsIgnoreCase(PROPERTY_BLOCKCOLUMNUPDATES_MODE_INHERIT)) {
                                    int col3;
                                    for (col3 = 0; col3 < fkcolumns.size(); ++col3) {
                                        DataSet fkedit;
                                        String fktype;
                                        int fkf;
                                        String[] parts;
                                        if (!rowstatus.equalsIgnoreCase("U") && !rowstatus.equalsIgnoreCase(PROPERTY_BLOCKCOLUMNUPDATES_MODE_INHERIT) || ((String)fkcolumns.get(col3)).indexOf(".") <= -1 || (parts = StringUtil.split((String)fkcolumns.get(col3), ".")).length != 2) continue;
                                        String linksdcolumnid = parts[0];
                                        String updatecolumnid = parts[1];
                                        String aliasid = "";
                                        if (!linksdcolumnid.equalsIgnoreCase("sdialias")) {
                                            aliasid = RequestParser.parseAlias(updatecolumnid);
                                            if (aliasid.length() == 0 || aliasid == updatecolumnid) {
                                                aliasid = (String)fkcolumns.get(col3);
                                            } else {
                                                updatecolumnid = RequestParser.parseColumn(updatecolumnid);
                                            }
                                        } else {
                                            aliasid = (String)fkcolumns.get(col3);
                                        }
                                        if (linksdcolumnid.equalsIgnoreCase("trackitem") || linksdcolumnid.equalsIgnoreCase("sdialias")) {
                                            DataSet fkedit2;
                                            String linkcolumnidvalue3;
                                            if (!sdiprops.containsKey(prefix + datasetcodes[i] + String.valueOf(row) + "_" + keycols[0])) continue;
                                            String linkcolumnidvalue = sdiprops.get(prefix + datasetcodes[i] + String.valueOf(row) + "_" + keycols[0]).toString();
                                            String linkcolumnidvalue2 = keycols.length > 1 && keycols[1].length() > 0 && sdiprops.get(prefix + datasetcodes[i] + String.valueOf(row) + "_" + keycols[1]) != null ? sdiprops.get(prefix + datasetcodes[i] + String.valueOf(row) + "_" + keycols[1]).toString() : "";
                                            String string4 = linkcolumnidvalue3 = keycols.length > 2 && keycols[2].length() > 0 && sdiprops.get(prefix + datasetcodes[i] + String.valueOf(row) + "_" + keycols[2]) != null ? sdiprops.get(prefix + datasetcodes[i] + String.valueOf(row) + "_" + keycols[2]).toString() : "";
                                            if (updatecolumnid.equalsIgnoreCase("trackitemid") && linksdcolumnid.equalsIgnoreCase("trackitem") || linkcolumnidvalue.length() <= 0 || sdcProps.getProperty("keycolid2").length() != 0 && linkcolumnidvalue2.length() <= 0 || sdcProps.getProperty("keycolid3").length() != 0 && linkcolumnidvalue3.length() <= 0) continue;
                                            if (fkeditdata.containsKey(linksdcolumnid)) {
                                                fkedit2 = fkeditdata.get(linksdcolumnid);
                                            } else {
                                                fkedit2 = new DataSet();
                                                fkedit2.addColumn("sdcid", 0);
                                                fkedit2.addColumn("keyid1", 0);
                                                if (sdcProps.getProperty("keycolid2").length() > 0) {
                                                    fkedit2.addColumn("keyid2", 0);
                                                }
                                                if (sdcProps.getProperty("keycolid3").length() > 0) {
                                                    fkedit2.addColumn("keyid3", 0);
                                                }
                                                fkeditdata.put(linksdcolumnid, fkedit2);
                                            }
                                            if (linksdcolumnid.equalsIgnoreCase("sdialias")) {
                                                String avalue = null;
                                                String acol = null;
                                                if (sdiprops.containsKey(prefix + datasetcodes[i] + String.valueOf(row) + "_" + aliasid)) {
                                                    avalue = sdiprops.get(prefix + datasetcodes[i] + String.valueOf(row) + "_" + aliasid).toString();
                                                    acol = updatecolumnid;
                                                } else {
                                                    for (Object akey : sdiprops.keySet()) {
                                                        String skey = akey.toString();
                                                        if (!skey.equalsIgnoreCase(prefix + datasetcodes[i] + String.valueOf(row) + "_" + aliasid)) continue;
                                                        avalue = sdiprops.get(skey).toString();
                                                        acol = skey.substring(skey.indexOf(prefix + datasetcodes[i] + String.valueOf(row) + "_") + (prefix + datasetcodes[i] + String.valueOf(row) + "_").length());
                                                        acol = StringUtil.split(acol, ".")[1];
                                                    }
                                                }
                                                if (avalue == null) continue;
                                                int fklinkrow = fkedit2.addRow();
                                                fkedit2.setValue(fklinkrow, "sdcid", sdcid);
                                                fkedit2.setValue(fklinkrow, "keyid1", linkcolumnidvalue);
                                                if (sdcProps.getProperty("keycolid2").length() > 0) {
                                                    fkedit2.setValue(fklinkrow, "keyid2", linkcolumnidvalue2);
                                                }
                                                if (sdcProps.getProperty("keycolid3").length() > 0) {
                                                    fkedit2.setValue(fklinkrow, "keyid3", linkcolumnidvalue3);
                                                }
                                                if (!fkedit2.isValidColumn("aliasid")) {
                                                    fkedit2.addColumn("aliasid", 0);
                                                }
                                                if (!fkedit2.isValidColumn("aliastype")) {
                                                    fkedit2.addColumn("aliastype", 0);
                                                }
                                                fkedit2.setValue(fklinkrow, "aliastype", acol);
                                                fkedit2.setValue(fklinkrow, "aliasid", avalue);
                                                continue;
                                            }
                                            if (!sdiprops.containsKey(prefix + datasetcodes[i] + String.valueOf(row) + "_" + aliasid)) continue;
                                            int fklinkrow = -1;
                                            boolean newrow = false;
                                            if (linkcolumnidvalue.equalsIgnoreCase("(auto)")) {
                                                linkcolumnidvalue = "(auto)" + row;
                                                newrow = true;
                                            }
                                            if (fkedit2.getRowCount() > 0) {
                                                HashMap<String, String> fkfind = new HashMap<String, String>();
                                                fkfind.put("sdcid", sdcid);
                                                fkfind.put("keyid1", linkcolumnidvalue);
                                                if (sdcProps.getProperty("keycolid2").length() > 0) {
                                                    fkfind.put("keyid2", linkcolumnidvalue2);
                                                }
                                                if (sdcProps.getProperty("keycolid3").length() > 0) {
                                                    fkfind.put("keyid3", linkcolumnidvalue3);
                                                }
                                                fklinkrow = fkedit2.findRow(fkfind);
                                            }
                                            if (fklinkrow == -1) {
                                                fklinkrow = fkedit2.addRow();
                                                fkedit2.setValue(fklinkrow, "sdcid", sdcid);
                                                fkedit2.setValue(fklinkrow, "keyid1", linkcolumnidvalue);
                                                if (sdcProps.getProperty("keycolid2").length() > 0) {
                                                    fkedit2.setValue(fklinkrow, "keyid2", linkcolumnidvalue2);
                                                }
                                                if (sdcProps.getProperty("keycolid3").length() > 0) {
                                                    fkedit2.setValue(fklinkrow, "keyid3", linkcolumnidvalue3);
                                                }
                                            }
                                            if (!fkedit2.isValidColumn(updatecolumnid)) {
                                                fkedit2.addColumn(updatecolumnid, 0);
                                            }
                                            fkedit2.setValue(fklinkrow, updatecolumnid, sdiprops.get(prefix + datasetcodes[i] + String.valueOf(row) + "_" + aliasid).toString());
                                            continue;
                                        }
                                        if (!sdiprops.containsKey(prefix + datasetcodes[i] + String.valueOf(row) + "_" + aliasid)) continue;
                                        if (linksdata == null) {
                                            linksdata = sdcProcessor.getLinksData(sdcid);
                                        }
                                        HashMap<String, String> findlink = new HashMap<String, String>();
                                        findlink.put("sdccolumnid", linksdcolumnid);
                                        int n = fkf = linksdata != null ? linksdata.findRow(findlink) : -1;
                                        if (fkf <= -1 || !(fktype = linksdata.getValue(fkf, "linktype", "")).equalsIgnoreCase("F")) continue;
                                        String linksdcid = linksdata.getValue(fkf, "linksdcid", "");
                                        String linksdcolumnid2 = linksdata.getValue(fkf, "sdccolumnid2", "");
                                        String linkcolumnidvalue = "";
                                        String linkcolumnid2value = "";
                                        boolean linkcont = false;
                                        if (sdiprops.containsKey(prefix + datasetcodes[i] + String.valueOf(row) + "_" + linksdcolumnid)) {
                                            linkcolumnidvalue = sdiprops.get(prefix + datasetcodes[i] + String.valueOf(row) + "_" + linksdcolumnid).toString();
                                            if (linksdcolumnid2.length() > 0) {
                                                if (sdiprops.containsKey(prefix + datasetcodes[i] + String.valueOf(row) + "_" + linksdcolumnid2)) {
                                                    linkcolumnid2value = sdiprops.get(prefix + datasetcodes[i] + String.valueOf(row) + "_" + linksdcolumnid2).toString();
                                                    if (linkcolumnidvalue.length() > 0 && linkcolumnid2value.length() > 0) {
                                                        linkcont = true;
                                                    }
                                                }
                                            } else if (linkcolumnidvalue.length() > 0) {
                                                linkcont = true;
                                            }
                                        }
                                        if (!linkcont) continue;
                                        if (fkeditdata.containsKey(linksdcolumnid)) {
                                            fkedit = fkeditdata.get(linksdcolumnid);
                                        } else {
                                            fkedit = new DataSet();
                                            fkedit.addColumn("sdcid", 0);
                                            fkedit.addColumn("keyid1", 0);
                                            if (linksdcolumnid2.length() > 0) {
                                                fkedit.addColumn("keyid2", 0);
                                            }
                                            fkeditdata.put(linksdcolumnid, fkedit);
                                        }
                                        int fklinkrow = -1;
                                        if (fkedit.getRowCount() > 0) {
                                            HashMap<String, String> fkfind = new HashMap<String, String>();
                                            fkfind.put("sdcid", linksdcid);
                                            fkfind.put("keyid1", linkcolumnidvalue);
                                            if (linksdcolumnid2.length() > 0) {
                                                fkfind.put("keyid2", linkcolumnid2value);
                                            }
                                            fklinkrow = fkedit.findRow(fkfind);
                                        }
                                        if (fklinkrow == -1) {
                                            fklinkrow = fkedit.addRow();
                                            fkedit.setValue(fklinkrow, "sdcid", linksdcid);
                                            fkedit.setValue(fklinkrow, "keyid1", linkcolumnidvalue);
                                            if (linksdcolumnid2.length() > 0) {
                                                fkedit.setValue(fklinkrow, "keyid2", linkcolumnid2value);
                                            }
                                        }
                                        if (!fkedit.isValidColumn(updatecolumnid)) {
                                            fkedit.addColumn(updatecolumnid, 0);
                                        }
                                        fkedit.setValue(fklinkrow, updatecolumnid, sdiprops.get(prefix + datasetcodes[i] + String.valueOf(row) + "_" + aliasid).toString());
                                    }
                                    for (col3 = 0; col3 < columns.size(); ++col3) {
                                        String value;
                                        String columnid = (String)columns.get(col3);
                                        if (columnid.equals("createdt") || columnid.equals("createby") || columnid.equals("moddt") || columnid.equals("modby") || keyset.contains(columnid)) continue;
                                        if (sdiprops.containsKey(prefix + datasetcodes[i] + String.valueOf(row) + "_" + columnid) || sdclinkMaint && columnid.equalsIgnoreCase("usersequence")) {
                                            String colalias;
                                            String id = prefix + datasetcodes[i] + String.valueOf(row) + "_" + columnid;
                                            value = (String)sdiprops.get(id);
                                            if (columnid.equalsIgnoreCase("usersequence") && (value == null || value.length() == 0)) {
                                                value = (String)sdiprops.get("__" + prefix + datasetcodes[i] + String.valueOf(row) + "_sequence");
                                            }
                                            if (value == null || value.length() == 0 || value.equals("(none)")) {
                                                value = "(null)";
                                            }
                                            if ((colalias = columnid).equalsIgnoreCase("sdcid") || colalias.equalsIgnoreCase("keyid1") || colalias.equalsIgnoreCase("keyid2") || colalias.equalsIgnoreCase("keyid3")) {
                                                colalias = "__" + colalias;
                                            }
                                            if (value.indexOf(DEFAULT_SEPARATOR) != -1) {
                                                value = value.replaceAll(DEFAULT_SEPARATOR, "#semicolon#");
                                            }
                                            if (props.containsKey(colalias)) {
                                                props.put(colalias, props.get(colalias) + separator + value);
                                            } else {
                                                props.put(colalias, value);
                                            }
                                            if (!derow) continue;
                                            if (deprops.containsKey(colalias)) {
                                                deprops.put(colalias, deprops.get(colalias) + separator + value);
                                                continue;
                                            }
                                            deprops.put(colalias, value);
                                            continue;
                                        }
                                        if (!sdiprops.containsKey(prefix + datasetcodes[i] + String.valueOf(row) + "_" + columnid + "_radiomarker")) continue;
                                        String colalias = columnid;
                                        value = "(null)";
                                        if (props.containsKey(colalias)) {
                                            props.put(colalias, props.get(colalias) + separator + value);
                                        } else {
                                            props.put(colalias, value);
                                        }
                                        if (!derow) continue;
                                        if (deprops.containsKey(colalias)) {
                                            deprops.put(colalias, deprops.get(colalias) + separator + value);
                                            continue;
                                        }
                                        deprops.put(colalias, value);
                                    }
                                    if (rowstatus.equalsIgnoreCase(PROPERTY_BLOCKCOLUMNUPDATES_MODE_INHERIT)) {
                                        if (sdiprops.containsKey("__" + prefix + datasetcodes[i] + String.valueOf(row) + "_templateid")) {
                                            props.put("templateid", sdiprops.get("__" + prefix + datasetcodes[i] + String.valueOf(row) + "_templateid"));
                                        }
                                        if (sdiprops.containsKey("__" + prefix + datasetcodes[i] + String.valueOf(row) + "_templatekeyid1")) {
                                            props.put("templatekeyid1", sdiprops.get("__" + prefix + datasetcodes[i] + String.valueOf(row) + "_templatekeyid1"));
                                        }
                                        if (sdiprops.containsKey("__" + prefix + datasetcodes[i] + String.valueOf(row) + "_templatekeyid2")) {
                                            props.put("templatekeyid2", sdiprops.get("__" + prefix + datasetcodes[i] + String.valueOf(row) + "_templatekeyid2"));
                                        }
                                        if (sdiprops.containsKey("__" + prefix + datasetcodes[i] + String.valueOf(row) + "_templatekeyid3")) {
                                            props.put("templatekeyid3", sdiprops.get("__" + prefix + datasetcodes[i] + String.valueOf(row) + "_templatekeyid3"));
                                        }
                                        props.put("copies", String.valueOf(copies++));
                                    }
                                }
                                String[] extraprops = StringUtil.split((String)sdiprops.get("__" + prefix + datasetcodes[i] + "_extraprops"), DEFAULT_SEPARATOR);
                                for (int ep = 0; ep < extraprops.length; ++ep) {
                                    String[] extraprop = StringUtil.split(extraprops[ep], "=");
                                    if (extraprop.length != 2 || extraprop[0].length() <= 0 || extraprop[1].length() <= 0) continue;
                                    props.put(extraprop[0], extraprop[1]);
                                    if (derow) {
                                        deprops.put(extraprop[0], extraprop[1]);
                                    }
                                    if (!extraprop[0].equals("auditactivity") && !extraprop[0].equals("buttonactivity") && !extraprop[0].equals("auditreason")) continue;
                                    sdiprops.put(extraprop[0], extraprop[1]);
                                }
                                continue;
                            }
                            this.logError("Keycol and keyval arrays differ in length for row: " + datasetcodes[i] + String.valueOf(row) + "_key = " + keyvalue);
                            continue;
                        }
                        this.logInfo("Row " + String.valueOf(row) + " filtered out");
                    }
                    traceLogIdStr = TagRequestPropertyHandler.saveDataset(sdiprops, errorHandler, traceLogIdStr, audit, keylist, props, editprops, delprops, addprops, deprops, fkeditdata, sdidef.getLinkid(datasetname), "", sdcid, sdcProps.getProperty("auditedflag"), sdcProps.getProperty("auditpromotflag"), actionService, linktable, false, datasetname, keycols, separator, this, blockColumnUpdatesMode, blockedColumnsList);
                    if (!datasetname.equals("primary")) continue;
                    this.resolveKeyid(sdiprops, keylist);
                }
                sdiprops.put(KEYLIST, keylist);
                this.processCustomPropertyHandlers(sdiprops, handlers_post, errorHandler);
                if (!sdiprops.containsKey("action_checkinpostsave") || !PROPERTY_BLOCKCOLUMNUPDATES_MODE_YES.equals(sdiprops.get("action_checkinpostsave")) || (cmtcheckinprops = this.getConfigurationProcessor().getProfileProperty("cmtcheckinprops")) == null || cmtcheckinprops.length() <= 0) break block149;
                JSONObject o = new JSONObject(cmtcheckinprops);
                String _sdcid = o.getString("sdcid");
                String _keyid1 = o.getString("keyid1");
                String _keyid2 = o.has("keyid2") ? o.getString("keyid2") : "";
                String _keyid3 = o.has("keyid3") ? o.getString("keyid3") : "";
                String changelogid = o.has("changelogid") ? o.getString("changelogid") : "";
                String checkincomments = o.has("checkincomments") ? o.getString("checkincomments") : "";
                String propertytreenodeid = o.has("propertytreenodeid") ? o.getString("propertytreenodeid") : "";
                PropertyList checkinprops = new PropertyList();
                checkinprops.setProperty("changelogid", changelogid);
                checkinprops.setProperty("notes", checkincomments);
                try {
                    CMTPolicy policy = CMTPolicy.getPolicy(this.getConnectionInfo().getConnectionId(), "");
                    if (policy.isMasterRepositoryEnabled()) {
                        checkinprops.setProperty("operation", "checkin");
                        if (checkinprops.getProperty("changelogid").length() == 0) {
                            checkinprops.setProperty("sdcid", _sdcid);
                            checkinprops.setProperty("keyid1", _keyid1);
                            checkinprops.setProperty("keyid2", _keyid2);
                            checkinprops.setProperty("keyid3", _keyid3);
                            if ("PropertyTree".equals(_keyid3)) {
                                checkinprops.setProperty("propertytreenodeid", propertytreenodeid);
                            }
                        }
                        this.getActionProcessor().processActionClass(RepositoryOperations.class.getName(), checkinprops);
                        break block149;
                    }
                    this.getActionProcessor().processActionClass(CheckInSDI.class.getName(), checkinprops);
                }
                catch (ActionException e) {
                    String message = "";
                    String errorMsg = e.getMessage();
                    int lastIndex = errorMsg.lastIndexOf("sapphire.accessor.ActionException");
                    message = lastIndex > -1 ? errorMsg.substring(lastIndex + 35) : this.getTranslationProcessor().translate("Error in checking in. Please contact Administrator.");
                    this.logError("Error raised processing SDITag properties. Reason: " + message, e);
                    errorHandler = new ErrorHandler(e.getErrorid(), e.getErrorType(), e.getMessage());
                    sdiprops.put("ERRORHANDLER", errorHandler);
                }
            }
            catch (SapphireException e) {
                this.logError("Error raised processing SDITag properties. Reason: " + e.getMessage(), e);
                errorHandler = new ErrorHandler(e.getErrorid(), e.getErrorType(), e.getMessage());
                sdiprops.put("ERRORHANDLER", errorHandler);
            }
            catch (ServiceException e) {
                this.logError("Error raised processing SDITag properties. Reason: " + e.getMessage(), e);
                sdiprops.put("ERRORHANDLER", errorHandler);
            }
            catch (Exception e) {
                this.logError("Error raised processing SDITag properties. Reason: " + e.getMessage(), e);
                sdiprops.put("ERRORHANDLER", errorHandler);
            }
            finally {
                try {
                    this.logInfo("Save finished. Remove the Tracelogid from DB Session: '" + traceLogIdStr + "'");
                    audit.removeTracelogIdFromDBSession();
                }
                catch (ServiceException e) {
                    this.logError("Failed to clear Tracelog ID from Database Session after Save operation.", e);
                }
            }
        }
    }

    public static String generateTraceLog(String traceLogIdStr, AuditService audit, String auditedflag, String auditpromptflag, PropertyList props, HashMap sdiprops, PropertyHandler logger) {
        block7: {
            try {
                if (auditedflag.equalsIgnoreCase(PROPERTY_BLOCKCOLUMNUPDATES_MODE_NO) || traceLogIdStr.length() != 0) break block7;
                logger.logInfo("Generate the tracelog records");
                String promptflag = auditpromptflag;
                String standard = !promptflag.equalsIgnoreCase("R") && !promptflag.equalsIgnoreCase("S") ? PROPERTY_BLOCKCOLUMNUPDATES_MODE_NO : PROPERTY_BLOCKCOLUMNUPDATES_MODE_YES;
                try {
                    String auditReason = props.getProperty("auditreason", "");
                    String auditActivity = props.getProperty("auditactivity", "");
                    String buttonActivity = props.getProperty("buttonactivity", "").trim();
                    if (auditReason.length() > 0 || auditActivity.length() > 0) {
                        int indexOfAmp;
                        int indexOfPage;
                        String traceLogDesc = buttonActivity.length() > 0 ? buttonActivity : auditActivity;
                        String pageName = (String)sdiprops.get("__self");
                        if (pageName != null && (indexOfPage = pageName.indexOf("page=")) != -1 && (pageName = (indexOfAmp = pageName.indexOf("&", indexOfPage)) > -1 ? pageName.substring(indexOfPage + 5, pageName.indexOf("&", indexOfPage + 1)) : pageName.substring(indexOfPage + 5)).length() > 0) {
                            traceLogDesc = traceLogDesc + " on " + pageName;
                        }
                        int tracelogid = Integer.parseInt(audit.addTraceLogEntry(auditReason, buttonActivity.length() > 0 ? buttonActivity : auditActivity, props.getProperty("auditsignedflag", PROPERTY_BLOCKCOLUMNUPDATES_MODE_NO), props.getProperty("auditdt"), traceLogDesc, standard.equals(PROPERTY_BLOCKCOLUMNUPDATES_MODE_YES)));
                        traceLogIdStr = String.valueOf(tracelogid);
                        sdiprops.put("tracelogid", traceLogIdStr);
                        if (buttonActivity.length() > 0) {
                            audit.setTracelogIdInDBSession(traceLogIdStr);
                            props.remove("auditreason");
                            props.remove("auditsignedflag");
                            props.remove("auditactivity");
                            props.remove("buttonactivity");
                        }
                    }
                }
                catch (ServiceException e) {
                    throw new SapphireException("Failed to add audit records", e);
                }
            }
            catch (Exception e) {
                logger.logError("Could not created tracelog id for this request", e);
            }
        }
        return traceLogIdStr;
    }

    public static String saveDataset(HashMap sdiprops, ErrorHandler errorHandler, String traceLogIdStr, AuditService audit, PropertyList keylist, PropertyList props, PropertyList editprops, PropertyList delprops, PropertyList addprops, PropertyList deprops, String linkid, String detailLinkId, String sdcid, String auditedflag, String auditpromptflag, ActionService actionService, boolean linktable, boolean detailLinkTable, String datasetname, String[] keycols, String separator, PropertyHandler logger) throws ServiceException, SapphireException {
        return TagRequestPropertyHandler.saveDataset(sdiprops, errorHandler, traceLogIdStr, audit, keylist, props, editprops, delprops, addprops, deprops, null, linkid, detailLinkId, sdcid, auditedflag, auditpromptflag, actionService, linktable, detailLinkTable, datasetname, keycols, separator, logger, PROPERTY_BLOCKCOLUMNUPDATES_MODE_NO, "");
    }

    public static String saveDataset(HashMap sdiprops, ErrorHandler errorHandler, String traceLogIdStr, AuditService audit, PropertyList keylist, PropertyList props, PropertyList editprops, PropertyList delprops, PropertyList addprops, PropertyList deprops, HashMap<String, DataSet> fkeditprops, String linkid, String detailLinkId, String sdcid, String auditedflag, String auditpromptflag, ActionService actionService, boolean linktable, boolean detailLinkTable, String datasetname, String[] keycols, String separator, PropertyHandler logger, String blockColumnUpdatesMode, String blockedColumns) throws ServiceException, SapphireException {
        int key;
        traceLogIdStr = TagRequestPropertyHandler.generateTraceLog(traceLogIdStr, audit, auditedflag, auditpromptflag, props, sdiprops, logger);
        String addactionid = "";
        String delactionid = "";
        String edtactionid = "";
        if (datasetname.equals("primary")) {
            addactionid = "AddSDI";
            edtactionid = "EditSDI";
            delactionid = "DeleteSDI";
        } else if (datasetname.equals("workflow")) {
            addactionid = "AddWorkflow";
            edtactionid = "EditWorkflow";
            delactionid = "DeleteWorkflow";
        } else if (datasetname.equals("dataset")) {
            addactionid = "AddDataSet";
            edtactionid = "EditDataSet";
            delactionid = "DeleteDataSet";
        } else if (datasetname.equals("dataitem")) {
            addactionid = "AddDataItem";
            edtactionid = "EditDataItem";
            delactionid = "DeleteDataItem";
        } else if (datasetname.equals("datalimit")) {
            addactionid = "AddDataItemLimit";
            edtactionid = "EditDataItemLimit";
            delactionid = "DeleteDataItemLimit";
        } else if (datasetname.equals("dataapproval")) {
            addactionid = "AddDataApproval";
            edtactionid = "EditDataApproval";
            delactionid = "DeleteDataApproval";
        } else if (datasetname.equals("dataspec")) {
            addactionid = "AddDataSpec";
            edtactionid = "";
            delactionid = "DeleteDataSpec";
        } else if (datasetname.equals("spec")) {
            addactionid = "AddSDISpec";
            edtactionid = "EditSDISpec";
            delactionid = "DeleteSDISpec";
        } else if (datasetname.equals("address")) {
            addactionid = "AddSDIAddress";
            edtactionid = "EditSDIAddress";
            delactionid = "DeleteSDIAddress";
        } else if (datasetname.equals("pricelistitem")) {
            addactionid = "AddPriceListItem";
            edtactionid = "EditPriceListItem";
            delactionid = "DeletePriceListItem";
        } else if (datasetname.equals("workgroupitem")) {
            addactionid = "AddWorkgroupItem";
            edtactionid = "EditWorkgroupItem";
            delactionid = "DeleteWorkgroupItem";
        } else if (datasetname.equals("chargelistitem")) {
            addactionid = "AddChargeListItem";
            edtactionid = "EditChargeListItem";
            delactionid = "DeleteChargeListItem";
        } else if (datasetname.equals("workitem")) {
            addactionid = "AddSDIWorkItem";
            edtactionid = "EditSDIWorkItem";
            delactionid = "DeleteSDIWorkItem";
        } else if (datasetname.equals("role")) {
            addactionid = "AddSDIRole";
            edtactionid = "";
            delactionid = "DeleteSDIRole";
        } else if (datasetname.equals("category")) {
            addactionid = "AddCategoryItem";
            edtactionid = "";
            delactionid = "DeleteCategoryItem";
        }
        if (delprops.size() > 0) {
            if (!separator.equals(DEFAULT_SEPARATOR)) {
                delprops.put("separator", separator);
            }
            String delactionclass = "";
            if (delprops.size() != 0) {
                delprops.setProperty("tracelogid", traceLogIdStr);
            }
            if (sdcid.equals("SDC") && linktable) {
                delactionid = "";
                delactionclass = datasetname.equalsIgnoreCase("syscolumn") ? "com.labvantage.sapphire.actions.ddt.DeleteSDCColumn" : (datasetname.equalsIgnoreCase("sdclink") ? "com.labvantage.sapphire.actions.ddt.DeleteSDCLink" : (datasetname.equalsIgnoreCase("sdcexport") ? "com.labvantage.sapphire.actions.ddt.DeleteSDCExport" : (datasetname.equalsIgnoreCase("sdcoperation") ? "com.labvantage.sapphire.actions.ddt.DeleteSDCOperation" : (datasetname.equalsIgnoreCase("sdcattributedef") ? "com.labvantage.sapphire.actions.ddt.DeleteSDCAttribute" : ""))));
            } else {
                if (!datasetname.equals("pricelistitem") && !datasetname.equals("workgroupitem")) {
                    delprops.setProperty("sdcid", sdcid);
                }
                if (linktable) {
                    delactionid = "DeleteSDIDetail";
                    delprops.setProperty("linkid", linkid);
                    for (key = 1; key <= keycols.length; ++key) {
                        delprops.setProperty("keyid" + key, (String)sdiprops.get("keyid" + key));
                    }
                    if (detailLinkTable) {
                        delprops.setProperty("detaillinkid", detailLinkId);
                    }
                }
            }
            delprops.setProperty("applylock", PROPERTY_BLOCKCOLUMNUPDATES_MODE_NO);
            logger.logInfo("Delete action properties: " + delprops.toString());
            try {
                if (delactionid.length() > 0) {
                    actionService.processAction(delactionid, "1", delprops, errorHandler);
                } else {
                    actionService.processActionClass(delactionclass, delprops, errorHandler);
                }
                if (datasetname.equals("primary")) {
                    for (key = 1; key <= keycols.length; ++key) {
                        keylist.setProperty("delkeyid" + key, delprops.getProperty("keyid" + key));
                    }
                }
            }
            catch (ServiceException e) {
                if (errorHandler.size() > 0 && ErrorHandler.isErrorHandlerFormat(e.getMessage())) {
                    throw e;
                }
                throw new SapphireException(e.getErrorid(), ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(actionService.getConnectionId())), e);
            }
        }
        if (addprops.size() > 0) {
            if (datasetname.equals("primary") && blockedColumns != null && blockedColumns.length() > 0) {
                addprops.setProperty(PROPERTY_BLOCKEDCOLUMNS, blockedColumns);
                addprops.setProperty(PROPERTY_BLOCKCOLUMNUPDATES_MODE, blockColumnUpdatesMode);
            }
            if (!separator.equals(DEFAULT_SEPARATOR)) {
                addprops.setProperty("separator", separator);
            }
            String addactionclass = "";
            if (addprops.size() != 0) {
                addprops.setProperty("tracelogid", traceLogIdStr);
            }
            if (sdcid.equals("SDC") && linktable) {
                addactionid = "";
                addactionclass = datasetname.equalsIgnoreCase("syscolumn") ? "com.labvantage.sapphire.actions.ddt.AddSDCColumn" : (datasetname.equalsIgnoreCase("sdclink") ? "com.labvantage.sapphire.actions.ddt.AddSDCLink" : (datasetname.equalsIgnoreCase("sdcexport") ? "com.labvantage.sapphire.actions.ddt.AddSDCExport" : (datasetname.equalsIgnoreCase("sdcoperation") ? "com.labvantage.sapphire.actions.ddt.AddSDCOperation" : (datasetname.equalsIgnoreCase("sdcattributedef") ? "com.labvantage.sapphire.actions.ddt.AddSDCAttribute" : ""))));
            } else {
                if (!datasetname.equals("pricelistitem") && !datasetname.equals("workgroupitem")) {
                    addprops.setProperty("sdcid", sdcid);
                }
                if (linktable) {
                    addactionid = "AddSDIDetail";
                    addprops.setProperty("linkid", linkid);
                    for (key = 1; key <= keycols.length; ++key) {
                        addprops.setProperty("keyid" + key, (String)sdiprops.get("keyid" + key));
                    }
                    if (detailLinkTable) {
                        addprops.setProperty("detaillinkid", detailLinkId);
                    }
                }
            }
            addprops.setProperty("propsmatch", PROPERTY_BLOCKCOLUMNUPDATES_MODE_YES);
            if ("AddSDI".equals(addactionid) && (addprops.getProperty("templateid").length() > 0 || addprops.getProperty("templatekeyid1").length() > 0) && !PROPERTY_BLOCKCOLUMNUPDATES_MODE_YES.equalsIgnoreCase(addprops.getProperty("templateflag")) && addprops.getProperty("applyworkitem").length() == 0) {
                addprops.setProperty("applyworkitems", PROPERTY_BLOCKCOLUMNUPDATES_MODE_YES);
            }
            logger.logInfo("Add action properties: " + addprops.toString());
            try {
                if (addactionid.length() > 0) {
                    if (TagRequestPropertyHandler.isSDCOperationAllowed(actionService.getConnectionid(), sdcid, addactionid, addprops, sdiprops)) {
                        actionService.processAction(addactionid, "1", addprops, errorHandler);
                    }
                } else {
                    actionService.processActionClass(addactionclass, addprops, errorHandler);
                }
                if (addprops.getProperty("(poll)").equals(PROPERTY_BLOCKCOLUMNUPDATES_MODE_YES)) {
                    sdiprops.put("(poll)", PROPERTY_BLOCKCOLUMNUPDATES_MODE_YES);
                }
                if (datasetname.equals("primary")) {
                    for (key = 1; key <= keycols.length; ++key) {
                        keylist.setProperty("newkeyid" + key, addprops.getProperty("newkeyid" + key));
                    }
                    String activitytransaction = (String)sdiprops.get("transactionid");
                    if (activitytransaction != null && activitytransaction.length() > 0) {
                        QueryProcessor qp = new QueryProcessor(actionService.getConnectionid());
                        String[] keyid1s = StringUtil.split(keylist.getProperty("newkeyid1"), DEFAULT_SEPARATOR);
                        String[] keyid2s = StringUtil.split(keylist.getProperty("newkeyid2"), DEFAULT_SEPARATOR);
                        String[] keyid3s = StringUtil.split(keylist.getProperty("newkeyid3"), DEFAULT_SEPARATOR);
                        for (int i = 0; i < keyid1s.length; ++i) {
                            qp.execPreparedUpdate("UPDATE activitylog set keyid1=?, keyid2=?, keyid3=? WHERE savetransaction=?", new Object[]{keyid1s[i], keycols.length > 1 && keycols[1] != null && keycols[1].length() > 0 ? keyid2s[i] : "(null)", keycols.length > 2 && keycols[2] != null && keycols[2].length() > 0 ? keyid3s[i] : "(null)", activitytransaction});
                        }
                    }
                }
            }
            catch (ServiceException e) {
                if (errorHandler.size() > 0 && ErrorHandler.isErrorHandlerFormat(e.getMessage())) {
                    throw e;
                }
                throw new SapphireException(e.getErrorid(), ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(actionService.getConnectionId())), e);
            }
        }
        if (editprops.size() > 0) {
            if (datasetname.equals("primary") && blockedColumns != null && blockedColumns.length() > 0) {
                editprops.setProperty(PROPERTY_BLOCKCOLUMNUPDATES_MODE, blockColumnUpdatesMode);
                editprops.setProperty(PROPERTY_BLOCKEDCOLUMNS, blockedColumns);
            }
            if (!separator.equals(DEFAULT_SEPARATOR)) {
                editprops.setProperty("separator", separator);
            }
            String edtactionclass = "";
            if (editprops.size() != 0) {
                editprops.setProperty("tracelogid", traceLogIdStr);
            }
            if (sdcid.equals("SDC") && linktable) {
                edtactionid = "";
                edtactionclass = datasetname.equalsIgnoreCase("syscolumn") ? "com.labvantage.sapphire.actions.ddt.EditSDCColumn" : (datasetname.equalsIgnoreCase("sdclink") ? "com.labvantage.sapphire.actions.ddt.EditSDCLink" : (datasetname.equalsIgnoreCase("sdcexport") ? "com.labvantage.sapphire.actions.ddt.EditSDCExport" : (datasetname.equalsIgnoreCase("sdcoperation") ? "com.labvantage.sapphire.actions.ddt.EditSDCOperation" : (datasetname.equalsIgnoreCase("sdcattributedef") ? "com.labvantage.sapphire.actions.ddt.EditSDCAttribute" : ""))));
            } else {
                if (!datasetname.equals("pricelistitem") && !datasetname.equals("workgroupitem")) {
                    editprops.setProperty("sdcid", sdcid);
                }
                if (linktable) {
                    edtactionid = "EditSDIDetail";
                    editprops.setProperty("linkid", linkid);
                    for (int key2 = 1; key2 <= keycols.length; ++key2) {
                        editprops.setProperty("keyid" + key2, (String)sdiprops.get("keyid" + key2));
                    }
                    if (detailLinkTable) {
                        editprops.setProperty("detaillinkid", detailLinkId);
                    }
                }
            }
            editprops.setProperty("propsmatch", PROPERTY_BLOCKCOLUMNUPDATES_MODE_YES);
            editprops.setProperty("applylock", PROPERTY_BLOCKCOLUMNUPDATES_MODE_NO);
            logger.logInfo("Edit action properties: " + editprops.toString());
            try {
                if (edtactionid.length() > 0) {
                    if (TagRequestPropertyHandler.isSDCOperationAllowed(actionService.getConnectionid(), sdcid, edtactionid, editprops, sdiprops)) {
                        actionService.processAction(edtactionid, "1", editprops, errorHandler);
                    }
                } else {
                    actionService.processActionClass(edtactionclass, editprops, errorHandler);
                }
                if (editprops.getProperty("(poll)").equals(PROPERTY_BLOCKCOLUMNUPDATES_MODE_YES)) {
                    sdiprops.put("(poll)", PROPERTY_BLOCKCOLUMNUPDATES_MODE_YES);
                }
                if (datasetname.equals("primary")) {
                    for (int key3 = 1; key3 <= keycols.length; ++key3) {
                        keylist.setProperty("editkeyid" + key3, editprops.getProperty("keyid" + key3));
                    }
                }
                if (deprops != null && deprops.size() > 0) {
                    deprops.setProperty("sdcid", sdcid);
                    deprops.setProperty("propsmatch", PROPERTY_BLOCKCOLUMNUPDATES_MODE_YES);
                    deprops.setProperty("applylock", PROPERTY_BLOCKCOLUMNUPDATES_MODE_NO);
                    deprops.setProperty("tracelogid", traceLogIdStr);
                    actionService.processAction("EnterDataItem", "1", deprops);
                    deprops.clear();
                }
            }
            catch (ServiceException e) {
                if (errorHandler.size() > 0 && ErrorHandler.isErrorHandlerFormat(e.getMessage())) {
                    throw e;
                }
                throw new SapphireException(e.getErrorid(), ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(actionService.getConnectionId())), e);
            }
        }
        if (fkeditprops != null && fkeditprops.size() > 0) {
            String[] newkeyid1 = addprops != null && addprops.size() > 0 ? StringUtil.split(addprops.getProperty("newkeyid1"), DEFAULT_SEPARATOR) : null;
            for (String linkcolid : fkeditprops.keySet()) {
                DataSet fkedit = fkeditprops.get(linkcolid);
                if (fkedit == null || fkedit.getRowCount() <= 0) continue;
                if (newkeyid1 != null && newkeyid1.length > 0) {
                    for (int r = 0; r < fkedit.getRowCount(); ++r) {
                        boolean autoKey = false;
                        if (linkcolid.equalsIgnoreCase("sdialias")) {
                            autoKey = fkedit.getValue(r, "keyid1", "").equalsIgnoreCase("(auto)");
                        } else if (linkcolid.equalsIgnoreCase("trackitem")) {
                            autoKey = fkedit.getValue(r, "keyid1", "").toLowerCase().startsWith("(auto)");
                        }
                        if (!autoKey) continue;
                        if (r < newkeyid1.length) {
                            fkedit.setValue(r, "keyid1", newkeyid1[r]);
                            continue;
                        }
                        fkedit.setValue(r, "keyid1", newkeyid1[newkeyid1.length - 1]);
                    }
                }
                PropertyList fkeditpropertylist = new PropertyList();
                if (!linkcolid.equalsIgnoreCase("trackitem")) {
                    if (linkcolid.equalsIgnoreCase("sdialias")) {
                        fkeditpropertylist.setProperty("tracelogid", traceLogIdStr);
                        fkeditpropertylist.setProperty("autoupdate", PROPERTY_BLOCKCOLUMNUPDATES_MODE_YES);
                        fkeditpropertylist.setProperty("padalias", PROPERTY_BLOCKCOLUMNUPDATES_MODE_NO);
                    } else {
                        if (!separator.equals(DEFAULT_SEPARATOR)) {
                            fkeditpropertylist.put("separator", separator);
                        }
                        fkeditpropertylist.setProperty("tracelogid", traceLogIdStr);
                        fkeditpropertylist.setProperty("propsmatch", PROPERTY_BLOCKCOLUMNUPDATES_MODE_YES);
                        fkeditpropertylist.setProperty("applylock", PROPERTY_BLOCKCOLUMNUPDATES_MODE_NO);
                    }
                }
                StringBuilder subblockcols = new StringBuilder();
                for (String columnid : fkedit.getColumns()) {
                    if (columnid.equalsIgnoreCase("sdcid")) {
                        fkeditpropertylist.setProperty("sdcid", fkedit.getValue(0, "sdcid", ""));
                        continue;
                    }
                    fkeditpropertylist.setProperty(columnid, fkedit.getColumnValues(columnid, separator));
                    if (blockedColumns == null || blockedColumns.length() <= 0 || !(DEFAULT_SEPARATOR + blockedColumns + DEFAULT_SEPARATOR).contains(DEFAULT_SEPARATOR + linkcolid + "." + columnid + DEFAULT_SEPARATOR)) continue;
                    subblockcols.append(DEFAULT_SEPARATOR).append(columnid);
                }
                if (subblockcols.length() > 0) {
                    fkeditpropertylist.setProperty(PROPERTY_BLOCKEDCOLUMNS, subblockcols.substring(1));
                    fkeditpropertylist.setProperty(PROPERTY_BLOCKCOLUMNUPDATES_MODE, blockColumnUpdatesMode);
                }
                try {
                    String sdcruleconfirm = (String)sdiprops.get("action___sdcruleconfirm");
                    fkeditpropertylist.setProperty("__sdcruleconfirm", sdcruleconfirm == null ? PROPERTY_BLOCKCOLUMNUPDATES_MODE_NO : sdcruleconfirm);
                    actionService.processAction(linkcolid.equalsIgnoreCase("trackitem") ? "EditTrackItem" : (linkcolid.equalsIgnoreCase("sdialias") ? "AddSDIAlias" : "EditSDI"), "1", fkeditpropertylist, errorHandler);
                }
                catch (ServiceException e) {
                    if (errorHandler.size() > 0 && ErrorHandler.isErrorHandlerFormat(e.getMessage())) {
                        throw e;
                    }
                    throw new SapphireException(e.getErrorid(), ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(actionService.getConnectionId())), e);
                }
            }
        }
        return traceLogIdStr;
    }

    public static boolean isSDCOperationAllowed(String connectionid, String sdcid, String actionid, PropertyList actionprops, HashMap sdiprops) throws SapphireException {
        if (("User".equals(sdcid) || "LV_JobType".equals(sdcid)) && ("EditSDI".equals(actionid) || "AddSDI".equals(actionid) || "AddSDIDetail".equals(actionid)) || "ModuleSDC".equals(sdcid) && ("AddSDIDetail".equals(actionid) || "DeleteSDIDetail".equals(actionid))) {
            ConnectionInfo connectionInfo = new ConnectionProcessor(connectionid).getConnectionInfo(connectionid);
            if (TagRequestPropertyHandler.isUserPreferencePageSave(sdiprops)) {
                String currentUserid = connectionInfo.getSysuserId();
                if (actionprops.getProperty("keyid1").length() > 0 && !currentUserid.equals(actionprops.getProperty("keyid1")) || actionprops.getProperty("sysuserid").length() > 0 && !currentUserid.equals(actionprops.getProperty("sysuserid"))) {
                    throw new SapphireException("Not authorized!");
                }
            } else if (!(connectionInfo.hasRole("Administrator") || connectionInfo.hasRole("WebPage_Admin") || connectionInfo.hasModule("Security"))) {
                throw new SapphireException("Not authorized!");
            }
        }
        return true;
    }

    private static boolean isUserPreferencePageSave(HashMap sdiprops) {
        return sdiprops.get("action___nexturl") != null && ((String)sdiprops.get("action___nexturl")).indexOf("&page=LV_UserPreferences") > 0 || sdiprops.get("__nexturl") != null && ((String)sdiprops.get("__nexturl")).indexOf("&page=LV_UserPreferences") > 0;
    }
}

