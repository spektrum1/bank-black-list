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

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.ItemID;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import java.lang.reflect.Field;

@Slf4j
@PluginDescriptor(
	name = "Bank Black List"
)
public class BankBlackListPlugin extends Plugin
{
	@Inject
	private Client client;
	@Inject
	private Config config;
	@Inject
	private ChatMessageManager chatMessageManager;

	private String[] blacklist;

	private ItemContainer bankItems;

	private static final String	BLACKLIST_MESSAGE = "You have a blacklisted item in your bank: ";

	@Subscribe
	public void onWidgetClosed(WidgetClosed event)
	{
		if (event.getGroupId() != InterfaceID.BANK)
		{
			return;
		}
		blacklist = formatBlacklistFromConfig();
		loadCurrentBankItems();
		searchBankForContraband();
	}

	private String[] formatBlacklistFromConfig(){
		String initialBlackList = config.blackList().trim();
		String[] blackList = initialBlackList.split(",");
		String[] blackListFormatted = new String[blackList.length];
		for (int i = 0; i < blackList.length; i++) {
			blackListFormatted[i] = blackList[i].trim();
			blackListFormatted[i] = blackListFormatted[i].toUpperCase();
			blackListFormatted[i] = blackListFormatted[i].replace(" ", "_");
		}
		return blackListFormatted;
	}

	private void searchBankForContraband()
	{
		for (String item: blacklist)
		{
			for (Item bankItem: bankItems.getItems())
			{
				if (bankItem.getId() == getItemID(item))
				{
					sendChatMessage(BLACKLIST_MESSAGE + item.replace("_"," "));
				}
			}
		}
	}

	private void loadCurrentBankItems()
	{
		bankItems = client.getItemContainer(InventoryID.BANK);
	}

	private int getItemID(String item)
	{
		Field field = null;
		int value = 0;
		try
		{
			field = ItemID.class.getDeclaredField(item);
			value = (int)field.get(ItemID.class);
		} catch (NoSuchFieldException e)
		{
			e.printStackTrace();
		} catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
		return value;
	}

	private void sendChatMessage(String chatMessage)
	{
		final String message = new ChatMessageBuilder()
				.append(ChatColorType.HIGHLIGHT)
				.append(chatMessage)
				.build();

		chatMessageManager.queue(
				QueuedMessage.builder()
						.type(ChatMessageType.CONSOLE)
						.runeLiteFormattedMessage(message)
						.build());
	}

	@Provides
	Config provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(Config.class);
	}
}
