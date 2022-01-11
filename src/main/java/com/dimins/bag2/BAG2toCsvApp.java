package com.dimins.bag2;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.om.xpath.AXIOMXPath;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Main class
 */
public class BAG2toCsvApp {

    public static String NAMESPACE_URI_STANDLEVERING_GENERIEK = "http://www.kadaster.nl/schemas/standlevering-generiek/1.0";
    public static String NAMESPACE_PREFIX_STANDLEVERING_GENERIEK = "st";
    public static String NAMESPACE_URI_OBJECTEN = "www.kadaster.nl/schemas/lvbag/imbag/objecten/v20200601";
    public static String NAMESPACE_PREFIX_OBJECTEN = "Objecten";

    public static void main(String[] args) {
        //TODO pass in filename via argument
        String filename = "src/test/resources/xml/lig_excerpt.xml";
        try {
            File file = new File(filename);
            if(!file.exists()) {
                System.out.println("Input " + filename + " not found");
                System.exit(1);
            }
            //TODO See if we can have one generic method (are all BAG2 XML collections of stand elements?)
            // or if we need a process method per XML
            processLigplaatsXML(file);
        } catch(Exception e) {
            System.out.println("ERROR:" + e.getLocalizedMessage());
        }
    }

    protected static void processLigplaatsXML(File file) throws Exception {
        // Create a builder for the file and get the root element
        InputStream in = new FileInputStream(file);

        // Create an XMLStreamReader without building the object model
        // Any reason to use OMXMLBuilderFactory vs StAXUtils?
        // This will use the StAX implementation in the JRE, see https://ws.apache.org/axiom/implementations/axiom-impl/
        XMLStreamReader reader = StAXUtils.createXMLStreamReader(in);
//        XMLStreamReader reader =
//                OMXMLBuilderFactory.createOMBuilder(in).getDocument().getXMLStreamReader(false);
        try {

            while (reader.hasNext()) {
                if (reader.getEventType() == XMLStreamReader.START_ELEMENT &&
                    reader.getName().equals(new QName(NAMESPACE_URI_STANDLEVERING_GENERIEK, "stand", NAMESPACE_PREFIX_STANDLEVERING_GENERIEK))) {

                    OMElement element =
                            OMXMLBuilderFactory.createStAXOMBuilder(reader).getDocumentElement();
                    element.build();
                    processLigplaatsFragment(element);
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
     *
     * @param element
     * @throws Exception TODO proper exception handling
     */
    public static void processLigplaatsFragment(OMElement element) throws Exception {
        //Check if stand has ligplaats
        //Alternatively change processLigPlaatsXML to look for Objecten:Ligplaats instead of st:stand
        //We will want to keep track of the number of stand/Ligplaats elements we find, so we can report on that
        OMElement firstEl = element.getFirstElement();

        if(firstEl != null && firstEl.getFirstElement() != null) {
            QName name = firstEl.getQName();
            System.out.println("First element under st:stand is" + name.getPrefix() + ":" + name.getLocalPart());

            String ligElName = firstEl.getFirstElement().getLocalName();
            if (ligElName.equalsIgnoreCase("Ligplaats")) {
                System.out.println("Found Ligplaats element under stand element.");
            } else {
                System.out.println("Expecting Ligplaats element under stand element, but did not find one.");
            }
        }
        //Shall we iterate over descendants or use XPath
//        Iterator<OMNode> descendants = element.getDescendants();
//        while(descendants.hasNext()){
//            OMNode node = (OMNode) descendants.next();
//        }
        AXIOMXPath xPath = new AXIOMXPath("//Objecten:identificatie");
        xPath.addNamespace(NAMESPACE_PREFIX_OBJECTEN, NAMESPACE_URI_OBJECTEN);
        OMElement identificatie = (OMElement) xPath.selectSingleNode(element);
        if(identificatie != null) {
            System.out.println("Identificatie: " + identificatie.getText());
        }


    }

}