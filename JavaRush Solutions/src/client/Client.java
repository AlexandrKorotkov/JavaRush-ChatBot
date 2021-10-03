package client;

import task3008.*;

import java.io.IOException;
import java.net.Socket;


public class Client {
    protected Connection connection;
    private volatile boolean clientConnected = false;

    protected String getServerAddress() {
        ConsoleHelper.writeMessage("Введите адрес сервера:");
        return ConsoleHelper.readString();
    }

    protected int getServerPort() {
        ConsoleHelper.writeMessage("Введите порт сервера:");
        return ConsoleHelper.readInt();
    }

    protected String getUserName() {
        ConsoleHelper.writeMessage("Введите имя пользователя:");
        return ConsoleHelper.readString();
    }

    protected boolean shouldSendTextFromConsole() {
        return true;
    }

    protected SocketThread getSocketThread() {
        return new SocketThread();
    }

    protected void sendTextMessage(String text) {
        try {
            connection.send(new Message(MessageType.TEXT, text));
        } catch (IOException e) {
            e.printStackTrace();
            clientConnected = false;
        }
    }

    public void run() {
        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();
        //wait метод тут вызываться будет у this объекта....шта, почему? по заданию Thread.currentThread.wait() - не прокатит.
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {
                ConsoleHelper.writeMessage("Ошибка клиентской части.");
                return;
            }
        }
        if (clientConnected) {
            ConsoleHelper.writeMessage("Соединение установлено.\n" +
                    "Для выхода наберите команду 'exit'.");
            String input = ConsoleHelper.readString();
            while (!input.equals("exit")) {
                if (shouldSendTextFromConsole()) {
                    sendTextMessage(input);
                }
                input = ConsoleHelper.readString();
            }
        } else {
            ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
        }

    }

    //---------------------Client main-------------------------------------------------
    public static void main(String[] args) {
        Client client = new Client();
        client.run();

    }

    //------------------------SocketThread------------------------------------------
    public class SocketThread extends Thread {
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
        }

        protected void informAboutAddingNewUser(String userName) {
            ConsoleHelper.writeMessage("Участник " + userName + " присоединился к чату");
        }

        protected void informAboutDeletingNewUser(String userName) {
            ConsoleHelper.writeMessage(userName + " покинул чат.");
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected) {
            Client.this.clientConnected = clientConnected;
            synchronized (Client.this) {
                Client.this.notify();
            }
        }

        protected void clientHandshake() throws IOException, ClassNotFoundException {
            while (true) {
                Message in = Client.this.connection.receive();
                if (in.getType() == MessageType.NAME_REQUEST) {
                    Client.this.connection.send(new Message(MessageType.USER_NAME, getUserName()));

                } else if (in.getType() == MessageType.NAME_ACCEPTED) {
                    notifyConnectionStatusChanged(true);
                    return;
                } else
                    throw new IOException("Unexpected MessageType");
            }
        }
        public void run(){
            try {
                connection=new Connection(new Socket(getServerAddress(),getServerPort()));
                clientHandshake();
                clientMainLoop();
            } catch (IOException | ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
            }


        }


        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            while (true) {
                Message in = Client.this.connection.receive();
                if (in != null) {
                    if (in.getType() == MessageType.TEXT) {
                        processIncomingMessage(in.getData());

                    } else if (in.getType() == MessageType.USER_ADDED) {
                        informAboutAddingNewUser(in.getData());
                    } else if (in.getType() == MessageType.USER_REMOVED) {
                        informAboutDeletingNewUser(in.getData());
                    } else {
                        throw new IOException("Unexpected MessageType");
                    }
                }
            }
        }
    }
}


