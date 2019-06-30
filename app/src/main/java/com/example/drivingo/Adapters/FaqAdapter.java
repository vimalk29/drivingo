package com.example.drivingo.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.drivingo.R;
import com.example.drivingo.model.FaqPojo;

import java.util.ArrayList;


public class FaqAdapter extends ArrayAdapter<FaqPojo> {

    private Context context;
    private int resource;
    private ArrayList<FaqPojo> arrayList;

    public FaqAdapter(Context context, int resource, ArrayList<FaqPojo> arrayList) {
        super(context, resource, arrayList);
        this.context=context;
        this.resource = resource;
        this.arrayList = arrayList;
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null){
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(resource, null, false);
        }

        TextView questionText = view.findViewById(R.id.faqQuestionText);
        final TextView answerText = view.findViewById(R.id.faqAnswerText);
        ImageView toggleAnswerVisibility = view.findViewById(R.id.toggleAnswerVisibility);

        FaqPojo faqPojo = arrayList.get(position);
        questionText.setText(faqPojo.getQuestion());
        answerText.setText(faqPojo.getAnswer());
        toggleAnswerVisibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (answerText.getVisibility() == View.GONE) {
                    answerText.setVisibility(View.VISIBLE);
                    v.setBackgroundResource(R.drawable.ic_keyboard_arrow_up_grey_24dp);
                }
                else{
                    answerText.setVisibility(View.GONE);
                    v.setBackgroundResource(R.drawable.ic_keyboard_arrow_down_grey_24dp);
                }
            }
        });
        return view;
    }
}
