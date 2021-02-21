package com.example.healthmonitor;



import java.io.File;

        import android.app.Activity;
        import android.content.ActivityNotFoundException;
        import android.content.Intent;
        import android.net.Uri;
        import android.os.Bundle;
        import android.os.Environment;
        import android.view.View;
        import android.widget.Button;
        import android.widget.Toast;

public class UploadFiles extends Activity {

    Button button1;
Intent intent;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload_files);
        button1 = (Button) findViewById(R.id.Click_btn);


        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent, 7);

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub

        switch(requestCode){

            case 7:

                if(resultCode==RESULT_OK){

                    String PathHolder = data.getData().getPath();

                    Toast.makeText(UploadFiles.this, PathHolder , Toast.LENGTH_SHORT).show();

                }
                Toast.makeText(UploadFiles.this,"Uploaded Pdf successfully" , Toast.LENGTH_LONG).show();
                break;

        }
    }
}