package com.example.levi.ros_e_stop;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ChangeVelOutputTopicActivity extends Activity {

    EditText mEditText;
    Button okButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_vel_output_topic);

        mEditText = (EditText) findViewById(R.id.editText);
        okButton = (Button) findViewById(R.id.button2);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String topic = mEditText.getText().toString();
                Intent resultData = new Intent();
                resultData.putExtra("topic", topic);
                setResult(Activity.RESULT_OK, resultData);
                finish();
            }
        });



    }
}
