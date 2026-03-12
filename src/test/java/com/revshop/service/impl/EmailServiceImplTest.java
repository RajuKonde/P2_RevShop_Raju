package com.revshop.service.impl;

import com.revshop.exception.InternalServerException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EmailServiceImplTest {

    @Mock
    private ObjectProvider<JavaMailSender> mailSenderProvider;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailServiceImpl emailService;

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(emailService, "fromAddress", "no-reply@revshop.local");
        ReflectionTestUtils.setField(emailService, "mailHost", "smtp.gmail.com");
        ReflectionTestUtils.setField(emailService, "mailUsername", "sender@test.com");
    }

    @Test
    public void sendPasswordResetEmail_throwsHelpfulMessageWhenHostMissing() {
        ReflectionTestUtils.setField(emailService, "mailHost", "");

        InternalServerException ex = assertThrows(
                InternalServerException.class,
                () -> emailService.sendPasswordResetEmail("buyer@test.com", "http://localhost/reset", 15)
        );

        assertEquals("SMTP host is not configured. Set MAIL_HOST or spring.mail.host.", ex.getMessage());
        verifyNoInteractions(mailSenderProvider, mailSender);
    }

    @Test
    public void sendPasswordResetEmail_usesMailUsernameAsFallbackFromAddress() {
        when(mailSenderProvider.getIfAvailable()).thenReturn(mailSender);

        emailService.sendPasswordResetEmail("buyer@test.com", "http://localhost/reset", 15);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage message = messageCaptor.getValue();
        assertEquals("sender@test.com", message.getFrom());
        assertEquals("buyer@test.com", message.getTo()[0]);
    }

    @Test
    public void sendPasswordResetEmail_throwsHelpfulMessageForAuthenticationFailure() {
        when(mailSenderProvider.getIfAvailable()).thenReturn(mailSender);
        doThrow(new MailAuthenticationException("bad credentials")).when(mailSender).send(any(SimpleMailMessage.class));

        InternalServerException ex = assertThrows(
                InternalServerException.class,
                () -> emailService.sendPasswordResetEmail("buyer@test.com", "http://localhost/reset", 15)
        );

        assertEquals(
                "SMTP authentication failed. Check MAIL_USERNAME, MAIL_PASSWORD, and your app password.",
                ex.getMessage()
        );
    }
}
