package net.cavdar.android.droidtranslate;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * User: accavdar
 * Date: 19/12/13
 */
public class SimpleOcrActivity extends Activity {

    private final int BUFFER = 1024;

    private final int SAMPLE_SIZE = 4;

    private final String FILE_SEPARATOR = "/";

    private final String APP_NAME = "DroidTranslate";

    private final String DATA_DIR_NAME = "tessdata";

    private final String EXTERNAL_DIR = Environment.getExternalStorageDirectory().toString() + FILE_SEPARATOR;

    private final String APP_DIR = EXTERNAL_DIR + APP_NAME;

    private final String DATA_DIR = APP_DIR + FILE_SEPARATOR + DATA_DIR_NAME;

    private final String LANG = "eng";

    private final String LANG_FILE_NAME = LANG + ".traineddata";

    private final String PHOTO_TAKEN = "photo_taken";

    protected static final String TAG = SimpleOcrActivity.class.getSimpleName();

    protected String IMAGE_FILE_NAME = APP_DIR + FILE_SEPARATOR + "ocr.jpg";

    protected Button button;

    protected EditText field;

    protected boolean isPhotoTaken = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        createDirectories();
        copyLanguageFile();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        field = (EditText) findViewById(R.id.field);
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new ButtonClickHandler());
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

    public class ButtonClickHandler implements View.OnClickListener {
        public void onClick(View view) {
            Log.v(TAG, "Starting Camera app");
            startCameraActivity();
        }
    }

    protected void startCameraActivity() {
        File file = new File(IMAGE_FILE_NAME);
        Uri outputFileUri = Uri.fromFile(file);

        final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

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

    protected void onPhotoTaken() {
        isPhotoTaken = true;

        Bitmap bitmap = createBitmap();
        String text = processBitmap(bitmap);

        if (text.length() != 0) {
            field.setText(field.getText().toString().length() == 0 ? text : field.getText() + " " + text);
            field.setSelection(field.getText().toString().length());
        }
    }

    private String processBitmap(Bitmap bitmap) {
        TessBaseAPI baseApi = new TessBaseAPI();
        baseApi.setDebug(true);
        baseApi.init(APP_DIR, LANG);
        baseApi.setImage(bitmap);

        String recognizedText = baseApi.getUTF8Text();
        baseApi.end();

        Log.v(TAG, "OCRed Text: " + recognizedText);

        if (LANG.equalsIgnoreCase("eng") ) {
            recognizedText = recognizedText.replaceAll("[^a-zA-Z0-9]+", " ");
        }

        return recognizedText.trim();
    }

    private Bitmap createBitmap() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = SAMPLE_SIZE;

        Bitmap bitmap;
        try {
            bitmap = BitmapFactory.decodeFile(IMAGE_FILE_NAME, options);

            ExifInterface exif = new ExifInterface(IMAGE_FILE_NAME);

            int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            Log.v(TAG, "Orient: " + exifOrientation);

            int rotate = 0;
            switch (exifOrientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
            }

            Log.v(TAG, "Rotation: " + rotate);

            if (rotate != 0) {
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();

                // set pre rotate
                Matrix matrix = new Matrix();
                matrix.preRotate(rotate);

                // rotate bitmap
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
            }

            // Convert to ARGB_8888, required by tess
            return bitmap.copy(Bitmap.Config.ARGB_8888, true);

        } catch (IOException e) {
            Log.e(TAG, "Couldn't correct orientation: " + e.toString());
        }

        return null;
    }

    private void createDirectories() {
        String[] paths = new String[]{APP_DIR, DATA_DIR};
        for (String path : paths) {
            File dir = new File(path);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.v(TAG, "ERROR: Creation of directory " + path + " on sd card failed");
                } else {
                    Log.v(TAG, "Created directory " + path + " on sd card");
                }
            }
        }
    }

    private void copyLanguageFile() {
        if (!(new File(DATA_DIR + FILE_SEPARATOR + LANG_FILE_NAME)).exists()) {
            InputStream in = null;
            OutputStream out = null;
            try {
                AssetManager assetManager = getAssets();

                in = assetManager.open(DATA_DIR_NAME + FILE_SEPARATOR + LANG_FILE_NAME);
                out = new FileOutputStream(DATA_DIR + FILE_SEPARATOR + LANG_FILE_NAME);

                byte[] buf = new byte[BUFFER];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }

                Log.v(TAG, "Copied " + LANG_FILE_NAME);
            } catch (IOException e) {
                Log.e(TAG, "Was unable to copy " + LANG_FILE_NAME + " " + e.toString());
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        Log.v(TAG, e.toString());
                    }
                }

                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        Log.v(TAG, e.toString());
                    }
                }
            }
        }
    }
}
