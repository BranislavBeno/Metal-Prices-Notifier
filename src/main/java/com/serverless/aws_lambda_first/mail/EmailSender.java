package com.serverless.aws_lambda_first.mail;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.Body;
import software.amazon.awssdk.services.sesv2.model.Content;
import software.amazon.awssdk.services.sesv2.model.Destination;
import software.amazon.awssdk.services.sesv2.model.EmailContent;
import software.amazon.awssdk.services.sesv2.model.Message;
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest;
import software.amazon.awssdk.services.sesv2.model.SesV2Exception;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmailSender {

   private static final Logger LOGGER = LoggerFactory.getLogger(EmailSender.class);

   private static final String BODY_HTML = """
         <html>
             <head></head>
             <body>
                 <h1>Hello!</h1>
                 <p> See the list of customers.</p>
             </body>
         </html>""";

   private final String sender;
   private final String recipient;

   public EmailSender(String sender, String recipient) {
      this.sender = sender;
      this.recipient = recipient;
   }

   public void sendMail() {
      Region region = Region.EU_CENTRAL_1;
      SesV2Client sesv2Client = SesV2Client.builder().region(region).build();
      send(sesv2Client);
   }

   private void send(SesV2Client client) {
      Destination destination = Destination.builder().toAddresses(recipient).build();

      Content content = Content.builder().data(BODY_HTML).build();

      Content sub = Content.builder().data("test").build();

      Body body = Body.builder().html(content).build();

      Message msg = Message.builder().subject(sub).body(body).build();

      EmailContent emailContent = EmailContent.builder().simple(msg).build();

      SendEmailRequest emailRequest =
            SendEmailRequest.builder().destination(destination).content(emailContent).fromEmailAddress(sender).build();

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
