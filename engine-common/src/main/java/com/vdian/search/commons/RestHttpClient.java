package com.vdian.search.commons;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.vdian.ergate.meta.common.Annotations;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.http.*;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;

/**
 * User: xukun.fyp
 * Date: 17/4/28
 * Time: 14:44
 */
@Annotations.ThreadSafe
public class RestHttpClient {
	private static final Logger 				LOGGER = LoggerFactory.getLogger(RestHttpClient.class);
	public final static int 					CONNECTIONS_MAX_TOTAL = 20;
	public final static int 					CONNECTIONS_PER_ROUTE = 2;

	public final static int 					CONNECT_TIMEOUT = 1000;
	public final static int 					REQUEST_TIMEOUT = 1000;

	private HttpClient 							httpClient;
	private CookieStore 						cookieStore;


	public RestHttpClient() {
		this.cookieStore = new BasicCookieStore();
		this.httpClient = createPoolingHttpClient();
	}

	public RestHttpClient(CookieStore cookieStore){
		this.cookieStore = cookieStore;
		this.httpClient = createPoolingHttpClient();
	}

	private CloseableHttpClient createPoolingHttpClient() {
		PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager();
		manager.setMaxTotal(CONNECTIONS_MAX_TOTAL);
		manager.setDefaultMaxPerRoute(CONNECTIONS_PER_ROUTE);

		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(CONNECT_TIMEOUT).setConnectionRequestTimeout(REQUEST_TIMEOUT).setSocketTimeout(REQUEST_TIMEOUT).build();

		Collection<? extends Header> headers = Lists.newArrayList(new BasicHeader(HttpMethodParams.HTTP_CONTENT_CHARSET, Charsets.UTF_8.name()));

		return HttpClientBuilder.create().setDefaultCookieStore(cookieStore).setConnectionManager(manager).setDefaultRequestConfig(requestConfig).setDefaultHeaders(headers).build();
	}

	public String httpGet(URI uri) throws IOException {
		HttpGet httpGet = new HttpGet(uri);
		HttpResponse response = this.httpClient.execute(httpGet);
		LOGGER.info("http get:{}",uri.toString());
		try{
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				HttpEntity entity = response.getEntity();

				long length = entity.getContentLength();
				if (length != -1 && length < 2048) {
					return EntityUtils.toString(entity);
				}else {
					return EntityUtils.toString(new BufferedHttpEntity(entity));
				}
			} else {
				throw new HttpResponseException(response.getStatusLine().getStatusCode(), "failed to get from uri:" + uri.toString());
			}
		}finally {
			EntityUtils.consumeQuietly(response.getEntity());
		}
	}


	public String httpPost(URI uri, HttpEntity httpEntity) throws IOException {
		HttpPost httpPost = new HttpPost(uri);
		httpPost.setEntity(httpEntity);


		HttpResponse response = this.httpClient.execute(httpPost);
		try{
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode >= HttpStatus.SC_OK && statusCode <= HttpStatus.SC_MULTI_STATUS) {
				HttpEntity entity = response.getEntity();

				long length = entity.getContentLength();
				if (length != -1 && length < 2048) {
					return EntityUtils.toString(entity);
				}else {
					return EntityUtils.toString(new BufferedHttpEntity(entity));
				}
			} else {
				throw new HttpResponseException(response.getStatusLine().getStatusCode(), "failed to post from uri:" + uri.toString());
			}
		}finally {
			EntityUtils.consumeQuietly(response.getEntity());
		}
	}

	public String httpPost(URI uri, String key, String requestParam) throws IOException {
		NameValuePair pair = new BasicNameValuePair(key, requestParam);
		HttpEntity entity = new UrlEncodedFormEntity(Lists.newArrayList(pair));
		return httpPost(uri, entity);
	}


	public String httpPost(URI uri, String requestBody) throws IOException {
		StringEntity entity = new StringEntity(requestBody, ContentType.APPLICATION_JSON);
		return httpPost(uri, entity);
	}


	public String httpGetSilent(URI uri){
		try{
			return httpGet(uri);
		}catch (Exception e){
			LOGGER.error("httpGet failed uri:" + uri,e);
		}
		return null;
	}

	public String httpPostSilent(URI uri, HttpEntity httpEntity){
		try{
			return httpPost(uri, httpEntity);
		}catch (Exception e){
			LOGGER.error("httpPost failed uri:" + uri + ";httpEntity:" + httpEntity,e);
			return null;
		}
	}

	public String httpPostSilent(URI uri, String requestBody){
		try{
			return httpPost(uri, requestBody);
		}catch (Exception e){
			LOGGER.error("httpPost failed uri:" + uri + ";requestBody:" + requestBody,e);
			return null;
		}
	}

}