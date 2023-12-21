package duc.thanhhoa.bookduck.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import duc.thanhhoa.bookduck.R;
import duc.thanhhoa.bookduck.databinding.ActivityPdfEditBinding;

public class PdfEditActivity extends AppCompatActivity {

    private ActivityPdfEditBinding binding;

    private String bookId;

    private ProgressDialog progressDialog;

    private ArrayList<String> categoryTitleList, categoryIdList;

    private static final String TAG = "PdfEditActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bookId = getIntent().getStringExtra("bookId");

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);
        
        loadCategories();
        loadBookInfo();

        binding.categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                categoryDialog();
            }
        });
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });

    }
    private String title="", description="";

    private void validateData() {

        title = binding.titleEt.getText().toString().trim();
        description = binding.descriptionEt.getText().toString().trim();

        if (TextUtils.isEmpty(title)){
            Toast.makeText(this, "Enter Title...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(description)){
            Toast.makeText(this, "Enter Description...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(selectedCategoryId)){
            Toast.makeText(this, "Choose Category...", Toast.LENGTH_SHORT).show();
        }
        else {
            updateBook();
        }
    }

    private void updateBook() {
        progressDialog.setMessage("Updating Book...");
        progressDialog.show();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("title", "" + title);
        hashMap.put("description", "" + description);
        hashMap.put("categoryId", "" + selectedCategoryId);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .updateChildren(hashMap)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(PdfEditActivity.this, "Updated...", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(PdfEditActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadBookInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        selectedCategoryId = "" + snapshot.child("categoryId").getValue();
                        String title = "" + snapshot.child("title").getValue();
                        String description = "" + snapshot.child("description").getValue();

                        binding.titleEt.setText(title);
                        binding.descriptionEt.setText(description);

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
                        ref.child(selectedCategoryId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        String category = "" + snapshot.child("category").getValue();
                                        binding.categoryTv.setText(category);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private String selectedCategoryId= "", selectedCategoryTitle = "";

    private void categoryDialog() {
        Log.d(TAG, "categoryDialog: called");

        String[] categoriesArray = new String[categoryTitleList.size()];
        for (int i = 0; i < categoryTitleList.size(); i++) {
            categoriesArray[i] = categoryTitleList.get(i);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Category")
                .setItems(categoriesArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        selectedCategoryId = categoryIdList.get(i);
                        selectedCategoryTitle = categoryTitleList.get(i);

                        binding.categoryTv.setText(selectedCategoryTitle);
                    }
                })
                .show();
    }

    private void loadCategories() {
        Log.d(TAG, "loadCategories: called");

        categoryIdList = new ArrayList<>();
        categoryTitleList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryIdList.clear();
                categoryTitleList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    String id = "" + ds.child("id").getValue();
                    String category = "" + ds.child("category").getValue();

                    categoryIdList.add(id);
                    categoryTitleList.add(category);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}