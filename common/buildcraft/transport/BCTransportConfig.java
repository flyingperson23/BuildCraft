/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import buildcraft.api.BCModules;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.transport.pipe.EnumPipeColourType;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeApi.PowerTransferInfo;
import buildcraft.api.transport.pipe.PipeDefinition;

import buildcraft.lib.config.EnumRestartRequirement;
import buildcraft.lib.misc.ConfigUtil;
import buildcraft.lib.misc.MathUtil;

import buildcraft.core.BCCoreConfig;

public class BCTransportConfig {
    public enum PowerLossMode {
        LOSSLESS,
        PERCENTAGE,
        ABSOLUTE;

        public static final PowerLossMode DEFAULT = LOSSLESS;
        public static final PowerLossMode[] VALUES = values();
    }

    private static final long MJ_REQ_MILLIBUCKET_MIN = 100;
    private static final long MJ_REQ_ITEM_MIN = 50_000;

    public static long mjPerMillibucket = 10_000;
    public static long mjPerItem = 100_000;
    public static int baseFlowRate = 50;
    public static int basePowerRate = 80;
    public static boolean fluidPipeColourBorder;
    public static PowerLossMode lossMode = PowerLossMode.DEFAULT;
    public static boolean pipeWrench = true;
    public static boolean pipeRightClick = false;
    public static int maxItemPerTick = 16;

    private static Property propPipeWrench;
    private static Property propPipeRightClick;
    private static Property propMjPerMillibucket;
    private static Property propMjPerItem;
    private static Property propBaseFlowRate;
    private static Property propFluidPipeColourBorder;
    private static Property propLossMode;
    private static Property propMaxItemPerTick;
    private static Property propBasePowerRate;

    public static void preInit() {
        Configuration config = BCCoreConfig.config;
        propMjPerMillibucket = config.get("general", "pipes.mjPerMillibucket", (int) mjPerMillibucket)
            .setMinValue((int) MJ_REQ_MILLIBUCKET_MIN);
        propMjPerMillibucket.setComment("Fluid pipe MJ per millibucket");
        EnumRestartRequirement.WORLD.setTo(propMjPerMillibucket);

        propMjPerItem = config.get("general", "pipes.mjPerItem", (int) mjPerItem).setMinValue((int) MJ_REQ_ITEM_MIN);
        propMjPerItem.setComment("Item pipe MJ per item");
        EnumRestartRequirement.WORLD.setTo(propMjPerItem);

        propBaseFlowRate = config.get("general", "pipes.baseFluidRate", baseFlowRate).setMinValue(1).setMaxValue(100);
        propBaseFlowRate.setComment("Fluid pipe base flow rate");
        EnumRestartRequirement.WORLD.setTo(propBaseFlowRate);

        propMaxItemPerTick = config.get("general", "pipes.maxItemsPerTick", maxItemPerTick).setMinValue(1).setMaxValue(64);
        propMaxItemPerTick.setComment("Max items a wooden pipe can extract every tick");
        EnumRestartRequirement.WORLD.setTo(propMaxItemPerTick);

        propBasePowerRate = config.get("general", "pipes.basePowerRate", basePowerRate).setMinValue(10).setMaxValue(1000);
        propBasePowerRate.setComment("Power pipe base flow rate");
        EnumRestartRequirement.WORLD.setTo(propBasePowerRate);

        propFluidPipeColourBorder = config.get("display", "pipes.fluidColourIsBorder", true);
        propFluidPipeColourBorder.setComment("Fluid pipe border color?");
        EnumRestartRequirement.WORLD.setTo(propFluidPipeColourBorder);

        propLossMode = config.get("experimental", "kinesisLossMode", "lossless");
        propLossMode.setComment("Lossless mode?");
        ConfigUtil.setEnumProperty(propLossMode, PowerLossMode.VALUES);
        EnumRestartRequirement.WORLD.setTo(propLossMode);

        propPipeWrench = config.get("general", "pipes.wrenchRotate", true);
        propPipeWrench.setComment("Should a wrench be able to rotate directional pipes?");
        EnumRestartRequirement.WORLD.setTo(propPipeWrench);

        propPipeRightClick = config.get("general", "pipes.rightClickRotate", false);
        propPipeRightClick.setComment("Should shift right clicking be able to rotate directional pipes?");
        EnumRestartRequirement.WORLD.setTo(propPipeRightClick);

        MinecraftForge.EVENT_BUS.register(BCTransportConfig.class);
    }

    public static void reloadConfig(EnumRestartRequirement restarted) {

        if (EnumRestartRequirement.WORLD.hasBeenRestarted(restarted)) {
            mjPerMillibucket = propMjPerMillibucket.getLong();
            if (mjPerMillibucket < MJ_REQ_MILLIBUCKET_MIN) {
                mjPerMillibucket = MJ_REQ_MILLIBUCKET_MIN;
            }

            mjPerItem = propMjPerItem.getLong();
            if (mjPerItem < MJ_REQ_ITEM_MIN) {
                mjPerItem = MJ_REQ_ITEM_MIN;
            }

            baseFlowRate = MathUtil.clamp(propBaseFlowRate.getInt(), 1, 100);
            maxItemPerTick = MathUtil.clamp(propMaxItemPerTick.getInt(), 1, 64);
            basePowerRate = MathUtil.clamp(propBasePowerRate.getInt(), 10, 1000);

            fluidPipeColourBorder = propFluidPipeColourBorder.getBoolean();
            PipeApi.flowFluids.fallbackColourType =
                fluidPipeColourBorder ? EnumPipeColourType.BORDER_INNER : EnumPipeColourType.TRANSLUCENT;

            lossMode = ConfigUtil.parseEnumForConfig(propLossMode, PowerLossMode.DEFAULT);

            pipeWrench = propPipeWrench.getBoolean();
            pipeRightClick = propPipeRightClick.getBoolean();

            fluidTransfer(BCTransportPipes.cobbleFluid, baseFlowRate, 10);
            fluidTransfer(BCTransportPipes.woodFluid, baseFlowRate, 10);

            fluidTransfer(BCTransportPipes.stoneFluid, baseFlowRate * 2, 10);
            fluidTransfer(BCTransportPipes.sandstoneFluid, baseFlowRate * 2, 10);

            fluidTransfer(BCTransportPipes.clayFluid, baseFlowRate * 4, 10);
            fluidTransfer(BCTransportPipes.ironFluid, baseFlowRate * 4, 10);
            fluidTransfer(BCTransportPipes.quartzFluid, baseFlowRate * 4, 10);

            fluidTransfer(BCTransportPipes.diamondFluid, baseFlowRate * 8, 10);
            fluidTransfer(BCTransportPipes.emeraldFluid, baseFlowRate * 8, 10);
            fluidTransfer(BCTransportPipes.goldFluid, baseFlowRate * 8, 2);
            fluidTransfer(BCTransportPipes.voidFluid, baseFlowRate * 8, 10);

            powerTransfer(BCTransportPipes.cobblePower, basePowerRate, false);
            powerTransfer(BCTransportPipes.stonePower, 2 * basePowerRate, false);
            powerTransfer(BCTransportPipes.woodPower, 4 * basePowerRate, true);
            powerTransfer(BCTransportPipes.sandstonePower, 4 * basePowerRate, false);
            powerTransfer(BCTransportPipes.quartzPower, 8 * basePowerRate, false);
            powerTransfer(BCTransportPipes.ironPower, 16 * basePowerRate, false);
            powerTransfer(BCTransportPipes.goldPower, 32 * basePowerRate, false);
            powerTransfer(BCTransportPipes.diamondPower, 128 * basePowerRate, false);
            powerTransfer(BCTransportPipes.emeraldPower, 64 * basePowerRate, true);
        }
    }

    private static void fluidTransfer(PipeDefinition def, int rate, int delay) {
        PipeApi.fluidTransferData.put(def, new PipeApi.FluidTransferInfo(rate, delay));
    }

    private static void powerTransfer(PipeDefinition def, int transferMultiplier, boolean recv) {
        PipeApi.powerTransferData.put(def, PowerTransferInfo.create(transferMultiplier, recv));
    }

    @SubscribeEvent
    public static void onConfigChange(OnConfigChangedEvent cce) {
        if (BCModules.isBcMod(cce.getModID())) {
            EnumRestartRequirement req = EnumRestartRequirement.NONE;
            if (Loader.instance().isInState(LoaderState.AVAILABLE)) {
                // The loaders state will be LoaderState.SERVER_STARTED when we are in a world
                req = EnumRestartRequirement.WORLD;
            }
            reloadConfig(req);
        }
    }
}
