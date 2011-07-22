package ie.clarity.cyclingrouteplanner.Networking;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Observable;
import java.util.Observer;

import org.apache.commons.net.ftp.FTPClient;

import android.os.Environment;
import android.util.Log;

public class Transmitter
{
	protected final int DEFAULT_TIMEOUT = 10000; // Measured in milliseconds
	protected final int CONNECT_TIMEOUT = 5000;  // Measured in milliseconds
	protected final int DATA_TIMEOUT    = 15000; // Measured in milliseconds, used when reading from the data connection.
	protected final int CONTROL_TIMEOUT = 20000; // Measured in milliseconds, should be longer than DEFAULT_TIMEOUT, but doesn't have to be.
	private int error = -1;
	
	Transmitter()
	{
		Log.e("NETWORK", "Creating a new Transmitter.");
	}
	
	/**
	 * Send a passed file to the FTP server.
	 * 
	 * Observer's are notified of status by an integer indicating the status of the operation.
	 * 0 - Success 
	 * 1 - Cannot connect to server
	 * 2 - Cannot log in to server
	 * 3 - Cannot find file to send
	 * 4 - Cannot store file on server
	 * 5 - Cannot logout
	 * 6 - Cannot close file output stream
	 * 
	 * @param filename The file to be uploader
	 */
	public int send(String filename)
	{	
	    FTPClient client = new FTPClient();
	    Log.e("NETWORK", "Client object created.");
	    FileInputStream fis = null;

	    // Set the custom timeouts
	    setTimeouts(client);
	    // Log messages to check timeouts
	    logTimeouts(client);
	    
	    // Connect to the server
	    try {
			client.connect("134.226.114.105");
			Log.i("NETWORK", "Connected to FTP Server");
		} catch (IOException e) {
			Log.e("NETWORK", "Failed to connect to server." + e.toString());
			e.printStackTrace();
			return 1;
		}
		
		// Provide log in credentials
	    try {
			client.login("rothar", "quei1HoR");
			Log.i("NETWORK", "Logged into server");
		} catch (IOException e) {
			Log.e("NETWORK", "Failed to log-in to server." + e.toString());
			e.printStackTrace();
			disconnect(client);
			return 2;
		}
		
		// Retrieve the file to be transmitted
	    try {
			File rootDir = Environment.getExternalStorageDirectory();
			String path = rootDir.toString() + "/Android/data/ie.clarity.cyclingplanner/history/";
			fis = new FileInputStream(path + filename);
			Log.i("NETWORK", "Found file to transmit");
		} catch (FileNotFoundException e) {
			Log.e("NETWORK", "Failed to find file for sending." + e.toString());
			e.printStackTrace();
			disconnect(client);
			return 3;
		}
		
		// Store the file on the server
	    try {
	    	if(fis != null)	// Prevent null pointer exception if the file cannot be read.
	    	{
	    		client.storeFile(filename, fis);
	    		Log.i("NETWORK", "Stored file on the server.");
	    	}
		} catch (IOException e) {
			Log.e("NETWORK", "Failed to store file on server. " + e.toString());
			e.printStackTrace();
			disconnect(client);
			return 4;
		}
		
		// Logout
	    try {
			client.logout();
			Log.i("NETWORK", "Successfully logged out.");
		} catch (IOException e) {
			Log.e("NETWORK", "Failed to logout of server. " + e.toString());
			e.printStackTrace();
			disconnect(client);
			return 5;
		}
		
		// Close the File Input Stream
	    try {
			fis.close();
			Log.i("NETWORK", "Closed file input stream.");
		} catch (IOException e) {
			Log.e("NETWORK", "Failed to close File Input Stream. " + e.toString());
			e.printStackTrace();
			disconnect(client);
			return 6;
		}
		
		// Disconnect the control connection to the FTP Server
		try {
			client.disconnect();
			Log.i("NETWORK", "Disconnected control connection to FTP Server correctly.");
		} catch (IOException e) {
			Log.e("NETWORK", "Failed to Disconnect control connection to server. " + e.toString());
			e.printStackTrace();
			return 7;
		}
		
		// Successful Transmission
		return 0;
	}

	/**
	 * This function sets the timeouts that the programmer.
	 * It overrides the default timeouts.
	 * Reference: http://commons.apache.org/net/apidocs/org/apache/commons/net/SocketClient.html#setDefaultTimeout%28int%29
	 * @param client The client who's timeouts are being modified.
	 */
	private void setTimeouts(FTPClient client) {
		client.setDefaultTimeout(DEFAULT_TIMEOUT); // Set the default timeout in milliseconds to use when opening a socket. This value is only used previous to a call to connect() and should not be confused with setSoTimeout() which operates on an the currently opened socket.
		client.setConnectTimeout(CONNECT_TIMEOUT); // Sets the connection timeout in milliseconds, which will be passed to the Socket object's connect() method. 
		client.setDataTimeout(DATA_TIMEOUT); // Sets the timeout in milliseconds to use when reading from the data connection. This timeout will be set immediately after opening the data connection.
		client.setControlKeepAliveTimeout(CONTROL_TIMEOUT);
	}

	/**
	 * This function is used to log the various timeout times of the client.
	 * Primary function is to debug.
	 * Reference: http://commons.apache.org/net/apidocs/org/apache/commons/net/SocketClient.html#setDefaultTimeout%28int%29
	 * @param client The client who's timeouts are being inspected.
	 */
	private void logTimeouts(FTPClient client) 
	{
		/*
		 * Default Log:
		 * DEBUG/NETWORK(16652): getControlKeepAliveTimeout: 0
		 * DEBUG/NETWORK(16652): getControlKeepAliveActiveReplyTimeout: 1000
		 * DEBUG/NETWORK(16652): getConnectTimeout:0
		 * DEBUG/NETWORK(16652): getDefaultTimeout:0
		 */
	    Log.d("NETWORK", "getControlKeepAliveTimeout:" + client.getControlKeepAliveTimeout());
	    Log.d("NETWORK", "getControlKeepAliveActiveReplyTimeout:" + client.getControlKeepAliveReplyTimeout());
	    Log.d("NETWORK", "getConnectTimeout:" + client.getConnectTimeout());
	    Log.d("NETWORK", "getDefaultTimeout:" + client.getDefaultTimeout());
	    
	    /*try // If this is required then call logTimeouts() after connect() and uncomment this block{
			Log.d("NETWORK", "getSoTimeout:" + client.getSoTimeout());
		} catch (SocketException e1) {
			Log.d("NETWORK", "Could not getSoTimeout:");
			e1.printStackTrace();
		}*/
	}

	/**
	 * This function disconnects the client from the FTP Server.
	 * It should be called if an operation with the server fails, such as storing or logging-in.
	 * It tries to close the control connection between the client and the FTP server.
	 * @param client The client to be disconnected
	 */
	private void disconnect(FTPClient client)
	{
		try {
			client.disconnect();
			Log.e("NETWORK", "Disconnected client from server prematurely but correctly.");
		} catch (IOException e) {
			Log.e("NETWORK", "Emergency disconnection failed also!" + e.toString());
			e.printStackTrace();
		}
	}
	
	public void setError(int error) {
		this.error = error;
	}

	public int getError() {
		return error;
	}	
	
	/*
	Boolean connectSocket(String ip, int port)
	{
		try {
			Log.e("NETWORK", "Client attempting connection...");
			socket = new Socket(ip, port);
			Log.e("NETWORK", "Client established connection...");
			in = new DataInputStream(socket.getInputStream());
			out = new PrintWriter(socket.getOutputStream());
			Log.e("NETWORK", "IO Stream connections established.");
		} catch (UnknownHostException e) {
			System.err.printf("Don't know about host: %s", ip);
			System.exit(1);
	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		return null;
	}

	
	void send(String data)
	{				
		out.write(data);
		out.write(data);
		out.flush();
		Log.e("NETWORK", "Written and flushed");
	}
	*/
}
		