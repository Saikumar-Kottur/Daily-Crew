package com.example.util

import com.example.data.model.AppLanguage

object DailyCrewLocalization {

    fun get(key: String, language: AppLanguage): String {
        return when (language) {
            AppLanguage.ENGLISH -> englishStrings[key] ?: key
            AppLanguage.TELUGU -> teluguStrings[key] ?: englishStrings[key] ?: key
            AppLanguage.HINDI -> hindiStrings[key] ?: englishStrings[key] ?: key
        }
    }

    private val englishStrings = mapOf(
        // Navigation
        "nav_shifts" to "Shifts",
        "nav_checkin" to "Check-In",
        "nav_wallet" to "Wallet",
        "nav_profile" to "Profile",
        "nav_dashboard" to "Dashboard",
        "nav_post_shift" to "Post Shift",
        "nav_marketplace" to "Marketplace",
        "nav_credibility" to "Credibility",
        "nav_moderation" to "Moderation",
        "nav_platform_settings" to "Settings",

        // Profile Tabs
        "tab_verification" to "Verification",
        "tab_history" to "History",
        "tab_badges" to "Badges",
        "tab_escrow" to "Escrow",
        "tab_settings" to "Settings",

        // Profile Metrics & Labels
        "metric_rating" to "Rating",
        "metric_shifts_done" to "Shifts Done",
        "metric_ontime" to "On-Time",
        "metric_trust_score" to "Trust Score",
        "btn_view_id_pass" to "View Verified Digital ID Pass",
        "standby_active" to "⚡ Standby Active: Ready for Instant Shifts",
        "standby_paused" to "Standby Paused (Tap to activate)",
        "member_since" to "Member since",

        // Settings Section
        "settings_title" to "Settings & Preferences",
        "language_settings_title" to "Language / భాష / भाषा",
        "language_settings_subtitle" to "Choose your preferred app language for shifts, check-in, and wallet",
        "lang_english_label" to "English",
        "lang_english_sub" to "Default Interface Language",
        "lang_telugu_label" to "తెలుగు (Telugu)",
        "lang_telugu_sub" to "తెలంగాణ & ఆంధ్రప్రదేశ్ ప్రాంతీయ భాష",
        "lang_hindi_label" to "हिन्दी (Hindi)",
        "lang_hindi_sub" to "राष्ट्रीय आतिथ्य मानक भाषा",

        "section_notifications" to "Shift & Payout Alerts",
        "opt_sms_alerts" to "Instant SMS Shift Alerts (Hyderabad Pilot)",
        "opt_push_notifs" to "Push Notifications for High-Wage Gigs",
        "opt_emergency_sound" to "Emergency 30-Min Shift Siren Alarm",

        "section_territory" to "Work Territory & GPS Radius",
        "label_pilot_zone" to "Current Operational Zone",
        "label_travel_radius" to "Maximum Commute Radius",

        "section_payout" to "Bank UPI Payout Account",
        "label_upi_id" to "Primary UPI Virtual Payment Address",
        "btn_save_preferences" to "Save Profile Settings",
        "settings_saved_success" to "Preferences and language updated successfully!",

        // Referral & Link Sharing
        "refer_title" to "Refer & Earn Rewards",
        "share_link_label" to "Share Link via Any App on Mobile",
        "download_app_link" to "App Download & Referral Link",
        "copy_link" to "Copy Link",
        "link_copied" to "Link Copied!",
        "copy_code" to "Copy Code",
        "code_copied" to "Code Copied!",
        "download_app_btn" to "Download DailyCrew App",
        "share_whatsapp" to "WhatsApp",
        "share_instagram" to "Instagram",
        "share_sms" to "SMS",
        "referral_perks" to "Referral Perks & Commission Rules"
    )

    private val teluguStrings = mapOf(
        // Navigation
        "nav_shifts" to "షిఫ్ట్‌లు",
        "nav_checkin" to "చెక్-ఇన్",
        "nav_wallet" to "వాలెట్",
        "nav_profile" to "ప్రొఫైల్",
        "nav_dashboard" to "డ్యాష్‌బోర్డ్",
        "nav_post_shift" to "షిఫ్ట్ పోస్ట్",
        "nav_marketplace" to "మార్కెట్‌ప్లేస్",
        "nav_credibility" to "నమ్మకం",
        "nav_moderation" to "మోడరేషన్",
        "nav_platform_settings" to "సెట్టింగ్‌లు",

        // Profile Tabs
        "tab_verification" to "ధృవీకరణ",
        "tab_history" to "చరిత్ర",
        "tab_badges" to "బ్యాడ్జీలు",
        "tab_escrow" to "ఎస్క్రో",
        "tab_settings" to "సెట్టింగ్‌లు",

        // Profile Metrics & Labels
        "metric_rating" to "రేటింగ్",
        "metric_shifts_done" to "పూర్తయిన షిఫ్ట్‌లు",
        "metric_ontime" to "సమయపాలన",
        "metric_trust_score" to "నమ్మకం స్కోర్",
        "btn_view_id_pass" to "ధృవీకరించబడిన డిజిటల్ ఐడి పాస్ చూడండి",
        "standby_active" to "⚡ స్టాండ్‌బై ఆన్‌లో ఉంది: తక్షణ షిఫ్ట్‌లకు సిద్ధం",
        "standby_paused" to "స్టాండ్‌బై పాజ్ చేయబడింది (ఆన్ చేయడానికి నొక్కండి)",
        "member_since" to "నుండి సభ్యుడు",

        // Settings Section
        "settings_title" to "సెట్టింగ్‌లు & ప్రాధాన్యతలు",
        "language_settings_title" to "యాప్ భాష మార్చుకోండి (Language)",
        "language_settings_subtitle" to "షిఫ్ట్‌లు, చెక్-ఇన్ మరియు వాలెట్ కోసం మీ ఇష్టమైన భాషను ఎంచుకోండి",
        "lang_english_label" to "English (ఇంగ్లీష్)",
        "lang_english_sub" to "డిఫాల్ట్ ఇంటర్‌ఫేస్ భాష",
        "lang_telugu_label" to "తెలుగు (Telugu)",
        "lang_telugu_sub" to "తెలంగాణ & ఆంధ్రప్రదేశ్ ప్రాంతీయ భాష (ఎంచుకోబడింది)",
        "lang_hindi_label" to "हिन्दी (హిందీ)",
        "lang_hindi_sub" to "జాతీయ ఆతిథ్య ప్రామాణిక భాష",

        "section_notifications" to "షిఫ్ట్ & చెల్లింపు హెచ్చరికలు",
        "opt_sms_alerts" to "తక్షణ ఎస్ఎంఎస్ షిఫ్ట్ హెచ్చరికలు (హైదరాబాద్ పైలట్)",
        "opt_push_notifs" to "అధిక వేతన షిఫ్ట్‌ల కోసం పుష్ నోటిఫికేషన్‌లు",
        "opt_emergency_sound" to "ఎమర్జెన్సీ 30-నిమిషాల షిఫ్ట్ సైరన్ అలారం",

        "section_territory" to "పని పరిధి & జీపీఎస్ వ్యాసార్థం",
        "label_pilot_zone" to "ప్రస్తుత ఆపరేషనల్ జోన్",
        "label_travel_radius" to "గరిష్ట ప్రయాణ దూరం",

        "section_payout" to "బ్యాంక్ యుపిఐ చెల్లింపు ఖాతా",
        "label_upi_id" to "ప్రధాన యుపిఐ వర్చువల్ చిరునామా",
        "btn_save_preferences" to "ప్రొఫైల్ సెట్టింగ్‌లను సేవ్ చేయండి",
        "settings_saved_success" to "భాష మరియు సెట్టింగ్‌లు విజయవంతంగా నవీకరించబడ్డాయి!",

        // Referral & Link Sharing (Telugu)
        "refer_title" to "రిఫర్ చేయండి & రివార్డులు పొందండి",
        "share_link_label" to "మొబైల్‌లోని ఏదైనా యాప్ ద్వారా లింక్ షేర్ చేయండి",
        "download_app_link" to "యాప్ డౌన్‌లోడ్ & రిఫరల్ లింక్",
        "copy_link" to "లింక్ కాపీ చేయండి",
        "link_copied" to "లింక్ కాపీ చేయబడింది!",
        "copy_code" to "కోడ్ కాపీ చేయండి",
        "code_copied" to "కోడ్ కాపీ చేయబడింది!",
        "download_app_btn" to "DailyCrew యాప్‌ను డౌన్‌లోడ్ చేయండి",
        "share_whatsapp" to "వాట్సాప్ (WhatsApp)",
        "share_instagram" to "ఇన్‌స్టాగ్రామ్ (Instagram)",
        "share_sms" to "ఎస్ఎంఎస్ (SMS)",
        "referral_perks" to "రిఫరల్ ప్రయోజనాలు & కమీషన్ నియమాలు"
    )

    private val hindiStrings = mapOf(
        // Navigation
        "nav_shifts" to "शिफ्ट्स",
        "nav_checkin" to "चेक-इन",
        "nav_wallet" to "वॉलेट",
        "nav_profile" to "प्रोफ़ाइल",
        "nav_dashboard" to "डैशबोर्ड",
        "nav_post_shift" to "शिफ्ट पोस्ट",
        "nav_marketplace" to "मार्केटप्लेस",
        "nav_credibility" to "विश्वसनीयता",
        "nav_moderation" to "मॉडरेशन",
        "nav_platform_settings" to "सेटिंग्स",

        // Profile Tabs
        "tab_verification" to "सत्यापन",
        "tab_history" to "इतिहास",
        "tab_badges" to "बैज",
        "tab_escrow" to "एस्क्रो",
        "tab_settings" to "सेटिंग्स",

        // Profile Metrics & Labels
        "metric_rating" to "रेटिंग",
        "metric_shifts_done" to "पूर्ण शिफ्ट्स",
        "metric_ontime" to "समय पर",
        "metric_trust_score" to "विश्वास स्कोर",
        "btn_view_id_pass" to "सत्यापित डिजिटल आईडी पास देखें",
        "standby_active" to "⚡ स्टैंडबाय सक्रिय: तत्काल शिफ्ट्स के लिए तैयार",
        "standby_paused" to "स्टैंडबाय रुका हुआ है (सक्रिय करने के लिए टैप करें)",
        "member_since" to "से सदस्य",

        // Settings Section
        "settings_title" to "सेटिंग्स और प्राथमिकताएं",
        "language_settings_title" to "ऐप भाषा बदलें (Language)",
        "language_settings_subtitle" to "शिफ्ट्स, चेक-इन और वॉलेट के लिए अपनी पसंदीदा भाषा चुनें",
        "lang_english_label" to "English (अंग्रेज़ी)",
        "lang_english_sub" to "डिफ़ॉल्ट इंटरफ़ेस भाषा",
        "lang_telugu_label" to "తెలుగు (तेलुगु)",
        "lang_telugu_sub" to "तेलंगाना और आंध्र प्रदेश क्षेत्रीय भाषा",
        "lang_hindi_label" to "हिन्दी (Hindi)",
        "lang_hindi_sub" to "राष्ट्रीय आतिथ्य मानक भाषा (चयनित)",

        "section_notifications" to "शिफ्ट और भुगतान सूचनाएं",
        "opt_sms_alerts" to "तत्काल एसएमएस अलर्ट (हैदराबाद पायलट ज़ोन)",
        "opt_push_notifs" to "उच्च वेतन वाली शिफ्ट्स के लिए पुश सूचनाएं",
        "opt_emergency_sound" to "आपातकालीन 30 मिनट शिफ्ट सायरन अलार्म",

        "section_territory" to "कार्य क्षेत्र और जीपीएस दायरा",
        "label_pilot_zone" to "वर्तमान कार्य क्षेत्र",
        "label_travel_radius" to "अधिकतम यात्रा दायरा",

        "section_payout" to "बैंक यूपीआई भुगतान खाता",
        "label_upi_id" to "प्राथमिक यूपीआई वीपीए आईडी",
        "btn_save_preferences" to "प्रोफ़ाइल सेटिंग्स सहेजें",
        "settings_saved_success" to "भाषा और प्राथमिकताएं सफलतापूर्वक अपडेट की गईं!",

        // Referral & Link Sharing (Hindi)
        "refer_title" to "रेफ़र करें और पुरस्कार अर्जित करें",
        "share_link_label" to "मोबाइल के किसी भी ऐप के माध्यम से लिंक शेयर करें",
        "download_app_link" to "ऐप डाउनलोड और रेफ़रल लिंक",
        "copy_link" to "लिंक कॉपी करें",
        "link_copied" to "लिंक कॉपी किया गया!",
        "copy_code" to "कोड कॉपी करें",
        "code_copied" to "कोड कॉपी किया गया!",
        "download_app_btn" to "DailyCrew ऐप डाउनलोड करें",
        "share_whatsapp" to "व्हाट्सएप (WhatsApp)",
        "share_instagram" to "इंस्टाग्राम (Instagram)",
        "share_sms" to "एसएमएस (SMS)",
        "referral_perks" to "रेफ़रल लाभ और कमीशन नियम"
    )
}
