package com.bankblacklist;


import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;


@ConfigGroup("Block List")
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
}
