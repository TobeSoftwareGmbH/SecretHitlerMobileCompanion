package de.tobias.secrethitlermobilecompanion.SHClasses;

import android.content.Context;

import androidx.cardview.widget.CardView;

import de.tobias.secrethitlermobilecompanion.R;

public class ClaimEvent extends GameEvent {

    private String presidentName, chancellorName;
    private int presidentClaim, chancellorClaim;
    private int playedPolicy;
    private boolean vetoed;

    Context c;

    public ClaimEvent(String presidentName, String chancellorName, int presidentClaim, int chancellorClaim, int playedPolicy, boolean vetoed, Context context) {
        this.chancellorName = chancellorName;
        this.presidentName = presidentName;

        this.presidentClaim = presidentClaim;
        this.chancellorClaim = chancellorClaim;

        this.playedPolicy = playedPolicy;

        this.vetoed = vetoed;

        c = context;
    }

    public int getChancellorClaim() {
        return chancellorClaim;
    }

    public String getChancellorName() {
        return chancellorName;
    }

    public int getPlayedPolicy() {
        return playedPolicy;
    }

    public int getPresidentClaim() {
        return presidentClaim;
    }

    public String getPresidentName() {
        return presidentName;
    }

    public boolean isVetoed() {
        return vetoed;
    }

    @Override
    public String toString() {
        String playedPolicysp;
        if(playedPolicy == Claim.LIBERAL) {
            playedPolicysp = "<font color='blue'>" + c.getString(R.string.liberal) + "</font>";
        } else {
            playedPolicysp = "<font color='red'>" + c.getString(R.string.fascist) + "</font>";
        }

        String presidentNamecolored = "<font color='grey'>" + presidentName + "</font>";
        String chancellorNamecolored = "<font color='grey'>" + chancellorName + "</font>";

        return c.getString(R.string.claim_string, playedPolicysp, presidentNamecolored, Claim.getClaimString(c, presidentClaim), chancellorNamecolored, Claim.getClaimString(c, chancellorClaim));
    }

    @Override
    public void setupCard(CardView cardLayout) {
        //Do nothing
    }

}
