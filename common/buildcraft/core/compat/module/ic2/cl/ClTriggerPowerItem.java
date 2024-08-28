package buildcraft.core.compat.module.ic2.cl;

import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerExternal;
import buildcraft.core.BCCoreSprites;
import buildcraft.core.statements.BCStatement;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.misc.LocaleUtil;
import ic2.api.item.ElectricItem;
import ic2.core.block.base.tile.TileEntityElectricBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public class ClTriggerPowerItem extends BCStatement implements ITriggerExternal {
    private final boolean charging;
    private final int id;
    // 0 - full/empty, 1 - low, 2 - high

    public ClTriggerPowerItem(boolean charging, int id) {
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

    public static boolean isTriggeringTile(TileEntity tile, boolean charge) {
        return tile instanceof TileEntityElectricBlock;
    }

    @Override
    public boolean isTriggerActive(TileEntity target, EnumFacing side, IStatementContainer source, IStatementParameter[] parameters) {
        if (target != null && target instanceof TileEntityElectricBlock) {
            TileEntityElectricBlock block = (TileEntityElectricBlock) target;
            ItemStack i = block.getStackInSlot(charging ? 0 : 1);
            double charge = ElectricItem.manager.getCharge(i);
            double max = ElectricItem.manager.getMaxCharge(i);
            double ratio = charge / max;
            if (max != 0) {
                if (charging && id == 0 && ratio == 1) return true;
                if (!charging && id == 0 && ratio == 0) return true;
                if (id == 1 && ratio <= 0.05) return true;
                if (id == 2 && ratio >= 0.95) return true;
            }
        }

        return false;
    }

    @Override
    public IStatement[] getPossible() {
        return charging ? ClIC2Statements.CHARGE_ITEM : ClIC2Statements.DISCHARGE_ITEM;
    }
}
