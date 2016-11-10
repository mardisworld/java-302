package com.users.repositories;
//entire file made in step5, #27
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import com.users.beans.UserRole;

public interface UserRoleRepository extends CrudRepository<UserRole, Long> {

	List<UserRole> findByUserId(long userId);
	
}
