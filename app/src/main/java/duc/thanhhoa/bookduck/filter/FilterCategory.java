package duc.thanhhoa.bookduck.filter;

import android.widget.Filter;

import java.util.ArrayList;

import duc.thanhhoa.bookduck.adapter.AdapterCategory;
import duc.thanhhoa.bookduck.model.ModelCategory;

public class FilterCategory extends Filter {
    ArrayList<ModelCategory> categoryList;

    AdapterCategory adapterCategory;

    public FilterCategory(ArrayList<ModelCategory> categoryList, AdapterCategory adapterCategory) {
        this.categoryList = categoryList;
        this.adapterCategory = adapterCategory;
    }

    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
        FilterResults filterResults= new FilterResults();

        if(charSequence != null && charSequence.length() > 0){
            charSequence = charSequence.toString().toUpperCase();

            ArrayList<ModelCategory> filteredModels = new ArrayList<>();
            for (int i=0; i<categoryList.size(); i++){
                if(categoryList.get(i).getCategory().toUpperCase().contains(charSequence)){
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

        adapterCategory.categoryList= (ArrayList<ModelCategory>) filterResults.values;

        adapterCategory.notifyDataSetChanged();

    }
}
