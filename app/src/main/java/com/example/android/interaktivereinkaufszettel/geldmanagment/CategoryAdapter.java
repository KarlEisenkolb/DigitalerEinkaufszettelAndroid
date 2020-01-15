package com.example.android.interaktivereinkaufszettel.geldmanagment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.interaktivereinkaufszettel.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class CategoryAdapter extends FirestoreRecyclerAdapter<Category, RecyclerView.ViewHolder> {
    private CategoryAdapter.OnItemClickListener listener;
    private final String TAG = "CategoryAdapterDebug";
    public CategoryAdapter(@NonNull FirestoreRecyclerOptions<Category> options) { super(options);
        Log.d(TAG, "CategoryAdapter: ");}

    @NonNull
    @Override
    public CategoryAdapter.CategoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.nutzer_item, parent, false);
        Log.d(TAG, "onCreateViewHolder: ");
        return new CategoryAdapter.CategoryHolder(itemView);
    }

    @Override
    protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull Category category) {
        ((CategoryHolder) holder).categoryNameView.setText(category.gibName());
    }

    class CategoryHolder extends RecyclerView.ViewHolder {
        private TextView categoryNameView;

        public CategoryHolder(View itemView) {
            super(itemView);
            categoryNameView = itemView.findViewById(R.id.nutzer);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (listener != null && position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(getItem(position));
                    }
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Category category);
    }

    public void setOnItemClickListener(CategoryAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

}
