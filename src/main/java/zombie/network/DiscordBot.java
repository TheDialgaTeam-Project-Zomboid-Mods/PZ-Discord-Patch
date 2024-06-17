package zombie.network;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.util.logging.ExceptionLogger;
import zombie.debug.DebugLog;
import zombie.debug.DebugType;

public class DiscordBot {

    private final String name;
    private final DiscordSender sender;

    private DiscordApi discordApi;
    private Channel channel;

    public DiscordBot(String name, DiscordSender sender) {
        this.name = name;
        this.sender = sender;
    }

    public void connect(boolean enabled, String token, String channelName, String channelId) {
        if (token == null || token.isEmpty()) {
            DebugLog.log(DebugType.Network, "DISCORD: token not configured");
            enabled = false;
        }

        if (!enabled) {
            DebugLog.log(DebugType.Network, "*** DISCORD DISABLED ****");
            channel = null;
        } else {
            DebugLog.log(DebugType.Network, "*** DISCORD ENABLED ****");

            new DiscordApiBuilder()
                    .setToken(token)
                    .addIntents(Intent.MESSAGE_CONTENT)
                    .addMessageCreateListener(event -> {
                        if (!event.getMessageAuthor().isYourself() && event.isServerMessage()) {
                            if (event.getChannel().getId() == channel.getId()) {
                                DebugLog.log(DebugType.Network, "DISCORD: get message on current channel");

                                String message = event.getMessageContent();

                                DebugLog.log(DebugType.Network, "DISCORD: send message = \"" + message + "\" for " + event.getMessageAuthor().getName() + ")");

                                message = replaceChannelIdByItsName(message);
                                message = removeSmilesAndImages(message);

                                if (!message.isEmpty() && !message.matches("^\\s$")) {
                                    sender.sendMessageFromDiscord(event.getMessageAuthor().getName(), message);
                                }
                            }
                        }
                    })
                    .login()
                    .thenAccept(discordApi -> {
                        this.discordApi = discordApi;

                        DebugLog.log(DebugType.Network, "*** DISCORD API CONNECTED ****");

                        discordApi.updateUsername(name);
                        setChannel(channelName, channelId);

                        if (channel != null) {
                            DebugLog.log(DebugType.Network, "*** DISCORD INITIALIZATION SUCCEEDED ****");
                        } else {
                            DebugLog.log(DebugType.Network, "*** DISCORD INITIALIZATION FAILED ****");
                        }
                    })
                    .exceptionally(ExceptionLogger.get())
                    .join();
        }
    }

    private void setChannel(String channelName, String channelId) {
        if (channelId != null && !channelId.isEmpty()) {
            setChannelById(channelId);
        } else if (channelName != null && !channelName.isEmpty()) {
            setChannelByName(channelName);
        } else {
            discordApi.getChannels().stream()
                    .takeWhile(channel -> channel.asTextableRegularServerChannel().isPresent())
                    .findFirst()
                    .flatMap(Channel::asRegularServerChannel)
                    .ifPresent(regularServerChannel -> {
                        channel = regularServerChannel;
                        DebugLog.log(DebugType.Network, "Discord enabled on channel: " + regularServerChannel.getName());
                    });
        }
    }

    public void sendMessage(String user, String message) {
        if (channel != null) {
            channel.asTextableRegularServerChannel().ifPresent(textableRegularServerChannel -> {
                if (textableRegularServerChannel.canWrite(discordApi.getYourself())) {
                    textableRegularServerChannel.sendMessage(user + ": " + message).exceptionally(ExceptionLogger.get());
                    DebugLog.log(DebugType.Network, "DISCORD: User '" + user + "' send message: '" + message + "'");
                }
            });
        }
    }

    private void setChannelByName(String channelName) {
        channel = null;

        List<Channel> channels = discordApi.getChannelsByName(channelName).stream()
                .takeWhile(channel -> channel.asTextableRegularServerChannel().isPresent())
                .toList();

        if (channels.size() > 1) {
            DebugLog.log(DebugType.Network, "Discord server has few channels with name '" + channelName + "'. Please, use channel ID instead");
            channel = null;
        } else {
            channel = channels.get(0);
        }

        if (channel == null) {
            DebugLog.log(DebugType.Network, "DISCORD: channel \"" + channelName + "\" is not found. Try to use channel ID instead");
        } else {
            DebugLog.log(DebugType.Network, "Discord enabled on channel: " + channelName);
        }
    }

    private void setChannelById(String channelId) {
        channel = null;

        discordApi.getChannelById(channelId)
                .flatMap(Channel::asTextableRegularServerChannel)
                .ifPresent(textableRegularServerChannel -> {
            DebugLog.log(DebugType.Network, "Discord enabled on channel with ID: " + channelId);
            channel = textableRegularServerChannel;
        });

        if (channel == null) {
            DebugLog.log(DebugType.Network, "DISCORD: channel with ID \"" + channelId + "\" not found");
        }
    }

    private String replaceChannelIdByItsName(String message) {
        Pattern pattern = Pattern.compile("<#(\\d+)>");
        Matcher matcher = pattern.matcher(message);

        if (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); ++i) {
                Optional<Channel> channel = discordApi.getChannelById(matcher.group(i));

                if (channel.isPresent()) {
                    Optional<ServerChannel> serverChannel = channel.get().asServerChannel();

                    if (serverChannel.isPresent()) {
                        message = message.replaceAll("<#" + matcher.group(i) + ">", "#" + serverChannel.get().getName());
                    }
                }
            }
        }

        return message;
    }

    private String removeSmilesAndImages(String message) {
        StringBuilder stringBuilder = new StringBuilder();

        for (char c : message.toCharArray()) {
            if (!Character.isLowSurrogate(c) && !Character.isHighSurrogate(c)) {
                stringBuilder.append(c);
            }
        }

        return stringBuilder.toString();
    }
}
