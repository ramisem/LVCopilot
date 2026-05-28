/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jfree.chart.ChartRenderingInfo
 */
package com.labvantage.sapphire.pageelements.datachart.session;

import com.labvantage.sapphire.pageelements.datachart.argbar.ArgumentBar;
import com.labvantage.sapphire.pageelements.datachart.chart.Chart;
import com.labvantage.sapphire.pageelements.datachart.data.Data;
import java.io.Serializable;
import org.jfree.chart.ChartRenderingInfo;
import sapphire.xml.PropertyList;

public final class CachedDataChart
implements Serializable {
    private final PropertyList dataChartProps;
    private final ArgumentBar argumentBar;
    private final String imageMap;
    private final String fileName;
    private final Data data;
    private final Chart chart;
    private final ChartRenderingInfo chartRenderingInfo;
    private final PropertyList requestParams;
    private final PropertyList elements;
    private final String requestId;
    private final String chartId;

    private CachedDataChart(Builder builder) {
        this.chartId = builder.chartId;
        this.dataChartProps = builder.dataChartProps;
        this.chart = builder.chart;
        this.argumentBar = builder.argumentBar;
        this.imageMap = builder.imageMap;
        this.fileName = builder.fileName;
        this.data = builder.data;
        this.chartRenderingInfo = builder.chartRenderingInfo;
        this.requestParams = builder.requestParams;
        this.elements = builder.elements;
        this.requestId = builder.requestId;
    }

    public ChartRenderingInfo getChartRenderingInfo() {
        if (this.chartRenderingInfo == null) {
            throw new IllegalStateException("Chart rendering info not stored yet");
        }
        return this.chartRenderingInfo;
    }

    public ArgumentBar getArgumentBar() {
        if (this.argumentBar == null) {
            throw new IllegalStateException("Argument bar not stored yet");
        }
        return this.argumentBar;
    }

    public Chart getChart() {
        if (this.chart == null) {
            throw new IllegalStateException("Chart not stored yet");
        }
        return this.chart;
    }

    public PropertyList getDataChartProps() {
        if (this.dataChartProps == null) {
            throw new IllegalStateException("Data chart props not stored yet");
        }
        return this.dataChartProps;
    }

    public PropertyList getRequestParams() {
        if (this.requestParams == null) {
            throw new IllegalStateException("Request params not stored yet");
        }
        return this.requestParams;
    }

    public PropertyList getElements() {
        if (this.elements == null) {
            throw new IllegalStateException("Elements not stored yet");
        }
        return this.elements;
    }

    public String getRequestId() {
        if (this.requestId == null) {
            throw new IllegalStateException("Request ID not stored yet");
        }
        return this.requestId;
    }

    public Data getData() {
        if (this.data == null) {
            throw new IllegalStateException("Data not stored yet");
        }
        return this.data;
    }

    public String getImageMap() {
        if (this.imageMap == null) {
            throw new IllegalStateException("Image map not stored yet");
        }
        return this.imageMap;
    }

    public String getFileName() {
        if (this.fileName == null) {
            throw new IllegalStateException("File name not stored yet");
        }
        return this.fileName;
    }

    public String getChartId() {
        return this.chartId;
    }

    public static class Builder {
        private final PropertyList dataChartProps;
        private final PropertyList requestParams;
        private final PropertyList elements;
        private final Chart chart;
        private final ArgumentBar argumentBar;
        private final Data data;
        private final String requestId;
        private final String chartId;
        private String imageMap = null;
        private String fileName = null;
        private ChartRenderingInfo chartRenderingInfo = null;

        public Builder(String chartId, PropertyList dataChartProps, Chart chart, ArgumentBar argumentBar, Data data, PropertyList requestParams, PropertyList elements, String requestId) {
            if (dataChartProps == null) {
                throw new IllegalArgumentException("Data chart props is null");
            }
            if (chart == null) {
                throw new IllegalArgumentException("Chart is null");
            }
            if (argumentBar == null) {
                throw new IllegalArgumentException("Argument bar is null");
            }
            if (data == null) {
                throw new IllegalArgumentException("Data is null");
            }
            if (requestParams == null) {
                throw new IllegalArgumentException("Request params is null");
            }
            if (elements == null) {
                throw new IllegalArgumentException("Elements is null");
            }
            if (requestId == null) {
                throw new IllegalArgumentException("Request ID is null");
            }
            this.chartId = chartId;
            this.dataChartProps = dataChartProps;
            this.chart = chart;
            this.argumentBar = argumentBar;
            this.data = data;
            this.requestParams = requestParams;
            this.elements = elements;
            this.requestId = requestId;
        }

        public Builder chartRenderingInfo(ChartRenderingInfo val) {
            if (val == null) {
                throw new IllegalArgumentException("Chart rendering info is null");
            }
            this.chartRenderingInfo = val;
            return this;
        }

        public Builder imageMap(String val) {
            if (val == null) {
                throw new IllegalArgumentException("Image map is null");
            }
            this.imageMap = val;
            return this;
        }

        public Builder fileName(String val) {
            if (val == null) {
                throw new IllegalArgumentException("File name is null");
            }
            this.fileName = val;
            return this;
        }

        public CachedDataChart build() {
            return new CachedDataChart(this);
        }
    }
}

