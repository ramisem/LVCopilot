/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.ro;

import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.SDI;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.modules.dashboard.gizmos.BaseGizmo;
import com.labvantage.sapphire.xml.PropertyDefinitionList;
import com.labvantage.sapphire.xml.PropertyTree;
import java.io.File;
import java.io.IOException;
import sapphire.SapphireException;
import sapphire.ext.BaseSDCRO;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class LV_GizmoDefRO
extends BaseSDCRO {
    private PropertyList elementProperties = null;

    @Override
    public int gotoSection(SDI sdi) {
        super.gotoSection(sdi);
        for (int i = 0; i < this.sdiList.size(); ++i) {
            SDI curr = (SDI)this.sdiList.get(i);
            if (!curr.getKeyid1().equalsIgnoreCase(sdi.getKeyid1())) continue;
            try {
                if (!this.dataSource.equals("XMLREPORT")) {
                    this.fetchGizmoPropertiesDB();
                    return i;
                }
                this.elementProperties = new PropertyList();
                this.elementProperties.setPropertyList(this.getGizmoPropertiesFromXML(this.refReportFolder, sdi));
                return i;
            }
            catch (Exception e) {
                Trace.log("Failed to create propertytree for:" + sdi.getKeyid1());
                e.printStackTrace();
            }
        }
        this.currentSDI = null;
        this.currentSDIData = null;
        return -1;
    }

    @Override
    public void setCurrentSDIData(SDIData sdiData) throws SapphireException {
        super.setCurrentSDIData(sdiData);
        try {
            this.fetchGizmoPropertiesFromSnapshot(sdiData);
        }
        catch (SapphireException e) {
            Trace.logDebug("Failed to fetch Gizmo properties from Snapshot");
        }
    }

    public String getGizmoDefId() {
        return this.getKeyid1();
    }

    public String getValueTree() {
        return this.currentSDIData.getDataset("primary").getClob(0, "valuetree");
    }

    public String getProductValueTree() {
        return this.currentSDIData.getDataset("primary").getClob(0, "productvaluetree");
    }

    public PropertyList getElementProperties() {
        return this.elementProperties;
    }

    public void fetchGizmoPropertiesDB() throws SapphireException {
        if (this.dataSource.equals("DATABASE")) {
            sapphire.pageelements.BaseGizmo b = BaseGizmo.getInstance(this.getConnectionId(), this.getGizmoDefId(), true);
            this.elementProperties = b != null ? b.getElementProperties() : new PropertyList();
        }
    }

    public void fetchGizmoPropertiesFromSnapshot(SDIData sdiData) throws SapphireException {
        PropertyList srcProps;
        if (this.dataSource.equals("INPUT")) {
            srcProps = new PropertyList();
            String valuetreexml = sdiData.getDataset("primary").getClob(0, "productvaluetree", "");
            if (valuetreexml.length() > 0) {
                srcProps.setPropertyList(valuetreexml);
            }
        } else {
            throw new SapphireException("This should be invoked for a snapshot RO");
        }
        this.elementProperties = srcProps;
    }

    public PropertyDefinitionList fetchGizmoPropertyTreeDefinition() {
        String propertytreeid = this.getPrimaryValue("propertytreeid");
        if (this.dataSource.equals("DATABASE") || this.dataSource.equals("INPUT")) {
            try {
                PropertyTree pt = this.webAdminProcessor.getPropertyTree(propertytreeid);
                return pt.getPropertyDefinitionList();
            }
            catch (Exception e) {
                Trace.log("Failed to get property tree definition for gizmo:" + propertytreeid);
            }
        } else if (this.dataSource.equals("INPUT")) {
            try {
                PropertyTree pt = this.webAdminProcessor.getPropertyTree(propertytreeid);
                return pt.getPropertyDefinitionList();
            }
            catch (Exception e) {
                Trace.log("Failed to get property tree definition for gizmo:" + propertytreeid);
            }
        } else {
            try {
                PropertyDefinitionList pl = new PropertyDefinitionList(this.getDefinitionTreeFromXMLReport(this.refReportFolder));
                return pl;
            }
            catch (Exception e) {
                Trace.log("Failed to get property tree definition for gizmo:" + propertytreeid);
            }
        }
        return null;
    }

    public String getGizmoNode() {
        return this.getPrimaryValue("extendnodeid");
    }

    public String getGizmoTypeFromDB() {
        String sql = "SELECT propertytreeid, propertytreedesc FROM propertytree WHERE propertytreeid = '" + this.getPrimaryValue("propertytreeid") + "'";
        DataSet gizmoDefType = this.getQueryProcessor().getSqlDataSet(sql);
        return gizmoDefType.getString(0, "propertytreedesc");
    }

    public String getGizmoPropertiesFromXML(String refReportFolder) {
        return this.getGizmoPropertiesFromXML(refReportFolder, this.currentSDI);
    }

    public String getRoles() {
        DataSet ds = this.getDataSet("role");
        if (ds != null) {
            ds.sort("roleid");
            return ds.getColumnValues("roleid", ";").replaceAll(";", ",");
        }
        return "";
    }

    private String getGizmoPropertiesFromXML(String refReportFolder, SDI sdi) {
        String refSDIFileName = refReportFolder + "/xmlreport/" + this.generateSDISectionXMLFileName(sdi).replace(".xml", "_gizmoproperties.xml");
        File f = new File(refSDIFileName);
        try {
            if (f.exists()) {
                String xml = FileUtil.getFileString(f);
                return xml;
            }
        }
        catch (IOException e) {
            Trace.log("gizmoproperties does not exist in the ref report");
        }
        return null;
    }

    public String getDefinitionTreeFromXMLReport(String refReportFolder) {
        return this.getDefinitionTreeFromXMLReport(refReportFolder, this.currentSDI);
    }

    private String getDefinitionTreeFromXMLReport(String refReportFolder, SDI sdi) {
        String refSDIFileName = refReportFolder + "/xmlreport/" + this.generateSDISectionXMLFileName(sdi).replace(".xml", "_deftree.xml");
        File f = new File(refSDIFileName);
        try {
            if (f.exists()) {
                String xml = FileUtil.getFileString(f);
                return xml;
            }
        }
        catch (IOException e) {
            Trace.log("definitiontree does not exist in the ref report");
        }
        return "";
    }
}

