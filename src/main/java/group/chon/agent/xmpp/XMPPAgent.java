package group.chon.agent.xmpp;

import group.chon.agent.xmpp.core.EMailMiddleware;
import group.chon.agent.xmpp.core.Info;
import group.chon.agent.xmpp.core.Util;
import jason.asSemantics.Message;
import jason.architecture.AgArch;
import java.util.ArrayList;

public class XMPPAgent extends AgArch {
    private Util util = new Util();

    private EMailMiddleware emailBridge = null;
    public XMPPAgent(){
        super();
        this.emailBridge = new EMailMiddleware();
    }

    @Override
    public void sendMsg(Message m){
        if(util.isValidEmail(m.getReceiver())){
            if(isOUTConfigured()){
                //this.emailBridge.sendMsg(m.getReceiver(),m.getIlForce(),m.getPropCont().toString());
                // TODO this.XMPPBridge.sendMsg
                // implementar o envio de msg via XMPP
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
        //this.getEMailMessage();
        //TODO this.getXMPPMessage();
        // implementar a busca de msg de algum agente externo ao SMA
    }



    public static XMPPAgent getMailerArch(jason.architecture.AgArch currentArch) {
        if (currentArch == null) {
            return null;
        }
        if (currentArch instanceof XMPPAgent) {
            return (XMPPAgent) currentArch;
        }
        return getMailerArch(currentArch.getNextAgArch());
    }

    public void getEMailMessage() {
        if (isINConfigured()){
            ArrayList<Message> list = this.emailBridge.checkEMail();
            if(list != null){
                for (Message item : list) {
                    this.getTS().getC().addMsg(item);
                }
            }
        }
    }

    public EMailMiddleware getEmailBridge() {
        return this.emailBridge;
    }

    public boolean isINConfigured(){
        return this.emailBridge.getLogin()!=null
                && this.emailBridge.getPassword()!=null
                && this.emailBridge.isRPropsEnable()
                && this.emailBridge.isRHostEnable();
    }

    public boolean isOUTConfigured(){
         return this.emailBridge.getLogin()!=null
                && this.emailBridge.getPassword()!=null
                && this.emailBridge.isSHostEnable()
                && this.emailBridge.isSPropsEnable();
    }
}

