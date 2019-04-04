package com.lc.mail;

import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

public abstract class IProtocolSmtp {

    private String TAG = getClass().getSimpleName();

    private final String SUBTYPE_MIXED = "mixed";
    private final String SUBTYPE_RELATED = "related";
    private final String SUBTYPE_ALTERNATIVE = "alternative";

    private Properties mProperties;

    private EmailProtocol mProtocol;

    private String mMailHost;

    private int mMailPort;

    private int mMailSSL;

    private Session mSession;

    private EmailMessage mEmailMessage;

    private Address mSenderAddress;


    private MimeMessage mMimeMessage;

    public IProtocolSmtp(){
        mProtocol = setupEmailProtocol();
    }


    protected abstract EmailProtocol setupEmailProtocol();


    public void sendEmail(EmailMessage message){
        if(message == null){
            return;
        }
        mEmailMessage = message;

        mMailHost = mProtocol.getHost();

        mMailPort = mProtocol.getPort();

        mMailSSL = mProtocol.getSsl();



        if(mMailPort == 0){
            mMailPort = 25;
        }

        if(mMailSSL == 0){
            mMailSSL = 465;
        }

        mSenderAddress = getSenderAddress();

        if(mSenderAddress == null){
            Log.e(TAG,"sender info is null");
            return;
        }

        createProperties();

        createSession();

        createMimeMessage();

        transportMessage();


    }

    private Address getSenderAddress(){
        Address address = null;
        String fromName = mEmailMessage.getFromName();
        String fromEmail = mEmailMessage.getFromEmail();

        try{
            if(!TextUtils.isEmpty(fromEmail)){
                address = new InternetAddress(fromEmail);
                if(!TextUtils.isEmpty(fromName)){
                    address = new InternetAddress(fromEmail, fromName, "utf-8");
                }
            }
        }catch (AddressException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return address;
    }

    private void createProperties(){
        mProperties = new Properties();
        //protocol smtp pop3
        mProperties.put("mail.transport.protocol","smtp");
        mProperties.put("mail.smtp.host",mMailHost);
        mProperties.put("mail.smtp.port",mMailPort);
        mProperties.put("mail.smtp.user",mSenderAddress);
        mProperties.put("mail.smtp.starttls.enable",true);
        mProperties.put("mail.debug",true);
        mProperties.put("mail.from",mSenderAddress);
        mProperties.put("mail.smtp.auth",true);


    }


    private void createSession(){
        mSession = Session.getInstance(mProperties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(mEmailMessage.getAccount(),mEmailMessage.getAuthCode());
            }
        });
    }


    private void createMimeMessage(){
        File[] files = null;
        if(mEmailMessage.getImageFiles() != null){
            files =  mEmailMessage.getImageFiles().toArray(new
                    File[mEmailMessage.getImageFiles().size()]);
        }
        Address[] toAddress = null;
        if(mEmailMessage.getToAddress() != null){
            toAddress = new Address[mEmailMessage.getToAddress().size()];
            for (int i = 0;i< mEmailMessage.getToAddress().size();i++){
                String addr = mEmailMessage.getToAddress().get(i);
                try {
                    toAddress[i] = new InternetAddress(addr);
                } catch (AddressException e) {
                    e.printStackTrace();
                }
            }
            //toAddress = mEmailMessage.getToAddress().toArray(new Address[mEmailMessage.getToAddress().size()]);
        }
        mMimeMessage = new MimeMessage(mSession);
        try {
            mMimeMessage.setFrom(mSenderAddress);
            mMimeMessage.setRecipients(Message.RecipientType.TO,toAddress);
            mMimeMessage.setSubject(mEmailMessage.getTitle(),"UTF-8");
            mMimeMessage.setSentDate(new Date());

            if(mEmailMessage.getMessage() != null){
                MimeMultipart multipart = new MimeMultipart(SUBTYPE_MIXED);
                BodyPart fileBodyPart = createFileBodyPart(files);
                if(fileBodyPart != null){
                    multipart.addBodyPart(fileBodyPart);
                }
                BodyPart htmlPart = new MimeBodyPart();
                htmlPart.setContent(mEmailMessage.getMessage(),"text/html; charset=utf-8");
                multipart.addBodyPart(htmlPart);
                mMimeMessage.setContent(multipart);
            }

            if(mEmailMessage.isReadReceipt()){
                mMimeMessage.setHeader("Disposition-Notification-To","1");
            }
            mMimeMessage.saveChanges();
        } catch (MessagingException e) {
            e.printStackTrace();
        }

    }

    private void transportMessage(){
        try {
            Transport transport = mSession.getTransport();
            transport.connect();
            transport.sendMessage(mMimeMessage,mMimeMessage.getAllRecipients());
            transport.close();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private MimeBodyPart createFileBodyPart(File[] files){
        if(files == null || files.length <=0){
            return null;
        }
        try {
            MimeBodyPart part = new MimeBodyPart();
            MimeMultipart fileMultiPart = new MimeMultipart(SUBTYPE_RELATED);
            for (File file : files){
                MimeBodyPart tmp = new MimeBodyPart();
                FileDataSource fds = new FileDataSource(file);
                tmp.setDataHandler(new DataHandler(fds));
                tmp.setFileName(MimeUtility.encodeText(file.getName()));
                fileMultiPart.addBodyPart(tmp);
            }
            part.setContent(fileMultiPart);
            return part;
            } catch (MessagingException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }



}
