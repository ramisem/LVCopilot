/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.util;

import com.labvantage.opal.actions.storageunit.AddStorageUnit;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.SecurityService;
import com.labvantage.sapphire.util.cache.CacheUtil;
import com.labvantage.sapphire.xml.Node;
import com.labvantage.sapphire.xml.PropertyTree;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class StorageUnitTypeDef {
    private static final StorageUnitTypeDef ourInstance = new StorageUnitTypeDef();

    public static StorageUnitTypeDef getInstance() {
        return ourInstance;
    }

    private StorageUnitTypeDef() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public String refreshDefinition(QueryProcessor queryProcessor, boolean reset) {
        boolean populate = false;
        String databaseid = "";
        if (queryProcessor != null && (databaseid = SecurityService.getDatabaseId(queryProcessor.getConnectionid())) != null && databaseid.length() > 0) {
            if (reset) {
                populate = true;
                CacheUtil.clear(databaseid, "storageunittype_nodemap");
            } else {
                int size = CacheUtil.getCacheSize(databaseid, "storageunittype_nodemap");
                if (size == 0) {
                    populate = true;
                }
            }
        }
        if (!populate) return databaseid;
        Class<StorageUnitTypeDef> clazz = StorageUnitTypeDef.class;
        synchronized (StorageUnitTypeDef.class) {
            List<String> boxTemplateList = OpalUtil.toUniqueList(queryProcessor.getSqlDataSet("select s_boxid from s_box where templateflag = 'Y'").getColumnValues("s_boxid", ";"), ";");
            DataSet ds = queryProcessor.getSqlDataSet("select propertytreeid, valuetree, definitiontree from PROPERTYTREE where PROPERTYTREETYPE = 'StorageUnitType'", true);
            if (ds != null && ds.size() > 0) {
                DataSet newBoxTemplateDS = new DataSet();
                for (int i = 0; i < ds.size(); ++i) {
                    String valuetree = ds.getString(i, "valuetree");
                    String definitionTree = ds.getClob(i, "definitiontree");
                    PropertyTree propertyTree = new PropertyTree();
                    try {
                        propertyTree.setValueXML(valuetree);
                        propertyTree.setDefinitionXML(definitionTree);
                        ArrayList allNodes = propertyTree.getAllNodes();
                        for (Object o : allNodes) {
                            PropertyListCollection collection;
                            String boxtemplateid;
                            Node node = (Node)o;
                            String nodeid = node.getNodeId();
                            if (node.isProduct() || node.isCustom()) continue;
                            PropertyList propertyList = propertyTree.getNodePropertyList(nodeid, true);
                            propertyList.setProperty("nodeid", node.getNodeId());
                            propertyList.setProperty("propertytreeid", ds.getString(i, "propertytreeid"));
                            CacheUtil.put(databaseid, "storageunittype_nodemap", node.getNodeId(), propertyList);
                            PropertyList templateList = propertyList.getPropertyListNotNull("template");
                            if (!"LV_Box".equals(templateList.getProperty("sdcid")) || (boxtemplateid = templateList.getProperty("keyid1")).trim().length() <= 0) continue;
                            String propertytreeid = propertyList.getProperty("propertytreeid");
                            if ("No Layout".equals(propertytreeid)) {
                                if (boxTemplateList.contains(boxtemplateid)) continue;
                                int row = newBoxTemplateDS.addRow();
                                newBoxTemplateDS.setString(row, "storageunittype", nodeid);
                                newBoxTemplateDS.setString(row, "keyid1", boxtemplateid);
                                newBoxTemplateDS.setString(row, "templateid", "New Unsorted");
                                continue;
                            }
                            if (!"Grid".equals(propertytreeid) || (collection = propertyList.getCollectionNotNull("childrentypes")).size() <= 0) continue;
                            for (int col = 0; col < collection.size(); ++col) {
                                PropertyList plist = collection.getPropertyList(col);
                                if (plist.getProperty("type", "").length() <= 0 || boxTemplateList.contains(boxtemplateid)) continue;
                                int row = newBoxTemplateDS.addRow();
                                newBoxTemplateDS.setString(row, "storageunittype", nodeid);
                                newBoxTemplateDS.setString(row, "keyid1", boxtemplateid);
                                newBoxTemplateDS.setString(row, "templateid", "New Sorted");
                            }
                        }
                        continue;
                    }
                    catch (SapphireException e) {
                        e.printStackTrace();
                    }
                }
                if (newBoxTemplateDS.size() > 0) {
                    ActionProcessor actionProcessor = new ActionProcessor(queryProcessor.getConnectionid());
                    for (int i = 0; i < newBoxTemplateDS.size(); ++i) {
                        PropertyList props = new PropertyList();
                        props.setProperty("storageunittype", newBoxTemplateDS.getString(i, "storageunittype"));
                        props.setProperty("copies", "1");
                        props.setProperty("primary_sdcid", "LV_Box");
                        props.setProperty("primary_keyid1", newBoxTemplateDS.getString(i, "keyid1"));
                        props.setProperty("primary_templateflag", "Y");
                        props.setProperty("primary_overrideautokey", "Y");
                        props.setProperty("primary_templateid", newBoxTemplateDS.getString(i, "templateid"));
                        props.setProperty("auditactivity", "Save As Template");
                        props.setProperty("auditreason", "New Box Template for new Box Storage Unit Type");
                        try {
                            actionProcessor.processActionClass(AddStorageUnit.class.getName(), props);
                            continue;
                        }
                        catch (ActionException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            this.resetLastNodeList(new ConnectionProcessor(queryProcessor.getConnectionid()).getSapphireConnection());
            // ** MonitorExit[var5_6] (shouldn't be in output)
            return databaseid;
        }
    }

    public List<String> getLastNodeList(SapphireConnection sapphireConnection) {
        List<String> cachedLastNodeList = (List<String>)CacheUtil.get(sapphireConnection.getDatabaseId(), "STORAGEUNITTYPE", "LASTNODELIST");
        if (cachedLastNodeList == null || cachedLastNodeList.size() == 0) {
            cachedLastNodeList = this.resetLastNodeList(sapphireConnection);
        }
        return cachedLastNodeList;
    }

    private List<String> resetLastNodeList(SapphireConnection sapphireConnection) {
        ArrayList<String> lastNodeList = new ArrayList<String>();
        Map<String, PropertyList> typeMap = this.getTypeMap(new QueryProcessor(sapphireConnection.getConnectionId()));
        for (String node : typeMap.keySet()) {
            if (node.endsWith(" Product") || node.endsWith(" Custom") || !this.isAllowedChild(node, typeMap) || this.isStorageContainer(node, typeMap) || !this.allowsOnlyStorageContainers(node, typeMap) || this.isParentStorageContainer(node, typeMap)) continue;
            lastNodeList.add(node);
        }
        CacheUtil.put(sapphireConnection.getDatabaseId(), "STORAGEUNITTYPE", "LASTNODELIST", lastNodeList);
        return lastNodeList;
    }

    public PropertyList getTypeDefinition(String connectionid, String storageunittype) {
        return this.getTypeDefinition(new QueryProcessor(connectionid), storageunittype);
    }

    public PropertyList getTypeDefinition(QueryProcessor queryProcessor, String storageunittype) {
        PropertyList propertyList;
        if (OpalUtil.isEmpty(storageunittype)) {
            return new PropertyList();
        }
        String databaseid = this.refreshDefinition(queryProcessor, false);
        if (OpalUtil.isEmpty(databaseid)) {
            databaseid = SecurityService.getDatabaseId(queryProcessor.getConnectionid());
        }
        return (propertyList = (PropertyList)CacheUtil.get(databaseid, "storageunittype_nodemap", storageunittype)) != null ? propertyList : new PropertyList();
    }

    public String getStorageUnitTypeLabel(QueryProcessor queryProcessor, String storageunittype) {
        PropertyList storageUnitTypeDef = this.getTypeDefinition(queryProcessor, storageunittype);
        return storageUnitTypeDef.getProperty("storageunittypelabel", storageUnitTypeDef.getProperty("nodeid", ""));
    }

    public String getStorageUnitTypeLabelByID(QueryProcessor queryProcessor, String storageunitid) {
        PropertyList storageUnitTypeDef = this.getTypeDefinitionByID(queryProcessor, storageunitid);
        return storageUnitTypeDef.getProperty("storageunittypelabel", storageUnitTypeDef.getProperty("nodeid", ""));
    }

    public Map<String, PropertyList> getTypeMap(QueryProcessor queryProcessor) {
        String databaseid = this.refreshDefinition(queryProcessor, false);
        if (OpalUtil.isEmpty(databaseid)) {
            databaseid = SecurityService.getDatabaseId(queryProcessor.getConnectionid());
        }
        Set<String> keySet = CacheUtil.keySet(databaseid, "storageunittype_nodemap");
        HashMap<String, PropertyList> map = new HashMap<String, PropertyList>();
        for (String key : keySet) {
            map.put(key, (PropertyList)CacheUtil.get(databaseid, "storageunittype_nodemap", key));
        }
        return map;
    }

    public Map<String, PropertyList> getTypeMap(QueryProcessor queryProcessor, String storageunittype) {
        HashMap<String, PropertyList> map = new HashMap<String, PropertyList>();
        Map<String, PropertyList> typeMap = this.getTypeMap(queryProcessor);
        for (String nodeid : typeMap.keySet()) {
            PropertyList propertyList = typeMap.get(nodeid);
            if (!storageunittype.equals(propertyList.getProperty("propertytreeid"))) continue;
            map.put(nodeid, propertyList);
        }
        return map;
    }

    public PropertyList getTypePropertyList(QueryProcessor queryProcessor, String storageunittype) {
        PropertyList props = new PropertyList();
        Map<String, PropertyList> typeMap = this.getTypeMap(queryProcessor);
        for (String nodeid : typeMap.keySet()) {
            PropertyList propertyList = typeMap.get(nodeid);
            if (!storageunittype.equals(propertyList.getProperty("propertytreeid"))) continue;
            props.setProperty(nodeid, propertyList);
        }
        return props;
    }

    public PropertyList getTypeDefinitionByID(String connectionid, String storageunitid) {
        return this.getTypeDefinitionByID(new QueryProcessor(connectionid), storageunitid);
    }

    public PropertyList getTypeDefinitionByID(QueryProcessor queryProcessor, String storageunitid) {
        String storageunittype = OpalUtil.getColumnValue(queryProcessor, "storageunit", "storageunittype", "storageunitid = ?", new String[]{storageunitid});
        return this.getTypeDefinition(queryProcessor, storageunittype);
    }

    public PropertyList getMandatoryChildTypeProps(QueryProcessor queryProcessor, String parentStorageUnitType) {
        PropertyList storageUnitTypeProps = this.getTypeDefinition(queryProcessor, parentStorageUnitType);
        PropertyListCollection childstorageunitprops = storageUnitTypeProps.getCollectionNotNull("childrentypes");
        if (childstorageunitprops.size() > 0) {
            for (int i = 0; i < childstorageunitprops.size(); ++i) {
                PropertyList list = childstorageunitprops.getPropertyList(i);
                if (!"Y".equals(list.getProperty("mandatory"))) continue;
                return this.getTypeDefinition(queryProcessor, list.getProperty("type"));
            }
        }
        return new PropertyList();
    }

    public boolean isStorageContainer(String storageunittype, Map<String, PropertyList> nodeMap) {
        return this.isLinkedToSDI(storageunittype, nodeMap) && this.isAllowedChild(storageunittype, nodeMap);
    }

    public boolean isLinkedToSDI(String storageunittype, Map<String, PropertyList> nodeMap) {
        if (nodeMap.containsKey(storageunittype)) {
            PropertyList props = nodeMap.get(storageunittype);
            PropertyList template = props.getPropertyListNotNull("template");
            return OpalUtil.isNotEmpty(template.getProperty("sdcid")) && OpalUtil.isNotEmpty(template.getProperty("keyid1"));
        }
        return false;
    }

    public boolean isAllowedChild(String storageunittype, Map<String, PropertyList> nodeMap) {
        boolean isAllowedChild = false;
        block0: for (String node : nodeMap.keySet()) {
            if (node.endsWith(" Product") || node.endsWith(" Custom") || node.equals(storageunittype)) continue;
            PropertyList props = nodeMap.get(node);
            PropertyListCollection collection = props.getCollectionNotNull("childrentypes");
            for (int i = 0; i < collection.size(); ++i) {
                if (!storageunittype.equals(collection.getPropertyList(i).getProperty("type"))) continue;
                isAllowedChild = true;
                break block0;
            }
        }
        return isAllowedChild;
    }

    public boolean allowsOnlyStorageContainers(String storageunittype, Map<String, PropertyList> nodeMap) {
        boolean allowsOnlyStorageContainers = true;
        PropertyList props = nodeMap.get(storageunittype);
        PropertyListCollection collection = props.getCollectionNotNull("childrentypes");
        for (int i = 0; i < collection.size(); ++i) {
            if (this.isStorageContainer(collection.getPropertyList(i).getProperty("type"), nodeMap)) continue;
            allowsOnlyStorageContainers = false;
            break;
        }
        return allowsOnlyStorageContainers;
    }

    public boolean isParentStorageContainer(String storageunittype, Map<String, PropertyList> nodeMap) {
        boolean isParentStorageContainer = false;
        block0: for (String node : nodeMap.keySet()) {
            if (node.endsWith(" Product") || node.endsWith(" Custom") || node.equals(storageunittype)) continue;
            PropertyList props = nodeMap.get(node);
            PropertyListCollection collection = props.getCollectionNotNull("childrentypes");
            for (int i = 0; i < collection.size(); ++i) {
                if (!storageunittype.equals(collection.getPropertyList(i).getProperty("type")) || !this.isStorageContainer(node, nodeMap)) continue;
                isParentStorageContainer = true;
                break block0;
            }
        }
        return isParentStorageContainer;
    }
}

