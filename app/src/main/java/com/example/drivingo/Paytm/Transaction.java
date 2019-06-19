package com.example.drivingo.Paytm;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.drivingo.Common.Common;
import com.example.drivingo.R;
import com.google.firebase.auth.FirebaseAuth;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;

import org.json.JSONObject;

import java.util.HashMap;

public class Transaction extends AppCompatActivity implements PaytmPaymentTransactionCallback{
    public static final String MID = "AlXzbd56892104822313";
    public static final String INDUSTRY_TYPE_ID = "Retail";
    public static final String CHANNEL_ID = "WAP";
    public static final String WEBSITE = "WEBSTAGING";
    public static final String verifyURL = "https://pguat.paytm.com/paytmchecksum/paytmCallback.jsp";
    String orderId,amount,custid;
    FirebaseAuth firebaseAuth;
    int status=0;//-1=>pending, 0=>fail, 1=>success
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        firebaseAuth = FirebaseAuth.getInstance();
        Bundle bundle = getIntent().getExtras();
        if(bundle!=null){
            orderId = bundle.getString("orderId");
            amount = bundle.getString("amount");
            if(firebaseAuth.getCurrentUser()!=null)
                custid =firebaseAuth.getCurrentUser().getUid();
            else{
                result();
            }
            Common.generateCheckSum(this, orderId, amount, new Common.onJSONResponse() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    try {
                        makeTransaction(jsonObject.getString("CHECKSUMHASH"));
                    }catch (Exception e){
                        e.printStackTrace();
                        result();
                    }
                }
            });
        }
    }

    private void makeTransaction(String checksumhash) {
        PaytmPGService Service = PaytmPGService.getStagingService();
        HashMap<String, String> paramMap = new HashMap<>();
        paramMap.put("MID", MID);
        paramMap.put("ORDER_ID", orderId);
        paramMap.put("CUST_ID", custid);
        paramMap.put("CHANNEL_ID", CHANNEL_ID);
        paramMap.put("TXN_AMOUNT", amount);
        paramMap.put("WEBSITE", WEBSITE);
        paramMap.put("CALLBACK_URL" ,verifyURL);
        paramMap.put("CHECKSUMHASH" ,checksumhash);
        paramMap.put("INDUSTRY_TYPE_ID", INDUSTRY_TYPE_ID);
        PaytmOrder Order = new PaytmOrder(paramMap);
        Log.e("checksum ", "param "+ paramMap.toString());
        Service.initialize(Order,null);
        // start payment service call here
        Service.startPaymentTransaction(this, true, true,
                this  );
    }
    @Override
    public void onTransactionResponse(Bundle bundle) {
        Log.e("checksum ", " respon true " + bundle.toString());
        String str = bundle.getString("STATUS");
        if(str!=null){
            if(str.equals("TXN_SUCCESS"))
                status=1;
            else if(str.equals("PENDING"))
                status=-1;
            else
                status=0;
        }else
            status=0;
        result();
    }
    @Override
    public void networkNotAvailable() {
        result();
    }
    @Override
    public void clientAuthenticationFailed(String s) {
        result();
    }
    @Override
    public void someUIErrorOccurred(String s) {
        Log.e("checksum ", " ui fail respon  "+ s );
        result();
    }
    @Override
    public void onErrorLoadingWebPage(int i, String s, String s1) {
        Log.e("checksum ", " error loading pagerespon true "+ s + "  s1 " + s1);
        result();
    }
    @Override
    public void onBackPressedCancelTransaction() {
        Log.e("checksum ", " cancel call back respon  " );
        result();
    }
    @Override
    public void onTransactionCancel(String s, Bundle bundle) {
        Log.e("checksum ", "  transaction cancel " );
        result();
    }

    private void result(){
        Intent data = new Intent();
        data.putExtra("status",status);
        setResult(RESULT_OK,data);
        finish();
    }
}
