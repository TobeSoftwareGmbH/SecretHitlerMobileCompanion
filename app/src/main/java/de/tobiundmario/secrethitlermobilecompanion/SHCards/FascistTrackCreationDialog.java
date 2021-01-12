package de.tobiundmario.secrethitlermobilecompanion.SHCards;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONException;

import de.tobiundmario.secrethitlermobilecompanion.ExceptionHandler;
import de.tobiundmario.secrethitlermobilecompanion.R;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.FascistTrack;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.SharedPreferencesManager;
import de.tobiundmario.secrethitlermobilecompanion.SpinnerAdapters.TrackActionSpinnerAdapter;

public class FascistTrackCreationDialog {

    private static CheckBox cb_manualMode;
    private static TextView tvPositive;
    private static TextView tvNegative;

    private static EditText input_name;
    private static EditText input_fpolicies;
    private static EditText input_lpolicies;
    private static EditText input_electionTrackerLength;
    private static TextView title_eTrackerLength;

    private static LinearLayout ll_actions;

    public static void destroy() {
        cb_manualMode = null;
        tvPositive = null;
        tvNegative = null;

        input_name = null;
        input_fpolicies = null;
        input_lpolicies = null;
        input_electionTrackerLength = null;
        title_eTrackerLength = null;
        ll_actions = null;
    }

    public static void showTrackCreationDialog(final Context c) {
        final CardDialog.CustomDialog customDialog = CardDialog.createDialog(c, c.getString(R.string.new_fascistTrack), null, c.getString(R.string.btn_continue), null, c.getString(R.string.btn_cancel), null, CardDialog.trackCreation);

        final Dialog dialog = customDialog.getDialog();
        final View dialogView = customDialog.getContent();

        final View[] pages = new View[] {dialogView.findViewById(R.id.container_general) , dialogView.findViewById(R.id.container_actions)};
        pages[1].setVisibility(View.GONE);
        pages[0].setVisibility(View.VISIBLE);

        initialiseLayout(dialogView);
        initialiseSetup(pages, customDialog, c);

        cb_manualMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    tvPositive.setText(c.getString(R.string.done));

                    input_electionTrackerLength.setVisibility(View.GONE);
                    title_eTrackerLength.setVisibility(View.GONE);
                } else {
                    tvPositive.setText(c.getString(R.string.btn_continue));

                    input_electionTrackerLength.setVisibility(View.VISIBLE);
                    title_eTrackerLength.setVisibility(View.VISIBLE);
                }
            }
        });

        dialog.show();
    }

    private static void initialiseSetup(View[] pages, final CardDialog.CustomDialog customDialog, final Context c) {
        CardSetupListeners cardSetupListeners = new CardSetupListeners();

        cardSetupListeners.setSetupFinishCondition(new SetupFinishCondition() {
            @Override
            public boolean shouldSetupBeFinished(int newPage) {
                return cb_manualMode.isChecked() && !errorsOnSetupPage(c);
            }
        });

        cardSetupListeners.setOnSetupFinishedListener(new OnSetupFinishedListener() {
            @Override
            public void onSetupFinished() {
                createTrack(c);
                customDialog.getDialog().dismiss();
            }
        });

        cardSetupListeners.setSetupPageOpenedListeners(new SetupPageOpenedListener[] {null, nextSetupPage(c)});

        CardSetupHelper.initialiseSetupPages(pages, tvPositive, tvNegative, cardSetupListeners);
    }

    private static void createTrack(Context c) {
        FascistTrack fascistTrack = new FascistTrack();

        String name = input_name.getText().toString();
        fascistTrack.setName(name);

        int fpolicies = Integer.parseInt(input_fpolicies.getText().toString());
        fascistTrack.setFasPolicies(fpolicies);
        int lpolicies = Integer.parseInt(input_lpolicies.getText().toString());
        fascistTrack.setLibPolicies(lpolicies);

        boolean manual = cb_manualMode.isChecked();

        if(!manual) {
            int[] actions = new int[fpolicies];
            for (int i = 0; i < ll_actions.getChildCount(); i++) {
                Spinner spinnerAtPos = (Spinner) ll_actions.getChildAt(i);

                int selection = (int) spinnerAtPos.getSelectedItem();
                actions[i] = selection;
            }
            fascistTrack.setActions(actions);

            fascistTrack.setElectionTrackerLength(Integer.parseInt(input_electionTrackerLength.getText().toString()));
        } else {
            fascistTrack.setManualMode(true);
        }

        try {
            SharedPreferencesManager.writeFascistTrack(fascistTrack, c);
        } catch (JSONException e) {
            ExceptionHandler.showErrorSnackbar(e, "CardDialog.showTrackCreationDialog() (SharedPreferencesManager.writeFascistTrack())");
        }
    }

    private static SetupPageOpenedListener nextSetupPage(final Context c) {
        return new SetupPageOpenedListener() {
            @Override
            public boolean onSetupPageOpened(int pageNumber, View pageView) {
                if(errorsOnSetupPage(c)) return false;

                //Create the LayoutParams for the spinners
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                params.bottomMargin = 50;

                //We now check if there are too little or too many spinners in the layout
                int fascistPolicies = Integer.parseInt(input_fpolicies.getText().toString());
                int children = ll_actions.getChildCount();

                if(children > fascistPolicies) {
                    //Too many spinners, we need to remove some
                    for (int i = children - 1; i > fascistPolicies - 1; i--) {
                        ll_actions.removeViewAt(i);
                    }
                } else if(children < fascistPolicies) {
                    //Too little spinners, we add some
                    for (int i = children; i < fascistPolicies; i++) {
                        Spinner spinner = new Spinner(c);
                        spinner.setAdapter(new TrackActionSpinnerAdapter(c));
                        spinner.setLayoutParams(params);

                        spinner.setDropDownWidth(spinner.getLayoutParams().width);

                        ll_actions.addView(spinner);
                    }
                }
                return true;
            }
        };
    }

    private static boolean errorsOnSetupPage(Context c) {
        boolean error = false;
        String name = input_name.getText().toString();
        String fpolicies = input_fpolicies.getText().toString();
        String lpolicies = input_lpolicies.getText().toString();
        String eTrackerLength = input_electionTrackerLength.getText().toString();
        boolean manual = cb_manualMode.isChecked();

        if(name.equals("")) {
            input_name.setError(c.getString(R.string.cannot_be_empty));
            error = true;
        }

        if(fpolicies.equals("") || Integer.parseInt(fpolicies) <= 0) {
            input_fpolicies.setError(c.getString(R.string.error_invalid_value));
            error = true;
        }

        if(lpolicies.equals("") || Integer.parseInt(lpolicies) <= 0) {
            input_lpolicies.setError(c.getString(R.string.error_invalid_value));
            error = true;
        }

        if (!manual && (eTrackerLength.equals("") || Integer.parseInt(eTrackerLength) <= 0)) {
            input_electionTrackerLength.setError(c.getString(R.string.error_invalid_value));
            error = true;
        }

        return error;
    }

    private static void initialiseLayout(View dialogView) {
        cb_manualMode = dialogView.findViewById(R.id.checkBox_manualMode);
        tvPositive = dialogView.findViewById(R.id.tv_positive);
        tvNegative = dialogView.findViewById(R.id.tv_negative);

        input_name = dialogView.findViewById(R.id.et_trackName);
        input_fpolicies = dialogView.findViewById(R.id.et_fpolicies);
        input_lpolicies = dialogView.findViewById(R.id.et_lpolicies);
        input_electionTrackerLength = dialogView.findViewById(R.id.et_electionTrackerLength);
        title_eTrackerLength = dialogView.findViewById(R.id.et_title_eTracker);

        ll_actions = dialogView.findViewById(R.id.actions);
    }

}