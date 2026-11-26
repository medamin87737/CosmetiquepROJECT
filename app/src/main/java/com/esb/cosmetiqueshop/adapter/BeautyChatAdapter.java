package com.esb.cosmetiqueshop.adapter;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.esb.cosmetiqueshop.R;
import com.esb.cosmetiqueshop.model.BeautyChatMessage;
import com.esb.cosmetiqueshop.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class BeautyChatAdapter extends RecyclerView.Adapter<BeautyChatAdapter.Holder> {

    private final List<BeautyChatMessage> items = new ArrayList<>();

    public void add(BeautyChatMessage message) {
        items.add(message);
        notifyItemInserted(items.size() - 1);
    }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_beauty_chat_bubble, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        BeautyChatMessage m = items.get(position);
        holder.txtBubble.setText(m.getText());
        holder.txtTime.setText(DateUtils.formatDisplay(m.getCreatedAt()));

        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) holder.layoutBubble.getLayoutParams();
        if (m.isFromUser()) {
            lp.gravity = Gravity.END;
            holder.layoutBubble.setBackgroundResource(R.drawable.bg_bubble_mine);
            holder.txtBubble.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.blanc));
            holder.txtTime.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.rose_poudre));
        } else {
            lp.gravity = Gravity.START;
            holder.layoutBubble.setBackgroundResource(R.drawable.bg_bubble_other);
            holder.txtBubble.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.texte));
            holder.txtTime.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.texte_secondaire));
        }
        holder.layoutBubble.setLayoutParams(lp);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final LinearLayout layoutBubble;
        final TextView txtBubble;
        final TextView txtTime;

        Holder(@NonNull View itemView) {
            super(itemView);
            layoutBubble = itemView.findViewById(R.id.layoutBubble);
            txtBubble = itemView.findViewById(R.id.txtBubble);
            txtTime = itemView.findViewById(R.id.txtBubbleTime);
        }
    }
}
