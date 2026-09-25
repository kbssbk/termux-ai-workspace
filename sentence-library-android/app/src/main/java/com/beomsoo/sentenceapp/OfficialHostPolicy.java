package com.beomsoo.sentenceapp;

import java.net.URI;

public final class OfficialHostPolicy {
  private OfficialHostPolicy() {}

  public static boolean isAllowed(String rawUrl) {
    try {
      URI uri = URI.create(rawUrl);
      if (!"https".equalsIgnoreCase(uri.getScheme())) return false;
      String host = uri.getHost();
      if (host == null) return false;
      host = host.toLowerCase();
      return host.equals("jw.org") || host.endsWith(".jw.org");
    } catch (Exception e) {
      return false;
    }
  }
}
