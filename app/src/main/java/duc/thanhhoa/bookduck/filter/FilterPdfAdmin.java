package duc.thanhhoa.bookduck.filter;

import android.widget.Filter;

import java.util.ArrayList;

import duc.thanhhoa.bookduck.adapter.AdapterCategory;
import duc.thanhhoa.bookduck.adapter.AdapterPdfAdmin;
import duc.thanhhoa.bookduck.model.ModelCategory;
import duc.thanhhoa.bookduck.model.ModelPdf;

public class FilterPdfAdmin extends Filter {
    ArrayList<ModelPdf> categoryList;

    AdapterPdfAdmin adapterPdfAdmin;

    public FilterPdfAdmin(ArrayList<ModelPdf> categoryList, AdapterPdfAdmin adapterPdfAdmin) {
        this.categoryList = categoryList;
        this.adapterPdfAdmin = adapterPdfAdmin;
    }

    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
        FilterResults filterResults= new FilterResults();

        if(charSequence != null && charSequence.length() > 0){
            charSequence = charSequence.toString().toUpperCase();

            ArrayList<ModelPdf> filteredModels = new ArrayList<>();
            for (int i=0; i<categoryList.size(); i++){
                if(categoryList.get(i).getTitle().toUpperCase().contains(charSequence)){
                    filteredModels.add(categoryList.get(i));
                }
            }
            filterResults.count = filteredModels.size();
            filterResults.values = filteredModels;
        }
        else {
            filterResults.count = categoryList.size();
            filterResults.values = categoryList;
        }
        return filterResults;
    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults filterResults) {

        adapterPdfAdmin.pdfList= (ArrayList<ModelPdf>) filterResults.values;

        adapterPdfAdmin.notifyDataSetChanged();

    }
}
