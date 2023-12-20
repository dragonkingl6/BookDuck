package duc.thanhhoa.bookduck.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import duc.thanhhoa.bookduck.adapter.AdapterCategory;
import duc.thanhhoa.bookduck.model.ModelCategory;

public class DashboardAdminActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    private ActivityDashboardAdminBinding binding;

    private ArrayList<ModelCategory> categoryList;

    private AdapterCategory adapterCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivityDashboardAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth= FirebaseAuth.getInstance();
        checkUser();

        loadCategory();

        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    adapterCategory.getFilter().filter(charSequence);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseAuth.signOut();
                checkUser();
            }
        });

        //handle click, open category activity
        binding.addCategoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DashboardAdminActivity.this, CategoryActivity.class));
            }
        });
    }

    private void loadCategory() {
        //init list
        categoryList= new ArrayList<>();
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Categories");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelCategory modelCategory= ds.getValue(ModelCategory.class);
                    //add to list
                    categoryList.add(modelCategory);
                }

                //setup adapter

                adapterCategory= new AdapterCategory(DashboardAdminActivity.this, categoryList);

                //set adapter to recyclerview
                binding.categoryRv.setAdapter(adapterCategory);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkUser() {
        FirebaseUser firebaseUser= firebaseAuth.getCurrentUser();
        if (firebaseUser==null){
            startActivity(new Intent(DashboardAdminActivity.this, MainActivity.class));
            finish();
        }
        else {
            //user logged in, check user type
            String email = firebaseUser.getEmail();

            binding.subtitleTv.setText(email);
        }
    }
}