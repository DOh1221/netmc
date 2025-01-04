package ru.armlix.base.networking.v1.test;

import ru.armlix.base.networking.v1.abs.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class Packet3 extends Packet {
    @Override
    public void readData(DataInputStream in) {

    }

    @Override
    public void writeData(DataOutputStream out) {

    }

    @Override
    public int getPacketId() {
        return 0x03;
    }
}