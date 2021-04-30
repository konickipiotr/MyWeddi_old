package com.myweddi.api;

import com.myweddi.conf.Global;
import com.myweddi.db.ActivationRepository;
import com.myweddi.model.Activation;
import com.myweddi.user.*;
import com.myweddi.user.reposiotry.GuestRepository;
import com.myweddi.user.reposiotry.HostRepository;
import com.myweddi.user.reposiotry.UserAuthRepository;
import com.myweddi.user.reposiotry.WeddingCodeRepository;
import net.bytebuddy.utility.RandomString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@RestController
@RequestMapping("/api/registration")
public class RegistrationAPIController {

    private final UserAuthRepository userAuthRepository;
    private final HostRepository hostRepository;
    private final WeddingCodeRepository weddingCodeRepository;
    private final GuestRepository guestRepository;
    private final GiftService giftService;
    private final JavaMailSender javaMailSender;
    private final ActivationRepository activationRepository;
    private final PasswordEncoder passwordEncoder;

    public final int ACTIVATION_CODE_LENGTH = 40;

    @Autowired
    public RegistrationAPIController(UserAuthRepository userAuthRepository, HostRepository hostRepository, WeddingCodeRepository weddingCodeRepository, GuestRepository guestRepository, GiftService giftService, JavaMailSender javaMailSender, ActivationRepository activationRepository, PasswordEncoder passwordEncoder) {
        this.userAuthRepository = userAuthRepository;
        this.hostRepository = hostRepository;
        this.weddingCodeRepository = weddingCodeRepository;
        this.guestRepository = guestRepository;
        this.giftService = giftService;
        this.javaMailSender = javaMailSender;
        this.activationRepository = activationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping
    public ResponseEntity<Long> addHost( @RequestBody RegistrationForm rf){
        UserAuth userAuth = this.userAuthRepository.findByUsername(rf.getUsername());

        if(userAuth != null){
            return new ResponseEntity<>(HttpStatus.FOUND);
        }
        userAuth = new UserAuth(rf);
        userAuth.setPassword(passwordEncoder.encode(rf.getPassword()));
        userAuth  = this.userAuthRepository.save(userAuth);

        Host host = new Host(userAuth.getId(), rf);
        this.hostRepository.save(host);
        this.giftService.newAccount(userAuth.getUsername());
        this.weddingCodeRepository.save(new WeddingCode(userAuth.getId(), generateWeddingCode()));

        String activationCode = RandomString.make(ACTIVATION_CODE_LENGTH);
        this.activationRepository.save(new Activation(userAuth.getId(), activationCode));
        sendActivationLink(host.getBrideemail(), activationCode);
        sendActivationLink(host.getGroomemail(), activationCode);
        rf.setUserid(userAuth.getId());

        return new ResponseEntity<>(userAuth.getId(), HttpStatus.OK);
    }

    @PostMapping("/guest")
    public ResponseEntity<Long> addGuest( @RequestBody RegistrationForm rf){
        UserAuth userAuth = this.userAuthRepository.findByUsername(rf.getUsername());

        if(userAuth != null){
            return  new ResponseEntity<>(HttpStatus.FOUND);
        }

        if(!this.weddingCodeRepository.existsByWeddingcode(rf.getWeddingcode())){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        WeddingCode weddingCode = this.weddingCodeRepository.findByWeddingcode(rf.getWeddingcode());

        userAuth = new UserAuth(rf);
        userAuth.setPassword(passwordEncoder.encode(rf.getPassword()));
        this.userAuthRepository.save(userAuth);
        this.guestRepository.save(new Guest(userAuth.getId(), weddingCode.getWeddingid(), userAuth.getUsername(), rf.getFirstname(), rf.getLastname()));

        String activationCode = RandomString.make(ACTIVATION_CODE_LENGTH);
        this.activationRepository.save(new Activation(userAuth.getId(), activationCode));
        sendActivationLink(userAuth.getUsername(), activationCode);

        return new ResponseEntity<>(userAuth.getId(), HttpStatus.OK);
    }

    @PostMapping("/activation")
    public ResponseEntity<Void> activeAccount(@RequestBody String activationCode){

        if(!this.activationRepository.existsByActivationcode(activationCode))
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        Activation activationcode = this.activationRepository.findByActivationcode(activationCode);
        UserAuth user = this.userAuthRepository.findById(activationcode.getUserid()).get();
        user.setStatus(UserStatus.FIRSTLOGIN);
        this.userAuthRepository.save(user);
        this.activationRepository.deleteById(activationcode.getUserid());

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/sendagain")
    public ResponseEntity<Void> sendActivationCodeAgain(@RequestBody Long userid){
        Optional<Activation> optionalActivation = this.activationRepository.findById(userid);
        if(optionalActivation.isEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        Activation activation = optionalActivation.get();
        String newActivationCode = RandomString.make(ACTIVATION_CODE_LENGTH);
        activation.setActivationcode(newActivationCode);
        this.activationRepository.save(activation);


        Optional<UserAuth> oUser = this.userAuthRepository.findById(userid);
        if(oUser.isEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        UserAuth user = oUser.get();
        if(!user.getRole().equals("HOST"))
            sendActivationLink(user.getUsername(), newActivationCode);
        else {
            Host host = this.hostRepository.findById(userid).get();
            sendActivationLink(host.getBrideemail(), newActivationCode);
            sendActivationLink(host.getGroomemail(), newActivationCode);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    private void sendActivationLink(String email, String activationCode){

        String link = Global.domain + "/registration/activation/" + activationCode;

        String message = "Witaj w MyWeddi!\n\n" +
                "Kliknij w link poniżej aby aktywować konto \n" + link;

        SimpleMailMessage emailMessage = new SimpleMailMessage();
        emailMessage.setTo(email);
        emailMessage.setSubject("MyWeddi - aktywacja");
        emailMessage.setText(message);

        javaMailSender.send(emailMessage);
    }

    private String generateWeddingCode(){
        String generatedString = RandomString.make(8);
        while (this.weddingCodeRepository.existsByWeddingcode(generatedString)){
            generatedString = RandomString.make(8);
        }
        return generatedString;
    }
}
