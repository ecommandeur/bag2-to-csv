package com.dimins.bag2;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.junit.jupiter.api.Test;

import javax.xml.namespace.QName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests go here
 */
public class BAG2toCsvAppTest {

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

    //TODO have excerpts of XML under test resources and test against those
    // E.g.reading new File("src/test/resources/xml/lig_excerpt.xml")

}