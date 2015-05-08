package im.youtiao.java_sdk;

import java.util.List;

import im.youtiao.java_sdk.core.Channel;
import im.youtiao.java_sdk.core.Feed;
import im.youtiao.java_sdk.core.Token;

/**
 * Hello world!
 *
 */
public class Test {
	public static void main(String[] args) {
		YTRequestConfig config = new YTRequestConfig("test");
		YTHost host = new YTHost("192.168.200.183:3000");
		YTClient client = new YTClient(config, null, host);
		try {
			Token token = client.signInUser("wangqiuping816@gmail.com", "12345678");
			
			client.setToken(token.getTokenType() + " " + token.getAccessToken());
			
			System.out.println(client.getToken());
			
			List<Channel> channels = client.listChannels();
			System.out.println("channels.size = " + channels.size());
			
			client.createChannel("test");
			channels = client.listChannels();
			System.out.println("channels.size = " + channels.size());
			
			//client.subscribeChannel(1);
			
			client.listFeeds(1);
			
			Feed feed = client.createFeed(1, "first feed");
			client.getFeed(1, feed.getId());
			
			client.feedbackFeed(1, feed.getId(), "cross");
			
		} catch (YTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
