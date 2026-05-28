/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  org.jfree.chart.encoders.ImageEncoder
 *  org.jfree.chart.encoders.ImageEncoderFactory
 */
package com.labvantage.sapphire.pageelements.workflow.workflowdefpainter;

import com.labvantage.sapphire.pageelements.workflow.taskdefpainter.TaskDefMaint;
import com.labvantage.sapphire.pageelements.workflow.taskdefpainter.TaskDefWorkflow;
import com.labvantage.sapphire.pageelements.workflow.workflowdefpainter.WorkflowDefMaint;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.servlet.command.BaseRequest;
import com.labvantage.sapphire.util.file.FileManager;
import com.labvantage.sapphire.util.images.ConnectorLine;
import com.labvantage.sapphire.util.images.ImageRef;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jfree.chart.encoders.ImageEncoder;
import org.jfree.chart.encoders.ImageEncoderFactory;
import org.json.JSONObject;
import sapphire.accessor.SDIProcessor;
import sapphire.util.Logger;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class WorkflowDefImageCreator
extends BaseRequest {
    static final int CANVASWIDTH = 5000;
    static final int CANVASHEIGHT = 5000;
    static final int TASKWIDTH = 150;
    static final int TASKHEIGHT = 60;
    static final int TASKFULLWIDTH = 250;
    static final int TASKFULLHEIGHT = 110;
    static final int STEPWIDTH = 125;
    static final int STEPHEIGHT = 45;
    private BufferedImage graph;
    private PropertyList props;
    private ImageType imageType = ImageType.PNG;
    private RenderType type;
    private int xMax = 0;
    private int yMax = 0;
    private String serverURL = "";

    private Color findColor(String color) {
        if (color.startsWith("#") && color.length() == 7) {
            return new Color(Integer.valueOf(color.substring(1, 3), 16), Integer.valueOf(color.substring(3, 5), 16), Integer.valueOf(color.substring(5, 7), 16));
        }
        try {
            return Color.decode(color);
        }
        catch (Exception e) {
            return Color.BLACK;
        }
    }

    private void setServerURL(String serverURL) {
        this.serverURL = serverURL;
    }

    private BufferedImage getBufferedImage(String imageSrc, int size, ServletContext servletContext) throws IOException {
        Object image = null;
        if (servletContext != null) {
            ImageRef imageRef = new ImageRef(this.getConnectionProcessor().getSapphireConnection());
            imageRef.setURL(imageSrc);
            imageRef.setDimensions(size, size);
            ByteArrayInputStream in = new ByteArrayInputStream(imageRef.getBytes(servletContext));
            return ImageIO.read(in);
        }
        if (this.serverURL != null && this.serverURL.length() > 0) {
            URL url = new URL(this.serverURL + (this.serverURL.endsWith("/") ? "" : "/") + imageSrc);
            BufferedImage i = null;
            try {
                i = ImageIO.read(url);
            }
            catch (Exception e) {
                this.logger.error(e.getMessage(), e);
            }
            return i;
        }
        return null;
    }

    private void renderTask(Graphics g, PropertyList taskProps, ServletContext context) {
        int isizeO = 14;
        int x = 25;
        int y = 25;
        this.xMax = x + 250 + isizeO + 10;
        this.yMax = y + 110 + isizeO + 10;
        String colorStr = taskProps.getProperty("taskcolor1", "#e7f2ff");
        Color c = this.findColor(colorStr);
        g.setColor(Color.BLACK);
        g.drawRect(x, y, 250, 110);
        g.setColor(c);
        g.fillRect(x + 1, y + 1, 249, 109);
        try {
            String im = taskProps.getProperty("icon", "WEB-CORE/elements/workflow/images/NoImage16.png");
            BufferedImage image = this.getBufferedImage(im, 48, context);
            if (image != null) {
                g.drawImage(image, x + 101, y + 10, null);
            }
        }
        catch (Exception im) {
            // empty catch block
        }
        String text = taskProps.getProperty("shorttitle", taskProps.getProperty("taskdefid"));
        g.setFont(new Font("Arial", 0, 16));
        int stringLen = (int)g.getFontMetrics().getStringBounds(text, g).getWidth();
        int start = 125 - stringLen / 2;
        g.setColor(Color.BLACK);
        g.drawString(text, start + x + 2, y + 95);
        PropertyListCollection connectors = TaskDefWorkflow.getConnectors(this.getSDIProcessor());
        if (connectors != null) {
            int total;
            PropertyListCollection ios = taskProps.getCollectionNotNull("taskio");
            ArrayList<Color> inputs = new ArrayList<Color>();
            ArrayList<Color> outputs = new ArrayList<Color>();
            for (int i = 0; i < ios.size(); ++i) {
                PropertyList io = ios.getPropertyList(i);
                String connector = io.getProperty("connectortypeid");
                PropertyList conn = connectors.find("connectortypeid", connector);
                if (conn == null) continue;
                String col = conn.getProperty("color");
                Color c2 = this.findColor(col);
                if (io.getProperty("ioflag", "I").equalsIgnoreCase("O")) {
                    outputs.add(c2);
                    continue;
                }
                inputs.add(c2);
            }
            if (inputs.size() > 0) {
                total = inputs.size() * isizeO + (inputs.size() - 2);
                int startY = y + (55 - total / 2);
                int startX = x - isizeO / 2;
                for (int k = 0; k < inputs.size(); ++k) {
                    g.setColor((Color)inputs.get(k));
                    g.fillOval(startX, startY, isizeO, isizeO);
                    startY += isizeO + 2;
                }
            }
            if (outputs.size() > 0) {
                total = outputs.size() * (isizeO + 2);
                int startY = y + (55 - total / 2);
                int startX = x + 250 - isizeO / 2;
                for (int k = 0; k < outputs.size(); ++k) {
                    g.setColor((Color)outputs.get(k));
                    g.fillOval(startX, startY, isizeO, isizeO);
                    startY += isizeO + 2;
                }
            }
        }
    }

    private void renderSteps(Graphics g, PropertyList taskProps, ServletContext context) {
        PropertyListCollection steps = taskProps.getCollection("steps");
        if (steps != null) {
            PropertyList step;
            int t;
            String startTask = taskProps.getProperty("startstepid", "");
            int startx = 0;
            int starty = 0;
            for (t = 0; t < steps.size(); ++t) {
                step = steps.getPropertyList(t);
                int x = 0;
                int y = 0;
                try {
                    x = (int)Math.round(Double.parseDouble(step.getProperty("x")));
                    y = (int)Math.round(Double.parseDouble(step.getProperty("y")));
                }
                catch (NumberFormatException e) {
                    Logger.logWarn("Could not obtain coordinates.");
                }
                if (x > this.xMax) {
                    this.xMax = x + 125;
                }
                if (y > this.yMax) {
                    this.yMax = y + 45;
                }
                if (startx == 0) {
                    startx = x;
                }
                if (starty == 0) {
                    starty = y;
                }
                String colorStr = startTask.equalsIgnoreCase(step.getProperty("stepid")) ? "#ECFDD3" : "#EEF3FA";
                Color c = this.findColor(colorStr);
                g.setColor(Color.BLACK);
                g.drawRect(x, y, 125, 45);
                g.setColor(c);
                g.fillRect(x + 1, y + 1, 124, 44);
                try {
                    String im = step.getProperty("icon", "WEB-CORE/elements/workflow/images/NoImage16.png");
                    BufferedImage image = this.getBufferedImage(im, 16, context);
                    if (image != null) {
                        g.drawImage(image, x + 54, y + 5, null);
                    }
                }
                catch (Exception im) {
                    // empty catch block
                }
                String text = step.getProperty("shorttitle", step.getProperty("title", step.getProperty("stepid")));
                g.setFont(new Font("Arial", 0, 12));
                int stringLen = (int)g.getFontMetrics().getStringBounds(text, g).getWidth();
                int start = 62 - stringLen / 2;
                g.setColor(Color.BLACK);
                g.drawString(text, start + x + 2, y + 35);
            }
            if (startx > 0) {
                this.xMax += startx;
            }
            if (starty > 0) {
                this.yMax += starty;
            }
            for (t = 0; t < steps.size(); ++t) {
                PropertyListCollection transitions;
                step = steps.getPropertyList(t);
                PropertyList next = step.getPropertyList("next");
                String fid = step.getProperty("stepid");
                PropertyListCollection propertyListCollection = transitions = next != null ? next.getCollection("transitions") : null;
                if (transitions == null) continue;
                for (int tr = 0; tr < transitions.size(); ++tr) {
                    PropertyList trans = transitions.getPropertyList(tr);
                    String tid = trans.getProperty("stepid");
                    PropertyList sp = steps.find("stepid", tid);
                    if (sp == null) continue;
                    int x1 = 0;
                    int y1 = 0;
                    int x2 = 0;
                    int y2 = 0;
                    try {
                        x1 = (int)Math.round(Double.parseDouble(step.getProperty("x")));
                        y1 = (int)Math.round(Double.parseDouble(step.getProperty("y")));
                        x2 = (int)Math.round(Double.parseDouble(sp.getProperty("x")));
                        y2 = (int)Math.round(Double.parseDouble(sp.getProperty("y")));
                    }
                    catch (NumberFormatException e) {
                        Logger.logWarn("Could not obtain coordinates.");
                    }
                    Rectangle from = new Rectangle(x1, y1, 125, 45);
                    Rectangle to = new Rectangle(x2, y2, 125, 45);
                    ConnectorLine.draw(from, to, Color.black, ConnectorLine.LineArrow.DESTINATION, trans.getProperty("text", trans.getProperty("case")), (Graphics2D)g);
                }
            }
        }
    }

    private void renderWorkflow(Graphics g, PropertyList workflowProps, ServletContext context) {
        PropertyListCollection tasks = workflowProps.getCollection("tasks");
        if (tasks != null) {
            PropertyList task;
            int t;
            for (t = 0; t < tasks.size(); ++t) {
                FileManager.FileData data;
                String b64;
                BufferedImage image;
                String im3;
                task = tasks.getPropertyList(t);
                int x = 0;
                int y = 0;
                try {
                    x = (int)Math.round(Double.parseDouble(task.getProperty("x")));
                    y = (int)Math.round(Double.parseDouble(task.getProperty("y")));
                }
                catch (NumberFormatException e) {
                    Logger.logWarn("Could not obtain coordinates.");
                }
                if (x + 150 > this.xMax) {
                    this.xMax = x + 150;
                }
                if (y + 60 > this.yMax) {
                    this.yMax = y + 60;
                }
                String colorStr = task.getProperty("taskcolor1", "#e7f2ff");
                Color c = this.findColor(colorStr);
                g.setColor(Color.BLACK);
                g.drawRect(x, y, 150, 60);
                g.setColor(c);
                g.fillRect(x + 1, y + 1, 149, 59);
                try {
                    String im2 = task.getProperty("icon", "WEB-CORE/elements/workflow/images/NoImage16.png");
                    BufferedImage image2 = this.getBufferedImage(im2, 32, context);
                    if (image2 != null) {
                        g.drawImage(image2, x + 59, y + 5, null);
                    }
                }
                catch (Exception im2) {
                    // empty catch block
                }
                String text = task.getProperty("shorttitle", task.getProperty("taskdefid"));
                g.setFont(new Font("Arial", 0, 12));
                int stringLen = (int)g.getFontMetrics().getStringBounds(text, g).getWidth();
                int start = 75 - stringLen / 2;
                g.setColor(Color.BLACK);
                g.drawString(text, start + x + 2, y + 50);
                if (task.getProperty("starttaskflag").equalsIgnoreCase("Y")) {
                    try {
                        im3 = "rc?command=image&image=BulletTriangleGreen";
                        image = this.getBufferedImage(im3, 16, context);
                        if (image != null) {
                            g.drawImage(image, x, y + 60 - 5, null);
                            continue;
                        }
                        b64 = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAABoElEQVR42mMYXMCz1Dzcs8zyvUehxQOvCqtAYvQwIXN+//s7pbqlVKCiuVReQJx3rVORwUrnKnVRol3gXGXw/9z/3f+f/7/2/wKQrj2c+t86T+OVbZlGJFEu+P73K8MrhocMzxgeM/xm+MXgZOPIENcSJMonzrPUJFt+g06RmCReA37+/sHwnOEBw32Gqwy3GS4BjbnDwMzLwOBWZMaoHSLvz/KD7apetoQ3sh4WZM6fX38Y3n57zfCe8R3Dr/8/GX7//cnwB+iWv39/MUgZCjDwyKgLHui4OhOoVAa7AT/+M7x7847h359/UAOAmv+B8F+wW399/8Pw7+8/Ntwu+P2H4c3rtww/v/8C2/733x+G/4z/GZjZmBne3vzC8Gjfm6+MzMxZOGNBJVz4f8hO/f+eq1T/u6xQ+O+xVvm/+2q1/7q54v+VggV3KUQLKOGNRoUA/v+OS+X/W04X/2+7QOq/cb34fwVv/rdKoYJJRKUDaVeu18bdov8NO4T+K4bx/pNy414rHswtQXRCEnfgCBd35Hgvbsf+UMqZM4hhSAAApPqtupSP1KIAAAArdEVYdENvcHlyaWdodABDb3B5cmlnaHQgqSAyMDA4LTIwMTEgSU5DT1JTIEdtYkgQjiFPAAAAAElFTkSuQmCC\n";
                        data = new FileManager.FileData(b64);
                        image = ImageIO.read(data.getInputStream());
                        g.drawImage(image, x, y + 60 - 5, null);
                    }
                    catch (Exception im3) {}
                    continue;
                }
                if (!task.getProperty("endtaskflag").equalsIgnoreCase("Y")) continue;
                try {
                    try {
                        im3 = "rc?command=image&image=BulletSquareRed";
                        image = this.getBufferedImage(im3, 16, context);
                        if (image != null) {
                            g.drawImage(image, x, y + 60 - 5, null);
                            continue;
                        }
                        b64 = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAA9ElEQVR42t3RMU7DMBjF8b8jewiN1AkOgrgUFyCwIJBQkVg5BxdBYqgYYS1VE6WhIXbixKnZW6dLmfjmTz/pvcexJ48FxJ8ClxfnD8lkkiqlJN7vfAp617vWmMfn17fbYIQEkT6lqeTsFLbbHSCCLJNXd/fXQBhQQkiKAuJ4H5ARlCUK1HiJwwAfnzB/x7OXAOITcMOBFaylX62ovzeBtgTJdIqwdhyIrKXOc4p1GW67s3AIsE1LtliyrjbByVzb0jXNOKC1ZrH8otI6CDRao40ZB36Mcbkx0joXBGxdU3nfjwKm62YvcCNB+UAHztrew4z/db9+SHHwinFCEQAAACt0RVh0Q29weXJpZ2h0AENvcHlyaWdodCCpIDIwMDgtMjAxMSBJTkNPUlMgR21iSBCOIU8AAAAASUVORK5CYII=";
                        data = new FileManager.FileData(b64);
                        image = ImageIO.read(data.getInputStream());
                        g.drawImage(image, x, y + 60 - 5, null);
                    }
                    catch (Exception im4) {}
                    continue;
                }
                catch (Exception im4) {
                    // empty catch block
                }
            }
            for (t = 0; t < tasks.size(); ++t) {
                task = tasks.getPropertyList(t);
                PropertyListCollection ios = task.getCollection("taskio");
                for (int i = 0; i < ios.size(); ++i) {
                    PropertyList io = ios.getPropertyList(i);
                    if (!io.getProperty("ioflag", "I").equalsIgnoreCase("O")) continue;
                    String fid = task.getProperty("taskdefitemid");
                    String tid = io.getProperty("connecttaskdefitemid");
                    PropertyList tp = tasks.find("taskdefitemid", tid);
                    if (tp == null) continue;
                    int x1 = 0;
                    int y1 = 0;
                    int x2 = 0;
                    int y2 = 0;
                    try {
                        x1 = (int)Math.round(Double.parseDouble(task.getProperty("x")));
                        y1 = (int)Math.round(Double.parseDouble(task.getProperty("y")));
                        x2 = (int)Math.round(Double.parseDouble(tp.getProperty("x")));
                        y2 = (int)Math.round(Double.parseDouble(tp.getProperty("y")));
                    }
                    catch (NumberFormatException e) {
                        Logger.logWarn("Could not obtain coordinates.");
                    }
                    Rectangle from = new Rectangle(x1, y1, 150, 60);
                    Rectangle to = new Rectangle(x2, y2, 150, 60);
                    ConnectorLine.draw(from, to, Color.black, ConnectorLine.LineArrow.DESTINATION, io.getProperty("connectortypeid"), (Graphics2D)g);
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Unable to fully structure code
     */
    private BufferedImage createGraph(PropertyList props, RenderType renderType, ServletContext context) {
        this.xMax = 0;
        this.yMax = 0;
        temp = new BufferedImage(5000, 5000, 1);
        g = temp.createGraphics();
        g.setBackground(Color.white);
        g.setColor(Color.white);
        g.fillRect(0, 0, 5000, 5000);
        try {
            switch (1.$SwitchMap$com$labvantage$sapphire$pageelements$workflow$workflowdefpainter$WorkflowDefImageCreator$RenderType[renderType.ordinal()]) {
                case 1: {
                    this.renderTask(g, props, context);
                    ** break;
lbl13:
                    // 1 sources

                    break;
                }
                case 2: {
                    this.renderSteps(g, props, context);
                    ** break;
lbl17:
                    // 1 sources

                    break;
                }
                default: {
                    this.renderWorkflow(g, props, context);
                    break;
                }
            }
        }
        finally {
            g.dispose();
        }
        this.graph = new BufferedImage(this.xMax + 10, this.yMax + 10, 1);
        g2 = this.graph.getGraphics();
        try {
            g2.drawImage(temp, 0, 0, null);
        }
        finally {
            g2.dispose();
        }
        return this.graph;
    }

    @Override
    public PropertyList getProperties() {
        return this.props;
    }

    public WorkflowDefImageCreator() {
    }

    public WorkflowDefImageCreator(PropertyList props, RenderType type, ServletContext context) {
        this.props = props;
        this.type = type;
        this.graph = this.createGraph(props, type, context);
    }

    public BufferedImage getImage() {
        return this.graph;
    }

    public void setImageType(ImageType imageType) {
        this.imageType = imageType;
    }

    public void setRenderType(RenderType renderType) {
        this.type = renderType;
    }

    public static int writeImage(String keyid1, String keyid2, String keyid3, ImageType imageType, RenderType renderType, OutputStream outputStream, String serverUrl, SDIProcessor sdiProcessor, SapphireConnection sapphireConnection, Logger logger) {
        return WorkflowDefImageCreator.writeImage(keyid1, keyid2, keyid3, imageType, renderType, outputStream, serverUrl, null, sdiProcessor, sapphireConnection, logger);
    }

    public static int writeImage(String keyid1, String keyid2, String keyid3, ImageType imageType, RenderType renderType, OutputStream outputStream, ServletContext servletContext, SDIProcessor sdiProcessor, SapphireConnection sapphireConnection, Logger logger) {
        return WorkflowDefImageCreator.writeImage(keyid1, keyid2, keyid3, imageType, renderType, outputStream, null, servletContext, sdiProcessor, sapphireConnection, logger);
    }

    private static int writeImage(String keyid1, String keyid2, String keyid3, ImageType imageType, RenderType renderType, OutputStream outputStream, String serverUrl, ServletContext servletContext, SDIProcessor sdiProcessor, SapphireConnection sapphireConnection, Logger logger) {
        if (keyid1.length() > 0 && keyid2.length() > 0) {
            PropertyList props;
            switch (renderType) {
                case TASK: 
                case STEPS: {
                    props = TaskDefMaint.getTaskData("LV_TaskDef", keyid1, keyid2, keyid3, false, sdiProcessor, logger);
                    if (props == null) break;
                    props.setProperty("taskdefid", keyid1 != null ? keyid1 : "");
                    props.setProperty("taskdefversionid", keyid2 != null ? keyid2 : "");
                    props.setProperty("taskdefvariantid", keyid3 != null ? keyid3 : "");
                    break;
                }
                default: {
                    props = WorkflowDefMaint.getWorkflowData("LV_WorkflowDef", keyid1, keyid2, keyid3, false, null, sdiProcessor, sapphireConnection, logger, false, false);
                    if (props == null) break;
                    props.setProperty("workflowdefid", keyid1 != null ? keyid1 : "");
                    props.setProperty("workflowdefversionid", keyid2 != null ? keyid2 : "");
                    props.setProperty("workflowdefvariantid", keyid3 != null ? keyid3 : "");
                }
            }
            WorkflowDefImageCreator workflowDefImageCreator = new WorkflowDefImageCreator();
            workflowDefImageCreator.logger = logger;
            workflowDefImageCreator.props = props;
            workflowDefImageCreator.setRenderType(renderType);
            workflowDefImageCreator.setRenderType(renderType);
            workflowDefImageCreator.setImageType(imageType);
            if (serverUrl != null && serverUrl.length() > 0) {
                workflowDefImageCreator.setServerURL(serverUrl);
            }
            workflowDefImageCreator.setConnectionId(sapphireConnection.getConnectionId());
            return workflowDefImageCreator.writeImage(outputStream, servletContext);
        }
        return 0;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private int writeImage(OutputStream outputStream, ServletContext context) {
        int size = 0;
        String error = "";
        if (this.props != null) {
            try {
                try {
                    this.graph = this.createGraph(this.props, this.type, context);
                    BufferedImage image = this.getImage();
                    try {
                        try {
                            ImageEncoder imageEncoder = ImageEncoderFactory.newInstance((String)(this.imageType == ImageType.PNG ? "png" : "jpeg"));
                            byte[] data = imageEncoder.encode(image);
                            Logger.logDebug("image encoded to byte[]  ");
                            size = data.length;
                            if (size > 0) {
                                outputStream.write(data);
                                Logger.logDebug("data written to output stream");
                            } else {
                                Logger.logWarn("no data stream");
                            }
                        }
                        finally {
                            outputStream.flush();
                        }
                    }
                    catch (IOException ioe) {
                        error = ioe.getMessage();
                    }
                }
                finally {
                    outputStream.close();
                }
            }
            catch (Exception e) {
                error = e.getMessage();
            }
        } else {
            error = "No properties provided.";
        }
        if (error.length() > 0) {
            Logger.logError(error);
        }
        return size;
    }

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext context) {
        try {
            String properties;
            String error = "";
            String keyid1 = request.getParameter("keyid1");
            String keyid2 = request.getParameter("keyid2");
            String keyid3 = request.getParameter("keyid3");
            String imtype = request.getParameter("type");
            String renderType = request.getParameter("render");
            if (imtype != null && imtype.length() > 0) {
                try {
                    this.imageType = ImageType.valueOf(imtype.toUpperCase());
                }
                catch (Exception e) {
                    this.imageType = ImageType.PNG;
                }
            } else {
                this.imageType = ImageType.PNG;
            }
            if (renderType != null && renderType.length() > 0) {
                try {
                    this.type = RenderType.valueOf(renderType.toUpperCase());
                }
                catch (Exception e) {
                    this.type = RenderType.WORKFLOW;
                }
            } else {
                this.type = RenderType.WORKFLOW;
            }
            if ((properties = request.getParameter("properties")) != null && properties.length() > 0) {
                try {
                    this.props = new PropertyList(new JSONObject(properties));
                }
                catch (Exception e) {
                    Logger.logWarn("Could not parse properties.");
                }
            } else {
                switch (this.type) {
                    case TASK: 
                    case STEPS: {
                        this.props = TaskDefMaint.getTaskData("LV_TaskDef", keyid1, keyid2, keyid3, false, this.getSDIProcessor(), this.logger);
                        if (this.props == null) break;
                        this.props.setProperty("taskdefid", keyid1 != null ? keyid1 : "");
                        this.props.setProperty("taskdefversionid", keyid2 != null ? keyid2 : "");
                        this.props.setProperty("taskdefvariantid", keyid3 != null ? keyid3 : "");
                        break;
                    }
                    default: {
                        this.props = WorkflowDefMaint.getWorkflowData("LV_WorkflowDef", keyid1, keyid2, keyid3, false, null, this.getSDIProcessor(), this.getConnectionProcessor().getSapphireConnection(), this.logger, false, false);
                        if (this.props == null) break;
                        this.props.setProperty("workflowdefid", keyid1 != null ? keyid1 : "");
                        this.props.setProperty("workflowdefversionid", keyid2 != null ? keyid2 : "");
                        this.props.setProperty("workflowdefvariantid", keyid3 != null ? keyid3 : "");
                    }
                }
            }
            response.setContentType(this.imageType == ImageType.PNG ? "image/png" : "image/jpeg");
            response.setHeader("Content-Disposition", "inline;filename=" + keyid1 + "(" + keyid2 + "_" + keyid3 + ")." + (this.imageType == ImageType.PNG ? "png" : "jpg") + "");
            int size = this.writeImage((OutputStream)response.getOutputStream(), context);
            response.setContentLength(size);
        }
        catch (Exception e) {
            Logger.logStackTrace(e);
        }
    }

    public static enum RenderType {
        WORKFLOW,
        STEPS,
        TASK;

    }

    public static enum ImageType {
        PNG,
        JPG;

    }
}

