package bot;


import bot.database.SummonersDatabase;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class Main extends ListenerAdapter {
    public static JDA jda;
    public static String RIOT_API_TOKEN = "RiotAPI-token";
    public static Timestamp timestampOfSeason11 = Timestamp.
            valueOf(LocalDateTime.of(2021, 1, 8, 4, 0));
    static String token = "discord-token";

    public static void main(String[] args) throws LoginException {
        SummonersDatabase.createTable();
        jda = JDABuilder.createDefault(token).build();
        jda.addEventListener(new LolStreak());
        jda.addEventListener(new LolRanking());
        jda.addEventListener(new LolDuoWinrate());
    }


}
