/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.webadmin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ByPassUtil {
    public static void writeToFile(String filename, String valuetree) throws IOException {
        FileWriter writer = new FileWriter(filename);
        writer.write(valuetree);
        writer.close();
    }

    public static String loadFromFile(String filename) throws IOException {
        File file = new File(filename);
        int len = new Long(file.length()).intValue();
        FileReader reader = new FileReader(file);
        char[] contents = new char[len];
        reader.read(contents);
        return new String(contents);
    }
}

