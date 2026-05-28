/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.elements.chart.util;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import javax.imageio.ImageIO;

public class BufferedImageWrapper
implements Serializable {
    private long id;
    private String name;
    private BufferedImage image;

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String str) {
        this.name = str;
    }

    public BufferedImage getImage() {
        return this.image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(this.name);
        ImageIO.write((RenderedImage)this.image, "jpeg", ImageIO.createImageOutputStream(out));
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.name = (String)in.readObject();
        this.image = ImageIO.read(ImageIO.createImageInputStream(in));
    }
}

