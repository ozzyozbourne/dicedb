package org.dice;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dice.server.TcpSync;

final public class Main {

    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main() {
        TcpSync.runSyncTCPServer();
    }
}