package com.example.android.interaktivereinkaufszettel.Geldmanagment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.example.android.interaktivereinkaufszettel.R;
import com.example.android.interaktivereinkaufszettel.ModelsAndAdapters.Category;
import com.example.android.interaktivereinkaufszettel.Dialogs.NewRechnungDialog;
import com.example.android.interaktivereinkaufszettel.ModelsAndAdapters.Rechnung;
import com.example.android.interaktivereinkaufszettel.ModelsAndAdapters.RechnungAdapter;
import com.example.android.interaktivereinkaufszettel.Utility.CalculateGeldmanagmentAndSetMenu;
import com.example.android.interaktivereinkaufszettel.Utility.CustomGlobalContext;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlaceholderFragment extends Fragment {

    private CollectionReference collectionIndividualBillReference;
    private RecyclerView recyclerViewRechnung;
    private RechnungAdapter adapterRechnung;
    private Category categoryOfAdapter;
    private long categoryType;
    private CustomGlobalContext cgc;

    public static PlaceholderFragment newInstance(Category category) {
        PlaceholderFragment f = new PlaceholderFragment();
        f.setCategory(category);
        return f;
    }

    public List<Rechnung> getAdapterData(){
        return adapterRechnung.getSnapshots();
    }
    public Category getCategory() {
        return categoryOfAdapter;
    }
    private void setCategory(Category category) {
        this.categoryOfAdapter = category;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        collectionIndividualBillReference = FirebaseFirestore.getInstance().collection(categoryOfAdapter.gibId());
        categoryType = categoryOfAdapter.gibType();
        cgc = CustomGlobalContext.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_geldmanagment, container, false);

        final TextView typeView = root.findViewById(R.id.solo_or_grouplist);
        if (categoryType == Category.CATEGORY_SOLO_LIST)
            typeView.setText("Privatliste");
        else
            typeView.setText("Gruppenliste");

        FirestoreRecyclerOptions<Rechnung> optionsRechnung = new FirestoreRecyclerOptions.Builder<Rechnung>()
                .setQuery(collectionIndividualBillReference.orderBy(Rechnung.DATUM, Query.Direction.DESCENDING), Rechnung.class)
                .build();

        recyclerViewRechnung = root.findViewById(R.id.recycler_view_rechnungen);
        recyclerViewRechnung.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewRechnung.setNestedScrollingEnabled(false);
        adapterRechnung = new RechnungAdapter(optionsRechnung, categoryType);
        final PlaceholderFragment placeholderFragment = this;
        adapterRechnung.setOnLongItemClickListener(new RechnungAdapter.OnLongItemClickListener() {
            @Override
            public void onLongItemClick(Rechnung rechnung) {
                NewRechnungDialog rechnungDialog = NewRechnungDialog.newUpdateInstance(rechnung, categoryOfAdapter, new NewRechnungDialog.OnDialogFinishedListener() {
                    @Override
                    public void onDialogFinished() {
                        new CalculateGeldmanagmentAndSetMenu(cgc.getCurrentNutzer(), cgc.getGeldmanagmentMenu(), categoryOfAdapter, placeholderFragment);
                    }
                });
                rechnungDialog.show(getActivity().getSupportFragmentManager(), "RechnungDialog");
            }
        });
        recyclerViewRechnung.setAdapter(adapterRechnung);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        adapterRechnung.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapterRechnung.stopListening();
    }
}