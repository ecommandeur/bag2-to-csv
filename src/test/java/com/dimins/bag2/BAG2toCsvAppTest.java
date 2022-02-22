package com.dimins.bag2;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.lang3.StringUtils;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import javax.xml.namespace.QName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests go here
 */
public class BAG2toCsvAppTest {

    //TODO have excerpts of XML under test resources and test against those
    // E.g.reading new File("src/test/resources/xml/lig_excerpt.xml")

    @Test
    public void test_new_qname() throws Exception {
        //Does namespace URI matter?
        QName qname = new QName("http://www.kadaster.nl/schemas/standlevering-generiek/1.0","stand", "st");
        assertEquals("http://www.kadaster.nl/schemas/standlevering-generiek/1.0", qname.getNamespaceURI());
        assertEquals("stand", qname.getLocalPart());
        assertEquals("st", qname.getPrefix());
    }

    @Test
    public void test_xml_string() throws Exception {
        String xmlString = "<greetings><greeting>Hello World</greeting></greetings>";
        OMElement xmlElement = AXIOMUtil.stringToOM(xmlString);
        assertEquals(xmlString, xmlElement.toString());
    }

    @Test
    public void test_int_division() {
        int x = 5 / 3;
        assertEquals(x, 1);
        int y = 5 / 2;
        assertEquals(y, 2);
    }

    @Test
    public void test_int_parseDouble() {
        assertEquals(Double.parseDouble("1"), 1.0);
        assertEquals(Double.parseDouble("1.2"), 1.2);
        //assertEquals(Double.parseDouble("1,2"), 1.2); this will throw a NumberFormatException
    }

//    @Test
//    public void test_sys_out_println() {
//        Coordinate coordinate = new Coordinate();
//        System.out.println("Coordinate x: " + coordinate.getY());
//        System.out.println("Coordinate y: " + coordinate.getY());
//        System.out.println("Coordinate z: " + coordinate.getZ());
//        System.out.println("Coordinate m: " + coordinate.getM());
//        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
//        System.out.println("Geometry factory SRID: " + geometryFactory.getSRID());
//    }

    @Test
    public void test_normalize_space() throws Exception {
        String xmlString = "<greetings><greeting>\n Hello\nWorld  </greeting></greetings>";
        OMElement xmlElement = AXIOMUtil.stringToOM(xmlString);
        String greeting = xmlElement.getFirstElement().getText();
        assertEquals("Hello World", StringUtils.normalizeSpace(greeting));

    }
}