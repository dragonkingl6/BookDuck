package duc.thanhhoa.bookduck.adapter;

import static duc.thanhhoa.bookduck.Constants.MAX_BYTES_PDF;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import duc.thanhhoa.bookduck.MyApplication;
import duc.thanhhoa.bookduck.databinding.RowListAdminBinding;
import duc.thanhhoa.bookduck.filter.FilterPdfAdmin;
import duc.thanhhoa.bookduck.model.ModelPdf;

public class AdapterPdfAdmin extends RecyclerView.Adapter<AdapterPdfAdmin.HolderPdfAdmin> implements Filterable {

    private Context context;

    public ArrayList<ModelPdf> pdfList, filterList;

    private FilterPdfAdmin filter;

    private RowListAdminBinding binding;

    private static final String TAG = "PDF_ADAPTER";

    public AdapterPdfAdmin(Context context, ArrayList<ModelPdf> pdfList) {
        this.context = context;
        this.pdfList = pdfList;
        this.filterList = pdfList;
    }

    @NonNull
    @Override
    public HolderPdfAdmin onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding= RowListAdminBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderPdfAdmin(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfAdmin holder, int position) {

        ModelPdf modelPdf= pdfList.get(position);
        String title= modelPdf.getTitle();
        String description= modelPdf.getDescription();
        String timestamp= modelPdf.getTimestamp();

        String fromDate = MyApplication.formatTimestamp(Long.parseLong(timestamp));

        holder.titleTv.setText(title);
        holder.descriptionTv.setText(description);
        holder.dateTv.setText(fromDate);

        loadCategory(modelPdf, holder);
        loadPdfFromUrl(modelPdf, holder);
        loadPdfSize(modelPdf, holder);

    }

    private void loadPdfSize(ModelPdf modelPdf, HolderPdfAdmin holder) {

        String pdfUrl= modelPdf.getUrl();

        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getMetadata()
                .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
                        Toast.makeText(context,""+storageMetadata.getSizeBytes(), Toast.LENGTH_SHORT).show();

                        double bytes= storageMetadata.getSizeBytes();
                        Log.e(TAG, "onSuccess: "+bytes);

                        double kb= bytes/1024;
                        double mb= kb/1024;

                        if(mb>=1){
                            holder.sizeTv.setText(String.format("%.2f", mb)+" MB");
                        }
                        else if(kb>=1){
                            holder.sizeTv.setText(String.format("%.2f", kb)+" KB");
                        }
                        else {
                            holder.sizeTv.setText(String.format("%.2f", bytes)+" Bytes");
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context,""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "onFailure: "+e.getMessage());

                    }
                });
    }

    private void loadPdfFromUrl(ModelPdf modelPdf, HolderPdfAdmin holder) {

        String pdfUrl= modelPdf.getUrl();
        StorageReference ref= FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        binding.progressBar.setVisibility(View.VISIBLE);

        ref.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Toast.makeText(context,""+bytes.length, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "onSuccess: "+bytes.length);

                        holder.pdfView.fromBytes(bytes)
                                .pages(0)
                                .spacing(0)
                                .swipeHorizontal(false)
                                .enableSwipe(false)
                                .onError(new OnErrorListener() {
                                    @Override
                                    public void onError(Throwable t) {
                                        holder.progressBar.setVisibility(View.VISIBLE);
                                        Log.e(TAG, "onError: "+t.getMessage());
                                    }
                                })
                                .onPageError(new OnPageErrorListener() {
                                    @Override
                                    public void onPageError(int page, Throwable t) {
                                        holder.progressBar.setVisibility(View.VISIBLE);
                                        Log.e(TAG, "onPageError: "+t.getMessage());

                                    }
                                })
                                .onLoad(new OnLoadCompleteListener() {
                                    @Override
                                    public void loadComplete(int nbPages) {
                                        holder.progressBar.setVisibility(View.GONE);
                                        Log.e(TAG, "loadComplete: "+nbPages);
                                    }
                                })
                                .load();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        holder.progressBar.setVisibility(View.GONE);
                        Toast.makeText(context,""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "onFailure: "+e.getMessage());

                    }
                });
    }

    private void loadCategory(ModelPdf modelPdf, HolderPdfAdmin holder) {

        String categoryId = modelPdf.getCategoryId();
        binding.progressBar.setVisibility(View.GONE);

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Categories");
        ref.child(categoryId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String category= ""+snapshot.child("category").getValue();

                        holder.categoryTv.setText(category);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return pdfList.size();
    }

    @Override
    public Filter getFilter() {
        if(filter== null){
            filter= new FilterPdfAdmin(filterList, this);
        }
        return filter;
    }

    class HolderPdfAdmin extends RecyclerView.ViewHolder {

        PDFView pdfView;
        ProgressBar progressBar;
        TextView titleTv, descriptionTv, categoryTv,sizeTv, dateTv;
        ImageButton imageButton;
        public HolderPdfAdmin(@NonNull View itemView) {
            super(itemView);

            pdfView= binding.pdfView;
            progressBar= binding.progressBar;
            titleTv= binding.titleTv;
            descriptionTv= binding.descriptionTv;
            categoryTv = binding.categoryTv;
            sizeTv= binding.sizeTv;
            dateTv= binding.dateTv;
            imageButton= binding.moreBtn;
        }
    }
}
