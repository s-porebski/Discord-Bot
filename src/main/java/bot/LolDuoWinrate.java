package bot;

import bot.service.HttpService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.net.http.HttpResponse;
import java.sql.Timestamp;
import java.util.ArrayList;

public class LolDuoWinrate extends ListenerAdapter {
    private static Timestamp timestampOfSeason11 = Main.timestampOfSeason11;

    private static String RIOT_API_TOKEN = Main.RIOT_API_TOKEN;

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        String commandLine = e.getMessage().getContentRaw();
        String[] commandLineArray = commandLine.split(" ");

        if (("!duo").equals(commandLineArray[0])){
            String[] names = commandLine.substring(5).split("/");
            Account account1 = null;
            try {
                account1 = getAccountId(names[0]);
            } catch (NotFoundException notFoundException) {
                e.getChannel().sendMessage("Gracz o nazwie " + names[0] + " nie istnieje").queue();
            }
            ArrayList<Match> matches1 = getMatchList(account1);
            Account account2 = null;
            try {
                account2 = getAccountId(names[1]);
            } catch (NotFoundException notFoundException) {
                e.getChannel().sendMessage("Gracz o nazwie " + names[1] + " nie istnieje").queue();            }
            ArrayList<Match> matches2 = getMatchList(account2);
            ArrayList<Match> duoMatches = getMatchList(matches1, matches2);
            if (duoMatches.size() == 0) {
                e.getChannel().sendMessage(account1.getName() + " i " + account2.getName() + " nie grali razem duo").queue();
            } else {
               String texToPrint =  checkDuoWinrate(account1, account2, duoMatches);
               e.getChannel().sendMessage(texToPrint).queue();


            }
        }
    }

    private JsonObject getMatchJson(Match match) {
        String uri = "https://eun1.api.riotgames.com/lol/match/v4/matches/" + match.getGameId() + "?api_key=" + RIOT_API_TOKEN;
        HttpResponse<String> response = HttpService.GetResponse(uri);

        JsonObject matchJson = JsonParser.parseString(response.body()).getAsJsonObject();
        return matchJson;
    }

    private String checkDuoWinrate(Account account1, Account account2, ArrayList<Match> duoMatches) {
        int wins = 0;
        int loses = 0;
        int games = 0;
        for (Match match:duoMatches) {
            JsonObject matchJson = getMatchJson(match);
            boolean remake = checkRemake(matchJson);
            boolean player1win = checkWin(account1, matchJson);
            boolean player2win = checkWin(account2, matchJson);
            if (player1win && player2win && !remake) {
                wins++;
                games++;
            } else if (!player1win && !player2win && !remake) {
                loses++;
                games++;
            }

        }
        Double winRatio = Double.valueOf(Double.valueOf(wins)/games*100);
        String winRatioFormatted = String.format("%.1f", winRatio);
        return account1.getName() + " i " + account2.getName() + " " + wins + "W-" + loses + "L Win rate " + winRatioFormatted + "%";

    }

    public boolean checkRemake(JsonObject matchJson) {
        return matchJson.get("gameDuration").getAsLong() < 300;

    }

    private boolean checkWin(Account account, JsonObject matchJson) {
        JsonArray participantIdentitiesJsonArray = matchJson.getAsJsonArray("participantIdentities");
        int participantId = 0;
        for (JsonElement element: participantIdentitiesJsonArray) {
            JsonObject player = element.getAsJsonObject().get("player").getAsJsonObject();
            String playerId = player.get("accountId").getAsString();
            if (account.getAccountId().equals(playerId)) {
                participantId = element.getAsJsonObject().get("participantId").getAsInt();
            }
        }
        int teamId = 0;
        JsonArray participantsArray = matchJson.getAsJsonArray("participants");
        for (JsonElement element: participantsArray) {
            int participant = element.getAsJsonObject().get("participantId").getAsInt();
            if (participant == participantId) {
                teamId = element.getAsJsonObject().get("teamId").getAsInt();
            }
        }

        if (teamId == 100) {
            String win = matchJson.getAsJsonArray("teams").get(0)
                    .getAsJsonObject().get("win").getAsString();
            if (win.equals("Win")) {
                return true;
            }
        } else if (teamId == 200) {
            String win = matchJson.getAsJsonArray("teams").get(1)
                    .getAsJsonObject().get("win").getAsString();
            if (win.equals("Win")) {
                return true;
            }
        }
        return false;
    }

    public Account getAccountId(String name) throws NotFoundException {
        String uri = "https://eun1.api.riotgames.com/lol/summoner/v4/summoners/by-name/"
                + name.replace(" ", "%20") + "?api_key=" + RIOT_API_TOKEN;
        HttpResponse<String> response = HttpService.GetResponse(uri);
        if (response.statusCode() == 404) {
            throw new NotFoundException();
        }

        JsonObject accountJson = JsonParser.parseString(response.body()).getAsJsonObject();
        String accountId = accountJson.get("accountId").getAsString();
        Account account = new Account(accountId, name);
        return account;
    }
    private ArrayList<Match> getMatchList(ArrayList<Match> matches1, ArrayList<Match> matches2) {
        ArrayList<Match> duoMatches = new ArrayList<>();
        for (int i = 0; i < matches1.size(); i++) {
            for (int j = 0; j < matches2.size(); j++) {
                if (matches1.get(i).getGameId().equals(matches2.get(j).getGameId())) {
                    duoMatches.add(new Match(null, matches1.get(i).getGameId()));
                    break;
                }

            }
        }
        return duoMatches;
    }
    public ArrayList<Match> getMatchList(Account account) {
        int beginIndex = 0;
        ArrayList<Match> arrayListMatches = new ArrayList<>();
        boolean afterTimestamp = true;
        while (afterTimestamp) {
            String uri = "https://eun1.api.riotgames.com/lol/match/v4/matchlists/by-account/"
                    + account.getAccountId() + "?queue=420&beginIndex=" + beginIndex + "&api_key=" + RIOT_API_TOKEN;
            HttpResponse<String> response = HttpService.GetResponse(uri);

            JsonObject matchesJson = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonArray matchesArray = matchesJson.getAsJsonArray("matches");

            for (JsonElement match : matchesArray) {
                Timestamp matchTimestamp = new Timestamp(match.getAsJsonObject().get("timestamp").getAsLong());
                if (matchTimestamp.after(timestampOfSeason11)) {
                    Long gameId = match.getAsJsonObject().get("gameId").getAsLong();
                    arrayListMatches.add(new Match(account, gameId));
                } else {
                    afterTimestamp = false;
                }
            }
            beginIndex += 100;
        }
        return arrayListMatches;

    }
}
