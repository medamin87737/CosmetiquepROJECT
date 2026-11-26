package com.esb.cosmetiqueshop.adapter;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.esb.cosmetiqueshop.R;
import com.esb.cosmetiqueshop.model.Conversation;
import com.esb.cosmetiqueshop.utils.DateUtils;

import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.Holder> {

    public interface Listener {
        void onClick(Conversation conversation);
    }

    private final List<Conversation> items;
    private final Listener listener;

    public ConversationAdapter(List<Conversation> items, Listener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conversation, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Conversation c = items.get(position);
        boolean unread = c.getUnreadCount() > 0;
        String subject = c.getSubject() != null && !c.getSubject().isEmpty()
                ? c.getSubject()
                : holder.itemView.getContext().getString(R.string.messages_title)
                + " #" + c.getId();
        holder.txtSubject.setText(subject);
        holder.txtMeta.setText(DateUtils.formatDisplay(c.getUpdatedAt()));

        int titleColor = ContextCompat.getColor(holder.itemView.getContext(),
                unread ? R.color.bordeaux : R.color.bordeaux_fonce);
        int secondary = ContextCompat.getColor(holder.itemView.getContext(), R.color.texte_secondaire);
        holder.txtSubject.setTextColor(titleColor);
        holder.txtSubject.setTypeface(null, unread ? Typeface.BOLD : Typeface.NORMAL);
        holder.txtMeta.setTextColor(secondary);

        if (unread) {
            holder.txtUnread.setVisibility(View.VISIBLE);
            holder.txtUnread.setText(holder.itemView.getContext()
                    .getString(R.string.conversation_unread, c.getUnreadCount()));
        } else {
            holder.txtUnread.setVisibility(View.GONE);
        }

        holder.imgAvatar.setImageResource(R.drawable.ic_message);
        holder.itemView.setOnClickListener(v -> listener.onClick(c));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ImageView imgAvatar;
        final TextView txtSubject;
        final TextView txtMeta;
        final TextView txtUnread;

        Holder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgConversationAvatar);
            txtSubject = itemView.findViewById(R.id.txtConversationSubject);
            txtMeta = itemView.findViewById(R.id.txtConversationMeta);
            txtUnread = itemView.findViewById(R.id.txtUnreadBadge);
        }
    }
}
