package servlets.module.challenge;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.owasp.encoder.Encode;


import utils.ShepherdLogManager;
import utils.SqlFilter;
import utils.Validate;
import dbProcs.Database;

/**
 * SQL Injection Escape Challenge - Does not use User specific keys
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
 * @author Mark Denihan
 *
 */
public class SqlInjectionEscaping extends HttpServlet
{
	//SQL Escaping Challenge
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(SqlInjectionEscaping.class);
	private static String levelName = "SQL Injection Escaping Challenge";
	public static String levelHash = "8c3c35c30cdbbb73b7be3a4f8587aa9d88044dc43e248984a252c6e861f673d4";
	//private static String levelResult = ""; //Stored in vulnerable DB. Not user Specific
	/**
	 * This SQL Injection Module Class uses a poor escaping method to sanitise user data being sent to a MySQL interpreter
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		HttpSession ses = request.getSession(true);
		
		//Translation Stuff
		Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
		ResourceBundle errors = ResourceBundle.getBundle("i18n.servlets.errors", locale);
		ResourceBundle bundle = ResourceBundle.getBundle("i18n.servlets.challenges.sqli.sqliEscaping", locale);
		if(Validate.validateSession(ses))
		{
			ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), ses.getAttribute("userName").toString());
			log.debug(levelName + " servlet accessed by: " + ses.getAttribute("userName").toString());
			PrintWriter out = response.getWriter();  
			out.print(getServletInfo());
			String htmlOutput = new String();
			String ApplicationRoot = getServletContext().getRealPath("");
			Connection conn = Database.getChallengeConnection(ApplicationRoot, "SqlChallengeEscape");
			
			try
			{
				String aUserId = request.getParameter("aUserId");
				log.debug("User Submitted - " + aUserId);
				aUserId = aUserId.replaceAll("'", "\\\\'"); //Replace ' with \'
				log.debug("Escaped to - " + aUserId);
				
				log.debug("Getting Connection to Database");
				
				//#Hackathon DK - SQL Injection
				//ResultSet resultSet = stmt.executeQuery("SELECT * FROM customers WHERE customerId = '" + aUserId + "'");
				String query = "SELECT * FROM customers WHERE customerId = ?";
				PreparedStatement stmt = conn.prepareStatement(query);
				stmt.setString(1, SqlFilter.levelOne(SqlFilter.levelTwo(SqlFilter.levelThree(SqlFilter.levelFour(aUserId)))));					
				ResultSet resultSet = stmt.executeQuery(query);			
				int i = 0;
				htmlOutput = "<h2 class='title'>" + bundle.getString("response.searchResults")+ "</h2>";
				htmlOutput += "<table><tr><th>"+ bundle.getString("response.table.name") +"</th><th>"+ bundle.getString("response.table.address") +"</th><th>"+ bundle.getString("response.table.comment") +"</th></tr>";
				
				log.debug("Opening Result Set from query");
				while(resultSet.next())
				{
					log.debug("Adding Customer " + resultSet.getString(2));
					htmlOutput += "<tr><td>"
						+ Encode.forHtml(resultSet.getString(2)) + "</td><td>" 
						+ Encode.forHtml(resultSet.getString(3)) + "</td><td>"
						+ Encode.forHtml(resultSet.getString(4)) + "</td></tr>";
					i++;
				}
				htmlOutput += "</table>";
				if(i == 0)
				{
					htmlOutput = "<p>"+bundle.getString("response.noResults")+"</p>";
				}
			}
			catch (SQLException e)
			{
				log.debug("SQL Error caught - " + e.toString());
				htmlOutput += "<p>"+errors.getString("error.detected")+"</p>" +
					"<p>" + Encode.forHtml(e.toString()) + "</p>";
			}
			catch(Exception e)
			{
				out.write(errors.getString("error.funky"));
				log.fatal(levelName + " - " + e.toString());
			}
			finally {
				Database.closeConnection(conn);
			}
			log.debug("Outputting HTML");
			out.write(htmlOutput);
		}
		else
		{
			log.error(levelName + " servlet accessed with no session");
		}
	}
}
