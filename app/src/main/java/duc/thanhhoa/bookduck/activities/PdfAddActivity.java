package duc.thanhhoa.bookduck.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.checkerframework.checker.units.qual.s;

import java.util.ArrayList;

import duc.thanhhoa.bookduck.R;
import duc.thanhhoa.bookduck.databinding.ActivityPdfAddBinding;
import duc.thanhhoa.bookduck.model.ModelCategory;

public class PdfAddActivity extends AppCompatActivity {

    private ActivityPdfAddBinding binding;
    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelCategory> categoryArrayList;

    private static final String TAG = "ADD_PDF_TAG";

    private static final int PDF_PICK_CODE = 1000;

    private Uri pdfUri= null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        loadPdfCategories();

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