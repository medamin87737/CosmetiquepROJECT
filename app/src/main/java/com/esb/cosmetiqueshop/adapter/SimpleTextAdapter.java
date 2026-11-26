package com.esb.cosmetiqueshop.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.esb.cosmetiqueshop.R;

import java.util.List;
import java.util.function.Function;

public class SimpleTextAdapter<T> extends RecyclerView.Adapter<SimpleTextAdapter.Holder> {

    public interface Click<T> {
        void onClick(T item);
    }

    private final List<T> items;
    private final Function<T, String> labelFn;
    private final Click<T> click;

    public SimpleTextAdapter(List<T> items, Function<T, String> labelFn, Click<T> click) {
        this.items = items;
        this.labelFn = labelFn;
        this.click = click;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_bubble, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        T item = items.get(position);
        holder.txtBubble.setText(labelFn.apply(item));
        holder.itemView.setOnClickListener(v -> click.onClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView txtBubble;

        Holder(@NonNull View itemView) {
            super(itemView);
            txtBubble = itemView.findViewById(R.id.txtBubble);
        }
    }
}