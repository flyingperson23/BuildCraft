package buildcraft.core.compat.module.ic2.exp;

import buildcraft.api.statements.IActionExternal;
import buildcraft.api.statements.ITriggerExternal;
import buildcraft.core.statements.TriggerPower;
import ic2.core.block.wiring.TileEntityElectricBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;
import java.util.Collection;

public class ExpIC2Statements {
    public static final ExpTriggerEU TRIGGER_EU_HIGH = new ExpTriggerEU(true);
    public static final ExpTriggerEU TRIGGER_EU_LOW = new ExpTriggerEU(false);
    public static final ExpTriggerEU[] TRIGGER_EU = { TRIGGER_EU_HIGH, TRIGGER_EU_LOW };

    public static final ExpTriggerPowerItem CHARGE_FULL = new ExpTriggerPowerItem(true, 0);
    public static final ExpTriggerPowerItem CHARGE_LOW = new ExpTriggerPowerItem(true, 1);
    public static final ExpTriggerPowerItem CHARGE_HIGH = new ExpTriggerPowerItem(true, 2);

    public static final ExpTriggerPowerItem DISCHARGE_EMPTY = new ExpTriggerPowerItem(false, 0);
    public static final ExpTriggerPowerItem DISCHARGE_LOW = new ExpTriggerPowerItem(false, 1);
    public static final ExpTriggerPowerItem DISCHARGE_HIGH = new ExpTriggerPowerItem(false, 2);

    public static final ExpTriggerPowerItem[] CHARGE_ITEM = {CHARGE_LOW, CHARGE_HIGH, CHARGE_FULL};
    public static final ExpTriggerPowerItem[] DISCHARGE_ITEM = {DISCHARGE_EMPTY, DISCHARGE_LOW, DISCHARGE_HIGH};

    public static final ExpActionSetRedstoneMode[] REDSTONE_MODE = new ExpActionSetRedstoneMode[TileEntityElectricBlock.redstoneModes];

    static {
        for (byte i = 0; i < REDSTONE_MODE.length; i++) {
            REDSTONE_MODE[i] = new ExpActionSetRedstoneMode(i);
        }
    }

    public static void addTriggers(Collection<ITriggerExternal> res, @Nonnull EnumFacing side, TileEntity tile) {
        if (!TriggerPower.isTriggeringTile(tile) && ExpTriggerEU.isTriggeringTile(tile)) {
            res.add(TRIGGER_EU_HIGH);
            res.add(TRIGGER_EU_LOW);
        }

        if (ExpTriggerPowerItem.isTriggeringTile(tile, true)) {
            res.add(CHARGE_LOW);
            res.add(CHARGE_HIGH);
            res.add(CHARGE_FULL);
        }
        if (ExpTriggerPowerItem.isTriggeringTile(tile, false)) {
            res.add(DISCHARGE_EMPTY);
            res.add(DISCHARGE_LOW);
            res.add(DISCHARGE_HIGH);
        }
    }

    public static void addActions(Collection<IActionExternal> res, @Nonnull EnumFacing side, TileEntity tile) {
        if (ExpActionSetRedstoneMode.canActivate(tile)) {
            res.addAll(java.util.Arrays.asList(REDSTONE_MODE));
        }
    }

}
