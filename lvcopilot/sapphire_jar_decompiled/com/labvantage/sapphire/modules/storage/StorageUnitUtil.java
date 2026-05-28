/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.modules.storage;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.opal.util.StorageUnitTypeDef;
import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.modules.storage.StorageUnit;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.http.HttpUtil;
import com.labvantage.sapphire.xml.Node;
import com.labvantage.sapphire.xml.NodeList;
import com.labvantage.sapphire.xml.PropertyTree;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.error.ErrorHandler;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class StorageUnitUtil
extends BaseCustom {
    protected static final String LABVANTAGE_CVS_ID = "$Revision: 103000 $";
    private static final String successMsgTitle = "SUCCESS";
    private static final String successMsg = "Created Storage units successfully";
    private static final String failMsgTitle = "Error";
    private static final String failMsg = "Storage units could not be created";
    public static final String RESTRICTIONS_OPERATOR_EQUALS = "Equals";
    public static final String RESTRICTIONS_OPERATOR_NOT_EQUALS = "Not Equals";
    public static final String RESTRICTIONS_OPERATOR_IN = "In";
    public static final String RESTRICTIONS_OPERATOR_NOT_IN = "Not In";
    public static final String RESTRICTIONS_OPERATOR_HOMOGENEOUS = "Homogeneous";
    StringBuffer suHierarchyid = null;
    StringBuffer nodeid = null;
    StringBuffer propertyTreeid = null;
    StringBuffer environment = null;
    StringBuffer suLevel = null;
    StringBuffer parentid = null;
    StringBuffer suPropNodeid = null;
    StringBuffer storageUnitLabel = null;
    StringBuffer storageUnitDesc = null;
    StringBuffer maxTIAllowed = null;
    StringBuffer moveableFlag = null;
    PageContext pageContext;
    WebAdminProcessor webAdminProcessor;
    PropertyList pagedata;
    boolean childAddMode;
    public static List<String> STORAGEUNITTYPELIST = new ArrayList<String>();
    private static Map<String, String> typeTreeMap;

    public StorageUnitUtil(String connectionid) {
        this.setConnectionId(connectionid);
    }

    public StorageUnitUtil(PageContext pageContext, PropertyList pagedata) {
        this.pageContext = pageContext;
        this.pagedata = pagedata;
        this.setConnectionId(new ConnectionProcessor(pageContext).getConnectionid());
    }

    public WebAdminProcessor getWebAdminProcessor() {
        if (this.webAdminProcessor == null) {
            this.webAdminProcessor = new WebAdminProcessor(this.getConnectionId());
        }
        return this.webAdminProcessor;
    }

    public String getSUTypeJSArray(String arrName, String childrenArrName) {
        StringBuilder storageUnitTypes = new StringBuilder();
        DataSet nodeDS = new DataSet();
        DataSet nodeValidChildrenDS = new DataSet();
        this.populateStorageUnitTypeDS(nodeDS, nodeValidChildrenDS);
        if (nodeDS.size() > 0) {
            storageUnitTypes.append(this.convertToJSArray(arrName, nodeDS)).append("\n").append(this.convertToJSArray(childrenArrName, nodeValidChildrenDS));
        }
        return storageUnitTypes.toString();
    }

    public void populateStorageUnitTypeDS(DataSet nodeDS, DataSet nodeValidChildrenDS) {
        nodeDS.addColumn("nodeid", 0);
        nodeDS.addColumn("propertytreeid", 0);
        nodeDS.addColumn("isenvselallowed", 0);
        nodeDS.addColumn("size", 0);
        nodeDS.addColumn("maxtiallowed", 0);
        nodeDS.addColumn("moveableflag", 0);
        nodeDS.addColumn("usesdiidaslabel", 0);
        nodeDS.addColumn("templatesdcid", 0);
        nodeDS.addColumn("allowatroot", 0);
        nodeDS.addColumn("mandatorychildtype", 0);
        nodeDS.addColumn("storageunittypelabel", 0);
        nodeValidChildrenDS.addColumn("parentnodeid", 0);
        nodeValidChildrenDS.addColumn("parentpropertytreeid", 0);
        nodeValidChildrenDS.addColumn("childnodeid", 0);
        nodeValidChildrenDS.addColumn("mandatory", 0);
        Map<String, PropertyList> typeMap = StorageUnitTypeDef.getInstance().getTypeMap(this.getQueryProcessor());
        for (String storageunittype : typeMap.keySet()) {
            PropertyList list = typeMap.get(storageunittype);
            String parentpropertytreeid = list.getProperty("propertytreeid");
            int dsRowNum = nodeDS.addRow();
            nodeDS.setString(dsRowNum, "nodeid", storageunittype);
            nodeDS.setString(dsRowNum, "propertytreeid", parentpropertytreeid);
            nodeDS.setString(dsRowNum, "size", list.getProperty("size").trim());
            nodeDS.setString(dsRowNum, "isenvselallowed", list.getProperty("isenvselallowed"));
            nodeDS.setString(dsRowNum, "maxtiallowed", list.getProperty("maxtiallowed").trim());
            nodeDS.setString(dsRowNum, "moveableflag", list.getProperty("moveable"));
            nodeDS.setString(dsRowNum, "allowatroot", list.getProperty("allowatroot"));
            PropertyList templateProps = list.getPropertyListNotNull("template");
            nodeDS.setString(dsRowNum, "usesdiidaslabel", templateProps.getProperty("usesdiidaslabel", "N"));
            nodeDS.setString(dsRowNum, "templatesdcid", templateProps.getProperty("sdcid"));
            String storageunittypelabel = list.getProperty("storageunittypelabel", "").trim();
            if (storageunittypelabel.length() == 0) {
                storageunittypelabel = storageunittype;
            }
            nodeDS.setString(dsRowNum, "storageunittypelabel", storageunittypelabel);
            String mandatoryChildType = "";
            PropertyListCollection validChildren = list.getCollectionNotNull("childrentypes");
            for (int count = 0; count < validChildren.size(); ++count) {
                PropertyList validChild = validChildren.getPropertyList(count);
                String childType = validChild.getProperty("type");
                String allowCreationInWizard = validChild.getProperty("allowcreationinwizard");
                if ("Y".equals(allowCreationInWizard) && childType != null && childType.trim().length() > 0) {
                    int row = nodeValidChildrenDS.addRow();
                    nodeValidChildrenDS.setString(row, "parentnodeid", storageunittype);
                    nodeValidChildrenDS.setString(row, "parentpropertytreeid", parentpropertytreeid);
                    nodeValidChildrenDS.setString(row, "childnodeid", childType);
                }
                if (!OpalUtil.isEmpty(mandatoryChildType) || !"Y".equals(validChild.getProperty("mandatory", "N"))) continue;
                mandatoryChildType = childType;
            }
            nodeDS.setString(dsRowNum, "mandatorychildtype", mandatoryChildType);
            nodeDS.sort("nodeid");
            nodeValidChildrenDS.sort("childnodeid");
        }
    }

    private String convertToJSArray(String jsArrayName, DataSet ds) {
        StringBuilder jsArray = new StringBuilder();
        String jsColumnsArrayName = jsArrayName + "_columns";
        jsArray.append("var ").append(jsColumnsArrayName).append(" = new Array(); \n");
        jsArray.append("var ").append(jsArrayName).append(" = new Array(); \n");
        if (ds != null) {
            int count;
            String[] columns = ds.getColumns();
            int maxColCount = columns.length;
            for (count = 0; count < maxColCount; ++count) {
                jsArray.append(jsColumnsArrayName).append("[ ").append(count).append(" ] = ").append(" \"").append(columns[count]).append("\";\n");
            }
            for (count = 0; count < ds.size(); ++count) {
                jsArray.append(jsArrayName).append("[ ").append(count).append(" ] = new Array( ");
                boolean appendCommaFlag = false;
                for (int colCount = 0; colCount < maxColCount; ++colCount) {
                    String colValue = ds.getString(count, columns[colCount]);
                    if (appendCommaFlag) {
                        jsArray.append(", ");
                    } else {
                        appendCommaFlag = true;
                    }
                    jsArray.append("\"").append(colValue).append("\" ");
                }
                jsArray.append("); \n");
            }
        }
        return jsArray.toString();
    }

    private void addNodesToDS(PropertyTree propertyTree, String propertytreeid, DataSet nodeDS, DataSet nodeValidChildrenDS) {
        if (propertyTree != null && nodeDS != null) {
            NodeList nodeList = propertyTree.getNodeList();
            ArrayList allNodes = new ArrayList();
            nodeList.getAllNodes(allNodes);
            for (Object allNode : allNodes) {
                Node node = (Node)allNode;
                if (node.isProduct() || node.isCustom()) continue;
                String nodeId = node.getNodeId();
                nodeDS.addRow();
                int dsRowNum = nodeDS.size() - 1;
                nodeDS.setValue(dsRowNum, "nodeid", nodeId);
                nodeDS.setValue(dsRowNum, "propertytreeid", propertytreeid);
                try {
                    PropertyList nodePropertyList = propertyTree.getNodePropertyList(nodeId, true);
                    if (nodePropertyList == null) continue;
                    nodeDS.setValue(dsRowNum, "size", nodePropertyList.getProperty("size"));
                    nodeDS.setValue(dsRowNum, "isenvselallowed", nodePropertyList.getProperty("isenvselallowed"));
                    nodeDS.setValue(dsRowNum, "maxtiallowed", nodePropertyList.getProperty("maxtiallowed"));
                    nodeDS.setValue(dsRowNum, "moveableflag", nodePropertyList.getProperty("moveable"));
                    nodeDS.setValue(dsRowNum, "allowatroot", nodePropertyList.getProperty("allowatroot"));
                    PropertyList templateProps = nodePropertyList.getPropertyList("template");
                    String useSDIIdAsLabel = "N";
                    String templatesdcid = "";
                    if (templateProps != null) {
                        useSDIIdAsLabel = templateProps.getProperty("usesdiidaslabel", "N");
                        templatesdcid = templateProps.getProperty("sdcid");
                    }
                    nodeDS.setValue(dsRowNum, "usesdiidaslabel", useSDIIdAsLabel);
                    nodeDS.setValue(dsRowNum, "templatesdcid", templatesdcid);
                    this.populateChildrenDS(nodePropertyList, nodeValidChildrenDS, nodeId, propertytreeid);
                }
                catch (SapphireException ex) {
                    Logger.logStackTrace(ex);
                }
            }
        }
    }

    private void populateChildrenDS(PropertyList nodePropertyList, DataSet nodeValidChildrenDS, String nodeId, String propertytreeid) {
        PropertyListCollection validChildren;
        if (nodePropertyList != null && (validChildren = nodePropertyList.getCollection("childrentypes")) != null) {
            for (int count = 0; count < validChildren.size(); ++count) {
                PropertyList validChild = validChildren.getPropertyList(count);
                String childType = validChild.getProperty("type");
                String allowCreationInWizard = validChild.getProperty("allowcreationinwizard");
                if (!"Y".equalsIgnoreCase(allowCreationInWizard) || childType == null || childType.trim().length() <= 0) continue;
                nodeValidChildrenDS.addRow();
                int dsRowNum = nodeValidChildrenDS.size() - 1;
                nodeValidChildrenDS.setValue(dsRowNum, "parentnodeid", nodeId);
                nodeValidChildrenDS.setValue(dsRowNum, "parentpropertytreeid", propertytreeid);
                nodeValidChildrenDS.setValue(dsRowNum, "childnodeid", childType);
                nodeValidChildrenDS.setValue(dsRowNum, "mandatory", validChild.getProperty("mandatory", "N"));
            }
        }
    }

    public DataSet createSUHierarchyDataSet(PropertyList pagedata) throws Exception {
        DataSet ds = new DataSet();
        boolean isDataValid = true;
        StringBuffer errorMsg = new StringBuffer();
        this.addStorageUnitDSColumns(ds);
        String tempSaveSUHNodeCount = pagedata.getProperty("saveSUHNodeCount");
        int saveSUHNodeCount = Integer.parseInt(tempSaveSUHNodeCount);
        for (int count = 0; count < saveSUHNodeCount; ++count) {
            String tempSerializedSUH = pagedata.getProperty("saveSUH_" + count);
            String[] tempSUHProps = StringUtil.split(tempSerializedSUH, "|");
            if (tempSUHProps == null || tempSUHProps.length != 15) {
                isDataValid = false;
                errorMsg.append("Invalid data: RowCount: ").append(count).append(" serialized SUH: ").append(tempSerializedSUH).append("<br/>");
                continue;
            }
            ds.addRow();
            int rowCount = ds.size() - 1;
            ds.setValue(rowCount, "suhierarchyid", tempSUHProps[0]);
            ds.setValue(rowCount, "sutypehierarchyid", tempSUHProps[1]);
            ds.setValue(rowCount, "sutypeid", tempSUHProps[2]);
            ds.setValue(rowCount, "nodeid", tempSUHProps[3]);
            ds.setValue(rowCount, "propertytreeid", tempSUHProps[4]);
            ds.setValue(rowCount, "suNodeCount", tempSUHProps[5]);
            ds.setValue(rowCount, "suEnvironment", tempSUHProps[6]);
            ds.setValue(rowCount, "level", tempSUHProps[7]);
            ds.setValue(rowCount, "parentid", tempSUHProps[10]);
            ds.setValue(rowCount, "storageunitlabel", tempSUHProps[11]);
            ds.setValue(rowCount, "storageunitdesc", tempSUHProps[12]);
            ds.setValue(rowCount, "maxtiallowed", tempSUHProps[13]);
        }
        if (!isDataValid) {
            throw new Exception(errorMsg.toString());
        }
        return ds;
    }

    private void addStorageUnitDSColumns(DataSet ds) {
        ds.addColumn("suhierarchyid", 0);
        ds.addColumn("sutypehierarchyid", 0);
        ds.addColumn("sutypeid", 0);
        ds.addColumn("nodeid", 0);
        ds.addColumn("propertytreeid", 0);
        ds.addColumn("suNodeCount", 0);
        ds.addColumn("suEnvironment", 0);
        ds.addColumn("level", 0);
        ds.addColumn("parentid", 0);
        ds.addColumn("storageunitlabel", 0);
        ds.addColumn("storageunitdesc", 0);
        ds.addColumn("maxtiallowed", 0);
    }

    public String createStorageUnits() {
        ErrorHandler errorHandler;
        long startTime = System.currentTimeMillis();
        String parentstorageunitid = this.pagedata.getProperty("parentid").trim();
        if (StringUtil.getLen(parentstorageunitid) > 0L) {
            this.childAddMode = true;
        }
        this.initializeSUProperties(this.pagedata);
        PropertyList actionProps = this.getStorageUnitProps();
        String maxtiallowed = actionProps.getProperty("maxtiallowed");
        if (StringUtil.getLen(maxtiallowed) == 0L) {
            actionProps.setProperty("maxtiallowed", "0");
        }
        actionProps.setProperty("childAddMode", this.childAddMode ? "Y" : "N");
        StringBuffer sb = new StringBuffer();
        try {
            DataSet ds;
            if (this.childAddMode) {
                StringBuilder sbparentid = new StringBuilder();
                String[] __suparentid = StringUtil.split(actionProps.getProperty("suparentid", ""), ";");
                int len = __suparentid.length;
                for (int i = 0; i < len; ++i) {
                    sbparentid.append(";");
                    if (!"USUH_1".equals(__suparentid[i])) continue;
                    sbparentid.append(parentstorageunitid);
                }
                actionProps.setProperty("parentid", sbparentid.substring(1));
            }
            ActionProcessor ap = this.getActionProcessor();
            ap.processAction("AddSDI", "1", actionProps);
            errorHandler = ap.getErrorHandler();
            if (errorHandler == null) {
                errorHandler = new ErrorHandler();
            }
            errorHandler.add("", "", this.getTranslationProcessor().translate(successMsgTitle), "INFORMATION", this.getTranslationProcessor().translate(successMsg));
            String newKeyid1 = actionProps.getProperty("newkeyid1");
            sb.append("<script language='JavaScript'>\n");
            sb.append("forwardflag = true;\n");
            sb.append("</script>\n");
            StringBuffer sql = new StringBuffer();
            if (StringUtil.getLen(newKeyid1) <= 2000L) {
                SafeSQL safeSQL = new SafeSQL();
                sql.append("select storageunitid, linksdcid, linkkeyid1 from storageunit where parentid is null");
                sql.append(" and storageunitid in (").append(safeSQL.addIn(newKeyid1, ";")).append(")");
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            } else {
                String rsetid = this.getDAMProcessor().createRSet("StorageUnitSDC", newKeyid1, null, null);
                sql.append("select storageunitid, linksdcid, linkkeyid1 from storageunit where parentid is null");
                sql.append(" and storageunitid in ( select rsetitems.keyid1 from rsetitems where rsetid = ?)");
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{rsetid});
                this.getDAMProcessor().clearRSet(rsetid);
            }
            String storageunitid = ds != null && ds.size() > 0 ? ds.getColumnValues("storageunitid", ";") : newKeyid1;
            if (this.childAddMode) {
                actionProps.clear();
                actionProps.setProperty("sdcid", "StorageUnitSDC");
                actionProps.setProperty("keyid1", storageunitid);
                actionProps.setProperty("parentid", parentstorageunitid);
                actionProps.setProperty("__ignoremoveableflag", "Y");
                this.pageContext.setAttribute("keyid1", (Object)parentstorageunitid);
            } else {
                this.pageContext.setAttribute("keyid1", (Object)storageunitid);
            }
            Logger.logInfo("==================================================================================================");
            Logger.logInfo("Took " + (System.currentTimeMillis() - startTime) + " ms to add " + StringUtil.split(newKeyid1, ";").length + " storage units");
            Logger.logInfo("==================================================================================================");
        }
        catch (ActionException ae) {
            errorHandler = ae.getErrorHandler();
            if (errorHandler == null) {
                errorHandler = new ErrorHandler();
                errorHandler.add("", "", failMsgTitle, "VALIDATION", ae.getMessage());
            } else {
                errorHandler.add("", "", failMsgTitle, "VALIDATION", failMsg);
            }
        }
        catch (Exception ex) {
            errorHandler = new ErrorHandler();
            errorHandler.add("", "", failMsgTitle, "VALIDATION", ex.getMessage());
        }
        sb.append(ErrorUtil.getErrorHTML(this.pageContext, errorHandler, true, null));
        sb.append("<script>var __newstorageunitid = '").append(this.pageContext.getAttribute("keyid1")).append("';</script>");
        return sb.toString();
    }

    public void initializeSUProperties(PropertyList pagedata) {
        int i;
        HashMap map = new HashMap();
        ArrayList list = new ArrayList();
        int saveSUHNodeCount = Integer.parseInt(pagedata.getProperty("saveSUHNodeCount"));
        for (i = 0; i < saveSUHNodeCount; ++i) {
            this.populateStorageUnit(map, list, pagedata, i);
        }
        for (i = 0; i < list.size(); ++i) {
            StorageUnit storageunit = (StorageUnit)list.get(i);
            storageunit.associateChildStorageUnits(map);
        }
        String umbrellaStorageUnitid = pagedata.getProperty("saveSUH_0_id");
        StorageUnit umbrellaStorageUnit = (StorageUnit)map.get(umbrellaStorageUnitid);
        this.initializeBuffers(umbrellaStorageUnit);
        this.setStorageUnitProps(umbrellaStorageUnit, umbrellaStorageUnitid, "");
    }

    private void populateStorageUnit(HashMap allStorageUnits, ArrayList allStorageUnitsArrList, PropertyList pagedata, int count) {
        StorageUnit storageunit = new StorageUnit();
        String prefix = "saveSUH_" + count;
        storageunit.setId(pagedata.getProperty(prefix + "_id"));
        storageunit.setSuTypeHierarchyId(pagedata.getProperty(prefix + "_sutypehierarchyid"));
        storageunit.setSuTypeId(pagedata.getProperty(prefix + "_sutypeid"));
        storageunit.setNodeid(pagedata.getProperty(prefix + "_nodeid"));
        storageunit.setPropertytreeid(pagedata.getProperty(prefix + "_propertytreeid"));
        if (!"".equals(pagedata.getProperty(prefix + "_sunodecount"))) {
            storageunit.setSuNodeCount(Integer.parseInt(pagedata.getProperty(prefix + "_sunodecount")));
        }
        storageunit.setSuEnvironment(pagedata.getProperty(prefix + "_suenvironment"));
        if (!"".equals(pagedata.getProperty(prefix + "_sulevel"))) {
            storageunit.setLevel(Integer.parseInt(pagedata.getProperty(prefix + "_sulevel")));
        }
        if (!"".equals(pagedata.getProperty(prefix + "_childidsequence"))) {
            storageunit.setChildIdSequence(Integer.parseInt(pagedata.getProperty(prefix + "_childidsequence")));
        }
        storageunit.setChildIds(pagedata.getProperty(prefix + "_childids"));
        storageunit.setParentId(pagedata.getProperty(prefix + "_parentid"));
        storageunit.setSuLabel(pagedata.getProperty(prefix + "_label"));
        storageunit.setSuDesc(pagedata.getProperty(prefix + "_sudesc"));
        storageunit.setSuMaxTIAllowed(pagedata.getProperty(prefix + "_maxtiallowed"));
        storageunit.setSuMoveableFlag(pagedata.getProperty(prefix + "_moveableflag"));
        allStorageUnits.put(storageunit.getId(), storageunit);
        allStorageUnitsArrList.add(storageunit);
    }

    private void setStorageUnitProps(StorageUnit storageunit, String currentStorageUnitId, String currentParentId) {
        int startLevel = -1;
        if (this.isChildAddMode()) {
            startLevel = 0;
        }
        if (storageunit.getLevel() > startLevel) {
            this.suHierarchyid.append(currentStorageUnitId).append(";");
            this.nodeid.append(storageunit.getNodeid()).append(";");
            this.propertyTreeid.append(storageunit.getPropertytreeid()).append(";");
            this.environment.append(storageunit.getSuEnvironment()).append(";");
            this.suLevel.append(storageunit.getLevel()).append(";");
            this.parentid.append(currentParentId).append(";");
            this.suPropNodeid.append(storageunit.getPropertytreeid()).append("|").append(storageunit.getNodeid()).append(";");
            this.storageUnitLabel.append(storageunit.getSuLabel()).append(";");
            this.storageUnitDesc.append(storageunit.getSuDesc()).append(";");
            this.maxTIAllowed.append(storageunit.getSuMaxTIAllowed()).append(";");
            this.moveableFlag.append(storageunit.getSuMoveableFlag()).append(";");
        }
        int tempchildIdSequence = 1;
        ArrayList children = storageunit.getChildren();
        for (int i = 0; i < children.size(); ++i) {
            StorageUnit tempStorageUnit = (StorageUnit)children.get(i);
            int nodeCount = tempStorageUnit.getSuNodeCount();
            for (int tempNodeCount = 1; tempNodeCount <= nodeCount; ++tempNodeCount) {
                int newChildSequence = tempchildIdSequence++;
                this.setStorageUnitProps(tempStorageUnit, currentStorageUnitId + "_" + newChildSequence, currentStorageUnitId);
            }
        }
    }

    public PropertyList getStorageUnitProps() {
        PropertyList props = new PropertyList();
        String suHierarchyIdstr = this.suHierarchyid.substring(0, this.suHierarchyid.length() - 1);
        String[] suHierarchyIdArr = StringUtil.split(suHierarchyIdstr, ";");
        props.setProperty("sdcid", "StorageUnitSDC");
        props.setProperty("suhierarchyid", suHierarchyIdstr);
        props.setProperty("storageunittype", this.nodeid.substring(0, this.nodeid.length() - 1));
        props.setProperty("nodeid", this.nodeid.substring(0, this.nodeid.length() - 1));
        props.setProperty("propertytreeid", this.propertyTreeid.substring(0, this.propertyTreeid.length() - 1));
        props.setProperty("storageenvid", this.environment.substring(0, this.environment.length() - 1));
        props.setProperty("sulevel", this.suLevel.substring(0, this.suLevel.length() - 1));
        props.setProperty("suparentid", this.parentid.substring(0, this.parentid.length() - 1));
        props.setProperty("propnodeid", this.suPropNodeid.substring(0, this.suPropNodeid.length() - 1));
        props.setProperty("storageunitlabel", this.storageUnitLabel.substring(0, this.storageUnitLabel.length() - 1));
        props.setProperty("storageunitdesc", this.storageUnitDesc.substring(0, this.storageUnitDesc.length() - 1));
        props.setProperty("maxtiallowed", this.maxTIAllowed.substring(0, this.maxTIAllowed.length() - 1));
        props.setProperty("moveableflag", this.moveableFlag.substring(0, this.moveableFlag.length() - 1));
        props.setProperty("fromaction", "createstorageunits");
        props.setProperty("copies", Integer.toString(suHierarchyIdArr.length));
        props.setProperty("propsmatch", "true");
        return props;
    }

    private void initializeBuffers(StorageUnit topLevelStorageUnit) {
        int size = topLevelStorageUnit.calculateSize(1);
        this.suHierarchyid = new StringBuffer(20 * size);
        this.nodeid = new StringBuffer(20 * size);
        this.propertyTreeid = new StringBuffer(20 * size);
        this.environment = new StringBuffer(20 * size);
        this.suLevel = new StringBuffer(4 * size);
        this.parentid = new StringBuffer(20 * size);
        this.suPropNodeid = new StringBuffer(40 * size);
        this.storageUnitLabel = new StringBuffer(80 * size);
        this.storageUnitDesc = new StringBuffer(80 * size);
        this.maxTIAllowed = new StringBuffer(6 * size);
        this.moveableFlag = new StringBuffer(2 * size);
    }

    public PropertyList getStorageUnitInfo(String storageunitid) {
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select storageunitid, labelpath, storageunittype, propertytreeid, storageunitdesc, maxtiallowed, moveableflag, storageunitlabel, storageenvid from storageunit where storageunitid = ?", (Object[])new String[]{storageunitid});
        PropertyList props = new PropertyList();
        if (ds != null && ds.size() > 0) {
            props.setProperty("storageunitid", ds.getValue(0, "storageunitid"));
            props.setProperty("labelpath", ds.getValue(0, "labelpath"));
            props.setProperty("storageunittype", ds.getValue(0, "storageunittype"));
            props.setProperty("propertytreeid", ds.getValue(0, "propertytreeid"));
            props.setProperty("storageunitdesc", ds.getValue(0, "storageunitdesc"));
            props.setProperty("maxtiallowed", ds.getValue(0, "maxtiallowed"));
            props.setProperty("moveableflag", ds.getValue(0, "moveableflag", "N"));
            props.setProperty("storageunitlabel", ds.getValue(0, "storageunitlabel"));
            props.setProperty("storageenvid", ds.getValue(0, "storageenvid"));
        }
        return props;
    }

    public String renderChildAddModeScript() {
        StringBuilder sb = new StringBuilder();
        sb.append("<script language=\"JavaScript\">");
        String parentstorageunitid = this.pagedata.getProperty("parentid", "").trim();
        if (StringUtil.getLen(parentstorageunitid) > 0L) {
            this.childAddMode = true;
            PropertyList props = this.getStorageUnitInfo(parentstorageunitid);
            sb.append("\nvar psu = new ParentStorageUnit( true );");
            sb.append("\npsu.setStorageunitid('").append(props.getProperty("storageunitid")).append("');");
            sb.append("\npsu.setLabelpath('").append(props.getProperty("labelpath")).append("');");
            sb.append("\npsu.setStorageunittype('").append(props.getProperty("storageunittype")).append("');");
            sb.append("\npsu.setPropertytreeid('").append(props.getProperty("propertytreeid")).append("');");
            sb.append("\npsu.setStorageunitdesc('").append(props.getProperty("storageunitdesc")).append("');");
            sb.append("\npsu.setMaxtiallowed('").append(props.getProperty("maxtiallowed")).append("');");
            sb.append("\npsu.setMoveableflag('").append(props.getProperty("moveableflag")).append("');");
            sb.append("\npsu.setStorageunitlabel('").append(props.getProperty("storageunitlabel")).append("');");
            sb.append("\npsu.setStorageenvid('").append(props.getProperty("storageenvid")).append("');");
        } else {
            sb.append("\nvar psu = new ParentStorageUnit( false );");
        }
        sb.append("\n</script>");
        return sb.toString();
    }

    public boolean isChildAddMode() {
        return this.childAddMode;
    }

    public static DataSet getStorageRestrictions(QueryProcessor queryProcessor, String storageunitid, boolean isOra) {
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder sql = new StringBuilder();
        if (isOra) {
            sql.append("select sr.storageunitid, (select su.labelpath from storageunit su where su.storageunitid = sr.storageunitid) labelpath, sr.restrictionbasedon, sr.propertyid, sr.operator, sr.propertyvalue, sr.failuremessage");
            sql.append("  from storagerestriction sr");
            sql.append("  where sr.storageunitid in (");
            sql.append("    select s.storageunitid ");
            sql.append("    from storageunit s");
            sql.append("    connect by prior s.parentid = s.storageunitid");
            sql.append("    start with s.storageunitid = ").append(safeSQL.addVar(storageunitid));
            sql.append("  )");
            sql.append("  and sr.activeflag = 'Y'");
        } else {
            sql.append("select sr.storageunitid, (select su.labelpath from storageunit su where su.storageunitid = sr.storageunitid) labelpath, sr.restrictionbasedon, sr.propertyid, sr.operator, sr.propertyvalue, sr.failuremessage");
            sql.append("  from storagerestriction sr");
            sql.append("  where sr.storageunitid in (");
            sql.append(safeSQL.addIn(StorageUnitUtil.getMSSParentStorageUnitsDataSet(queryProcessor, storageunitid).getColumnValues("storageunitid", "','")));
            sql.append(") and sr.activeflag = 'Y'");
        }
        return queryProcessor.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
    }

    public static DataSet getMSSChildStorageUnitsDataSet(QueryProcessor queryProcessor, String storageunitid) {
        StringBuilder sql = new StringBuilder();
        sql.append("WITH StorageUnitTree (storageunitid, parentid, linksdcid, linkkeyid1)");
        sql.append(" AS");
        sql.append(" (");
        sql.append("    SELECT su.storageunitid, su.parentid, su.linksdcid, su.linkkeyid1");
        sql.append("    FROM storageunit AS su");
        sql.append("    WHERE su.storageunitid = ?");
        sql.append("    UNION ALL");
        sql.append("    SELECT su.storageunitid, su.parentid, su.linksdcid, su.linkkeyid1");
        sql.append("    FROM storageunit AS su");
        sql.append("    INNER JOIN StorageUnitTree AS d");
        sql.append("    ON su.parentid = d.storageunitid");
        sql.append(" )");
        sql.append(" SELECT storageunitid, parentid, linksdcid, linkkeyid1");
        sql.append(" FROM StorageUnitTree");
        DataSet ds = queryProcessor.getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{storageunitid});
        return ds == null ? new DataSet() : ds;
    }

    public static DataSet getMSSParentStorageUnitsDataSet(QueryProcessor queryProcessor, String storageunitid) {
        StringBuilder sql = new StringBuilder();
        sql.append("WITH StorageUnitTree (storageunitid, parentid, linksdcid, linkkeyid1)");
        sql.append(" AS");
        sql.append(" (");
        sql.append("    SELECT su.storageunitid, su.parentid, su.linksdcid, su.linkkeyid1");
        sql.append("    FROM storageunit AS su");
        sql.append("    WHERE su.storageunitid = ?");
        sql.append("    UNION ALL");
        sql.append("    SELECT su.storageunitid, su.parentid, su.linksdcid, su.linkkeyid1");
        sql.append("    FROM storageunit AS su");
        sql.append("    INNER JOIN StorageUnitTree AS d");
        sql.append("    ON su.storageunitid = d.parentid");
        sql.append(" )");
        sql.append(" SELECT storageunitid, parentid, linksdcid, linkkeyid1");
        sql.append(" FROM StorageUnitTree");
        DataSet ds = queryProcessor.getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{storageunitid});
        return ds == null ? new DataSet() : ds;
    }

    public static List<String> validateStorageRestrictions(QueryProcessor queryProcessor, DAMProcessor damProcessor, String storageunitid, String trackitemid, SapphireConnection sapphireConnection) {
        DataSet restrictions;
        TranslationProcessor translationProcessor = new TranslationProcessor(sapphireConnection.getConnectionId());
        SDCProcessor sdcProcessor = new SDCProcessor(sapphireConnection.getConnectionId());
        ArrayList<String> errors = new ArrayList<String>();
        boolean isOra = sapphireConnection.isOracle();
        if (StringUtil.getLen(storageunitid) > 0L && StringUtil.getLen(trackitemid) > 0L && (restrictions = StorageUnitUtil.getStorageRestrictions(queryProcessor, storageunitid, isOra)) != null && restrictions.size() > 0) {
            StringBuilder sql = new StringBuilder();
            int trackitemcount = StringUtil.split(trackitemid, ";").length;
            for (int j = 0; j < restrictions.size(); ++j) {
                List<String> pvlist;
                SafeSQL safeSQL;
                ArrayList _ds;
                String labelpath = restrictions.getString(j, "labelpath");
                String restrictionbasedon = restrictions.getString(j, "restrictionbasedon");
                String propertyid = restrictions.getString(j, "propertyid");
                String operator = restrictions.getString(j, "operator");
                String propertyvalue = restrictions.getString(j, "propertyvalue");
                String failuremessage = restrictions.getString(j, "failuremessage", "");
                if ("TrackItem".equals(restrictionbasedon)) {
                    if (RESTRICTIONS_OPERATOR_HOMOGENEOUS.equals(operator)) {
                        StorageUnitUtil.validateHomogeneousRestriction(queryProcessor, damProcessor, translationProcessor, isOra, restrictions.getString(j, "storageunitid"), trackitemid, trackitemcount, propertyid, "TrackItem", errors, failuremessage);
                        continue;
                    }
                    _ds = null;
                    safeSQL = new SafeSQL();
                    sql.setLength(0);
                    sql.append("select trackitem.trackitemid, trackitem.linksdcid, trackitem.linkkeyid1");
                    sql.append(" from trackitem");
                    if (RESTRICTIONS_OPERATOR_EQUALS.equals(operator)) {
                        sql.append(" where ( trackitem.").append(propertyid).append(" != ").append(safeSQL.addVar(propertyvalue));
                        sql.append(" or trackitem.").append(propertyid).append(" is null )");
                    } else if (RESTRICTIONS_OPERATOR_NOT_EQUALS.equals(operator)) {
                        sql.append(" where trackitem.").append(propertyid).append(" = ").append(safeSQL.addVar(propertyvalue));
                    } else if (RESTRICTIONS_OPERATOR_IN.equals(operator)) {
                        sql.append(" where ( trackitem.").append(propertyid).append(" not in (").append(safeSQL.addIn(propertyvalue, ",")).append(")");
                        sql.append(" or trackitem.").append(propertyid).append(" is null )");
                    } else if (RESTRICTIONS_OPERATOR_NOT_IN.equals(operator)) {
                        sql.append(" where trackitem.").append(propertyid).append(" in (").append(safeSQL.addIn(propertyvalue, ",")).append(")");
                    }
                    if (trackitemcount < 1000) {
                        sql.append(" and trackitem.trackitemid in ( ").append(safeSQL.addIn(trackitemid, ";")).append(" )");
                        _ds = queryProcessor.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                    } else {
                        try {
                            String rsetid = damProcessor.createRSet("TrackItemSDC", trackitemid, null, null);
                            sql.append(" and trackitem.trackitemid in ( select r.keyid1 from rsetitems r where r.rsetid = ").append(safeSQL.addVar(rsetid)).append(" )");
                            _ds = queryProcessor.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                        }
                        catch (SapphireException e) {
                            e.printStackTrace();
                        }
                    }
                    if (_ds == null) continue;
                    for (int i = 0; i < _ds.size(); ++i) {
                        String sdcid = ((DataSet)_ds).getString(i, "linksdcid");
                        String keyid1 = ((DataSet)_ds).getString(i, "linkkeyid1");
                        if ("TrackItem".equals(restrictionbasedon)) {
                            sdcid = sdcProcessor.getProperty(sdcid, "singular");
                        }
                        errors.add(StorageUnitUtil.formatFailureMessage(sdcid, keyid1, labelpath, translationProcessor.translate(failuremessage)));
                    }
                    continue;
                }
                if ("Sample".equals(restrictionbasedon)) {
                    SafeSQL safeSQL2 = new SafeSQL();
                    if (RESTRICTIONS_OPERATOR_HOMOGENEOUS.equals(operator)) {
                        StorageUnitUtil.validateHomogeneousRestriction(queryProcessor, damProcessor, translationProcessor, isOra, restrictions.getString(j, "storageunitid"), trackitemid, trackitemcount, propertyid, "Sample", errors, failuremessage);
                        continue;
                    }
                    ArrayList _ds2 = null;
                    safeSQL2.reset();
                    sql.setLength(0);
                    sql.append("select trackitem.trackitemid, trackitem.linksdcid, trackitem.linkkeyid1, s_sample.s_sampleid");
                    sql.append(" from trackitem, s_sample");
                    sql.append(" where trackitem.linksdcid = 'Sample'");
                    sql.append(" and trackitem.linkkeyid1 = s_sample.s_sampleid");
                    if (RESTRICTIONS_OPERATOR_EQUALS.equals(operator)) {
                        sql.append(" and ( s_sample.").append(propertyid).append(" != ").append(safeSQL2.addVar(propertyvalue));
                        sql.append(" or s_sample.").append(propertyid).append(" is null )");
                    } else if (RESTRICTIONS_OPERATOR_NOT_EQUALS.equals(operator)) {
                        sql.append(" and s_sample.").append(propertyid).append(" = ").append(safeSQL2.addVar(propertyvalue));
                    } else if (RESTRICTIONS_OPERATOR_IN.equals(operator)) {
                        sql.append(" and ( s_sample.").append(propertyid).append(" not in (").append(safeSQL2.addIn(propertyvalue, ",")).append(")");
                        sql.append(" or s_sample.").append(propertyid).append(" is null )");
                    } else if (RESTRICTIONS_OPERATOR_NOT_IN.equals(operator)) {
                        sql.append(" and s_sample.").append(propertyid).append(" in (").append(safeSQL2.addIn(propertyvalue, ",")).append(")");
                    }
                    if (trackitemcount < 1000) {
                        sql.append(" and trackitem.trackitemid in (").append(safeSQL2.addIn(trackitemid, ";")).append(")");
                        _ds2 = queryProcessor.getPreparedSqlDataSet(sql.toString(), safeSQL2.getValues());
                    } else {
                        try {
                            String rsetid = damProcessor.createRSet("TrackItemSDC", trackitemid, null, null);
                            sql.append(" and trackitem.trackitemid in (select r.keyid1 from rsetitems r where r.rsetid = ").append(safeSQL2.addVar(rsetid)).append(")");
                            _ds2 = queryProcessor.getPreparedSqlDataSet(sql.toString(), safeSQL2.getValues());
                        }
                        catch (SapphireException e) {
                            e.printStackTrace();
                        }
                    }
                    if (_ds2 == null) continue;
                    for (int i = 0; i < _ds2.size(); ++i) {
                        errors.add(StorageUnitUtil.formatFailureMessage("Sample", ((DataSet)_ds2).getString(i, "s_sampleid"), labelpath, translationProcessor.translate(failuremessage)));
                    }
                    continue;
                }
                if ("Sample Family".equals(restrictionbasedon)) {
                    if (RESTRICTIONS_OPERATOR_HOMOGENEOUS.equals(operator)) {
                        StorageUnitUtil.validateHomogeneousRestriction(queryProcessor, damProcessor, translationProcessor, isOra, restrictions.getString(j, "storageunitid"), trackitemid, trackitemcount, propertyid, "Sample Family", errors, failuremessage);
                        continue;
                    }
                    _ds = null;
                    safeSQL = new SafeSQL();
                    sql.setLength(0);
                    sql.append("select trackitem.trackitemid, trackitem.linksdcid, trackitem.linkkeyid1, s_sample.s_sampleid");
                    sql.append(" from trackitem, s_sample, s_samplefamily");
                    sql.append(" where trackitem.linksdcid = 'Sample'");
                    sql.append(" and trackitem.linkkeyid1 = s_sample.s_sampleid");
                    sql.append(" and s_samplefamily.s_samplefamilyid = s_sample.samplefamilyid");
                    if (RESTRICTIONS_OPERATOR_EQUALS.equals(operator)) {
                        sql.append(" and ( s_samplefamily.").append(propertyid).append(" != ").append(safeSQL.addVar(propertyvalue));
                        sql.append(" or s_samplefamily.").append(propertyid).append(" is null )");
                    } else if (RESTRICTIONS_OPERATOR_NOT_EQUALS.equals(operator)) {
                        sql.append(" and s_samplefamily.").append(propertyid).append(" = ").append(safeSQL.addVar(propertyvalue));
                    } else if (RESTRICTIONS_OPERATOR_IN.equals(operator)) {
                        sql.append(" and ( s_samplefamily.").append(propertyid).append(" not in (").append(safeSQL.addIn(propertyvalue, ",")).append(")");
                        sql.append(" or s_samplefamily.").append(propertyid).append(" is null )");
                    } else if (RESTRICTIONS_OPERATOR_NOT_IN.equals(operator)) {
                        sql.append(" and s_samplefamily.").append(propertyid).append(" in (").append(safeSQL.addIn(propertyvalue, ",")).append(")");
                    }
                    if (trackitemcount < 1000) {
                        sql.append(" and trackitem.trackitemid in (").append(safeSQL.addIn(trackitemid, ";")).append(")");
                        _ds = queryProcessor.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                    } else {
                        try {
                            String rsetid = damProcessor.createRSet("TrackItemSDC", trackitemid, null, null);
                            sql.append(" and trackitem.trackitemid in ( select r.keyid1 from rsetitems r where r.rsetid = ").append(safeSQL.addVar(rsetid)).append(")");
                            _ds = queryProcessor.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                        }
                        catch (SapphireException e) {
                            e.printStackTrace();
                        }
                    }
                    if (_ds == null) continue;
                    for (int i = 0; i < _ds.size(); ++i) {
                        errors.add(StorageUnitUtil.formatFailureMessage("Sample", ((DataSet)_ds).getString(i, "s_sampleid"), labelpath, translationProcessor.translate(failuremessage)));
                    }
                    continue;
                }
                if (!"Current User".equals(restrictionbasedon)) continue;
                if ("User ID".equals(propertyid)) {
                    if (RESTRICTIONS_OPERATOR_EQUALS.equals(operator)) {
                        if (sapphireConnection.getSysuserId().equals(propertyvalue)) continue;
                        errors.add(StorageUnitUtil.formatFailureMessage(restrictionbasedon, "", labelpath, failuremessage));
                        continue;
                    }
                    if (RESTRICTIONS_OPERATOR_NOT_EQUALS.equals(operator)) {
                        if (!sapphireConnection.getSysuserId().equals(propertyvalue)) continue;
                        errors.add(StorageUnitUtil.formatFailureMessage(restrictionbasedon, "", labelpath, failuremessage));
                        continue;
                    }
                    if (RESTRICTIONS_OPERATOR_IN.equals(operator)) {
                        if (OpalUtil.toList(propertyvalue, ",").contains(sapphireConnection.getSysuserId())) continue;
                        errors.add(StorageUnitUtil.formatFailureMessage(restrictionbasedon, "", labelpath, failuremessage));
                        continue;
                    }
                    if (!RESTRICTIONS_OPERATOR_NOT_IN.equals(operator) || !OpalUtil.toList(propertyvalue, ",").contains(sapphireConnection.getSysuserId())) continue;
                    errors.add(StorageUnitUtil.formatFailureMessage(restrictionbasedon, "", labelpath, failuremessage));
                    continue;
                }
                if ("Role".equals(propertyid)) {
                    List<String> roles = OpalUtil.toList(sapphireConnection.getRoleList(), ";");
                    if (RESTRICTIONS_OPERATOR_EQUALS.equals(operator)) {
                        if (roles.contains(propertyvalue)) continue;
                        errors.add(StorageUnitUtil.formatFailureMessage(restrictionbasedon, "", labelpath, failuremessage));
                        continue;
                    }
                    if (RESTRICTIONS_OPERATOR_NOT_EQUALS.equals(operator)) {
                        if (!roles.contains(propertyvalue)) continue;
                        errors.add(StorageUnitUtil.formatFailureMessage(restrictionbasedon, "", labelpath, failuremessage));
                        continue;
                    }
                    if (RESTRICTIONS_OPERATOR_IN.equals(operator)) {
                        pvlist = OpalUtil.toList(propertyvalue, ",");
                        boolean roleexists = false;
                        block10: for (String pvrole : pvlist) {
                            if (roleexists) break;
                            for (String userrole : roles) {
                                if (!userrole.equals(pvrole)) continue;
                                roleexists = true;
                                continue block10;
                            }
                        }
                        if (roleexists) continue;
                        errors.add(StorageUnitUtil.formatFailureMessage(restrictionbasedon, "", labelpath, translationProcessor.translate(failuremessage)));
                        continue;
                    }
                    if (!RESTRICTIONS_OPERATOR_NOT_IN.equals(operator)) continue;
                    pvlist = OpalUtil.toList(propertyvalue, ",");
                    boolean roleexists = false;
                    block12: for (String pvrole : pvlist) {
                        if (roleexists) break;
                        for (String userrole : roles) {
                            if (!userrole.equals(pvrole)) continue;
                            roleexists = true;
                            continue block12;
                        }
                    }
                    if (!roleexists) continue;
                    errors.add(StorageUnitUtil.formatFailureMessage(restrictionbasedon, "", labelpath, translationProcessor.translate(failuremessage)));
                    continue;
                }
                if ("Department".equals(propertyid)) {
                    List<String> userDepartments = OpalUtil.toList(sapphireConnection.getDepartmentList(), ";");
                    if (RESTRICTIONS_OPERATOR_EQUALS.equals(operator)) {
                        if (userDepartments.contains(propertyvalue)) continue;
                        errors.add(StorageUnitUtil.formatFailureMessage(restrictionbasedon, "", labelpath, failuremessage));
                        continue;
                    }
                    if (RESTRICTIONS_OPERATOR_NOT_EQUALS.equals(operator)) {
                        if (!userDepartments.contains(propertyvalue)) continue;
                        errors.add(StorageUnitUtil.formatFailureMessage(restrictionbasedon, "", labelpath, translationProcessor.translate(failuremessage)));
                        continue;
                    }
                    if (RESTRICTIONS_OPERATOR_IN.equals(operator)) {
                        pvlist = OpalUtil.toList(propertyvalue, ",");
                        boolean deptexists = false;
                        block14: for (String pvdept : pvlist) {
                            if (deptexists) break;
                            for (String departmentid : userDepartments) {
                                if (!departmentid.equals(pvdept)) continue;
                                deptexists = true;
                                continue block14;
                            }
                        }
                        if (deptexists) continue;
                        errors.add(StorageUnitUtil.formatFailureMessage(restrictionbasedon, "", labelpath, translationProcessor.translate(failuremessage)));
                        continue;
                    }
                    if (!RESTRICTIONS_OPERATOR_NOT_IN.equals(operator)) continue;
                    pvlist = OpalUtil.toList(propertyvalue, ",");
                    boolean deptexists = false;
                    block16: for (String pvdept : pvlist) {
                        if (deptexists) break;
                        for (String departmentid : userDepartments) {
                            if (!departmentid.equals(pvdept)) continue;
                            deptexists = true;
                            continue block16;
                        }
                    }
                    if (!deptexists) continue;
                    errors.add(StorageUnitUtil.formatFailureMessage(restrictionbasedon, "", labelpath, translationProcessor.translate(failuremessage)));
                    continue;
                }
                if (!"JobType".equals(propertyid)) continue;
                String userJobType = sapphireConnection.getCurrentJobtype();
                if (RESTRICTIONS_OPERATOR_EQUALS.equals(operator)) {
                    if (userJobType.equals(propertyvalue)) continue;
                    errors.add(StorageUnitUtil.formatFailureMessage(restrictionbasedon, "", labelpath, translationProcessor.translate(failuremessage)));
                    continue;
                }
                if (RESTRICTIONS_OPERATOR_NOT_EQUALS.equals(operator)) {
                    if (!userJobType.equals(propertyvalue)) continue;
                    errors.add(StorageUnitUtil.formatFailureMessage(restrictionbasedon, "", labelpath, translationProcessor.translate(failuremessage)));
                    continue;
                }
                if (RESTRICTIONS_OPERATOR_IN.equals(operator)) {
                    if (OpalUtil.toList(propertyvalue, ",").contains(userJobType)) continue;
                    errors.add(StorageUnitUtil.formatFailureMessage(restrictionbasedon, "", labelpath, translationProcessor.translate(failuremessage)));
                    continue;
                }
                if (!RESTRICTIONS_OPERATOR_NOT_IN.equals(operator) || !OpalUtil.toList(propertyvalue, ",").contains(userJobType)) continue;
                errors.add(StorageUnitUtil.formatFailureMessage(restrictionbasedon, "", labelpath, translationProcessor.translate(failuremessage)));
            }
        }
        return errors;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void validateHomogeneousRestriction(QueryProcessor queryProcessor, DAMProcessor damProcessor, TranslationProcessor translationProcessor, boolean isOra, String homogeneousStorageUnitid, String trackitemid, int trackitemcount, String propertyid, String basedOn, List<String> errors, String failuremessage) {
        block65: {
            SafeSQL safeSQL;
            StringBuilder sql;
            block64: {
                ArrayList ds = null;
                sql = new StringBuilder();
                safeSQL = new SafeSQL();
                switch (basedOn) {
                    case "TrackItem": {
                        sql.append("select trackitem.trackitemid, trackitem.").append(propertyid);
                        sql.append(" from trackitem");
                        sql.append(" where trackitem.").append(propertyid).append(" is not null");
                        break;
                    }
                    case "Sample": {
                        sql.append("select s_sample.s_sampleid, s_sample.").append(propertyid);
                        sql.append(" from s_sample, trackitem");
                        sql.append(" where trackitem.linksdcid = 'Sample'");
                        sql.append(" and trackitem.linkkeyid1 = s_sample.s_sampleid");
                        sql.append(" and s_sample.").append(propertyid).append(" is not null");
                        break;
                    }
                    case "Sample Family": {
                        sql.append("select s_sample.s_sampleid, s_samplefamily.").append(propertyid);
                        sql.append(" from s_sample, trackitem, s_samplefamily");
                        sql.append(" where trackitem.linksdcid = 'Sample'");
                        sql.append(" and trackitem.linkkeyid1 = s_sample.s_sampleid");
                        sql.append(" and s_samplefamily.s_samplefamilyid = s_sample.samplefamilyid");
                        sql.append(" and s_samplefamily.").append(propertyid).append(" is not null");
                    }
                }
                if (isOra) {
                    sql.append(" and trackitem.currentstorageunitid in (");
                    sql.append(" select su.storageunitid from storageunit su");
                    sql.append(" connect by prior su.storageunitid = su.parentid");
                    sql.append(" start with su.storageunitid = ?)");
                    sql.append(" and rownum = 1");
                    ds = queryProcessor.getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{homogeneousStorageUnitid});
                } else {
                    DataSet dschild = StorageUnitUtil.getMSSChildStorageUnitsDataSet(queryProcessor, homogeneousStorageUnitid);
                    if (dschild.size() > 1000) {
                        String rsetid = null;
                        try {
                            rsetid = damProcessor.createRSet("StorageUnitSDC", dschild.getColumnValues("storageunitid", ";"), null, null);
                            sql.append(" and trackitem.currentstorageunitid in (select r.keyid1 from rsetitems r where r.rsetid = ?)");
                            ds = queryProcessor.getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{rsetid});
                        }
                        catch (SapphireException e) {
                            e.printStackTrace();
                        }
                        finally {
                            if (rsetid != null && rsetid.length() > 0) {
                                damProcessor.clearRSet(rsetid);
                            }
                        }
                    } else {
                        sql.append(" and trackitem.currentstorageunitid in (").append(safeSQL.addIn(dschild.getColumnValues("storageunitid", "','"))).append(")");
                        ds = queryProcessor.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                    }
                }
                if (ds == null || ds.size() <= 0) break block64;
                String homogeneousValue = ((DataSet)ds).getValue(0, propertyid);
                if (StringUtil.getLen(homogeneousValue) <= 0L) break block65;
                safeSQL.reset();
                sql.setLength(0);
                ArrayList _ds = null;
                switch (basedOn) {
                    case "TrackItem": {
                        sql.append("select trackitem.trackitemid, trackitem.linksdcid sdcid, trackitem.linkkeyid1 keyid1");
                        sql.append(" from trackitem");
                        sql.append(" where (trackitem.").append(propertyid).append(" != ").append(safeSQL.addVar(homogeneousValue));
                        sql.append(" or trackitem.").append(propertyid).append(" is null)");
                        break;
                    }
                    case "Sample": {
                        sql.append("select trackitem.trackitemid, 'Sample' sdcid, s_sample.s_sampleid keyid1");
                        sql.append(" from s_sample, trackitem");
                        sql.append(" where trackitem.linksdcid = 'Sample'");
                        sql.append(" and trackitem.linkkeyid1 = s_sample.s_sampleid");
                        sql.append(" and s_sample.").append(propertyid).append(" != ").append(safeSQL.addVar(homogeneousValue));
                        break;
                    }
                    case "Sample Family": {
                        sql.append("select trackitem.trackitemid, 'Sample' sdcid, s_sample.s_sampleid keyid1");
                        sql.append(" from s_sample, trackitem, s_samplefamily");
                        sql.append(" where trackitem.linksdcid = 'Sample'");
                        sql.append(" and trackitem.linkkeyid1 = s_sample.s_sampleid");
                        sql.append(" and s_samplefamily.s_samplefamilyid = s_sample.samplefamilyid");
                        sql.append(" and s_samplefamily.").append(propertyid).append(" != ").append(safeSQL.addVar(homogeneousValue));
                    }
                }
                if (trackitemcount < 1000) {
                    sql.append(" and trackitem.trackitemid in (").append(safeSQL.addIn(trackitemid, ";")).append(")");
                    _ds = queryProcessor.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                } else {
                    String rsetid = null;
                    try {
                        rsetid = damProcessor.createRSet("TrackItemSDC", trackitemid, null, null);
                        sql.append(" and trackitem.trackitemid in ( select r.keyid1 from rsetitems r where r.rsetid = ? )");
                        _ds = queryProcessor.getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{rsetid});
                    }
                    catch (SapphireException e) {
                        e.printStackTrace();
                    }
                    finally {
                        if (rsetid != null) {
                            damProcessor.clearRSet(rsetid);
                        }
                    }
                }
                if (_ds != null && _ds.size() > 0) {
                    for (int i = 0; i < _ds.size(); ++i) {
                        if (failuremessage != null && !failuremessage.equals("")) {
                            errors.add(translationProcessor.translate(failuremessage));
                            continue;
                        }
                        String sdcid = ((DataSet)_ds).getString(i, "sdcid", "");
                        String keyid1 = ((DataSet)_ds).getString(i, "keyid1", "");
                        String message = translationProcessor.translate("Failed Homogeneous Storage Restriction:");
                        message = message + " " + translationProcessor.translate(sdcid) + " " + keyid1;
                        message = message + " [" + translationProcessor.translate("Property") + ": " + propertyid.toUpperCase() + "]";
                        errors.add(message);
                    }
                }
                break block65;
            }
            ArrayList _ds = null;
            safeSQL.reset();
            sql.setLength(0);
            switch (basedOn) {
                case "TrackItem": {
                    sql.append("select linksdcid sdcid, linkkeyid1 keyid1, ").append(propertyid);
                    sql.append(" from trackitem");
                    sql.append(" where trackitemid is not null");
                    break;
                }
                case "Sample": {
                    sql.append("select 'Sample' sdcid, s_sample.s_sampleid keyid1, s_sample.").append(propertyid);
                    sql.append(" from s_sample, trackitem");
                    sql.append(" where trackitem.linksdcid = 'Sample'");
                    sql.append(" and trackitem.linkkeyid1 = s_sample.s_sampleid");
                    break;
                }
                case "Sample Family": {
                    sql.append("select 'Sample' sdcid, s_sample.s_sampleid keyid1, s_samplefamily.").append(propertyid);
                    sql.append(" from s_sample, s_samplefamily, trackitem");
                    sql.append(" where trackitem.linksdcid = 'Sample'");
                    sql.append(" and trackitem.linkkeyid1 = s_sample.s_sampleid");
                    sql.append(" and s_samplefamily.s_samplefamilyid = s_sample.samplefamilyid");
                }
            }
            if (trackitemcount < 1000) {
                sql.append(" and trackitem.trackitemid in (").append(safeSQL.addIn(trackitemid, ";")).append(")");
                _ds = queryProcessor.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            } else {
                String rsetid = null;
                try {
                    rsetid = damProcessor.createRSet("TrackItemSDC", trackitemid, null, null);
                    sql.append(" and trackitem.trackitemid in ( select r.keyid1 from rsetitems r where r.rsetid = ? )");
                    _ds = queryProcessor.getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{rsetid});
                }
                catch (SapphireException e) {
                    e.printStackTrace();
                }
                finally {
                    if (rsetid != null) {
                        damProcessor.clearRSet(rsetid);
                    }
                }
            }
            if (_ds != null && _ds.size() > 0) {
                String propertyValue = ((DataSet)_ds).getValue(0, propertyid, "");
                for (int i = 1; i < _ds.size(); ++i) {
                    if (propertyValue.equals(((DataSet)_ds).getValue(i, propertyid, ""))) continue;
                    if (failuremessage != null && !failuremessage.equals("")) {
                        errors.add(translationProcessor.translate(failuremessage));
                        continue;
                    }
                    String sdcid = ((DataSet)_ds).getString(i, "sdcid", "");
                    String keyid1 = ((DataSet)_ds).getString(i, "keyid1", "");
                    String message = translationProcessor.translate("Failed Homogeneous Storage Restriction on");
                    message = message + " " + translationProcessor.translate(sdcid) + " " + keyid1;
                    message = message + " [" + translationProcessor.translate("Property") + ": " + propertyid.toUpperCase() + "]";
                    errors.add(message);
                }
            }
        }
    }

    private static String formatFailureMessage(String basedon, String keyid, String labelpath, String failuremessage) {
        String message = "";
        message = "Current User".equals(basedon) ? "Storage Restrictions Failure: User not allowed to place items in Storage Unit \"" + labelpath + "\". (" + failuremessage + ")" : "Storage Restrictions Failure: " + basedon + " " + keyid + " in Storage Unit \"" + labelpath + "\". (" + failuremessage + ")";
        return message;
    }

    public static String parseExplorerDisplayData(PropertyList props, String storageunitid, int rownumber, QueryProcessor queryProcessor, TranslationProcessor translationProcessor) {
        return StorageUnitUtil.parseExplorerDisplayData(props, storageunitid, rownumber, 0, -1, queryProcessor, translationProcessor);
    }

    public static String parseRendererDisplayData(PropertyListCollection collection, String displayformat, String storageunitid, String trackitemid, QueryProcessor queryProcessor, TranslationProcessor translationProcessor) {
        String[] tokens;
        DataSet ds;
        String parameter;
        String columnid;
        PropertyList list;
        int i;
        String sql;
        if (collection == null) {
            collection = new PropertyListCollection();
        }
        if (collection.size() == 0) {
            PropertyList list2 = new PropertyList();
            list2.setProperty("parameter", "storageunitindex");
            list2.setProperty("columnid", "storageunitindex");
            collection.add(list2);
        }
        HashMap<String, String> tokenMap = new HashMap<String, String>();
        HashMap<String, String> displayValueMap = new HashMap<String, String>();
        String sysuserid = new ConnectionProcessor(queryProcessor.getConnectionid()).getSapphireConnection().getSysuserId();
        if (OpalUtil.isEmpty(storageunitid)) {
            sql = "select trackitemid";
            for (i = 0; i < collection.size(); ++i) {
                list = collection.getPropertyList(i);
                columnid = list.getProperty("columnid").trim();
                parameter = list.getProperty("parameter");
                if (!OpalUtil.isNotEmpty(parameter) || !OpalUtil.isNotEmpty(columnid)) continue;
                sql = sql + ", " + columnid;
            }
            sql = sql + " from trackitem where trackitemid = ?";
            sql = StringUtil.replaceAll(sql, "[%currentuser%]", sysuserid);
            sql = StringUtil.replaceAll(sql, "[currentuser]", sysuserid);
            ds = queryProcessor.getPreparedSqlDataSet(sql, (Object[])new String[]{trackitemid});
        } else {
            sql = "select storageunitid";
            for (i = 0; i < collection.size(); ++i) {
                list = collection.getPropertyList(i);
                columnid = list.getProperty("columnid").trim();
                columnid = StringUtil.replaceAll(columnid, "[trackitemid]", trackitemid);
                columnid = StringUtil.replaceAll(columnid, "[%currentuser%]", sysuserid);
                columnid = StringUtil.replaceAll(columnid, "[currentuser]", sysuserid);
                parameter = list.getProperty("parameter");
                if (!OpalUtil.isNotEmpty(parameter) || !OpalUtil.isNotEmpty(columnid)) continue;
                sql = sql + ", " + columnid;
            }
            if ((sql = sql + " from storageunit where storageunitid = ?").contains("[%currentuser%]")) {
                sql = StringUtil.replaceAll(sql, "[%currentuser%]", new ConnectionProcessor(queryProcessor.getConnectionid()).getSapphireConnection().getSysuserId());
            }
            ds = queryProcessor.getPreparedSqlDataSet(sql, (Object[])new String[]{storageunitid});
        }
        if (ds != null && ds.size() > 0) {
            for (i = 0; i < collection.size(); ++i) {
                list = collection.getPropertyList(i);
                columnid = list.getProperty("columnid").trim();
                parameter = list.getProperty("parameter");
                if (!OpalUtil.isNotEmpty(parameter) || !OpalUtil.isNotEmpty(columnid)) continue;
                if (columnid.contains(" ")) {
                    columnid = columnid.substring(columnid.lastIndexOf(" ") + 1);
                }
                String columnvalue = "";
                if ("storageunitindex".equals(columnid)) {
                    columnvalue = ds.getValue(0, columnid, "");
                } else if (ds.getColumnType(columnid) == 1) {
                    BigDecimal bd = ds.getBigDecimal(0, columnid);
                    if (bd != null) {
                        bd = bd.setScale(3, 4);
                        columnvalue = String.valueOf(bd.doubleValue());
                    }
                } else {
                    columnvalue = ds.getValue(0, columnid, "");
                }
                tokenMap.put(parameter, columnvalue);
                displayValueMap.put(parameter, list.getProperty("displayvalue"));
            }
        }
        for (String token : tokens = StringUtil.getTokens(displayformat)) {
            String value = StorageUnitUtil.parseDisplayValue((String)tokenMap.get(token), (String)displayValueMap.get(token));
            if (OpalUtil.isNotEmpty(value) && value.length() > 20) {
                value = "<span class='otitle' otitle='" + StringUtil.replaceAll(value, "'", "&quot;") + "'>" + value.substring(0, 18) + "...</span>";
            }
            displayformat = StringUtil.replaceAll(displayformat, "[" + token + "]", value);
        }
        return translationProcessor.translatePartial(displayformat.trim());
    }

    public static String parseRendererDisplayData(PropertyListCollection displaycolumns, String displayformat, DataSet childds, int childrow, TranslationProcessor translationProcessor) {
        String[] tokens;
        if (displaycolumns == null) {
            displaycolumns = new PropertyListCollection();
        }
        if (displaycolumns.size() == 0) {
            PropertyList list = new PropertyList();
            list.setProperty("parameter", "storageunitindex");
            list.setProperty("columnid", "storageunitindex");
            displaycolumns.add(list);
        }
        HashMap<String, String> tokenMap = new HashMap<String, String>();
        HashMap<String, String> displayValueMap = new HashMap<String, String>();
        for (int i = 0; i < displaycolumns.size(); ++i) {
            PropertyList list = displaycolumns.getPropertyList(i);
            String columnid = list.getProperty("columnid").trim();
            String parameter = list.getProperty("parameter");
            if (!OpalUtil.isNotEmpty(parameter) || !OpalUtil.isNotEmpty(columnid)) continue;
            if (columnid.contains(" ")) {
                columnid = columnid.substring(columnid.lastIndexOf(" ") + 1);
            }
            String columnvalue = "";
            if ("storageunitindex".equals(columnid)) {
                columnvalue = childds.getValue(childrow, columnid, "");
            } else if (childds.getColumnType(columnid) == 1) {
                BigDecimal bd = childds.getBigDecimal(childrow, columnid);
                if (bd != null) {
                    bd = bd.setScale(3, 4);
                    columnvalue = String.valueOf(bd.doubleValue());
                }
            } else {
                columnvalue = childds.getValue(childrow, columnid, "");
            }
            tokenMap.put(parameter, columnvalue);
            displayValueMap.put(parameter, list.getProperty("displayvalue"));
        }
        for (String token : tokens = StringUtil.getTokens(displayformat)) {
            String value = StorageUnitUtil.parseDisplayValue((String)tokenMap.get(token), (String)displayValueMap.get(token));
            if (OpalUtil.isNotEmpty(value) && value.length() > 20) {
                value = "<span class='otitle' otitle='" + StringUtil.replaceAll(value, "'", "&quot;") + "'>" + value.substring(0, 18) + "...</span>";
            }
            displayformat = StringUtil.replaceAll(displayformat, "[" + token + "]", value);
        }
        return translationProcessor.translatePartial(displayformat.trim());
    }

    public static String parseRendererDisplayData(PropertyListCollection collection, String displayformat, String storageunitid, String trackitemid, DataSet displaydata, TranslationProcessor translationProcessor) {
        String[] tokens;
        if (collection == null) {
            collection = new PropertyListCollection();
        }
        if (collection.size() == 0) {
            PropertyList list = new PropertyList();
            list.setProperty("parameter", "storageunitindex");
            list.setProperty("columnid", "storageunitindex");
            collection.add(list);
        }
        HashMap<String, String> tokenMap = new HashMap<String, String>();
        HashMap<String, String> displayValueMap = new HashMap<String, String>();
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("storageunitid", storageunitid);
        DataSet ds = displaydata.getFilteredDataSet(filter);
        if (ds != null && ds.size() > 0) {
            for (int i = 0; i < collection.size(); ++i) {
                PropertyList list = collection.getPropertyList(i);
                String columnid = list.getProperty("columnid").trim();
                String parameter = list.getProperty("parameter");
                if (!OpalUtil.isNotEmpty(parameter) || !OpalUtil.isNotEmpty(columnid)) continue;
                if ((columnid = StringUtil.replaceAll(columnid, "[trackitemid]", trackitemid)).contains(" ")) {
                    columnid = columnid.substring(columnid.lastIndexOf(" ") + 1);
                }
                tokenMap.put(parameter, ds.getValue(0, columnid));
                displayValueMap.put(parameter, list.getProperty("displayvalue"));
            }
        }
        for (String token : tokens = StringUtil.getTokens(displayformat)) {
            displayformat = StringUtil.replaceAll(displayformat, "[" + token + "]", StorageUnitUtil.parseDisplayValue((String)tokenMap.get(token), (String)displayValueMap.get(token)));
        }
        return translationProcessor.translatePartial(displayformat.trim());
    }

    public static String parseExplorerDisplayData(PropertyList props, String storageunitid, int rownumber, int contentcount, int maxcontentcount, QueryProcessor queryProcessor, TranslationProcessor translationProcessor) {
        String[] tokens;
        PropertyListCollection collection = props.getCollectionNotNull("columns");
        if (collection.size() == 0) {
            PropertyList list = new PropertyList();
            list.setProperty("parameter", "storageunittype");
            list.setProperty("columnid", "storageunittype");
            collection.add(list);
        }
        boolean isOra = new ConnectionProcessor(queryProcessor.getConnectionid()).isOra();
        HashMap<String, String> tokenMap = new HashMap<String, String>();
        HashMap<String, String> displayValueMap = new HashMap<String, String>();
        String sql = "select storageunitid";
        for (int i = 0; i < collection.size(); ++i) {
            PropertyList list = collection.getPropertyList(i);
            String columnid = list.getProperty("columnid").trim();
            String parameter = list.getProperty("parameter");
            if (!OpalUtil.isNotEmpty(parameter) || !OpalUtil.isNotEmpty(columnid)) continue;
            if (columnid.contains("{{")) {
                String[] tokens2;
                String[] stringArray = tokens2 = StringUtil.getTokens(columnid, "{{", "}}");
                int n = stringArray.length;
                for (int j = 0; j < n; ++j) {
                    String token = stringArray[j];
                    if (token.toLowerCase().startsWith("ora:")) {
                        if (isOra) {
                            columnid = StringUtil.replaceAll(columnid, "{{" + token + "}}", token.substring(4));
                            continue;
                        }
                        columnid = StringUtil.replaceAll(columnid, "{{" + token + "}}", "");
                        continue;
                    }
                    if (!token.toLowerCase().startsWith("mss:")) continue;
                    columnid = !isOra ? StringUtil.replaceAll(columnid, "{{" + token + "}}", token.substring(4)) : StringUtil.replaceAll(columnid, "{{" + token + "}}", "");
                }
            }
            sql = sql + ", " + columnid;
        }
        sql = sql + " from storageunit where storageunitid = ?";
        DataSet ds = queryProcessor.getPreparedSqlDataSet(sql = StringUtil.replaceAll(sql, "[%currentuser%]", new ConnectionProcessor(queryProcessor.getConnectionid()).getSapphireConnection().getSysuserId()), (Object[])new String[]{storageunitid});
        if (ds != null && ds.size() > 0) {
            for (int i = 0; i < collection.size(); ++i) {
                PropertyList list = collection.getPropertyList(i);
                String columnid = list.getProperty("columnid").trim();
                String parameter = list.getProperty("parameter");
                if (!OpalUtil.isNotEmpty(parameter) || !OpalUtil.isNotEmpty(columnid)) continue;
                if (columnid.contains(" ")) {
                    columnid = columnid.substring(columnid.lastIndexOf(" ") + 1);
                }
                tokenMap.put(parameter, ds.getValue(0, columnid));
                displayValueMap.put(parameter, list.getProperty("displayvalue"));
            }
        }
        String progressBarHTML = "";
        if (maxcontentcount <= 0) {
            progressBarHTML = "<div class=\"progressbar-unlimited\" title='" + contentcount + "'><div class='text'>" + contentcount + " / " + translationProcessor.translate("unlimited") + "</div></div>";
        } else {
            int prcnt = (int)Math.round((double)contentcount / (double)maxcontentcount * 100.0);
            progressBarHTML = "<div class=\"progressbar-small\" title='" + prcnt + "% " + translationProcessor.translate("filled") + "'><div class='bar' style='width:" + prcnt + "%'></div><div class='text'>" + contentcount + " / " + maxcontentcount + "</div></div>";
        }
        String text = props.getProperty("displayhtml", "[storageunittype] " + storageunitid);
        for (String token : tokens = StringUtil.getTokens(text)) {
            text = "rownumber".equals(token) ? StringUtil.replaceAll(text, "[" + token + "]", String.valueOf(rownumber)) : ("progressbar".equals(token) ? StringUtil.replaceAll(text, "[" + token + "]", progressBarHTML) : ("storageunittypelabel".equals(token) ? StringUtil.replaceAll(text, "[" + token + "]", StorageUnitTypeDef.getInstance().getStorageUnitTypeLabelByID(queryProcessor, storageunitid)) : StringUtil.replaceAll(text, "[" + token + "]", StorageUnitUtil.parseDisplayValue((String)tokenMap.get(token), (String)displayValueMap.get(token)))));
        }
        return translationProcessor.translatePartial(text.trim());
    }

    public static String parseDisplayValue(String text, String displayvalue) {
        if (OpalUtil.isNotEmpty(displayvalue)) {
            String[] s1;
            for (String s : s1 = StringUtil.split(displayvalue, ";")) {
                int index = s.indexOf("=");
                if (index == -1) continue;
                String t0 = s.substring(0, index);
                String t1 = s.substring(index + 1);
                if ("*".equals(t0) && OpalUtil.isNotEmpty(text)) {
                    text = StringUtil.replaceAll(t1, "[value]", text);
                    break;
                }
                if (!text.equals(t0)) continue;
                text = t1;
                break;
            }
        }
        if (OpalUtil.isNotEmpty(text) && text.toLowerCase().contains("<script>")) {
            text = HttpUtil.htmlEncode(text);
        }
        return text;
    }

    public static int getMaxTrackItemAllowedInStorageContainer(QueryProcessor queryProcessor, String sdcid, String keyid1) {
        int count = queryProcessor.getPreparedSqlDataSet("select maxtiallowed from storageunit where storageunit.linksdcid = ? and storageunit.linkkeyid1 = ?", (Object[])new String[]{sdcid, keyid1}).getInt(0, "maxtiallowed", 0);
        if (count >= 0) {
            count += queryProcessor.getPreparedSqlDataSet("select sum(maxtiallowed) maxticount from storageunit where storageunit.parentid = ( select parentsu.storageunitid from storageunit parentsu where parentsu.linksdcid = ? and parentsu.linkkeyid1 = ?)", (Object[])new String[]{sdcid, keyid1}).getInt(0, "maxticount", 0);
        }
        return count;
    }

    public static int getTrackItemCountInStorageContainer(QueryProcessor queryProcessor, String sdcid, String keyid1) {
        int count = queryProcessor.getPreparedSqlDataSet("select count(trackitem.trackitemid) ticount from trackitem, storageunit where trackitem.currentstorageunitid = storageunit.storageunitid and storageunit.linksdcid = ? and storageunit.linkkeyid1 = ?", (Object[])new String[]{sdcid, keyid1}).getInt(0, "ticount", 0);
        return count += queryProcessor.getPreparedSqlDataSet("select count(trackitem.trackitemid) ticount from trackitem where trackitem.currentstorageunitid in (select childsu.storageunitid from storageunit childsu where childsu.parentid = (select storageunit.storageunitid from storageunit where storageunit.linksdcid = ? and storageunit.linkkeyid1 = ?))", (Object[])new String[]{sdcid, keyid1}).getInt(0, "ticount", 0);
    }

    public static int getMaxTrackItemAllowedInStorageContainer(QueryProcessor queryProcessor, String storageunitid) {
        int count = queryProcessor.getPreparedSqlDataSet("select maxtiallowed from storageunit where storageunit.storageunitid = ?", (Object[])new String[]{storageunitid}).getInt(0, "maxtiallowed", 0);
        if (count >= 0) {
            count += queryProcessor.getPreparedSqlDataSet("select sum(maxtiallowed) maxticount from storageunit where storageunit.parentid = ( select parentsu.storageunitid from storageunit parentsu where parentsu.storageunitid = ?)", (Object[])new String[]{storageunitid}).getInt(0, "maxticount", 0);
        }
        return count;
    }

    public static int getTrackItemCountInStorageContainer(QueryProcessor queryProcessor, String storageunitid) {
        int count = queryProcessor.getPreparedSqlDataSet("select count(trackitem.trackitemid) ticount from trackitem, storageunit where trackitem.currentstorageunitid = storageunit.storageunitid and storageunit.storageunitid = ?", (Object[])new String[]{storageunitid}).getInt(0, "ticount", 0);
        return count += queryProcessor.getPreparedSqlDataSet("select count(trackitem.trackitemid) ticount from trackitem where trackitem.currentstorageunitid in (select childsu.storageunitid from storageunit childsu where childsu.parentid = (select storageunit.storageunitid from storageunit where storageunit.storageunitid = ?))", (Object[])new String[]{storageunitid}).getInt(0, "ticount", 0);
    }

    public static PropertyList getDefinition(QueryProcessor queryProcessor, String storageunittype) {
        WebAdminProcessor wap = new WebAdminProcessor(queryProcessor.getConnectionid());
        String propertytreeid = StorageUnitUtil.getPropertyTreeID(queryProcessor, wap, storageunittype);
        return StorageUnitUtil.getDefinition(wap, storageunittype, propertytreeid);
    }

    public static PropertyList getDefinition(WebAdminProcessor wap, String storageunittype, String propertytreeid) {
        PropertyList propertyList = null;
        if (OpalUtil.isNotEmpty(propertytreeid)) {
            try {
                PropertyTree propertyTree = wap.getPropertyTree(propertytreeid);
                if (propertyTree != null) {
                    propertyList = propertyTree.getNodePropertyList(storageunittype, true);
                    propertyList.setProperty("nodeid", storageunittype);
                    propertyList.setProperty("propertytreeid", propertytreeid);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return propertyList == null ? new PropertyList() : propertyList;
    }

    public static String getPropertyTreeID(QueryProcessor queryProcessor, WebAdminProcessor wap, String storageunittype) {
        if (!typeTreeMap.containsKey(storageunittype)) {
            try {
                for (String storageunittypeid : STORAGEUNITTYPELIST) {
                    PropertyTree propertyTree = wap.getPropertyTree(storageunittypeid);
                    ArrayList allNodes = propertyTree.getAllNodes();
                    for (Object object : allNodes) {
                        Node node = (Node)object;
                        String nodeid = node.getNodeId();
                        if (nodeid.endsWith(" Product") || nodeid.endsWith(" Custom")) continue;
                        typeTreeMap.put(nodeid, storageunittypeid);
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return typeTreeMap.containsKey(storageunittype) ? typeTreeMap.get(storageunittype) : "";
    }

    public static PropertyList getDefinitionByID(QueryProcessor queryProcessor, String storageunitid) {
        String storageunittype = OpalUtil.getColumnValue(queryProcessor, "storageunit", "storageunittype", "storageunitid = ?", new String[]{storageunitid});
        return StorageUnitUtil.getDefinition(queryProcessor, storageunittype);
    }

    public static PropertyList getMandatoryChildTypeDefinition(QueryProcessor queryProcessor, String parentStorageUnitType) {
        PropertyList storageUnitTypeProps = StorageUnitUtil.getDefinition(queryProcessor, parentStorageUnitType);
        PropertyListCollection childstorageunitprops = storageUnitTypeProps.getCollectionNotNull("childrentypes");
        if (childstorageunitprops.size() > 0) {
            for (int i = 0; i < childstorageunitprops.size(); ++i) {
                PropertyList list = childstorageunitprops.getPropertyList(i);
                if (!"Y".equals(list.getProperty("mandatory"))) continue;
                return StorageUnitUtil.getDefinition(queryProcessor, list.getProperty("type"));
            }
        }
        return new PropertyList();
    }

    public static String getPhysicalStoreDepartment(QueryProcessor queryProcessor, String storageunitid, boolean isOra) {
        StringBuilder sql = new StringBuilder();
        String departmentid = "";
        if (isOra) {
            sql.append("SELECT SU.STORAGEUNITID, SU.LINKSDCID, (select ps.departmentid from s_physicalstore ps where ps.s_physicalstoreid = SU.linkkeyid1 and SU.linksdcid = 'PhysicalStore') departmentid");
            sql.append(" FROM STORAGEUNIT SU");
            sql.append(" CONNECT BY PRIOR SU.PARENTID = SU.STORAGEUNITID");
            sql.append(" START WITH SU.STORAGEUNITID = ?");
        } else {
            sql.append("WITH StorageUnitTree (storageunitid, parentid, linksdcid, linkkeyid1, storageunittype)");
            sql.append(" AS (");
            sql.append("    SELECT su.storageunitid, su.parentid, su.linksdcid, su.linkkeyid1, su.storageunittype");
            sql.append("    FROM storageunit AS su");
            sql.append("    WHERE su.storageunitid = ?");
            sql.append("    UNION ALL");
            sql.append("    SELECT su.storageunitid, su.parentid, su.linksdcid, su.linkkeyid1, su.storageunittype");
            sql.append("   FROM storageunit AS su");
            sql.append("    INNER JOIN StorageUnitTree AS d");
            sql.append("    ON su.storageunitid = d.parentid");
            sql.append(" )");
            sql.append(" SELECT st.storageunitid, st.linksdcid, (select ps.departmentid from s_physicalstore ps where ps.s_physicalstoreid = st.linkkeyid1 and st.linksdcid = 'PhysicalStore') departmentid");
            sql.append(" FROM StorageUnitTree st");
        }
        DataSet ds = queryProcessor.getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{storageunitid});
        if (ds != null && ds.size() > 0) {
            for (int i = 0; i < ds.size(); ++i) {
                if (ds.getString(i, "departmentid", "").length() <= 0) continue;
                departmentid = ds.getString(i, "departmentid");
                break;
            }
        }
        return departmentid;
    }

    static {
        STORAGEUNITTYPELIST.add("Grid");
        STORAGEUNITTYPELIST.add("Linear");
        STORAGEUNITTYPELIST.add("No Layout");
        STORAGEUNITTYPELIST.add("Circular");
        typeTreeMap = new HashMap<String, String>();
    }
}

