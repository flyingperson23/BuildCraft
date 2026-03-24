package buildcraft.core.compat.module.forestry;

import buildcraft.api.BCModules;
import buildcraft.api.core.BCLog;
import buildcraft.api.fuels.BuildcraftFuelRegistry;
import buildcraft.api.lists.ListRegistry;

import buildcraft.core.compat.CompatModuleBase;
import buildcraft.core.compat.module.forestry.list.ListMatchGenome;
import buildcraft.core.compat.module.forestry.pipe.ForestryPipes;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class CompatModuleForestry extends CompatModuleBase {
    @Override
    public String compatModId() {
        return "forestry";
    }

    @Override
    public void preInit() {
        ListRegistry.registerHandler(new ListMatchGenome());
        if (canLoadPropolisPipe()) {
            ForestryPipes.preInit();
        }
    }

    @Override
    public void init() {
        Fluid ice = FluidRegistry.getFluid("ice");
        if (ice != null) {
            BuildcraftFuelRegistry.coolant.addCoolant(ice, 0.01f);
        }
    }

    private static boolean canLoadPropolisPipe() {
        if (!BCModules.TRANSPORT.isLoaded()) {
            return false;
        }
        try {
            // Ensure that forestry is up-to-date
            Class.forName("forestry.sorting.tiles.IFilterContainer");
            return true;
        } catch (ClassNotFoundException ignored) {
            BCLog.logger.warn(
                "[compat.forestry] IFilterContainer not found -- forestry must be updated to add the propolis pipe!");
            return false;
        }
    }
}
