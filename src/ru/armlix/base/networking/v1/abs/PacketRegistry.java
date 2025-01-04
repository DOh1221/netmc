/*
 * This file is part of NetMinecraft - https://github.com/RaphiMC/NetMinecraft
 * Copyright (C) 2022-2024 RK_01/RaphiMC and contributors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ru.armlix.base.networking.v1.abs;

import ru.armlix.base.networking.v1.Connection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface PacketRegistry {
    void registerPacket(Class<? extends Packet> packetClass, int id);

    void unregisterPacket(Packet packet);
    void unregisterPacket(int id);
    Packet getPacket(int id);
    void writePacket(DataOutputStream out, Packet packet) throws IOException;
    Packet readPacket(DataInputStream in, Connection conn) throws IOException;
}
