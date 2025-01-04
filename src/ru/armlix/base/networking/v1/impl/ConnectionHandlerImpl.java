package ru.armlix.base.networking.v1.impl;

import ru.armlix.base.networking.v1.Connection;
import ru.armlix.base.networking.v1.abs.EventConnectionHandler;
import ru.armlix.base.networking.v1.abs.NetHandler;
import ru.armlix.base.networking.v1.abs.Packet;

public class ConnectionHandlerImpl extends EventConnectionHandler {
    @Override
    public void clientConnected(Connection conn) {
        System.out.println("Client connected");
    }

    @Override
    public void clientDisconnected(Connection conn) {

    }

    @Override
    public Packet packetReceived(Connection conn, Packet packet) {
        return packet;
    }

    @Override
    public Packet packetSent(Connection conn, Packet packet) {
        return packet;
    }

    @Override
    public NetHandler handlerChanged(Connection conn, NetHandler handler) {
        return handler;
    }
}
