package com.s_maruks.tutinava.eventgallery;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;


public class InviteActivity extends AppCompatActivity {

    private String event_id;
    private String event_name;

    private final int WHITE = 0xFFFFFFFF;
    private final int BLACK = 0xFF000000;
    private final int WIDTH = 400;
    private final int HEIGHT = 400;

    private TextView tw_name;
    private ImageView iw_qr_code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            event_id = extras.getString("event_id");
            event_name = extras.getString("event_name");
        }

        iw_qr_code = (ImageView) findViewById(R.id.iw_qr);
        tw_name = (TextView)findViewById(R.id.txt_event_name);
        tw_name.setText(event_name);
        try {
            Bitmap bitmap = encodeAsBitmap(event_id);
            iw_qr_code.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    Bitmap encodeAsBitmap(String str) throws WriterException {
        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(str,
                    BarcodeFormat.QR_CODE, WIDTH, HEIGHT, null);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }

        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
        return bitmap;
    }
}
