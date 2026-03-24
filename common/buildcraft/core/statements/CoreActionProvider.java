/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.statements;

import java.util.Collection;

import javax.annotation.Nonnull;

import buildcraft.core.compat.module.ic2.IC2VersionHelper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import buildcraft.api.statements.IActionExternal;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IActionInternalSided;
import buildcraft.api.statements.IActionProvider;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.containers.IRedstoneStatementContainer;
import buildcraft.api.tiles.IControllable;
import buildcraft.api.tiles.TilesAPI;

import buildcraft.core.BCCoreStatements;
import net.minecraftforge.fml.common.Loader;

public enum CoreActionProvider implements IActionProvider {
    INSTANCE;

    @Override
    public void addInternalActions(Collection<IActionInternal> res, IStatementContainer container) {
        if (container instanceof IRedstoneStatementContainer) {
            res.add(BCCoreStatements.ACTION_REDSTONE);
        }
    }

    @Override
    public void addInternalSidedActions(Collection<IActionInternalSided> actions, IStatementContainer container, @Nonnull EnumFacing side) { }

    @Override
    public void addExternalActions(Collection<IActionExternal> res, @Nonnull EnumFacing side, TileEntity tile) {
        if (Loader.isModLoaded("ic2")) {
            IC2VersionHelper.addExternalActions(res, side, tile);
        }
        IControllable controllable = tile.getCapability(TilesAPI.CAP_CONTROLLABLE, side.getOpposite());
        if (controllable != null) {
            for (ActionMachineControl action : BCCoreStatements.ACTION_MACHINE_CONTROL) {
                if (controllable.acceptsControlMode(action.mode)) {
                    res.add(action);
                }
            }
        }
    }
}
