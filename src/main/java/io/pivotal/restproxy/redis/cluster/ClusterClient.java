package io.pivotal.restproxy.redis.cluster;

import com.lambdaworks.redis.RedisURI;
import com.lambdaworks.redis.cluster.RedisClusterClient;
import com.lambdaworks.redis.cluster.models.partitions.Partitions;
import com.lambdaworks.redis.resource.ClientResources;

public class ClusterClient extends RedisClusterClient {

	protected ClusterClient(ClientResources clientResources,
			Iterable<RedisURI> redisURIs) {
		super(clientResources, redisURIs);
		
	}
	
	
	
	public int getChannelCount() {
		return super.getChannelCount();
	}
	
	public int getResourceCount() {
		return super.getResourceCount();
	}
	

}
