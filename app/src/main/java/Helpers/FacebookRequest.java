package Helpers;

import android.os.AsyncTask;

/**
 * Created by Sergejs on 12/10/2017.
 */

public class FacebookRequest extends AsyncTask {

    @Override
    protected Object doInBackground(Object[] params) {
        return null;
    }

    // you may separate this or combined to caller class.
    public interface AsyncResponse {
        void processFinish(String output);
    }

    public AsyncResponse delegate = null;

    public FacebookRequest(AsyncResponse delegate){
        this.delegate = delegate;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
    }

}
