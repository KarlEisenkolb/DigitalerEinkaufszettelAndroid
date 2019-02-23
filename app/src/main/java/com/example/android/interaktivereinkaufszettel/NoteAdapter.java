package com.example.android.interaktivereinkaufszettel;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteHolder> {
    private List<Note> notes;
    private OnItemClickListener listenerShort;
    private OnLongItemClickListener listenerLong;

    public NoteAdapter(List<Note> notes) {
        this.notes = notes;
    }

    @NonNull
    @Override
    public NoteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_item, parent, false);
        return new NoteHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteHolder holder, int position) {
        Note currentNote = notes.get(position);
        holder.textViewContent.setText(currentNote.getContent());
        //holder.textViewId.setText(currentNote.getId());
        Log.d("onBind ", "position: "+position+" adapterPos: "+currentNote.getAdapterPos()+" currentColor: "+currentNote.getNoteColor());
        if (currentNote.getNoteColor() == Note.NOTE_COLOR_GREEN){
            holder.itemViewId.setBackgroundColor(Color.parseColor("#388E3C"));
            Log.d("onBind ", "Green");}
        else if (currentNote.getNoteColor() == Note.NOTE_COLOR_YELLOW){
            holder.itemViewId.setBackgroundColor(Color.parseColor("#FF8F00"));
            Log.d("onBind ", "Yellow");}
        else{
            holder.itemViewId.setBackgroundColor(Color.parseColor("#E0E0E0"));
            Log.d("onBind ", "Gray");}

    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public String getIdAt(int position) {
        return notes.get(position).getId();
    }

    public Note getNoteAt(int position) {
        return notes.get(position);
    }

    class NoteHolder extends RecyclerView.ViewHolder {
        private TextView textViewContent;
        //private TextView textViewId;
        private RelativeLayout itemViewId;

        public NoteHolder(View itemView) {
            super(itemView);
            textViewContent = itemView.findViewById(R.id.text_view_content);
            //textViewId = itemView.findViewById(R.id.text_view_id);
            itemViewId = itemView.findViewById(R.id.list_item_color);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (listenerShort != null && position != RecyclerView.NO_POSITION) {
                        listenerShort.onItemClick(notes.get(position));
                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener(){
                @Override
                public boolean onLongClick(View v) {
                    int position = getAdapterPosition();
                    if (listenerLong != null && position != RecyclerView.NO_POSITION) {
                        listenerLong.onLongItemClick(notes.get(position));
                    }
                    return true;
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Note note);
    }

    public interface OnLongItemClickListener {
        void onLongItemClick(Note note);
    }

    public void setOnItemClickListener(OnItemClickListener listenerShort) {
        this.listenerShort = listenerShort;
    }

    public void setOnLongItemClickListener(OnLongItemClickListener listenerLong) {
        this.listenerLong = listenerLong;
    }
}