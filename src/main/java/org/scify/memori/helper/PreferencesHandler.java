package org.scify.memori.helper;

import java.util.prefs.Preferences;

public class PreferencesHandler {

    private Preferences prefs;

    public PreferencesHandler() {
        prefs = Preferences.userNodeForPackage(this.getClass());
    }

    public void setPreference(String preferenceName, String preferenceValue) {
        prefs.put(preferenceName, preferenceValue);
    }

    public Object getPreferenceByName(String preferenceName) {
        return prefs.get(preferenceName, null);
    }
}
