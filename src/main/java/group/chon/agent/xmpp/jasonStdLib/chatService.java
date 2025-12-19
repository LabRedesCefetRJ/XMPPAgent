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
            if (args.length == 2) {
                String pDomain = xmppArch.getXMPPBridge().getXmppDomain();
                String pHost = ((StringTerm) args[0]).getString();
                int pPort = Integer.parseInt(((StringTerm) args[1]).getString());

                if(pDomain != null && !pDomain.isEmpty() && !pHost.isEmpty() && pPort > 0){
                    xmppArch.getXMPPBridge().setLogger(ts.getLogger());
                    xmppArch.getXMPPBridge().setProps(
                            pHost,
                            pPort);
                    xmppArch.getXMPPBridge().connect();
                    return true;
                }else{
                    ts.getLogger().warning(Info.wrongParametersERROR("configuring chatService - domain not set or invalid parameters"));
                    return false;
                }
            } else {
                ts.getLogger().warning(Info.wrongParametersERROR(this.getClass().getName() + " - expects 2 arguments (host, port)"));
                return false;
            }
        }else{
            ts.getLogger().warning(Info.nonMailerAgentERROR(this.getClass().getName()));
            return false;
        }
    }
}
