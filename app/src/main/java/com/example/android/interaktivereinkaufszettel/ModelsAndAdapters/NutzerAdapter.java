package com.example.android.interaktivereinkaufszettel.ModelsAndAdapters;

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

public class NutzerAdapter extends FirestoreRecyclerAdapter<Nutzer, RecyclerView.ViewHolder> {
    private NutzerAdapter.OnItemClickListener listener;
    private final String TAG = "NutzerAdapterDebug";
    public NutzerAdapter(@NonNull FirestoreRecyclerOptions<Nutzer> options) {
        super(options);
    }

    @NonNull
    @Override
    public NutzerAdapter.NutzerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.nutzer_item, parent, false);
        Log.d(TAG, "onCreateViewHolder: ");
        return new NutzerAdapter.NutzerHolder(itemView);
    }

    @Override
    protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull Nutzer nutzer) {
        ((NutzerHolder) holder).nutzerView.setText(nutzer.gibName());
    }

    class NutzerHolder extends RecyclerView.ViewHolder {
        private TextView nutzerView;

        public NutzerHolder(View itemView) {
            super(itemView);
            nutzerView = itemView.findViewById(R.id.nutzer);

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
        void onItemClick(Nutzer nutzer);
    }

    public void setOnItemClickListener(NutzerAdapter.OnItemClickListener listener) {
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
