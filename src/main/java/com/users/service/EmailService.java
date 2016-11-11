package com.users.service;

import static javax.mail.Message.RecipientType.TO;

import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.users.beans.Email;
import com.users.security.PermissionService;

import com.users.security.PermissionService;

@Service
public class EmailService {
	private static final Logger log = LoggerFactory.getLogger(EmailService.class);
	private String username ="";	//why do we have to input desired username & password? 
	private String password = "";	//How would this work if we hadn't input it?
	private Properties props;
	private Authenticator auth;
	
	@Autowired
	private PermissionService permissionService;
	
	public EmailService() {

//		From: https://docs.oracle.com/javase/7/docs/api/java/util/Properties.html
//		public class Properties
//		extends Hashtable<Object,Object>. Each key and its corresponding value in the property list is a string.
//		The Properties class represents a persistent set of properties. The Properties can be saved to a stream or loaded from a stream. Each key and its corresponding value in the property list is a string.
//		Because Properties inherits from Hashtable, the put and putAll methods can be applied to a Properties object.
//		Parameters being passed in are strings
		
		props = new Properties();
		props.put("mail.smtp.auth", "true");//auth=true, If true, attempt to authenticate the user using the AUTH command. Defaults to false.
		props.put("mail.smtp.starttls.enable", "true");//starttls.enable=true, If true, enables the use of the STARTTLS command (if supported by the server) to switch the connection to a TLS-protected connection before issuing any login commands. Defaults to false.
		props.put("mail.smtp.host", "smtp.gmail.com");//smtp server to connect to smtp.gmail.com
		props.put("mail.smtp.port", "587");//The SMTP server port to connect to, if the connect() method doesn't explicitly specify one. Defaults to 25.
		//SMTP is an acronym for Simple Mail Transfer Protocol.
		//It is an Internet standard for electronic mail (e-mail) transmission across Internet Protocol (IP) networks. 
		
		auth = new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		};
	}

	public boolean sendMessage(Email email) {//returning a boolean bc method is evaluating whether it will be able to send the message or not
		Session session = Session.getInstance(props, auth);//It returns the new session by passing in props and authentication. If these are evaluated as true, method can continue.
		Message message = new MimeMessage(session);//What's a MimeMessage?
		//MIME: Acronym for Multipurpose Internet Mail Extensions.
		//It is not a mail transfer protocol. 
		//Instead, it defines the content of what is transferred: the format 
		//of the messages, attachments, and so on. 
		try {
			message.setRecipient(TO, new InternetAddress(email.getTo()));
			message.setReplyTo(
					new Address[] { new InternetAddress(permissionService.getCurrentEmail()) });
			message.setSubject(email.getSubject());
			message.setText(email.getCustom() + "\n\n" + email.getMessage());

			Transport.send(message);//Transport is sending the message
		} catch (Exception e) {
			log.error("Unable to send message", e);
			return false;
		}
		return true;//evaluates whether message can be sent should be called canSendMessage
	}

}

	
	
