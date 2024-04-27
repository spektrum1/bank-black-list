package com.bankblacklist;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.ItemID;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.WidgetID;
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
		if (event.getGroupId() != WidgetID.BANK_GROUP_ID)
		{
			return;
		}
		blacklist = formatConfigString();
		loadCurrentBankItems();
		searchBankForContraband();
	}

	private String[] formatConfigString(){
		String[] blackList = config.blackList().split(",");
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
