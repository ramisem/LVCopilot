/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.adhocbrowser;

import com.labvantage.sapphire.modules.adhocbrowser.AdhocMetaData;
import com.labvantage.sapphire.modules.adhocbrowser.SDCTreeRenderer;
import com.labvantage.sapphire.modules.adhocbrowser.TreeNode;
import com.labvantage.sapphire.modules.adhocbrowser.TreeNodeRenderer;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class RootSDCTreeRenderer
extends SDCTreeRenderer {
    static final String LABVANTAGE_CVS_ID = "$Revision: 63215 $";
    private boolean showRootDropDown;

    public RootSDCTreeRenderer(boolean showRootDropDown, String sdcid, PropertyList pagedata, AdhocMetaData adhocmetadata, QueryProcessor queryProcessor, SDCProcessor sdcProcessor, TranslationProcessor tp) {
        super(sdcid, pagedata, adhocmetadata, queryProcessor, sdcProcessor, tp);
        this.showRootDropDown = showRootDropDown;
        this.isRoot = true;
        this.linkcolumnid = "";
    }

    @Override
    public PropertyList getNodePropertyList() throws SapphireException {
        TreeNode rootNode = new TreeNode();
        rootNode.setNodeid("root");
        rootNode.setNodeimage("WEB-CORE/pagetypes/adhocbrowser/images/sdc.gif");
        StringBuffer nodelabel = new StringBuffer();
        String showtextsearch = "N";
        String textsearchtitle = "Text Search";
        boolean useDefault = true;
        if (!this.showRootDropDown) {
            nodelabel.append(this.sdcid);
        } else {
            String tempsdcid;
            PropertyListCollection searchablesdcs = this.pagedata.getCollection("searchablesdcs");
            if (searchablesdcs != null && searchablesdcs.size() > 0) {
                for (int i = 0; i < searchablesdcs.size(); ++i) {
                    String sdctitle;
                    PropertyList searchableSDCPL = searchablesdcs.getPropertyList(i);
                    if ("N".equals(searchableSDCPL.getProperty("showinrootdropdown"))) continue;
                    tempsdcid = searchableSDCPL.getProperty("sdcid");
                    if (tempsdcid.equals(this.sdcid)) {
                        showtextsearch = searchableSDCPL.getProperty("showtextsearch");
                        String string = textsearchtitle = searchableSDCPL.getProperty("textsearchtitle").length() > 0 ? searchableSDCPL.getProperty("textsearchtitle") : this.tp.translate("Text Search");
                    }
                    if ((sdctitle = searchableSDCPL.getProperty("title")) == null || sdctitle.length() == 0) {
                        sdctitle = tempsdcid;
                    }
                    if (nodelabel.length() > 0) {
                        nodelabel.append("|%|");
                    }
                    nodelabel.append(tempsdcid + "|" + sdctitle + "|" + searchableSDCPL.getProperty("img"));
                }
                useDefault = false;
            }
            if (useDefault) {
                DataSet searchablesdcDs = this.queryProcessor.getSqlDataSet("select sdcid from sdc where searchableflag='Y' and ( select count(*) from syscolumn, sdc jsdc where jsdc.sdcid= sdc.sdcid and jsdc.tableid = syscolumn.tableid and syscolumn.searchableflag='Y' ) > 0 order by sdcid");
                if (searchablesdcDs != null && searchablesdcDs.getRowCount() > 0) {
                    for (int i = 0; i < searchablesdcDs.getRowCount(); ++i) {
                        tempsdcid = searchablesdcDs.getString(i, "sdcid");
                        if (nodelabel.length() > 0) {
                            nodelabel.append("|%|");
                        }
                        nodelabel.append(tempsdcid + "|" + tempsdcid + "|");
                    }
                } else {
                    nodelabel.append("No Searchable SDC Defined. Please define at least one searchable SDC.");
                }
            }
        }
        rootNode.setNodelabel(nodelabel.toString());
        this.nodes = this.getChildNodes();
        rootNode.setChildNodes(this.nodes);
        PropertyList rootPropertyList = TreeNodeRenderer.getRootNodePropertyList(rootNode);
        rootPropertyList.setProperty("showtextsearch", showtextsearch);
        rootPropertyList.setProperty("textsearchtitle", textsearchtitle);
        return rootPropertyList;
    }
}

