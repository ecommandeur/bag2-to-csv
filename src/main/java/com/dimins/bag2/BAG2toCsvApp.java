package com.dimins.bag2;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.util.StAXUtils;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.jts.geom.*;

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
                    OMElement nodeFirstEl = nodeEl.getFirstElement();
                    //Capitalize on the fact that the actual GML geometry element is at most two levels below Objecten:geometrie
                    // e.g. <Objecten:geometrie><gml:Polygon srsName="urn:ogc:def:crs:EPSG::28992" srsDimension="2">...</gml:Polygon></Object:geometrie>
                    //  <Objecten:geometrie><Objecten:punt><gml:Point srsName="urn:ogc:def:crs:EPSG::28992" srsDimension="3">...</gml:Point><Objecten:punt></Object:geometrie>
                    if(nodeFirstEl.getQName().getPrefix().equals("gml")) {
                        System.out.println("Geometrie:" + nodeFirstEl.getQName().getLocalPart());
                        parseGeometry(nodeFirstEl);
                    } else {
                        OMElement childFirstEl = nodeFirstEl.getFirstElement();
                        System.out.println("Geometrie:" + childFirstEl.getQName().getLocalPart());
                        parseGeometry(childFirstEl);
                    }
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
        //However, that does not seem to be all that easy... GML processing (in GeoTools) is a complex beast
        //Also the GML we get in the BAG export is predictable, so it is feasible to parse ourselves
        //https://docs.geotools.org/latest/userguide/library/jts/geometry.html
        //
        //Geometry geometry;
        String geom = geomEl.getQName().getLocalPart();
        switch (geom) {
            case "Polygon":
                //geometry = parsePolygon(geomEl);
                parsePolygon(geomEl);
                break;
            case "Point":
                //geometry = parsePoint(geomEl);
                parsePoint(geomEl);
        }
    }

    protected static Polygon parsePolygon(OMElement geomEl) {
        //TODO get srsDimension from Polygon
        // Does srsDimension always need to be declared on top-level geom?
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        Iterator<OMElement> geomChildren = geomEl.getChildElements();
        LinearRing shell = null;
        while(geomChildren.hasNext()) {
            OMElement el = geomChildren.next();
            System.out.println("GeomEl child: " + el.getQName().getLocalPart());
            if(el.getQName().getLocalPart() == "exterior"){
                OMElement exteriorFirstChildEl = el.getFirstElement();
                if(exteriorFirstChildEl.getQName().getLocalPart() == "LinearRing"){
                    shell = parseLinearRing(exteriorFirstChildEl);
                }
            }
        }
        Polygon polygon = geometryFactory.createPolygon(shell);
        System.out.println("Polygon as WKT: " + polygon.toString());
        return polygon;
    }

    protected static LinearRing parseLinearRing(OMElement ringEl) {
        OMElement firstChildEl = ringEl.getFirstElement();
        Coordinate[] coordinates = null;
        if(firstChildEl.getQName().getLocalPart() == "posList") {
            //Attribute does not have a prefix or namespace
            OMAttribute count = firstChildEl.getAttribute(new QName("count"));
            System.out.println("count attr value: " + count.getAttributeValue());
            //System.out.println("posList value: " + firstChildEl.getText());
            coordinates = parsePosList(firstChildEl.getText());
        }
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        LinearRing linearRing = geometryFactory.createLinearRing(coordinates);
        System.out.println("LinearRing centroid: " + linearRing.getCentroid());
        System.out.println("LinearRing interior point: " + linearRing.getInteriorPoint());
        System.out.println("LinearRing as WKT: " + linearRing.toString());
        return linearRing;
    }

    protected static Coordinate[] parsePosList(String posList) {
        String[] posValues = posList.split("\\s+");
        System.out.println("coords length: " + posValues.length);
        //TODO value to divide by is determined by SrSDimension
        //TODO ensure we always get an integer...
        int coordinatesSize = posValues.length / 2; //should be equal to count attribute
        Coordinate[] coordinates = new Coordinate[coordinatesSize];
        int j = 0;
        int max = posValues.length - 1;
        for(int i = 0; i <= max;i++){
            //TODO should we parse as doubles?
            System.out.println("posValues " + i + ":"+ posValues[i]);
            System.out.println("posValues " + (i+1) + ":"+ posValues[i+1]);
            Double x = Double.parseDouble(posValues[i]);
            Double y = Double.parseDouble(posValues[i+1]);
            Coordinate c = new Coordinate(x,y);
            coordinates[j] = c;
            i++; //increment by another one
            j++;
        }
        return coordinates;
    }

    protected static Geometry parsePoint(OMElement geomEl) {
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        Point geometry = geometryFactory.createPoint(new Coordinate(1,2));
        return geometry;
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