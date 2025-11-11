package com.example.ewallet_thang.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ewallet_thang.R;
import com.example.ewallet_thang.models.Transaction;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private Context context;
    private List<Transaction> transactionList;
    private NumberFormat currencyFormat;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat timeFormat;

    public TransactionAdapter(Context context, List<Transaction> transactionList) {
        this.context = context;
        this.transactionList = transactionList;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);

        // Set transaction icon based on type
        if (transaction.isIncome()) {
            holder.tvIcon.setText("↓");
            holder.tvIcon.setBackgroundResource(R.drawable.circle_income);
            holder.tvAmount.setTextColor(context.getResources().getColor(R.color.green));
            holder.tvAmount.setText("+" + currencyFormat.format(transaction.getAmount()));
        } else {
            holder.tvIcon.setText("↑");
            holder.tvIcon.setBackgroundResource(R.drawable.circle_expense);
            holder.tvAmount.setTextColor(context.getResources().getColor(R.color.red));
            holder.tvAmount.setText("-" + currencyFormat.format(transaction.getAmount()));
        }

        // Set description and category
        String description = transaction.getDescription();
        if (description == null || description.isEmpty()) {
            description = transaction.getCategory();
        }
        holder.tvDescription.setText(description);
        holder.tvCategory.setText(transaction.getCategory());

        // Format date
        try {
            SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = dbFormat.parse(transaction.getTransactionDate());
            if (date != null) {
                holder.tvDate.setText(dateFormat.format(date));
                holder.tvTime.setText(timeFormat.format(date));
            }
        } catch (Exception e) {
            holder.tvDate.setText(transaction.getTransactionDate());
        }
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvIcon, tvDescription, tvCategory, tvAmount, tvDate, tvTime;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIcon = itemView.findViewById(R.id.tvTransactionIcon);
            tvDescription = itemView.findViewById(R.id.tvTransactionDescription);
            tvCategory = itemView.findViewById(R.id.tvTransactionCategory);
            tvAmount = itemView.findViewById(R.id.tvTransactionAmount);
            tvDate = itemView.findViewById(R.id.tvTransactionDate);
            tvTime = itemView.findViewById(R.id.tvTransactionTime);
        }
    }
}