package com.myweddi.api.posts;

import com.myweddi.model.Comment;
import com.myweddi.model.Post;
import com.myweddi.utils.ListWrapper;
import com.myweddi.view.PostView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/post")
public class PostAPIController {

    @Autowired
    private PostService postService;

    @GetMapping("/{weddingid}/{page}")
    public ResponseEntity<ListWrapper<PostView>> getLastPostList(@PathVariable("weddingid") Long weddingid, @PathVariable("page") int page){
        return  new ResponseEntity<ListWrapper<PostView>>(postService.getPostFromPage(page), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Long> newPostInDb(@RequestBody Post post){
        Long postid = postService.newPostInDb(post);
        return  new ResponseEntity<Long>(postid, HttpStatus.CREATED);
    }

    @PostMapping(path = "/{userid}/{postid}")
    public ResponseEntity<Long> savePostFiles(@PathVariable("userid") Long userid, @PathVariable("postid") Long postid, MultipartFile[] images){
        postService.savePostFiles(postid, userid, images);
        return  new ResponseEntity<Long>(postid, HttpStatus.CREATED);
    }

    @PostMapping("/addcomment")
    public void addComment(@RequestBody Comment comment){
        this.postService.addComment(comment);

    }
}