/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.gwt.server;

import com.labvantage.sapphire.pageelements.gwt.server.GWTElement;
import com.labvantage.sapphire.pageelements.gwt.server.SDIMaintPropertyHandler;
import com.labvantage.sapphire.pageelements.gwt.server.command.SDIMaint;
import com.labvantage.sapphire.util.http.HttpUtil;
import java.util.HashSet;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;

public class GWTDetailMaintElement
extends GWTElement {
    @Override
    public String getHtml() {
        StringBuffer html = new StringBuffer(super.getHtml());
        String elementDatasetname = this.element.getProperty("datasetname");
        if (this.sdiInfo != null) {
            if (elementDatasetname.length() == 0 && this.sdiInfo.getDataSet(this.elementid) != null) {
                elementDatasetname = this.elementid;
            }
            if (elementDatasetname.length() > 0) {
                String[] datasetnames;
                HashSet<String> datasets = new HashSet<String>();
                for (String datasetname : datasetnames = StringUtil.split(elementDatasetname, ",")) {
                    DataSet dataset = this.sdiInfo.getDataSet(datasetname);
                    if (dataset == null) continue;
                    datasets.add(datasetname);
                }
                SDIMaint sdiMaint = new SDIMaint(this.getSDCProcessor().getPropertyList(this.sdiInfo.getSdcid()), this.sdiInfo.getSDIData(), datasets);
                html.append("\n<input type=\"hidden\" id=\"__sdimaint_").append(this.elementid).append("\" name=\"__sdimaint_").append(this.elementid).append("\" value=\"").append(HttpUtil.htmlEncode(sdiMaint.toJSONString())).append("\"/>");
                html.append("\n<input type=\"hidden\" name=\"__sdimaint_").append(this.elementid).append("_sdcid\" value=\"").append(sdiMaint.getSdcid()).append("\"/>");
                html.append("\n<input type=\"hidden\" name=\"").append("__propertyhandler_").append(this.elementid).append("\" value=\"").append(SDIMaintPropertyHandler.class.getName()).append("\"/>");
            }
        }
        return html.toString();
    }
}

