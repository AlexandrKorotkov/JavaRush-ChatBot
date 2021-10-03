package task3008;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketAddress;

public class Connection implements Closeable {
    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;

    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        //сначала создаем OUT, а потом IN, иначе будет дэдлок
        out = new ObjectOutputStream(socket.getOutputStream());
        //OIS будет ждать объявления OUT`а и будет дэдлок
        in = new ObjectInputStream(socket.getInputStream());
    }

    public void send(Message message) throws IOException {
        synchronized (out) {
            out.writeObject(message);
        }
    }
    public Message receive() throws IOException, ClassNotFoundException {
        synchronized (in){
            Object innn = in.readObject();
            Message m=null;
            if(innn instanceof Message){
               m = (Message) innn;
            }
            return m;
        }
    }
    public SocketAddress getRemoteSocketAddress(){
        return socket.getRemoteSocketAddress();
    }

    @Override
    public void close() throws IOException {
        socket.close();
        out.close();
        in.close();
    }
}
