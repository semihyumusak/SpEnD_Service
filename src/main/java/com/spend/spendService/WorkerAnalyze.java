/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spend.spendService;


import java.awt.EventQueue;
import java.awt.Toolkit;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Semih
 */
public class WorkerAnalyze extends Thread {

    private EventQueue eventQueue = null;
    private boolean running = true;
    private int count = 0;
    String connectionUrl = "jdbc:mysql://127.0.0.1/crawler?" + "user=admin&password=12345";
    Connection con;
    int threadNumber, numberOfThreads;
    
    WorkerAnalyze(String connectionString, int threadNumber, int numberOfThreads) {
        this.connectionUrl = connectionString;
        this.threadNumber = threadNumber;
        this.numberOfThreads = numberOfThreads;
        initializeJdbc();
    }

    /**
     * stopRunning stop the SimpleWorker thread.
     */
    public void stopRunning() {
        running = false;

    }

    /**
     * run do the work.
     */
    public void run() {
        System.out.println("WorkerAnalyzer.run : Thread "
                + Thread.currentThread().getName() + Thread.currentThread().getId() + " started");

        eventQueue = Toolkit.getDefaultToolkit().getSystemEventQueue();

        boolean wait = false;
        while (running) {
            try {
                if (wait) {
                    Thread.sleep(60000);
                    wait = false;
                }
                if (!getNextUrlAndAnalyze()) {
                    Thread.sleep(10000);//stopRunning();
                    //Thread.sleep(1000);
                } else {

                    System.gc();
                }
            } catch (SQLException s) {
                System.out.println(s.getMessage());
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                wait = true;
            }
        }
        System.out.println("WorkerAnalyzer.run : Thread "
                + Thread.currentThread().getName() + Thread.currentThread().getId() + " stopped");
    }                       // run
    
    
    private int getUnprocessedSeedUrlId() throws SQLException {
        PreparedStatement pstmt
                = con.prepareStatement("select id,url from seedurlraw where isEndpoint is null and MOD(id,?)=? order by id asc;");
        // execute the query, and get a java resultset
        pstmt.setInt(2, threadNumber);
        pstmt.setInt(1, numberOfThreads);
        ResultSet rs = pstmt.executeQuery();

        // iterate through the java resultset
        while (rs.next()) {
            int id = rs.getInt("id");
            rs.close();
            pstmt.close();
            return id;
            //String firstName = rs.getString("url");
        }
        rs.close();
        pstmt.close();
        return 0;
    }


    private int isKnownEndpoint(String url) throws SQLException {

        if (url.substring(url.length() - 1, url.length()).equals("/")) {
            url = url.substring(0, url.length() - 1);
        }
        int isEndpoint = 0;
        try {
            PreparedStatement pstmt
                    = con.prepareStatement("select endpointUrl from endpoints where endpointUrl ='" + url + "';");
            // execute the query, and get a java resultset
            ResultSet rs = pstmt.executeQuery();;

            // iterate through the java resultset
            while (rs.next()) {
                //int id = rs.getInt("id");
                return 2;
            }
            rs.close();
            pstmt.close();
        } catch (Exception ex) {

        }

        //WHERE 'John Smith and Peter Johnson are best friends' LIKE
        //CONCAT('%', name, '%')
        try {
            PreparedStatement pstmt
                    = con.prepareStatement("select id,url,isEndpoint from seedurlraw where url ='" + url + "';");
            // execute the query, and get a java resultset
            ResultSet rs = pstmt.executeQuery();;

            // iterate through the java resultset
            if (rs.next()) {
                //int id = rs.getInt("id");
                int temp = rs.getInt("isEndpoint");
                if (temp == 2) {
                    rs.close();
                    pstmt.close();
                    return temp;
                }
            }
            rs.close();
            pstmt.close();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return isEndpoint;
    }

    private String getSeedUrlFromId(int id) throws SQLException {

        PreparedStatement pstmt
                = con.prepareStatement("select id,url from seedurlraw where id =" + String.valueOf(id) + ";");
        // execute the query, and get a java resultset
        ResultSet rs = pstmt.executeQuery();;

        // iterate through the java resultset
        while (rs.next()) {
            //int id = rs.getInt("id");
            String url = rs.getString("url");
            rs.close();
            pstmt.close();
            return url;
        }
        rs.close();
        pstmt.close();
        return "";
    }

    private void updateSeedUrlFromId(int id, int isEndpointResult) throws SQLException {

        PreparedStatement pstmt
                = con.prepareStatement("UPDATE seedurlraw set isEndpoint=? where id =?;");

        pstmt.setInt(1, isEndpointResult);
        pstmt.setInt(2, id);
// execute the query, and get a java resultset
        pstmt.executeUpdate();

        pstmt.close();

    }
    private void updateSeedUrlFromId(int id, int isEndpointResult, String url) throws SQLException {

        PreparedStatement pstmt
                = con.prepareStatement("UPDATE seedurlraw set isEndpoint=?, url = ? where id =?;");

        pstmt.setInt(1, isEndpointResult);
        pstmt.setString(2, url);
        pstmt.setInt(3, id);
// execute the query, and get a java resultset
        pstmt.executeUpdate();

        pstmt.close();

    }

    public static String extractUrl(String text) {
        List<String> containedUrls = new ArrayList<String>();
        String urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
        Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
        Matcher urlMatcher = pattern.matcher(text);

        if ((urlMatcher.find())) {
            String url = text.substring(urlMatcher.start(0),
                    urlMatcher.end(0));
            if (url.endsWith(".") || url.endsWith(";") || url.endsWith(":") || url.endsWith(".") || url.endsWith(",") ) {
                url = url.substring(0, url.length() - 1);
            }
            if (url.contains("<")) {
                url = url.split("<")[0];
            }

            return url;
        }
        return text;
    }
    
    public static String extractUrlRemoveEndingDash(String text) {
        List<String> containedUrls = new ArrayList<String>();
        String urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
        Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
        Matcher urlMatcher = pattern.matcher(text);

        if ((urlMatcher.find())) {
            String url = text.substring(urlMatcher.start(0),
                    urlMatcher.end(0));
            if (url.endsWith(".") || url.endsWith(";") || url.endsWith(":") || url.endsWith(".") || url.endsWith(",") || url.endsWith("/")) {
                url = url.substring(0, url.length() - 1);
            }
            if (url.contains("<")) {
                url = url.split("<")[0];
            }

            return url;
        }
        return text;
    }

    private boolean getNextUrlAndAnalyze() throws AbstractMethodError, SQLException, InterruptedException {

        int urlid = getUnprocessedSeedUrlId();
        if (urlid == 0) {
            return false;
        }
        updateSeedUrlFromId(urlid, 0);//update for process start (prevents other threads to use this id) 
        String url = getSeedUrlFromId(urlid);

        int isKnownEndpoint = isKnownEndpoint(url);
        if (isKnownEndpoint > 0) {
            updateSeedUrlFromId(urlid, isKnownEndpoint); //update for sparql endpoint
        } else {
            if (JenaSparql.isSparqlEndpoint(url)) {
                updateSeedUrlFromId(urlid, 2); //update for sparql endpoint
            } else {
                String tempurl = extractUrl(url);
                if (!tempurl.equals(url)) {
                    if (JenaSparql.isSparqlEndpoint(tempurl)) {                       
                        updateSeedUrlFromId(urlid, 2,extractUrlRemoveEndingDash(tempurl));
                    } else {
                        updateSeedUrlFromId(urlid, 1,tempurl);// update for non-endpoint                    
                    }
                } 
                else {
                    updateSeedUrlFromId(urlid, 1);
                }
            }
        }
        return true;
    }

    private void initializeJdbc() {
        try {
            Class.forName("com.mysql.jdbc.Driver");//.newInstance();
            con = DriverManager.getConnection(connectionUrl);
        } catch (Exception ex) {
            String a = ex.getMessage();
        }
    }
}
