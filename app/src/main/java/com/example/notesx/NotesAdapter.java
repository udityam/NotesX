package com.example.notesx;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    private Context context;
    private List<Note> notesList;

    public NotesAdapter(Context context, List<Note> notesList) {
        this.context = context;
        this.notesList = notesList;
    }

    public void updateNotes(List<Note> newNotes) {
        this.notesList = newNotes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.note_item, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notesList.get(position);
        holder.titleText.setText(note.getTitle());
        holder.contentText.setText(note.getContent());
        holder.dateText.setText(note.getDate());
        holder.categoryText.setText(note.getCategory());

        int[] pastels = {
            R.color.pastel_1, R.color.pastel_2, R.color.pastel_3, 
            R.color.pastel_4, R.color.pastel_5, R.color.pastel_6
        };
        int bgColor = ContextCompat.getColor(context, pastels[note.getId() % pastels.length]);
        holder.cardView.setCardBackgroundColor(bgColor);

        holder.cardView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddEditNoteActivity.class);
            intent.putExtra("id", note.getId());
            intent.putExtra("title", note.getTitle());
            intent.putExtra("content", note.getContent());
            intent.putExtra("category", note.getCategory());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return notesList.size();
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView titleText, contentText, dateText, categoryText;
        MaterialCardView cardView;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            titleText = itemView.findViewById(R.id.textNoteTitle);
            contentText = itemView.findViewById(R.id.textNoteContent);
            dateText = itemView.findViewById(R.id.textNoteDate);
            categoryText = itemView.findViewById(R.id.textCategory);
        }
    }
}