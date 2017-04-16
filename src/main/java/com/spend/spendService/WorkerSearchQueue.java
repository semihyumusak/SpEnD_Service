/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spend.spendService;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebAssert;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.google.common.net.InternetDomainName;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Struct;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import javax.print.attribute.standard.DateTimeAtCompleted;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author Semih
 */
public class WorkerSearchQueue extends Thread {

    private int failIncrementalSleep = 0;
    public static int MAXWORK = 10;
    private int crawlId;
    private int maxPage;
    private EventQueue eventQueue = null;
    private int MAXSLEEP = 3000;
    private boolean running = true;
    private int count = 0;
    private SearchEngine searchEngine;
    String connectionUrl = "jdbc:mysql://127.0.0.1/crawler?" + "user=admin&password=12345";
    Connection con;
    
    WorkerSearchQueue(String se, int crawlId,String connectionString) {
        this.searchEngine = getSearchEngineFromName(se);
        this.MAXSLEEP = this.searchEngine.waitIntervalMs;
        this.crawlId = crawlId;
        this.connectionUrl = connectionString;
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
    private void initQueue() {

    }

    public void run() {
        initializeJdbc();
        int percent = 0;
        String msg = "";
        System.out.println("SimpleWorker.run : Thread "
                + Thread.currentThread().getName() + " started");

        eventQueue = Toolkit.getDefaultToolkit().getSystemEventQueue();
        
        final WebClient webClient = new WebClient(getBrowserVersionFromName(searchEngine.getDefaultBrowser()));//BrowserVersion.FIREFOX_24);

        while (running) {
            if (hasSearchQuery()) {
                search(getNextSearchQuery(), webClient);
                System.gc();
            } else {
                try {
                    Thread.sleep(10000);
                } catch (Exception ex) {
                }
            }
        }
        webClient.close();

        System.out.println("SimpleWorker.run : Thread "
                + Thread.currentThread().getName() + " stoped");
    }                       // run

    private boolean hasSearchQuery() {
        try {
            String SQL = "SELECT id, searchText FROM searchqueue where disabled=0 and isProcessStarted is null and searchEngineName=? LIMIT 1;";
            PreparedStatement selectstmt = con.prepareStatement(SQL);
            selectstmt.setString(1, searchEngine.name);
            ResultSet rs = selectstmt.executeQuery();

            while (rs.next()) {
                return true;
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return false;
    }

    class searchEngineQuery {

        public searchEngineQuery(int maxPage, String queryText) {
            this.maxPage = maxPage;
            this.queryText = queryText;
        }
        int maxPage;
        String queryText;
    };

    private searchEngineQuery getNextSearchQuery() {
        try {
            String SQL = "SELECT id, searchText, maxSearchPage FROM searchqueue where disabled=0 and isProcessStarted is null and searchEngineName=? LIMIT 1;";
            PreparedStatement selectstmt = con.prepareStatement(SQL);
            selectstmt.setString(1, searchEngine.name);
            ResultSet rs = selectstmt.executeQuery();

            while (rs.next()) {
                try {
                    int id = rs.getInt(1);
                    String searchText = rs.getString(2);
                    int maxPage = rs.getInt(3);
                    if (maxPage == 0) {
                        maxPage = this.maxPage;
                    } else {
                        this.maxPage = maxPage;
                    }
                    PreparedStatement updateStmt
                            = con.prepareStatement("UPDATE searchqueue set isProcessStarted=?, processStartDate=? where id =?;");

                    updateStmt.setInt(1, 1);
                    updateStmt.setTimestamp(2, new java.sql.Timestamp(System.currentTimeMillis()));
                    updateStmt.setInt(3, id);
                    updateStmt.executeUpdate();
                    updateStmt.close();
                    return new searchEngineQuery(maxPage, searchText);

                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
            }
            selectstmt.close();
            rs.close();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return null;
    }

    private HtmlPage getPage(String url) throws IOException {
        final WebClient webClient = new WebClient(getBrowserVersionFromName(searchEngine.getDefaultBrowser()));//BrowserVersion.FIREFOX_24);
        webClient.getOptions().setJavaScriptEnabled(false);
        return webClient.getPage(url);
    }

    private void search(searchEngineQuery query, final WebClient webClient) {

        try {
            System.out.println(searchEngine.name + " ilk aramaya girdi: " + (new Timestamp(System.currentTimeMillis())).toString());

            String htmlSource;
            HtmlPage searchResultPage = null;
            int numberOfUrlsExtracted;
            searchResultPage = clickSearchButtonMainPage(webClient, searchEngine, query.queryText);
            htmlSource = searchResultPage.asXml();
            
            numberOfUrlsExtracted = ExtractAndInsertSeedUrls(searchResultPage, htmlSource, 1, 0, searchEngine.getName(), query.queryText);

            failIncrementalSleep = 0;
            Random r = new Random();
            int randomSleepTime = r.nextInt(MAXSLEEP);//minimum 200 ms max 2200 ms
            Thread.sleep(randomSleepTime);

            try {
                for (int i = 1; i < query.maxPage; i++) {

                    HtmlAnchor ha = (HtmlAnchor) getNextButtonOrLink(searchResultPage, searchEngine.getNextButtonIdentifier());
                    System.out.println(searchEngine.name + " " + String.valueOf(i) + " sonraki aramaya girdi: " + (new Timestamp(System.currentTimeMillis())).toString());

                    randomSleepTime = r.nextInt(MAXSLEEP);//minimum 200 ms max 2200 ms
                    Thread.sleep(randomSleepTime);

                    searchResultPage = ha.click();
                    htmlSource = searchResultPage.getWebResponse().getContentAsString();
                    numberOfUrlsExtracted = ExtractAndInsertSeedUrls(searchResultPage, htmlSource, i + 1, numberOfUrlsExtracted, searchEngine.getName(), query.queryText);
                   
                    System.out.println(searchEngine.name + " " + String.valueOf(i) + " sonraki arama bitti: " + (new Timestamp(System.currentTimeMillis())).toString());

                }
            } catch (Exception ex) {
                String x = ex.getMessage();
            }
        } catch (Exception ex) {
            String x = ex.getMessage();
            try {
                System.out.println(searchEngine.name + " hataya girdi 60 saniye bekleyecek. " + x);
                Thread.sleep(60000);
            } catch (Exception eex) {
            }
        }
    }

    public Object getNextButtonOrLink(HtmlPage page, final String id) {
        return getAnchorByAnyIdentifyingSingleWord(page, id);
    }

    public HtmlAnchor getAnchorByAnyIdentifyingSingleWord(HtmlPage page, final String id) throws ElementNotFoundException {
        WebAssert.notNull("text", id);

        for (final HtmlAnchor anchor : page.getAnchors()) {

            String anchortext = anchor.asXml();
            System.out.println(anchortext+"\n");
            try {
                anchortext = URLDecoder.decode(anchortext, "ISO-8859-1");
            } catch (Exception ex) {

            }
            if (anchortext.contains(id)) {
                return anchor;
            }
        }
        throw new ElementNotFoundException("a", "<text>", id);
    }

    public List<String> getAnchorLinks(HtmlPage page, final String id) throws ElementNotFoundException {
        WebAssert.notNull("text", id);

        WebAssert.notNull("text", id);
        List<String> urls = new ArrayList<String>();
        for (final HtmlAnchor anchor : page.getAnchors()) {

            String anchortext = anchor.asXml();
            if (anchortext.contains(id)) {

                try {
                    anchortext = URLDecoder.decode(anchortext, "ISO-8859-1");
                    int start = 0;

                    while (start != -1) {
                           start = anchortext.indexOf("href=", start);
                        if (start != -1) {
                            start = anchortext.indexOf("http", start);
                        }
                        int end = anchortext.indexOf("&amp", start + 10);
                        int end2 = anchortext.indexOf('"', start + 10);
                        if(end>end2 && end2!=-1)
                        {
                            end = end2;
                        }
                        if (start != -1) {
                            urls.add(anchortext.substring(start, end));
                            start = end;
                        }
                    }
                } catch (Exception ex) {
                }
            }
        }
        return urls;
    }

    private HtmlPage clickSearchButtonMainPage(final WebClient webClient, SearchEngine se, String query) {

        String url = se.getBaseUrl();
        String queryTextboxName = se.getQueryTextBoxName();
        String searchButtonId = se.getSubmitButtonId();
        String searchButtonName = se.getSubmitButtonName();
        Object tempSubmitFromName, tempSubmitFromId, tempSubmitFromTagName;
        try {

            HtmlPage page1 = webClient.getPage(url);

            try {
                HtmlInput input1 = page1.getElementByName(queryTextboxName);
                input1.setValueAttribute(query);
                tempSubmitFromName = page1.getElementByName(searchButtonName);
                return clickButtonReturnPage(tempSubmitFromName);
            } catch (Exception ex) {
                String aaa = "deneme";
            }
            try {
                HtmlInput input1 = page1.getElementByName(queryTextboxName);
                input1.setValueAttribute(query);
                tempSubmitFromId = page1.getElementById(searchButtonId);
                return clickButtonReturnPage(tempSubmitFromId);
            } catch (Exception ex2) {
                String aaa = "deneme";
            }
            try {
                HtmlInput input1 = page1.getElementByName(queryTextboxName);
                input1.setValueAttribute(query);
                tempSubmitFromTagName = page1.getElementsByTagName("button").get(0);
                return clickButtonReturnPage(tempSubmitFromTagName);
            } catch (Exception ex2) {
                String aaa = "deneme";
            }
        } catch (Exception ex) {

        }
        return null;
    }

    private HtmlPage clickButtonReturnPage(Object tempsubmit) throws IOException {

        try {

            Thread.sleep(failIncrementalSleep);

            HtmlSubmitInput htmlsubmit;
            HtmlButton htmlbutton;
            if (tempsubmit.getClass().getName().contains("HtmlButton")) {
                htmlbutton = (HtmlButton) tempsubmit;
                return htmlbutton.click();
            } else if (tempsubmit.getClass().getName().contains("HtmlSubmitInput")) {
                htmlsubmit = (HtmlSubmitInput) tempsubmit;
                return htmlsubmit.click();
            }
            
        } catch (FailingHttpStatusCodeException fex) {
            if (failIncrementalSleep == 0) {
                failIncrementalSleep = 600000;//5 dakika
            } else {
                failIncrementalSleep += 600000;
            }
            System.out.println(searchEngine.name +" "+ String.valueOf(failIncrementalSleep / 1000 / 60) + " Dakika bekleyecek. Hata Detay: " + fex.getMessage().substring(0, 50));
            //stopRunning();
        } catch (Exception ex) {
            System.out.println(searchEngine.name + " button click hata verdi. Detay: " + ex.getMessage().substring(0, 50));
        }
        return null;

    }

    private void initializeJdbc() {
        try {
            Class.forName("com.mysql.jdbc.Driver");//.newInstance();
            con = DriverManager.getConnection(connectionUrl);
        } catch (Exception ex) {
            String a = ex.getMessage();
        }
    }

    private int returnMinPositiveInteger(int x1, int x2) {
        if (x1 != -1 && x2 != -1) {
            if (x1 > x2) {
                return x2;
            } else {
                return x1;
            }
        } else if (x1 == -1) {
            return x2;
        } else if (x2 == -1) {
            return x1;
        }
        return 0;
    }

    private int ExtractAndInsertSeedUrls(HtmlPage page, String text, int pageNumber, int prevExtractedUrlCount, String searchEngineName, String queryText) {

        int pageContentId = insertPageContent(text, pageNumber, searchEngineName, crawlId, queryText);

        HashSet<String> previousUrls = new HashSet<String>();

        int count = 0;
        int urlStartIndex = 0;

        List<String> links = getAnchorLinks(page, "url");

        for (String url : links) {
            try {
                if (!isExcludedFileType(url, searchEngineName)) {
                    if (getSearchEngineFromName(searchEngineName).getUseUrlRedirection().equals("true") && url.contains("url")) {
                        HtmlPage p = getPage(url);
                        url = p.getUrl().toString();
                        Thread.sleep(200);
                    }
                    if (!includesExcludedKeyword(url, searchEngineName) && !previousUrls.contains(url)) {
                        String encodedurl = "";
                        try {
                            encodedurl = URLDecoder.decode(url, "ISO-8859-1");
                            url = encodedurl;
                        } catch (Exception ex) {

                        }
                        count++;
                        insertSeedLink(url, searchEngineName, queryText, prevExtractedUrlCount + count, pageContentId);
                        previousUrls.add(url);           
                    }
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
        while (urlStartIndex > -1) {
            int indexencoded = text.indexOf("http://", urlStartIndex + 1);
            int indexnotencoded = text.indexOf("http%3a%2f%2f", urlStartIndex + 1);
            urlStartIndex = returnMinPositiveInteger(indexencoded, indexnotencoded);

            int indexOfNextQuote = text.indexOf('"', urlStartIndex + 10);
            int indexOfNextCloseParanthesis = text.indexOf(')', urlStartIndex + 10);

            int urlEndIndex = returnMinPositiveInteger(indexOfNextCloseParanthesis, indexOfNextQuote);

            try {
                String rawurl = text.substring(urlStartIndex, urlEndIndex);
                if (urlStartIndex > -1) {
                    String url;
                    if (indexencoded == urlStartIndex) {
                        url = refineUrlString(rawurl, true);
                    } else {
                        url = refineUrlString(rawurl, false);
                    }

                    if (!isExcludedFileType(url, searchEngineName)) {

                        if (!includesExcludedKeyword(url, searchEngineName) && !previousUrls.contains(url)) {
                            String encodedurl = "";
                            try {
                                encodedurl = URLDecoder.decode(url, "ISO-8859-1");
                                url = encodedurl;
                            } catch (Exception ex) {

                            }
                            count++;
                            insertSeedLink(url, searchEngineName,queryText, prevExtractedUrlCount + count, pageContentId);
                            previousUrls.add(url);           
                        }
                    }
                }
            } catch (Exception ex) {

            }
        }

        return count;

    }

    private int ExtractAndInsertSeedUrlsOld(String text, int pageNumber, int prevExtractedUrlCount, String searchEngineName) {

        int pageContentId = insertPageContent(text, pageNumber, searchEngineName, crawlId, "");

        HashSet<String> previousUrls = new HashSet<String>();

        int count = 0;
        int index = 0;
        while (index > -1) {
            index = text.indexOf("http://", index + 1);
            int temp = text.indexOf('"', index + 10);

            int tempparant = text.indexOf(')', index + 10);

            try {
                String rawurl = text.substring(index, temp);

                if (index > -1) {
                    String url = refineUrlString(rawurl, true);
                    String encodedurl = "";

                    if (!includesExcludedKeyword(url, searchEngineName) && !previousUrls.contains(url)) {
                        count++;                    
                        previousUrls.add(url);                  
                    }
                }
            } catch (Exception ex) {

            }
        }
        index = 0;
        while (index > -1) {
            index = text.indexOf("http%3a%2f%2f", index + 1);

            int temp = text.indexOf('"', index + 10);
            try {
                String rawurl = text.substring(index, temp);
                if (index > -1) {
                    String url = refineUrlString(rawurl, false);

                    boolean excludeMatch = includesExcludedKeyword(url, searchEngineName);
                    if (!excludeMatch && !previousUrls.contains(url)) {
                        String encodedurl = "";
                        try {
                            encodedurl = URLDecoder.decode(url, "ISO-8859-1");
                            url = encodedurl;
                        } catch (Exception ex) {

                        }
                        count++;
                        
                        previousUrls.add(url);
                                  
                    }
                }
            } catch (Exception ex) {

            }
        }

        return count;

    }

    private void insertSeedLink(String url, String searchEngineName, String text, int resultOrder, int pageContentId) {
        try {
            URL u = new URL(url);
            String host = u.getHost();
            if (InternetDomainName.isValid(host) || com.google.common.net.HostSpecifier.isValid(host)) {

                PreparedStatement pstmt
                        = con.prepareStatement("INSERT INTO seedurlraw (url, searchEngine,resultOrder,pageContentId) VALUES (?,?,?,?);");
                pstmt.setString(1, url);
                pstmt.setString(2, searchEngineName);
                pstmt.setString(3, String.valueOf(resultOrder));
                pstmt.setInt(4, pageContentId);

                pstmt.executeUpdate();
                pstmt.close();
                
                DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                        Object[] row = {searchEngineName,text,url,dateFormat.format(new Date())};
                        
            }
        } catch (Exception ex) {
            String a = "";
            String b = "";

        }
    }

    private int insertPageContent(String text, int resultPageNumber, String searchEngineName, int crawlRecordId, String queryText) {
        try {
            PreparedStatement pstmt
                    = con.prepareStatement("INSERT INTO pagecontent (htmlcontent, resultPageNumber, searchEngineName, crawlRecordId,queryText) VALUES (?,?,?,?,?);",
                            Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, text);
            pstmt.setInt(2, resultPageNumber);
            pstmt.setString(3, searchEngineName);
            pstmt.setInt(4, crawlRecordId);
            pstmt.setString(5, queryText);

            pstmt.executeUpdate();

            java.sql.ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            }
            pstmt.close();

        } catch (Exception ex) {
            String a = "";
            String b = "";

        }
        return 0;
    }

    private boolean includesExcludedKeyword(String url, String searchEngineName) {
        String excludeString = getSearchEngineFromName(searchEngineName).getExcludedWords();
        boolean excludeMatch = false;
        if (!excludeString.equals("")) {
            excludeMatch = url.matches(".*(" + excludeString + ").*");
        }
        if (!excludeMatch) {
            excludeMatch = url.matches(".*(.gif|.png|.jpg).*");
        }
        return excludeMatch;
    }

    private boolean isExcludedFileType(String url, String searchEngineName) {
        boolean excludeMatch = false;
        excludeMatch = url.matches(".*(.gif|.png|.jpg).*");
        return excludeMatch;
    }

    private SearchEngine getSearchEngineFromName(String enginename) {
        List<SearchEngine> selist = getSearchEnginesFromXml().getSearchEngines();

        String[] seNames = new String[selist.size()];
        int i = 0;
        for (SearchEngine se : selist) {
            if (se.getName().equals(enginename)) {
                return se;
            }
        }
        return null;
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

    private String refineUrlString(String rawurl, boolean isEncoded) {
        int endIndex = 0;

        if (isEncoded) {
            if (!rawurl.contains("?")) {
                int ampersandSign = rawurl.indexOf("&");
                int percentageSign = rawurl.indexOf("%");

                if (ampersandSign != -1 && percentageSign != -1) {
                    if (ampersandSign > percentageSign) {
                        endIndex = percentageSign;
                    } else if (ampersandSign < percentageSign) {
                        endIndex = ampersandSign;
                    }
                } else if (ampersandSign != -1) {
                    endIndex = ampersandSign;
                } else if (percentageSign != -1) {
                    endIndex = percentageSign;
                }

            } else {
                int percentageSign = rawurl.indexOf("%");
                if (percentageSign != -1) {
                    endIndex = percentageSign;
                }
            }
        } else {
            int slashSign = rawurl.indexOf("/");
            if (slashSign != -1) {
                endIndex = slashSign;
            }
        }
        if (endIndex != 0) {
            return rawurl.substring(0, endIndex);
        } else {
            return rawurl;
        }

    }

    private BrowserVersion getBrowserVersionFromName(String browsername) {
        switch (browsername) {
            case "chrome":
                return BrowserVersion.CHROME;
            case "googlechrome":
                return BrowserVersion.CHROME;
            case "google":
                return BrowserVersion.CHROME;
            case "firefox":
                return BrowserVersion.FIREFOX_45;
            case "mozilla":
                return BrowserVersion.FIREFOX_45;
            case "explorer":
                return BrowserVersion.INTERNET_EXPLORER;
            case "internetexplorer":
                return BrowserVersion.INTERNET_EXPLORER;
            default:
                return BrowserVersion.getDefault();

        }

    }

}
