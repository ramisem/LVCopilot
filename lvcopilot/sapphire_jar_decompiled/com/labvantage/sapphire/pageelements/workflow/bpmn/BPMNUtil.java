/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.xml.bind.JAXBContext
 *  javax.xml.bind.JAXBException
 *  javax.xml.bind.Marshaller
 *  javax.xml.bind.Unmarshaller
 */
package com.labvantage.sapphire.pageelements.workflow.bpmn;

import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.Definitions;
import com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl.Package;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.StreamReaderDelegate;
import sapphire.util.StringUtil;

public class BPMNUtil {
    public static Definitions readBPMN(File f) throws JAXBException, IOException {
        if (!f.exists()) {
            throw new IOException("File not found.");
        }
        JAXBContext jaxbContext = JAXBContext.newInstance((Class[])new Class[]{Definitions.class});
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        return (Definitions)jaxbUnmarshaller.unmarshal(f);
    }

    public static Package readXPDL(File f) throws JAXBException, IOException, XMLStreamException {
        if (!f.exists()) {
            throw new IOException("File not found.");
        }
        JAXBContext jaxbContext = JAXBContext.newInstance((Class[])new Class[]{Package.class});
        XMLInputFactory xif = XMLInputFactory.newInstance();
        xif.setProperty("javax.xml.stream.supportDTD", false);
        XMLStreamReader xsr = xif.createXMLStreamReader(new FileReader(f));
        xsr = new CaseInsensitiveReaderDelegate(xsr);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        return (Package)jaxbUnmarshaller.unmarshal(xsr);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Definitions readBPMN(String s) throws JAXBException, IOException {
        if (s.length() == 0) {
            throw new IOException("XML Not Provided.");
        }
        JAXBContext jaxbContext = JAXBContext.newInstance((Class[])new Class[]{Definitions.class});
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        try (StringReader reader = new StringReader(s);){
            Definitions definitions = (Definitions)jaxbUnmarshaller.unmarshal((Reader)reader);
            return definitions;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Package readXPDL(String s) throws JAXBException, IOException, XMLStreamException {
        if (s.length() == 0) {
            throw new IOException("XML Not Provided.");
        }
        JAXBContext jaxbContext = JAXBContext.newInstance((Class[])new Class[]{Package.class});
        StringReader reader = new StringReader(s);
        XMLInputFactory xif = XMLInputFactory.newInstance();
        xif.setProperty("javax.xml.stream.supportDTD", false);
        XMLStreamReader xsr = xif.createXMLStreamReader(reader);
        xsr = new CaseInsensitiveReaderDelegate(xsr);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        try {
            Package package_ = (Package)jaxbUnmarshaller.unmarshal(xsr);
            return package_;
        }
        finally {
            reader.close();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String writeXPDL(Package wpdlPackage) throws JAXBException, IOException {
        JAXBContext jaxbContext = JAXBContext.newInstance((Class[])new Class[]{Package.class});
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty("jaxb.formatted.output", (Object)true);
        try (StringWriter sw = new StringWriter();){
            jaxbMarshaller.marshal((Object)wpdlPackage, (Writer)sw);
            String string = StringUtil.replaceAll(StringUtil.replaceAll(StringUtil.replaceAll(StringUtil.replaceAll(sw.toString(), " id=", " Id="), " name=", " Name="), "x__x:", ""), ":x__x=", "=");
            return string;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String writeBPMN(Definitions bpmnDefinitions) throws JAXBException, IOException {
        JAXBContext jaxbContext = JAXBContext.newInstance((Class[])new Class[]{Definitions.class});
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty("jaxb.formatted.output", (Object)true);
        try (StringWriter sw = new StringWriter();){
            jaxbMarshaller.marshal((Object)bpmnDefinitions, (Writer)sw);
            String out = sw.toString();
            out = StringUtil.replaceAll(StringUtil.replaceAll(out, "<ns2:", "<dc:"), "</ns2:", "</dc:");
            out = StringUtil.replaceAll(StringUtil.replaceAll(out, "<ns3:", "<bpmndi:"), "</ns3:", "</bpmndi:");
            out = StringUtil.replaceAll(out, ":ns2=", ":dc=");
            String string = out = StringUtil.replaceAll(out, ":ns3=", ":bpmndi=");
            return string;
        }
    }

    private static class CaseInsensitiveReaderDelegate
    extends StreamReaderDelegate {
        public CaseInsensitiveReaderDelegate(XMLStreamReader xsr) {
            super(xsr);
        }

        @Override
        public String getAttributeLocalName(int index) {
            if (super.getAttributeLocalName(index).equalsIgnoreCase("name") || super.getAttributeLocalName(index).equalsIgnoreCase("id")) {
                return super.getAttributeLocalName(index).toLowerCase().intern();
            }
            return super.getAttributeLocalName(index).intern();
        }
    }
}

