/*
 * Decompiled with CFR 0.152.
 */
package sapphire.report;

import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.report.ReportConstants;
import com.labvantage.sapphire.util.file.FileManager;
import com.labvantage.sapphire.util.file.FileType;
import java.io.File;
import java.io.OutputStream;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.report.PrintReportOptions;
import sapphire.util.ConnectionInfo;

public abstract class BaseJavaReport
extends BaseCustom
implements ReportConstants {
    private ClassLoader classLoader = null;

    public String[] getReportParameters() {
        return new String[0];
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public ClassLoader getClassLoader() {
        return this.classLoader != null ? this.classLoader : this.getClass().getClassLoader();
    }

    public abstract void init(String var1, String var2, HashMap var3, ConnectionInfo var4) throws SapphireException;

    public abstract String getLogicalFileName(String var1);

    public abstract void runReport(OutputStream var1) throws SapphireException;

    public String getMimeType(String filename) {
        return filename == null || filename.length() == 0 ? "" : FileType.getFileTypeByExtension(FileManager.getExtension(filename), this.getConnectionId()).getMime();
    }

    public void adjustPrintOptions(PrintReportOptions options) {
    }

    public void sendToPrinter(String printerName, File file, PrintReportOptions options) {
    }

    public boolean canPrint() {
        return false;
    }
}

