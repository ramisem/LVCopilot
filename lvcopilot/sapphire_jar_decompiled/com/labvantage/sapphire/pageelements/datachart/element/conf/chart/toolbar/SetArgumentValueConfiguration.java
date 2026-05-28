/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.toolbar;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.toolbar.ArgumentDetailsConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.toolbar.StandardOperationConfiguration;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public final class SetArgumentValueConfiguration
implements Serializable {
    private final StandardOperationConfiguration parent;
    private final List<ArgumentDetailsConfiguration> argumentDetailsConfList;

    public SetArgumentValueConfiguration(PropertyList setArgumentValueProps, StandardOperationConfiguration parent) {
        if (setArgumentValueProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        this.parent = parent;
        this.argumentDetailsConfList = new ArrayList<ArgumentDetailsConfiguration>();
        PropertyListCollection buttonCollection = setArgumentValueProps.getCollectionNotNull("argumentdetailscollection");
        for (int i = 0; i < buttonCollection.size(); ++i) {
            PropertyList argumentDetailsProps = buttonCollection.getPropertyList(i);
            this.argumentDetailsConfList.add(new ArgumentDetailsConfiguration(argumentDetailsProps, this));
        }
    }

    public SetArgumentValueConfiguration(SetArgumentValueConfiguration copy, StandardOperationConfiguration parent) {
        this.parent = parent;
        this.argumentDetailsConfList = new ArrayList<ArgumentDetailsConfiguration>();
        for (ArgumentDetailsConfiguration argumentDetailsConf : copy.argumentDetailsConfList) {
            this.argumentDetailsConfList.add(new ArgumentDetailsConfiguration(argumentDetailsConf, this));
        }
    }

    public StandardOperationConfiguration getParent() {
        return this.parent;
    }

    public List<ArgumentDetailsConfiguration> getArgumentDetailsConfigurationList() {
        return this.argumentDetailsConfList;
    }
}

