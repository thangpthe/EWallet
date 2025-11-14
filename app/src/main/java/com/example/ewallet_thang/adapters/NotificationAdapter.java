package com.example.ewallet_thang.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ewallet_thang.R;
import com.example.ewallet_thang.models.Notification;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private Context context;
    private List<Notification> notificationList;
    private OnNotificationClickListener listener;
    private SimpleDateFormat dateFormat;
    private NumberFormat currencyFormat;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    public NotificationAdapter(Context context, List<Notification> notificationList,
                               OnNotificationClickListener listener) {
        this.context = context;
        this.notificationList = notificationList;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        this.currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notificationList.get(position);

        holder.tvNotificationTitle.setText(notification.getTitle());

        // Parse message để lấy số tiền và ngày
        String message = notification.getMessage();

        // Set icon và màu dựa trên type
        if ("EXPENSE".equals(notification.getType()) || "Chi tiêu".equals(notification.getTitle())) {
            holder.ivNotificationIcon.setImageResource(R.drawable.ic_expense);
            holder.ivNotificationIcon.setColorFilter(context.getResources().getColor(R.color.red));

            // Hiển thị số tiền và ngày cho chi tiêu
            holder.tvAmount.setTextColor(context.getResources().getColor(R.color.red));
            holder.layoutExpense.setVisibility(View.VISIBLE);
            holder.layoutGoal.setVisibility(View.GONE);
            holder.layoutIncome.setVisibility(View.GONE);
            holder.tvGoalDescription.setVisibility(View.GONE);
            holder.tvAmount.setText(message);
            // Ẩn tvAmount nếu không có message
            holder.tvAmount.setVisibility(message.isEmpty() ? View.GONE : View.VISIBLE);

        } else if ("INCOME".equals(notification.getType()) || "Thu nhập".equals(notification.getTitle())) {
            holder.ivNotificationIcon.setImageResource(R.drawable.ic_income);
            holder.ivNotificationIcon.setColorFilter(context.getResources().getColor(R.color.green));


            holder.layoutExpense.setVisibility(View.GONE);
            holder.layoutGoal.setVisibility(View.GONE);
            holder.layoutIncome.setVisibility(View.VISIBLE);
            holder.tvGoalDescription.setVisibility(View.GONE);
            holder.tvIncomeAmount.setText(message);

        } else if ("GOAL".equals(notification.getType()) || "Mục tiêu tiết kiệm".equals(notification.getTitle())) {
            holder.ivNotificationIcon.setImageResource(R.drawable.ic_goal);
            holder.ivNotificationIcon.setColorFilter(context.getResources().getColor(R.color.green));
            holder.tvGoalDescription.setText(message);

            // SỬA LẠI:
            holder.layoutExpense.setVisibility(View.GONE);
            holder.layoutGoal.setVisibility(View.VISIBLE); // Hiện
            holder.layoutIncome.setVisibility(View.GONE);
            holder.tvGoalDescription.setVisibility(View.VISIBLE);
        } else {
            holder.ivNotificationIcon.setImageResource(R.drawable.ic_notification);
            holder.ivNotificationIcon.setColorFilter(context.getResources().getColor(R.color.primary));
            holder.layoutExpense.setVisibility(View.GONE);
            holder.layoutGoal.setVisibility(View.GONE);
            holder.layoutIncome.setVisibility(View.GONE);

            // Hiển thị nội dung message (ví dụ: "Cảm ơn bạn đã đăng ký...")
            // Tận dụng tvGoalDescription để hiển thị
            holder.tvGoalDescription.setVisibility(View.VISIBLE);
            holder.tvGoalDescription.setText(message);
        }

        // Format date
        try {
            SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = dbFormat.parse(notification.getDate());
            if (date != null) {
                holder.tvDate.setText(dateFormat.format(date));
            }
        } catch (Exception e) {
            holder.tvDate.setText(notification.getDate());
        }

        // Highlight nếu chưa đọc
        if (!notification.isRead()) {
            holder.cardNotification.setCardBackgroundColor(context.getResources().getColor(R.color.unread_bg));
        } else {
            holder.cardNotification.setCardBackgroundColor(context.getResources().getColor(android.R.color.white));
        }

        holder.cardNotification.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationClick(notification);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        CardView cardNotification;
        ImageView ivNotificationIcon;
        TextView tvNotificationTitle, tvAmount, tvDate;
        LinearLayout layoutGoal, layoutIncome,layoutExpense;
        TextView tvGoalDescription, tvIncomeAmount;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            cardNotification = itemView.findViewById(R.id.cardNotification);
            ivNotificationIcon = itemView.findViewById(R.id.ivNotificationIcon);
            tvNotificationTitle = itemView.findViewById(R.id.tvNotificationTitle);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvDate = itemView.findViewById(R.id.tvDate);
            layoutGoal = itemView.findViewById(R.id.layoutGoal);
            layoutIncome = itemView.findViewById(R.id.layoutIncome);
            layoutExpense = itemView.findViewById(R.id.layoutExpense);
            tvGoalDescription = itemView.findViewById(R.id.tvGoalDescription);
            tvIncomeAmount = itemView.findViewById(R.id.tvIncomeAmount);
        }
    }
}