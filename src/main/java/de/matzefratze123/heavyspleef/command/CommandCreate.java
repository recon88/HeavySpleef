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

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.command.handler.HSCommand;
import de.matzefratze123.heavyspleef.command.handler.Help;
import de.matzefratze123.heavyspleef.command.handler.UserType;
import de.matzefratze123.heavyspleef.command.handler.UserType.Type;
import de.matzefratze123.heavyspleef.core.GameCuboid;
import de.matzefratze123.heavyspleef.core.GameCylinder;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.hooks.HookManager;
import de.matzefratze123.heavyspleef.hooks.WorldEditHook;
import de.matzefratze123.heavyspleef.objects.RegionCuboid;
import de.matzefratze123.heavyspleef.objects.RegionCylinder;
import de.matzefratze123.heavyspleef.selection.Selection;
import de.matzefratze123.heavyspleef.util.Permissions;

@UserType(Type.ADMIN)
public class CommandCreate extends HSCommand {

	public CommandCreate() {
		setMinArgs(2);
		setOnlyIngame(true);
		setPermission(Permissions.CREATE_GAME);
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player)sender;
		if (GameManager.hasGame(args[0].toLowerCase())) {
			player.sendMessage(_("arenaAlreadyExists"));
			return;
		}
		
		if (args[1].equalsIgnoreCase("cylinder") || args[1].equalsIgnoreCase("cyl")) {
			//Create a new cylinder game
			if (args.length < 4) {
				player.sendMessage(getUsage());
				return;
			}
			
			try {
				int radius = Integer.parseInt(args[2]);
				int height = Integer.parseInt(args[3]);
				
				Location center = player.getLocation();
				
				int minY = center.getBlockY();
				int maxY = center.getBlockY() + height;
				
				RegionCylinder region = new RegionCylinder(-1, center, radius, minY, maxY);
				Game game = new GameCylinder(args[0], region);
				GameManager.addGame(game);
			} catch (NumberFormatException e) {
				player.sendMessage(_("notANumber", args[2]));
				return;
			}
			
			player.sendMessage(_("gameCreated"));
		} else if (args[1].equalsIgnoreCase("cuboid") || args[1].equalsIgnoreCase("cub")) {
			//Create a new cuboid game
			Selection s = HeavySpleef.getInstance().getSelectionManager().getSelection(player);
			if (!s.hasSelection()) {
				player.sendMessage(_("needSelection"));
				return;
			}
			if (s.isTroughWorlds()) {
				player.sendMessage(_("selectionCantTroughWorlds"));
				return;
			}
			
			RegionCuboid region = new RegionCuboid(-1, s.getFirst(), s.getSecond());
			Game game = new GameCuboid(args[0], region);
			
			GameManager.addGame(game);
			player.sendMessage(_("gameCreated"));
		} else {
			player.sendMessage(_("unknownSpleefType"));
		}
	}
	
	@Override
	public Help getHelp(Help help) {
		help.setUsage("/spleef create <name> cuboid\n" +
				 "/spleef create <name> cylinder <radius> <height>\n");
		help.addHelp("Creates a new spleef game");
		
		return help;
	}
	
	

}
