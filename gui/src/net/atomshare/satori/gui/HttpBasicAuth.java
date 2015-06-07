package net.atomshare.satori.gui;//package net.atomshare.satori.cli;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.*;


public class HttpBasicAuth {

    String login = "farqd";
    String password = "korwinkrul";

    public void listContests()
    {
        JSONArray clist = new JSONArray();

        try {
            URL url = new URL ("https://satori.atomshare.net/contests"); // "http://ip:port/login"
            String encoding = Base64.encodeBase64String(  (login+":"+password).getBytes(Charset.forName("UTF-8")));

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
                System.out.println(cont.get("name"));
            }
            //System.out.println(K.get("contest").getClass());
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
            String encoding = Base64.encodeBase64String((login+":"+password).getBytes(Charset.forName("UTF-8")));

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
            String encoding = Base64.encodeBase64String((login+":"+password).getBytes(Charset.forName("UTF-8")));
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
        System.out.println("GIFF LOGIN...");
        BufferedReader br =
                new BufferedReader(new InputStreamReader(System.in));
        String input = "";
        try {
            input=br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        login = input;

        System.out.println("GIFF PASSWORD...");

        try {
            input=br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        password = input;

    }

    public static void main(String[] args) {
        HttpBasicAuth CLI = new HttpBasicAuth();


        String[] AR = new String[5];
        AR[0]="contests";
        AR[1]="MP";
       // CLI.Login(); // reading login and password from standard input


        if(AR[0]=="contests")
        {CLI.listContests(); return;}

        if(AR[0]=="submit")
        {
            JSONObject con = CLI.findContest(args[1]);
            if(con==null)
            {
               System.out.println("WRONG CONTEST NAME");
                return;
            }
            CLI.submit(con, args[2]);
        }

        if(AR[0]=="problems")
        {
           // JSONObject con = CLI.findContest(args[1]);
            JSONObject con = CLI.findContest(AR[1]);
            CLI.listProblems((Long) con.get("id"));
        }

    }

}
