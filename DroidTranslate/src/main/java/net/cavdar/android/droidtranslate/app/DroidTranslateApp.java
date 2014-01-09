package net.cavdar.android.droidtranslate.app;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;

import net.cavdar.android.droidtranslate.helper.FileUtil;

/**
 * User: accavdar
 * Date: 09/01/14
 */

public class DroidTranslateApp extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();

        this.context = this;

        // create dirs
        FileUtil.createDirectories();

        // copy language file
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                FileUtil.copyLanguageFile();
                return null;
            }
        }.execute();
    }

    public static Context getContext() {
        return context;
    }
}
