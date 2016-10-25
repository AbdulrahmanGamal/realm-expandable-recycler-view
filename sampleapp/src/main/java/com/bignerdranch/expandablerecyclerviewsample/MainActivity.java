package com.bignerdranch.expandablerecyclerviewsample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.bignerdranch.expandablerecyclerviewsample.linear.vertical.VerticalLinearRecyclerViewSampleActivity;

/**
 * Main Activity that contains navigation for sample application.
 *
 * @author Ryan Brooks
 * @version 1.0
 * @since 5/27/2015
 */
public class MainActivity extends AppCompatActivity {

    private Button mVerticalSampleButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mVerticalSampleButton = (Button) findViewById(R.id.activity_main_vertical_linear_sample_button);
        mVerticalSampleButton.setOnClickListener(mVerticalSampleButtonClickListener);
    }

    @NonNull
    private View.OnClickListener mVerticalSampleButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(@NonNull View v) {
            startActivity(VerticalLinearRecyclerViewSampleActivity.newIntent(v.getContext()));
        }
    };
}
