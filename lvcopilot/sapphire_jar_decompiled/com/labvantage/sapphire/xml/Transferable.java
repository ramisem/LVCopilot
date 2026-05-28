/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.xml;

import com.labvantage.sapphire.xml.ImportDirective;
import com.labvantage.sapphire.xml.Logger;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipOutputStream;
import sapphire.SapphireException;
import sapphire.util.DBAccess;
import sapphire.xml.PropertyList;

public interface Transferable {
    public File getFile();

    public void setFile(File var1);

    public String getId();

    public void export(File var1, PrintStream var2, ZipOutputStream var3, DBAccess var4, int var5, Logger var6, Map var7) throws CloneNotSupportedException, SQLException, IOException, SapphireException;

    public boolean startElementImport(DBAccess var1, String var2, Properties var3, Logger var4) throws SapphireException;

    public boolean endElementImport(DBAccess var1, String var2, String var3, boolean var4, Logger var5) throws SapphireException;

    public void setVerbose(boolean var1);

    public void setParseOnly(boolean var1);

    public Object getParsedData();

    public void generateAntTask(PrintStream var1, int var2);

    public Object clone() throws CloneNotSupportedException;

    public List getReferencedItems();

    public void setTransferOption(String var1, String var2);

    public String getTransferOption(String var1);

    public void setImportTarget(int var1);

    public void setImportObject(Object var1);

    public void setCommitScope(String var1);

    public void setIgnoreMissingObjects(boolean var1);

    public void evalProperties(PropertyList var1);

    public void setZipFile(File var1);

    public void setZipFileEntry(String var1);

    public void setImportForceUpdate(boolean var1);

    public void setImportDirectives(ArrayList<ImportDirective> var1);
}

