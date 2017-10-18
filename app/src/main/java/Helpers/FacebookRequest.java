package Helpers;

import android.os.Bundle;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;


/**
 * Created by Sergejs on 12/10/2017.
 */

public class FacebookRequest {
    private static JSONObject object;

    private FacebookRequest(JSONObject object) {
        this.object = object;
    }

    private static void set_GraphApiRequest(String path, AccessToken token){
        GraphRequest request = GraphRequest.newGraphPathRequest(
                token,path,
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        object=response.getJSONObject();
                    }
                });
        request.executeAndWait();
    }


    public static JSONObject getGraphApi(String path, AccessToken token) {
        set_GraphApiRequest(path, token);
        return object;
    }
}
