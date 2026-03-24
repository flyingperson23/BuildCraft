/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.behaviour;

import java.io.IOException;

import buildcraft.transport.BCTransportConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.items.IItemHandlerModifiable;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.IStackFilter;
import buildcraft.api.transport.IItemPluggable;
import buildcraft.api.transport.pipe.IFlowFluid;
import buildcraft.api.transport.pipe.IFlowItems;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder.PipeMessageReceiver;

import buildcraft.lib.inventory.filter.ArrayFluidFilter;
import buildcraft.lib.inventory.filter.DelegatingItemHandlerFilter;
import buildcraft.lib.inventory.filter.InvertedFluidFilter;
import buildcraft.lib.inventory.filter.InvertedStackFilter;
import buildcraft.lib.inventory.filter.StackFilter;
import buildcraft.lib.misc.EntityUtil;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.tile.item.ItemHandlerSimple;

import buildcraft.transport.BCTransportGuis;

public class PipeBehaviourEmerald extends PipeBehaviourWood {

    public enum FilterMode {
        WHITE_LIST,
        BLACK_LIST,
        ROUND_ROBIN;

        public static FilterMode get(int index) {
            switch (index) {
                default:
                case 0:
                    return WHITE_LIST;
                case 1:
                    return BLACK_LIST;
                case 2:
                    return ROUND_ROBIN;
            }
        }
    }

    public final ItemHandlerSimple filters = new ItemHandlerSimple(9, this::onSlotChanged);
    public FilterMode filterMode = FilterMode.WHITE_LIST;
    public int currentFilter = 0;
    public boolean filterValid = false;

    public PipeBehaviourEmerald(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourEmerald(IPipe pipe, NBTTagCompound nbt) {
        super(pipe, nbt);
        filters.deserializeNBT(nbt.getCompoundTag("filters"));
        filterMode = FilterMode.get(nbt.getByte("mode"));
        currentFilter = nbt.getByte("currentFilter") % filters.getSlots();
        filterValid = !filters.extract(StackFilter.ALL, 1, 1, true).isEmpty();
    }

    @Override
    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = super.writeToNbt();
        nbt.setTag("filters", filters.serializeNBT());
        nbt.setByte("mode", (byte) filterMode.ordinal());
        nbt.setByte("currentFilter", (byte) currentFilter);
        return nbt;
    }

    @Override
    public void readPayload(PacketBuffer buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(buffer, side, ctx);
        if (side == Side.CLIENT) {
            filterMode = FilterMode.get(buffer.readUnsignedByte());
            currentFilter = buffer.readUnsignedByte() % filters.getSlots();
            filterValid = buffer.readBoolean();
        }
    }

    @Override
    public void writePayload(PacketBuffer buffer, Side side) {
        super.writePayload(buffer, side);
        if (side == Side.SERVER) {
            buffer.writeByte(filterMode.ordinal());
            buffer.writeByte(currentFilter);
            buffer.writeBoolean(filterValid);
        }
    }

    @Override
    public boolean onPipeActivate(EntityPlayer player, RayTraceResult trace, float hitX, float hitY, float hitZ,
        EnumPipePart part) {
        ItemStack held = player.getHeldItemMainhand();
        if ((player.isSneaking() && player.isSneaking() && BCTransportConfig.pipeRightClick)
                || (EntityUtil.getWrenchHand(player) != null && BCTransportConfig.pipeWrench)) {
            return super.onPipeActivate(player, trace, hitX, hitY, hitZ, part);
        }
        if (!held.isEmpty()) {
            if (held.getItem() instanceof IItemPluggable) {
                return false;
            }
        }
        if (!player.world.isRemote) {
            BCTransportGuis.PIPE_EMERALD.openGui(player, pipe.getHolder().getPipePos());
        }
        return true;
    }

    private void onSlotChanged(IItemHandlerModifiable itemHandler, int slot, ItemStack before, ItemStack after) {
        if (!after.isEmpty()) {
            if (!filterValid) {
                currentFilter = slot;
                filterValid = true;
            }
        } else if (slot == currentFilter) {
            advanceFilter();
        }
    }

    private IStackFilter getStackFilter() {
        switch (filterMode) {
            default:
            case WHITE_LIST:
                if (filters.extract(s -> true, 1, 1, true).isEmpty()) {
                    return s -> true;
                }
                return new DelegatingItemHandlerFilter(StackUtil::isMatchingItemOrList, filters);
            case BLACK_LIST:
                return new InvertedStackFilter(
                    new DelegatingItemHandlerFilter(StackUtil::isMatchingItemOrList, filters));
            case ROUND_ROBIN:
                return (comparison) -> {
                    ItemStack filter = filters.getStackInSlot(currentFilter);
                    return StackUtil.isMatchingItemOrList(filter, comparison);
                };
        }
    }

    @Override
    protected long extract(long power, boolean simulate) {
        if (power > 0) {
            if (pipe.getFlow() instanceof IFlowItems) {
                IFlowItems flow = (IFlowItems) pipe.getFlow();
                int maxItems = (int) (power / BCTransportConfig.mjPerItem);
                if (maxItems > 64) maxItems = 64;
                if (maxItems > 0) {
                    int extracted = extractItems(flow, getCurrentDir(), maxItems, simulate);
                    if (extracted > 0) {
                        return power - extracted * BCTransportConfig.mjPerItem;
                    }
                }
            } else if (pipe.getFlow() instanceof IFlowFluid) {
                IFlowFluid flow = (IFlowFluid) pipe.getFlow();
                int maxMillibuckets = (int) (power / BCTransportConfig.mjPerMillibucket);
                if (maxMillibuckets > 1000) maxMillibuckets = 1000;
                if (maxMillibuckets > 0) {
                    FluidStack extracted = extractFluid(flow, getCurrentDir(), maxMillibuckets, simulate);
                    if (extracted != null && extracted.amount > 0) {
                        return power - extracted.amount * BCTransportConfig.mjPerMillibucket;
                    }
                }
            }
        }
        return power;
    }




    @Override
    protected int extractItems(IFlowItems flow, EnumFacing dir, int count, boolean simulate) {
        if (filters.getStackInSlot(currentFilter).isEmpty()) {
            advanceFilter();
        }
        int extracted = flow.tryExtractItems(1, getCurrentDir(), null, getStackFilter(), simulate);
        if (extracted > 0 & filterMode == FilterMode.ROUND_ROBIN && !simulate) {
            advanceFilter();
        }
        return extracted;
    }
    @Override
    protected FluidStack extractFluid(IFlowFluid flow, EnumFacing dir, int millibuckets, boolean simulate) {
        if (filters.getStackInSlot(currentFilter).isEmpty()) {
            advanceFilter();
        }

        switch (filterMode) {
            default:
            case WHITE_LIST:
                if (filters.extract(s -> true, 1, 1, true).isEmpty()) {
                    return flow.tryExtractFluid(millibuckets, dir, null, simulate);
                }
                // Firstly try the advanced version - if that fails we will need to try the basic version
                ActionResult<FluidStack> result = flow.tryExtractFluidAdv(millibuckets, dir, new ArrayFluidFilter(filters.stacks), simulate);
                FluidStack extracted = result.getResult();
                if (result.getType() != EnumActionResult.PASS) {
                    return extracted;
                }

                if (extracted.amount <= 0) {
                    for (int i = 0; i < filters.getSlots(); i++) {
                        ItemStack stack = filters.getStackInSlot(i);
                        if (stack.isEmpty()) {
                            continue;
                        }
                        extracted = flow.tryExtractFluid(millibuckets, dir, FluidUtil.getFluidContained(stack), simulate);
                        if (extracted != null && extracted.amount > 0) {
                            return extracted;
                        }
                    }
                }
                return null;
            case BLACK_LIST:
                // We cannot fallback to the basic version - only use the advanced version
                InvertedFluidFilter filter = new InvertedFluidFilter(new ArrayFluidFilter(filters.stacks));
                return flow.tryExtractFluidAdv(millibuckets, dir, filter, simulate).getResult();
            case ROUND_ROBIN:
                // We can't do this -- amounts might differ and its just ugly
                return null;
        }
    }

    private void advanceFilter() {
        int lastFilter = currentFilter;
        filterValid = false;
        while (true) {
            currentFilter++;
            if (currentFilter >= filters.getSlots()) {
                currentFilter = 0;
            }
            if (!filters.getStackInSlot(currentFilter).isEmpty()) {
                filterValid = true;
                break;
            }
            if (currentFilter == lastFilter) {
                break;
            }
        }
        if (lastFilter != currentFilter) {
            pipe.getHolder().scheduleNetworkGuiUpdate(PipeMessageReceiver.BEHAVIOUR);
        }
    }
}
