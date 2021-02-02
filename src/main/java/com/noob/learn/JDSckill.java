package com.noob.learn;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import lombok.Getter;

public class JDSckill {
	public static void main(String[] args) throws InterruptedException {
		String cookiesString = "__jdu=260770938; areaId=19";
		CloseableHttpClient HttpClient = HttpClientBuilder.create().setDefaultCookieStore(null).build();

		HttpPost httpPost = new HttpPost(
				"https://marathon.jd.com/seckillnew/orderService/pc/submitOrder.action?skuId=100012033476");

		// 转json参数
		String paramJson = JSON.toJSONString(new Data());
		StringEntity stringEntity = new StringEntity(paramJson, ContentType.create("text/json", "UTF-8"));
		httpPost.setEntity(stringEntity);
		httpPost.addHeader("Content-Type", "application/json");

		// form形式参数
		httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");
		httpPost.addHeader("Cookie", cookiesString);
		Set<Entry<String, Object>> entrySet = new Data().toMap().entrySet();
		List<NameValuePair> params = entrySet.stream()
				.map(t -> new BasicNameValuePair(String.valueOf(t.getKey()), String.valueOf(t.getValue())))
				.collect(Collectors.toList());
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			CloseableHttpResponse response = HttpClient.execute(httpPost);
			HttpEntity responseEntity = response.getEntity();
			System.out.println(response.getStatusLine() + EntityUtils.toString(responseEntity));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

@Getter
class Data {
	private long skuId = 100012043978L;
	private int num = 1;
	private long addressId = 1430968494;
	private boolean yuShou = true;
	private boolean isModifyAddress = false;
	private String name = "邹金儒";
	private int provinceId = 19;
	private int cityId = 1601;
	private int countyId = 3633;
	private int townId = 63251;
	private String addressDetail = "燕塘路燕塘大院燕塘路燕塘大院燕塘大院26号楼";
	private String mobile = "189****2471";
	private String mobileKey = "52f89a07644b2db544f66e77f389b8f4";
	private String email = null;
	private String postCode = null;
	private int invoiceTitle = 4;
	private String invoiceCompanyName = null;
	private int invoiceContent = 1;
	private String invoiceTaxpayerNO = null;
	private String invoiceEmail = null;
	private String invoicePhone = "189****2471";
	private String invoicePhoneKey = "52f89a07644b2db544f66e77f389b8f4";
	private boolean invoice = true;
	private String password = null;
	private int codTimeType = 3;
	private int paymentType = 4;
	private String areaCode = null;
	private int overseas = 0;
	private String phone = null;
	private String eid = "MC53DFTRBVAPFTIAS7VSS6EWXVDZXIYURCXACPSPZ6UA743R3CGXOBE6H64IT3E27Z6CC5XID3BTA42OGZJHJM4JF4";
	private String fp = "cd4d29213bea77b562c4fb336d21114c";
	private String token = "37bfa25c29f9d3f815d398fb7dd84ebf";
	private String pru = null;
	private String provinceName = "广东";
	private String cityName = "广州市";
	private String countyName = "天河区";
	private String townName = "兴华街道";

	public JSONObject toMap() {
		return (JSONObject) JSON.toJSON(this);
	}
}