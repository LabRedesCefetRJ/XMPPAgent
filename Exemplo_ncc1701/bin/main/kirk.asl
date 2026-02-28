/*XMPP*/
+!conf : myCredentials(Login, Password) & chat(Host, Port) <-
	print ("This is Commander James T. Kirk, Enterprise's Captain");
	.xmpp.credentials(Login, Password);
	.xmpp.chatService(Host, Port);
	+connected;
	!testComm;
.

+!contact: connected & uhuraContact(Uhura) & communication(ok) <-
	.print("Damage report!");
	.xmpp.sendMessage(Uhura, achieve, damageReport);
.

+!contact: not connected <-
	.print("Without Communication!");
	!conf;
.

+report (Damages)[source (X)] <-
	.print("Damages in ", Damages);
	.print("Stay in standard orbit and wait for instructions.");
	.xmpp.sendMessage(X, tell, retransmit(scott, redAlert) );
	.wait (2000);
.

+!testComm : connected & uhuraContact(Uhura) & not communication(ok) <-
	.print("Kirk to Enterprise... ");
	.xmpp.sendMessage(Uhura, tell, communication(trying) );
	.wait(2500);
	!testComm;
.

+!testComm: communication(ok) <- !contact.