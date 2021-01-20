package com.example.android.interaktivereinkaufszettel.models_and_adapters;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.interaktivereinkaufszettel.R;
import com.example.android.interaktivereinkaufszettel.utility.CalculateGeldmanagmentAndSetMenu;
import com.example.android.interaktivereinkaufszettel.utility.CustomGlobalContext;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.text.SimpleDateFormat;

public class RechnungAdapter extends FirestoreRecyclerAdapter<Rechnung, RecyclerView.ViewHolder> {
    final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
    private RechnungAdapter.OnLongItemClickListener listener;
    private CustomGlobalContext customGlobalContext;
    private long categoryType;

    public RechnungAdapter(@NonNull FirestoreRecyclerOptions<Rechnung> options, long categoryType) {
        super(options);
        customGlobalContext = CustomGlobalContext.getInstance();
        this.categoryType = categoryType;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == Rechnung.RECHNUNG_GEKAUFT || viewType == Rechnung.RECHNUNG_GEPLANT) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.rechnung_item, parent, false);
            return new RechnungHolder(itemView);
        }else if(viewType == Rechnung.RECHNUNG_ZAHLUNG){
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.rechnung_zahlung, parent, false);
            return new ZahlungHolder(itemView);
        }else{
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.rechnung_month_summary, parent, false);
            return new MonthSummaryHolder(itemView);
        }

    }

    @Override
    protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull Rechnung rechnung) {
        String currentNutzer = customGlobalContext.getCurrentNutzer();
        if (holder instanceof RechnungAdapter.RechnungHolder) { // Rechnung
            fillRechnungsHolder((RechnungHolder) holder, rechnung, currentNutzer);
        }else if(holder instanceof RechnungAdapter.ZahlungHolder){ // Zahlung
            fillZahlungHolder((ZahlungHolder) holder, rechnung);
        }else
            fillMonthSummary((MonthSummaryHolder) holder, rechnung);
    }

    private void fillMonthSummary(MonthSummaryHolder holder, Rechnung rechnung) {
        SimpleDateFormat monthFormatter = new SimpleDateFormat("MMMM yyyy");
        holder.month_date.setText(monthFormatter.format(rechnung.gibDatum()));
        holder.month_summary.setText(" | " + rechnung.gibPreis() + " €");
    }

    private void fillZahlungHolder(@NonNull ZahlungHolder holder, @NonNull Rechnung rechnung) {
        holder.zahlung_nutzer.setText(rechnung.gibKauefer());
        holder.zahlung_preis.setText(rechnung.gibPreis() + " €");
        holder.zahlung_name.setText(rechnung.gibContent());
        holder.zahlung_datum.setText(simpleDateFormat.format(rechnung.gibDatum()));
        holder.zahlung_color.setBackgroundColor(Color.parseColor("#F5F5F5"));
    }

    private void fillRechnungsHolder(@NonNull RechnungHolder holder, @NonNull Rechnung rechnung, String currentNutzer) {
        holder.rechnung_name.setText(rechnung.gibContent());
        Double nutzerAnteil = CalculateGeldmanagmentAndSetMenu.extractNutzerAnteil(rechnung, currentNutzer);
        holder.rechnung_nutzer.setText(rechnung.gibKauefer());

        if (categoryType == Category.CATEGORY_GROUP_LIST) {
            SpannableString coloredString;
            if (rechnung.gibKauefer().equals(currentNutzer)) { // Rot wenn Kontostand weniger als -1 Cent
                coloredString = new SpannableString(Math.round(rechnung.gibPreis() * (1 - nutzerAnteil) * 100) / 100.0 + " €");
                coloredString.setSpan(new ForegroundColorSpan(Color.rgb(34, 128, 34)), 0, coloredString.length(), 0);
            } else {
                coloredString = new SpannableString(Math.round(rechnung.gibPreis() * nutzerAnteil * 100) / 100.0 + " €");
                coloredString.setSpan(new ForegroundColorSpan(Color.RED), 0, coloredString.length(), 0);
            }
            holder.rechnung_preis.setText(rechnung.gibPreis() + " €   (" + Math.round(nutzerAnteil * 10000)/100.0 + "%) ");
            holder.rechnung_preis_anteil.setText(coloredString);
        }else {
            holder.rechnung_preis_anteil.setText("");
            holder.rechnung_preis.setText(rechnung.gibPreis() + " €");
        }
        if (rechnung.gibType() == Rechnung.RECHNUNG_GEKAUFT)
            holder.rechnung_datum.setText(simpleDateFormat.format(rechnung.gibDatum()));
        else
            holder.rechnung_datum.setText("geplant");
    }

    class RechnungHolder extends RecyclerView.ViewHolder {
        private TextView rechnung_name;
        private TextView rechnung_nutzer;
        private TextView rechnung_preis;
        private TextView rechnung_preis_anteil;
        private TextView rechnung_datum;
        private LinearLayout rechnung_color;

        public RechnungHolder(View itemView) {
            super(itemView);
            rechnung_name = itemView.findViewById(R.id.rechnung_name);
            rechnung_nutzer = itemView.findViewById(R.id.rechnung_nutzer);
            rechnung_preis = itemView.findViewById(R.id.rechnung_preis);
            rechnung_preis_anteil = itemView.findViewById(R.id.rechnung_preis_anteil);
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

    class MonthSummaryHolder extends RecyclerView.ViewHolder {
        private TextView month_date;
        private TextView month_summary;

        public MonthSummaryHolder(View itemView) {
            super(itemView);
            month_date = itemView.findViewById(R.id.month_date);
            month_summary = itemView.findViewById(R.id.month_summary);

            /*itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = getAdapterPosition();
                    if (listener != null && position != RecyclerView.NO_POSITION) {
                        listener.onLongItemClick(getItem(position));
                    }
                    return true;
                }
            });*/
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
