/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.chart.toolbar;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.toolbar.ArgumentDetailsConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.toolbar.OperationConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.toolbar.ShowSDINotesConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.toolbar.StandardOperationConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.ChartBindingMap;
import com.labvantage.sapphire.pageelements.datachart.groovy.StringExpression;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class Operation
implements Serializable {
    private final OperationConfiguration operationConf;

    public Operation(OperationConfiguration operationConf) {
        if (operationConf == null) {
            throw new IllegalArgumentException("Source configuration is null");
        }
        this.operationConf = operationConf;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public PropertyList getProps(ChartBindingMap chartBindingMap) throws SapphireException {
        if (chartBindingMap == null) {
            throw new IllegalArgumentException("Binding map is null");
        }
        PropertyList operationProps = new PropertyList();
        OperationConfiguration.OperationType operationType = this.operationConf.getOperationType();
        if (operationType == OperationConfiguration.OperationType.STANDARD) {
            StandardOperationConfiguration standardOperationConf = this.operationConf.getStandardOperationConfiguration();
            StandardOperationConfiguration.StandardOperationType standardOperationType = standardOperationConf.getStandardOperationType();
            if (standardOperationType == StandardOperationConfiguration.StandardOperationType.EXPORT_DATA) {
                operationProps.setProperty("javascript", standardOperationConf.getExportDataConfiguration().getJavaScript(chartBindingMap));
                return operationProps;
            }
            if (standardOperationType == StandardOperationConfiguration.StandardOperationType.REFRESH) {
                operationProps.setProperty("javascript", chartBindingMap.getDataChartJsObject() + ".refresh();");
                return operationProps;
            }
            if (standardOperationType == StandardOperationConfiguration.StandardOperationType.SET_ARGUMENT_VALUE) {
                List<ArgumentDetailsConfiguration> argumentDetailsConfList = standardOperationConf.getSetArgumentValueConfiguration().getArgumentDetailsConfigurationList();
                PropertyList setArgumentValueProps = new PropertyList();
                PropertyListCollection argumentDetailsCollection = new PropertyListCollection();
                for (ArgumentDetailsConfiguration argumentDetailsConfiguration : argumentDetailsConfList) {
                    PropertyList argumentDetailsProps = new PropertyList();
                    argumentDetailsProps.setProperty("argumentid", argumentDetailsConfiguration.getArgumentId());
                    try {
                        argumentDetailsProps.setProperty("argumentvalue", URLEncoder.encode(argumentDetailsConfiguration.getArgumentValue().evaluate(chartBindingMap), "UTF-8"));
                    }
                    catch (UnsupportedEncodingException e) {
                        throw new SapphireException("Cannot encode argument value", e);
                    }
                    argumentDetailsCollection.add(argumentDetailsProps);
                }
                setArgumentValueProps.setProperty("argumentdetailscollection", argumentDetailsCollection);
                operationProps.setProperty("javascript", chartBindingMap.getDataChartJsObject() + ".standardOperation.setArgumentValue('" + setArgumentValueProps.toJSONString(false) + "');");
                return operationProps;
            }
            if (standardOperationType == StandardOperationConfiguration.StandardOperationType.OPEN_URL) {
                String url = standardOperationConf.getOpenURLConfiguration().getURL().evaluate(chartBindingMap);
                operationProps.setProperty("javascript", "window.open('" + url + "', 'OpenURL', 'resizable=yes');");
                return operationProps;
            } else {
                if (standardOperationType != StandardOperationConfiguration.StandardOperationType.SHOW_SDI_NOTES) throw new IllegalArgumentException("Unknown standard operation: " + (Object)((Object)standardOperationType));
                ShowSDINotesConfiguration showSDINotesConf = standardOperationConf.getShowSDINotesConfiguration();
                String sdcId = new StringExpression("$G{seriesgroup.getSeriesDataSet(seriesid).getString(itemindex, \"" + showSDINotesConf.getSdcIdColumn() + "\")" + "}").evaluate(chartBindingMap);
                String keyId1 = new StringExpression("$G{seriesgroup.getSeriesDataSet(seriesid).getString(itemindex, \"" + showSDINotesConf.getKeyId1Column() + "\")" + "}").evaluate(chartBindingMap);
                String keyId2 = new StringExpression("$G{seriesgroup.getSeriesDataSet(seriesid).getString(itemindex, \"" + showSDINotesConf.getKeyId2Column() + "\")" + "}").evaluate(chartBindingMap);
                String keyId3 = new StringExpression("$G{seriesgroup.getSeriesDataSet(seriesid).getString(itemindex, \"" + showSDINotesConf.getKeyId3Column() + "\")" + "}").evaluate(chartBindingMap);
                String context = showSDINotesConf.getContext().evaluate(chartBindingMap);
                boolean highlightContext = showSDINotesConf.isHighlightContext();
                String javaScript = chartBindingMap.getDataChartJsObject() + ".standardOperation.showSDINotes('" + sdcId + "', '" + keyId1 + "', '" + keyId2 + "', '" + keyId3 + "', '" + context + "', " + highlightContext + ");";
                operationProps.setProperty("javascript", javaScript);
            }
            return operationProps;
        }
        operationProps.setProperty("javascript", this.operationConf.getJavaScript().evaluate(chartBindingMap));
        return operationProps;
    }
}

