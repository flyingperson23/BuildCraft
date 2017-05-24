/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc.data;

import java.io.IOException;

/** Indicates that we failed to load from NBT or some other file. */
public class InvalidInputDataException extends IOException {
    private static final long serialVersionUID = -3439641111545783074L;

    public InvalidInputDataException() {}

    public InvalidInputDataException(String message) {
        super(message);
    }

    public InvalidInputDataException(Throwable cause) {
        super(cause);
    }

    public InvalidInputDataException(String message, Throwable cause) {
        super(message, cause);
    }
}