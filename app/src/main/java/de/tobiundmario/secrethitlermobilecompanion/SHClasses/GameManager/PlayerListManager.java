package de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;

import java.util.ArrayList;

import de.tobiundmario.secrethitlermobilecompanion.MainActivity;
import de.tobiundmario.secrethitlermobilecompanion.R;
import de.tobiundmario.secrethitlermobilecompanion.RecyclerViewAdapters.ModifiedDefaultItemAnimator;
import de.tobiundmario.secrethitlermobilecompanion.RecyclerViewAdapters.PlayerCardRecyclerViewAdapter;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.Claim;

public class PlayerListManager {
    private static ArrayList<String> playerList = new ArrayList<>();
    private static ArrayList<Integer> claimList = new ArrayList<>();
    private static ArrayList<Boolean> isDead = new ArrayList<>();

    private static Context c;
    private static PlayerCardRecyclerViewAdapter playerCardRecyclerViewAdapter;
    private static RecyclerView playerRecyclerView;

    private PlayerListManager() {}

    public static void initialise(RecyclerView playerCardList, Context context) {
        /*
        Called when pushing the "Create Game" Button. Initialises and resets all variables used
         */
        playerList = new ArrayList<>();
        claimList = new ArrayList<>();
        isDead = new ArrayList<>();

        playerCardRecyclerViewAdapter = new PlayerCardRecyclerViewAdapter(playerList, context);
        playerCardList.setAdapter(playerCardRecyclerViewAdapter);
        playerRecyclerView = playerCardList;
        setupSwipeToDelete();

        c = context;
    }

    public static void setContext(Context c) {
        PlayerListManager.c = c;
    }

    public static void changeRecyclerView(RecyclerView playerCardList) {
        if(playerRecyclerView != null) playerRecyclerView.setAdapter(null);

        playerCardRecyclerViewAdapter = new PlayerCardRecyclerViewAdapter(playerList, c);
        playerCardList.setAdapter(playerCardRecyclerViewAdapter);
        playerCardList.setItemAnimator(new ModifiedDefaultItemAnimator());
        playerRecyclerView = playerCardList;
        setupSwipeToDelete();
    }

    public static void destroy() {
        c = null;
        playerCardRecyclerViewAdapter = null;
        playerRecyclerView = null;
        playerList = null;
        claimList = null;
        isDead = null;
    }


    public static void addPlayer(String name) {
        /*
        This function adds a player from the player list
         */
        if(playerList == null) {
            playerList = new ArrayList<>();
            claimList = new ArrayList<>();
            isDead = new ArrayList<>();
        }

        playerList.add(name);

        //Also add an additional entry to our claim- and dead-lists
        claimList.add(Claim.NO_CLAIM);
        isDead.add(false);

        //Notify the adapter, if it exists. This method is also called during a game restoration from backup. In that case, no layout exists during the restoration => nothing to notify
        if(playerRecyclerView != null) playerCardRecyclerViewAdapter.notifyItemInserted(playerList.size() - 1);
    }

    public static void addPlayer(String name, int position) {
        /*
        This function adds a player from the player list
         */
        if(playerList == null) {
            playerList = new ArrayList<>();
            claimList = new ArrayList<>();
            isDead = new ArrayList<>();
        }

        playerList.add(position, name);

        //Also add an additional entry to our claim- and dead-lists
        claimList.add(position, Claim.NO_CLAIM);
        isDead.add(position, false);

        playerCardRecyclerViewAdapter.notifyItemInserted(position);
    }

    public static void removePlayer(String player) {
        /*
        This function removes a player from the player list
         */
        if(!GameManager.isGameStarted()) { //As the player list shouldn't be changed once the game starts, we check here
            int index = getPlayerPosition(player);

            playerList.remove(player);
            claimList.remove(index);
            isDead.remove(index);

            playerCardRecyclerViewAdapter.notifyItemRemoved(index);
        }
    }

    public static void setClaim(String player, int playerPartyMemberShip) {
        int position = getPlayerPosition(player);
        claimList.set(position, playerPartyMemberShip);
        if(playerCardRecyclerViewAdapter != null) playerCardRecyclerViewAdapter.notifyItemChanged(position);
    }

    public static boolean playerAlreadyExists(String name) {
        return playerList.contains(name);
    }

    public static int getPlayerPosition(String name) { return playerList.indexOf(name); }

    public static PlayerCardRecyclerViewAdapter getplayerCardRecyclerViewAdapter() {
        return playerCardRecyclerViewAdapter;
    }

    public static void setClaimImage(CardView cardView, int playerPartyMemberShip) {
        /*
        Receives the Player-card as an input and then adds the claim-symbol to it. Is called by the RecyclerViewAdapter
         */

        Drawable membershipDrawable;
        if(playerPartyMemberShip == Claim.LIBERAL) membershipDrawable = ContextCompat.getDrawable(c, R.drawable.membership_liberal);
        else if (playerPartyMemberShip == Claim.FASCIST) membershipDrawable = ContextCompat.getDrawable(c, R.drawable.membership_fascist);
        else membershipDrawable = ContextCompat.getDrawable(c, R.drawable.secret_role);

        ImageView iv_membership = cardView.findViewById(R.id.img_secretRole);
        iv_membership.setImageDrawable(membershipDrawable);
        if(playerPartyMemberShip == Claim.NO_CLAIM) {
            iv_membership.setAlpha((float) 1);
        } else {
            iv_membership.setAlpha((float) 0.4);
        }

        TextView tv_questionMark = cardView.findViewById(R.id.tv_questionmark);
        if(playerPartyMemberShip == Claim.NO_CLAIM) {
            tv_questionMark.setVisibility(View.GONE);
        } else {
            tv_questionMark.setText("?");
            tv_questionMark.setVisibility(View.VISIBLE);
        }
    }

    public static void setDeadSymbol(CardView cardView) {
        /*
        Receives the Player-card as an input and then adds the dead-symbol to it. Is called by the RecyclerViewAdapter
         */
        ImageView ivmembership = cardView.findViewById(R.id.img_secretRole);
        ivmembership.setImageDrawable(c.getDrawable(R.drawable.dead));
        ivmembership.setAlpha((float) 1);

        TextView tvquestionMark = cardView.findViewById(R.id.tv_questionmark);
        tvquestionMark.setVisibility(View.INVISIBLE);
    }

    public static ArrayList<String> getPlayerList() {
        return playerList;
    }

    public static ArrayList<String> getAlivePlayerList() {
        /*
        Returns a list of all alive players. Is used by the Spinners in the setup cards, as no dead players should be selectable.
         */
        ArrayList<String> result = new ArrayList<>();
        for(int i = 0; i < playerList.size(); i++) {
            if(!isDead(i)) result.add(playerList.get(i));
        }
        return result;
    }

    public static ArrayList<Integer> getMembershipClaims() {
        return claimList;
    }


    public static void setAsDead(String playerName, boolean dead) {
        int position = playerList.indexOf(playerName);
        isDead.set(position, dead);
        if(playerCardRecyclerViewAdapter != null) playerCardRecyclerViewAdapter.notifyItemChanged(position);

        if(getAlivePlayerCount() <= 2) {
            ((MainActivity) c).fragment_game.displayEndGameOptions();
        }
    }

    public static int getAlivePlayerCount() {
        return getAlivePlayerList().size();
    }

    public static void setPlayerList(ArrayList<String> playerList) {
        initialise(playerRecyclerView, c);

        for(String player: playerList) {
            addPlayer(player);
        }
    }

    public static boolean isDead(int playerPosition) {
        return isDead.get(playerPosition);
    }

    public static String boldPlayerName(String name) {
        return "<b>" + name + "</b>";
    }

    public static JSONArray getPlayerListJSON() {
        JSONArray arr = new JSONArray();

        for(String player : playerList) {
            arr.put(player);
        }

        return arr;
    }

    private static void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.UP) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();
                if(position > playerList.size() - 1) {
                    playerCardRecyclerViewAdapter.notifyItemChanged(position);
                    return;
                }

                final String playerName = playerList.get(position);

                removePlayer(playerName);
                playerCardRecyclerViewAdapter.notifyItemRemoved(position);

                Snackbar.make(((MainActivity) c).getCurrentFragmentContainer(), c.getString(R.string.snackbar_message_player_removed), BaseTransientBottomBar.LENGTH_SHORT)
                        .setAction(c.getString(R.string.undo), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                addPlayer(playerName, position);
                            }
                        })
                        .setAnchorView(((MainActivity) c).findViewById(R.id.setup_buttons))
                        .show();
            }

            @Override
            public boolean isItemViewSwipeEnabled() {
                return !GameManager.isGameStarted();
            }

        };

        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(playerRecyclerView);
    }
}
