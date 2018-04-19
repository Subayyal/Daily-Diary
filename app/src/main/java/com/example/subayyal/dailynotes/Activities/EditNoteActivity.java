package com.example.subayyal.dailynotes.Activities;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.example.subayyal.dailynotes.Activities.ViewModels.EditNoteActivityViewModel;
import com.example.subayyal.dailynotes.HelperClasses.Utils;
import com.example.subayyal.dailynotes.LocalDatabase.Entities.NoteEntity;
import com.example.subayyal.dailynotes.R;
import com.github.irshulx.Editor;
import com.github.irshulx.models.EditorTextStyle;
import com.google.gson.Gson;

import java.util.UUID;

public class EditNoteActivity extends AppCompatActivity {

    Toolbar toolbar;
    Editor editor;
    String noteJson;
    NoteEntity noteEntity;
    EditNoteActivityViewModel editNoteActivityViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);
        toolbar = findViewById(R.id.editscreen_toolbar);
        setSupportActionBar(toolbar);

        editor = (Editor) findViewById(R.id.note_editor);

        editNoteActivityViewModel = ViewModelProviders.of(this).get(EditNoteActivityViewModel.class);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            noteJson = b.getString("Note");
            noteEntity = new Gson().fromJson(noteJson, NoteEntity.class);
            if (noteEntity.getNote_id() != null) {
                editor.render(editor.getContentDeserialized(noteEntity.getNote_text()));
            } else {
                editor.render();
            }
        }

        editNoteActivityViewModel.getShouldClose().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean aBoolean) {
                if(aBoolean)
                    finish();
            }
        });


        //Setup event listeners
        findViewById(R.id.action_h1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.updateTextStyle(EditorTextStyle.H1);
            }
        });

        findViewById(R.id.action_h2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.updateTextStyle(EditorTextStyle.H2);
            }
        });

        findViewById(R.id.action_h3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.updateTextStyle(EditorTextStyle.H3);
            }
        });

        findViewById(R.id.action_bold).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.updateTextStyle(EditorTextStyle.BOLD);
            }
        });

        findViewById(R.id.action_Italic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.updateTextStyle(EditorTextStyle.ITALIC);
            }
        });

        findViewById(R.id.action_indent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.updateTextStyle(EditorTextStyle.INDENT);
            }
        });

        findViewById(R.id.action_outdent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.updateTextStyle(EditorTextStyle.OUTDENT);
            }
        });

        findViewById(R.id.action_bulleted).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.insertList(false);
            }
        });

        findViewById(R.id.action_unordered_numbered).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.insertList(true);
            }
        });

        findViewById(R.id.action_hr).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.insertDivider();
            }
        });

        findViewById(R.id.action_erase).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.clearAllContents();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.editscreen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.save_note:
                saveNote();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveNote() {
        try {
            if (noteEntity.getNote_id() == null) {
                Log.d("Test", editor.getContentAsSerialized());
                noteEntity = new NoteEntity(UUID.randomUUID().toString(), Utils.getUser_id(), editor.getContentAsSerialized(), noteEntity.getNote_timestamp(), false,  false, false);
                editNoteActivityViewModel.saveNote(noteEntity);
            } else {
                Log.d("Test", editor.getContentAsSerialized());
                noteEntity.setNote_text(editor.getContentAsSerialized());
                editNoteActivityViewModel.saveEditNote(noteEntity);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Utils.createToast(this, "Failed to save note!");
        }
    }


}


