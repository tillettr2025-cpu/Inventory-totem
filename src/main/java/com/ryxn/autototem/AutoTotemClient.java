package com.ryxn.autototem;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Items;

public class AutoTotemClient implements ClientModInitializer {

    public static AutoTotemConfig CONFIG;

    @Override
    public void onInitializeClient() {
        CONFIG = AutoTotemConfig.load();

        ClientTickEvents.END_CLIENT_TICK.register(
                AutoTotemClient::tick
        );
    }

    private static void tick(MinecraftClient client) {
        if (!CONFIG.enabled || client.player == null) {
            return;
        }

        ClientPlayerEntity player = client.player;

        // Keep the offhand supplied with a Totem.
        if (player.getOffHandStack().isEmpty()) {
            int totemSlot = findTotem(player);

            if (totemSlot >= 0) {
                moveToOffhand(client, totemSlot);
            }
        }

        // Keep a backup Totem in the configured hotbar slot.
        if (CONFIG.backupSlot >= 0 && CONFIG.backupSlot < 9) {
            ensureBackupTotem(client, CONFIG.backupSlot);
        }
    }

    private static int findTotem(ClientPlayerEntity player) {

        // Prefer the configured backup slot.
        int preferred = CONFIG.backupSlot;

        if (preferred >= 0 && preferred < 9) {
            if (player.getInventory()
                    .getStack(preferred)
                    .isOf(Items.TOTEM_OF_UNDYING)) {

                return preferred;
            }
        }

        // Search the rest of the inventory.
        for (int slot = 0;
             slot < player.getInventory().size();
             slot++) {

            if (slot == preferred) {
                continue;
            }

            if (player.getInventory()
                    .getStack(slot)
                    .isOf(Items.TOTEM_OF_UNDYING)) {

                return slot;
            }
        }

        return -1;
    }

    private static void moveToOffhand(
            MinecraftClient client,
            int slot
    ) {

        if (client.interactionManager == null
                || client.player == null) {
            return;
        }

        /*
         * Inventory slot IDs:
         *
         * 0-8   = hotbar
         * 9-35  = main inventory
         * 40    = offhand
         *
         * Swap the selected inventory slot
         * with the offhand slot.
         */

        client.interactionManager.clickSlot(
                client.player.currentScreenHandler.syncId,
                inventorySlotId(slot),
                40,
                net.minecraft.screen.slot.SlotActionType.SWAP,
                client.player
        );
    }

    private static void ensureBackupTotem(
            MinecraftClient client,
            int backupSlot
    ) {

        ClientPlayerEntity player = client.player;

        if (player == null) {
            return;
        }

        if (player.getInventory()
                .getStack(backupSlot)
                .isOf(Items.TOTEM_OF_UNDYING)) {

            return;
        }

        int source = findInventoryTotem(
                player,
                backupSlot
        );

        if (source < 0) {
            return;
        }

        if (client.interactionManager == null) {
            return;
        }

        client.interactionManager.clickSlot(
                player.currentScreenHandler.syncId,
                inventorySlotId(source),
                inventorySlotId(backupSlot),
                net.minecraft.screen.slot.SlotActionType.SWAP,
                player
        );
    }

    private static int findInventoryTotem(
            ClientPlayerEntity player,
            int ignoredSlot
    ) {

        for (int slot = 0;
             slot < player.getInventory().size();
             slot++) {

            if (slot == ignoredSlot) {
                continue;
            }

            if (player.getInventory()
                    .getStack(slot)
                    .isOf(Items.TOTEM_OF_UNDYING)) {

                return slot;
            }
        }

        return -1;
    }

    private static int inventorySlotId(int inventorySlot) {

        /*
         * Player inventory:
         *
         * 0-8  -> hotbar
         * 9-35 -> main inventory
         */

        return inventorySlot < 9
                ? 36 + inventorySlot
                : inventorySlot;
    }
          }
