package com.Crowdsourcing.nailbending;

import com.Crowdsourcing.CrowdsourcingManager;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.Skill;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.eventbus.Subscribe;

public class CrowdsourcingNailBending
{
	private static final String NAIL_SUCCESS = "You use a nail.";
	private static final String NAIL_FAILURE = "You accidentally bend a nail.";

	private String lastMessage;
	private int lastMessageTick;
	private Map<Integer, Integer> NAILS = new HashMap<Integer, Integer>()
	{
		{
			put(ItemID.BRONZE_NAILS, 0);
			put(ItemID.IRON_NAILS, 0);
			put(ItemID.STEEL_NAILS, 0);
			put(ItemID.BLACK_NAILS, 0);
			put(ItemID.MITHRIL_NAILS, 0);
			put(ItemID.ADAMANTITE_NAILS, 0);
			put(ItemID.RUNE_NAILS, 0);
		}
	};

	@Inject
	public CrowdsourcingManager manager;

	@Inject
	public Client client;

	private boolean hasCrystalSaw()
	{
		final ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
		if (inventory == null)
		{
			return false;
		}
		return inventory.contains(ItemID.CRYSTAL_SAW);
	}

	@Subscribe
	public void onChatMessage(final ChatMessage event)
	{
		if (!ChatMessageType.SPAM.equals(event.getType()))
		{
			return;
		}

		lastMessage = event.getMessage();
		lastMessageTick = client.getTickCount();
	}

	@Subscribe
	public void onItemContainerChanged(final ItemContainerChanged event)
	{
		if (event.getContainerId() != InventoryID.INVENTORY.getId())
		{
			return;
		}

		ItemContainer inventoryContainer = event.getItemContainer();
		if (inventoryContainer == null)
		{
			return;
		}

		int usedNailType = -1;
		final Map<Integer, Integer> updatedNails = new HashMap<>();
		final Map<Integer, Integer> currentInventory = new HashMap<>();
		for (Item item : inventoryContainer.getItems())
		{
			currentInventory.put(item.getId(), item.getQuantity());
		}
		for (final int nailId : NAILS.keySet())
		{
			int newQuantity = 0;
			if (currentInventory.containsKey(nailId))
			{
				newQuantity = currentInventory.get(nailId);
			}
			updatedNails.put(nailId, newQuantity);
			final int quantityDifference = newQuantity - NAILS.get(nailId);
			if (quantityDifference == -1)
			{
				usedNailType = nailId;
			}
		}

		if (lastMessageTick == client.getTickCount() &&
			(NAIL_SUCCESS.equals(lastMessage) || NAIL_FAILURE.equals(lastMessage)))
		{
			final int constructionLevel = client.getBoostedSkillLevel(Skill.CONSTRUCTION);
			NailBendingData data = new NailBendingData(lastMessage, hasCrystalSaw(), usedNailType, constructionLevel);
			manager.storeEvent(data);
		}

		NAILS = updatedNails;
	}
}
