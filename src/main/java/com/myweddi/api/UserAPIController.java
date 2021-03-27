package com.myweddi.api;

import com.myweddi.user.Host;
import com.myweddi.user.User;
import com.myweddi.user.UserAuth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/user")
public class UserAPIController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<User> getUserAuth(Principal principal){
        return userService.getUser(principal.getName());
    }

    @PostMapping("/host")
    public ResponseEntity<Host> getWeddingHostsInfo(@RequestBody Long weddingid){
        return userService.getWeddingHosts(weddingid);
    }
}