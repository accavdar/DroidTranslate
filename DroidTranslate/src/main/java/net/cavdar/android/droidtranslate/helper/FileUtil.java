package net.cavdar.android.droidtranslate.helper;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

import net.cavdar.android.droidtranslate.app.DroidTranslateApp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * User: accavdar
 * Date: 09/01/14
 */

public class FileUtil {

    private static final String TAG = FileUtil.class.getSimpleName();

    public static final int BUFFER = 1024;

    public static int SAMPLE_SIZE = 4;

    public static final String FILE_SEPARATOR = "/";

    public static final String APP_NAME = "DroidTranslate";

    public static final String DATA_DIR_NAME = "tessdata";

    public static final String EXTERNAL_DIR = Environment.getExternalStorageDirectory().toString() + FILE_SEPARATOR;

    public static final String APP_DIR = EXTERNAL_DIR + APP_NAME;

    public static final String DATA_DIR = APP_DIR + FILE_SEPARATOR + DATA_DIR_NAME;

    public static final String LANG = "eng";

    public static final String LANG_FILE_NAME = LANG + ".traineddata";

    public static final String IMAGE_FILE_NAME = APP_DIR + FILE_SEPARATOR + "ocr.jpg";

    private FileUtil() {

    }

    public static void createDirectories() {
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

    public static void copyLanguageFile() {
        if (!(new File(DATA_DIR + FILE_SEPARATOR + LANG_FILE_NAME)).exists()) {
            InputStream in = null;
            OutputStream out = null;
            try {
                AssetManager assetManager = DroidTranslateApp.getContext().getAssets();

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

    public static Bitmap createBitmap() {
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

    public static String processBitmap(Bitmap bitmap) {
        TessBaseAPI baseApi = new TessBaseAPI();
        baseApi.setDebug(true);
        baseApi.init(APP_DIR, LANG);
        baseApi.setImage(bitmap);

        String recognizedText = baseApi.getUTF8Text();
        baseApi.end();

        Log.v(TAG, "OCRed Text: " + recognizedText);

        if (LANG.equalsIgnoreCase("eng")) {
            recognizedText = recognizedText.replaceAll("[^a-zA-Z0-9]+", " ");
        }

        return recognizedText.trim();
    }

    public static Uri getOutputFileUri() {
        File file = new File(IMAGE_FILE_NAME);
        return Uri.fromFile(file);
    }
}
