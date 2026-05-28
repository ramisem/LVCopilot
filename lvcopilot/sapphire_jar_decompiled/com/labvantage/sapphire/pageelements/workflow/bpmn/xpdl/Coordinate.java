/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.xml.bind.annotation.XmlAccessType
 *  javax.xml.bind.annotation.XmlAccessorType
 *  javax.xml.bind.annotation.XmlAttribute
 *  javax.xml.bind.annotation.XmlType
 */
package com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl;

import com.labvantage.sapphire.pageelements.workflow.bpmn.Element;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(value=XmlAccessType.FIELD)
@XmlType(name="Coordinates", namespace="http://www.wfmc.org/2009/XPDL2.2")
public class Coordinate
extends Element {
    @XmlAttribute(name="XCoordinate")
    private int x = 0;
    @XmlAttribute(name="YCoordinate")
    private int y = 0;

    public int getX() {
        return this.x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return this.y;
    }

    public void setY(int y) {
        this.y = y;
    }
}

