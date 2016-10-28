/*
 * Copyright (c) 2013, Jim Connors
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above
 *     copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided
 *     with the distribution.
 *   * Neither the name of this project nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package scoreboard.common;

import scoreboard.fx2.networking.FxSocketReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import scoreboard.fx2.framework.FocusableParent;
import scoreboard.fx2.framework.hockey.HockeyScoreboard;
import static scoreboard.common.Constants.DEFAULT_SESSION_ADDR;
import static scoreboard.common.Constants.DEFAULT_PORT;
import static scoreboard.common.Constants.DEFAULT_HOST;
import static scoreboard.common.Constants.DEFAULT_UNLIT_OPACITY;
import static scoreboard.common.Constants.DEBUG_NONE;
import scoreboard.fx2.networking.FxMulticastReader;
import scoreboard.fx2.networking.FxMulticastWriter;
import scoreboard.fx2.networking.FxMultipleSocketWriter;

public class Globals {
    /*
     * Global debug flags.  Each debug flag is assigned a value that is a
     * power of two, for easy bitmask operations.
     */
    public static int debugFlags = DEBUG_NONE;
    /*
     * URL resource (in XML) describing layout of remote (slave) display
     */
    public static String configURL;
    /*
     * URL resource pointing to horn sound
     */
    public static String hornURL;
    /*
     * Reference to HockeyScoreboard instance
     */
    public static HockeyScoreboard hockeyScoreboardRef;
    /*
     * Kludge needed to prevent multiple nodes handling the same keyboard
     * input more than once.
     */
    public static boolean keyEventAlreadyProcessed = false;

    /*
     * With respect to keyboard focus, this points to the last focused
     * node.
     */
    public static FocusableParent lastFocused = null;
    
    /*
     * Command-line switch determines whether to use Single IP Socket
     * to send XML updates over.
     */
    public static boolean useIPSocket = true;
    
    /*
     * Closed staus flag for simpleIPSocket, updated as status changes
     */
    public static boolean socketClosed = true;
    
    /*
     * IP Address of simpleIPSocket
     */
    public static String socketAddr;
    
    /*
     * Configurable (via command-line) IP Address for multicast socket
     */
    public static String sessionAddr = DEFAULT_SESSION_ADDR;
    
    /*
     * Command-line flag used to dump the scoreboard configuration in XML
     */
    public static boolean dumpConfig = false;
    
    /*
     * Command-line flag used to specify a port number.
     */
    public static int port = DEFAULT_PORT;
    
    /*
     * Command-line flag used to determine whether or not to display
     * Socket IP:port on the UI.
     */
    public static boolean displaySocket = false;
    
    /*
     * Command-line flag used to specify a port number.
     */
    public static String host = DEFAULT_HOST;
    
    /*
     * Flag used to specify whether the display will be master or slave
     */
    public static boolean isSlave = false;
    
    /*
     * Flag used to specify whether the display will be a TV. TVs can exhibit
     * overscan, where anything close to the edges of the screen won't be
     * dislayed.  So compensate for it.
     */
    public static boolean isTV = false;
    
    /*
     * Determines the opacity of unlit scoreboard bulbs (range: 0-1).
     * Depending upon the display (e.g. TV) the DEFAULT_UNLIT_OPACITY value
     * may appear to be too bright, so it could be lessened.
     */
    public static double unlitOpacity = DEFAULT_UNLIT_OPACITY;
    
    /*
     * Command-line arguments help message supplied if user specifies
     * either "-help" or "--help" on command-line"
     */
    private static String[] helpMsg = {
        "Command-line options:\n",
        "  -configURL:URL (default: /scoreboard/config/config.xml in Scoreboard.jar)",
        "\t\tURL pointing to XML file describing remote client configuration",
        "  -debug:value ",
        "\t\tset debug flags (for values see scoreboard.common.Constants.java)",
        "  -DisplaySocket",
        "\t\tShow socket connection info at bottom of scoreboard display",
        "  -DumpConfig:[true or false] (default false)",
        "\t\tDump layout of scoreboard in XML (for client customization)",
        "  -help or --help",
        "\t\tPrint this screen for command-line argument options and exit",
        "  -hornURL:URL (default: /scoreboard/util/sounds/BUZZER.mp3 in Scoreboard.jar)",
        "\t\tURL pointer to alternate media file representing horn sound",
        "  -host:IP_ADDRESS (default: localhost)",
        "\t\tSpecify IP Address of socket",
        "  -master (default)",
        "\t\tRun as a scoreboard controller (server)",
        "  -MulticastAddr:IP_ADDRESS (default: 227.27.27.27)",
        "\t\tUse multicast socket and specify its IP address",
        "  -port:PORT_NUMBER (default 2011)",
        "\t\tSpecify port for socket connection",
        "  -slave\t\t",
        "\t\tRun as a remote scoreboard client",
        "  -tv",
        "\t\tRun in full screen mode for a TV (with padding for overscan)",
        "  -unlitOpacity:[0-100] (default 10)",
        "\t\tChange opacity of unlit scoreboard bulbs",
        "  -UseIPSocket (default)",
        "\t\tUse IP sockets (with defaults) for scoreboard updates",
        "  -UseMulticastSocket",
        "\t\tUse multicast sockets (with defaults) for scoreboard updates",
        ""
    };
    
    public static void printCmdLineHelpMsg() {
        for (String str : helpMsg) {
            System.out.println(str);    
        }
    }
    
    /*
     * Parse command-line arguments in one central place, as there are many
     * main methods which could use these common flags.
     */
    public static void parseArgs(String[] args) {

        InetAddress localAddr = null;
        try {
            localAddr = InetAddress.getLocalHost(); 
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        
        // Corner case where VirtualBox (and maybe other virtualization
        // packages) interferes with getLocalHost() and actually
        // causes it to return the IP address of a virtual interface.
//        InetAddress localAddr = IPAddrKludge.getLocalAddress();
        
        for (String arg : args) {
            if (arg.equals("-slave")) {
                isSlave = true;    
            } else if (arg.equals("-master")) {
                isSlave = false;
            } else if (arg.equals("-tv")) {
                isTV = true;
            } else if (arg.equals("-help") || arg.equals("--help")) {
                printCmdLineHelpMsg() ;
                System.exit(0);
            }
            
            String[] subarg = arg.split(":");
            if (subarg[0].equals("-MulticastAddr")) {
                useIPSocket = false;
                if (subarg.length > 1) {
                    if (subarg[1].equals("BasedOnLastQuartet")) {
                        byte[] lquartets = localAddr.getAddress();
                        String[] squartets = sessionAddr.split("[^0-9]");
                        StringBuilder sb = new StringBuilder();
                        for (int i=0; i<=2; i++) {
                            sb.append(squartets[i]);
                            sb.append(".");
                        }
                        // convert byte value to unisgned int
                        int lastQuartet = lquartets[3];
                        if (lastQuartet < 0) {
                            lastQuartet += 256;
                        }
                        sb.append(lastQuartet);
                        sessionAddr = new String(sb);
                    } else {
                        try {
                            InetAddress address =
                                    InetAddress.getByName(subarg[1]);
                            sessionAddr = subarg[1];
                        } catch (UnknownHostException e) {
                            System.out.println("Bad Multicast IP address: " +
                                    subarg[1] + " supplied by command-line.");
                        }
                    }
                }
            } else if (subarg[0].equals("-host")) {
                if (subarg.length > 1) {
                    try {
                        InetAddress address = InetAddress.getByName(subarg[1]);
                        host = subarg[1];
                    } catch (UnknownHostException e) {
                        System.out.println("Bad IP address: " +
                                subarg[1] + " supplied by command-line.");
                    }
                }
            } else if (subarg[0].equals("-debug")) {
                if (subarg.length > 1) {
                    debugFlags = Integer.parseInt(subarg[1]);
                }    
            } else if (subarg[0].equals("-port")) {
                if (subarg.length > 1) {
                    port = Integer.parseInt(subarg[1]);
                }   
            } else if (subarg[0].equals("-DumpConfig")) {
                if (subarg.length > 1) {
                    if (subarg[1].equals("true")) {
                        dumpConfig = true;    
                    }
                }   
            } else if (subarg[0].equals("-DisplaySocket")) {
                if (subarg.length > 1) {
                    if (subarg[1].equals("true")) {
                        displaySocket = true;    
                    }
                }   
            } else if (subarg[0].equals("-UseIPSocket")) {               
                useIPSocket = true;
                socketAddr = localAddr.getHostAddress();
            } else if (subarg[0].equals("-UseMulticastSocket")) {               
                useIPSocket = false;
            } else if (subarg[0].equals("-configURL")
                    || (subarg[0].equals("-hornURL"))) {
                /*
                 * A little bit of kludgery here, the original separator chosen
                 * for command-line arguments was ":", which happens to
                 * conflict with URL syntax.  For historical reasons, don't
                 * want to change.  Just append all the substrings together
                 * and insert a ":" in between (except for the last)
                 */
                StringBuffer sb = new StringBuffer();
                for (int i = 1; i < subarg.length; i++) {
                    sb.append(subarg[i]);
                    if (i < (subarg.length - 1)) {
                        sb.append(":");
                    }
                    if (subarg[0].equals("-configURL")) {
                        configURL = sb.toString();
                    } else {
                        hornURL = sb.toString();
                    }
                }
            } else if (subarg[0].equals("-unlitOpacity")) {
                if (subarg.length > 1) {
                    int value = Integer.parseInt(subarg[1]);
                    if (value >= 0 && value <= 100) {
                        unlitOpacity = (double) value / 100.0d;
                        System.out.println("unlitOpacity changed to " +
                                unlitOpacity);
                    }
                }   
            }
        }
        
        System.out.println();
        if (useIPSocket) {
            /*
             * If host was specifically set by command-line, set 
             * simpleIPSocketAddr here.  
             * 1) -host switch may have been issued after -UseIPSocket
             * 2) This may be required for a master
             * host which has a virtualbox interface confined.  Virtualbox
             * can confuse Java as to which is the proper network interface.
             */
            socketAddr = host;
            System.out.println("Socket - " + socketAddr + ":" + port);
        } else {
            System.out.println("IP Multicast - " + sessionAddr + ":" + port);
        }
    }
}
