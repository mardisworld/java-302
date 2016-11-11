package com.users.controller;


import static org.h2.util.StringUtils.isNullOrEmpty;
import java.util.List;
//import java.util.Optional; //?

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.users.beans.Contact;
import com.users.beans.ContactImage;
import com.users.beans.User;
import com.users.repositories.ContactImageRepository;
import com.users.repositories.ContactRepository;
import com.users.security.PermissionService;



@Controller
public class ContactController {
	private static final Logger log = LoggerFactory.getLogger(ContactController.class);
	

	@Autowired
	private ContactRepository contactRepo;
	
	
	@Autowired
	private ContactImageRepository contactImageRepo;
	
	
	@Autowired
	private PermissionService permissionService;
	
	
	@Secured("ROLE_USER") 
	@RequestMapping("/contacts")
	public String listContacts(Model model) {
		long currentUserId = permissionService.findCurrentUserId();
		model.addAttribute("contacts",
	contactRepo.findAllByUserIdOrderByFirstNameAscLastNameAsc(currentUserId));
		return "listContacts"; //returns a list of contacts based on Current User's id
	}
	
	
	@Secured("ROLE_USER") //authenticates that current user has role of user, and authenticates so user can access contact
	@RequestMapping("/contact/{contactId}")
	public String contact(@PathVariable long contactId, Model model) {
		model.addAttribute("contact", contactRepo.findOne(contactId));
		List<ContactImage> images = contactImageRepo.findByContactId(contactId);
		if (!CollectionUtils.isEmpty(images)) {
			model.addAttribute("contactImage", images.get(0));
		}
		model.addAttribute("permissions", permissionService);
		return "contact";	//returns a contact based on the contactId, which is passed in by the user when he/she clicks on "Contact" in the listContacts.html file
	}
	
	
	
	@Secured("ROLE_USER") //authenticates that current user has role of user, and authenticates so user can RequestMapping to edit contact edit
	@RequestMapping(value = "/contact/{contactId}/edit", method = RequestMethod.GET)
	public String contactEdit(@PathVariable long contactId, Model model) {
		model.addAttribute("contact", contactRepo.findOne(contactId));

		if (!permissionService.canEditContact(contactId)) {
			log.warn("Cannot allow user to edit " + contactId);
			return "contact";
		}

		List<ContactImage> images = contactImageRepo.findByContactId(contactId);
		if (!CollectionUtils.isEmpty(images)) {
			model.addAttribute("contactImage", images.get(0));
		}
		return "contactEdit"; //returns the html page to edit a contact based on the contactId, which is passed in by the user when he/she clicks on "Contact" in the listContacts.html file
	}
	
	
	@Secured("ROLE_USER") //authenticates that current user has role of user, and authenticates so user can RequestMapping to create contact
	@RequestMapping(value = "/contact/create", method = RequestMethod.GET)
	public String createContact(Model model) {
		model.addAttribute("contact", new Contact(permissionService.findCurrentUserId()));
		
		return "contactCreate"; //returns the html page to create a contact based on the current user's id
	}

	@Secured("ROLE_USER") //authenticates that current user has role of user, and authenticates so user can PostMapping to save contact changes
	@RequestMapping(value = "/contact/create", method = RequestMethod.POST)
	public String createContact(@ModelAttribute Contact contact,
			@RequestParam("file") MultipartFile file, Model model) {

		Contact savedContact = contactRepo.save(contact);

		return profileSave(savedContact, savedContact.getId(), false, file, model); //saves new contact to current user's list of contacts
	} //returns string/steam of contact's data to temporary datasource(part of Model)

	
	
	@Secured("ROLE_USER")
	@RequestMapping(value = "/contact/search", method = RequestMethod.POST)
	public String searchContacts(@RequestParam("search") String search, Model model) {
		log.debug("Searching by " + search);
		model.addAttribute("contacts",
				contactRepo.findByLastNameOrFirstNameOrEmailOrTwitterHandleOrFacebookUrlIgnoreCase(
						search, search, search, search, search));
		model.addAttribute("search", search);
		return "listContacts";
	} ////Controller calls methods that act on conacts through contactRepo methods


	@Secured("ROLE_USER")
	@RequestMapping(value = "/email/contact/{contactId}", method = RequestMethod.GET)
	public String prepEmailContact(@PathVariable long contactId, Model model) {
		User user = permissionService.findCurrentUser();
		Contact contact = contactRepo.findByUserIdAndId(user.getId(), contactId);

		StringBuilder message = new StringBuilder().append("Your friend ")
				.append(user.getFirstName()).append(" ").append(user.getLastName())
				.append(" has forwarded you the following contact:\n\n")
				.append(contact.getFirstName()).append(" ").append(contact.getLastName())
				.append("\n");
		if (!isNullOrEmpty(contact.getEmail())) {//if email not null or empty
			message.append("Email: ").append(contact.getEmail()).append("\n");
		}
		if (!isNullOrEmpty(contact.getPhoneNumber())) { //if phoneNumber not null or empty
			message.append("Phone: ").append(contact.getPhoneNumber()).append("\n");
		}
		if (!isNullOrEmpty(contact.getTwitterHandle())) { //if twitterHandle not null or empty
			message.append("Twitter: ").append(contact.getTwitterHandle()).append("\n");
		}
		if (!isNullOrEmpty(contact.getFacebookUrl())) { //if facebookUrl not null or empty
			message.append("Facebook: ").append(contact.getFacebookUrl()).append("\n");
		}

		model.addAttribute("message", message.toString());	//adds message attribute
		model.addAttribute("pageTitle", "Forward Contact");	//adds pageTitle, Forward Contact
		model.addAttribute("subject",
				"Introducing " + contact.getFirstName() + " " + contact.getLastName());
				//adds subject attribute, returns subject "Introducing FirstName LastName
		return "sendMail";	//calls sendMain.html (we have not written this yet)
	}

	
	
	@Secured("ROLE_USER") 
	@RequestMapping(value = "/contact/{contactId}/edit", method = RequestMethod.POST) //authenticates that current user has role of user, and authenticates so user can PostMapping to save contact
	public String profileSave(@ModelAttribute Contact contact, @PathVariable long contactId,
			@RequestParam(name = "removeImage", defaultValue = "false") boolean removeImage,
			@RequestParam("file") MultipartFile file, Model model) {

		if (!permissionService.canEditContact(contactId)) {
			log.warn("Cannot allow user to edit " + contactId);
			return "contact";
		} 
		//user is a contact
		log.debug("Saving contact " + contact);
		contactRepo.save(contact);
		model.addAttribute("message", "Contact " + contact.getEmail() + " saved.");

		if (!file.isEmpty()) {
			try {
				List<ContactImage> images = contactImageRepo.findByContactId(contact.getId());
				ContactImage img = (images.size() > 0) ? images.get(0) : new ContactImage(contactId);
				img.setContentType(file.getContentType());
				img.setImage(file.getBytes());
				contactImageRepo.save(img);

				log.debug("Saved Image");
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

		} else if (removeImage) {
			log.debug("Removing Image");
			// contact.setImage(null);
			List<ContactImage> images = contactImageRepo.findByContactId(contact.getId());

			for (ContactImage img : images) {
				contactImageRepo.delete(img);
			}
		}

		return contact(contactId, model); //posts modified contact data to temporary datasource(part of Model)
	}//return type is a string, but it looks like method is returning a contact object?

	


}
