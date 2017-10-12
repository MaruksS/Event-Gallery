package Helpers;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;

import org.json.JSONObject;

/**
 * Created by Sergejs on 12/10/2017.
 */

public class FacebookRequest {
    private static JSONObject object;

    private FacebookRequest(JSONObject object) {
        this.object = object;
    }

    private static JSONObject GraphApiRequest(String path, AccessToken token){
            new GraphRequest(
                    token,
                    path,
                    null,
                    HttpMethod.GET,
                    new GraphRequest.Callback() {
                        public void onCompleted(GraphResponse response) {
                            object = response.getJSONObject();
                        }
                    }
            ).executeAsync();
        return object;
    }

    public static JSONObject getGraphApi(String path, AccessToken token){
        return GraphApiRequest(path, token);
    }

}
