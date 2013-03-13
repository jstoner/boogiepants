/*
 *
 * Copyright (c) 2008-2009, John Stoner
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list 
 * of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this 
 * list of conditions and the following disclaimer in the documentation and/or 
 * other materials provided with the distribution.
 * Neither the name of the John Stoner nor the names of its contributors may be 
 * used to endorse or promote products derived from this software without specific 
 * prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE 
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 * 
 * John Stoner is reachable at johnstoner2 [[at]] gmail [[dot]] com.
 * His current physical address is
 * 
 * 2358 S Marshall 
 * Chicago, IL 60623
 */
package boogiepants.instruments;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.illposed.osc.OSCPacket;
import com.illposed.osc.OSCPortOut;

/**
 * This object encapsulates the OSC connection code.
 * 
 * <P>It is a singleton wrapper for an OSCPortOut object.</P>
 * 
 * @author jstoner
 *
 */
public class OSCConnection {
    private OSCPortOut oscSender;
    private String oscEndpoint= "localhost";
    private int oscEndpointPort = 57110;
    private static OSCConnection connection;

    /**
     * private constructor
     */
    private OSCConnection() {
        updateConnection();
    }
    
    public static OSCConnection getConnection(){
        if (connection == null){
            connection = new OSCConnection();
        }
        return connection;
    }
    
    /**
     * @param oscEndpoint
     * @param oscEndpointPort
     */
    public OSCConnection(String oscEndpoint, int oscEndpointPort) {
        this.oscEndpoint = oscEndpoint;
        this.oscEndpointPort = oscEndpointPort;
        updateConnection();
    }
    
    /**
     * Actually creates/recreates the OCPortOut object to send data with
     */
    private void updateConnection(){
        try {
            oscSender = new OSCPortOut(InetAddress.getByName(oscEndpoint), oscEndpointPort);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * @param packet
     */
    public void send(OSCPacket packet){
        try {
            oscSender.send(packet);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    /**
     * @return
     */
    public String getOscEndpoint() {
        return oscEndpoint;
    }
    
    /**
     * @param oscEndpoint
     */
    public void setOscEndpoint(String oscEndpoint) {
        this.oscEndpoint = oscEndpoint;
        updateConnection();
    }
    
    /**
     * @return
     */
    public int getOscEndpointPort() {
        return oscEndpointPort;
    }
    
    /**
     * @param oscEndpointPort
     */
    public void setOscEndpointPort(int oscEndpointPort) {
        this.oscEndpointPort = oscEndpointPort;
        updateConnection();
    }
}
