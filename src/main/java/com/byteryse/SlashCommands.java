package com.byteryse;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class SlashCommands extends ListenerAdapter {

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
            default:
                event.reply("Something went wrong.");
                break;
        }
    }

    private void echo(SlashCommandInteractionEvent event) {
        for (int i = 0; i < event.getOption("times").getAsInt(); i++) {
            event.reply(String.format("%s\n", event.getOption("message").getAsString())).queue();
        }
    }
}
