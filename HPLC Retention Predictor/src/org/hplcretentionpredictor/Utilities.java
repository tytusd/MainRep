package org.hplcretentionpredictor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;

public class Utilities {

	public static void parseCSV(String fileName, ArrayList<IsocraticCompound> compounds)
	{
		File file = new File(fileName);
		System.out.println(fileName);
		compounds.clear();
		try 
		{
			CSVParser parser = CSVParser.parse(file, Charset.defaultCharset(), CSVFormat.RFC4180);
			IsocraticCompound compound = null;
			
			for(CSVRecord record : parser)
			{
				String key = record.get(0);
				key = key.toLowerCase();
				
				if(key.equals("id")){
					compound = new IsocraticCompound();
				}
				else if(key.equals("names")){
					compound.setId(record.get(1));
				}
				else if(key.equals("concentration")){
					for(int i = 1; i < record.size(); i++)
					{
						if(record.get(i).equals(""))
						{
							break;
						}
						double concentrationValue = Double.parseDouble(record.get(i));
						compound.getConcentrationList().add(concentrationValue);
					}
				}
				else if(key.equals("log k")){
					for(int i = 1; i < record.size(); i++)
					{
						if(record.get(i).equals(""))
						{
							break;
						}
						double logKValue = Double.parseDouble(record.get(i));
						compound.getLogKList().add(logKValue);
					}
				}
				else if(key.equals("")){
					if(compound.getConcentrationList().size() > 1){
						compounds.add(compound);	
					}
					compound = null;
				}
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static long computeCRC(InputStream stream)
	{
		CRC32 checksum = new CRC32();
		boolean computed = false;
		byte[] buffer = new byte[4096];
		
		while(!computed)
		{
			try 
			{
				int bytesRead = stream.read(buffer);
				if(bytesRead < 0)
				{
					computed = true;
				}
				else if(bytesRead > 0 )
				{
					checksum.update(buffer, 0, bytesRead);
				}
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		try 
		{
			stream.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		return checksum.getValue();
	}
	
	public static boolean areDifferent(InputStream stream1, InputStream stream2)
	{
		return computeCRC(stream1)!=computeCRC(stream2);
	}
	
	
	/*Method not in use
	 */
	public static void updateFileFromInternet(String fileName, String fileURL)
	{
		InputStream urlStream = null;
		InputStream fileStream = null;
		try 
		{
			urlStream = new URL(fileURL).openStream();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		try 
		{
			fileStream = new FileInputStream(new File(fileName));
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
		
		boolean diff = areDifferent(urlStream, fileStream);
		
		if(diff)
		{
			File f = new File(fileName);
			f.delete();
			try 
			{
				FileUtils.copyURLToFile(new URL(fileURL), f);
			} catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
	}
	
	public static long urlFileSize(String url){
		long fileSize = -1;
		try 
		{
			URL urlFile = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) urlFile.openConnection();
			conn.setRequestMethod("HEAD");
			conn.getInputStream();
			fileSize = conn.getContentLength();
		} 
		catch (MalformedURLException e) 
		{
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileSize;
	}
	
    public static ArrayList<IsocraticCompound> parseDirectory(File subDir, ArrayList<IsocraticCompound> list){
    	if(subDir != null){
    		DirectoryStream<Path> files = null;
			try {
				files = Files.newDirectoryStream(subDir.toPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
    		Iterator<Path> iter = files.iterator();
    		while(iter.hasNext()){
    			Path path = iter.next();
    			String extension = FilenameUtils.getExtension(path.toString());
    			if(extension.equalsIgnoreCase("lcxml")){
    				File f = path.toFile();
    				String[] values = xmlFileParser(f);
    				IsocraticCompound c = valuesToIsocraticCompoundObj(values);
    				if(c != null){
    					list.add(c);
    				}
    			}
    		}
    		try {
				files.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    	return list;
    }
    
    /**
     * This function parses a LCXML file and returns a string array containing:
     * compound name
     * a0,a1,a2,b1,b2
     * @param selectedFile
     * @return
     */
    public static String[] xmlFileParser(File selectedFile){
    	String compoundName="",
    			a0 = "",
    			a1 = "",
    			a2 = "",
    			b1 = "",
    			b2 = "";
    	boolean isCompound = false,
    			isA0 = false, 
    			isA1 = false,
    			isA2 = false,
    			isB1 = false,
    			isB2 = false;
		
    		XMLInputFactory2 factory = (XMLInputFactory2) XMLInputFactory2.newInstance();
    		XMLStreamReader parser = null;
    		factory.configureForSpeed();
    		try {
				parser = (XMLStreamReader2) factory.createXMLStreamReader(selectedFile);
			} catch (XMLStreamException e1) {
				e1.printStackTrace();
			}
    		assert parser != null;
    		try {
				while(parser.hasNext()) {
					int eventType = parser.next();
				  switch(eventType) {
				    case XMLStreamConstants.START_ELEMENT:
				      String tag = parser.getLocalName();
				      if(tag.equalsIgnoreCase("compound")){
				    	  isCompound = true;
				      }
				      else if(tag.equalsIgnoreCase("a0")){
				    	  isA0 = true; 
				      }
				      else if(tag.equalsIgnoreCase("a1")){
				    	  isA1 = true;
				      }
				      else if(tag.equalsIgnoreCase("a2")){
				    	  isA2 = true;
				      }
				      else if(tag.equalsIgnoreCase("b1")){
				    	  isB1 = true;
				      }
				      else if(tag.equalsIgnoreCase("b2")){
				    	  isB2 = true;
				      }

				      break;
				 
				    case XMLStreamConstants.CHARACTERS:
				      if (!parser.isWhiteSpace()){
				    	  String value = parser.getText();
				    	  if(isCompound){
				    		  compoundName = value;
				    	  }
				    	  else if(isA0){
				    		  a0 = value;
				    	  }
				    	  else if(isA1){
				    		  a1 = value;
				    	  }
				    	  else if(isA2){
				    		  a2 = value;
				    	  }
				    	  else if(isB1){
				    		  b1 = value;
				    	  }
				    	  else if(isB2){
				    		  b2 = value;
				    	  }
				      }
				    	  
				        break; 
				 
				    case XMLStreamConstants.END_ELEMENT:
				    	  isCompound = false;
				    	  isA0 = false; 
				    	  isA1 = false;
				    	  isA2 = false;
				    	  isB1 = false;
				    	  isB2 = false;
				    	  break;
				 
				    default:
				    	  break;
				  }
				}
			} catch (XMLStreamException e1) {
				e1.printStackTrace();
			}
    		String[] result = {compoundName,a0,a1,a2,b1,b2};
    		return result;
    }	
    
    /**
     * This function takes in parsed LCXML file output and returns an IsocraticCompound type object
     * @param values
     * @return
     */
    public static IsocraticCompound valuesToIsocraticCompoundObj(String[] values){
		double[] padeCoefficients = new double[5];
		try{
			padeCoefficients[0] = Double.parseDouble(values[1]);
			padeCoefficients[1] = Double.parseDouble(values[2]);
			padeCoefficients[2] = Double.parseDouble(values[3]);
			padeCoefficients[3] = Double.parseDouble(values[4]);
			padeCoefficients[4] = Double.parseDouble(values[5]);
		}
		catch(NumberFormatException e){
			e.printStackTrace();
			return null;
		}
		
		IsocraticCompound compound = new IsocraticCompound();
		for(int i = 0; i < 20; i++){
			double phi = i*0.05;
			double logK = (padeCoefficients[0] + padeCoefficients[1]*phi + padeCoefficients[2]*phi*phi)/(1 + padeCoefficients[3]*phi + padeCoefficients[4]*phi*phi);
			compound.getConcentrationList().add(phi);
			compound.getLogKList().add(logK);
		}
		compound.setId(values[0]);
		return compound;
    }
    
    /**
     * Reads dollar-seperated values database(LCDSV) that contains 
     * filename,compound name, a0, a1, a2, b1, b2, last-modified date, CRC32.
     * The sole purpose of maintaining this database is to
     * reduce costs of parsing entire XML files.
     * @param selectedFile
     * @return
     */
    public static ArrayList<IsocraticCompound> readDatabase(File selectedFile){
    	BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(selectedFile)));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
    	String line;
    	ArrayList<IsocraticCompound> compounds = new ArrayList<IsocraticCompound>(16);
    	try {
			while((line = br.readLine()) != null){
				String[] words = line.split("\\$");
				double[] padeCoefficients = new double[6];
				try{
					padeCoefficients[0] = Double.parseDouble(words[2]);
					padeCoefficients[1] = Double.parseDouble(words[3]);
					padeCoefficients[2] = Double.parseDouble(words[4]);
					padeCoefficients[3] = Double.parseDouble(words[5]);
					padeCoefficients[4] = Double.parseDouble(words[6]);
				}
				catch(NumberFormatException e){
					e.printStackTrace();
					return null;
				}
				
				IsocraticCompound compound = new IsocraticCompound();
				for(int i = 0; i < 20; i++){
					double phi = i*0.05;
					double logK = (padeCoefficients[0] + padeCoefficients[1]*phi + padeCoefficients[2]*phi*phi)/(1 + padeCoefficients[3]*phi + padeCoefficients[4]*phi*phi);
					compound.getConcentrationList().add(phi);
					compound.getLogKList().add(logK);
				}
				compound.setId(words[1]);
				compounds.add(compound);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
    	try {
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return compounds;
    }
    
    /***
     * Checks if there are any new files added or any existed files modified in the local database directory 
     * since the last time compressed database was updated.
     * @param dir
     * @param dsv
     */
    public static boolean checkForLocalUpdates(File dir, File dsv){
    	SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    	Map<String,String[]> map = new HashMap<String,String[]>();
    	BufferedReader reader = null;
    	
    	boolean hasMapChanged = false;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(dsv)));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
    	String line = null;
    	try {
    		
    		//Build a map of local database files - key - filename, value - [compound name, a0,a1,a2,b1,b2,last modified,CRC32]
			while((line = reader.readLine())!= null){
				String[] words = line.split("\\$");
				String[] values = Arrays.copyOfRange(words, 1, words.length);
				map.put(words[0], values);
			}
			
			DirectoryStream<Path> files = null;
			try {
				files = Files.newDirectoryStream(dir.toPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
    		Iterator<Path> iter = files.iterator();
    		
    		//Parse each file in the directory specified on the local machine.
    		while(iter.hasNext()){
    			Path path = iter.next();
    			String extension = FilenameUtils.getExtension(path.toString());
    			
    			if(extension.equalsIgnoreCase("lcxml")){
    				File f = path.toFile();
    				String fileName = f.getName();
    				
    				//If the map doesn't contain this file, parse this xml, find its CRC and place it in the map
    				if(!map.containsKey(fileName)){
    					String[] xmlValues = xmlFileParser(f);
    					String[] mapValues = new String[xmlValues.length+2];
    					for(int i = 0; i < mapValues.length-2; i++){
    						mapValues[i] = xmlValues[i];
    					}
    					mapValues[mapValues.length-2] = sdf.format(f.lastModified());
    					mapValues[mapValues.length-1] = computeCRC(new FileInputStream(f))+"";
    					map.put(fileName, mapValues);
    					hasMapChanged = true;
    				}
    				else{
    					//If the map contains this file but its not the same since the local database summary was last updated, then replace the entry in the map
    					String storedLastDate = map.get(fileName)[6];
    					String currentLastModified = sdf.format(f.lastModified());
    					if(!storedLastDate.equals(currentLastModified)){
    						String[] xmlValues = xmlFileParser(f);
        					String[] mapValues = new String[xmlValues.length+2];
        					for(int i = 0; i < mapValues.length-2; i++){
        						mapValues[i] = xmlValues[i];
        					}
        					mapValues[mapValues.length-2] = currentLastModified;
        					mapValues[mapValues.length-1] = computeCRC(new FileInputStream(f))+"";
        					map.remove(fileName);
        					map.put(fileName, mapValues);
        					hasMapChanged = true;
    					}
    				}
    				//TODO: One more case here. What if the file is deleted? The entry doesnt get deleted from LCDSV file
    			}
    		}
    		try {
				files.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	//End of reading and parsing files above.
    	
    	if(hasMapChanged){
    		//Begin updating LCDSV database - DSV means dollar seperated value
        	BufferedWriter writer;
        	Iterator<String> iter = map.keySet().iterator();
        	try{
        		writer = new BufferedWriter(new FileWriter(dsv,false));
    	    	while(iter.hasNext()){
    	    		String key = iter.next();
    	    		String[] values = map.get(key);
    	    		StringBuffer sb = new StringBuffer();
    	    		sb.append(key+"$");
    	    		for(String value: values){
    	    			sb.append(value+"$");
    	    		}
    	    		sb.setLength(sb.length()-1);
    	    		writer.write(sb.toString()+"\n");
    	    	}
    	    	writer.close();
        	}
        	catch(IOException e){
        		e.printStackTrace();
        	}
    	}
    	return hasMapChanged;
    }

    /**
     * Uses the local database summary file and online database summary file to compare the difference.
     * If there are any new or modified files on the web, add them to a list and return the list.
     * @param urlStr
     * @param localDb
     * @return 
     */
    public static List<String> findDifferenceInLocalAndWebDb(String urlStr, File localDb){
    	List<String> filesNotInLocalDb = new ArrayList<String>();
		
    	try {
			URL url = new URL(urlStr);
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
			String line = null;
			Map<String,String> urlMap = new HashMap<String,String>();
			Map<String,String> localMap = new HashMap<String,String>();
			
			//Build a map for url files with - filename , CRC value pair
			while((line = reader.readLine()) != null){
				line = line.trim();
				String[] values = line.split("\\$");
				urlMap.put(values[0], values[1]);
			}
			reader.close();
			
			//Build a map for local files with - filename, CRC value pair
			reader = new BufferedReader(new FileReader(localDb));
			while((line = reader.readLine()) != null){
				line = line.trim();
				String[] values = line.split("\\$");
				localMap.put(values[0], values[values.length-1]);
			}
			reader.close();
			//Find the files that need to be downloaded from the web as local db doesnt have it/ needs to be modified.
			Iterator<String> iter = urlMap.keySet().iterator();
			while(iter.hasNext()){
				String key = iter.next();
				if(!localMap.containsKey(key) ){
					filesNotInLocalDb.add(key);
				}
				else if(!(localMap.get(key).equals(urlMap.get(key)))){
					filesNotInLocalDb.add(key);
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return filesNotInLocalDb;
    }
    
    public static boolean fileDownloader(String urlStr, String dest){
    	boolean isDone = false;
    	URL url = null;
    	try{
    		url = new URL(urlStr);
    		URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
	    	urlStr = uri.toASCIIString();
	    	url = new URL(urlStr);
    		ReadableByteChannel rbc = Channels.newChannel(url.openStream());
	    	FileOutputStream fos = new FileOutputStream(dest);
	    	fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
	    	isDone = true;
    	}
    	catch(Exception e){
    		e.printStackTrace();
    	}
    	return isDone;
    }
}
