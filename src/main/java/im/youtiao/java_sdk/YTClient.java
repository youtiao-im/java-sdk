package im.youtiao.java_sdk;

import im.youtiao.java_sdk.core.Channel;
import im.youtiao.java_sdk.core.Feed;
import im.youtiao.java_sdk.core.Feedback;
import im.youtiao.java_sdk.core.Token;
import im.youtiao.java_sdk.http.HttpRequestor;
import im.youtiao.java_sdk.http.HttpRequestor.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.type.TypeReference;

public class YTClient {

	private String token = null;
	private YTHost host = null;
	private YTRequestConfig requestConfig = null;
	
	private final String apiPrefix = "api/v1";

	public YTClient(YTRequestConfig requestConfig) {
		this(requestConfig, null);
	}

	public YTClient(YTRequestConfig requestConfig, String token) {
		this(requestConfig, token, YTHost.DEFAULT);
	}

	public YTClient(YTRequestConfig requestConfig, String token, YTHost host) {
		this.requestConfig = requestConfig;
		this.token = token;
		this.host = host;
	}

	public void setToken(String token) {
		this.token = token;
	}
	
	public String getToken() {
		return token;
	}

	public Token signInUser(String userName, String password) throws YTException {
		String apiPath = "oauth/token";
		Map<String, String> params = new HashMap<String, String>();
		params.put("grant_type", "password");
		params.put("username", userName);
		params.put("password", password);

		return this.doPost(apiPath, params, null, null, new YTRequestUtil.ResponseHandler<Token>() {
			@Override
			public Token handle(Response response) throws YTException {
				if (response.statusCode != 200) {
					throw YTRequestUtil.unexpectedStatus(response);
				}
				return YTRequestUtil.readJsonFromResponse(Token.class, response.body);
			}
		});
	}

	public List<Channel> listChannels() throws YTException {
		String apiPath =  apiPrefix + "/channels";
		return this.doGet(apiPath, null, null, new YTRequestUtil.ResponseHandler<List<Channel>>() {
			@Override
			public List<Channel> handle(Response response) throws YTException {
				if (response.statusCode != 200) {
					throw YTRequestUtil.unexpectedStatus(response);
				}
				return YTRequestUtil.readJsonFromResponse(new TypeReference<List<Channel>>() {
				}, response.body);
			}
		});
	}
	
	public Channel getChannel(String id) throws YTException {
		String apiPath = apiPrefix + "/channels/" + id;
		return this.doGet(apiPath, null, null, new YTRequestUtil.ResponseHandler<Channel>() {
			@Override
			public Channel handle(Response response) throws YTException {
				if (response.statusCode != 200) {
					throw YTRequestUtil.unexpectedStatus(response);
				}
				return YTRequestUtil.readJsonFromResponse(Channel.class, response.body);
			}
		});
	}
	
	public Channel createChannel(String name) throws YTException {
		String apiPath = apiPrefix + "/channels";
		Map<String, String> params = new HashMap<String, String>();
		params.put("name", name);
		return this.doPost(apiPath, params, null, null, new YTRequestUtil.ResponseHandler<Channel>() {
			@Override
			public Channel handle(Response response) throws YTException {
				if (response.statusCode != 200) {
					throw YTRequestUtil.unexpectedStatus(response);
				}
				return YTRequestUtil.readJsonFromResponse(Channel.class, response.body);
			}
		});
	}
	
	public Channel subscribeChannel(String id) throws YTException {
		String apiPath = apiPrefix + "/channels/" + id + "/join";
		return this.doPost(apiPath, null, null, null, new YTRequestUtil.ResponseHandler<Channel>() {
			@Override
			public Channel handle(Response response) throws YTException {
				if (response.statusCode != 200) {
					throw YTRequestUtil.unexpectedStatus(response);
				}
				return YTRequestUtil.readJsonFromResponse(Channel.class, response.body);
			}
		});
	}
	
	public List<Feed> listFeeds(String channelId) throws YTException {
		String apiPath = apiPrefix + "/channels/" + channelId + "/feeds";
		return this.doGet(apiPath, null, null, new YTRequestUtil.ResponseHandler<List<Feed>>() {
			@Override
			public List<Feed> handle(Response response) throws YTException {
				if (response.statusCode != 200) {
					throw YTRequestUtil.unexpectedStatus(response);
				}
				return YTRequestUtil.readJsonFromResponse(new TypeReference<List<Feed>>() {
				}, response.body);
			}
		});
	}
	
	public Feed getFeed(String channelId, String feedId) throws YTException {
		String apiPath = apiPrefix + "/channels/" + channelId + "/feeds/" + feedId;
		return this.doGet(apiPath, null, null, new YTRequestUtil.ResponseHandler<Feed>() {
			@Override
			public Feed handle(Response response) throws YTException {
				if (response.statusCode != 200) {
					throw YTRequestUtil.unexpectedStatus(response);
				}
				return YTRequestUtil.readJsonFromResponse(Feed.class, response.body);
			}
		});
	}
	
	public Feed createFeed(String channelId, String content) throws YTException {
		String apiPath = apiPrefix + "/channels/" + channelId + "/feeds";
		Map<String, String> params = new HashMap<String, String>();
		params.put("content", content);
		return this.doPost(apiPath, params, null, null, new YTRequestUtil.ResponseHandler<Feed>() {
			@Override
			public Feed handle(Response response) throws YTException {
				if (response.statusCode != 200) {
					throw YTRequestUtil.unexpectedStatus(response);
				}
				return YTRequestUtil.readJsonFromResponse(Feed.class, response.body);
			}
		});
	}
	
	public Feedback feedbackFeed(String channelId, String feedId, String sticker) throws YTException {
		String apiPath = apiPrefix + "/channels/" + channelId + "feeds/" + feedId + "/stamp";
		Map<String, String> params = new HashMap<String, String>();
		params.put("sticker", sticker);
		return this.doPost(apiPath, params, null, null, new YTRequestUtil.ResponseHandler<Feedback>() {
			@Override
			public Feedback handle(Response response) throws YTException {
				if (response.statusCode != 200) {
					throw YTRequestUtil.unexpectedStatus(response);
				}
				return YTRequestUtil.readJsonFromResponse(Feedback.class, response.body);
			}
		});
	}

	private <T> T doGet(String path, Map<String, String> params, List<HttpRequestor.Header> headers,
			YTRequestUtil.ResponseHandler<T> handler) throws YTException {
		return YTRequestUtil.doGet(this.requestConfig, this.host.api, path, this.token, params, headers, handler);
	}

	private <T> T doPost(String path, Map<String, String> params, List<HttpRequestor.Header> headers, String body,
			YTRequestUtil.ResponseHandler<T> handler) throws YTException {
		return YTRequestUtil
				.doPost(this.requestConfig, this.host.api, path, this.token, params, body, headers, handler);
	}
}
