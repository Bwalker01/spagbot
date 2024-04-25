package com.byteryse;

import com.byteryse.Database.DatabaseController;

import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class EventListeners extends ListenerAdapter {
    private DatabaseController dbCon;

    public EventListeners(DatabaseController dbCon) {
        this.dbCon = dbCon;
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        String name = event.getGuild().getName();
        String id = event.getGuild().getId();
        Boolean notInGuild = this.dbCon
                .executeSQL("SELECT 1 FROM guilds WHERE guild_id = ?", id).isEmpty();
        if (notInGuild) {
            this.dbCon.executeSQL("INSERT INTO guilds VALUES (?,?)", name, id);
            System.out.println("Successfully added new guild.");
        }

    }
}
