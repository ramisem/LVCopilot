/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.admin.propertytree;

import com.labvantage.sapphire.admin.propertytree.TypeSimple;
import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyValue;

public class ReadOnlyEditor
implements TypeSimple {
    @Override
    public String getEditor(String fieldname, PropertyValue propertyValue, PropertyList toppropertylist, boolean ancestorvalue, HashMap attributes, PageContext pagecontext, boolean debug) {
        return propertyValue.value;
    }
}

