/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.argbar;

import com.labvantage.sapphire.pageelements.datachart.element.conf.argbar.ArgumentGroupConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.argbar.IncludeConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.StringExpression;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.jsp.PageContext;
import sapphire.util.ConnectionInfo;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public final class ArgumentBarConfiguration
implements Serializable {
    private static final String DEFAULT_WIDTH = "100";
    private static final String DEFAULT_VISIBLE = "Y";
    private static final String DEFAULT_REFRESH_BUTTON_VISIBLE = "Y";
    private final List<ArgumentGroupConfiguration> argumentGroupConfList;
    private final int width;
    private final StringExpression visibleExpression;
    private final boolean hasClientSideArguments;
    private boolean refreshButtonVisible;
    private final List<IncludeConfiguration> includeConfigurationList;

    public ArgumentBarConfiguration(PropertyList argumentBarProps, ConnectionInfo connectionInfo, PageContext pageContext) {
        if (argumentBarProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        if (connectionInfo == null) {
            throw new IllegalArgumentException("Connection info is null");
        }
        boolean hasClientSideArgs = false;
        this.argumentGroupConfList = new ArrayList<ArgumentGroupConfiguration>();
        PropertyListCollection argumentGroupCollection = argumentBarProps.getCollectionNotNull("argumentgroupcollection");
        for (int i = 0; i < argumentGroupCollection.size(); ++i) {
            PropertyList argumentGroupProps = argumentGroupCollection.getPropertyList(i);
            ArgumentGroupConfiguration argumentGroupConf = new ArgumentGroupConfiguration(argumentGroupProps, connectionInfo.getConnectionId(), this, pageContext);
            if (!hasClientSideArgs && argumentGroupConf.hasClientSideArguments()) {
                hasClientSideArgs = true;
            }
            this.argumentGroupConfList.add(argumentGroupConf);
        }
        this.includeConfigurationList = new ArrayList<IncludeConfiguration>();
        PropertyListCollection includeCollection = argumentBarProps.getCollectionNotNull("includecollection");
        for (int i = 0; i < includeCollection.size(); ++i) {
            PropertyList includeProps = includeCollection.getPropertyList(i);
            IncludeConfiguration includeConfiguration = new IncludeConfiguration(includeProps);
            this.includeConfigurationList.add(includeConfiguration);
        }
        this.hasClientSideArguments = hasClientSideArgs;
        this.visibleExpression = new StringExpression(argumentBarProps.getProperty("visible", "Y"));
        this.width = Integer.parseInt(argumentBarProps.getProperty("width", DEFAULT_WIDTH));
        this.refreshButtonVisible = argumentBarProps.getProperty("refreshbuttonvisible", "Y").toLowerCase().startsWith("y");
    }

    public StringExpression getVisible() {
        return this.visibleExpression;
    }

    public int getWidth() {
        return this.width;
    }

    public List<ArgumentGroupConfiguration> getArgumentGroupConfigurationList() {
        return this.argumentGroupConfList;
    }

    public List<IncludeConfiguration> getIncludeConfigurationList() {
        return this.includeConfigurationList;
    }

    public boolean hasClientSideArguments() {
        return this.hasClientSideArguments;
    }

    public boolean isRefreshButtonVisible() {
        return this.refreshButtonVisible;
    }
}

