/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.ro;

import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.modules.configreport.ro.BaseRO;
import com.labvantage.sapphire.platform.Configuration;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.xml.PropertyList;

public class CoverPageRO
extends BaseRO {
    @Override
    public void startChapter() {
        if (this.createdBy == null || this.createdBy.length() == 0) {
            this.createdBy = this.sapphireConnection.getSysuserName();
            if (this.createdBy == null || this.createdBy.length() == 0) {
                this.createdBy = this.sapphireConnection.getSysuserId();
            }
        } else if (!this.dataSource.equals("XMLREPORT")) {
            String sql = "SELECT sysuserdesc from sysuser where sysuserid='" + this.createdBy + "'";
            DataSet ds = this.getQueryProcessor().getSqlDataSet(sql);
            if (ds != null && ds.getRowCount() > 0 && ds.getString(0, "sysuserdesc", "").length() > 0) {
                this.createdBy = ds.getString(0, "sysuserdesc");
            }
        }
    }

    public PropertyList getReportInfo() throws SapphireException {
        PropertyList ret = new PropertyList();
        if (this.dataSource.equals("XMLREPORT")) {
            String fileName = this.refReportFolder + "/xmlreport/coverpage.xml";
            File f = new File(fileName);
            try {
                String xml = FileUtil.getFileString(f);
                ret.setPropertyList(xml, false, false);
            }
            catch (IOException e) {
                Trace.logError("Could not read TOC file:" + e.getMessage());
                return null;
            }
        } else {
            M18NUtil util = new M18NUtil(this.sapphireConnection);
            ret.setProperty("date", util.format(Calendar.getInstance(), true));
            ret.setProperty("createdby", this.createdBy);
            ret.setProperty("database", this.sapphireConnection.getDatabaseId());
            ret.setProperty("isora", this.getConnectionProcessor().isOra() ? "Y" : "N");
            Configuration conf = Configuration.getInstance();
            ret.setProperty("hostname", conf.getServerHostName());
            ret.setProperty("port", conf.getHttpPort());
            ret.setProperty("build", conf.getAdmindbBuild());
            ret.setProperty("serverinfo", conf.getServerInfo());
            ret.setProperty("reportfolder", this.folder);
        }
        return ret;
    }
}

