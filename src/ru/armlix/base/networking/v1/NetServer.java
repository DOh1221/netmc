package ru.armlix.base.networking.v1;

import lombok.Getter;
import lombok.SneakyThrows;
import ru.armlix.base.networking.v1.abs.EventConnectionHandler;
import ru.armlix.base.networking.v1.abs.NetHandler;
import ru.armlix.base.networking.v1.abs.PacketRegistry;
import ru.armlix.base.networking.v1.impl.ConnectionHandlerImpl;
import ru.armlix.base.networking.v1.impl.PacketRegistryImpl;
import ru.armlix.base.networking.v1.abs.Packet;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
public class NetServer {
    private int port;
    private String host = InetAddress.getLocalHost().getHostAddress();
    private ServerSocket serverSocket;
    private boolean isPaused = false;
    private final Set<Connection> clients = ConcurrentHashMap.newKeySet();
    private PacketRegistry packetRegistry = new PacketRegistryImpl();
    private EventConnectionHandler connectionHandler = new ConnectionHandlerImpl();
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    private NetServer(int port) throws UnknownHostException {
        this.port = port;
    }

    public static NetServer build() throws UnknownHostException {
        return new NetServer(25565); // По умолчанию порт 25565
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
    public void start() {
        this.serverSocket = new ServerSocket(port, 50, InetAddress.getByName(host));
        new Thread(this::acceptConnections).start();
    }

    @SneakyThrows
    private void acceptConnections() {
        try {
            while (true) {
                if (!isPaused) {
                    Connection connection = new Connection(serverSocket.accept());
                    clients.add(connection);
                    connectionHandler.clientConnected(connection);
                    executorService.submit(() -> handleConnection(connection));
                }
            }
        } catch (IOException e) {
            // Обработка исключения, если необходимо
        }
    }

    @SneakyThrows
    private void handleConnection(Connection connection) {
        try {
            readPackets(connection);
            writePackets(connection);
        } finally {
            synchronized (clients) {
                clients.remove(connection);
            }
            connectionHandler.clientDisconnected(connection);
        }
    }

    @SneakyThrows
    private void readPackets(Connection connection) {
        try {
            while (true) {
                Packet packet = packetRegistry.readPacket(connection.in, connection);
                if (packet != null) {
                    connection.getNetHandler().transferHandle(connectionHandler.packetReceived(connection, packet));
                }
            }
        } catch (IOException e) {
            // Соединение закрыто или ошибка чтения
        }
    }

    @SneakyThrows
    private void writePackets(Connection connection) {
        try {
            while (true) {
                Packet packet = connection.packetQueue.take(); // Блокирующее ожидание
                packetRegistry.writePacket(connection.out, connectionHandler.packetSent(connection, packet));
            }
        } catch (IOException | InterruptedException e) {
            // Соединение закрыто или ошибка записи
        }
    }

    public void broadcast(Packet packet) {
        synchronized (clients) {
            for (Connection client : clients) {
                client.packetQueue.add(packet);
            }
        }
    }

    public void pause() {
        isPaused = true;
    }

    public void accept() {
        isPaused = false;
    }

    @SneakyThrows
    public void disable() {
        synchronized (clients) {
            for (Connection client : clients) {
                client.socket.close();
                connectionHandler.clientDisconnected(client);
            }
            clients.clear();
        }
    }

    public boolean isAlive() {
        return serverSocket != null && !serverSocket.isClosed();
    }

    @SneakyThrows
    public void stop() {
        disable();
        if (serverSocket != null) {
            serverSocket.close();
        }
    }

    @SneakyThrows
    public void restart() {
        stop();
        serverSocket = new ServerSocket(port, 50, InetAddress.getByName(host));
        start();
    }

    public void setNetHandler(Connection connection, NetHandler handler) {
        connection.setNetHandler(connectionHandler.handlerChanged(connection, handler));
    }
}
