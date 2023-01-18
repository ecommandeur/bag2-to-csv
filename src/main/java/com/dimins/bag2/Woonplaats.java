package com.dimins.bag2;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.util.StAXUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;

import static com.dimins.bag2.BAG2toCsvApp.*;
import static com.dimins.bag2.BAG2toCsvApp.NAMESPACE_PREFIX_OBJECTEN;

public class Woonplaats {

    public static String WPL_IDENTIFICATIE = "identificatie";
    public static String WPL_NAAM = "naam";
    public static String WPL_STATUS = "status";
    public static String WPL_GECONSTATEERD = "geconstateerd";
    public static String WPL_DOCUMENTDATUM = "documentdatum";
    public static String WPL_DOCUMENTNUMMER = "documentnummer";
    public static String WPL_VOORKOMEN_ID = "voorkomenidentificatie";
    public static String WPL_VOORKOMEN_BEGIN_GELDIGHEID = "beginGeldigheid";
    public static String WPL_VOORKOMEN_EIND_GELDIGHEID = "eindGeldigheid";
    /**
     * Proces a Woonplaats XML document
     */
    protected static void processWoonplaatsXML(File file, HashMap<String, String> params) throws Exception {
        InputStream in = new FileInputStream(file);
        XMLStreamReader reader = StAXUtils.createXMLStreamReader(in);
        try (FileOutputStream fos = new FileOutputStream(params.get(PARAM_OUTPUT_FILENAME));
             OutputStreamWriter streamWriter = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             BufferedWriter bufferedWriter = new BufferedWriter(streamWriter)) {

            bufferedWriter.write(Utils.getOutputHeaders(getWoonplaatsFields(),params));
            HashMap<String,String> record = new HashMap<>();
            while (reader.hasNext()) {
                if (reader.getEventType() == XMLStreamReader.START_ELEMENT &&
                        reader.getName().equals(new QName(NAMESPACE_URI_OBJECTEN, OBJECT_TYPE_LOCAL_NAME_WOONPLAATS, NAMESPACE_PREFIX_OBJECTEN))){
                    OMElement element =
                            OMXMLBuilderFactory.createStAXOMBuilder(reader).getDocumentElement();
                    element.build();
                    record = processWoonplaats(element);
                    bufferedWriter.write(Utils.getOutputRecord(getWoonplaatsFields(), record, params));
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
    protected static String[] getWoonplaatsFields (){
        String[] verblijfsobjectFields = new String[] {
                WPL_IDENTIFICATIE,
                WPL_NAAM,
                WPL_STATUS,
                WPL_GECONSTATEERD,
                WPL_DOCUMENTDATUM,
                WPL_DOCUMENTNUMMER,
                WPL_VOORKOMEN_ID,
                WPL_VOORKOMEN_BEGIN_GELDIGHEID,
                WPL_VOORKOMEN_EIND_GELDIGHEID
        };
        return verblijfsobjectFields;
    }

    /**
     *
     * @param element
     * @throws Exception TODO proper exception handling
     */
    protected static HashMap<String, String> processWoonplaats(OMElement element) throws Exception {
        HashMap<String, String> record = new HashMap<>();

        //Iterate over descendants one time versus separate XPath calls
        Iterator<OMNode> descendants = element.getDescendants(false);
        while (descendants.hasNext()) {
            OMNode node = (OMNode) descendants.next();
            if (node.getType() == 1) {
                OMElement nodeEl = (OMElement) node;
                String nodeLocalName = nodeEl.getQName().getLocalPart();
                if (nodeLocalName.equals(WPL_IDENTIFICATIE)) {
                    record.put(WPL_IDENTIFICATIE, nodeEl.getText());
                } else if (nodeLocalName.equals(WPL_NAAM)) {
                    record.put(WPL_NAAM, nodeEl.getText());
                } else if (nodeLocalName.equals(WPL_STATUS)) {
                    record.put(WPL_STATUS, nodeEl.getText());
                } else if (nodeLocalName.equals(WPL_GECONSTATEERD)) {
                    record.put(WPL_GECONSTATEERD, nodeEl.getText());
                } else if (nodeLocalName.equals(WPL_DOCUMENTDATUM)) {
                    record.put(WPL_DOCUMENTDATUM, nodeEl.getText());
                } else if (nodeLocalName.equals(WPL_DOCUMENTNUMMER)) {
                    record.put(WPL_DOCUMENTNUMMER, nodeEl.getText());
                } else if (nodeEl.getQName().equals(new QName(NAMESPACE_URI_HISTORIE, WPL_VOORKOMEN_ID, NAMESPACE_PREFIX_HISTORIE))){
                    record.put(WPL_VOORKOMEN_ID, nodeEl.getText());
                } else if(nodeEl.getQName().equals(new QName(NAMESPACE_URI_HISTORIE, WPL_VOORKOMEN_BEGIN_GELDIGHEID, NAMESPACE_PREFIX_HISTORIE))){
                    record.put(WPL_VOORKOMEN_BEGIN_GELDIGHEID, nodeEl.getText());
                } else if(nodeEl.getQName().equals(new QName(NAMESPACE_URI_HISTORIE, WPL_VOORKOMEN_EIND_GELDIGHEID, NAMESPACE_PREFIX_HISTORIE))){
                    record.put(WPL_VOORKOMEN_EIND_GELDIGHEID, nodeEl.getText());
                }
            }
        }
        return record;
    }

}
