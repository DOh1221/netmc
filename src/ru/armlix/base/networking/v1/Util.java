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
package ru.armlix.base.networking.v1;

import ru.armlix.base.networking.v1.impl.PacketRegistryImpl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Util {

    public static void writeVarInt(DataOutputStream out, int value) throws IOException {
        while ((value & -128) != 0) {
            out.writeByte((value & 127) | 128);
            value >>>= 7;
        }
        out.writeByte(value);
    }

    public static int readVarInt(DataInputStream in) throws IOException {
        int value = 0;
        int position = 0;
        int b;
        while (((b = in.readByte()) & 0xFF) != -1) {
            value |= (b & 0x7F) << position;
            position += 7;
            if ((b & 0x80) == 0) {
                return value;
            }
            if (position >= 35) {
                throw new IOException("VarInt is too big");
            }
        }
        throw new IOException("End of stream reached before VarInt was fully read");
    }

    public static void writeFShort(DataOutputStream out, float fshortValue) throws IOException {
        int intValue = (int) (fshortValue * 32);
        short shortValue = (short) (intValue & 0xFFFF);
        out.writeShort(shortValue);
    }

    public static float readFShort(DataInputStream in) throws IOException {
        short shortValue = in.readShort();
        int intValue = shortValue & 0xFFFF;
        return (float) intValue / 32;
    }

    public static void writeFByte(DataOutputStream out, float fbyteValue) throws IOException {
        byte byteValue = (byte) (fbyteValue * 32);
        out.writeByte(byteValue);
    }

    public static float readFByte(DataInputStream in) throws IOException {
        byte byteValue = in.readByte();
        return (float) byteValue / 32;
    }

    // CraftBukkit - throws IOException
    public static void writeString(String string, DataOutputStream dataoutputstream)  throws IOException {
        if (string.length() > 32767) {
            throw new IOException("String too big");
        } else {
            dataoutputstream.writeShort(string.length());
            dataoutputstream.writeChars(string);
        }
    }

    // CraftBukkit - throws IOException
    public static String readString(DataInputStream datainputstream, int expectedSize)  throws IOException {
        short receivedSize = datainputstream.readShort();

        if (receivedSize > expectedSize) {
            throw new IOException("Received string length longer than maximum allowed (" + receivedSize + " > " + expectedSize + ")");
        } else if (receivedSize < 0) {
            throw new IOException("Received string length is less than zero! Weird string!");
        } else {
            StringBuilder stringbuilder = new StringBuilder();

            for (int j = 0; j < receivedSize; ++j) {
                stringbuilder.append(datainputstream.readChar());
            }

            return stringbuilder.toString();
        }
    }
}
