/*
 * Copyright (c) 2015 Kloudtek Ltd
 */

package com.kloudtek.teamcityconfigurator;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

/**
 * Created by yannick on 30/03/15.
 */
public class Main {
    public static void main(String[] args) throws SetupException {
        ConfigureTeamcity configure = new ConfigureTeamcity();
        JCommander jc = new JCommander(configure);
        jc.setProgramName("teamcity-configurator");
        if( args.length > 0 ) {
            try {
                jc.parse(args);
                configure.execute();
            } catch (ParameterException e) {
                System.err.println(e.getMessage());
                jc.usage();
            }
        } else {
            jc.usage();
        }
    }
}
