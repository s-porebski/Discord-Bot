package bot.database;

import bot.Main;
import bot.NotFoundException;
import bot.Summoner;
import bot.service.HttpService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static bot.database.DatabaseManager.getConnection;

public class SummonersDatabase {
    private static String RIOT_API_TOKEN = Main.RIOT_API_TOKEN;
    List<Summoner> summoners = new ArrayList<>();


    public SummonersDatabase() {
    }

    public static void createTable() {
        try (Connection connection = getConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS summoners(" +
                        "summoner_id TEXT PRIMARY KEY)");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }

    public List<Summoner> getSummoners() {
        getDataFromAPI();
        sort();
        return summoners;
    }

    public void insertSummonerIdToDatabase(String summonerName) throws NotFoundException {
            String uri = "https://eun1.api.riotgames.com/lol/summoner/v4/summoners/by-name/" + summonerName.replace(" ", "%20") + "?api_key=" + RIOT_API_TOKEN;
            HttpResponse<String> response = HttpService.GetResponse(uri);
            if (response.statusCode() == 404) {
                throw new NotFoundException();
            }
            JsonObject summonerObject = JsonParser.parseString(response.body()).getAsJsonObject();
            String summonerId = summonerObject.get("id").getAsString();
        try (Connection con = getConnection()) {
            try (PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO summoners (summoner_id) VALUES (?)")) {
                preparedStatement.setString(1, summonerId);

                preparedStatement.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
    public void deleteSummonerIdFromDatabase(String summonerName) throws NotFoundException {
        String uri = "https://eun1.api.riotgames.com/lol/summoner/v4/summoners/by-name/" + summonerName.replace(" ", "%20") + "?api_key=" + RIOT_API_TOKEN;
        HttpResponse<String> response = HttpService.GetResponse(uri);
        if (response.statusCode() == 404) {
            throw new NotFoundException();
        }
        JsonObject summonerObject = JsonParser.parseString(response.body()).getAsJsonObject();
        String summonerId = summonerObject.get("id").getAsString();
        try (Connection con = getConnection()) {
            try (PreparedStatement preparedStatement = con.prepareStatement("DELETE FROM summoners WHERE summoner_id = ?")) {
                preparedStatement.setString(1, summonerId);

                preparedStatement.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }


    public void getDataFromAPI() {
        try (Connection connection = getConnection()) {
            try (Statement statement = connection.createStatement()) {
                try (ResultSet resultSet = statement.executeQuery("SELECT * FROM summoners")){
                    while (resultSet.next()) {
                        String summonerId = resultSet.getString("summoner_id");

                        String uri = "https://eun1.api.riotgames.com/lol/league/v4/entries/by-summoner/" + summonerId + "?api_key=" + RIOT_API_TOKEN;
                        HttpResponse<String> response = HttpService.GetResponse(uri);
                        JsonArray summonerArray = JsonParser.parseString(response.body()).getAsJsonArray();
                        Summoner newSummoner = new Summoner();
                        for (int j = 0; j < summonerArray.size(); j++) {
                            JsonObject summonerSoloRanked = summonerArray.get(j).getAsJsonObject();
                            String queueType = summonerSoloRanked.get("queueType").getAsString();
                            if ("RANKED_SOLO_5x5".equals(queueType)) {
                                newSummoner.setSummonerName(summonerSoloRanked.get("summonerName").getAsString());
                                newSummoner.setTier(summonerSoloRanked.get("tier").getAsString());
                                newSummoner.setRank(summonerSoloRanked.get("rank").getAsString());
                                newSummoner.setLeaguePoints(summonerSoloRanked.get("leaguePoints").getAsInt());
                                newSummoner.setPositionValue(calculatePositionValue(newSummoner));
                            }

                        }
                        if (newSummoner.getSummonerName() == null) {
                            String uriForName = "https://eun1.api.riotgames.com/lol/summoner/v4/summoners/" + summonerId + "?api_key=" + RIOT_API_TOKEN;
                            HttpResponse<String> responseForName = HttpService.GetResponse(uriForName);
                            JsonObject summonerObject = JsonParser.parseString(responseForName.body()).getAsJsonObject();
                            newSummoner.setSummonerName(summonerObject.get("name").getAsString());

                        }
                        summoners.add(newSummoner);
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private int calculatePositionValue(Summoner summoner) {
        int value = 0;
        switch (summoner.getTier()) {
            case "IRON":
                value += 10000;
                break;
            case "BRONZE":
                value += 20000;
                break;
            case "SILVER":
                value += 30000;
                break;
            case "GOLD":
                value += 40000;
                break;
            case "PLATINUM":
                value += 50000;
                break;
            case "DIAMOND":
                value += 60000;
                break;
            case "MASTER":
                value += 70000;
                break;
            case "GRANDMASTER":
                value += 80000;
                break;
            case "CHALLENGER":
                value += 90000;
                break;
            default:
                value += 0;
        }
        switch (summoner.getRank()) {
            case "I":
                value += 4000;
                break;
            case "II":
                value += 3000;
                break;
            case "III":
                value += 2000;
                break;
            case "IV":
                value += 1000;
                break;
            default:
                value += 0;
        }
        value += summoner.getLeaguePoints();
        return value;
    }
    public static Comparator<Summoner> SummonerComparator = (s1, s2) -> {

        int positionValue1 = s1.getPositionValue();
        int positionValue2 = s2.getPositionValue();

        return positionValue2-positionValue1;


    };


    public void sort() {
        Collections.sort(summoners, SummonerComparator);
    }
}


