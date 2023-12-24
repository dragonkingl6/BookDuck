package duc.thanhhoa.bookduck;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;
import static duc.thanhhoa.bookduck.Constants.MAX_BYTES_PDF;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

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

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import duc.thanhhoa.bookduck.adapter.AdapterPdfAdmin;
import duc.thanhhoa.bookduck.model.ModelPdf;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static final String formatTimestamp(long timestamp) {

        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(timestamp);

        String date = DateFormat.format("dd/MM/yyyy", calendar).toString();

        return date;
    }

    public static void deletePdfBook(Context context, String bookId, String bookUrl, String bookTitle) {
        String TAG = "deletePdfBook";

        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Deleting...");
        progressDialog.show();

        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        ref.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Books");
                        databaseReference.child(bookId)
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        progressDialog.dismiss();
                                        Toast.makeText(context, "Deleted Successfully...", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e(TAG, "onSuccess: "+e.getMessage());
                                        progressDialog.dismiss();
                                    }
                                });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    public static void loadPdfSize(String pdfUrl, String pdfTitle, TextView sizeTv) {
        String TAG = "loadPdfSize";

        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getMetadata()
                .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {

                        double bytes= storageMetadata.getSizeBytes();
                        Log.e(TAG, "onSuccess: "+bytes);

                        double kb= bytes/1024;
                        double mb= kb/1024;

                        if(mb>=1){
                            sizeTv.setText(String.format("%.2f", mb)+" MB");
                        }
                        else if(kb>=1){
                            sizeTv.setText(String.format("%.2f", kb)+" KB");
                        }
                        else {
                            sizeTv.setText(String.format("%.2f", bytes)+" Bytes");
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: "+e.getMessage());

                    }
                });
    }

    public static void loadPdfFromUrlSignlePage(String pdfUrl, String pdfTitle, PDFView pdfView, ProgressBar progressBar){
        String TAG = "loadPdfFromUrlSignlePage";

        StorageReference ref= FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);

        ref.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.e(TAG, "onSuccess: "+bytes.length);

                        pdfView.fromBytes(bytes)
                                .pages(0)
                                .spacing(0)
                                .swipeHorizontal(false)
                                .enableSwipe(false)
                                .onError(new OnErrorListener() {
                                    @Override
                                    public void onError(Throwable t) {
                                        progressBar.setVisibility(View.VISIBLE);
                                        Log.e(TAG, "onError: "+t.getMessage());
                                    }
                                })
                                .onPageError(new OnPageErrorListener() {
                                    @Override
                                    public void onPageError(int page, Throwable t) {
                                        progressBar.setVisibility(View.VISIBLE);
                                        Log.e(TAG, "onPageError: "+t.getMessage());

                                    }
                                })
                                .onLoad(new OnLoadCompleteListener() {
                                    @Override
                                    public void loadComplete(int nbPages) {
                                        progressBar.setVisibility(View.GONE);
                                        Log.e(TAG, "loadComplete: "+nbPages);
                                    }
                                })
                                .load();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.VISIBLE);
                        Log.e(TAG, "onFailure: "+e.getMessage());

                    }
                });
    }

    public static void loadCategory(String categoryId, TextView categoryTv) {


        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Categories");
        ref.child(categoryId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String category= ""+snapshot.child("category").getValue();
                        categoryTv.setText(category);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public static void voidViewCount(String bookId){
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String views= ""+snapshot.child("viewCount").getValue();
                        if (views.equals("") ||views.equals("null")){
                            views= "0";
                        }

                        long newViews= Long.parseLong(views)+1;

                        HashMap<String, Object> hashMap= new HashMap<>();
                        hashMap.put("viewCount", ""+newViews);

                        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Books");
                        ref.child(bookId)
                                .updateChildren(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.e("TAG", "onSuccess: ");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e("TAG", "onFailure: "+e.getMessage());
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

//    public static void dowloadBook(Context context, String bookId, String bookUrl, String bookTitle) {
//        String TAG = "dowloadBook";
//
//        String nameWithEx= ""+bookTitle+".pdf";
//
//        ProgressDialog progressDialog= new ProgressDialog(context);
//        progressDialog.setTitle("Please wait");
//        progressDialog.setMessage("Downloading...");
//        progressDialog.setCanceledOnTouchOutside(false);
//        progressDialog.show();
//
//        StorageReference ref= FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
//        ref.getBytes(MAX_BYTES_PDF)
//                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
//                    @Override
//                    public void onSuccess(byte[] bytes) {
//                        progressDialog.dismiss();
//                        saveDowloadBook(context, progressDialog, bytes, nameWithEx, bookId);
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//
//                        progressDialog.dismiss();
//
//                    }
//                });
//    }
//
//    private static void saveDowloadBook(Context context, ProgressDialog progressDialog, byte[] bytes, String nameWithEx, String bookId) {
//        try {
//            File dowloadFolder= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
//            dowloadFolder.mkdirs();
//
//            String filePath = dowloadFolder.getPath() + "/" + nameWithEx;
//
//            FileOutputStream out = new FileOutputStream(filePath);
//            out.write(bytes);
//            out.close();
//
//            progressDialog.dismiss();
//
//            incrementBookDowloadCount(bookId);
//
//        }catch (Exception e){
//            progressDialog.dismiss();
//            Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    private static void incrementBookDowloadCount(String bookId) {
//        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Books");
//        ref.child(bookId)
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        String downloadCount = ""+snapshot.child("downloadCount").getValue();
//
//                        if (downloadCount.equals("") || downloadCount.equals("null")){
//                            downloadCount= "0";
//                        }
//
//                        long newViewCount = Long.parseLong(downloadCount)+1;
//
//                        HashMap<String, Object> hashMap= new HashMap<>();
//                        hashMap.put("downloadCount", ""+newViewCount);
//
//                        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Books");
//                        ref.child(bookId).updateChildren(hashMap)
//                                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                    @Override
//                                    public void onSuccess(Void unused) {
//
//                                    }
//                                })
//                                .addOnFailureListener(new OnFailureListener() {
//                                    @Override
//                                    public void onFailure(@NonNull Exception e) {
//
//                                    }
//                                });
//
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });
//    }

    public static void dowloadBook(Context context, String bookId, String bookTitle, String bookUrl) {
        String TAG = "dowloadBook";

        String nameWithEx = bookTitle + ".pdf";

        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Downloading...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        ref.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        saveDowloadBook(context, progressDialog, bytes, nameWithEx, bookId, bookTitle);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.e(TAG, "Download failed: " + e.getMessage());
                    }
                });
    }

    private static void saveDowloadBook(Context context, ProgressDialog progressDialog, byte[] bytes, String nameWithEx, String bookId, String bookTitle) {
        try {
            File downloadFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            downloadFolder.mkdirs();

            String filePath = downloadFolder.getPath() + "/" + nameWithEx;

            FileOutputStream out = new FileOutputStream(filePath);
            out.write(bytes);
            out.close();

            progressDialog.dismiss();

            incrementBookDownloadCount(bookId);

            // Notify user about successful download
            Toast.makeText(context, "Downloaded successfully to Downloads folder", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            progressDialog.dismiss();
            Log.e(TAG, "Save download failed: " + e.getMessage());
            Toast.makeText(context, "Download failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private static void incrementBookDownloadCount(String bookId) {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String views= ""+snapshot.child("downloadCount").getValue();
                        if (views.equals("") ||views.equals("null")){
                            views= "0";
                        }

                        long newViews= Long.parseLong(views)+1;

                        HashMap<String, Object> hashMap= new HashMap<>();
                        hashMap.put("downloadCount", ""+newViews);

                        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Books");
                        ref.child(bookId)
                                .updateChildren(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.e("TAG", "onSuccess: ");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e("TAG", "onFailure: "+e.getMessage());
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}
