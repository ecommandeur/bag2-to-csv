package com.dimins.bag2;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.jts.geom.*;

import javax.xml.namespace.QName;
import java.util.Iterator;

/**
 * Utilities for dealing with geometries in BAG2 application schema
 */
public class GeoUtils {

    protected static Geometry parseGeometry(OMElement geomEl) {
        //Ideally we get the first gml element and pass that to GeoTools to parse into a geometry
        //However, that does not seem to be all that easy... GML processing (in GeoTools) is a complex beast
        //Also the GML we get in the BAG export is predictable, so it is feasible to parse ourselves
        //https://docs.geotools.org/latest/userguide/library/jts/geometry.html
        //
        Geometry geometry = null;
        String geomLocalName = geomEl.getQName().getLocalPart();
        switch (geomLocalName) {
            case "Polygon":
                //geometry = parsePolygon(geomEl);
                geometry = parsePolygon(geomEl);
                break;
            case "Point":
                //geometry = parsePoint(geomEl);
                geometry = parsePoint(geomEl);
        }
        return geometry;
    }

    protected static Polygon parsePolygon(OMElement geomEl) {
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        //TODO Does srsDimension always need to be declared on top-level geom?
        // is this specific to posList...?
        OMAttribute srsDimensionAttr = geomEl.getAttribute(new QName("srsDimension"));
        int srsDimension = 2; //default to 2-D
        if(srsDimensionAttr != null) {
            srsDimension = getIntOrDefault(srsDimensionAttr.getAttributeValue(), srsDimension);
        }
        Iterator<OMElement> geomChildren = geomEl.getChildElements();
        LinearRing shell = null;
        while(geomChildren.hasNext()) {
            OMElement el = geomChildren.next();
            System.out.println("GeomEl child: " + el.getQName().getLocalPart());
            if(el.getQName().getLocalPart() == "exterior"){
                OMElement exteriorFirstChildEl = el.getFirstElement();
                if(exteriorFirstChildEl.getQName().getLocalPart() == "LinearRing"){
                    shell = parseLinearRing(exteriorFirstChildEl, srsDimension);
                }
            }
            if(el.getQName().getLocalPart() == "interior"){
                //TODO parse interior rings
            }

        }
        Polygon polygon = geometryFactory.createPolygon(shell);
        System.out.println("Polygon as WKT: " + polygon.toString());
        return polygon;
    }

    protected static LinearRing parseLinearRing(OMElement ringEl, int srsDimension) {
        OMElement firstChildEl = ringEl.getFirstElement();
        Coordinate[] coordinates = null;
        if(firstChildEl.getQName().getLocalPart() == "posList") {
            //Attribute does not have a prefix or namespace
            OMAttribute count = firstChildEl.getAttribute(new QName("count"));
            System.out.println("count attr value: " + count.getAttributeValue());
            //System.out.println("posList value: " + firstChildEl.getText());
            coordinates = parsePosList(firstChildEl.getText(), srsDimension);
        }
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        LinearRing linearRing = geometryFactory.createLinearRing(coordinates);
        System.out.println("LinearRing centroid: " + linearRing.getCentroid());
        System.out.println("LinearRing interior point: " + linearRing.getInteriorPoint());
        System.out.println("LinearRing as WKT: " + linearRing.toString());
        return linearRing;
    }

    protected static Coordinate[] parsePosList(String posList, int srsDimension) {
        String[] posValues = posList.split("\\s+");
        System.out.println("coords length: " + posValues.length);
        //When the outcome of the division of two integers is not an integer it will be truncated to an integer if we store the value in an int.
        int coordinatesSize = posValues.length / srsDimension; //posValues.length should be equal to count attribute
        System.out.println("coords size: " + coordinatesSize);
        Coordinate[] coordinates = new Coordinate[coordinatesSize];
        int j = 0;
        int max = posValues.length - 1;
        for(int i = 0; i <= max;i++){
            //TODO should app die hard on posValues that are not doubles...?
            //System.out.println("posValues " + i + ":"+ posValues[i]);
            //System.out.println("posValues " + (i+1) + ":"+ posValues[i+1]);
            Double x = Double.parseDouble(posValues[i]);
            Double y = Double.parseDouble(posValues[i+1]);
            Double z = Double.NaN;
            if(srsDimension == 3){
                z = Double.parseDouble(posValues[i+2]);
                i++;
            }
            Coordinate c = new Coordinate(x,y,z);
            coordinates[j] = c;
            i++; //increment by another one
            j++;
        }
        return coordinates;
    }

    protected static Point parsePoint(OMElement geomEl) {
        //TODO Set SRID? Set it via getGeometryFactory?
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        OMAttribute srsDimensionAttr = geomEl.getAttribute(new QName("srsDimension"));
        int srsDimension = 2; //default to 2-D
        if(srsDimensionAttr != null) {
            srsDimension = getIntOrDefault(srsDimensionAttr.getAttributeValue(), srsDimension);
        }
        Coordinate coordinate = null;
        OMElement geomElFirstChild = geomEl.getFirstElement();
        if(geomElFirstChild != null && geomElFirstChild.getQName().getLocalPart().equals("pos")) {
            coordinate = parsePos(geomElFirstChild.getText(), srsDimension);
        }
        Point point = geometryFactory.createPoint(coordinate);
        return point;
    }

    protected static Coordinate parsePos(String pos, int srsDimension){
        Coordinate coordinate = new Coordinate(); //Creating a new Coordinate initializes x, y, z to 0.0, 0.0 and NaN respectively
        String[] posValues = pos.split("\\s+");
        int posValuesLength = posValues.length;
        if(posValuesLength >= 2 ) {
            //TODO should app die hard on posValues that are not doubles...?
            // or should we have a getDoubleOrNaN and return NaN + alert if value could not be parsed as a double
            coordinate.setX(Double.parseDouble(posValues[0]));
            coordinate.setY(Double.parseDouble(posValues[1]));
            if(posValuesLength >= 3) {
                coordinate.setZ(Double.parseDouble(posValues[2]));
            }
        }
        return coordinate;
    }

    protected static int getIntOrDefault(String intString, int defaultValue) {
        int i = defaultValue;
        try {
            i = Integer.parseInt(intString);
        } catch (NumberFormatException e) {
            //return default value
        }
        return i;
    }
}
