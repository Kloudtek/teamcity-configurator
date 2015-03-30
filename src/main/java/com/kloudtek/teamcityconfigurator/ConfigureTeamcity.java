/*
 * Copyright (c) 2015 Kloudtek Ltd
 */

package com.kloudtek.teamcityconfigurator;

import com.beust.jcommander.Parameter;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by yannick on 30/03/15.
 */
public class ConfigureTeamcity {
    public static final String AGREEMENT = "/showAgreement.html";
    public static final String SETUP_ADMIN = "/setupAdmin.html";
    public static final String LOGIN = "/login.html";
    @Parameter(names = "-u", description = "Server URL", required = true)
    private String serverUrl;
    @Parameter(names = {"-ht", "--http-timeout"}, description = "HTTP connection timeout")
    private int timeout = 10000;
    @Parameter(names = {"-wt", "--wait-timeout"}, description = "How long to wait for server to be ready")
    private long waitTimeout = 3 * 60000L;
    @Parameter(names = {"-n"}, description = "Username for the admin account to be created on teamcity",required = true)
    private String adminUsername;
    @Parameter(names = {"-p"}, description = "Password for the admin account to be created on teamcity",required = true)
    private String adminPassword;
    private boolean debug = false;

    public void execute() throws SetupException {
        System.out.println("Configuring teamcity server " + serverUrl);
        long waitExpiry = System.currentTimeMillis() + waitTimeout;
        Logger.getLogger("").setLevel(Level.OFF);

        WebClient webClient = new WebClient();
        webClient.getOptions().setTimeout(timeout);
        Page page;
        for (;;) {
            try {
                page = webClient.getPage(serverUrl);
                if (validSetupPage(page)) {
                    break;
                }
            } catch (Exception e) {
                if (debug) {
                    e.printStackTrace();
                }
            }
            if (System.currentTimeMillis() > waitExpiry) {
                throw new SetupException("Timeout waiting for server to be ready");
            } else {
                sleep(2500L);
            }
        }
        try {
            boolean completed = false;
            boolean adminSetup = false;
            while (!completed) {
                String url = page.getUrl().toString();
                if( url.endsWith(AGREEMENT) ) {
                    System.out.println("Accepting license agreement");
                    ((HtmlCheckBoxInput) ((HtmlPage) page).getElementById("accept")).setChecked(true);
                    ((HtmlCheckBoxInput) ((HtmlPage) page).getElementById("sendUsageStatistics")).setChecked(false);
                    page = ((HtmlSubmitInput) ((HtmlPage) page).getElementByName("Continue")).click();
                    validatePageUrl(page, SETUP_ADMIN);
                } else if( url.endsWith(SETUP_ADMIN) ) {
                    if( ! adminSetup ) {
                        System.out.println("Creating admin user");
                        adminSetup = true;
                        webClient.getOptions().setJavaScriptEnabled(true);
                        page = webClient.getPage(url);
                    } else {
                        DomElement usernameInputField = ((HtmlPage) page).getElementById("input_teamcityUsername");
                        if( usernameInputField == null ) {
                            page = webClient.getPage(serverUrl);
                        } else {
                            ((HtmlTextInput) usernameInputField).type(adminUsername);
                            ((HtmlPasswordInput) ((HtmlPage) page).getElementById("password1")).type(adminPassword);
                            ((HtmlPasswordInput) ((HtmlPage) page).getElementById("retypedPassword")).type(adminPassword);
                            page = ((HtmlSubmitInput) ((HtmlPage) page).getFirstByXPath("//input[@value='Create Account']")).click();
                            if( page.getUrl().toString().endsWith(LOGIN) ) {
                                completed = true;
                            } else {
                                sleep(1000L);
                                page = webClient.getPage(serverUrl);
                            }
                        }
                    }
                } else {
                    if( adminSetup && pageIsProjectPage(page) ) {
                        completed = true;
                    } else {
                        System.out.println(((HtmlPage) page).asXml());
                        throw new SetupException("Unexpected page: "+ url);
                    }
                }
            }
            System.out.println("Teamcity server configuration completed");
        } catch (IOException e) {
            throw new SetupException(e);
        }
    }

    private boolean pageIsProjectPage(Page page) {
        HtmlTitle title = ((HtmlPage) page).getFirstByXPath("/html/head/title");
        return title != null && title.getTextContent().contains("Project");
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e1) {
            throw new RuntimeException(e1);
        }
    }

    private boolean validSetupPage(Page page) {
        String url = page.getUrl().toString();
        return url.endsWith(AGREEMENT) || url.endsWith(SETUP_ADMIN) || url.endsWith(LOGIN);
    }

    private void validatePageUrl(Page page, String suffix) throws SetupException {
        if (!page.getUrl().toString().endsWith(suffix)) {
            System.out.println(((HtmlPage) page).asXml());
            throw new SetupException();
        }
    }
}
