package bot;

import bot.service.HttpService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;


import java.net.http.HttpResponse;
import java.util.ArrayList;

public class LolStreak extends ListenerAdapter {
    private static String RIOT_API_TOKEN = Main.RIOT_API_TOKEN;
    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        String commandLine = e.getMessage().getContentRaw();
        String[] commandLineArray = commandLine.split(" ");

        if (("!streak").equals(commandLineArray[0])){
            String name = commandLine.substring(8);
            Account account = null;
            try {
                account = getAccountId(name);
                ArrayList<Match> matches = getMatchList(account);
                String textToPrint = checkStreak(account, matches);
                e.getChannel().sendMessage(textToPrint).queue();
            } catch (NotFoundException notFoundException) {
                e.getChannel().sendMessage("Gracz o nazwie " + name + " nie istnieje").queue();
            }
        }
    }

    private String checkStreak(Account account, ArrayList<Match> matches) {
        String typeOfStreak = "";
        int i = 0;
        int streakValue = 0;
        Boolean firstMatchWin = null;
        boolean isRemake = true;
        while (i < matches.size() && isRemake) {
            JsonObject match = getMatchJson(matches.get(i));
            isRemake = checkRemake(match);
            if (!isRemake) {
                firstMatchWin = checkWin(account, match);
            } else {
                i++;
            }
        }
        if (firstMatchWin) {
            typeOfStreak = "Win streak";
            Boolean streak = true;
            while (i < matches.size() && streak) {
                JsonObject match = getMatchJson(matches.get(i));
                if (checkWin(account, match) && !checkRemake(match)) {
                    streakValue++;
                } else {
                    streak = false;
                }
                i++;
            }
        } else if (!firstMatchWin){
            typeOfStreak = "Lose streak";
            Boolean streak = true;
            while (i < matches.size() && streak) {
                JsonObject match = getMatchJson(matches.get(i));
                if (!checkWin(account, match) && !checkRemake(match)) {
                    streakValue++;
                } else {
                    streak = false;
                }
                i++;
            }
        }
        return typeOfStreak + " gracza " + account.getName() + " wynosi " + streakValue;

    }

    private JsonObject getMatchJson(Match match) {
        String uri = "https://eun1.api.riotgames.com/lol/match/v4/matches/" + match.getGameId() + "?api_key=" + RIOT_API_TOKEN;
        HttpResponse<String> response = HttpService.GetResponse(uri);

        JsonObject matchJson = JsonParser.parseString(response.body()).getAsJsonObject();
        return matchJson;
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
    public ArrayList<Match> getMatchList(Account account) {
        String uri = "https://eun1.api.riotgames.com/lol/match/v4/matchlists/by-account/"
                + account.getAccountId() + "?queue=420&api_key=" + RIOT_API_TOKEN;
        HttpResponse<String> response = HttpService.GetResponse(uri);

        JsonObject matchesJson = JsonParser.parseString(response.body()).getAsJsonObject();
        JsonArray matchesArray = matchesJson.getAsJsonArray("matches");
        ArrayList<Match> arrayListMatches = new ArrayList<>();
        for (JsonElement match : matchesArray) {
            Long gameId = match.getAsJsonObject().get("gameId").getAsLong();
            arrayListMatches.add(new Match(account, gameId));
        }
        return arrayListMatches;

    }


}