package com.esb.cosmetiqueshop.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.esb.cosmetiqueshop.R;
import com.esb.cosmetiqueshop.model.Conversation;
import com.esb.cosmetiqueshop.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

/** Cartes messages admin : objet, client, aperçu. */
public class AdminMessageAdapter extends RecyclerView.Adapter<AdminMessageAdapter.Holder> {

    public interface Listener {
        void onClick(Conversation conversation);
    }

    private final List<Conversation> items = new ArrayList<>();
    private final Listener listener;

    public AdminMessageAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<Conversation> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_message, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Conversation c = items.get(position);
        String subject = !TextUtils.isEmpty(c.getSubject())
                ? c.getSubject()
                : holder.itemView.getContext().getString(R.string.messages_title) + " #" + c.getId();
        holder.txtSubject.setText(subject);

        String client = !TextUtils.isEmpty(c.getClientName())
                ? c.getClientName()
                : ("#" + c.getClientUserId());
        holder.txtClient.setText(holder.itemView.getContext()
                .getString(R.string.message_client_label, client));

        if (!TextUtils.isEmpty(c.getClientEmail())) {
            holder.txtEmail.setVisibility(View.VISIBLE);
            holder.txtEmail.setText(c.getClientEmail());
        } else {
            holder.txtEmail.setVisibility(View.GONE);
        }

        String preview = c.getLastMessagePreview();
        if (TextUtils.isEmpty(preview)) {
            holder.txtPreview.setText(R.string.message_no_preview);
        } else {
            holder.txtPreview.setText(preview);
        }

        holder.txtDate.setText(DateUtils.formatDisplay(c.getUpdatedAt()));

        if (c.getUnreadCount() > 0) {
            holder.txtUnread.setVisibility(View.VISIBLE);
            holder.txtUnread.setText(holder.itemView.getContext()
                    .getString(R.string.conversation_unread, c.getUnreadCount()));
        } else {
            holder.txtUnread.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(c);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView txtSubject;
        final TextView txtClient;
        final TextView txtEmail;
        final TextView txtPreview;
        final TextView txtDate;
        final TextView txtUnread;

        Holder(@NonNull View itemView) {
            super(itemView);
            txtSubject = itemView.findViewById(R.id.txtAdminMsgSubject);
            txtClient = itemView.findViewById(R.id.txtAdminMsgClient);
            txtEmail = itemView.findViewById(R.id.txtAdminMsgEmail);
            txtPreview = itemView.findViewById(R.id.txtAdminMsgPreview);
            txtDate = itemView.findViewById(R.id.txtAdminMsgDate);
            txtUnread = itemView.findViewById(R.id.txtAdminMsgUnread);
        }
    }
}
