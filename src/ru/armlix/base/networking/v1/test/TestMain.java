package ru.armlix.base.networking.v1.test;

import lombok.SneakyThrows;
import ru.armlix.base.networking.v1.NetClient;
import ru.armlix.base.networking.v1.NetServer;
import ru.armlix.base.networking.v1.impl.ConnectionHandlerImpl;
import ru.armlix.base.networking.v1.impl.PacketRegistryImpl;

public class TestMain {

    @SneakyThrows
    public static void main(String[] args) {

        PacketRegistryImpl packetRegistry = new PacketRegistryImpl();

        packetRegistry.registerPacket(Packet1.class, 0x01);
        packetRegistry.registerPacket(Packet2.class, 0x02);

        NetServer server = NetServer.build();

        server.setAddress("localhost:25565");
        server.setRegistry(packetRegistry);
        server.setHandler(new ConnectionHandlerImpl());
        server.start();

        NetClient client = NetClient.build();

        client.setAddress("localhost:25565");
        client.setRegistry(packetRegistry);
        client.setHandler(new ConnectionHandlerImpl());
        client.connect();

        client.getConnection().queuePacket(new Packet1());

        server.broadcast(new Packet2());

    }

}
