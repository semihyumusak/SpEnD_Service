/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spend.spendService;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.commons.io.IOUtils;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author elif_
 */
public class DomainLearning {
    private String connectionUrl;
    private Connection con;
    private String newQuery;
    private List<String> domainList;
    
    public void main(){
        connectionJdbc();
        GetNewQuery();
    }
    
    private void GetNewQuery(){
        try{
            TimerTask timertask=new TimerTask(){
              public void run(){
                  try{
                      domainList=new ArrayList<String>();
                      String[] seList=getSearchEngineNamesArray();
                      /* get urls from seedurlraw table */
                      PreparedStatement psmt=con.prepareStatement("SELECT url FROM seedurlraw");
                      ResultSet rs=psmt.executeQuery();                     
                      String regex="[/]";
                      String regex2="[.]";
                      String PLDomain;
                      while(rs.next()){
                          PLDomain=rs.getString("url");
                          PLDomain=PLDomain.replaceAll("http://|https://", "");
                          Pattern p=Pattern.compile(regex);
                          Matcher m=p.matcher(PLDomain);
                          if(m.find())
                          {
                              PLDomain=PLDomain.substring(0, m.start());
                          }
                          Pattern p2=Pattern.compile(regex2);
                          Matcher m2=p2.matcher(PLDomain);
                          int count=0;
                          while(m2.find())
                          {
                              count++;
                          }
                          m2=p2.matcher(PLDomain);
                          if(count>1 && m2.find())
                          {
                              PLDomain=PLDomain.substring(m2.end());
                          }                                     
                          
                          //System.out.println(PLDomain);                        
                         
                          if (!domainList.contains(PLDomain)) {
                              domainList.add(PLDomain);
                              newQuery = "sparql endpoint site:" + PLDomain;
                              for (Object se : seList) {
                                  PreparedStatement psmt1 = con.prepareStatement("INSERT INTO searchqueue(searchText,disabled,searchEngineName) VALUES(?,0,?);");
                                  psmt1.setString(1, newQuery);
                                  psmt1.setString(2, se.toString());
                                  psmt1.executeUpdate();
                                  psmt1.close();
                              }
                          }
                      }
                  }
                  catch(Exception ex){
                      System.out.println("DomainLearning.java timertask run function SQL ERROR "+ex.getMessage());
                  }
              }  
            };
            Timer timer=new Timer();
            DateFormat dateformat=new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            Date date=dateformat.parse("20-07-2017 00:00:00"); // set date and time
            timer.schedule(timertask, date, 1000*60*60*24*7); // for a week 1000*60*60*24*7
        }
        catch(Exception ex){
            System.out.println("DomainLearning.java GetNewQuery function ERROR "+ex.getMessage());
        }
    }
    
    private String[] getSearchEngineNamesArray(){
        try{
            List<SearchEngine> selist=getSearchEnginesFromXml().getSearchEngines();
            String[] seNames=new String[selist.size()];
            int i=0;
            for(SearchEngine se:selist)
            {
                seNames[i++]=se.name;
            }
            return seNames;
        }
        catch(Exception ex)
        {
            System.out.println("DomainLearning.java getSearchEngineNamesArray function ERROR "+ex.getMessage());
            String[] seNames={"ERROR"};
            return seNames;
        }
    }
    
    private SearchEngines getSearchEnginesFromXml(){
        try{
            File file=new File("SearchEngines.xml");
            JAXBContext jaxbContext=JAXBContext.newInstance(SearchEngines.class);
            Unmarshaller jaxbUnmarshaller=jaxbContext.createUnmarshaller();
            SearchEngines searchEngineList=(SearchEngines)jaxbUnmarshaller.unmarshal(file);
            return searchEngineList;
        }
        catch(JAXBException ex){
            System.out.println("DomainLearning.java getSearchEnginesFromXml function ERROR "+ex.getMessage());   
            ex.printStackTrace();
        }
        return null;
    }
    
    private void connectionJdbc(){
        try{
            Class.forName("com.mysql.jdbc.Driver");
            connectionUrl=getConnectionString();
            con=DriverManager.getConnection(connectionUrl);
        }
        catch(Exception ex)
        {
            System.out.println("DoaminLearning.java connectionJdbc function ERROR "+ex.getMessage());
        }
    }
    
    private String getConnectionString(){
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
            System.out.println("DomainLearning.java getConnectionString function ERROR "+ex.getMessage());
            return "jdbc:mysql://localhost/crawler?user=root&password=62217769";
        } 
    }
}
