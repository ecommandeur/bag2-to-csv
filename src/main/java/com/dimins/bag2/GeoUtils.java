package com.dimins.bag2;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.*;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Utilities for dealing with geometries in the BAG2 application schema.
 *
 * In the BAG2 XML there seem to be only points and polygons that are encoded in a predictable way,
 * so it seems feasible to parse these with some custom code.
 *
 * TODO See if we can use xsd-gml3 from GeoTools.
 *  Not sure it that plays nice with Apache Axiom with regard to dependencies.
 *  GeoTools does not seem to be able to parse fragments, but
 *  We could get the GML as string, prepend an XML prolog, and parse that.
 *  See for example https://github.com/geotools/geotools/blob/main/modules/extension/xsd/xsd-gml3/src/test/java/org/geotools/gml3/GML3ParsingTest.java .
 *  The testParse3D method parses an XML that only contains one Polygon.
 *
 * Deegree has a tool to create a SQLFeatureStore from a GML,
 * see https://download.deegree.org/documentation/3.4.24/html/#deegree-gml-tools .
 * However, preferably we stick to GeoTools...
 */
public class GeoUtils {

    protected static Geometry parseGeometry(OMElement geomEl) {
        Geometry geometry = null;
        String geomLocalName = geomEl.getQName().getLocalPart();
        switch (geomLocalName) {
            case "Polygon":
                geometry = parsePolygon(geomEl);
                break;
            case "Point":
                geometry = parsePoint(geomEl);
        }
        return geometry;
    }

    protected static Polygon parsePolygon(OMElement geomEl) {
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        //Does srsDimension always need to be declared on top-level geom? Is this specific to posList...?
        OMAttribute srsDimensionAttr = geomEl.getAttribute(new QName("srsDimension"));
        int srsDimension = 2; //default to 2-D
        if(srsDimensionAttr != null) {
            srsDimension = getIntOrDefault(srsDimensionAttr.getAttributeValue(), srsDimension);
        }
        Iterator<OMElement> geomChildren = geomEl.getChildElements();
        LinearRing shell = null;
        ArrayList<LinearRing> holeList = new ArrayList<>();
        while(geomChildren.hasNext()) {
            OMElement el = geomChildren.next();
            if(el.getQName().getLocalPart() == "exterior"){
                OMElement exteriorFirstChildEl = el.getFirstElement();
                if(exteriorFirstChildEl.getQName().getLocalPart() == "LinearRing"){
                    shell = parseLinearRing(exteriorFirstChildEl, srsDimension);
                }
            }
            if(el.getQName().getLocalPart() == "interior"){
                LinearRing hole = null;
                OMElement interiorFirstChildEl = el.getFirstElement();
                if(interiorFirstChildEl.getQName().getLocalPart() == "LinearRing"){
                    hole = parseLinearRing(interiorFirstChildEl, srsDimension);
                }
                if(hole != null) {
                    holeList.add(hole);
                }
            }
        }
        LinearRing[] holes = holeList.toArray(new LinearRing[0]);
        Polygon polygon = geometryFactory.createPolygon(shell, holes);
        return polygon;
    }

    protected static LinearRing parseLinearRing(OMElement ringEl, int srsDimension) {
        OMElement firstChildEl = ringEl.getFirstElement();
        Coordinate[] coordinates = null;
        if(firstChildEl.getQName().getLocalPart() == "posList") {
            //Instead of reading the count attribute and passing that here we parse the posList using srsDimension
            //OMAttribute count = firstChildEl.getAttribute(new QName("count"));
            coordinates = parsePosList(firstChildEl.getText(), srsDimension);
        }
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        LinearRing linearRing = geometryFactory.createLinearRing(coordinates);
        return linearRing;
    }

    protected static Coordinate[] parsePosList(String posList, int srsDimension) {
        //This method relies on srsDimension. Alternatively we could look at the count attribute.
        String[] posValues = posList.split("\\s+");
        //When the outcome of the division of two integers is not an integer it will be truncated to an integer if we store the outcome in an int.
        int coordinatesSize = posValues.length / srsDimension; //coordinateSize should be equal to count attribute
        Coordinate[] coordinates = new Coordinate[coordinatesSize];
        int j = 0;
        int max = posValues.length - 1;
        for(int i = 0; i <= max;i++){
            //TODO should app die hard on posValues that are not doubles or issue a warning...?
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
            //TODO should app die hard on posValues that are not doubles or issue a warning...?
            // We could have a getDoubleOrNaN and return NaN
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
            // do nothing here
            // return default value if there is an NFE
        }
        return i;
    }

    /**
     * Get point for geometry.
     *
     * Return point if geometry is a point and interior point otherwise
     */
    protected static Point getPointForGeometry(Geometry geometry) {
        Point p = null;
        String geometryType = geometry.getGeometryType();
        if(geometryType.equals(Geometry.TYPENAME_POINT)){
            p = (Point) geometry;
        } else if (geometryType.equals(Geometry.TYPENAME_POLYGON)){
            p = geometry.getInteriorPoint();
        }
        return p;
    }

    protected static Geometry toWGS84(Geometry sourceGeometry) throws Exception {
        Geometry targetGeometry = null;
        if(sourceGeometry != null) {
            CoordinateReferenceSystem fromCRS = CRS.decode("EPSG:28992");
            CoordinateReferenceSystem toCRS = CRS.decode("EPSG:4326");
            MathTransform transform = CRS.findMathTransform(fromCRS, toCRS, false);
            targetGeometry = JTS.transform( sourceGeometry, transform);
        }
        return targetGeometry;
    }

    protected static String getPointX(Point point) {
        String x = "";
        if(point != null) {
            x = Double.toString(point.getX());
        }
        return x;
    }

    protected static String getPointY(Point point) {
        String y = "";
        if(point != null) {
            y = Double.toString(point.getY());
        }
        return y;
    }
}
