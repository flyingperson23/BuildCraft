/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.factory;

import buildcraft.api.BCBlocks;
import buildcraft.lib.recipe.RecipeBuilderShaped;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class BCFactoryRecipes {
    public static void init() {
        {
            RecipeBuilderShaped builder = new RecipeBuilderShaped();
            builder.add("gwg");
            builder.map('w', "craftingTableWood");
            builder.map('g', "gearStone");
            builder.setResult(new ItemStack(BCBlocks.FACTORY_AUTOWORKBENCH_ITEM));
            builder.register();
            builder.registerRotated();
        }

        {
            RecipeBuilderShaped builder = new RecipeBuilderShaped();
            builder.add("iri");
            builder.add("igi");
            builder.add("ipi");
            builder.map('i', "ingotIron");
            builder.map('r', "dustRedstone");
            builder.map('g', "gearIron");
            builder.map('p', Items.IRON_PICKAXE);
            builder.setResult(new ItemStack(BCBlocks.FACTORY_MINING_WELL));
            builder.register();
        }

        {
            RecipeBuilderShaped builder = new RecipeBuilderShaped();
            builder.add("ggg");
            builder.add("g g");
            builder.add("ggg");
            builder.map('g', "blockGlassColorless");
            builder.setResult(new ItemStack(BCBlocks.FACTORY_TANK));
            builder.register();
        }

        {
            RecipeBuilderShaped builder = new RecipeBuilderShaped();
            builder.add("iri");
            builder.add("igi");
            builder.add("tbt");
            builder.map('i', "ingotIron");
            builder.map('r', "dustRedstone");
            builder.map('g', "gearIron");
            builder.map('b', Items.BUCKET);
            builder.map('t', BCBlocks.FACTORY_TANK, "blockGlassColorless");
            builder.setResult(new ItemStack(BCBlocks.FACTORY_PUMP));
            builder.register();
        }

        {
            RecipeBuilderShaped builder = new RecipeBuilderShaped();
            builder.add("igi");
            builder.add("btb");
            builder.add("ibi");
            builder.map('i', "ingotIron");
            builder.map('g', "gearIron");
            builder.map('b', Blocks.IRON_BARS);
            builder.map('t', BCBlocks.FACTORY_TANK, "blockGlassColorless");
            builder.setResult(new ItemStack(BCBlocks.FACTORY_FLOOD_GATE));
            builder.register();
        }

        {
            RecipeBuilderShaped builder = new RecipeBuilderShaped();
            builder.add("ici");
            builder.add("igi");
            builder.add(" i ");
            builder.map('i', "ingotIron");
            builder.map('g', "gearStone");
            builder.map('c', Blocks.CHEST);
            builder.setResult(new ItemStack(BCBlocks.FACTORY_CHUTE));
            builder.register();
        }
    }
}
