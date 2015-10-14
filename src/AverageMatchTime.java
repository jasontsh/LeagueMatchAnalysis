import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import com.google.gson.Gson;


public class AverageMatchTime {

	private static final String matchListBySummoner = "https://na.api.pvp.net/api/lol/na/v2.2/matchlist/by-summoner/";
	private static final String matchListSpec = "?rankedQueues=RANKED_SOLO_5x5&seasons=SEASON2015&"
			+ MyConstants.apikey;
	private static final String matchString = "https://na.api.pvp.net/api/lol/na/v2.2/match/";
	private static final String matchSpec = "?" + MyConstants.apikey;
	private static long startId = MyConstants.startID;
	private static final int times = 2000;
	public static void main(String[] args) {
		Stack<Long> visited = new Stack<Long>();
		Stack<Long> next = new Stack<Long>();
		Set<Long> visitedSet = new HashSet<Long>();
		Set<Match> matches = new HashSet<Match>();
		visited.push(startId);
		visitedSet.add(startId);
		Gson gson = new Gson();
		long id = startId;
		BufferedWriter writer = null;
		File file = new File("records.txt");
		while(matches.size() < times){
			boolean added = false;
			Match match = null;
			try{
				MatchList matchlist = gson.fromJson(getJsonString(getMatchListUrl(id)), MatchList.class);
				if(matchlist.matches.size() < 1){
					if(!next.isEmpty()){
						id = next.pop();
					} else{
						break;
					}
					continue;
				}
				match = gson.fromJson(getJsonString(getMatchUrl(matchlist.matches.get(1).matchId)), Match.class);
				added = matches.add(match);
				for(int i = 0; i < 10; i++){
					try{
						long bufferid = match.participantIdentities.get(i).player.summonerId;
						if (!visitedSet.contains(bufferid)){
							next.add(bufferid);
							visitedSet.add(bufferid);
						}
					} catch (NullPointerException e){
						
					}
				}
			} catch(Exception e){
				if(!next.isEmpty()){
					id = next.pop();
				} else{
					break;
				}
				continue;
			}
			try{
				Thread.sleep(3000);
				if(added && match != null){
					System.out.println(match.matchDuration);
					writer = new BufferedWriter(new FileWriter(file, true));
					writer.write(match.matchDuration + "\n");
				}
			}catch (Exception e){
				
			}finally{
				try{
					writer.close();
				}catch(Exception e){
					
				}
			}
			if(!next.isEmpty()){
				id = next.pop();
			} else{
				break;
			}
		}
	}
	
	private static String getJsonString(String url){
		try{
			InputStream is = new URL(url).openStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			return readAll(rd);
		}catch(IOException e){
			
		}
		return "";
	}
	private static String readAll(Reader rd) throws IOException {
	    StringBuilder sb = new StringBuilder();
	    int cp;
	    while ((cp = rd.read()) != -1) {
	      sb.append((char) cp);
	    }
	    return sb.toString();
	}
	
	private static String getMatchListUrl(long id){
		StringBuilder sb = new StringBuilder();
		sb.append(matchListBySummoner);
		sb.append(id);
		sb.append(matchListSpec);
		return sb.toString();
	}
	
	private static String getMatchUrl(long id){
		StringBuilder sb = new StringBuilder();
		sb.append(matchString);
		sb.append(id);
		sb.append(matchSpec);
		return sb.toString();
	}
	

}
