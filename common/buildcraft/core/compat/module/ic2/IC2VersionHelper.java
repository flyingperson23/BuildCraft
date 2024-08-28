package buildcraft.core.compat.module.ic2;

import buildcraft.api.statements.IActionExternal;
import buildcraft.api.statements.ITriggerExternal;
import buildcraft.core.compat.module.ic2.cl.ClIC2Statements;
import buildcraft.core.compat.module.ic2.exp.ExpIC2Statements;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;
import java.util.Collection;

public class IC2VersionHelper {
    public static void addExternalTriggers(Collection<ITriggerExternal> res, @Nonnull EnumFacing side, TileEntity tile) {
        Class<?> clazz = null;
        try {
            clazz = Class.forName("ic2/core/platform/lang/components/base/LocaleComp");
        } catch (ClassNotFoundException e) {}

        if (clazz == null) {
            ExpIC2Statements.addTriggers(res, side, tile);
        } else {
            ClIC2Statements.addTriggers(res, side, tile);
        }

    }

    public static void addExternalActions(Collection<IActionExternal> res, @Nonnull EnumFacing side, TileEntity tile) {
        Class<?> clazz = null;
        try {
            clazz = Class.forName("ic2/core/platform/lang/components/base/LocaleComp");
        } catch (ClassNotFoundException e) {}

        if (clazz == null) {
            ExpIC2Statements.addActions(res, side, tile);
        }
    }
}
