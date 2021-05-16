package com.example.gallery;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceContour;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceLandmark;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSIONS = 100;
    GridView gridView;
    FaceDetector detector;
    Uri uri;
    Uri ur;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    ArrayList<File> list1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        gridView = findViewById(R.id.grid_view);
        Log.d("eeeee", "imageReader: ");
        if ((ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
            if ((ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) && (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE))) {

            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_PERMISSIONS);

            }
        }
        else{
            list1 = imageReader(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
            Log.d("listSizeq", "imageReader: " + list1.size());
            gridAdapter gr = new gridAdapter();
            gridView.setAdapter(gr);
            for (int i = 0; i < list1.size(); i++) {
                ml(list1.get(i), String.valueOf(i));
            }
            Log.d("jize", "onCreate: " + list1.size());
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
if(requestCode == REQUEST_PERMISSIONS ){
    if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
        list1 = imageReader(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
        Log.d("listSizeq", "imageReader: " + list1.size());
        gridAdapter gr = new gridAdapter();
        gridView.setAdapter(gr);
        for (int i = 0; i < list1.size(); i++) {
            ml(list1.get(i), String.valueOf(i+1));
        }
        Log.d("jize", "onCreate: " + list1.size());
    }
}
    }

//        ml(list.get(1),"2");
//        storage.getReference("1.jpeg").putFile(Uri.fromFile(list.get(0))).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//            @Override
//            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                storage.getReference("2.jpeg").putFile(Uri.fromFile(list.get(1)));
//            }
//        });
    public void ml(final File file, final String num){
        Log.d("registering", "ml: " + num);
        InputImage image = null;
        ur = Uri.fromFile(file);
        try {
            image = InputImage.fromFilePath(getApplicationContext(), ur);
             detector= FaceDetection.getClient();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ImageLabeler labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);
        labeler.process(image)
                .addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
                    @Override
                    public void onSuccess(List<ImageLabel> labels) {
                        for (ImageLabel label : labels) {
                            Log.d("labelll", "onSuccess: " + num);
                            String text = label.getText();
                            float confidence = label.getConfidence();
                            int index = label.getIndex();
                            if(text.equals("Model") && confidence>=0.5){
                                Log.d("modelllll", "onSuccess: ");
                                storage.getReference("girl"+num+".jpeg").putFile(Uri.fromFile(file)).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("failurrrr2", "onFailure: ");
                                    }
                                });
                            }
                            else if(text.equals("Dude") && confidence>=0.5){
                                Log.d("dudeeee", "onSuccess: ");
                                storage.getReference("guy"+num+".jpeg").putFile(Uri.fromFile(file)).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("failurrrrr1", "onFailure: ");
                                    }
                                });
                            }
                            Log.d("texttttttttt" + num, "onSuccess: " + text + "  "+ confidence );
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("trrajendran", "onFailure: " + num);
                    }
                });
    }


    private ArrayList<File> imageReader(File externalStorageDirectory) {
        Log.d("eeeee1", "imageReader: " + externalStorageDirectory.toString());
        ArrayList<File> files = new ArrayList<>();
        Log.d("eeeee2", "imageReader: " );

       File[] fi = externalStorageDirectory.listFiles();
        Log.d("eeeee2", "imageReader: " + Arrays.toString(fi));
//        Log.d("eeeee3", "imageReader: " + fi.length);
       for(int i =0 ; i<fi.length; i++){
           Log.d("eeeee4", "imageReader: " );
           if(fi[i].isDirectory()){
               files.addAll(imageReader(fi[i]));
           }else{
               if(fi[i].getName().endsWith(".jpeg") || fi[i].getName().endsWith(".jpg")){
                   files.add(fi[i]);
               }
           }
       }
        Log.d("wize", "imageReader: " + files.size());
       return files;
    }
   public class gridAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return list1.size();
        }

        @Override
        public Object getItem(int position) {
            return list1.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = getLayoutInflater().inflate(R.layout.row_layout,parent,false);
            ImageView imageView = view.findViewById(R.id.gallery_image);
            imageView.setImageURI(Uri.parse(list1.get(position).toString()));

            return view;
        }
    }
}

//    Task<List<Face>> result =
//                detector.process(image)
//                        .addOnSuccessListener(
//                                new OnSuccessListener<List<Face>>() {
//                                    @Override
//                                    public void onSuccess(List<Face> faces) {
//                                        for (Face face : faces) {
//                                            Rect bounds = face.getBoundingBox();
//                                            float rotY = face.getHeadEulerAngleY();  // Head is rotated to the right rotY degrees
//                                            float rotZ = face.getHeadEulerAngleZ();  // Head is tilted sideways rotZ degrees
//
//                                            // If landmark detection was enabled (mouth, ears, eyes, cheeks, and
//                                            // nose available):
//                                            FaceLandmark leftEar = face.getLandmark(FaceLandmark.LEFT_EAR);
//                                            if (leftEar != null) {
//                                                PointF leftEarPos = leftEar.getPosition();
//                                            }
//
//                                            // If contour detection was enabled:
////                                            List<PointF> leftEyeContour =
////                                                    face.getContour(FaceContour.LEFT_EYE).getPoints();
////                                            List<PointF> upperLipBottomContour =
////                                                    face.getContour(FaceContour.UPPER_LIP_BOTTOM).getPoints();
//
//                                            // If classification was enabled:
//                                            if (face.getSmilingProbability() != null) {
//                                                float smileProb = face.getSmilingProbability();
//                                            }
//                                            if (face.getRightEyeOpenProbability() != null) {
//                                                float rightEyeOpenProb = face.getRightEyeOpenProbability();
//                                            }
//
//                                            // If face tracking was enabled:
//                                            if (face.getTrackingId() != null) {
//                                                int id = face.getTrackingId();
//                                                Log.d("faceworst", "onSuccess: " + id);
//                                            }
//                                        }
//
//                                    }
//                                })
//                        .addOnFailureListener(
//                                new OnFailureListener() {
//                                    @Override
//                                    public void onFailure(@NonNull Exception e) {
//                                        // Task faile
//                                    }
//                                });