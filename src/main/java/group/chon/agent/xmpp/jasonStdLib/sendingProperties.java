package group.chon.agent.xmpp.jasonStdLib;

import group.chon.agent.xmpp.XMPPAgent;
import group.chon.agent.xmpp.core.Info;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

public class sendingProperties extends DefaultInternalAction {

    @Override
    public Object execute(final TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        final XMPPAgent mailerArch = XMPPAgent.getMailerArch(ts.getAgArch());
        if(mailerArch != null){
            if (args.length == 5) {
                mailerArch.getEmailBridge().setLogger(ts.getLogger());
                mailerArch.getEmailBridge().setSendAuth(
                        Boolean.parseBoolean(args[0].toString().replaceAll("\"","")),
                        Boolean.parseBoolean(args[1].toString().replaceAll("\"","")),
                        Boolean.parseBoolean(args[2].toString().replaceAll("\"","")),
                        args[3].toString().replaceAll("\"",""),
                        args[4].toString().replaceAll("\"",""));
                return true;
            } else {
                ts.getLogger().warning(Info.wrongParametersAdvancedActionsERROR(this.getClass().getName()));
                return false;
            }
        }else {
            ts.getLogger().warning(Info.nonMailerAgentERROR(this.getClass().getName()));
            return false;
        }
    }
}
