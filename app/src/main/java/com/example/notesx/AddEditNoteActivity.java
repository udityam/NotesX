package com.example.notesx;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

public class AddEditNoteActivity extends AppCompatActivity {

    private EditText editTitle, editContent;
    private Button btnSave;
    private ImageButton btnDelete, btnUndo, btnRedo, btnBack;
    private ChipGroup chipGroupCategory;
    private DBHelper dbHelper;
    private int noteId = -1;

    private Stack<String> undoStack = new Stack<>();
    private Stack<String> redoStack = new Stack<>();
    private boolean isUndoOrRedo = false;
    private List<String> currentCategories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_note);

        editTitle = findViewById(R.id.editTitle);
        editContent = findViewById(R.id.editContent);
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);
        btnBack = findViewById(R.id.btnBack);
        btnUndo = findViewById(R.id.btnUndo);
        btnRedo = findViewById(R.id.btnRedo);
        chipGroupCategory = findViewById(R.id.chipGroupCategory);

        dbHelper = new DBHelper(this);
        currentCategories = dbHelper.getAllCategories();
        String category = "Personal";

        if (getIntent().hasExtra("id")) {
            noteId = getIntent().getIntExtra("id", -1);
            editTitle.setText(getIntent().getStringExtra("title"));
            editContent.setText(getIntent().getStringExtra("content"));
            String incomingCategory = getIntent().getStringExtra("category");
            if (incomingCategory != null) category = incomingCategory;
            btnDelete.setVisibility(View.VISIBLE);
        }

        loadCategories(category);

        // Initialize Undo/Redo tracking for content
        undoStack.push(editContent.getText().toString());
        updateUndoRedoButtons();

        editContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!isUndoOrRedo) {
                    undoStack.push(s.toString());
                    redoStack.clear();
                    updateUndoRedoButtons();
                }
            }
        });

        btnUndo.setOnClickListener(v -> {
            if (undoStack.size() > 1) {
                isUndoOrRedo = true;
                redoStack.push(undoStack.pop());
                editContent.setText(undoStack.peek());
                editContent.setSelection(editContent.getText().length());
                isUndoOrRedo = false;
                updateUndoRedoButtons();
            }
        });

        btnRedo.setOnClickListener(v -> {
            if (!redoStack.isEmpty()) {
                isUndoOrRedo = true;
                String text = redoStack.pop();
                undoStack.push(text);
                editContent.setText(text);
                editContent.setSelection(editContent.getText().length());
                isUndoOrRedo = false;
                updateUndoRedoButtons();
            }
        });

        btnSave.setOnClickListener(v -> saveNote());
        btnDelete.setOnClickListener(v -> confirmDelete());
        btnBack.setOnClickListener(v -> finish());
    }

    private void updateUndoRedoButtons() {
        btnUndo.setEnabled(undoStack.size() > 1);
        btnUndo.setAlpha(btnUndo.isEnabled() ? 1.0f : 0.4f);
        
        btnRedo.setEnabled(!redoStack.isEmpty());
        btnRedo.setAlpha(btnRedo.isEnabled() ? 1.0f : 0.4f);
    }

    private void loadCategories(String selectedCategory) {
        chipGroupCategory.removeAllViews();
        for (String cat : currentCategories) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.item_chip_filter, chipGroupCategory, false);
            chip.setText(cat);
            chipGroupCategory.addView(chip);
            if (cat.equals(selectedCategory)) {
                chip.setChecked(true);
            }
        }

        Chip addChip = (Chip) getLayoutInflater().inflate(R.layout.item_chip_filter, chipGroupCategory, false);
        addChip.setText("➕ New");
        addChip.setCheckable(false);
        addChip.setOnClickListener(v -> showAddCategoryDialog());
        chipGroupCategory.addView(addChip);
    }

    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Category");

        final EditText input = new EditText(this);
        input.setHint("e.g. Work");
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String newCat = input.getText().toString().trim();
            if (!newCat.isEmpty()) {
                if (!currentCategories.contains(newCat)) {
                    currentCategories.add(newCat);
                }
                loadCategories(newCat);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void saveNote() {
        String title = editTitle.getText().toString().trim();
        String content = editContent.getText().toString().trim();

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        int checkedId = chipGroupCategory.getCheckedChipId();
        String category = "Personal";
        if (checkedId != -1) {
            Chip chip = findViewById(checkedId);
            if (chip != null) category = chip.getText().toString();
        }

        String date = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());

        if (noteId == -1) {
            dbHelper.insertNote(title, content, date, category);
            Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show();
        } else {
            dbHelper.updateNote(noteId, title, content, date, category);
            Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Note")
                .setMessage("Are you sure you want to delete this note?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    dbHelper.deleteNote(noteId);
                    Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }
}