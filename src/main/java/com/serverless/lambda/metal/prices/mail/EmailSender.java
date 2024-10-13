package com.serverless.lambda.metal.prices.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.ISpringTemplateEngine;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest;
import software.amazon.awssdk.services.sesv2.model.SesV2Exception;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class EmailSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailSender.class);

    private static final DateTimeFormatter DATE_FORMATTER_DD_MM_YYYY = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final String sender;
    private final String recipient;
    private final ISpringTemplateEngine emailTemplateEngine;

    public EmailSender(String sender, String recipient, ISpringTemplateEngine emailTemplateEngine) {
        this.sender = sender;
        this.recipient = recipient;
        this.emailTemplateEngine = emailTemplateEngine;
    }

    public void sendMail() {
        Region region = Region.EU_CENTRAL_1;
        SesV2Client sesv2Client = SesV2Client.builder().region(region).build();
        send(sesv2Client);
    }

    private String createMailBody(String reportDate) {
        Context ctx = new Context(Locale.US);
        ctx.setVariable("name", "Hugo");
        ctx.setVariable("reportDate", reportDate);

        return emailTemplateEngine.process("email-template.html", ctx);
    }

    private void send(SesV2Client client) {
        String reportDate = LocalDate.now().format(DATE_FORMATTER_DD_MM_YYYY);
        String subject = String.format("Sales Report for %s", reportDate);

        SendEmailRequest emailRequest = SendEmailRequest.builder()
                .destination(d -> d.toAddresses(recipient))
                .content(
                        c -> c.simple(
                                m -> m.subject(s -> s.data(subject)).body(b -> b.html(d -> d.data(createMailBody(reportDate))))))
                .fromEmailAddress(sender)
                .build();

        try {
            LOGGER.info("Attempting to send an email through Amazon SES " + "using the AWS SDK for Java...");
            client.sendEmail(emailRequest);
            LOGGER.info("email was sent");

        } catch (SesV2Exception e) {
            LOGGER.error(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }
}
