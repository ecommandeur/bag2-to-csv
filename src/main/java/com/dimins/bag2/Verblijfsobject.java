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

public class Verblijfsobject {

    public static String VBO_HOOFDADRES_NUMMERAANDUIDING = "hoofdadresNummeraanduiding";
    public static String VBO_IDENTIFICATIE = "identificatie";
    public static String VBO_GEBRUIKSDOEL = "gebruiksdoel";
    public static String VBO_OPPERVLAKTE = "oppervlakte";
    public static String VBO_STATUS = "status";
    public static String VBO_GECONSTATEERD = "geconstateerd";
    public static String VBO_DOCUMENTDATUM = "documentdatum";
    public static String VBO_DOCUMENTNUMMER = "documentnummer";
    public static String VBO_GEOMETRIE = "geometrie";
    public static String VBO_GEOM_TYPE = "geom_type";
    public static String VBO_GEOM_X = "geom_x"; //check naming convention gdal/qgis
    public static String VBO_GEOM_Y = "geom_y"; //check naming convention gdal/qgis

    /**
     * Proces a VerblijfsObject XML document
     */
    protected static void processVerblijfsobjectXML(File file, HashMap<String, String> params) throws Exception {
        InputStream in = new FileInputStream(file);
        XMLStreamReader reader = StAXUtils.createXMLStreamReader(in);
        try (FileOutputStream fos = new FileOutputStream(params.get(PARAM_OUTPUT_FILENAME));
             OutputStreamWriter streamWriter = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             BufferedWriter bufferedWriter = new BufferedWriter(streamWriter)) {

            bufferedWriter.write(getVerblijfsobjectOutputHeaders(params));
            HashMap<String,String> record = new HashMap<>();
            while (reader.hasNext()) {
                if (reader.getEventType() == XMLStreamReader.START_ELEMENT &&
                        reader.getName().equals(new QName(NAMESPACE_URI_OBJECTEN, OBJECT_TYPE_LOCAL_NAME_VERBLIJFSOBJECT, NAMESPACE_PREFIX_OBJECTEN))){
                    OMElement element =
                            OMXMLBuilderFactory.createStAXOMBuilder(reader).getDocumentElement();
                    element.build();
                    record = processVerblijfsobject(element);
                    bufferedWriter.write(getVerblijfsobjectOutputRecord(record, params));
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
     * Get headers for delimited text output
     */
    protected static String getVerblijfsobjectOutputHeaders(HashMap<String, String> params) {
        StringJoiner headerValues = new StringJoiner(params.get(PARAM_OUTPUT_DELIMITER));
        headerValues.add(VBO_HOOFDADRES_NUMMERAANDUIDING)
                .add(VBO_IDENTIFICATIE)
                .add(VBO_GEBRUIKSDOEL)
                .add(VBO_OPPERVLAKTE)
                .add(VBO_STATUS)
                .add(VBO_GECONSTATEERD)
                .add(VBO_DOCUMENTDATUM)
                .add(VBO_DOCUMENTNUMMER)
                .add(VBO_GEOM_TYPE)
                .add(VBO_GEOM_X)
                .add(VBO_GEOM_Y)
                .add("\n");
        return headerValues.toString();
    };

    protected static String getVerblijfsobjectOutputRecord(HashMap<String,String> record, HashMap<String, String> params) {
        StringJoiner rowValues = new StringJoiner(params.get(PARAM_OUTPUT_DELIMITER));
        String defaultValue = "";
        //TODO ensure that field index headers and records match
        rowValues.add(record.getOrDefault(VBO_HOOFDADRES_NUMMERAANDUIDING, defaultValue))
                .add(record.getOrDefault(VBO_IDENTIFICATIE, defaultValue))
                .add(record.getOrDefault(VBO_GEBRUIKSDOEL, defaultValue))
                .add(record.getOrDefault(VBO_OPPERVLAKTE, defaultValue))
                .add(record.getOrDefault(VBO_STATUS,defaultValue))
                .add(record.getOrDefault(VBO_GECONSTATEERD, defaultValue))
                .add(record.getOrDefault(VBO_DOCUMENTDATUM, defaultValue))
                .add(record.getOrDefault(VBO_DOCUMENTNUMMER, defaultValue))
                .add(record.getOrDefault(VBO_GEOM_TYPE, defaultValue))
                .add(record.getOrDefault(VBO_GEOM_X, defaultValue))
                .add(record.getOrDefault(VBO_GEOM_Y, defaultValue))
                .add("\n");
        return rowValues.toString();
    }

    /**
     *
     * @param element
     * @throws Exception TODO proper exception handling
     */
    protected static HashMap<String, String> processVerblijfsobject(OMElement element) throws Exception {
        HashMap<String, String> record = new HashMap<>();

        //Iterate over descendants one time versus separate XPath calls
        Iterator<OMNode> descendants = element.getDescendants(false);
        while (descendants.hasNext()) {
            OMNode node = (OMNode) descendants.next();
            //System.out.println("node type: " + node.getType() + " class:" + node.getClass());
            if (node.getType() == 1) {
                OMElement nodeEl = (OMElement) node;
                String nodeLocalName = nodeEl.getQName().getLocalPart();
                if (nodeLocalName.equals("heeftAlsHoofdadres")) {
                    OMElement nodeFirstEl = nodeEl.getFirstElement();
                    if (nodeFirstEl != null && nodeFirstEl.getQName().getLocalPart().equals("NummeraanduidingRef")) {
                        record.put(VBO_HOOFDADRES_NUMMERAANDUIDING, nodeEl.getFirstElement().getText());
                        System.out.println("Iter " + VBO_HOOFDADRES_NUMMERAANDUIDING + ":" + nodeEl.getFirstElement().getText());
                    }
                } else if (nodeLocalName.equals(VBO_IDENTIFICATIE)) {
                    record.put(VBO_IDENTIFICATIE, nodeEl.getText());
                } else if (nodeLocalName.equals(VBO_GEBRUIKSDOEL)) {
                    record.put(VBO_GEBRUIKSDOEL, nodeEl.getText());
                } else if (nodeLocalName.equals(VBO_OPPERVLAKTE)) {
                    record.put(VBO_OPPERVLAKTE, nodeEl.getText());
                } else if(nodeLocalName.equals(VBO_STATUS)){
                    record.put(VBO_STATUS, nodeEl.getText());
                } else if(nodeLocalName.equals(VBO_GECONSTATEERD)){
                    record.put(VBO_GECONSTATEERD, nodeEl.getText());
                } else if(nodeLocalName.equals(VBO_DOCUMENTDATUM)){
                    record.put(VBO_DOCUMENTDATUM, nodeEl.getText());
                } else if(nodeLocalName.equals(VBO_DOCUMENTNUMMER)){
                    record.put(VBO_DOCUMENTNUMMER, nodeEl.getText());
                } else if(nodeLocalName.equals(VBO_GEOMETRIE)) {
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
                        record.put(VBO_GEOM_TYPE, geometryType);
                        if(geometryType.equals(Geometry.TYPENAME_POINT)){
                            Point p = (Point) geometry;
                            record.put(VBO_GEOM_X, Double.toString(p.getX()));
                            record.put(VBO_GEOM_Y, Double.toString(p.getY()));
                        } else if (geometryType.equals(Geometry.TYPENAME_POLYGON)){
                            Point p = geometry.getInteriorPoint();
                            record.put(VBO_GEOM_X, Double.toString(p.getX()));
                            record.put(VBO_GEOM_Y, Double.toString(p.getY()));
                        }
                    }
                }
            }
        }
        return record;
    }

}
