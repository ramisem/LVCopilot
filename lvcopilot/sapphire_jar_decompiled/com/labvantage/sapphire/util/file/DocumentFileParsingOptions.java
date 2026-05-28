/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.file;

import com.labvantage.sapphire.DataSetUtil;
import java.util.ArrayList;
import java.util.List;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class DocumentFileParsingOptions {
    private int pageFrom = -1;
    private int pageTo = -1;
    private int tableToExtract = -1;
    private String pdfName = "pdf";
    private String xmlName = "xml";
    private String textName = "text";
    private String tableName = "table";
    private String imageName = "image";
    private boolean clearWorkingDirectory = true;
    private boolean extractText = true;
    private boolean extractTables = false;
    private boolean extractAsXML = false;
    private boolean generatePDFForPS = false;
    private boolean extractImages = false;
    private boolean backwardsCompatiableTableColumns = false;
    private String[] tableColumns = new String[0];
    private String endTableText = "";
    private ArrayList<DataSetUtil.MergeColumn> mergeColumns = new ArrayList();
    private String tableAttachmentClass = "";
    private String pdfAttachmentClass = "";
    private String xmlAttachmentClass = "";
    private String textAttachmentClass = "";
    private String imageAttachmentClass = "";

    public DocumentFileParsingOptions() {
    }

    public void setTableAttachmentClass(String tableAttachmentClass) {
        this.tableAttachmentClass = tableAttachmentClass;
    }

    public void setPdfAttachmentClass(String pdfAttachmentClass) {
        this.pdfAttachmentClass = pdfAttachmentClass;
    }

    public void setXmlAttachmentClass(String xmlAttachmentClass) {
        this.xmlAttachmentClass = xmlAttachmentClass;
    }

    public void setImageAttachmentClass(String imageAttachmentClass) {
        this.imageAttachmentClass = imageAttachmentClass;
    }

    public void setTextAttachmentClass(String textAttachmentClass) {
        this.textAttachmentClass = textAttachmentClass;
    }

    public String getTableAttachmentClass() {
        return this.tableAttachmentClass;
    }

    public String getPdfAttachmentClass() {
        return this.pdfAttachmentClass;
    }

    public String getXmlAttachmentClass() {
        return this.xmlAttachmentClass;
    }

    public String getImageAttachmentClass() {
        return this.imageAttachmentClass;
    }

    public String getTextAttachmentClass() {
        return this.textAttachmentClass;
    }

    public void setTableToExtract(int p) {
        this.tableToExtract = p;
    }

    public int getTableToExtract() {
        return this.tableToExtract;
    }

    public void setPageFrom(int p) {
        this.pageFrom = p;
    }

    public void setPageTo(int p) {
        this.pageTo = p;
    }

    public int getPageFrom() {
        return this.pageFrom;
    }

    public int getPageTo() {
        return this.pageTo;
    }

    public DocumentFileParsingOptions(PropertyList properties) {
        this.setClearWorkingFolder(properties.getProperty("clearworkingfolder", "Y").equalsIgnoreCase("Y"));
        this.setExtractImages(properties.getProperty("extractimages", "N").equalsIgnoreCase("Y"));
        this.setExtractAsXML(properties.getProperty("extractasxml", "N").equalsIgnoreCase("Y"));
        this.setExtractTables(properties.getProperty("extracttables", "N").equalsIgnoreCase("Y"));
        this.setExtractText(properties.getProperty("extracttext", "Y").equalsIgnoreCase("Y"));
        this.setGeneratePDFForPS(properties.getProperty("generatepdfforps", "N").equalsIgnoreCase("Y"));
        this.setTableColumns(properties.getProperty("tablecolumns", ""));
        this.setMergeColumns(properties.getProperty("mergecolumns", ""));
        this.setEndTableText(properties.getProperty("endtablete", ""));
        this.pdfName = properties.getProperty("pdffilenameprefix", "pdf");
        this.xmlName = properties.getProperty("xmlfilenameprefix", "xml");
        this.textName = properties.getProperty("textfilenameprefix", "text");
        this.tableName = properties.getProperty("tablefilenameprefix", "table");
        this.imageName = properties.getProperty("imagefilenameprefix", "image");
        this.setTableAttachmentClass(properties.getProperty("tableattachmentclass", ""));
        this.setXmlAttachmentClass(properties.getProperty("xmlattachmentclass", ""));
        this.setImageAttachmentClass(properties.getProperty("imageattachmentclass", ""));
        this.setTextAttachmentClass(properties.getProperty("textattachmentclass", ""));
        this.setPdfAttachmentClass(properties.getProperty("pdfattachmentclass", ""));
        try {
            this.setPageTo(Integer.parseInt(properties.getProperty("pageto", "-1")));
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            this.setPageFrom(Integer.parseInt(properties.getProperty("pagefrom", "-1")));
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            this.setTableToExtract(Integer.parseInt(properties.getProperty("tabletoextract", "-1")));
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public PropertyList toPropertyList() {
        int i;
        PropertyList out = new PropertyList();
        out.setProperty("clearworkingfolder", this.getClearWorkingFolder() ? "Y" : "N");
        out.setProperty("extractimages", this.getExtractImages() ? "Y" : "N");
        out.setProperty("extractasxml", this.getExtractAsXML() ? "Y" : "N");
        out.setProperty("extracttables", this.getExtractTables() ? "Y" : "N");
        out.setProperty("extracttext", this.getExtractText() ? "Y" : "N");
        out.setProperty("generatepdfforps", this.getGeneratePDFForPS() ? "Y" : "N");
        StringBuffer temp = new StringBuffer();
        for (i = 0; i < this.getTableColumns().length; ++i) {
            if (temp.length() > 0) {
                temp.append(";");
            }
            temp.append(this.getTableColumns()[i]);
        }
        out.setProperty("tablecolumns", temp.toString());
        temp = new StringBuffer();
        for (i = 0; i < this.getMergeColumns().size(); ++i) {
            if (temp.length() > 0) {
                temp.append("|");
            }
            DataSetUtil.MergeColumn m = this.getMergeColumns().get(i);
            temp.append(m.getFrom());
            temp.append(";");
            temp.append(m.getTo());
        }
        out.setProperty("mergecolumns", temp.toString());
        out.setProperty("endtablete", this.getEndTableText());
        out.setProperty("pdffilenameprefix", this.getPdfFilenamePrefix());
        out.setProperty("xmlfilenameprefix", this.getXmlFilenamePrefix());
        out.setProperty("textfilenameprefix", this.getTextFilenamePrefix());
        out.setProperty("tablefilenameprefix", this.getTableFilenamePrefix());
        out.setProperty("imagefilenameprefix", this.getImageFilenamePrefix());
        out.setProperty("pagefrom", this.getPageFrom() + "");
        out.setProperty("pageto", this.getPageTo() + "");
        out.setProperty("tabletoextract", this.getTableToExtract() + "");
        out.setProperty("tableattachmentclass", this.getTableAttachmentClass());
        out.setProperty("xmlattachmentclass", this.getXmlAttachmentClass());
        out.setProperty("imageattachmentclass", this.getImageAttachmentClass());
        out.setProperty("textattachmentclass", this.getTextAttachmentClass());
        out.setProperty("pdfattachmentclass", this.getPdfAttachmentClass());
        return out;
    }

    public String getPdfFilenamePrefix() {
        return this.pdfName;
    }

    public void setPdfFilenamePrefix(String pdfName) {
        this.pdfName = pdfName;
    }

    public String getXmlFilenamePrefix() {
        return this.xmlName;
    }

    public void setXmlFilenamePrefix(String xmlName) {
        this.xmlName = xmlName;
    }

    public String getTextFilenamePrefix() {
        return this.textName;
    }

    public void setTextFilenamePrefix(String textName) {
        this.textName = textName;
    }

    public String getTableFilenamePrefix() {
        return this.tableName;
    }

    public void setTableFilenamePrefix(String tableName) {
        this.tableName = tableName;
    }

    public String getImageFilenamePrefix() {
        return this.imageName;
    }

    public void setImageFilenamePrefix(String imageName) {
        this.imageName = imageName;
    }

    public List<DataSetUtil.MergeColumn> getMergeColumns() {
        return this.mergeColumns;
    }

    public void setMergeColumns(List<DataSetUtil.MergeColumn> mCols) {
        this.mergeColumns = (ArrayList)mCols;
    }

    public void setMergeColumns(String mergeColumns) {
        if (mergeColumns.length() > 0) {
            String[] mergeDetails = StringUtil.split(mergeColumns, "|");
            ArrayList<DataSetUtil.MergeColumn> merge = new ArrayList<DataSetUtil.MergeColumn>();
            for (int i = 0; i < mergeDetails.length; ++i) {
                String[] m = StringUtil.split(mergeDetails[i], ";");
                if (m.length != 2) continue;
                try {
                    DataSetUtil.MergeColumn mergeColumn = new DataSetUtil.MergeColumn(Integer.parseInt(m[0]), Integer.parseInt(m[1]));
                    merge.add(mergeColumn);
                    continue;
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            this.mergeColumns = merge;
        } else {
            this.mergeColumns = new ArrayList();
        }
    }

    public String getEndTableText() {
        return this.endTableText;
    }

    public void setEndTableText(String endText) {
        this.endTableText = endText;
    }

    public String[] getTableColumns() {
        return this.tableColumns;
    }

    public void setTableColumns(String cols) {
        this.tableColumns = cols.length() > 0 ? StringUtil.split(cols, ";") : new String[0];
    }

    public void setTableColumns(String[] cols) {
        this.tableColumns = cols;
    }

    public boolean getClearWorkingFolder() {
        return this.clearWorkingDirectory;
    }

    public void setClearWorkingFolder(boolean clear) {
        this.clearWorkingDirectory = clear;
    }

    public boolean getExtractText() {
        return this.extractText;
    }

    public void setExtractText(boolean extract) {
        this.extractText = extract;
    }

    public boolean getExtractTables() {
        return this.extractTables;
    }

    public void setExtractTables(boolean extract) {
        this.extractTables = extract;
    }

    public boolean getExtractAsXML() {
        return this.extractAsXML;
    }

    public void setExtractAsXML(boolean extract) {
        this.extractAsXML = extract;
    }

    public boolean getGeneratePDFForPS() {
        return this.generatePDFForPS;
    }

    public void setGeneratePDFForPS(boolean generate) {
        this.generatePDFForPS = generate;
    }

    public boolean getExtractImages() {
        return this.extractImages;
    }

    public void setExtractImages(boolean extract) {
        this.extractImages = extract;
    }

    public void setBackwardsCompatiableTableColumns(boolean extract) {
        this.backwardsCompatiableTableColumns = extract;
    }

    public boolean getBackwardsCompatiableTableColumns() {
        return this.backwardsCompatiableTableColumns;
    }
}

