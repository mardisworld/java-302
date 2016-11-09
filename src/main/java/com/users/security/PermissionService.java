package com.users.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import static org.springframework.security.core.context.SecurityContextHolder.getContext;
import org.springframework.stereotype.Service;

import com.users.repositories.ContactRepository;
import com.users.repositories.UserRepository;
import static com.users.security.Role.ROLE_USER;
import static com.users.security.Role.ROLE_ADMIN;


//from Spring documentations @ https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/stereotype/Service.html
@Service //"an operation offered as an interface that stands alone in the model, with no encapsulated state." In simpler terms, this allows for "scanning the project" for annotating classes at service layer level. All business logic should be in Service classes. 
public class PermissionService {

	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private ContactRepository contactRepo;
	
	private UsernamePasswordAuthenticationToken getToken() { //method that returns type UsernamePasswordAuthenticationToken
		return (UsernamePasswordAuthenticationToken) 
				getContext().getAuthentication(); //getContext() obtains the current SecurityContext.; getAuthentication() obtains the currently authenticated principal, or an authentication request token.

}

	public boolean hasRole(Role role) { //takes in user's role, returns true/false
		for (GrantedAuthority ga : getToken().getAuthorities()) { //GrantedAuthority represents an authority granted to an Authentication object
			if (role.toString().equals(ga.getAuthority())) { //if role.toString() is equal to (ga.getAuthority()))
				return true;
			}
		}
		return false;
	}
	
	public long findCurrentUserId() {
		return userRepo.findByEmail(getToken().getName()).get(0).getId(); // 
	}
	

	
//	public boolean canEditUser(long userId) {
//		long currentUserId = userRepo.findByEmail(getToken().getName()).get(0).getId();
//		return hasRole(ADMIN) || (hasRole(USER) && currentUserId == userId);
//	}
//
//	
//	public long findCurrentUserEmail() {
//		return userRepo.findByEmail(getToken().getName()).get(0).getId(); // cut out from canEditUserMethod: long currentUserId = userRepo.findByEmail(getToken().getName()).get(0).getId();//finds currentUserID based on email
//
//	}

	public boolean canAccessUser(long userId) { //method that returns boolean true/false if can edit user
		return hasRole(ROLE_ADMIN) || (hasRole(ROLE_USER) && findCurrentUserId() == userId); //returns true if user is an ADMIN or if current userId == userID
	}

	public boolean canEditContact(long contactId) {
		return hasRole(ROLE_USER) && contactRepo.findByUserIdAndId(findCurrentUserId(), contactId) != null; //canEditContact is true if user is a user and if contact is among list of contacts
	}
	
	
}
