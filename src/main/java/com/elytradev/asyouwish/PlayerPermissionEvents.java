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

import java.util.HashSet;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

public class PlayerPermissionEvents {
	
	@SubscribeEvent
	public static void foo(PlayerEvent.PlayerLoggedInEvent event) {
		System.out.println("Loading permissions fro player "+event.player.getName() + " ("+event.player.getPersistentID()+")");
		
		Actor actor = new Actor();
		actor.id = event.player.getPersistentID().toString();
		
		NBTTagCompound tag = event.player.getEntityData();
		if (tag!=null && tag.hasKey("AYW", NBTType.COMPOUND.id())) {
			NBTTagCompound root = tag.getCompoundTag("AYW");
			if (root.hasKey("Groups", NBTType.LIST.id())) {
				NBTTagList list = root.getTagList("Groups", NBTType.STRING.id());
				Set<String> groups = new HashSet<>(); //Stuff into a list first to dedupe, but also to avoid CME
				for(NBTBase nbt : list) {
					groups.add(((NBTTagString)nbt).getString());
				}
				for(String group : groups) {
					Group resolved = PermissionHandler.INSTANCE.getGroup(group);
					if (resolved==null) {
						continue;
					}
					actor.groups.add(resolved);
				}
				
				
			}
		}
		
		
		
	}
	
	private static void updatePlayerNBT(Actor actor, EntityPlayer player) {
		NBTTagCompound tag = player.getEntityData();
		NBTTagCompound root = new NBTTagCompound();
		NBTTagList groupsList = new NBTTagList();
		synchronized(actor) {
			for(Group group : actor.groups) {
				groupsList.appendTag(new NBTTagString(group.getName()));
			}
			
		}
		root.setTag("Groups", groupsList);
		
	}
}
