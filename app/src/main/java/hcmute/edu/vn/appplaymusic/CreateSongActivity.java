package hcmute.edu.vn.appplaymusic;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import hcmute.edu.vn.appplaymusic.Model.UploadFile;

public class CreateSongActivity extends AppCompatActivity {
    TextInputEditText editTextSong;
    TextInputEditText editTextSinger;
    TextView textViewFile;
    TextView textViewImage;
    ProgressBar progressBarAudio;
    ProgressBar progressBarImage;
    Uri audioUri;
    Uri imageUri;
    StorageReference mstorageRef;
    StorageTask muploadTask;
    DatabaseReference referenceSong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_song);
        editTextSong = findViewById(R.id.song_title);
        editTextSinger = findViewById(R.id.song_singer);
        textViewImage = findViewById(R.id.txtViewSelectImage);
        textViewFile = findViewById(R.id.txtViewSelectFile);
        progressBarAudio = findViewById(R.id.progressBarAudio);
        progressBarImage = findViewById(R.id.progressBarImage);
        referenceSong = FirebaseDatabase.getInstance().getReference().child("songs");
        mstorageRef = FirebaseStorage.getInstance().getReference().child("songs");
    }

    public void OpenAudioFile(View view){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        startActivityForResult(intent, 101);
    }

    public void OpenImageFile(View view){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 102);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 101 && resultCode == RESULT_OK && data.getData() != null){
            audioUri = data.getData();
            String filename = getFileName(audioUri);
            textViewFile.setText(filename);
        }
        else if(requestCode == 102 && resultCode == RESULT_OK && data.getData() != null){
            imageUri = data.getData();
            String filename = getFileName(imageUri);
            textViewImage.setText(filename);
        }
    }

    @SuppressLint("Range")
    private String getFileName(Uri uri){
        String result = null;
        if(uri.getScheme().equals("content")){
            Cursor cursor = getContentResolver().query(uri, null,null,null,null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
            finally {
                cursor.close();
            }
        }


        if(result == null){
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if(cut != -1){
                result = result.substring(cut + 1);
            }
        }

        return result;
    }

    public void uploadAudioToFireBase(View view){
        if(textViewFile.getText().toString().equals("No file selected")){
            Toast.makeText(getApplicationContext(),"Please select an audio",Toast.LENGTH_LONG).show();
        }
        else if(textViewImage.getText().toString().equals("No image selected")){
            Toast.makeText(getApplicationContext(),"Please select an image",Toast.LENGTH_LONG).show();
        }
        else{
            if(muploadTask != null && muploadTask.isInProgress()){
                Toast.makeText(getApplicationContext(),"Song upload is already in progress",Toast.LENGTH_LONG).show();
            }
            else {
                uploadFile();
            }
        }
    }

    private void uploadFile(){
        if(audioUri != null && imageUri != null){
            String durationTxt;
            Toast.makeText(getApplicationContext(),"Uploading please wait...",Toast.LENGTH_LONG).show();
            progressBarAudio.setVisibility(View.VISIBLE);
            progressBarImage.setVisibility(View.VISIBLE);

            StorageReference storageReference = mstorageRef.child(System.currentTimeMillis() + "." +getFileExtension(audioUri));

            int durationMillis = findSongDuration(audioUri);

            if(durationMillis == 0){
                durationTxt = "NA";
            }
            durationTxt = getDurationFromMillis(durationMillis);

            String finalDurationTxt = durationTxt;
            muploadTask = storageReference.putFile(audioUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    StorageReference storageReferenceImage = mstorageRef.child(System.currentTimeMillis() + "." +getFileExtension(imageUri));
                                    storageReferenceImage.putFile(imageUri)
                                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                @Override
                                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                    storageReferenceImage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                        @Override
                                                        public void onSuccess(Uri imageuri) {
                                                            UploadFile uploadFile = new UploadFile(editTextSong.getText().toString(), finalDurationTxt, uri.toString(),
                                                                                                    editTextSinger.getText().toString(), imageuri.toString());

                                                            String uploadId = referenceSong.push().getKey();
                                                            referenceSong.child(uploadId).setValue(uploadFile);
                                                        }
                                                    });
                                                }
                                            })
                                            .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                                @Override
                                                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                                    double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                                                    progressBarImage.setProgress((int)progress);
                                                }
                                            });
                                }
                            });
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                            progressBarAudio.setProgress((int)progress);
                        }
                    });

        }
        else{
            Toast.makeText(getApplicationContext(), "No file selected to upload", Toast.LENGTH_LONG).show();
        }
    }

    private String getDurationFromMillis(int durationMillis){
        Date date = new Date(durationMillis);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());
        String myTime = simpleDateFormat.format(date);
        return myTime;
    }

    private int findSongDuration(Uri uri){
        int timeInMillisec = 0;
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(this,uri);
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            timeInMillisec = Integer.parseInt(time);
            return timeInMillisec;
        }
        catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }
}
