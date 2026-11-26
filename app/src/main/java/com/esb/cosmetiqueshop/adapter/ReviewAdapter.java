package com.esb.cosmetiqueshop.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.esb.cosmetiqueshop.R;
import com.esb.cosmetiqueshop.model.Review;
import com.esb.cosmetiqueshop.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Cartes avis professionnelles : avatar, étoiles jaunes, commentaire.
 */
public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.Holder> {

    private static final int MAX_STARS = 6;

    private final List<Review> reviews = new ArrayList<>();

    public ReviewAdapter(List<Review> reviews) {
        if (reviews != null) {
            this.reviews.addAll(reviews);
        }
    }

    public void submit(List<Review> items) {
        reviews.clear();
        if (items != null) {
            reviews.addAll(items);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Review r = reviews.get(position);
        String name = r.getUserName() != null && !r.getUserName().trim().isEmpty()
                ? r.getUserName().trim() : "Client";
        holder.txtReviewerName.setText(name);
        holder.txtAvatar.setText(String.valueOf(Character.toUpperCase(name.charAt(0))));
        holder.txtReviewDate.setText(r.getCreatedAt() > 0
                ? DateUtils.formatDisplay(r.getCreatedAt()) : "");
        String comment = r.getComment() != null ? r.getComment().trim() : "";
        holder.txtReviewComment.setText(comment);
        holder.txtReviewComment.setVisibility(comment.isEmpty() ? View.GONE : View.VISIBLE);
        bindStars(holder.layoutReviewStars, r.getRating());
    }

    private void bindStars(LinearLayout layout, int rating) {
        layout.removeAllViews();
        int safe = Math.max(0, Math.min(MAX_STARS, rating));
        int size = (int) (18 * layout.getResources().getDisplayMetrics().density);
        int gap = (int) (2 * layout.getResources().getDisplayMetrics().density);
        for (int i = 1; i <= MAX_STARS; i++) {
            ImageView star = new ImageView(layout.getContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
            if (i < MAX_STARS) {
                lp.setMarginEnd(gap);
            }
            star.setLayoutParams(lp);
            star.setImageResource(i <= safe ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);
            layout.addView(star);
        }
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView txtAvatar;
        final TextView txtReviewerName;
        final TextView txtReviewDate;
        final TextView txtReviewComment;
        final LinearLayout layoutReviewStars;

        Holder(@NonNull View itemView) {
            super(itemView);
            txtAvatar = itemView.findViewById(R.id.txtAvatar);
            txtReviewerName = itemView.findViewById(R.id.txtReviewerName);
            txtReviewDate = itemView.findViewById(R.id.txtReviewDate);
            txtReviewComment = itemView.findViewById(R.id.txtReviewComment);
            layoutReviewStars = itemView.findViewById(R.id.layoutReviewStars);
        }
    }
}
