package fr.bouboule.unitiumplugin2.commands.country.tabcompleter;

import fr.bouboule.unitiumplugin2.database.DatabaseManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CountryTabCompleter implements TabCompleter {

    DatabaseManager databaseManager;

    public CountryTabCompleter(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            // Liste des subcommands disponibles pour l'autocomplétion
            List<String> subCommands = Arrays.asList("create", "disband", "rename", "kick", "invite", "join", "leave", "createrank");

            // Filtrer les subcommands en fonction de l'entrée partielle de l'utilisateur
            return filterStartingWith(args[0], subCommands);
        } else if (args.length == 2 && args[0].equalsIgnoreCase("invite")) {
            // Compléter la deuxième partie de la commande "invite" avec les noms des joueurs en ligne
            List<String> onlinePlayers = new ArrayList<>();

            // Ajoutez ici la logique pour récupérer les noms des joueurs en ligne

            // Filtrer les noms des joueurs en fonction de l'entrée partielle de l'utilisateur
            return filterStartingWith(args[1], onlinePlayers);
        }else if (args.length == 2 && args[0].equalsIgnoreCase("kick")){
            if (!(sender instanceof Player)) {
                return Collections.emptyList(); // Si l'expéditeur n'est pas un joueur, retourner une liste vide
            }

            Player player = (Player) sender;

            // Vérifier si le joueur a un pays
            if (!databaseManager.playerHasCountry(player.getUniqueId())) {
                return Collections.emptyList(); // Si le joueur n'a pas de pays, retourner une liste vide
            }

            String countryName = databaseManager.getCountryName(player.getUniqueId());

            // Récupérer la liste des joueurs dans le pays du joueur expéditeur
            List<String> countryPlayers = databaseManager.getCountryPlayers(countryName);

            // Filtrer les noms des joueurs en fonction de l'entrée partielle de l'utilisateur
            return filterStartingWith(args[1], countryPlayers);
        }

        // Autres autocomplétions...

        return null;
    }

    private List<String> filterStartingWith(String input, List<String> options) {
        List<String> completions = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase().startsWith(input.toLowerCase())) {
                completions.add(option);
            }
        }
        return completions;
    }
}
