package com.example.notetakingapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

interface OnNoteActionListener {
    fun onEditClick(note: Note)
    fun onDeleteClick(note: Note)
}

class NoteAdapter(
    private val notes: MutableList<Note>,
    private val listener: OnNoteActionListener
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.noteTitleTextView)
        val contentTextView: TextView = itemView.findViewById(R.id.noteContentTextView)
        val editButton: Button = itemView.findViewById(R.id.editButton)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val currentNote = notes[position]
        holder.titleTextView.text = currentNote.title
        holder.contentTextView.text = currentNote.content

        holder.editButton.setOnClickListener {
            listener.onEditClick(currentNote)
        }
        holder.deleteButton.setOnClickListener {
            listener.onDeleteClick(currentNote)
        }
        holder.itemView.setOnClickListener { // Make whole item clickable for edit too
            listener.onEditClick(currentNote)
        }
    }

    override fun getItemCount(): Int {
        return notes.size
    }

    fun addNote(note: Note) {
        notes.add(note)
        notifyItemInserted(notes.size - 1)
    }

    fun updateNote(updatedNote: Note) {
        val index = notes.indexOfFirst { it.id == updatedNote.id }
        if (index != -1) {
            notes[index].title = updatedNote.title
            notes[index].content = updatedNote.content
            notifyItemChanged(index)
        }
    }

    fun deleteNote(note: Note) {
        val index = notes.indexOfFirst { it.id == note.id }
        if (index != -1) {
            notes.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}
