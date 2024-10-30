package config;

import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


public class BackupHandler {

    private final YamlHandler yamlHandler;

    public BackupHandler(YamlHandler yaml) {
        this.yamlHandler = yaml;
    }

    Logger logger = Logger.getLogger(BackupHandler.class.getName());

    File backupFolder = new File("news/backup/");
    File[] getBackupFiles = backupFolder.listFiles();

    /**
    * makeBackup copies the content of news.yaml into a new created backup file in /news/backup
    * */

    public String makeBackup (String time) {
        try {
            Path copyFrom = Paths.get("news/news.yaml");
            Path copyTo = Paths.get("news/backup/news-" + time + ".yaml");

            Files.copy(copyFrom, copyTo);
            //new FileWriter("news/news.yaml", false).close();

        } catch (IOException e) {
            return "Error backing up news.yaml in Class BackupHandler!";
        }

        return "Successfully backed up news.yaml as news-" + time + ".yaml";
    }

    /**
    * listFiles lists all available files in the backup folder
    * returns a String for the sendMessage of TeamspeakAPI
    * */

    public String listFiles() {
        String message = "";

        if (getBackupFiles == null || getBackupFiles.length == 0) {
            return "No backup files found!";
        }

        for (File file : getBackupFiles) {
            message = message + file.getName() + "\n";
        }

        return message;
    }

    /**
    * getBackupFile loads a named backup file and returns its content as a map.
    */

    public Map<Integer, String> getBackupFile (String filename) {
        File file = new File("news/backup/" + filename);
        try {
            Yaml yaml = new Yaml();
            InputStream fis = new FileInputStream(file);

            return yaml.load(fis);
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE,"Error loading backup file " + e.getMessage());
        }
        return null;
    }

    /**
    * loadBackup loads a given backup file into the news.yaml, so the Backup will be used.
    * Returns a successful String, so the bot can send a message in the chat.
    */

    public String loadBackup (String filename) {

        for (Map.Entry<Integer, String> entry : getBackupFile(filename).entrySet()) {
            yamlHandler.addNews(entry.getKey(), entry.getValue());
        }
        yamlHandler.writeNews();

        return "Successfully loaded " + filename;
    }

    /**
    * printBackup returns a String that contains the content of a given filename
    */

    public String printBackup (String filename) {
        String content = "\n";

        for (Map.Entry<Integer, String> entry : getBackupFile(filename).entrySet()) {
            content = content + entry.getKey() + ": " + entry.getValue() + "\n";
        }

        return content;
    }

}
