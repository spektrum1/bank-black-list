package com.bankblacklist;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class BankBlackListTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(BankBlackListPlugin.class);
		RuneLite.main(args);
	}
}