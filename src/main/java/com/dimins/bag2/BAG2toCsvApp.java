package com.dimins.bag2;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;

/**
 * Main class
 */
public class BAG2toCsvApp {

    //Make a separate class with constants?
    //public static String NAMESPACE_PREFIX_STANDLEVERING_GENERIEK = "st";
    //public static String NAMESPACE_URI_STANDLEVERING_GENERIEK = "http://www.kadaster.nl/schemas/standlevering-generiek/1.0";
    public static String NAMESPACE_PREFIX_OBJECTEN = "Objecten";
    public static String NAMESPACE_URI_OBJECTEN = "www.kadaster.nl/schemas/lvbag/imbag/objecten/v20200601";
    //public static String NAMESPACE_PREFIX_OBJECTEN_REF = "Objecten-ref";
    //public static String NAMESPACE_URI_OBJECTEN_REF = "www.kadaster.nl/schemas/lvbag/imbag/objecten-ref/v20200601";
    //public static String NAMESPACE_PREFIX_SL_BAGEXTRACT = "sl-bag-extract";
    //public static String NAMESPACE_URI_SL_BAGEXTRACT = "http://www.kadaster.nl/schemas/lvbag/extract-deelbestand-lvc/v20200601";
    public static String NAMESPACE_PREFIX_HISTORIE = "Historie";
    public static String NAMESPACE_URI_HISTORIE = "www.kadaster.nl/schemas/lvbag/imbag/historie/v20200601";

    public static String OBJECT_TYPE_LOCAL_NAME_LIGPLAATS = "Ligplaats";
    public static String OBJECT_TYPE_LOCAL_NAME_VERBLIJFSOBJECT = "Verblijfsobject";
    public static String OBJECT_TYPE_LOCAL_NAME_STANDPLAATS = "Standplaats";

    public static String PARAM_INPUT_FILENAME = "input_filename";
    public static String PARAM_OUTPUT_DIR = "output_dir";
    public static String PARAM_OUTPUT_FILENAME = "output_filename"; //determined from input_filename and output dir
    public static String PARAM_OUTPUT_DELIMITER = "output_delimiter";

    public static void main(String[] args) {
        HashMap<String,String> params = new HashMap();
        params = parseArguments(args);

        //Hardcode output delimiter for now
        params.put(PARAM_OUTPUT_DELIMITER, "\t");
//        params.put(PARAM_OUTPUT_DIR, "D:/temp");

//            params.put(PARAM_INPUT_FILENAME, "src/test/resources/xml/lig_excerpt.xml");
//            params.put(PARAM_INPUT_FILENAME, "src/test/resources/xml/vbo_excerpt.xml");
//            params.put(PARAM_INPUT_FILENAME, "src/test/resources/xml/sta_excerpt.xml");
//            params.put(PARAM_INPUT_FILENAME, "D:\\DI\\Projects\\7.1\\open-data\\kadaster-inspire-addressen\\data\\20211208\\9999STA08122021\\9999STA08122021-000001.xml");

        File inputFile = new File(params.get(PARAM_INPUT_FILENAME));
        if(!inputFile.exists()) {
            System.out.println("Input " + params.get(PARAM_INPUT_FILENAME) + " not found");
            System.exit(1);
        }

        String inputName = inputFile.getName();
        if(!inputName.toLowerCase().endsWith("xml")) {
            System.out.println("Input " + params.get(PARAM_INPUT_FILENAME) + " does not seem to be xml");
            System.exit(1);
        }

        //app will not create output dir if it is not there
        File outputDir = new File(params.get(PARAM_OUTPUT_DIR));
        if(!outputDir.isDirectory()) {
            System.out.println("Output dir " + params.get(PARAM_OUTPUT_DIR) + " not found");
            System.exit(1);
        }

        //TODO replace xml with txt or csv depending on delimiter
        String outputFilename = params.get(PARAM_OUTPUT_DIR) + "/" + inputName + ".txt";
        params.putIfAbsent(PARAM_OUTPUT_FILENAME, outputFilename);

        String objectType = getObjectTypeFromFilename(inputName);
        //TODO alert that objectType could not be found from filename

        try {
            if(objectType.equalsIgnoreCase("ligplaats")) {
                Ligplaats.processLigplaatsXML(inputFile, params);
            } else if (objectType.equalsIgnoreCase("verblijfsobject")){
                Verblijfsobject.processVerblijfsobjectXML(inputFile, params);
            } else if (objectType.equalsIgnoreCase( "standplaats")) {
                Standplaats.processStandplaatsXML(inputFile, params);
            }
        } catch(Exception e) {
            System.out.println("ERROR:" + e.getLocalizedMessage());
        }
    }

    /**
     * Parse commandline arguments passed to the script
     */
    private static HashMap<String, String> parseArguments(String[] args){
        HashMap<String, String> params = new HashMap<>();

        for (String arg : args) {
            int splitIndex = arg.indexOf("=");
            if (splitIndex < 0) {
                System.out.println("Malformed argument: " + arg + ". Expected format name=value.\n");
            }
            String argName = arg.substring(0, splitIndex);
            String argValue = arg.substring(splitIndex + 1);
            String[] splitArg = {argName, argValue};
            if(PARAM_INPUT_FILENAME.equals(splitArg[0])) {
                params.put(PARAM_INPUT_FILENAME, splitArg[1]);
            } else if (PARAM_OUTPUT_DIR.equals(splitArg[0])) {
                params.put(PARAM_OUTPUT_DIR, splitArg[1]);
            } else if (PARAM_OUTPUT_DELIMITER.equals(splitArg[0])) {
                params.put(PARAM_OUTPUT_DELIMITER, splitArg[1]);
            }
        }
        return params;
    }

    private static String getObjectTypeFromFilename(String filename){
        String objectType = "";
        String filenameLowercase = filename.toLowerCase();//we do not expect weird characters so we do not explicitly set locale

        if(filenameLowercase.indexOf("sta") >= 0) {
            objectType = OBJECT_TYPE_LOCAL_NAME_STANDPLAATS;
        } else if (filenameLowercase.indexOf("lig") >= 0) {
            objectType = OBJECT_TYPE_LOCAL_NAME_LIGPLAATS;
        } else if (filenameLowercase.indexOf("vbo") >= 0) {
            objectType = OBJECT_TYPE_LOCAL_NAME_VERBLIJFSOBJECT;
        }
        return objectType;
    }

}