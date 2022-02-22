package com.dimins.bag2;

import org.apache.axiom.om.OMElement;
import org.apache.commons.lang3.StringUtils;
import org.locationtech.jts.geom.Geometry;

import java.util.HashMap;
import java.util.Iterator;
import java.util.StringJoiner;

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
        System.out.println("hoofdAdresNummeraanduiding: " + StringUtils.normalizeSpace(aanduiding));
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
}
