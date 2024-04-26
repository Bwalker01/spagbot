package com.byteryse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import com.byteryse.Database.DatabaseController;

import kotlin.Pair;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Mentions;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.SelectTarget;

public class SlashCommands extends ListenerAdapter {
    private DatabaseController dbCon;

    public SlashCommands(DatabaseController dbCon) {
        this.dbCon = dbCon;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String command = event.getName();

        switch (command) {
            case "ping":
                event.reply("PONG").queue();
                break;
            case "echo":
                echo(event);
                break;
            case "game":
                game(event);
                break;
            case "newgame":
                newGame(event);
                break;
            case "addroles":
                addRoles(event);
                break;
            case "removeroles":
                removeRoles(event);
                break;
            default:
                event.reply("Something went wrong.");
                break;
        }
    }

    private void addRoles(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();
        ArrayList<SelectOption> options = new ArrayList<>();
        ArrayList<ArrayList<String>> results = dbCon
                .executeSQL("SELECT game_name, role_id FROM game_roles WHERE guild_id = ?", event.getGuild().getId());
        List<String> roles = event.getMember().getRoles().stream().map((role) -> role.getId()).toList();
        for (ArrayList<String> result : results) {
            if (!roles.contains(result.get(1))) {
                options.add(SelectOption.of(result.get(0), result.get(1)));
            }
        }
        if (options.isEmpty()) {
            event.getHook().sendMessage("You already have all of the roles!").setEphemeral(true).queue();
            return;
        }
        event.getHook().sendMessage("Select Game Roles:").setEphemeral(true)
                .addActionRow(
                        StringSelectMenu.create("add-games").addOptions(options).setMinValues(1).setMaxValues(25)
                                .build())
                .queue();
    }

    private void removeRoles(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();
        ArrayList<SelectOption> options = new ArrayList<>();
        ArrayList<ArrayList<String>> results = dbCon
                .executeSQL("SELECT game_name, role_id FROM game_roles WHERE guild_id = ?", event.getGuild().getId());
        List<String> roles = event.getMember().getRoles().stream().map((role) -> role.getId()).toList();
        for (ArrayList<String> result : results) {
            if (roles.contains(result.get(1))) {
                options.add(SelectOption.of(result.get(0), result.get(1)));
            }
        }
        if (options.isEmpty()) {
            event.getHook().sendMessage("You don't have any roles!").setEphemeral(true).queue();
            return;
        }
        event.getHook().sendMessage("Select Game Roles:").setEphemeral(true)
                .addActionRow(
                        StringSelectMenu.create("remove-games").addOptions(options).setMinValues(1).setMaxValues(25)
                                .build())
                .queue();

    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        if (event.getComponentId().equals("add-games")) {
            UserSnowflake snowflakeUser = User.fromId(event.getUser().getId());
            String addedRoles = "";
            for (SelectOption option : event.getSelectedOptions()) {
                event.getGuild().addRoleToMember(snowflakeUser, event.getGuild().getRoleById(option.getValue()))
                        .queue();
                addedRoles += "`" + option.getLabel() + "` ";
            }
            event.reply("Added you to roles: " + addedRoles).setEphemeral(true).queue();
        } else if (event.getComponentId().equals("remove-games")) {
            UserSnowflake snowflakeUser = User.fromId(event.getUser().getId());
            String removedRoles = "";
            for (SelectOption option : event.getSelectedOptions()) {
                event.getGuild().removeRoleFromMember(snowflakeUser, event.getGuild().getRoleById(option.getValue()))
                        .queue();
                removedRoles += "`" + option.getLabel() + "` ";
            }
            event.reply("Removed you from roles: " + removedRoles).setEphemeral(true).queue();
        }
    }

    private void echo(SlashCommandInteractionEvent event) {
        for (int i = 0; i < event.getOption("times").getAsInt(); i++) {
            event.reply(String.format("%s\n", event.getOption("message").getAsString())).queue();
        }
    }

    // private void gameRole(SlashCommandInteractionEvent event) {
    // }

    private void newGame(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();
        String gameName = event.getOption("game").getAsString();
        String guildId = event.getGuild().getId();
        String roleName;
        if (event.getOption("role") == null) {
            roleName = gameName;
        } else {
            roleName = event.getOption("role").getAsString();
        }
        String roleId = event.getGuild().createRole()
                .setName(roleName)
                .setPermissions(0L)
                .complete().getId();
        System.out.println("Created Role: " + roleName);
        dbCon.executeSQL("INSERT INTO game_roles VALUES (?,?,?,?)", roleId, roleName, guildId, gameName);
        System.out.println("Entered Role into Database.");
        event.getHook().sendMessage(String.format("New role created for %s.", gameName)).setEphemeral(true).queue();
    }

    private void game(SlashCommandInteractionEvent event) {
        switch (event.getOption("option").getAsString()) {
            case "random":
                event.reply("Who do you want to find a random game with?")
                        .addActionRow(
                                EntitySelectMenu.create("random-game", SelectTarget.USER)
                                        .setMaxValues(25)
                                        .setMinValues(0)
                                        .build())
                        .setEphemeral(true).queue();
                break;
            case "common":
                event.reply("Who do you want to find all common games with?")
                        .addActionRow(
                                EntitySelectMenu.create("common-games", SelectTarget.USER)
                                        .setMaxValues(25)
                                        .setMinValues(0)
                                        .build())
                        .setEphemeral(true).queue();
                break;
            default:
                event.reply("That's not a valid option.").queue();
        }
    }

    @Override
    public void onEntitySelectInteraction(EntitySelectInteractionEvent event) {
        ArrayList<ArrayList<String>> rawRoles = dbCon
                .executeSQL("SELECT role_id, game_name FROM game_roles WHERE guild_id = ?", event.getGuild().getId());
        List<String> gameRoles = rawRoles.stream().map((result) -> result.get(0)).toList();
        HashMap<String, String> gameNames = new HashMap<>();
        for (ArrayList<String> game : rawRoles) {
            gameNames.put(game.get(0), game.get(1));
        }
        List<String> userGames = event.getMember().getRoles().stream()
                .map((result) -> result.getId())
                .filter(gameRoles::contains).toList();
        ArrayList<List<String>> othersGames = new ArrayList<>();
        for (Member member : event.getMentions().getMembers()) {
            othersGames.add(member.getRoles().stream().map((result) -> result.getId()).toList());
        }
        for (List<String> role : othersGames) {
            userGames = userGames.stream().filter(role::contains).toList();
        }

        if (userGames.isEmpty()) {
            String reply = event.getMember().getAsMention();
            for (Member member : event.getMentions().getMembers()) {
                reply += " " + member.getAsMention();
            }
            event.reply(reply + " don't share any games!").queue();
        } else if (event.getComponentId().equals("common-games")) {
            String reply = event.getMember().getAsMention();
            for (Member member : event.getMentions().getMembers()) {
                reply += " " + member.getAsMention();
            }
            reply += " common games are: \n"
                    + userGames.stream().map((game) -> gameNames.get(game)).collect(Collectors.joining("\n"));
            event.reply(reply)
                    .queue();
        } else if (event.getComponentId().equals("random-game")) {
            String reply = event.getMember().getAsMention();
            for (Member member : event.getMentions().getMembers()) {
                reply += " " + member.getAsMention();
            }
            reply += " could play: " + gameNames.get(userGames.get(new Random().nextInt(userGames.size())));
            event.reply(reply + "!").queue();
        }
    }
}
