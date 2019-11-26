package com.example.beacontest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class EditActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_view);

        final EditText uuid_edit = (EditText)findViewById(R.id.uuid_edit);
        final EditText major_edit = (EditText)findViewById(R.id.major_edit);
        final EditText minor_edit = (EditText)findViewById(R.id.minor_edit);
        final EditText name_edit = (EditText)findViewById(R.id.name_edit);
        final EditText url_edit = (EditText)findViewById(R.id.url_edit);
        Button button = (Button)findViewById(R.id.button);

        Intent intent = getIntent();
        String mod = intent.getStringExtra("mod");

        if(mod != null) {
            String uuid = intent.getStringExtra("UUID");
            String major = intent.getStringExtra("Major");
            String minor = intent.getStringExtra("Minor");
            intent.putExtra("uuid", uuid);
            intent.putExtra("major", major);
            intent.putExtra("minor", minor);

            uuid_edit.setText(uuid);
            major_edit.setText(major);
            minor_edit.setText(minor);
        }

        button.setOnClickListener(new View.OnClickListener() {
            String result = null;
            @Override
            public void onClick(View v) {
                String UUID = uuid_edit.getText().toString();
                String Major = major_edit.getText().toString();
                String Minor = minor_edit.getText().toString();
                String Name = name_edit.getText().toString();
                String Url = url_edit.getText().toString();


            }
        });
    }
}
