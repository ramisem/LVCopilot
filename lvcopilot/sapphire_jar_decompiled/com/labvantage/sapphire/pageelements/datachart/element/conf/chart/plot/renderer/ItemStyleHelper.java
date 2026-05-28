/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer;

import com.labvantage.sapphire.pageelements.datachart.data.TraceableSeriesGroup;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.ComponentPlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.ItemStyleConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.RendererConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.SeriesStyleConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.seriesgroup.SeriesConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.seriesgroup.SeriesGroupConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.ChartBindingMap;
import java.util.ArrayList;
import java.util.List;
import sapphire.SapphireException;

public final class ItemStyleHelper {
    private ItemStyleHelper() {
    }

    public static ItemStyleConfiguration storeActualItemStyleConfiguration(TraceableSeriesGroup traceableSeriesGroup, SeriesGroupConfiguration seriesGroupConf, RendererConfiguration rendererConf, ChartBindingMap chartBindingMap, int seriesIndex, int itemIndex) throws SapphireException {
        String plotId = traceableSeriesGroup.getPlotId();
        String seriesGroupId = traceableSeriesGroup.getSeriesGroupId();
        String seriesId = traceableSeriesGroup.getSeriesId(seriesIndex);
        chartBindingMap.setActiveItem(plotId, seriesGroupId, seriesId, itemIndex);
        SeriesConfiguration seriesConf = seriesGroupConf.getSeriesConfiguration(seriesId);
        SeriesStyleConfiguration seriesStyleConf = rendererConf.getSeriesStyleConfiguration(seriesConf.getSeriesStyleId());
        ItemStyleConfiguration storedStyleConf = null;
        List<com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.seriesgroup.ItemStyleConfiguration> itemConfigurationList = seriesConf.getItemConfiguration().getItemStyleConfigurationList();
        ArrayList<String> actualItemStyleIdList = new ArrayList<String>();
        actualItemStyleIdList.add(seriesStyleConf.getSeriesStyleId());
        for (com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.seriesgroup.ItemStyleConfiguration seriesItemStyleConf : itemConfigurationList) {
            String itemStyleId = seriesItemStyleConf.getItemStyleId();
            boolean enable = seriesItemStyleConf.getEnable().evaluate(chartBindingMap);
            if (!enable || itemStyleId.isEmpty()) continue;
            actualItemStyleIdList.add(itemStyleId);
            ItemStyleConfiguration itemStyleConf = seriesStyleConf.getItemStyleConfiguration(itemStyleId);
            if (storedStyleConf != null) {
                storedStyleConf = new ItemStyleConfiguration(storedStyleConf, itemStyleConf, null);
                continue;
            }
            storedStyleConf = itemStyleConf;
        }
        if (storedStyleConf != null) {
            seriesStyleConf.addActualItemStyleConfiguration(actualItemStyleIdList, storedStyleConf, seriesGroupId, seriesId, itemIndex);
        }
        return storedStyleConf;
    }

    public static ItemStyleConfiguration getActualItemStyleConfiguration(TraceableSeriesGroup traceableSeriesGroup, SeriesGroupConfiguration seriesGroupConf, RendererConfiguration rendererConf, int seriesIndex, int itemIndex) {
        String seriesGroupId = traceableSeriesGroup.getSeriesGroupId();
        String seriesId = traceableSeriesGroup.getSeriesId(seriesIndex);
        SeriesConfiguration seriesConf = seriesGroupConf.getSeriesConfiguration(seriesId);
        SeriesStyleConfiguration seriesStyleConf = rendererConf.getSeriesStyleConfiguration(seriesConf.getSeriesStyleId());
        return seriesStyleConf.getItemStyleConfiguration(seriesGroupId, seriesId, itemIndex);
    }

    public static void resetActualItemStyleConfigurations(ComponentPlotConfiguration componentPlotConf) {
        for (RendererConfiguration rendererConf : componentPlotConf.getRendererConfList()) {
            if (rendererConf.getRendererId().isEmpty()) continue;
            for (SeriesStyleConfiguration seriesStyleConf : rendererConf.getSeriesStyleConfList()) {
                seriesStyleConf.resetActualItemStyleConfigurations();
            }
        }
    }
}

