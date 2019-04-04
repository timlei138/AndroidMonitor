package com.lc.mail.netease;

import com.lc.mail.EmailProtocol;
import com.lc.mail.IProtocolSmtp;

import java.util.Properties;

public class NetEaseProtocolSmtp extends IProtocolSmtp {

    private static final String HOST = "smtp.163.com";
    private static final int PORT = 25;
    private static final int PORT_SSL = 465 ;

    @Override
    protected EmailProtocol setupEmailProtocol() {
        EmailProtocol protocol = new EmailProtocol(HOST,PORT,PORT_SSL);
        return protocol;
    }
}
