/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sf.jasperreports.engine.JRPropertiesMap
 *  net.sf.jasperreports.engine.export.GenericElementHandlerBundle
 *  net.sf.jasperreports.extensions.ExtensionsRegistry
 *  net.sf.jasperreports.extensions.ExtensionsRegistryFactory
 */
package net.sf.jasperreports.extensions;

import java.util.Collections;
import java.util.List;
import net.sf.jasperreports.engine.JRPropertiesMap;
import net.sf.jasperreports.engine.export.GenericElementHandlerBundle;
import net.sf.jasperreports.extensions.ExtensionsRegistry;
import net.sf.jasperreports.extensions.ExtensionsRegistryFactory;
import net.sf.jasperreports.extensions.HtmlElementHandlerBundle;

public class HtmlElementExtensionsRegistryFactory
implements ExtensionsRegistryFactory {
    private static final ExtensionsRegistry defaultExtensionsRegistry = new ExtensionsRegistry(){

        public <T> List<T> getExtensions(Class<T> extensionType) {
            if (GenericElementHandlerBundle.class.equals(extensionType)) {
                return Collections.singletonList(HtmlElementHandlerBundle.getInstance());
            }
            return null;
        }
    };

    public ExtensionsRegistry createRegistry(String registryId, JRPropertiesMap properties) {
        return defaultExtensionsRegistry;
    }
}

