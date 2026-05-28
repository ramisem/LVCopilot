/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.pdfbox.pdmodel.font.PDFont
 */
package com.labvantage.sapphire.report.digitalsignature.api;

import com.labvantage.sapphire.modules.eln.DigitalSignatureRectangle;
import com.labvantage.sapphire.report.digitalsignature.api.BasePDFSigner;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import org.apache.pdfbox.pdmodel.font.PDFont;

public class SignatureData {
    private BasePDFSigner signer = null;
    private String signatureLocation = "";
    private String signatureReason = "";
    private String signatureName = "";
    private int signaturePage = 1;
    private byte[] image = null;
    private Rectangle2D signatureShape;
    private DigitalSignatureRectangle digitalSignatureRectangle;
    private Locale signatureLocale = Locale.getDefault();
    private PDFont font = null;
    private float fontSize = 10.0f;
    private Color fontColor = Color.black;

    public SignatureData() {
        this.signatureShape = new Rectangle2D.Float();
        this.signatureShape.setFrame(0.0, 0.0, 200.0, 100.0);
    }

    public void signDocument(InputStream document, OutputStream outputStream) throws IOException {
        if (this.image != null) {
            this.signer.setImageFile(this.image);
        }
        this.signer.signPDF(this, document, outputStream, this.signatureShape, this.digitalSignatureRectangle);
    }

    public void setSignatureShape(Rectangle2D signatureShape) {
        this.signatureShape = signatureShape;
    }

    public DigitalSignatureRectangle getDigitalSignatureRectangle() {
        return this.digitalSignatureRectangle;
    }

    public void setDigitalSignatureRectangle(DigitalSignatureRectangle digitalSignatureRectangle) {
        this.digitalSignatureRectangle = digitalSignatureRectangle;
    }

    public void setSignatureLocation(String signatureLocation) {
        this.signatureLocation = signatureLocation;
    }

    public void setSignatureReason(String signatureReason) {
        this.signatureReason = signatureReason;
    }

    public void setSignatureName(String signatureName) {
        this.signatureName = signatureName;
    }

    public void setSignaturePage(int signaturePage) {
        this.signaturePage = signaturePage;
    }

    public void setSignatureLocale(Locale signatureLocale) {
        this.signatureLocale = signatureLocale;
    }

    public void setSignatureImage(byte[] image) {
        this.image = image;
    }

    public void setFont(PDFont font) {
        this.font = font;
    }

    public void setFontSize(float fontSize) {
        this.fontSize = fontSize;
    }

    public void setFontColor(Color fontColor) {
        this.fontColor = fontColor;
    }

    public String getSignatureLocation() {
        return this.signatureLocation;
    }

    public String getSignatureReason() {
        return this.signatureReason;
    }

    public String getSignatureName() {
        return this.signatureName;
    }

    public int getSignaturePage() {
        return this.signaturePage;
    }

    public Locale getSignatureLocale() {
        return this.signatureLocale;
    }

    public PDFont getFont() {
        return this.font;
    }

    public float getFontSize() {
        return this.fontSize;
    }

    public Color getFontColor() {
        return this.fontColor;
    }

    public void setSigner(BasePDFSigner signer) {
        this.signer = signer;
    }
}

