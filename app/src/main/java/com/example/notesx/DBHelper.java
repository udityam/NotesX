package com.example.notesx;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "NotesDB";
    private static final int DATABASE_VERSION = 2; // Updated for category migration
    private static final String TABLE_NOTES = "notes";
    
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_CONTENT = "content";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_CATEGORY = "category";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NOTES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE + " TEXT NOT NULL, " +
                COLUMN_CONTENT + " TEXT NOT NULL, " +
                COLUMN_DATE + " TEXT, " +
                COLUMN_CATEGORY + " TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_NOTES + " ADD COLUMN " + COLUMN_CATEGORY + " TEXT DEFAULT 'Personal'");
        }
    }

    public long insertNote(String title, String content, String date, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_CONTENT, content);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_CATEGORY, category);
        return db.insert(TABLE_NOTES, null, values);
    }

    public List<Note> searchAndFilterNotes(String query, String category) {
        List<Note> notes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = "";
        List<String> selectionArgs = new ArrayList<>();

        if (category != null && !category.equals("All")) {
            selection += COLUMN_CATEGORY + " = ?";
            selectionArgs.add(category);
        }

        if (query != null && !query.trim().isEmpty()) {
            if (!selection.isEmpty()) selection += " AND ";
            selection += "(" + COLUMN_TITLE + " LIKE ? OR " + COLUMN_CONTENT + " LIKE ?)";
            selectionArgs.add("%" + query.trim() + "%");
            selectionArgs.add("%" + query.trim() + "%");
        }

        Cursor cursor = db.query(TABLE_NOTES, null,
                selection.isEmpty() ? null : selection,
                selectionArgs.isEmpty() ? null : selectionArgs.toArray(new String[0]),
                null, null, COLUMN_ID + " DESC");

        if (cursor.moveToFirst()) {
            do {
                notes.add(new Note(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return notes;
    }

    public List<Note> getAllNotes() {
        return searchAndFilterNotes("", "All");
    }

    public List<String> getAllCategories() {
        List<String> categories = new ArrayList<>();
        categories.add("Personal");
        categories.add("Study");
        categories.add("Ideas");

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT DISTINCT " + COLUMN_CATEGORY + " FROM " + TABLE_NOTES, null);

        if (cursor.moveToFirst()) {
            do {
                String cat = cursor.getString(0);
                if (cat != null && !cat.trim().isEmpty() && !categories.contains(cat)) {
                    categories.add(cat);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return categories;
    }

    public int updateNote(int id, String title, String content, String date, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_CONTENT, content);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_CATEGORY, category);
        return db.update(TABLE_NOTES, values, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }

    public void deleteNote(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NOTES, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }
}