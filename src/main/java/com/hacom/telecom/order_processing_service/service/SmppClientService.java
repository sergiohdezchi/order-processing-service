package com.hacom.telecom.order_processing_service.service;

import com.cloudhopper.commons.charset.CharsetUtil;
import com.cloudhopper.smpp.SmppBindType;
import com.cloudhopper.smpp.SmppSession;
import com.cloudhopper.smpp.SmppSessionConfiguration;
import com.cloudhopper.smpp.impl.DefaultSmppClient;
import com.cloudhopper.smpp.impl.DefaultSmppSessionHandler;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.cloudhopper.smpp.pdu.SubmitSmResp;
import com.cloudhopper.smpp.type.Address;
import com.cloudhopper.smpp.type.SmppChannelException;
import com.cloudhopper.smpp.type.SmppTimeoutException;
import com.cloudhopper.smpp.type.UnrecoverablePduException;
import com.hacom.telecom.order_processing_service.config.SmppProperties;
import io.micrometer.core.instrument.Counter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Service
public class SmppClientService {

    private static final Logger log = LoggerFactory.getLogger(SmppClientService.class);

    @Autowired
    private SmppProperties smppProperties;

    @Autowired
    private Counter smsSentCounter;

    @Autowired
    private Counter smsFailedCounter;

    private DefaultSmppClient smppClient;
    private SmppSession session;

    @PostConstruct
    public void init() {
        if (!smppProperties.isEnabled()) {
            log.info("SMPP client is disabled");
            return;
        }

        try {
            smppClient = new DefaultSmppClient();
            log.info("SMPP client created successfully");

            connectToSmppServer();
        } catch (Exception e) {
            log.warn("Could not initialize SMPP client: {}. SMS sending will be disabled.", e.getMessage());
        }
    }

    private void connectToSmppServer() {
        try {
            SmppSessionConfiguration config = new SmppSessionConfiguration();
            config.setWindowSize(smppProperties.getWindowSize());
            config.setName("OrderProcessingSession");
            config.setType(SmppBindType.TRANSCEIVER);
            config.setHost(smppProperties.getHost());
            config.setPort(smppProperties.getPort());
            config.setConnectTimeout(smppProperties.getConnectTimeout());
            config.setSystemId(smppProperties.getSystemId());
            config.setPassword(smppProperties.getPassword());
            config.setSystemType(smppProperties.getSystemType());
            config.setRequestExpiryTimeout(smppProperties.getRequestExpiryTimeout());
            config.setWindowMonitorInterval(smppProperties.getWindowMonitorInterval());

            session = smppClient.bind(config, new DefaultSmppSessionHandler());
            log.info("SMPP session bound successfully to {}:{}", smppProperties.getHost(), smppProperties.getPort());
        } catch (Exception e) {
            log.warn("Could not bind SMPP session: {}. Will operate without SMS capability.", e.getMessage());
            session = null;
        }
    }

    /**
     * Sends an SMS message via SMPP
     */
    public boolean sendSms(String destinationNumber, String message) {
        if (!smppProperties.isEnabled()) {
            log.info("SMPP is disabled. SMS not sent to {}: {}", destinationNumber, message);
            return false;
        }

        if (session == null || !session.isBound()) {
            log.warn("SMPP session is not bound. Attempting to reconnect...");
            connectToSmppServer();
            
            if (session == null || !session.isBound()) {
                log.error("Cannot send SMS, SMPP session is not available");
                return false;
            }
        }

        try {
            SubmitSm submit = new SubmitSm();

            submit.setSourceAddress(new Address(
                    (byte) smppProperties.getSourceAddressTon(),
                    (byte) smppProperties.getSourceAddressNpi(),
                    smppProperties.getSourceAddress()
            ));

            submit.setDestAddress(new Address(
                    (byte) smppProperties.getDestAddressTon(),
                    (byte) smppProperties.getDestAddressNpi(),
                    destinationNumber
            ));

            submit.setShortMessage(CharsetUtil.encode(message, CharsetUtil.CHARSET_GSM));

            SubmitSmResp submitResp = session.submit(submit, 10000);

            if (submitResp.getCommandStatus() == 0) {
                log.info("SMS sent successfully to {}. Message ID: {}", destinationNumber, submitResp.getMessageId());
                smsSentCounter.increment();
                return true;
            } else {
                log.error("SMS send failed with status: {}", submitResp.getCommandStatus());
                smsFailedCounter.increment();
                return false;
            }

        } catch (SmppTimeoutException e) {
            log.error("SMS send timeout for {}: {}", destinationNumber, e.getMessage());
            smsFailedCounter.increment();
            return false;
        } catch (SmppChannelException e) {
            log.error("SMPP channel error for {}: {}", destinationNumber, e.getMessage());
            session = null;
            smsFailedCounter.increment();
            return false;
        } catch (UnrecoverablePduException e) {
            log.error("Unrecoverable PDU error for {}: {}", destinationNumber, e.getMessage());
            smsFailedCounter.increment();
            return false;
        } catch (Exception e) {
            log.error("Error sending SMS to {}: {}", destinationNumber, e.getMessage());
            smsFailedCounter.increment();
            return false;
        }
    }

    /**
     * Sends an order processed notification SMS
     */
    public void sendOrderProcessedNotification(String orderId, String phoneNumber) {
        String message = "Your order " + orderId + " has been processed";
        log.info("Sending order notification SMS to {}: {}", phoneNumber, message);
        
        boolean sent = sendSms(phoneNumber, message);
        
        if (sent) {
            log.info("Order notification SMS sent successfully for order {}", orderId);
        } else {
            log.warn("Order notification SMS could not be sent for order {}", orderId);
        }
    }

    @PreDestroy
    public void destroy() {
        if (session != null && session.isBound()) {
            log.info("Closing SMPP session...");
            session.close();
            session.destroy();
        }
        
        if (smppClient != null) {
            log.info("Destroying SMPP client...");
            smppClient.destroy();
        }
    }
}
