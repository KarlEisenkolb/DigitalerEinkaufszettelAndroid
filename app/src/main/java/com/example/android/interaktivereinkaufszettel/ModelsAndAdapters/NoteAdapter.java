package com.example.android.interaktivereinkaufszettel.ModelsAndAdapters;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.android.interaktivereinkaufszettel.ModelsAndAdapters.Note;
import com.example.android.interaktivereinkaufszettel.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class NoteAdapter extends FirestoreRecyclerAdapter<Note, RecyclerView.ViewHolder> {

    private OnItemClickListener listenerShort;
    private OnLongItemClickListener listenerLong;
    private final LayoutInflater inflater;

    public NoteAdapter(@NonNull FirestoreRecyclerOptions<Note> options, Context ctx) {
        super(options);
        this.inflater = LayoutInflater.from(ctx);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == Note.NOTE) {
            View itemView = inflater.inflate(R.layout.note_item, parent, false);
            return new NoteHolder(itemView);
        } else {
            View itemView = inflater.inflate(R.layout.note_category_title, parent, false);
            return new CategoryHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull Note note) {
        if (holder instanceof NoteHolder) {
            ((NoteHolder) holder).textViewContent.setText(note.gibContent());

            if (note.gibNoteColor() == Note.NOTE_COLOR_GREEN) {
                ((NoteHolder) holder).itemViewId.setBackgroundColor(Color.parseColor("#388E3C"));
                ((NoteHolder) holder).textViewContent.setTextColor(Color.BLACK);
                Log.d("onBind ", "Green");
            } else if (note.gibNoteColor() == Note.NOTE_NO_COLOR) {
                ((NoteHolder) holder).itemViewId.setBackgroundColor(Color.parseColor("#FFFFFF"));//#E0E0E0
                ((NoteHolder) holder).textViewContent.setTextColor(Color.BLACK);
                Log.d("onBind ", "Gray");
            } else {
                ((NoteHolder) holder).itemViewId.setBackgroundColor(Color.parseColor("#FF8F00"));
                ((NoteHolder) holder).textViewContent.setTextColor(Color.BLACK);
                Log.d("onBind ", "Yellow");
            }
        } else {
            ((CategoryHolder) holder).textViewContent.setText(note.gibContent());

            if (note.gibNoteColor() == Note.NOTE_COLOR_GREEN) {
                ((CategoryHolder) holder).itemViewId.setBackgroundColor(Color.parseColor("#388E3C"));
                ((CategoryHolder) holder).textViewContent.setTextColor(Color.BLACK);
                Log.d("onBind ", "Green");
            } else if (note.gibNoteColor() == Note.NOTE_NO_COLOR) {
                ((CategoryHolder) holder).itemViewId.setBackgroundColor(Color.parseColor("#757575")); //#E0E0E0
                ((CategoryHolder) holder).textViewContent.setTextColor(Color.WHITE);
                Log.d("onBind ", "Gray");
            } else {
                ((CategoryHolder) holder).itemViewId.setBackgroundColor(Color.parseColor("#FF8F00"));
                ((CategoryHolder) holder).textViewContent.setTextColor(Color.BLACK);
                Log.d("onBind ", "Yellow");

            }
        }
    }

    public class NoteHolder extends RecyclerView.ViewHolder {
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

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
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

    public class CategoryHolder extends RecyclerView.ViewHolder {
        private TextView textViewContent;
        private RelativeLayout itemViewId;

        public CategoryHolder(View itemView) {
            super(itemView);
            textViewContent = itemView.findViewById(R.id.text_view_title);
            itemViewId = itemView.findViewById(R.id.title_color);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (listenerShort != null && position != RecyclerView.NO_POSITION) {
                        listenerShort.onItemClick(getSnapshots().get(position), getSnapshots().getSnapshot(position).getId());
                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
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

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public int getItemViewType(int position) {
        return (int) getSnapshots().get(position).gibType();
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