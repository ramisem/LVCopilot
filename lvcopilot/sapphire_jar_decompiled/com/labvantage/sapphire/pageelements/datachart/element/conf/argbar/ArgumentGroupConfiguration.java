/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.argbar;

import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.pageelements.datachart.element.conf.argbar.ArgumentBarConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.argbar.ArgumentConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.BooleanExpression;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.jsp.PageContext;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public final class ArgumentGroupConfiguration
extends BaseCustom
implements Serializable {
    private static final String DEFAULT_VISIBLE = "y";
    private final ArgumentBarConfiguration parent;
    private final List<ArgumentConfiguration> argumentConfList;
    private final String title;
    private final String argumentGroupId;
    private final boolean hasClientSideArguments;
    private final BooleanExpression visible;

    public BooleanExpression getVisible() {
        return this.visible;
    }

    public ArgumentGroupConfiguration(PropertyList argumentGroupProps, String connectionId, ArgumentBarConfiguration parent, PageContext pageContext) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (argumentGroupProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.setConnectionId(connectionId);
        this.argumentGroupId = argumentGroupProps.getProperty("argumentgroupid", "");
        if (this.argumentGroupId.isEmpty()) {
            throw new IllegalArgumentException("Argument group ID is empty");
        }
        this.title = argumentGroupProps.getProperty("title", this.argumentGroupId);
        this.visible = new BooleanExpression(argumentGroupProps.getProperty("visible", DEFAULT_VISIBLE));
        boolean hasClientSideArgs = false;
        this.argumentConfList = new ArrayList<ArgumentConfiguration>();
        PropertyListCollection argumentCollection = argumentGroupProps.getCollectionNotNull("argumentcollection");
        for (int i = 0; i < argumentCollection.size(); ++i) {
            PropertyList argumentProps = argumentCollection.getPropertyList(i);
            ArgumentConfiguration argumentConf = new ArgumentConfiguration(argumentProps, connectionId, this, pageContext);
            this.argumentConfList.add(argumentConf);
            if (hasClientSideArgs || !argumentConf.isClientSideArgument()) continue;
            hasClientSideArgs = true;
        }
        this.hasClientSideArguments = hasClientSideArgs;
        this.parent = parent;
    }

    public String getArgumentGroupId() {
        return this.argumentGroupId;
    }

    public List<ArgumentConfiguration> getArgumentConfigurationList() {
        return this.argumentConfList;
    }

    public String getTitle() {
        return this.title;
    }

    public ArgumentBarConfiguration getParent() {
        return this.parent;
    }

    public boolean hasClientSideArguments() {
        return this.hasClientSideArguments;
    }
}

