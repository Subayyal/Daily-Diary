package com.example.subayyal.dailynotes.Activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.subayyal.dailynotes.Activities.ViewModels.HomeScreenViewModel;
import com.example.subayyal.dailynotes.HelperClasses.Utils;
import com.example.subayyal.dailynotes.LocalDatabase.Dao.NoteDao;
import com.example.subayyal.dailynotes.LocalDatabase.Entities.NoteEntity;
import com.example.subayyal.dailynotes.R;
import com.github.irshulx.Editor;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HomeScreen extends AppCompatActivity {

    private FloatingActionButton fab;
    private TextView date_text;
    private Editor editor;
    private Toolbar toolbar;
    private Dialog calender;
    private AlertDialog.Builder delete;
    private CompactCalendarView calendarView;
    private TextView month;
    private HomeScreenViewModel homeScreenViewModel;
    private BroadcastReceiver br;
    private ImageView cloudOff;
    private ImageView cloudDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        homeScreenViewModel = ViewModelProviders.of(this)
                .get(HomeScreenViewModel.class);

        ConstraintLayout constraintLayout = findViewById(R.id.activity_main);
        //initialize calender view components
        calender = new Dialog(this);
        calender.setContentView(R.layout.calender_popup);
        calendarView = calender.findViewById(R.id.compactcalendarview);
        month = calender.findViewById(R.id.calender_month);
        //finish setting up calender view

        Utils.setSelectedDate(new SimpleDateFormat("MM-dd-yyyy").format(new Date()));
        Utils.setUser_id(GoogleSignIn.getLastSignedInAccount(this).getId());
        date_text = (TextView) findViewById(R.id.date_text);
        fab = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        editor = (Editor) findViewById(R.id.editor_preview);
        toolbar = findViewById(R.id.toolbar_homescreen);
        cloudOff = findViewById(R.id.cloud_off);
        cloudDone = findViewById(R.id.cloud_done);

        //set up tool bar
        setSupportActionBar(toolbar);

        homeScreenViewModel.isConnected()
                .observe(this, new Observer<Boolean>() {
                    @Override
                    public void onChanged(@Nullable Boolean aBoolean) {
                        Snackbar.make(constraintLayout, "asdas", Snackbar.LENGTH_SHORT);
                    }
                });


        homeScreenViewModel.getCurrentNote()
                .observe(this, new Observer<NoteEntity>() {
                    @Override
                    public void onChanged(@Nullable NoteEntity noteEntity) {
                        if (noteEntity != null) {
                            updateUI(true);
                            Date date = null;
                            String formattedDate;
                            try {
                                date = new SimpleDateFormat("MM-dd-yyyy").parse(noteEntity.getNote_timestamp());
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            if (date != null) {
                                formattedDate = new SimpleDateFormat("EEEE, MMMM dd, yyyy").format(date);
                                date_text.setText(formattedDate);
                            } else {
                                date_text.setText(noteEntity.getNote_timestamp());
                            }
                            editor.render(editor.getContentDeserialized(noteEntity.getNote_text()));
                            if (noteEntity.isSynced() && !noteEntity.isEdited()) {
                                cloudOff.setVisibility(View.GONE);
                                cloudDone.setVisibility(View.VISIBLE);
                            } else {
                                cloudDone.setVisibility(View.GONE);
                                cloudOff.setVisibility(View.VISIBLE);
                            }
                        } else {
                            updateUI(false);
                            Date date = null;
                            try {
                                date = new SimpleDateFormat("MM-dd-yyyy").parse(homeScreenViewModel.getCurrentDate().getValue());
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            String formattedDate = new SimpleDateFormat("EEEE, MMMM dd, yyyy").format(date);
                            date_text.setText(formattedDate);
                        }
                    }
                });

        homeScreenViewModel.getDates()
                .observe(this, new Observer<List<NoteDao.NoteDates>>() {
                    @Override
                    public void onChanged(@Nullable List<NoteDao.NoteDates> noteDates) {
                        Log.d("Test", "getDates() onChanged Called");
                        if (noteDates != null) {
                            calendarView.removeAllEvents();
                            Log.d("Test", "getDates() noteDates is not null: ");

                            for (int i = 0; i < noteDates.size(); i++) {
                                List<Event> events = new ArrayList<>();
                                try {
                                    events = calendarView.getEvents(new SimpleDateFormat("MM-dd-yyyy").parse(noteDates.get(i).getNote_timestamp()));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                if (events.isEmpty()) {
                                    calendarView
                                            .addEvent(new Event(R.color.colorAccent, Utils.convertStringDateToMilliseconds(noteDates.get(i).getNote_timestamp())));
                                }
                            }
                        }
                    }
                });


        fab.setOnClickListener(view -> {
            Intent intent = new Intent(HomeScreen.this, EditNoteActivity.class);
            String noteJson;
            if (homeScreenViewModel.getCurrentNote().getValue() != null) {
                noteJson = new Gson().toJson(homeScreenViewModel.getCurrentNote().getValue());
            } else {
                noteJson = new Gson().toJson(new NoteEntity(null, Utils.getUser_id(), null, homeScreenViewModel.getCurrentDate().getValue(), null));
            }
            intent.putExtra("Note", noteJson);
            startActivity(intent);

        });

    }

    public void updateUI(boolean noteFound) {
        if (!noteFound) {
            findViewById(R.id.editor_preview_container).setVisibility(View.GONE);
            findViewById(R.id.not_found).setVisibility(View.VISIBLE);
            cloudDone.setVisibility(View.GONE);
            cloudOff.setVisibility(View.GONE);
        } else {
            findViewById(R.id.editor_preview_container).setVisibility(View.VISIBLE);
            findViewById(R.id.not_found).setVisibility(View.GONE);
        }
    }

    public void showDeleteDialog() {
        delete = new AlertDialog.Builder(this);
        delete.setMessage("This log will be deleted permanently").setTitle("Delete Note?");

        delete.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                homeScreenViewModel.deleteCurrentNote();
                dialogInterface.dismiss();
            }
        });

        delete.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog dialog = delete.create();
        dialog.show();
    }



    public void showCalenderDialog() {

        month.setText(new SimpleDateFormat("MMM yy").format(calendarView.getFirstDayOfCurrentMonth()));
        calender.show();

        calendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                HomeScreen.this.homeScreenViewModel
                        .setCurrentDate(new SimpleDateFormat("MM-dd-yyyy")
                                .format(dateClicked));
                HomeScreen.this.calender.dismiss();
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                month.setText(new SimpleDateFormat("MMM yy").format(firstDayOfNewMonth));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.homescreen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.calender_icon:
                showCalenderDialog();
                return true;
            case R.id.delete_icon:
                showDeleteDialog();
                return true;
            case R.id.sync_icon:
                homeScreenViewModel.cloudSync();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
