import bot.BotCommandsHandler;
import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.TS3Config;
import com.github.theholywaffle.teamspeak3.TS3Query;
import config.AutoUpdateFeed;
import config.YamlHandler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class CatoBot {

    /**
     * main
     * YamlHandler reads the config.yaml
     * TS3Config gets the host from YamlHandler
     * TS3Query connects the bot to the ipAddress
     * TS3Api logins the bot with data from YamlHandler
     * Initialize AutoUpdate
     * Listen to Bot Commands -> Constructor takes TS3Api (for the commands) , TS3Query (to disconnect the bot on shutdown) and ScheduledExecutorService (to stop the Service on shutdown)
     * */

    public static void main(String[] args) {

        Logger logger = Logger.getLogger(CatoBot.class.getName());

        //Read Login Data from config.yaml
        YamlHandler yaml = new YamlHandler();
        yaml.readConfig();

        //Connect and Login Bot
        TS3Config config = new TS3Config();
        config.setHost(yaml.getIpAddress());
        config.setEnableCommunicationsLogging(true);

        TS3Query query = new TS3Query(config);
        query.connect();

        TS3Api api = query.getApi();
        api.login(yaml.getUsername(), yaml.getPassword());
        api.selectVirtualServerById(yaml.getVirtualServer());
        api.setNickname(yaml.getBotname());
        api.sendChannelMessage("!cato is online.");
        logger.info("cato is now online on " + yaml.getIpAddress());

        //Auto update
        AutoUpdateFeed autoUpdateFeed = new AutoUpdateFeed(api);
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        Runnable task = () -> {
            logger.info("Starting auto update of feeds.");
            autoUpdateFeed.readNews();
            logger.info("Finished auto update of feeds.");
        };
        executor.scheduleAtFixedRate(task, 1, yaml.getAutoUpdate(), TimeUnit.MINUTES);

        //Listen to Bot commands
        BotCommandsHandler botCommandsHandler = new BotCommandsHandler(api, query, executor);
        botCommandsHandler.commands();
    }
}