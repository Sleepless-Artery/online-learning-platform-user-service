package org.sleepless_artery.user_service.service;


public interface EmailChangeService {

    void requestEmailChange(Long userId, String oldEmailAddress, String newEmailAddress);

    void confirmEmailAddressChange(String oldEmailAddress, String newEmailAddress);
}
