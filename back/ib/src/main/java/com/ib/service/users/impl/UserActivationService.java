package com.ib.service.users.impl;

import com.ib.DTO.AccountActivationDTO;
import com.ib.exception.InvalidActivationResourceException;
import com.ib.exception.MailSendingException;
import com.ib.exception.SMSSendingException;
import com.ib.exception.UserActivationExpiredException;
import com.ib.model.users.EndUser;
import com.ib.model.users.User;
import com.ib.model.users.UserActivation;
import com.ib.repository.users.IEndUserRepository;
import com.ib.repository.users.IUserActivationRepository;
import com.ib.service.EndUserService;
import com.ib.service.base.impl.JPAService;
import com.ib.service.users.interfaces.IUserActivationService;
import com.ib.service.users.interfaces.IUserService;
import com.sendgrid.*;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.Random;

@Service
public class UserActivationService extends JPAService<UserActivation> implements IUserActivationService {

    @Value("${spring.sendgrid.api-key}")
    private String sendGridAPIKey;

    @Value("${angular.path}")
    private String angularPath;

    @Value("${TWILIO_ACCOUNT_SID}")
    private String ACCOUNT_SID;

    @Value("${TWILIO_AUTH_TOKEN}")
    private String AUTH_TOKEN;

    @Value("${TWILIO_OUTGOING_SMS_NUMBER}")
    private String OUTGOING_SMS_NUMBER;

    private final IUserActivationRepository userActivationRepository;
    private final IEndUserRepository userRepository;

    private static final String EMAIL_SENDER = "jnizvodno@gmail.com";

    @Autowired
    public UserActivationService(IUserActivationRepository userActivationRepository, IEndUserRepository userRepository) {
        this.userActivationRepository = userActivationRepository;
        this.userRepository = userRepository;
    }

    @PostConstruct
    private void setup(){
        Twilio.init(ACCOUNT_SID,AUTH_TOKEN);
    }

    @Override
    protected JpaRepository<UserActivation, Integer> getEntityRepository() {
        return userActivationRepository;
    }

    @Override
    public void create(EndUser user, String userActivationType) throws MailSendingException, SMSSendingException {
        Integer activationToken = generateToken();

        UserActivation userActivation = new UserActivation( user, LocalDateTime.now() , LocalDateTime.now().plusMinutes(5), activationToken);

        userActivationRepository.deleteAllByUser(user);
        save(userActivation);

        if (userActivationType.equals("email")){
            try {
                sendMail(user, activationToken);
            } catch (IOException | MailSendingException e) {
                throw new MailSendingException("Error while sending email");
            }
        }
        else {
            try{
                sendSMS(user,activationToken);
            } catch (Exception e) {
                throw new SMSSendingException("Error while sending sms");
            }
        }

    }

    public void sendSMS(EndUser user, Integer activationToken){
        Message message = Message.creator(new PhoneNumber(user.getTelephoneNumber()),
                new PhoneNumber(OUTGOING_SMS_NUMBER),
                constructSMSMessage(activationToken,user)).create();
        System.out.println(message.getStatus().toString());
    }

    private String constructSMSMessage(Integer token, EndUser user)  {
        return "Hello " + user.getName() + ","
                + "Your IB activation code is: " + token;
    }


    public void sendMail(EndUser user, Integer activationToken) throws IOException, MailSendingException {
        Email from = new Email(EMAIL_SENDER, "IB Support");
        String subject = "Account activation";
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
    private String constructEmailContent(Integer token, EndUser user)  {
        String url = angularPath + "/account-activated?token=" + token;
        return "<p>Hello " + user.getName() + ",</p>"
                + "<p>You have requested to activate your account.</p>"
                + "<p>Click the link below to activate:</p>"
                + "<p><b><a href=\"" + url + "\">Activate account</a></b></p>"
                + "<p>Or use activation code: <b>" + token + "</b></p>";
    }

    @Override
    public void activate(AccountActivationDTO accountActivationDTO) throws EntityNotFoundException, UserActivationExpiredException, InvalidActivationResourceException {
        UserActivation userActivation = getByToken(accountActivationDTO.getActivationCode());

        if (userActivation == null) {
            throw new EntityNotFoundException("Activation with entered id does not exist!");
        }

        String activationResource = accountActivationDTO.getActivationResource();
        boolean isValidActivationResource = accountActivationDTO.getActivationType().equals("email")
                ? userActivation.getUser().getEmail().equals(activationResource)
                : userActivation.getUser().getTelephoneNumber().equals(activationResource);

        if (!isValidActivationResource) {
            throw new InvalidActivationResourceException("Invalid " + accountActivationDTO.getActivationType()+" activation");
        }

        if (userActivation.getExpiryDate().isBefore(LocalDateTime.now()))
            throw new UserActivationExpiredException("Activation expired. Register again!");

        userActivation.getUser().setEnabled(true);
        userRepository.save(userActivation.getUser());
        userActivationRepository.deleteAllByUser(userActivation.getUser());
    }

    private UserActivation getByToken(Integer token) {
        return userActivationRepository.findByToken(token).orElse(null);
    }
}
