package com.denisindenbom.discordauth.discord;

//import com.denisindenbom.discordauth.utils;
import com.denisindenbom.discordauth.units.Account;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import com.denisindenbom.discordauth.DiscordAuth;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

public class VerificationHandler extends ListenerAdapter
{
    private final DiscordAuth plugin;

    private final String channelId;
    private final int maxNumOfAccounts;
    private final FileConfiguration messagesConfig;

    public VerificationHandler(DiscordAuth plugin)
    {
        this.plugin = plugin;

        this.channelId = this.plugin.getConfig().getString("channel-id");
        this.maxNumOfAccounts = this.plugin.getConfig().getInt("max-num-of-accounts");

        this.messagesConfig = this.plugin.getMessagesConfig();
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event)
    {
        String message = event.getMessage().getContentDisplay();
        String authorId = event.getAuthor().getId();
        String authorRoles = event.getAuthor().get
        String channelId = event.getChannel().getId();

        // ignoring unnecessary messages
        if (!(channelId.equals(this.channelId) && (message.startsWith("!")))) return;
        
        if ( message.startsWith("!info")){
            this.plugin.getBot().sendSuccessful(this.plugin.getAuthDB().getAccountsByDiscordId(authorId), event.getChannel());
            return;
        }
        if ( message.startsWith("!help")){
            this.plugin.getBot().sendSuccessful("you can use:\n!add\n\tto add new user.\n\n!delete\n\tto delete user, you can only delete your users\n\n!info\n\tto get user assigned to you,\n\n!help\n\tto display this\n\n\nwith <3 by dnetto", event.getChannel());
            return;
        }

        // split message
        String[] splitMessage = message.split(" ");

        if ( message.startsWith("!delete") ){
            if (splitMessage.length < 2)
                this.plugin.getBot().sendError(this.messagesConfig.getString("bot_error.name_no_set").replace("!verify", "!delete"), event.getChannel());
            else
               if (this.plugin.getAuthDB().removeAccountFromDiscord(splitMessage[1], authorId)) 
                    this.plugin.getBot().sendSuccessful(this.messagesConfig.getString("bot.verification_successful"), event.getChannel());
               else
                    this.plugin.getBot().sendSuccessful(this.messagesConfig.getString("remove_user.user_removed").replace("{%username%}", splitMessage[1]), event.getChannel());
            return;
        }
        
        if ( message.startsWith("!add") ){
            // check that the user does not exceed the number of maximum accounts
            if (this.plugin.getAuthDB().countAccountsByDiscordId(authorId) >= this.maxNumOfAccounts)
            {
                this.plugin.getBot().sendError(this.messagesConfig.getString("bot_error.enough_accounts"), event.getChannel());
                return;
            }

            // checking for the presence of an argument
            if (splitMessage.length < 2)
            {
                this.plugin.getBot().sendError(this.messagesConfig.getString("bot_error.name_no_set").replace("!verify", "!add"), event.getChannel());
                return;
            }

            // add user to database
            boolean result = this.plugin.getAuthDB().addAccount(new Account(splitMessage[1], authorId));

            if (!result)
            {
                this.plugin.getBot().sendError(this.messagesConfig.getString("bot_error.user_exists"), event.getChannel());
                return;
            }
            this.plugin.getBot().sendSuccessful(this.messagesConfig.getString("bot.verification_successful"), event.getChannel());
        }

    }
}
