package com.definitelynotexample.etlantis.dro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("vall", Context.MODE_PRIVATE);

        boolean not_first_time = sharedPref.getBoolean("consented", false);

        if (not_first_time){
            Intent accepted = new Intent (MainActivity.this, MapsActivity.class);
            startActivity(accepted);
            finish();
        }


        Button accept = findViewById(R.id.button);
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("consented", true);
                editor.apply();

                Intent accepted = new Intent (MainActivity.this, MapsActivity.class);
                startActivity(accepted);
                finish();
            }
        });
    }
}
