package com.users.beans;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import org.springframework.util.Base64Utils;


@Entity
@Table(name = "contact_images")
public class ContactImage {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(unique = true)
	private long contactId;

	private String contentType;

	@Lob
	private byte[] image;

	protected ContactImage() {}

	public ContactImage(long contactId) {
		this.contactId = contactId;
	}

	public ContactImage(long contactId, String contentType, byte[] image) {
		this.contactId = contactId;
		this.contentType = contentType;
		this.image = image;
	}

	@Override
	public String toString() {
		return "ContactImage [Contact Id= " + contactId + ", User Id= " + id + ", contentType= " + contentType + "]";
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getContactId() {
		return contactId;
	}

	public void setContactId(long contactId) {
		this.contactId = contactId;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public byte[] getImage() {
		return image;
	}

	public void setImage(byte[] image) {
		this.image = image;
	}

	public String getHtmlSrc() {
		return "data:" + this.contentType + ";base64," + Base64Utils.encodeToString(image);
	}

}
