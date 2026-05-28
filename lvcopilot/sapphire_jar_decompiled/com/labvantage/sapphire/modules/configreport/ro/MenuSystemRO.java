/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.ro;

import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.modules.configreport.ro.BaseRO;
import com.labvantage.sapphire.modules.configreport.ro.WebPageRO;
import com.labvantage.sapphire.modules.dashboard.gizmos.BaseGizmo;
import com.labvantage.sapphire.modules.dashboard.gizmos.MenuGizmo;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.services.SecurityService;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import sapphire.SapphireException;
import sapphire.accessor.SDIProcessor;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.xml.PropertyList;

public class MenuSystemRO
extends BaseRO {
    private ArrayList menuNameList;
    private HashMap<String, PropertyList> menuDetails;
    HashMap colors;
    DataSet modules;

    @Override
    public void startChapter() throws SapphireException {
        block5: {
            block4: {
                this.menuDetails = new PropertyList();
                this.menuNameList = new ArrayList();
                if (!this.dataSource.equals("DATABASE")) break block4;
                DataSet menugizmos = MenuSystemRO.getGizmoTargets("", "menugizmo", this.getSDIProcessor());
                if (menugizmos == null) break block5;
                for (int i = 0; i < menugizmos.getRowCount(); ++i) {
                    String menugizmodefid = menugizmos.getValue(i, "gizmodefid");
                    MenuGizmo current = (MenuGizmo)BaseGizmo.getInstance(this.getConnectionId(), menugizmodefid, true);
                    if (current == null) {
                        Logger.logDebug("Null menu found for " + menugizmodefid);
                    }
                    if (current == null || !current.getElementProperties().getProperty("showinpicker", "N").equalsIgnoreCase("Y")) continue;
                    String title = current.getTitle();
                    if (title == null || title.length() == 0) {
                        title = menugizmos.getValue(i, "gizmodefdesc", menugizmodefid);
                    }
                    this.menuNameList.add(title);
                    this.menuDetails.put(title, current.getElementProperties());
                }
                break block5;
            }
            PropertyList menuDetailsXML = this.getMenuDetailsFromXMLReport(this.refReportFolder);
            Object[] arr = menuDetailsXML.keySet().toArray();
            for (int i = 0; i < arr.length; ++i) {
                String currmenu = (String)arr[i];
                this.menuNameList.add(currmenu);
                String menuGizmoXML = (String)menuDetailsXML.get(currmenu);
                PropertyList menuGizmoPL = new PropertyList();
                menuGizmoPL.setPropertyList(menuGizmoXML);
                this.menuDetails.put(currmenu, menuGizmoPL);
            }
        }
    }

    public ArrayList getMenuNameList() throws SapphireException {
        return this.menuNameList;
    }

    public PropertyList getMenuDetailsXML() throws SapphireException {
        ArrayList menuList = this.getMenuNameList();
        PropertyList menuDetails = new PropertyList();
        for (int i = 0; i < menuList.size(); ++i) {
            String menuName = menuList.get(i).toString();
            PropertyList currGizmo = this.getMenuDetails(menuName);
            menuDetails.setProperty(menuName, currGizmo.toXMLString());
        }
        return menuDetails;
    }

    public PropertyList getMenuDetails(String menu) {
        return this.menuDetails.get(menu);
    }

    public static DataSet getGizmoTargets(String category, String gizmoType, SDIProcessor sdiProcessor) {
        DataSet gizmoTargets = null;
        if (sdiProcessor != null) {
            SDIRequest sdiReq = new SDIRequest();
            sdiReq.setSDCid("LV_GizmoDef");
            if (category.length() > 0) {
                sdiReq.setQueryFrom("gizmodef, categoryitem");
            } else {
                sdiReq.setQueryFrom("gizmodef");
            }
            StringBuffer where = new StringBuffer();
            if (category.length() > 0) {
                where.append("gizmodefid IN (SELECT categoryitem.keyid1 FROM categoryitem WHERE categoryitem.sdcid='LV_GizmoDef' AND categoryitem.categoryid='").append(category).append("')");
                if (gizmoType.length() > 0) {
                    where.append(" AND ");
                }
            }
            if (gizmoType.length() > 0) {
                where.append("propertytreeid='").append(gizmoType).append("'");
            }
            if (where.length() > 0) {
                sdiReq.setQueryWhere(where.toString());
            }
            sdiReq.setRequestItem("primary");
            sdiReq.setQueryOrderBy("gizmodefid");
            sdiReq.setExtendedDataTypes(true);
            SDIData sdi = sdiProcessor.getSDIData(sdiReq);
            if (sdi != null) {
                gizmoTargets = sdi.getDataset("primary");
            }
        }
        return gizmoTargets;
    }

    public WebPageRO getWebPageRO() throws SapphireException {
        WebPageRO webpageRO = new WebPageRO();
        if (this.dataSource.equals("DATABASE")) {
            webpageRO.initialize("WebPage", this.sapphireConnection, this.folder, this.createdBy);
        } else {
            webpageRO.initialize("WebPage", this.folder, this.createdBy, this.refReportFolder, this.sapphireConnection);
        }
        return webpageRO;
    }

    public boolean checkModuleAvailable(String module) throws SapphireException {
        DataSet modules = this.getQueryProcessor().getSqlDataSet("SELECT * FROM module where moduleid = '" + module + "'");
        if (modules != null) {
            Properties licenseModules = Configuration.getInstance().getLicense(SecurityService.getDatabaseId(this.getConnectionid())).getModuleProperties();
            String licensedflag = modules.getString(0, "licensedflag", "");
            if (licensedflag.equals("Y")) {
                return licenseModules.getProperty(module) != null;
            }
        }
        return true;
    }

    private PropertyList getMenuDetailsFromXMLReport(String refReportFolder) {
        String xmlFileName = this.generateSectionXMLFileName("Menu", "System");
        String refSDIFileName = refReportFolder + "/xmlreport/" + xmlFileName;
        File f = new File(refSDIFileName);
        try {
            if (f.exists()) {
                String xml = FileUtil.getFileString(f);
                PropertyList pl = new PropertyList();
                pl.setPropertyList(xml, false, false);
                return pl;
            }
        }
        catch (SapphireException e) {
            Trace.log("Cannot fetch sitemap details from ref report" + e.getMessage());
        }
        catch (IOException e) {
            Trace.log("Cannot fetch sitemap details from ref report");
        }
        return null;
    }

    public String generateSectionXMLFileName(String chapterName, String sectionName) {
        sectionName = chapterName + "_" + sectionName.replaceAll(",", "");
        String sectionFileName = sectionName.trim().replaceAll(" ", "_");
        return sectionFileName + ".xml";
    }

    public DataSet getMenuDrivenRoleMatrixFromXMLReport() {
        String xmlFileName = this.generateSectionXMLFileName("MenuDriven", "PageButtonRoleMatrix");
        String refSDIFileName = this.refReportFolder + "/xmlreport/" + xmlFileName;
        File f = new File(refSDIFileName);
        try {
            if (f.exists()) {
                String xml = FileUtil.getFileString(f);
                DataSet ds = new DataSet();
                ds.setColidCaseSensitive(true);
                ds.setXML(xml);
                return ds;
            }
        }
        catch (IOException e) {
            Trace.log("Cannot fetch sitemap page button rolematrix from xmlreport");
        }
        return null;
    }
}

