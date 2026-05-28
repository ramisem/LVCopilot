/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.chart.toolbar;

import com.labvantage.sapphire.pageelements.datachart.chart.toolbar.Button;
import com.labvantage.sapphire.pageelements.datachart.chart.toolbar.ButtonSet;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.ButtonConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.ButtonSetConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.PlotConfigurationInterface;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.toolbar.ToolbarConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.ChartBindingMap;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class Toolbar
implements Serializable {
    private final List<ButtonSet> buttonSetList;
    private final boolean floating;
    private final boolean collapse;
    private final ToolbarConfiguration.HorizontalAlignment horizontalAlignment;

    public Toolbar(ToolbarConfiguration toolbarConf) {
        if (toolbarConf == null) {
            throw new IllegalArgumentException("Source configuration is null");
        }
        this.buttonSetList = new ArrayList<ButtonSet>();
        this.floating = toolbarConf.isFloating();
        this.collapse = toolbarConf.isCollapse();
        this.horizontalAlignment = toolbarConf.getHorizontalAlignment();
        List<com.labvantage.sapphire.pageelements.datachart.element.conf.chart.toolbar.ButtonSetConfiguration> buttonSetConfList = toolbarConf.getButtonSetConfigurationList();
        for (com.labvantage.sapphire.pageelements.datachart.element.conf.chart.toolbar.ButtonSetConfiguration buttonSetConf : buttonSetConfList) {
            String buttonSetId = buttonSetConf.getButtonSetId();
            if (buttonSetId.isEmpty()) continue;
            this.buttonSetList.add(new ButtonSet(buttonSetConf));
        }
    }

    public void setButtonVisibility(PlotConfigurationInterface plotConf, ChartBindingMap chartBindingMap) throws SapphireException {
        if (plotConf == null) {
            throw new IllegalArgumentException("Plot configuration is null");
        }
        com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.ToolbarConfiguration plotToolbarConf = plotConf.getToolbarConfiguration();
        List<ButtonSetConfiguration> buttonSetConfList = plotToolbarConf.getButtonSetConfigurationList();
        for (ButtonSetConfiguration buttonSetConf : buttonSetConfList) {
            String buttonSetId = buttonSetConf.getButtonSetId();
            if (buttonSetId.isEmpty()) continue;
            List<ButtonConfiguration> buttonConfList = buttonSetConf.getButtonConfigurationList();
            for (ButtonConfiguration buttonConf : buttonConfList) {
                String buttonId = buttonConf.getButtonId();
                if (buttonId.isEmpty()) continue;
                boolean found = false;
                block2: for (ButtonSet buttonSet : this.buttonSetList) {
                    if (!buttonSet.getButtonSetId().equals(buttonSetId)) continue;
                    for (Button button : buttonSet.getButtonList()) {
                        if (!button.getButtonId().equals(buttonId)) continue;
                        button.setVisible(buttonConf.isVisible().evaluate(chartBindingMap));
                        found = true;
                        continue block2;
                    }
                }
                if (found) continue;
                throw new IllegalArgumentException("Button " + buttonId + " not found in button set " + buttonSetId);
            }
        }
    }

    public PropertyList getProps(ChartBindingMap chartBindingMap, PropertyList argumentValueList) throws SapphireException {
        if (chartBindingMap == null) {
            throw new IllegalArgumentException("Binding map is null");
        }
        PropertyList toolbarProps = new PropertyList();
        PropertyListCollection buttonSetCollection = new PropertyListCollection();
        toolbarProps.setProperty("floating", this.isFloating() ? "Y" : "N");
        toolbarProps.setProperty("collapse", this.isCollapse() ? "Y" : "N");
        toolbarProps.setProperty("horizontalalignment", this.getHorizontalAlignment().getName());
        toolbarProps.setProperty("buttonsetcollection", buttonSetCollection);
        for (ButtonSet buttonSet : this.buttonSetList) {
            buttonSetCollection.add(buttonSet.getProps(chartBindingMap, argumentValueList));
        }
        return toolbarProps;
    }

    public boolean isFloating() {
        return this.floating;
    }

    public boolean isCollapse() {
        return this.collapse;
    }

    public ToolbarConfiguration.HorizontalAlignment getHorizontalAlignment() {
        return this.horizontalAlignment;
    }
}

