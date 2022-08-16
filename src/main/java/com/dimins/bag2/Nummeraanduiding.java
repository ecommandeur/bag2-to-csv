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

public class Nummeraanduiding {
    public static String NUM_IDENTIFICATIE = "identificatie";
    public static String NUM_HUISNUMMER = "huisnummer";
    public static String NUM_HUISLETTER = "huisletter";
    public static String NUM_POSTCODE = "postcode";
    public static String NUM_TYPE_ADRESSEERBAAR_OBJECT = "typeAdresseerbaarObject";
    public static String NUM_STATUS = "status";
    public static String NUM_GECONSTATEERD = "status";
    public static String NUM_DOCUMENTDATUM = "documentdatum";
    public static String NUM_DOCUMENTNUMMER = "documentnummer";
    public static String NUM_VOORKOMEN_ID = "voorkomenidentificatie";
    public static String NUM_VOORKOMEN_BEGIN_GELDIGHEID = "beginGeldigheid";
    public static String NUM_VOORKOMEN_EIND_GELDIGHEID = "eindGeldigheid";
    public static String NUM_IS_HUIDIG_VOORKOMEN = "isHuidigVoorkomen";
    public static String NUM_OPENBARE_RUIMTE_REF = "OpenbareRuimteRef";


    protected static void processNummeraanduidingXML(File file, HashMap<String, String> params) throws Exception {
        InputStream in = new FileInputStream(file);
        XMLStreamReader reader = StAXUtils.createXMLStreamReader(in);
        try (FileOutputStream fos = new FileOutputStream(params.get(PARAM_OUTPUT_FILENAME));
             OutputStreamWriter streamWriter = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             BufferedWriter bufferedWriter = new BufferedWriter(streamWriter)) {

            bufferedWriter.write(Utils.getOutputHeaders(getNummeraanduidingFields(), params));
            HashMap<String,String> record = new HashMap<>();
            while (reader.hasNext()) {
                if (reader.getEventType() == XMLStreamReader.START_ELEMENT &&
                        reader.getName().equals(new QName(NAMESPACE_URI_OBJECTEN, OBJECT_TYPE_LOCAL_NAME_NUMMMERAANDUIDING, NAMESPACE_PREFIX_OBJECTEN))) {
                    OMElement element =
                            OMXMLBuilderFactory.createStAXOMBuilder(reader).getDocumentElement();
                    element.build();
                    record = processStandplaats(element);
                    bufferedWriter.write(Utils.getOutputRecord(getNummeraanduidingFields(), record, params));
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
    protected static String[] getNummeraanduidingFields (){
        String[] standplaatsFields = new String[] {
                NUM_IDENTIFICATIE,
                NUM_HUISNUMMER,
                NUM_HUISLETTER,
                NUM_POSTCODE,
                NUM_TYPE_ADRESSEERBAAR_OBJECT,
                NUM_STATUS,
                NUM_GECONSTATEERD,
                NUM_DOCUMENTDATUM,
                NUM_DOCUMENTNUMMER,
                NUM_VOORKOMEN_ID,
                NUM_VOORKOMEN_BEGIN_GELDIGHEID,
                NUM_VOORKOMEN_EIND_GELDIGHEID,
                NUM_IS_HUIDIG_VOORKOMEN,
                NUM_OPENBARE_RUIMTE_REF
        };
        return standplaatsFields;
    }

    /**
     * Process a Nummeraanduiding XML Element
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
                if(nodeLocalName.equals(NUM_IDENTIFICATIE)){
                    record.put(NUM_IDENTIFICATIE, nodeEl.getText());
                } else if(nodeLocalName.equals(NUM_HUISNUMMER)){
                    record.put(NUM_HUISNUMMER, nodeEl.getText());
                } else if(nodeLocalName.equals(NUM_HUISLETTER)){
                    record.put(NUM_HUISLETTER, nodeEl.getText());
                } else if(nodeLocalName.equals(NUM_POSTCODE)){
                    record.put(NUM_POSTCODE, nodeEl.getText());
                } else if(nodeLocalName.equals(NUM_TYPE_ADRESSEERBAAR_OBJECT)){
                    record.put(NUM_TYPE_ADRESSEERBAAR_OBJECT, nodeEl.getText());
                } else if(nodeLocalName.equals(NUM_STATUS)){
                    record.put(NUM_STATUS, nodeEl.getText());
                } else if(nodeLocalName.equals(NUM_GECONSTATEERD)){
                    record.put(NUM_GECONSTATEERD, nodeEl.getText());
                } else if(nodeLocalName.equals(NUM_DOCUMENTDATUM)){
                    record.put(NUM_DOCUMENTDATUM, nodeEl.getText());
                } else if(nodeLocalName.equals(NUM_DOCUMENTNUMMER)){
                    record.put(NUM_DOCUMENTNUMMER, nodeEl.getText());
                } else if(nodeEl.getQName().equals(new QName(NAMESPACE_URI_HISTORIE, NUM_VOORKOMEN_ID, NAMESPACE_PREFIX_HISTORIE))){
                    record.put(NUM_VOORKOMEN_ID, nodeEl.getText());
                } else if(nodeEl.getQName().equals(new QName(NAMESPACE_URI_HISTORIE, NUM_VOORKOMEN_BEGIN_GELDIGHEID, NAMESPACE_PREFIX_HISTORIE))){
                    record.put(NUM_VOORKOMEN_BEGIN_GELDIGHEID, nodeEl.getText());
                } else if(nodeEl.getQName().equals(new QName(NAMESPACE_URI_HISTORIE, NUM_VOORKOMEN_EIND_GELDIGHEID, NAMESPACE_PREFIX_HISTORIE))){
                    record.put(NUM_VOORKOMEN_EIND_GELDIGHEID, nodeEl.getText());
                } else if(nodeLocalName.equals(NUM_OPENBARE_RUIMTE_REF)){
                    //According to the domain model there a nummeraanduiding is associated with 1 openbare ruimte
                    record.put(NUM_OPENBARE_RUIMTE_REF, nodeEl.getText());
                }
            }
        }
        record.put(NUM_IS_HUIDIG_VOORKOMEN, Utils.isHuidigVoorkomen(record.getOrDefault(NUM_STATUS,""),
                record.getOrDefault(NUM_VOORKOMEN_EIND_GELDIGHEID,"")));
        return record;
    }
}
