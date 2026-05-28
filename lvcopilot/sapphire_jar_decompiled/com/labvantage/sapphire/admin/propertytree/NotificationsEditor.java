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

public class NotificationsEditor
implements TypeSimple {
    @Override
    public String getEditor(String fieldName, PropertyValue propertyValue, PropertyList topPropertyList, boolean ancestorValue, HashMap attributes, PageContext pageContext, boolean debug) {
        Button b = new Button(pageContext);
        b.setAction("var fieldValue = document.getElementById('" + fieldName + "').value;if ( fieldValue.indexOf( '{|' ) == 0 && fieldValue.indexOf( '|}' ) > 0 ) { fieldValue = fieldValue.substring( 2, fieldValue.length - 2 );}var pl = sapphire.util.propertyList.create();pl.set( 'fv', fieldValue );var jsonPL = sapphire.util.propertyList.toJSONString( pl );sapphire.ui.dialog.open( 'Notifications', 'rc?command=file&file=WEB-CORE/modules/eventmanager/notificationseditor.jsp&jsonpl=' + jsonPL, true, 650, 400, {'OK': 'notificationsEditorCallback( this.dialog.frame.getNotifications() );sapphire.ui.dialog.close( this.dialogNumber );', 'Cancel' : 'sapphire.ui.dialog.close( this.dialogNumber );'} );");
        b.setImg("WEB-CORE/elements/images/ellipsisblank.gif");
        b.setMargin("none");
        b.setHighlight("false");
        StringBuffer output = new StringBuffer();
        output.append("<script>");
        output.append("function notificationsEditorCallback( notifications ) {");
        output.append("  var field = document.getElementById( '" + fieldName + "' );");
        output.append("  field.value = notifications;");
        output.append("  sapphire.events.fireEvent(field, 'onchange');");
        output.append("}");
        output.append("</script>");
        output.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr><td>");
        output.append("<textarea name=\"" + fieldName + "\" id=\"" + fieldName + "\" style=\"width:250px " + (ancestorValue ? "; color:blue" : "") + "\" onchange=\"propertyChange()\" readonly >" + propertyValue + "</textarea>");
        output.append("</td><td>").append(b.getHtml()).append("</td></tr></table>");
        return output.toString();
    }
}

