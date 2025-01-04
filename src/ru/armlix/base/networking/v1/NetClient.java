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
import lombok.SneakyThrows;
import ru.armlix.base.networking.v1.abs.EventConnectionHandler;
import ru.armlix.base.networking.v1.abs.Packet;
import ru.armlix.base.networking.v1.abs.PacketRegistry;
import ru.armlix.base.networking.v1.impl.ConnectionHandlerImpl;

import java.io.*;
import java.net.Socket;

@Getter
public class NetClient {
    private String host;
    private int port;
    private Socket socket;
    private PacketRegistry packetRegistry;
    private DataInputStream in;
    private DataOutputStream out;
    private EventConnectionHandler connectionHandler = new ConnectionHandlerImpl();
    private Connection connection;

    private NetClient(String address, int port) {
        this.host = address;
        this.port = port;
    }

    public static NetClient build() {
        return new NetClient("localhost", 25400);
    }

    public void setAddress(String address) throws IOException {
        String[] parts = address.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid address format. Use 'host:port'.");
        }
        this.host = parts[0];
        this.port = Integer.parseInt(parts[1]);
    }

    public void setRegistry(PacketRegistry registry) {
        this.packetRegistry = registry;
    }

    public void setHandler(EventConnectionHandler handler) {
        this.connectionHandler = handler;
    }

    @SneakyThrows
    public void connect() {
        socket = new Socket(host, port);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        connection = new Connection(socket);

        connectionHandler.clientConnected(connection);

        new Thread(() -> readPackets(connection)).start();
        new Thread(() -> writePackets(connection)).start();
    }

    @SneakyThrows
    private void readPackets(Connection connection) {
        while(connection.socket.isConnected()) {
            if(connection.socket.getInputStream().available() > 0) {
                Packet packet = packetRegistry.readPacket(connection.in, connection);
                if(packet != null) {
                    connection.getNetHandler().transferHandle(connectionHandler.packetReceived(connection, packet));
                }
            }
        }
    }

    @SneakyThrows
    private void writePackets(Connection connection) {
        while(connection.socket.isConnected()) {
            Packet packet = connection.packetQueue.poll();
            if(packet != null) {
                packetRegistry.writePacket(connection.out, connectionHandler.packetSent(connection, packet));
            }
        }
    }


    @SneakyThrows
    public void disconnect() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
            connectionHandler.clientDisconnected(connection);
        }
    }

    public boolean isConnected() {
        return socket != null && !socket.isClosed();
    }
}
