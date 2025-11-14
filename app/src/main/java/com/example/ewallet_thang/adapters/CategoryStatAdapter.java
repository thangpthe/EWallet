package com.example.ewallet_thang.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ewallet_thang.R;
import com.example.ewallet_thang.models.CategoryStat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CategoryStatAdapter extends RecyclerView.Adapter<CategoryStatAdapter.ViewHolder> {

    private Context context;
    private List<CategoryStat> categoryStatList;
    private NumberFormat currencyFormat;

    public CategoryStatAdapter(Context context, List<CategoryStat> categoryStatList) {
        this.context = context;
        this.categoryStatList = categoryStatList;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category_stat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryStat stat = categoryStatList.get(position);

        holder.tvCategory.setText(stat.getCategory());
        holder.tvAmount.setText(currencyFormat.format(stat.getAmount()));
        holder.tvPercentage.setText(String.format("%.1f%%", stat.getPercentage()));
        holder.progressBar.setProgress((int) stat.getPercentage());
    }

    @Override
    public int getItemCount() {
        return categoryStatList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvAmount, tvPercentage;
        ProgressBar progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvPercentage = itemView.findViewById(R.id.tvPercentage);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}