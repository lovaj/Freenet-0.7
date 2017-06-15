package dataStructure.message;

import java.util.Stack;

import dataStructure.DarkPeer;
import protocol.LinkableProtocol;
import protocol.MessageProtocol;

public class GetNotFoundMessage extends BackwardMessage {

	//the HTL of the message which created this BackwardMessage
	//it will be used as HTL in case we generate a new GetMessage
	public final int getMessageHTL;
	
	public GetNotFoundMessage(float messageLocationKey, Stack<DarkPeer> routingPath, 
			long originalMessageId, int getMessageHTL){
		super(messageLocationKey, routingPath, originalMessageId);
		this.getMessageHTL = getMessageHTL;
	}
	
	public GetNotFoundMessage(GetNotFoundMessage another){
		super(another);
		this.getMessageHTL = another.getMessageHTL;
	}

	@Override
	public void doMessageAction(DarkPeer sender, MessageProtocol mp) {
		//debug check: it's impossible that we receive a GetNotFoundMessage for a message that we have never seen before
		if(!mp.alreadySeenMessage(this.originalMessageId))
			throw new RuntimeException("Peer "+sender.darkId+" has never seen message "+this.originalMessageId);
		//send the get message to the node which satisfies the following conditions:
		//1. is the closest neighbor w.r.t. the message key
		//2. he didn't receive this get message yet
		//find the closest neighbor w.r.t. the message key which never received this message before
		//notice that all the neighbors could have already received this message
		 DarkPeer receiver = ((LinkableProtocol) sender.getProtocol(mp.getLpId()))
				.getClosestNeighbor(this.messageLocationKey, mp.getMessageSent(this.originalMessageId));
		//if there are no available neighbors anymore, send back a GetNotFound message
		if(receiver==null){
			GetNotFoundMessage message = new GetNotFoundMessage(this);
			mp.sendBackwardMessage(sender, message);
		}
		//if there is a suitable neighbor
		else{
			//create a new GetMessage referring to the original GetMessage
			//notice that HTL has the same value of the previous GetMessage
			//notice that we pass the old routingPath
			//(i.e. the one who created this GetNotFoundMessage)
			GetMessage message = new GetMessage(this.messageLocationKey, this.getMessageHTL, 
				 this.originalMessageId, this.routingPath);
			mp.sendForwardMessage(sender, receiver, message);
		}
	}

	@Override
	public Object clone() {
		// TODO Auto-generated method stub
		return null;
	}

}