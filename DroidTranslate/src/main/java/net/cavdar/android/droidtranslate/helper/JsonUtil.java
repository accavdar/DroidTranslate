package net.cavdar.android.droidtranslate.helper;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * User: accavdar
 * Date: 08/01/14
 */

public class JsonUtil {

    private static final String TAG = JsonUtil.class.getSimpleName();

    private static String DATA = "data";

    private static String TRANSLATIONS = "translations";

    private static String TRANSLATED_TEXT = "translatedText";

    private JsonUtil() {
    }

    public static String getTranslatedTextFromJson(String jsonString) {
        try {
            JSONObject root = new JSONObject(jsonString);
            JSONObject data = root.getJSONObject(DATA);
            JSONArray translations = data.getJSONArray(TRANSLATIONS);
            JSONObject translatedText = translations.getJSONObject(0);
            return translatedText.getString(TRANSLATED_TEXT);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }

        return null;
    }
}
