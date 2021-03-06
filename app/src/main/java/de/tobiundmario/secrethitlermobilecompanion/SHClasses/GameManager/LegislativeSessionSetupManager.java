package de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import de.tobiundmario.secrethitlermobilecompanion.R;
import de.tobiundmario.secrethitlermobilecompanion.SHCards.CardSetupHelper;
import de.tobiundmario.secrethitlermobilecompanion.SHCards.CardSetupListeners;
import de.tobiundmario.secrethitlermobilecompanion.SHCards.OnSetupCancelledListener;
import de.tobiundmario.secrethitlermobilecompanion.SHCards.OnSetupFinishedListener;
import de.tobiundmario.secrethitlermobilecompanion.SHCards.SetupFinishCondition;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.Claim;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.ClaimEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.LegislativeSession;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.VoteEvent;

public class LegislativeSessionSetupManager {

    private CheckBox cb_vetoed;
    private Switch sw_votingoutcome;

    private Spinner presSpinner;
    private Spinner chancSpinner;
    private Spinner presClaimSpinner;
    private Spinner chancClaimSpinner;

    private ImageView icon_fascist;
    private ImageView icon_liberal;

    private ImageView icon_ja, icon_nein;

    private LinearLayout ll_policyplayed;
    private Button btn_continue;

    private Context context;
    private LegislativeSession legislativeSession;

    public LegislativeSessionSetupManager(LegislativeSession legislativeSession, Context context)  {
        this.legislativeSession = legislativeSession;
        this.context = context;
    }


    private void initialiseLayoutVariables(CardView cardView, boolean edit) {
        if(edit) {
            cb_vetoed = cardView.findViewById(R.id.checkBox_policy_vetoed);
            sw_votingoutcome = cardView.findViewById(R.id.switch_vote_outcome);

            presSpinner = cardView.findViewById(R.id.spinner_president);
            chancSpinner = cardView.findViewById(R.id.spinner_chancellor);
            presClaimSpinner = cardView.findViewById(R.id.spinner_pres_claim);
            chancClaimSpinner = cardView.findViewById(R.id.spinner_chanc_claim);

            ll_policyplayed = cardView.findViewById(R.id.ll_policy_outcome);
            icon_fascist = cardView.findViewById(R.id.img_policy_fascist);
            icon_liberal = cardView.findViewById(R.id.img_policy_liberal);
            btn_continue = cardView.findViewById(R.id.btn_setup_forward);
        } else {
            cardView.findViewById(R.id.legacy).setVisibility(View.GONE);
            cardView.findViewById(R.id.initial_setup).setVisibility(View.VISIBLE);

            presSpinner = cardView.findViewById(R.id.spinner_president_selection);
            chancSpinner = cardView.findViewById(R.id.spinner_chancellor_selection);

            presClaimSpinner = cardView.findViewById(R.id.spinner_president_claim);
            chancClaimSpinner = cardView.findViewById(R.id.spinner_chancellor_claim);

            icon_fascist = cardView.findViewById(R.id.icon_policyf);
            icon_liberal = cardView.findViewById(R.id.icon_policyl);

            icon_ja = cardView.findViewById(R.id.icon_voting_ja);
            icon_nein = cardView.findViewById(R.id.icon_voting_nein);

            cb_vetoed = cardView.findViewById(R.id.checkBox_played_policy_vetoed);
        }
    }

    public void initialiseSetupCard(CardView cardView) {
        if(legislativeSession.isEditing) {
            initialiseLayoutVariables(cardView,true);
            initialiseEditCard(cardView);
        } else {
            initialiseLayoutVariables(cardView, false);
            initialiseSetup(cardView);
        }

        ArrayAdapter<String> playerListadapter = CardSetupHelper.getArrayAdapter(context, PlayerListManager.getAlivePlayerList(), false);
        playerListadapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        presSpinner.setAdapter(playerListadapter);

        //Attempting to get the last Legislative Session and setting the next player in order as president. If this is the first LegSession, the first player will be selected
        LegislativeSession lastSession = LegislativeSessionManager.getLastLegislativeSession();
        int newPresidentPos = getNewPresidentPosition(lastSession);
        int newChancellorPos = getNewChancellorPosition(newPresidentPos);
        presSpinner.setSelection(newPresidentPos);

        chancSpinner.setAdapter(playerListadapter);
        chancSpinner.setSelection(newChancellorPos); //Setting a different item on the chancellor spinner so they don't have the same name at the beginning

        final ArrayAdapter<String> presClaimListadapter = CardSetupHelper.getArrayAdapter(context, Claim.getPresidentClaims(), true);
        presClaimListadapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        presClaimSpinner.setAdapter(presClaimListadapter);

        ArrayAdapter<String> chancClaimListadapter = CardSetupHelper.getArrayAdapter(context, Claim.getChancellorClaims(), true);
        chancClaimListadapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        chancClaimSpinner.setAdapter(chancClaimListadapter);
    }
    
    private int getNewPresidentPosition(LegislativeSession lastLegislativeSession) {
        int newPresidentPos = 0;
        if (lastLegislativeSession != null) {
            newPresidentPos = PlayerListManager.getPlayerPosition(lastLegislativeSession.getVoteEvent().getPresidentName()) + 1;
            if (newPresidentPos == PlayerListManager.getPlayerList().size()) newPresidentPos = 0;
        }
        return newPresidentPos;
    }
    
    private int getNewChancellorPosition(int newPresidentPos) {
        return  (newPresidentPos == PlayerListManager.getPlayerList().size() - 1) ? 0 : newPresidentPos + 1;
    }

    private void initialiseSetup(CardView cardView) {
        icon_ja.setAlpha(1f);
        icon_nein.setAlpha(0.2f);

        //Setting up ImageViewSelectors
        CardSetupHelper.setupImageViewSelector(new ImageView[] {icon_liberal, icon_fascist}, new ColorStateList[] {ColorStateList.valueOf(context.getColor(R.color.colorLiberal)), ColorStateList.valueOf(context.getColor(R.color.colorFascist))}, new View[]{cb_vetoed}, new Runnable[] {null, null});
        CardSetupHelper.setupImageViewSelector(new ImageView[] {icon_ja, icon_nein}, new ColorStateList[] {null, null}, null, new Runnable[] {null, null});

        CardSetupListeners cardSetupListeners = new CardSetupListeners();

        cardSetupListeners.setOnSetupFinishedListener(new OnSetupFinishedListener() {
            @Override
            public void onSetupFinished() {
                setupFinished();
            }
        });

        cardSetupListeners.setOnSetupCancelledListener(new OnSetupCancelledListener() {
            @Override
            public void onSetupCancelled() {
                GameEventsManager.remove(legislativeSession);
            }
        });

        cardSetupListeners.setSetupFinishCondition(new SetupFinishCondition() {
            @Override
            public boolean shouldSetupBeFinished(int newPage) {
                return icon_nein.getAlpha() == 1f && newPage == 3;
            }
        });

        CardSetupHelper.initialiseSetupPages(new View[]{cardView.findViewById(R.id.page1_selection), cardView.findViewById(R.id.page2_voting), cardView.findViewById(R.id.page3_policies), cardView.findViewById(R.id.page4_claims)}, (Button) cardView.findViewById(R.id.btn_setup_forward), (Button) cardView.findViewById(R.id.btn_setup_back), cardSetupListeners);
    }

    private void setupFinished() {
        boolean voteRejected = icon_nein.getAlpha() == 1.0f;
        String presName = (String) presSpinner.getSelectedItem();
        String chancName = (String) chancSpinner.getSelectedItem();

        VoteEvent newVoteEvent = new VoteEvent(presName, chancName, voteRejected ? VoteEvent.VOTE_FAILED : VoteEvent.VOTE_PASSED);
        ClaimEvent newClaimEvent;

        if (voteRejected) {
            newClaimEvent = null;
        } else {
            int presClaim = Claim.getClaimInt((String) presClaimSpinner.getSelectedItem());
            int chancClaim = Claim.getClaimInt((String) chancClaimSpinner.getSelectedItem());

            int playedPolicy = (icon_fascist.getAlpha() == (float) 1) ? Claim.FASCIST : Claim.LIBERAL;
            boolean vetoed = cb_vetoed.isChecked();

            newClaimEvent = new ClaimEvent(presClaim, chancClaim, playedPolicy, vetoed);
        }
        legislativeSession.leaveSetupPhase(newClaimEvent, newVoteEvent);
    }

    private void initialiseEditCard(CardView cardView) {
        cardView.findViewById(R.id.legacy).setVisibility(View.VISIBLE);
        cardView.findViewById(R.id.initial_setup).setVisibility(View.GONE);

        //Setting up Spinners
        presSpinner = cardView.findViewById(R.id.spinner_president);
        chancSpinner = cardView.findViewById(R.id.spinner_chancellor);

        presClaimSpinner = cardView.findViewById(R.id.spinner_pres_claim);

        chancClaimSpinner = cardView.findViewById(R.id.spinner_chanc_claim);

        //When the switch is changed, we want certain UI elements to disappear
        sw_votingoutcome.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ll_policyplayed.setVisibility(View.GONE);
                    cb_vetoed.setVisibility(View.GONE);
                    chancClaimSpinner.setVisibility(View.GONE);
                    presClaimSpinner.setVisibility(View.GONE);
                } else {
                    ll_policyplayed.setVisibility(View.VISIBLE);
                    cb_vetoed.setVisibility(View.VISIBLE);
                    chancClaimSpinner.setVisibility(View.VISIBLE);
                    presClaimSpinner.setVisibility(View.VISIBLE);
                }
            }
        });

        //Setting up the OnClickListeners for the ImageViews
        CardSetupHelper.setupImageViewSelector(new ImageView[] {icon_liberal, icon_fascist}, new ColorStateList[] {ColorStateList.valueOf(context.getColor(R.color.colorLiberal)), ColorStateList.valueOf(context.getColor(R.color.colorFascist))}, new View[]{cb_vetoed, sw_votingoutcome}, new Runnable[] {null, null});

        btn_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (presSpinner.getSelectedItem().equals(chancSpinner.getSelectedItem())) {
                    Toast.makeText(context, context.getString(R.string.err_names_cannot_be_the_same), Toast.LENGTH_LONG).show();
                } else processEdit(sw_votingoutcome.isChecked(), (icon_fascist.getAlpha() == (float) 1) ? Claim.FASCIST : Claim.LIBERAL, cb_vetoed.isChecked());
            }
        });
    }

    private void processEdit(boolean voteRejected, int playedPolicy, boolean vetoed) {
        String presName = (String) presSpinner.getSelectedItem();
        String chancName = (String) chancSpinner.getSelectedItem();

        VoteEvent newVoteEvent = new VoteEvent(presName, chancName, voteRejected ? VoteEvent.VOTE_FAILED : VoteEvent.VOTE_PASSED);
        ClaimEvent newClaimEvent = createNewClaimEvent(voteRejected, playedPolicy, vetoed);

        VoteEvent oldVoteEvent = legislativeSession.getVoteEvent();
        ClaimEvent oldClaimEvent = legislativeSession.getClaimEvent();

        if (legislativeSession.isEditing) { //We are editing the card, we need to process the changes (e.g. update the policy count)
            LegislativeSessionManager.processLegislativeSessionEdit(legislativeSession, newClaimEvent, newVoteEvent);
        }

        legislativeSession.leaveSetupPhase(newClaimEvent, newVoteEvent);
        if (LegislativeSessionManager.trackActionRequired(oldClaimEvent, newClaimEvent, oldVoteEvent, newVoteEvent)) {
            LegislativeSessionManager.addTrackAction(legislativeSession, false);
        }
    }

    private ClaimEvent createNewClaimEvent(boolean voteRejected, int playedPolicy, boolean vetoed) {
        if (voteRejected) {
            return null;
        } else {
            int presClaim = Claim.getClaimInt((String) presClaimSpinner.getSelectedItem());
            int chancClaim = Claim.getClaimInt((String) chancClaimSpinner.getSelectedItem());
            return new ClaimEvent(presClaim, chancClaim, playedPolicy, vetoed);
        }
    }
}
