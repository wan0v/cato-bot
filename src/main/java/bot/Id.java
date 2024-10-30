package bot;

import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.api.wrapper.Channel;

import java.util.List;

public class Id {

    private final TS3Api api;

    public Id(TS3Api api) {
        this.api = api;
    }

    //looking for: channel_name=Welcome cid=1, ...
    public String tellChannelID () {
        //Save all channels in a List
        List<Channel> allChannels = api.getChannels();
        String message = "";

        //For each Channel api.getChannels
        for (Channel channelProperty : allChannels) {
            String completeInfo = channelProperty.toString();

            //Split data of every Channel and filter relevant Information
            for (String filteredInfo : completeInfo.split(",")) {
                String channelName = "";
                String channelId = "";

                if (filteredInfo.contains("channel_name")) {
                    String[] split = filteredInfo.split("=");
                    channelName = split[1];

                    message = message + channelName + ": ";
                }
                if (filteredInfo.contains("cid")) {
                    String[] split = filteredInfo.split("=");
                    channelId = split[1];

                    message = message + channelId +'\n';
                }
            }
        }
        return message;
    }
}
