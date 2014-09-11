package es.upm.oeg.ar2dtool;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import es.upm.oeg.ar2dtool.utils.graphml.GraphMLGenerator;
import es.upm.oeg.ar2dtool.utils.gv.GraphViz;
import es.upm.oeg.ar2dtool.utils.gv.Rdf2Gv;

public class Main {

	public static String syntaxErrorMsg = "Syntax error. Please use the following syntax \"java -jar ar2dtool.jar -i PathToInputRdfFile -o FileToOutputFile -t OutputFileType -c PathToConfFile [-d]\"";
	private static String pathToInputFile="";
	private static String pathToOuputFile="";
	private static String outputFileType="";
	private static String pathToConfFile="";
	
	private static boolean DEBUG=false;
	
	public static void main(String[] args) {

		//parsing args
		//syntax: 
		
		parseArgs(args);
		
		if((pathToInputFile.equals(""))||(outputFileType.equals(""))||(pathToOuputFile.equals(""))||(pathToConfFile.equals("")))
		{
			System.err.println(syntaxErrorMsg);
			return;
		}
		
		dbg("pathToInputFile:" + pathToInputFile);
		dbg("pathToOuputFile:" + pathToOuputFile);
		dbg("outputFileType:" + outputFileType);
		dbg("pathToConfFile:" + pathToConfFile);
		
		
		//read the config file
		ConfigValues cv = new ConfigValues(pathToConfFile);
		cv.readConfigValues();
		
		dbg("\n"+cv.toString());
		
		Rdf2Gv rgv = new Rdf2Gv(pathToInputFile,cv);
		
		rgv.parseRdf();

		dbg("Found classes" + rgv.getClasses());
		dbg("Found individuals" + rgv.getIndividuals());
		dbg("Found literals" + rgv.getLiterals());
		
		dbg("GraphViz	:\n" + rgv.getGvContent());
		
		String dotPath = cv.keys.get("pathToDot");
		GraphViz gv = new GraphViz(dotPath);
		gv.add(rgv.getGvContent());
		
		

		//write the GV file
		if(cv.keys.get("generateGvFile").equals("true"))
		{
			try {
				PrintWriter gvOut = new PrintWriter(pathToOuputFile+".gv");
				gvOut.println(rgv.getGvContent());
				gvOut.close();
			} catch (FileNotFoundException e) {
				System.err.println("Error while trying to generate the gv file at "+ pathToOuputFile+".gv");
				e.printStackTrace();
			}
		}
	
		
		
		//write the image file
		File out = new File(pathToOuputFile); 
		byte[] img = gv.getGraph(gv.getDotSource(), outputFileType);
		dbg("img byte[].length="+img.length);
		
		int code = gv.writeGraphToFile(img , out);

		dbg("writeGraphToFile returns="+code);
		if(code==-1)
		{
			System.err.println("An error ocurred when saving the file " + pathToOuputFile);
		}
		

		dbg("outputFile length="+out.length());
		if(out.length()==0)
		{
			System.err.println("File " + pathToOuputFile + " seems to be empty :(");
		}
		else
		{
			System.out.println("Done! File " + pathToOuputFile +" generated");
		}
		
		
//		//generate the GraphML file (reading the triples information from the Rdf2Gv object)
//		if(cv.keys.get("generateGraphMLFile").equals("true"))
//		{
//			GraphMLGenerator gml = new GraphMLGenerator(rgv.getDtLines());
//			dbg(gml.generateXML());
//		}
		
		
		
	}


	private static void parseArgs(String[] args) 
	{
		if(args.length<8)
		{
			System.err.println(syntaxErrorMsg);
			return;
		}
		
		for(int i=0; i<args.length; i++)
		{
			if(args[i].equals("-i"))
			{
				i++;
				pathToInputFile = args[i];
			}
			else
			{

				if(args[i].equals("-o"))
				{
					i++;
					pathToOuputFile = args[i];
				}
				else
				{

					if(args[i].equals("-t"))
					{
						i++;
						outputFileType = args[i];
					}
					else
					{

						if(args[i].equals("-c"))
						{
							i++;
							pathToConfFile = args[i];
						}
						else
						{
							if(args[i].equals("-d"))
							{
								DEBUG=true;
							}
							else
							{
								System.err.println(syntaxErrorMsg);
								return;
							}
						}
					}
				}
			}
		}
		
	}


	public static void dbg(String msg)
	{
		if(DEBUG)			
			System.out.println(msg);
	}
}
