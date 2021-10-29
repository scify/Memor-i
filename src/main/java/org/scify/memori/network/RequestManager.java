package org.scify.memori.network;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.scify.memori.helper.MemoriConfiguration;
import org.scify.memori.helper.MemoriLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static org.apache.http.protocol.HTTP.USER_AGENT;

public class RequestManager {

    public static int MAX_REQUEST_TRIES = 40;

    public String doPost(String urlEndpoint, List<NameValuePair> params) {
        List<NameValuePair> postParams = new ArrayList<>();
        for (NameValuePair param : params) {
            postParams.add(new BasicNameValuePair(param.getName(), param.getValue()));
        }
        HttpPost post = preparePostObject(urlEndpoint);
        try {
            post.setEntity(new UrlEncodedFormEntity(postParams));
            return performPOSTCall(post);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String doPost(String urlEndpoint, String json) {
        HttpPost post = preparePostObject(urlEndpoint);
        post.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
        return performPOSTCall(post);
    }

    private HttpPost preparePostObject(String urlEndpoint) {
        MemoriConfiguration configuration = MemoriConfiguration.getInstance();
        if (!urlEndpoint.startsWith("http"))
            urlEndpoint = configuration.getDataPackProperty("SERVER_URL") + urlEndpoint;

        HttpPost post = new HttpPost(urlEndpoint);
        post.setHeader("User-Agent", USER_AGENT);
        return post;
    }

    private String performPOSTCall(HttpPost post) {
        CloseableHttpClient client = HttpClients.createDefault();
        try {
            HttpResponse response = client.execute(post);
            MemoriLogger.LOGGER.log(Level.INFO, "\nSending 'POST' request to URL : " + post.getURI());
            MemoriLogger.LOGGER.log(Level.INFO, "Response Code : " +
                    response.getStatusLine().getStatusCode());

            return getString(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getString(HttpResponse response) throws IOException {
        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        return result.toString();
    }

    public String doGet(String urlEndpoint) {
        MemoriConfiguration configuration = MemoriConfiguration.getInstance();
        if (!urlEndpoint.startsWith("http"))
            urlEndpoint = configuration.getDataPackProperty("SERVER_URL") + urlEndpoint;
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(urlEndpoint);

        // add request header
        request.addHeader("User-Agent", USER_AGENT);

        HttpResponse response = null;
        try {
            response = client.execute(request);
            System.out.println("\nSending 'GET' request to URL : " + urlEndpoint);
            System.out.println("Response Code : " +
                    response.getStatusLine().getStatusCode());

            return getString(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean networkAvailable() {
        try {
            MemoriConfiguration configuration = MemoriConfiguration.getInstance();
            final URL url = new URL(configuration.getDataPackProperty("SERVER_URL"));
            final URLConnection conn = url.openConnection();
            conn.connect();
            return true;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            return false;
        }
    }
}
