package com.beomsoo.sentencelibrary.sync

import java.net.URI

object OfficialHostPolicy {
    private val exact=setOf("jw.org","www.jw.org","wol.jw.org","b.jw-cdn.org")
    fun isAllowed(url:String):Boolean {
        return try {
            val uri=URI(url);val host=uri.host?.lowercase() ?: return false
            uri.scheme.equals("https",true) && (host in exact || host.endsWith(".jw.org") || host.endsWith(".jw-cdn.org"))
        } catch(_:Exception){ false }
    }
}
