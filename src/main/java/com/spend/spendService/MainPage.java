/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spend.spendService;


import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Scanner;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.apache.commons.io.IOUtils;


/**
 *
 * @author elif_
 */
public class MainPage {
    
    private String connectionUrl;
    private Connection con;
    private Connection con2;   
    
    private int maxPage=10;
    private int maxThreadAnalysis=3;
    private Thread[] threadAnalysis;
    private Thread[] threadArray;
    private Thread[] threadSearchQueue;   
    
    private String searchQuery;
    
    public void main()
    {
        connectionJdbc();
        createSearchQueue();
    
        
        threadAnalysis=new Thread[maxThreadAnalysis];
        threadSearchQueue=new Thread[getSearchEngineNamesArray().length];
        
        
        try{
            int crawlId = createCrawl(searchQuery, "SearchEngineCrawler");
            threadArray=new Thread[maxPage];
            
            String[] selist = getSearchEngineNamesArray();
            
            int j = 0;
            for (Object se : selist) 
            {
                WorkerSearchQueue sworker = new WorkerSearchQueue( se.toString(), crawlId, connectionUrl);
                sworker.setName("Worker " + se.toString());            
                threadSearchQueue[j++] = sworker;
                sworker.start();
            try {
                Thread.sleep(300);
            } 
            catch (Exception ex) {
            }
        }
            
            for(int i=0; i<maxThreadAnalysis; i++)
            {    
                WorkerAnalyze sworker=new WorkerAnalyze(connectionUrl,i,maxThreadAnalysis);
                sworker.setName("Worker Endpoint Analyzer"+String.valueOf(i));
                threadAnalysis[i]=sworker;
                sworker.start();
            }
        }
        catch(Exception ex)
        {
            System.out.println(ex.getMessage());
        }
    }
   
    private void connectionJdbc()
    {
        try{
            Class.forName("com.mysql.jdbc.Driver");
            connectionUrl=getConnectionString();
            con=DriverManager.getConnection(connectionUrl);
            con2=DriverManager.getConnection(connectionUrl);
        }
        catch(Exception ex)
        {
            System.out.println("ERROR connectionJdbc function in MainPage.java "+ex.getMessage());
		//System.out.println(connectionUrl);
        }
    }
   
    
    private String getConnectionString()
    {
        String fileString;
        try {
            FileInputStream inputStream = new FileInputStream("db.conf");
            String HostName = "", Port = "", Username = "", Password = "", Schema = "";
            fileString = IOUtils.toString(inputStream);
            for (String s : fileString.split("\n")) {
                try {
                    String label = s.split(" ")[0];
                    String text = s.split(" ")[1];

                    switch (label) {
                        case "Hostname":
                            HostName = text;
                            break;
                        case "Port":
                            Port = text;
                            break;
                        case "Username":
                            Username = text;
                            break;
                        case "Password":
                            Password = text;
                            break;
                        case "Schema":
                            Schema = text;
                            break;
                    }
                } 
                catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
            }
            inputStream.close();
            return "jdbc:mysql://" + HostName + ":" + Port + "/" + Schema + "?user=" + Username + "&password=" + Password;

        } 
        catch (Exception ex) {
            System.out.println("Error getConnectionString function in MainPage.java "+ ex.getMessage());
            return "jdbc:mysql://localhost/crawler?user=root&password=62217769";
        } 
    }
    
    
    private void createSearchQueue()
    {
        try{
            String st=getSearchQuery();                        
            String[] selist = getSearchEngineNamesArray();
           
            for (Object se : selist) {               
                String SQLi = "INSERT INTO searchqueue (searchText,searchEngineName, disabled) VALUES (?,?,0)";
                PreparedStatement pstmt = con2.prepareStatement(SQLi);
                pstmt.setString(1, st);
                pstmt.setString(2, se.toString());

                pstmt.executeUpdate();
                pstmt.close();
            }
        }
        catch(Exception ex)
        {
            System.out.println(ex.getMessage());
        }
    }
       
    
    public int createCrawl(String queryText, String crawlerName) 
    {
        try {
            String SQL = "SELECT max(crawlid) FROM crawlrecord";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(SQL);
            int id = 0;
            if (rs.next()) {
                try {
                    id = rs.getInt(1);

                } catch (Exception ex) {
                }

            }
            rs.close();

            String SQLi = "INSERT INTO crawlrecord (crawlid,crawlerName,queryText) VALUES (?,?,?)";
            PreparedStatement pstmt
                    = con.prepareStatement(SQLi);
            pstmt.setInt(1, id + 1);
            pstmt.setString(2, crawlerName);
            pstmt.setString(3, queryText);
            pstmt.executeUpdate();
            pstmt.close();
            stmt.close();
            return id + 1;
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return 0;
    }
    
    
    private String getSearchQuery()
    {
        Scanner s=new Scanner(System.in);      
        System.out.print("Enter query : ");
        searchQuery=s.next();
        return searchQuery;
    }
    
    
    private String[] getSearchEngineNamesArray() {
        try {
            List<SearchEngine> selist = getSearchEnginesFromXml().getSearchEngines();

            String[] seNames = new String[selist.size()];
            int i = 0;
            for (SearchEngine se : selist) {
                seNames[i++] = se.name;
            }
            return seNames;
        } catch (Exception ex) {
            String[] seNames = {"SearchEngine", "Config", "Failure"};
            return seNames;
        }

    }
    
   
    private SearchEngines getSearchEnginesFromXml() {
        try {
            File file = new File("SearchEngines.xml");
            JAXBContext jaxbContext = JAXBContext.newInstance(SearchEngines.class
            );

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            SearchEngines searchEngineList = (SearchEngines) jaxbUnmarshaller.unmarshal(file);
            return searchEngineList;

        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return null;
    }    
}
