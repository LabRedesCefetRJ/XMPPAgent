package group.chon.agent.xmpp.jasonStdLib;

import group.chon.agent.xmpp.XMPPAgent;
import group.chon.agent.xmpp.core.Info;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

public class receiveEMail extends DefaultInternalAction {

    @Override
    public Object execute(final TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        final XMPPAgent mailerArch = XMPPAgent.getMailerArch(ts.getAgArch());
        if(mailerArch != null){
            if (args.length == 2) {
                mailerArch.getEmailBridge().setLogger(ts.getLogger());
                mailerArch.getEmailBridge().setLogin(args[0].toString());
                mailerArch.getEmailBridge().setPassword(args[1].toString());
                mailerArch.getEMailMessage();
                return true;
            }else if(args.length == 0){
                if(mailerArch.isINConfigured()){
                    mailerArch.getEmailBridge().setLogger(ts.getLogger());
                    mailerArch.getEMailMessage();
                    return true;
                }else{
                    ts.getLogger().warning(Info.eMailProviderConfigurationNOTFOUND(this.getClass().getName()));
                    return false;
                }
            }else{
                ts.getLogger().warning(Info.wrongParametersERROR(this.getClass().getName()));
                return false;
            }
        }else {
            ts.getLogger().warning(Info.nonMailerAgentERROR(this.getClass().getName()));
            return false;
        }
    }
}
