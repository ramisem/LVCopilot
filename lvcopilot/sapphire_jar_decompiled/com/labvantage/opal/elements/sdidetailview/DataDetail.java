/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.elements.sdidetailview;

import com.labvantage.opal.elements.sdidetailview.DataDetailByDataSet;
import com.labvantage.opal.elements.sdidetailview.DataDetailBySpec;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;

public class DataDetail
extends BaseElement {
    private String LABVANTAGE_CVS_ID = "$Revision: 50515 $";
    private TranslationProcessor __Tp;

    private void setTranslationProcessor() {
        this.__Tp = this.getTranslationProcessor();
    }

    @Override
    public String getHtml() {
        this.setTranslationProcessor();
        String dataView = "";
        dataView = this.element.getProperty("dataviewmode");
        if (dataView.equalsIgnoreCase("By DataSet")) {
            DataDetailByDataSet dataDetail = new DataDetailByDataSet(this.pageContext, this.getConnectionId());
            dataDetail.setElementProperties(this.element);
            return dataDetail.getHtml();
        }
        if (dataView.equalsIgnoreCase("By Sample")) {
            DataDetailByDataSet dataDetail = new DataDetailByDataSet(this.pageContext, this.getConnectionId());
            dataDetail.setElementProperties(this.element);
            return dataDetail.getHtml();
        }
        if (dataView.equalsIgnoreCase("By Spec")) {
            DataDetailBySpec dataDetail = new DataDetailBySpec(this.pageContext, this.getConnectionId());
            dataDetail.setElementProperties(this.element);
            return dataDetail.getHtml();
        }
        return this.__Tp.translate("Invalid dataviewmode set in the properties for the DataDetail element. Cannot continue.");
    }
}

