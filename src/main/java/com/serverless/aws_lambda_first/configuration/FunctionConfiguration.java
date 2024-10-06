package com.serverless.aws_lambda_first.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.*;

import java.util.function.Supplier;

@Configuration
public class FunctionConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(FunctionConfiguration.class);
    public static final String BODY_HTML = """
            <html>
                <head></head>
                <body>
                    <h1>Hello!</h1>
                    <p> See the list of customers.</p>
                </body>
            </html>""";

    @Value("${custom.mail.recipient}")
    private String recipient;
    @Value("${custom.mail.sender}")
    private String sender;

    @Bean
    public Supplier<Void> reverse() {
        Region region = Region.EU_CENTRAL_1;
        SesV2Client sesv2Client = SesV2Client.builder()
                .region(region)
                .build();

        send(sesv2Client);

        return () -> null;
    }

    private void send(SesV2Client client) {
        Destination destination = Destination.builder()
                .toAddresses(recipient)
                .build();

        Content content = Content.builder()
                .data(BODY_HTML)
                .build();

        Content sub = Content.builder()
                .data("test")
                .build();

        Body body = Body.builder()
                .html(content)
                .build();

        Message msg = Message.builder()
                .subject(sub)
                .body(body)
                .build();

        EmailContent emailContent = EmailContent.builder()
                .simple(msg)
                .build();

        SendEmailRequest emailRequest = SendEmailRequest.builder()
                .destination(destination)
                .content(emailContent)
                .fromEmailAddress(sender)
                .build();

        try {
            LOGGER.info("Attempting to send an email through Amazon SES "
                    + "using the AWS SDK for Java...");
            client.sendEmail(emailRequest);
            LOGGER.info("email was sent");

        } catch (SesV2Exception e) {
            LOGGER.error(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }
}
