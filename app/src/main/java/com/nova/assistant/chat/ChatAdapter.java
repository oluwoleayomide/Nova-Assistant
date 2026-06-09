package com.nova.assistant.chat;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.nova.assistant.R;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {

    private final List<ChatMessage> messages;

    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage msg = messages.get(position);
        holder.bind(msg);
    }

    @Override
    public int getItemCount() { return messages.size(); }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        LinearLayout bubbleContainer, bubble;
        ImageView    ivAvatar;
        TextView     tvMessage, tvTime;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            bubbleContainer = itemView.findViewById(R.id.bubble_container);
            bubble          = itemView.findViewById(R.id.bubble);
            ivAvatar        = itemView.findViewById(R.id.iv_avatar);
            tvMessage       = itemView.findViewById(R.id.tv_message);
            tvTime          = itemView.findViewById(R.id.tv_time);
        }

        void bind(ChatMessage msg) {
            tvMessage.setText(msg.getText());
            tvTime.setText(msg.getFormattedTime());

            if (msg.isUser()) {
                // User bubble - right aligned, cyan
                bubbleContainer.setGravity(Gravity.END);
                ivAvatar.setVisibility(View.GONE);
                bubble.setBackgroundResource(R.drawable.bg_bubble_user);
                tvMessage.setTextColor(itemView.getContext().getColor(R.color.bubble_user_text));
            } else {
                // Nova bubble - left aligned, dark card
                bubbleContainer.setGravity(Gravity.START);
                ivAvatar.setVisibility(View.VISIBLE);
                bubble.setBackgroundResource(R.drawable.bg_bubble_nova);
                tvMessage.setTextColor(itemView.getContext().getColor(R.color.bubble_nova_text));
            }
        }
    }
}
