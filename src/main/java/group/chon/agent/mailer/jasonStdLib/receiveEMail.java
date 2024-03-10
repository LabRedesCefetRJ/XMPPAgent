package group.chon.agent.mailer.jasonStdLib;

import group.chon.agent.mailer.Mailer;
//import group.chon.agent.mailer.core.MailerTS;
import group.chon.agent.mailer.core.Info;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

public class receiveEMail extends DefaultInternalAction {
    //MailerTS mailerTS = null;

    @Override
    public Object execute(final TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        final Mailer mailerArch = Mailer.getMailerArch(ts.getAgArch());
        if(mailerArch != null){
            if (args.length == 2) {
                mailerArch.getEmailBridge().setLogin(args[0].toString());
                mailerArch.getEmailBridge().setPassword(args[1].toString());
                mailerArch.getEMailMessage();
                return true;
            }else if(args.length == 0){
                if(mailerArch.isINConfigured()){
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
//        mailerTS = new MailerTS(ts.getAg(), ts.getC(), ts.getSettings(), ts.getAgArch());
//        if (args.length == 2) {
//            mailerTS.getMailerArch().getEmailBridge().setLogin(args[0].toString());
//            mailerTS.getMailerArch().getEmailBridge().setPassword(args[1].toString());
//        }
//        mailerTS.getMailerArch().getEMailMessage();
//        return true;

    }
}
