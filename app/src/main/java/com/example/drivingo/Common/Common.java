package com.example.drivingo.Common;

import android.content.Context;
import android.util.Log;

import com.example.drivingo.Paytm.Transaction;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONObject;

public class Common {
    public interface onJSONResponse {
        void onResponse(JSONObject jsonObject);
    }

    public static void generateCheckSum(Context context, String orderID, String amount, final onJSONResponse listener) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("MID", Transaction.MID);
            jsonObject.put("ORDER_ID",orderID);
            jsonObject.put("CUST_ID", FirebaseAuth.getInstance().getCurrentUser().getUid());
            jsonObject.put("INDUSTRY_TYPE_ID",Transaction.INDUSTRY_TYPE_ID);
            jsonObject.put("CHANNEL_ID",Transaction.CHANNEL_ID);
            jsonObject.put("TXN_AMOUNT",amount);
            jsonObject.put("WEBSITE",Transaction.WEBSITE);
            jsonObject.put("CALLBACK_URL",Transaction.verifyURL);
            Server.fetchData(context, Server.URL + "/generateChecksum.php", jsonObject, new Server.OnResponseListener() {
                @Override
                public void onJSONResponse(JSONObject response) {
                    Log.e("response",response.toString());
                    listener.onResponse(response);
                }

                @Override
                public void onJSONErrorResponse() {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
