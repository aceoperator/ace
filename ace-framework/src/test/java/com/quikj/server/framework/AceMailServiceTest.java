package com.quikj.server.framework;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class AceMailServiceTest {

    private static String server = System.getProperty("test.mail.host", null);
    private static int port = Integer.valueOf(System.getProperty("test.mail.port", "25"));
    private static boolean tls = Boolean.valueOf(System.getProperty("test.mail.tls", "false"));
    private static boolean debug = Boolean.valueOf(System.getProperty("test.mail.debug", "true"));
    private static String username = System.getProperty("test.mail.username", null);
    private static String password = System.getProperty("test.mail.password", null);;
    private static String pendingDir = System.getProperty("test.mail.pend.dir", "mail");
    private static String pendingFile = System.getProperty("test.mail.pend.file", "pend_");
    private static String overrideFrom = System.getProperty("test.mail.override.from", null);

    private static boolean skip = false;

    @BeforeClass
    public static void setup() throws Exception {
        try {
            AceConfigFileHelper.getAceRoot();
        } catch (AceRuntimeException e) {
            skip = true;
            return;
        }

        new AceTimer().start();
        new AceLogger("JUNIT", 1).start();
        new AceMailService(server, port, tls, debug, username, password, pendingDir, pendingFile, overrideFrom).start();

    }

    @AfterClass
    public static void destroy() throws InterruptedException {
        if (skip) return;
        
        AceMailService.getInstance().dispose();
        AceLogger.Instance().dispose();
        AceTimer.Instance().dispose();

        Thread.sleep(5000L);
    }

    @Test
    public void test() throws InterruptedException {
        // This test is meant to be driven using system property. So, if the server is not specified, pass the tests
        if (skip || server == null) return;

        AceMailMessage message = new AceMailMessage();
        message.setFrom("memyself@nowhere.com");
        message.addTo("info@quik-j.com");
        message.addCc("sales@quik-j.com");
        message.setSubType("html");
        message.setSubject("Test mail 1234");
        message.setBody("<b>Hello</b><i>New Year</i>");
        AceMailService.getInstance().addToMailQueue(message);

        Thread.sleep(5000L);
    }
}