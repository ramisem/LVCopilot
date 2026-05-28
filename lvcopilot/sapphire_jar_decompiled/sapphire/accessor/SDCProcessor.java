/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package sapphire.accessor;

import com.labvantage.sapphire.BaseAccessor;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.services.DDTConstants;
import java.io.File;
import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SDCProcessor
extends BaseAccessor
implements DDTConstants {
    private static HashMap propertyListCache = new HashMap();

    public SDCProcessor(String connectionid) {
        super(connectionid);
    }

    public SDCProcessor(String nameserverlist, String connectionid) {
        super(connectionid);
    }

    public SDCProcessor(File rakFile, String connectionid) {
        super(rakFile, connectionid);
    }

    public SDCProcessor(PageContext pageContext) {
        super(pageContext);
    }

    public PropertyList getPropertyList(String sdcid) {
        return this.getPropertyList(sdcid, true);
    }

    private PropertyList getPropertyList(String sdcid, boolean copy) {
        if (sdcid == null || sdcid.length() == 0) {
            Trace.logWarn("No SDC passed to SDCProcessor.getXX method.");
            return null;
        }
        PropertyList propertyList = null;
        try {
            PropertyList propertyList2 = propertyList = local ? this.getLocalAccessManager().getSDCProperties(this.getConnectionid(), sdcid) : this.getRemoteAccessManager().getSDCProperties(this.getConnectionid(), sdcid);
            if (propertyList != null && copy) {
                propertyList = propertyList.copy();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return propertyList;
    }

    public String getSDCColumnProperty(String sdcid, String columnid, String propertyname) {
        if (sdcid == null || sdcid.length() == 0) {
            Trace.logWarn("No SDC passed to SDCProcessor.getXX method.");
            return "";
        }
        String value = "";
        try {
            PropertyListCollection columns = this.getPropertyList(sdcid, false).getCollection("columns");
            PropertyList column = columns.find("columnid", columnid);
            value = column != null ? column.getProperty(propertyname) : "";
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public String getProperty(String sdcid, String propertyid) {
        return this.getProperty(sdcid, propertyid, "");
    }

    public String getProperty(String sdcid, String propertyid, String defaultValue) {
        if (sdcid == null || sdcid.length() == 0) {
            Trace.logWarn("No SDC passed to SDCProcessor.getXX method.");
            return "";
        }
        String value = "";
        try {
            PropertyList sdc = this.getPropertyList(sdcid, false);
            value = sdc.getProperty(propertyid, defaultValue);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public PropertyList getProperties(String sdcid) {
        if (sdcid == null || sdcid.length() == 0) {
            Trace.logWarn("No SDC passed to SDCProcessor.getXX method.");
            return null;
        }
        PropertyList propertyList = this.getPropertyList(sdcid, false);
        PropertyList sdcProperties = new PropertyList();
        for (String propertyid : propertyList.keySet()) {
            Object propertyvalue = propertyList.get(propertyid);
            if (!(propertyvalue instanceof String)) continue;
            sdcProperties.setProperty(propertyid, (String)propertyvalue);
        }
        return sdcProperties;
    }

    public HashMap getSDCProperties(String sdcid) {
        if (sdcid == null || sdcid.length() == 0) {
            Trace.logWarn("No SDC passed to SDCProcessor.getXX method.");
            return null;
        }
        HashMap sdcprops = new HashMap();
        try {
            PropertyList propertyList = this.getPropertyList(sdcid, false);
            for (String propertyid : propertyList.keySet()) {
                Object propertyvalue = propertyList.get(propertyid);
                if (!(propertyvalue instanceof String)) continue;
                sdcprops.put(propertyid, propertyvalue);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return sdcprops;
    }

    public PropertyListCollection getColumns(String sdcid) {
        if (sdcid == null || sdcid.length() == 0) {
            Trace.logWarn("No SDC passed to SDCProcessor.getXX method.");
            return null;
        }
        PropertyListCollection columns = new PropertyListCollection();
        try {
            PropertyList propertyList = this.getPropertyList(sdcid, false);
            PropertyListCollection cols = propertyList.getCollection("columns");
            if (cols == null) {
                throw new SapphireException("Columns not found");
            }
            for (int i = 0; i < cols.size(); ++i) {
                columns.add(cols.getPropertyList(i).copy());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return columns;
    }

    public PropertyListCollection getAttributes(String sdcid) {
        if (sdcid == null || sdcid.length() == 0) {
            Trace.logWarn("No SDC passed to SDCProcessor.getXX method.");
            return null;
        }
        PropertyListCollection attributes = new PropertyListCollection();
        try {
            PropertyList propertyList = this.getPropertyList(sdcid, false);
            PropertyListCollection attribs = propertyList.getCollection("attributes");
            if (attribs == null) {
                throw new SapphireException("Attributes not found");
            }
            for (int i = 0; i < attribs.size(); ++i) {
                attributes.add(attribs.getPropertyList(i).copy());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return attributes;
    }

    public HashMap getLinkProperties(String sdcid, String linkid) {
        if (sdcid == null || sdcid.length() == 0) {
            Trace.logWarn("No SDC passed to SDCProcessor.getXX method.");
            return null;
        }
        HashMap linkprops = null;
        try {
            PropertyList propertyList = this.getPropertyList(sdcid, false);
            PropertyListCollection links = propertyList.getCollection("links");
            PropertyList link = links.getPropertyList(linkid);
            if (link == null) {
                throw new SapphireException("Link not found");
            }
            linkprops = new HashMap();
            linkprops.putAll(link.copy());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return linkprops;
    }

    public HashMap getDetailLinkProperties(String sdcid, String linkid) {
        if (sdcid == null || sdcid.length() == 0) {
            Trace.logWarn("No SDC passed to SDCProcessor.getXX method.");
            return null;
        }
        HashMap linkprops = null;
        try {
            PropertyList propertyList = this.getPropertyList(sdcid, false);
            PropertyListCollection links = propertyList.getCollection("detaillinks");
            PropertyList link = links.getPropertyList(linkid);
            if (link == null) {
                throw new SapphireException("Link not found");
            }
            linkprops = new HashMap();
            linkprops.putAll(link.copy());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return linkprops;
    }

    public DataSet getColumnData(String sdcid) {
        if (sdcid == null || sdcid.length() == 0) {
            Trace.logWarn("No SDC passed to SDCProcessor.getXX method.");
            return null;
        }
        try {
            PropertyList propertyList = this.getPropertyList(sdcid, false);
            PropertyListCollection columns = propertyList.getCollection("columns");
            if (columns == null) {
                throw new SapphireException("Column data not found");
            }
            DataSet columnData = new DataSet();
            for (int i = 0; i < columns.size(); ++i) {
                PropertyList column = columns.getPropertyList(i);
                int row = columnData.addRow();
                columnData.setString(row, "columnid", column.getProperty("columnid"));
                columnData.setString(row, "pkflag", column.getProperty("pkflag"));
                columnData.setString(row, "datatype", column.getProperty("datatype"));
                columnData.setNumber(row, "columnlength", column.getProperty("columnlength"));
                columnData.setString(row, "columnlabel", column.getProperty("columnlabel"));
            }
            return columnData;
        }
        catch (Exception e) {
            this.setError(e.getMessage(), e);
            return null;
        }
    }

    public DataSet getLinksData(String sdcid) {
        if (sdcid == null || sdcid.length() == 0) {
            Trace.logWarn("No SDC passed to SDCProcessor.getXX method.");
            return null;
        }
        try {
            PropertyList propertyList = this.getPropertyList(sdcid, false);
            PropertyListCollection links = propertyList.getCollection("links");
            if (links == null) {
                throw new SapphireException("Link data not found");
            }
            DataSet linksData = new DataSet();
            for (int i = 0; i < links.size(); ++i) {
                PropertyList link = links.getPropertyList(i);
                int row = linksData.addRow();
                linksData.setString(row, "linkid", link.getProperty("linkid"));
                linksData.setString(row, "linktype", link.getProperty("linktype"));
                linksData.setString(row, "linktableid", link.getProperty("linktableid", null));
                linksData.setString(row, "sdccolumnid", link.getProperty("sdccolumnid", null));
                linksData.setString(row, "sdccolumnid2", link.getProperty("sdccolumnid2", null));
                linksData.setString(row, "sdccolumnid3", link.getProperty("sdccolumnid3", null));
                linksData.setString(row, "loadflag", link.getProperty("loadflag"));
                linksData.setString(row, "deleteflag", link.getProperty("deleteflag"));
                linksData.setString(row, "sdcid", link.getProperty("sdcid"));
                linksData.setString(row, "lookuppageid", link.getProperty("lookuppageid", null));
                linksData.setString(row, "linksdcid", link.getProperty("linksdcid", null));
                linksData.setString(row, "tableid", link.getProperty("tableid", null));
                linksData.setString(row, "reftypeid", link.getProperty("reftypeid", null));
                linksData.setString(row, "linksdccolumnid", link.getProperty("linksdccolumnid", null));
            }
            return linksData;
        }
        catch (Exception e) {
            this.setError(e.getMessage(), e);
            return null;
        }
    }

    public DataSet getReverseLinksData(String sdcid) {
        if (sdcid == null || sdcid.length() == 0) {
            Trace.logWarn("No SDC passed to SDCProcessor.getXX method.");
            return null;
        }
        try {
            return local ? this.getLocalAccessManager().getReverseLinksData(this.getConnectionid(), sdcid) : this.getRemoteAccessManager().getReverseLinksData(this.getConnectionid(), sdcid);
        }
        catch (Exception e) {
            this.setError(e.getMessage(), e);
            return null;
        }
    }

    public DataSet getTableColumnData(String tableid) {
        try {
            PropertyListCollection columns;
            PropertyListCollection propertyListCollection = columns = local ? this.getLocalAccessManager().getTableColumns(this.getConnectionid(), tableid) : this.getRemoteAccessManager().getTableColumns(this.getConnectionid(), tableid);
            if (columns == null) {
                throw new SapphireException("Column data not found");
            }
            DataSet columnData = new DataSet();
            for (int i = 0; i < columns.size(); ++i) {
                PropertyList column = columns.getPropertyList(i);
                int row = columnData.addRow();
                columnData.setString(row, "columnid", column.getProperty("columnid"));
                columnData.setString(row, "columndesc", column.getProperty("columndesc"));
                columnData.setString(row, "columnlabel", column.getProperty("columnlabel"));
                columnData.setString(row, "columndoc", column.getProperty("columndoc"));
                columnData.setString(row, "pkflag", column.getProperty("pkflag"));
                columnData.setString(row, "datatype", column.getProperty("datatype"));
                columnData.setNumber(row, "columnlength", column.getProperty("columnlength"));
            }
            return columnData;
        }
        catch (Exception e) {
            this.setError(e.getMessage(), e);
            return null;
        }
    }

    public PropertyListCollection getDetailLinks(String sdcid) {
        if (sdcid == null || sdcid.length() == 0) {
            Trace.logWarn("No SDC passed to SDCProcessor.getXX method.");
            return null;
        }
        PropertyListCollection detailLinksCollection = new PropertyListCollection();
        try {
            PropertyList propertyList = this.getPropertyList(sdcid, false);
            PropertyListCollection detailLinks = propertyList.getCollection("detaillinks");
            if (detailLinks == null) {
                throw new SapphireException("DetailLinks Not found");
            }
            for (int i = 0; i < detailLinks.size(); ++i) {
                detailLinksCollection.add(detailLinks.getPropertyList(i).copy());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return detailLinksCollection;
    }

    public PropertyListCollection getLinks(String sdcid) {
        if (sdcid == null || sdcid.length() == 0) {
            Trace.logWarn("No SDC passed to SDCProcessor.getXX method.");
            return null;
        }
        PropertyListCollection detailLinksCollection = new PropertyListCollection();
        try {
            PropertyList propertyList = this.getPropertyList(sdcid, false);
            PropertyListCollection detailLinks = propertyList.getCollection("links");
            if (detailLinks == null) {
                throw new SapphireException("Links Not found");
            }
            for (int i = 0; i < detailLinks.size(); ++i) {
                detailLinksCollection.add(detailLinks.getPropertyList(i).copy());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return detailLinksCollection;
    }
}

