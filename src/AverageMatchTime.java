import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


public class AverageMatchTime {

	private static final String matchListBySummoner = "https://na.api.pvp.net/api/lol/na/v2.2/matchlist/by-summoner/";
	private static final String matchListSpec = "?rankedQueues=RANKED_SOLO_5x5&seasons=SEASON2015&"
			+ MyConstants.apikey;
	private static final String matchString = "https://na.api.pvp.net/api/lol/na/v2.2/match/";
	private static final String matchSpec = "?" + MyConstants.apikey;
	private static final String rankString = "https://na.api.pvp.net/api/lol/na/v2.5/league/by-summoner/";
	private static final String rankEntry = "/entry";
	private static final String champString = "https://na.api.pvp.net/api/lol/na/v1.3/stats/by-summoner/";
	private static final String champSpec = "/ranked?season=SEASON2015&" + MyConstants.apikey;
	private static final int times = 499;
	public static void main(String[] args) {
		Stack<Long> visited = new Stack<Long>();
		Stack<Long> next = new Stack<Long>();
		Set<Long> visitedSet = new HashSet<Long>();
		Set<Match> matches = new HashSet<Match>();
		next.push(MyConstants.startBronzeID);
//		next.push(MyConstants.startPlatID);
//		next.push(MyConstants.startGSID);
	//	visited.push(MyConstants.startDiaID);
		visited.push(MyConstants.startBronzeID);
		visitedSet.add(MyConstants.startBronzeID);
	//	visitedSet.add(MyConstants.startDiaID);
		Gson gson = new Gson();
		long id = MyConstants.startBronzeID;
		BufferedWriter writer = null;
		HashMap<String, Integer> count = new HashMap<>();
		
		do{
			boolean added = false;
			Match match = null;
			String rank = null;
			int champid;
			double kda;
			boolean win;
			try{
				Type rankType = new TypeToken<Map<Long, List<Rank>>>(){}.getType();
				Map<Long, List<Rank>> ranks = gson.fromJson(getJsonString(getRankUrl(id)), rankType);
				for(List<Rank> l: ranks.values()){
					for(Rank r: l){
						if(r.queue.equals("RANKED_SOLO_5x5")){
							rank = r.tier;
						}
					}
				}
				if(count.containsKey(rank) && count.get(rank) > times || rank == null || rank.equals("null")){
					if(!next.isEmpty()){
						id = next.pop();
					} else{
						break;
					}
					Thread.sleep(4200);
					continue;
				}
				MatchList matchlist = gson.fromJson(getJsonString(getMatchListUrl(id)), MatchList.class);
				if(matchlist.matches.size() < 1){
					if(!next.isEmpty()){
						id = next.pop();
					} else{
						break;
					}
					Thread.sleep(4200);
					continue;
				}
				match = gson.fromJson(getJsonString(getMatchUrl(matchlist.matches.get(1).matchId)), Match.class);
				added = matches.add(match);
				Participant participant = null;
				int participantId = 0;
				for(ParticipantIdentity p: match.participantIdentities){
					if(p.player.summonerId == id){
						participantId = p.participantId;
					}
				}
				if(participantId == 0){
					System.out.println("Error finding participant ID");
					if(!next.isEmpty()){
						id = next.pop();
					} else{
						break;
					}
					Thread.sleep(4200);
					continue;
				}
				for(Participant p: match.participants){
					if(p.participantId == participantId){
						participant = p;
					}
				}
				if(participant == null){
					System.out.println("Error finding participant");
					if(!next.isEmpty()){
						id = next.pop();
					} else{
						break;
					}
					Thread.sleep(4200);
					continue;
				}
				win = participant.stats.winner;
				champid = participant.championId;
				//kda is kills + assist and then divide by deaths
				kda = ((double)(participant.stats.assists + participant.stats.kills))/participant.stats.deaths;
				//add api call here
				
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
				try {
					Thread.sleep(4200);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				continue;
			}
			try{
				Thread.sleep(4200);
				if(added && match != null){
					System.out.println(match.matchDuration + " " + rank);
					writer = new BufferedWriter(new FileWriter(new File(rank+"records.txt"), true));
					writer.write(match.matchDuration + "\n");
					if(count.containsKey(rank)){
						count.put(rank, count.get(rank) + 1);
					} else{
						count.put(rank, 1);
					}
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
		} while(loop(count));
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
	
	private static String getRankUrl(long id){
		StringBuilder sb = new StringBuilder();
		sb.append(rankString);
		sb.append(id);
		sb.append(rankEntry);
		sb.append(matchSpec);
		return sb.toString();
	}
	
	private static String getChampStatUrl(long id){
		StringBuilder sb = new StringBuilder();
		sb.append(champString);
		sb.append(id);
		sb.append(champSpec);
		return sb.toString();
	}
	
	private static boolean loop(Map<String, Integer> count){
		for(Map.Entry<String, Integer> entry: count.entrySet()){
			if(entry.getValue() < times){
				return true;
			}
		}
		return false;
	}
	

}
