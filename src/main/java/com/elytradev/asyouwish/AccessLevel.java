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

public enum AccessLevel {
	DEFAULT_ALLOW,
	DEFAULT_FORBID,
	ALLOW,
	FORBID;
	
	public boolean isDefault() {
		return this==DEFAULT_ALLOW || this==DEFAULT_FORBID;
	}
	
	public boolean supersedes(AccessLevel other) {
		/* - Non-Default access is always more relevant than Default access.
		 * - FORBID supersedes everything
		 * 
		 * For these reasons it's reccommended to set a root node with Default-Forbid on anything remotely controversial,
		 * and build up positive ALLOW nodes for role-specific tasks.
		 */
		
		return (!this.isDefault() && other.isDefault()) || (this==FORBID); //FORBID is the unfriendliest permission and clobbers other, friendlier settings
	}
}
