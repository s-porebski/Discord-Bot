package bot;

public class Match {
    private Account account;
    private Long gameId;

    public Match(Account account, Long gameId) {
        this.account = account;
        this.gameId = gameId;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }
}
