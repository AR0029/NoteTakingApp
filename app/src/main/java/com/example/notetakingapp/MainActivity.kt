package com.example.notetakingapp

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : AppCompatActivity(), OnNoteActionListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var noteAdapter: NoteAdapter
    private lateinit var addNoteFab: FloatingActionButton
    private lateinit var emptyStateTextView: TextView

    private val notesList = mutableListOf<Note>()
    private var nextNoteId = 1

    // Constants for SharedPreferences (for persistence)
    private val PREFS_NAME = "note_app_prefs"
    private val NOTES_KEY = "notes_json"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Find views by their IDs from activity_main.xml
        recyclerView = findViewById(R.id.recyclerViewNotes)
        addNoteFab = findViewById(R.id.addNoteFab)
        emptyStateTextView = findViewById(R.id.emptyStateTextView)

        // Load notes when the app starts
        loadNotes()

        // 2. Set up the RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        noteAdapter = NoteAdapter(notesList, this)
        recyclerView.adapter = noteAdapter

        // 3. Set up the FAB click listener
        addNoteFab.setOnClickListener {
            showNoteDialog(null) // Pass null to indicate adding a new note
        }

        // 4. Initially check for empty state
        updateEmptyState()
    }

    // --- Implementation of OnNoteActionListener interface ---
    override fun onEditClick(note: Note) {
        showNoteDialog(note) // Pass the note to edit
    }

    override fun onDeleteClick(note: Note) {
        AlertDialog.Builder(this)
            .setTitle("Delete Note")
            .setMessage("Are you sure you want to delete '${note.title}'?")
            .setPositiveButton("Delete") { _, _ ->
                noteAdapter.deleteNote(note)
                saveNotes() // Save changes after deletion
                updateEmptyState()
            }
            .setNegativeButton("Cancel", null) // Do nothing on cancel
            .show()
    }

    // --- Helper function to show Add/Edit Note Dialog ---
    private fun showNoteDialog(noteToEdit: Note?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_note, null)
        val titleEditText: EditText = dialogView.findViewById(R.id.dialogTitleEditText)
        val contentEditText: EditText = dialogView.findViewById(R.id.dialogContentEditText)
        val saveButton: Button = dialogView.findViewById(R.id.dialogSaveButton)
        val cancelButton: Button = dialogView.findViewById(R.id.dialogCancelButton)

        // Pre-fill if editing an existing note
        if (noteToEdit != null) {
            titleEditText.setText(noteToEdit.title)
            contentEditText.setText(noteToEdit.content)
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        saveButton.setOnClickListener {
            val title = titleEditText.text.toString().trim()
            val content = contentEditText.text.toString().trim()

            if (title.isNotBlank() && content.isNotBlank()) {
                if (noteToEdit == null) {
                    // Add new note
                    val newNote = Note(id = nextNoteId++, title = title, content = content)
                    noteAdapter.addNote(newNote)
                } else {
                    // Update existing note
                    val updatedNote = noteToEdit.copy(title = title, content = content)
                    noteAdapter.updateNote(updatedNote)
                }
                saveNotes() // Save changes after add/edit
                updateEmptyState()
                dialog.dismiss()
            } else {
                // You can add a Toast message here if needed
                // Toast.makeText(this, "Title and Content cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    // Helper to show/hide empty state message
    private fun updateEmptyState() {
        if (notesList.isEmpty()) {
            emptyStateTextView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyStateTextView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    // --- Persistence Functions ---
    private fun saveNotes() {
        val sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        val json = Gson().toJson(notesList)
        editor.putString(NOTES_KEY, json)
        editor.apply()
    }

    private fun loadNotes() {
        val sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = sharedPrefs.getString(NOTES_KEY, null)

        if (json != null) {
            val type = object : TypeToken<MutableList<Note>>() {}.type
            val loadedNotes: MutableList<Note> = Gson().fromJson(json, type)
            notesList.addAll(loadedNotes)

            // Find the highest ID among loaded notes to continue the counter
            nextNoteId = (notesList.maxOfOrNull { it.id } ?: 0) + 1
        }
    }
}
