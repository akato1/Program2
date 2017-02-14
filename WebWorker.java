/**
 * Web worker: an object of this class executes in its own new thread
 * to receive and respond to a single HTTP request. After the constructor
 * the object executes on its "run" method, and leaves when it is done.
 *
 * One WebWorker object is only responsible for one client connection. 
 * This code uses Java threads to parallelize the handling of clients:
 * each WebWorker runs in its own thread. This means that you can essentially
 * just think about what is happening on one client at a time, ignoring 
 * the fact that the entirety of the webserver execution might be handling
 * other clients, too. 
 *
 * This WebWorker class (i.e., an object of this class) is where all the
 * client interaction is done. The "run()" method is the beginning -- think
 * of it as the "main()" for a client interaction. It does three things in
 * a row, invoking three methods in this class: it reads the incoming HTTP
 * request; it writes out an HTTP header to begin its response, and then it
 * writes out some HTML content for the response content. HTTP requests and
 * responses are just lines of text (in a very particular format). 
 *
 **/
 import java.net.Socket;
 import java.lang.Runnable;
 import java.io.*;
 import java.util.Date;
 import java.text.DateFormat;
 import java.util.TimeZone;
 import java.lang.String;
 import java.nio.file.*;
  
 public class WebWorker implements Runnable  {

 	private Socket socket;
 
 /**
 * Constructor: must have a valid open socket
 **/
 public WebWorker(Socket s)
 {
    socket = s;
 }
 
 /**
 * Worker thread starting point. Each worker handles just one HTTP 
 * request and then returns, which destroys the thread. This method
 * assumes that whoever created the worker created it with a valid
 * open socket object.
 **/
 public void run()  {

    System.err.println("Handling connection...");
    try {
       InputStream  is = socket.getInputStream();
       OutputStream os = socket.getOutputStream();
       String path = readHTTPRequest(is);
       String type = contentType(path);
       writeHTTPHeader(os, type);
       writeContent(os, path);
       os.flush();
       socket.close();
    } catch (Exception e) {
       System.err.println("Output error: "+e);
    }
    System.err.println("Done handling connection.");
    return;
 }
 
 private String contentType(String type)  {
	Path p = Paths.get(type);
	String content = "";
	try { 
		content = Files.probeContentType(p);
	}
	catch (IOException ex) {
		ex.printStackTrace();
	}
	System.out.println("Content Type: " + content);

	return content;
 }

 /**
 * Read the HTTP request header.
 **/
 private String readHTTPRequest(InputStream is)  {
 	String line;
 	String part = "";
 	BufferedReader r = new BufferedReader(new	InputStreamReader (is));

 	while (true) {
 		try {
 			while (!r.ready()) Thread.sleep(1);
 			line = r.readLine();
 			System.err.println("Request line: ("+line+")");
 			if (line.length()==0) break;
	//if statement used to get the first line from terminal
	
		//split line by spaces and put them in array
           	String file[] = line.split(" ");	
 			if(file[0].equals("GET"))  {
		//split string, this is the path that will be used
 				part = "." + file[1];
  	    		}
     		} 
		catch (Exception e) {
          		System.err.println("Request error: "+e);
          		break;
     		}
    	}
    	return part;
 }
 
 /**
 * Write the HTTP header lines to the client network connection.
 * @param os is the OutputStream object to write to
 * @param contentType is the string MIME content type (e.g. "text/html")
 **/

 private void writeHTTPHeader(OutputStream os, String contentType) throws Exception
 {
    Date d = new Date();
    DateFormat df = DateFormat.getDateTimeInstance();
    df.setTimeZone(TimeZone.getTimeZone("GMT"));
    os.write("HTTP/1.1 200 OK\n".getBytes());
    os.write("Date: ".getBytes());
    os.write((df.format(d)).getBytes());
    os.write("\n".getBytes());
    os.write("Server: Jon's very own server\n".getBytes());
    //os.write("Last-Modified: Wed, 08 Jan 2003 23:11:55 GMT\n".getBytes());
    //os.write("Content-Length: 438\n".getBytes()); 
    os.write("Connection: close\n".getBytes());
    os.write("Content-Type: ".getBytes());
    os.write(contentType.getBytes());
    os.write("\n\n".getBytes()); // HTTP header ends with 2 newlines
    return;
 }
 
 /**
 * Write the data content to the client network connection. This MUST
 * be done after the HTTP header has been written out.
 * @param os is the OutputStream object to write to
 **/

 private void writeContent(OutputStream os, String path) throws Exception  {

 	//use the part string which contains the path to find 		the file
 	File f = new File(path);	
	
 	//if the file doesn't exist within the directory you 			will receive an 404 error message
 	if(!f.exists())  {
		os.write("<html><head></head><body>\n".getBytes()); 	                              
 		os.write("<h1>Error!</h1>\n<h2>404 - File Not 				Found</h2>\n".getBytes());
 		os.write("<h4>The file mentioned above could not be found on our servers. The file may have been damaged, moved, deleted, or mispelled.</h4>".getBytes());
		os.write("</body></html>\n".getBytes());
	}
	
	else  {
 		FileInputStream input = new FileInputStream(path);
		int i;
		while ((i = input.read()) != -1) {
		   os.write(i);
	}

	//gives you the date and time, will be used for tags
	Date d = new Date();
 	DateFormat df = DateFormat.getDateTimeInstance();
		
 	FileReader fr = new FileReader(f);
 	BufferedReader br = new BufferedReader(fr);
 	StringBuffer sr = new StringBuffer();
	String line = br.readLine();

 	/**try  {
		while(line != null) {
			sr.append(line);
 			sr.append("\n");
			line = br.readLine();
 		}
 		fr.close();
	}
	catch (IOException e) {
 		e.printStackTrace();
 	}**/

	String convert = sr.toString();
 	//replaces tags, cs371date and server, with formatted 		date and server name
 	convert = convert.replaceAll("<cs371date>", 	df.format(d));
 	String replace = convert.replace("<cs371server>", "Server: 	The Unfinished Server");
 	os.write(replace.getBytes());
 	}
   }
} 
