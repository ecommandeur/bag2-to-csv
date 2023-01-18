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

public class OpenbareRuimte {

    public static String OPR_IDENTIFICATIE = "identificatie";
    public static String OPR_NAAM = "naam";
    public static String OPR_TYPE = "type";
    public static String OPR_STATUS = "status";
    public static String OPR_GECONSTATEERD = "geconstateerd";
    public static String OPR_DOCUMENTDATUM= "documentdatum";
    public static String OPR_DOCUMENTNUMMER = "documentnummer";
    public static String OPR_WOONPLAATS_REF = "WoonplaatsRef";

    /**
     * Proces a OpenbareRuimte XML document
     */
    protected static void processOpenbareRuimteXML(File file, HashMap<String, String> params) throws Exception {
        InputStream in = new FileInputStream(file);
        XMLStreamReader reader = StAXUtils.createXMLStreamReader(in);
        try (FileOutputStream fos = new FileOutputStream(params.get(PARAM_OUTPUT_FILENAME));
             OutputStreamWriter streamWriter = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             BufferedWriter bufferedWriter = new BufferedWriter(streamWriter)) {

            bufferedWriter.write(Utils.getOutputHeaders(getOpenbareRuimteFields(),params));
            HashMap<String,String> record = new HashMap<>();
            while (reader.hasNext()) {
                if (reader.getEventType() == XMLStreamReader.START_ELEMENT &&
                        reader.getName().equals(new QName(NAMESPACE_URI_OBJECTEN, OBJECT_TYPE_LOCAL_NAME_OPENBARE_RUIMTE, NAMESPACE_PREFIX_OBJECTEN))){
                    OMElement element =
                            OMXMLBuilderFactory.createStAXOMBuilder(reader).getDocumentElement();
                    element.build();
                    record = processOpenbareRuimte(element);
                    bufferedWriter.write(Utils.getOutputRecord(getOpenbareRuimteFields(), record, params));
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
    protected static String[] getOpenbareRuimteFields (){
        String[] verblijfsobjectFields = new String[] {
                OPR_IDENTIFICATIE,
                OPR_NAAM,
                OPR_TYPE,
                OPR_STATUS,
                OPR_GECONSTATEERD,
                OPR_DOCUMENTDATUM,
                OPR_DOCUMENTNUMMER,
                OPR_WOONPLAATS_REF
        };
        return verblijfsobjectFields;
    }

    /**
     *
     * @param element
     * @throws Exception TODO proper exception handling
     */
    protected static HashMap<String, String> processOpenbareRuimte(OMElement element) throws Exception {
        HashMap<String, String> record = new HashMap<>();

        //Iterate over descendants one time versus separate XPath calls
        Iterator<OMNode> descendants = element.getDescendants(false);
        while (descendants.hasNext()) {
            OMNode node = (OMNode) descendants.next();
            if (node.getType() == 1) {
                OMElement nodeEl = (OMElement) node;
                String nodeLocalName = nodeEl.getQName().getLocalPart();
                if (nodeLocalName.equals(OPR_IDENTIFICATIE)) {
                    record.put(OPR_IDENTIFICATIE, nodeEl.getText());
                } else if (nodeLocalName.equals(OPR_NAAM)) {
                    record.put(OPR_NAAM, nodeEl.getText());
                } else if (nodeLocalName.equals(OPR_TYPE)) {
                    record.put(OPR_TYPE, nodeEl.getText());
                } else if (nodeLocalName.equals(OPR_STATUS)) {
                    record.put(OPR_STATUS, nodeEl.getText());
                } else if (nodeLocalName.equals(OPR_GECONSTATEERD)) {
                    record.put(OPR_GECONSTATEERD, nodeEl.getText());
                } else if (nodeLocalName.equals(OPR_DOCUMENTDATUM)) {
                    record.put(OPR_DOCUMENTDATUM, nodeEl.getText());
                } else if (nodeLocalName.equals(OPR_DOCUMENTNUMMER)) {
                    record.put(OPR_DOCUMENTNUMMER, nodeEl.getText());
                } else if (nodeLocalName.equals(OPR_WOONPLAATS_REF)) {
                    record.put(OPR_WOONPLAATS_REF, nodeEl.getText());
                }
            }
        }
        return record;
    }
}
