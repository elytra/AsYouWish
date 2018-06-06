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

import java.util.HashMap;
import java.util.Map;

import javax.annotation.concurrent.GuardedBy;

public class Group {
	protected final String name;
	@GuardedBy("PermissionHandler.INSTANCE.groupMutex")
	protected Group parent;
	protected Map<String, AccessLevel> perms = new HashMap<>();
	protected Map<String, AccessLevel> inherited = new HashMap<>(); //This is just a cache!
	
	public Group(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public Group getParent() {
		synchronized(PermissionHandler.INSTANCE.groupMutex) {
			return parent;
		}
	}
}
