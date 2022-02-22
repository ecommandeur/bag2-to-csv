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

public class Standplaats {

    public static String STA_HOOFDADRES = "heeftAlsHoofdadres";
    public static String STA_HOOFDADRES_NUMMERAANDUIDING = "hoofdadresNummeraanduiding";
    public static String STA_VOORKOMEN_ID = "voorkomenidentificatie";
    public static String STA_VOORKOMEN_BEGIN_GELDIGHEID = "beginGeldigheid";
    public static String STA_VOORKOMEN_EIND_GELDIGHEID = "eindGeldigheid";
    public static String STA_IDENTIFICATIE = "identificatie";
    public static String STA_STATUS = "status";
    public static String STA_GECONSTATEERD = "geconstateerd";
    public static String STA_DOCUMENTDATUM = "documentdatum";
    public static String STA_DOCUMENTNUMMER = "documentnummer";
    public static String STA_GEOMETRIE = "geometrie";
    public static String STA_GEOM_TYPE = "geom_type";
    public static String STA_GEOM_IS_VALID = "geom_is_valid";
    public static String STA_GEOM_X = "geom_x"; //check naming convention gdal/qgis
    public static String STA_GEOM_Y = "geom_y"; //check naming convention gdal/qgis
    public static String STA_GEOM_X_WGS84 = "geom_x_wgs84"; //check naming convention gdal/qgis
    public static String STA_GEOM_Y_WGS84 = "geom_y_wgs84"; //check naming convention gdal/qgis

    protected static void processStandplaatsXML(File file, HashMap<String, String> params) throws Exception {
        InputStream in = new FileInputStream(file);
        XMLStreamReader reader = StAXUtils.createXMLStreamReader(in);
        try (FileOutputStream fos = new FileOutputStream(params.get(PARAM_OUTPUT_FILENAME));
             OutputStreamWriter streamWriter = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             BufferedWriter bufferedWriter = new BufferedWriter(streamWriter)) {

            bufferedWriter.write(Utils.getOutputHeaders(getStandplaatsFields(), params));
            HashMap<String,String> record = new HashMap<>();
            while (reader.hasNext()) {
                if (reader.getEventType() == XMLStreamReader.START_ELEMENT &&
                        reader.getName().equals(new QName(NAMESPACE_URI_OBJECTEN, OBJECT_TYPE_LOCAL_NAME_STANDPLAATS, NAMESPACE_PREFIX_OBJECTEN))) {
                    OMElement element =
                            OMXMLBuilderFactory.createStAXOMBuilder(reader).getDocumentElement();
                    element.build();
                    record = processStandplaats(element);
                    bufferedWriter.write(Utils.getOutputRecord(getStandplaatsFields(), record, params));
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
     * Fields to be written to delimited output
     */
    protected static String[] getStandplaatsFields (){
        String[] standplaatsFields = new String[] {
                STA_HOOFDADRES_NUMMERAANDUIDING,
                STA_VOORKOMEN_ID,
                STA_VOORKOMEN_BEGIN_GELDIGHEID,
                STA_VOORKOMEN_EIND_GELDIGHEID,
                STA_IDENTIFICATIE,
                STA_STATUS,
                STA_GECONSTATEERD,
                STA_DOCUMENTDATUM,
                STA_DOCUMENTNUMMER,
                STA_GEOM_TYPE,
                STA_GEOM_IS_VALID,
                STA_GEOM_X,
                STA_GEOM_Y,
                STA_GEOM_X_WGS84,
                STA_GEOM_Y_WGS84
        };
        return standplaatsFields;
    }

    /**
     * Proces a LigPlaats XML Element
     */
    protected static HashMap<String, String> processStandplaats(OMElement element) throws Exception {
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
                if(nodeLocalName.equals(STA_HOOFDADRES)){
                    record.put(STA_HOOFDADRES_NUMMERAANDUIDING, Utils.getHoofdadresNummeraanduiding(nodeEl));
                } else if(nodeEl.getQName().equals(new QName(NAMESPACE_URI_HISTORIE, STA_VOORKOMEN_ID, NAMESPACE_PREFIX_HISTORIE))){
                    record.put(STA_VOORKOMEN_ID, nodeEl.getText());
                } else if(nodeEl.getQName().equals(new QName(NAMESPACE_URI_HISTORIE, STA_VOORKOMEN_BEGIN_GELDIGHEID, NAMESPACE_PREFIX_HISTORIE))){
                    record.put(STA_VOORKOMEN_BEGIN_GELDIGHEID, nodeEl.getText());
                } else if(nodeEl.getQName().equals(new QName(NAMESPACE_URI_HISTORIE, STA_VOORKOMEN_EIND_GELDIGHEID, NAMESPACE_PREFIX_HISTORIE))){
                    record.put(STA_VOORKOMEN_EIND_GELDIGHEID, nodeEl.getText());
                } else if(nodeLocalName.equals(STA_IDENTIFICATIE)) {
                    record.put(STA_IDENTIFICATIE, nodeEl.getText());
                } else if(nodeLocalName.equals(STA_STATUS)){
                    record.put(STA_STATUS, nodeEl.getText());
                } else if(nodeLocalName.equals(STA_GECONSTATEERD)){
                    record.put(STA_GECONSTATEERD, nodeEl.getText());
                } else if(nodeLocalName.equals(STA_DOCUMENTDATUM)){
                    record.put(STA_DOCUMENTDATUM, nodeEl.getText());
                } else if(nodeLocalName.equals(STA_DOCUMENTNUMMER)){
                    record.put(STA_DOCUMENTNUMMER, nodeEl.getText());
                } else if(nodeLocalName.equals(STA_GEOMETRIE)) {
                    Geometry geometry = Utils.getGeometry(nodeEl);
                    if(geometry != null){
                        record.put(STA_GEOM_TYPE, geometry.getGeometryType());
                        record.put(STA_GEOM_IS_VALID, Boolean.toString(geometry.isValid()));
                        Point point = GeoUtils.getPointForGeometry(geometry);
                        record.put(STA_GEOM_X, GeoUtils.getPointX(point));
                        record.put(STA_GEOM_Y, GeoUtils.getPointY(point));
                        Point wgs84Point = (Point) GeoUtils.toWGS84(point);
                        record.put(STA_GEOM_X_WGS84, GeoUtils.getPointX(wgs84Point));
                        record.put(STA_GEOM_Y_WGS84, GeoUtils.getPointY(wgs84Point));
                     }
                }
            }
        }
        return record;
    }
}
