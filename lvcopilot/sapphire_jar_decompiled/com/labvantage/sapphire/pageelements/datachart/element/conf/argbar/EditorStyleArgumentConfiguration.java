/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.argbar;

import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.pageelements.datachart.element.conf.argbar.ArgumentConfiguration;
import com.labvantage.sapphire.pageelements.maint.EditorStyleField;
import java.io.Serializable;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public final class EditorStyleArgumentConfiguration
extends BaseCustom
implements Serializable {
    private final ArgumentConfiguration parent;
    private final String editorStyle;
    private final String html;

    public EditorStyleArgumentConfiguration(PropertyList editorStyleArgumentProps, String connectionId, ArgumentConfiguration parent, PageContext pageContext) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (editorStyleArgumentProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.setConnectionId(connectionId);
        this.editorStyle = editorStyleArgumentProps.getProperty("editorstyle");
        StringBuilder html = new StringBuilder();
        EditorStyleField editorStyleField = new EditorStyleField(pageContext);
        try {
            editorStyleField.setEditorStyleId(this.editorStyle);
            editorStyleField.setFieldName(parent.getArgumentId());
            PropertyList column = new PropertyList();
            column.setProperty("columnid", parent.getArgumentId());
            PropertyListCollection events = new PropertyListCollection();
            PropertyList oninput = new PropertyList();
            oninput.setProperty("event", "oninput");
            oninput.setProperty("js", "this.value=this.value");
            events.add(oninput);
            column.setProperty("events", events);
            editorStyleField.setReadonly(false);
            editorStyleField.setChangeEvent("console.log('ON CHANGE EVENT');");
            editorStyleField.setColumn(column);
            html.append(editorStyleField.getHtml());
        }
        catch (SapphireException e) {
            html.append("<span>Error rendering editorstyle:" + this.editorStyle + "." + e.getMessage() + "</span>");
        }
        this.html = html.toString();
        this.parent = parent;
    }

    public ArgumentConfiguration getParent() {
        return this.parent;
    }

    public String getEditorStyle() {
        return this.editorStyle;
    }

    public String getHtml() {
        return this.html;
    }
}

