import java.util.List;



public class Match {
	
	public long matchId;
	public long matchDuration;
	public List<ParticipantIdentity> participantIdentities;
	public List<Participant> participants;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (matchId ^ (matchId >>> 32));
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Match other = (Match) obj;
		if (matchId != other.matchId)
			return false;
		return true;
	}

	
}
