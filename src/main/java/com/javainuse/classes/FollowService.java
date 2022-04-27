package com.javainuse.classes;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;


@AllArgsConstructor
@Service
public class FollowService {
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private TopicRepository topicRepository;
	@Autowired
	private UserFollowerRepository userFollowerRepository;
	@Autowired
	private TopicFollowerRepository topicFollowerRepository;
	
	public List<User> retrieveFollowedUsers(Integer userID) {
		List<UserFollower> userFollowerObjects;
		List<User> users = new ArrayList<>();
		try {
			//retrieve all relevant rows from UserFollower table as a List of UserFollower objects
			userFollowerObjects = userFollowerRepository.findByFollowingID(userID);
		} catch (Exception e) {
			System.out.println("userFollowerRepository.findByFollowingID error in FollowService.retrieveFollowedUsers");
			userFollowerObjects = new ArrayList<>();
		}
		if (userFollowerObjects.size() > 0) {
			for (UserFollower u : userFollowerObjects) {
				try {
					//attempt to grab each User via followedID from the retrieved UserFollower objects
					Optional<User> opUser = userRepository.findByUserID(u.getFollowedID());
					if (opUser.isPresent()) {
						users.add(opUser.get()); //if the Optional object actually holds a user, add it to users List
					} else {
						System.out.println("Empty User object returned in FollowService.retrieveFollowedUsers");
					}
				} catch (Exception e) {
					System.out.println("findByUserID failure in FollowService.retrieveFollowedUsers");
				}
			}
		}
		return users;
	}
	
	//retrieves a List of all the topics that the user associated with given userID follows
	public List<Topic> retrieveFollowedTopics(Integer userID) {
		List<TopicFollower> topicFollowerObjects;
		List<Topic> topics = new ArrayList<>();
		try {
			//retrieve all relevant rows from TopicFollower table as a List of TopicFollower objects
			topicFollowerObjects = topicFollowerRepository.findByUserID(userID);
		} catch (Exception e) {
			System.out.println("topicFollowerRepository.findByUserID error in FollowService.retrieveFollowedUsers");
			topicFollowerObjects = new ArrayList<>();
		}
		if (topicFollowerObjects.size() > 0) {
			for (TopicFollower t : topicFollowerObjects) {
				try {
					//attempt to grab every Topic via each topicID from the retrieved TopicFollower objects
					Optional<Topic> opTopic = topicRepository.findByTopicID(t.getTopicID());
					if (opTopic.isPresent()) {
						topics.add(opTopic.get()); //if the Optional object actually holds a topic, add it to topics List
					} else {
						System.out.println("Empty Topic object returned in FollowService.retrieveFollowedTopics");
					}
				} catch (Exception e) {
					System.out.println("findByUserID failure in FollowService.retrieveFollowedUsers");
				}
			}
		}
		return topics;
	}
	
	/* -=- ACCOUNT DELETION METHODS -=- */
	public void deleteUsersUserFollows(User user) {
		List<UserFollower> allUserFollowerObjects = new ArrayList<>();
		
		//grabbing all UserFollower objects with this user associated, whether followed or following
		try {
			allUserFollowerObjects = userFollowerRepository.findByFollowingIDOrFollowedID(user.getUserID(), user.getUserID());
		} catch (Exception e) {
			System.out.println("ERROR retrieving UserFollower objects in FollowService.deleteUsersUserFollows");
		}
		
		int totalFollowings = 0;
		int totalFollowers = 0;
		for (UserFollower uf : allUserFollowerObjects) {
			if (Objects.equals(uf.getFollowingID(), user.getUserID())) totalFollowings++;
			else totalFollowers++;
			userFollowerRepository.delete(uf);
		}
		System.out.println("["+totalFollowings+"] total UserFollower objects (following) deleted for ["+user.getUsername()+"]");
		System.out.println("["+totalFollowers+"] total UserFollower objects (followed) deleted for ["+user.getUsername()+"]");
	}
	
	public void deleteUsersTopicFollows(User user) {
		List<TopicFollower> allTopicFollows = new ArrayList<>();
		
		//grabbing all UserFollower objects with this user associated, whether followed or following
		try {
			allTopicFollows = topicFollowerRepository.findByUserID(user.getUserID());
		} catch (Exception e) {
			System.out.println("ERROR retrieving TopicFollower objects in FollowService.deleteUsersTopicFollows");
		}
		
		int totalFollows = allTopicFollows.size();
		for (TopicFollower tf : allTopicFollows) {
			topicFollowerRepository.delete(tf);
		}
		System.out.println("["+totalFollows+"] total topic follows deleted for ["+user.getUsername()+"]");
	}
}
