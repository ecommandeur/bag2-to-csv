package com.dimins.bag2;

import org.apache.axiom.om.OMElement;
import org.apache.commons.lang3.StringUtils;
import org.locationtech.jts.geom.Geometry;

import java.text.SimpleDateFormat;
import java.util.*;

import static com.dimins.bag2.BAG2toCsvApp.PARAM_OUTPUT_DELIMITER;

public class Utils {

    /**
     * Headers to be written to delimited output
     */
    protected static String getOutputHeaders(String[] fields, HashMap<String, String> params) {
        StringJoiner headerValues = new StringJoiner(params.get(PARAM_OUTPUT_DELIMITER));
        for (String field : fields){
            headerValues.add(field);
        }
        return headerValues.toString() + "\n";
    };

    /**
     * Record to be written to delimited output
     */
    protected static String getOutputRecord (String[] fields, HashMap<String,String> record, HashMap<String, String> params) {
        StringJoiner rowValues = new StringJoiner(params.get(PARAM_OUTPUT_DELIMITER));
        String defaultValue = "";
        for(String field : fields){
            //TODO if delimiter is not tab, we need to take into account quoting
            rowValues.add(StringUtils.normalizeSpace(record.getOrDefault(field, defaultValue)));
        }
        return rowValues.toString() + "\n";
    }

    /**
     * Get HoofdadresNummeraanduiding given a hoofdAdres element
     */
    protected static String getHoofdadresNummeraanduiding(OMElement hoofdAdres){
        String aanduiding = "";
        OMElement nodeFirstEl = hoofdAdres.getFirstElement();
        if(nodeFirstEl != null && nodeFirstEl.getQName().getLocalPart().equals("NummeraanduidingRef")) {
            aanduiding = nodeFirstEl.getText();
        }
        return aanduiding;
    }

    /**
     * Get Geometry given a geometrie element
     */
    protected static Geometry getGeometry(OMElement geometrie) {
        Geometry geometry = null;
        OMElement nodeFirstEl = geometrie.getFirstElement();
        //TODO Descent geometrie until the first gml element is found.
        // For now, capitalize on the fact that the actual GML geometry element is at most two levels below Objecten:geometrie
        // e.g. <Objecten:geometrie><gml:Polygon srsName="urn:ogc:def:crs:EPSG::28992" srsDimension="2">...</gml:Polygon></Object:geometrie>
        //  <Objecten:geometrie><Objecten:punt><gml:Point srsName="urn:ogc:def:crs:EPSG::28992" srsDimension="3">...</gml:Point><Objecten:punt></Object:geometrie>
        if(nodeFirstEl.getQName().getPrefix().equals("gml")) {
            geometry = GeoUtils.parseGeometry(nodeFirstEl);
        } else {
            OMElement childFirstEl = nodeFirstEl.getFirstElement();
            geometry = GeoUtils.parseGeometry(childFirstEl);
        }
        return geometry;
    }

    protected static int getChildCount(OMElement element) {
        int count = 0;
        Iterator<OMElement> elementIterator = element.getChildElements();
        while(elementIterator.hasNext()){
            elementIterator.next();
            count++;
        }
        return count;
    }

    protected static String isHuidigVoorkomen(String status, String eindGeldigheid){
        String isHuidig = "0";
        String[] huidigStatus = new String[] {
                "naamgeving uitgegeven",
                "woonplaats aangewezen",
                "plaats aangewezen",
                "verblijfsobject gevormd", //include?
                "verblijfsobject in gebruik (niet ingemeten)",
                "verblijfsobject in gebruik",
                "verblijfsobject buiten gebruik",
                "verbouwing verblijfsobject",
                "bouwvergunning verleend", //include?
                "bouw gestart",
                "pand in gebruik (niet ingemeten)",
                "pand in gebruik",
                "sloopvergunning verleend",
                //"pand gesloopt", //include?
                "pand buiten gebruik",
                "verbouwing pand"
        };
        if(Arrays.asList(huidigStatus).contains(status.toLowerCase()) && eindGeldigheid.isEmpty()){
            isHuidig = "1";
        }
        return isHuidig;
    }

    protected static String isValidGeometry(Geometry geometry){
        String isValid = "0";
        if(geometry.isValid()){
            isValid = "1";
        }
        return isValid;
    }

    protected static String getLogTimeStamp(){
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    protected static void writeTimeStampedMessage(String message){
        System.out.println(getLogTimeStamp() + " - " + message);
    }
}
