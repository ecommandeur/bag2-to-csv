package com.dimins.bag2;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.util.StAXUtils;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringJoiner;

import static com.dimins.bag2.BAG2toCsvApp.*;

public class Ligplaats {

    public static String LIG_HOOFDADRES_NUMMERAANDUIDING = "hoofdadresNummeraanduiding";
    public static String LIG_IDENTIFICATIE = "identificatie";
    public static String LIG_STATUS = "status";
    public static String LIG_GECONSTATEERD = "geconstateerd";
    public static String LIG_DOCUMENTDATUM = "documentdatum";
    public static String LIG_DOCUMENTNUMMER = "documentnummer";
    public static String LIG_GEOMETRIE = "geometrie";
    public static String LIG_GEOM_TYPE = "geom_type";
    public static String LIG_GEOM_X = "geom_x"; //check naming convention gdal/qgis
    public static String LIG_GEOM_Y = "geom_y"; //check naming convention gdal/qgis

    /**
     * Proces a LigPlaats XML document
     */
    protected static void processLigplaatsXML(File file, HashMap<String, String> params) throws Exception {
        InputStream in = new FileInputStream(file);
        XMLStreamReader reader = StAXUtils.createXMLStreamReader(in);
        try (FileOutputStream fos = new FileOutputStream(params.get(PARAM_OUTPUT_FILENAME));
             OutputStreamWriter streamWriter = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             BufferedWriter bufferedWriter = new BufferedWriter(streamWriter)) {

            bufferedWriter.write(getLigplaatsOutputHeaders(params));
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

    protected static String getLigplaatsOutputHeaders(HashMap<String, String> params) {
        StringJoiner headerValues = new StringJoiner(params.get(PARAM_OUTPUT_DELIMITER));
        headerValues.add(LIG_HOOFDADRES_NUMMERAANDUIDING)
                .add(LIG_IDENTIFICATIE)
                .add(LIG_STATUS)
                .add(LIG_GECONSTATEERD)
                .add(LIG_DOCUMENTDATUM)
                .add(LIG_DOCUMENTNUMMER)
                .add(LIG_GEOM_TYPE)
                .add(LIG_GEOM_X)
                .add(LIG_GEOM_Y)
                .add("\n");
        return headerValues.toString();
    };

    protected static String getLigPlaatsOutputRecord (HashMap<String,String> record, HashMap<String, String> params) {
        StringJoiner rowValues = new StringJoiner(params.get(PARAM_OUTPUT_DELIMITER));
        String defaultValue = "";
        rowValues.add(record.getOrDefault(LIG_HOOFDADRES_NUMMERAANDUIDING, defaultValue))
                .add(record.getOrDefault(LIG_IDENTIFICATIE, defaultValue))
                .add(record.getOrDefault(LIG_STATUS,defaultValue))
                .add(record.getOrDefault(LIG_GECONSTATEERD, defaultValue))
                .add(record.getOrDefault(LIG_DOCUMENTDATUM, defaultValue))
                .add(record.getOrDefault(LIG_DOCUMENTNUMMER, defaultValue))
                .add(record.getOrDefault(LIG_GEOM_TYPE, defaultValue))
                .add(record.getOrDefault(LIG_GEOM_X, defaultValue))
                .add(record.getOrDefault(LIG_GEOM_Y, defaultValue))
                .add("\n");
        return rowValues.toString();
    }

    /**
     * Proces a LigPlaats XML Element
     */
    protected static HashMap<String, String> processLigplaats(OMElement element) throws Exception {
        //We need some intermediate structure to keep data extracted from XML
        //This record can then be passed on to a method that writes to a CSV
        HashMap<String, String> record = new HashMap<>();

        //Iterate over descendants seems better than separate XPaths
        //By iterating over descendants we can iterate all nodes once
        Iterator<OMNode> descendants = element.getDescendants(false);
        while(descendants.hasNext()){
            OMNode node = (OMNode) descendants.next();
            if(node.getType() == 1){
                OMElement nodeEl = (OMElement) node;
                String nodeLocalName = nodeEl.getQName().getLocalPart();
                if(nodeLocalName.equals("heeftAlsHoofdadres")){
                    OMElement nodeFirstEl = nodeEl.getFirstElement();
                    if(nodeFirstEl != null && nodeFirstEl.getQName().getLocalPart().equals("NummeraanduidingRef")) {
                        record.put(LIG_HOOFDADRES_NUMMERAANDUIDING, nodeEl.getFirstElement().getText());
                        //TODO remove println hoofdAdresNummeraanduiding
                        System.out.println("Iter " + LIG_HOOFDADRES_NUMMERAANDUIDING + ": " + nodeEl.getFirstElement().getText());
                    }
                } else if(nodeLocalName.equals(LIG_IDENTIFICATIE)) {
                    record.put(LIG_IDENTIFICATIE, nodeEl.getText());
                } else if(nodeLocalName.equals(LIG_STATUS)){
                    record.put(LIG_STATUS, nodeEl.getText());
                } else if(nodeLocalName.equals(LIG_GECONSTATEERD)){
                    record.put(LIG_GECONSTATEERD, nodeEl.getText());
                } else if(nodeLocalName.equals(LIG_DOCUMENTDATUM)){
                    record.put(LIG_DOCUMENTDATUM, nodeEl.getText());
                } else if(nodeLocalName.equals(LIG_DOCUMENTNUMMER)){
                    record.put(LIG_DOCUMENTNUMMER, nodeEl.getText());
                } else if(nodeLocalName.equals(LIG_GEOMETRIE)) {
                    OMElement nodeFirstEl = nodeEl.getFirstElement();
                    Geometry geometry = null;
                    //TODO descent geometrie until the first gml element is found
                    //Capitalize on the fact that the actual GML geometry element is at most two levels below Objecten:geometrie
                    // e.g. <Objecten:geometrie><gml:Polygon srsName="urn:ogc:def:crs:EPSG::28992" srsDimension="2">...</gml:Polygon></Object:geometrie>
                    //  <Objecten:geometrie><Objecten:punt><gml:Point srsName="urn:ogc:def:crs:EPSG::28992" srsDimension="3">...</gml:Point><Objecten:punt></Object:geometrie>
                    if(nodeFirstEl.getQName().getPrefix().equals("gml")) {
                        System.out.println("Geometrie:" + nodeFirstEl.getQName().getLocalPart());
                        geometry = GeoUtils.parseGeometry(nodeFirstEl);
                    } else {
                        OMElement childFirstEl = nodeFirstEl.getFirstElement();
                        System.out.println("Geometrie:" + childFirstEl.getQName().getLocalPart());
                        geometry = GeoUtils.parseGeometry(childFirstEl);
                    }
                    if(geometry != null){
                        String geometryType = geometry.getGeometryType();
                        record.put(LIG_GEOM_TYPE, geometryType);
                        if(geometryType.equals(Geometry.TYPENAME_POINT)){
                            Point p = (Point) geometry;
                            record.put(LIG_GEOM_X, Double.toString(p.getX()));
                            record.put(LIG_GEOM_Y, Double.toString(p.getY()));
                        } else if (geometryType.equals(Geometry.TYPENAME_POLYGON)){
                            Point p = geometry.getInteriorPoint();
                            record.put(LIG_GEOM_X, Double.toString(p.getX()));
                            record.put(LIG_GEOM_Y, Double.toString(p.getY()));
                        }
                    }
                }
            }
        }
        return record;
    }

}
