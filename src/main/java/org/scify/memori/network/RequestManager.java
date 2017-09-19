package org.scify.memori.network;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.scify.memori.helper.MemoriConfiguration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import static org.apache.http.protocol.HTTP.USER_AGENT;

public class RequestManager {

    private static String SERVER_URL;
    public static int MAX_REQUEST_TRIES = 15;
    public RequestManager() {
        MemoriConfiguration configuration = new MemoriConfiguration();
        SERVER_URL = configuration.getProjectProperty("SERVER_URL");
    }

    public String doPost(String urlEndpoint, List<NameValuePair> params) {
        urlEndpoint = SERVER_URL + urlEndpoint;
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(urlEndpoint);
        // add header
        post.setHeader("User-Agent", USER_AGENT);
        List<NameValuePair> urlParameters = new ArrayList<>();
        for(NameValuePair param: params) {
            urlParameters.add(new BasicNameValuePair(param.getName(), param.getValue()));
        }
        try {
            post.setEntity(new UrlEncodedFormEntity(urlParameters));

            HttpResponse response = client.execute(post);
//            System.out.println("\nSending 'POST' request to URL : " + urlEndpoint);
//            System.out.println("Response Code : " +
//                    response.getStatusLine().getStatusCode());

            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));
            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            return result.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String doGet(String urlEndpoint) {
        urlEndpoint = SERVER_URL + urlEndpoint;
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

            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            return result.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean networkAvailable() {
        try {
            MemoriConfiguration configuration = new MemoriConfiguration();
            final URL url = new URL(configuration.getProjectProperty("SERVER_URL"));
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
