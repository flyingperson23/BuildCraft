/*
 * Copyright (c) 2020 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.compat;

import java.util.HashMap;
import java.util.Map;

import buildcraft.core.compat.module.ic2.CompatModuleIndustrialCraft2;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import buildcraft.api.core.BCLog;

import buildcraft.core.compat.module.crafttweaker.CompatModuleCraftTweaker;
import buildcraft.core.compat.module.forestry.CompatModuleForestry;
import buildcraft.core.compat.module.theoneprobe.CompatModuleTheOneProbe;
import buildcraft.core.compat.network.CompatGui;
import buildcraft.core.BCCoreConfig;

//@formatter:off
@Mod(
        modid = BCCompat.MODID,
        name = "BuildCraft Compat",
        version = BCCompat.VERSION,
        updateJSON = "https://mod-buildcraft.com/version/versions-compat.json",
        acceptedMinecraftVersions = "(gradle_replace_mcversion,)",
        dependencies = BCCompat.DEPENDENCIES
)
//@formatter:on
public class BCCompat {

    static final String DEPENDENCIES = "required-after:forge@(gradle_replace_forgeversion,)"//
        + ";required-after:buildcraftcore@[$bc_version,)"//
        + ";after:buildcrafttransport"//
        + ";after:buildcraftbuilders"//
        + ";after:buildcraftsilicon"//
        + ";after:theoneprobe"//
        + ";after:forestry"//
        + ";after:crafttweaker"//
        + ";after:ic2"//
    ;

    public static final String MODID = "buildcraftcompat";
    public static final String VERSION = "$version";

    @Mod.Instance(MODID)
    public static BCCompat instance;

    private static final Map<String, CompatModuleBase> modules = new HashMap<>();

    private static void offerAndPreInitModule(final CompatModuleBase module) {
        String cModId = module.compatModId();
        if (module.canLoad()) {
            Property prop = BCCoreConfig.config.get("modules", cModId, true);
            prop.setComment("Enable compat for "+cModId+"?");
            if (prop.getBoolean(true)) {
                modules.put(cModId, module);
                BCLog.logger.info("[compat]   + " + cModId);
                module.preInit();
            } else {
                BCLog.logger.info("[compat]   x " + cModId + " (It has been disabled in the config)");
            }
        } else {
            BCLog.logger.info("[compat]   x " + cModId + " (It cannot load)");
        }
    }

    @Mod.EventHandler
    public static void preInit(final FMLPreInitializationEvent evt) {

        // List of all modules
        offerAndPreInitModule(new CompatModuleForestry());
        offerAndPreInitModule(new CompatModuleTheOneProbe());
        offerAndPreInitModule(new CompatModuleCraftTweaker());
        offerAndPreInitModule(new CompatModuleIndustrialCraft2());
        // End of module list





    }

    @Mod.EventHandler
    public static void init(final FMLInitializationEvent evt) {
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, CompatGui.guiHandlerProxy);

        // compatChannelHandler = new ChannelHandler();
        // MinecraftForge.EVENT_BUS.register(this);

        // compatChannelHandler.registerPacketType(PacketGenomeFilterChange.class);
        // compatChannelHandler.registerPacketType(PacketTypeFilterChange.class);
        // compatChannelHandler.registerPacketType(PacketRequestFilterSet.class);

        for (final CompatModuleBase m : modules.values()) {
            m.init();
        }
    }

    @Mod.EventHandler
    public static void postInit(final FMLPostInitializationEvent evt) {
        for (final CompatModuleBase m : modules.values()) {
            m.postInit();
        }
    }
}
