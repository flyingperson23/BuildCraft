package buildcraft.core.compat.module.ic2.exp;

import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerExternal;
import buildcraft.core.BCCoreSprites;
import buildcraft.core.statements.BCStatement;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.misc.LocaleUtil;
import ic2.api.energy.tile.IChargingSlot;
import ic2.api.energy.tile.IDischargingSlot;
import ic2.api.item.ElectricItem;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.comp.Energy;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.machine.tileentity.TileEntityElectricMachine;
import ic2.core.block.wiring.TileEntityElectricBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import java.lang.reflect.Field;
import java.util.List;

public class ExpTriggerPowerItem extends BCStatement implements ITriggerExternal {
    private final boolean charging;
    private final int id;
    // 0 - full/empty, 1 - low, 2 - high

    public ExpTriggerPowerItem(boolean charging, int id) {
        super("buildcraft:eu" + (charging ? "Charge" : "Discharge")+id);
        this.charging = charging;
        this.id = id;
    }

    @Override
    public SpriteHolderRegistry.SpriteHolder getSprite() {
        if (id == 1) return BCCoreSprites.CHARGE_LOW;
        if (id == 2) return BCCoreSprites.CHARGE_HIGH;
        if (charging) return BCCoreSprites.CHARGE_FULL;
        return BCCoreSprites.CHARGE_EMPTY;
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("gate.trigger.machine.eu." + (charging ? "charge." : "discharge.")+id);
    }

    private static Energy getEnergy(TileEntity tile) {
        if (tile instanceof TileEntityElectricMachine) {
            if (((TileEntityBlock) tile).hasComponent(Energy.class)) {
                return ((TileEntityBlock) tile).getComponent(Energy.class);
            }
        }
        return null;
    }

    private static List<InvSlot> getSlots(TileEntity tile) {
        Energy energy = getEnergy(tile);
        if (energy == null) return null;
        try {
            Field f = Energy.class.getDeclaredField("managedSlots");
            f.setAccessible(true);
            return (List<InvSlot>) f.get(energy);
        } catch(Exception ignored) {}
        return null;
    }

    public static boolean isTriggeringTile(TileEntity tile, boolean charge) {
        if (tile instanceof TileEntityElectricBlock) return true;
        List<InvSlot> slots = getSlots(tile);
        if (slots != null) {
            for (InvSlot slot : slots) {
                if (slot instanceof IChargingSlot && charge) return true;
                if (slot instanceof IDischargingSlot && !charge) return true;
            }
        }
        return false;
    }

    @Override
    public boolean isTriggerActive(TileEntity target, EnumFacing side, IStatementContainer source, IStatementParameter[] parameters) {
        if (target instanceof TileEntityElectricBlock) {
            TileEntityElectricBlock block = (TileEntityElectricBlock) target;
            if (charging) {
                double charge = ElectricItem.manager.getCharge(block.chargeSlot.get());
                double max = ElectricItem.manager.getMaxCharge(block.chargeSlot.get());
                double ratio = charge / max;
                if (max != 0) {
                    if (id == 0 && ratio == 1) return true;
                    if (id == 1 && ratio <= 0.05) return true;
                    if (id == 2 && ratio >= 0.95) return true;
                }
            } else {
                double charge = ElectricItem.manager.getCharge(block.dischargeSlot.get());
                double max = ElectricItem.manager.getMaxCharge(block.dischargeSlot.get());
                double ratio = charge / max;
                if (max != 0) {
                    if (id == 0 && ratio == 0) return true;
                    if (id == 1 && ratio <= 0.05) return true;
                    if (id == 2 && ratio >= 0.95) return true;
                }
            }
        } else {
            List<InvSlot> slots = getSlots(target);
            if (slots != null) {
                for (InvSlot slot : slots) {
                    if (slot != null) {
                        if (slot instanceof IChargingSlot && charging) {
                            double charge = ElectricItem.manager.getCharge(slot.get());
                            double max = ElectricItem.manager.getMaxCharge(slot.get());
                            double ratio = charge / max;
                            if (max != 0) {
                                if (id == 0 && ratio == 1) return true;
                                if (id == 1 && ratio <= 0.05) return true;
                                if (id == 2 && ratio >= 0.95) return true;
                            }
                        } else if (slot instanceof IDischargingSlot && !charging) {
                            double charge = ElectricItem.manager.getCharge(slot.get());
                            double max = ElectricItem.manager.getMaxCharge(slot.get());
                            double ratio = charge / max;
                            if (max != 0) {
                                if (id == 0 && ratio == 0) return true;
                                if (id == 1 && ratio <= 0.05) return true;
                                if (id == 2 && ratio >= 0.95) return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public IStatement[] getPossible() {
        return charging ? ExpIC2Statements.CHARGE_ITEM : ExpIC2Statements.DISCHARGE_ITEM;
    }
}
