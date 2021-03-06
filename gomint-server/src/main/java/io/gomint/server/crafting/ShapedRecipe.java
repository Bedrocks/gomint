/*
 * Copyright (c) 2017, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.crafting;

import io.gomint.inventory.item.ItemAir;
import io.gomint.inventory.item.ItemStack;
import io.gomint.inventory.item.ItemType;
import io.gomint.jraknet.PacketBuffer;
import io.gomint.server.inventory.CraftingInputInventory;
import io.gomint.server.inventory.Inventory;
import io.gomint.server.network.packet.Packet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

/**
 * Resembles a shaped crafting recipe, i.e. a recipe that requires its
 * arrangement to be arranged in specific way and does not accept work
 * if any ingredient is not in the right spot.
 *
 * @author BlackyPaw
 * @version 1.0
 */
public class ShapedRecipe extends CraftingRecipe {

    private final int id;

    private final String name;
    private final String block;

    private final int width;
    private final int height;

    private final ItemStack[] arrangement;
    private final ItemStack[] outcome;

    private Collection<ItemStack> ingredients;

    /**
     * New shaped recipe
     *
     * @param width       The width of the recipe
     * @param height      The height of the recipe
     * @param ingredients Input of the recipe
     * @param outcome     Output of the recipe
     * @param uuid        UUID of the recipe
     */
    public ShapedRecipe(String name, String block, int width, int height, ItemStack[] ingredients, ItemStack[] outcome, UUID uuid, int priority) {
        super(outcome, uuid, priority);
        assert ingredients.length == width * height : "Invalid arrangement: Fill out empty slots with air!";

        this.name = name;
        this.block = block;

        this.width = width;
        this.height = height;
        this.arrangement = ingredients;
        this.outcome = outcome;

        this.id = this.getNewID();
    }

    /**
     * Gets the width of this shaped recipe.
     *
     * @return The width of this shaped recipe
     */
    public int getWidth() {
        return this.width;
    }

    /**
     * Gets the height of this shaped recipe.
     *
     * @return The height of this shaped recipe
     */
    public int getHeight() {
        return this.height;
    }

    @Override
    public ItemStack[] getIngredients() {
        if (this.ingredients == null) {
            // Got to sort out possible AIR slots and combine types:
            this.ingredients = new ArrayList<>();

            for (int j = 0; j < this.height; ++j) {
                for (int i = 0; i < this.width; ++i) {
                    ItemStack stack = this.arrangement[j * this.width + i];
                    if (!(stack instanceof ItemAir)) {
                        this.ingredients.add(stack);
                    }
                }
            }
        }

        return this.ingredients.toArray(new ItemStack[0]);
    }

    @Override
    public void serialize(PacketBuffer buffer) {
        // Type of recipe ( 1 == shaped )
        buffer.writeSignedVarInt(1);

        buffer.writeString(this.name);

        // Size of grid
        buffer.writeSignedVarInt(this.width);
        buffer.writeSignedVarInt(this.height);

        // Input items
        for (int j = 0; j < this.height; ++j) {
            for (int i = 0; i < this.width; ++i) {
                Packet.writeRecipeInput(this.arrangement[j * this.width + i], buffer);
            }
        }

        // Amount of result
        buffer.writeUnsignedVarInt(this.outcome.length);

        for (ItemStack itemStack : this.outcome) {
            Packet.writeItemStack(itemStack, buffer);
        }

        // Write recipe UUID
        buffer.writeUUID(this.getUUID());
        buffer.writeString(this.block);
        buffer.writeSignedVarInt(this.getPriority());
        buffer.writeUnsignedVarInt(this.id);
    }

    @Override
    public int[] isCraftable(Inventory inputInventory) {
        ItemStack[] inputItems = inputInventory.getContents();
        ItemStack[] ingredients = getIngredients();
        int[] consumeSlots = new int[ingredients.length];
        Arrays.fill(consumeSlots, -1);

        for (int rI = 0; rI < ingredients.length; rI++) {
            ItemStack recipeWanted = ingredients[rI];
            boolean found = false;

            for (int i = 0; i < inputItems.length; i++) {
                ItemStack input = inputItems[i];

                if (canBeUsedForCrafting(recipeWanted, input)) {
                    // Check if we already consumed this
                    int alreadyConsumed = 0;
                    for (int consumeSlot : consumeSlots) {
                        if (consumeSlot == i) {
                            alreadyConsumed++;
                        }
                    }

                    if (input.getAmount() >= alreadyConsumed + 1) {
                        consumeSlots[rI] = i;
                        found = true;
                        break;
                    }
                }
            }

            if (!found) {
                return null;
            }
        }

        return consumeSlots;
    }

    private int[] check(Inventory inputInventory) {
        // Order the input so the recipe is in the upper left corner
        int xSpace = 0;
        int zSpace = 0;

        int x;
        int z = x = inputInventory.size() == 4 ? 2 : 3;

        // Default order is:
        // 0 1 2
        // 3 4 5
        // 6 7 8
        for (int i = 0; i < z; i++) {
            boolean freeX = true;
            for (int i2 = 0; i2 < x; i2++) {
                ItemStack itemStack = inputInventory.getItem((i * z) + i2);
                if (itemStack.getItemType() != ItemType.AIR) {
                    freeX = false;
                    break;
                }
            }

            if (!freeX) {
                break;
            } else {
                xSpace++;
            }
        }

        for (int i = 0; i < x; i++) {
            boolean freeZ = true;
            for (int i2 = 0; i2 < z; i2++) {
                ItemStack itemStack = inputInventory.getItem((i2 * z) + i);
                if (itemStack.getItemType() != ItemType.AIR) {
                    freeZ = false;
                    break;
                }
            }

            if (!freeZ) {
                break;
            } else {
                zSpace++;
            }
        }

        // Items not found in grid (only air left)
        if (zSpace == z && xSpace == x) {
            return null;
        }

        int[] consumeItems = new int[this.width * this.height];
        for (int i = 0; i < this.height; i++) {
            for (int j = 0; j < this.width; j++) {
                int itemSlot = ((i + xSpace) * x) + (j + zSpace);

                ItemStack invItem = inputInventory.getItem(itemSlot);
                ItemStack recipeItem = this.arrangement[j + (this.width * i)];

                if (!canBeUsedForCrafting(recipeItem, invItem)) {
                    return null;
                }

                // Ignore AIR
                if (invItem.getItemType() == ItemType.AIR) {
                    continue;
                }

                consumeItems[j + (this.width * i)] = itemSlot;
            }
        }

        return consumeItems;
    }

}
