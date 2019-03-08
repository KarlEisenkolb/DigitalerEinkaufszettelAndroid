package com.example.android.interaktivereinkaufszettel;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class NoteAdapter extends FirestoreRecyclerAdapter<Note, NoteAdapter.NoteHolder> {

    private OnItemClickListener listenerShort;
    private OnLongItemClickListener listenerLong;

    public NoteAdapter(@NonNull FirestoreRecyclerOptions<Note> options) {
        super(options);
    }

    @NonNull
    @Override
    public NoteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_item, parent, false);
        return new NoteHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteHolder holder, int position, @NonNull Note note) {
        holder.textViewContent.setText(note.getContent());

        if (note.getNoteColor() == Note.NOTE_COLOR_GREEN){
            holder.itemViewId.setBackgroundColor(Color.parseColor("#388E3C"));
            Log.d("onBind ", "Green");}
        else if (note.getNoteColor() == Note.NOTE_COLOR_YELLOW){
            holder.itemViewId.setBackgroundColor(Color.parseColor("#FF8F00"));
            Log.d("onBind ", "Yellow");}
        else{
            holder.itemViewId.setBackgroundColor(Color.parseColor("#E0E0E0"));
            Log.d("onBind ", "Gray");}

    }

    public void deleteNote(int position) {
        getSnapshots().getSnapshot(position).getReference().delete();
    }

    class NoteHolder extends RecyclerView.ViewHolder {
        private TextView textViewContent;
        private RelativeLayout itemViewId;

        public NoteHolder(View itemView) {
            super(itemView);
            textViewContent = itemView.findViewById(R.id.text_view_content);
            itemViewId = itemView.findViewById(R.id.list_item_color);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (listenerShort != null && position != RecyclerView.NO_POSITION) {
                        listenerShort.onItemClick(getSnapshots().get(position), getSnapshots().getSnapshot(position).getId());
                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener(){
                @Override
                public boolean onLongClick(View v) {
                    int position = getAdapterPosition();
                    if (listenerLong != null && position != RecyclerView.NO_POSITION) {
                        listenerLong.onLongItemClick(getSnapshots().get(position), getSnapshots().getSnapshot(position).getId());
                    }
                    return true;
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Note note, String id);
    }

    public interface OnLongItemClickListener {
        void onLongItemClick(Note note, String id);
    }

    public void setOnItemClickListener(OnItemClickListener listenerShort) {
        this.listenerShort = listenerShort;
    }

    public void setOnLongItemClickListener(OnLongItemClickListener listenerLong) {
        this.listenerLong = listenerLong;
    }
}