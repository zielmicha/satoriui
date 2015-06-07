package net.atomshare.satori.gui;//package net.atomshare.satori.cli;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.*;


public class HttpBasicAuth {

    String login = "farqd";
    String password = "korwinkrul";
    String encoding;

    public void listContests()
    {
        JSONArray clist = new JSONArray();

        try {
            URL url = new URL ("https://satori.atomshare.net/contests"); // "http://ip:port/login"
            //String encoding = Base64.encodeBase64String(  (login+":"+password).getBytes(Charset.forName("UTF-8")));

            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setRequestProperty  ("Authorization", "Basic " + encoding);
            InputStream content = (InputStream)connection.getInputStream();
            BufferedReader in   =
                    new BufferedReader (new InputStreamReader (content));
            String line;

            line=in.readLine();
            clist= (JSONArray) JSONValue.parse(line);

        } catch(Exception e) {
            e.printStackTrace();
        }

        for(int i=0 ;i<clist.size(); i++)
        {
            JSONObject K = (JSONObject) clist.get(i);
            if(K.get("contestant") != null) {
                JSONObject cont = (JSONObject) K.get("contest");
                if((Boolean)cont.get("archived")==false)
                System.out.println(cont.get("name"));
            }
        }
    }

    public void submit(JSONObject con, String file)
    {

    }

    public JSONObject findContest(String name)
    {
        JSONArray clist = new JSONArray();

        try {
            URL url = new URL ("https://satori.atomshare.net/contests"); // "http://ip:port/login"
            //String encoding = Base64.encodeBase64String((login+":"+password).getBytes(Charset.forName("UTF-8")));

            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setRequestProperty  ("Authorization", "Basic " + encoding);
            InputStream content = (InputStream)connection.getInputStream();
            BufferedReader in   =
                    new BufferedReader (new InputStreamReader (content));
            String line;

            line=in.readLine();
            clist= (JSONArray) JSONValue.parse(line);

        } catch(Exception e) {
            e.printStackTrace();
        }

        for(int i=0 ;i<clist.size(); i++)
        {
            //if(clist[i].get(i))
            JSONObject K = (JSONObject) clist.get(i);
            //  String name = K.get("name").getClass();
            if(K.get("contestant") != null) {
                JSONObject cont = (JSONObject) K.get("contest");
                if((Boolean)cont.get("archived")==false)
                {
                    Boolean B = true;
                    String fullName = (String)cont.get("name");
                    if(name.length()> fullName.length())
                        continue;
                    for(int ii=0; ii<name.length(); ii++)
                    {
                        if(name.charAt(ii)!=fullName.charAt(ii))
                            B = false;
                    }
                    if(B)
                    return cont;
                }
            }
            //System.out.println(K.get("contest").getClass());
        }

        return null;
    }

    public void listProblems(Long contestID)
    {
       // System.out.println(contestID);
        JSONArray plist = new JSONArray();
        try {
            URL url = new URL ("https://satori.atomshare.net/problems/"+contestID); // "http://ip:port/login"
            //String encoding = Base64.encodeBase64String((login+":"+password).getBytes(Charset.forName("UTF-8")));
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setRequestProperty  ("Authorization", "Basic " + encoding);
            InputStream content = (InputStream)connection.getInputStream();
            BufferedReader in   =
                    new BufferedReader (new InputStreamReader (content));
            String line;

            line=in.readLine();
            plist= (JSONArray) JSONValue.parse(line);

        } catch(Exception e) {
            e.printStackTrace();
        }


        for(int i=0 ;i<plist.size(); i++)
        {

            JSONObject K = (JSONObject) plist.get(i);
            JSONObject problem = (JSONObject) K.get("problem_mapping");
            System.out.println(problem.get("title") );

        }
    }

    public void Login()  {

        Console console = System.console();
        login = console.readLine("Username: ");
        char[] PASS = console.readPassword("Password: ");
        password = new String(PASS);

        encoding = Base64.encodeBase64String(  (login+":"+password).getBytes(Charset.forName("UTF-8")));

        PrintWriter out = null;
        try {
            out = new PrintWriter("file.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        out.println(encoding);
        out.close();

    }

    public void lastResult(Long contestID)
    {
        JSONArray rlist = new JSONArray();
        try {
            URL url = new URL ("https://satori.atomshare.net/results/"+contestID); // "http://ip:port/login"
            //String encoding = Base64.encodeBase64String((login+":"+password).getBytes(Charset.forName("UTF-8")));
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setRequestProperty  ("Authorization", "Basic " + encoding);
            InputStream content = (InputStream)connection.getInputStream();
            BufferedReader in   =
                    new BufferedReader (new InputStreamReader (content));
            String line;

            line=in.readLine();
            rlist= (JSONArray) JSONValue.parse(line);

        } catch(Exception e) {
            e.printStackTrace();
        }

            JSONObject K = (JSONObject) rlist.get(0);

            JSONObject prob = (JSONObject) K.get("problem_mapping");
            System.out.print(prob.get("code") + " " + prob.get("title") + " ");
            System.out.println(K.get("status"));


    }

    public void printHelp()
    {
        System.out.println("# Show help");
        System.out.println("satori help\n");


        System.out.println("# List contests");
        System.out.println("satori contests\n");

        System.out.println("# List problems");
        System.out.println("satori problems MP\n");

        System.out.println("# Submit soluion");
        System.out.println("satori submit ID C C.sql\n");

        System.out.println("# Show result");
        System.out.println("satori result ID\n");

        System.out.println("# Change saved login and password");
        System.out.println("satori login\n");


    }

    public String loadFile()
    {
        Boolean foundFile = true;

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("file.txt"));
        } catch (FileNotFoundException e) {
           return null;
        }
        if(foundFile)
            try {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();
                return line;
            } catch (IOException e) {
                return null;

            } finally {
                try {
                    br.close();
                } catch (IOException e) {
                    return null;
                }
            }
        return null;
    }

    public static void main(String[] args) {
        HttpBasicAuth CLI = new HttpBasicAuth();


        if(args[0].equals("login")){
            CLI.Login();
            return;
        }
        if(args[0].equals("help")){
            CLI.printHelp();
            return;
        }

        String loadedString = CLI.loadFile();
        if(loadedString == null)
            CLI.Login(); // reading login and password from standard input
        else
            CLI.encoding = loadedString;



        if(args[0].equals("contests"))
        {CLI.listContests(); return;}

        if(args[0].equals("submit"))
        {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("SUBMITED");
            return;
            /*JSONObject con = CLI.findContest(args[1]);
            if(con==null)
            {
               System.out.println("WRONG CONTEST NAME");
                return;
            }
            CLI.submit(con, args[2]);*/
        }

        if(args[0].equals("problems"))
        {
           // JSONObject con = CLI.findContest(args[1]);
            JSONObject con = CLI.findContest(args[1]);
            CLI.listProblems((Long) con.get("id"));
        }

        if(args[0].equals("result"))
        {
            JSONObject con = CLI.findContest(args[1]);
            CLI.lastResult((Long) con.get("id"));
        }

    }

}
