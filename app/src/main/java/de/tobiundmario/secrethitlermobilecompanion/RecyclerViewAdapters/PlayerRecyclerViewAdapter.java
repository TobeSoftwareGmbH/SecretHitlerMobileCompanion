package de.tobiundmario.secrethitlermobilecompanion.RecyclerViewAdapters;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import de.tobiundmario.secrethitlermobilecompanion.R;
import de.tobiundmario.secrethitlermobilecompanion.SHCards.CardDialog;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameLog;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.PlayerList;

public class PlayerRecyclerViewAdapter extends RecyclerView.Adapter<PlayerRecyclerViewAdapter.PlayerCardViewHolder> {

    ArrayList<String> players;
    Context context;
    private ArrayList<String> hiddenPlayers = new ArrayList<>();

    private static int ADD_BUTTON = 2;
    private static int ADD_BUTTON_POSITION_ONE = 3;
    private static int NORMAL = 1;
    private static int FIRST_CARD = 0;

    public PlayerRecyclerViewAdapter(ArrayList<String> players, Context context) {
        this.players = players;
        this.context = context;
    }

    public static class PlayerCardViewHolder extends RecyclerView.ViewHolder {
        CardView cv;

        PlayerCardViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cardView);
        }
    }

    @Override
    public int getItemCount() {
        if(GameLog.isGameStarted()) return players.size();
        else return players.size() + 1;
    }

    @Override
    public PlayerCardViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_player_list_single_player, viewGroup, false);

        if(type == ADD_BUTTON) {
            v.setClickable(true);
            v.setFocusable(true);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CardDialog.showInputDialog(context, context.getString(R.string.title_input_player_name), context.getString(R.string.hint_player_name), context.getString(R.string.btn_ok), new CardDialog.InputDialogSubmittedListener() {
                        @Override
                        public void onInputDialogSubmitted(EditText inputField, Dialog rootDialog) {
                            String playerName = inputField.getText().toString();
                            if(PlayerList.playerAlreadyExists(playerName)) {
                                inputField.setError(context.getString(R.string.error_player_already_exists));
                                return;
                            }
                            if(playerName.matches("")) {
                                rootDialog.dismiss();
                                return;
                            }
                            PlayerList.addPlayer(playerName);
                            rootDialog.dismiss();
                        }
                    }, context.getString(R.string.dialog_mismatching_claims_btn_cancel), null);
                }
            });

            TextView tv_plus = v.findViewById(R.id.tv_addplayer);
            tv_plus.setVisibility(View.VISIBLE);

            ImageView iv_symbol = v.findViewById(R.id.img_secretRole);
            iv_symbol.setAlpha((float) 0.5);

            TextView tvPlayerName = v.findViewById(R.id.tv_playerName);
            tvPlayerName.setText(context.getString(R.string.new_player));
        }

        if(type == FIRST_CARD || type == ADD_BUTTON_POSITION_ONE) {
            Resources r = context.getResources();
            int px = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    16,
                    r.getDisplayMetrics()
            );

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) v.getLayoutParams();
            params.leftMargin = px;
            v.setLayoutParams(params);
        }

        PlayerCardViewHolder cardViewHolder = new PlayerCardViewHolder(v);
        return cardViewHolder;
    }

    @Override
    public void onBindViewHolder(PlayerCardViewHolder cardViewHolder, int i) {
        if(cardViewHolder.getItemViewType() == ADD_BUTTON || cardViewHolder.getItemViewType() == ADD_BUTTON_POSITION_ONE) return;

        CardView cardView = cardViewHolder.cv;

        final String player = players.get(i);
        TextView tvPlayerName = cardView.findViewById(R.id.tv_playerName);
        tvPlayerName.setText(player);


        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(GameLog.isGameStarted()) {
                    CardView cv = (CardView) v;

                    if (cv.getAlpha() == 1.0) { //if it is unselected, select it
                        cv.setAlpha((float) 0.5);
                        hiddenPlayers.add(player); //add it to the list of hidden players
                    } else { //if it is selected (Alpha is smaller than 1), remove it from the list and reset alpha
                        cv.setAlpha(1);
                        hiddenPlayers.remove(player);
                    }
                    GameLog.blurEventsInvolvingHiddenPlayers(hiddenPlayers); //Tell GameLog to update the list of which cards to blur
                }
            }
        });

        cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(!GameLog.isGameStarted()) {
                    //In the setup phase, we want to give the user the option to remove a user
                    CardDialog.showMessageDialog(context, context.getString(R.string.are_you_sure), context.getString(R.string.delete_player_msg, player), context.getString(R.string.yes), new Runnable() {
                        @Override
                        public void run() {
                            PlayerList.removePlayer(player);
                        }
                    }, context.getString(R.string.no), null);
                }
                return false;
            }
        });
    }


    @Override
    public int getItemViewType(int position) {
        if(position >= players.size()) return (position == 0) ? ADD_BUTTON_POSITION_ONE : ADD_BUTTON;
        else return (position == 0) ? FIRST_CARD : NORMAL;
    }

    @Override
    public void onViewAttachedToWindow(@NonNull PlayerCardViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if(holder.getItemViewType() == ADD_BUTTON || !GameLog.isGameStarted()) return;

        int position = holder.getLayoutPosition();
        int claim = PlayerList.getMembershipClaims().get(position);

        CardView cv = holder.cv;
        if(PlayerList.isDead(position)) {
            PlayerList.setDeadSymbol(cv);
            return;
        }
        PlayerList.setClaimImage(cv, claim);
    }
}

