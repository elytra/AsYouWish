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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.GuardedBy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.authlib.GameProfile;

import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.IPermissionHandler;
import net.minecraftforge.server.permission.context.IContext;

@ParametersAreNonnullByDefault
public final class PermissionHandler implements IPermissionHandler {
	public static final Logger LOG = LogManager.getLogger("AsYouWish");
	public static PermissionHandler INSTANCE;
	
	/** True if nodes has been frozen into an immutable map */
	private volatile boolean frozen = false;
	
	/** Registered permission nodes */
	@GuardedBy("nodes")
	private Map<String, PermissionNode> nodes = new HashMap<>();
	
	/** Guards all objects and operations which define group hierarchy. You MUST synchronize on this object if you
	 * modify *either* the groups list *or* the parent field within any individual Group. */
	protected final Object groupMutex = new Object();
	
	@GuardedBy("groupMutex")
	private HashMap<String, Group> groups = new HashMap<>();
	
	@GuardedBy("actors")
	private HashMap<String, Actor> actors = new HashMap<>();
	
	protected PermissionHandler() {
		INSTANCE = this;
	}
	
	@Override
	public void registerNode(String node, DefaultPermissionLevel level, String desc) {
		synchronized(nodes) {
			PermissionNode existing = nodes.get(node);
			if (existing!=null) {
				if (existing.fallback==level) {
					//We already have this permission registered, but its default level is identical. Allow it and add the desc if we need one.
					if (existing.desc==null || existing.desc.isEmpty()) existing.desc = desc;
					return;
				} else {
					DefaultPermissionLevel result = mostRestrictive(existing.fallback, level);
					existing.fallback = result;
					LOG.warn("The permission node '{}' was registered twice with conflicting default levels. Using the more restrictive '{}'.", node, result);
				}
			}
			
			nodes.put(node, new PermissionNode(node, level, desc));
		}
		
	}
	
	public DefaultPermissionLevel mostRestrictive(DefaultPermissionLevel a, DefaultPermissionLevel b) {
		if (a==DefaultPermissionLevel.NONE || b==DefaultPermissionLevel.NONE) return DefaultPermissionLevel.NONE;
		if (a==DefaultPermissionLevel.OP || b==DefaultPermissionLevel.OP) return DefaultPermissionLevel.OP;
		return DefaultPermissionLevel.ALL;
	}

	@Override
	public Collection<String> getRegisteredNodes() {
		if (frozen) return nodes.keySet();
		
		synchronized(nodes) {
			return ImmutableSet.copyOf(nodes.keySet());
		}
	}

	@Override
	public boolean hasPermission(GameProfile profile, String node, IContext context) {
		return false;
	}

	@Override
	public String getNodeDescription(String node) {
		synchronized(nodes) {
			PermissionNode cur = nodes.get(node);
			if (cur==null) return "";
			return cur.desc;
		}
	}
	
	
	/* AYW-SPECIFIC API */
	
	protected void freezePermissions() {
		nodes = ImmutableMap.copyOf(nodes);
	}
	
	@Nullable
	public Group getGroup(String name) {
		synchronized(groups) {
			return groups.get(name);
		}
	}
	
	/** Recursively finds all descendants of a parent group. */
	public List<Group> getAllDescendants(Group parent) {
		synchronized(groupMutex) {
			return getAllDescendants(parent, new ArrayList<>());
		}
	}
	
	/** Internal version of getAllDescendants. MUST BE EXTERNALLY SYNCHRONIZED on groupMutex! */
	private List<Group> getAllDescendants(Group parent, List<Group> descendants) {
		for(Group group : groups.values()) {
			if (!descendants.contains(group) && group.parent==parent) {
				descendants.add(group);
				getAllDescendants(group, descendants);
			}
		}
		return descendants;
	}
}
