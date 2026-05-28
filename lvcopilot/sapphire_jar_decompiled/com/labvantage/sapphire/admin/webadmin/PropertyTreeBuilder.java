/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletRequest
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.PageContext
 *  org.apache.xpath.XPathAPI
 */
package com.labvantage.sapphire.admin.webadmin;

import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.propertytree.TypeSimple;
import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.util.http.HttpUtil;
import com.labvantage.sapphire.xml.PropertyDefinition;
import com.labvantage.sapphire.xml.PropertyDefinitionList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import javax.xml.transform.TransformerException;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeIterator;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.RequestContext;
import sapphire.util.Browser;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.DOMUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;
import sapphire.xml.PropertyValue;

public class PropertyTreeBuilder {
    public static final String PIPE_DELIMETER = "__PIPE__";
    public static final String SEMICOLON_DELIMETER = "__SEMICOLON__";

    private static String getRequestParameter(ServletRequest request, HashMap<String, Object> parameters, String name) {
        if (parameters != null) {
            Object out = parameters.get(name);
            return out == null ? "" : out.toString();
        }
        return request.getParameter(name) == null ? "" : request.getParameter(name);
    }

    protected static HashMap<String, Object> getRequestParams(ServletRequest request) {
        HashMap out;
        Object s = request.getAttribute("__propertytreebuilderrequest");
        if (s != null && s instanceof HashMap) {
            out = (HashMap)s;
        } else {
            Object ar = request.getAttribute("__ajaxresponse");
            if (ar == null || !(ar instanceof AjaxResponse)) {
                return null;
            }
            AjaxResponse r = (AjaxResponse)ar;
            out = (HashMap)r.getRequestParameters();
            request.setAttribute("__propertytreebuilderrequest", (Object)out);
        }
        return out;
    }

    public static void buildPropertyDepTree(HttpServletRequest request, Element propertydeplist, boolean debug) {
        HashMap<String, Object> requestParams = PropertyTreeBuilder.getRequestParams((ServletRequest)request);
        String dothis = PropertyTreeBuilder.getRequestParameter((ServletRequest)request, requestParams, "dothis");
        String args1 = PropertyTreeBuilder.getRequestParameter((ServletRequest)request, requestParams, "args1");
        String args2 = PropertyTreeBuilder.getRequestParameter((ServletRequest)request, requestParams, "args2");
        if (debug) {
            Trace.log("PTREEBUILD", "BUILDDEP: dothis: " + dothis);
            Trace.log("PTREEBUILD", "BUILDDEP: args1: " + args1);
            Trace.log("PTREEBUILD", "BUILDDEP: args2: " + args2);
        }
        int propertydeps = 0;
        if (PropertyTreeBuilder.getRequestParameter((ServletRequest)request, requestParams, "PROPERTYDEPCOUNT").length() > 0) {
            try {
                propertydeps = Integer.parseInt(PropertyTreeBuilder.getRequestParameter((ServletRequest)request, requestParams, "PROPERTYDEPCOUNT"));
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        if (dothis.equals("adddep")) {
            ++propertydeps;
        }
        ArrayList<Integer> index = new ArrayList<Integer>();
        for (int plcount = 0; plcount < propertydeps; ++plcount) {
            index.add(new Integer(plcount));
        }
        int intarg1 = -1;
        try {
            intarg1 = Integer.parseInt(args1);
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (dothis.equals("deletedep")) {
            index.remove(intarg1);
        } else if (dothis.equals("moveback") && intarg1 > 0) {
            index.add(intarg1 - 1 < 0 ? 0 : intarg1 - 1, (Integer)index.remove(intarg1));
        } else if (dothis.equals("moveforward") && intarg1 < propertydeps - 1) {
            index.add(intarg1 + 1 >= index.size() ? index.size() : intarg1 + 1, (Integer)index.remove(intarg1));
        }
        if (debug) {
            Trace.log("PTREEBUILD", "BUILDDEP: Looking for " + propertydeps + " property dependencies");
        }
        for (int plcount = 0; plcount < index.size(); ++plcount) {
            int plindex = (Integer)index.get(plcount);
            String name = "PROPERTYDEP" + plindex + "__";
            String propertytreeid = PropertyTreeBuilder.getRequestParameter((ServletRequest)request, requestParams, name + "propertytreeid");
            String elementid = PropertyTreeBuilder.getRequestParameter((ServletRequest)request, requestParams, name + "elementid");
            String mandatory = PropertyTreeBuilder.getRequestParameter((ServletRequest)request, requestParams, name + "mandatory");
            String description = PropertyTreeBuilder.getRequestParameter((ServletRequest)request, requestParams, name + "description");
            if (debug) {
                Trace.log("PTREEBUILD", "Found: " + name + "propertytreeid = " + propertytreeid + ", " + name + "elementid= " + elementid);
            }
            if (propertytreeid.length() == 0) {
                elementid = args1;
                propertytreeid = args2;
                description = "";
            }
            Element propertydep = propertydeplist.getOwnerDocument().createElement("propertydep");
            propertydep.setAttribute("elementid", elementid);
            propertydep.setAttribute("propertytreeid", propertytreeid);
            propertydep.setAttribute("mandatory", mandatory == null || mandatory.length() == 0 ? "false" : "true");
            propertydep.setAttribute("description", description);
            propertydeplist.appendChild(propertydep);
        }
    }

    public static void buildPropertyDefTree(HttpServletRequest request, Element propertydeflist, String path, boolean debug) throws TransformerException {
        String defattribs;
        HashMap<String, Object> requestParams = PropertyTreeBuilder.getRequestParams((ServletRequest)request);
        String dothis = PropertyTreeBuilder.getRequestParameter((ServletRequest)request, requestParams, "dothis");
        String args1 = PropertyTreeBuilder.getRequestParameter((ServletRequest)request, requestParams, "args1");
        String args2 = PropertyTreeBuilder.getRequestParameter((ServletRequest)request, requestParams, "args2");
        String args3 = PropertyTreeBuilder.getRequestParameter((ServletRequest)request, requestParams, "args3");
        if (debug) {
            Trace.log("PTREEBUILD", path + " BUILDTREE: dothis: " + dothis);
            Trace.log("PTREEBUILD", path + " BUILDTREE: args1: " + args1);
            Trace.log("PTREEBUILD", path + " BUILDTREE: args2: " + args2);
            Trace.log("PTREEBUILD", path + " BUILDTREE: args3: " + args3);
        }
        if ((defattribs = PropertyTreeBuilder.getRequestParameter((ServletRequest)request, requestParams, path + "__DEFATTRIBUTES")).length() > 0) {
            String[] defattrib = StringUtil.split(defattribs, "\n");
            for (int j = 0; j < defattrib.length; ++j) {
                if (defattrib[j].indexOf(61) <= 0) continue;
                String id = defattrib[j].substring(0, defattrib[j].indexOf(61));
                String value = defattrib[j].substring(defattrib[j].indexOf(61) + 1);
                propertydeflist.setAttribute(id, value.trim());
            }
        }
        int propertydefs = 0;
        if (debug) {
            Trace.log("PTREEBUILD", "Looking for " + path + "__PROPERTYDEFCOUNT");
        }
        if (PropertyTreeBuilder.getRequestParameter((ServletRequest)request, requestParams, path + "__PROPERTYDEFCOUNT").length() > 0) {
            try {
                propertydefs = Integer.parseInt(PropertyTreeBuilder.getRequestParameter((ServletRequest)request, requestParams, path + "__PROPERTYDEFCOUNT"));
            }
            catch (Exception j) {
                // empty catch block
            }
        }
        if (dothis.equals("addproperty") && args1.equals(path)) {
            ++propertydefs;
        }
        ArrayList<Integer> index = new ArrayList<Integer>();
        for (int plcount = 0; plcount < propertydefs; ++plcount) {
            index.add(new Integer(plcount));
        }
        int intarg2 = -1;
        try {
            intarg2 = Integer.parseInt(args2);
        }
        catch (Exception value) {
            // empty catch block
        }
        int intarg3 = -1;
        try {
            intarg3 = Integer.parseInt(args3);
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (dothis.equals("deleteproperty") && args1.equals(path)) {
            index.remove(intarg2);
        } else if (dothis.equals("moveback") && args1.equals(path) && intarg2 > 0) {
            index.add(intarg2 - intarg3 < 0 ? 0 : intarg2 - intarg3, (Integer)index.remove(intarg2));
        } else if (dothis.equals("moveforward") && args1.equals(path) && intarg2 < propertydefs - 1) {
            index.add(intarg2 + intarg3 >= propertydefs ? propertydefs - 1 : intarg2 + intarg3, (Integer)index.remove(intarg2));
        }
        if (debug) {
            Trace.log("PTREEBUILD", path + " BUILDTREE: Looking for " + propertydefs + " propertydefinitions");
        }
        for (int plcount = 0; plcount < index.size(); ++plcount) {
            String attribs;
            int plindex = (Integer)index.get(plcount);
            String name = path + "__" + plindex + "__";
            String propertyid = PropertyTreeBuilder.getRequestParameter((ServletRequest)request, requestParams, name + "id");
            String type = PropertyTreeBuilder.getRequestParameter((ServletRequest)request, requestParams, name + "type");
            String title = PropertyTreeBuilder.getRequestParameter((ServletRequest)request, requestParams, name + "title");
            String editor = PropertyTreeBuilder.getRequestParameter((ServletRequest)request, requestParams, name + "editor");
            String advanced = PropertyTreeBuilder.getRequestParameter((ServletRequest)request, requestParams, name + "advanced");
            String translate = PropertyTreeBuilder.getRequestParameter((ServletRequest)request, requestParams, name + "translate");
            String deprecate = PropertyTreeBuilder.getRequestParameter((ServletRequest)request, requestParams, name + "deprecate");
            String expression = PropertyTreeBuilder.getRequestParameter((ServletRequest)request, requestParams, name + "expression");
            String resolution = PropertyTreeBuilder.getRequestParameter((ServletRequest)request, requestParams, name + "resolution");
            String showif = PropertyTreeBuilder.getRequestParameter((ServletRequest)request, requestParams, name + "showif");
            String help = HttpUtil.htmlEncode(PropertyTreeBuilder.getRequestParameter((ServletRequest)request, requestParams, name + "help"));
            String defaultvalue = PropertyTreeBuilder.getRequestParameter((ServletRequest)request, requestParams, name + "defaultvalue");
            if (propertyid.length() == 0) {
                propertyid = args2;
                type = args3;
                title = args2;
                editor = type.equals("simple") ? "com.labvantage.sapphire.admin.propertytree.StringEditor" : "com.labvantage.sapphire.admin.propertytree.PropertyListEditor";
            }
            Element propertydef = propertydeflist.getOwnerDocument().createElement("propertydef");
            if (propertyid.length() > 0) {
                propertydef.setAttribute("id", propertyid);
            }
            if (type.length() > 0) {
                propertydef.setAttribute("type", type);
            }
            if (title.length() > 0) {
                propertydef.setAttribute("title", title);
            }
            if (editor.length() > 0) {
                propertydef.setAttribute("editor", editor);
            }
            if (translate.equals("Y")) {
                propertydef.setAttribute("translate", "Y");
            }
            if (advanced.equals("Y")) {
                propertydef.setAttribute("advanced", "Y");
            }
            if (deprecate.equals("Y")) {
                propertydef.setAttribute("deprecate", "Y");
            }
            if (expression.equals("Y")) {
                propertydef.setAttribute("expression", "Y");
            }
            if (resolution.equals("Y")) {
                propertydef.setAttribute("resolution", "Y");
            }
            if (showif.length() > 0) {
                propertydef.setAttribute("showif", showif);
            }
            if (help.length() > 0) {
                propertydef.setAttribute("help", help);
            }
            if ((attribs = PropertyTreeBuilder.getRequestParameter((ServletRequest)request, requestParams, name + "attributes")).length() > 0) {
                if (debug) {
                    Trace.log("PTREEBUILD", "attribute string: " + attribs);
                }
                String[] attrib = StringUtil.split(attribs, "\n");
                for (int j = 0; j < attrib.length; ++j) {
                    if (attrib[j].indexOf(61) <= 0) continue;
                    String id = attrib[j].substring(0, attrib[j].indexOf(61));
                    String value = attrib[j].substring(attrib[j].indexOf(61) + 1);
                    if (debug) {
                        Trace.log("PTREEBUILD", id + "=" + value);
                    }
                    propertydef.setAttribute(id, HttpUtil.htmlEncode(value.trim()));
                }
            }
            if (type.equals("simple")) {
                if (defaultvalue != null && defaultvalue.length() > 0) {
                    propertydef.appendChild(propertydef.getOwnerDocument().createTextNode(defaultvalue));
                }
            } else {
                if (debug) {
                    Trace.log("PTREEBUILD", "Going recursive");
                }
                Element newpropertydeflist = propertydeflist.getOwnerDocument().createElement("propertydeflist");
                PropertyTreeBuilder.buildPropertyDefTree(request, newpropertydeflist, path + "__" + propertyid, debug);
                propertydef.appendChild(newpropertydeflist);
            }
            propertydeflist.appendChild(propertydef);
        }
    }

    public static long buildPropertyValueTree(PageContext pageContext, PropertyList parentPropertyList, PropertyDefinitionList propertyDefinitionList, Element appendto, long idseed, boolean isnode) throws TransformerException {
        ArrayList deleteList = new ArrayList();
        HashMap renameList = new HashMap();
        PropertyListCollection collection = new PropertyListCollection();
        collection.add(parentPropertyList);
        return PropertyTreeBuilder.buildPropertyValueTree(pageContext, null, collection, false, propertyDefinitionList, appendto, "root", idseed, isnode, deleteList, renameList, null);
    }

    public static long buildPropertyValueTree(PageContext pageContext, PropertyList parentPropertyList, PropertyDefinitionList propertyDefinitionList, Element appendto, long idseed, boolean isnode, ArrayList extraPropertyLists) throws TransformerException {
        ArrayList deleteList = new ArrayList();
        HashMap renameList = new HashMap();
        PropertyListCollection collection = new PropertyListCollection();
        collection.add(parentPropertyList);
        return PropertyTreeBuilder.buildPropertyValueTree(pageContext, null, collection, false, propertyDefinitionList, appendto, "root", idseed, isnode, deleteList, renameList, extraPropertyLists);
    }

    public static long buildPropertyValueTree(PageContext pageContext, Node node, PropertyListCollection ptreecollection, boolean isCollection, PropertyDefinitionList propertyDefinitionList, Element appendto, String path, long idseed, boolean isnode) throws TransformerException {
        ArrayList deleteList = new ArrayList();
        HashMap renameList = new HashMap();
        return PropertyTreeBuilder.buildPropertyValueTree(pageContext, node, ptreecollection, isCollection, propertyDefinitionList, appendto, path, idseed, isnode, deleteList, renameList, null);
    }

    public static long buildPropertyValueTree(PageContext pageContext, Node node, PropertyListCollection ptreecollection, boolean isCollection, PropertyDefinitionList propertyDefinitionList, Element appendto, String path, long idseed, boolean isnode, ArrayList deleteList, HashMap renameList, ArrayList extraPropertyLists) throws TransformerException {
        return PropertyTreeBuilder.buildPropertyValueTree_recursive(pageContext, node, ptreecollection, isCollection, propertyDefinitionList, appendto, path, idseed, isnode, deleteList, renameList, extraPropertyLists);
    }

    private static long buildPropertyValueTree_recursive(PageContext pageContext, Node node, PropertyListCollection ptreecollection, boolean isCollection, PropertyDefinitionList propertyDefinitionList, Element appendto, String path, long idseed, boolean isnode, ArrayList deleteList, HashMap renameList, ArrayList extraPropertyLists) throws TransformerException {
        Element parent;
        int moveTo;
        int colCount;
        int firstPos;
        int propertylists;
        String pasteString;
        ServletRequest request = pageContext.getRequest();
        HashMap<String, Object> requestParams = PropertyTreeBuilder.getRequestParams(request);
        String dothis = PropertyTreeBuilder.getRequestParameter(request, requestParams, "dothis");
        String args1 = PropertyTreeBuilder.getRequestParameter(request, requestParams, "args1");
        String args2 = PropertyTreeBuilder.getRequestParameter(request, requestParams, "args2");
        String args3 = PropertyTreeBuilder.getRequestParameter(request, requestParams, "args3");
        Browser browser = new Browser(pageContext);
        String pastePath = "";
        if (dothis.equals("pastepropertylist") && (pasteString = (String)pageContext.getSession().getAttribute("collectionitempaste")) != null && pasteString.length() > 0) {
            pastePath = pasteString.substring(0, pasteString.indexOf(";"));
        }
        int n = propertylists = isCollection ? 0 : 1;
        if (PropertyTreeBuilder.getRequestParameter(request, requestParams, path + "__PROPERTYLISTCOUNT").length() > 0) {
            try {
                propertylists = Integer.parseInt(PropertyTreeBuilder.getRequestParameter(request, requestParams, path + "__PROPERTYLISTCOUNT"));
            }
            catch (Exception e) {
                propertylists = 0;
            }
        }
        int intarg2 = -1;
        try {
            intarg2 = Integer.parseInt(args2);
        }
        catch (Exception exception) {
            // empty catch block
        }
        int intarg3 = -1;
        try {
            intarg3 = Integer.parseInt(args3);
        }
        catch (Exception exception) {
            // empty catch block
        }
        ArrayList<String> newPropsList = null;
        if (dothis.equals("addpropertylist") && args1.equals(path)) {
            if (args2 != null && args2.length() > 0 && intarg2 == -1 || intarg3 > 0) {
                if (args2 != null && args2.length() > 0 && intarg2 == -1) {
                    String[] rows = StringUtil.split(args2, "|");
                    newPropsList = new ArrayList<String>(Arrays.asList(rows));
                    propertylists += rows.length;
                }
                if (intarg3 > 0) {
                    propertylists += intarg3;
                }
            } else {
                propertylists += intarg2 > 0 ? intarg2 : 1;
            }
        }
        int lastsequence = 0;
        int maxsequence = 0;
        ArrayList<Integer> seq = new ArrayList<Integer>();
        ArrayList<Integer> index = new ArrayList<Integer>();
        ArrayList<Boolean> selected = new ArrayList<Boolean>();
        for (int plcount = 0; plcount < propertylists; ++plcount) {
            int sequence = 0;
            try {
                sequence = Integer.parseInt(PropertyTreeBuilder.getRequestParameter(request, requestParams, path + "_" + plcount + "__SEQUENCE"));
            }
            catch (Exception exception) {
                // empty catch block
            }
            if (sequence == 0) {
                sequence = lastsequence + 1000000;
            }
            lastsequence = sequence;
            if (sequence > maxsequence) {
                maxsequence = sequence;
            }
            seq.add(new Integer(sequence));
            index.add(new Integer(plcount));
            selected.add("on".equals(PropertyTreeBuilder.getRequestParameter(request, requestParams, path + "_" + plcount + "_ITEMSELECTOR")));
        }
        if (dothis.equals("moveback") && args1.equals(path) && intarg3 > 0) {
            firstPos = -1;
            colCount = 0;
            for (int i = 0; i < index.size(); ++i) {
                if (!((Boolean)selected.get(i)).booleanValue()) continue;
                if (firstPos == -1) {
                    moveTo = i - intarg3;
                    if (moveTo < 0) {
                        moveTo = 0;
                    }
                    firstPos = moveTo;
                } else {
                    moveTo = firstPos + ++colCount;
                }
                if (moveTo == 0) {
                    seq.set(i, new Integer((Integer)seq.get(0) / 2));
                } else {
                    seq.set(i, new Integer(((Integer)seq.get(moveTo) + (Integer)seq.get(moveTo - 1)) / 2));
                }
                index.add(moveTo, (Integer)index.remove(i));
                seq.add(moveTo, (Integer)seq.remove(i));
            }
        }
        if (dothis.equals("moveforward") && args1.equals(path) && intarg3 > 0) {
            firstPos = -1;
            colCount = 0;
            for (int i = index.size() - 1; i >= 0; --i) {
                if (!((Boolean)selected.get(i)).booleanValue()) continue;
                if (firstPos == -1) {
                    moveTo = i + intarg3;
                    if (moveTo > propertylists - 1) {
                        moveTo = propertylists - 1;
                    }
                    firstPos = moveTo;
                } else {
                    moveTo = firstPos - ++colCount;
                }
                if (moveTo == propertylists - 1) {
                    seq.set(i, new Integer((Integer)seq.get(propertylists - 1) + 1000000));
                } else {
                    seq.set(i, new Integer(((Integer)seq.get(moveTo) + (Integer)seq.get(moveTo + 1)) / 2));
                }
                index.add(moveTo, (Integer)index.remove(i));
                seq.add(moveTo, (Integer)seq.remove(i));
            }
        }
        if ((parent = (Element)appendto.getParentNode()) != null && parent.getAttribute("selectedindex") != null && parent.getAttribute("selectedindex").length() > 0) {
            int selectedindex = Integer.parseInt(parent.getAttribute("selectedindex"));
            int newIndex = index.indexOf(new Integer(selectedindex));
            parent.setAttribute("selectedindex", newIndex == -1 ? "0" : "" + newIndex);
        }
        StringBuffer selectedXML = new StringBuffer();
        for (int plcount = 0; plcount < index.size(); ++plcount) {
            String modulelist;
            String rolelist;
            String propertylisttitle;
            String locked;
            Element propertylist;
            NodeIterator it2;
            int plindex = (Integer)index.get(plcount);
            boolean ancestor = false;
            boolean newPropertyList = false;
            String propertylistid = PropertyTreeBuilder.getRequestParameter(request, requestParams, path + "_" + plindex + "__PROPERTYLISTID");
            String newpropertylistid = PropertyTreeBuilder.getRequestParameter(request, requestParams, path + "_" + plindex + "__NEWPROPERTYLISTID");
            if (node != null && newpropertylistid.length() > 0 && !newpropertylistid.equals(propertylistid) && XPathAPI.selectSingleNode((Node)node, (String)("//propertylist[@id='" + newpropertylistid + "']")) == null && XPathAPI.selectSingleNode((Node)appendto, (String)("//propertylist[@id='" + newpropertylistid + "']")) == null) {
                if (renameList != null) {
                    renameList.put(propertylistid, newpropertylistid);
                }
                it2 = XPathAPI.selectNodeIterator((Node)node, (String)("//propertylist[@id='" + propertylistid + "']"));
                while ((propertylist = (Element)it2.nextNode()) != null) {
                    propertylist.setAttribute("id", newpropertylistid);
                }
                propertylistid = newpropertylistid;
            }
            if (dothis.equals("deletepropertylist") && args1.equals(path) && ((Boolean)selected.get(plindex)).booleanValue()) {
                if (deleteList != null) {
                    deleteList.add(propertylistid);
                }
                if (!isnode) continue;
                it2 = XPathAPI.selectNodeIterator((Node)node, (String)("nodelist//node//collection/propertylist[@id='" + propertylistid + "']"));
                while ((propertylist = (Element)it2.nextNode()) != null) {
                    propertylist.getParentNode().removeChild(propertylist);
                }
                continue;
            }
            PropertyList currentPropertyList = null;
            try {
                currentPropertyList = (PropertyList)ptreecollection.get(plindex);
            }
            catch (Exception it2) {
                // empty catch block
            }
            if (currentPropertyList == null) {
                currentPropertyList = new PropertyList();
                currentPropertyList.setUsePropertyValues(true);
            }
            if (propertylistid == null || propertylistid.length() == 0) {
                newPropertyList = true;
                if (path.equals("root")) {
                    propertylistid = "root";
                } else if (isnode && node != null) {
                    ++idseed;
                    while (XPathAPI.selectSingleNode((Node)node, (String)("//propertylist[@id='" + idseed + "']")) != null || XPathAPI.selectSingleNode((Node)appendto, (String)("//propertylist[@id='" + idseed + "']")) != null) {
                        idseed = System.currentTimeMillis();
                    }
                    propertylistid = Long.toString(idseed);
                } else {
                    ++idseed;
                    while (XPathAPI.selectSingleNode((Node)appendto, (String)("//propertylist[@id='p" + idseed + "']")) != null) {
                        idseed = System.currentTimeMillis();
                    }
                    propertylistid = "p" + idseed;
                }
            } else {
                ancestor = PropertyTreeBuilder.getRequestParameter(request, requestParams, path + "_" + plindex + "__ANCESTOR").equals("Y");
            }
            Element newPropertyListNode = appendto.getOwnerDocument().createElement("propertylist");
            newPropertyListNode.setAttribute("id", propertylistid);
            if (newPropsList != null && newPropsList.size() > 0 && newPropertyList) {
                try {
                    String newProps = newPropsList.remove(0);
                    String[] props = StringUtil.split(newProps, ";");
                    for (int i = 0; i < props.length; ++i) {
                        Element property = appendto.getOwnerDocument().createElement("property");
                        property.setAttribute("id", props[i].substring(0, props[i].indexOf("=")));
                        property.setAttribute("type", "simple");
                        property.appendChild(appendto.getOwnerDocument().createTextNode(StringUtil.replaceAll(StringUtil.replaceAll(props[i].substring(props[i].indexOf("=") + 1), PIPE_DELIMETER, "|"), SEMICOLON_DELIMETER, ";")));
                        newPropertyListNode.appendChild(property);
                    }
                }
                catch (Exception newProps) {
                    // empty catch block
                }
            }
            if ("Y".equals(locked = PropertyTreeBuilder.getRequestParameter(request, requestParams, path + "_" + plindex + "__LOCKED"))) {
                newPropertyListNode.setAttribute("locked", locked);
            }
            String previousSequence = currentPropertyList.getPreviousSequence();
            if (isCollection && (!ancestor || previousSequence != null && previousSequence.length() > 0 && !previousSequence.equals(seq.get(plcount).toString()))) {
                newPropertyListNode.setAttribute("sequence", seq.get(plcount).toString());
            }
            if ((propertylisttitle = PropertyTreeBuilder.getRequestParameter(request, requestParams, path + "_" + plindex + "__PROPERTYLISTTITLE")).length() > 0) {
                newPropertyListNode.setAttribute("title", propertylisttitle);
            }
            if (!((rolelist = PropertyTreeBuilder.getRequestParameter(request, requestParams, path + "_" + plindex + "__ROLES")).length() <= 0 || currentPropertyList.getPreviousRoleList() != null && currentPropertyList.getPreviousRoleList().equals(rolelist))) {
                newPropertyListNode.setAttribute("rolelist", rolelist);
            }
            if (!((modulelist = PropertyTreeBuilder.getRequestParameter(request, requestParams, path + "_" + plindex + "__MODULES")).length() <= 0 || currentPropertyList.getPreviousModuleList() != null && currentPropertyList.getPreviousModuleList().equals(modulelist))) {
                newPropertyListNode.setAttribute("modulelist", modulelist);
            }
            for (PropertyDefinition propertyDefinition : propertyDefinitionList) {
                PropertyDefinitionList subPropertyDefinitionList;
                String propertyid = propertyDefinition.getId();
                String type = propertyDefinition.getType();
                boolean expressionable = propertyDefinition.isExpression();
                boolean resolutionable = propertyDefinition.isResolution();
                Element property = appendto.getOwnerDocument().createElement("property");
                property.setAttribute("id", propertyid);
                property.setAttribute("type", type);
                String nextpath = path + "_" + plindex + "_" + propertyid;
                if (type.equals("simple")) {
                    String value = PropertyTreeBuilder.getRequestParameter(request, requestParams, nextpath);
                    if (value.contains("\r")) {
                        value = StringUtil.replaceAll(value, "\r", "");
                    }
                    if (resolutionable) {
                        boolean isRes = "Y".equals(PropertyTreeBuilder.getRequestParameter(request, requestParams, nextpath + "__RES"));
                        ArrayList<Browser.GUIMode> modes = browser.getGUIModes();
                        if (isRes) {
                            boolean allEmpty = true;
                            boolean isAllAncestor = true;
                            for (Browser.GUIMode mode : modes) {
                                String modeid = mode.getId();
                                String guiValue = PropertyTreeBuilder.getRequestParameter(request, requestParams, nextpath + "_" + modeid);
                                if (guiValue != null && guiValue.length() > 0) {
                                    allEmpty = false;
                                }
                                if (guiValue.startsWith("{|") && guiValue.endsWith("|}")) continue;
                                isAllAncestor = false;
                            }
                            if (allEmpty && !isAllAncestor && value.contains(":|:")) {
                                value = "";
                            } else {
                                String emptyValue = "";
                                if (allEmpty) {
                                    emptyValue = value;
                                    emptyValue = PropertyTreeBuilder.removeAncestor(emptyValue);
                                }
                                value = "";
                                for (Browser.GUIMode mode : modes) {
                                    String modeid = mode.getId();
                                    String guiValue = PropertyTreeBuilder.getRequestParameter(request, requestParams, nextpath + "_" + modeid);
                                    guiValue = PropertyTreeBuilder.removeAncestor(guiValue);
                                    if (emptyValue.length() > 0 && !emptyValue.startsWith("$R{")) {
                                        guiValue = emptyValue;
                                    }
                                    if (value.length() > 0) {
                                        value = value + ":|:";
                                    }
                                    value = value + modeid + ":" + guiValue;
                                }
                                value = "$R{" + value + "}";
                                if (isAllAncestor) {
                                    value = "{|" + value + "|}";
                                }
                            }
                        } else if (modes != null && modes.size() > 0) {
                            String modeid = modes.get(0).getId();
                            if (value.startsWith("$R{") && PropertyTreeBuilder.getRequestParameter(request, requestParams, nextpath + "_" + modeid) != null) {
                                value = PropertyTreeBuilder.getRequestParameter(request, requestParams, nextpath + "_" + modeid);
                            }
                        }
                    }
                    String lockedSimple = PropertyTreeBuilder.getRequestParameter(request, requestParams, nextpath + "__LOCKED");
                    String treevalue = currentPropertyList.getProperty(propertyid);
                    if (expressionable) {
                        String expression = PropertyTreeBuilder.getRequestParameter(request, requestParams, nextpath + "__EXPRESSION");
                        String expressionancestor = PropertyTreeBuilder.getRequestParameter(request, requestParams, nextpath + "__EXPRESSIONANCESTOR");
                        if (!(expression.length() <= 0 || "Y".equals(expressionancestor) || value != null && value.length() != 0 && value.equals("{|" + treevalue + "|}"))) {
                            value = "$G{" + expression + "}";
                        }
                    }
                    if (!(!"id".equals(propertyid) || value != null && value.length() != 0 || treevalue != null && treevalue.length() != 0)) {
                        String prefix = propertyDefinitionList.getLabelSingular();
                        if (prefix == null || prefix.length() == 0) {
                            prefix = "item";
                        }
                        int count = ptreecollection.size();
                        while (ptreecollection.find("id", value = prefix.toLowerCase() + ++count) != null || XPathAPI.selectSingleNode((Node)appendto, (String)("propertylist[property[@id='id']=\"" + value + "\"]")) != null) {
                        }
                    }
                    if (value == null || value.length() <= 0 || value.equals("{|" + treevalue + "|}")) continue;
                    if ((value = PropertyTreeBuilder.removeAncestor(value)).startsWith("HEX_") && pageContext.getSession().getAttribute(value) != null) {
                        value = (String)pageContext.getSession().getAttribute(value);
                    }
                    if (value.length() > 0 && propertyDefinition.getEditor().equals("StringEditor") && "Y".equals(propertyDefinition.getAttributes().get("encrypt")) && !value.startsWith("__!ENC!__")) {
                        value = "__!ENC!__" + EncryptDecrypt.encrypt(value, new ConnectionProcessor(pageContext).getSapphireConnection().getDatabaseId());
                    }
                    if (value.contains("\r")) {
                        value = StringUtil.replaceAll(value, "\r", "");
                    }
                    property.appendChild(appendto.getOwnerDocument().createTextNode(value));
                    if ("Y".equals(lockedSimple)) {
                        property.setAttribute("locked", lockedSimple);
                    }
                    newPropertyListNode.appendChild(property);
                    continue;
                }
                if (type.equals("propertylist")) {
                    subPropertyDefinitionList = propertyDefinition.getPropertyDefinitionList();
                    String expanded = PropertyTreeBuilder.getRequestParameter(request, requestParams, nextpath + "__EXPANDED");
                    property.setAttribute("expanded", expanded.length() == 0 ? "Y" : expanded);
                    PropertyListCollection c = new PropertyListCollection();
                    c.add(currentPropertyList.getPropertyList(propertyid));
                    idseed = PropertyTreeBuilder.buildPropertyValueTree(pageContext, node, c, false, subPropertyDefinitionList, property, nextpath, idseed, isnode, deleteList, renameList, extraPropertyLists);
                    newPropertyListNode.appendChild(property);
                    continue;
                }
                if (!type.equals("collection")) continue;
                subPropertyDefinitionList = propertyDefinition.getPropertyDefinitionList();
                Element collection = appendto.getOwnerDocument().createElement("collection");
                String expanded = PropertyTreeBuilder.getRequestParameter(request, requestParams, nextpath + "__EXPANDED");
                property.setAttribute("expanded", expanded.length() == 0 ? "Y" : expanded);
                String selectedindex = dothis.equals("addpropertylist") && args1.equals(nextpath) ? PropertyTreeBuilder.getRequestParameter(request, requestParams, nextpath + "__PROPERTYLISTCOUNT") : PropertyTreeBuilder.getRequestParameter(request, requestParams, nextpath + "__SELECTEDINDEX");
                property.setAttribute("selectedindex", selectedindex.length() == 0 ? "0" : selectedindex);
                PropertyListCollection c = currentPropertyList.getCollectionNotNull(propertyid);
                property.appendChild(collection);
                idseed = PropertyTreeBuilder.buildPropertyValueTree(pageContext, node, c, true, subPropertyDefinitionList, collection, nextpath, idseed, isnode, deleteList, renameList, extraPropertyLists);
                newPropertyListNode.appendChild(property);
            }
            appendto.appendChild(newPropertyListNode);
            if (!dothis.equals("copypropertylist") || !args1.equals(path) || !((Boolean)selected.get(plindex)).booleanValue()) continue;
            selectedXML.append(";" + newPropertyListNode.getAttribute("id"));
        }
        if (dothis.equals("pastepropertylist") && pastePath.equals(path) && extraPropertyLists != null) {
            Iterator iterator = extraPropertyLists.iterator();
            while (iterator.hasNext()) {
                String propertylistid;
                if (isnode && node != null) {
                    ++idseed;
                    while (XPathAPI.selectSingleNode((Node)node, (String)("//propertylist[@id='" + idseed + "']")) != null || XPathAPI.selectSingleNode((Node)appendto, (String)("//propertylist[@id='" + idseed + "']")) != null) {
                        idseed = System.currentTimeMillis();
                    }
                    propertylistid = Long.toString(idseed);
                } else {
                    ++idseed;
                    while (XPathAPI.selectSingleNode((Node)appendto, (String)("//propertylist[@id='p" + idseed + "']")) != null) {
                        idseed = System.currentTimeMillis();
                    }
                    propertylistid = "p" + idseed;
                }
                Element propertylist = (Element)iterator.next();
                propertylist.setAttribute("sequence", Integer.toString(maxsequence += 1000000));
                propertylist.setAttribute("id", propertylistid);
                appendto.appendChild(propertylist);
            }
        }
        if (selectedXML.length() > 0) {
            pageContext.getSession().setAttribute("collectionitempaste", (Object)(path + selectedXML.toString()));
        }
        return idseed;
    }

    private static String removeAncestor(String value) {
        if (value == null) {
            return "";
        }
        if (value.startsWith("{|") && value.endsWith("|}")) {
            value = value.substring("{|".length(), value.length() - "|}".length());
        }
        return value;
    }

    public static PropertyList buildExportPropertyList(PropertyList reference, String path, Set includeList) {
        String propertylistid = reference.getId();
        PropertyList newPropertyList = new PropertyList(propertylistid);
        newPropertyList.setAttributes(reference.getAttributes());
        newPropertyList.setUsePropertyValues(true);
        Set properties = reference.keySet();
        for (String propertyid : properties) {
            PropertyList subReference;
            PropertyList newsubpropertylist;
            String fieldid = path + "_" + propertyid;
            if (reference.isSimple(propertyid)) {
                if (includeList.contains(fieldid)) {
                    newPropertyList.setProperty(propertyid, "dummy");
                }
            } else if (reference.isPropertyList(propertyid) && (newsubpropertylist = PropertyTreeBuilder.buildExportPropertyList(subReference = reference.getPropertyList(propertyid), fieldid + "_0", includeList)) != null) {
                newPropertyList.put(propertyid, newsubpropertylist);
            }
            if (!reference.isCollection(propertyid)) continue;
            PropertyListCollection collectionReference = reference.getCollection(propertyid);
            PropertyListCollection newCollection = new PropertyListCollection();
            for (int i = 0; i < collectionReference.size(); ++i) {
                PropertyList subReference2 = (PropertyList)collectionReference.get(i);
                PropertyList newsubpropertylist2 = PropertyTreeBuilder.buildExportPropertyList(subReference2, fieldid + "_" + i, includeList);
                if (newsubpropertylist2 == null) continue;
                newCollection.add(newsubpropertylist2);
            }
            if (newCollection.size() <= 0) continue;
            newPropertyList.put(propertyid, newCollection);
        }
        return newPropertyList.size() > 0 ? newPropertyList : null;
    }

    public static String modifyNodes(String dothis, String args1, String args2, String selectednodeid, PageContext pageContext, Document document, Node ptreenode, String ptreeid, String selectedpageid, String selectededition, String selectedelementid) throws Exception {
        boolean isEditable;
        ConfigurationProcessor config = new ConfigurationProcessor(pageContext);
        boolean isDevMode = "Y".equals(config.getSysConfigProperty("devmode"));
        boolean isImplMode = "Y".equals(config.getSysConfigProperty("implmode"));
        String compCode = isDevMode ? "" : config.getSysConfigProperty("compcode");
        WebAdminProcessor wp = new WebAdminProcessor(pageContext);
        boolean devProtectedNode = selectednodeid.endsWith(" Product") || selectednodeid.endsWith(" Custom") && !selectednodeid.endsWith(" Comp Custom");
        boolean implProtectedNode = selectednodeid.endsWith(" Impl") || selectednodeid.endsWith(" ImplCustom");
        boolean bl = isEditable = isDevMode || isImplMode && !devProtectedNode || !devProtectedNode && !implProtectedNode;
        if (ptreenode == null) {
            ptreenode = wp.loadPropertyTreeValues(ptreeid);
            document = ptreenode.getOwnerDocument();
        }
        if (dothis.equals("rename")) {
            Element node;
            if (isEditable && (node = DOMUtil.findNode(ptreenode, selectednodeid)) != null) {
                node.setAttribute("id", args1);
                wp.renameWebPagePropertyTreeNode(ptreeid, selectednodeid, args1);
                selectednodeid = args1;
            }
        } else if (dothis.equals("nodecategories")) {
            Element node = DOMUtil.findNode(ptreenode, selectednodeid);
            if (node != null) {
                node.setAttribute("categorylist", args1);
            }
        } else if (dothis.equals("autosort")) {
            NodeList nodelists = XPathAPI.selectNodeList((Node)ptreenode, (String)"//nodelist");
            for (int i = 0; i < nodelists.getLength(); ++i) {
                Node nodelist = nodelists.item(i);
                List childElements = DOMUtil.getChildElements(nodelist, "node");
                Collections.sort(childElements, new Comparator<Element>(){

                    @Override
                    public int compare(Element o1, Element o2) {
                        return o1.getAttribute("id").compareTo(o2.getAttribute("id"));
                    }
                });
                while (nodelist.hasChildNodes()) {
                    nodelist.removeChild(nodelist.getFirstChild());
                }
                for (Node node : childElements) {
                    nodelist.appendChild(node);
                }
            }
            selectednodeid = "__root";
        } else if (dothis.equals("togglelock")) {
            if (isDevMode || isImplMode && !devProtectedNode) {
                Element node = DOMUtil.findNode(ptreenode, selectednodeid);
                if (node != null) {
                    String newLockStatus = "Y".equals(node.getAttribute("locked")) ? "N" : "Y";
                    node.setAttribute("locked", newLockStatus);
                } else {
                    selectednodeid = "__root";
                }
            }
        } else if (dothis.equals("moveup")) {
            Element node = DOMUtil.findNode(ptreenode, selectednodeid);
            if (node != null) {
                Node previous;
                for (previous = node.getPreviousSibling(); previous != null && !previous.getNodeName().equals("node"); previous = previous.getPreviousSibling()) {
                }
                Node parentnodelist = XPathAPI.selectSingleNode((Node)node, (String)"..");
                if (previous != null && parentnodelist != null) {
                    parentnodelist.removeChild(node);
                    parentnodelist.insertBefore(node, previous);
                }
            }
        } else if (dothis.equals("movedown")) {
            Element node = DOMUtil.findNode(ptreenode, selectednodeid);
            if (node != null) {
                Node next;
                for (next = node.getNextSibling(); next != null && !next.getNodeName().equals("node"); next = next.getNextSibling()) {
                }
                Node parentnodelist = XPathAPI.selectSingleNode((Node)node, (String)"..");
                if (next != null && parentnodelist != null) {
                    parentnodelist.removeChild(next);
                    parentnodelist.insertBefore(next, node);
                }
            }
        } else if (dothis.equals("delete")) {
            if (isEditable) {
                Element node = DOMUtil.findNode(ptreenode, selectednodeid);
                if (node != null) {
                    Node parentnodelist = XPathAPI.selectSingleNode((Node)node, (String)"..");
                    if (parentnodelist != null) {
                        parentnodelist.removeChild(node);
                        wp.renameWebPagePropertyTreeNode(ptreeid, selectednodeid, "__root");
                        selectednodeid = "__root";
                        NodeList subnodes = XPathAPI.selectNodeList((Node)node, (String)"nodelist/node");
                        for (int ii = 0; ii < subnodes.getLength(); ++ii) {
                            Node childnode = subnodes.item(ii);
                            parentnodelist.appendChild(childnode);
                            if (!selectednodeid.equals("__root")) continue;
                            selectednodeid = ((Element)childnode).getAttribute("id");
                        }
                    }
                } else {
                    selectednodeid = "__root";
                }
            }
        } else if (dothis.equals("addchild")) {
            Element node = selectednodeid.equals("__root") ? (Element)ptreenode : DOMUtil.findNode(ptreenode, selectednodeid);
            Element checknode = DOMUtil.findNode(ptreenode, args1);
            if (node != null && checknode == null) {
                Element nodelist = (Element)XPathAPI.selectSingleNode((Node)node, (String)"nodelist");
                if (nodelist == null) {
                    nodelist = document.createElement("nodelist");
                    node.appendChild(nodelist);
                }
                Element newnode = document.createElement("node");
                newnode.setAttribute("id", args1);
                Element newpropertylist = document.createElement("propertylist");
                newpropertylist.setAttribute("id", "root");
                newnode.appendChild(newpropertylist);
                nodelist.appendChild(newnode);
                selectednodeid = args1;
            } else {
                selectednodeid = "";
            }
        } else if (dothis.equals("addchild")) {
            Element node = selectednodeid.equals("__root") ? (Element)ptreenode : DOMUtil.findNode(ptreenode, selectednodeid);
            Element checknode = DOMUtil.findNode(ptreenode, args1);
            if (node != null && checknode == null) {
                Element nodelist = (Element)XPathAPI.selectSingleNode((Node)node, (String)"nodelist");
                if (nodelist == null) {
                    nodelist = document.createElement("nodelist");
                    node.appendChild(nodelist);
                }
                Element newnode = document.createElement("node");
                newnode.setAttribute("id", args1);
                Element newpropertylist = document.createElement("propertylist");
                newpropertylist.setAttribute("id", "root");
                newnode.appendChild(newpropertylist);
                nodelist.appendChild(newnode);
                selectednodeid = args1;
                if (args1.endsWith(" Product") && isDevMode) {
                    newnode.setAttribute("locked", "Y");
                    Element productnodelist = document.createElement("nodelist");
                    newnode.appendChild(productnodelist);
                    Element customnode = document.createElement("node");
                    customnode.setAttribute("id", args1.substring(0, args1.length() - 7) + " Custom");
                    Element custompropertylist = document.createElement("propertylist");
                    custompropertylist.setAttribute("id", "root");
                    customnode.appendChild(custompropertylist);
                    productnodelist.appendChild(customnode);
                }
            } else {
                selectednodeid = "";
            }
        } else if (dothis.equals("addcomponentnode") && compCode.length() > 0 && compCode.equals(args2)) {
            Element parentNode = selectednodeid.equals("__root") ? (Element)ptreenode : DOMUtil.findNode(ptreenode, selectednodeid);
            String newnodeid = args1 + " Comp " + compCode;
            Element checknode = DOMUtil.findNode(ptreenode, newnodeid);
            if (parentNode != null && checknode == null) {
                if (parentNode != null && checknode == null) {
                    Element nodelist = (Element)XPathAPI.selectSingleNode((Node)parentNode, (String)"nodelist");
                    if (nodelist == null) {
                        nodelist = document.createElement("nodelist");
                        parentNode.appendChild(nodelist);
                    }
                    Element newnode = document.createElement("node");
                    newnode.setAttribute("id", newnodeid);
                    newnode.setAttribute("locked", "Y");
                    Element newpropertylist = document.createElement("propertylist");
                    newpropertylist.setAttribute("id", "root");
                    newnode.appendChild(newpropertylist);
                    nodelist.appendChild(newnode);
                    selectednodeid = newnodeid;
                    Element newnodelist = document.createElement("nodelist");
                    newnode.appendChild(newnodelist);
                    Element customnode = document.createElement("node");
                    customnode.setAttribute("id", args1 + " Comp Custom");
                    Element custompropertylist = document.createElement("propertylist");
                    custompropertylist.setAttribute("id", "root");
                    customnode.appendChild(custompropertylist);
                    newnodelist.appendChild(customnode);
                } else {
                    selectednodeid = "";
                }
            }
        } else if (dothis.equals("insertcomponentnode") && compCode.length() > 0 && compCode.equals(args1)) {
            if (selectednodeid.endsWith(" Custom")) {
                Node parentnodelist;
                String newnodeid = selectednodeid.substring(0, selectednodeid.length() - 7) + " Comp " + compCode;
                Element node = DOMUtil.findNode(ptreenode, selectednodeid);
                Element checknode = DOMUtil.findNode(ptreenode, newnodeid);
                if (node != null && checknode == null && (parentnodelist = XPathAPI.selectSingleNode((Node)node, (String)"..")) != null) {
                    Element newnode = document.createElement("node");
                    newnode.setAttribute("id", newnodeid);
                    newnode.setAttribute("locked", "Y");
                    Element newpropertylist = document.createElement("propertylist");
                    newpropertylist.setAttribute("id", "root");
                    newnode.appendChild(newpropertylist);
                    Element newnodelist = document.createElement("nodelist");
                    newnodelist.appendChild(node);
                    newnode.appendChild(newnodelist);
                    parentnodelist.appendChild(newnode);
                    selectednodeid = newnodeid;
                }
            }
        } else if (dothis.equals("insertparent")) {
            Node parentnodelist;
            Element node;
            if (isEditable && (node = DOMUtil.findNode(ptreenode, selectednodeid)) != null && (parentnodelist = XPathAPI.selectSingleNode((Node)node, (String)"..")) != null) {
                parentnodelist.removeChild(node);
                Element newnode = document.createElement("node");
                newnode.setAttribute("id", args1);
                Element newpropertylist = document.createElement("propertylist");
                newpropertylist.setAttribute("id", "root");
                newnode.appendChild(newpropertylist);
                Element newnodelist = document.createElement("nodelist");
                newnodelist.appendChild(node);
                newnode.appendChild(newnodelist);
                parentnodelist.appendChild(newnode);
                selectednodeid = args1;
            }
        } else if (dothis.equals("copynode")) {
            Node newnode;
            Element parentnodelist;
            Element node = DOMUtil.findNode(ptreenode, selectednodeid);
            if (node != null && (parentnodelist = (Element)XPathAPI.selectSingleNode((Node)node, (String)"..")) != null && (newnode = node.cloneNode(true)) != null) {
                ((Element)newnode).setAttribute("id", args1);
                if (args2.equals("Y")) {
                    Node findnode;
                    ArrayList allnodes = DOMUtil.getAllNodes(ptreenode);
                    NodeIterator it = XPathAPI.selectNodeIterator((Node)newnode, (String)".//node");
                    while ((findnode = it.nextNode()) != null) {
                        String currentid = ((Element)findnode).getAttribute("id");
                        int extra = 1;
                        while (allnodes.contains(currentid + "_" + extra)) {
                            ++extra;
                        }
                        ((Element)findnode).setAttribute("id", currentid + "_" + extra);
                        allnodes.add(currentid + "_" + extra);
                    }
                } else {
                    Node newnodelist = XPathAPI.selectSingleNode((Node)newnode, (String)"nodelist");
                    if (newnodelist != null) {
                        newnode.removeChild(newnodelist);
                    }
                }
                parentnodelist.appendChild(newnode);
                selectednodeid = args1;
            }
        } else if (dothis.equals("movenode")) {
            Element node = DOMUtil.findNode(ptreenode, selectednodeid);
            Element targetnode = args1.equals("__root") ? (Element)ptreenode : DOMUtil.findNode(ptreenode, args1);
            if (node != null && targetnode != null) {
                NodeList subnodes = XPathAPI.selectNodeList((Node)node, (String)"//collection/propertylist[@descendant='true']");
                for (int ii = 0; ii < subnodes.getLength(); ++ii) {
                    Element parentcollection = (Element)XPathAPI.selectSingleNode((Node)subnodes.item(ii), (String)"..");
                    if (parentcollection == null) continue;
                    parentcollection.removeChild(subnodes.item(ii));
                }
                Element parentnodelist = (Element)XPathAPI.selectSingleNode((Node)node, (String)"..");
                if (parentnodelist != null) {
                    parentnodelist.removeChild(node);
                    Element targetnodelist = (Element)XPathAPI.selectSingleNode((Node)targetnode, (String)"nodelist");
                    if (targetnodelist == null) {
                        targetnodelist = document.createElement("nodelist");
                        targetnode.appendChild(targetnodelist);
                    }
                    targetnodelist.appendChild(node);
                }
            }
        } else if (dothis.equals("movepage")) {
            boolean isChild;
            QueryProcessor qp = new QueryProcessor(pageContext);
            SafeSQL safeSQL = new SafeSQL();
            boolean isParent = qp.getPreparedCount("SELECT count(*) from webpage WHERE extendwebpageid=" + safeSQL.addVar(selectedpageid) + " AND extendproductedition=" + safeSQL.addVar(selectededition), safeSQL.getValues()) > 0;
            safeSQL.reset();
            boolean bl2 = isChild = qp.getPreparedCount("SELECT count(*) from webpage WHERE webpageid=" + safeSQL.addVar(selectedpageid) + " AND productedition=" + safeSQL.addVar(selectededition) + " AND COALESCE( extendwebpageid, '' ) <> ''", safeSQL.getValues()) > 0;
            if (!isParent && !isChild) {
                Element pagepropertylist = wp.isProductPage(selectedpageid, selectededition) ? wp.loadPageProductValues(selectedpageid, selectededition, ptreeid, selectedelementid) : wp.loadPageValues(selectedpageid, selectededition, ptreeid, selectedelementid);
                if (pagepropertylist != null) {
                    NodeList subnodes = XPathAPI.selectNodeList((Node)pagepropertylist, (String)"//collection/propertylist");
                    for (int ii = 0; ii < subnodes.getLength(); ++ii) {
                        Element parentcollection;
                        Node node = subnodes.item(ii);
                        if (node.getChildNodes().getLength() != 0 || (parentcollection = (Element)XPathAPI.selectSingleNode((Node)node, (String)"..")) == null) continue;
                        parentcollection.removeChild(node);
                    }
                }
                wp.savePageValues(selectedpageid, selectededition, ptreeid, selectedelementid, pagepropertylist, null);
                wp.moveWebPagePropertyTreePage(selectedpageid, selectededition, selectedelementid, ptreeid, args1);
            } else {
                Trace.logError("You cannot move a parent or child page");
            }
        } else if (dothis.equals("addcomponentpageoverride")) {
            if (compCode.length() > 0) {
                wp.setComponentPageOverride(selectedpageid, selectededition, ptreeid, selectedelementid, compCode, "<propertylist/>");
            }
        } else if (dothis.equals("removecomponentpageoverride")) {
            if (compCode.length() > 0) {
                wp.removeComponentPageOverride(selectedpageid, selectededition, ptreeid, selectedelementid, compCode);
            }
        } else {
            SafeSQL safeSQL = new SafeSQL();
            String sql = "select extendnodeid from webpagepropertytree where webpageid=" + safeSQL.addVar(selectedpageid) + " and productedition=" + safeSQL.addVar(selectededition) + " and elementid = " + safeSQL.addVar(selectedelementid) + " and propertytreeid=" + safeSQL.addVar(ptreeid);
            if (dothis.equals("promotepage")) {
                boolean isChild;
                QueryProcessor qp = new QueryProcessor(pageContext);
                boolean isParent = qp.getPreparedCount("SELECT count(*) from webpage WHERE extendwebpageid=? AND extendproductedition=?", new Object[]{selectedpageid, selectededition}) > 0;
                boolean bl3 = isChild = qp.getPreparedCount("SELECT count(*) from webpage WHERE webpageid=? AND productedition=? AND extendwebpageid is not null", new Object[]{selectedpageid, selectededition}) > 0;
                if (!isParent && !isChild) {
                    DataSet dsextendnode = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
                    if (dsextendnode.getRowCount() == 1) {
                        String extendnodeid = dsextendnode.getString(0, "extendnodeid");
                        Element node = extendnodeid.equals("__root") ? (Element)ptreenode : DOMUtil.findNode(ptreenode, extendnodeid);
                        Element checknode = DOMUtil.findNode(ptreenode, args1);
                        if (node != null && checknode == null) {
                            Element nodelist = (Element)XPathAPI.selectSingleNode((Node)node, (String)"nodelist");
                            if (nodelist == null) {
                                nodelist = document.createElement("nodelist");
                                node.appendChild(nodelist);
                            }
                            Element newnode = document.createElement("node");
                            newnode.setAttribute("id", args1);
                            Element pagepropertylist = wp.isProductPage(selectedpageid, selectededition) ? wp.loadPageProductValues(selectedpageid, selectededition, ptreeid, selectedelementid) : wp.loadPageValues(selectedpageid, selectededition, ptreeid, selectedelementid);
                            newnode.appendChild(document.importNode(pagepropertylist, true));
                            pagepropertylist = pagepropertylist.getOwnerDocument().createElement("propertylist");
                            wp.savePageValues(selectedpageid, selectededition, ptreeid, selectedelementid, pagepropertylist, null);
                            wp.moveWebPagePropertyTreePage(selectedpageid, selectededition, selectedelementid, ptreeid, args1);
                            nodelist.appendChild(newnode);
                        }
                    }
                } else {
                    Trace.logError("You cannot promote a parent or child page");
                }
            } else if (dothis.equals("collapsepage")) {
                boolean isChild;
                QueryProcessor qp = new QueryProcessor(pageContext);
                boolean isParent = qp.getPreparedCount("SELECT count(*) from webpage WHERE extendwebpageid=? AND extendproductedition=?", new Object[]{selectedpageid, selectededition}) > 0;
                boolean bl4 = isChild = qp.getPreparedCount("SELECT count(*) from webpage WHERE webpageid=? AND productedition=? AND extendwebpageid is not null", new Object[]{selectedpageid, selectededition}) > 0;
                if (!isParent && !isChild) {
                    DataSet dsextendnode = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
                    if (dsextendnode.getRowCount() == 1) {
                        String extendnodeid = dsextendnode.getString(0, "extendnodeid");
                        Element pagepropertylist = wp.loadPageValues(selectedpageid, selectededition, ptreeid, selectedelementid);
                        Element pageproductpropertylist = wp.loadPageProductValues(selectedpageid, selectededition, ptreeid, selectedelementid);
                        PropertyList propertylist = new PropertyList();
                        propertylist.setUsePropertyValues(true);
                        if (!extendnodeid.equals("__root")) {
                            PropertyDefinitionList propertyDefinitionList = wp.getPropertyDefinitionList(ptreeid);
                            propertylist.setPropertyTree(ptreenode, extendnodeid, false, propertyDefinitionList);
                            propertylist.addPropertyList(pageproductpropertylist, true, extendnodeid);
                            propertylist.addPropertyList(pagepropertylist, true, extendnodeid);
                            propertylist.setPropertyTreeDefaults(XPathAPI.selectSingleNode((Node)ptreenode, (String)"propertydefaultlist"), propertyDefinitionList);
                            wp.savePageValues(selectedpageid, selectededition, ptreeid, selectedelementid, propertylist, null);
                            wp.moveWebPagePropertyTreePage(selectedpageid, selectededition, selectedelementid, ptreeid, "__root");
                        }
                    }
                } else {
                    Trace.logError("You cannot collapse a parent or child page");
                }
            }
        }
        return selectednodeid;
    }

    public static String drawPropertyDefTree(Element propertydeflist, int indent, String parentid, StringBuffer simpleeditorlist, StringBuffer propertylisteditorlist, PageContext pageContext, boolean readonly) {
        StringBuffer output = new StringBuffer();
        String name = parentid + "__";
        TranslationProcessor tp = new TranslationProcessor(pageContext);
        try {
            NamedNodeMap defm = propertydeflist.getAttributes();
            StringBuffer defattriblist = new StringBuffer();
            for (int j = 0; j < defm.getLength(); ++j) {
                String attributeid = defm.item(j).getNodeName();
                defattriblist.append(attributeid).append("=").append(defm.item(j).getNodeValue()).append("\n");
            }
            output.append("<input type=\"hidden\" name=\"").append(name).append("DEFATTRIBUTES\" id=\"").append(name).append("DEFATTRIBUTES\" value=\"").append(defattriblist).append("\">");
            output.append("<table style=\"width:100%\" border=\"1\" cellspacing=\"0\" cellpadding=\"0\" class=\"propertytable\">");
            output.append("<tr height=20><td colspan=\"12\" class=\"propertylisttitle\">");
            output.append("<table cellpadding=\"0\" cellspacing=\"0\"><tr><td>");
            output.append("<b>").append(propertydeflist.getAttribute("labelsingular")).append(" ").append(tp.translate("Properties")).append("</b>");
            output.append("</td>");
            if (!readonly) {
                output.append("<td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a title=\"Edit Property List Display Options\" href=\"javascript:editPropertyListAttributes( '").append(name).append("' );sapphire.events.cancelEvent(event, false);\">" + tp.translate("Display Options") + "</a></td>");
                output.append("<td>&nbsp;<a title=\"Edit Attributes\" href=\"\" onClick=\"editPropertyListAttributes( '").append(name).append("' );sapphire.events.cancelEvent(event, false);\"><img style=\"border-color: black; border-width: 1px\" src=\"WEB-CORE/modules/webadmin/images/ellipsis.gif\"></a></td>");
            }
            output.append("</tr></table></td></tr>");
            output.append("<tr height=20>");
            output.append("<td style=\"min-width: 100px\" class=\"propertylisttitle\">" + tp.translate("Property Id") + "</td>");
            output.append("<td style=\"min-width: 100px\" class=\"propertylisttitle\">" + tp.translate("Title") + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>");
            output.append("<td style=\"min-width: 100px\" class=\"propertylisttitle\">" + tp.translate("Show If") + "</td>");
            output.append("<td style=\"min-width: 80px\" class=\"propertylisttitle\">" + tp.translate("Editor") + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>");
            output.append("<td style=\"min-width: 150px\" class=\"propertylisttitle\">" + tp.translate("Editor Attributes") + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>");
            output.append("<td class=\"propertylisttitle\">Default Value&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>");
            output.append("<td style=\"width: 10px\" class=\"propertylisttitle\">" + tp.translate("Adv") + "</td>");
            output.append("<td style=\"width: 10px\" class=\"propertylisttitle\">" + tp.translate("Trans") + "</td>");
            output.append("<td style=\"width: 10px\" class=\"propertylisttitle\">" + tp.translate("Dep") + "</td>");
            output.append("<td style=\"width: 10px\" class=\"propertylisttitle\">" + tp.translate("Expr") + "</td>");
            output.append("<td style=\"width: 10px\" class=\"propertylisttitle\">" + tp.translate("Res") + "</td>");
            output.append("<td width=\"*\" style=\"min-width: 100px\" class=\"propertylisttitle\">" + tp.translate("Help") + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>");
            output.append("</tr>");
            List propertydefs = DOMUtil.getChildElements(propertydeflist, "propertydef");
            output.append("<input type=\"hidden\" name=\"").append(name).append("PROPERTYDEFCOUNT\" value=\"").append(propertydefs.size()).append("\">");
            for (int i = 0; i < propertydefs.size(); ++i) {
                String pname = name + i + "__";
                Element propertydef = (Element)propertydefs.get(i);
                String propertyid = propertydef.getAttribute("id");
                String type = propertydef.getAttribute("type");
                NamedNodeMap m = propertydef.getAttributes();
                StringBuffer attriblist = new StringBuffer();
                HashMap<String, String> attribMap = new HashMap<String, String>();
                for (int j = 0; j < m.getLength(); ++j) {
                    String attributeid = m.item(j).getNodeName();
                    if (";id;title;type;editor;advanced;translate;deprecate;expression;resolution;showif;help;defaultvalue;".indexOf(";" + attributeid + ";") != -1) continue;
                    attriblist.append(attributeid).append("=").append(m.item(j).getNodeValue()).append("\n");
                    attribMap.put(attributeid, m.item(j).getNodeValue());
                }
                output.append("<tr valign=\"top\"><td ").append(!type.equals("simple") ? "rowspan=\"2\"" : "").append(" bgcolor=\"").append(propertydeflist.getAttribute("color").equals("") ? "#EFEFEF" : propertydeflist.getAttribute("color")).append("\">");
                if (readonly) {
                    output.append(propertyid);
                } else {
                    output.append("<table width=\"100%\" cellpadding=0 cellspacing=0><tr>");
                    output.append("<td>").append(propertyid).append(!type.equals("simple") ? "<br>(" + type + ")" : "").append("</td>");
                    output.append("<td align=\"right\">&nbsp;<a title=\"Move up (Ctrl-click for options)\" href=\"#\" onclick=\"moveProperty( 'moveback', '").append(parentid).append("', ").append(i).append(", event );sapphire.events.cancelEvent(event, false);\"><img style=\"border-color: black; border-width: 1px\" src=\"WEB-CORE/modules/webadmin/images/up.gif\"></a>");
                    output.append("<a title=\"Move down  (Ctrl-click for options)\" href=\"#\" onclick=\"moveProperty( 'moveforward', '").append(parentid).append("', ").append(i).append(",event );sapphire.events.cancelEvent(event, false);\"><img style=\"border-color: black; border-width: 1px\" src=\"WEB-CORE/modules/webadmin/images/down.gif\"></a>&nbsp;");
                    output.append("<a title=\"Delete property\" href=\"javascript:deleteProperty( '").append(parentid).append("', ").append(i).append(" );sapphire.events.cancelEvent(event, false);\"><img style=\"border-color: black; border-width: 1px\" src=\"WEB-CORE/modules/webadmin/images/close.gif\"></a>");
                    output.append("</td></tr></table>");
                }
                output.append("</td>");
                output.append("<input type=\"hidden\" name=\"").append(pname).append("id\" id=\"").append(pname).append("id\" value=\"").append(propertyid).append("\">");
                output.append("<input type=\"hidden\" name=\"").append(pname).append("type\" id=\"").append(pname).append("type\" value=\"").append(type).append("\">");
                output.append("<input type=\"hidden\" name=\"").append(pname).append("attributes\" id=\"").append(pname).append("attributes\" value=\"").append(attriblist).append("\"></td>");
                output.append("<td><input onpropertychange=\"propertyChange()\" oninput=\"propertyChange()\" " + (readonly ? " readonly " : "") + " style=\"border:none; display:table-cell; width:100%\" type=\"text\" name=\"").append(pname).append("title\" value=\"").append(propertydef.getAttribute("title")).append("\"></td>");
                String showif = propertydef.getAttribute("showif");
                output.append("<td nowrap><input onpropertychange=\"propertyChange()\" oninput=\"propertyChange()\" " + (readonly ? " readonly " : "") + "title=\"" + (showif.length() > 0 ? showif + "\n\n" : "") + "propertyid=xxx or top.propertyid=xxx or parent.propertyid=xxx or propertyid=xxx;yyy (for xxx OR yyy) or propertyid=* (for Not Null) or propertyid!=xxx or (property1=xxx || property2=zzz) or (property1=xxx OR property2=zzz) or propertyid=xxx* (for values that starts with xxx) or (property1=xxx AND property2=zzz) or (property1=xxx && property2=zzz) ( With save, && converted to AND, || converted to OR ) \" style=\"border:none; display:table-cell; width: 100%\" type=\"text\" name=\"").append(pname).append("showif\" id=\"").append(pname).append("showif\" value=\"").append(showif).append("\"></td>");
                output.append("<td>");
                String editorname = propertydef.getAttribute("editor");
                if (readonly) {
                    output.append(editorname);
                } else {
                    output.append("<select onchange=\"propertyChange()\" style=\"border:none;display:table-cell; width:100%\" name=\"").append(pname).append("editor\" id=\"").append(pname).append("editor\" >");
                    String editorlist = type.equals("simple") ? simpleeditorlist.toString() : propertylisteditorlist.toString();
                    editorlist = StringUtil.replaceAll(editorlist, "<option value=\"" + editorname + "\">", "<option value=\"" + editorname + "\" selected>");
                    output.append(editorlist);
                    output.append("</select>");
                }
                output.append("</td>");
                output.append("<td style=\"background-color: #EFEFEF\"><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\"><tr>");
                output.append("<td><div id=\"").append(pname).append("attributes_display\">").append(attriblist == null || attriblist.length() == 0 ? "&nbsp;" : attriblist.toString()).append("</div></td><td align=\"right\">");
                if (!readonly) {
                    output.append("<a title=\"Edit Attributes\" href=\"javascript:editAttributes( '").append(pname).append("' );sapphire.events.cancelEvent(event, false);\"><img style=\"border-color: black; border-width: 1px\" src=\"WEB-CORE/modules/webadmin/images/ellipsis.gif\"></a>");
                }
                output.append("</td></tr></table></td>");
                if (type.equals("simple")) {
                    String defaultValue;
                    output.append("<td align = \"left\" >");
                    String string = defaultValue = propertydef.getFirstChild() == null ? "" : propertydef.getFirstChild().getNodeValue();
                    if (readonly) {
                        output.append(defaultValue);
                    } else {
                        TypeSimple editor = null;
                        PropertyValue propertyValue = new PropertyValue(propertyid, false, null);
                        propertyValue.value = defaultValue;
                        try {
                            Class<?> c = Class.forName("com.labvantage.sapphire.admin.propertytree." + editorname);
                            editor = (TypeSimple)c.newInstance();
                        }
                        catch (Exception e1) {
                            try {
                                Class<?> c = Class.forName(editorname);
                                editor = (TypeSimple)c.newInstance();
                            }
                            catch (Exception e2) {
                                output.append("Editor not found");
                                Trace.log("ERROR: DISPLAY EDITOR: Unable to diplay SimpleEditor ('" + editorname + "') for property " + propertyid);
                            }
                        }
                        if (editor == null) {
                            output.append("Editor not found");
                        } else {
                            try {
                                output.append(editor.getEditor(pname + "defaultvalue", propertyValue, null, false, attribMap, pageContext, false));
                            }
                            catch (Exception e) {
                                output.append("Failed to display Editor");
                                new Logger(pageContext).error("ERROR: DISPLAY EDITOR: Unable to diplay SimpleEditor ('" + editorname + "') for property " + propertyid, e);
                            }
                        }
                    }
                    output.append("</td>");
                } else {
                    output.append("<td>&nbsp</td>");
                }
                String advanced = propertydef.getAttribute("advanced");
                output.append("<td align=\"left\"><input onpropertychange=\"propertyChange()\" " + (readonly ? " disabled " : "") + " oninput=\"propertyChange()\" title=\"Advanced\" type=\"checkbox\" name=\"").append(pname).append("advanced\" value=\"Y\" ").append(advanced != null && advanced.equals("Y") ? "checked" : "").append("></td>");
                if (type.equals("simple")) {
                    String translate = propertydef.getAttribute("translate");
                    output.append("<td align=\"middle\"><input onpropertychange=\"propertyChange()\" " + (readonly ? " disabled " : "") + " oninput=\"propertyChange()\" title=\"Translate\" type=\"checkbox\" name=\"").append(pname).append("translate\" value=\"Y\" ").append(translate != null && translate.equals("Y") ? "checked" : "").append("></td>");
                    String deprecate = propertydef.getAttribute("deprecate");
                    output.append("<td align=\"left\"><input onpropertychange=\"propertyChange()\" " + (readonly ? " disabled " : "") + " oninput=\"propertyChange()\" title=\"Deprecated\" type=\"checkbox\" name=\"").append(pname).append("deprecate\" value=\"Y\" ").append(deprecate != null && deprecate.equals("Y") ? "checked" : "").append("></td>");
                    String expression = propertydef.getAttribute("expression");
                    output.append("<td align=\"left\"><input onpropertychange=\"propertyChange()\" " + (readonly ? " disabled " : "") + " oninput=\"propertyChange()\" title=\"Expression\" type=\"checkbox\" name=\"").append(pname).append("expression\" value=\"Y\" ").append(expression != null && expression.equals("Y") ? "checked" : "").append("></td>");
                    String resolution = propertydef.getAttribute("resolution");
                    output.append("<td align=\"left\"><input onpropertychange=\"propertyChange()\" " + (readonly ? " disabled " : "") + " oninput=\"propertyChange()\" title=\"Resolution\" type=\"checkbox\" name=\"").append(pname).append("resolution\" value=\"Y\" ").append(resolution != null && resolution.equals("Y") ? "checked" : "").append("></td>");
                } else {
                    output.append("<td colspan=\"4\">&nbsp;</td>");
                }
                String helptext = propertydef.getAttribute("help");
                output.append("<td><input onpropertychange=\"propertyChange()\" " + (readonly ? " disabled " : "") + " oninput=\"propertyChange()\" style=\"border:none; min-width: 500px\" type=\"text\" name=\"").append(pname).append("help\" value=\"").append(helptext).append("\"></td>");
                if (!type.equals("simple")) {
                    output.append("</tr><tr>");
                    Node subpropertydeflist = DOMUtil.getChildElement(propertydef, "propertydeflist");
                    if (subpropertydeflist != null) {
                        output.append("<td colspan=\"12\"><table style=\"width:100%\" cellspacing=0 cellpadding=\"10\"><tr><td>");
                        output.append(PropertyTreeBuilder.drawPropertyDefTree((Element)subpropertydeflist, indent + 1, name + propertyid, simpleeditorlist, propertylisteditorlist, pageContext, readonly));
                        output.append("</td></tr></table></td>");
                    }
                }
                output.append("</tr>");
            }
            output.append("</table><br>");
            if (!readonly && !parentid.contains("root__stepdef")) {
                Button button = new Button(pageContext);
                button.setAction("addProperty('" + parentid + "')");
                button.setText(tp.translate("Add Property..."));
                output.append(button.getHtml());
            }
        }
        catch (Exception e) {
            Trace.log("ERROR: Exception writing property def tree: " + e.getMessage());
            output.append("Exception!!!!");
        }
        return output.toString();
    }

    public static void saveRootNodeValues(HttpServletRequest request, Element propertydeflist, Element newpropertydefaultlist, String parentid) throws TransformerException {
        List propertydefs = DOMUtil.getChildElements(propertydeflist, "propertydef");
        for (Element propertydef : propertydefs) {
            String propertyid = propertydef.getAttribute("id");
            String type = propertydef.getAttribute("type");
            String editor = propertydef.getAttribute("editor");
            String encrypt = propertydef.getAttribute("encrypt");
            Element propertydefault = newpropertydefaultlist.getOwnerDocument().createElement("propertydefault");
            propertydefault.setAttribute("id", propertyid);
            propertydefault.setAttribute("type", type);
            newpropertydefaultlist.appendChild(propertydefault);
            if (type.equals("simple")) {
                String translate;
                String propertyvalue = request.getParameter(parentid + "__" + propertyid);
                if (propertyvalue != null && propertyvalue.length() > 0 && !propertyvalue.startsWith("{|") && !propertyvalue.endsWith("|}")) {
                    if ("StringEditor".equals(editor) && "Y".equals(encrypt)) {
                        if (propertyvalue.startsWith("HEX_") && request.getSession().getAttribute(propertyvalue) != null) {
                            propertyvalue = (String)request.getSession().getAttribute(propertyvalue);
                        }
                        if (!propertyvalue.startsWith("__!ENC!__")) {
                            RequestContext requestContext = (RequestContext)request.getAttribute("RequestContext");
                            String connectionid = requestContext.getConnectionId();
                            propertyvalue = "__!ENC!__" + EncryptDecrypt.encrypt(propertyvalue, new ConnectionProcessor(connectionid).getSapphireConnection().getDatabaseId());
                        }
                    }
                    propertydefault.appendChild(newpropertydefaultlist.getOwnerDocument().createCDATASection(propertyvalue));
                }
                if ((translate = request.getParameter(parentid + "__" + propertyid + "__TRANSLATE")) == null || translate.length() <= 0) continue;
                propertydefault.setAttribute("translate", translate);
                continue;
            }
            Node subpropertydeflist = DOMUtil.getChildElement(propertydef, "propertydeflist");
            Element subpropertydefaultlist = newpropertydefaultlist.getOwnerDocument().createElement("propertydefaultlist");
            propertydefault.appendChild(subpropertydefaultlist);
            if (subpropertydeflist == null) continue;
            PropertyTreeBuilder.saveRootNodeValues(request, (Element)subpropertydeflist, subpropertydefaultlist, parentid + "__" + propertyid);
        }
    }
}

