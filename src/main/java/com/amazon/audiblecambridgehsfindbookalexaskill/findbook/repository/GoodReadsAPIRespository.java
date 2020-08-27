package com.amazon.audiblecambridgehsfindbookalexaskill.findbook.repository;

import com.amazon.audiblecambridgehsfindbookalexaskill.findbook.handlers.Book;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterResult;
import com.amazonaws.services.simplesystemsmanagement.model.Parameter;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.json.XML;
import sun.net.www.protocol.http.HttpURLConnection;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class GoodReadsAPIRespository {

    private static AWSSimpleSystemsManagement systemsManagerClient = AWSSimpleSystemsManagementClientBuilder.defaultClient();;

    public GoodReadsAPIRespository() {
        //systemsManagerClient = AWSSimpleSystemsManagementClientBuilder.defaultClient();
    }

    /**
     * @return the URL to use for the GoodReads API request
     */
    private static String getGoodReadsURL(String title) {

        // Setup to get Good Reads key
        GetParameterRequest keyRequest = new GetParameterRequest();
        keyRequest.withName("GOOD_READS_KEY");

        // Setup to get Good reads token
        GetParameterRequest tokenRequest = new GetParameterRequest();
        tokenRequest.withName("GOOD_READS_TOKEN");

        // Finally call Systems Manager to get the tokens.
        String key = systemsManagerClient.getParameter(keyRequest).getParameter().getValue();
//        String token = systemsManagerClient.getParameter(tokenRequest).getParameter().getValue();

        String url = null;
        try {
            url = "https://www.goodreads.com/search.xml?key=" + key + "&q=" + URLEncoder.encode(title, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
//            log(input, e.toString()); // Log exception here
            System.out.println(key + e.toString()); // temporary logging, input may not be key
        }
        return url;
    }

    /**
     * @param searchKey GoodReadsURL generated in the above method
     * @return an HTTP request for the GoodReads API
     * @throws IOException TODO see if the throw part works
     */
    private static HttpsURLConnection buildHttpGetRequest(String searchKey) throws IOException {
        // Is this url the right one to add????
        String urlString= searchKey; // TODO add url you build with query here (from the getGoodReadsURL function)
        URL url = new URL(urlString);
//        log.info("url query: " + url.getQuery()); // TODO: change to your logging method. Used for debugging purposes.
        System.out.println("url query:  " + url.getQuery()); // temporary logging
        System.out.println(urlString); // testing

        HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
        conn.setRequestMethod("GET");
        return conn;
    }

    /**
     * @param conn is the HttpURLConnection output of the function above
     * @return is the result of the request if it goes through properly
     * @throws IOException if the request fails
     */
    private static InputStream executeHttpRequest(HttpsURLConnection conn) throws IOException {
        if(conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed: HTTP error code: " + conn.getResponseCode());
        }
        return conn.getInputStream();
    }

    /**
     * @param inputStream is the output from the HTTP request from the function above
     * @return a Book object containing the author and title of the book, parsed from inputStream
     */
    private static Book parseHttpResponse(final InputStream inputStream) {
        Book myBook = new Book();
        try {
            // Convert InputStream object to JSON object
            JSONObject responseJson = XML.toJSONObject(IOUtils.toString(inputStream, StandardCharsets.UTF_8.toString()));

            // Gets through a lot of the JSON stuff all at once
            JSONObject bookInfo = responseJson.getJSONObject("GoodreadsResponse").getJSONObject("search")
                    .getJSONObject("results").getJSONArray("work").getJSONObject(0).getJSONObject("best_book");

            // Parse responseJson and extract data you need
            String title = bookInfo.getString("title");
            String author = bookInfo.getJSONObject("author").getString("name");

            // Load the title and author into the myBook Book object for returning
            myBook.setTitle(title);
            myBook.setAuthor(author);

        } catch (Exception e) {
//            log.error("Error in readHttpResponse ", e); // TODO change your logging method
            System.out.println("Error in readHttpResponse " + e); // temporary logging
        }
        return myBook; // return the author name and title in the Book object
    }

    /**
     * @param title is the title of the book passed as a string
     * @return a Book object containing the title and author of the input book
     * @throws IOException - TODO see if this is the best way to do things, I went with it because Intellij suggested it
     */
    // TODO make sure the title input is used correctly, 99% sure it isn't right now
    public static Book findBook(String title) throws IOException {
        String url = getGoodReadsURL(title); // problems here
        HttpsURLConnection conn = buildHttpGetRequest(url); // try catch?
        InputStream inputStream = executeHttpRequest(conn); // try catch?
        return parseHttpResponse(inputStream);
    }

}
