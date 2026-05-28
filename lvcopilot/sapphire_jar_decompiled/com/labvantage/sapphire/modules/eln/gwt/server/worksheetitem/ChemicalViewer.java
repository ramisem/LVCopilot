/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.codec.binary.Base64
 *  org.jfree.chart.encoders.ImageEncoder
 *  org.jfree.chart.encoders.ImageEncoderFactory
 *  org.jsoup.Jsoup
 *  org.jsoup.nodes.Document
 *  org.openscience.cdk.AtomContainerSet
 *  org.openscience.cdk.ChemFile
 *  org.openscience.cdk.Reaction
 *  org.openscience.cdk.depict.DepictionGenerator
 *  org.openscience.cdk.exception.CDKException
 *  org.openscience.cdk.interfaces.IAtom
 *  org.openscience.cdk.interfaces.IAtomContainer
 *  org.openscience.cdk.interfaces.IAtomContainerSet
 *  org.openscience.cdk.interfaces.IChemFile
 *  org.openscience.cdk.interfaces.IChemObject
 *  org.openscience.cdk.interfaces.IReaction
 *  org.openscience.cdk.io.ISimpleChemObjectReader
 *  org.openscience.cdk.io.ReaderFactory
 *  org.openscience.cdk.qsar.DescriptorEngine
 *  org.openscience.cdk.qsar.DescriptorSpecification
 *  org.openscience.cdk.qsar.DescriptorValue
 *  org.openscience.cdk.qsar.IMolecularDescriptor
 *  org.openscience.cdk.qsar.result.DoubleArrayResult
 *  org.openscience.cdk.qsar.result.DoubleResult
 *  org.openscience.cdk.qsar.result.IDescriptorResult
 *  org.openscience.cdk.qsar.result.IntegerArrayResult
 *  org.openscience.cdk.tools.manipulator.ChemFileManipulator
 */
package com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.ChemicalViewerAjaxHandler;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemFields;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemIncludes;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemOptions;
import com.labvantage.sapphire.pageelements.spreadsheeteditor.SparklineStreamer;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.util.file.ChemicalFileDetails;
import com.labvantage.sapphire.util.file.FileTypeGroup;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import org.jfree.chart.encoders.ImageEncoder;
import org.jfree.chart.encoders.ImageEncoderFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.ChemFile;
import org.openscience.cdk.Reaction;
import org.openscience.cdk.depict.DepictionGenerator;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomContainerSet;
import org.openscience.cdk.interfaces.IChemFile;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.interfaces.IReaction;
import org.openscience.cdk.io.ISimpleChemObjectReader;
import org.openscience.cdk.io.ReaderFactory;
import org.openscience.cdk.qsar.DescriptorEngine;
import org.openscience.cdk.qsar.DescriptorSpecification;
import org.openscience.cdk.qsar.DescriptorValue;
import org.openscience.cdk.qsar.IMolecularDescriptor;
import org.openscience.cdk.qsar.result.DoubleArrayResult;
import org.openscience.cdk.qsar.result.DoubleResult;
import org.openscience.cdk.qsar.result.IDescriptorResult;
import org.openscience.cdk.qsar.result.IntegerArrayResult;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.ext.BaseWorksheetItem;
import sapphire.util.HttpUtil;
import sapphire.util.Logger;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ChemicalViewer
extends BaseWorksheetItem {
    public static String[][] chemTypes = new String[][]{{"CML", "cmlreader", "org.openscience.cdk.io.CMLReader", "Reads a molecule in CML 1.x and 2.0 format. CML is an XML based application and applies the method described in [Willighagen, E.L., Processing CML Conventions in Java, Internet Journal of Chemistry, 2001, 4:?-?]."}, {"CTX", "ctxreader", "org.openscience.cdk.io.CTXReader", "Extracts information from the IDENT, NAME, ATOMS and BONDS blocks in CTX files."}, {"INChI Plain Text", "inchiplaintext", "org.openscience.cdk.io.INChIPlainTextReader", "Reads the content of a IUPAC/NIST Chemical Identifier (INChI) plain text document"}, {"INChI", "inchi", "org.openscience.cdk.io.INChIReader", "Reads the content of a IUPAC/NIST Chemical Identifier (INChI) document. See [Stein, S. and Heller, S., IUPAC Chemical Identifier (IChI), Chemistry International, 2001, 23:?-?]. Recently a new INChI format was introduced an files generated with the latest INChI generator cannot be parsed with this class"}, {"MDL RXNfile V2000", "mdlrxnv2000", "org.openscience.cdk.io.MDLRXNV2000Reader", "Reads a molecule from an MDL RXN file [Dalby, A. and Nourse, J. G. and Hounshell, W. D. and Gushurst, A. K. and Grier, D. L. and Leland, B. A. and Laufer, J., Description of Several Chemical Structure File Formats Used by Computer Programs Developed at Molecular Design Limited, Journal of Chemical Information and Computer Sciences, 1992, 32:244-255]."}, {"MDL RXNfile V3000", "mdlrxnv3000", "org.openscience.cdk.io.MDLRXNV3000Reader", "Class that implements the new MDL mol format introduced in August 2002. The overall syntax should be compatible with the old format."}, {"MDL MOLfile V2000", "mdlv2000", "org.openscience.cdk.io.MDLV2000Reader", "Reads content from MDL molfiles and SD files. From the Atom block it reads atomic coordinates, element types and formal charges. From the Bond block it reads the bonds and the orders. Additionally, it reads 'M CHG', 'G ', 'M RAD' and 'M ISO' lines from the property block.\nRGroups which are saved in the MDL molfile as R#, are renamed according to their appearance, e.g. the first R# is named R1. With PseudAtom.getLabel() \"R1\" is returned (instead of R#). This is introduced due to the SAR table generation procedure of Scitegics PipelinePilot."}, {"MDL MOLfile V3000", "mdlv3000", "org.openscience.cdk.io.MDLV3000Reader", "Reads the MDL mol V3000 format. Reads the element symbol and 2D or 3D coordinates from the ATOM block."}, {"Mol2", "mol2", "org.openscience.cdk.io.Mol2Reader", "Reads a molecule from an Mol2 file, such as written by Sybyl."}, {"Pub Chem Compound ASN", "pccompoundasn", "org.openscience.cdk.io.PCCompoundASNReader", "Reads an object from ASN formated input for PubChem Compound entries. The following bits are supported: atoms.aid, atoms.element, bonds.aid1, bonds.aid2. Additionally, it extracts the InChI and canonical SMILES properties."}, {"Pub Chem Compound XML", "pccompoundxml", "org.openscience.cdk.io.PCCompoundXMLReader", "Reads an object from ASN.1 XML formated input for PubChem Compound entries. The following bits are supported: atoms.aid, atoms.element, atoms.2d, atoms.3d, bonds.aid1, bonds.aid2."}, {"Pub Chem Substance XML", "pcsubstancexml", "org.openscience.cdk.io.PCSubstanceXMLReader", "Reads an object from ASN formated input for PubChem Compound entries. The following bits are supported: atoms.aid, atoms.element, bonds.aid1, bonds.aid2. Additionally, it extracts the InChI and canonical SMILES properties."}, {"RGroup Query", "rgroupquery", "org.openscience.cdk.io.RGroupQueryReader", "A reader for Symyx' Rgroup files (RGFiles). An RGfile describes a single molecular query with Rgroups. Each RGfile is a combination of Ctabs defining the root molecule and each member of each Rgroup in the query."}, {"SMILES", "smiles", "org.openscience.cdk.io.SMILESReader", "This Reader reads files which has one SMILES string on each line, where the format is given as below:\nCOC ethoxy ethane\nThus first the SMILES, and then after the first space (or tab) on the line a title that is stored as CDKConstants.TITLE. For legacy comparability the title is also placed in a \"SMIdbNAME\" property. If a line is invalid an empty molecule is inserted into the container set. The molecule with have the prop IteratingSMILESReader.BAD_SMILES_INPUT set to the input line that could not be read.\nFor each line a molecule is generated, and multiple Molecules are read as MoleculeSet."}, {"XYZ", "xyz", "org.openscience.cdk.io.XYZReader", "Reads an object from XYZ formated input."}};
    public static int CHEMTYPE_TITLE = 0;
    public static int CHEMTYPE_CODE = 1;
    public static int CHEMTYPE_CLASS = 2;
    public static int CHEMTYPE_HELP = 3;

    @Override
    public void setupOptions(WorksheetItemOptions worksheetItemOptions) {
        worksheetItemOptions.setSupportsExport(true);
        worksheetItemOptions.setSupportsFields(true);
    }

    @Override
    public void setupIncludes(WorksheetItemIncludes worksheetItemIncludes) {
        worksheetItemIncludes.addScriptInclude("WEB-CORE/modules/eln/worksheetitem/scripts/chemicalviewer.js");
        worksheetItemIncludes.setJSObjectName("chemicalViewer");
    }

    @Override
    public String getViewHTML() throws SapphireException {
        String contents = this.getContents();
        StringBuffer html = new StringBuffer();
        try {
            PropertyList fileDetails = contents.length() > 0 ? new PropertyList(new JSONObject(contents)) : new PropertyList();
            html.append(ChemicalViewerAjaxHandler.getViewArea(fileDetails, this.getWorksheetItemId(), this.getWorksheetItemVersionId()));
        }
        catch (Exception e) {
            this.logError("Could not load editor.", e);
        }
        return html.toString();
    }

    @Override
    public String getEditorHTML() throws SapphireException {
        PropertyList fileDetails = null;
        StringBuffer html = new StringBuffer();
        boolean devMode = Configuration.isDevmode(this.getSapphireConnection().getDatabaseId());
        String contents = this.getContents();
        try {
            fileDetails = contents.length() > 0 ? new PropertyList(new JSONObject(contents)) : new PropertyList();
            String attNum = fileDetails.getProperty("attachment", "");
            if (attNum.length() == 0) {
                html.append(ChemicalViewerAjaxHandler.getUploadArea(this.getElementId(), this.config.getProperty("defaultchemtype", "auto")));
            } else {
                html.append(ChemicalViewerAjaxHandler.getEditArea(this.getElementId(), fileDetails, this.getWorksheetItemId(), this.getWorksheetItemVersionId(), new Logger(this.logContext), this.config, Configuration.isDevmode(this.getSapphireConnection().getDatabaseId()), this.getTranslationProcessor()));
            }
        }
        catch (Exception e) {
            this.logError("Could not load editor.", e);
        }
        html.append("<textarea style=\"display:none;\" id=\"").append(this.getElementId()).append("_content\">");
        html.append(fileDetails != null && fileDetails.size() > 0 ? fileDetails.toJSONString() : "");
        html.append("</textarea>");
        StringBuffer types = new StringBuffer();
        for (int i = 0; i < FileTypeGroup.CHEMICAL.getFileTypeExtensions().size(); ++i) {
            String ext;
            if (types.length() > 0) {
                types.append(",");
            }
            if ((ext = FileTypeGroup.CHEMICAL.getFileTypeExtensions().get(i)).length() > 0 && !ext.startsWith(".")) {
                ext = "." + ext;
            }
            types.append(ext);
        }
        html.append("<script>");
        html.append("chemicalViewer.settings['").append(this.getElementId()).append("'] = {");
        html.append("'fileTypes':");
        html.append("'").append(types).append("'");
        html.append(",");
        html.append("'config':'").append(this.config.toJSONString()).append("'");
        html.append(",");
        html.append("'worksheetid':").append("'").append(this.getWorksheetId()).append("'");
        html.append(",");
        html.append("'worksheetversionid':").append("'").append(this.getWorksheetVersionId()).append("'");
        html.append(",");
        html.append("'worksheetitemid':").append("'").append(this.getWorksheetItemId()).append("'");
        html.append(",");
        html.append("'worksheetversionitemid':").append("'").append(this.getWorksheetItemVersionId()).append("'");
        html.append("};");
        html.append("</script>");
        return html.toString();
    }

    public static int getChemicalFromBIS(StringBuffer out, ByteArrayInputStream bis, ChemicalFileDetails fileDetails, String chemType, PropertyList config, Logger logger, TranslationProcessor tp) throws Exception {
        int data = 0;
        try {
            ISimpleChemObjectReader reader = ChemicalViewer.getChemObjectReader(chemType);
            if (reader != null) {
                reader.setReader((InputStream)bis);
                if (reader == null) {
                    throw new Exception("Unable to parse molecule file.");
                }
            } else {
                ReaderFactory readerFactory = new ReaderFactory();
                reader = readerFactory.createReader((InputStream)bis);
                bis.reset();
                if (reader == null) {
                    reader = ChemicalViewer.getChemObjectReader("smiles");
                    if (reader != null) {
                        reader.setReader((InputStream)bis);
                        if (reader == null) {
                            throw new Exception("Unable to parse molecule file.");
                        }
                    } else {
                        throw new Exception("Unable to determine the file format. Try nominating a specific format.");
                    }
                }
            }
            if (reader.accepts(IReaction.class)) {
                IReaction reaction = (IReaction)reader.read((IChemObject)new Reaction());
                String image = ChemicalViewer.generateReactionImage(fileDetails, reaction);
                PropertyList params = config.getPropertyListNotNull("descriptors");
                boolean requiresFields = ChemicalViewer.getRequiresFields(params);
                StringBuffer reactionTable = new StringBuffer();
                if (requiresFields || fileDetails.isShowDescriptors()) {
                    String paramTable;
                    IAtomContainer molecule;
                    int i;
                    reactionTable.append("<table><tr>");
                    IAtomContainerSet reactants = reaction.getReactants();
                    IAtomContainerSet products = reaction.getProducts();
                    for (i = 0; i < reactants.getAtomContainerCount(); ++i) {
                        reactionTable.append("<td style=\"padding-left:5px\">").append(" " + tp.translate("Reactant") + " " + (i + 1)).append("</td>");
                    }
                    for (i = 0; i < products.getAtomContainerCount(); ++i) {
                        reactionTable.append("<td style=\"padding-left:5px\">").append(" " + tp.translate("Product") + " " + (i + 1)).append("</td>");
                    }
                    reactionTable.append("</tr><tr>");
                    for (i = 0; i < reactants.getAtomContainerCount(); ++i) {
                        molecule = reactants.getAtomContainer(i);
                        paramTable = ChemicalViewer.getMoleculeDescriptors(fileDetails, molecule, params, tp);
                        reactionTable.append("<td valign=\"top\">").append(paramTable).append("</td>");
                    }
                    for (i = 0; i < products.getAtomContainerCount(); ++i) {
                        molecule = products.getAtomContainer(i);
                        paramTable = ChemicalViewer.getMoleculeDescriptors(fileDetails, molecule, params, tp);
                        reactionTable.append("<td valign=\"top\">").append(paramTable).append("</td>");
                    }
                    reactionTable.append("</tr></table>");
                }
                ChemicalViewer.drawImageTable(out, fileDetails, params, image, reactionTable.toString());
                reader.close();
            } else if (reader.accepts(IChemFile.class)) {
                IChemFile file = (IChemFile)reader.read((IChemObject)new ChemFile());
                List c = ChemFileManipulator.getAllAtomContainers((IChemFile)file);
                PropertyList params = config.getPropertyListNotNull("descriptors");
                boolean requiresFields = ChemicalViewer.getRequiresFields(params);
                for (int j = 0; j < c.size(); ++j) {
                    if (j > 0) {
                        out.append("<br><br>");
                    }
                    IAtomContainer molecule = (IAtomContainer)c.get(j);
                    String image = ChemicalViewer.generateMoleculeImage(fileDetails, molecule);
                    String paramTable = "";
                    if (requiresFields || fileDetails.isShowDescriptors()) {
                        paramTable = ChemicalViewer.getMoleculeDescriptors(fileDetails, molecule, params, tp);
                    }
                    ChemicalViewer.drawImageTable(out, fileDetails, params, image, paramTable);
                }
                reader.close();
            } else {
                IAtomContainerSet set = (IAtomContainerSet)reader.read((IChemObject)new AtomContainerSet());
                int count = set.getAtomContainerCount();
                for (int i = 0; i < count; ++i) {
                    IAtomContainer container = set.getAtomContainer(i);
                    String image = ChemicalViewer.generateMoleculeImage(fileDetails, container);
                    out.append(image);
                }
                reader.close();
            }
        }
        catch (Exception e) {
            throw new SapphireException("Could not process chemical structure: " + e.getMessage());
        }
        return data;
    }

    public static void drawImageTable(StringBuffer out, ChemicalFileDetails fileDetails, PropertyList params, String image, String paramTable) {
        if (!fileDetails.isShowDescriptors()) {
            out.append(image);
        } else {
            String pos = params.getProperty("position", "below");
            if (paramTable.length() > 0) {
                out.append("<table>");
                if (pos.equalsIgnoreCase("left")) {
                    out.append("<tr><td style=\"padding:right:20px\">" + paramTable + "</td><td>" + image + "</td></tr>");
                } else if (pos.equalsIgnoreCase("right")) {
                    out.append("<tr><td>" + image + "</td><td style=\"padding-left:20px\">" + paramTable + "</td></tr>");
                } else if (pos.equalsIgnoreCase("above")) {
                    out.append("<tr><td>" + paramTable + "</td></tr><tr><td>" + image + "</td></tr>");
                } else if (pos.equalsIgnoreCase("below")) {
                    out.append("<tr><td>" + image + "</td></tr><tr><td>" + paramTable + "</td></tr>");
                }
                out.append("</table>");
            } else {
                out.append(image);
            }
        }
    }

    private static boolean getRequiresFields(PropertyList params) {
        PropertyListCollection descriptors = params.getCollectionNotNull("descriptors");
        for (int i = 0; i < descriptors.size(); ++i) {
            PropertyList desc = descriptors.getPropertyList(i);
            if (desc.getProperty("saveasfield").equals("Y") && desc.getProperty("fieldid").length() > 0) {
                return true;
            }
            PropertyListCollection subdescriptors = desc.getCollectionNotNull("subdescriptors");
            for (int ii = 0; ii < subdescriptors.size(); ++ii) {
                PropertyList subdesc = subdescriptors.getPropertyList(ii);
                if (!subdesc.getProperty("saveasfield").equals("Y") || subdesc.getProperty("fieldid").length() <= 0) continue;
                return true;
            }
        }
        return false;
    }

    public static String getMoleculeDescriptors(ChemicalFileDetails fileDetails, IAtomContainer molecule, PropertyList params, TranslationProcessor tp) {
        StringBuffer out = new StringBuffer();
        try {
            DescriptorEngine descriptorEngine = new DescriptorEngine(IMolecularDescriptor.class, null);
            descriptorEngine.process(molecule);
            Map prop = molecule.getProperties();
            Iterator iterator = prop.keySet().iterator();
            HashMap<String, IDescriptorResult> values = new HashMap<String, IDescriptorResult>();
            HashMap<String, String[]> names = new HashMap<String, String[]>();
            while (iterator.hasNext()) {
                Object l = iterator.next();
                if (!(l instanceof DescriptorSpecification)) continue;
                String key = ((DescriptorSpecification)l).getSpecificationReference().toString();
                String code = key.substring(key.lastIndexOf("#") + 1);
                IDescriptorResult result = ((DescriptorValue)prop.get(l)).getValue();
                values.put(code, result);
                names.put(code, ((DescriptorValue)prop.get(l)).getNames());
            }
            int totalsize = 0;
            ArrayList<StringBuffer> valueouts = new ArrayList<StringBuffer>();
            ArrayList<Integer> valueoutssize = new ArrayList<Integer>();
            PropertyListCollection descriptors = params.getCollectionNotNull("descriptors");
            for (int i = 0; i < descriptors.size(); ++i) {
                PropertyList desc = descriptors.getPropertyList(i);
                String code = desc.getProperty("code");
                String show = desc.getProperty("show", "N");
                String title = desc.getProperty("title", code);
                int sigfigs = ChemicalViewer.getInt(desc.getProperty("sigfigs"), 5);
                String units = desc.getProperty("units");
                boolean save = desc.getProperty("saveasfield").equals("Y");
                String fieldid = desc.getProperty("fieldid");
                HashMap<String, String> subdesctitle = new HashMap<String, String>();
                HashMap<String, Integer> subdescsigfig = new HashMap<String, Integer>();
                HashMap<String, String> subdescsaveasfield = new HashMap<String, String>();
                HashMap<String, String> subdescfieldid = new HashMap<String, String>();
                PropertyListCollection subdescriptors = desc.getCollectionNotNull("subdescriptors");
                for (int ii = 0; ii < subdescriptors.size(); ++ii) {
                    PropertyList subdesc = subdescriptors.getPropertyList(ii);
                    String subcode = subdesc.getProperty("code");
                    subdesctitle.put(subcode, subdesc.getProperty("title", subcode));
                    subdescsigfig.put(subcode, ChemicalViewer.getInt(subdesc.getProperty("sigfigs"), sigfigs));
                    subdescsaveasfield.put(subcode, subdesc.getProperty("saveasfield", subcode));
                    subdescfieldid.put(subcode, subdesc.getProperty("fieldid", subcode));
                }
                if (!show.equals("Y")) continue;
                IDescriptorResult value = (IDescriptorResult)values.get(code);
                String[] name = (String[])names.get(code);
                if (value == null || value.length() <= 0) continue;
                StringBuffer valueout = new StringBuffer();
                valueouts.add(valueout);
                int size = 0;
                valueout.append("<tr><td>" + tp.translate(title) + "</td><td>");
                StringBuffer subdescriptorout = new StringBuffer();
                try {
                    String subtitle;
                    if (value instanceof DoubleResult) {
                        subdescriptorout.append("<table cellspacing=\"0\" cellpadding=\"0\">");
                        BigDecimal bd = new BigDecimal(((DoubleResult)value).doubleValue());
                        bd = bd.round(new MathContext(sigfigs));
                        subdescriptorout.append("<tr><td>" + bd.toPlainString() + units + "</td></tr>");
                        subdescriptorout.append("</table>");
                        ++size;
                        if (save && fieldid.length() > 0) {
                            fileDetails.addField(fieldid, bd.doubleValue());
                        }
                    } else if (value instanceof DoubleArrayResult) {
                        subdescriptorout.append("<table cellspacing=\"0\" cellpadding=\"0\">");
                        for (int k = 0; k < value.length(); ++k) {
                            String subfieldid;
                            if (subdesctitle.size() != 0 && !subdesctitle.keySet().contains(name[k])) continue;
                            subtitle = subdesctitle.keySet().contains(name[k]) ? (String)subdesctitle.get(name[k]) : name[k];
                            int subsigfigs = subdesctitle.keySet().contains(name[k]) ? (Integer)subdescsigfig.get(name[k]) : sigfigs;
                            BigDecimal bd = new BigDecimal(((DoubleArrayResult)value).get(k));
                            bd = bd.round(new MathContext(subsigfigs));
                            subdescriptorout.append("<tr><td>" + tp.translate(subtitle) + "</td><td>&nbsp;=&nbsp;</td><td>" + bd.toPlainString() + units + "</td></tr>");
                            ++size;
                            if (!"Y".equals(subdescsaveasfield.get(name[k])) || (subfieldid = (String)subdescfieldid.get(name[k])) == null || subfieldid.length() <= 0) continue;
                            fileDetails.addField(subfieldid, bd.doubleValue());
                        }
                        subdescriptorout.append("</table>");
                    } else if (value instanceof IntegerArrayResult) {
                        subdescriptorout.append("<table cellspacing=\"0\" cellpadding=\"0\">");
                        for (int k = 0; k < value.length(); ++k) {
                            String subfieldid;
                            if (subdesctitle.size() != 0 && !subdesctitle.keySet().contains(name[k])) continue;
                            subtitle = subdesctitle.keySet().contains(name[k]) ? (String)subdesctitle.get(name[k]) : name[k];
                            subdescriptorout.append("<tr><td>" + tp.translate(subtitle) + "</td><td>&nbsp;=&nbsp;</td><td>" + ((IntegerArrayResult)value).get(k) + units + "</td></tr>");
                            ++size;
                            if (!"Y".equals(subdescsaveasfield.get(name[k])) || (subfieldid = (String)subdescfieldid.get(name[k])) == null || subfieldid.length() <= 0) continue;
                            fileDetails.addField(subfieldid, ((IntegerArrayResult)value).get(k));
                        }
                        subdescriptorout.append("</table>");
                    } else {
                        subdescriptorout.append("<table cellspacing=\"0\" cellpadding=\"0\">");
                        subdescriptorout.append("<tr><td>" + value.toString() + units + "</td></tr>");
                        subdescriptorout.append("</table>");
                        ++size;
                        if (save && fieldid.length() > 0) {
                            fileDetails.addField(fieldid, value);
                        }
                    }
                }
                catch (Exception e) {
                    subdescriptorout.setLength(0);
                    subdescriptorout.append("Error");
                }
                valueout.append(subdescriptorout);
                valueout.append("</td></tr>");
                valueoutssize.add(size);
                totalsize += size;
            }
            int columns = ChemicalViewer.getInt(params.getProperty("columns"), -1);
            if (columns <= 0) {
                int n = totalsize < 10 ? 1 : (columns = totalsize < 20 ? 2 : 3);
            }
            if (totalsize > 0) {
                out.append("<table><tr>");
                int next = totalsize / columns;
                int count = 0;
                for (int i = 0; i < valueouts.size(); ++i) {
                    if (count >= next) {
                        out.append("</table>");
                        out.append("</td>");
                    }
                    if (i == 0 || count >= next) {
                        count = 0;
                        out.append("<td valign=\"top\">");
                        out.append("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">");
                    }
                    out.append((StringBuffer)valueouts.get(i));
                    count += ((Integer)valueoutssize.get(i)).intValue();
                }
                out.append("</table>");
                out.append("</td>");
                out.append("</tr></table>");
            } else {
                out.append("No descriptors defined.");
            }
        }
        catch (CDKException e) {
            out.append("Unable to render descriptors. Error: " + e.getMessage());
            Trace.logError("Failed to derive descriptors: " + e.getMessage(), e);
        }
        return out.toString();
    }

    public static String generateMoleculeImage(ChemicalFileDetails fileDetails, IAtomContainer molecule) throws CDKException, IOException {
        StringBuffer out = new StringBuffer();
        molecule.setProperty((Object)"cdk:Title", (Object)"My Molecule");
        boolean isSVG = fileDetails.getRenderFormat().equalsIgnoreCase("SVG");
        DepictionGenerator dptgen = ChemicalViewer.getDepictionGenerator(fileDetails, isSVG);
        if (fileDetails.getHighlightAtoms().length() > 0) {
            Color highlightColor = SparklineStreamer.getColor(fileDetails.getHighlightColor());
            String[] atomString = StringUtil.split(fileDetails.getHighlightAtoms(), fileDetails.getHighlightAtoms().contains(",") ? "," : " ");
            HashSet<IAtom> highlights = new HashSet<IAtom>();
            for (int i = 0; i < atomString.length; ++i) {
                try {
                    Integer iAtom = Integer.parseInt(atomString[i].trim());
                    IAtom atom = molecule.getAtom(iAtom - 1);
                    if (atom == null) continue;
                    atom.setProperty((Object)"stdgen.highlight.color", (Object)highlightColor);
                    highlights.add(atom);
                    continue;
                }
                catch (NumberFormatException numberFormatException) {
                    // empty catch block
                }
            }
            if (highlights.size() > 0) {
                dptgen.withOuterGlowHighlight();
                dptgen.withHighlight(highlights, highlightColor);
                dptgen.withOuterGlowHighlight();
            }
        }
        if (isSVG) {
            String svg = dptgen.depict(molecule).toSvgStr();
            out.append(svg);
        } else {
            BufferedImage image = dptgen.depict(molecule).toImg();
            ImageEncoder imageEncoder = ImageEncoderFactory.newInstance((String)"png");
            byte[] imagedata = imageEncoder.encode(image);
            out.append("<img src=\"data:image/png;base64,");
            out.append(Base64.encodeBase64String((byte[])imagedata));
            out.append("\">");
            out.append("<br>");
        }
        return out.toString();
    }

    public static String generateReactionImage(ChemicalFileDetails fileDetails, IReaction reaction) throws CDKException, IOException {
        StringBuffer out = new StringBuffer();
        boolean isSVG = fileDetails.getRenderFormat().equalsIgnoreCase("SVG");
        DepictionGenerator dptgen = ChemicalViewer.getDepictionGenerator(fileDetails, isSVG);
        if (isSVG) {
            String svg = dptgen.depict(reaction).toSvgStr();
            out.append(svg);
        } else {
            BufferedImage image = dptgen.depict(reaction).toImg();
            ImageEncoder imageEncoder = ImageEncoderFactory.newInstance((String)"png");
            byte[] imagedata = imageEncoder.encode(image);
            out.append("<img src=\"data:image/png;base64,");
            out.append(Base64.encodeBase64String((byte[])imagedata));
            out.append("\">");
            out.append("<br>");
        }
        return out.toString();
    }

    private static ISimpleChemObjectReader getChemObjectReader(String chemType) {
        ISimpleChemObjectReader reader = null;
        for (int i = 0; i < chemTypes.length; ++i) {
            String[] type = chemTypes[i];
            if (!chemType.equalsIgnoreCase(type[CHEMTYPE_CODE])) continue;
            try {
                reader = (ISimpleChemObjectReader)Class.forName(type[CHEMTYPE_CLASS]).newInstance();
                continue;
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return reader;
    }

    public static DepictionGenerator getDepictionGenerator(ChemicalFileDetails fileDetails, boolean isSVG) {
        DepictionGenerator dptgen = new DepictionGenerator();
        int imageWidth = fileDetails.getImageWidth();
        int imageHeight = fileDetails.getImageHeight();
        if (isSVG) {
            imageWidth = (int)((double)imageWidth / 3.78);
            imageHeight = (int)((double)imageHeight / 3.78);
        }
        dptgen = dptgen.withSize((double)imageWidth, (double)imageHeight);
        dptgen = dptgen.withFillToFit();
        if (fileDetails.isShowAtomColors()) {
            dptgen = dptgen.withAtomColors();
        }
        if (fileDetails.isShowAtomNumbers()) {
            dptgen = dptgen.withAtomNumbers();
        }
        if (fileDetails.getShowCarbons().equals("all")) {
            dptgen = dptgen.withCarbonSymbols();
        } else if (fileDetails.getShowCarbons().equals("terminal")) {
            dptgen = dptgen.withTerminalCarbons();
        }
        dptgen = dptgen.withOuterGlowHighlight();
        return dptgen;
    }

    @Override
    public String getLiveIndexingText() {
        if (this.hasContents()) {
            String contents = this.getContents();
            StringBuffer html = new StringBuffer();
            try {
                PropertyList fileDetails = contents.length() > 0 ? new PropertyList(new JSONObject(contents)) : new PropertyList();
                html.append(ChemicalViewerAjaxHandler.getViewArea(fileDetails, this.getWorksheetItemId(), this.getWorksheetItemVersionId()));
                Document jdoc = Jsoup.parse((String)html.toString());
                return jdoc.body().text();
            }
            catch (Exception e) {
                this.logError("Could not load content.", e);
                return "";
            }
        }
        return "";
    }

    @Override
    public String validateContents(String contents) throws SapphireException {
        contents = HttpUtil.decodeURIComponent(contents);
        try {
            PropertyList pl = new PropertyList(new JSONObject(contents));
            PropertyList markup = pl.getPropertyList("markup");
            String jsfields = markup.getProperty("fields");
            if (jsfields.length() > 0) {
                JSONObject jso = new JSONObject(jsfields);
                Iterator keys = jso.keys();
                WorksheetItemFields worksheetItemFields = this.getWorksheetItemFields();
                HashSet<String> addoredit = new HashSet<String>();
                while (keys.hasNext()) {
                    String fieldid = (String)keys.next();
                    Object val = jso.get(fieldid);
                    addoredit.add(fieldid);
                    if (!worksheetItemFields.contains(fieldid)) {
                        String type = "number";
                        worksheetItemFields.addField(fieldid, fieldid, type, 1, null);
                        worksheetItemFields.enterFieldValue(fieldid, 0, val == null ? "" : val.toString());
                        continue;
                    }
                    String fieldDatatype = worksheetItemFields.getFieldDatatype(fieldid);
                    String type = val == null ? fieldDatatype : "number";
                    worksheetItemFields.updateFieldDatatype(fieldid, type);
                    worksheetItemFields.enterFieldValue(fieldid, 0, val == null ? "" : val.toString());
                }
                Iterator<String> iterator = worksheetItemFields.iterator();
                while (iterator.hasNext()) {
                    String fieldid = iterator.next();
                    if (addoredit.contains(fieldid)) continue;
                    worksheetItemFields.deleteField(fieldid);
                }
                worksheetItemFields.save();
            }
        }
        catch (JSONException e) {
            throw new SapphireException("Failed to validate contents. Reason: " + e.getMessage(), e);
        }
        return contents;
    }
}

