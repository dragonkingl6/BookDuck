package duc.thanhhoa.bookduck.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import duc.thanhhoa.bookduck.R;
import duc.thanhhoa.bookduck.databinding.ActivityPdfDetailBinding;

public class PdfDetailActivity extends AppCompatActivity {

    private ActivityPdfDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}