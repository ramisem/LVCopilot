/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.xml.bind.annotation.XmlAccessType
 *  javax.xml.bind.annotation.XmlAccessorType
 *  javax.xml.bind.annotation.XmlElement
 *  javax.xml.bind.annotation.XmlType
 */
package com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl;

import com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl.Header;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(value=XmlAccessType.FIELD)
@XmlType(name="RedefinableHeader", namespace="http://www.wfmc.org/2009/XPDL2.2")
public class RedefinableHeader
extends Header {
    @XmlElement(name="Author", namespace="http://www.wfmc.org/2009/XPDL2.2")
    private String author = "";
    @XmlElement(name="Version", namespace="http://www.wfmc.org/2009/XPDL2.2")
    private String version = "";
    @XmlElement(name="Countrykey", namespace="http://www.wfmc.org/2009/XPDL2.2")
    private String countrykey = "";

    public String getAuthor() {
        return this.author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCountrykey() {
        return this.countrykey;
    }

    public void setCountrykey(String countrykey) {
        this.countrykey = countrykey;
    }
}

