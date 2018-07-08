/*
 * Copyright 2011-2015. by Koudai Corporation.
 *
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Koudai Corporation ("Confidential Information"). You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Koudai.
 */

package com.vdian.search.commons;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author chenlinbin
 * @create 2015年10月8日 下午3:47:34
 */
public class NetworkUtils {

	private static String[] getLocalHostNames() {
		final Set<String> hostNames = new HashSet<String>();
		// we add localhost to this set manually, because if the ip 127.0.0.1 is
		// configured with more than one name in the /etc/hosts, only the first
		// name
		// is returned
		try {
			final Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
			for (final Enumeration ifaces = networkInterfaces; ifaces.hasMoreElements();) {
				final NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
				InetAddress ia = null;
				for (final Enumeration ips = iface.getInetAddresses(); ips.hasMoreElements();) {
					ia = (InetAddress) ips.nextElement();
					hostNames.add(ia.getCanonicalHostName());
					hostNames.add(ipToString(ia.getAddress()));
				}
			}
		} catch (final SocketException e) {
			throw new RuntimeException("unable to retrieve host names of localhost");
		}
		return hostNames.toArray(new String[hostNames.size()]);
	}

	private static String ipToString(final byte[] bytes) {
		final StringBuffer addrStr = new StringBuffer();
		for (int cnt = 0; cnt < bytes.length; cnt++) {
			final int uByte = bytes[cnt] < 0 ? bytes[cnt] + 256 : bytes[cnt];
			addrStr.append(uByte);
			if (cnt < 3)
				addrStr.append('.');
		}
		return addrStr.toString();
	}

	private static boolean ipvalid(String ipAddress) {
		String ip = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
		Pattern pattern = Pattern.compile(ip);
		Matcher matcher = pattern.matcher(ipAddress);
		return matcher.matches();
	}

	private static String[] getValidHost() {
		String[] ips = getLocalHostNames();
		List<String> ipList = new ArrayList<String>();
		for (String temp : ips)
			if (temp != null && ipvalid(temp) && !temp.equals("127.0.0.1"))
				ipList.add(temp);
		String[] result = new String[ipList.size()];
		int i = 0;
		for (String temp : ipList)
			result[i++] = temp;
		return result;
	}

	/**
	 * 有 vpn 可能有多个, 有虚拟机也有多个. 也慢
	 * @deprecated use {@link #localIp()}
	 */
	public static String getHostIps() {
		String result = "";
		String[] ips = getValidHost();
		for (String temp : ips)
			if (result.equals(""))
				result = temp;
			else
				result += "," + temp;
		return result;
	}

	/**
	 * 有 vpn 时返回 127.0.0.1
	 */
	public static String getLocalHostAddress() {
		try {
			InetAddress addr = InetAddress.getLocalHost();
			return addr.getHostAddress();
		} catch (UnknownHostException e) {
			throw new RuntimeException("InetAddress.getLocalHost() fail!", e);
		}
	}

	private static final String LOCAL_IP = "local.ip";
	private static final String LOCAL_IP_NAME = "local.ip.name";
	private static final String LINUX_IP_NAME = "eth0";
	private static final String MAC_IP_NAME = "en0";

	/**
	 * 可以 -Dlocal.ip=iphost 来取得 ip.
	 * 可以 -Dlocal.ip.name=eth0 来指定名来取 ip.
	 * @return 依次 'eth0', 'en0' 来取 ip.
	 */
	public static String localIp() {
		String ip = System.getProperty(LOCAL_IP);
		if(ip != null && !ip.isEmpty()) {
			return ip;
		}
		Map<String, String> ips = localNamedIp();
		if(ips.size() <1) {
			throw new RuntimeException("Not Found Local Ip, names map is blank");
		}
		String name = System.getProperty(LOCAL_IP_NAME);
		if(name != null && !name.isEmpty()) {
			ip = ips.get(name);
			if(ip == null) {
				throw new RuntimeException("Not Found Local Ip by system param '"+LOCAL_IP_NAME+"'");
			}
			return ip;
		}
		ip = ips.get(LINUX_IP_NAME);
		if(ip != null) {
			return ip;
		}
		ip = ips.get(MAC_IP_NAME);
		if(ip != null) {
			return ip;
		}
		//TODO win?
		return ips.values().iterator().next();
	}

	public static Map<String, String> localNamedIp() {
		Map<String, String> ipMap = new HashMap<>();
		try {
			Enumeration<NetworkInterface>  netInterfaces = NetworkInterface.getNetworkInterfaces();
			while (netInterfaces.hasMoreElements()) {
				NetworkInterface ni = netInterfaces.nextElement();
				Enumeration<InetAddress> ips = ni.getInetAddresses();
				while (ips.hasMoreElements()) {
					String ip = ips.nextElement().getHostAddress();
					if(ip.contains(":")) {
						continue;
					}
					if("127.0.0.1".equals(ip)) {
						continue;
					}
					ipMap.put(ni.getName(), ip);
				}
			}
		} catch (SocketException e) {
			throw new RuntimeException("final local ip fail!", e);
		}
		return ipMap;
	}
}
