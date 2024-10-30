package bot;

import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.TS3Query;
import com.github.theholywaffle.teamspeak3.api.ChannelProperty;
import com.github.theholywaffle.teamspeak3.api.TextMessageTargetMode;
import com.github.theholywaffle.teamspeak3.api.event.TS3EventAdapter;
import com.github.theholywaffle.teamspeak3.api.event.TextMessageEvent;
import com.github.theholywaffle.teamspeak3.api.exception.TS3CommandFailedException;
import config.AutoUpdateFeed;
import config.BackupHandler;
import config.YamlHandler;
import getNews.HtmlHandler;
import getNews.XmlHandler;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;
import java.util.logging.Level;

public class BotCommandsHandler {

    Logger logger = Logger.getLogger(BotCommandsHandler.class.getName());

    private final TS3Api api;
    private final TS3Query query;
    private final ScheduledExecutorService executor;

    YamlHandler yaml = new YamlHandler();
    BackupHandler backup = new BackupHandler(yaml);

    DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm");

    public BotCommandsHandler(TS3Api api, TS3Query query, ScheduledExecutorService executor) {
        this.api = api;
        this.query = query;
        this.executor = executor;
    }

    /**
     * commands contains all the reactions to all the commands
     * I added an event listener for channel messages and one for private messages
     * I designed the bot to listen to !cato in the Standard Channel and then everything else will be handled via a private chat
     * I tried to only split the commands here and handle all the logic in the relevant classes
     * */

    public void commands () {
        //Own Client ID (Bot ID), to prevent listening to own messages
        final int clientId = api.whoAmI().getId();

        //Type of event to listen to
        api.registerAllEvents();

        //Listen to Channel Messages
        api.addTS3Listeners(new TS3EventAdapter() {
            @Override
            public void onTextMessage(TextMessageEvent e) {
                if (e.getTargetMode() == TextMessageTargetMode.CHANNEL && e.getInvokerId() != clientId) {
                    String inputMessage = e.getMessage().toLowerCase();

                    switch (inputMessage) {
                        case "!cato":
                            api.sendPrivateMessage(e.getInvokerId(), "Hello there! Type !h for an overview of available commands.");
                            break;
                    }
                }
            }
        });

        //Listen to Private Messages
        api.addTS3Listeners(new TS3EventAdapter() {
            @Override
            public void onTextMessage(TextMessageEvent e) {

                if (e.getTargetMode() == TextMessageTargetMode.CLIENT) {

                    String inputMessage = e.getMessage().toLowerCase();

                    switch (inputMessage) {
                        case "!help", "!h":
                            api.sendPrivateMessage(e.getInvokerId(), "[b]Available commands:[/b]" + '\n' +
                                    "[u]General[/u]" + '\n' +
                                    "!shutdown - Disconnects the Bot from the server, disables any channel updates and removes the feeds from all channel descriptions!" + '\n' +
                                    '\n' + "[u]Feed Configuration[/u]" + '\n' +
                                    "!cid - Shows all Channels and their IDs." + '\n' +
                                    "!add CID RSS-URL - Only Accepts xml. For other sources check !addHTML." + '\n' +
                                    "!addHTML CID URL CSS-PARENT CSS-TITLE CSS-LINK CSS-DESCRIPTION CSS-DATE - Fetches Websites HTML with the defined CSS-Selectors. [url=https://github.com/wan0v/cato-bot/tree/main#addhtml-boilerplate]Examples[/url]" + '\n' +
                                    "!rm CID - Removes the feed from the given channel-id" + '\n' +
                                    "!refresh - Manually refresh your feeds once." + '\n' +
                                    '\n' + "[u]Backups[/u]" + '\n' +
                                    "A backup of the feed configuration is made automatically on !shutdown." + '\n' +
                                    "!makebackup - Does a manual backup." + '\n' +
                                    "!listbackup - Lists all available backup files." + '\n' +
                                    "!loadbackup FILENAME - Loads the backup file, if available." + '\n' +
                                    "!printbackup FILENAME - Prints the content of the named backup file."
                            );
                            break;

                        case "!cid":
                            Id id = new Id(api);
                            api.sendPrivateMessage(e.getInvokerId(), id.tellChannelID());
                            break;

                        case "!shutdown":
                            LocalDateTime shutdownTime = LocalDateTime.now();
                            api.sendPrivateMessage(e.getInvokerId(), backup.makeBackup(shutdownTime.format(format)));

                            for (Map.Entry<Integer, Object> entry : yaml.getNews().entrySet()) {
                                api.editChannel(entry.getKey(), ChannelProperty.CHANNEL_DESCRIPTION, "");
                            }

                            api.sendPrivateMessage(e.getInvokerId(), "Successfully removed feeds from channel descriptions.");

                            try {
                                new FileWriter("news/news.yaml", false).close();
                            } catch (IOException shutdownE) {}

                            api.sendPrivateMessage(e.getInvokerId(),"Everything done - Goodbye!");

                            query.exit();
                            executor.shutdown();
                            break;

                        case "!listbackup", "!listbackups":
                            api.sendPrivateMessage(e.getInvokerId(), backup.listFiles());
                            break;

                        case "!makebackup":
                            LocalDateTime manualBackupTime = LocalDateTime.now();
                            api.sendPrivateMessage(e.getInvokerId(), backup.makeBackup(manualBackupTime.format(format)));
                            break;

                        case "!refresh":
                            AutoUpdateFeed autoUpdateFeed = new AutoUpdateFeed(api);
                            autoUpdateFeed.readNews();
                            api.sendPrivateMessage(e.getInvokerId(), "Manual update of feeds successful!");
                            break;
                    }

                    if (inputMessage.startsWith("!addhtml")) {
                        String url = null;
                        int channelNumber = 0;

                        try {
                            String[] splitCommand = inputMessage.split("\\s+");
                            channelNumber = Integer.parseInt(splitCommand[1]);
                            url = splitCommand[2].replaceAll("\\[.*?\\] ?", "");
                            String parent = splitCommand[3];
                            String title = splitCommand[4];
                            String link = splitCommand[5];
                            String description = splitCommand[6];
                            String date = splitCommand[7];

                            HtmlHandler htmlHandler = new HtmlHandler(url, parent, title, link, description, date);
                            api.editChannel(channelNumber, ChannelProperty.CHANNEL_DESCRIPTION, htmlHandler.handleHtml());
                            api.sendPrivateMessage(e.getInvokerId(),"Added custom html feed from  " + url + " to channel: " + channelNumber + " :)");

                            String store = url + " " + parent + " " + title + " " + link + " " + description + " " + date;
                            yaml.addNews(channelNumber, store);
                            //System.out.println(yaml.getNews());
                            yaml.writeNews();

                        } catch (TS3CommandFailedException ts) {
                            System.err.println("Error loading website: " + url + " too many characters " + ts.getMessage());
                            api.sendPrivateMessage(e.getInvokerId(),"Error parsing " + url + " to channel: " + channelNumber + ". Invalid parameter size.");

                        } catch (ArrayIndexOutOfBoundsException aiobe) {
                            String errorMessage = "Command seems to be incomplete. Did you use: !addHTML CID URL CSS-PARENT CSS-TITLE CSS-LINK CSS-DESCRIPTION CSS-DATE ?";
                            logger.log(Level.SEVERE, errorMessage + " Error: " + aiobe.getMessage());
                            api.sendPrivateMessage(e.getInvokerId(),errorMessage);
                        }
                    }
                    else if (inputMessage.startsWith("!add")) {
                        String url = null;
                        int channelNumber = 0;

                        try {
                            String[] splitCommand = inputMessage.split("\\s+");
                            channelNumber = Integer.parseInt(splitCommand[1]);
                            url = splitCommand[2].replaceAll("\\[.*?\\] ?", "");

                            XmlHandler xmlHandler = new XmlHandler(url);
                            api.editChannel(channelNumber, ChannelProperty.CHANNEL_DESCRIPTION, xmlHandler.handleXml());
                            api.sendPrivateMessage(e.getInvokerId(),"Added rss-feed from " + url + " to channel: " + channelNumber + " :)");

                            yaml.addNews(channelNumber, url);
                            //System.out.println(yaml.getNews());
                            yaml.writeNews();

                        } catch (TS3CommandFailedException ts) {
                            System.err.println("Error loading website: " + url + " too many characters " + ts.getMessage());
                            api.sendPrivateMessage(e.getInvokerId(),"Error parsing " + url + " to channel: " + channelNumber + ". Invalid parameter size.");

                        } catch (ArrayIndexOutOfBoundsException aiobe) {
                            String errorMessage = "Command seems to be incomplete. Did you use: !add CID SOURCE ?";
                            logger.log(Level.SEVERE, errorMessage + " Error: " + aiobe.getMessage());
                            api.sendPrivateMessage(e.getInvokerId(),errorMessage);
                        }
                    }
                    else if (inputMessage.startsWith("!rm")) {
                        String[] splitCommand = inputMessage.split("\\s+");
                        int channelNumber = Integer.parseInt(splitCommand[1]);

                        api.editChannel(channelNumber, ChannelProperty.CHANNEL_DESCRIPTION, "");
                        yaml.removeNews(channelNumber);
                        yaml.writeNews();

                        api.sendPrivateMessage(e.getInvokerId(),"Removed feed from channel: " + channelNumber + " :)");
                    }
                    else if (inputMessage.startsWith("!loadbackup")) {
                        String[] splitCommand = inputMessage.split("\\s+");
                        String filename = splitCommand[1];

                        api.sendPrivateMessage(e.getInvokerId(), backup.loadBackup(filename));
                    }
                    else if (inputMessage.startsWith("!printbackup")) {
                        String[] splitCommand = inputMessage.split("\\s+");
                        String filename = splitCommand[1];

                        api.sendPrivateMessage(e.getInvokerId(), backup.printBackup(filename));
                    }
                }
            }
        });
    }

}