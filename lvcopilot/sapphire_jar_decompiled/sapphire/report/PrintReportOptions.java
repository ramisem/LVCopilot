/*
 * Decompiled with CFR 0.152.
 */
package sapphire.report;

import com.labvantage.sapphire.util.file.FileManager;
import java.util.Arrays;
import javax.print.DocFlavor;

public class PrintReportOptions {
    public static final int PRINTMODE_UNKNOWN = 0;
    public static final int PRINTMODE_PLAINTEXT = 1;
    public static final int PRINTMODE_EXCEL = 2;
    public static final int PRINTMODE_WORD = 3;
    public static final int PRINTMODE_PDF = 4;
    public static final int PRINTMODE_IMAGE = 5;
    public static final int PRINTMODE_CUSTOM = 6;
    public int printMode;
    public DocFlavor.INPUT_STREAM imagePrintFavor;
    public int excelSheetIndexToPrint = 0;

    public PrintReportOptions(String fileName) {
        String extension = FileManager.getExtension(fileName).toLowerCase();
        int n = Arrays.asList("txt", "csv", "seq").contains(extension) ? 1 : (Arrays.asList("xls", "xlsx").contains(extension) ? 2 : (Arrays.asList("doc", "docx").contains(extension) ? 3 : (Arrays.asList("pdf", "pdfa").contains(extension) ? 4 : (this.printMode = Arrays.asList("gif", "jpeg", "jpg", "png", "ps").contains(extension) ? 5 : 0))));
        this.imagePrintFavor = Arrays.asList("jpg", "jpeg").contains(extension) ? DocFlavor.INPUT_STREAM.JPEG : (Arrays.asList("gif").contains(extension) ? DocFlavor.INPUT_STREAM.GIF : (Arrays.asList("png").contains(extension) ? DocFlavor.INPUT_STREAM.PNG : (Arrays.asList("ps").contains(extension) ? DocFlavor.INPUT_STREAM.POSTSCRIPT : DocFlavor.INPUT_STREAM.AUTOSENSE)));
    }

    public void setPrintMode(int printMode) {
        this.printMode = printMode;
    }

    public void setImagePrintFavor(DocFlavor.INPUT_STREAM imagePrintFavor) {
        this.imagePrintFavor = imagePrintFavor;
    }

    public void setExcelSheetIndexToPrint(int excelSheetIndexToPrint) {
        this.excelSheetIndexToPrint = excelSheetIndexToPrint;
    }
}

