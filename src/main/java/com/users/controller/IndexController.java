package com.users.controller;

import static com.users.security.Role.ROLE_ADMIN;
import static com.users.security.Role.ROLE_USER;

import java.util.List;
import java.util.Optional; //?

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
import org.springframework.web.servlet.ModelAndView;

import com.users.beans.Email;
import com.users.beans.User;
import com.users.beans.UserImage;
import com.users.beans.UserRole;

import com.users.repositories.UserImageRepository;
import com.users.repositories.UserRepository;
import com.users.repositories.UserRoleRepository;

import com.users.security.PermissionService;
import com.users.service.EmailService;
import com.users.service.ImageService;


@Controller
public class IndexController {
	private static final Logger log = LoggerFactory.getLogger(IndexController.class);

	@Autowired
	private UserRepository userRepo;

	
	@Autowired
	private UserImageRepository userImageRepo;
	
	
	@Autowired
	private  PermissionService permissionService;
	
	
	@Autowired
	private ImageService imageService;	//step 5, #11
	
	
	@Autowired
	private UserRoleRepository userRoleRepo;
	
	
	@Autowired
	private EmailService emailService;		//step 6, #31

	@RequestMapping("/greeting") //step 6, #32
	public String greeting(@RequestParam(value = "name", required = false, defaultValue = "World") String name, Model model) {
		model.addAttribute("name", name);
		model.addAttribute("repoCount", userRepo.count());//??
		return "greeting";
	}

	@RequestMapping("/")
	public String home(Model model) {
		return permissionService.hasRole(ROLE_ADMIN) ? "redirect:/users" : "redirect:/contacts";
	}
	
	@Secured("ROLE_ADMIN")		//if user is an ADMIN, this will allow him/her to see a list of users
	@RequestMapping("/users")
	public String listUsers(Model model) {
		model.addAttribute("users", userRepo.findAllByOrderByFirstNameAscLastNameAsc());
		return "listUsers";	//listUsers can't work yet bc I don't have a listUsers.html, but I renamed list to be listUsers, this should work but it doesn't
	}
	
	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public ModelAndView getLoginPage(@RequestParam Optional<String> error) {
		return new ModelAndView("login", "error", error);
	}

	@RequestMapping("/myprofile")
	public String myprofile(Model model) {
		return profile(permissionService.findCurrentUserId(), model);
	}
	
	@RequestMapping("/register")
	public String register(Model model) {
	return createUser(model);
}
	
	
	@RequestMapping("/user/{userId}")
	public String profile(@PathVariable long userId, Model model) {
		model.addAttribute("user", userRepo.findOne(userId));
		
		if(!permissionService.canAccessUser(userId)) {
			log.warn("Cannot allow user to view " + userId);
			return "redirect:/";
		}
		
		List<UserImage> images = userImageRepo.findByUserId(userId);
		
		if (!CollectionUtils.isEmpty(images)) {
			model.addAttribute("userImage", images.get(0));
		}
		
		model.addAttribute("permissions", permissionService);
		return "profile";
	}
	
	
	
	@RequestMapping(value = "/user/{userId}/edit", method = RequestMethod.GET)
	public String profileEdit(@PathVariable long userId, Model model) {
		model.addAttribute("user", userRepo.findOne(userId));
		
		if(!permissionService.canAccessUser(userId)) {
			log.warn("Cannot allow user to edit " + userId);
			return "profile";
		}
		
		List<UserImage> images = userImageRepo.findByUserId(userId);
		if (!CollectionUtils.isEmpty(images)) {
			model.addAttribute("userImage", images.get(0));
		}
		return "profileEdit";
	}

	
	
	@RequestMapping(value = "/user/{userId}/edit", method = RequestMethod.POST)
	public String profileSave(@ModelAttribute User user,
			@PathVariable long userId,
			@RequestParam(name = "removeImage", defaultValue = "false") boolean removeImage,
			@RequestParam("file") MultipartFile file,
			Model model) {
		
		if(!permissionService.canAccessUser(userId)) { 
			log.warn("Cannot allow user to edit " + userId);
			return "profile";
		} //user is an Admin
		
		log.debug("Saving user " + user);
		userRepo.save(user);
		model.addAttribute("message", "User " + user.getEmail() + " saved.");
		if(removeImage) {
			imageService.deleteImage(user);
		} else {
			imageService.saveImage(file, user);
		}
		return profile(userId, model);
	}
	
	
	

	//commented out @Secured("ROLE_ADMIN") this is GET method for createUser
	@RequestMapping(value = "/user/create", method = RequestMethod.GET)	//changed from createContact
	public String createUser(Model model) {	//model is a placeholder to hold the information you want to display on the view.
		model.addAttribute("user", new User());	//here, it is the list of parameters associated with user
		
		return "userCreate";	//takes us to userCreate.html, where admin can fill out new user's details
	}
	
	
	
	//cut out @Secured("ROLE_ADMIN") this is POST method for create User
	@RequestMapping(value = "/user/create", method = RequestMethod.POST)
	public String createUser(@ModelAttribute User user,			//changed from createContact
			@RequestParam("file") MultipartFile file, Model model) {
		
		log.info(user.toString());		//step5, #18
		
		User savedUser = userRepo.save(user);	
		UserRole role = new UserRole(savedUser, ROLE_USER);		
		userRoleRepo.save(role);
		imageService.saveImage(file, savedUser); //lines of code120-122 from step5, #29

		return profile(savedUser.getId(), model); //saving the new user to the datasource, modified in step5, #19
	} //did the changes made to userRoleRepo get saved? 
	
	//THIS SHOULDN"T BE TAKING ME BACK TO LOGIN PAGE!!!

	
	
	@RequestMapping(value = "/email/user", method = RequestMethod.GET)
	public String prepEmailUser(Model model) { //step6, #35: 
		String url = "http://localhost:8080/register/";

		model.addAttribute("message", "To join SRM just follow this link: " + url);
		model.addAttribute("pageTitle", "Invite User");
		model.addAttribute("subject", "Join me on CRM");
		
		return "sendEmail";//changed from sendMail
	}//	step6, #36: this method will prepare an email message to invite a user to join CRM
	//bc you can se what the message says, the pageTitle, the subject it returns sendMail
	//but you know it hasn't been sent yet bc Transport isn't being used. 

	
	
	
	//Finishing Step #6 changed "/email/send" on line 78 to "/send/mail" bc that it what it says in header.html
	//but then I changed it back. 
	@RequestMapping(value = "/email/send", method = RequestMethod.POST) //step 6, #33
	public String sendEmail(Email email, Model model) {
		emailService.sendMessage(email); //step6, #34: Why is sendMessage being called here?
		//IDK. From: https://www.tutorialspoint.com/javamail_api/javamail_api_core_classes.htm=
		//Transport class is used as a message transport mechanism. This class normally uses the SMTP protocol to send a message.
		
		return "redirect:/";
	}

	
	
	

	



	
	
	
}
