package bot;

public class Summoner {
    private String summonerId;
    private String summonerName;
    private String tier;
    private String rank;
    private int leaguePoints;
    private int positionValue;

    public Summoner() {
    }


    public String getSummonerId() {
        return summonerId;
    }

    public void setSummonerId(String summonerId) {
        this.summonerId = summonerId;
    }

    public String getSummonerName() {
        return summonerName;
    }

    public void setSummonerName(String summonerName) {
        this.summonerName = summonerName;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public int getLeaguePoints() {
        return leaguePoints;
    }

    public void setLeaguePoints(int leaguePoints) {
        this.leaguePoints = leaguePoints;
    }

    public int getPositionValue() {
        return positionValue;
    }

    public void setPositionValue(int positionValue) {
        this.positionValue = positionValue;
    }
}
