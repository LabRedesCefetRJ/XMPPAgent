package group.chon.agent.xmpp.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smack.chat2.*;
import org.jivesoftware.smack.packet.Message;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;

public class XMPPMiddleware {
    private AbstractXMPPConnection connection;
    private ChatManager chatManager;

    private String login;
    private String password;
    private String xmppDomain;
    private String xmppHost;
    private int xmppPort;

    private long lastChecked = 0;
    private Logger logger;

    private final List<Message> receivedMessages = Collections.synchronizedList(new ArrayList<>());

    private final Util util = new Util();

    public void connect() throws Exception {
        DomainBareJid serviceName = JidCreate.domainBareFrom(xmppDomain);

        XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
        .setUsernameAndPassword(this.login, this.password)
        .setXmppDomain(serviceName)
        .setHost(this.xmppHost)
        .setPort(this.xmppPort)
        .setSecurityMode(ConnectionConfiguration.SecurityMode.ifpossible)
        .build();

        this.connection = new XMPPTCPConnection(config);
        this.connection.connect().login();

        this.chatManager = ChatManager.getInstanceFor(this.connection);
        this.logger.info("Connected as " + this.login + "@" + this.xmppDomain);

        this.chatManager.addIncomingListener((from, message, chat) -> {
            this.logger.info("Message received from " + from + ": " + message.getBody());
            receivedMessages.add(message);
        });

    }

    public ArrayList<jason.asSemantics.Message> checkMessages() {
        
        if ((System.currentTimeMillis() - (this.lastChecked + util.getRandom())) > 60000) {
            try {
                if (this.connection == null ) {
                    return null;
                }else if(!this.connection.isConnected()){
                    this.connect();
                }
            } catch (Exception e) {
                this.logger.severe("[ERROR] " + e.getMessage());
                return null;
            }
            ArrayList<jason.asSemantics.Message> jMsg = new ArrayList<jason.asSemantics.Message>();
            synchronized (receivedMessages) {
                for (Message message : receivedMessages) {
                    try {
                        String sender = message.getFrom() != null ? message.getFrom().asBareJid().toString() : "unknown";
                        String body = message.getBody();
                        if (!body.isEmpty()) {
                            String ilf = "tell";
                            String content = "";
                            
                            String[] parts = body.split(" ", 2);
                            if (body.startsWith("#")) {
                                ilf = parts[0].replace("#", "");
                            }
                            content = parts.length > 1 ? parts[1] : "";
                            //this.logger.info("Processing message from " + sender + ":" + content.toString());

                            if (!util.isIllocutionaryForce(ilf)) {
                                    this.logger.severe("The message doesn't have a valid Illocutionary KQML force!");
                            } else {
                                if(ilf.equals("tellHow")){
                                    if ((!content.startsWith("\"")) && (!content.endsWith("\""))){
                                        content = '\"' + content + '\"';
                                    }
                                }
                                try{
                                    jason.asSemantics.Message jasonMsgs = new jason.asSemantics.Message(
                                            util.getKqmlILF(),
                                            sender,
                                            null,
                                            content);
                                    jMsg.add(jasonMsgs);
                                }catch (Exception exception){
                                    this.logger.severe("Something is wrong with the message!");
                                }
                            }
                        }

                    } catch (Exception e) {
                        this.logger.severe("Error processing received message: " + e.getMessage());
                        return null;
                    }  
                }
                receivedMessages.clear();
                return jMsg;
            }
        }
        return null;
    }
    
    public void sendMsg(String to, String ifl, String message) throws Exception {
        EntityBareJid jid = JidCreate.entityBareFrom(to);
        try {
            if (this.connection == null || !this.connection.isConnected()) {
                this.connect();
            }
            Chat chat = this.chatManager.chatWith(jid);
            chat.send('#'+ifl+' '+message);

            this.logger.info("Message to "+to+" sent successfully!");
        } catch (Exception e) {
            this.logger.severe("Error sending message: " + e.getMessage());
        }
    }

    public void disconnect() {
        if (this.connection != null && this.connection.isConnected()) {
            this.connection.disconnect();
            this.logger.info("Disconnected.");
        }
    }

    public void setProps(String domain,String host, int port) {
        this.xmppDomain = domain;
        this.xmppHost = host;
        this.xmppPort = port;
    }

    public String getXmppDomain() {
        return this.xmppDomain;
    }

    public String getXmppHost() {
        return this.xmppHost;
    }

    public int getXmppPort() {
        return this.xmppPort;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLogin() {
        return this.login;
    }

    public String getPassword() {
        return this.password;
    }

    public void setLogger(Logger l){
        this.logger = l;
    }


}
