package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class QueryDatabase {
	public static JSONObject getJsonFromUrl(String urlStr) throws IOException, JSONException {
		URL url = new URL(urlStr);
		URLConnection conn = url.openConnection();
		BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		
		StringBuilder sb = new StringBuilder();
		String inLine = null;
		while ((inLine = br.readLine()) != null) {
			sb.append(inLine);
		}
		
		return new JSONObject(sb.toString());
	}
	
	public static boolean hasNext(JSONObject obj) {
		try {
			obj.getJSONObject("query-continue");
			return true;
		} catch (JSONException e) {
			return false;
		}
	}
	
	public static void main(String[] args) throws IOException, JSONException {
		String baseURL = "http://wiki.linked.earth/wiki/api.php?action=query&list=categorymembers&cmlimit=500&format=json&rawcontinue&cmtitle=Category:";
		String catagory = args[0];
		catagory = catagory.replaceAll(" ", "_");
		
		FileWriter outFile = new FileWriter(args[1]);
		BufferedWriter out = new BufferedWriter(outFile);
		
		JSONObject query = getJsonFromUrl(baseURL + catagory);
		while (hasNext(query)) {
			JSONArray queryMembers = query.getJSONObject("query").getJSONArray("categorymembers");
			for (int i = 0; i < queryMembers.length(); ++i) {
				JSONObject member = (JSONObject) queryMembers.get(i);
				String memberStr = member.getString("title");
				out.write(memberStr + "\n");
			}
			
			String cont = URLEncoder.encode(query.getJSONObject("query-continue").getJSONObject("categorymembers").getString("cmcontinue"), "UTF-8");
			query = getJsonFromUrl(baseURL + catagory + "&rawcontinue&cmcontinue=" + cont);
		}
		JSONArray queryMembers = query.getJSONObject("query").getJSONArray("categorymembers");
		for (int i = 0; i < queryMembers.length(); ++i) {
			JSONObject member = (JSONObject) queryMembers.get(i);
			String memberStr = member.getString("title");
			out.write(memberStr + "\n");
		}
		out.flush();
	}
}
