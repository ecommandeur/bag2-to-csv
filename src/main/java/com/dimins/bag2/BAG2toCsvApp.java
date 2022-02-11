package com.dimins.bag2;

import java.io.File;
import java.util.HashMap;

/**
 * Main class
 */
public class BAG2toCsvApp {

    //Make a separate class with constants?
    public static String NAMESPACE_PREFIX_STANDLEVERING_GENERIEK = "st";
    public static String NAMESPACE_URI_STANDLEVERING_GENERIEK = "http://www.kadaster.nl/schemas/standlevering-generiek/1.0";
    public static String NAMESPACE_PREFIX_OBJECTEN = "Objecten";
    public static String NAMESPACE_URI_OBJECTEN = "www.kadaster.nl/schemas/lvbag/imbag/objecten/v20200601";
    public static String NAMESPACE_PREFIX_OBJECTEN_REF = "Objecten-ref";
    public static String NAMESPACE_URI_OBJECTEN_REF = "www.kadaster.nl/schemas/lvbag/imbag/objecten-ref/v20200601";
    public static String NAMESPACE_PREFIX_SL_BAGEXTRACT = "sl-bag-extract";
    public static String NAMESPACE_URI_SL_BAGEXTRACT = "http://www.kadaster.nl/schemas/lvbag/extract-deelbestand-lvc/v20200601";

    public static String OBJECT_TYPE_LOCAL_NAME_LIGPLAATS = "Ligplaats";
    public static String OBJECT_TYPE_LOCAL_NAME_VERBLIJFSOBJECT = "Verblijfsobject";

    public static String PARAM_INPUT_FILENAME = "input_filename";
    public static String PARAM_OUTPUT_DIR = "output_dir";
    public static String PARAM_OUTPUT_FILENAME = "output_filename"; //determined from input_filename and output dir
    public static String PARAM_OUTPUT_DELIMITER = "output_delimiter";

    public static void main(String[] args) {
        HashMap<String,String> params = new HashMap();

        //Set params for testing
        params.put(PARAM_OUTPUT_DELIMITER, "\t");
        params.put(PARAM_OUTPUT_DIR, "D:/temp");
//        params.put(PARAM_INPUT_FILENAME, "src/test/resources/xml/lig_excerpt.xml");
        params.put(PARAM_INPUT_FILENAME, "src/test/resources/xml/vbo_excerpt.xml");
//        params.put(PARAM_OUTPUT_FILENAME, "D:/temp/lig_excerpt.txt");
        params.put(PARAM_OUTPUT_FILENAME, "D:/temp/vbo_excerpt.txt");

        //TODO determine objecttype from filename
//        String objectType = "Ligplaats";
        String objectType = "Verblijfsobject";


        File file = new File(params.get(PARAM_INPUT_FILENAME));
        if(!file.exists()) {
            System.out.println("Input " + params.get(PARAM_INPUT_FILENAME) + " not found");
            System.exit(1);
        }

        try {
            if(objectType.equalsIgnoreCase("ligplaats")) {
                Ligplaats.processLigplaatsXML(file, params);
            } else if (objectType.equalsIgnoreCase("verblijfsobject")){
                Verblijfsobject.processVerblijfsobjectXML(file, params);
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


}