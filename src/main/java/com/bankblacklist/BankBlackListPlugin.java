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
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.Menu;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetUtil;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

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
	@Inject
	private ItemManager itemManager;

	private String[] blacklist;

	private ItemContainer bankItems;

	private boolean PLACEHOLDERS_INCLUDED = true;

	private static final String	BLACKLIST_MESSAGE = "You have a blacklisted item in your bank: ";

	private final List<Integer> includedMenus = Arrays.asList(InterfaceID.BANK, InterfaceID.INVENTORY);

	@Subscribe
	public void onWidgetClosed(WidgetClosed event)
	{
		if (event.getGroupId() != InterfaceID.BANK)
		{
			return;
		}
		blacklist = formatBlacklistFromConfig();
		PLACEHOLDERS_INCLUDED = config.includePlaceholders();
		loadCurrentBankItems();
		searchBankForContraband();
	}

	@Subscribe
	public void onMenuOpened(final MenuOpened event)
	{
		if (!client.isKeyPressed(KeyCode.KC_SHIFT) || !config.enableShiftClick())
		{
			return;
		}
		blacklist = formatBlacklistFromConfig();
		final MenuEntry[] entries = event.getMenuEntries();
		for (int idx = entries.length - 1; idx >= 0; --idx)
		{
			final MenuEntry entry = entries[idx];
			final Widget w = entry.getWidget();

			if (w != null && includedMenus.contains(WidgetUtil.componentToInterface(w.getId()))
				&& "Examine".equals(entry.getOption()) && entry.getIdentifier() == 10)
			{
				final String itemName = itemManager.getItemComposition(w.getItemId()).getName();

				final MenuEntry parent = client.getMenu().createMenuEntry(idx)
					.setOption("Black list")
					.setTarget(entry.getTarget())
					.setType(MenuAction.RUNELITE);
				final Menu submenu = parent.createSubMenu();

				if (Arrays.stream(blacklist).anyMatch(itemName::equals))
				{
					submenu.createMenuEntry(0)
						.setOption("Remove from list")
						.setType(MenuAction.RUNELITE)
						.onClick(e ->
						{
							removeBlackListItem(itemName);
						});
				}
				else {
					submenu.createMenuEntry(0)
						.setOption("Add to list")
						.setType(MenuAction.RUNELITE)
						.onClick(e ->
						{
							addBlackListItem(itemName);
						});
				}
			}
		}
	}

	private String[] formatBlacklistFromConfig() {
		String initialBlackList = config.blackList().trim();
		String[] blackList = initialBlackList.split(",");

		String[] formattedBlacklistArray = Arrays.stream(blackList)
			.map(String::trim)
			.filter(item -> !item.isEmpty())
			.map(item ->
			{
				String lowercased = item.toLowerCase();
				return Character.toUpperCase(lowercased.charAt(0)) + lowercased.substring(1);
			})
			.toArray(String[]::new);

		String formattedBlacklistSingleString = String.join(",", formattedBlacklistArray);

		config.setBlackList(formattedBlacklistSingleString);

		return formattedBlacklistArray;
	}

	private void searchBankForContraband()
	{
		for (String item: blacklist)
		{
			for (Item bankItem: bankItems.getItems())
			{
				ItemComposition bankItemComposition = itemManager.getItemComposition(bankItem.getId());
				if (bankItemComposition.getPlaceholderTemplateId() != -1 && !PLACEHOLDERS_INCLUDED) //checks if placeholders should be ignored, -1 is a real item while 14401 is a placeholder
				{
					continue;
				}
				String bankItemName = bankItemComposition.getName();
				if (bankItemName.contains(item))
				{
					sendChatMessage(BLACKLIST_MESSAGE + bankItemName);
				}
			}
		}
	}

	private void addBlackListItem(String itemName)
	{
		String currentBlackList = config.blackList().trim();

		if (currentBlackList.isEmpty())
		{
			config.setBlackList(itemName);
		} else
		{
			config.setBlackList(currentBlackList + "," + itemName);
		}
	}

	private void removeBlackListItem(String itemName)
	{
		String[] parts = config.blackList().split(",\\s*");

		StringBuilder result = new StringBuilder();

		// Iterate over the parts and append them if they do not match the substringToRemove
		for (String part : parts) {
			if (!part.equalsIgnoreCase(itemName)) {
				if (result.length() > 0) {
					result.append(", ");
				}
				result.append(part);
			}
		}
		config.setBlackList(result.toString());
	}


	private void loadCurrentBankItems()
	{
		bankItems = client.getItemContainer(InventoryID.BANK);
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
