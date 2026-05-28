/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpSession
 */
package com.labvantage.sapphire.pageelements.forms;

import com.labvantage.sapphire.admin.propertytree.ListEditor;
import com.labvantage.sapphire.admin.propertytree.StringEditor;
import com.labvantage.sapphire.admin.propertytree.TypeSimple;
import com.labvantage.sapphire.admin.propertytree.ValidationEditor;
import com.labvantage.sapphire.admin.propertytree.YesNoEditor;
import com.labvantage.sapphire.util.groovy.GroovyUtil;
import com.labvantage.sapphire.xml.PropertyDefinition;
import com.labvantage.sapphire.xml.PropertyDefinitionList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import javax.servlet.http.HttpSession;
import sapphire.SapphireException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;
import sapphire.xml.PropertyValue;

public class PropertyBuilder {
    public static final int TYPE_DATASOURCE = 5;
    public static final int TYPE_FIELD = 0;
    public static final int TYPE_GROUP = 1;
    public static final int TYPE_SECTION = 2;
    public static final int TYPE_LABEL = 3;
    public static final int TYPE_PAGE = 4;
    public static final int TYPE_ELEMENT = 6;
    public static final int TYPE_GENERIC = 7;
    public static final int FORM = 0;
    public static final int FIELD = 1;
    public static final int PARSER = 2;
    public static final int GENERIC = 3;

    public static void addPropertyMsg(String msg, StringBuffer buff, TranslationProcessor tp) {
        if (tp != null) {
            msg = tp.translate(msg);
        }
        buff.append(msg);
    }

    public static void clearSession(HttpSession session) {
        Enumeration names = session.getAttributeNames();
        while (names.hasMoreElements()) {
            String name = (String)names.nextElement();
            if (!name.startsWith("propertybuilder_")) continue;
            session.removeAttribute(name);
        }
    }

    private static void renderGenericEditor(int type, String editorId, String itemid, String propertyId, int collectionType, PropertyDefinition item, PropertyList props, boolean disabled, String groupid, boolean dynamic, StringBuffer buff, TranslationProcessor tp) {
        boolean expression;
        boolean bl = expression = item.getAttributes() != null && item.getAttributes().containsKey("expression") && item.getAttributes().get("expression").toString().equalsIgnoreCase("y");
        if (propertyId != null && propertyId.length() > 0) {
            String title = item.getTitle() == null || item.getTitle().length() == 0 ? propertyId : item.getTitle();
            switch (type) {
                case 0: {
                    PropertyBuilder.renderFormStringEditor(buff, tp, itemid, propertyId, title, props.getProperty(item.getId(), ""), item.getEditor().equalsIgnoreCase("longstringeditor"), collectionType, expression, item.getEditor().equalsIgnoreCase("readonlyeditor"), disabled, item.getHelp(), groupid, dynamic);
                    break;
                }
                case 1: {
                    PropertyBuilder.renderFieldStringEditor(buff, tp, propertyId, title, props.getProperty(item.getId(), ""), item.getEditor().equalsIgnoreCase("longstringeditor"), expression, item.getEditor().equalsIgnoreCase("readonlyeditor"), disabled, item.getHelp(), groupid, dynamic);
                    break;
                }
                case 2: {
                    PropertyBuilder.renderParserStringEditor(buff, tp, itemid, propertyId, title, props.getProperty(item.getId(), ""), item.getEditor().equalsIgnoreCase("longstringeditor"), expression, disabled);
                    break;
                }
                case 3: {
                    String onblur = "pb.propertyChange('" + propertyId + "'," + expression + "," + dynamic + ")";
                    String elementid = "pb_property_" + StringUtil.replaceAll(propertyId, ".", "_");
                    PropertyBuilder.renderEditor(buff, tp, editorId, propertyId, elementid, title, props.getProperty(item.getId(), ""), disabled, onblur, item.getHelp(), groupid);
                }
            }
        }
    }

    private static void renderGenericStringEditor(int type, String itemid, String propertyId, int collectionType, PropertyDefinition item, PropertyList props, boolean disabled, String groupid, boolean dynamic, StringBuffer buff, TranslationProcessor tp) {
        boolean expression;
        boolean bl = expression = item.getAttributes() != null && item.getAttributes().containsKey("expression") && item.getAttributes().get("expression").toString().equalsIgnoreCase("y");
        if (propertyId != null && propertyId.length() > 0) {
            String title = item.getTitle() == null || item.getTitle().length() == 0 ? propertyId : item.getTitle();
            switch (type) {
                case 0: {
                    PropertyBuilder.renderFormStringEditor(buff, tp, itemid, propertyId, title, props.getProperty(item.getId(), ""), item.getEditor().equalsIgnoreCase("longstringeditor"), collectionType, expression, item.getEditor().equalsIgnoreCase("readonlyeditor"), disabled, item.getHelp(), groupid, dynamic);
                    break;
                }
                case 1: {
                    PropertyBuilder.renderFieldStringEditor(buff, tp, propertyId, title, props.getProperty(item.getId(), ""), item.getEditor().equalsIgnoreCase("longstringeditor"), expression, item.getEditor().equalsIgnoreCase("readonlyeditor"), disabled, item.getHelp(), groupid, dynamic);
                    break;
                }
                case 2: {
                    PropertyBuilder.renderParserStringEditor(buff, tp, itemid, propertyId, title, props.getProperty(item.getId(), ""), item.getEditor().equalsIgnoreCase("longstringeditor"), expression, disabled);
                    break;
                }
                case 3: {
                    String onblur = "pb.propertyChange('" + propertyId + "'," + expression + "," + dynamic + ")";
                    String elementid = "pb_property_" + StringUtil.replaceAll(propertyId, ".", "_");
                    String onclick = "";
                    boolean showEditor = item.getEditor().equalsIgnoreCase("longstringeditor");
                    if (showEditor) {
                        onclick = "pb.editLongString('" + elementid + "', false);";
                    }
                    String gonclick = "";
                    if (expression) {
                        gonclick = "pb.editGroovy('" + elementid + "__EXPRESSION')";
                    }
                    PropertyBuilder.renderStringEditor(buff, tp, propertyId, elementid, title, props.getProperty(item.getId(), ""), disabled, item.getEditor().equalsIgnoreCase("readonlyeditor"), onblur, onclick, gonclick, showEditor || expression, item.getHelp(), groupid);
                }
            }
        }
    }

    private static void renderGenericURLEditor(int type, String itemid, String propertyId, int collectionType, PropertyDefinition item, PropertyList props, boolean disabled, String groupid, boolean dynamic, StringBuffer buff, TranslationProcessor tp) {
        if (propertyId != null && propertyId.length() > 0) {
            String title = item.getTitle() == null || item.getTitle().length() == 0 ? propertyId : item.getTitle();
            switch (type) {
                case 0: {
                    PropertyBuilder.renderFormURLEditor(buff, tp, itemid, propertyId, title, props.getProperty(item.getId(), ""), collectionType, disabled, item.getHelp(), groupid, dynamic);
                    break;
                }
                case 1: {
                    PropertyBuilder.renderFieldURLEditor(buff, tp, propertyId, title, props.getProperty(item.getId(), ""), disabled, item.getHelp(), groupid, dynamic);
                    break;
                }
                case 2: {
                    break;
                }
                case 3: {
                    String onblur = "pb.propertyChange('" + propertyId + "',false," + dynamic + ")";
                    String elementid = "pb_property_" + StringUtil.replaceAll(propertyId, ".", "_");
                    String onclick = "pb.editURL('" + elementid + "');";
                    PropertyBuilder.renderStringEditor(buff, tp, propertyId, elementid, title, props.getProperty(item.getId(), ""), disabled, false, onblur, onclick, "", false, item.getHelp(), groupid);
                }
            }
        }
    }

    private static void renderGenericValidationEditor(int type, String itemid, String propertyId, int collectionType, PropertyDefinition item, PropertyList props, boolean disabled, String groupid, boolean dynamic, boolean advanced, boolean hideMandatory, StringBuffer buff, TranslationProcessor tp) {
        if (propertyId != null && propertyId.length() > 0) {
            String title = item.getTitle() == null || item.getTitle().length() == 0 ? propertyId : item.getTitle();
            switch (type) {
                case 0: {
                    break;
                }
                case 1: {
                    break;
                }
                case 2: {
                    break;
                }
                case 3: {
                    String onblur = "pb.propertyChange('" + propertyId + "',false," + dynamic + ")";
                    String elementid = "pb_property_" + StringUtil.replaceAll(propertyId, ".", "_");
                    String onclick = "pb.editValidation('" + elementid + "', " + advanced + "," + hideMandatory + ");";
                    PropertyBuilder.renderStringEditor(buff, tp, propertyId, elementid, title, props.getProperty(item.getId(), ""), disabled, false, onblur, onclick, "", false, item.getHelp(), groupid);
                }
            }
        }
    }

    private static void renderGenericListEditor(int type, String itemid, String propertyId, int collectionType, PropertyDefinition item, PropertyList props, boolean disabled, String groupid, boolean dynamic, StringBuffer buff, TranslationProcessor tp, String connectionId, HttpSession session, HashMap bindingMap) {
        boolean expression = item.getAttributes() != null && item.getAttributes().containsKey("expression") && item.getAttributes().get("expression").toString().equalsIgnoreCase("y");
        String values = "";
        if (item.getAttributes() != null) {
            if (item.getAttributes().containsKey("values")) {
                values = item.getAttributes().get("values").toString();
                if (values.startsWith("$G{")) {
                    try {
                        values = GroovyUtil.getInstance(new ConnectionProcessor(connectionId).getConnectionInfo(connectionId)).evaluateSecure(values, bindingMap);
                    }
                    catch (SapphireException e) {
                        values = "Invalid Groovy Expression for values";
                    }
                }
            } else if (item.getAttributes().containsKey("reftypeid")) {
                String reftypeid = item.getAttributes().get("reftypeid").toString();
                Object ob = null;
                if (session != null) {
                    ob = session.getAttribute("propertybuilder_reftype_" + reftypeid);
                }
                if (ob != null) {
                    values = ob.toString();
                } else {
                    StringBuffer valuesbuff = new StringBuffer();
                    try {
                        DataSet ds = new QueryProcessor(connectionId).getRefTypeDataSet(reftypeid);
                        int rowCount = ds.getRowCount();
                        for (int i = 0; i < rowCount; ++i) {
                            if (i > 0) {
                                valuesbuff.append(";");
                            }
                            String refvalue = ds.getValue(i, "refvalueid", "");
                            String refdesc = ds.getValue(i, "refvaluedesc", refvalue);
                            String refdisplay = ds.getValue(i, "refdisplayvalue", refdesc);
                            valuesbuff.append(StringUtil.replaceAll(refvalue, ";", "#semicolon#")).append("=").append(StringUtil.replaceAll(refdisplay, ";", "#semicolon#"));
                        }
                    }
                    catch (Exception e) {
                        Logger.logWarn("Could not obtain items for reference type " + reftypeid + ".");
                    }
                    if (session != null) {
                        session.setAttribute("propertybuilder_reftype_" + reftypeid, (Object)valuesbuff.toString());
                    }
                    values = valuesbuff.toString();
                }
            } else if (item.getAttributes().containsKey("sdcid")) {
                String sdcid = item.getAttributes().get("sdcid").toString();
                String extendedwhere = item.getAttributes().containsKey("extendedwhere") ? item.getAttributes().get("extendedwhere").toString() : "";
                Object ob = null;
                if (session != null) {
                    ob = session.getAttribute("propertybuilder_" + sdcid + "_" + extendedwhere);
                }
                if (ob != null) {
                    values = ob.toString();
                } else {
                    StringBuffer valuesbuff = new StringBuffer();
                    try {
                        SDCProcessor sdcproc = new SDCProcessor(connectionId);
                        if (sdcproc != null && sdcproc != null) {
                            SDIData sdi;
                            SDIRequest req = new SDIRequest();
                            req.setSDCid(sdcid);
                            req.setRetrieve(true);
                            req.setQueryFrom(sdcproc.getProperty(sdcid, "tableid"));
                            req.setQueryWhere(extendedwhere);
                            req.setRequestItem("primary[" + sdcproc.getProperty(sdcid, "keycolid1") + "]");
                            req.setQueryOrderBy(sdcproc.getProperty(sdcid, "keycolid1"));
                            SDIProcessor sdiproc = new SDIProcessor(connectionId);
                            if (sdiproc != null && (sdi = sdiproc.getSDIData(req)) != null && sdi.getDataset("primary") != null) {
                                DataSet ds = sdi.getDataset("primary");
                                int rowCount = ds.getRowCount();
                                for (int i = 0; i < rowCount; ++i) {
                                    if (i > 0) {
                                        valuesbuff.append(";");
                                    }
                                    valuesbuff.append(ds.getValue(i, sdcproc.getProperty(sdcid, "keycolid1"), ""));
                                }
                            }
                        }
                        values = valuesbuff.toString();
                    }
                    catch (Exception e) {
                        Logger.logWarn("Could not obtain items for " + sdcid + ".");
                    }
                    if (session != null) {
                        session.setAttribute("propertybuilder_" + sdcid + "_" + extendedwhere, (Object)valuesbuff.toString());
                    }
                }
            }
        }
        if (propertyId != null && propertyId.length() > 0) {
            String title = item.getTitle() == null || item.getTitle().length() == 0 ? propertyId : item.getTitle();
            boolean edtiable = item.getAttributes().containsKey("editable") ? item.getAttributes().get("editable").toString().equalsIgnoreCase("y") : false;
            switch (type) {
                case 0: {
                    PropertyBuilder.renderFormListEditor(buff, tp, itemid, propertyId, title, values, props.getProperty(item.getId(), ""), collectionType, disabled, expression, item.getHelp(), groupid, edtiable, dynamic);
                    break;
                }
                case 1: {
                    PropertyBuilder.renderFieldListEditor(buff, tp, propertyId, title, values, props.getProperty(item.getId(), ""), expression, item.getHelp(), groupid, disabled, edtiable, dynamic);
                    break;
                }
                case 2: {
                    PropertyBuilder.renderParserListEditor(buff, tp, itemid, propertyId, title, values, props.getProperty(item.getId(), ""), disabled);
                    break;
                }
                case 3: {
                    String onchange = "pb.propertyChange('" + propertyId + "'," + expression + "," + dynamic + ")";
                    String elementid = "pb_property_" + StringUtil.replaceAll(propertyId, ".", "_");
                    String onclick = "pb.editGroovy( 'pb_property_" + propertyId + "__EXPRESSION' );";
                    PropertyBuilder.renderListEditor(buff, tp, propertyId, elementid, title, values, props.getProperty(item.getId(), ""), disabled, expression, edtiable, onchange, onclick, item.getHelp(), groupid);
                }
            }
        }
    }

    private static void renderGenericYesNoEditor(int type, String itemid, String propertyId, int collectionType, PropertyDefinition item, PropertyList props, boolean disabled, String groupid, boolean dynamic, StringBuffer buff, TranslationProcessor tp) {
        boolean expression;
        boolean bl = expression = item.getAttributes() != null && item.getAttributes().containsKey("expression") && item.getAttributes().get("expression").toString().equalsIgnoreCase("y");
        if (propertyId != null && propertyId.length() > 0) {
            String title = item.getTitle() == null || item.getTitle().length() == 0 ? propertyId : item.getTitle();
            switch (type) {
                case 0: {
                    PropertyBuilder.renderFormYesNoEditor(buff, tp, itemid, propertyId, title, props.getProperty(item.getId(), ""), expression, collectionType, disabled, item.getHelp(), groupid, dynamic);
                    break;
                }
                case 1: {
                    PropertyBuilder.renderFieldYesNoEditor(buff, tp, propertyId, title, props.getProperty(item.getId(), ""), expression, item.getHelp(), groupid, disabled, dynamic);
                    break;
                }
                case 2: {
                    PropertyBuilder.renderParserYesNoEditor(buff, tp, itemid, propertyId, title, props.getProperty(item.getId(), ""), expression, disabled);
                    break;
                }
                case 3: {
                    String onchange = "pb.propertyChange('" + propertyId + "', " + expression + "," + dynamic + ")";
                    String elementid = "pb_property_" + StringUtil.replaceAll(propertyId, ".", "_");
                    String onclick = "pb.editGroovy( 'pb_property_" + propertyId + "__EXPRESSION' );";
                    PropertyBuilder.renderYesNoEditor(buff, tp, propertyId, elementid, title, props.getProperty(item.getId(), ""), expression, disabled, onchange, onclick, item.getHelp(), groupid);
                }
            }
        }
    }

    public static PropertyListCollection getCollectionStructure(PropertyDefinition item, HashMap preview) {
        PropertyListCollection inputs = new PropertyListCollection();
        for (Object key : item.getPropertyDefinitionList()) {
            String editor;
            if (key == null) continue;
            PropertyDefinition def = (PropertyDefinition)key;
            PropertyList input = new PropertyList();
            String id = def.getId();
            String label = def.getTitle() == null || def.getTitle().length() == 0 ? def.getId() : def.getTitle();
            input.setProperty("name", id);
            input.setProperty("label", label);
            if (preview.size() < 2) {
                preview.put(id, label);
            }
            if ((editor = def.getEditor()).equalsIgnoreCase("StringEditor")) {
                if (def.getAttributes().get("expression") != null && def.getAttributes().get("expression").toString().equalsIgnoreCase("Y")) {
                    input.setProperty("type", "groovytext");
                } else {
                    input.setProperty("type", "text");
                }
            } else if (editor.equalsIgnoreCase("ListEditor")) {
                if (def.getAttributes().get("expression") != null && def.getAttributes().get("expression").toString().equalsIgnoreCase("Y") && def.getAttributes().get("editable") != null && def.getAttributes().get("editable").toString().equalsIgnoreCase("Y")) {
                    input.setProperty("type", "editablegroovyselect");
                } else if (def.getAttributes().get("expression") != null && def.getAttributes().get("expression").toString().equalsIgnoreCase("Y")) {
                    input.setProperty("type", "groovyselect");
                } else if (def.getAttributes().get("editable") != null && def.getAttributes().get("editable").toString().equalsIgnoreCase("Y")) {
                    input.setProperty("type", "editableselect");
                } else {
                    input.setProperty("type", "select");
                }
                input.setProperty("items", def.getValues());
            }
            input.setProperty("value", "");
            inputs.add(input);
        }
        return inputs;
    }

    private static void renderGenericCollectionEditor(int type, String itemid, String propertyId, int collectionType, PropertyDefinition item, PropertyList props, boolean disabled, String groupid, boolean dynamic, StringBuffer buff, TranslationProcessor tp) {
        if (propertyId != null && propertyId.length() > 0) {
            String title = item.getTitle() == null || item.getTitle().length() == 0 ? propertyId : item.getTitle();
            String single = item.getAttributes() != null && item.getAttributes().containsKey("labelsingular") && item.getAttributes().get("labelsingular").toString().length() >= 0 ? item.getAttributes().get("labelsingular").toString() : propertyId;
            String plural = item.getAttributes() != null && item.getAttributes().containsKey("labelplural") && item.getAttributes().get("labelplural").toString().length() >= 0 ? item.getAttributes().get("labelplural").toString() : single;
            HashMap preview = new HashMap();
            PropertyList structure = new PropertyList();
            structure.setProperty("inputs", PropertyBuilder.getCollectionStructure(item, preview));
            switch (type) {
                case 0: {
                    PropertyBuilder.renderFormCollectionEditor(buff, tp, itemid, propertyId, title, single, plural, props.getCollection(item.getId()), collectionType, disabled, structure, groupid, dynamic);
                    break;
                }
                case 1: {
                    PropertyBuilder.renderFieldCollectionEditor(buff, tp, propertyId, title, single, plural, props.getCollection(item.getId()), structure, groupid, disabled, dynamic);
                    break;
                }
                case 2: {
                    PropertyBuilder.renderParserCollectionEditor(buff, tp, itemid, propertyId, title, single, plural, props.getCollection(item.getId()), disabled, structure);
                    break;
                }
                case 3: {
                    String elementid = "pb_property_" + StringUtil.replaceAll(propertyId, ".", "_");
                    String mouseover = "pb.showCollectionPreview('" + elementid + "');";
                    String onclick = "pb.editCollection('" + propertyId + "','" + title + "',sapphire.util.propertyList.create(" + StringUtil.replaceAll(structure.toJSONString(false), "\"", "'") + ")," + dynamic + ");";
                    PropertyBuilder.renderCollectionEditor(buff, tp, propertyId, elementid, title, single, plural, disabled, props.getCollection(item.getId()), mouseover, onclick, preview, groupid);
                }
            }
        }
    }

    private static boolean evalExpression(String expression, PropertyList props) {
        String[] tokens = StringUtil.getExpressionTokens(expression);
        for (int i = 0; i < tokens.length; ++i) {
            String prop = tokens[i];
            PropertyList use = props;
            while (prop.contains(".")) {
                String proplist = prop.substring(0, prop.indexOf("."));
                prop = prop.substring(prop.indexOf(".") + 1);
                use = props.getPropertyList(proplist);
                if (use != null) continue;
                break;
            }
            if (use == null) continue;
            String val = use.getProperty(prop, "null");
            expression = StringUtil.replaceAll(expression, "[" + tokens[i] + "]", val, false);
        }
        String[] orparts = StringUtil.split(expression, "|", false);
        boolean oreval = false;
        for (int or = 0; or < orparts.length; ++or) {
            String orpart = orparts[or];
            String[] andparts = StringUtil.split(orpart, "&", false);
            boolean andeval = false;
            for (int and = 0; and < andparts.length; ++and) {
                String[] parts;
                String andpart = andparts[and];
                if (andpart.endsWith("=")) {
                    andpart = andpart + "null";
                }
                if (andpart.contains("!=")) {
                    parts = andpart.split("!=");
                    andeval = parts.length == 1 ? parts[0].length() != 0 : !parts[0].equalsIgnoreCase(parts[1]);
                } else if (andpart.contains("=")) {
                    parts = andpart.split("=");
                    andeval = parts.length == 1 ? parts[0].length() == 0 : parts[0].equalsIgnoreCase(parts[1]);
                } else {
                    boolean bl = andeval = andpart.equalsIgnoreCase("Y") || andpart.equalsIgnoreCase("true");
                }
                if (!andeval) break;
            }
            if (andeval) {
                oreval = true;
                break;
            }
            oreval = false;
        }
        return oreval;
    }

    private static String renderPropertyList(int type, String itemid, String parentitemid, String parentproperty, int collectionType, PropertyDefinitionList itemsdeflist, PropertyList props, PropertyList itemprops, String currentpropertygroup, boolean disabled, boolean multiple, LinkedHashMap groupedbuffers, StringBuffer headbuff, String connectionId, PropertyList userConfig, TranslationProcessor tp, HttpSession session, HashMap bindingMap) {
        String idprop = "";
        switch (collectionType) {
            case 0: {
                PropertyList field;
                PropertyListCollection fields = props.getCollection("fields");
                if (fields != null && (field = fields.find("fieldid", itemid)) != null) {
                    bindingMap.put("field", field);
                }
                idprop = "fieldid";
                break;
            }
            case 5: {
                PropertyList datasource;
                PropertyListCollection datasources = props.getCollection("datasources");
                if (datasources != null && (datasource = datasources.find("datasourceid", itemid)) != null) {
                    bindingMap.put("datasource", datasource);
                }
                idprop = "datasourceid";
                break;
            }
            case 6: {
                idprop = "elementid";
                break;
            }
            case 1: {
                idprop = "groupid";
                break;
            }
            case 2: {
                idprop = "datasourceid";
                break;
            }
            case 3: {
                idprop = "fieldid.labelid";
                break;
            }
            case 4: {
                idprop = "pageid";
                break;
            }
            case 7: {
                idprop = "";
            }
        }
        if (collectionType != 7) {
            if (!bindingMap.containsKey("field")) {
                bindingMap.put("field", new PropertyList());
            }
            if (!bindingMap.containsKey("datasource")) {
                bindingMap.put("datasource", new PropertyList());
            }
            if (!props.containsKey("form")) {
                props.setProperty("form", new PropertyList());
            }
            if (!props.containsKey("formlet")) {
                props.setProperty("formlet", new PropertyList());
            }
        }
        bindingMap.put("props", props);
        Comparator<String> ALPHABETICAL_ORDER = new Comparator<String>(){

            @Override
            public int compare(String str1, String str2) {
                int res = String.CASE_INSENSITIVE_ORDER.compare(str1, str2);
                if (res == 0) {
                    res = str1.compareTo(str2);
                }
                return res;
            }
        };
        for (Object key : itemsdeflist) {
            String fullpropertyid;
            String propertyId;
            String groupid;
            Map<Object, StringBuffer> ordered;
            StringBuffer currentbuff;
            String propertygroup;
            boolean exclude;
            if (key == null) continue;
            PropertyDefinition item = (PropertyDefinition)key;
            String visible = item.getAttributes() != null && item.getAttributes().containsKey("visible") ? item.getAttributes().get("visible").toString() : "Y";
            boolean hasdependency = (item.getAttributes() != null && item.getAttributes().containsKey("hasdependents") ? item.getAttributes().get("hasdependents").toString() : "N").equalsIgnoreCase("Y");
            if (visible.equalsIgnoreCase("n") || visible.equalsIgnoreCase("false")) {
                exclude = true;
            } else if (visible.startsWith("$G{")) {
                try {
                    String result = GroovyUtil.getInstance(new ConnectionProcessor(connectionId).getConnectionInfo(connectionId)).evaluateSecure(visible, bindingMap);
                    exclude = !result.equalsIgnoreCase("Y") && !result.equalsIgnoreCase("true");
                }
                catch (SapphireException e) {
                    exclude = false;
                }
            } else {
                exclude = false;
            }
            boolean usedisabled = disabled;
            if (!usedisabled) {
                String evaldisabled;
                String string = evaldisabled = item.getAttributes() != null && item.getAttributes().containsKey("disabled") ? item.getAttributes().get("disabled").toString() : "N";
                if (evaldisabled.equalsIgnoreCase("y") || evaldisabled.equalsIgnoreCase("true")) {
                    usedisabled = true;
                } else if (evaldisabled.contains("[")) {
                    usedisabled = PropertyBuilder.evalExpression(evaldisabled, itemprops);
                }
            }
            if (exclude) continue;
            if (userConfig != null && userConfig.getProperty("propertybuilder_groupby", "").equalsIgnoreCase("none")) {
                propertygroup = "";
            } else if (userConfig != null && userConfig.getProperty("propertybuilder_groupby", "").equalsIgnoreCase("used")) {
                propertygroup = itemprops.getProperty(item.getId(), null) != null ? (tp != null ? tp.translate("Used") : "Used") : (tp != null ? tp.translate("Unused") : "Unused");
            } else if (userConfig != null && userConfig.getProperty("propertybuilder_groupby", "").equalsIgnoreCase("alphabetical")) {
                propertygroup = item.getType().equalsIgnoreCase("Simple") || item.getType().equalsIgnoreCase("Collection") ? (item.getTitle().length() > 0 ? (item.getTitle().charAt(0) + "").toUpperCase() + " " + (tp != null ? tp.translate("Properties") : "Properties") : (item.getId().length() > 0 ? (item.getId().charAt(0) + "").toUpperCase() + " " + (tp != null ? tp.translate("Properties") : "Properties") : "")) : "";
            } else if (userConfig != null && userConfig.getProperty("propertybuilder_groupby", "").equalsIgnoreCase("type")) {
                String ed;
                propertygroup = item.getType().equalsIgnoreCase("Simple") ? ((ed = item.getEditor()).equalsIgnoreCase("StringEditor") ? (tp != null ? tp.translate("String") : "String") : (ed.equalsIgnoreCase("LongStringEditor") ? (tp != null ? tp.translate("Long String") : "Long String") : (ed.equalsIgnoreCase("YesNoEditor") ? (tp != null ? tp.translate("Yes No") : "Yes No") : (ed.equalsIgnoreCase("ListEditor") ? (tp != null ? tp.translate("List") : "List") : (ed.equalsIgnoreCase("LinkEditor") ? (tp != null ? tp.translate("Link") : "Link") : (tp != null ? tp.translate("Other") : "Other")))))) : (item.getType().equalsIgnoreCase("Collection") ? (tp != null ? tp.translate("Collection") : "Collection") : "");
            } else {
                String string = propertygroup = item.getAttributes() != null && item.getAttributes().containsKey("propertygroup") ? item.getAttributes().get("propertygroup").toString() : "";
            }
            if (propertygroup.length() == 0) {
                propertygroup = currentpropertygroup.length() == 0 ? "Properties" : currentpropertygroup;
            }
            if (groupedbuffers.size() == 0 || !groupedbuffers.containsKey(propertygroup)) {
                currentbuff = new StringBuffer();
                ordered = userConfig != null && userConfig.getProperty("propertybuilder_groupby", "").equalsIgnoreCase("alphabetical") ? new TreeMap(ALPHABETICAL_ORDER) : new LinkedHashMap();
                ordered.put(item.getTitle().length() > 0 ? item.getTitle() : item.getId(), currentbuff);
                currentpropertygroup = propertygroup;
                groupedbuffers.put(propertygroup, ordered);
                groupid = propertygroup.replaceAll("[^a-zA-Z 0-9]", "_").replaceAll("\\s", "_");
                StringBuffer titleBuff = new StringBuffer();
                PropertyBuilder.renderPropTitle(titleBuff, headbuff, tp, propertygroup, groupid, userConfig);
                ordered.put("__title", titleBuff);
            } else {
                currentpropertygroup = propertygroup;
                ordered = (Map)groupedbuffers.get(propertygroup);
                currentbuff = new StringBuffer();
                ordered.put(item.getTitle().length() > 0 ? item.getTitle() : item.getId(), currentbuff);
                groupid = currentpropertygroup.replaceAll("[^a-zA-Z 0-9]", "_").replaceAll("\\s", "_");
            }
            if ((propertyId = item.getId()).length() <= 0) continue;
            String fullitemid = parentitemid.length() > 0 ? parentitemid + "." + itemid : itemid;
            String string = fullpropertyid = parentproperty.length() > 0 ? parentproperty + "." + propertyId : propertyId;
            if (item.getType().equalsIgnoreCase("simple")) {
                if (multiple && fullpropertyid.equals(idprop)) {
                    usedisabled = true;
                }
                if (item.getEditor().equalsIgnoreCase("stringeditor") || item.getEditor().equalsIgnoreCase("longstringeditor") || item.getEditor().equalsIgnoreCase("readonlyeditor")) {
                    PropertyBuilder.renderGenericStringEditor(type, fullitemid, fullpropertyid, collectionType, item, itemprops, usedisabled, groupid, hasdependency, currentbuff, tp);
                    continue;
                }
                if (item.getEditor().equalsIgnoreCase("listeditor")) {
                    PropertyBuilder.renderGenericListEditor(type, fullitemid, fullpropertyid, collectionType, item, itemprops, usedisabled, groupid, hasdependency, currentbuff, tp, connectionId, session, bindingMap);
                    continue;
                }
                if (item.getEditor().equalsIgnoreCase("yesnoeditor")) {
                    PropertyBuilder.renderGenericYesNoEditor(type, fullitemid, fullpropertyid, collectionType, item, itemprops, usedisabled, groupid, hasdependency, currentbuff, tp);
                    continue;
                }
                if (item.getEditor().equalsIgnoreCase("linkeditor")) {
                    PropertyBuilder.renderGenericURLEditor(type, fullitemid, fullpropertyid, collectionType, item, itemprops, usedisabled, groupid, hasdependency, currentbuff, tp);
                    continue;
                }
                if (item.getEditor().equalsIgnoreCase("validationeditor")) {
                    boolean advanced = item.getAttributes().containsKey("advanced") ? item.getAttributes().get("advanced").toString().equalsIgnoreCase("Y") : false;
                    boolean hideMandatory = item.getAttributes().containsKey("hidemandatory") ? item.getAttributes().get("hidemandatory").toString().equalsIgnoreCase("Y") : false;
                    PropertyBuilder.renderGenericValidationEditor(type, fullitemid, fullpropertyid, collectionType, item, itemprops, usedisabled, groupid, hasdependency, advanced, hideMandatory, currentbuff, tp);
                    continue;
                }
                if (item.getEditor().length() <= 0) continue;
                PropertyBuilder.renderGenericEditor(type, item.getEditor(), fullitemid, fullpropertyid, collectionType, item, itemprops, usedisabled, groupid, hasdependency, currentbuff, tp);
                continue;
            }
            if (item.getType().equalsIgnoreCase("collection") && !multiple) {
                PropertyBuilder.renderGenericCollectionEditor(type, fullitemid, fullpropertyid, collectionType, item, itemprops, usedisabled, groupid, hasdependency, currentbuff, tp);
                continue;
            }
            if (!item.getType().equalsIgnoreCase("propertylist")) continue;
            PropertyList sublist = itemprops.getPropertyList(item.getId());
            if (sublist == null) {
                sublist = new PropertyList();
                itemprops.setProperty(item.getId(), sublist);
            }
            currentpropertygroup = PropertyBuilder.renderPropertyList(type, itemid, "", propertyId, collectionType, item.getPropertyDefinitionList(), props, sublist, currentpropertygroup, usedisabled, multiple, groupedbuffers, headbuff, connectionId, userConfig, tp, session, bindingMap);
        }
        return currentpropertygroup;
    }

    public static void renderProperties(int type, String itemid, int collectionType, PropertyDefinitionList propertydeflist, PropertyList props, boolean disabled, StringBuffer buff, String connectionId, PropertyList userConfig, TranslationProcessor tp, HttpSession session, HashMap bindingMap) {
        String idfield;
        String identifier;
        String parentitemid = itemid.contains(".") ? itemid.substring(0, itemid.lastIndexOf(".")) : "";
        itemid = itemid.contains(".") ? itemid.substring(itemid.lastIndexOf(".") + 1) : itemid;
        String parentidentifier = "";
        switch (collectionType) {
            case 5: {
                identifier = "datasources";
                idfield = "datasourceid";
                break;
            }
            case 6: {
                identifier = "elements";
                idfield = "elementid";
                break;
            }
            case 0: {
                identifier = "fields";
                idfield = "fieldid";
                break;
            }
            case 1: {
                identifier = "groups";
                idfield = "groupid";
                break;
            }
            case 3: {
                identifier = "labels";
                idfield = "labelid";
                break;
            }
            case 4: {
                identifier = "pages";
                idfield = "pageid";
                break;
            }
            case 2: {
                identifier = "sections";
                idfield = "sectionid";
                break;
            }
            case 7: {
                identifier = "";
                idfield = "";
                break;
            }
            default: {
                identifier = "fields";
                idfield = "fieldid";
            }
        }
        PropertyList itemprops = null;
        PropertyDefinitionList itemsdeflist = null;
        if (identifier.length() > 0) {
            PropertyDefinition itemsdef = propertydeflist.getPropertyDef(identifier);
            if (itemsdef != null) {
                itemsdeflist = itemsdef.getPropertyDefinitionList();
                if (itemsdeflist != null && itemsdeflist.size() > 0) {
                    if (props.containsKey(idfield)) {
                        itemprops = props;
                    } else if (props.containsKey(identifier)) {
                        PropertyListCollection items = props.getCollection(identifier);
                        if (items != null) {
                            itemprops = items.find(idfield, itemid);
                        } else {
                            PropertyBuilder.addPropertyMsg("Incorrect Property values found", buff, tp);
                        }
                    } else {
                        itemprops = props;
                    }
                } else {
                    PropertyBuilder.addPropertyMsg("Incorrect Property definition found", buff, tp);
                }
            } else {
                PropertyBuilder.addPropertyMsg("No Property definition found", buff, tp);
            }
        } else {
            itemprops = props;
            itemsdeflist = propertydeflist;
        }
        if (itemprops != null) {
            if (itemsdeflist != null) {
                PropertyBuilder.renderStart(buff);
                LinkedHashMap groupedbuffers = new LinkedHashMap();
                StringBuffer headbuff = new StringBuffer();
                PropertyBuilder.renderPropertyList(type, itemid, parentitemid, parentidentifier, collectionType, itemsdeflist, props, itemprops, "", disabled, itemid.contains(";"), groupedbuffers, headbuff, connectionId, userConfig, tp, session, bindingMap);
                if (groupedbuffers != null && groupedbuffers.size() > 0) {
                    buff.append(headbuff);
                    ArrayList keys = new ArrayList(groupedbuffers.keySet());
                    if (userConfig != null && userConfig.getProperty("propertybuilder_groupby", "").equalsIgnoreCase("alphabetical")) {
                        Collections.sort(keys);
                    } else {
                        keys = new ArrayList(groupedbuffers.keySet());
                    }
                    Iterator keyit = keys.iterator();
                    while (keyit.hasNext()) {
                        String propertygroup = keyit.next().toString();
                        Map map = (Map)groupedbuffers.get(propertygroup);
                        buff.append((StringBuffer)map.get("__title"));
                        for (Map.Entry entry : map.entrySet()) {
                            if (((String)entry.getKey()).equalsIgnoreCase("__title")) continue;
                            buff.append((StringBuffer)entry.getValue());
                        }
                    }
                }
                PropertyBuilder.renderEnd(buff);
            } else {
                PropertyBuilder.addPropertyMsg("No Property definition found", buff, tp);
            }
        } else {
            PropertyBuilder.addPropertyMsg("No Property values found", buff, tp);
        }
    }

    public static void renderProperties(PropertyDefinitionList propertydeflist, PropertyList props, boolean disabled, StringBuffer buff, String connectionId, PropertyList userConfig, TranslationProcessor tp, HttpSession session, HashMap bindingMap) {
        PropertyBuilder.renderProperties(3, "", 7, propertydeflist, props, disabled, buff, connectionId, userConfig, tp, session, bindingMap);
    }

    public static void renderPropTitle(StringBuffer sb, TranslationProcessor tp, String title) {
        PropertyBuilder.renderPropTitle(sb, tp, title, "", null);
    }

    public static void renderPropTitle(StringBuffer sb, TranslationProcessor tp, String title, String groupid) {
        PropertyBuilder.renderPropTitle(sb, tp, title, groupid, null);
    }

    public static void renderPropTitle(StringBuffer sb, TranslationProcessor tp, String title, String groupid, PropertyList userConfig) {
        PropertyBuilder.renderPropTitle(sb, null, tp, title, groupid, userConfig);
    }

    public static void renderPropTitle(StringBuffer htmlBuffer, StringBuffer headbuffer, TranslationProcessor tp, String title, String groupid, PropertyList userConfig) {
        if (tp != null) {
            title = tp.translate(title);
        }
        htmlBuffer.append("<tr>");
        if (groupid == null || groupid.length() == 0) {
            htmlBuffer.append("<td class=\"form_prop_header\" colspan=\"2\">").append(title).append("</td>");
        } else {
            String display = "table-row";
            String img = "minus";
            if (userConfig != null && userConfig.getProperty("propertybuilder_group_" + groupid, "Y").equalsIgnoreCase("N")) {
                display = "none";
                img = "plus";
            }
            if (headbuffer != null) {
                headbuffer.append("<style id=\"propstyle_").append(groupid).append("\">TR.propgroup_").append(groupid).append("{display:").append(display).append(";}</style>");
            } else {
                htmlBuffer.append("<style id=\"propstyle_").append(groupid).append("\">TR.propgroup_").append(groupid).append("{display:").append(display).append(";}</style>");
            }
            htmlBuffer.append("<td class=\"form_prop_header\" style=\"cursor: pointer;\" colspan=\"2\" id=\"grouphead_").append(groupid).append("\" onclick=\"if(typeof(propToggleGroup)!='undefined')propToggleGroup('").append(groupid).append("')\">").append("<img id=\"groupimg_").append(groupid).append("\" src=\"WEB-CORE/elements/images/").append(img).append(".gif\">&nbsp;").append(title).append("</td>");
        }
        htmlBuffer.append("</tr>");
    }

    public static void renderStart(StringBuffer sb) {
        sb.append("<style>.form_prop_value input, .form_prop_value textarea{box-sizing:border-box;}</style>");
        sb.append("<table style=\"width:100%;table-layout:fixed;\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">");
        sb.append("<thead>");
        sb.append("<tr style=\"display:table-row;height:1px;\">");
        sb.append("<td class=\"form_prop_title\"></td>");
        sb.append("<td class=\"form_prop_value\"></td>");
        sb.append("</tr>");
        sb.append("</thead>");
        sb.append("<tbody>");
    }

    public static void renderEnd(StringBuffer sb) {
        sb.append("</tbody>");
        sb.append("</table>");
    }

    public static void renderFormCollectionEditor(StringBuffer sb, TranslationProcessor tp, String fieldId, String editorName, String editorTitle, String singleitemtext, String pluralitemtext, PropertyListCollection collection, int collectionType) {
        PropertyBuilder.renderFormCollectionEditor(sb, tp, fieldId, editorName, editorTitle, singleitemtext, pluralitemtext, collection, collectionType, false, new PropertyList(), "");
    }

    public static void renderFormCollectionEditor(StringBuffer sb, TranslationProcessor tp, String fieldId, String editorName, String editorTitle, String singleitemtext, String pluralitemtext, PropertyListCollection collection, int collectionType, boolean disabled, PropertyList structure, String groupid) {
        PropertyBuilder.renderFormCollectionEditor(sb, tp, fieldId, editorName, editorTitle, singleitemtext, pluralitemtext, collection, collectionType, disabled, structure, groupid, true);
    }

    public static void renderFormCollectionEditor(StringBuffer sb, TranslationProcessor tp, String fieldId, String editorName, String editorTitle, String singleitemtext, String pluralitemtext, PropertyListCollection collection, int collectionType, boolean disabled, PropertyList structure, String groupid, boolean dynamic) {
        String onclick;
        String elementid = "formbuilder_property_" + StringUtil.replaceAll(editorName, ".", "_");
        String mouseover = "formBuilder.showCollectionPreview('" + elementid + "');";
        switch (collectionType) {
            case 1: {
                onclick = "formBuilder.editCollection('groups','groupid','" + fieldId + "','" + editorName + "','" + editorTitle + "',sapphire.util.propertyList.create(" + StringUtil.replaceAll(structure.toJSONString(false), "\"", "'") + ")," + dynamic + ");";
                break;
            }
            case 0: {
                onclick = "formBuilder.editCollection('fields','fieldid','" + fieldId + "','" + editorName + "','" + editorTitle + "',sapphire.util.propertyList.create(" + StringUtil.replaceAll(structure.toJSONString(false), "\"", "'") + ")," + dynamic + ");";
                break;
            }
            case 5: {
                onclick = "formBuilder.editCollection('datasources','datasourceid','" + fieldId + "','" + editorName + "','" + editorTitle + "',sapphire.util.propertyList.create(" + StringUtil.replaceAll(structure.toJSONString(false), "\"", "'") + ")," + dynamic + ");";
                break;
            }
            case 2: {
                onclick = "formBuilder.editCollection('sections','sectionid','" + fieldId + "','" + editorName + "','" + editorTitle + "',sapphire.util.propertyList.create(" + StringUtil.replaceAll(structure.toJSONString(false), "\"", "'") + ")," + dynamic + ");";
                break;
            }
            case 3: {
                onclick = "formBuilder.editCollection('labels','labelid','" + fieldId + "','" + editorName + "','" + editorTitle + "',sapphire.util.propertyList.create(" + StringUtil.replaceAll(structure.toJSONString(false), "\"", "'") + ")," + dynamic + ");";
                break;
            }
            case 4: {
                onclick = "formBuilder.editCollection('pages','pageid','" + fieldId + "','" + editorName + "','" + editorTitle + "',sapphire.util.propertyList.create(" + StringUtil.replaceAll(structure.toJSONString(false), "\"", "'") + ")," + dynamic + ");";
                break;
            }
            default: {
                onclick = "formBuilder.editCollection('fields','fieldid','" + fieldId + "','" + editorName + "','" + editorTitle + "',sapphire.util.propertyList.create(" + StringUtil.replaceAll(structure.toJSONString(false), "\"", "'") + ")," + dynamic + ");";
            }
        }
        HashMap<String, String> properties = new HashMap<String, String>();
        if (editorName.equalsIgnoreCase("validation")) {
            switch (collectionType) {
                case 1: {
                    properties.put("Operation", "operation");
                    properties.put("Operator", "operator");
                    properties.put("Value 1", "value1");
                    properties.put("Value 2", "value2");
                    properties.put("Error Message", "message");
                    break;
                }
                case 0: {
                    properties.put("Operation", "operation");
                    properties.put("Operator", "operator");
                    properties.put("Value 1", "value1");
                    properties.put("Value 2", "value2");
                    properties.put("Error Message", "message");
                    break;
                }
            }
        } else if (editorName.equalsIgnoreCase("attributes")) {
            switch (collectionType) {
                case 1: {
                    properties.put("Id", "attributeid");
                    properties.put("Value", "attributevalue");
                    break;
                }
                case 0: {
                    properties.put("Id", "attributeid");
                    properties.put("Value", "attributevalue");
                    break;
                }
            }
        }
        PropertyBuilder.renderCollectionEditor(sb, tp, editorName, elementid, editorTitle, singleitemtext, pluralitemtext, disabled, collection, mouseover, onclick, properties, groupid);
    }

    public static void renderParserCollectionEditor(StringBuffer sb, TranslationProcessor tp, String fieldId, String editorName, String editorTitle, String singleitemtext, String pluralitemtext, PropertyListCollection collection, boolean disabled) {
        PropertyBuilder.renderParserCollectionEditor(sb, tp, fieldId, editorName, editorTitle, singleitemtext, pluralitemtext, collection, disabled, new PropertyList());
    }

    public static void renderParserCollectionEditor(StringBuffer sb, TranslationProcessor tp, String fieldId, String editorName, String editorTitle, String singleitemtext, String pluralitemtext, PropertyListCollection collection, boolean disabled, PropertyList structure) {
        String elementid = "ssparser_property_" + StringUtil.replaceAll(editorName, ".", "_");
        String mouseover = "ssParser.showCollectionPreview('" + elementid + "');";
        String onclick = "ssParser.editCollection('fields','fieldid','" + fieldId + "','" + editorName + "');";
        HashMap<String, String> properties = new HashMap<String, String>();
        if (editorName.equalsIgnoreCase("validation")) {
            properties.put("Operation", "operation");
            properties.put("Operator", "operator");
            properties.put("Value 1", "value1");
            properties.put("Value 2", "value2");
            properties.put("Error Message", "message");
        } else if (editorName.equalsIgnoreCase("attributes")) {
            properties.put("Id", "attributeid");
            properties.put("Value", "attributevalue");
        }
        PropertyBuilder.renderCollectionEditor(sb, tp, editorName, elementid, editorTitle, singleitemtext, pluralitemtext, disabled, collection, mouseover, onclick, properties, "");
    }

    public static void renderFieldCollectionEditor(StringBuffer sb, TranslationProcessor tp, String editorName, String editorTitle, String singleitemtext, String pluralitemtext, PropertyListCollection collection) {
        PropertyBuilder.renderFieldCollectionEditor(sb, tp, editorName, editorTitle, singleitemtext, pluralitemtext, collection, new PropertyList(), "");
    }

    public static void renderFieldCollectionEditor(StringBuffer sb, TranslationProcessor tp, String editorName, String editorTitle, String singleitemtext, String pluralitemtext, PropertyListCollection collection, PropertyList structure, String groupid) {
        PropertyBuilder.renderFieldCollectionEditor(sb, tp, editorName, editorTitle, singleitemtext, pluralitemtext, collection, new PropertyList(), groupid, false);
    }

    public static void renderFieldCollectionEditor(StringBuffer sb, TranslationProcessor tp, String editorName, String editorTitle, String singleitemtext, String pluralitemtext, PropertyListCollection collection, PropertyList structure, String groupid, boolean disabled) {
        PropertyBuilder.renderFieldCollectionEditor(sb, tp, editorName, editorTitle, singleitemtext, pluralitemtext, collection, new PropertyList(), groupid, false);
    }

    public static void renderFieldCollectionEditor(StringBuffer sb, TranslationProcessor tp, String editorName, String editorTitle, String singleitemtext, String pluralitemtext, PropertyListCollection collection, PropertyList structure, String groupid, boolean disabled, boolean dynamic) {
        String elementid = "fieldbuilder_property_" + StringUtil.replaceAll(editorName, ".", "_");
        String mouseover = "fieldBuilder.showCollectionPreview('" + elementid + "');";
        String onclick = "fieldBuilder.editCollection('" + editorName + "'," + dynamic + ");";
        HashMap<String, String> properties = new HashMap<String, String>();
        if (editorName.equalsIgnoreCase("validation")) {
            properties.put("Operation", "operation");
            properties.put("Operator", "operator");
            properties.put("Value 1", "value1");
            properties.put("Value 2", "value2");
            properties.put("Error Message", "message");
        } else if (editorName.equalsIgnoreCase("attributes")) {
            properties.put("Id", "attributeid");
            properties.put("Value", "attributevalue");
        }
        PropertyBuilder.renderCollectionEditor(sb, tp, editorName, elementid, editorTitle, singleitemtext, pluralitemtext, disabled, collection, mouseover, onclick, properties, groupid);
    }

    public static void renderCollectionEditor(StringBuffer sb, TranslationProcessor tp, String editorName, String editorid, String editorTitle, String singleitemtext, String pluralitemtext, boolean disabled, PropertyListCollection collection, String mousemove, String buttonclick, HashMap previewProps, String groupid) {
        String itemno = "No";
        if (tp != null) {
            editorTitle = tp.translate(editorTitle);
            singleitemtext = tp.translate(singleitemtext);
            pluralitemtext = tp.translate(pluralitemtext);
            itemno = tp.translate(itemno);
        }
        String itemtext = collection == null || collection.size() == 0 ? itemno + " " + pluralitemtext : (collection.size() == 1 ? "1 " + singleitemtext : collection.size() + " " + pluralitemtext);
        if (groupid != null && groupid.length() > 0) {
            sb.append("<tr class=\"propgroup_").append(groupid).append("\">");
        } else {
            sb.append("<tr>");
        }
        sb.append("<td class=\"form_prop_title\">").append(editorTitle).append("</td>");
        sb.append("<td class=\"form_prop_value\">");
        sb.append("<table cellpadding=\"0\" border=\"0\" cellspacing=\"0\" style=\"table-layout:fixed;width:100%;\"><tbody><tr><td onmouseover=\"").append(mousemove).append("\"\" class=\"form_readonlytext_wrapper\">");
        sb.append("<div class=\"form_readonlytext form_readonlytext_nonie\">&nbsp;").append(itemtext).append("</div>");
        sb.append("</td>");
        if (!disabled) {
            sb.append("<td style=\"width:20px;\">");
            sb.append("<button style=\"height:21px;width:20px;padding-left:2px;\" onclick=\"").append(buttonclick).append("\">...</button>");
            sb.append("</td>");
        }
        sb.append("</td></tr></tbody></table>");
        if (previewProps.size() > 0) {
            sb.append("<div editorname=\"").append(editorName).append("\" id=\"").append(editorid).append("_DIV\" class=\"form_hint\">");
            sb.append("<table border=0 style=\"font-size:8pt;font-family:Arial;\"><tbody>");
            sb.append("<tr>");
            if (collection != null && collection.size() > 0) {
                sb.append("<td>");
                sb.append("&nbsp;");
                sb.append("</td>");
                Iterator it = previewProps.keySet().iterator();
                while (it.hasNext()) {
                    String key = it.next().toString();
                    sb.append("<td>");
                    sb.append(previewProps.get(key) != null ? tp.translate(previewProps.get(key).toString()) : key);
                    sb.append("</td>");
                }
                sb.append("</tr>");
                for (int i = 0; i < collection.size(); ++i) {
                    PropertyList current = collection.getPropertyList(i);
                    sb.append("<tr>");
                    sb.append("<td>");
                    sb.append(i + 1);
                    sb.append("</td>");
                    it = previewProps.keySet().iterator();
                    while (it.hasNext()) {
                        String key = it.next().toString();
                        sb.append("<td>");
                        sb.append(current.getProperty(key, "&nbsp;"));
                        sb.append("</td>");
                    }
                    sb.append("</tr>");
                }
            } else {
                sb.append("<td>").append(itemtext).append("</td>");
                sb.append("</tr>");
            }
            sb.append("</tbody></table>");
            sb.append("</div>");
        }
        sb.append("</td>");
        sb.append("</tr>");
    }

    public static void renderFormCustomEditor(StringBuffer sb, TranslationProcessor tp, String fieldId, String editorName, String editorTitle, String editorText, int collectionType, boolean disabled, String groupid) {
        PropertyBuilder.renderFormCustomEditor(sb, tp, fieldId, editorName, editorTitle, editorText, collectionType, disabled, groupid, true);
    }

    public static void renderFormCustomEditor(StringBuffer sb, TranslationProcessor tp, String fieldId, String editorName, String editorTitle, String editorText, int collectionType, boolean disabled, String groupid, boolean dynamic) {
        String onclick;
        String elementid = "formbuilder_property_" + StringUtil.replaceAll(editorName, ".", "_");
        String mouseover = "formBuilder.showCustomPreview('" + elementid + "');";
        switch (collectionType) {
            case 1: {
                onclick = "formBuilder.editCustom('groups','groupid','" + fieldId + "','" + editorName + "'," + collectionType + "'," + dynamic + ");";
                break;
            }
            case 0: {
                onclick = "formBuilder.editCustom('fields','fieldid','" + fieldId + "','" + editorName + "'," + collectionType + "'," + dynamic + ");";
                break;
            }
            case 5: {
                onclick = "formBuilder.editCustom('datasources','datasourceid','" + fieldId + "','" + editorName + "'," + collectionType + "'," + dynamic + ");";
                break;
            }
            case 2: {
                onclick = "formBuilder.editCustom('sections','sectionid','" + fieldId + "','" + editorName + "'," + collectionType + "'," + dynamic + ");";
                break;
            }
            case 4: {
                onclick = "formBuilder.editCustom('pages','pageid','" + fieldId + "','" + editorName + "'," + collectionType + "'," + dynamic + ");";
                break;
            }
            case 3: {
                onclick = "formBuilder.editCustom('labels','labelid','" + fieldId + "','" + editorName + "'," + collectionType + "'," + dynamic + ");";
                break;
            }
            default: {
                onclick = "formBuilder.editCustom('fields','fieldid','" + fieldId + "','" + editorName + "'," + collectionType + "'," + dynamic + ");";
            }
        }
        PropertyBuilder.renderCustomEditor(sb, tp, editorName, elementid, editorTitle, editorText, disabled, mouseover, onclick, groupid);
    }

    public static void renderCustomEditor(StringBuffer sb, TranslationProcessor tp, String editorName, String editorid, String editorTitle, String editorText, boolean disabled, String mousemove, String buttonclick, String groupid) {
        if (tp != null) {
            editorTitle = tp.translate(editorTitle);
            editorText = tp.translate(editorText);
        }
        if (groupid != null && groupid.length() > 0) {
            sb.append("<tr class=\"propgroup_").append(groupid).append("\">");
        } else {
            sb.append("<tr>");
        }
        sb.append("<td class=\"form_prop_title\">").append(editorTitle).append("</td>");
        sb.append("<td class=\"form_prop_value\">");
        sb.append("<table cellpadding=\"0\" border=\"0\" cellspacing=\"0\" style=\"table-layout:fixed;width:100%;\"><tbody><tr><td onmouseover=\"").append(mousemove).append("\"\" class=\"form_readonlytext_wrapper;\">");
        sb.append("<div class=\"form_readonlytext form_readonlytext_nonie\">&nbsp;").append(editorText).append("</div>");
        sb.append("</td>");
        if (!disabled) {
            sb.append("<td style=\"width:20px;\">");
            sb.append("<button style=\"height:21px;width:20px;padding-left:2px;\" onclick=\"").append(buttonclick).append("\">...</button>");
            sb.append("</td>");
        }
        sb.append("</td></tr></tbody></table>");
        sb.append("<div editorname=\"").append(editorName).append("\"id=\"").append(editorid).append("_DIV\" class=\"form_hint\">");
        sb.append("</div>");
        sb.append("</td>");
        sb.append("</tr>");
    }

    public static void renderFormStringEditor(StringBuffer sb, TranslationProcessor tp, String fieldId, String editorName, String editorTitle, String value, boolean showEditor, int collectionType) {
        PropertyBuilder.renderFormStringEditor(sb, tp, fieldId, editorName, editorTitle, value, showEditor, collectionType, false, false);
    }

    public static void renderFieldStringEditor(StringBuffer sb, TranslationProcessor tp, String editorName, String editorTitle, String value, boolean showEditor) {
        PropertyBuilder.renderFieldStringEditor(sb, tp, editorName, editorTitle, value, showEditor, false, false);
    }

    public static void renderFormStringEditor(StringBuffer sb, TranslationProcessor tp, String fieldId, String editorName, String editorTitle, String value, boolean showEditor, int collectionType, boolean groovy) {
        PropertyBuilder.renderFormStringEditor(sb, tp, fieldId, editorName, editorTitle, value, showEditor, collectionType, groovy, false);
    }

    public static void renderFormStringEditor(StringBuffer sb, TranslationProcessor tp, String fieldId, String editorName, String editorTitle, String value, boolean showEditor, int collectionType, boolean groovy, boolean disabled) {
        PropertyBuilder.renderFormStringEditor(sb, tp, fieldId, editorName, editorTitle, value, showEditor, collectionType, groovy, false, disabled, "", "");
    }

    public static void renderFormStringEditor(StringBuffer sb, TranslationProcessor tp, String fieldId, String editorName, String editorTitle, String value, boolean showEditor, int collectionType, boolean groovy, boolean groovyonly, boolean disabled, String tip, String groupid) {
        PropertyBuilder.renderFormStringEditor(sb, tp, fieldId, editorName, editorTitle, value, showEditor, collectionType, groovy, false, disabled, "", "", true);
    }

    public static void renderFormStringEditor(StringBuffer sb, TranslationProcessor tp, String fieldId, String editorName, String editorTitle, String value, boolean showEditor, int collectionType, boolean groovy, boolean groovyonly, boolean disabled, String tip, String groupid, boolean dynamic) {
        String onblur;
        switch (collectionType) {
            case 1: {
                onblur = "formBuilder.propertyChange('groups','groupid','" + fieldId + "','" + editorName + "'," + groovy + "," + dynamic + ")";
                break;
            }
            case 5: {
                onblur = "formBuilder.propertyChange('datasources','datasourceid','" + fieldId + "','" + editorName + "'," + groovy + "," + dynamic + ")";
                break;
            }
            case 6: {
                onblur = "formBuilder.propertyChange('elements','elementid','" + fieldId + "','" + editorName + "'," + groovy + "," + dynamic + ")";
                break;
            }
            case 0: {
                onblur = "formBuilder.propertyChange('fields','fieldid','" + fieldId + "','" + editorName + "'," + groovy + "," + dynamic + ")";
                break;
            }
            case 2: {
                onblur = "formBuilder.propertyChange('sections','sectionid','" + fieldId + "','" + editorName + "'," + groovy + "," + dynamic + ")";
                break;
            }
            case 4: {
                onblur = "formBuilder.propertyChange('pages','pageid','" + fieldId + "','" + editorName + "'," + groovy + "," + dynamic + ")";
                break;
            }
            case 3: {
                onblur = "formBuilder.propertyChange('labels','labelid','" + fieldId + "','" + editorName + "'," + groovy + "," + dynamic + ")";
                break;
            }
            default: {
                onblur = "formBuilder.propertyChange('fields','fieldid','" + fieldId + "','" + editorName + "'," + groovy + "," + dynamic + ")";
            }
        }
        String elementid = "formbuilder_property_" + StringUtil.replaceAll(editorName, ".", "_");
        String onclick = "";
        if (showEditor) {
            onclick = "formBuilder.editLongString('" + elementid + "', false);";
        }
        String gonclick = "";
        if (groovy) {
            gonclick = "formBuilder.editGroovy('" + elementid + "__EXPRESSION')";
        }
        PropertyBuilder.renderStringEditor(sb, tp, editorName, elementid, editorTitle, value, disabled, groovyonly, onblur, onclick, gonclick, showEditor || groovy, tip, groupid);
    }

    public static void renderParserStringEditor(StringBuffer sb, TranslationProcessor tp, String fieldId, String editorName, String editorTitle, String value, boolean showEditor, boolean groovy, boolean disabled) {
        String onblur = "ssParser.propertyChange('fields','fieldid','" + fieldId + "','" + editorName + "'," + groovy + ")";
        String elementid = "ssparser_property_" + StringUtil.replaceAll(editorName, ".", "_");
        String onclick = "";
        if (showEditor || groovy) {
            onclick = "ssParser.editLongString('" + elementid + "', " + groovy + ");";
        }
        PropertyBuilder.renderStringEditor(sb, tp, editorName, elementid, editorTitle, value, disabled, false, onblur, onclick, "", showEditor || groovy, "", "");
    }

    public static void renderParserValidationEditor(StringBuffer sb, TranslationProcessor tp, String fieldId, String editorName, String editorTitle, String validationrule, boolean showEditor, boolean groovy, boolean disabled, String fieldtype) {
        String onblur = "ssParser.propertyChange('fields','fieldid','" + fieldId + "','" + editorName + "'," + groovy + ")";
        String elementid = "ssparser_property_" + StringUtil.replaceAll(editorName, ".", "_");
        String onclick = "";
        if (showEditor || groovy) {
            onclick = "ssParser.editLongString('" + elementid + "', " + groovy + ");";
        }
        PropertyBuilder.renderValidationEditor(sb, tp, editorName, elementid, editorTitle, validationrule, disabled, false, onblur, onclick, "", "", "", fieldId, fieldtype);
    }

    public static void renderFieldStringEditor(StringBuffer sb, TranslationProcessor tp, String editorName, String editorTitle, String value, boolean showEditor, boolean groovy, boolean disabled) {
        PropertyBuilder.renderFieldStringEditor(sb, tp, editorName, editorTitle, value, showEditor, groovy, false, disabled, "", "");
    }

    public static void renderFieldStringEditor(StringBuffer sb, TranslationProcessor tp, String editorName, String editorTitle, String value, boolean showEditor, boolean groovy, boolean groovyonly, boolean disabled, String tip, String groupid) {
        PropertyBuilder.renderFieldStringEditor(sb, tp, editorName, editorTitle, value, showEditor, groovy, groovyonly, disabled, tip, groupid, true);
    }

    public static void renderFieldStringEditor(StringBuffer sb, TranslationProcessor tp, String editorName, String editorTitle, String value, boolean showEditor, boolean groovy, boolean groovyonly, boolean disabled, String tip, String groupid, boolean dynamic) {
        String onblur = "fieldBuilder.propertyChange('" + editorName + "'," + groovy + "," + dynamic + ")";
        String elementid = "fieldbuilder_property_" + StringUtil.replaceAll(editorName, ".", "_");
        String onclick = "";
        if (showEditor) {
            onclick = "fieldBuilder.editLongString('" + elementid + "', false);";
        }
        String gonclick = "";
        if (groovy) {
            gonclick = "fieldBuilder.editGroovy('" + elementid + "__EXPRESSION')";
        }
        PropertyBuilder.renderStringEditor(sb, tp, editorName, elementid, editorTitle, value, disabled, groovyonly, onblur, onclick, gonclick, showEditor || groovy, tip, groupid);
    }

    public static void renderGroovyEditor(StringBuffer sb, TranslationProcessor tp, String editorid, String editorTitle, String groovyvalue, boolean disabled, boolean groovyonly, String onchange, String groovyonclick, String tip, String groupid) {
        if (tp != null) {
            editorTitle = tp.translate(editorTitle);
            if (tip.length() > 0) {
                tip = tp.translate(tip);
            }
        }
        if (groupid != null && groupid.length() > 0) {
            sb.append("<tr class=\"propgroup_").append(groupid).append("\">");
        } else {
            sb.append("<tr>");
        }
        sb.append("<tr class=\"propgroup_").append(groupid).append("\">");
        sb.append("<td class=\"form_prop_title\">").append(editorTitle).append("</td>");
        sb.append("<td class=\"form_prop_value\"");
        if (tip.length() > 0) {
            sb.append(" title=\"").append(tip).append("\" ");
        }
        sb.append(">");
        sb.append("<table cellpadding=\"0\" border=\"0\" cellspacing=\"0\" style=\"table-layout:fixed;width:100%;\"><tbody><tr><td>");
        String hover = !groovyonly && !disabled ? "Groovy expression. Clear expression to re-enable normal property." : "Groovy expression.";
        if (tp != null) {
            hover = tp.translate(hover);
        }
        String style = "";
        if (disabled) {
            style = " style=\"color:gray;\" ";
        }
        sb.append("<textarea readonly class=\"groovy\"").append(style).append("title=\"").append(hover).append("\" id=\"").append(editorid).append("__EXPRESSION\" name=\"").append(editorid).append("__EXPRESSION\"");
        sb.append("onchange=\"").append(onchange).append("\" groovyonly=\"").append(groovyonly ? "Y" : "N").append("\">");
        sb.append(groovyvalue).append("</textarea>");
        sb.append("</td>");
        if (groovyonclick.length() > 0 && !disabled) {
            sb.append("<td style=\"width:16px;\">");
            String im = groovyvalue.length() > 0 ? "WEB-CORE/modules/webadmin/images/editexpression.gif" : "WEB-CORE/modules/webadmin/images/addexpression.gif";
            sb.append("<img id=\"").append(editorid).append("__EXPICON\" onclick=\"").append(groovyonclick).append("\" );\" style=\"cursor: pointer;\" src=\"").append(im).append("\">");
            sb.append("</td>");
        }
        sb.append("</tr></tbody></table>");
        sb.append("</td>");
        sb.append("</tr>");
    }

    public static void renderStringEditor(StringBuffer sb, TranslationProcessor tp, String editorName, String editorid, String editorTitle, String value, boolean disabled, boolean groovyonly, String onchange, String textonclick, String groovyonclick, boolean longString, String tip, String groupid) {
        if (groovyonly || groovyonclick.length() > 0 && value.startsWith("$G{") && value.endsWith("}")) {
            PropertyBuilder.renderGroovyEditor(sb, tp, editorid, editorTitle, value, disabled, groovyonly, onchange, groovyonclick, tip, groupid);
        } else {
            if (tp != null) {
                editorTitle = tp.translate(editorTitle);
                if (tip.length() > 0) {
                    tip = tp.translate(tip);
                }
            }
            if (groupid != null && groupid.length() > 0) {
                sb.append("<tr class=\"propgroup_").append(groupid).append("\">");
            } else {
                sb.append("<tr>");
            }
            sb.append("<tr class=\"propgroup_").append(groupid).append("\">");
            sb.append("<td class=\"form_prop_title\">").append(editorTitle).append("</td>");
            sb.append("<td class=\"form_prop_value\"");
            if (tip.length() > 0) {
                sb.append(" title=\"").append(tip).append("\" ");
            }
            sb.append(">");
            sb.append("<table cellpadding=\"0\" border=\"0\" cellspacing=\"0\" style=\"table-layout:fixed;width:100%;\"><tbody><tr><td>");
            HashMap<String, String> editorprops = new HashMap<String, String>();
            editorprops.put("width", "100%");
            editorprops.put("customonchange", "");
            if (longString) {
                editorprops.put("longstring", "Y");
            }
            if (disabled) {
                editorprops.put("disabled", "Y");
            }
            editorprops.put("onblur", onchange);
            PropertyValue pv = new PropertyValue(editorName, false, null);
            pv.value = value;
            sb.append(new StringEditor().getEditor(editorid, pv, null, false, editorprops, null, false));
            sb.append("</td>");
            if (textonclick.length() > 0 && !disabled) {
                sb.append("<td style=\"width:20px;\">");
                sb.append("<button style=\"height:21px;width:20px;padding-left:2px;\" onclick=\"").append(textonclick).append("\">...</button>");
                sb.append("</td>");
            }
            if (groovyonclick.length() > 0 && !disabled) {
                sb.append("<td style=\"width:16px;\">");
                sb.append("<textarea style=\"display:none;\" id=\"").append(editorid).append("__EXPRESSION\" name=\"").append(editorid).append("__EXPRESSION\"");
                sb.append("onchange=\"").append(onchange).append("\" groovyonly=\"").append("N").append("\">");
                sb.append("").append("</textarea>");
                String im = "WEB-CORE/modules/webadmin/images/addexpression.gif";
                sb.append("<img id=\"").append(editorid).append("__EXPICON\" onclick=\"").append(groovyonclick).append("\" );\" style=\"cursor: pointer;\" src=\"").append(im).append("\">");
                sb.append("</td>");
            }
            sb.append("</tr></tbody></table>");
            sb.append("</td>");
            sb.append("</tr>");
        }
    }

    public static void renderValidationEditor(StringBuffer sb, TranslationProcessor tp, String editorName, String editorid, String editorTitle, String validationrule, boolean disabled, boolean groovyonly, String onchange, String textonclick, String groovyonclick, String tip, String groupid, String fieldId, String fieldDataType) {
        if (groovyonly || groovyonclick.length() > 0 && validationrule.startsWith("$G{") && validationrule.endsWith("}")) {
            PropertyBuilder.renderGroovyEditor(sb, tp, editorid, editorTitle, validationrule, disabled, groovyonly, onchange, groovyonclick, tip, groupid);
        } else {
            if (tp != null) {
                editorTitle = tp.translate(editorTitle);
                if (tip.length() > 0) {
                    tip = tp.translate(tip);
                }
            }
            if (groupid != null && groupid.length() > 0) {
                sb.append("<tr class=\"propgroup_").append(groupid).append("\">");
            } else {
                sb.append("<tr>");
            }
            sb.append("<tr class=\"propgroup_").append(groupid).append("\">");
            sb.append("<td class=\"form_prop_title\">").append(editorTitle).append("</td>");
            sb.append("<td class=\"form_prop_value\"");
            if (tip.length() > 0) {
                sb.append(" title=\"").append(tip).append("\" ");
            }
            sb.append(">");
            sb.append("<table cellpadding=\"0\" border=\"0\" cellspacing=\"0\" style=\"table-layout:fixed;width:100%;\"><tbody><tr><td>");
            HashMap<String, String> editorprops = new HashMap<String, String>();
            editorprops.put("width", "100%");
            editorprops.put("customonchange", "ssParser.propertyChange( 'fields','fieldid'," + fieldId + ",'validationrule' );");
            editorprops.put("mode", "DFD");
            editorprops.put("validationrule", validationrule);
            editorprops.put("fieldid", fieldId);
            editorprops.put("fielddatatype", fieldDataType);
            if (disabled) {
                editorprops.put("disabled", "Y");
            }
            editorprops.put("onblur", onchange);
            editorprops.put("onchange", onchange);
            PropertyValue pv = new PropertyValue(editorName, false, null);
            pv.value = validationrule;
            sb.append(new ValidationEditor().getEditor(editorid, pv, null, false, editorprops, null, false));
            sb.append("</td>");
            if (textonclick.length() > 0 && !disabled) {
                sb.append("<td style=\"width:20px;\">");
                sb.append("<button style=\"height:21px;width:20px;padding-left:2px;\" onclick=\"").append(textonclick).append("\">...</button>");
                sb.append("</td>");
            }
            if (groovyonclick.length() > 0 && !disabled) {
                sb.append("<td style=\"width:16px;\">");
                sb.append("<textarea style=\"display:none;\" id=\"").append(editorid).append("__EXPRESSION\" name=\"").append(editorid).append("__EXPRESSION\"");
                sb.append("onchange=\"").append(onchange).append("\" groovyonly=\"").append("N").append("\">");
                sb.append("").append("</textarea>");
                String im = "WEB-CORE/modules/webadmin/images/addexpression.gif";
                sb.append("<img id=\"").append(editorid).append("__EXPICON\" onclick=\"").append(groovyonclick).append("\" );\" style=\"cursor: pointer;\" src=\"").append(im).append("\">");
                sb.append("</td>");
            }
            sb.append("</tr></tbody></table>");
            sb.append("</td>");
            sb.append("</tr>");
        }
    }

    public static void renderEditor(StringBuffer sb, TranslationProcessor tp, String editor, String editorName, String editorid, String editorTitle, String value, boolean disabled, String onchange, String tip, String groupid) {
        if (tp != null) {
            editorTitle = tp.translate(editorTitle);
            if (tip.length() > 0) {
                tip = tp.translate(tip);
            }
        }
        if (groupid != null && groupid.length() > 0) {
            sb.append("<tr class=\"propgroup_").append(groupid).append("\">");
        } else {
            sb.append("<tr>");
        }
        sb.append("<tr class=\"propgroup_").append(groupid).append("\">");
        sb.append("<td class=\"form_prop_title\">").append(editorTitle).append("</td>");
        sb.append("<td class=\"form_prop_value\"");
        if (tip.length() > 0) {
            sb.append(" title=\"").append(tip).append("\" ");
        }
        sb.append(">");
        sb.append("<table cellpadding=\"0\" border=\"0\" cellspacing=\"0\" style=\"table-layout:fixed;width:100%;\"><tbody><tr><td>");
        HashMap<String, String> editorprops = new HashMap<String, String>();
        editorprops.put("width", "100%");
        editorprops.put("customonchange", "");
        if (disabled) {
            editorprops.put("disabled", "Y");
        }
        editorprops.put("onblur", onchange);
        PropertyValue pv = new PropertyValue(editorName, false, null);
        pv.value = value;
        try {
            Class<?> c = Class.forName("com.labvantage.sapphire.admin.propertytree." + editor);
            TypeSimple editorClass = (TypeSimple)c.newInstance();
            if (editorClass != null) {
                sb.append(editorClass.getEditor(editorid, pv, null, false, editorprops, null, false));
            } else {
                sb.append(tp.translate("Editor not found"));
            }
        }
        catch (Exception e1) {
            tp.translate("Editor not found");
            Logger.logError("Unable to diplay SimpleEditor ('" + editor + "')");
        }
        sb.append("</td>");
        sb.append("</tr></tbody></table>");
        sb.append("</td>");
        sb.append("</tr>");
    }

    public static void renderFormURLEditor(StringBuffer sb, TranslationProcessor tp, String fieldId, String editorName, String editorTitle, String value, int collectionType, boolean disabled) {
        PropertyBuilder.renderFormURLEditor(sb, tp, fieldId, editorName, editorTitle, value, collectionType, disabled, "", "");
    }

    public static void renderFormURLEditor(StringBuffer sb, TranslationProcessor tp, String fieldId, String editorName, String editorTitle, String value, int collectionType, boolean disabled, String tip, String groupid) {
        PropertyBuilder.renderFormURLEditor(sb, tp, fieldId, editorName, editorTitle, value, collectionType, disabled, tip, groupid, true);
    }

    public static void renderFormURLEditor(StringBuffer sb, TranslationProcessor tp, String fieldId, String editorName, String editorTitle, String value, int collectionType, boolean disabled, String tip, String groupid, boolean dynamic) {
        String onblur;
        switch (collectionType) {
            case 1: {
                onblur = "formBuilder.propertyChange('groups','groupid','" + fieldId + "','" + editorName + "',false," + dynamic + ")";
                break;
            }
            case 0: {
                onblur = "formBuilder.propertyChange('fields','fieldid','" + fieldId + "','" + editorName + "',false," + dynamic + ")";
                break;
            }
            case 2: {
                onblur = "formBuilder.propertyChange('sections','sectionid','" + fieldId + "','" + editorName + "',false," + dynamic + ")";
                break;
            }
            case 3: {
                onblur = "formBuilder.propertyChange('labels','labelid','" + fieldId + "','" + editorName + "',false," + dynamic + ")";
                break;
            }
            case 5: {
                onblur = "formBuilder.propertyChange('datasources','datasourceid','" + fieldId + "','" + editorName + "',false," + dynamic + ")";
                break;
            }
            case 6: {
                onblur = "formBuilder.propertyChange('elements','elementid','" + fieldId + "','" + editorName + "',false," + dynamic + ")";
                break;
            }
            case 4: {
                onblur = "formBuilder.propertyChange('pages','pageid','" + fieldId + "','" + editorName + "',false," + dynamic + ")";
                break;
            }
            default: {
                onblur = "formBuilder.propertyChange('fields','fieldid','" + fieldId + "','" + editorName + "',false," + dynamic + ")";
            }
        }
        String elementid = "formbuilder_property_" + StringUtil.replaceAll(editorName, ".", "_");
        String onclick = "formBuilder.editURL('" + elementid + "');";
        PropertyBuilder.renderStringEditor(sb, tp, editorName, elementid, editorTitle, value, disabled, false, onblur, onclick, "", false, tip, groupid);
    }

    public static void renderFieldURLEditor(StringBuffer sb, TranslationProcessor tp, String editorName, String editorTitle, String value, boolean disabled) {
        PropertyBuilder.renderFieldURLEditor(sb, tp, editorName, editorTitle, value, disabled, "", "");
    }

    public static void renderFieldURLEditor(StringBuffer sb, TranslationProcessor tp, String editorName, String editorTitle, String value, boolean disabled, String tip, String groupid) {
        PropertyBuilder.renderFieldURLEditor(sb, tp, editorName, editorTitle, value, disabled, tip, groupid, true);
    }

    public static void renderFieldURLEditor(StringBuffer sb, TranslationProcessor tp, String editorName, String editorTitle, String value, boolean disabled, String tip, String groupid, boolean dynamic) {
        String onblur = "fieldBuilder.propertyChange('" + editorName + "',false," + dynamic + ")";
        String elementid = "fieldbuilder_property_" + StringUtil.replaceAll(editorName, ".", "_");
        String onclick = "fieldBuilder.editURL('" + elementid + "');";
        PropertyBuilder.renderStringEditor(sb, tp, editorName, elementid, editorTitle, value, disabled, false, onblur, onclick, "", false, tip, groupid);
    }

    public static void renderFormListEditor(StringBuffer sb, TranslationProcessor tp, String fieldId, String editorName, String editorTitle, String values, String value, int collectionType) {
        PropertyBuilder.renderFormListEditor(sb, tp, fieldId, editorName, editorTitle, values, value, collectionType, false, false);
    }

    public static void renderFormListEditor(StringBuffer sb, TranslationProcessor tp, String fieldId, String editorName, String editorTitle, String values, String value, int collectionType, boolean disabled) {
        PropertyBuilder.renderFormListEditor(sb, tp, fieldId, editorName, editorTitle, values, value, collectionType, disabled, false);
    }

    public static void renderFormListEditor(StringBuffer sb, TranslationProcessor tp, String fieldId, String editorName, String editorTitle, String values, String value, int collectionType, boolean disabled, boolean useGroovy) {
        PropertyBuilder.renderFormListEditor(sb, tp, fieldId, editorName, editorTitle, values, value, collectionType, disabled, useGroovy, "", "");
    }

    public static void renderFormListEditor(StringBuffer sb, TranslationProcessor tp, String fieldId, String editorName, String editorTitle, String values, String value, int collectionType, boolean disabled, boolean useGroovy, String tip, String groupid) {
        PropertyBuilder.renderFormListEditor(sb, tp, fieldId, editorName, editorTitle, values, value, collectionType, disabled, useGroovy, tip, groupid, false, true);
    }

    public static void renderFormListEditor(StringBuffer sb, TranslationProcessor tp, String fieldId, String editorName, String editorTitle, String values, String value, int collectionType, boolean disabled, boolean useGroovy, String tip, String groupid, boolean editable, boolean dynamic) {
        String onchange;
        switch (collectionType) {
            case 1: {
                onchange = "formBuilder.propertyChange('groups','groupid','" + fieldId + "','" + editorName + "', " + useGroovy + "," + dynamic + ")";
                break;
            }
            case 0: {
                onchange = "formBuilder.propertyChange('fields','fieldid','" + fieldId + "','" + editorName + "', " + useGroovy + "," + dynamic + ")";
                break;
            }
            case 2: {
                onchange = "formBuilder.propertyChange('sections','sectionid','" + fieldId + "','" + editorName + "', " + useGroovy + "," + dynamic + ")";
                break;
            }
            case 5: {
                onchange = "formBuilder.propertyChange('datasources','datasourceid','" + fieldId + "','" + editorName + "', " + useGroovy + "," + dynamic + ")";
                break;
            }
            case 6: {
                onchange = "formBuilder.propertyChange('elements','elementid','" + fieldId + "','" + editorName + "', " + useGroovy + "," + dynamic + ")";
                break;
            }
            case 4: {
                onchange = "formBuilder.propertyChange('pages','pageid','" + fieldId + "','" + editorName + "', " + useGroovy + "," + dynamic + ")";
                break;
            }
            case 3: {
                onchange = "formBuilder.propertyChange('labels','labelid','" + fieldId + "','" + editorName + "', " + useGroovy + "," + dynamic + ")";
                break;
            }
            default: {
                onchange = "formBuilder.propertyChange('fields','fieldid','" + fieldId + "','" + editorName + "', " + useGroovy + "," + dynamic + ")";
            }
        }
        String elementid = "formbuilder_property_" + StringUtil.replaceAll(editorName, ".", "_");
        String onclick = "formBuilder.editGroovy('" + elementid + "__EXPRESSION');";
        PropertyBuilder.renderListEditor(sb, tp, editorName, elementid, editorTitle, values, value, disabled, useGroovy, editable, onchange, onclick, tip, groupid);
    }

    public static void renderParserListEditor(StringBuffer sb, TranslationProcessor tp, String fieldId, String editorName, String editorTitle, String values, String value, boolean disabled) {
        String onchange = "ssParser.propertyChange('fields','fieldid','" + fieldId + "','" + editorName + "', false)";
        String elementid = "ssparser_property_" + StringUtil.replaceAll(editorName, ".", "_");
        PropertyBuilder.renderListEditor(sb, tp, editorName, elementid, editorTitle, values, value, disabled, false, onchange, "", "", "");
    }

    public static void renderFieldListEditor(StringBuffer sb, TranslationProcessor tp, String editorName, String editorTitle, String values, String value, boolean useGroovy) {
        PropertyBuilder.renderFieldListEditor(sb, tp, editorName, editorTitle, values, value, useGroovy, "", "");
    }

    public static void renderFieldListEditor(StringBuffer sb, TranslationProcessor tp, String editorName, String editorTitle, String values, String value, boolean useGroovy, String tip, String groupid) {
        PropertyBuilder.renderFieldListEditor(sb, tp, editorName, editorTitle, values, value, useGroovy, tip, groupid, false);
    }

    public static void renderFieldListEditor(StringBuffer sb, TranslationProcessor tp, String editorName, String editorTitle, String values, String value, boolean useGroovy, String tip, String groupid, boolean disabled) {
        PropertyBuilder.renderFieldListEditor(sb, tp, editorName, editorTitle, values, value, useGroovy, tip, groupid, disabled, false, true);
    }

    public static void renderFieldListEditor(StringBuffer sb, TranslationProcessor tp, String editorName, String editorTitle, String values, String value, boolean useGroovy, String tip, String groupid, boolean disabled, boolean editable, boolean dynamic) {
        String onchange = "fieldBuilder.propertyChange('" + editorName + "', " + useGroovy + "," + dynamic + ")";
        String elementid = "fieldbuilder_property_" + StringUtil.replaceAll(editorName, ".", "_");
        String onclick = "fieldBuilder.editGroovy( 'fieldbuilder_property_" + editorName + "__EXPRESSION' );";
        PropertyBuilder.renderListEditor(sb, tp, editorName, elementid, editorTitle, values, value, disabled, editable, useGroovy, onchange, onclick, tip, groupid);
    }

    public static void renderListEditor(StringBuffer sb, TranslationProcessor tp, String editorName, String editorid, String editorTitle, String values, String value, boolean disabled, boolean useGroovy, String onchange, String onclick, String tip, String groupid) {
        PropertyBuilder.renderListEditor(sb, tp, editorName, editorid, editorTitle, values, value, disabled, useGroovy, false, onchange, onclick, tip, groupid);
    }

    public static void renderListEditor(StringBuffer sb, TranslationProcessor tp, String editorName, String editorid, String editorTitle, String values, String value, boolean disabled, boolean useGroovy, boolean editable, String onchange, String onclick, String tip, String groupid) {
        if (useGroovy && value.startsWith("$G{") && value.endsWith("}")) {
            PropertyBuilder.renderGroovyEditor(sb, tp, editorid, editorTitle, value, disabled, false, onchange, onclick, tip, groupid);
        } else if (disabled) {
            PropertyBuilder.renderStringEditor(sb, tp, editorName, editorid, editorTitle, value, true, false, "", "", "", false, tip, groupid);
        } else {
            if (tp != null) {
                editorTitle = tp.translate(editorTitle);
                if (tip.length() > 0) {
                    tip = tp.translate(tip);
                }
            }
            if (groupid != null && groupid.length() > 0) {
                sb.append("<tr class=\"propgroup_").append(groupid).append("\">");
            } else {
                sb.append("<tr>");
            }
            sb.append("<td class=\"form_prop_title\">").append(editorTitle).append("</td>");
            sb.append("<td class=\"form_prop_value\">");
            sb.append("<table style=\"table-layout:fixed;width:100%;\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">");
            sb.append("<tbody>");
            sb.append("<tr>");
            sb.append("<td class=\"form_prop_value\"");
            if (tip.length() > 0) {
                sb.append(" title=\"").append(tip).append("\" ");
            }
            sb.append(">");
            HashMap<String, String> editorprops = new HashMap<String, String>();
            editorprops.put("customstyle", "width:100%;");
            editorprops.put("values", values);
            editorprops.put("customonchange", onchange);
            if (editable) {
                editorprops.put("editable", "Y");
                editorprops.put("editmode", "simple");
            }
            PropertyValue pv = new PropertyValue(editorName, false, null);
            pv.value = value;
            sb.append(new ListEditor().getEditor("" + editorid, pv, null, false, editorprops, null, false));
            if (useGroovy && onclick.length() > 0) {
                sb.append("<td style=\"width:16px;\">");
                sb.append("<textarea style=\"display:none;\" id=\"").append(editorid).append("__EXPRESSION\" name=\"").append(editorid).append("__EXPRESSION\"");
                sb.append("onchange=\"").append(onchange).append("\" groovyonly=\"N\">");
                sb.append("").append("</textarea>");
                String im = "WEB-CORE/modules/webadmin/images/addexpression.gif";
                sb.append("<img id=\"").append(editorid).append("__EXPICON\" onclick=\"").append(onclick).append("\" );\" style=\"cursor: pointer;\" src=\"").append(im).append("\">");
                sb.append("</td>");
            }
            sb.append("</td>");
            sb.append("</tr>");
            sb.append("</tbody>");
            sb.append("</table>");
            sb.append("</td>");
            sb.append("</tr>");
        }
    }

    public static void renderFormYesNoEditor(StringBuffer sb, TranslationProcessor tp, String fieldId, String editorName, String editorTitle, String value, boolean useGroovy, int collectionType) {
        PropertyBuilder.renderFormYesNoEditor(sb, tp, fieldId, editorName, editorTitle, value, useGroovy, collectionType, false);
    }

    public static void renderFormYesNoEditor(StringBuffer sb, TranslationProcessor tp, String fieldId, String editorName, String editorTitle, String value, boolean useGroovy, int collectionType, boolean disabled) {
        PropertyBuilder.renderFormYesNoEditor(sb, tp, fieldId, editorName, editorTitle, value, useGroovy, collectionType, disabled, "", "");
    }

    public static void renderFormYesNoEditor(StringBuffer sb, TranslationProcessor tp, String fieldId, String editorName, String editorTitle, String value, boolean useGroovy, int collectionType, boolean disabled, String tip, String groupid) {
        PropertyBuilder.renderFormYesNoEditor(sb, tp, fieldId, editorName, editorTitle, value, useGroovy, collectionType, disabled, tip, groupid, true);
    }

    public static void renderFormYesNoEditor(StringBuffer sb, TranslationProcessor tp, String fieldId, String editorName, String editorTitle, String value, boolean useGroovy, int collectionType, boolean disabled, String tip, String groupid, boolean dynamic) {
        String onchange;
        switch (collectionType) {
            case 1: {
                onchange = "formBuilder.propertyChange('groups','groupid','" + fieldId + "','" + editorName + "', " + useGroovy + "," + dynamic + ")";
                break;
            }
            case 0: {
                onchange = "formBuilder.propertyChange('fields','fieldid','" + fieldId + "','" + editorName + "', " + useGroovy + "," + dynamic + ")";
                break;
            }
            case 2: {
                onchange = "formBuilder.propertyChange('sections','sectionid','" + fieldId + "','" + editorName + "', " + useGroovy + "," + dynamic + ")";
                break;
            }
            case 4: {
                onchange = "formBuilder.propertyChange('pages','pageid','" + fieldId + "','" + editorName + "', " + useGroovy + "," + dynamic + ")";
                break;
            }
            case 3: {
                onchange = "formBuilder.propertyChange('labels','labelid','" + fieldId + "','" + editorName + "', " + useGroovy + "," + dynamic + ")";
                break;
            }
            case 5: {
                onchange = "formBuilder.propertyChange('datasources','datasourceid','" + fieldId + "','" + editorName + "', " + useGroovy + "," + dynamic + ")";
                break;
            }
            case 6: {
                onchange = "formBuilder.propertyChange('elements','elementid','" + fieldId + "','" + editorName + "', " + useGroovy + "," + dynamic + ")";
                break;
            }
            default: {
                onchange = "formBuilder.propertyChange('fields','fieldid','" + fieldId + "','" + editorName + "', " + useGroovy + "," + dynamic + ")";
            }
        }
        String elementid = "formbuilder_property_" + StringUtil.replaceAll(editorName, ".", "_");
        String onclick = "formBuilder.editGroovy('" + elementid + "__EXPRESSION' );";
        PropertyBuilder.renderYesNoEditor(sb, tp, editorName, elementid, editorTitle, value, useGroovy, disabled, onchange, onclick, tip, groupid);
    }

    public static void renderParserYesNoEditor(StringBuffer sb, TranslationProcessor tp, String fieldId, String editorName, String editorTitle, String value, boolean useGroovy, boolean disabled) {
        String onchange = "ssParser.propertyChange('fields','fieldid','" + fieldId + "','" + editorName + "', " + useGroovy + ")";
        String elementid = "ssparser_property_" + StringUtil.replaceAll(editorName, ".", "_");
        String onclick = "ssParser.editGroovy( 'formbuilder_property_" + editorName + "__EXPRESSION' );";
        PropertyBuilder.renderYesNoEditor(sb, tp, editorName, elementid, editorTitle, value, useGroovy, disabled, onchange, onclick, "", "");
    }

    public static void renderFieldYesNoEditor(StringBuffer sb, TranslationProcessor tp, String editorName, String editorTitle, String value, boolean useGroovy) {
        PropertyBuilder.renderFieldYesNoEditor(sb, tp, editorName, editorTitle, value, useGroovy, "", "");
    }

    public static void renderFieldYesNoEditor(StringBuffer sb, TranslationProcessor tp, String editorName, String editorTitle, String value, boolean useGroovy, String tip, String groupid) {
        PropertyBuilder.renderFieldYesNoEditor(sb, tp, editorName, editorTitle, value, useGroovy, tip, groupid, false);
    }

    public static void renderFieldYesNoEditor(StringBuffer sb, TranslationProcessor tp, String editorName, String editorTitle, String value, boolean useGroovy, String tip, String groupid, boolean disabled) {
        PropertyBuilder.renderFieldYesNoEditor(sb, tp, editorName, editorTitle, value, useGroovy, tip, groupid, disabled, true);
    }

    public static void renderFieldYesNoEditor(StringBuffer sb, TranslationProcessor tp, String editorName, String editorTitle, String value, boolean useGroovy, String tip, String groupid, boolean disabled, boolean dynamic) {
        String onchange = "fieldBuilder.propertyChange('" + editorName + "', " + useGroovy + "," + dynamic + ")";
        String elementid = "fieldbuilder_property_" + StringUtil.replaceAll(editorName, ".", "_");
        String onclick = "fieldBuilder.editGroovy( 'fieldbuilder_property_" + editorName + "__EXPRESSION' );";
        PropertyBuilder.renderYesNoEditor(sb, tp, editorName, elementid, editorTitle, value, useGroovy, disabled, onchange, onclick, tip, groupid);
    }

    public static void renderYesNoEditor(StringBuffer sb, TranslationProcessor tp, String editorName, String editorid, String editorTitle, String value, boolean useGroovy, boolean disabled, String onchange, String onclick, String tip, String groupid) {
        if (useGroovy && value.startsWith("$G{") && value.endsWith("}")) {
            PropertyBuilder.renderGroovyEditor(sb, tp, editorid, editorTitle, value, disabled, false, onchange, onclick, tip, groupid);
        } else if (disabled) {
            String tousevalue = "";
            if (value.equalsIgnoreCase("Y")) {
                tousevalue = "Yes";
            } else if (value.equalsIgnoreCase("N")) {
                tousevalue = "No";
            }
            PropertyBuilder.renderStringEditor(sb, tp, editorName, editorid, editorTitle, tousevalue, true, false, "", "", "", false, tip, groupid);
        } else {
            if (tp != null) {
                editorTitle = tp.translate(editorTitle);
                if (tip.length() > 0) {
                    tip = tp.translate(tip);
                }
            }
            if (groupid != null && groupid.length() > 0) {
                sb.append("<tr class=\"propgroup_").append(groupid).append("\">");
            } else {
                sb.append("<tr>");
            }
            sb.append("<td class=\"form_prop_title\">").append(editorTitle).append("</td>");
            sb.append("<td class=\"form_prop_value\">");
            sb.append("<table style=\"table-layout:fixed;width:100%;\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">");
            sb.append("<tbody>");
            sb.append("<tr>");
            sb.append("<td class=\"form_prop_value\"");
            if (tip.length() > 0) {
                sb.append(" title=\"").append(tip).append("\" ");
            }
            sb.append(">");
            HashMap<String, String> editorprops = new HashMap<String, String>();
            editorprops.put("customstyle", "width:100%;");
            editorprops.put("customonchange", onchange);
            PropertyValue pv = new PropertyValue(editorName, false, null);
            pv.value = value;
            sb.append(new YesNoEditor().getEditor("" + editorid, pv, null, false, editorprops, null, false));
            sb.append("</td>");
            if (useGroovy && onclick.length() > 0) {
                sb.append("<td style=\"width:16px;\">");
                sb.append("<textarea style=\"display:none;\" id=\"").append(editorid).append("__EXPRESSION\" name=\"").append(editorid).append("__EXPRESSION\"");
                sb.append("onchange=\"").append(onchange).append("\" groovyonly=\"N\">");
                sb.append("").append("</textarea>");
                String im = "WEB-CORE/modules/webadmin/images/addexpression.gif";
                sb.append("<img id=\"").append(editorid).append("__EXPICON\" onclick=\"").append(onclick).append("\" );\" style=\"cursor: pointer;\" src=\"").append(im).append("\">");
                sb.append("</td>");
            }
            sb.append("</tr>");
            sb.append("</tbody>");
            sb.append("</table>");
            sb.append("</td>");
            sb.append("</tr>");
        }
    }
}

