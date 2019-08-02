package eu.arrowhead.core.gatekeeper.relay;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import eu.arrowhead.common.dto.GeneralAdvertisementMessageDTO;

public interface GatekeeperRelayClient {

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public Session createConnection(final String host, final int port) throws JMSException;
	public void closeConnection(final Session session);
	public MessageConsumer subscribeGeneralAdvertisementTopic(final Session session) throws JMSException;
	public GeneralAdvertisementMessageDTO getGeneralAdvertisementMessage(final Message msg) throws JMSException;
	public void publishGeneralAdvertisement(final Session session, final String recipientCN, final String recipientPublicKey, final String senderCN) throws JMSException;
}