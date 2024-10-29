package com.serverless.lambda.metal.prices.mail;

import com.serverless.lambda.metal.prices.configuration.SsmParamsProvider;
import com.serverless.lambda.metal.prices.service.MetalExchangeService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.ISpringTemplateEngine;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest;
import software.amazon.awssdk.services.sesv2.model.SesV2Exception;

public class EmailSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailSender.class);
    private static final DateTimeFormatter DATE_FORMATTER_DD_MM_YYYY = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final Region region;
    private final String sender;
    private final List<String> recipients;
    private final String base;
    private final ISpringTemplateEngine emailTemplateEngine;
    private final MetalExchangeService exchangeService;

    public EmailSender(
            String sender,
            SsmParamsProvider paramsProvider,
            ISpringTemplateEngine emailTemplateEngine,
            MetalExchangeService exchangeService) {
        this.region = paramsProvider.getRegion();
        this.sender = sender;
        this.recipients = Arrays.stream(paramsProvider.getRecipients()).toList();
        this.base = paramsProvider.getBase();
        this.emailTemplateEngine = emailTemplateEngine;
        this.exchangeService = exchangeService;
    }

    public void sendMail() {
        SesV2Client sesv2Client = SesV2Client.builder().region(region).build();
        send(sesv2Client);
    }

    private String createMailBody(String reportDate) {
        Map<String, BigDecimal> rates = exchangeService.getMetalRates();

        Context ctx = new Context(Locale.GERMANY);
        ctx.setVariable("name", "Ivana & Branislav");
        ctx.setVariable("reportDate", reportDate);
        ctx.setVariable("metalRates", rates);
        ctx.setVariable("currency", base);

        return emailTemplateEngine.process("email-template.html", ctx);
    }

    private void send(SesV2Client client) {
        String reportDate = LocalDate.now().format(DATE_FORMATTER_DD_MM_YYYY);
        String mailBody = createMailBody(reportDate);
        String subject = String.format("Actual metal prices for %s", reportDate);

        SendEmailRequest emailRequest = SendEmailRequest.builder()
                .destination(d -> d.toAddresses(recipients))
                .content(c -> c.simple(m -> m.subject(s -> s.data(subject)).body(b -> b.html(d -> d.data(mailBody)))))
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
