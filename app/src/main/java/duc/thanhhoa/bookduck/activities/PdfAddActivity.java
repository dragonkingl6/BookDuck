package duc.thanhhoa.bookduck.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;

import duc.thanhhoa.bookduck.databinding.ActivityPdfAddBinding;
import duc.thanhhoa.bookduck.model.ModelCategory;

public class PdfAddActivity extends AppCompatActivity {

    private ActivityPdfAddBinding binding;
    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelCategory> categoryArrayList;

    private static final String TAG = "ADD_PDF_TAG";

    private static final int PDF_PICK_CODE = 1000;

    private Uri pdfUri= null;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        loadPdfCategories();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pdfPickIntent();
            }
        });

        binding.categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                categoryPickDialog();
            }
        });

        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });
    }

    private String title = "", description = "", category = "";
    private void validateData() {

        //step 1

        //get data

        title = binding.titleEt.getText().toString().trim();
        description = binding.descriptionEt.getText().toString().trim();
        category = binding.categoryTv.getText().toString().trim();

        //validate data

        if (TextUtils.isEmpty(title)){
            Toast.makeText(this, "Enter Title...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(description)){
            Toast.makeText(this, "Enter Description...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(category)){
            Toast.makeText(this, "Pick Category...", Toast.LENGTH_SHORT).show();
        }
        else if (pdfUri == null){
            Toast.makeText(this, "Pick PDF File...", Toast.LENGTH_SHORT).show();
        }
        else {
            uploadPdf();
        }
    }

    private void uploadPdf() {
        //step 2

        Log.d(TAG, "uploadPdf: ");

        progressDialog.setMessage("Uploading PDF...");
        progressDialog.show();

        long timestamp = System.currentTimeMillis();

        String filePathAndName = "Books/" + timestamp;

        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(pdfUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Log.d(TAG, "onSuccess: ");

                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String uploadedPdfUrl = ""+uriTask.getResult().toString();

                        uploadPdfInfo(uploadedPdfUrl, timestamp);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    private void uploadPdfInfo(String uploadedPdfUrl, long timestamp) {

        //step 3


        Log.d(TAG, "uploadPdfInfo: ");

        progressDialog.setMessage("Uploading PDF Info...");
        progressDialog.show();

        String uid = firebaseAuth.getUid();

        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("uid", uid);
        hashMap.put("id", ""+timestamp);
        hashMap.put("title", ""+title);
        hashMap.put("description", ""+description);
        hashMap.put("category", ""+category);
        hashMap.put("timestamp", ""+timestamp);
        hashMap.put("url", ""+uploadedPdfUrl);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
        reference.child(""+timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onSuccess: ");
                        Toast.makeText(PdfAddActivity.this, "PDF Uploaded...", Toast.LENGTH_SHORT).show();


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG, ""+e.getMessage());
                        Toast.makeText(PdfAddActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void loadPdfCategories() {
        Log.d(TAG, "loadPdfCategories: ");
        categoryArrayList = new ArrayList<>();

        //get all categories

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Categories");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryArrayList.clear();

                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    ModelCategory modelCategory = dataSnapshot.getValue(ModelCategory.class);

                    //add to list
                    categoryArrayList.add(modelCategory);

                    Log.d(TAG, "onDataChange: "+modelCategory.getCategory());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void categoryPickDialog() {

        Log.d(TAG, "categoryPickDialog: ");

        //get all categories

        String[] categoriesArray = new String[categoryArrayList.size()];

        for (int i=0; i<categoryArrayList.size(); i++){
            categoriesArray[i] = categoryArrayList.get(i).getCategory();
        }

        //dialog

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Category")
                .setItems(categoriesArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //handle item click
                        //get picked category

                        String category = categoriesArray[i];
                        //set picked category
                        binding.categoryTv.setText(category);

                        Log.d(TAG, "onClick: "+category);

                    }
                })
                .show();

    }

    private void pdfPickIntent() {
        Log.d(TAG, "pdfPickIntent: ");

        Intent intent = new Intent();

        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select PDF"), PDF_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){
            if (requestCode == PDF_PICK_CODE ){
                Log.d(TAG, "onActivityResult: ");

                pdfUri = data.getData();

                Log.d(TAG, "onActivityResult: "+pdfUri);
            }
        }
        else {
            Log.d(TAG, "onActivityResult: ");
            Toast.makeText(this, "oke", Toast.LENGTH_SHORT).show();
        }
    }
}