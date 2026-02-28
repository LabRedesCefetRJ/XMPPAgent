/* Crenças Iniciais */

/*XMPP*/
myCredentials("v-sobri1@jabber.hot-chilli.net","tcc2025").
chat("jabber.hot-chilli.net","5222").

/*ContextNet*/
myUUID("49a13d91-5fb7-45ac-aad3-be05b6ce4c77").
skyNet ("skynet.chon.group",5500).

/* Objetivos Iniciais */
!conf.

/* XMPP */
+!conf : myCredentials(Login,Password) & chat(Host,Port) <-
	.print("This is Lieutenant Nyota Uhura, Communications officer");
	.xmpp.credentials(Login,Password);
	.xmpp.chatService(Host,Port);
.

+!damageReport[source(X)] <-
	.print("Commander Kirk, Deck 2 compromised!");
	.xmpp.sendMessage(X,tell,report("Deck 2"));
.

+communication(trying)[source(X)] <-
	.print("Entreprise Listen ",X);
	.xmpp.sendMessage(X,tell,communication(ok));
	-communication(trying)[source(X)];
.
	
+retransmit(Dest,Msg)[source(X)] <-
	.send(Dest,tell,Msg);
	-retransmit(Dest,Msg)[source(X)].
	
