package bot;

import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.api.ChannelProperty;

public class ChannelEditor {
    private final TS3Api api;

    public ChannelEditor(TS3Api api) {
        this.api = api;
    }

    int chosenChannelID = 1;

    public void editChannel (String newsMessage) {
        api.editChannel(chosenChannelID, ChannelProperty.CHANNEL_DESCRIPTION, newsMessage);
    }
}