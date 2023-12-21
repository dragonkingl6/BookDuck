package duc.thanhhoa.bookduck.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ProgressBar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import duc.thanhhoa.bookduck.R;
import duc.thanhhoa.bookduck.adapter.AdapterPdfAdmin;
import duc.thanhhoa.bookduck.databinding.ActivityPdfListAdminBinding;
import duc.thanhhoa.bookduck.model.ModelPdf;

public class PdfListAdminActivity extends AppCompatActivity {

    private ActivityPdfListAdminBinding binding;

    private ArrayList<ModelPdf> pdfList;

    private AdapterPdfAdmin adapterPdfAdmin;

    private String categoryId, categoryTitle;

    private ProgressBar progressBar;

    private static final String TAG = "PDF_LIST_ADMIN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivityPdfListAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent= getIntent();
        categoryId=intent.getStringExtra("categoryId");
        categoryTitle=intent.getStringExtra("categoryTitle");

        binding.subTitleTv.setText(categoryTitle);

        loadPdfList();

        //search
        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                try {
                    adapterPdfAdmin.getFilter().filter(charSequence);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


    }

    private void loadPdfList() {
        pdfList= new ArrayList<>();

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Books");
        ref.orderByChild("categoryId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        pdfList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelPdf modelPdf= ds.getValue(ModelPdf.class);
                            pdfList.add(modelPdf);
                        }

                        adapterPdfAdmin= new AdapterPdfAdmin(PdfListAdminActivity.this, pdfList);
                        binding.bookRv.setAdapter(adapterPdfAdmin);


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}