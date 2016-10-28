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

package test.scoreboard.common.networking.simple;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import scoreboard.common.networking.*;
import static scoreboard.common.Constants.DEFAULT_HOST;
import static scoreboard.common.Constants.DEFAULT_PORT;
import static scoreboard.common.Constants.DEBUG_STATUS;
import static scoreboard.common.Constants.DEBUG_ALL;
import static scoreboard.common.Constants.DEBUG_NONE;

/**
 * WARNING: This class is meant for testing outside the JavaFX framework. In
 * JavaFX we must insure that certain work must be done on the main thread, and
 * this class does no such thing.
 */
public class SimpleSocketWriter extends SocketBase {

    private SocketServer socketServer;
    private boolean socketClosed = true;
    private int port;
    private int debugFlags;

    class SingleSocketWriterListener implements SocketListener {

        @Override
        public void onMessage(String msg) {
            // Should never read anything
        }

        @Override
        public void onClosedStatus(boolean isClosed) {
            System.out.print("onClosedStatus: socket is ");
            if (isClosed) {
                System.out.println("closed");
                socketClosed = true;
            } else {
                System.out.println("open");
                socketClosed = false;
            }
            if (isClosed) {
                connect();
            }
        }
    }

    public void connect() {
        socketServer = new SocketServer(
                new SingleSocketWriterListener(), port, debugFlags);
        socketServer.connect();
    }

    /**
     * Send a message in the form of a String to the socket.
     *
     * @param msg The String message to send
     */
    public void sendMessage(String msg) {
        if (!socketClosed) {
            socketServer.sendMessage(msg);
        }
    }

    public SimpleSocketWriter() {
        this(DEFAULT_PORT, DEBUG_NONE);
    }

    public SimpleSocketWriter(int port) {
        this(port, DEBUG_NONE);
    }

    public SimpleSocketWriter(int port, int debugFlags) {
        this.port = port;
        this.debugFlags = debugFlags;
    }
    
    public static void main(String[] args) {
        SimpleSocketWriter w = new SimpleSocketWriter(2011,
                DEBUG_ALL);
        w.connect();
        while (w.socketClosed) {
            try {
                Thread.sleep(2000);
            } catch (Exception e) {
            }
        }
        
        String line = "";
        int messageNum = 0;
        while (true) {
            System.out.println("type <RETURN> to send message ('q' to exit): ");
            InputStreamReader converter = new InputStreamReader(System.in);
            BufferedReader in = new BufferedReader(converter);
            try {
                line = in.readLine();
            } catch (Exception e) {
            }
            if (line.equals("q")) {
                break;
            } else {
                String msg = "message " + ++messageNum;
                w.sendMessage(msg);
                System.out.println("SimpleSocketWriter sent> " + msg);
            }
        }
    }
}
