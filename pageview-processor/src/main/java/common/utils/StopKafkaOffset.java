package common.utils;

public class StopKafkaOffset {
	final long maxKafkaOffset;
	final int partitionId;
	public StopKafkaOffset(long maxKafkaOffset, int partitionId) {
		super();
		this.maxKafkaOffset = maxKafkaOffset;
		this.partitionId = partitionId;
	}
	
	public long getMaxKafkaOffset() {
		return maxKafkaOffset;
	}
	public int getPartitionId() {
		return partitionId;
	}		
	public boolean shouldStop(int partitionId, long offset){
		return partitionId == this.partitionId && offset >= this.maxKafkaOffset;
	}
}
