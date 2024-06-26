package net.darktree;

import net.darktree.urp.R2UMessage;
import net.darktree.urp.URPClient;
import net.darktree.urp.u2rmessage.U2RBrod;
import net.darktree.urp.u2rmessage.U2RSend;


public class Typewriter {
    protected URPClient client;
    protected int hostUid = -1;
    protected OnTypedCallback onTyped;
    protected Thread listener;

    public Typewriter(URPClient client, OnTypedCallback onTyped) {
        this.client = client;
        this.onTyped = onTyped;
        initialize();
    }

    protected void initialize() {
        // wait for initialization message (hello) from host
        while (true) {
            R2UMessage message = client.getRxBuffer().receive(true);
            // t - typewriter is receiving this message, i - initialization
            if (message.getType() == R2UMessage.R2U.TEXT && new String(message.getData()).startsWith("ti")) {
                hostUid = message.getFromUid();
                String wholeText = new String(message.getData());
                wholeText = wholeText.substring(2, wholeText.length() - 1);
                onTyped.onTyped(0, wholeText, 0, false);
                System.out.println("Host UID: " + hostUid);
                break;
            }
        }
    }

    public void write(int offset, String text) {
        // h - host is receiving this message, w - write
        String command = "hw" + offset + " " + text;
        client.getTxBuffer().send(new U2RSend(hostUid, command), false);
    }

    public void remove(int offset, int length) {
        // h - host is receiving this message, r - remove
        String command = "hr" + offset + " " + length;
        client.getTxBuffer().send(new U2RSend(hostUid, command), false);
    }

    protected void update(R2UMessage message, OnTypedCallback callback) {
        int firstSpace = new String(message.getData()).indexOf(" ");
        int typedBy = Integer.parseInt(new String(message.getData()).substring(2, firstSpace));
        int secondSpace = new String(message.getData()).indexOf(" ", firstSpace + 1);
        int offset = Integer.parseInt(new String(message.getData()).substring(firstSpace + 1, secondSpace));
        String str = new String(message.getData());
        int zero = str.length();
        for (int i = str.length() - 1; i >= 0; i--) {
            if (str.charAt(i) == 0) {
                zero = i;
            } else {
                break;
            }
        }
        String text = str.substring(secondSpace + 1, zero);
        boolean moveCursor = typedBy != client.getUid();

        if (new String(message.getData()).startsWith("tw")) {
            callback.onTyped(offset, text, 0, moveCursor);
        }
        else if (new String(message.getData()).startsWith("tr")) {
            int length = Integer.parseInt(text);
            callback.onTyped(offset, "", -length, moveCursor);
        }
    }

    public void listen() {
        listener = new Thread(() -> {
            while (!listener.isInterrupted()) {
                R2UMessage message = client.getRxBuffer().receive(true);
                // t - typewriter is receiving this message, w - write
                if (message.getType() == R2UMessage.R2U.TEXT) {
                    update(message, onTyped);
                }
            }
        });
        listener.start();
    }

    public void close() {
        listener.interrupt();
    }
}
