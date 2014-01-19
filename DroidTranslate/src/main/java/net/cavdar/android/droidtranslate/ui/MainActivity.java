package net.cavdar.android.droidtranslate.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import net.cavdar.android.droidtranslate.R;
import net.cavdar.android.droidtranslate.domain.Languages;
import net.cavdar.android.droidtranslate.helper.FileUtil;
import net.cavdar.android.droidtranslate.translate.TranslateHandler;
import net.cavdar.android.droidtranslate.translate.TranslateHelper;
import net.cavdar.android.droidtranslate.translate.TranslateTask;

/**
 * User: accavdar
 * Date: 19/12/13
 */

public class MainActivity extends Activity implements TranslateHandler {

    private static final String TAG = MainActivity.class.getSimpleName();

    private final String PHOTO_TAKEN = "photo_taken";

    protected Button button;

    protected EditText ocred;

    protected EditText translated;

    protected boolean isPhotoTaken = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        ocred = (EditText) findViewById(R.id.ocred);
        translated = (EditText) findViewById(R.id.translated);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(PHOTO_TAKEN, isPhotoTaken);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.i(TAG, "onRestoreInstanceState()");
        if (savedInstanceState.getBoolean(PHOTO_TAKEN)) {
            onPhotoTaken();
        }
    }

    @Override
    public void handleTranslation(String translatedText) {
        translated.setText(translatedText);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    protected void startCameraActivity() {
        final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, FileUtil.getOutputFileUri());

        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "resultCode: " + resultCode);

        if (resultCode == -1) {
            onPhotoTaken();
        } else {
            Log.v(TAG, "User cancelled");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_photo:
                startCameraActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void onPhotoTaken() {
        isPhotoTaken = true;
        String text = FileUtil.processBitmap(FileUtil.createBitmap());

        if (text.length() != 0) {
            ocred.setText(text);
            String url = TranslateHelper.createUrl(Languages.EN, Languages.TR, text);
            new TranslateTask(this, this).execute(url);
        } else {
            ocred.setText("Error: Text recognition failed!");
            translated.setText("Hata: Karakter tanıması başarısız!");
        }
    }
}
