/**
 *   HeavySpleef - Advanced spleef plugin for bukkit
 *   
 *   Copyright (C) 2013 matzefratze123
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package de.matzefratze123.heavyspleef.command;


import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.command.handler.HSCommand;
import de.matzefratze123.heavyspleef.command.handler.Help;
import de.matzefratze123.heavyspleef.command.handler.UserType;
import de.matzefratze123.heavyspleef.command.handler.UserType.Type;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.flag.BooleanFlag;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.flag.FlagType;
import de.matzefratze123.heavyspleef.util.Permissions;
import de.matzefratze123.heavyspleef.util.Util;

@UserType(Type.ADMIN)
public class CommandFlag extends HSCommand {

	private final String[] flagNames;
	
	public CommandFlag() {
		List<Flag<?>> flags = FlagType.getFlagList();
		flagNames = new String[flags.size()];
		
		for (int i = 0; i < flags.size(); i++) {
			Flag<?> flag = flags.get(i);
			
			flagNames[i] = flag.getName();
		}
		
		setMinArgs(2);
		setOnlyIngame(true);
		setPermission(Permissions.SET_FLAG);
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player)sender;
		
		if (!GameManager.hasGame(args[0])) {
			sender.sendMessage(_("arenaDoesntExists"));
			return;
		}
		
		Game game = GameManager.getGame(args[0]);
		
		boolean found = false;
		Flag<?> flag = null;
		
		flags: for (Flag<?> f : FlagType.getFlagList()) {
			if (f.getName().equalsIgnoreCase(args[1])) {
				found = true;
				flag = f;
				break;
			} else {
				//Check the aliases
				String[] aliases = f.getAliases();
				if (aliases == null) //Aliases null, continue
					continue;
				
				for (String alias : aliases) {
					if (alias.equalsIgnoreCase(args[1])) {
						found = true;
						flag = f;
						break flags;
					}
				}
			}
		}
		
		if (!found || flag == null) {
			player.sendMessage(_("invalidFlag"));
			player.sendMessage(__(ChatColor.RED + "Available flags: " + Util.toFriendlyString(flagNames, ", ")));
			return;
		}
		
		
		//Check required flags
		for (Flag<?> required : flag.getRequiredFlags()) {
			if (!game.hasFlag(required) || (required instanceof BooleanFlag && !(Boolean)game.getFlag(required))) {
				List<String> flagNames = new ArrayList<String>();
				for (Flag<?> f : flag.getRequiredFlags()) {
					flagNames.add(f.getName());
				}
				
				player.sendMessage(_("requiringFlags", Util.toFriendlyString(flagNames, ", ")));
				return;
			}
		}
		
		//Check conflicting flags
		for (Flag<?> conflicting : flag.getConflictingFlags()) {
			if (game.hasFlag(conflicting)  || (conflicting instanceof BooleanFlag && (Boolean)game.getFlag(conflicting))) {
				List<String> flagNames = new ArrayList<String>();
				for (Flag<?> f : flag.getConflictingFlags()) {
					flagNames.add(f.getName());
				}
				
				player.sendMessage(_("conflictingFlags", Util.toFriendlyString(flagNames, ", ")));
				return;
			}
		}
		
		StringBuilder buildArgs = new StringBuilder();
		for (int i = 2; i < args.length; i++)
			buildArgs.append(args[i]).append(" ");
		
		if (args.length > 1) {
			if (args.length > 2 && args[2].equalsIgnoreCase("clear")) {
				game.setFlag(flag, null);
				player.sendMessage(_("flagCleared", flag.getName()));
				return;
			}
			
			try {
				Object previousValue = game.getFlag(flag);
				
				Object value = flag.parse(player, buildArgs.toString(), previousValue);
				
				if (value == null) {
					player.sendMessage(_("invalidFlagFormat"));
					player.sendMessage(flag.getHelp());
					return;
				}
				
				setFlag(game, flag, value);
				player.sendMessage(_("flagSet", flag.getName()));
			} catch (Exception e) {
				player.sendMessage(_("invalidFlagFormat"));
				player.sendMessage(flag.getHelp());
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public <V> void setFlag(Game game, Flag<V> flag, Object value) {
		game.setFlag(flag, (V)value);
	}

	@Override
	public Help getHelp(Help help) {
		help.setUsage("/spleef flag <name> <flag> [state]\n" + ChatColor.RED + "Available flags: " + Util.toFriendlyString(flagNames, ", "));
		help.addHelp("Sets a flag for this game");
		
		return help;
	} 
	
}
