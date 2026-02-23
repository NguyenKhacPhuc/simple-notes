package com.simplenotes.app.data.remote

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

fun createAppSupabaseClient(url: String, key: String) = createSupabaseClient(
    supabaseUrl = url,
    supabaseKey = key
) {
    install(Auth)
    install(Postgrest)
    install(Realtime)
}
