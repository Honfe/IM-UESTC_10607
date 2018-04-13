package Server; /**
 *
 */

import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import network.commonClass.*;
import network.networkDataPacketOperate.*;
import network.NetworkForServer.*;

/**
 * @author 97njczh
 */
public class ServerThread extends Thread {
	
	private CommunicateWithClient client;
	private Account account;
	
	public String getAccountId() {
		
		return account.getID();
	}
	
	public void setAccountOffline() {
		
		account.setOnLine(false);
	}
	
	/*======================================= related to RecvThread =======================================*/
	
	private RecvThread recvThread;
	private Vector<String> recvQueue;
	
	public boolean isRecvQueueEmpty() {
		
		return recvQueue.isEmpty();
	}
	
	public synchronized String getMsgFromRecvQueue() {
		
		String message = recvQueue.firstElement();
		recvQueue.removeElementAt(0);
		return message;
	}
	
	public synchronized void putMsgToRecvQueue( String message ) {
		
		recvQueue.add(message);
	}
	
	
	
	/*======================================= related to sendThread =======================================*/
	
	private SendThread sendThread;
	private Vector<String> sendQueue;
	
	public boolean isSendQueueEmpty() {
		
		return sendQueue.isEmpty();
	}
	
	public synchronized String getMsgFromSendQueue() {
		
		String message = this.sendQueue.firstElement();
		this.sendQueue.removeElementAt(0);
		return message;
	}
	
	public synchronized void putMsgToSendQueue( String message ) {
		
		sendQueue.add(message);
	}
	
	
	
	/*====================================== related to ServerThread ======================================*/
	
	public ServerThread( Socket socket ) throws IOException {
		
		client = new CommunicateWithClient(socket);
		recvQueue = new Vector<String>();
		sendQueue = new Vector<String>();
		account = null;
	}
	
	
	private int signIn() {
		
		String loginMsg = null;
		
		try {
			//用户尚未登录，暂时没有创建收发子线程，调用底层recv函数接收好友登录信息
			loginMsg = client.recvFromClient();
			
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("[ ERROR ] 接受用户登录请求（及信息）失败！无法处理用户登录！");
			return 1;   // 登录错误代码 1，接受用户登录请求失败
		}
		
		if (MessageOperate.getMsgType(loginMsg) != MessageOperate.LOGIN) {
			
			System.out.println("[ ERROR ] 未登录，试图进行其他操作！");
			return 2;   // 登录错误代码 2，未登录并试图进行其他操作
			
		}
		// 从接收队列取出登录请求，并解析出用户登录信息
		Login loginInfo = MessageOperate.getLoginAccountInfo(loginMsg);
		
		if (Server.isUserOnline(loginInfo.getAccountId())) {  // 用户重复登录
			
			// TODO 需要给用户反馈不同的提示
			putMsgToSendQueue(MessageOperate.sendNotFinishMsg());
			System.out.println("[ ERROR ] 用户重复登陆！");
			return Integer.parseInt(loginInfo.getAccountId());   // 登录错误代码 用户id，该用户重复登陆
		}
		
		// 在数据库中查询登陆信息，查询成功返回用户account信息，否则返回null
		account = (new DatabaseOperator()).isLoginInfoCorrect(loginInfo);
		
		if (account != null) { // 登录信息与数据库中比对成功
			
			// 在服务器的服务子线程数据库中注册该线程
			Server.regServerThread(this);
			
			// 客户服务子线程创建其子线程：收线程和发线程，注意：收应该首先创建
			// 由于在发线程中返回登录成功信息，因此放在后面创建以确保收发线程都已创建
			recvThread = new RecvThread(client, account.getID(), this);
			sendThread = new SendThread(client, account.getID(), this);
			
			System.out.println("[ LOGIN ] 用户ID：" + loginInfo.getAccountId() + " login successful!");
			System.out.println("[ READY ] 当前在线人数【 "
					                   + Server.getOnlineCounter() + " 】");
			
			recvThread.start(); // 负责监听有没有消息到达，有则把这些消息加入到接收队列中，由ServerThread处理
			sendThread.start(); // 负责监听消息发送队列中有没有消息
			
			// TODO 对线程创建失败的处理
			
			putMsgToSendQueue(MessageOperate.sendFinishMsg());
			
			return -1; // 登录成功成功代码 -1
			
		} else { // 登录信息与数据库中比对失败
			
			try {
				// TODO 这里最好加一下返回失败代码
				client.sendToClient(MessageOperate.sendNotFinishMsg()); // 登录失败，收发子线程无法创建
				System.out.println("[ READY ] 用户ID：" + loginInfo.getAccountId() + " 登录信息验证失败，返回失败反馈！");
				
			} catch (IOException e) {
				System.out.println("[ ERROR ] 用户ID：" + loginInfo.getAccountId() + " 用户登录失败反馈发送失败！");
				e.printStackTrace();
			}
			
			return 0;   // 登录错误代码 0，登录验证失败
		}
		
	}
	
	private boolean logout() {
		
		try {
			
			// 在服务器服务子线程数据库中注销该线程
			Server.delServerThread(account.getID());
			
			System.out.println("[ LOGIN ] 用户ID：" + account.getID() + " logout!");
			System.out.println("[ READY ] 当前在线人数【 "
					                   + Server.getOnlineCounter() + " 】");
			
			recvThread.setExit(true);
			sendThread.setExit(true);
			recvThread.join();
			sendThread.join();
			
		} catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		}
		return true;
		
	}
	
	/**
	 * 将待发送的好友列表信息加入发送队列
	 */
	private void sendFriendsList() {
		
		putMsgToSendQueue(
				MessageOperate.sendFriendList(
						new DatabaseOperator().getFriendListFromDb(
								account.getID())));
		
	}
	
	
	/**
	 * 将待发送的用户个人信息加入发送队列
	 */
	public void sendMyselfInfo() {
		
		putMsgToSendQueue(
				MessageOperate.sendUserInfo(account));
		
	}
	
	public void run() {
		
		int signInStatus = signIn();
		
		if (signInStatus < 0) {
			
			// 登陆成功用户为登录状态，线程持续接受客户端消息，直到判定用户下线
			while (account.getOnLine()) {
				
				if (!recvQueue.isEmpty()) {
					
					String message = getMsgFromRecvQueue();
					switch (MessageOperate.getMsgType(message)) {        // 判断请求类型
						
						// 客户端请求好友列表，返回好友列表；
						case MessageOperate.FRIENDLIST:
							sendFriendsList();
							break;
						
						case MessageOperate.MYSELF: // 请求个人信息
							sendMyselfInfo();
							break;
						
						case MessageOperate.CHAT:   // 转发消息
							Server.sendToOne(message);
							break;
						
						default:
							break;
					} // end switch
				} // end if - !recvQueue.isEmpty()
			} // end while;
			
			logout();
		} // end if - sign in
		
		try {   // 关闭套接字
			
			client.endConnect();
			
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("[ ERROR ] 客户端地址：" + client.getClientSocket() +"套接字关闭失败！");
		}
		
		if (signInStatus >= 10000)
			System.out.println("[ READY ] 重复登录用户ID：" + signInStatus + " 服务子线程已结束！");
		else if (signInStatus == -1)
			System.out.println("[ READY ] 用户ID：" + account.getID() + " 服务子线程已结束！");
		else
			System.out.println("[ READY ] 用户服务子线程已结束！");
	}
	
}
