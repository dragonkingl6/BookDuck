package duc.thanhhoa.bookduck.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import duc.thanhhoa.bookduck.R;
import duc.thanhhoa.bookduck.databinding.ActivityPdfAddBinding;

public class PdfAddActivity extends AppCompatActivity {

    private ActivityPdfAddBinding binding;
    private FirebaseAuth firebaseAuth;

    private static final String TAG = "ADD_PDF_TAG";

    private static final int PDF_PICK_CODE = 1000;

    private Uri pdfUri= null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

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

        if (requestCode == RESULT_OK){
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