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

import static com.dimins.bag2.BAG2toCsvApp.*;

public class Verblijfsobject {

    public static String VBO_HOOFDADRES = "heeftAlsHoofdadres";
    public static String VBO_HOOFDADRES_NUMMERAANDUIDING = "hoofdadresNummeraanduiding";
    public static String VBO_VOORKOMEN_ID = "voorkomenidentificatie";
    public static String VBO_VOORKOMEN_BEGIN_GELDIGHEID = "beginGeldigheid";
    public static String VBO_VOORKOMEN_EIND_GELDIGHEID = "eindGeldigheid";
    public static String VBO_IDENTIFICATIE = "identificatie";
    public static String VBO_GEBRUIKSDOEL = "gebruiksdoel";
    public static String VBO_OPPERVLAKTE = "oppervlakte";
    public static String VBO_STATUS = "status";
    public static String VBO_GECONSTATEERD = "geconstateerd";
    public static String VBO_DOCUMENTDATUM = "documentdatum";
    public static String VBO_DOCUMENTNUMMER = "documentnummer";
    public static String VBO_GEOMETRIE = "geometrie";
    public static String VBO_GEOM_TYPE = "geom_type";
    public static String VBO_GEOM_IS_VALID = "geom_is_valid";
    public static String VBO_GEOM_X = "geom_x"; //check naming convention gdal/qgis
    public static String VBO_GEOM_Y = "geom_y"; //check naming convention gdal/qgis
    public static String VBO_GEOM_X_WGS84 = "geom_x_wgs84"; //check naming convention gdal/qgis
    public static String VBO_GEOM_Y_WGS84 = "geom_y_wgs84"; //check naming convention gdal/qgis

    /**
     * Proces a VerblijfsObject XML document
     *
     * TODO generate mapping from identificatie VBO to pandref
     * TODO generate geo format output (geojson) with geometries in featurecollection???
     *   geojson simple featurecollection should only contain geometries of one kind if I am correct...
     *   we could only write geom if it is a polygon
     */
    protected static void processVerblijfsobjectXML(File file, HashMap<String, String> params) throws Exception {
        InputStream in = new FileInputStream(file);
        XMLStreamReader reader = StAXUtils.createXMLStreamReader(in);
        try (FileOutputStream fos = new FileOutputStream(params.get(PARAM_OUTPUT_FILENAME));
             OutputStreamWriter streamWriter = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             BufferedWriter bufferedWriter = new BufferedWriter(streamWriter)) {

            bufferedWriter.write(Utils.getOutputHeaders(getVerblijfsobjectFields(),params));
            HashMap<String,String> record = new HashMap<>();
            while (reader.hasNext()) {
                if (reader.getEventType() == XMLStreamReader.START_ELEMENT &&
                        reader.getName().equals(new QName(NAMESPACE_URI_OBJECTEN, OBJECT_TYPE_LOCAL_NAME_VERBLIJFSOBJECT, NAMESPACE_PREFIX_OBJECTEN))){
                    OMElement element =
                            OMXMLBuilderFactory.createStAXOMBuilder(reader).getDocumentElement();
                    element.build();
                    record = processVerblijfsobject(element);
                    bufferedWriter.write(Utils.getOutputRecord(getVerblijfsobjectFields(), record, params));
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
     * Fields to be written to delimited output
     */
    protected static String[] getVerblijfsobjectFields (){
        String[] verblijfsobjectFields = new String[] {
                VBO_HOOFDADRES_NUMMERAANDUIDING,
                VBO_VOORKOMEN_ID,
                VBO_VOORKOMEN_BEGIN_GELDIGHEID,
                VBO_VOORKOMEN_EIND_GELDIGHEID,
                VBO_IDENTIFICATIE,
                VBO_GEBRUIKSDOEL,
                VBO_OPPERVLAKTE,
                VBO_STATUS,
                VBO_GECONSTATEERD,
                VBO_DOCUMENTDATUM,
                VBO_DOCUMENTNUMMER,
                VBO_GEOM_TYPE,
                VBO_GEOM_IS_VALID,
                VBO_GEOM_X,
                VBO_GEOM_Y,
                VBO_GEOM_Y_WGS84,
                VBO_GEOM_Y_WGS84
        };
        return verblijfsobjectFields;
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
            if (node.getType() == 1) {
                OMElement nodeEl = (OMElement) node;
                String nodeLocalName = nodeEl.getQName().getLocalPart();
                if (nodeLocalName.equals(VBO_HOOFDADRES)) {
                    record.put(VBO_HOOFDADRES_NUMMERAANDUIDING, Utils.getHoofdadresNummeraanduiding(nodeEl));
                } else if(nodeEl.getQName().equals(new QName(NAMESPACE_URI_HISTORIE, VBO_VOORKOMEN_ID, NAMESPACE_PREFIX_HISTORIE))){
                    record.put(VBO_VOORKOMEN_ID, nodeEl.getText());
                } else if(nodeEl.getQName().equals(new QName(NAMESPACE_URI_HISTORIE, VBO_VOORKOMEN_BEGIN_GELDIGHEID, NAMESPACE_PREFIX_HISTORIE))){
                    record.put(VBO_VOORKOMEN_BEGIN_GELDIGHEID, nodeEl.getText());
                } else if(nodeEl.getQName().equals(new QName(NAMESPACE_URI_HISTORIE, VBO_VOORKOMEN_EIND_GELDIGHEID, NAMESPACE_PREFIX_HISTORIE))){
                    record.put(VBO_VOORKOMEN_EIND_GELDIGHEID, nodeEl.getText());
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
                    Geometry geometry = Utils.getGeometry(nodeEl);
                    if(geometry != null){
                        record.put(VBO_GEOM_TYPE, geometry.getGeometryType());
                        record.put(VBO_GEOM_IS_VALID, Boolean.toString(geometry.isValid()));
                        Point point = GeoUtils.getPointForGeometry(geometry);
                        record.put(VBO_GEOM_X, GeoUtils.getPointX(point));
                        record.put(VBO_GEOM_Y, GeoUtils.getPointY(point));
                        Point wgs84Point = (Point) GeoUtils.toWGS84(point);
                        record.put(VBO_GEOM_X_WGS84, GeoUtils.getPointX(wgs84Point));
                        record.put(VBO_GEOM_Y_WGS84, GeoUtils.getPointY(wgs84Point));
                    }
                }
            }
        }
        return record;
    }
}
