package dbProcs;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

/** 
 * Locates the database Properties File for Database manipulation methods. This file contains the application sign on credentials for the database.	
 * <br/><br/>
 * This file is part of the Security Shepherd Project.
 * 
 * The Security Shepherd project is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.<br/>
 * 
 * The Security Shepherd project is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.<br/>
 * 
 * You should have received a copy of the GNU General Public License
 * along with the Security Shepherd project.  If not, see <http://www.gnu.org/licenses/>. 
 *  @author Mark			
 */
public class FileInputProperties 
{
	private static org.apache.log4j.Logger log = Logger.getLogger(FileInputProperties.class);
	private static final String FILE_NOT_FOUND = "Error: Properties file can not be found: ";
	/**
	 * Reads the properties file for a specific property and returns it as a string.
	 * @param filename The file to read
	 * @param Property The name of the property to be found
	 * @return The value of the specified property to be found
	 */
	@SuppressWarnings("deprecation")
	public static String readfile(String filename, String Property) 
	{
		//log.debug("Debug: Properties filename: "+filename);
		String path = FilenameUtils.normalize(filename);
		File file = new File(path);
		// File file = new File(filename);
		String temp = "";
	    String result = "NO RESULT";
	    FileInputStream fis = null;
	    BufferedInputStream bis = null;
	    DataInputStream dis = null;
	    try 
	    {
		  //log.debug("Debug: Looking for Property: "+Property);
	      fis = new FileInputStream(file);
	      bis = new BufferedInputStream(fis);
	      dis = new DataInputStream(bis);
	      boolean bool = false;
	      while (dis.available() != 0) 
	      {
	        temp = dis.readLine();
	        if(temp.contains(Property))
	        {
	        	result = temp.substring(Property.length()+1, temp.length());
	        	//log.debug("Debug: Property Found: "+result);
	        	bool = true;
	        }
	      }
	      if(!bool)
	      {
	    	  // falso positivo, pois nao temos intencao de exibir logs do tipo debug no ambiente de producao
	    	  //log.debug("Debug: Property not found: "+Property);
	  	  }
	      fis.close();
	      bis.close();
	      dis.close();
	
	    } 
	    catch (FileNotFoundException e) 
	    {
	    	log.error(FILE_NOT_FOUND);
	    	log.error(e.toString());
	    	result = result +  e.toString();
	    } 
	    catch (IOException e) 
	    {
	    	log.error(FILE_NOT_FOUND);
	    	log.error(e.toString());
	    	result = result + e.toString();
	    }
	    return result;
	  }
}
