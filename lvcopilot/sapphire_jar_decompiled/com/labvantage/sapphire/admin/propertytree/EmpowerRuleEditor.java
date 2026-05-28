/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.admin.propertytree;

import com.labvantage.sapphire.admin.propertytree.TypeSimple;
import com.labvantage.sapphire.pageelements.controls.Button;
import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyValue;

public class EmpowerRuleEditor
implements TypeSimple {
    @Override
    public String getEditor(String fieldname, PropertyValue propertyValue, PropertyList toppropertylist, boolean ancestorvalue, HashMap attributes, PageContext pagecontext, boolean debug) {
        String datasetname = attributes.get("datasetname").toString();
        StringBuffer out = new StringBuffer("<table cellpadding=\"0\" cellspacing=\"0\"><tr><td><input onchange=\"propertyChange()\" onkeyup=\"propertyChange()\" type=\"text\" name=\"" + fieldname + "\" id=\"" + fieldname + "\" style=\"width:250px " + (ancestorvalue ? "; color:blue" : "") + "\" onchange=\"this.style.color='black';checkEvent( this )\" value=\"" + propertyValue + "\"/></td>");
        Button b = new Button(pagecontext);
        if (datasetname.equals("SampleSet")) {
            b.setAction("lookupempowerrule_sampleset( '" + fieldname + "', '" + datasetname + "' )");
        } else if (datasetname.equals("SampleSetLine")) {
            b.setAction("lookupempowerrule_samplesetline( '" + fieldname + "', '" + datasetname + "' )");
        } else if (datasetname.equals("Result")) {
            b.setAction("lookupempowerrule_result( '" + fieldname + "', '" + datasetname + "' )");
        } else if (datasetname.equals("Peak")) {
            b.setAction("lookupempowerrule_peak( '" + fieldname + "', '" + datasetname + "' )");
        } else if (datasetname.equals("DownloadSampleSetMethod")) {
            b.setAction("lookupempower_downloadrule_samplesetmethod( '" + fieldname + "', '" + datasetname + "' )");
        } else if (datasetname.equals("DownloadSampleSetLine")) {
            b.setAction("lookupempower_downloadrule_samplesetline( '" + fieldname + "', '" + datasetname + "' )");
        } else if (datasetname.equals("DownloadReagentLotLine")) {
            b.setAction("lookupempower_downloadrule_reagentlotline( '" + fieldname + "', '" + datasetname + "' )");
        } else if (datasetname.equals("DownloadComponent")) {
            b.setAction("lookupempower_downloadrule_component( '" + fieldname + "', '" + datasetname + "' )");
        } else if (datasetname.equals("DownloadReagentLotComponent")) {
            b.setAction("lookupempower_downloadrule_reagentlotcomponent( '" + fieldname + "', '" + datasetname + "' )");
        } else {
            return "Not Supported";
        }
        b.setImg("WEB-CORE/elements/images/ellipsisblank.gif");
        b.setMargin("none");
        b.setHighlight("false");
        out.append("<td>" + b.getHtml() + "</td></tr></table>");
        return out.toString();
    }
}

