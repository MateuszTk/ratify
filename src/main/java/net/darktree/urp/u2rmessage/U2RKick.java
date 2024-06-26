package net.darktree.urp.u2rmessage;

import net.darktree.urp.NetUtils;

import java.io.DataOutputStream;

public class U2RKick implements U2RMessage {
    private final int uid;

    public U2RKick(int uid) {
        this.uid = uid;
    }

    @Override
    public void send(DataOutputStream dataOut){
        try {
            dataOut.writeByte(0x07);
            NetUtils.writeIntLE(dataOut, uid);
        } catch (Exception e) {
            System.out.println("Failed to send kick request");
        }
    }

    @Override
    public String toString() {
        return "U2RKick{ uid=" + uid + '}';
    }
}
