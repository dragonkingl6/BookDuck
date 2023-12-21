package duc.thanhhoa.bookduck.adapter;

import static duc.thanhhoa.bookduck.Constants.MAX_BYTES_PDF;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import androidx.appcompat.app.AlertDialog;
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
import duc.thanhhoa.bookduck.activities.PdfEditActivity;
import duc.thanhhoa.bookduck.databinding.RowListAdminBinding;
import duc.thanhhoa.bookduck.filter.FilterPdfAdmin;
import duc.thanhhoa.bookduck.model.ModelPdf;

public class AdapterPdfAdmin extends RecyclerView.Adapter<AdapterPdfAdmin.HolderPdfAdmin> implements Filterable {

    private Context context;

    public ArrayList<ModelPdf> pdfList, filterList;

    private FilterPdfAdmin filter;

    private RowListAdminBinding binding;

    private ProgressDialog progressDialog;

    private static final String TAG = "PDF_ADAPTER";

    public AdapterPdfAdmin(Context context, ArrayList<ModelPdf> pdfList) {
        this.context = context;
        this.pdfList = pdfList;
        this.filterList = pdfList;

        progressDialog= new ProgressDialog(context);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
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
        String pdfUrl= modelPdf.getUrl();
        String pdfId= modelPdf.getId();
        String categoryId= modelPdf.getCategoryId();
        String timestamp= modelPdf.getTimestamp();

        String fromDate = MyApplication.formatTimestamp(Long.parseLong(timestamp));

        holder.titleTv.setText(title);
        holder.descriptionTv.setText(description);
        holder.dateTv.setText(fromDate);

        MyApplication.loadCategory(""+ categoryId, holder.categoryTv);
        MyApplication.loadPdfFromUrlSignlePage("" + pdfUrl, ""+title,holder.pdfView, holder.progressBar);
        MyApplication.loadPdfSize("" + pdfUrl,"" + title, holder.sizeTv);

        binding.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moreOptions(modelPdf, holder);
            }
        });

    }

    private void moreOptions(ModelPdf modelPdf, HolderPdfAdmin holder) {

        String bookId= modelPdf.getId();
        String bookUrl= modelPdf.getUrl();
        String bookTitle= modelPdf.getTitle();

        String[] options= {"Edit", "Delete"};

        AlertDialog.Builder builder= new AlertDialog.Builder(context);
        builder.setTitle("Choose Action")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i==0){
                            //Edit
                            Toast.makeText(context, "Edit", Toast.LENGTH_SHORT).show();
                            Intent intent= new Intent(context, PdfEditActivity.class);
                            intent.putExtra("bookId", bookId);
                            context.startActivity(intent);
                        }
                        else if (i==1){
                            //Delete
                            MyApplication.deletePdfBook(context, ""+ bookId, ""+ bookUrl, ""+ bookTitle);
                            //deletePdfBook(modelPdf, holder);
                        }
                    }
                })
                .show();
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
