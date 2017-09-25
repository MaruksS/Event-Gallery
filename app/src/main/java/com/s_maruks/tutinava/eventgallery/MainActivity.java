package com.s_maruks.tutinava.eventgallery;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import static android.R.attr.name;
import static android.view.View.Y;

public class MainActivity extends AppCompatActivity {
    JSONObject events;
    TextView tw;
    ImageView iw;
    String name="nothing";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tw = (TextView) findViewById(R.id.tw_text);
        iw = (ImageView) findViewById(R.id.iw_image);

        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/499224723762695",
                // "/357357958049311",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        events= response.getJSONObject();
                        get();
                    }
                }
        ).executeAsync();

    }
    public void get(){
            try {
                name= events.getString("name");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        tw.setText(name);
    }
}
