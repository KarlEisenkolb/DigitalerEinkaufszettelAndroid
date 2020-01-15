package com.example.android.interaktivereinkaufszettel.geldmanagment.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.interaktivereinkaufszettel.R;
import com.example.android.interaktivereinkaufszettel.geldmanagment.CalculateGeldmanagmentAndSetMenu;
import com.example.android.interaktivereinkaufszettel.geldmanagment.Category;
import com.example.android.interaktivereinkaufszettel.geldmanagment.NewRechnungDialog;
import com.example.android.interaktivereinkaufszettel.geldmanagment.Rechnung;
import com.example.android.interaktivereinkaufszettel.geldmanagment.RechnungAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import static com.example.android.interaktivereinkaufszettel.geldmanagment.Geldmanagment.currentNutzer;
import static com.example.android.interaktivereinkaufszettel.geldmanagment.Geldmanagment.viewPager;
import static com.example.android.interaktivereinkaufszettel.geldmanagment.Geldmanagment.menu;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlaceholderFragment extends Fragment {

    private CollectionReference collectionIndividualBillReference;
    private long categoryType;
    private RecyclerView recyclerViewRechnung;
    private RechnungAdapter adapterRechnung;
    private SectionsPagerAdapter sectionsPagerAdapter;

    public static PlaceholderFragment newInstance(Category category, SectionsPagerAdapter sectionsPagerAdapter) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        fragment.setSectionsPagerAdapter(sectionsPagerAdapter);
        Bundle bundle = new Bundle();
        bundle.putString(Category.ID, category.gibId());
        bundle.putString(Category.NAME, category.gibName());
        bundle.putLong(Category.TYPE, category.gibType());
        fragment.setArguments(bundle);
        return fragment;
    }

    private void setSectionsPagerAdapter(SectionsPagerAdapter sectionsPagerAdapter){
        this.sectionsPagerAdapter = sectionsPagerAdapter;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryType = getArguments().getLong(Category.TYPE);
            String categoryID = getArguments().getString(Category.ID);
            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            collectionIndividualBillReference = firebaseFirestore.collection(categoryID);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_geldmanagment, container, false);

        TextView typeView = root.findViewById(R.id.solo_or_grouplist);
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
        adapterRechnung = new RechnungAdapter(optionsRechnung);
        adapterRechnung.setOnLongItemClickListener(new RechnungAdapter.OnLongItemClickListener() {
            @Override
            public void onLongItemClick(Rechnung rechnung) {
                NewRechnungDialog rechnungDialog = NewRechnungDialog.newUpdateInstance(rechnung, collectionIndividualBillReference.getId(), new NewRechnungDialog.OnDialogFinishedListener() {
                    @Override
                    public void onDialogFinished() {
                        new CalculateGeldmanagmentAndSetMenu(currentNutzer, menu, sectionsPagerAdapter.getItem(viewPager.getCurrentItem()).getArguments());
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