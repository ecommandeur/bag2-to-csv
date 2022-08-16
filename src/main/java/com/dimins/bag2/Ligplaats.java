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

public class Ligplaats {

    public static String LIG_HOOFDADRES = "heeftAlsHoofdadres";
    public static String LIG_HOOFDADRES_NUMMERAANDUIDING = "hoofdadresNummeraanduiding";
    public static String LIG_VOORKOMEN_ID = "voorkomenidentificatie";
    public static String LIG_VOORKOMEN_BEGIN_GELDIGHEID = "beginGeldigheid";
    public static String LIG_VOORKOMEN_EIND_GELDIGHEID = "eindGeldigheid";
    public static String LIG_IDENTIFICATIE = "identificatie";
    public static String LIG_STATUS = "status";
    public static String LIG_GECONSTATEERD = "geconstateerd";
    public static String LIG_DOCUMENTDATUM = "documentdatum";
    public static String LIG_DOCUMENTNUMMER = "documentnummer";
    public static String LIG_IS_HUIDIG_VOORKOMEN = "isHuidigVoorkomen";
    public static String LIG_GEOMETRIE = "geometrie";
    public static String LIG_GEOM_TYPE = "geom_type";
    public static String LIG_GEOM_IS_VALID = "geom_is_valid";
    public static String LIG_GEOM_X = "geom_x"; //check naming convention gdal/qgis
    public static String LIG_GEOM_Y = "geom_y"; //check naming convention gdal/qgis
    public static String LIG_GEOM_X_WGS84 = "geom_x_wgs84"; //check naming convention gdal/qgis
    public static String LIG_GEOM_Y_WGS84 = "geom_y_wgs84"; //check naming convention gdal/qgis

    /**
     * Proces a LigPlaats XML document
     */
    protected static void processLigplaatsXML(File file, HashMap<String, String> params) throws Exception {
        InputStream in = new FileInputStream(file);
        XMLStreamReader reader = StAXUtils.createXMLStreamReader(in);
        try (FileOutputStream fos = new FileOutputStream(params.get(PARAM_OUTPUT_FILENAME));
             OutputStreamWriter streamWriter = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             BufferedWriter bufferedWriter = new BufferedWriter(streamWriter)) {

            bufferedWriter.write(Utils.getOutputHeaders(getLigplaatsFields(), params));
            HashMap<String,String> record = new HashMap<>();
            while (reader.hasNext()) {
                if (reader.getEventType() == XMLStreamReader.START_ELEMENT &&
                        reader.getName().equals(new QName(NAMESPACE_URI_OBJECTEN, OBJECT_TYPE_LOCAL_NAME_LIGPLAATS, NAMESPACE_PREFIX_OBJECTEN))) {
                    OMElement element =
                            OMXMLBuilderFactory.createStAXOMBuilder(reader).getDocumentElement();
                    element.build();
                    record = processLigplaats(element);
                    bufferedWriter.write(Utils.getOutputRecord(getLigplaatsFields(), record, params));
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
    protected static String[] getLigplaatsFields (){
        String[] ligplaatsFields = new String[] {
                LIG_HOOFDADRES_NUMMERAANDUIDING,
                LIG_VOORKOMEN_ID,
                LIG_VOORKOMEN_BEGIN_GELDIGHEID,
                LIG_VOORKOMEN_EIND_GELDIGHEID,
                LIG_IDENTIFICATIE,
                LIG_STATUS,
                LIG_GECONSTATEERD,
                LIG_DOCUMENTDATUM,
                LIG_DOCUMENTNUMMER,
                LIG_IS_HUIDIG_VOORKOMEN,
                LIG_GEOM_TYPE,
                LIG_GEOM_IS_VALID,
                LIG_GEOM_X,
                LIG_GEOM_Y,
                LIG_GEOM_X_WGS84,
                LIG_GEOM_Y_WGS84
        };
        return ligplaatsFields;
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
                if(nodeLocalName.equals(LIG_HOOFDADRES)){
                    record.put(LIG_HOOFDADRES_NUMMERAANDUIDING, Utils.getHoofdadresNummeraanduiding(nodeEl));
                } else if(nodeEl.getQName().equals(new QName(NAMESPACE_URI_HISTORIE, LIG_VOORKOMEN_ID, NAMESPACE_PREFIX_HISTORIE))){
                    record.put(LIG_VOORKOMEN_ID, nodeEl.getText());
                } else if(nodeEl.getQName().equals(new QName(NAMESPACE_URI_HISTORIE, LIG_VOORKOMEN_BEGIN_GELDIGHEID, NAMESPACE_PREFIX_HISTORIE))){
                    record.put(LIG_VOORKOMEN_BEGIN_GELDIGHEID, nodeEl.getText());
                } else if(nodeEl.getQName().equals(new QName(NAMESPACE_URI_HISTORIE, LIG_VOORKOMEN_EIND_GELDIGHEID, NAMESPACE_PREFIX_HISTORIE))){
                    record.put(LIG_VOORKOMEN_EIND_GELDIGHEID, nodeEl.getText());
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
                    Geometry geometry = Utils.getGeometry(nodeEl);
                    if(geometry != null){
                        record.put(LIG_GEOM_TYPE, geometry.getGeometryType());
                        record.put(LIG_GEOM_IS_VALID, Utils.isValidGeometry(geometry));
                        Point point = GeoUtils.getPointForGeometry(geometry);
                        record.put(LIG_GEOM_X, GeoUtils.getPointX(point));
                        record.put(LIG_GEOM_Y, GeoUtils.getPointY(point));
                        Point wgs84Point = (Point) GeoUtils.toWGS84(point);
                        record.put(LIG_GEOM_X_WGS84, GeoUtils.getPointX(wgs84Point));
                        record.put(LIG_GEOM_Y_WGS84, GeoUtils.getPointY(wgs84Point));
                    }
                }
            }
        }
        record.put(LIG_IS_HUIDIG_VOORKOMEN, Utils.isHuidigVoorkomen(record.getOrDefault(LIG_STATUS,""),
                record.getOrDefault(LIG_VOORKOMEN_EIND_GELDIGHEID,"")));
        return record;
    }

}
