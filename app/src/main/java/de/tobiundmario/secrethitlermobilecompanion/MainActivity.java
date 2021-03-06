package de.tobiundmario.secrethitlermobilecompanion;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import java.util.Arrays;

import de.tobiundmario.secrethitlermobilecompanion.SHCards.CardDialog;
import de.tobiundmario.secrethitlermobilecompanion.SHCards.FascistTrackCreationDialog;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.BackupManager;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.FascistTrackSelectionManager;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.GameEventsManager;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.GameManager;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.JSONManager;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.PlayerListManager;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.RecyclerViewManager;

public class MainActivity extends AppCompatActivity {

    public MainScreenFragment fragment_main;
    public SetupFragment fragment_setup;
    public GameFragment fragment_game;

    private LinearLayout currentFragmentContainer;
    private int currentFragment = 0;

    private LinearLayout container_main, container_setup, container_game;

    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;

    public static final int page_main = 0, page_setup = 1, page_game = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragment_main = (MainScreenFragment) fragmentManager.findFragmentById(R.id.fragment_main);
        fragment_setup = (SetupFragment) fragmentManager.findFragmentById(R.id.fragment_setup);
        fragment_game = (GameFragment) fragmentManager.findFragmentById(R.id.fragment_game);

        container_main = findViewById(R.id.container_fragment_main);
        container_game = findViewById(R.id.container_fragment_game);
        container_setup = findViewById(R.id.container_fragment_setup);

        currentFragmentContainer = container_main;

        fragment_setup.initialiseLayout();

        GameEventsManager.setContext(this);
        checkForBackups();

        uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        ExceptionHandler.initialise(this);
        Thread.setDefaultUncaughtExceptionHandler (new Thread.UncaughtExceptionHandler()
        {
            @Override
            public void uncaughtException (Thread thread, Throwable e)
            {
                try {
                    Log.e("Error", Arrays.toString(e.getStackTrace()));

                    //Re-set the exception Handler
                    Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler);

                    Intent intent = new Intent(MainActivity.this, CrashActivity.class);
                    intent.putExtra("e", e);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    startActivity(intent);
                    System.exit(2);
                } catch (Exception ex) {
                    Log.e("Error", Arrays.toString(ex.getStackTrace()));
                }
            }
        });
    }

    public void replaceFragment(int fragmentNumberToReplace, boolean fade) {
        final LinearLayout oldContainer = currentFragmentContainer;

        switch (fragmentNumberToReplace) {
            case page_main:
                container_main.setVisibility(View.VISIBLE);
                currentFragmentContainer = container_main;
                break;
            case page_game:
                container_game.setVisibility(View.VISIBLE);
                currentFragmentContainer = container_game;
                fragment_game.start();
                break;
            case page_setup:
                container_setup.setVisibility(View.VISIBLE);
                currentFragmentContainer = container_setup;
                fragment_setup.startSetup();
                break;
        }

        currentFragment = fragmentNumberToReplace;

        if(fade) {
            oldContainer.animate()
                    .alpha(0f)
                    .setDuration(500)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            oldContainer.setVisibility(View.GONE);
                        }
                    })
                    .start();

            currentFragmentContainer.setAlpha(0f);
            currentFragmentContainer.animate()
                    .alpha(1f)
                    .setDuration(500)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            currentFragmentContainer.setAlpha(1f);
                        }
                    })
                    .start();
        } else {
            oldContainer.setVisibility(View.GONE);
        }
        invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if(currentFragment == page_main) {
            inflater.inflate(R.menu.actionbar_menu_mainscreen, menu);
        } else if (currentFragment == page_game) {
            inflater.inflate(R.menu.actionbar_menu_game, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.menu_settings) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            return true;
        } else if(item.getItemId() == R.id.menu_help){
            CardDialog.showMessageDialog(this, getString(R.string.dialog_explanations_title), getString(R.string.dialog_explanations_message), getString(R.string.btn_ok), null, null, null);
            return true;
        } else return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(currentFragmentContainer.equals(container_setup)) {
            fragment_setup.previousSetupPage();
        } else if(GameManager.isGameStarted()) { //Game is currently running, we ask the user if he wants to end the game
            if(!RecyclerViewManager.swipeEnabled) return; //This means that the "Game Ended" Screen is currently showing, we do not want to show the dialog during this

            CardDialog.showMessageDialog(this, getString(R.string.title_end_game_policies), null, getString(R.string.yes), new Runnable() {
                @Override
                public void run() {
                    fragment_game.displayEndGameOptions();
                }
            }, getString(R.string.no), null);
        } else{ //User is in the empty screen, end the activity
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //These functions set references to MainActivity to null, as not doing this would result in a memory leak
        GameEventsManager.destroy();
        PlayerListManager.destroy();
        JSONManager.destroy();
        FascistTrackSelectionManager.destroy();
        FascistTrackCreationDialog.destroy();
        ExceptionHandler.destroy();
        Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler);
        uncaughtExceptionHandler = null;
    }

    public void checkForBackups() {
        if(BackupManager.backupPresent()) {
            if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("autoRestoreBackup", false)) {
                restoreBackup();
            } else {
                CardDialog.showMessageDialog(this, getString(R.string.dialog_restore_title), getString(R.string.dialog_restore_msg), getString(R.string.yes), new Runnable() {
                    @Override
                    public void run() {
                        restoreBackup();
                    }
                }, getString(R.string.no), new Runnable() {
                    @Override
                    public void run() {
                        BackupManager.deleteBackup();
                    }
                });
            }
        }
    }

    private void restoreBackup() {
        PlayerListManager.setContext(MainActivity.this);
        BackupManager.restoreBackup();
        replaceFragment(page_game, true);
    }

    public LinearLayout getCurrentFragmentContainer() {
        return currentFragmentContainer;
    }

    public static class ProgressBarAnimation extends Animation{
        private ProgressBar progressBar;
        private float from;
        private float  to;

        public ProgressBarAnimation(ProgressBar progressBar, float from, float to) {
            super();
            this.progressBar = progressBar;
            this.from = from;
            this.to = to;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            float value = from + (to - from) * interpolatedTime;
            progressBar.setProgress((int) value);
        }

    }
}