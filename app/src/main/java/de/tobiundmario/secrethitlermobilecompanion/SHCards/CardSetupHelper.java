package de.tobiundmario.secrethitlermobilecompanion.SHCards;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.List;

import de.tobiundmario.secrethitlermobilecompanion.R;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.Claim;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.GameEventsManager;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.GameManager;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.PlayerListManager;

public final class CardSetupHelper {
    private CardSetupHelper() {}

    public static void lockPresidentSpinner(String presidentName, Spinner spinner) {
        int position = PlayerListManager.getPlayerPosition(presidentName);
        spinner.setSelection(position);
        if(!GameManager.gameTrack.isManualMode()) spinner.setEnabled(false);
    }

    public static ArrayAdapter<String> getPlayerNameAdapter(final Context context) {
        return new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_item, PlayerListManager.getAlivePlayerList()) {

            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);

                Typeface externalFont = ResourcesCompat.getFont(context, R.font.comfortaa_light);
                ((TextView) v).setTypeface(externalFont);

                return v;
            }


            public View getDropDownView(int position,  View convertView,  ViewGroup parent) {
                View v =super.getDropDownView(position, convertView, parent);

                Typeface externalFont = ResourcesCompat.getFont(context, R.font.comfortaa_light);
                ((TextView) v).setTypeface(externalFont);

                return v;
            }
        };
    }

    public static ArrayAdapter<String> getClaimAdapter(final Context context, List<String> data) {
        return new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_item, data) {

            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);

                Typeface externalFont = ResourcesCompat.getFont(context, R.font.comfortaa_light);
                TextView tv = (TextView) v;
                tv.setTypeface(externalFont);
                tv.setText(Claim.colorClaim(tv.getText().toString()), TextView.BufferType.SPANNABLE);

                return v;
            }


            public View getDropDownView(int position,  View convertView,  ViewGroup parent) {
                View v =super.getDropDownView(position, convertView, parent);

                Typeface externalFont = ResourcesCompat.getFont(context, R.font.comfortaa_light);
                TextView tv = (TextView) v;
                tv.setTypeface(externalFont);
                tv.setText(Claim.colorClaim(tv.getText().toString()), TextView.BufferType.SPANNABLE);

                return v;
            }
        };
    }

    public static ArrayAdapter<String> getPlayerNameAdapterWithDeadPlayer(final Context context, String deadPlayer) {
        ArrayList<String> playerList = PlayerListManager.getAlivePlayerList();
        playerList.add(deadPlayer);
        return new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_item, playerList) {

            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);

                Typeface externalFont = ResourcesCompat.getFont(context, R.font.comfortaa_light);
                ((TextView) v).setTypeface(externalFont);

                return v;
            }


            public View getDropDownView(int position,  View convertView,  ViewGroup parent) {
                View v =super.getDropDownView(position, convertView, parent);

                Typeface externalFont = ResourcesCompat.getFont(context, R.font.comfortaa_light);
                ((TextView) v).setTypeface(externalFont);

                return v;
            }
        };
    }

    /**
     * Creates an imageView selector, as e.g. seen in the Setup of a Legislative session. If the first image is clicked, the alpha of the second image is reduced, indicating a selection.
     * Furthermore, Colorstatelists can be provided, leading to Views e.g. Switches being colored in a specific way if an image is selected
     * @param image1 The first imageView
     * @param image2 The second imageView
     * @param cl1 The Color set to views in case image1 is selected
     * @param cl2 The color set to views in case image2 is selected
     * @param coloredViews The views that are to be colored
     */
    public static void setupImageViewSelector(final ImageView image1, final ImageView image2, final ColorStateList cl1, final ColorStateList cl2, final View[] coloredViews) {
        image1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                image1.setAlpha(1f);
                image2.setAlpha(0.2f);

                if(cl1 != null && coloredViews != null) colorViews(cl1, coloredViews);
            }
        });

        image2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                image2.setAlpha(1f);
                image1.setAlpha(0.2f);

                if(cl2 != null && coloredViews != null) colorViews(cl2, coloredViews);
            }
        });
    }

    /**
     * Colors an array of Views according to a ColorStateList
     * @param colorStateList The color that will be used
     * @param viewsToBeColored An array of views that will be colored. Supports Switches and Checkboxes
     */
    private static void colorViews (ColorStateList colorStateList, View[] viewsToBeColored) {
        for(View view : viewsToBeColored) {

            if(view instanceof CheckBox) ((CheckBox) view).setButtonTintList(colorStateList);
            else if (view instanceof Switch) {
                ((Switch) view).setThumbTintList(colorStateList);
                ((Switch) view).setTrackTintList(colorStateList);
            }

        }
    }

    /**
     * Provides an easy way to create a setup with multiple pages
     * @param setupPages An Array of pages, must be in correct order
     * @param btn_continue a reference to the next page button
     * @param btn_back a reference to the previous page button
     * @param cardSetupListeners A helper class containing multiple Listeners useful for creating a setup
     */
    public static void initialiseSetupPages(final View[] setupPages, View btn_continue, final View btn_back, final CardSetupListeners cardSetupListeners) {
        changeButtonText(btn_back, GameEventsManager.getContext().getString(R.string.btn_cancel));
        final int[] setupPage = {1};

        for(int i = 0; i < setupPages.length; i++) {
            setupPages[i].setVisibility((i == 0) ? View.VISIBLE : View.GONE);
        }

        btn_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cardSetupListeners.shouldPageTransitionOccurr(setupPage[0])) {
                    setupPage[0]++;
                    if (setupPage[0] == 2)
                        changeButtonText(btn_back, GameEventsManager.getContext().getString(R.string.back));
                    nextSetupPage(setupPage, setupPages, cardSetupListeners.getSetupFinishCondition(), cardSetupListeners.getOnSetupFinishedListener());
                    cardSetupListeners.triggerPageSetup(setupPage[0] - 1, setupPages);
                }
            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setupPage[0]--;
                previousSetupPage(setupPage, setupPages, btn_back, cardSetupListeners.getOnSetupCancelledListener());
            }
        });
    }

    private static void nextSetupPage(int[] setupPage, View[] setupPages, SetupFinishCondition setupFinishCondition, OnSetupFinishedListener onSetupFinishedListener) {
        final View oldPage, newPage;
        if(setupPage[0] <= setupPages.length && (setupFinishCondition == null || !setupFinishCondition.shouldSetupBeFinished(setupPage[0]))) {
            oldPage = setupPages[setupPage[0] - 2];
            newPage = setupPages[setupPage[0] - 1];

            //Animations
            Animation slideOutLeft = AnimationUtils.loadAnimation(GameEventsManager.getContext(), R.anim.slide_out_left);
            slideOutLeft.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    oldPage.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            Animation slideInRight = AnimationUtils.loadAnimation(GameEventsManager.getContext(), R.anim.slide_in_right);
            slideInRight.setFillAfter(true);
            oldPage.startAnimation(slideOutLeft);

            newPage.setVisibility(View.VISIBLE);
            newPage.startAnimation(slideInRight);

        } else {
            onSetupFinishedListener.onSetupFinished();
        }
    }

    private static void previousSetupPage(int[] setupPage, View[] setupPages, View btn_back, OnSetupCancelledListener onSetupCancelledListener) {
        final View oldPage, newPage;
        if(setupPage[0] >= 1) {
            oldPage = setupPages[setupPage[0]];
            newPage = setupPages[setupPage[0] - 1];

            //Animations
            Animation slideOutRight = AnimationUtils.loadAnimation(GameEventsManager.getContext(), R.anim.slide_out_right);
            slideOutRight.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }
                @Override
                public void onAnimationEnd(Animation animation) {
                    oldPage.setVisibility(View.GONE);
                }
                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            Animation slideInLeft = AnimationUtils.loadAnimation(GameEventsManager.getContext(), R.anim.slide_in_left);
            oldPage.startAnimation(slideOutRight);

            newPage.setVisibility(View.VISIBLE);
            newPage.startAnimation(slideInLeft);

            if(setupPage[0] == 1) changeButtonText(btn_back, GameEventsManager.getContext().getString(R.string.btn_cancel));
        } else {
            if(onSetupCancelledListener != null) onSetupCancelledListener.onSetupCancelled();
        }
    }

    private static void changeButtonText(View btn, String text) {
        if(btn instanceof Button) ((Button) btn).setText(text);
        else if(btn instanceof TextView) ((TextView) btn).setText(text);
    }
}