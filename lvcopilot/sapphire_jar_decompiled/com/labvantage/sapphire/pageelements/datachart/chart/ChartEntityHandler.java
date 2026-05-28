/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jfree.chart.JFreeChart
 *  org.jfree.chart.entity.ChartEntity
 *  org.jfree.chart.entity.EntityCollection
 *  org.jfree.chart.entity.PlotEntity
 *  org.jfree.chart.entity.TitleEntity
 */
package com.labvantage.sapphire.pageelements.datachart.chart;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.ChartConfiguration;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.PlotEntity;
import org.jfree.chart.entity.TitleEntity;

public final class ChartEntityHandler {
    private ChartEntityHandler() {
    }

    public static void setURL(String dataChartJsObject, EntityCollection entityCollection, JFreeChart jFreeChart, ChartConfiguration chartConf) {
        if (entityCollection == null) {
            throw new IllegalArgumentException("Entity collection is null");
        }
        if (jFreeChart == null) {
            throw new IllegalArgumentException("Chart is null");
        }
        if (chartConf == null) {
            throw new IllegalArgumentException("Chart configuration is null");
        }
        int entityCount = entityCollection.getEntityCount();
        for (int i = 0; i < entityCount; ++i) {
            ChartEntityHandler.setEntityURL(dataChartJsObject, entityCollection.getEntity(i), jFreeChart, chartConf);
        }
    }

    private static String setEntityURL(String dataChartJsObject, ChartEntity entity, JFreeChart jFreeChart, ChartConfiguration chartConf) {
        String urlText = "";
        if (entity instanceof TitleEntity) {
            ChartEntityHandler.handleTitleEntity((TitleEntity)entity, jFreeChart, chartConf);
        } else if (entity instanceof PlotEntity) {
            ChartEntityHandler.handlePlotEntity(dataChartJsObject, (PlotEntity)entity, jFreeChart, chartConf);
        }
        return urlText;
    }

    private static void handlePlotEntity(String dataChartJsObject, PlotEntity plotEntity, JFreeChart jFreeChart, ChartConfiguration chartConf) {
        StringBuilder urlText = new StringBuilder();
        StringBuilder onMouseDown = new StringBuilder();
        onMouseDown.append(dataChartJsObject).append(".zoomPlot.startZoom();");
        urlText.append("onmousedown=\"").append((CharSequence)onMouseDown).append("\"");
    }

    private static void handleTitleEntity(TitleEntity titleEntity, JFreeChart jFreeChart, ChartConfiguration chartConf) {
    }
}

