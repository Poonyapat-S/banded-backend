package com.javainuse.controllers;

import com.javainuse.classes.*;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.sql.*;

@RestController
@RequestMapping(path = "api/service/delete")
@AllArgsConstructor
public class DeletionController 
{
    @Autowired
    private UserService userService;
    private PostService postService;
    private PostInteractionService postInteractionService;
    private FollowService followService;
    private BlockService blockService;

    @PostMapping
    public String deleteUser(@AuthenticationPrincipal User user) {
        postInteractionService.deleteUsersReactions(user);
        postInteractionService.deleteUsersSavedPosts(user);
        followService.deleteUsersUserFollows(user);
        followService.deleteUsersTopicFollows(user);
        blockService.deleteUsersBlocks(user);
        postService.deleteUsersPosts(user);
        
        return userService.deleteByEmail(user.getEmail());
    }
}
