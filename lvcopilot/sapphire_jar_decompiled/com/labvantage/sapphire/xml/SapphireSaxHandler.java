/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.JspWriter
 */
package com.labvantage.sapphire.xml;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.Timer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;
import javax.servlet.jsp.JspWriter;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

public class SapphireSaxHandler
extends DefaultHandler
implements LexicalHandler {
    protected Timer timer = new Timer();
    private long _jspScrollCounter;
    protected DBUtil _dbu;
    protected PrintStream _out = System.out;
    protected File _xmlFile;
    protected String _xmlString;
    protected JspWriter _jspOut;
    protected boolean verbose = false;
    protected String connectionid;
    protected File rakFile;
    private XMLReader xmlReader;
    private ContentHandler parentHandler;
    protected InputStream inputStream = null;

    public void setXMLReader(XMLReader xmlReader) {
        this.xmlReader = xmlReader;
    }

    public XMLReader getXMLReader() {
        return this.xmlReader;
    }

    public ContentHandler getParentHandler() {
        return this.parentHandler;
    }

    public void setParentHandler(ContentHandler parentHandler) {
        this.parentHandler = parentHandler;
    }

    public String getConnectionid() {
        return this.connectionid;
    }

    public void setConnectionid(String connectionid) {
        this.connectionid = connectionid;
    }

    public File getRakFile() {
        return this.rakFile;
    }

    public void setRakFile(File rakFile) {
        this.rakFile = rakFile;
    }

    public InputStream getInputStream() {
        return this.inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public void setXMLFile(File file) {
        this._xmlFile = file;
    }

    public File getXMLFile() {
        return this._xmlFile;
    }

    public void setXMLString(String xml) {
        this._xmlString = xml;
    }

    public String getXMLString() {
        return this._xmlString;
    }

    public PrintStream getPrintStream() {
        return this._out;
    }

    public void setPrintStream(PrintStream printStream) {
        this._out = printStream;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public void setLogFile(File file) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            this._out = new PrintStream(fos);
        }
        catch (IOException ioe) {
            this.println("Failed to set log file. Exception: " + ioe.getMessage());
        }
    }

    public void setJspOut(JspWriter out) {
        this._jspOut = out;
        this._jspScrollCounter = System.currentTimeMillis();
    }

    public JspWriter getJspOut() {
        return this._jspOut;
    }

    public void setDBUtil(DBUtil dbu) {
        this._dbu = dbu;
    }

    public DBUtil getDBUtil() {
        return this._dbu;
    }

    public void log(String output) {
        if (this._out != null) {
            this._out.println(output);
        }
    }

    public void println(String output) {
        if (this._out != null) {
            this._out.println(output);
            if (this._jspOut != null) {
                try {
                    this._jspOut.println(output + "<br id=\"" + this._jspScrollCounter + "\"/><script>document.getElementById( \"" + this._jspScrollCounter + "\" ).scrollIntoView( true )</script>");
                    this._jspOut.flush();
                    ++this._jspScrollCounter;
                }
                catch (IOException iOException) {
                    // empty catch block
                }
            }
        }
    }

    public void print(String output) {
        if (this._out != null) {
            this._out.print(output);
            if (this._jspOut != null) {
                try {
                    this._jspOut.println(output + "<span id=\"" + this._jspScrollCounter + "\"/><script>document.getElementById( \"" + this._jspScrollCounter + "\" ).scrollIntoView( true )</script>");
                    this._jspOut.flush();
                    ++this._jspScrollCounter;
                }
                catch (IOException iOException) {
                    // empty catch block
                }
            }
        }
    }

    protected Properties getAttributes(Attributes attributes) {
        Properties attr = new Properties();
        if (attributes != null) {
            for (int i = 0; i < attributes.getLength(); ++i) {
                attr.setProperty(attributes.getQName(i), attributes.getValue(i));
            }
        }
        return attr;
    }

    protected String getCharacters(char[] ch, int start, int length) {
        StringBuffer sb = new StringBuffer(length);
        for (int i = 0; i < length; ++i) {
            sb.append(ch[start + i]);
        }
        return sb.toString();
    }

    public void reset() {
        if (this._out != null && this._out != System.out) {
            this._out.close();
        }
    }

    @Override
    public void startCDATA() throws SAXException {
    }

    @Override
    public void endCDATA() throws SAXException {
    }

    @Override
    public void startDTD(String name, String publicId, String systemId) throws SAXException {
    }

    @Override
    public void endDTD() throws SAXException {
    }

    @Override
    public void startEntity(String name) throws SAXException {
    }

    @Override
    public void endEntity(String name) throws SAXException {
    }

    @Override
    public void comment(char[] ch, int start, int length) throws SAXException {
    }

    protected String getAttributesText(Properties attributes) {
        StringBuffer text = new StringBuffer();
        for (String string : attributes.keySet()) {
            text.append(" ").append(string).append("=\"").append(attributes.getProperty(string)).append("\"");
        }
        return text.length() > 0 ? text.substring(1) : "";
    }

    protected void transferHandler(SapphireSaxHandler targetHandler) throws SAXException {
        XMLReader parser = this.getXMLReader();
        parser.setContentHandler(targetHandler);
        targetHandler.setParentHandler(this);
        targetHandler.setXMLReader(parser);
    }

    protected void returnHandler() {
        XMLReader parser;
        if (this.getParentHandler() != null && (parser = this.getXMLReader()) != null) {
            parser.setContentHandler(this.getParentHandler());
        }
    }
}

