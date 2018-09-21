package com.example.mateusz.notatnik;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class NoteActivity extends AppCompatActivity {

    private final int MEMORY_ACCESS=5;
    private String path= Environment.getExternalStorageDirectory().toString()+"/MyNotatnik";
    Bundle bundle=new Bundle();
    EditText editText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        editText =(EditText)findViewById(R.id.editText2);
        editText.setText(bundle.getString("editText"));
        if(ActivityCompat.shouldShowRequestPermissionRationale(NoteActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){}
        else{
            ActivityCompat.requestPermissions(NoteActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},MEMORY_ACCESS);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode)
        {
            case MEMORY_ACCESS:
            {
                if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){}
                else{
                    Toast.makeText(getApplicationContext(),"Brak zgody",Toast.LENGTH_LONG).show();

                }
            }
        }
    }

    @Override
    protected void onPause() {
        bundle.putString("editText", editText.getText().toString());
        super.onPause();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save)
        {
            createDir();
            createFile();
            finish();
        }

        return super.onOptionsItemSelected(item);

        }

        public void createDir()
        {
            File folder=new File(path);
            if(!folder.exists())
            {   try {
                folder.mkdir();
                }

                catch(Exception e)
                {
                    Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_LONG).show();
                }
            }

        }

        public void createFile()
        {
            File file=new File(path+"/"+System.currentTimeMillis()+".txt");
            FileOutputStream fOut;
            OutputStreamWriter myOutWriter;
            try{
                fOut=new FileOutputStream(file);
                myOutWriter=new OutputStreamWriter(fOut);
                myOutWriter.append(editText.getText());
                myOutWriter.close();
                fOut.close();
            }
            catch (Exception e)
            {
                Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_LONG).show();

            }
        }
    }
