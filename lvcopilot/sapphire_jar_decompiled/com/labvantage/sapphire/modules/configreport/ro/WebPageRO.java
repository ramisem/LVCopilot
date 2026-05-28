/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.ro;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.SDI;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.services.RequestService;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.xml.PropertyDefinitionList;
import com.labvantage.sapphire.xml.PropertyTree;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.ext.BaseSDCRO;
import sapphire.servlet.RequestContext;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class WebPageRO
extends BaseSDCRO {
    public PropertyList pageProps = null;
    public static final String SEPARATOR = "|!|";

    @Override
    public void startSection() throws SapphireException {
        String webpageid = this.getWebPageId();
        if (webpageid == null || webpageid.length() == 0) {
            this.logger.error("Webpage id not found in startsection.");
            this.pageProps = new PropertyList();
        } else if (this.dataSource.equals("DATABASE")) {
            PropertyList dummy = new PropertyList();
            RequestContext rc = new RequestContext(dummy);
            try {
                RequestService rs = new RequestService(this.sapphireConnection);
                if (this.sapphireConnection.getConnection() == null) {
                    throw new SapphireException("Cannot get webpage properties.Connection is empty");
                }
                this.pageProps = rs.getWebPageProperties(webpageid, this.getWebPageProductEdition(), rc.getPropertyList(), false);
            }
            catch (ServiceException e) {
                throw new SapphireException("Failed to get webpage properties." + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())));
            }
        } else {
            this.pageProps = this.dataSource.equals("INPUT") ? new PropertyList() : this.getPagePropsFromXMLReport(this.refReportFolder);
        }
    }

    @Override
    public int gotoSection(SDI sdi) {
        super.gotoSection(sdi);
        for (int i = 0; i < this.sdiList.size(); ++i) {
            SDI curr = (SDI)this.sdiList.get(i);
            if (!curr.getKeyid1().equalsIgnoreCase(sdi.getKeyid1()) || curr.getKeyid2() != null && !curr.getKeyid2().equalsIgnoreCase(sdi.getKeyid2()) || curr.getKeyid3() != null && !curr.getKeyid3().equalsIgnoreCase(sdi.getKeyid3())) continue;
            this.pageProps = this.getPagePropsFromXMLReport(this.refReportFolder);
            return i;
        }
        this.pageProps = null;
        return -1;
    }

    public String getWebPageId() {
        return this.getKeyid1();
    }

    public String getWebPageProductEdition() {
        String edition = this.getPrimaryValue("productedition");
        if (edition == null || edition.length() == 0 || edition.equals("*")) {
            return "R5";
        }
        return edition;
    }

    public String getWebPageDesc() {
        return this.getDescription();
    }

    public String getWebPageFileName() {
        return this.getPrimaryValue("filename");
    }

    public static String getWebPageType(SDIData sdiData) {
        String colValue = WebPageRO.getPrimaryValue(sdiData, "webpagetypeflag");
        if ("C".equals(colValue)) {
            return "Core";
        }
        if ("S".equals(colValue)) {
            return "System";
        }
        if (sdiData.getDataset("primary") != null) {
            return "User";
        }
        return "";
    }

    public static String getIsVirtualPage(SDIData currentSDIData) {
        String colValue = WebPageRO.getPrimaryValue(currentSDIData, "virtualpageflag");
        if ("Y".equals(colValue)) {
            return "Yes";
        }
        if (currentSDIData.getDataset("primary") != null) {
            return "No";
        }
        return "";
    }

    public static String getPrimaryValue(SDIData sdiData, String columnName) {
        if (sdiData == null || sdiData.getDataset("primary") == null) {
            return "";
        }
        return sdiData.getDataset("primary").getValue(0, columnName);
    }

    public static String getIsExpressPage(SDIData sdiData) {
        String colValue = WebPageRO.getPrimaryValue(sdiData, "expresspage");
        if (colValue.length() > 0) {
            return "Yes";
        }
        if (sdiData.getDataset("primary") != null) {
            return "No";
        }
        return "";
    }

    public String getLocation() {
        return this.getPrimaryValue("location");
    }

    public boolean hasWebPageProperties() {
        DataSet ds = this.getDataSet("webpageproperty");
        return ds.getRowCount() != 0;
    }

    public DataSet getWebPageProperties() {
        DataSet ds = this.getDataSet("webpageproperty");
        DataSet ret = new DataSet();
        ret.setColidCaseSensitive(true);
        ret.addColumn("Property ID", 0);
        ret.addColumn("Property Value", 0);
        ret.addColumnValues("Property ID", 0, ds.getColumnValues("propertyid", SEPARATOR), SEPARATOR);
        ret.addColumnValues("Property Value", 0, ds.getColumnValues("propertyvalue", SEPARATOR), SEPARATOR);
        return ret;
    }

    public PropertyList getPageTypeInfo() throws SapphireException {
        return this.pageProps.getPropertyListNotNull("pagedata");
    }

    public ArrayList getWebPageElements() throws SapphireException {
        Object[] ids = this.pageProps.keySet().toArray();
        ArrayList<Object> elements = new ArrayList<Object>();
        for (int i = 0; i < ids.length; ++i) {
            if ("pagedata".equals(ids[i]) || "layout".equals(ids[i]) || ids[i].toString().startsWith("__")) continue;
            elements.add(ids[i]);
        }
        return elements;
    }

    public String[] getElementProperties(String elementid) throws SapphireException {
        PropertyList elementProps = this.pageProps.getPropertyListNotNull(elementid);
        String[] elementPropList = new String[elementProps.size()];
        Object[] ids = elementProps.keySet().toArray();
        int elementPropCount = 0;
        for (int i = 0; i < ids.length; ++i) {
            elementPropList[elementPropCount++] = (String)ids[i];
        }
        return elementPropList;
    }

    public PropertyList getElementDetails(String elementid) throws SapphireException {
        return this.pageProps.getPropertyListNotNull(elementid);
    }

    public PropertyDefinitionList getElementPropertyDefinitionList(String elementid, String propertytreeid) throws SapphireException {
        try {
            PropertyTree pt = this.webAdminProcessor.getPropertyTree(propertytreeid);
            return pt.getPropertyDefinitionList();
        }
        catch (Exception e) {
            throw new SapphireException("Failed to get definition list for:" + propertytreeid + "," + elementid, e);
        }
    }

    public ArrayList getPageRoles(DBUtil database) throws SapphireException {
        String pageId = this.getKeyid1();
        ArrayList<String> roles = new ArrayList<String>();
        database.createPreparedResultSet("roles", "SELECT roleid FROM sdirole WHERE sdcid='WebPage' and keyid1=?", new String[]{pageId});
        while (database.getNext("roles")) {
            roles.add(database.getString("roles", "roleid"));
        }
        database.closeStatement("roles");
        return roles;
    }

    public static String getPageType(SDIData currentSDIData) {
        String pageType = "";
        DataSet childWebPagePropertyTrees = currentSDIData.getDataset("webpagepropertytree");
        if (childWebPagePropertyTrees != null) {
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("elementid", "pagedata");
            DataSet pageDataRow = childWebPagePropertyTrees.getFilteredDataSet(filter);
            if (pageDataRow != null && pageDataRow.size() > 0 && (pageType = pageDataRow.getValue(0, "propertytreeid", "")).length() == 0) {
                String location = currentSDIData.getDataset("primary").getValue(0, "location", "");
                String filename = currentSDIData.getDataset("primary").getValue(0, "filename", "");
                pageType = "Custom (" + (location == null ? "" : location) + filename + ")";
            }
        }
        return pageType;
    }

    public String getPageType() {
        if (this.pageProps == null) {
            Trace.log("pageprops is null");
            return "";
        }
        PropertyList pagetypeprops = this.pageProps.getPropertyListNotNull("pagedata");
        String pageType = pagetypeprops.getProperty("propertytreeid", "");
        if (pageType.length() == 0) {
            String location = this.getLocation();
            pageType = "Custom (" + (location == null ? "" : location) + this.getWebPageFileName() + ")";
        }
        return pageType;
    }

    private PropertyList getPagePropsFromXMLReport(String refReportFolder) {
        String fileName = refReportFolder + "/xmlreport/" + this.generateSDISectionXMLFileName(this.currentSDI);
        fileName = fileName.replace(".xml", "_pageprops.xml");
        File f = new File(fileName);
        try {
            if (f.exists()) {
                String xml = FileUtil.getFileString(f, "UTF-8");
                PropertyList pl = new PropertyList();
                pl.setPropertyList(xml, false, false);
                return pl;
            }
        }
        catch (SapphireException e) {
            Trace.log("Failed to read pageprops from xml file." + e.getMessage());
        }
        catch (IOException e) {
            Trace.log("SDI does not exist in the ref report");
        }
        return null;
    }
}

