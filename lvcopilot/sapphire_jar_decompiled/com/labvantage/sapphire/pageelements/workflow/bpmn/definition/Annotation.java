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
package com.labvantage.sapphire.pageelements.workflow.bpmn.definition;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(value=XmlAccessType.FIELD)
@XmlType(name="textAnnotation", namespace="http://www.omg.org/spec/BPMN/20100524/MODEL")
public class Annotation {
    @XmlAttribute
    private String textFormat;
    @XmlAttribute
    private String id = "";
    @XmlElement(name="text", namespace="http://www.omg.org/spec/BPMN/20100524/MODEL")
    private String text = "";

    public String getTextFormat() {
        return this.textFormat;
    }

    public void setTextFormat(String textFormat) {
        this.textFormat = textFormat;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }
}

