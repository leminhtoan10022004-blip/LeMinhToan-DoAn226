package com.chaquo.python.console;

import android.app.*;
import android.content.Intent;
import android.os.Bundle;

import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }
        
        Intent intent = new Intent(this, MLPSampleActivity.class);
        FirestoreImporter.importData(this);
        startActivity(intent);
        finish();
    }
}
