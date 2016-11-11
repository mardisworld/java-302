package com.users.security;


import static com.users.security.Role.ROLE_USER;
import static com.users.security.Role.ROLE_ADMIN;
import static org.springframework.security.core.context.SecurityContextHolder.getContext;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import com.users.beans.User;
import com.users.repositories.ContactRepository;
import com.users.repositories.UserRepository;



//from Spring documentations @ https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/stereotype/Service.html
@Service //"an operation offered as an interface that stands alone in the model, with no encapsulated state." In simpler terms, this allows for "scanning the project" for annotating classes at service layer level. All business logic should be in Service classes. 
public class PermissionService {

	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private ContactRepository contactRepo;
	
	
	//method was changed to AbstractAuthenticationToken in step5, #20&21
	private AbstractAuthenticationToken getToken() { //method that returns type UsernamePasswordAuthenticationToken
		return (AbstractAuthenticationToken) 
				getContext().getAuthentication(); //getContext() obtains the current SecurityContext.; getAuthentication() obtains the currently authenticated principal, or an authentication request token.

}
	
	public User findCurrentUser() {	//commented out in step5, #22
		List<User> users = userRepo.findByEmail(getToken().getName());//UserRepository should be userRepo
		return users != null && !users.isEmpty() ? users.get(0) : new User();
	}

//difference between findCurrentUser and find CurrentUserId is that one CurrentEmail and getCurrentUser
//is that findCurrentUser creates a list of users' names (found by emails) and returns a new user	
//find CurrentUserId also creates a list of users' names (found by emails) and returns current userId(-1) ?? Not sure what this is, really.	

	public long findCurrentUserId() {		//replaced with method below in step5, #22
		List<User> users = userRepo.findByEmail(getToken().getName());//UserRepository should be userRepo
		return users != null && !users.isEmpty() ? users.get(0).getId() : -1;//active=false?
	}

	public String getCurrentEmail() {	//in step6, #7
		return getToken().getName();//This member function returns the token at nToken index in a string where a token is separated by the delimiter specified by chDelimiter.??
	}//Tokens are the smallest unit of Program There is Five Types of Tokens
//	1. Reserved Keywords – Reserved keywords are java tokens with predefined meaning. Java has 60 reserved keywords.
//	2. Identifiers – Identifiers are java tokens designed and decided by the java programmer. Examples for java tokens namely identifiers are: name for the class, name for members of the class, and temporary variables in class methods.
//	3. Literals – Literals are java tokens containing set of characters. Literals are used to represent a constant that has to be stored in a variable.
//	4. Operators – Operators are java tokens containing a special symbol and predefined meaning in Java. Operators can be used with one or more operands to achieve a result.
//	5. Separators – Separators are java tokens that are used to divide as well as arrange codes in group.
//	I don't know what kind of token this is getName=email?
	

	public boolean hasRole(Role role) { //takes in user's role, returns true/false
		for (GrantedAuthority ga : getToken().getAuthorities()) { //getAuthorities is getting Authorities on Role? ! GrantedAuthority represents an authority granted to an Authentication object
			if (role.toString().equals(ga.getAuthority())) { //if role.toString() is equal to (ga.getAuthority()))
				return true;
			}
		}
		return false;
	}


	public boolean canAccessUser(long userId) { //method that returns boolean true/false if can edit user
		return hasRole(ROLE_ADMIN) || (hasRole(ROLE_USER) && findCurrentUserId() == userId); //returns true if user is an ADMIN or if current userId == userID
	}

	public boolean canEditContact(long contactId) {
		return hasRole(ROLE_USER) && contactRepo.findByUserIdAndId(findCurrentUserId(), contactId) != null; //canEditContact is true if user is a user and if contact is among list of contacts
	}
	
}
