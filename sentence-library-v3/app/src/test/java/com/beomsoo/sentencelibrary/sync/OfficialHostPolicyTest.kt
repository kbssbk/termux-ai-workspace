package com.beomsoo.sentencelibrary.sync

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OfficialHostPolicyTest {
    @Test fun only_official_hosts_are_allowed() {
        assertTrue(OfficialHostPolicy.isAllowed("https://www.jw.org/ko/라이브러리/"))
        assertTrue(OfficialHostPolicy.isAllowed("https://wol.jw.org/ko/wol/h/r8/lp-ko"))
        assertTrue(OfficialHostPolicy.isAllowed("https://b.jw-cdn.org/apis/pub-media/"))
        assertFalse(OfficialHostPolicy.isAllowed("https://example.com/jw.org"))
        assertFalse(OfficialHostPolicy.isAllowed("http://jw.org/"))
    }
}
