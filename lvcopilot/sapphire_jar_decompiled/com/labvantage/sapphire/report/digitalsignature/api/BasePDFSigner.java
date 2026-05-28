/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.io.FileUtils
 *  org.apache.pdfbox.cos.COSArray
 *  org.apache.pdfbox.cos.COSBase
 *  org.apache.pdfbox.cos.COSDictionary
 *  org.apache.pdfbox.cos.COSName
 *  org.apache.pdfbox.pdmodel.PDDocument
 *  org.apache.pdfbox.pdmodel.PDPage
 *  org.apache.pdfbox.pdmodel.PDPageContentStream
 *  org.apache.pdfbox.pdmodel.PDResources
 *  org.apache.pdfbox.pdmodel.common.PDRectangle
 *  org.apache.pdfbox.pdmodel.common.PDStream
 *  org.apache.pdfbox.pdmodel.font.PDFont
 *  org.apache.pdfbox.pdmodel.font.PDType1Font
 *  org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject
 *  org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
 *  org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget
 *  org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary
 *  org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream
 *  org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature
 *  org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface
 *  org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions
 *  org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm
 *  org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField
 *  org.apache.pdfbox.util.Matrix
 */
package com.labvantage.sapphire.report.digitalsignature.api;

import com.labvantage.sapphire.modules.eln.DigitalSignatureRectangle;
import com.labvantage.sapphire.report.digitalsignature.api.PDFSigner;
import com.labvantage.sapphire.report.digitalsignature.api.SignatureData;
import com.labvantage.sapphire.report.digitalsignature.api.SignatureValidator;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;
import org.apache.pdfbox.util.Matrix;
import sapphire.xml.PropertyList;

public abstract class BasePDFSigner
implements SignatureInterface,
PDFSigner,
SignatureValidator {
    protected static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----";
    protected static final String END_CERT = "-----END CERTIFICATE-----";
    private List<Certificate> certificateChain;
    private String tsaUrl;
    private byte[] imageFile;
    private boolean validate = false;

    @Override
    public InputStream signDocument(PropertyList properties, InputStream input, SignatureData signatureData) throws IOException {
        try {
            File readyFile = File.createTempFile("output", ".pdf");
            readyFile.deleteOnExit();
            this.initSigner(properties);
            try (FileOutputStream fos = new FileOutputStream(readyFile);){
                signatureData.setSigner(this);
                this.tsaUrl = properties.getProperty("tsaurl", "");
                signatureData.signDocument(input, fos);
            }
            return this.validateSignature(FileUtils.openInputStream((File)readyFile));
        }
        catch (FileNotFoundException e) {
            throw new IOException("FileNotFoundException", e);
        }
    }

    public abstract void initSigner(PropertyList var1) throws IOException;

    protected final void setCertificateChain(List<Certificate> certificateChain) {
        this.certificateChain = certificateChain;
    }

    @Override
    public InputStream validateSignature(InputStream inStream) throws IOException {
        return inStream;
    }

    protected void setTsaUrl(String tsaUrl) {
        this.tsaUrl = tsaUrl;
    }

    protected String getTsaUrl() {
        return this.tsaUrl;
    }

    protected void buildCertificateChain(String ... certificates) {
        this.setCertificateChain(Arrays.stream(certificates).map(certString -> {
            try {
                byte[] decodedCertificate = Base64.getDecoder().decode(certString.replace(BEGIN_CERT, "").replace(END_CERT, "").replaceAll("\\s+", ""));
                return CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(decodedCertificate));
            }
            catch (CertificateException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList()));
    }

    protected X509Certificate getCertificate(String fileName) throws CertificateException, IOException {
        try (FileInputStream in = new FileInputStream(new File(fileName));){
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            Collection<? extends Certificate> certs = certFactory.generateCertificates(in);
            X509Certificate x509Certificate = (X509Certificate)certs.iterator().next();
            return x509Certificate;
        }
    }

    protected void setImageFile(byte[] imageFile) {
        this.imageFile = imageFile;
    }

    public void signPDF(SignatureData signatureData, InputStream inputFile, OutputStream fos, Rectangle2D humanRect, DigitalSignatureRectangle digitalSignatureRectangle) throws IOException {
        try (PDDocument doc = PDDocument.load((InputStream)inputFile);
             SignatureOptions signatureOptions = new SignatureOptions();){
            int accessPermissions = BasePDFSigner.getMDPPermission(doc);
            if (accessPermissions == 1) {
                throw new IllegalStateException("No changes to the document are permitted due to DocMDP transform parameters dictionary");
            }
            PDSignature signature = new PDSignature();
            int signaturePage = signatureData.getSignaturePage() > doc.getNumberOfPages() || signatureData.getSignaturePage() == -1 ? doc.getNumberOfPages() - 1 : signatureData.getSignaturePage() - 1;
            PDRectangle rect = this.createSignatureRectangle(doc, humanRect, signaturePage, digitalSignatureRectangle);
            signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
            signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
            signature.setName(signatureData.getSignatureName());
            signature.setLocation(signatureData.getSignatureLocation());
            signature.setReason(signatureData.getSignatureReason());
            signature.setSignDate(Calendar.getInstance());
            BasePDFSigner signatureInterface = this;
            signatureOptions.setVisualSignature(this.createVisualSignatureTemplate(doc, signaturePage, rect, signature, signatureData));
            signatureOptions.setPage(signaturePage);
            signatureOptions.setPreferredSignatureSize(18944);
            doc.addSignature(signature, (SignatureInterface)signatureInterface, signatureOptions);
            doc.saveIncremental(fos);
        }
    }

    private PDRectangle createSignatureRectangle(PDDocument doc, Rectangle2D humanRect, int signaturePage, DigitalSignatureRectangle digitalSignatureRectangle) {
        float x = (float)humanRect.getX();
        float y = (float)humanRect.getY();
        float width = (float)humanRect.getWidth();
        float height = (float)humanRect.getHeight();
        PDPage page = doc.getPage(signaturePage);
        PDRectangle pageRect = page.getCropBox();
        PDRectangle rect = new PDRectangle();
        switch (page.getRotation()) {
            case 90: {
                rect.setLowerLeftY(x);
                rect.setUpperRightY(x + width);
                rect.setLowerLeftX(y);
                rect.setUpperRightX(y + height);
                break;
            }
            case 180: {
                rect.setUpperRightX(pageRect.getWidth() - x);
                rect.setLowerLeftX(pageRect.getWidth() - x - width);
                rect.setLowerLeftY(y);
                rect.setUpperRightY(y + height);
                break;
            }
            case 270: {
                rect.setLowerLeftY(pageRect.getHeight() - x - width);
                rect.setUpperRightY(pageRect.getHeight() - x);
                rect.setLowerLeftX(pageRect.getWidth() - y - height);
                rect.setUpperRightX(pageRect.getWidth() - y);
                break;
            }
            default: {
                if (digitalSignatureRectangle != null) {
                    this.populatedRectFromDigitalSignatureRect(digitalSignatureRectangle, rect);
                    break;
                }
                rect.setLowerLeftX(x);
                rect.setUpperRightX(x + width);
                rect.setLowerLeftY(pageRect.getHeight() - y - height);
                rect.setUpperRightY(pageRect.getHeight() - y);
            }
        }
        return rect;
    }

    private void populatedRectFromDigitalSignatureRect(DigitalSignatureRectangle digitalSignatureRectangle, PDRectangle rect) {
        rect.setLowerLeftX(Float.parseFloat(digitalSignatureRectangle.getLowerLeftX()));
        rect.setUpperRightX(Float.parseFloat(digitalSignatureRectangle.getUpperRightX()));
        rect.setLowerLeftY(Float.parseFloat(digitalSignatureRectangle.getLowerLeftY()));
        rect.setUpperRightY(Float.parseFloat(digitalSignatureRectangle.getUpperRightY()));
    }

    private InputStream createVisualSignatureTemplate(PDDocument srcDoc, int pageNum, PDRectangle rect, PDSignature signature, SignatureData signatureData) throws IOException {
        try (PDDocument doc = new PDDocument();){
            PDPage page = new PDPage(srcDoc.getPage(pageNum).getMediaBox());
            doc.addPage(page);
            PDAcroForm acroForm = new PDAcroForm(doc);
            doc.getDocumentCatalog().setAcroForm(acroForm);
            PDSignatureField signatureField = new PDSignatureField(acroForm);
            PDAnnotationWidget widget = (PDAnnotationWidget)signatureField.getWidgets().get(0);
            List acroFormFields = acroForm.getFields();
            acroForm.setSignaturesExist(true);
            acroForm.setAppendOnly(true);
            acroForm.getCOSObject().setDirect(true);
            acroFormFields.add(signatureField);
            widget.setRectangle(rect);
            PDStream stream = new PDStream(doc);
            PDFormXObject form = new PDFormXObject(stream);
            PDResources res = new PDResources();
            form.setResources(res);
            form.setFormType(1);
            PDRectangle bbox = new PDRectangle(rect.getWidth(), rect.getHeight());
            float height = bbox.getHeight();
            float width = bbox.getHeight();
            Matrix initialScale = null;
            switch (srcDoc.getPage(pageNum).getRotation()) {
                case 90: {
                    form.setMatrix(AffineTransform.getQuadrantRotateInstance(1));
                    initialScale = Matrix.getScaleInstance((float)(bbox.getWidth() / bbox.getHeight()), (float)(bbox.getHeight() / bbox.getWidth()));
                    height = bbox.getWidth();
                    width = bbox.getHeight();
                    break;
                }
                case 180: {
                    form.setMatrix(AffineTransform.getQuadrantRotateInstance(2));
                    break;
                }
                case 270: {
                    form.setMatrix(AffineTransform.getQuadrantRotateInstance(3));
                    initialScale = Matrix.getScaleInstance((float)(bbox.getWidth() / bbox.getHeight()), (float)(bbox.getHeight() / bbox.getWidth()));
                    height = bbox.getWidth();
                    width = bbox.getHeight();
                    break;
                }
            }
            form.setBBox(bbox);
            PDAppearanceDictionary appearance = new PDAppearanceDictionary();
            appearance.getCOSObject().setDirect(true);
            PDAppearanceStream appearanceStream = new PDAppearanceStream(form.getCOSObject());
            appearance.setNormalAppearance(appearanceStream);
            widget.setAppearance(appearance);
            PDPageContentStream cs = new PDPageContentStream(doc, appearanceStream);
            Object object = null;
            try {
                PDFont font;
                if (initialScale != null) {
                    cs.transform(initialScale);
                }
                float fontSize = signatureData.getFontSize();
                float leading = fontSize * 1.5f;
                if (this.imageFile != null) {
                    PDImageXObject img = PDImageXObject.createFromByteArray((PDDocument)doc, (byte[])this.imageFile, (String)"signature-image");
                    Dimension scaledDim = BasePDFSigner.getScaledDimension(new Dimension(img.getWidth(), img.getHeight()), new Dimension((int)width, (int)(height - leading * 2.0f - 1.0f)));
                    cs.drawImage(img, 0.0f, 0.0f, (float)scaledDim.getWidth(), (float)scaledDim.getHeight());
                }
                if ((font = signatureData.getFont()) == null) {
                    font = PDType1Font.HELVETICA_BOLD;
                    PDPage pagei = srcDoc.getPage(0);
                    PDResources pDResources = pagei.getResources();
                }
                cs.beginText();
                cs.setFont(font, fontSize);
                cs.setNonStrokingColor(signatureData.getFontColor());
                cs.newLineAtOffset(fontSize, height - leading);
                cs.setLeading(leading);
                DateFormat dateFormat = DateFormat.getDateInstance(2, signatureData.getSignatureLocale());
                String date = dateFormat.format(signature.getSignDate().getTime());
                String name2 = signature.getName();
                cs.showText(name2);
                cs.newLineAtOffset(0.0f, -leading);
                cs.showText(date);
                cs.endText();
            }
            catch (Throwable throwable) {
                object = throwable;
                throw throwable;
            }
            finally {
                if (cs != null) {
                    if (object != null) {
                        try {
                            cs.close();
                        }
                        catch (Throwable throwable) {
                            ((Throwable)object).addSuppressed(throwable);
                        }
                    } else {
                        cs.close();
                    }
                }
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save((OutputStream)baos);
            object = new ByteArrayInputStream(baos.toByteArray());
            return object;
        }
    }

    public List<Certificate> getCertificateChain() {
        return this.certificateChain;
    }

    public static int getMDPPermission(PDDocument doc) {
        COSDictionary signatureDict;
        COSDictionary permsDict;
        COSBase base = doc.getDocumentCatalog().getCOSObject().getDictionaryObject(COSName.PERMS);
        if (base instanceof COSDictionary && (base = (permsDict = (COSDictionary)base).getDictionaryObject(COSName.DOCMDP)) instanceof COSDictionary && (base = (signatureDict = (COSDictionary)base).getDictionaryObject("Reference")) instanceof COSArray) {
            COSArray refArray = (COSArray)base;
            for (int i = 0; i < refArray.size(); ++i) {
                COSDictionary sigRefDict;
                base = refArray.getObject(i);
                if (!(base instanceof COSDictionary) || !COSName.DOCMDP.equals((Object)(sigRefDict = (COSDictionary)base).getDictionaryObject("TransformMethod")) || !((base = sigRefDict.getDictionaryObject("TransformParams")) instanceof COSDictionary)) continue;
                COSDictionary transformDict = (COSDictionary)base;
                int accessPermissions = transformDict.getInt(COSName.P, 2);
                if (accessPermissions < 1 || accessPermissions > 3) {
                    accessPermissions = 2;
                }
                return accessPermissions;
            }
        }
        return 0;
    }

    protected String parsePrivateKey(String input) {
        return this.parsePem(input, "PRIVATE KEY");
    }

    protected String parseCertificate(String input) {
        return this.parsePem(input, "CERTIFICATE");
    }

    private String parsePem(String input, String type) {
        String privatekey = String.format("-----BEGIN %s-----\n%s\n-----END %s-----", type, input.replaceAll("\\s+", "").replaceAll("-----.*?-----", ""), type);
        return privatekey;
    }

    private static Dimension getScaledDimension(Dimension imgSize, Dimension boundary) {
        int originalWidth = imgSize.width;
        int originalHeight = imgSize.height;
        int boundWidth = boundary.width;
        int boundHeight = boundary.height;
        int newWidth = originalWidth;
        int newHeight = originalHeight;
        if (originalWidth > boundWidth) {
            newWidth = boundWidth;
            newHeight = newWidth * originalHeight / originalWidth;
        }
        if (newHeight > boundHeight) {
            newHeight = boundHeight;
            newWidth = newHeight * originalWidth / originalHeight;
        }
        return new Dimension(newWidth, newHeight);
    }

    public boolean isValidate() {
        return this.validate;
    }

    public void setValidate(boolean validate) {
        this.validate = validate;
    }
}

