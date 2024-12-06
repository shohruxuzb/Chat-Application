package by.shohruh.scenefx;
import java.io.*;
import java.net.*;
import java.util.*;

public class SocketServer {
    // Store client handlers
    private static final Set<ClientHandler> clients = Collections.synchronizedSet(new HashSet<>());

    public static void main(String[] args) throws IOException {
        try (var serverSocket = new ServerSocket(8082)) {
            System.out.println("Server is running on port 8082...");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected: " + socket.getInetAddress().getHostAddress());
                ClientHandler clientHandler = new ClientHandler(socket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        }
    }

    static class ClientHandler implements Runnable {
        private final Socket socket;
        private DataInputStream inputStream;
        private DataOutputStream outputStream;
        private String clientName;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                inputStream = new DataInputStream(socket.getInputStream());
                outputStream = new DataOutputStream(socket.getOutputStream());

                // Ask for a client name
                outputStream.writeUTF("Enter your name:");
                clientName = inputStream.readUTF();
                broadcast(clientName + " has joined the chat.", null);

                String message;
                while (true) {
                    message = inputStream.readUTF();

                    if (message.equalsIgnoreCase("exit")) {
                        broadcast(clientName + " has left the chat.", this);
                        break;
                    }

                    broadcast(clientName + ": " + message, this);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    clients.remove(this);
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void broadcast(String message, ClientHandler excludeClient) {
            synchronized (clients) {
                for (ClientHandler client : clients) {
                    if (client != excludeClient) {
                        try {
                            client.outputStream.writeUTF(message);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
