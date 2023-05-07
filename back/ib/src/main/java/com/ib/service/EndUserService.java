package com.ib.service;

import com.ib.DTO.ForgotPasswordDTO;
import com.ib.DTO.ResetPasswordDTO;
import com.ib.exception.*;
import com.ib.model.users.*;
import com.ib.repository.users.IEndUserRepository;
import com.ib.repository.users.IPasswordResetTokenRepository;
import com.ib.repository.users.IUserRepository;
import com.ib.service.users.interfaces.IUserActivationService;
import com.sendgrid.*;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Random;


@Service
public class EndUserService {

    @Value("${spring.sendgrid.api-key}")
    private String sendGridAPIKey;

    @Value("${TWILIO_ACCOUNT_SID}")
    private String ACCOUNT_SID;

    @Value("${TWILIO_AUTH_TOKEN}")
    private String AUTH_TOKEN;

    @Value("${TWILIO_OUTGOING_SMS_NUMBER}")
    private String OUTGOING_SMS_NUMBER;

    @Value("${EMAIL_SENDER}")
    private String EMAIL_SENDER;
    private final AuthorityService authorityService;
    private final PasswordEncoder passwordEncoder;
    private final IEndUserRepository endUserRepository;

    private final IUserRepository userRepository;
    private final IPasswordResetTokenRepository passwordResetTokenRepository;
    private final IUserActivationService userActivationService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public EndUserService(AuthorityService authorityService, PasswordEncoder passwordEncoder, IEndUserRepository endUserRepository,
                          IUserRepository userRepository, IPasswordResetTokenRepository passwordResetTokenRepository, IUserActivationService userActivationService) {
        this.authorityService = authorityService;
        this.passwordEncoder = passwordEncoder;
        this.endUserRepository = endUserRepository;
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.userActivationService = userActivationService;
        this.bCryptPasswordEncoder = new BCryptPasswordEncoder();

    }

    public boolean checkUserEnabled(String email) {
        User user = userRepository.findByEmail(email).orElseGet(null);;
        Boolean enabled = userRepository.findEnabledByEmail(email);
        return user != null && enabled;
    }

    public EndUser save(EndUser endUser) { return endUserRepository.save(endUser);};


    public void register(EndUser newUser, String userActivationType) throws MailSendingException, SMSSendingException, PhoneNumberAlreadyUsedException, EmailAlreadyUsedException {
        Authority authority = authorityService.findByName("END_USER");
        newUser.setAuthority(authority);
        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));

        newUser.setEnabled(false);
        checkIFUserResourcesAreUsed(newUser);
        save(newUser);

        userActivationService.create(newUser, userActivationType);
    }

    public void checkIFUserResourcesAreUsed(EndUser newUser) throws EmailAlreadyUsedException, PhoneNumberAlreadyUsedException {
        EndUser userByEmail = endUserRepository.findByEmail(newUser.getEmail());
        Boolean enabled = endUserRepository.findEnabledByEmail(newUser.getEmail());
        if (userByEmail != null && enabled) {
            throw new EmailAlreadyUsedException("Email already used exception");
        }
        if (userByEmail != null) {
            endUserRepository.deleteByEmail(userByEmail.getEmail());
        }

        EndUser userByTelephoneNumber = endUserRepository.findByTelephoneNumber(newUser.getTelephoneNumber());
        Boolean enabledByNumber = endUserRepository.findEnabledByPhoneNumber(newUser.getTelephoneNumber());
        if (userByTelephoneNumber != null && enabledByNumber) {
            throw new PhoneNumberAlreadyUsedException("Phone number already used exception");
        }
        if (userByTelephoneNumber != null) {
            endUserRepository.deleteByTelephoneNumber(userByTelephoneNumber.getTelephoneNumber());
        }
    }

    public void sendMFAToken(String email, String mfaType) throws MailSendingException, SMSSendingException {
        Integer mfaToken = generateToken();

        User user = userRepository.findByEmail(email).orElseGet(null);
        user.setMFAToken(mfaToken);
        user.setMFATokenExpiryDate(LocalDateTime.now().plusMinutes(5));
        user = userRepository.save(user);
        if (mfaType.equals("email")){
            try {
                sendMail(user, mfaToken);
            } catch (IOException | MailSendingException e) {
                throw new MailSendingException("Error while sending email");
            }
        }
        else {
            try{
                sendSMS(user,mfaToken);
            } catch (Exception e) {
                throw new SMSSendingException("Error while sending sms");
            }
        }
    }

    public void sendSMS(User user, Integer mfaToken){
        Message message = Message.creator(new PhoneNumber(user.getTelephoneNumber()),
                new PhoneNumber(OUTGOING_SMS_NUMBER),
                constructSMSMessage(mfaToken,user)).create();
    }

    private String constructSMSMessage(Integer token, User user)  {
        return "Hello " + user.getName() + ","
                + "Your MFA code is: " + token;
    }


    public void sendMail(User user, Integer activationToken) throws IOException, MailSendingException {
        Email from = new Email(EMAIL_SENDER, "IB Support");
        String subject = "MFA sign in";
        Email to = new Email(user.getEmail());
        Content content = new Content("text/html", constructEmailContent(activationToken, user));
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(sendGridAPIKey);
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());
        Response response = sg.api(request);
        int statusCode = response.getStatusCode();
        if (statusCode==400 || statusCode==404){
            throw new MailSendingException("Error while sending email");
        }

    }
    private Integer generateToken() {
        Random rnd = new Random();
        return 100000 + rnd.nextInt(900000);
    }
    private String constructEmailContent(Integer token, User user)  {
        return "<p>Hello " + user.getName() + ",</p>"
                + "<p>Your MFA code is: <b>" + token + "</b></p>";
    }

    public void checkMFA(String email, Integer token) throws InvalidUserException, UserMFAExpiredException {
        User user = userRepository.findByEmail(email).orElseGet(null);
        if (user==null)
            throw new InvalidUserException("Invalid user");
        if (!user.getMFAToken().equals(token))
            throw new InvalidUserException("Invalid mfa token");
        if (user.getMFATokenExpiryDate().isBefore(LocalDateTime.now()))
            throw new UserMFAExpiredException("MFA token expired. Try again!");

        user.setMFATokenExpiryDate(null);
        user.setMFAToken(null);
        userRepository.save(user);
    }

    public void forgotPassword(ForgotPasswordDTO forgotPasswordDTO) throws MailSendingException, SMSSendingException, EntityNotFoundException {

        if (forgotPasswordDTO.getActivationType().equals("email")){
            User user = userRepository.findByEmail(forgotPasswordDTO.getActivationResource()).orElseGet(null);
            PasswordResetToken token = updatePasswordResetToken(user);
            try {
                sendForgotPasswordMail(user, token.getToken());
            } catch (IOException | MailSendingException e) {
                throw new MailSendingException("Error while sending email");
            }
        }
        else {
            EndUser user = endUserRepository.findByTelephoneNumber(forgotPasswordDTO.getActivationResource());
            PasswordResetToken token = updatePasswordResetToken(user);
            try{
                sendForgotPasswordSMS(user,token.getToken());
            } catch (Exception e) {
                throw new SMSSendingException("Error while sending sms");
            }
        }
    }

    private PasswordResetToken updatePasswordResetToken(User user) {
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setToken(generateToken());
        token.setTokenExpiryDate(LocalDateTime.now().plusMinutes(5));

        passwordResetTokenRepository.deleteAllByUser(user);
        return passwordResetTokenRepository.save(token);
    }

    public void sendForgotPasswordSMS(User user, Integer mfaToken){
        Message message = Message.creator(new PhoneNumber(user.getTelephoneNumber()),
                new PhoneNumber(OUTGOING_SMS_NUMBER),
                constructForgotPasswordSMSMessage(mfaToken,user)).create();
    }

    private String constructForgotPasswordSMSMessage(Integer token, User user)  {
        return "Hello " + user.getName() + ","
                + "Your code for password reset is: " + token;
    }


    public void sendForgotPasswordMail(User user, Integer activationToken) throws IOException, MailSendingException {
        Email from = new Email(EMAIL_SENDER, "IB Support");
        String subject = "Forgot password";
        Email to = new Email(user.getEmail());
        Content content = new Content("text/html", constructForgotPasswordEmailContent(activationToken, user));
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(sendGridAPIKey);
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());
        Response response = sg.api(request);
        int statusCode = response.getStatusCode();
        if (statusCode==400 || statusCode==404){
            throw new MailSendingException("Error while sending email");
        }

    }
    private String constructForgotPasswordEmailContent(Integer token, User user)  {
        return "<p>Hello " + user.getName() + ",</p>"
                + "<p>Your code for password reset is: <b>" + token + "</b></p>";
    }

    public void resetPassword(ResetPasswordDTO resetPasswordDTO) throws IncorrectCodeException, CodeExpiredException, EntityNotFoundException {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(resetPasswordDTO.getCode()).orElse(null);
        if (passwordResetToken == null) {
            throw new IncorrectCodeException("Code is expired or not correct!");
        }

        User user;
        if (resetPasswordDTO.getActivationType().equals("email")){
            user = userRepository.findByEmail(resetPasswordDTO.getActivationResource()).orElseGet(null);
        }
        else {
            user = userRepository.findByTelephoneNumber(resetPasswordDTO.getActivationResource());
        }

        if (user==null || user!=passwordResetToken.getUser()){
            throw new IncorrectCodeException("Code is expired or not correct!");
        }
        if (passwordResetToken.getTokenExpiryDate().isBefore(LocalDateTime.now())) {
            throw new CodeExpiredException("Code is expired or not correct!");
        }
        updatePassword(user, resetPasswordDTO.getNewPassword());
    }

    private void updatePassword(User user, String newPassword) {
        user.setPassword(bCryptPasswordEncoder.encode(newPassword));
        userRepository.save(user);

        passwordResetTokenRepository.deleteAllByUser(user);
    }
}
