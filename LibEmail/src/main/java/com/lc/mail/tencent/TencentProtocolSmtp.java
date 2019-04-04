package com.lc.mail.tencent;

import com.lc.mail.EmailProtocol;
import com.lc.mail.IProtocolSmtp;

public class TencentProtocolSmtp extends IProtocolSmtp {

    private static final String HOST = "smtp.qq.com";
    private static final int PORT = 25;
    private static final int PORT_SSL = 465;

     @Override
    protected EmailProtocol setupEmailProtocol() {
        EmailProtocol protocol = new EmailProtocol(HOST,PORT,PORT_SSL);
        return protocol;
    }
}
