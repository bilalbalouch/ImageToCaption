package com.example.captionapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.BitSet;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView imageView;
    private Button btnSelectImage, btnUpload;
    private TextView tvCaption;
    private Uri selectedImageUri;
    private File imageFile;

    // Correct base URL (just domain with trailing slash)
    private static final String BASE_URL = "https://bde6-2400-adc7-2111-3e00-b5c0-fa1e-6ca1-eb8d.ngrok-free.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnUpload = findViewById(R.id.btnUpload);
        tvCaption = findViewById(R.id.tvCaption);

        btnSelectImage.setOnClickListener(v -> openFileChooser());

        btnUpload.setOnClickListener(v -> {
            if (imageFile != null) {
                uploadImage(Uri.fromFile(imageFile));
            } else {
                Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            imageView.setImageURI(selectedImageUri);

            String path = getPathFromUri(selectedImageUri);
            if (path != null) {
                imageFile = new File(path);
            }
        }
    }

    private String getPathFromUri(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        cursor.close();
        return path;
    }

    private void uploadImage(Uri imageUri) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);

            byte[] bytes = readBytesFromInputStream(inputStream);
            RequestBody requestFile = RequestBody.create(bytes, MediaType.parse("image/*"));
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", "upload.jpg", requestFile);

            Call<CaptionResponse> call = apiService.uploadImage(body);
            call.enqueue(new Callback<CaptionResponse>() {
                @Override
                public void onResponse(Call<CaptionResponse> call, Response<CaptionResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        tvCaption.setText(response.body().getCaption());
                    } else {
                        tvCaption.setText("Failed to get caption");
                    }
                }

                @Override
                public void onFailure(Call<CaptionResponse> call, Throwable t) {
                    tvCaption.setText("Error: " + t.getMessage());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error reading image", Toast.LENGTH_SHORT).show();
        }
    }

    private byte[] readBytesFromInputStream(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[8192];
        int bytesRead;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
        return output.toByteArray();
    }


}

