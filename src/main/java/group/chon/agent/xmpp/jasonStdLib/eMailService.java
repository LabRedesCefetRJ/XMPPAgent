package group.chon.agent.xmpp.jasonStdLib;

import group.chon.agent.xmpp.XMPPAgent;
import group.chon.agent.xmpp.core.Info;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

public class eMailService extends DefaultInternalAction {

    @Override
    public Object execute(final TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        final XMPPAgent mailerArch = XMPPAgent.getMailerArch(ts.getAgArch());
        if(mailerArch != null){
            if (args.length == 2) {
                String[] input = parseString(args[0].toString());
                String[] output = parseString(args[1].toString());

                if(input[1].equals("imaps")){
                    mailerArch.getEmailBridge().setLogger(ts.getLogger());
                    mailerArch.getEmailBridge().setRAuth(
                            false,
                            true,
                            false,
                            "null",
                            "null");
                    mailerArch.getEmailBridge().setReceiverProps(
                            input[0].toString(),
                            input[1].toString(),
                            "993");
                }else{
                    ts.getLogger().warning(Info.eMailServiceActionProtocolNOTFOUND(input[1]));
                    return false;
                }

                if(output[1].equals("smtpOverTLS")){
                    mailerArch.getEmailBridge().setLogger(ts.getLogger());
                    mailerArch.getEmailBridge().setSendAuth(
                            true,
                            true,
                            false,
                            "null",
                            "null");
                    mailerArch.getEmailBridge().setSendProps(
                            output[0].toString(),
                            "smtp",
                            "465"
                    );
                }else{
                    ts.getLogger().warning(Info.eMailServiceActionProtocolNOTFOUND(output[1]));
                    return false;
                }
                return true;
            } else {
                ts.getLogger().warning(Info.wrongParametersERROR(this.getClass().getName()));
                return false;
            }
        }else{
            ts.getLogger().warning(Info.nonMailerAgentERROR(this.getClass().getName()));
            return false;
        }
    }


    private String[] parseString(String input) {

        input = input.substring(1, input.length() - 1);

        String[] parts = input.split(",");
        parts[0] = parts[0].replaceAll("\"", "").trim();
        parts[1] = parts[1].replaceAll("\"", "").trim();

        return parts;
    }
}
