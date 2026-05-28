/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jfree.chart.title.Title
 *  org.jfree.ui.HorizontalAlignment
 *  org.jfree.ui.RectangleEdge
 *  org.jfree.ui.VerticalAlignment
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.ChartConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.LegendTitleConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.TextTitleConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.BooleanExpression;
import java.io.Serializable;
import org.jfree.chart.title.Title;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.VerticalAlignment;
import sapphire.xml.PropertyList;

public class TitleConfiguration
implements Serializable {
    private static final String DEFAULT_VISIBLE = "Y";
    private static final String DEFAULT_HORIZONTAL_ALIGNMENT = HorizontalAlignmentType.fromHorizontalAlignment(Title.DEFAULT_HORIZONTAL_ALIGNMENT).getName();
    private static final String DEFAULT_VERTICAL_ALIGNMENT = VerticalAlignmentType.fromVerticalAlignment(Title.DEFAULT_VERTICAL_ALIGNMENT).getName();
    private static final String DEFAULT_PADDING_TOP = String.valueOf(Title.DEFAULT_PADDING.getTop());
    private static final String DEFAULT_PADDING_LEFT = String.valueOf(Title.DEFAULT_PADDING.getLeft());
    private static final String DEFAULT_PADDING_BOTTOM = String.valueOf(Title.DEFAULT_PADDING.getBottom());
    private static final String DEFAULT_PADDING_RIGHT = String.valueOf(Title.DEFAULT_PADDING.getRight());
    private static final String DEFAULT_TITLE_ID = "";
    private final ChartConfiguration parent;
    private final BooleanExpression visible;
    private final TitleType titleType;
    private final TextTitleConfiguration textTitleConf;
    private final LegendTitleConfiguration legendTitleConf;
    private final HorizontalAlignment horizontalAlignment;
    private final VerticalAlignment verticalAlignment;
    private final RectangleEdge position;
    private final double paddingTop;
    private final double paddingLeft;
    private final double paddingBottom;
    private final double paddingRight;
    private final boolean showBorder;
    private final String titleId;
    private Integer index;
    private final boolean honorRTLmode;

    public TitleConfiguration(PropertyList titleProps, boolean chartHonorRTLmode, ChartConfiguration parent) {
        this(titleProps, TitleType.TEXT, chartHonorRTLmode, parent);
    }

    public TitleConfiguration(PropertyList titleProps, TitleType defaultTitleType, boolean chartHonorRTLmode, ChartConfiguration parent) {
        this.parent = parent;
        if (titleProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        if (defaultTitleType == null) {
            throw new IllegalArgumentException("Default title type is null");
        }
        String defaultShowBorder = "N";
        String defaultPosition = RectangleEdgeType.fromRectangleEdge(Title.DEFAULT_POSITION).getName();
        this.visible = new BooleanExpression(titleProps.getProperty("visible", DEFAULT_VISIBLE));
        this.titleType = TitleType.fromString(titleProps.getProperty("titletype", defaultTitleType.getName()));
        this.textTitleConf = this.titleType == TitleType.TEXT ? new TextTitleConfiguration(titleProps.getPropertyListNotNull("texttitleprops"), this) : null;
        if (this.titleType == TitleType.LEGEND) {
            defaultPosition = RectangleEdgeType.BOTTOM.getName();
            defaultShowBorder = DEFAULT_VISIBLE;
            this.legendTitleConf = new LegendTitleConfiguration(titleProps.getPropertyListNotNull("legendtitleprops"), this);
        } else {
            this.legendTitleConf = null;
        }
        this.titleId = titleProps.getProperty("titleid", DEFAULT_TITLE_ID);
        this.horizontalAlignment = HorizontalAlignmentType.fromString(titleProps.getProperty("horizontalalignment", DEFAULT_HORIZONTAL_ALIGNMENT)).getHorizontalAlignment();
        this.verticalAlignment = VerticalAlignmentType.fromString(titleProps.getProperty("verticalalignment", DEFAULT_VERTICAL_ALIGNMENT)).getVerticalAlignment();
        this.position = RectangleEdgeType.fromString(titleProps.getProperty("position", defaultPosition)).getRectangleEdge();
        this.paddingTop = Double.parseDouble(titleProps.getProperty("paddingtop", DEFAULT_PADDING_TOP));
        this.paddingLeft = Double.parseDouble(titleProps.getProperty("paddingleft", DEFAULT_PADDING_LEFT));
        this.paddingBottom = Double.parseDouble(titleProps.getProperty("paddingbottom", DEFAULT_PADDING_BOTTOM));
        this.paddingRight = Double.parseDouble(titleProps.getProperty("paddingright", DEFAULT_PADDING_RIGHT));
        this.showBorder = titleProps.getProperty("showborder", defaultShowBorder).toLowerCase().startsWith("y");
        this.honorRTLmode = chartHonorRTLmode;
        this.index = null;
    }

    public String getTitleId() {
        return this.titleId;
    }

    public boolean showBorder() {
        return this.showBorder;
    }

    public HorizontalAlignment getHorizontalAlignment() {
        return this.horizontalAlignment;
    }

    public VerticalAlignment getVerticalAlignment() {
        return this.verticalAlignment;
    }

    public RectangleEdge getPosition() {
        return this.position;
    }

    public double getPaddingTop() {
        return this.paddingTop;
    }

    public double getPaddingLeft() {
        return this.paddingLeft;
    }

    public double getPaddingBottom() {
        return this.paddingBottom;
    }

    public double getPaddingRight() {
        return this.paddingRight;
    }

    public BooleanExpression getVisible() {
        return this.visible;
    }

    public LegendTitleConfiguration getLegendTitleConfiguration() {
        if (this.legendTitleConf == null) {
            throw new IllegalStateException("Title type is: " + (Object)((Object)this.titleType));
        }
        return this.legendTitleConf;
    }

    public TextTitleConfiguration getTextTitleConfiguration() {
        if (this.textTitleConf == null) {
            throw new IllegalStateException("Title type is: " + (Object)((Object)this.titleType));
        }
        return this.textTitleConf;
    }

    public boolean hasIndex() {
        return this.index != null;
    }

    public int getIndex() {
        if (this.index == null) {
            throw new IllegalStateException("Index is not set");
        }
        return this.index;
    }

    public void setIndex(int index) {
        if (index < 0) {
            throw new IllegalArgumentException("Index is negative: " + index);
        }
        this.index = index;
    }

    public boolean getHonorRTLmode() {
        return this.honorRTLmode;
    }

    public TitleType getTitleType() {
        return this.titleType;
    }

    public ChartConfiguration getParent() {
        return this.parent;
    }

    public static enum RectangleEdgeType {
        BOTTOM("Bottom", RectangleEdge.BOTTOM),
        LEFT("Left", RectangleEdge.LEFT),
        TOP("Top", RectangleEdge.TOP),
        RIGHT("Right", RectangleEdge.RIGHT);

        private final String name;
        private final RectangleEdge rectangleEdge;

        private RectangleEdgeType(String name, RectangleEdge rectangleEdge) {
            this.name = name;
            this.rectangleEdge = rectangleEdge;
        }

        public static RectangleEdgeType fromString(String name) {
            if (name != null) {
                for (RectangleEdgeType type : RectangleEdgeType.values()) {
                    if (!name.equalsIgnoreCase(type.name)) continue;
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown name: " + name);
        }

        public static RectangleEdgeType fromRectangleEdge(RectangleEdge rectangleEdge) {
            if (rectangleEdge != null) {
                for (RectangleEdgeType type : RectangleEdgeType.values()) {
                    if (rectangleEdge != type.rectangleEdge) continue;
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown rectangle edge: " + rectangleEdge);
        }

        public String getName() {
            return this.name;
        }

        public RectangleEdge getRectangleEdge() {
            return this.rectangleEdge;
        }
    }

    public static enum VerticalAlignmentType {
        CENTER("Center", VerticalAlignment.CENTER),
        BOTTOM("Bottom", VerticalAlignment.BOTTOM),
        TOP("Top", VerticalAlignment.TOP);

        private final String name;
        private final VerticalAlignment verticalAlignment;

        private VerticalAlignmentType(String name, VerticalAlignment verticalAlignment) {
            this.name = name;
            this.verticalAlignment = verticalAlignment;
        }

        public static VerticalAlignmentType fromString(String name) {
            if (name != null) {
                for (VerticalAlignmentType type : VerticalAlignmentType.values()) {
                    if (!name.equalsIgnoreCase(type.name)) continue;
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown name: " + name);
        }

        public static VerticalAlignmentType fromVerticalAlignment(VerticalAlignment verticalAlignment) {
            if (verticalAlignment != null) {
                for (VerticalAlignmentType type : VerticalAlignmentType.values()) {
                    if (verticalAlignment != type.verticalAlignment) continue;
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown vertical alignment: " + verticalAlignment);
        }

        public String getName() {
            return this.name;
        }

        public VerticalAlignment getVerticalAlignment() {
            return this.verticalAlignment;
        }
    }

    public static enum HorizontalAlignmentType {
        CENTER("Center", HorizontalAlignment.CENTER),
        LEFT("Left", HorizontalAlignment.LEFT),
        RIGHT("Right", HorizontalAlignment.RIGHT);

        private final String name;
        private final HorizontalAlignment horizontalAlignment;

        private HorizontalAlignmentType(String name, HorizontalAlignment horizontalAlignment) {
            this.name = name;
            this.horizontalAlignment = horizontalAlignment;
        }

        public static HorizontalAlignmentType fromString(String name) {
            if (name != null) {
                for (HorizontalAlignmentType type : HorizontalAlignmentType.values()) {
                    if (!name.equalsIgnoreCase(type.name)) continue;
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown name: " + name);
        }

        public static HorizontalAlignmentType fromHorizontalAlignment(HorizontalAlignment horizontalAlignment) {
            if (horizontalAlignment != null) {
                for (HorizontalAlignmentType type : HorizontalAlignmentType.values()) {
                    if (horizontalAlignment != type.horizontalAlignment) continue;
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown horizontal alignment: " + horizontalAlignment);
        }

        public String getName() {
            return this.name;
        }

        public HorizontalAlignment getHorizontalAlignment() {
            return this.horizontalAlignment;
        }
    }

    public static enum TitleType {
        TEXT("Text"),
        LEGEND("Legend");

        private final String name;

        private TitleType(String name) {
            this.name = name;
        }

        public static TitleType fromString(String name) {
            if (name != null) {
                for (TitleType type : TitleType.values()) {
                    if (!name.equalsIgnoreCase(type.name)) continue;
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown name: " + name);
        }

        public String getName() {
            return this.name;
        }
    }
}

