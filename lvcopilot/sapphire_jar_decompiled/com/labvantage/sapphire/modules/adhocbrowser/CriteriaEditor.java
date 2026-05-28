/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.adhocbrowser;

import com.labvantage.sapphire.modules.adhocbrowser.AdhocMetaData;
import com.labvantage.sapphire.pageelements.ElementUtil;
import com.labvantage.sapphire.tagext.SDITagUtil;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.xml.PropertyList;

public abstract class CriteriaEditor {
    protected static int uniqueid = 0;

    protected static synchronized String getUniqueId() {
        return "" + ++uniqueid;
    }

    public abstract PropertyList getEditorProperty(String var1, PropertyList var2, AdhocMetaData var3, SDCProcessor var4, SDITagUtil var5, TranslationProcessor var6, PropertyList var7) throws SapphireException;

    public static void setDefaultLookupLink(PropertyList column, String linksdcid, QueryProcessor qp) {
        PropertyList lookuplink;
        if (column.getPropertyList("lookuplink") == null) {
            column.setProperty("lookuplink", new PropertyList());
        }
        if ((lookuplink = column.getPropertyList("lookuplink")).getProperty("dialogtype").length() == 0) {
            lookuplink.setProperty("dialogtype", "Sapphire Dialog");
        }
        if (lookuplink.getProperty("href").length() == 0) {
            String pageid = ElementUtil.getSDCLookUpPage(linksdcid, qp);
            if (pageid != null && pageid.length() > 0 && !pageid.equals("ReportLookup")) {
                if ("SampleTypeLookup".equals(pageid)) {
                    pageid = "SampleTypeLookSingle";
                }
                lookuplink.setProperty("href", pageid);
            } else {
                lookuplink.setProperty("href", "rc?command=file&file=WEB-CORE/lookup/lookup.jsp&sdcid=" + linksdcid);
            }
        }
        if ("Y".equals(lookuplink.getProperty("enablesuggest")) && (linksdcid == null || linksdcid.length() == 0)) {
            if (lookuplink.getProperty("sdcid").length() == 0) {
                if (lookuplink.getProperty("href").indexOf("sdcid=") > 0) {
                    linksdcid = lookuplink.getProperty("href").substring(lookuplink.getProperty("href").indexOf("sdcid=") + 6);
                    column.setProperty("sdcid", linksdcid);
                }
            } else {
                column.setProperty("sdcid", lookuplink.getProperty("sdcid"));
            }
        }
    }
}

