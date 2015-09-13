package com.mail;

import com.vaadin.demo.dashboard.domain.MailConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.FlagTerm;
import java.util.Properties;

public class MailChecker {

    private static final Logger log = LoggerFactory.getLogger(MailChecker.class);

    private MailConfiguration configuration;

    public void setConfiguration(MailConfiguration configuration) {
        this.configuration=configuration;
    }

    public String[] check() throws Exception {

        //create properties field
        Properties props = new Properties();
        props.setProperty("mail.store.protocol", configuration.getProtocol());

        Session session = Session.getDefaultInstance(props, null);

        Store store = null;
        Folder newlead = null;
        String[] result = new String[0];
        try {
            store = session.getStore(configuration.getProtocol());
            store.connect(configuration.getHost(), configuration.getPort(), configuration.getUsername(), configuration.getPassword());

            //create the folder object and open it
            newlead = store.getFolder(configuration.getFolder());
            newlead.open(Folder.READ_WRITE);

            Flags seen = new Flags(Flags.Flag.SEEN);
            FlagTerm unseenFlagTerm = new FlagTerm(seen, false);
            Message messages[] = newlead.search(unseenFlagTerm);

            // retrieve the messages from the folder in an array and print it
            log.info("messages.length---" + messages.length);
            result = new String[messages.length];
            for (int i = 0, n = messages.length; i < n; i++) {
                Message message = messages[i];
                log.debug("Email Number " + (i + 1));
                log.debug("Subject: " + message.getSubject());
                log.debug("From: " + message.getFrom()[0]);
                String text = getText(message);
                log.debug("Message: " + text);
                result[i] = text;
                message.setFlags(new Flags(Flags.Flag.SEEN), true);
            }
            //close the store and folder objects
        } finally {
            if (newlead != null) {
                newlead.close(true);
            }
            if (store != null) {
                store.close();
            }
        }
        return result;
    }

    private static String getText(Part p) throws Exception {
        if (p.isMimeType("text/*")) {
            return (String) p.getContent();
        }

        if (p.isMimeType("multipart/alternative")) {
            // prefer html text over plain text
            Multipart mp = (Multipart) p.getContent();
            String text = null;
            for (int i = 0; i < mp.getCount(); i++) {
                Part bp = mp.getBodyPart(i);
                if (bp.isMimeType("text/plain")) {
                    if (text == null)
                        text = getText(bp);
                } else if (bp.isMimeType("text/html")) {
                    String s = getText(bp);
                    if (s != null)
                        return s;
                } else {
                    return getText(bp);
                }
            }
            return text;
        } else if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) p.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                String s = getText(mp.getBodyPart(i));
                if (s != null)
                    return s;
            }
        }
        return null;
    }
}