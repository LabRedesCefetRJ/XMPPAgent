package group.chon.agent.xmpp.jasonStdLib;

import group.chon.agent.xmpp.XMPPAgent;
import group.chon.agent.xmpp.core.Info;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Term;

public class chatService extends DefaultInternalAction {

    @Override
    public Object execute(final TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        final XMPPAgent xmppArch = XMPPAgent.getXmppArch(ts.getAgArch());
        if(xmppArch != null){
            if (args.length == 3) {
                String pDomain = ((StringTerm) args[0]).getString();
                String pHost = ((StringTerm) args[1]).getString();
                int pPort = Integer.parseInt(((StringTerm) args[2]).getString());

                if(pDomain!="" || pHost!="" || pPort>0){
                    xmppArch.getXMPPBridge().setLogger(ts.getLogger());
                    xmppArch.getXMPPBridge().setProps(
                            pDomain,
                            pHost,
                            pPort);
                    xmppArch.getXMPPBridge().connect();
                }else{
                    ts.getLogger().warning(Info.wrongParametersERROR("configuring chatService"));
                    return false;
                }
                return true;
            } else {
                ts.getLogger().warning(Info.wrongParametersERROR(this.getClass().getName() + " - " + args.length));
                return false;
            }
        }else{
            ts.getLogger().warning(Info.nonMailerAgentERROR(this.getClass().getName()));
            return false;
        }
    }
}
