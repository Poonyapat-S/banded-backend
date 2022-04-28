package com.javainuse.classes;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static java.sql.Types.NULL;

@Service
@AllArgsConstructor
public class PostService {
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostInteractionService postInteractionService;

    @Autowired
    private BlockService blockService;

    public List<Post> loadByUsername(String username) {
        List<Post> posts = new ArrayList<>();
        try {
            posts = postRepository.findByUser(userRepository.findByUserName(username).orElseThrow(() -> (new UsernameNotFoundException(String.format("", "")))));
        }
        catch(Exception e) {
            posts = new ArrayList<>();
        }
        return posts;
    }

    public List<Post> loadByTopicName(String topicName) {
        List<Post> posts;
        try {
            posts = postRepository.findByTopic(topicRepository.findByTopicName(topicName).orElseThrow(Exception::new));
        }
        catch (Exception e) {
            posts = new ArrayList<>();
        }
        return posts;
    }

    public List<Post> anonymizeName(List<Post> posts){
        List<Post> toReturn = posts;
        for(int i = 0; i < toReturn.size(); i++){
            if (toReturn.get(i).getIsAnon()){
                //iterates through a list of fetched posts and changes all anonymous tagged names to anonymous
                toReturn.get(i).setUserName("Anonymous");
            }
        }
        return toReturn;
    }
    public List<Post> anonymizeForUsers(String username) {
        List<Post> posts = new ArrayList<>();
        try {
            posts = postRepository.findByUserAndIsAnonFalse(userRepository.findByUserName(username).orElseThrow(()
                    -> (new UsernameNotFoundException(String.format("", "")))));
        } catch (Exception e) {
            posts = new ArrayList<>();
        }
        return posts;
    }

    public void sortByDateTimeDesc(List<Post> allPosts) {
        Comparator<Post> dateComparator = Comparator.comparing(Post::getPostTime);
        Collections.sort(allPosts,dateComparator);
        Collections.reverse(allPosts);
    }

    public List<Post> removeDup(List<Post> allPosts) {
        return allPosts.stream().distinct().collect(Collectors.toList());
    }

    public String deletePost(Integer postID) {
        Post delPost;

        try {
            delPost = postRepository.findByPostID(postID).orElseThrow();
        } catch (Exception e) {
            System.out.println("ERROR unable to retrieve Post with postID: ["+postID+"] in PostService.deletePost(Integer)");
            return "Post deletion failure";
        }

        long replyCount = postRepository.countByParentPostID(postID);
        if (replyCount > 0) {
            List<Post> replies = new ArrayList<>();

            try {
                replies = postRepository.findByParentPostID(postID);
            } catch (Exception e) {
                System.out.println("ERROR unable to retrieve replies for Post with postID: ["+postID+"] in PostService.deletePost(Integer)");
            }

            for (Post rep : replies) {
                deletePost(rep.getPostID());
            }
        }

        postInteractionService.deletePostsReactions(delPost);
        postInteractionService.deletePostsSaves(delPost);
        postRepository.delete(delPost);
        System.out.println("Post with postID:["+postID+"] has been deleted");
        return "Post deleted";
    }

    /* -=- ACCOUNT DELETION METHOD -=- */
    public void deleteUsersPosts(User user) {
        List<Post> allUsersPosts = new ArrayList<>();

        try {
            allUsersPosts = postRepository.findByUser(user);
        } catch (Exception e) {
            System.out.println("ERROR retrieving posts in PostService.deleteUsersPosts - pls inspect database");
        }

        for (Post p : allUsersPosts) {
            deletePost(p);
        }
    }

    public void deletePost(Post post) {
        long replyCount = postRepository.countByParentPostID(post.getPostID());
        if (replyCount > 0) {
            List<Post> replies = new ArrayList<>();

            try {
                replies = postRepository.findByParentPostID(post.getPostID());
            } catch (Exception e) {
                System.out.println("ERROR retrieving replies via PostService.deletePost");
            }

            for (Post rep : replies) {
                /*if (rep.getUser() != post.getUser()) {
                    deletePost(rep);
                }*/
                deletePost(rep);
            }
        }

        postInteractionService.deletePostsReactions(post);
        postInteractionService.deletePostsSaves(post);
        postRepository.delete(post);
    }

    public List<Post> removeReplies(List<Post> posts){
        ListIterator<Post> iter = posts.listIterator();
        while(iter.hasNext()){
            if(iter.next().getParentPostID() != null){
                iter.remove();
            }
        }
        return posts;
    }

    public List<Post> removeBlock(List<Post> posts, User generator){
        ListIterator<Post> iter = posts.listIterator();
        while(iter.hasNext()){
            if(blockService.blockExists(generator.getUserID(), iter.next().getUserID())){
                iter.remove();
            }
        }
        return posts;
    }
}
