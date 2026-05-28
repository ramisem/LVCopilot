/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.xml;

import com.labvantage.sapphire.xml.SapphireSaxHandler;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Properties;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.helpers.DefaultHandler;
import sapphire.SapphireException;

public class SaxUtil {
    private SaxUtil() {
    }

    public static void parseFile(SapphireSaxHandler handler) throws SapphireException {
        SaxUtil.parse(handler.getXMLFile(), handler, "");
    }

    public static void parseFile(SapphireSaxHandler handler, String zipEntry) throws SapphireException {
        SaxUtil.parse(handler.getXMLFile(), handler, zipEntry);
    }

    public static void parseStream(SapphireSaxHandler handler, String zipEntry) throws SapphireException {
        SaxUtil.parse(handler.getInputStream(), handler, "");
    }

    public static void parseString(SapphireSaxHandler handler) throws SapphireException {
        SaxUtil.parseString(handler, "");
    }

    public static void parseString(SapphireSaxHandler handler, String encoding) throws SapphireException {
        if (encoding == null || encoding.length() == 0) {
            SaxUtil.parse(handler.getXMLString(), handler, "");
        } else {
            try {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
                SAXParser saxParser = factory.newSAXParser();
                saxParser.setProperty("http://xml.org/sax/properties/lexical-handler", handler);
                handler.setXMLReader(saxParser.getXMLReader());
                saxParser.parse((InputStream)new ByteArrayInputStream(handler.getXMLString().getBytes(encoding)), (DefaultHandler)handler);
            }
            catch (Exception e) {
                throw new SapphireException("Message could not be successfully parsed and processed. Reason:" + e.getMessage());
            }
        }
    }

    private static void parse(Object xml, SapphireSaxHandler handler, String zipEntry) throws SapphireException {
        block29: {
            try {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
                SAXParser saxParser = factory.newSAXParser();
                saxParser.setProperty("http://xml.org/sax/properties/lexical-handler", handler);
                handler.setXMLReader(saxParser.getXMLReader());
                if (xml instanceof FileInputStream) {
                    saxParser.parse((InputStream)((FileInputStream)xml), (DefaultHandler)handler);
                    break block29;
                }
                if (xml instanceof ByteArrayInputStream) {
                    saxParser.parse((InputStream)((ByteArrayInputStream)xml), (DefaultHandler)handler);
                    break block29;
                }
                if (xml instanceof String) {
                    StringReader reader = new StringReader((String)xml);
                    InputSource insource = new InputSource(reader);
                    saxParser.parse(insource, (DefaultHandler)handler);
                    break block29;
                }
                if (zipEntry != null && zipEntry.length() > 0) {
                    ZipFile zipFile = new ZipFile((File)xml);
                    ZipEntry entry = zipFile.getEntry(zipEntry);
                    if (entry != null) {
                        BufferedInputStream inputStream = new BufferedInputStream(zipFile.getInputStream(entry));
                        saxParser.parse((InputStream)inputStream, (DefaultHandler)handler);
                        break block29;
                    }
                    throw new SapphireException("IOException: Could not locate '" + zipEntry + "' file in zip.");
                }
                try (FileInputStream fis = new FileInputStream((File)xml);){
                    GZIPInputStream zipin = new GZIPInputStream(fis);
                    saxParser.parse((InputStream)zipin, (DefaultHandler)handler);
                }
                catch (IOException e) {
                    saxParser.parse((File)xml, (DefaultHandler)handler);
                }
            }
            catch (ParserConfigurationException e) {
                throw new SapphireException("ParserConfigurationException: " + e.getMessage(), e);
            }
            catch (SAXNotRecognizedException e) {
                throw new SapphireException("SAXNotRecognizedException: " + e.getMessage(), e);
            }
            catch (SAXNotSupportedException e) {
                throw new SapphireException("SAXNotSupportedException: " + e.getMessage(), e);
            }
            catch (SAXException e) {
                throw new SapphireException("SAXException: " + e.getMessage(), e);
            }
            catch (IOException e) {
                throw new SapphireException("IOException: " + e.getMessage(), e);
            }
            catch (Exception e) {
                throw new SapphireException("Exception: " + e.getMessage(), e);
            }
            finally {
                handler.reset();
            }
        }
    }

    public static String getAttributesText(Properties attributes) {
        StringBuffer text = new StringBuffer();
        for (String string : attributes.keySet()) {
            text.append(" ").append(string).append("=\"").append(attributes.getProperty(string)).append("\"");
        }
        return text.length() > 0 ? text.substring(1) : "";
    }
}

