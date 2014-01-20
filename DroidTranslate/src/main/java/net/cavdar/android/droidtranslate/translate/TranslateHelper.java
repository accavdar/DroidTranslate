package net.cavdar.android.droidtranslate.translate;

import net.cavdar.android.droidtranslate.domain.Languages;

/**
 * User: accavdar
 * Date: 08/01/14
 */

public class TranslateHelper {

    private static final String API_KEY = " [PUT YOUR API KEY HERE] ";

    private static final String URL_PATH = "https://www.googleapis.com/language/translate/v2";

    private TranslateHelper() {
    }

    public static String createUrl(Languages source, Languages target, String text) {
        StringBuilder builder = new StringBuilder();
        builder.append(URL_PATH);
        builder.append("?key=");
        builder.append(API_KEY);
        builder.append("&source=");
        builder.append(source.getLanguage());
        builder.append("&target=");
        builder.append(target.getLanguage());
        builder.append("&q=");
        builder.append(text.replaceAll(" ", "%20"));

        return builder.toString();
    }
}
