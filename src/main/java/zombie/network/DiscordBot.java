package zombie.network;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.util.logging.ExceptionLogger;
import zombie.debug.DebugLog;
import zombie.debug.DebugType;

public class DiscordBot {
    private DiscordApi api;
    private Channel current;
    private final String name;
    private final DiscordSender sender;

    public DiscordBot(String name, DiscordSender sender) {
        this.name = name;
        this.sender = sender;
        this.current = null;
    }

    public void connect(boolean enabled, String token, String channelName, String channelID) {
        if (token == null || token.isEmpty()) {
            DebugLog.log(DebugType.Network, "DISCORD: token not configured");
            enabled = false;
        }

        if (!enabled) {
            DebugLog.log(DebugType.Network, "*** DISCORD DISABLED ****");
            this.current = null;
        } else {
            DebugLog.log(DebugType.Network, "*** DISCORD ENABLED ****");

            new DiscordApiBuilder()
                    .setToken(token)
                    .addIntents(Intent.MESSAGE_CONTENT)
                    .addMessageCreateListener(new Listener())
                    .login()
                    .thenAccept(discordApi -> {
                        this.api = discordApi;

                        DebugLog.log(DebugType.Network, "*** DISCORD API CONNECTED ****");

                        discordApi.updateUsername(this.name);
                        setChannel(channelName, channelID);

                        if (this.current != null) {
                            DebugLog.log(DebugType.Network, "*** DISCORD INITIALIZATION SUCCEEDED ****");
                        } else {
                            DebugLog.log(DebugType.Network, "*** DISCORD INITIALIZATION FAILED ****");
                        }
                    })
                    .exceptionally(ExceptionLogger.get());
        }
    }

    private void setChannel(String channelName, String channelID) {
        if (channelID != null && !channelID.isEmpty()) {
            this.setChannelByID(channelID);
        } else if (channelName != null && !channelName.isEmpty()) {
            this.setChannelByName(channelName);
        } else {
            this.api.getChannels().stream()
                    .takeWhile(channel -> channel.asTextableRegularServerChannel().isPresent())
                    .findFirst()
                    .flatMap(Channel::asRegularServerChannel)
                    .ifPresent(regularServerChannel -> {
                        this.current = regularServerChannel;
                        DebugLog.log(DebugType.Network, "Discord enabled on channel: " + regularServerChannel.getName());
                    });
        }
    }

    public void sendMessage(String user, String message) {
        if (this.current != null) {
            this.current.asTextableRegularServerChannel().ifPresent(textableRegularServerChannel -> {
                if (textableRegularServerChannel.canWrite(this.api.getYourself())) {
                    textableRegularServerChannel.sendMessage(user + ": " + message).exceptionally(ExceptionLogger.get());
                    DebugLog.log(DebugType.Network, "DISCORD: User '" + user + "' send message: '" + message + "'");
                }
            });
        }
    }

    private void setChannelByName(String channelName) {
        this.current = null;

        List<Channel> channels = this.api.getChannelsByName(channelName).stream()
                .takeWhile(channel -> channel.asTextableRegularServerChannel().isPresent())
                .toList();

        if (channels.size() > 1) {
            DebugLog.log(DebugType.Network, "Discord server has few channels with name '" + channelName + "'. Please, use channel ID instead");
            this.current = null;
        } else {
            this.current = channels.get(0);
        }

        if (this.current == null) {
            DebugLog.log(DebugType.Network, "DISCORD: channel \"" + channelName + "\" is not found. Try to use channel ID instead");
        } else {
            DebugLog.log(DebugType.Network, "Discord enabled on channel: " + channelName);
        }
    }

    private void setChannelByID(String channelID) {
        this.current = null;

        this.api.getChannelById(channelID).flatMap(Channel::asTextableRegularServerChannel).ifPresent(textableRegularServerChannel -> {
            DebugLog.log(DebugType.Network, "Discord enabled on channel with ID: " + channelID);
            this.current = textableRegularServerChannel;
        });

        if (this.current == null) {
            DebugLog.log(DebugType.Network, "DISCORD: channel with ID \"" + channelID + "\" not found");
        }
    }

    class Listener implements MessageCreateListener {
        Listener() {
        }

        @Override
        public void onMessageCreate(MessageCreateEvent messageCreateEvent) {
            if (!messageCreateEvent.getMessageAuthor().isYourself() && messageCreateEvent.isServerMessage()) {
                messageCreateEvent.getServerTextChannel().ifPresent(serverTextChannel -> {
                    if (DiscordBot.this.current.getId() == serverTextChannel.getId()) {
                        DebugLog.log(DebugType.Network, "DISCORD: get message on current channel");
                        DebugType var10000 = DebugType.Network;
                        String var10001 = messageCreateEvent.getMessageContent();
                        DebugLog.log(var10000, "DISCORD: send message = \"" + var10001 + "\" for " + messageCreateEvent.getMessageAuthor().getName() + ")");
                        String var3 = this.replaceChannelIDByItsName(DiscordBot.this.api, messageCreateEvent);
                        var3 = this.removeSmilesAndImages(var3);
                        if (!var3.isEmpty() && !var3.matches("^\\s$")) {
                            DiscordBot.this.sender.sendMessageFromDiscord(messageCreateEvent.getMessageAuthor().getName(), var3);
                        }
                    }
                });
            }
        }

        private String replaceChannelIDByItsName(DiscordApi var1, MessageCreateEvent var2) {
            String var3 = var2.getMessageContent();
            Pattern var4 = Pattern.compile("<#(\\d+)>");
            Matcher var5 = var4.matcher(var2.getMessageContent());
            if (var5.find()) {
                for(int var6 = 1; var6 <= var5.groupCount(); ++var6) {
                    Optional<Channel> channel = var1.getChannelById(var5.group(var6));
                    if (channel.isPresent()) {
                        Optional<ServerTextChannel> serverTextChannel = channel.get().asServerTextChannel();
                        if (serverTextChannel.isPresent()) {
                            var3 = var3.replaceAll("<#" + var5.group(var6) + ">", "#" + serverTextChannel.get().getName());
                        }
                    }
                }
            }
            return var3;
        }

        private String removeSmilesAndImages(String var1) {
            StringBuilder var2 = new StringBuilder();
            char[] var3 = var1.toCharArray();
            for (char var6 : var3) {
                if (!Character.isLowSurrogate(var6) && !Character.isHighSurrogate(var6)) {
                    var2.append(var6);
                }
            }
            return var2.toString();
        }
    }
}
