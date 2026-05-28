/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.aspose.cells.Workbook
 *  com.aspose.cells.Worksheet
 *  org.apache.commons.io.FilenameUtils
 */
package com.labvantage.sapphire.util.array;

import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import org.apache.commons.io.FilenameUtils;
import sapphire.util.DataSet;

public class ExcelUtil {
    static final String LABVANTAGE_CVS_ID = "1: 1.1 $";

    public static File createExcelWorkbook(String path, String fileName, Map<String, DataSet> sheetDSMap) {
        File file = new File(path + "/" + fileName);
        String extension = FilenameUtils.getExtension((String)file.getName());
        try {
            if (!new File(path).exists()) {
                new File(path).mkdir();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            Workbook workbook = new Workbook();
            workbook.getWorksheets().removeAt(0);
            for (String sheetName : sheetDSMap.keySet()) {
                DataSet dataSet = sheetDSMap.get(sheetName);
                Worksheet sheet = workbook.getWorksheets().add(sheetName);
                String[] columns = dataSet.getColumns();
                int row = 0;
                if (columns.length > 0) {
                    for (int i = 0; i < columns.length; ++i) {
                        String column = columns[i];
                        sheet.getCells().get(row, i).setValue((Object)(sheetName + "_" + column));
                    }
                }
                ++row;
                for (int rowIndex = 0; rowIndex < dataSet.size(); ++rowIndex) {
                    for (int i = 0; i < columns.length; ++i) {
                        String column = columns[i];
                        sheet.getCells().get(row, i).setValue((Object)dataSet.getValue(rowIndex, column, ""));
                    }
                    ++row;
                }
            }
            FileOutputStream out = new FileOutputStream(file);
            workbook.save((OutputStream)out, extension.equalsIgnoreCase("xlsx") ? 6 : 5);
            out.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }
}

