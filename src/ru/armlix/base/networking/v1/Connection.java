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

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import ru.armlix.base.networking.v1.abs.NetHandler;
import ru.armlix.base.networking.v1.abs.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Setter
@Getter
public class Connection {

    public Socket socket;
    public NetHandler netHandler = new NetHandler();
    public final DataOutputStream out;
    public final DataInputStream in;
    public BlockingQueue<Packet> packetQueue; // Заменено на BlockingQueue
    public HashMap<String, Object> params = new HashMap<>();

    public Connection(Socket clientSocket) throws IOException {
        socket = clientSocket;
        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(socket.getInputStream());
        packetQueue = new ArrayBlockingQueue<>(100); // Ограниченная очередь
    }

    public void setParam(String key, Object value) {
        params.put(key, value);
    }

    public Object getParam(String key) {
        return params.getOrDefault(key, null);
    }

    public void queuePacket(Packet packet) throws InterruptedException {
        packetQueue.put(packet); // Блокирующее добавление пакета
    }
}
