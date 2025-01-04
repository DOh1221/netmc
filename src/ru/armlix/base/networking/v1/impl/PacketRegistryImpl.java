package ru.armlix.base.networking.v1.impl;

import lombok.SneakyThrows;
import ru.armlix.base.networking.v1.Connection;
import ru.armlix.base.networking.v1.abs.PacketRegistry;
import ru.armlix.base.networking.v1.abs.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PacketRegistryImpl implements PacketRegistry {

    private final Map<Integer, Class<? extends Packet>> packetIdToClassMap = new HashMap<>();
    private final Map<Class<? extends Packet>, Integer> packetClassToIdMap = new HashMap<>();

    @Override
    public void registerPacket(Class<? extends Packet> packetClass, int id) {
        if (packetIdToClassMap.putIfAbsent(id, packetClass) != null) {
            throw new IllegalArgumentException("Duplicate packet id: " + id);
        }
        if (packetClassToIdMap.putIfAbsent(packetClass, id) != null) {
            throw new IllegalArgumentException("Duplicate packet class: " + packetClass);
        }
    }

    @Override
    public void unregisterPacket(Packet packet) {
        unregisterPacketByClass(packet.getClass());
    }

    private void unregisterPacketByClass(Class<? extends Packet> packetClass) {
        Integer id = packetClassToIdMap.remove(packetClass);
        if (id != null) {
            packetIdToClassMap.remove(id);
        }
    }

    @Override
    public void unregisterPacket(int id) {
        Class<? extends Packet> packetClass = packetIdToClassMap.remove(id);
        if (packetClass != null) {
            packetClassToIdMap.remove(packetClass);
        }
    }

    @Override
    public Packet getPacket(int id) {
        Class<? extends Packet> packetClass = packetIdToClassMap.get(id);
        return createPacketInstance(packetClass);
    }

    private Packet createPacketInstance(Class<? extends Packet> packetClass) {
        if (packetClass == null) {
            return null;
        }
        try {
            return packetClass.newInstance();
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void writePacket(DataOutputStream out, Packet packet) throws IOException {
        out.write(packet.getPacketId());
        packet.writeData(out);
    }

    @SneakyThrows
    @Override
    public Packet readPacket(DataInputStream in, Connection conn) {
        Packet packet = getPacket(in.read());;
        if(packet != null) {
            packet.readData(in);
            conn.getNetHandler().transferHandle(packet);
        }
        return packet;
    }
}
