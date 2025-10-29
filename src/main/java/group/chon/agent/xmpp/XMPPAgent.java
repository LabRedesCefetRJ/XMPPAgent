package group.chon.agent.xmpp;

import group.chon.agent.xmpp.core.Info;
import group.chon.agent.xmpp.core.Util;
import group.chon.agent.xmpp.core.XMPPMiddleware;
import jason.asSemantics.Message;
import jason.architecture.AgArch;
import java.util.ArrayList;

public class XMPPAgent extends AgArch {
    private Util util = new Util();

    private XMPPMiddleware xmppBridge = null;
    public XMPPAgent(){
        super();
        this.xmppBridge = new XMPPMiddleware();
    }

    @Override
    public void sendMsg(Message m){
        if(util.isValidEmail(m.getReceiver())){
            if(isOUTConfigured()){
                try {
                    this.xmppBridge.sendMsg(m.getReceiver(),m.getIlForce(),m.getPropCont().toString());
                } catch (Exception e) {
                    this.getTS().getLogger().warning("Failed to send XMPP message: " + e.getMessage());
                }
            }else{
                this.getTS().getLogger().warning(Info.eMailProviderConfigurationNOTFOUND("sendMsg"));
            }
        }
        else{
            // se não um e-mail o destinatinario da MSG... então ele envia para um agente local
            try {
                super.sendMsg(m);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    @Override
    public void checkMail() {
        super.checkMail();          // buscando msg de algum agente interno ao SMA
        this.getChatMessage();
        //this.getEMailMessage();
        // implementar a busca de msg de algum agente externo ao SMA
    }

    public static XMPPAgent getXmppArch(jason.architecture.AgArch currentArch) {
        if (currentArch == null) {
            return null;
        }
        if (currentArch instanceof XMPPAgent) {
            return (XMPPAgent) currentArch;
        }
        return getXmppArch(currentArch.getNextAgArch());
    }

    public void getChatMessage() {
        if (isINConfigured()){
            ArrayList<Message> list = this.xmppBridge.checkMessages();
            if(list != null){
                for (Message item : list) {
                    this.getTS().getC().addMsg(item);
                }
            }
        }
    }

    public XMPPMiddleware getXMPPBridge() {
        return this.xmppBridge;
    }

    public boolean isINConfigured(){
        return this.xmppBridge.getLogin()!=null
                && this.xmppBridge.getPassword()!=null;
    }

    public boolean isOUTConfigured(){
         return this.xmppBridge.getLogin()!=null
                && this.xmppBridge.getPassword()!=null
                && this.xmppBridge.getXmppDomain()!=null
                && this.xmppBridge.getXmppHost()!=null
                && this.xmppBridge.getXmppPort()>0;
    }
}

