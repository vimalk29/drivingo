package com.example.drivingo.Common;

import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class Server {
    public static final int SOCKET_TIMEOUT_MS = 30000;
    public static final String URL ="https://bus-tracking.000webhostapp.com/Drivingo";


    public static void fetchData(Context context, String URL, final JSONObject jsonObject, final OnResponseListener listener ){
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        JsonRequest jsonRequest= new JsonRequest(Request.Method.POST, URL,jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                listener.onJSONResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.onJSONErrorResponse();
            }
        })
        {
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }
        };
        jsonRequest.setRetryPolicy(new DefaultRetryPolicy(SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(jsonRequest);
    }

    public interface OnResponseListener{
        void onJSONResponse(JSONObject response);
        void onJSONErrorResponse();
    }
    public static class JsonRequest extends JsonObjectRequest {

        public JsonRequest(int method, String url, JSONObject requestBody, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
            super(method, url, requestBody, listener, errorListener);
        }
    }
}
