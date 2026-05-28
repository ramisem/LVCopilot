/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sf.jasperreports.engine.JRPropertiesMap
 *  net.sf.jasperreports.engine.JRPropertiesUtil
 *  net.sf.jasperreports.engine.JRPropertiesUtil$PropertySuffix
 *  net.sf.jasperreports.engine.fonts.FontExtensionsRegistry
 *  net.sf.jasperreports.extensions.ExtensionsRegistry
 *  net.sf.jasperreports.extensions.ExtensionsRegistryFactory
 */
package net.sf.jasperreports.extensions;

import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.util.file.ZipFileUtil;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import net.sf.jasperreports.engine.JRPropertiesMap;
import net.sf.jasperreports.engine.JRPropertiesUtil;
import net.sf.jasperreports.engine.fonts.FontExtensionsRegistry;
import net.sf.jasperreports.extensions.ExtensionsRegistry;
import net.sf.jasperreports.extensions.ExtensionsRegistryFactory;

public class SimpleFontExtensionsRegistry
implements ExtensionsRegistryFactory {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";
    public static final String SIMPLE_FONT_FAMILIES_PROPERTY_PREFIX = "net.sf.jasperreports.extension.simple.font.families.";

    public ExtensionsRegistry createRegistry(String registryId, JRPropertiesMap properties) {
        try {
            String REPORT_ROOT = Configuration.getInstance().getApplicationHome() + "/reports";
            String fontExtensionJarRoot = REPORT_ROOT + "/lib/fonts";
            Properties propertiesFile = new Properties();
            File fontJarDir = new File(fontExtensionJarRoot);
            File tempDir = new File(fontExtensionJarRoot + "/font-extension");
            if (fontJarDir.isDirectory()) {
                if (tempDir.exists() && tempDir.isDirectory()) {
                    FileUtil.deleteAll(tempDir);
                }
                File[] jars = fontJarDir.listFiles();
                for (int i = 0; i < jars.length; ++i) {
                    if (jars[i].isDirectory() || !jars[i].getName().endsWith(".jar")) continue;
                    ZipFileUtil.extractAll(jars[i], tempDir);
                    File[] jarFiles = tempDir.listFiles();
                    for (int j = 0; j < jarFiles.length; ++j) {
                        if (jarFiles[j].isDirectory() || !jarFiles[j].getName().endsWith(".properties")) continue;
                        FileInputStream fis = new FileInputStream(jarFiles[j]);
                        propertiesFile.clear();
                        propertiesFile.load(fis);
                        fis.close();
                        break;
                    }
                    Enumeration<Object> enumKeys = propertiesFile.keys();
                    String key = "";
                    HashSet<String> value = new HashSet<String>();
                    while (enumKeys.hasMoreElements()) {
                        key = (String)enumKeys.nextElement();
                        value.add(propertiesFile.getProperty(key));
                        if (!key.contains("font.families")) continue;
                        properties.setProperty(key, tempDir + "/" + propertiesFile.getProperty(key));
                    }
                    String jarFontFilePath = fontExtensionJarRoot + "/font-extension/fonts/";
                    File[] jarFontFiles = new File(jarFontFilePath).listFiles();
                    for (int j = 0; j < jarFontFiles.length; ++j) {
                        if (jarFontFiles[j].isDirectory() || !jarFontFiles[j].getName().endsWith(".xml") || value == null || value.size() <= 0 || !value.contains("fonts/" + jarFontFiles[j].getName())) continue;
                        this.updateXMLFile(jarFontFilePath + jarFontFiles[j].getName(), "fonts/", jarFontFilePath);
                    }
                }
            }
        }
        catch (Exception REPORT_ROOT) {
            // empty catch block
        }
        List fontFamiliesProperties = JRPropertiesUtil.getProperties((JRPropertiesMap)properties, (String)SIMPLE_FONT_FAMILIES_PROPERTY_PREFIX);
        ArrayList<String> fontFamiliesLocations = new ArrayList<String>();
        for (JRPropertiesUtil.PropertySuffix fontFamiliesProp : fontFamiliesProperties) {
            String fontFamiliesLocation = fontFamiliesProp.getValue();
            fontFamiliesLocations.add(fontFamiliesLocation);
        }
        return new FontExtensionsRegistry(fontFamiliesLocations);
    }

    private void updateXMLFile(String filePath, String toReplace, String replaceWith) {
        File file = new File(filePath);
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            StringBuffer stringBuffer = new StringBuffer();
            String line = null;
            FileWriter fw = null;
            BufferedWriter bw = null;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line.replace(toReplace, replaceWith) + "\n");
            }
            bufferedReader.close();
            if (file.delete()) {
                fw = new FileWriter(file);
                bw = new BufferedWriter(fw);
                bw.write(stringBuffer.toString());
                bw.flush();
                bw.close();
            }
        }
        catch (FileNotFoundException fileNotFoundException) {
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }
}

