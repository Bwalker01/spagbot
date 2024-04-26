package com.byteryse;

import java.util.List;

import com.byteryse.Database.DatabaseController;

import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
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

    @Override
    public void onRoleDelete(RoleDeleteEvent event) {
        List<String> roles = this.dbCon
                .executeSQL("SELECT role_id FROM game_roles WHERE guild_id = ?", event.getGuild().getId())
                .stream().map((result) -> result.get(0)).toList();
        if (roles.contains(event.getRole().getId())) {
            System.out.println("Removing Role for " + event.getRole().getName());
            this.dbCon.executeSQL("DELETE FROM game_roles WHERE role_id = ?", event.getRole().getId());
        } else {
            System.out.println("This role is not tied to me.");
        }
    }
}
