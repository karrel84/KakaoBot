package com.suyong.kakaobot.net.base;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageCache;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.NoCache;
import com.android.volley.toolbox.RequestFuture;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.SSLSocketFactory;

/**
 * <pre>
 * &lt;uses-permission android:name="android.permission.INTERNET" />
 * </pre>
 * */
public class Net {
	private static class InstanceHolder {
		static final RequestQueue mRequestQueue = new RequestQueue(new NoCache(), new BasicNetwork(new HurlStack(null, (SSLSocketFactory) SSLSocketFactory.getDefault())));
		static {
			mRequestQueue.start();
		}
	}

	public static interface OnNetResponse<T extends NetEnty> extends Response.Listener<T>, Response.ErrorListener {
		public void onErrorResponse(VolleyError error);
		public void onResponse(T response);
	}
	public static class OnSimpleNetResponse<T extends NetEnty> implements OnNetResponse<T> {
		@Override
		public void onErrorResponse(VolleyError error) {
		}
		@Override
		public void onResponse(T response) {
		}
	}
	public static void setCookieHandler() {
		CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
	}

	public static <T extends NetEnty> RequestFuture<T> sync(T enty) {
		RequestFuture<T> future = RequestFuture.newFuture();
		InstanceHolder.mRequestQueue.add(new NetRequest<T>(enty, future));

		return future;
	}
	public static <T extends NetEnty> T block(T enty) throws InterruptedException, ExecutionException {
		return sync(enty).get();
	}

	public static <T extends NetEnty> CallbackBuilder<T> async(T enty) {
		return new CallbackBuilder<T>(enty);
	}
	public static class CallbackBuilder<T extends NetEnty> {
		private T enty;
		public CallbackBuilder(T enty) {
			this.enty = enty;
		}
		public void setOnNetResponse(OnNetResponse<T> onNetResponse) {
			NetRequest<T> netRequest = new NetRequest<T>(enty, onNetResponse);
			InstanceHolder.mRequestQueue.add(netRequest);
		}
	}

	public static <T extends NetEnty> void async(T enty, OnNetResponse<T> onNetResponse) {
		NetRequest<T> netRequest = new NetRequest<T>(enty, onNetResponse);
		InstanceHolder.mRequestQueue.add(netRequest);
	}

	public static <T extends NetEnty> void sent(T enty) {
		async(enty, (OnNetResponse<T>) null);
	}

	public static void setImageSource(NetworkImageView niv, String url, ImageCache imageCache) {
		if (url.toLowerCase().matches("http(|s)://.*")) {
			niv.setImageUrl(url, new ImageLoader(InstanceHolder.mRequestQueue, imageCache));
		}
	}
}
