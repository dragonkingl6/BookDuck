package duc.thanhhoa.bookduck.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import duc.thanhhoa.bookduck.MyApplication;
import duc.thanhhoa.bookduck.R;
import duc.thanhhoa.bookduck.databinding.ActivityPdfDetailBinding;

public class PdfDetailActivity extends AppCompatActivity {

    private ActivityPdfDetailBinding binding;

    String bookId, bookTitle, bookUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();

        bookId = intent.getStringExtra("bookId");

        binding.dowloadNowBtn.setVisibility(View.GONE);

        loadBookDetails();

        MyApplication.voidViewCount(bookId);

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.readNowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PdfDetailActivity.this, PdfViewActivity.class);
                intent.putExtra("bookId", bookId);
                startActivity(intent);
            }
        });

        binding.dowloadNowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(PdfDetailActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    MyApplication.dowloadBook(PdfDetailActivity.this, "" + bookId, "" + bookTitle, "" + bookUrl);
                }else {
                    iActivityResultLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
            }
        });
    }

    private ActivityResultLauncher<String> iActivityResultLauncher= registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted) {
            MyApplication.dowloadBook(this, ""+bookId, ""+bookTitle, ""+bookUrl);
        }else {
            Log.d("TAG", "onActivityResult: Permission Denied");
        }
    });

    private void loadBookDetails() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
        reference.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        bookTitle = "" + snapshot.child("title").getValue();
                        String description = "" + snapshot.child("description").getValue();
                        String categoryId = "" + snapshot.child("categoryId").getValue();
                        bookUrl = "" + snapshot.child("url").getValue();
                        String timestamp = "" + snapshot.child("timestamp").getValue();
                        String viewCount = "" + snapshot.child("viewCount").getValue();
                        String downloadCount = "" + snapshot.child("downloadCount").getValue();

                        binding.dowloadNowBtn.setVisibility(View.VISIBLE);

                        String date= MyApplication.formatTimestamp(Long.parseLong(timestamp));

                        MyApplication.loadCategory(""+ categoryId, binding.categoryTv);

                        MyApplication.loadPdfFromUrlSignlePage(""+bookUrl, ""+ bookTitle,binding.pdfView, binding.progressBar);

                        MyApplication.loadPdfSize(""+bookUrl, ""+ bookTitle, binding.sizeTv);

                        binding.titleTv.setText(bookTitle);
                        binding.descriptionTv.setText(description);
                        binding.viewTv.setText(viewCount);
                        binding.dowloadTv.setText(downloadCount);
                        binding.dateTv.setText(date);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
}