/*
 * MIT License
 *
 * Copyright (c) 2018 Isaac Ellingson (Falkreon) and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.elytradev.asyouwish;

import java.util.Collection;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.server.permission.DefaultPermissionHandler;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.IPermissionHandler;
import net.minecraftforge.server.permission.PermissionAPI;

@Mod(modid=AsYouWish.MODID, version=AsYouWish.VERSION, name="As You Wish")
public class AsYouWish {
	public static final String MODID = "asyouwish";
	public static final String VERSION = "@VERSION@";
	
	public boolean installed = false;
	
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		IPermissionHandler oldHandler = PermissionAPI.getPermissionHandler();
		if (!(oldHandler instanceof DefaultPermissionHandler)) {
			PermissionHandler.LOG.error("AsYouWish cannot start. Another permission handler has been registered: "+oldHandler.getClass().getCanonicalName());
			return;
		}
		
		PermissionAPI.setPermissionHandler(new PermissionHandler());
		installed = true;
		PermissionHandler.LOG.info("Permissions installed. Have fun storming the castle!");
	}
	
	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		if (!installed) return;
		
		IPermissionHandler handler = PermissionAPI.getPermissionHandler();
		if (!(handler instanceof PermissionHandler)) {
			installed = false;
			PermissionHandler.LOG.error("AsYouWish tried to start, but was replaced by another permission handler: "+handler.getClass().getCanonicalName());
			return;
		}
		
		handler.registerNode("break", DefaultPermissionLevel.ALL, "Allows players to break blocks.");
		handler.registerNode("place", DefaultPermissionLevel.ALL, "Allows players to place blocks.");
		handler.registerNode("activate", DefaultPermissionLevel.ALL, "Allow players to activate (right-click) blocks");
		handler.registerNode("attack", DefaultPermissionLevel.ALL, "Allow players to attack or otherwise harm entities");
		handler.registerNode("pickup", DefaultPermissionLevel.ALL, "Allows players to pick up dropped item entities.");
		
		MinecraftForge.EVENT_BUS.register(PlayerPermissionEvents.class);
	}
	
	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		if (!installed) return;
		PermissionHandler.INSTANCE.freezePermissions();
		Collection<String> nodes = PermissionHandler.INSTANCE.getRegisteredNodes();
		PermissionHandler.LOG.info("Permission node registry frozen in post-init. There are a total of "+nodes.size()+" nodes:");
		for(String s : nodes) {
			PermissionHandler.LOG.info("    "+s);
		}
		PermissionHandler.LOG.info("MetaPermissions available:");
		PermissionHandler.LOG.info("    world.n.node - World-specific nodes override non-world-specific nodes.");
	}
	
	@Mod.EventHandler
	public void onServerStart(FMLServerStartingEvent event) {
		event.registerServerCommand(new WishCommand());
	}
}
