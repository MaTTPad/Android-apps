package com.example.mateusz.notatnik;
import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.mateusz.notatnik.utils.CameraUtils;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.util.List;
public class CameraActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 100;

    public static final String IMAGE_PATH = "image_path";

    public static final int IMG_TYPE = 1;

    public static final int BITMAP_SIZE = 8;

    public static final String ALBUM_NAME = "Zdjęcia z notatnika";

    public static final String IMAGE_EXT = "jpg";

    private static String imagePath;

    private ImageView imgPreview;
    private Button takePictureButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (!CameraUtils.isDeviceSupportingCamera(getApplicationContext())) {
            Toast.makeText(getApplicationContext(),
                    "Twoje urządzenie nie posiada kamery.",
                    Toast.LENGTH_LONG).show();
            finish();
        }

        imgPreview = findViewById(R.id.imgPreview);
        takePictureButton = findViewById(R.id.btnCapturePicture);

        takePictureButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (CameraUtils.checkPermissions(getApplicationContext())) {
                    takePicture();
                } else {
                    requestCameraPermission(IMG_TYPE);
                }
            }
        });
        restoreFromBundle(savedInstanceState);
    }

    private void restoreFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(IMAGE_PATH)) {
                imagePath = savedInstanceState.getString(IMAGE_PATH);
                if (!TextUtils.isEmpty(imagePath)) {
                    if (imagePath.substring(imagePath.lastIndexOf(".")).equals("." + IMAGE_EXT)) {
                        previewTakenPicture();
                    }
                    }
                }
            }
        }

    private void requestCameraPermission(final int type) {
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {

                            if (type == IMG_TYPE) {
                                takePicture();
                            }
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File file = CameraUtils.getOutputMediaFile(IMG_TYPE);
        if (file != null) {
            imagePath = file.getAbsolutePath();
        }

        Uri fileUri = CameraUtils.getOutputMediaFileUri(getApplicationContext(), file);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(IMAGE_PATH, imagePath);
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        imagePath = savedInstanceState.getString(IMAGE_PATH);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                CameraUtils.refreshGallery(getApplicationContext(), imagePath);


                previewTakenPicture();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(),
                        "Powrót.", Toast.LENGTH_SHORT)
                        .show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "Błąd. Nie mogę zapisać zdjęcia.", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    private void previewTakenPicture() {
        try {
            imgPreview.setVisibility(View.VISIBLE);

            Bitmap bitmap = CameraUtils.optimizeBitmap(BITMAP_SIZE, imagePath);

            imgPreview.setImageBitmap(bitmap);

        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

}
