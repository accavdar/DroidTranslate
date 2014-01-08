package net.cavdar.android.droidtranslate.translate;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;

import net.cavdar.android.droidtranslate.helper.JsonUtil;
import net.cavdar.android.droidtranslate.ui.MainActivity;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;

import java.io.IOException;

/**
 * User: accavdar
 * Date: 08/01/14
 */
public class TranslateTask extends AsyncTask<String, Void, String> {

    private static final String TAG = TranslateTask.class.getSimpleName();

    private TranslateHandler translateHandler;

    private Context context;

    private ProgressDialog progressDialog;

    public TranslateTask(Context context, TranslateHandler translateHandler) {
        this.context = context;
        this.translateHandler = translateHandler;
    }

    @Override
    protected void onPreExecute() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Translating");
        progressDialog.setMessage("Please wait. Translating...");
        progressDialog.setIndeterminate(true);
        progressDialog.show();
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            AndroidHttpClient client = AndroidHttpClient.newInstance("droidtranslate");
            HttpGet request = new HttpGet(params[0]);
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String serverResponse = client.execute(request, responseHandler);
            return JsonUtil.getTranslatedTextFromJson(serverResponse);
        } catch (ClientProtocolException e) {
            Log.e(TAG, e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

        return null;
    }

    @Override
    protected void onPostExecute(String translatedText) {
        translateHandler.handleTranslation(translatedText);
        progressDialog.dismiss();
    }
}
