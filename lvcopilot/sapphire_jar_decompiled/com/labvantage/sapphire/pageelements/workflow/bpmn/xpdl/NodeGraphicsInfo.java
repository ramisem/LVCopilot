/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.xml.bind.annotation.XmlAccessType
 *  javax.xml.bind.annotation.XmlAccessorType
 *  javax.xml.bind.annotation.XmlAttribute
 *  javax.xml.bind.annotation.XmlElement
 *  javax.xml.bind.annotation.XmlType
 */
package com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl;

import com.labvantage.sapphire.pageelements.workflow.bpmn.Element;
import com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl.Coordinate;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(value=XmlAccessType.FIELD)
@XmlType(name="NodeGraphicsInfo", namespace="http://www.wfmc.org/2009/XPDL2.2")
public class NodeGraphicsInfo
extends Element {
    @XmlAttribute(name="FillColor")
    private String fillColor = "";
    @XmlAttribute(name="BorderColor")
    private String borderColor = "";
    @XmlAttribute(name="Width")
    private int width = 0;
    @XmlAttribute(name="Height")
    private int height = 0;
    @XmlAttribute(name="ToolId")
    private String toolId = "";
    @XmlElement(name="Coordinates", type=Coordinate.class, namespace="http://www.wfmc.org/2009/XPDL2.2")
    private Coordinate coordinates = new Coordinate();

    public String getFillColor() {
        return this.fillColor;
    }

    public void setFillColor(String fillColor) {
        this.fillColor = fillColor;
    }

    public String getBorderColor() {
        return this.borderColor;
    }

    public void setBorderColor(String borderColor) {
        this.borderColor = borderColor;
    }

    public int getWidth() {
        return this.width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return this.height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getToolId() {
        return this.toolId;
    }

    public void setToolId(String toolId) {
        this.toolId = toolId;
    }

    public Coordinate getCoordinates() {
        return this.coordinates;
    }

    public void setCoordinates(Coordinate coordinates) {
        this.coordinates = coordinates;
    }
}

