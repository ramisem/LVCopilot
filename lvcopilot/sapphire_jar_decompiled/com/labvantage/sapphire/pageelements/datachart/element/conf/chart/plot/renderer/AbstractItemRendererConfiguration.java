/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.RendererConfiguration;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public abstract class AbstractItemRendererConfiguration
implements Serializable {
    private final String urlGeneratorId;
    private final RendererConfiguration.RendererType rendererType;
    private final List<String> annotationIdList;

    public AbstractItemRendererConfiguration(PropertyList itemRendererProps, RendererConfiguration.RendererType rendererType) {
        if (itemRendererProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.urlGeneratorId = itemRendererProps.getProperty("urlgeneratorid");
        this.rendererType = rendererType;
        this.annotationIdList = new ArrayList<String>();
        PropertyListCollection annotationIdCollection = itemRendererProps.getCollectionNotNull("annotationcollection");
        for (int i = 0; i < annotationIdCollection.size(); ++i) {
            PropertyList annotationIdProps = annotationIdCollection.getPropertyList(i);
            this.annotationIdList.add(annotationIdProps.getProperty("annotationid"));
        }
    }

    public AbstractItemRendererConfiguration(AbstractItemRendererConfiguration copy) {
        if (copy == null) {
            throw new IllegalArgumentException("Source is null");
        }
        this.urlGeneratorId = copy.urlGeneratorId;
        this.rendererType = copy.rendererType;
        this.annotationIdList = new ArrayList<String>(copy.getAnnotationIdList());
    }

    public AbstractItemRendererConfiguration(AbstractItemRendererConfiguration copy, AbstractItemRendererConfiguration override) {
        if (copy == null) {
            throw new IllegalArgumentException("Copy is null");
        }
        if (override == null) {
            throw new IllegalArgumentException("Override is null");
        }
        this.urlGeneratorId = !override.urlGeneratorId.isEmpty() ? override.urlGeneratorId : copy.urlGeneratorId;
        this.rendererType = override.rendererType;
        this.annotationIdList = new ArrayList<String>();
        this.annotationIdList.addAll(copy.getAnnotationIdList());
        this.annotationIdList.addAll(override.getAnnotationIdList());
    }

    public List<String> getAnnotationIdList() {
        return this.annotationIdList;
    }

    public String getUrlGeneratorId() {
        return this.urlGeneratorId;
    }

    public RendererConfiguration.RendererType getRendererType() {
        return this.rendererType;
    }
}

