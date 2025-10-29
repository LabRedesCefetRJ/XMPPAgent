package group.chon.agent.xmpp.jasonStdLib;

import group.chon.agent.xmpp.XMPPAgent;
import group.chon.agent.xmpp.core.Info;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

public class credentials extends DefaultInternalAction {


    @Override
    public Object execute(final TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        final XMPPAgent xmppArch = XMPPAgent.getXmppArch(ts.getAgArch());
        if(xmppArch != null){
            if(args.length == 2){
                xmppArch.getXMPPBridge().setLogger(ts.getLogger());
                xmppArch.getXMPPBridge().setLogin(args[0].toString().replaceAll("\"",""));
                xmppArch.getXMPPBridge().setPassword(args[1].toString().replaceAll("\"",""));
                return true;
            }else {
                ts.getLogger().warning(Info.wrongParametersERROR(this.getClass().getName()));
                return false;
            }
        }else{
            ts.getLogger().warning(Info.nonMailerAgentERROR(this.getClass().getName()));
            return false;
        }
    }
}
