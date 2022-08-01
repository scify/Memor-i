package org.scify.memori.helper.analytics;

import org.scify.memori.helper.MemoriConfiguration;
import org.scify.memori.interfaces.AnalyticsLogger;

import java.util.HashMap;
import java.util.Map;

import static org.scify.memori.helper.StringUtils.MapToJSON;

public class AnalyticsManager {

    protected AnalyticsLogger analyticsLogger;
    private static AnalyticsManager instance;

    private AnalyticsManager(AnalyticsLogger analyticsLogger) {
        this.analyticsLogger = analyticsLogger;
    }

    public static AnalyticsManager getInstance() {
        if (instance == null) {
            instance = new AnalyticsManager(new MemoriStudioAnalyticsLogger());
        }
        return instance;
    }

    public void logEvent(String eventName) {
        Map<String, String> payload = new HashMap<>();
        this.analyticsLogger.logEvent(getParams(payload, eventName));
    }

    public void logEvent(String eventName, Map<String, String> payload) {
        this.analyticsLogger.logEvent(getParams(payload, eventName));
    }

    private Map<String, String> getParams(Map<String, String> params, String eventName) {
        MemoriConfiguration configuration = MemoriConfiguration.getInstance();
        Map<String, String> newParams = new HashMap<>();
        if (params.containsKey("game_name"))
            newParams.put("game_name", params.get("game_name"));
        newParams.put("name", eventName);
        newParams.put("source", "Memor-i Desktop " + configuration.getDataPackProperty("APP_VERSION"));
        params.put("JAVA_VERSION", System.getProperty("java.version"));
        params.put("lang", configuration.getDataPackProperty("APP_LANG"));
        params.put("input_method", configuration.getPropertyByName("INPUT_METHOD"));
        params.put("APP_VERSION", configuration.getDataPackProperty("APP_VERSION"));
        String authToken = configuration.getPropertyByName("AUTH_TOKEN");
        if (authToken != null)
            params.put("AUTH_TOKEN", authToken);
        newParams.put("payload", MapToJSON(params));
        return newParams;
    }
}
