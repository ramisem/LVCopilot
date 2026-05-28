/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpSession
 *  javax.servlet.jsp.PageContext
 */
package sapphire.util;

import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.cache.CacheUtil;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;
import org.json.JSONArray;
import org.json.JSONObject;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.servlet.RequestContext;
import sapphire.util.Logger;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class Browser
implements Serializable {
    private static final String[] supportedBrowsers = new String[]{"{ 'browser': 9, 'minversion': [ 12.0 ], 'maxversion': [ -1 ], 'excludeversion': [ ], 'os': [ 0 ], 'alias':'' }", "{ 'browser': 0, 'minversion': [ 11.0 ], 'maxversion': [ 11.0 ], 'excludeversion': [ ], 'os': [ 0 ], 'alias':'' }", "{ 'browser': 4, 'minversion': [ 5.0, 5.1 ], 'maxversion': [ 5.1 ], 'excludeversion': [ ], 'os': [ 0, 3, 4, 5 ], 'alias':'' }", "{ 'browser': 4, 'minversion': [ 5.0 ], 'maxversion': [ 5.1 ], 'excludeversion': [ ], 'os': [ 13, 14 ], 'alias': 'iPad, iPhone 4+' }", "{ 'browser': 6, 'minversion': [ 4.0 ], 'maxversion': [ -1 ], 'excludeversion': [ ], 'os': [ 6 ], 'alias': 'Android 4+' }", "{ 'browser': 1, 'minversion': [ 5.0 ], 'maxversion': [ -1 ], 'excludeversion': [ ], 'os': [ 3, 9, 10, 11 ], 'alias':'' }"};
    public static String ATTRIBUTE = "sapphirebrowser";
    public static final int IE = 0;
    public static final int CHROME = 1;
    public static final int FIREFOX = 2;
    public static final int OPERA = 3;
    public static final int SAFARI = 4;
    public static final int OTHER = 5;
    public static final int ANDROID = 6;
    public static final int SKYFIRE = 7;
    public static final int BLACKBERRY = 8;
    public static final int EDGE = 9;
    public static final int WINDOWS_OS = 0;
    public static final int WINDOWS_CE_OS = 2;
    public static final int MACINTOSH_OS = 3;
    public static final int LINUX_OS = 4;
    public static final int UBUNTU_LINUX_OS = 5;
    public static final int ANDROID_OS = 6;
    public static final int OTHER_OS = 7;
    public static final int BLACKBERRY_OS = 8;
    public static final int WINDOWS_XP_OS = 9;
    public static final int WINDOWS_VISTA_OS = 10;
    public static final int WINDOWS_7_OS = 11;
    public static final int WINDOWS_2K_OS = 12;
    public static final int IPHONE_OS = 13;
    public static final int IPAD_OS = 14;
    public static final int SYMBIAN_OS = 15;
    public static final int WINDOWS_8_OS = 16;
    public static final int WINDOWS_8_1_OS = 17;
    public static final int WINDOWS_10_OS = 18;
    private String useragent;
    private double version;
    private double compatiableVersion;
    private double webkitVersion;
    private double mozillaVersion;
    private int oS;
    private double html;
    private double css;
    private boolean compatibilityMode;
    private boolean mozillaBased;
    private boolean chromiumBased;
    private int browser;
    private boolean netEnabled;
    private boolean webkitBased;
    private boolean supported;
    private boolean embedded;
    private boolean gc;
    private String supportedString;
    private GUIMode guiMode;
    private ViewPort viewport;
    private ArrayList<GUIMode> guiModes;

    public static ArrayList<GUIMode> getGUIModes(ConfigurationProcessor cp) {
        PropertyListCollection guimodes;
        ArrayList<GUIMode> guiModes = new ArrayList<GUIMode>();
        HashMap guiPolicy = null;
        if (cp != null) {
            try {
                guiPolicy = cp.getPolicy("GUIPolicy", "Sapphire Custom");
            }
            catch (Exception e) {
                guiPolicy = null;
            }
        }
        if (guiPolicy != null && guiPolicy.size() > 0 && (guimodes = ((PropertyList)guiPolicy).getCollection("guimodes")) != null) {
            for (int g = 0; g < guimodes.size(); ++g) {
                PropertyList guimode = guimodes.getPropertyList(g);
                if (guimode.getProperty("guimodeid").length() <= 0 || !guimode.getProperty("enabled", "Y").equalsIgnoreCase("Y")) continue;
                ArrayList<Integer> os = new ArrayList<Integer>();
                PropertyListCollection oss = guimode.getCollection("os");
                if (oss != null) {
                    for (int o = 0; o < oss.size(); ++o) {
                        PropertyList osp = oss.getPropertyList(o);
                        String osid = osp.getProperty("osid", "");
                        int osidi = -1;
                        if (osid.length() > 0) {
                            try {
                                osidi = Integer.parseInt(osid);
                            }
                            catch (Exception exception) {
                                // empty catch block
                            }
                        }
                        if (osidi <= -1) continue;
                        os.add(new Integer(osidi));
                    }
                }
                int[] osIntArray = null;
                if (os.size() > 0) {
                    osIntArray = new int[os.size()];
                    for (int i = 0; i < os.size(); ++i) {
                        osIntArray[i] = (Integer)os.get(i);
                    }
                }
                ArrayList<Integer> browser = new ArrayList<Integer>();
                PropertyListCollection browsers = guimode.getCollection("browser");
                if (browsers != null) {
                    for (int b = 0; b < browsers.size(); ++b) {
                        PropertyList browserp = browsers.getPropertyList(b);
                        String browserid = browserp.getProperty("browserid", "");
                        int browseridi = -1;
                        if (browserid.length() > 0) {
                            try {
                                browseridi = Integer.parseInt(browserid);
                            }
                            catch (Exception exception) {
                                // empty catch block
                            }
                        }
                        if (browseridi <= -1) continue;
                        browser.add(new Integer(browseridi));
                    }
                }
                int[] browserIntArray = null;
                if (browser.size() > 0) {
                    browserIntArray = new int[browser.size()];
                    for (int i = 0; i < browser.size(); ++i) {
                        browserIntArray[i] = (Integer)browser.get(i);
                    }
                }
                int maxwidth = -1;
                if (guimode.getProperty("maxwidth").length() > 0) {
                    try {
                        maxwidth = Integer.parseInt(guimode.getProperty("maxwidth"));
                    }
                    catch (Exception browserid) {
                        // empty catch block
                    }
                }
                int maxheight = -1;
                if (guimode.getProperty("maxheight").length() > 0) {
                    try {
                        maxheight = Integer.parseInt(guimode.getProperty("maxheight"));
                    }
                    catch (Exception browseridi) {
                        // empty catch block
                    }
                }
                GUIMode guiMode = new GUIMode(guimode.getProperty("guimodeid"), guimode.getProperty("guimodename", guimode.getProperty("guimodeid")), maxwidth, maxheight, guimode.getProperty("hastouch", "N").equalsIgnoreCase("Y"), guimode.getProperty("adaptfortouch", "Y").equalsIgnoreCase("Y"), osIntArray, browserIntArray, guimode.getProperty("useragentmatch", ""), guimode.getProperty("autodetect", "N").equalsIgnoreCase("Y"), guimode.getProperty("image", "FlatBlackQuestionHelp1"), guimode.getProperty("ismobile", "N").equalsIgnoreCase("Y"), guimode.getProperty("istablet", "N").equalsIgnoreCase("Y"), guimode.getProperty("isphone", "N").equalsIgnoreCase("Y"), !guimode.getProperty("showsidebar", "Y").equalsIgnoreCase("N"), !guimode.getProperty("navigationbar", "Y").equalsIgnoreCase("N"), guimode.getProperty("navigationbarmode", ""), guimode.getProperty("starturl", ""), guimode.getProperty("startgroupgizmo", ""), guimode.getProperty("startmenugizmo", ""));
                guiModes.add(guiMode);
            }
        }
        return guiModes;
    }

    public boolean hasTouch() {
        return this.guiMode != null && this.viewport != null ? this.viewport.hasTouch() && this.guiMode.adaptForTouch() : (this.viewport != null ? this.viewport.hasTouch() : false);
    }

    public boolean isEmbedded() {
        return this.viewport != null && this.viewport.isEmbedded();
    }

    public static ArrayList<GUIMode> getGUIModes(SapphireConnection sapphireConnection, String databaseid) {
        if ((databaseid == null || databaseid.length() == 0) && sapphireConnection != null) {
            databaseid = sapphireConnection.getDatabaseId();
        }
        if (databaseid != null && databaseid.length() > 0) {
            ArrayList guiModes = new ArrayList();
            Object o = CacheUtil.get(databaseid, "GUIModes", "GUIModes");
            if (o != null && o instanceof ArrayList) {
                try {
                    return (ArrayList)o;
                }
                catch (Exception e) {
                    return null;
                }
            }
            if (sapphireConnection != null && sapphireConnection.getConnectionId().length() > 0) {
                return Browser.getGUIModes(new ConfigurationProcessor(sapphireConnection.getConnectionId()));
            }
            return null;
        }
        return null;
    }

    public ArrayList<GUIMode> getGUIModes() {
        ArrayList<GUIMode> guis = this.guiModes == null ? new ArrayList<GUIMode>() : this.guiModes;
        return guis;
    }

    public Browser() {
        this.compatiableVersion = this.version = 0.0;
        this.webkitVersion = 0.0;
        this.mozillaVersion = 0.0;
        this.oS = 7;
        this.html = 0.0;
        this.css = 0.0;
        this.compatibilityMode = false;
        this.mozillaBased = false;
        this.chromiumBased = false;
        this.browser = 5;
        this.netEnabled = false;
        this.webkitBased = false;
        this.supported = false;
        this.embedded = false;
        this.gc = false;
        this.supportedString = "";
        this.guiMode = null;
        this.viewport = null;
        this.guiModes = null;
    }

    public Browser(PageContext pageContext) {
        this.compatiableVersion = this.version = 0.0;
        this.webkitVersion = 0.0;
        this.mozillaVersion = 0.0;
        this.oS = 7;
        this.html = 0.0;
        this.css = 0.0;
        this.compatibilityMode = false;
        this.mozillaBased = false;
        this.chromiumBased = false;
        this.browser = 5;
        this.netEnabled = false;
        this.webkitBased = false;
        this.supported = false;
        this.embedded = false;
        this.gc = false;
        this.supportedString = "";
        this.guiMode = null;
        this.viewport = null;
        this.guiModes = null;
        this.setPageContext(pageContext);
    }

    public Browser(HttpServletRequest request) {
        this.compatiableVersion = this.version = 0.0;
        this.webkitVersion = 0.0;
        this.mozillaVersion = 0.0;
        this.oS = 7;
        this.html = 0.0;
        this.css = 0.0;
        this.compatibilityMode = false;
        this.mozillaBased = false;
        this.chromiumBased = false;
        this.browser = 5;
        this.netEnabled = false;
        this.webkitBased = false;
        this.supported = false;
        this.embedded = false;
        this.gc = false;
        this.supportedString = "";
        this.guiMode = null;
        this.viewport = null;
        this.guiModes = null;
        this.setRequest(request);
    }

    public Browser(String useragent) {
        this.compatiableVersion = this.version = 0.0;
        this.webkitVersion = 0.0;
        this.mozillaVersion = 0.0;
        this.oS = 7;
        this.html = 0.0;
        this.css = 0.0;
        this.compatibilityMode = false;
        this.mozillaBased = false;
        this.chromiumBased = false;
        this.browser = 5;
        this.netEnabled = false;
        this.webkitBased = false;
        this.supported = false;
        this.embedded = false;
        this.gc = false;
        this.supportedString = "";
        this.guiMode = null;
        this.viewport = null;
        this.guiModes = null;
        this.setUseragent(useragent);
    }

    public void setUseragent(String user) {
        this.useragent = user;
        this.detect();
    }

    public void setBrowser(Browser basedon) {
        this.useragent = basedon.useragent;
        this.gc = basedon.gc;
        this.version = basedon.version;
        this.compatiableVersion = basedon.compatiableVersion;
        this.css = basedon.css;
        this.html = basedon.html;
        this.webkitVersion = basedon.webkitVersion;
        this.mozillaVersion = basedon.mozillaVersion;
        this.oS = basedon.oS;
        this.compatibilityMode = basedon.compatibilityMode;
        this.mozillaBased = basedon.mozillaBased;
        this.chromiumBased = basedon.chromiumBased;
        this.browser = basedon.browser;
        this.netEnabled = basedon.netEnabled;
        this.webkitBased = basedon.webkitBased;
        this.supported = basedon.supported;
        this.supportedString = basedon.supportedString;
        this.guiModes = basedon.guiModes;
        this.guiMode = basedon.guiMode;
        this.viewport = basedon.viewport;
    }

    public void setRequest(HttpServletRequest request) {
        Object ob;
        Object object = ob = request.getSession() != null ? request.getSession().getAttribute(ATTRIBUTE) : null;
        if (ob != null && ob instanceof Browser) {
            Browser org = (Browser)ob;
            if (org.getUseragent() != null && org.getUseragent().equals(request.getHeader("User-Agent"))) {
                RequestContext rc;
                this.setBrowser((Browser)ob);
                boolean change = false;
                if (this.guiModes == null && (rc = RequestContext.getRequestContext(request)) != null && rc.getConnectionId().length() > 0) {
                    ConnectionProcessor cp = new ConnectionProcessor(rc.getConnectionId());
                    this.guiModes = Browser.getGUIModes(cp.getSapphireConnection(), null);
                    change = true;
                }
                if (this.viewport == null && request.getParameter("_viewport") != null && request.getParameter("_viewport").length() > 0) {
                    this.viewport = new ViewPort(request.getParameter("_viewport"));
                    change = true;
                }
                if (this.guiMode == null) {
                    this.detectDevice();
                    change = true;
                }
                if (change) {
                    this.saveCache(request.getSession());
                }
            } else {
                RequestContext rc;
                if (this.guiModes == null && (rc = RequestContext.getRequestContext(request)) != null && rc.getConnectionId().length() > 0) {
                    ConnectionProcessor cp = new ConnectionProcessor(rc.getConnectionId());
                    this.guiModes = Browser.getGUIModes(cp.getSapphireConnection(), null);
                }
                if (this.viewport == null && request.getParameter("_viewport") != null && request.getParameter("_viewport").length() > 0) {
                    this.viewport = new ViewPort(request.getParameter("_viewport"));
                }
                this.setUseragent(request.getHeader("User-Agent"));
                this.detectDevice();
                this.saveCache(request.getSession());
            }
        } else {
            RequestContext rc;
            if (this.guiModes == null && (rc = RequestContext.getRequestContext(request)) != null && rc.getConnectionId().length() > 0) {
                ConnectionProcessor cp = new ConnectionProcessor(rc.getConnectionId());
                this.guiModes = Browser.getGUIModes(cp.getSapphireConnection(), null);
            }
            if (this.viewport == null && request.getParameter("_viewport") != null && request.getParameter("_viewport").length() > 0) {
                this.viewport = new ViewPort(request.getParameter("_viewport"));
            }
            this.setUseragent(request.getHeader("User-Agent"));
            this.detectDevice();
            this.saveCache(request.getSession());
        }
    }

    public ViewPort getViewPort() {
        return this.viewport;
    }

    public void setViewPort(String viewportString, HttpServletRequest request) {
        if (this.viewport == null && viewportString.length() > 0) {
            this.viewport = new ViewPort(viewportString);
            this.detectDevice();
            if (request != null) {
                this.saveCache(request.getSession());
            }
        }
    }

    private void detectDevice() {
        if (this.guiModes != null && this.viewport != null) {
            try {
                GUIMode highMatched = null;
                if (this.guiModes != null && this.guiModes.size() > 0) {
                    ArrayList filterlist = new ArrayList();
                    int matchScore = -1;
                    for (int i = 0; i < this.guiModes.size(); ++i) {
                        int k;
                        GUIMode guiMode = this.guiModes.get(i);
                        if (!guiMode.autodetect) continue;
                        boolean matchTouch = false;
                        boolean matchUserAgent = false;
                        boolean matchRes = false;
                        boolean matchOS = false;
                        boolean matchBrowser = false;
                        int currentMatch = 0;
                        if (guiMode.maxheight == -1 || guiMode.maxwidth == -1) {
                            matchRes = true;
                        } else if (this.viewport.getDeviceWidth() < guiMode.maxwidth && this.viewport.getDeviceHeight() < guiMode.maxheight || this.viewport.getDeviceWidth() < guiMode.maxwidth && this.viewport.getDeviceHeight() < guiMode.maxheight) {
                            matchRes = true;
                            ++currentMatch;
                        }
                        if (guiMode.oS != null) {
                            for (k = 0; k < guiMode.oS.length; ++k) {
                                if (guiMode.oS[k] != this.getOS()) continue;
                                matchOS = true;
                                ++currentMatch;
                                break;
                            }
                        } else {
                            matchOS = true;
                            ++currentMatch;
                        }
                        if (guiMode.userAgentMatch.length() > 0) {
                            String[] matches;
                            for (String match : matches = StringUtil.split(guiMode.userAgentMatch.toLowerCase(), ";")) {
                                if (!this.useragent.toLowerCase().contains(match)) continue;
                                ++currentMatch;
                                matchUserAgent = true;
                                break;
                            }
                        }
                        if (guiMode.browser != null) {
                            for (k = 0; k < guiMode.browser.length; ++k) {
                                if (guiMode.browser[k] != this.getBrowser()) continue;
                                matchBrowser = true;
                                ++currentMatch;
                                break;
                            }
                        } else {
                            matchBrowser = true;
                            ++currentMatch;
                        }
                        if (guiMode.hastouch() && this.viewport.hasTouch()) {
                            ++currentMatch;
                        }
                        if (currentMatch <= matchScore) continue;
                        highMatched = guiMode;
                        matchScore = currentMatch;
                    }
                }
                if (highMatched != null) {
                    this.guiMode = highMatched;
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    public void setRequest(HttpServletRequest request, boolean useCache) {
        if (useCache) {
            this.setRequest(request);
        } else {
            this.setUseragent(request.getHeader("User-Agent"));
        }
    }

    public void setMobile(boolean mobile, PageContext pageContext) {
    }

    private void clearCache(PageContext pageContext) {
        if (pageContext.getRequest() instanceof HttpServletRequest && pageContext.getRequest().getAttribute(ATTRIBUTE) != null) {
            pageContext.getRequest().removeAttribute(ATTRIBUTE);
        }
        if (pageContext.getSession() != null && pageContext.getSession().getAttribute(ATTRIBUTE) != null) {
            pageContext.getSession().removeAttribute(ATTRIBUTE);
        }
    }

    private void saveCache(HttpSession store) {
        if (store != null) {
            store.setAttribute(ATTRIBUTE, (Object)this);
        }
    }

    public void setPageContext(PageContext pageContext, boolean useCache) {
        if (useCache) {
            this.setPageContext(pageContext);
        } else if (pageContext.getRequest() instanceof HttpServletRequest) {
            this.setRequest((HttpServletRequest)pageContext.getRequest(), false);
        }
    }

    public void setGUIModes(String databaseid, HttpSession session) {
        this.guiModes = Browser.getGUIModes(null, databaseid);
        this.detectDevice();
        this.saveCache(session);
    }

    public void setPageContext(PageContext pageContext) {
        if (pageContext.getRequest() instanceof HttpServletRequest) {
            Object ob;
            HttpServletRequest req = (HttpServletRequest)pageContext.getRequest();
            Object object = ob = pageContext.getSession() != null ? pageContext.getSession().getAttribute(ATTRIBUTE) : null;
            if (ob != null && ob instanceof Browser) {
                Browser org = (Browser)ob;
                if (org.getUseragent().equals(req.getHeader("User-Agent"))) {
                    ConnectionProcessor cp;
                    this.setBrowser((Browser)ob);
                    boolean change = false;
                    if (this.guiModes == null && (cp = new ConnectionProcessor(pageContext)) != null && cp.getConnectionid().length() > 0) {
                        this.guiModes = Browser.getGUIModes(cp.getSapphireConnection(), null);
                        change = true;
                    }
                    if (this.viewport == null && req.getParameter("_viewport") != null && req.getParameter("_viewport").length() > 0) {
                        this.viewport = new ViewPort(req.getParameter("_viewport"));
                        change = true;
                    }
                    if (this.guiMode == null) {
                        this.detectDevice();
                        change = true;
                    }
                    if (req.getParameter("guimode") != null && req.getParameter("guimode").length() > 0) {
                        try {
                            this.setGUIMode(req.getParameter("guimode"), req);
                            change = true;
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                    if (change) {
                        this.saveCache(req.getSession());
                    }
                } else {
                    this.setRequest(req, false);
                    this.saveCache(pageContext.getSession());
                }
            } else {
                this.setRequest(req, false);
                this.saveCache(pageContext.getSession());
            }
        }
    }

    private double getVersionFromString(String input) {
        int fin;
        double out = 0.0;
        String temp = input;
        int end = temp.indexOf(" (");
        if (end == -1) {
            end = temp.indexOf(" ");
            if (end == -1) {
                end = temp.indexOf("-");
                if (end == -1 && (end = temp.indexOf(";")) == -1) {
                    end = temp.length();
                }
            } else {
                fin = temp.indexOf(";");
                if (fin > -1 && fin < end) {
                    end = fin;
                } else {
                    fin = temp.indexOf(")");
                    if (fin > -1 && fin < end) {
                        end = fin;
                    }
                }
            }
        } else {
            fin = temp.indexOf(" ");
            if (fin > -1 && fin < end) {
                end = fin;
                fin = temp.indexOf("-");
                if (fin == -1) {
                    fin = temp.indexOf(";");
                    if (end != -1) {
                        end = fin;
                    }
                } else {
                    end = fin;
                }
            }
        }
        end = (end = (temp = temp.substring(0, end).trim()).indexOf(".")) == -1 ? temp.length() : ((fin = temp.indexOf(".", end + 1)) > -1 ? fin : temp.length());
        temp = temp.substring(0, end);
        try {
            out = Double.parseDouble(temp);
        }
        catch (Exception e) {
            out = 0.0;
        }
        return out;
    }

    private void detect() {
        if (this.useragent != null && this.useragent.length() > 0) {
            String user = this.useragent.toLowerCase();
            int i = user.indexOf(" msie ");
            int rv = i < 0 ? user.indexOf(" rv:") : -1;
            int trident = user.indexOf(" trident");
            if (i > -1 || rv > -1 && trident > -1) {
                this.browser = 0;
                this.mozillaBased = false;
                this.chromiumBased = false;
                this.webkitBased = false;
                this.webkitVersion = 0.0;
                this.version = rv > -1 ? this.getVersionFromString(user.substring(rv + 4)) : this.getVersionFromString(user.substring(i + 6));
                if (this.version < 9.0) {
                    if (this.version < 8.0 && user.indexOf("trident/4.0") > -1) {
                        this.compatibilityMode = true;
                        this.compatiableVersion = this.version;
                        this.version = 8.0;
                    } else if (this.version < 9.0 && user.indexOf("trident/5.0") > -1) {
                        this.compatibilityMode = true;
                        this.compatiableVersion = this.version;
                        this.version = 9.0;
                    } else if (this.version < 10.0 && user.indexOf("trident/6.0") > -1) {
                        this.compatibilityMode = true;
                        this.compatiableVersion = this.version;
                        this.version = 10.0;
                    } else if (this.version < 11.0 && user.indexOf("trident/7.0") > -1) {
                        this.compatibilityMode = true;
                        this.compatiableVersion = this.version;
                        this.version = 11.0;
                    }
                } else {
                    this.compatibilityMode = false;
                    this.compatiableVersion = this.version;
                }
                if (this.version > 8.0) {
                    this.mozillaVersion = 5.0;
                    this.html = 5.0;
                    this.css = 3.0;
                } else {
                    if (this.version >= 5.0) {
                        this.html = 4.01;
                        this.css = this.version > 5.5 ? (this.version > 7.0 ? 2.1 : 1.0) : 0.0;
                    } else {
                        this.html = 3.2;
                        this.css = 0.0;
                    }
                    this.mozillaVersion = 4.0;
                }
                this.gc = true;
            } else if (user.startsWith("mozilla/")) {
                this.html = 4.01;
                this.css = 2.1;
                this.mozillaBased = true;
                this.mozillaVersion = this.getVersionFromString(user.substring(8));
                boolean mob = user.contains("mobile/") || user.contains(" mobile ") || user.contains(" android ");
                i = user.indexOf(" safari/");
                if (i > -1 && !mob) {
                    i = user.indexOf(" applewebkit/");
                    if (i > 0) {
                        this.webkitBased = true;
                        this.webkitVersion = this.getVersionFromString(user.substring(i + 13));
                        i = user.indexOf(" chrome/");
                        if (i > 0) {
                            int j = user.indexOf(" edge/");
                            if (j > 0) {
                                this.browser = 9;
                                this.version = this.getVersionFromString(user.substring(j + 6));
                            } else {
                                this.chromiumBased = true;
                                j = user.indexOf(" edg/");
                                if (j > 0) {
                                    this.browser = 9;
                                    this.version = this.getVersionFromString(user.substring(j + 5));
                                } else {
                                    this.browser = 1;
                                    this.version = this.getVersionFromString(user.substring(i + 8));
                                }
                            }
                        } else {
                            this.browser = 4;
                            this.version = this.getVersionFromString(user.substring(user.indexOf("version/") + 8));
                        }
                    } else {
                        this.webkitBased = false;
                        this.browser = 5;
                        this.version = 0.0;
                    }
                } else if (user.contains(" applewebkit/")) {
                    this.webkitBased = true;
                    this.webkitVersion = this.getVersionFromString(user.substring(user.indexOf(" applewebkit/") + 13));
                    if (i > -1) {
                        i = user.indexOf(" android ");
                        if (i > 0) {
                            int i2 = user.indexOf("chrome/");
                            if (i2 > -1) {
                                this.browser = 1;
                                this.version = this.getVersionFromString(user.substring(i2 + 7));
                            } else {
                                this.browser = 6;
                                this.version = this.getVersionFromString(user.substring(i + 9));
                            }
                        } else if (user.contains("ipad") || user.contains("iphone")) {
                            i = user.indexOf("crios/");
                            if (i > -1) {
                                this.browser = 1;
                                this.version = this.getVersionFromString(user.substring(i + 6));
                            } else {
                                this.browser = 4;
                                this.version = this.getVersionFromString(user.substring(user.indexOf("version/") + 8));
                            }
                        } else {
                            i = user.indexOf(" skyfire/");
                            if (i > 0) {
                                this.browser = 7;
                                this.version = this.getVersionFromString(user.substring(i + 9));
                            } else {
                                this.browser = 4;
                                this.version = this.getVersionFromString(user.substring(user.indexOf("version/") + 8));
                            }
                        }
                    } else if (user.contains("ipad") || user.contains("iphone")) {
                        this.browser = 4;
                        this.version = 6.0;
                    }
                } else {
                    this.webkitBased = false;
                    i = user.indexOf(" firefox/");
                    if (i > 0) {
                        this.browser = 2;
                        this.version = this.getVersionFromString(user.substring(i + 9));
                    } else {
                        this.browser = 5;
                        this.version = 0.0;
                    }
                }
                this.compatiableVersion = this.version;
            } else if (user.startsWith("opera")) {
                this.html = 4.01;
                this.css = 2.1;
                this.chromiumBased = false;
                this.mozillaBased = false;
                this.browser = 3;
                this.webkitBased = false;
                this.compatiableVersion = this.version = this.getVersionFromString(user.substring(6));
            } else if (user.startsWith("blackberry")) {
                this.html = 4.01;
                this.css = 2.1;
                this.chromiumBased = false;
                this.mozillaBased = false;
                this.webkitBased = false;
                this.webkitVersion = 0.0;
                this.mozillaVersion = 0.0;
                this.compatiableVersion = this.version = this.getVersionFromString(user.substring(user.indexOf("/")));
            } else {
                this.html = 0.0;
                this.mozillaBased = false;
                this.chromiumBased = false;
                this.webkitBased = false;
                this.webkitVersion = 0.0;
                this.browser = 5;
                this.compatiableVersion = this.version = 0.0;
            }
            this.oS = this.browser == 6 ? 6 : (this.browser == 8 ? 8 : (user.indexOf("windows ce") > -1 ? 2 : (user.indexOf("windows") > -1 ? (user.indexOf("nt 5.2") > -1 || user.indexOf("nt 5.1") > -1 ? 9 : (user.indexOf("nt 5.0") > -1 ? 12 : (user.indexOf("nt 6.0") > -1 ? 10 : (user.indexOf("nt 6.1") > -1 ? 11 : (user.indexOf("nt 6.2") > -1 ? 16 : (user.indexOf("nt 6.3") > -1 ? 17 : (user.indexOf("nt 10.0") > -1 ? 18 : 0))))))) : (user.indexOf("macintosh") > -1 ? (this.browser == 7 ? 7 : (this.browser == 4 ? (this.getViewPort() != null && this.getViewPort().hasTouch() ? 14 : 3) : 3)) : (user.indexOf("iphone") > -1 ? 13 : (user.indexOf("ipad") > -1 ? 14 : (user.indexOf("symbianos") > -1 ? 15 : (user.indexOf("linux") > -1 ? (user.indexOf("android") > -1 ? 6 : (user.indexOf("ubuntu") > -1 ? 5 : 4)) : 7))))))));
            if (user.indexOf(".net clr") > -1) {
                this.netEnabled = true;
            }
        }
        this.supported = this.getSupported();
        this.supportedString = this.getSupportedText();
        Logger.logDebug("BROWSER AGENT:" + (this.isMobile() ? "Mobile " : "") + this.getId() + " Version " + this.getVersion() + " on " + this.getOSId() + " (" + (this.supported ? "SUPPORTED" : "NOT SUPPORTED") + ").");
    }

    public String getSupportedText() {
        if (this.supportedString.length() == 0) {
            StringBuffer sbsup = new StringBuffer();
            ArrayList<Integer> processed = new ArrayList<Integer>();
            for (int i = 0; i < supportedBrowsers.length; ++i) {
                if (i > 0) {
                    sbsup.append(i < supportedBrowsers.length - 1 ? ", " : " and ");
                }
                String b = supportedBrowsers[i];
                try {
                    JSONObject job = new JSONObject(b);
                    String alias = job.getString("alias");
                    int currB = job.getInt("browser");
                    if (alias != null && alias.length() > 0) {
                        sbsup.append(alias);
                    } else {
                        double maxver;
                        sbsup.append(this.getName(currB));
                        JSONArray jayminver = job.getJSONArray("minversion");
                        double minver = jayminver.length() > 0 ? jayminver.getDouble(0) : -1.0;
                        JSONArray jaymaxver = job.getJSONArray("maxversion");
                        double d = maxver = jaymaxver.length() > 0 ? jaymaxver.getDouble(0) : -1.0;
                        if (minver > -1.0 && maxver > -1.0) {
                            sbsup.append(" from ").append(minver).append(" to ").append(maxver);
                        } else if (minver < 0.0 && maxver > -1.0) {
                            sbsup.append(" up to ").append(maxver);
                        } else if (minver > -1.0 && maxver < 0.0) {
                            sbsup.append(" ").append(minver).append(" onwards");
                        } else {
                            sbsup.append(" all versions");
                        }
                        JSONArray jayexcludever = job.getJSONArray("excludeversion");
                        if (jayexcludever.length() > 0) {
                            sbsup.append(" excluding ");
                            for (int k = 0; k < jayexcludever.length(); ++k) {
                                double exver = jayexcludever.getDouble(k);
                                if (k > 0) {
                                    sbsup.append(", ");
                                }
                                sbsup.append(exver);
                            }
                        }
                    }
                    processed.add(this.browser);
                    continue;
                }
                catch (Exception e) {
                    break;
                }
            }
            sbsup.append(".");
            this.supportedString = sbsup.toString();
        }
        return this.supportedString;
    }

    private boolean getSupported() {
        boolean sup = false;
        for (int i = 0; i < supportedBrowsers.length; ++i) {
            String b = supportedBrowsers[i];
            try {
                JSONObject job = new JSONObject(b);
                if (job.getInt("browser") != this.browser) continue;
                JSONArray jayminver = job.getJSONArray("minversion");
                boolean supver = false;
                for (int v = 0; v < jayminver.length(); ++v) {
                    double ver = jayminver.getDouble(v);
                    if (!(this.version >= ver)) continue;
                    supver = true;
                    break;
                }
                if (!supver || !supver) continue;
                JSONArray jayexcludever = job.getJSONArray("excludeversion");
                for (int v = 0; v < jayexcludever.length(); ++v) {
                    double ver = jayexcludever.getDouble(v);
                    if (this.version != ver) continue;
                    supver = false;
                    break;
                }
                if (!supver) continue;
                sup = true;
            }
            catch (Exception e) {}
            break;
        }
        return sup;
    }

    public boolean isSupported() {
        return this.supported;
    }

    public String getUseragent() {
        return this.useragent;
    }

    public boolean getNetEnabled() {
        return this.netEnabled;
    }

    public boolean getCompatibilityMode() {
        return this.compatibilityMode;
    }

    public double getCompatibleVersion() {
        return this.compatiableVersion > 0.0 ? this.compatiableVersion : this.version;
    }

    public double getHTMLVersion() {
        return this.html;
    }

    public double getCSSVersion() {
        return this.css;
    }

    public boolean requiresGarbageCollection() {
        return this.gc;
    }

    public String getId() {
        switch (this.browser) {
            case 0: {
                String id = "IE";
                id = this.version == 5.5 ? id + "5.5" : id + Math.round(this.version);
                return id;
            }
            case 1: {
                return "CH";
            }
            case 2: {
                return "FF";
            }
            case 3: {
                return "OP";
            }
            case 4: {
                return "SA";
            }
            case 6: {
                return "AN";
            }
            case 7: {
                return "SF";
            }
            case 8: {
                return "BB";
            }
            case 9: {
                return "ED";
            }
        }
        return "";
    }

    public String getCompatibleId() {
        switch (this.browser) {
            case 0: {
                String id = "IE";
                double version = this.getCompatibleVersion();
                id = version == 5.5 ? id + "5.5" : id + Math.round(version);
                return id;
            }
            case 1: {
                return "CH";
            }
            case 2: {
                return "FF";
            }
            case 3: {
                return "OP";
            }
            case 4: {
                return "SA";
            }
            case 6: {
                return "AN";
            }
            case 7: {
                return "SF";
            }
            case 8: {
                return "BB";
            }
            case 9: {
                return "ED";
            }
        }
        return "";
    }

    public String getOSId() {
        switch (this.oS) {
            case 2: {
                return "Windows CE";
            }
            case 0: {
                return "Windows";
            }
            case 12: {
                return "Windows 2K";
            }
            case 9: {
                return "Windows XP";
            }
            case 10: {
                return "Windows Vista";
            }
            case 11: {
                return "Windows 7";
            }
            case 17: {
                return "Windows 8";
            }
            case 16: {
                return "Windows 8.1";
            }
            case 18: {
                return "Windows 10";
            }
            case 4: {
                return "Linux";
            }
            case 5: {
                return "Ubuntu";
            }
            case 3: {
                return "MacOS";
            }
            case 6: {
                return "Android";
            }
            case 8: {
                return "BlackBerry";
            }
            case 14: {
                return "IOS (iPad)";
            }
            case 13: {
                return "IOS (iPhone)";
            }
        }
        return "";
    }

    protected String getName(int b) {
        switch (b) {
            case 0: {
                return "IE";
            }
            case 1: {
                return "Chrome";
            }
            case 2: {
                return "FireFox";
            }
            case 3: {
                return "Opera";
            }
            case 4: {
                return "Safari";
            }
            case 6: {
                return "Android";
            }
            case 7: {
                return "SkyFire";
            }
            case 8: {
                return "BlackBerry";
            }
            case 9: {
                return "EDGE";
            }
        }
        return "";
    }

    public String getName() {
        return this.getName(this.browser);
    }

    public boolean isIE() {
        return this.browser == 0;
    }

    public boolean isEdge() {
        return this.browser == 9;
    }

    public boolean isChrome() {
        return this.browser == 1;
    }

    public boolean isFireFox() {
        return this.browser == 2;
    }

    public boolean isSafari() {
        return this.browser == 4;
    }

    public boolean isOpera() {
        return this.browser == 3;
    }

    public boolean isMozilla() {
        return this.getMozillaBased();
    }

    public boolean isChromium() {
        return this.getChromiumBased();
    }

    public boolean isWebkit() {
        return this.getWebkitBased();
    }

    public GUIMode getGUIMode() {
        if (this.guiMode == null) {
            GUIMode testDesktop = new GUIMode("desktop", "Desktop", -1, -1, false, null, null, "", true, "FlatBlackDesktopComputer", false, false, false, true, true, "", "", "", "");
            return testDesktop;
        }
        return this.guiMode;
    }

    public void setGUIMode(String guimode, HttpServletRequest request) {
        ArrayList<GUIMode> guiModes = this.getGUIModes();
        GUIMode g = null;
        for (int i = 0; i < guiModes.size(); ++i) {
            if (!guiModes.get(i).id.equalsIgnoreCase(guimode)) continue;
            g = guiModes.get(i);
        }
        if (g != null) {
            this.setGUIMode(g, request);
        }
    }

    public void setGUIMode(GUIMode guimode, HttpServletRequest request) {
        this.guiMode = guimode;
        if (request != null) {
            this.saveCache(request.getSession());
        }
    }

    public boolean isMobile() {
        if (this.guiMode != null) {
            return this.guiMode.isMobile();
        }
        return false;
    }

    public boolean isTablet() {
        if (this.guiMode != null) {
            return this.guiMode.isTablet();
        }
        return false;
    }

    public boolean isPhone() {
        if (this.guiMode != null) {
            return this.guiMode.isPhone();
        }
        return false;
    }

    public double getWebkit() {
        return this.webkitVersion;
    }

    public double getMozilla() {
        return this.mozillaVersion;
    }

    public int getBrowser() {
        return this.browser;
    }

    public boolean getMozillaBased() {
        return this.mozillaBased;
    }

    public boolean getChromiumBased() {
        return this.chromiumBased;
    }

    public boolean getWebkitBased() {
        return this.webkitBased;
    }

    public double getVersion() {
        return this.version;
    }

    public int getOS() {
        return this.oS;
    }

    public String getBlankSrc() {
        if (this.isChrome()) {
            return "about:blank";
        }
        return "WEB-CORE/blank.html";
    }

    public static class GUIMode
    implements Serializable {
        private String startupUrl = "";
        private String startupMenu = "";
        private String startupGroup = "";
        private int maxwidth = -1;
        private int maxheight = -1;
        private boolean autodetect = false;
        private boolean hastouch = false;
        private boolean adaptfortouch = true;
        private String id = "";
        private String title = "";
        private int[] oS = null;
        private String userAgentMatch = "";
        private int[] browser = null;
        private String imageRef = "";
        private boolean sidebar = true;
        private boolean navigationbar = true;
        private String navigationbarMode = "";
        private boolean mobile = false;
        private boolean tablet = false;
        private boolean phone = false;

        public int getMaxwidth() {
            return this.maxwidth;
        }

        public int getMaxheight() {
            return this.maxheight;
        }

        public boolean isAutodetect() {
            return this.autodetect;
        }

        public String getUserAgentMatch() {
            return this.userAgentMatch;
        }

        public boolean hastouch() {
            return this.hastouch;
        }

        public boolean adaptForTouch() {
            return this.adaptfortouch;
        }

        public String getId() {
            return this.id;
        }

        public boolean getSidebar() {
            return this.sidebar;
        }

        public boolean getNavigationBar() {
            return this.navigationbar;
        }

        public String getNavigationBarMode() {
            return this.navigationbarMode;
        }

        public String getTitle() {
            return this.title;
        }

        public int[] getoS() {
            return this.oS;
        }

        public int[] getBrowser() {
            return this.browser;
        }

        public String getImageRef() {
            return this.imageRef;
        }

        public boolean isTablet() {
            return this.tablet;
        }

        public boolean isMobile() {
            return this.mobile;
        }

        public boolean isPhone() {
            return this.mobile;
        }

        public String getStartupUrl() {
            return this.startupUrl;
        }

        public String getStartupGroupGizmo() {
            return this.startupGroup;
        }

        public String getStartupMenuGizmo() {
            return this.startupMenu;
        }

        public GUIMode(String id, String title, int maxwidth, int maxheight, boolean hastouch, int[] oS, int[] browser, String userAgentMatch, boolean autodetect, String imageRef, boolean isMobile, boolean isTablet, boolean isPhone, boolean sidebar, boolean navigationbar, String navigationbarMode, String startupUrl, String startupGroup, String startupMenu) {
            this.id = id;
            this.title = title;
            this.userAgentMatch = userAgentMatch;
            this.maxwidth = maxwidth;
            this.maxheight = maxheight;
            this.hastouch = hastouch;
            this.oS = oS;
            this.browser = browser;
            this.imageRef = imageRef;
            this.mobile = isMobile;
            this.tablet = isTablet;
            this.phone = isPhone;
            this.autodetect = autodetect;
            this.sidebar = sidebar;
            this.navigationbar = navigationbar;
            this.navigationbarMode = navigationbarMode;
            this.startupGroup = startupGroup;
            this.startupMenu = startupMenu;
            this.startupUrl = startupUrl;
        }

        public GUIMode(String id, String title, int maxwidth, int maxheight, boolean hastouch, boolean adaptfortouch, int[] oS, int[] browser, String userAgentMatch, boolean autodetect, String imageRef, boolean isMobile, boolean isTablet, boolean isPhone, boolean sidebar, boolean navigationbar, String navigationbarMode, String startupUrl, String startupGroup, String startupMenu) {
            this.id = id;
            this.title = title;
            this.userAgentMatch = userAgentMatch;
            this.maxwidth = maxwidth;
            this.maxheight = maxheight;
            this.hastouch = hastouch;
            this.adaptfortouch = adaptfortouch;
            this.oS = oS;
            this.browser = browser;
            this.imageRef = imageRef;
            this.mobile = isMobile;
            this.tablet = isTablet;
            this.phone = isPhone;
            this.autodetect = autodetect;
            this.sidebar = sidebar;
            this.navigationbar = navigationbar;
            this.navigationbarMode = navigationbarMode;
            this.startupGroup = startupGroup;
            this.startupMenu = startupMenu;
            this.startupUrl = startupUrl;
        }
    }

    public class ViewPort
    implements Serializable {
        private boolean touch = false;
        private boolean portrait = false;
        private int dotsPerInchX = 0;
        private int dotsPerInchY = 0;
        private int viewportWidth = 0;
        private int viewportHeight = 0;
        private int deviceWidth = 0;
        private int deviceHeight = 0;
        private int devicePixelRatio = 0;
        private String userLanguage = "";
        private boolean embedded = false;

        private void processViewPort(JSONObject viewportdata) {
            if (viewportdata != null) {
                try {
                    this.touch = viewportdata.has("touch") ? viewportdata.getBoolean("touch") : false;
                    this.dotsPerInchX = viewportdata.has("dpix") ? viewportdata.getInt("dpix") : 0;
                    this.dotsPerInchY = viewportdata.has("dpiy") ? viewportdata.getInt("dpiy") : 0;
                    this.viewportWidth = viewportdata.has("vw") ? viewportdata.getInt("vw") : 0;
                    this.viewportHeight = viewportdata.has("vh") ? viewportdata.getInt("vh") : 0;
                    this.deviceWidth = viewportdata.has("dw") ? viewportdata.getInt("dw") : 0;
                    this.deviceHeight = viewportdata.has("dh") ? viewportdata.getInt("dh") : 0;
                    this.devicePixelRatio = viewportdata.has("dpr") ? viewportdata.getInt("dpr") : 0;
                    this.userLanguage = viewportdata.has("language") ? viewportdata.getString("language") : "";
                    this.embedded = viewportdata.has("embedded") ? viewportdata.getBoolean("embedded") : false;
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }

        ViewPort(JSONObject viewportdata) {
            this.processViewPort(viewportdata);
        }

        ViewPort(String viewportdata) {
            try {
                JSONObject viewportdataob = new JSONObject(viewportdata);
                this.processViewPort(viewportdataob);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }

        public boolean isEmbedded() {
            return this.embedded;
        }

        public boolean hasTouch() {
            return this.touch;
        }

        public boolean isPortrait() {
            int dw = this.deviceWidth;
            int dh = this.deviceHeight;
            return dw < dh;
        }

        public int getDotsPerInchX() {
            return this.dotsPerInchX;
        }

        public int getDotsPerInchY() {
            return this.dotsPerInchY;
        }

        public int getViewportWidth() {
            return this.viewportWidth;
        }

        public int getViewportHeight() {
            return this.viewportHeight;
        }

        public int getDeviceWidth() {
            return this.deviceWidth;
        }

        public int getDeviceHeight() {
            return this.deviceHeight;
        }

        public int getDevicePixelRatio() {
            return this.devicePixelRatio;
        }

        public String getLanguage() {
            return this.userLanguage;
        }
    }
}

