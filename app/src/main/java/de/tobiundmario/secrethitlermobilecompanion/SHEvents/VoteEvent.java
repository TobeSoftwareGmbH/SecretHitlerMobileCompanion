package de.tobiundmario.secrethitlermobilecompanion.SHEvents;

public class VoteEvent {

    private String presidentName, chancellorName;
    private int votingResult;

    public static final int VOTE_PASSED = 1;
    public static final int VOTE_FAILED = 0;

    public VoteEvent(String presidentName, String chancellorName, int votingResult) {
        this.presidentName = presidentName;
        this.chancellorName = chancellorName;

        this.votingResult = votingResult;
    }

    public boolean isRejected() {
        return votingResult == VOTE_FAILED;
    }

    public String getPresidentName() {
        return presidentName;
    }

    public String getChancellorName() {
        return chancellorName;
    }

    public int getVotingResult() {
        return votingResult;
    }
}
