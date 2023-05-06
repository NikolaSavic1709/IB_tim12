package com.ib.service.users.interfaces;

import com.ib.DTO.AccountActivationDTO;
import com.ib.exception.InvalidActivationResourceException;
import com.ib.exception.MailSendingException;
import com.ib.exception.SMSSendingException;
import com.ib.exception.UserActivationExpiredException;
import com.ib.model.users.EndUser;
import com.ib.model.users.User;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;

import java.io.UnsupportedEncodingException;

public interface IUserActivationService {

    void create(EndUser user, String userActivationType) throws MailSendingException, SMSSendingException;
    void activate(AccountActivationDTO accountActivationDTO) throws EntityNotFoundException, UserActivationExpiredException, InvalidActivationResourceException;
}
