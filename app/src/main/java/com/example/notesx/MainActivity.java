package com.example.notesx;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private LinearLayout emptyState;
    private EditText editSearch;
    private ChipGroup chipGroupFilter;
    private DBHelper dbHelper;
    private NotesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        fabAdd = findViewById(R.id.fabAdd);
        emptyState = findViewById(R.id.emptyState);
        editSearch = findViewById(R.id.editSearch);
        chipGroupFilter = findViewById(R.id.chipGroupFilter);

        dbHelper = new DBHelper(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotesAdapter(this, dbHelper.getAllNotes());
        recyclerView.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AddEditNoteActivity.class));
        });

        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadNotes();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            loadNotes();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupCategoryFilters();
        loadNotes();
    }

    private void setupCategoryFilters() {
        int checkedId = chipGroupFilter.getCheckedChipId();
        String currentFilter = "All";
        if (checkedId != -1) {
            Chip currentChip = findViewById(checkedId);
            if (currentChip != null) currentFilter = currentChip.getText().toString();
        }

        chipGroupFilter.setOnCheckedStateChangeListener(null);
        chipGroupFilter.removeAllViews();

        Chip allChip = (Chip) getLayoutInflater().inflate(R.layout.item_chip_filter, chipGroupFilter, false);
        allChip.setText("All");
        chipGroupFilter.addView(allChip);
        if (currentFilter.equals("All")) allChip.setChecked(true);

        List<String> categories = dbHelper.getAllCategories();
        for (String cat : categories) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.item_chip_filter, chipGroupFilter, false);
            chip.setText(cat);
            chipGroupFilter.addView(chip);
            if (currentFilter.equals(cat)) chip.setChecked(true);
        }

        chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> loadNotes());
    }

    private void loadNotes() {
        String query = editSearch.getText().toString();
        int checkedId = chipGroupFilter.getCheckedChipId();
        String category = "All";
        
        if (checkedId != -1) {
            Chip chip = findViewById(checkedId);
            if (chip != null) {
                category = chip.getText().toString();
            }
        }

        List<Note> notes = dbHelper.searchAndFilterNotes(query, category);
        
        if (notes.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.updateNotes(notes);
        }
    }
}