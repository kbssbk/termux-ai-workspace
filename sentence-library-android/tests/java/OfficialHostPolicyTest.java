package com.beomsoo.sentenceapp;

public class OfficialHostPolicyTest {
  private static void ok(boolean value, String name){ if(!value) throw new AssertionError(name); }
  private static void no(boolean value, String name){ if(value) throw new AssertionError(name); }
  public static void main(String[] args){
    ok(OfficialHostPolicy.isAllowed("https://www.jw.org/ko/"), "www.jw.org");
    ok(OfficialHostPolicy.isAllowed("https://wol.jw.org/ko/wol/h/r8/lp-ko"), "wol.jw.org");
    no(OfficialHostPolicy.isAllowed("https://b.jw-cdn.org/apis/pub-media/x"), "jw-cdn excluded by source policy");
    no(OfficialHostPolicy.isAllowed("http://www.jw.org/ko/"), "http rejected");
    no(OfficialHostPolicy.isAllowed("https://evil.example/jw.org"), "foreign host rejected");
    no(OfficialHostPolicy.isAllowed("https://jw.org.evil.example/x"), "suffix trick rejected");
    System.out.println("PASS OfficialHostPolicy");
  }
}
