/*
 * Copyright (c) 2018, Viktor Altintas <viktor.altintas@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.bankblacklist;

import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("Bank Block List")
public interface Config extends net.runelite.client.config.Config
{
	@ConfigItem(
		keyName = "blackList",
		name = "Black List",
		description = "Add items to cause a warning notification if banked."
	)
	default String blackList()
	{
		return "";
	}

	@ConfigItem(
		keyName = "blackList",
		name = "",
		description = ""
	)
	void setBlackList(String key);

	@ConfigItem(
		keyName = "includePlaceholders",
		name = "Include Placeholders",
		description = "Determines if to trigger warnings on placeholders."
	)
	default boolean includePlaceholders()
	{
		return true;
	}

	@ConfigItem(
		keyName = "enableShiftClick",
		name = "Enable shift click to add/remove items",
		description = "Determines if to trigger add/remove option on shift click."
	)
	default boolean enableShiftClick()
	{
		return false;
	}
}
