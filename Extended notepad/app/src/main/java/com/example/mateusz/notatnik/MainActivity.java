package com.example.mateusz.notatnik;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mateusz.notatnik.database.DatabaseHelper;
import com.example.mateusz.notatnik.database.model.Note;
import com.example.mateusz.notatnik.utils.Divider;
import com.example.mateusz.notatnik.utils.RecTouchListener;
import com.example.mateusz.notatnik.view.NotesView;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {


    private NotesView notesView;
    private List<Note> notesList = new ArrayList<>();
    private RecyclerView recyclerView;
    private TextView noNotesView;

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recycler_view);
        noNotesView = findViewById(R.id.empty_notes_view);

        databaseHelper = new DatabaseHelper(this);

        notesList.addAll(databaseHelper.getAllNotes());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNoteDialog(false, null, -1);
            }
        });

        FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this, NoteActivity.class);
                    startActivity(intent);
            }
        });

        FloatingActionButton fab3 = (FloatingActionButton) findViewById(R.id.fab3);
        fab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent intent=new Intent(MainActivity.this, NoteActivity.class);
                Intent intent=new Intent(MainActivity.this, CameraActivity.class);
                startActivity(intent);
            }
        });

        notesView = new NotesView(this, notesList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new Divider(this, LinearLayoutManager.VERTICAL, 16));
        recyclerView.setAdapter(notesView);

        showEmptyNotesMsg();

        recyclerView.addOnItemTouchListener(new RecTouchListener(this,
                recyclerView, new RecTouchListener.ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                showActionsDialog(position);
            }

            @Override
            public void onLongClick(View view, int position) {
                showDeleteAllDialog(position);
            }
        }));


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 10:
                break;
            default:
                break;
        }
    }




    private void createNote(String note) {

        long id = databaseHelper.insertNote(note);
        Note newNote = databaseHelper.getNote(id);

        if (newNote != null) {
            notesList.add(0, newNote);
            notesView.notifyDataSetChanged();
            showEmptyNotesMsg();
        }
    }


    private void deleteNote(int position) {
        databaseHelper.deleteNote(notesList.get(position));
        notesList.remove(position);
        notesView.notifyItemRemoved(position);

        showEmptyNotesMsg();
    }


    public void deleteAllNotes() {
        databaseHelper.deleteAllNotes();
        notesList.clear();
        notesView.notifyDataSetChanged();

        showEmptyNotesMsg();
    }


    private void updateNote(String note, int position) {
        Note n = notesList.get(position);
        n.setNote(note);
        databaseHelper.updateNote(n);
        notesList.set(position, n);
        notesView.notifyItemChanged(position);

        showEmptyNotesMsg();
    }


    private void showActionsDialog(final int position) {
        CharSequence colors[] = new CharSequence[]{"Edytuj", "Usuń"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Wybierz opcję:");
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    showNoteDialog(true, notesList.get(position), position);
                } else {
                    deleteNote(position);
                }
            }
        });
        builder.show();
    }


    private void showDeleteAllDialog(final int position) {
        CharSequence choice[] = new CharSequence[]{"Tak", "Nie"};

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Usunąć wszystkie notatki?");
        builder.setItems(choice, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int pos) {
                if (pos == 0) {
                    deleteAllNotes();
                    Toast.makeText(MainActivity.this, "Usunięto wszystkie notatki.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.show();
    }

    private void showNoteDialog(final boolean toUpdate, final Note note, final int position) {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getApplicationContext());
        View view = layoutInflaterAndroid.inflate(R.layout.add_note, null);

        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilderUserInput.setView(view);

        final EditText inputNote = view.findViewById(R.id.note);
        TextView dialogTitle = view.findViewById(R.id.dialog_title);
        dialogTitle.setText(!toUpdate ? getString(R.string.lbl_new_note_title) : getString(R.string.lbl_edit_note_title));

        if (toUpdate && note != null) {
            inputNote.setText(note.getNote());
        }
        alertDialogBuilderUserInput
                .setPositiveButton(toUpdate ? "Aktualizuj" : "Zapisz", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {
                        if (TextUtils.isEmpty(inputNote.getText().toString())) {
                            Toast.makeText(MainActivity.this, "Wprowadź notatkę!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (toUpdate && note != null) {
                            updateNote(inputNote.getText().toString(), position);
                        } else {
                            createNote(inputNote.getText().toString());
                        }
                    }
                })
                .setNegativeButton("Anuluj",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                dialogBox.cancel();
                            }
                        });

        final AlertDialog alertDialog = alertDialogBuilderUserInput.create();
        alertDialog.show();
    }


    private void showEmptyNotesMsg() {
        if (databaseHelper.getNotesCount() > 0) {
            noNotesView.setVisibility(View.GONE);
        } else {
            noNotesView.setVisibility(View.VISIBLE);
        }
    }
}
