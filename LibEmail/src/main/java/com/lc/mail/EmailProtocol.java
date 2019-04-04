package com.lc.mail;

public class EmailProtocol {

    private String host;

    private int port;

    private int ssl;

    public  EmailProtocol(String host,int port,int ssl){
        this.host = host;
        this.port = port;
        this.ssl = ssl;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getSsl() {
        return ssl;
    }

    public void setSsl(int ssl) {
        this.ssl = ssl;
    }
}
