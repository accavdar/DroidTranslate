package net.cavdar.android.droidtranslate.domain;

/**
 * User: accavdar
 * Date: 08/01/14
 */

public enum Languages {

    TR("tr"),
    EN("en");

    private String language;

    private Languages(String language) {
        this.language = language;
    }

    public String getLanguage() {
        return this.language;
    }
}
