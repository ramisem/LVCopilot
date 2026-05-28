/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.opal.beans;

import java.io.Serializable;
import javax.servlet.jsp.PageContext;
import sapphire.accessor.QueryProcessor;
import sapphire.tagext.PageTagInfo;

public class BasePageBean
implements Serializable {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";
    private String sdcid;
    private String keyid1;
    private String keyid2;
    private String keyid3;
    protected transient PageContext pagecontext;
    protected PageTagInfo pageinfo;
    protected transient QueryProcessor __QueryProcessor;

    public String getSdcid() {
        return this.sdcid;
    }

    public void setSdcid(String parSdc) {
        this.sdcid = parSdc;
    }

    public String getKeyid1() {
        return this.keyid1;
    }

    public void setKeyid1(String parKeyid1) {
        this.keyid1 = parKeyid1;
    }

    public String getKeyid2() {
        return this.keyid2;
    }

    public void setKeyid2(String parKeyid2) {
        this.keyid2 = parKeyid2;
    }

    public String getKeyid3() {
        return this.keyid3;
    }

    public void setKeyid3(String parKeyid3) {
        this.keyid3 = parKeyid3;
    }

    public PageContext getPagecontext() {
        return this.pagecontext;
    }

    public void setPagecontext(PageContext parPageContext) {
        this.pagecontext = parPageContext;
    }

    public PageTagInfo getPageinfo() {
        return this.pageinfo;
    }

    public void setPageinfo(PageTagInfo parPageinfo) {
        this.pageinfo = parPageinfo;
    }

    public QueryProcessor getQueryProcessor() {
        if (this.__QueryProcessor == null) {
            this.__QueryProcessor = new QueryProcessor(this.getPagecontext());
        }
        return this.__QueryProcessor;
    }
}

