package com.dimins.bag2;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;

/**
 * Main class
 */
public class BAG2toCsvApp {

    public static String NAMESPACE_PREFIX_OBJECTEN = "Objecten";
    public static String NAMESPACE_URI_OBJECTEN = "www.kadaster.nl/schemas/lvbag/imbag/objecten/v20200601";
    public static String NAMESPACE_PREFIX_HISTORIE = "Historie";
    public static String NAMESPACE_URI_HISTORIE = "www.kadaster.nl/schemas/lvbag/imbag/historie/v20200601";

    public static String OBJECT_TYPE_LOCAL_NAME_LIGPLAATS = "Ligplaats";
    public static String OBJECT_TYPE_LOCAL_NAME_VERBLIJFSOBJECT = "Verblijfsobject";
    public static String OBJECT_TYPE_LOCAL_NAME_STANDPLAATS = "Standplaats";
    public static String OBJECT_TYPE_LOCAL_NAME_NUMMMERAANDUIDING = "Nummeraanduiding";

    //user defined params
    public static String PARAM_INPUT_DIR = "input_dir";
    public static String PARAM_INPUT_FILENAME = "input_filename";
    public static String PARAM_OUTPUT_DIR = "output_dir";
    //internal params
    public static String PARAM_OUTPUT_FILENAME = "output_filename"; //determined from input_filename and output dir
    public static String PARAM_OUTPUT_DELIMITER = "output_delimiter";

    public static void main(String[] args) {
        HashMap<String, String> params = new HashMap();
        params = parseArguments(args);

        //Hardcode output delimiter for now
        params.put(PARAM_OUTPUT_DELIMITER, "\t");

        //For testing purposes (delete at some point)
//        params.put(PARAM_OUTPUT_DIR, "D:/Temp");
//        params.put(PARAM_INPUT_FILENAME, "src/test/resources/xml/lig_excerpt.xml");

        File inputDir = null;
        if (params.get(PARAM_INPUT_DIR) != null) {
            inputDir = new File(params.get(PARAM_INPUT_DIR));
            if (!inputDir.isDirectory()) {
                Utils.writeTimeStampedMessage("Input directory " + params.get(PARAM_INPUT_DIR) + " not found");
            }
        }

        File inputFile = null;
        if (params.get(PARAM_INPUT_FILENAME) != null && params.get(PARAM_INPUT_DIR) == null) {
            inputFile = new File(params.get(PARAM_INPUT_FILENAME));
            if (!inputFile.exists()) {
                Utils.writeTimeStampedMessage("Input file " + params.get(PARAM_INPUT_FILENAME) + " not found");
                System.exit(1);
            }
        }

        if (params.get(PARAM_INPUT_FILENAME) == null & params.get(PARAM_INPUT_DIR) == null) {
            Utils.writeTimeStampedMessage("An input dir (" + PARAM_INPUT_DIR + "=mydir) or input file (" + PARAM_INPUT_FILENAME + "=myfile) argument is required.");
            System.exit(1);
        }

        //app will not create output dir if it is not there
        File outputDIr = null;
        if (params.get(PARAM_OUTPUT_DIR) != null) {
            File outputDir = new File(params.get(PARAM_OUTPUT_DIR));
            if (!outputDir.isDirectory()) {
                Utils.writeTimeStampedMessage("Output dir " + params.get(PARAM_OUTPUT_DIR) + " not found");
                System.exit(1);
            }
        } else {
            Utils.writeTimeStampedMessage("An output dir (" + PARAM_OUTPUT_DIR + "=mydir) argument is required.");
            System.exit(1);
        }

        File[] xmlFiles = null;
        if(inputFile != null && inputDir == null) {
            String inputName = inputFile.getName();
            if (!inputName.toLowerCase().endsWith("xml")) {
                Utils.writeTimeStampedMessage("Input file " + params.get(PARAM_INPUT_FILENAME) + " does not seem to be XML. Expecting XML input (file with XML extension). ");
                System.exit(1);
            }
            xmlFiles = new File[] { inputFile };
        }
        if(inputDir != null) {
            xmlFiles = getXMLFiles(inputDir);
        }
        if(xmlFiles == null) {
            Utils.writeTimeStampedMessage("No XML files to process");
        }

        for(File xmlFile : xmlFiles) {
            //TODO set extension based on delimiter (txt or csv)
            String outputFilename = params.get(PARAM_OUTPUT_DIR) + "/" + xmlFile.getName() + ".txt";
            String objectType = getObjectTypeFromFilename(xmlFile.getName());
            if(objectType.isEmpty()){
                Utils.writeTimeStampedMessage("Could not determine object type from filename " + xmlFile.getName());
                break;
            }
            params.put(PARAM_OUTPUT_FILENAME, outputFilename);
            try {
                Utils.writeTimeStampedMessage("Converting " + xmlFile.getCanonicalPath() + " to delimited text.");
                if (objectType.equalsIgnoreCase(OBJECT_TYPE_LOCAL_NAME_VERBLIJFSOBJECT)) {
                    Verblijfsobject.processVerblijfsobjectXML(xmlFile, params);
                } else if (objectType.equalsIgnoreCase(OBJECT_TYPE_LOCAL_NAME_LIGPLAATS)) {
                    Ligplaats.processLigplaatsXML(xmlFile, params);
                } else if (objectType.equalsIgnoreCase(OBJECT_TYPE_LOCAL_NAME_STANDPLAATS)) {
                    Standplaats.processStandplaatsXML(xmlFile, params);
                } else if (objectType.equalsIgnoreCase(OBJECT_TYPE_LOCAL_NAME_NUMMMERAANDUIDING)) {
                    Nummeraanduiding.processNummeraanduidingXML(xmlFile, params);
                }
                Utils.writeTimeStampedMessage("Finished converting. Wrote output to " + new File(outputFilename).getCanonicalPath());
            } catch (Exception e) {
                Utils.writeTimeStampedMessage("ERROR: " + e.getLocalizedMessage());
            }
        }
    }

    /**
     * Parse commandline arguments
     */
    private static HashMap<String, String> parseArguments(String[] args) {
        HashMap<String, String> params = new HashMap<>();

        for (String arg : args) {
            int splitIndex = arg.indexOf("=");
            if (splitIndex < 0) {
                Utils.writeTimeStampedMessage("Malformed argument: " + arg + ". Expected format name=value.\n");
            }
            String argName = arg.substring(0, splitIndex);
            String argValue = arg.substring(splitIndex + 1);
            if (PARAM_INPUT_FILENAME.equals(argName)) {
                params.put(PARAM_INPUT_FILENAME, argValue);
            } else if (PARAM_INPUT_DIR.equals(argName)) {
                params.put(PARAM_INPUT_DIR, argValue);
            } else if (PARAM_OUTPUT_DIR.equals(argName)) {
                params.put(PARAM_OUTPUT_DIR, argValue);
            } else if (PARAM_OUTPUT_DELIMITER.equals(argName)) {
                params.put(PARAM_OUTPUT_DELIMITER, argValue);
            }
        }
        return params;
    }

    /**
     * BAG2 filenames indicate the object type in the file
     */
    private static String getObjectTypeFromFilename(String filename) {
        String objectType = "";
        String filenameLowercase = filename.toLowerCase();//we do not expect weird characters so we do not explicitly set locale

        if (filenameLowercase.indexOf("vbo") >= 0) {
            objectType = OBJECT_TYPE_LOCAL_NAME_VERBLIJFSOBJECT;
        } else if (filenameLowercase.indexOf("lig") >= 0) {
            objectType = OBJECT_TYPE_LOCAL_NAME_LIGPLAATS;
        } else if (filenameLowercase.indexOf("sta") >= 0) {
            objectType = OBJECT_TYPE_LOCAL_NAME_STANDPLAATS;
        } else if (filenameLowercase.indexOf("num") >= 0) {
            objectType = OBJECT_TYPE_LOCAL_NAME_NUMMMERAANDUIDING;
        }
        return objectType;
    }

    /**
     * Get all XML Files in a directory
     */
    private static File[] getXMLFiles(File inputDir) {
        FilenameFilter xmlFilenameFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return isXMLFile(name);
            }
        };
        File filesList[] = inputDir.listFiles(xmlFilenameFilter);
        return filesList;
    }

    /**
     * Return true if filename has a .xml extension (case independent)
     */
    private static boolean isXMLFile(String name) {
        boolean isXMLFile = false;
        if(name.toLowerCase().endsWith(".xml")){
            isXMLFile = true;
        }
        return isXMLFile;
    }

}