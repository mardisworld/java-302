package com.users.repositories;

import java.util.List;
import org.springframework.data.repository.CrudRepository;

import com.users.beans.User;

public interface UserRepository extends CrudRepository<User, Long> {

//	List<User> findByLastName(String lastName);
	
	List<User> findByLastNameOrFirstNameOrEmailOrTwitterHandleOrFacebookUrlIgnoreCase(
			String lastName, String firstName, String email, String twitterHandle,
			String facebookUrl);

	List<User> findByEmail(String email);

	List<User> findAllByOrderByFirstNameAscLastNameAsc();
}
