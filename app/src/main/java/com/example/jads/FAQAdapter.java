package com.example.jads;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FAQAdapter extends RecyclerView.Adapter<FAQAdapter.FAQViewHolder> {

    private List<FAQItem> faqList;

    public FAQAdapter(List<FAQItem> faqList) {
        this.faqList = faqList;
    }

    // Method to update the adapter's data
    public void setData(List<FAQItem> newFaqList) {
        this.faqList = newFaqList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FAQViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for individual FAQ items
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_faqitem, parent, false);
        return new FAQViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FAQViewHolder holder, int position) {
        FAQItem faq = faqList.get(position);

        // Set question and answer text
        holder.questionTextView.setText(faq.getQuestion());
        holder.answerTextView.setText(faq.getAnswer());

        // Toggle visibility of the answer when the question is clicked
        holder.questionTextView.setOnClickListener(v -> {
            if (holder.answerTextView.getVisibility() == View.GONE) {
                holder.answerTextView.setVisibility(View.VISIBLE);
            } else {
                holder.answerTextView.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public int getItemCount() {
        return faqList.size();
    }

    static class FAQViewHolder extends RecyclerView.ViewHolder {
        TextView questionTextView, answerTextView;

        public FAQViewHolder(@NonNull View itemView) {
            super(itemView);

            // Bind the UI components
            questionTextView = itemView.findViewById(R.id.questionTextView);
            answerTextView = itemView.findViewById(R.id.answerTextView);
        }
    }
}
