package org.scify.memori.helper.analytics;

import org.asynchttpclient.*;
import org.scify.memori.helper.MemoriConfiguration;
import org.scify.memori.interfaces.AnalyticsLogger;

import java.util.Map;

public class MemoriStudioAnalyticsLogger implements AnalyticsLogger {

    private static String SERVER_URL;
    AsyncHttpClient client;

    public MemoriStudioAnalyticsLogger() {
        SERVER_URL = MemoriConfiguration.getInstance().getDataPackProperty("SERVER_URL") + "analytics/store";
        client = Dsl.asyncHttpClient();
    }

    @Override
    public void logEvent(Map<String, String> payload) {
        if (SERVER_URL != null)
            executePOSTCall(payload);
    }

    protected void executePOSTCall(Map<String, String> payload) {
        BoundRequestBuilder request = client.preparePost(SERVER_URL);
        for (Map.Entry<String, String> entry : payload.entrySet()) {
            request.addQueryParam(entry.getKey(), entry.getValue());
        }
        request.addHeader("Content-Type", "application/json;charset=UTF-8")
                .addHeader("Accept", "application/json")
                .execute(new AsyncCompletionHandler<>() {
                    @Override
                    public Object onCompleted(Response response) {
                        return response;
                    }
                });
    }
}
