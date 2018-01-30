package cn.pany.walle.common.protocol;

/**
 * Created by pany on 17/11/5.
 */
public enum  MessageType {
    SERVICE_REQ((byte) 0), SERVICE_RESP((byte) 1), ONE_WAY((byte) 2), LOGIN_REQ(
            (byte) 3), LOGIN_RESP((byte) 4), HEARTBEAT_REQ((byte) 5), HEARTBEAT_RESP(
            (byte) 6), CLIENT_REQ((byte) 7), CLIENT_RESP((byte) 8);

    private byte value;

    private MessageType(byte value) {
        this.value = value;
    }

    public byte value() {
        return this.value;
    }
    public static MessageType getByValue(byte value){
        for(MessageType messageType:MessageType.values()){
            if(messageType.value==value){
                return messageType;
            }
        }
        return null;
    }
}