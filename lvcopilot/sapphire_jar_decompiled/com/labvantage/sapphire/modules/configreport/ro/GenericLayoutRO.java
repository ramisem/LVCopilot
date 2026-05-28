/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.ro;

import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.modules.configreport.ro.BaseRO;
import com.labvantage.sapphire.modules.configreport.ro.WebPageRO;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.xml.PropertyTree;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Properties;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class GenericLayoutRO
extends BaseRO {
    private PropertyTree genericLayout;
    private ArrayList siteMapList;
    private HashMap siteMapDetails;
    HashMap colors;
    DataSet modules;

    @Override
    public void startChapter() throws SapphireException {
        if (this.dataSource.equals("DATABASE")) {
            WebAdminProcessor webAdminProcessor = new WebAdminProcessor(this.getConnectionId());
            try {
                this.genericLayout = webAdminProcessor.getPropertyTree("Generic");
            }
            catch (Exception e) {
                throw new SapphireException("Failed to get Generic propertytree", e);
            }
        } else {
            this.genericLayout = this.getGenericLayoutFromXMLReport(this.refReportFolder);
        }
        this.siteMapList = this.populateSitemapList();
        this.siteMapDetails = this.populateSitemapDetails();
    }

    public PropertyList getSitemapDetails(String sitemap) {
        if (this.siteMapDetails != null) {
            return (PropertyList)this.siteMapDetails.get(sitemap);
        }
        return null;
    }

    private ArrayList populateSitemapList() throws SapphireException {
        ArrayList<String> sitemaps = new ArrayList<String>();
        if (this.genericLayout != null) {
            PropertyList node = this.genericLayout.getNodePropertyList("Sapphire Custom", true);
            PropertyListCollection links = node.getCollection("links");
            for (PropertyList link : links) {
                if (!link.getProperty("group").equals("sitemap")) continue;
                String title = link.getProperty("text");
                sitemaps.add(title);
            }
        }
        Collections.sort(sitemaps, new Comparator(){

            public int compare(Object o1, Object o2) {
                String first = (String)o1;
                String second = (String)o2;
                return first.compareToIgnoreCase(second);
            }
        });
        return sitemaps;
    }

    public ArrayList getSitemapList() {
        return (ArrayList)this.siteMapList.clone();
    }

    private HashMap populateSitemapDetails() throws SapphireException {
        HashMap<String, PropertyList> siteMapDetails = new HashMap<String, PropertyList>();
        if (this.dataSource.equals("DATABASE")) {
            for (int i = 0; i < this.siteMapList.size(); ++i) {
                String currSiteMap = this.siteMapList.get(i).toString();
                PropertyList pl = this.fetchSitemapDetailsFromDB(currSiteMap);
                siteMapDetails.put(currSiteMap, pl);
            }
        } else {
            for (int i = 0; i < this.siteMapList.size(); ++i) {
                String currSiteMap = this.siteMapList.get(i).toString();
                PropertyList pl = this.getSitemapDetailsFromXMLReport(this.refReportFolder, currSiteMap);
                siteMapDetails.put(currSiteMap, pl);
            }
        }
        return siteMapDetails;
    }

    private PropertyList fetchSitemapDetailsFromDB(String sitemap) throws SapphireException {
        PropertyList node = this.genericLayout.getNodePropertyList("Sapphire Custom", true);
        PropertyListCollection links = node.getCollection("links");
        for (PropertyList link : links) {
            int start;
            String title;
            if (!link.getProperty("group").equals("sitemap") || !(title = link.getProperty("text")).equals(sitemap)) continue;
            String url = link.getProperty("link");
            int end = url.indexOf("&", start = url.indexOf("page="));
            String pageid = end == -1 ? url.substring(start + 5) : url.substring(start, end);
            String edition = "R5";
            try {
                edition = this.webAdminProcessor.getDefaultPageEdition(pageid);
            }
            catch (Exception e) {
                Trace.log("Failed to get page edition:" + e.getMessage());
            }
            String sql = "SELECT extendnodeid FROM webpagepropertytree WHERE propertytreeid='Generic' and elementid='layout' and webpageid='" + pageid + "' and productedition='" + edition + "'";
            DataSet extendnodeds = this.getQueryProcessor().getSqlDataSet(sql, true);
            if (extendnodeds.size() != 1) continue;
            String extendnodeid = extendnodeds.getValue(0, "extendnodeid");
            return this.genericLayout.getNodePropertyList(extendnodeid, true);
        }
        return new PropertyList();
    }

    public boolean checkModuleAvailable(String module) throws SapphireException {
        DataSet modules = this.getQueryProcessor().getSqlDataSet("SELECT * FROM module where moduleid = '" + module + "'");
        if (modules != null) {
            Properties licenseModules = Configuration.getInstance().getLicense(this.sapphireConnection.getDatabaseId()).getModuleProperties();
            String licensedflag = modules.getString(0, "licensedflag", "");
            if (licensedflag.equals("Y")) {
                return licenseModules.getProperty(module) != null;
            }
        }
        return true;
    }

    public PropertyTree getGenericLayout() {
        return this.genericLayout;
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

    private PropertyTree getGenericLayoutFromXMLReport(String refReportFolder) {
        String xmlFileName = this.generateSectionXMLFileName("Generic", "Layout");
        String refSDIFileName = refReportFolder + "/xmlreport/" + xmlFileName;
        File f = new File(refSDIFileName);
        try {
            if (f.exists()) {
                String xml = FileUtil.getFileString(f);
                PropertyTree pt = new PropertyTree();
                pt.setValueXML(xml);
                return pt;
            }
        }
        catch (SapphireException e) {
            Trace.log("Failed to get propertytree from refreport");
        }
        catch (IOException e) {
            Trace.log("Cannot fetch GenericLayout from ref report");
        }
        return null;
    }

    private PropertyList getSitemapDetailsFromXMLReport(String refReportFolder, String sitemap) {
        String xmlFileName = this.generateSectionXMLFileName("GenericLayout", sitemap);
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

    public DataSet getLayoutRoleMatrixFromXMLReport() {
        String xmlFileName = this.generateSectionXMLFileName("GenericLayout", "PageButtonRoleMatrix");
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

