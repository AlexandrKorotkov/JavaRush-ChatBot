package client;

import task3008.ConsoleHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class BotClient extends Client {


    @Override
    protected String getUserName() {
        return "date_bot_" + (int) (Math.random() * 100);
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    //-----------------------------main--------------------------------
    public static void main(String[] args) {
        BotClient bc = new BotClient();
        bc.run();
    }

    //---------------------------BotSocketThread------------------------
    public class BotSocketThread extends SocketThread {
        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            BotClient.this.sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
            if (message.contains(":")) {
                //[0] - имя отправителя, [1]- текс сообщения
                String[] arr = message.split(": ");
                //переменные для упрощения свитча
                String mes = "";
                SimpleDateFormat formatter = null;
                Date date = null;
                switch (arr[1]) {
                    case "дата":
                        formatter = new SimpleDateFormat("d.MM.YYYY");
                        break;
                    case "день":
                        formatter = new SimpleDateFormat("d");
                        break;
                    case "месяц":
                        formatter = new SimpleDateFormat("MMMM");
                        break;
                    case "год":
                        formatter = new SimpleDateFormat("YYYY");
                        break;
                    case "время":
                        formatter = new SimpleDateFormat("H:mm:ss");
                        break;
                    case "час":
                        formatter = new SimpleDateFormat("H");
                        break;
                    case "минуты":
                        formatter = new SimpleDateFormat("m");
                        break;
                    case "секунды":
                        formatter = new SimpleDateFormat("s");
                        break;

                }
                if (formatter != null ) {
                    date = Calendar.getInstance().getTime();
                    mes = "Информация для " + arr[0] + ": " + formatter.format(date);
                    sendTextMessage(mes);
                }
            }
        }
    }
}
