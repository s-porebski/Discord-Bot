package bot;

import bot.database.SummonersDatabase;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.List;

public class LolRanking extends ListenerAdapter {

    private static String RIOT_API_TOKEN = Main.RIOT_API_TOKEN;

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        String commandLine = e.getMessage().getContentRaw();
        String[] commandLineArray = commandLine.split(" ");
        SummonersDatabase summonersDatabase = new SummonersDatabase();



        if ("!add".equals(commandLineArray[0])) {
            if (checkAdmin(e.getMember())) {
                String name = commandLine.substring(5);
                try {
                    summonersDatabase.insertSummonerIdToDatabase(name);
                    e.getChannel().sendMessage("Dodano gracza " + name + " do rankingu").queue();
                } catch (NotFoundException notFoundException) {
                    e.getChannel().sendMessage("Gracz o nazwie " + name + " nie istnieje").queue();
                }

            } else {
                e.getChannel().sendMessage("Nie masz odpowiednich uprawnień").queue();
            }

        } else if ("!remove".equals(commandLineArray[0]) && checkAdmin(e.getMember())) {
            if (checkAdmin(e.getMember())) {
                String name = commandLine.substring(8);
                try {
                    summonersDatabase.deleteSummonerIdFromDatabase(name);
                } catch (NotFoundException notFoundException) {
                    e.getChannel().sendMessage("Gracz o nazwie " + name + " nie istnieje").queue();
                }
            } else {
            e.getChannel().sendMessage("Nie masz odpowiednich uprawnień").queue();
        }

        } else if ("!ranking".equals(commandLine)) {

            List<Summoner> summoners = summonersDatabase.getSummoners();
            EmbedBuilder ranking = new EmbedBuilder();
            ranking.setTitle("Ranking League of Legends");
            String names = "";
            String places = "";
            String ranks = "";
            for (int i = 0; i < summoners.size(); i++) {
                places += String.valueOf(i + 1) + "\n\n";
                names += summoners.get(i).getSummonerName() + "\n\n";
                if (summoners.get(i).getTier() == null) {
                    ranks += "Unranked\n\n";
                } else {
                    ranks += summoners.get(i).getTier() + " " + summoners.get(i).getRank() + " "
                            + summoners.get(i).getLeaguePoints() + "LP" + "\n\n";
                }

            }
            ranking.addField("Place", places, true);
            ranking.addField("Name", names, true);
            ranking.addField("Rank", ranks, true);




            e.getChannel().sendMessage(ranking.build()).queue();
            ranking.clear();



        }
    }
    private boolean checkAdmin(Member member) {
        boolean hasRoleAdmin = false;
        List<Role> roles = member.getRoles();
        for (Role role:roles) {
            if (role.getName().equals("Admin")) {
                hasRoleAdmin = true;
        }
    }
        return hasRoleAdmin;
    }
}