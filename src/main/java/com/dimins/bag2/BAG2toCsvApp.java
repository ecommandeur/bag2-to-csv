package com.dimins.bag2;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.util.StAXUtils;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringJoiner;

/**
 * Main class
 */
public class BAG2toCsvApp {

    //Make a separate class with constants?
    public static String NAMESPACE_PREFIX_STANDLEVERING_GENERIEK = "st";
    public static String NAMESPACE_URI_STANDLEVERING_GENERIEK = "http://www.kadaster.nl/schemas/standlevering-generiek/1.0";
    public static String NAMESPACE_PREFIX_OBJECTEN = "Objecten";
    public static String NAMESPACE_URI_OBJECTEN = "www.kadaster.nl/schemas/lvbag/imbag/objecten/v20200601";
    public static String NAMESPACE_PREFIX_OBJECTEN_REF = "Objecten-ref";
    public static String NAMESPACE_URI_OBJECTEN_REF = "www.kadaster.nl/schemas/lvbag/imbag/objecten-ref/v20200601";
    public static String NAMESPACE_PREFIX_SL_BAGEXTRACT = "sl-bag-extract";
    public static String NAMESPACE_URI_SL_BAGEXTRACT = "http://www.kadaster.nl/schemas/lvbag/extract-deelbestand-lvc/v20200601";

    public static String OBJECT_TYPE_LOCAL_NAME_LIGPLAATS = "Ligplaats";
    public static String OBJECT_TYPE_LOCAL_NAME_VERBLIJFSOBJECT = "Verblijfsobject";

    public static String PARAM_INPUT_FILENAME = "input_filename";
    public static String PARAM_OUTPUT_DIR = "output_dir";
    public static String PARAM_OUTPUT_FILENAME = "output_filename"; //determined from input_filename and output dir
    public static String PARAM_OUTPUT_DELIMITER = "output_delimiter";

    public static String LIGPLAATS_HOOFDADRES_NUMMERAANDUIDING = "HoofdadresNummeraanduiding";
    public static String LIGPLAATS_IDENTIFICATIE = "Identificatie";

    public static void main(String[] args) {
        //TODO pass in params via argument
        HashMap<String,String> params = new HashMap();
        params.put(PARAM_OUTPUT_DELIMITER, "\t");
        params.put(PARAM_OUTPUT_DIR, "D:/temp");
        params.put(PARAM_INPUT_FILENAME, "src/test/resources/xml/lig_excerpt.xml");
        params.put(PARAM_OUTPUT_FILENAME, "D:/temp/lig_excerpt.txt");

        //TODO determine objecttype from filename
        String objectType = "Ligplaats";

//      params.put(PARAM_INPUT_FILENAME, "src/test/resources/xml/vbo_excerpt.xml");
//        String objectType = "Verblijfsobject";
        //params.put(PARAM_INPUT_FILENAME, "src/test/resources/xml/lig_excerpt.xml");
        File file = new File(params.get(PARAM_INPUT_FILENAME));
        if(!file.exists()) {
            System.out.println("Input " + params.get(PARAM_INPUT_FILENAME) + " not found");
            System.exit(1);
        }

        try {
            if(objectType.equalsIgnoreCase("ligplaats")) {
                processLigPlaatsXML(file, params);
            } else if (objectType.equalsIgnoreCase("verblijfsobject")){
                processVerblijfsObjectXML(file, params);
            }
        } catch(Exception e) {
            System.out.println("ERROR:" + e.getLocalizedMessage());
        }
    }

    /**
     * Parse commandline arguments passed to the script
     */
    private static HashMap<String, String> parseArguments(String[] args){
        HashMap<String, String> params = new HashMap<>();

        for (String arg : args) {
            int splitIndex = arg.indexOf("=");
            if (splitIndex < 0) {
                System.out.println("Malformed argument: " + arg + ". Expected format name=value.\n");
            }
            String argName = arg.substring(0, splitIndex);
            String argValue = arg.substring(splitIndex + 1);
            String[] splitArg = {argName, argValue};
            if(PARAM_INPUT_FILENAME.equals(splitArg[0])) {
                params.put(PARAM_INPUT_FILENAME, splitArg[1]);
            } else if (PARAM_OUTPUT_DIR.equals(splitArg[0])) {
                params.put(PARAM_OUTPUT_DIR, splitArg[1]);
            } else if (PARAM_OUTPUT_DELIMITER.equals(splitArg[0])) {
                params.put(PARAM_OUTPUT_DELIMITER, splitArg[1]);
            }
        }
        return params;
    }

    /**
     * Proces a LigPlaats XML document
     */
    protected static void processLigPlaatsXML(File file, HashMap<String, String> params) throws Exception {
        InputStream in = new FileInputStream(file);
        XMLStreamReader reader = StAXUtils.createXMLStreamReader(in);
        try (FileOutputStream fos = new FileOutputStream(params.get(PARAM_OUTPUT_FILENAME));
             OutputStreamWriter streamWriter = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             BufferedWriter bufferedWriter = new BufferedWriter(streamWriter)) {

            bufferedWriter.write(getLigPlaatsOutputHeaders(params));
            HashMap<String,String> record = new HashMap<>();
            while (reader.hasNext()) {
                if (reader.getEventType() == XMLStreamReader.START_ELEMENT &&
                        reader.getName().equals(new QName(NAMESPACE_URI_OBJECTEN, OBJECT_TYPE_LOCAL_NAME_LIGPLAATS, NAMESPACE_PREFIX_OBJECTEN))) {
                        OMElement element =
                                OMXMLBuilderFactory.createStAXOMBuilder(reader).getDocumentElement();
                        element.build();
                        record = processLigplaats(element);
                        bufferedWriter.write(getLigPlaatsOutputRecord(record, params));
                    reader.next();
                } else {
                    reader.next();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Because Axiom uses deferred parsing, the stream must be closed AFTER
            // processing the document (unless OMElement#build() is called)
            in.close();
        }
    }

    /**
     * Proces a VerblijfsObject XML document
     */
    protected static void processVerblijfsObjectXML(File file, HashMap<String, String> params) throws Exception {
        InputStream in = new FileInputStream(file);
        XMLStreamReader reader = StAXUtils.createXMLStreamReader(in);
        try {
            while (reader.hasNext()) {
                if (reader.getEventType() == XMLStreamReader.START_ELEMENT &&
                        reader.getName().equals(new QName(NAMESPACE_URI_OBJECTEN, OBJECT_TYPE_LOCAL_NAME_VERBLIJFSOBJECT, NAMESPACE_PREFIX_OBJECTEN))){
        OMElement element =
                OMXMLBuilderFactory.createStAXOMBuilder(reader).getDocumentElement();
        element.build();
        processVerblijfsObject(element);
                    reader.next();
                } else {
                    reader.next();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            in.close();
        }
    }

    /**
     * Proces a LigPlaats XML Element
     */
    protected static HashMap<String, String> processLigplaats(OMElement element) throws Exception {
        //We need some intermediate structure to keep data extracted from XML
        //This record can then be passed on to a method that writes to a CSV
        HashMap<String, String> record = new HashMap<>();

        //Iterate over descendants seems better than separate XPaths
        //since we can iterate all nodes once
        Iterator<OMNode> descendants = element.getDescendants(false);
        while(descendants.hasNext()){
            OMNode node = (OMNode) descendants.next();
            //System.out.println("node type: " + node.getType() + " class:" + node.getClass());
            if(node.getType() == 1){
                OMElement nodeEl = (OMElement) node;

                String nodeLocalName = nodeEl.getQName().getLocalPart();
                if(nodeLocalName.equals("heeftAlsHoofdadres")){
                    System.out.println("Iter nummeraanduiding:" + nodeEl.getFirstElement().getText());
                    OMElement nodeFirstEl = nodeEl.getFirstElement();
                    if(nodeFirstEl != null && nodeFirstEl.getQName().getLocalPart().equals("NummeraanduidingRef")) {
                        record.put(LIGPLAATS_HOOFDADRES_NUMMERAANDUIDING, nodeEl.getFirstElement().getText());
                    }
                } else if(nodeLocalName.equals("identificatie")){
                    System.out.println("Iter identificatie:" + nodeEl.getText());
                    record.put(LIGPLAATS_IDENTIFICATIE, nodeEl.getText());
                } else if(nodeLocalName.equals("geometrie")) {
                    System.out.println("Geometrie:" + nodeEl.getFirstElement().getQName().getLocalPart());
                    parseGeometry(nodeEl);
                }
            }
        }
        return record;
    }

    protected static String getLigPlaatsOutputHeaders (HashMap<String, String> params) {
        StringJoiner headerValues = new StringJoiner(params.get(PARAM_OUTPUT_DELIMITER));
        headerValues.add(LIGPLAATS_HOOFDADRES_NUMMERAANDUIDING)
                .add(LIGPLAATS_IDENTIFICATIE)
                .add("\n");
        return headerValues.toString();
    };

    protected static String getLigPlaatsOutputRecord (HashMap<String,String> record, HashMap<String, String> params) {
        StringJoiner rowValues = new StringJoiner(params.get(PARAM_OUTPUT_DELIMITER));
        rowValues.add(record.getOrDefault(LIGPLAATS_HOOFDADRES_NUMMERAANDUIDING, ""))
                .add(record.getOrDefault(LIGPLAATS_IDENTIFICATIE, ""))
                .add("\n");
        return rowValues.toString();
    }

    protected static void parseGeometry(OMElement geomEl) {
        //Ideally we get the first gml element and pass that to GeoTools to parse into a geometry
        //That does not seem to be all that easy... GML is a complex beast
        //https://docs.geotools.org/latest/userguide/library/jts/geometry.html
        //
        //perhaps use XPath here?
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        Iterator<OMNode> descendants = geomEl.getDescendants(false);
        Geometry geometry;
        while(descendants.hasNext()) {
            OMNode node = (OMNode) descendants.next();
            if(node.getType() == 1){
                OMElement nodeEl = (OMElement) node;
                String nodeLocalName = nodeEl.getQName().getLocalPart();
                //linear ring will be encoded as a posList
                if(nodeLocalName.equals("LinearRing")){
                    ((OMElement) node).getFirstElement();
                }
            }
        }
    }

    /**
     *
     * @param element
     * @throws Exception TODO proper exception handling
     */
    protected static void processVerblijfsObject(OMElement element) throws Exception {
        HashMap<String, String> record = new HashMap<>();

        //Shall we iterate over descendants or use XPath
        Iterator<OMNode> descendants = element.getDescendants(false);
        while (descendants.hasNext()) {
            OMNode node = (OMNode) descendants.next();
            //System.out.println("node type: " + node.getType() + " class:" + node.getClass());
            if (node.getType() == 1) {
                OMElement nodeEl = (OMElement) node;

                if (nodeEl.getQName().getLocalPart().equals("heeftAlsHoofdadres")) {
                    System.out.println("Iter nummeraanduiding:" + nodeEl.getFirstElement().getText());
                    OMElement nodeFirstEl = nodeEl.getFirstElement();
                    if (nodeFirstEl != null && nodeFirstEl.getQName().getLocalPart().equals("NummeraanduidingRef")) {
                        record.put("HoofdadresNummeraanduiding", nodeEl.getFirstElement().getText());
                    }
                }
                if (nodeEl.getQName().getLocalPart().equals("identificatie")) {
                    System.out.println("Iter identificatie:" + nodeEl.getText());
                    record.put("Identificatie", nodeEl.getText());
                }

            }
        }
    }
}