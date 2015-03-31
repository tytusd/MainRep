package org.hplcretentionpredictor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.zip.CRC32;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;

public class Utilities {

	public static void parseCSV(String fileName, ArrayList<IsocraticCompound> compounds)
	{
		File file = new File(fileName);
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
			// TODO Auto-generated catch block
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fileSize;
	}
}
