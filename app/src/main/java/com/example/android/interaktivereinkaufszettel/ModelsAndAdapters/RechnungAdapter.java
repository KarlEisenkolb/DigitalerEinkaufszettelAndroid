package com.example.android.interaktivereinkaufszettel.ModelsAndAdapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.interaktivereinkaufszettel.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.text.SimpleDateFormat;

public class RechnungAdapter extends FirestoreRecyclerAdapter<Rechnung, RecyclerView.ViewHolder> {
    final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
    private RechnungAdapter.OnLongItemClickListener listener;

    public RechnungAdapter(@NonNull FirestoreRecyclerOptions<Rechnung> options) { super(options); }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == Rechnung.RECHNUNG_GEKAUFT || viewType == Rechnung.RECHNUNG_GEPLANT) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.rechnung_item, parent, false);
            return new RechnungHolder(itemView);
        }else{
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.rechnung_zahlung, parent, false);
            return new ZahlungHolder(itemView);
        }

    }

    @Override
    protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull Rechnung rechnung) {
        if (holder instanceof RechnungAdapter.RechnungHolder) { // Rechnung
            ((RechnungHolder) holder).rechnung_name.setText(rechnung.gibContent());
            ((RechnungHolder) holder).rechnung_nutzer.setText(rechnung.gibKauefer());
            ((RechnungHolder) holder).rechnung_preis.setText(rechnung.gibPreis() + " €");
            if (rechnung.gibType() == Rechnung.RECHNUNG_GEKAUFT)
                ((RechnungHolder) holder).rechnung_datum.setText(simpleDateFormat.format(rechnung.gibDatum()));
            else
                ((RechnungHolder) holder).rechnung_datum.setText("geplant");
        }else{ // Zahlung
            ((ZahlungHolder) holder).zahlung_nutzer.setText(rechnung.gibKauefer());
            ((ZahlungHolder) holder).zahlung_preis.setText(rechnung.gibPreis() + " €");
            ((ZahlungHolder) holder).zahlung_name.setText(rechnung.gibContent());
            ((ZahlungHolder) holder).zahlung_datum.setText(simpleDateFormat.format(rechnung.gibDatum()));
            ((ZahlungHolder) holder).zahlung_color.setBackgroundColor(Color.parseColor("#F5F5F5"));
        }
    }

    class RechnungHolder extends RecyclerView.ViewHolder {
        private TextView rechnung_name;
        private TextView rechnung_nutzer;
        private TextView rechnung_preis;
        private TextView rechnung_datum;
        private LinearLayout rechnung_color;

        public RechnungHolder(View itemView) {
            super(itemView);
            rechnung_name = itemView.findViewById(R.id.rechnung_name);
            rechnung_nutzer = itemView.findViewById(R.id.rechnung_nutzer);
            rechnung_preis = itemView.findViewById(R.id.rechnung_preis);
            rechnung_datum = itemView.findViewById(R.id.rechnung_datum);
            rechnung_color = itemView.findViewById(R.id.background_color_rechnung_item);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = getAdapterPosition();
                    if (listener != null && position != RecyclerView.NO_POSITION) {
                        listener.onLongItemClick(getItem(position));
                    }
                    return true;
                }
            });
        }
    }

    class ZahlungHolder extends RecyclerView.ViewHolder {
        private TextView zahlung_nutzer;
        private TextView zahlung_preis;
        private TextView zahlung_name;
        private TextView zahlung_datum;
        private LinearLayout zahlung_color;

        public ZahlungHolder(View itemView) {
            super(itemView);
            zahlung_nutzer = itemView.findViewById(R.id.zahlung_nutzer);
            zahlung_preis = itemView.findViewById(R.id.zahlung_preis);
            zahlung_name = itemView.findViewById(R.id.zahlung_name);
            zahlung_datum = itemView.findViewById(R.id.zahlung_datum);
            zahlung_color = itemView.findViewById(R.id.background_color_zahlung_item);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = getAdapterPosition();
                    if (listener != null && position != RecyclerView.NO_POSITION) {
                        listener.onLongItemClick(getItem(position));
                    }
                    return true;
                }
            });
        }
    }

    public interface OnLongItemClickListener {
        void onLongItemClick(Rechnung rechnung);
    }

    public void setOnLongItemClickListener(RechnungAdapter.OnLongItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return (int) getSnapshots().get(position).gibType();
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
