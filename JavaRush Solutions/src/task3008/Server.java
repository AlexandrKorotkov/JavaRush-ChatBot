package task3008;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void sendBroadcastMessage(Message message) {
        for (Map.Entry<String, Connection> pair : connectionMap.entrySet()) {
            try {
                pair.getValue().send(message);
            } catch (IOException e) {
                System.out.println("Сообщение пользователю " + pair.getKey() + " не отправлено.");
            }
        }
    }

    //---------------------main--------------------------
    public static void main(String[] args) {
        ConsoleHelper.writeMessage("Введите порт сервера...");
        int port = ConsoleHelper.readInt();
        try (ServerSocket sock = new ServerSocket(port)) {
            System.out.println("Сервер запущен.");
            while (true) {
                new Handler(sock.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //----------------------------------Handler-------------------------------------------------------
    private static class Handler extends Thread {
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            connection.send(new Message(MessageType.NAME_REQUEST));
            Message rec = connection.receive();
            //проверка, что бы был тип мессаджа USER_NAME и имя в виде непустой строки или имя уже есть в списке, иначе повторный запрос.
            while (rec.getType() != MessageType.USER_NAME || rec.getData().equals("") || connectionMap.containsKey(rec.getData())) {
                connection.send(new Message(MessageType.NAME_REQUEST));
                rec = connection.receive();
            }
            connectionMap.put(rec.getData(), connection);
            connection.send(new Message(MessageType.NAME_ACCEPTED));
            return rec.getData();

        }

        private void notifyUsers(Connection connection, String userName) throws IOException {
            for (Map.Entry<String, Connection> pair : connectionMap.entrySet()) {
                String name = pair.getKey();
                if (!userName.equals(name)) {
                    connection.send(new Message(MessageType.USER_ADDED, name));

                }
            }
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            Message receivedMessage = connection.receive();
            while (true) {

                if (receivedMessage.getType() == MessageType.TEXT) {
                    sendBroadcastMessage(new Message(MessageType.TEXT, userName + ": " + receivedMessage.getData()));
                } else {
                    ConsoleHelper.writeMessage("Полученное сообщение не TEXT");
                }
                receivedMessage = connection.receive();
            }
        }

        public void run() {
            Connection connection = null;
            ConsoleHelper.writeMessage("Установлено соединение с удаленным адресом " + socket.getRemoteSocketAddress());

            try {
                connection = new Connection(socket);
            } catch (Exception e) {
                ConsoleHelper.writeMessage("Ошибка при создании соединения.");
            }

            String userName = "";
            try {
                userName = serverHandshake(connection);
            } catch (Exception e) {
                ConsoleHelper.writeMessage("IOException в рукопожатии.");
            }
            sendBroadcastMessage(new Message(MessageType.USER_ADDED, userName));
            try {
                notifyUsers(connection, userName);
            } catch (Exception e) {
                ConsoleHelper.writeMessage("Ошибка оповещения участников чата.");
            }
            try {
                serverMainLoop(connection, userName);
            } catch (Exception e) {
                ConsoleHelper.writeMessage("Ошибка ввода в главном цикле сервера.");
            }
            connectionMap.remove(userName);
            sendBroadcastMessage(new Message(MessageType.USER_REMOVED, userName));
            ConsoleHelper.writeMessage("Соединение с удаленным адресом закрыто.");
        }

    }
}
