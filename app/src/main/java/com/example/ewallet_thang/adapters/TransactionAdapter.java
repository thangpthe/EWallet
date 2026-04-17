package com.example.ewallet_thang.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ewallet_thang.R;
import com.example.ewallet_thang.models.Transaction;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private final Context context;
    private final List<Transaction> transactionList;
    private final NumberFormat currencyFormat;

    public TransactionAdapter(Context context, List<Transaction> transactionList) {
        this.context = context;
        this.transactionList = transactionList;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_recent_transaction, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction tx = transactionList.get(position);

        // Tên giao dịch
        holder.tvTransactionName.setText(
                tx.getDescription() != null && !tx.getDescription().isEmpty()
                        ? tx.getDescription()
                        : "Giao dịch");

        // Ngày + Giờ (định dạng đẹp)
        String fullDateTime = tx.getTransactionDate();
        if (fullDateTime != null && fullDateTime.length() >= 16) {
            // Giả sử định dạng là "2026-04-16 10:30:00"
            String datePart = fullDateTime.substring(8, 10) + "/" +
                    fullDateTime.substring(5, 7) + "/" +
                    fullDateTime.substring(0, 4);
            String timePart = fullDateTime.substring(11, 16);
            holder.tvDateTime.setText(datePart + " • " + timePart);
        } else {
            holder.tvDateTime.setText(fullDateTime != null ? fullDateTime : "--/--/---- • --:--");
        }

        // Số tiền + màu
        double amount = tx.getAmount();
        String type = tx.getTransactionType() != null ? tx.getTransactionType() : "";

        if (type.equalsIgnoreCase("INCOME") || type.equalsIgnoreCase("DEPOSIT")) {
            holder.tvAmount.setText("+" + currencyFormat.format(amount));
            holder.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.colorSuccess));
            holder.ivIcon.setImageResource(R.drawable.ic_income);
            holder.ivIcon.setColorFilter(ContextCompat.getColor(context, R.color.colorSuccess));
        } else {
            holder.tvAmount.setText("-" + currencyFormat.format(Math.abs(amount)));
            holder.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.colorError));
            holder.ivIcon.setImageResource(R.drawable.ic_expense);
            holder.ivIcon.setColorFilter(ContextCompat.getColor(context, R.color.colorError));
        }
    }

    @Override
    public int getItemCount() {
        return transactionList != null ? transactionList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTransactionName, tvDateTime, tvAmount;
        ImageView ivIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTransactionName = itemView.findViewById(R.id.tvTransactionName);
            tvDateTime        = itemView.findViewById(R.id.tvDateTime);
            tvAmount          = itemView.findViewById(R.id.tvAmount);
            ivIcon            = itemView.findViewById(R.id.ivIcon);
        }
    }
}