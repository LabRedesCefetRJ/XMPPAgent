package group.chon.agent.xmpp.core;

import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Logger;
import javax.mail.*;
import javax.mail.internet.*;

public class EMailMiddleware{
    private String Shost,Rhost;
    private String Sport,Rport;
    private String Sprotocol,Rprotocol;
    private String login;
    private String password;
    private boolean Sauth, Sstarttls, Ssslenable,Rauth, Rstarttls, Rsslenable;
    private String Sssltrust,Ssslprotocol,Rssltrust,Rsslprotocol;
    private boolean RHostEnable = false;
    private boolean RPropsEnable = false;
    private boolean SPropsEnable = false;
    private boolean SHostEnable = false;
    private long lastChecked = 0;
    private Logger logger;

    private final Util util = new Util();

    private int attemptConnection = 0;

    public Properties sslProps () {
        //Checks which properties are required for the connection / else uses the defaut
        Properties properties = new Properties();
        if(Sauth){
            properties.put("mail"+Sprotocol+"auth", true);
        }
        if (Sstarttls) {
            properties.put("mail."+Sprotocol+".starttls.enable", true);
        }
        if (Ssslenable) {
            properties.put("mail."+Sprotocol+".ssl.enable", true);
        }
        if (Sssltrust != null) {
            properties.put("mail."+Sprotocol+".ssl.trust",Sssltrust);
        }
        if (Ssslprotocol != null) {
            properties.put("mail."+Sprotocol+".ssl.protocols",Ssslprotocol);
        }
        if(Rauth){
            properties.put("mail"+Rprotocol+"auth", true);
        }
        if (Rstarttls) {
            properties.put("mail."+Rprotocol+".starttls.enable", true);
        }
        if (Rsslenable) {
            properties.put("mail."+Rprotocol+".ssl.enable", true);
        }
        if (Rssltrust != null) {
            properties.put("mail."+Rprotocol+".ssl.trust",Rssltrust);
        }
        if (Rsslprotocol != null) {
            properties.put("mail."+Rprotocol+".ssl.protocols",Rsslprotocol);
        }

        return properties;
    }

    public ArrayList<jason.asSemantics.Message> checkEMail() {
        if(!util.isValidEmail(this.login)){
            this.logger.severe(Info.credentialsINVALID(this.getClass().getName()));
            setLogin(null);
            setPassword(null);
        }else if ((System.currentTimeMillis() - (this.lastChecked + util.getRandom())) > 60000) {
            ArrayList<jason.asSemantics.Message> jMsg = new ArrayList<jason.asSemantics.Message>();
            this.logger.info("Cheking mailbox: "+this.login);
            this.lastChecked = System.currentTimeMillis();
            Session session = null;
            Properties props = sslProps();
            try {
                props.put("mail.store.protocol", Rprotocol);
                props.put("mail." + Rprotocol + ".host", Rhost);
                props.put("mail." + Rprotocol + ".port", Rport);
                props.put("mail." + Rprotocol + ".leaveonserver", false);
                session = Session.getInstance(props);
            } catch (Exception e) {
                this.logger.severe("Configuration error:" + e);
                return null;
            }

            try {
                Store store = session.getStore(this.Rprotocol);
                store.connect(this.login, this.password);

                // Getting the list of folders
                Folder[] allFolders = store.getDefaultFolder().list("*");
                // Looking for Message in each folder
                for(int i=0; i<allFolders.length; i++){
                    allFolders[i].open(Folder.READ_WRITE);
                    javax.mail.Message[] messages = allFolders[i].getMessages();
                    for (Message message : messages) {
                        if (message.getFlags().contains(Flags.Flag.DELETED)  || message.getFlags().contains(Flags.Flag.SEEN)) {
                            continue;
                        }else{
                            this.logger.info("New e-mail in "+allFolders[i].getName());
                                message.setFlag(Flags.Flag.SEEN, true);
                            if (!util.isValidEmail(message.getFrom())){
                                this.logger.severe("The sender hasn't a valid Agent name!");
                            } else if (!util.isIllocutionaryForce(message.getSubject())) {
                                this.logger.severe("The subject is not a valid Illocutionary KQML force!");
                            } else if (!util.isValidTerm(message.getContent())){
                                this.logger.severe("The content is not a valid KQML message!");
                            } else {
                                try{
                                    jason.asSemantics.Message jasonMsgs = new jason.asSemantics.Message(
                                            util.getKqmlILF(),
                                            util.getSender(),
                                            null,
                                            util.getKqmlMessage());
                                    jMsg.add(jasonMsgs);
                                    message.setFlag(Flags.Flag.DELETED, true);
                                }catch (Exception exception){
                                    this.logger.severe("Something is wrong with the message!");
                                }
                            }
                        }
                    }
                    if (Rprotocol.contains("imap")) {
                        allFolders[i].expunge();
                    }
                    allFolders[i].close();
                }
                store.close();
                this.attemptConnection = 0;
                return jMsg;
            } catch (Exception e) {
                if(e.getMessage().equals("authentication failed")){
                    setLogin(null);
                    setPassword(null);
                    this.logger.severe("[ERROR] " + e.getMessage());
                    this.logger.info(Info.credentialsINVALID(this.getClass().getName()));
                } else if (e.getMessage().contains("Couldn't connect to host") && (this.attemptConnection < 9)) {
                    this.attemptConnection++;
                    int delay = util.getRandom() * this.attemptConnection;
                    this.logger.severe(e.getMessage()+"; attempt "+this.attemptConnection+"; trying again in "+(delay+60000)/1000+" seconds.");
                    this.lastChecked = System.currentTimeMillis()+delay;
                }
                else{
                    setReceiverProps(null,null,null);
                    this.logger.severe("[ERROR] " + e.getMessage());
                    this.logger.info(Info.eMailProviderConfigurationNOTFOUND(this.getClass().getName()));
                }
                return null;
            }
        }
        return null;
    }


//    public static String addressToString(Address[] rawAddress) {
//        if (rawAddress != null) {
//            return rawAddress[0].toString();
//        } else return "null";
//    }

    public void sendMsg(String recipientEmail, String subject, String message) {
        Session session = null;
        Properties props = sslProps();

        try {
            props.put("mail.store.protocol", Sprotocol);
            props.put("mail." + Sprotocol + ".host", Shost);
            props.put("mail." + Sprotocol + ".port", Sport);
            props.put("mail." + Sprotocol + ".leaveonserver", false);
            props.put("mail.smtp.socketFactory.port", Sport);
            props.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");

            session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(login, password);
                }
            });
        }catch (Exception e){
            this.logger.severe("Connection error:" + e);
            return;
        }

        try {
            Message msg = new MimeMessage(session);

            msg.setFrom(new InternetAddress(login));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            msg.setSubject(subject);
            msg.setText(message);

            Transport.send(msg,login,password);

            this.logger.info("Email to "+recipientEmail+" sent successfully!");
        }catch (MessagingException e) {
            this.logger.severe("Error sending email: " + e.getMessage());
            if(e.getMessage().equals("535 Authentication credentials invalid")){
                setLogin(null);
                setPassword(null);
                this.logger.info(Info.credentialsINVALID(this.getClass().getName()));
            }else{
                setSendProps(null,null,null);
                this.logger.info(Info.eMailProviderConfigurationNOTFOUND(this.getClass().getName()));
            }
        }
    }

    public void setSendProps(String shost,String sprotocol, String sport) {
        this.Sprotocol = sprotocol;
        this.Sport = sport;
        this.Shost = shost;
        this.SHostEnable = true;
    }

    public void setReceiverProps(String rhost, String rprotocol, String rport) {
        this.Rprotocol = rprotocol;
        this.Rport = rport;
        this.Rhost = rhost;
        this.RHostEnable = true;
    }

    public void setSendAuth(boolean sauth,boolean sstarttls, boolean ssslenable, String sssltrust, String ssslprotocol) {
        if(sssltrust.equals("null")){
            sssltrust = null;
        }
        if(ssslprotocol.equals("null")){
            ssslprotocol = null;
        }
        this.Sauth = sauth;
        this.Sstarttls = sstarttls;
        this.Ssslenable = ssslenable;
        this.Sssltrust = sssltrust;
        this.Ssslprotocol = ssslprotocol;
        this.SPropsEnable = true;
    }

    public void setRAuth(boolean rauth,boolean rstarttls, boolean rsslenable, String rssltrust, String rsslprotocol) {
        if(rssltrust.equals("null")){
            rssltrust = null;
        }
        if(rsslprotocol.equals("null")){
            rsslprotocol = null;
        }
        this.Rauth = rauth;
        this.Rstarttls = rstarttls;
        this.Rsslenable = rsslenable;
        this.Rssltrust = rssltrust;
        this.Rsslprotocol = rsslprotocol;
        this.RPropsEnable = true;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public boolean isRHostEnable() {
        return RHostEnable;
    }

    public boolean isRPropsEnable() {
        return RPropsEnable;
    }

    public boolean isSPropsEnable() {
        return SPropsEnable;
    }

    public boolean isSHostEnable() {
        return SHostEnable;
    }

    public void setLogger(Logger l){
        this.logger = l;
    }
}

