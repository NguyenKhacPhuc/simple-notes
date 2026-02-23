package com.simplenotes.app

actual object Config {
    actual val supabaseUrl: String = BuildConfig.SUPABASE_URL
    actual val supabaseAnonKey: String = BuildConfig.SUPABASE_ANON_KEY
}
