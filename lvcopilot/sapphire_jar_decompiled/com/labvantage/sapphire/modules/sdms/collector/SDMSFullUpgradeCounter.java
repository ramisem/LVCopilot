/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.sdms.collector;

import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.util.file.FileTransfer;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.xml.PropertyList;

public class SDMSFullUpgradeCounter {
    static HashMap<String, String> hashes = new HashMap();
    static HashMap<String, Long> lastChecked = new HashMap();

    public static String getCounter() {
        return "1";
    }

    public static String getLibCustomHash(ConfigurationProcessor configurationProcessor) {
        try {
            PropertyList policy = configurationProcessor.getPolicy("SDMSPolicy", "Sapphire Custom");
            String customJarFolderName = FileUtil.substituteConfigurationPaths(policy.getPropertyListNotNull("installer").getProperty("customjarfolder"));
            if (customJarFolderName.length() > 0) {
                Path customFolder = Paths.get(customJarFolderName, new String[0]);
                if (!customFolder.toFile().exists()) {
                    throw new SapphireException("Could not locate the root image path " + customFolder);
                }
                return SDMSFullUpgradeCounter.getLibCustomHash(customFolder.resolve("libcustom"), customFolder.resolve("conf/wrapper-custom.conf"));
            }
            return "";
        }
        catch (SapphireException e) {
            return "Unknown";
        }
    }

    public static String getLibCustomHash(Path libCustom, Path customConif) {
        String key = libCustom.toFile().getAbsolutePath();
        String hash = hashes.get(key);
        Long last = lastChecked.get(key);
        if (hash == null || System.currentTimeMillis() - last > 60000L) {
            File[] files;
            long checksum = 0L;
            if (libCustom != null && libCustom.toFile().exists() && ((files = libCustom.toFile().listFiles()) != null || files.length > 0)) {
                for (File file : files) {
                    try {
                        checksum += (long)file.getName().hashCode() + FileTransfer.generateCheckSum(new FileInputStream(file));
                    }
                    catch (Exception e) {
                        checksum += file.length();
                    }
                }
            }
            if (customConif != null && customConif.toFile().exists()) {
                try {
                    checksum += (long)customConif.toFile().getName().hashCode() + FileTransfer.generateCheckSum(new FileInputStream(customConif.toFile()));
                }
                catch (Exception e) {
                    checksum += customConif.toFile().length();
                }
            }
            hash = "" + checksum;
            hashes.put(key, hash);
            lastChecked.put(key, System.currentTimeMillis());
        }
        return hash;
    }
}

