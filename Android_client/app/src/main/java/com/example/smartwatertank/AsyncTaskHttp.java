package com.example.automaticwatertank;


import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class AsyncTaskHttp extends AsyncTask<String, Void, String> {

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        //We need to convert the string in result to a JSONObject
        if (result != null) {
            try {

                JSONObject jsonObject = new JSONObject(result);


                String username = jsonObject.getString("username");

                Log.d("OnPostExecute. User  " , username);
            }
            catch(JSONException e){
                e.printStackTrace();
            }
        }
        else {
            Log.d("OnPostExecute  ", "Result is empty");
        }



    }

    @Override
    protected String doInBackground(String... params) {
        return URLConnHelper.sendLoginInfoToServer(params[0]);
    }
}