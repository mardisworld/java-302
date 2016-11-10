package com.users.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.users.beans.User;
import com.users.beans.UserImage;
import com.users.repositories.UserImageRepository;

@Service
public class ImageService {
	
	private static final Logger log = LoggerFactory.getLogger(ImageService.class);
	
	@Autowired
	private UserImageRepository userImageRepo;
	
	
	public void saveImage(MultipartFile file, User user) {

		if (!file.isEmpty()) {
			try {
				List<UserImage> images = userImageRepo.findByUserId(user.getId());
				UserImage img = (images.size() > 0) ? images.get(0) : new UserImage(user.getId());
				img.setContentType(file.getContentType());
				img.setImage(file.getBytes());
				userImageRepo.save(img);

				log.debug("Saved Image"); //logger.debug instead of log.debug
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	public void deleteImage(User user) {
		log.debug("Removing Image");

		List<UserImage> images = userImageRepo.findByUserId(user.getId());

		for (UserImage img : images) {
			userImageRepo.delete(img);
		}
	}

}

