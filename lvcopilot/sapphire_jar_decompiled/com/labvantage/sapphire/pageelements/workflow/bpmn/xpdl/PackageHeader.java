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
@XmlType(name="PackageHeader", namespace="http://www.wfmc.org/2009/XPDL2.2")
public class PackageHeader
extends Header {
    @XmlElement(name="XPDLVersion", namespace="http://www.wfmc.org/2009/XPDL2.2")
    private String xpdlVersion = "";
    @XmlElement(name="Vendor", namespace="http://www.wfmc.org/2009/XPDL2.2")
    private String vendor = "";
    @XmlElement(name="ModificationDate", namespace="http://www.wfmc.org/2009/XPDL2.2")
    private String modificationDate = "";
    @XmlElement(name="Documentation", namespace="http://www.wfmc.org/2009/XPDL2.2")
    private String documentation = "";

    public String getXpdlVersion() {
        return this.xpdlVersion;
    }

    public void setXpdlVersion(String xpdlVersion) {
        this.xpdlVersion = xpdlVersion;
    }

    public String getVendor() {
        return this.vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getModificationDate() {
        return this.modificationDate;
    }

    public void setModificationDate(String modificationDate) {
        this.modificationDate = modificationDate;
    }

    public String getDocumentation() {
        return this.documentation;
    }

    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }
}

