/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.controls;

import com.labvantage.sapphire.util.images.ImageRef;
import java.util.ArrayList;
import javax.servlet.jsp.PageContext;
import sapphire.attachment.Attachment;
import sapphire.pageelements.BaseElement;

public class Image
extends BaseElement {
    private String imageId = "";
    private String imageSrc = "";
    private int width = 0;
    private int height = 0;
    private boolean disabled = false;
    private float opacity = 0.0f;
    private String title = "";
    private String overlay = "";
    private String style = "";
    private String className = "";
    private String tempid = "";
    private boolean attachmentThumbnail = false;
    private Attachment.ThumbnailGeneration attachmentThumbnailGeneration = null;
    private String attachmentKeyid1 = "";
    private String attachmentKeyid2 = "";
    private String attachmentKeyid3 = "";
    private String attachmentSDCId = "";
    private int attachmentNum = 0;
    private String defaultImage = "";
    private ImageRef basedOn = null;
    private boolean renderAllResolutions = false;
    private String color = "";
    private boolean nocache = false;
    private String src16 = "";
    private String src32 = "";
    private String src48 = "";
    private String src = "";

    public Image() {
    }

    public Image(PageContext pageContext) {
        this.setPageContext(pageContext);
    }

    public void setNoCache(boolean noCache) {
        this.nocache = noCache;
    }

    public void setDefault(String defaultImage) {
        this.defaultImage = defaultImage;
    }

    public void setSDITempThumbnail(String sdcid, String keyid1, String keyid2, String keyid3, String tempid) {
        this.attachmentThumbnail = true;
        this.setSDITemp(sdcid, keyid1, keyid2, keyid3, tempid);
    }

    public void setSDITempThumbnail(String sdcid, String keyid1, String keyid2, String keyid3, String tempid, Attachment.ThumbnailGeneration thumbnailGeneration) {
        this.attachmentThumbnail = true;
        this.setSDITemp(sdcid, keyid1, keyid2, keyid3, tempid, thumbnailGeneration);
    }

    public void setSDITemp(String sdcid, String keyid1, String keyid2, String keyid3, String tempid, Attachment.ThumbnailGeneration thumbnailGeneration) {
        this.attachmentThumbnailGeneration = thumbnailGeneration;
        this.setSDITemp(sdcid, keyid1, keyid2, keyid3, tempid);
    }

    public void setSDITemp(String sdcid, String keyid1, String keyid2, String keyid3, String tempid) {
        this.attachmentKeyid1 = keyid1;
        this.attachmentKeyid2 = keyid2;
        this.attachmentKeyid3 = keyid3;
        this.attachmentSDCId = sdcid;
        this.tempid = tempid;
    }

    public void setAttachmentThumbnail(String sdcid, String keyid1, String keyid2, String keyid3, int attachmentNum, Attachment.ThumbnailGeneration thumbnailGeneration) {
        this.attachmentThumbnail = true;
        this.setAttachment(sdcid, keyid1, keyid2, keyid3, attachmentNum, thumbnailGeneration);
    }

    public void setAttachmentThumbnail(String sdcid, String keyid1, String keyid2, String keyid3, int attachmentNum) {
        this.attachmentThumbnail = true;
        this.setAttachment(sdcid, keyid1, keyid2, keyid3, attachmentNum);
    }

    public void setAttachment(String sdcid, String keyid1, String keyid2, String keyid3, int attachmentNum, Attachment.ThumbnailGeneration thumbnailGeneration) {
        this.attachmentThumbnailGeneration = thumbnailGeneration;
        this.setAttachment(sdcid, keyid1, keyid2, keyid3, attachmentNum);
    }

    public void setAttachment(String sdcid, String keyid1, String keyid2, String keyid3, int attachmentNum) {
        this.attachmentNum = attachmentNum;
        this.attachmentKeyid1 = keyid1;
        this.attachmentKeyid2 = keyid2;
        this.attachmentKeyid3 = keyid3;
        this.attachmentSDCId = sdcid;
        this.imageId = "";
        this.imageSrc = "";
    }

    public void setImageRef(ImageRef imageRef) {
        this.imageId = "";
        this.imageSrc = "";
        this.basedOn = imageRef;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
        this.imageSrc = "";
    }

    public void setDimensions(int width, int height) {
        this.setWidth(width);
        this.setHeight(height);
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public void setOpacity(float opacity) {
        this.opacity = opacity;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setImageSrc(String imageSrc) {
        imageSrc = imageSrc.trim();
        String lc = imageSrc.toLowerCase();
        this.imageId = "";
        this.imageSrc = imageSrc;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setOverlay(String overlay) {
        this.overlay = overlay;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setRenderAllResolutions(boolean r) {
        this.renderAllResolutions = r;
    }

    private void generateSource() {
        ImageRef imageRef = null;
        this.src = "";
        this.src16 = "";
        this.src32 = "";
        this.src48 = "";
        if (this.basedOn != null) {
            imageRef = this.basedOn;
        } else if (this.imageSrc != null && this.imageSrc.length() > 0 && (!this.imageSrc.contains("command=image") || this.imageSrc.contains("&svgimage="))) {
            this.src = this.imageSrc;
        } else if (this.getConnectionId() != null) {
            this.getConnectionProcessor().setConnectionid(this.getConnectionId());
            if (this.imageId.length() > 0) {
                imageRef = ImageRef.getImage(this.getConnectionProcessor().getSapphireConnection(), this.imageId);
            } else if (this.imageSrc.length() > 0) {
                imageRef = ImageRef.getURLImage(this.getConnectionProcessor().getSapphireConnection(), this.imageSrc);
            } else if (this.tempid.length() > 0) {
                imageRef = this.attachmentThumbnail ? ImageRef.getSDITempThumbnailImage(this.getConnectionProcessor().getSapphireConnection(), this.attachmentSDCId, this.attachmentKeyid1, this.attachmentKeyid2, this.attachmentKeyid3, this.tempid) : ImageRef.getSDITempImage(this.getConnectionProcessor().getSapphireConnection(), this.attachmentSDCId, this.attachmentKeyid1, this.attachmentKeyid2, this.attachmentKeyid3, this.tempid);
            } else if (this.attachmentKeyid1.length() > 0) {
                imageRef = this.attachmentThumbnail ? ImageRef.getAttachmentThumbnailImage(this.getConnectionProcessor().getSapphireConnection(), this.attachmentSDCId, this.attachmentKeyid1, this.attachmentKeyid2, this.attachmentKeyid3, this.attachmentNum) : ImageRef.getAttachmentImage(this.getConnectionProcessor().getSapphireConnection(), this.attachmentSDCId, this.attachmentKeyid1, this.attachmentKeyid2, this.attachmentKeyid3, this.attachmentNum);
            }
        }
        if (imageRef != null) {
            if (this.attachmentThumbnailGeneration != null) {
                imageRef.setThumbnailGeneration(this.attachmentThumbnailGeneration);
            }
            this.imageId = imageRef.getImageRefId();
            if (imageRef.getFileType() != ImageRef.FileType.URL) {
                imageRef.setDimensions(this.width, this.height);
            }
            if (this.nocache) {
                imageRef.setNoCache(true);
            }
            if (this.overlay.length() > 0) {
                imageRef.setOverlay(this.overlay);
            }
            if (this.color.length() > 0) {
                imageRef.setColor(this.color);
            }
            if (this.disabled) {
                imageRef.setGrayscale(true);
                imageRef.setOpacity(0.8f);
            }
            if (this.opacity > -1.0f) {
                if (this.opacity > 1.0f) {
                    this.opacity /= 100.0f;
                }
                imageRef.setOpacity(this.opacity);
            }
            if (this.defaultImage.length() > 0) {
                imageRef.setDefault(this.defaultImage);
            }
            this.src = imageRef.getSrc();
            if ((imageRef.getFileType() == ImageRef.FileType.BITMAP || imageRef.getFileType() == ImageRef.FileType.SVG) && this.renderAllResolutions) {
                int s = imageRef.getSize();
                ArrayList<Integer> arr = imageRef.getSizes();
                block4: for (Integer i : arr) {
                    String csrc;
                    if (i == s) {
                        csrc = "$";
                    } else {
                        imageRef.setDimensions(i, i);
                        csrc = imageRef.getSrc();
                    }
                    switch (i) {
                        case 32: {
                            this.src32 = csrc;
                            continue block4;
                        }
                        case 48: {
                            this.src48 = csrc;
                            continue block4;
                        }
                    }
                    this.src16 = csrc;
                }
            }
        } else if (this.src == null || this.src.length() == 0) {
            this.logger.error("No image details provided.");
            this.src = this.defaultImage.length() > 0 ? this.defaultImage : "WEB-CORE/images/blank.gif";
        }
    }

    public String getImageSrc() {
        Object imageRef = null;
        this.generateSource();
        return this.src;
    }

    @Override
    public String getHtml() {
        StringBuffer html = new StringBuffer();
        this.generateSource();
        String alt = this.title.length() > 0 ? this.title : (this.imageId.length() > 0 ? this.imageId : "");
        html.append("<img").append(this.width > 0 ? " width=\"" + this.width + "\"" : "").append(this.height > 0 ? " height=\"" + this.height + "\"" : "");
        html.append(" border=0 src=\"").append(this.src).append("\"").append(alt.length() > 0 ? " title=\"" + alt + "\"" : "");
        html.append(this.className.length() > 0 ? " class=\"" + this.className + "\"" : "").append(this.style.length() > 0 ? " style=\"" + this.style + "\"" : "");
        if (this.elementid != null && this.elementid.length() > 0) {
            html.append(" id=\"").append(this.elementid).append("\"");
        }
        if (this.imageId != null && this.imageId.length() > 0) {
            html.append(" imageid=\"").append(this.imageId).append("\"");
        }
        if (this.renderAllResolutions) {
            html.append(" img16=\"").append(this.src16).append("\"");
            html.append(" img32=\"").append(this.src32).append("\"");
            html.append(" img48=\"").append(this.src48).append("\"");
        }
        html.append(">");
        return html.toString();
    }
}

