/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.energy;

import java.util.function.Consumer;

import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import buildcraft.lib.BCLib;
import buildcraft.lib.registry.MigrationManager;
import buildcraft.lib.registry.RegistryConfig;
import buildcraft.lib.registry.TagManager;
import buildcraft.lib.registry.TagManager.EnumTagType;
import buildcraft.lib.registry.TagManager.TagEntry;

import buildcraft.core.BCCore;

//@formatter:off
@Mod(
    modid = BCEnergy.MODID,
    name = "BuildCraft Energy",
    version = BCLib.VERSION,
    dependencies = "required-after:buildcraftcore@[" + BCLib.VERSION + "]"
)
//@formatter:on
public class BCEnergy {
    public static final String MODID = "buildcraftenergy";

    static {
        FluidRegistry.enableUniversalBucket();
    }

    @Mod.Instance(MODID)
    public static BCEnergy INSTANCE;

    @Mod.EventHandler
    public static void preInit(FMLPreInitializationEvent evt) {
        RegistryConfig.useOtherModConfigFor(MODID, BCCore.MODID);
        BCEnergyConfig.preInit();
        BCEnergyEntities.preInit();
        BCEnergyFluids.preInit();
        BCEnergyBlocks.preInit();
        BCEnergyItems.preInit();

        NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, BCEnergyProxy.getProxy());

        BCEnergyProxy.getProxy().fmlPreInit();
    }

    @Mod.EventHandler
    public static void init(FMLInitializationEvent evt) {
        BCEnergyRecipes.init();
        BCEnergyWorldGen.init();
        BCEnergyProxy.getProxy().fmlInit();
    }

    @Mod.EventHandler
    public static void postInit(FMLPostInitializationEvent evt) {
        BCEnergyProxy.getProxy().fmlPostInit();
        BCEnergyConfig.validateBiomeNames();
        registerMigrations();
    }

    private static void registerMigrations() {
        /** 8.0.x */
        // Unable to perform the following migrations because of an archaic Forge bug: https://github.com/MinecraftForge/MinecraftForge/issues/2768
        /*
        String[] oldOilNames = new String[5 * 3];
        int pointer = 0;
        for (String type : new String[] { "", "_residue", "_dense", "_distilled", "_heavy" }) {
            for (int i = 0; i < 3; i++) {
                oldOilNames[pointer++] = "fluid_block_oil" + type + "_heat_" + i;
            }
        }
        MigrationManager.INSTANCE.addBlockMigration(BCEnergyFluids.oil.getBlock(), oldOilNames);
        pointer = 0;
        String[] oldFuelNames = new String[5 * 3];
        for (String type : new String[] { "gaseous", "light", "mixed_light", "mixed_heavy", "dense" }) {
            for (int i = 0; i < 3; i++) {
                oldFuelNames[pointer++] = "fluid_block_fuel_" + type + "_heat_" + i;
            }
        }
        MigrationManager.INSTANCE.addBlockMigration(BCEnergyFluids.fuel.getBlock(), oldFuelNames);
         */

        // Stopgap: Old default oil/fuel => new default oil/fuel
        MigrationManager.INSTANCE.addBlockMigration(BCEnergyFluids.oil.getBlock(), "fluid_block_oil_heat_0");
        MigrationManager.INSTANCE.addBlockMigration(BCEnergyFluids.fuel.getBlock(), "fluid_block_fuel_light_heat_0");
    }

    static {
        startBatch();
        // Items
        registerTag("item.glob.oil").reg("glob_of_oil").oldReg("glob_oil").locale("globOil").model("glob_oil");

        // Item Blocks

        // Blocks

        // Tiles
        registerTag("tile.engine.stone").reg("engine.stone");
        registerTag("tile.engine.iron").reg("engine.iron");
        registerTag("tile.spring.oil").reg("spring.oil");

        endBatch(TagManager.prependTags("buildcraftenergy:", EnumTagType.REGISTRY_NAME, EnumTagType.MODEL_LOCATION)
            .andThen(TagManager.setTab("buildcraft.main")));
    }

    private static TagEntry registerTag(String id) {
        return TagManager.registerTag(id);
    }

    private static void startBatch() {
        TagManager.startBatch();
    }

    private static void endBatch(Consumer<TagEntry> consumer) {
        TagManager.endBatch(consumer);
    }
}
