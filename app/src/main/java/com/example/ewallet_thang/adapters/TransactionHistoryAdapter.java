package com.example.ewallet_thang.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ewallet_thang.R;
import com.example.ewallet_thang.models.Transaction;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionHistoryAdapter extends RecyclerView.Adapter<TransactionHistoryAdapter.TransactionViewHolder> {

    private Context context;
    private List<Transaction> transactionList;
    private NumberFormat currencyFormat;
    private SimpleDateFormat inputDateFormat;
    private SimpleDateFormat outputDateFormat;
    private SimpleDateFormat outputTimeFormat;

    public TransactionHistoryAdapter(Context context, List<Transaction> transactionList) {
        this.context = context;
        this.transactionList = transactionList;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        this.inputDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        this.outputDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        this.outputTimeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction_history, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);

        // Set icon and background based on transaction type
        String type = transaction.getTransactionType();
        if (type.equals("INCOME") || type.equals("DEPOSIT")) {
            holder.ivIcon.setImageResource(R.drawable.ic_income);
            holder.ivIcon.setBackgroundResource(R.drawable.circle_receive);
            holder.tvAmount.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
            holder.tvAmount.setText("+" + currencyFormat.format(transaction.getAmount()));
        } else if (type.equals("EXPENSE") || type.equals("WITHDRAW")) {
            holder.ivIcon.setImageResource(R.drawable.ic_expense);
            holder.ivIcon.setBackgroundResource(R.drawable.circle_withdraw);
            holder.tvAmount.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
            holder.tvAmount.setText("-" + currencyFormat.format(transaction.getAmount()));
        } else if (type.equals("TRANSFER")) {
            holder.ivIcon.setImageResource(R.drawable.ic_transfer);
            holder.ivIcon.setBackgroundResource(R.drawable.circle_send);
            holder.tvAmount.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
            holder.tvAmount.setText("-" + currencyFormat.format(transaction.getAmount()));
        }

        // Set description
        String description = transaction.getDescription();
        if (description == null || description.isEmpty()) {
            description = transaction.getCategory();
        }
        holder.tvDescription.setText(description);

        // Set category
        String category = transaction.getCategory();
        if (category != null && !category.isEmpty()) {
            holder.tvCategory.setText(category);
            holder.tvCategory.setVisibility(View.VISIBLE);
        } else {
            holder.tvCategory.setVisibility(View.GONE);
        }

        // Format and set date/time
        try {
            Date date = inputDateFormat.parse(transaction.getTransactionDate());
            if (date != null) {
                holder.tvDate.setText(outputDateFormat.format(date));
                holder.tvTime.setText(outputTimeFormat.format(date));
            }
        } catch (ParseException e) {
            holder.tvDate.setText(transaction.getTransactionDate());
            holder.tvTime.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvDescription, tvCategory, tvDate, tvTime, tvAmount;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivTransactionIcon);
            tvDescription = itemView.findViewById(R.id.tvTransactionDescription);
            tvCategory = itemView.findViewById(R.id.tvTransactionCategory);
            tvDate = itemView.findViewById(R.id.tvTransactionDate);
            tvTime = itemView.findViewById(R.id.tvTransactionTime);
            tvAmount = itemView.findViewById(R.id.tvTransactionAmount);
        }
    }
}