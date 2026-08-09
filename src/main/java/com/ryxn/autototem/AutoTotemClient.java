package com.ryxn.autototem;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public class AutoTotemClient implements ClientModInitializer {

    public static AutoTotemConfig CONFIG;

    private static boolean movingItems = false;
    private static int actionDelay = 0;
    private static boolean openedInventory = false;

    @Override
    public void onInitializeClient() {

        CONFIG = AutoTotemConfig.load();

        ClientTickEvents.END_CLIENT_TICK.register(
                AutoTotemClient::tick
        );
    }

    private static void tick(MinecraftClient client) {

        if (client.player == null || client.world == null) {
            return;
        }

        if (!CONFIG.enabled) {
            return;
        }

        ClientPlayerEntity player = client.player;

        if (actionDelay > 0) {
            actionDelay--;
            return;
        }

        /*
         * Don't interfere with another container,
         * chest, crafting table, etc.
         */
        if (client.currentScreen != null
                && !(client.currentScreen instanceof InventoryScreen)) {

            return;
        }

        /*
         * Nothing to do if the offhand already
         * contains a Totem.
         */
        boolean offhandHasTotem =
                player.getOffHandStack()
                        .isOf(Items.TOTEM_OF_UNDYING);

        boolean backupHasTotem =
                CONFIG.backupSlot >= 0
                        && CONFIG.backupSlot < 9
                        && player.getInventory()
                        .getStack(CONFIG.backupSlot)
                        .isOf(Items.TOTEM_OF_UNDYING);

        /*
         * Everything is already ready.
         */
        if (offhandHasTotem && backupHasTotem) {
            movingItems = false;
            openedInventory = false;
            return;
        }

        /*
         * Find a Totem somewhere in the inventory.
         */
        int source = findTotem(player);

        if (source < 0) {
            movingItems = false;
            return;
        }

        /*
         * Open the player's inventory visibly.
         */
        if (!(client.currentScreen instanceof InventoryScreen)) {

            client.setScreen(
                    new InventoryScreen(player)
            );

            movingItems = true;
            openedInventory = true;

            /*
             * Give the screen one tick to initialize.
             */
            actionDelay = 2;

            return;
        }

        /*
         * Inventory is now open.
         */
        if (movingItems) {

            if (!offhandHasTotem) {

                moveToOffhand(
                        client,
                        source
                );

                actionDelay = 2;
                return;
            }

            /*
             * Re-check because moving the first
             * Totem may have changed the inventory.
             */
            source = findTotemForBackup(player);

            if (source >= 0
                    && !backupHasTotem) {

                moveToBackupSlot(
                        client,
                        source,
                        CONFIG.backupSlot
                );

                actionDelay = 3;
                return;
            }

            /*
             * Finished.
             */
            movingItems = false;

            /*
             * Close the visible inventory.
             */
            if (openedInventory
                    && client.currentScreen
                    instanceof InventoryScreen) {

                client.setScreen(null);
            }

            openedInventory = false;
        }
    }

    /*
     * Find any Totem in the player's inventory.
     */
    private static int findTotem(
            ClientPlayerEntity player
    ) {

        /*
         * First search the hotbar.
         */
        for (int slot = 0; slot < 9; slot++) {

            if (player.getInventory()
                    .getStack(slot)
                    .isOf(Items.TOTEM_OF_UNDYING)) {

                return slot;
            }
        }

        /*
         * Then search the main inventory.
         */
        for (int slot = 9;
             slot < 36;
             slot++) {

            if (player.getInventory()
                    .getStack(slot)
                    .isOf(Items.TOTEM_OF_UNDYING)) {

                return slot;
            }
        }

        return -1;
    }

    /*
     * Find a Totem for the backup slot.
     */
    private static int findTotemForBackup(
            ClientPlayerEntity player
    ) {

        int backup =
                CONFIG.backupSlot;

        for (int slot = 0;
             slot < 36;
             slot++) {

            if (slot == backup) {
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

    /*
     * Move a Totem from an inventory slot
     * into the offhand.
     *
     * Player inventory handler slot IDs:
     *
     * Hotbar:
     * 36-44
     *
     * Main inventory:
     * 9-35
     *
     * Offhand:
     * 45
     */
    private static void moveToOffhand(
            MinecraftClient client,
            int inventorySlot
    ) {

        if (client.player == null
                || client.interactionManager == null) {

            return;
        }

        int sourceId =
                inventorySlotId(inventorySlot);

        int offhandId = 45;

        int syncId =
                client.player
                        .currentScreenHandler
                        .syncId;

        /*
         * Pick up the Totem.
         */
        client.interactionManager.clickSlot(
                syncId,
                sourceId,
                0,
                SlotActionType.PICKUP,
                client.player
        );

        /*
         * Place it into offhand.
         */
        client.interactionManager.clickSlot(
                syncId,
                offhandId,
                0,
                SlotActionType.PICKUP,
                client.player
        );
    }

    /*
     * Move/swap a Totem into the configured
     * hotbar slot.
     */
    private static void moveToBackupSlot(
            MinecraftClient client,
            int source,
            int backupSlot
    ) {

        if (client.player == null
                || client.interactionManager == null) {

            return;
        }

        if (backupSlot < 0
                || backupSlot > 8) {

            return;
        }

        int sourceId =
                inventorySlotId(source);

        int destinationId =
                inventorySlotId(backupSlot);

        int syncId =
                client.player
                        .currentScreenHandler
                        .syncId;

        /*
         * If destination is empty, two clicks
         * are enough.
         *
         * If it contains another item, the
         * third click puts that item back.
         */
        boolean destinationEmpty =
                client.player
                        .getInventory()
                        .getStack(backupSlot)
                        .isEmpty();

        /*
         * Pick up the Totem.
         */
        client.interactionManager.clickSlot(
                syncId,
                sourceId,
                0,
                SlotActionType.PICKUP,
                client.player
        );

        /*
         * Put the Totem into the backup slot.
         */
        client.interactionManager.clickSlot(
                syncId,
                destinationId,
                0,
                SlotActionType.PICKUP,
                client.player
        );

        /*
         * If the backup slot contained another
         * item, the cursor now contains it.
         *
         * Put that item back into the source slot.
         */
        if (!destinationEmpty) {

            client.interactionManager.clickSlot(
                    syncId,
                    sourceId,
                    0,
                    SlotActionType.PICKUP,
                    client.player
            );
        }
    }

    /*
     * Convert player's inventory index into
     * PlayerScreenHandler slot ID.
     */
    private static int inventorySlotId(
            int inventorySlot
    ) {

        /*
         * Hotbar inventory 0-8 maps to
         * handler slots 36-44.
         */
        if (inventorySlot >= 0
                && inventorySlot < 9) {

            return 36 + inventorySlot;
        }

        /*
         * Main inventory 9-35 uses
         * the same handler IDs.
         */
        return inventorySlot;
    }
}
