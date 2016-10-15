package com.stream.commentperfect;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.ExpandedMenuView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    TextView editText;

    PathView pathView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pathView = (PathView) findViewById(R.id.path_view);
        editText = (TextView) findViewById(R.id.input_score);
        pathView.setMaxScore(3);
    }

    public void setSore(View view){
        if (editText != null) {

            editText.setText("" + pathView.getScore());
        }
    }

}
