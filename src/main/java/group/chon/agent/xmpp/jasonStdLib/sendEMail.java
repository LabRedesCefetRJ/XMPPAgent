package group.chon.agent.xmpp.jasonStdLib;

import group.chon.agent.xmpp.XMPPAgent;
import group.chon.agent.xmpp.core.Info;
import group.chon.agent.xmpp.core.Util;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

public class sendEMail extends DefaultInternalAction {
//    Util util;

    @Override
    public Object execute(final TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        final XMPPAgent mailerArch = XMPPAgent.getMailerArch(ts.getAgArch());
        if(mailerArch != null){
            if (args.length == 3) {
                Util util = new Util();
                String destination =  args[0].toString().replaceAll("\"","");
                if(util.isValidEmail(destination)){
                    if(mailerArch.isOUTConfigured()){
                        mailerArch.getEmailBridge().setLogger(ts.getLogger());
                        mailerArch.getEmailBridge().sendMsg(destination,args[1].toString(),args[2].toString());
                        return true;
                    }else {
                        ts.getLogger().warning(Info.eMailProviderConfigurationNOTFOUND(this.getClass().getName()));
                        return false;
                    }
                }else{
                    ts.getLogger().warning(Info.emailINVALID(this.getClass().getName()));
                    return false;
                }
            } else{
                ts.getLogger().warning(Info.wrongParametersERROR(this.getClass().getName()));
                return false;
            }
        }else {
            ts.getLogger().warning(Info.nonMailerAgentERROR(this.getClass().getName()));
            return false;
        }
    }
}
