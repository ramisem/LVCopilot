/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sf.jasperreports.engine.JRPropertiesMap
 *  net.sf.jasperreports.engine.component.ComponentsBundle
 *  net.sf.jasperreports.engine.component.ComponentsXmlParser
 *  net.sf.jasperreports.engine.component.DefaultComponentXmlParser
 *  net.sf.jasperreports.engine.component.DefaultComponentsBundle
 *  net.sf.jasperreports.engine.component.XmlDigesterConfigurer
 *  net.sf.jasperreports.extensions.ExtensionsRegistry
 *  net.sf.jasperreports.extensions.ExtensionsRegistryFactory
 */
package net.sf.jasperreports.components.html;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import net.sf.jasperreports.components.html.HtmlComponentCompiler;
import net.sf.jasperreports.components.html.HtmlComponentDesignConverter;
import net.sf.jasperreports.components.html.HtmlComponentDigester;
import net.sf.jasperreports.components.html.HtmlComponentFillFactory;
import net.sf.jasperreports.components.html.HtmlComponentManager;
import net.sf.jasperreports.engine.JRPropertiesMap;
import net.sf.jasperreports.engine.component.ComponentsBundle;
import net.sf.jasperreports.engine.component.ComponentsXmlParser;
import net.sf.jasperreports.engine.component.DefaultComponentXmlParser;
import net.sf.jasperreports.engine.component.DefaultComponentsBundle;
import net.sf.jasperreports.engine.component.XmlDigesterConfigurer;
import net.sf.jasperreports.extensions.ExtensionsRegistry;
import net.sf.jasperreports.extensions.ExtensionsRegistryFactory;

public class HtmlComponentExtensionsRegistryFactory
implements ExtensionsRegistryFactory {
    public static final String NAMESPACE = "http://jasperreports.sourceforge.net/htmlcomponent";
    public static final String XSD_LOCATION = "http://jasperreports.sourceforge.net/xsd/htmlcomponent.xsd";
    public static final String XSD_RESOURCE = "net/sf/jasperreports/components/html/htmlcomponent.xsd";
    protected static final String HTML_COMPONENT_NAME = "html";
    private static final ExtensionsRegistry REGISTRY;

    public ExtensionsRegistry createRegistry(String registryId, JRPropertiesMap properties) {
        return REGISTRY;
    }

    static {
        final DefaultComponentsBundle bundle = new DefaultComponentsBundle();
        HtmlComponentDigester htmlDigester = new HtmlComponentDigester();
        DefaultComponentXmlParser parser = new DefaultComponentXmlParser();
        parser.setNamespace(NAMESPACE);
        parser.setPublicSchemaLocation(XSD_LOCATION);
        parser.setInternalSchemaResource(XSD_RESOURCE);
        parser.setDigesterConfigurer((XmlDigesterConfigurer)htmlDigester);
        bundle.setXmlParser((ComponentsXmlParser)parser);
        HashMap<String, HtmlComponentManager> componentManagers = new HashMap<String, HtmlComponentManager>();
        HtmlComponentManager htmlManager = new HtmlComponentManager();
        htmlManager.setDesignConverter(new HtmlComponentDesignConverter());
        htmlManager.setComponentCompiler(new HtmlComponentCompiler());
        htmlManager.setComponentFillFactory(new HtmlComponentFillFactory());
        componentManagers.put(HTML_COMPONENT_NAME, htmlManager);
        bundle.setComponentManagers(componentManagers);
        REGISTRY = new ExtensionsRegistry(){

            public <T> List<T> getExtensions(Class<T> extensionType) {
                if (ComponentsBundle.class.equals(extensionType)) {
                    return Collections.singletonList(bundle);
                }
                return null;
            }
        };
    }
}

