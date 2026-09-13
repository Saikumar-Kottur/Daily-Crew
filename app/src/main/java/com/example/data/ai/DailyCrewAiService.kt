package com.example.data.ai

import com.example.data.model.JobCategory

data class AiGeneratedJobDraft(
    val title: String,
    val suggestedWage: Double,
    val description: String,
    val dressCode: String,
    val instructions: String,
    val keySkills: List<String>,
    val tips: String
)

object DailyCrewAiService {

    /**
     * Generates a complete, high-quality, professional job posting draft
     * tailored to Indian hospitality, catering, event, and warehouse requirements.
     */
    fun generateJobDraft(
        roleKeyword: String,
        category: JobCategory,
        zone: String = "Uppal"
    ): AiGeneratedJobDraft {
        val cleanKeyword = roleKeyword.trim().ifBlank { category.label }
        val wage = suggestFairWage(category, zone)

        val (description, dressCode, instructions, skills, tips) = when (category) {
            JobCategory.CATERING -> {
                Tuple5(
                    "Professional banquet steward needed for guest greeting, live buffet replenishment, and VIP table dining assistance. Must maintain high etiquette standards and ensure pristine cleanliness throughout the service.",
                    "Crisp White / Black Formal Shirt, Formal Dark Trousers, Polished Black Closed Shoes. Groomed hair required.",
                    "Report 15 minutes before shift start to Hall Captain at Main Entrance. Sign in via DailyCrew QR Scanner. Free meal provided post shift.",
                    listOf("Banquet Table Service", "Tray Handling", "Beverage Dispensing", "FSSAI Food Hygiene"),
                    "AI Tip: Catering stewards in $zone during weekend evenings have a 95% fill rate when wage is ₹${wage.toInt()}+."
                )
            }
            JobCategory.KITCHEN_HELPER -> {
                Tuple5(
                    "Fast-paced kitchen assistant for vegetable prep, meat washing, masala grinding, and aiding the head chef during rush hours. Ensure clean workstations and follow strict safety hygiene.",
                    "Neat dark T-shirt, jeans/work pants, closed non-slip rubber shoes. Apron & hairnet will be provided on site.",
                    "Enter through back service door. Report to Sous Chef. Keep phone in designated locker during active cook line hours.",
                    listOf("Knife Skills", "Vegetable Peeling", "Kitchen Sanitation", "Speed & Agility"),
                    "AI Tip: Kitchen assistants who arrive 10 minutes early earn high owner repeat hire badges."
                )
            }
            JobCategory.EVENT -> {
                Tuple5(
                    "Dynamic event host and delegate registration usher. Responsible for badge scanning, seating VIP guests, and guiding attendees across convention halls with clear communication.",
                    "Formal Navy Blazer or Black Suit, Formal Shoes, DailyCrew lanyard with ID pass. Professional posture.",
                    "Assemble at Registration Counter 1 for shift briefing. Maintain high energy and polite courtesy at all times.",
                    listOf("Guest Relations", "Crowd Coordination", "Badge QR Scanning", "English/Hindi/Telugu Fluency"),
                    "AI Tip: Premium event ushers receive instant tip payouts directly via UPI on checkout."
                )
            }
            JobCategory.CANTEEN -> {
                Tuple5(
                    "Corporate cafeteria food server and tray clearance attendant. Help manage student and employee lunch queues with fast, courteous portion serving and counter hygiene.",
                    "Company branded cap/apron provided. Wear neat dark pants and closed footwear.",
                    "Check in at Cafeteria Office at 11:45 AM. Ensure hand sanitization before entering buffet lines.",
                    listOf("Counter Serving", "Queue Management", "Tray Clearing", "Quick Service"),
                    "AI Tip: Steady shifts with guaranteed same-day instant settlement."
                )
            }
            JobCategory.HOUSEKEEPING -> {
                Tuple5(
                    "Thorough housekeeping attendant for hotel rooms, corridor sanitization, linen replenishment, and deep restroom hygiene. High attention to detail required.",
                    "Comfortable scrub / uniform pants, closed rubber soled shoes. Housekeeping smock provided.",
                    "Report to Floor Supervisor on Level 2. Follow DailyCrew sanitation checklist before checking out.",
                    listOf("Room Turn-down", "Linen Handling", "Chemical Safety", "Disinfection Protocol"),
                    "AI Tip: Senior citizen friendly shift with regular rest breaks."
                )
            }
            JobCategory.WAREHOUSE -> {
                Tuple5(
                    "Assistance with cargo unloading, parcel sorting, scanning barcode labels, and pallet staging. Moderate physical agility required.",
                    "Sturdy jeans/cargo pants, steel-toe or durable closed sneakers. Safety reflective vest provided.",
                    "Enter via Gate 4 logistics bay. Check in with Bay Manager. Bring reusable water bottle.",
                    listOf("Parcel Sorting", "Hand Truck Operation", "Barcode Scanning", "Physical Agility"),
                    "AI Tip: Night shifts include ₹150 night-differential surge allowance."
                )
            }
            JobCategory.RETAIL -> {
                Tuple5(
                    "Retail store display helper and customer cart attendant for weekend sales rush. Assist shoppers in locating items and maintain orderly shelf aisles.",
                    "Smart casual black polo and jeans. Comfortable walking shoes.",
                    "Report to Store Manager at Cash Counter 1. QR attendance scan upon arrival.",
                    listOf("Shelf Stocking", "Customer Assistance", "Merchandising", "Price Tagging"),
                    "AI Tip: Ideal for students and part-time weekend workers."
                )
            }
            JobCategory.DELIVERY -> {
                Tuple5(
                    "Dispatch assistant and local parcel runner to accompany vehicle driver and carry packages to customer doorsteps in $zone.",
                    "Comfortable casual clothing, weather-appropriate jacket, closed sports shoes.",
                    "Meet vehicle driver at fulfillment hub parking. Bring charged smartphone for delivery confirmations.",
                    listOf("Local Route Knowledge", "Package Care", "Customer Courtesy", "Verification OTP"),
                    "AI Tip: Standby workers within 2km get prioritized notification."
                )
            }
            JobCategory.CASHIER -> {
                Tuple5(
                    "Cashier and POS billing executive for busy dining shift. Input food orders accurately, accept UPI/card payments, and balance register at shift closure.",
                    "Smart neat casual shirt or polo, comfortable footwear.",
                    "Report to head cashier. Verify opening cash float. Zero tolerance for billing discrepancies.",
                    listOf("POS Software", "UPI & Card Billing", "Math Agility", "Customer Courtesy"),
                    "AI Tip: Verified Aadhaar candidates are strongly preferred for cash-handling roles."
                )
            }
            JobCategory.SERVING -> {
                Tuple5(
                    "Attentive table server and dining room assistant for fast turnaround customer service. Ensure prompt plate serving and continuous guest attention.",
                    "Clean white or dark shirt, formal black pants, black slip-resistant shoes.",
                    "Report to Floor Manager at reception. Follow table allocation guidelines.",
                    listOf("Food Service", "Order Taking", "Table Clearance", "Guest Courtesies"),
                    "AI Tip: High tips earned during peak meal hours."
                )
            }
            JobCategory.HOTEL -> {
                Tuple5(
                    "Hotel hospitality attendant for guest luggage assistance, lobby upkeep, and front-desk coordination support.",
                    "Formal hotel uniform or clean dark suit, formal shoes.",
                    "Report to Front Office Duty Manager at main lobby.",
                    listOf("Guest Luggage Handling", "Lobby Courtesy", "Concierge Assist", "Multilingual"),
                    "AI Tip: High repeat ratings lead to regular hotel shift allocations."
                )
            }
            JobCategory.RESTAURANT -> {
                Tuple5(
                    "Restaurant crew member assisting with busing tables, water and cutlery replenishment, and order expediting between kitchen and dining floor.",
                    "Neat dark collared shirt, black trousers, closed dark shoes.",
                    "Enter through service entry and report to Shift In-charge.",
                    listOf("Table Busing", "Glassware Handling", "Hygiene Standard", "Fast Paced"),
                    "AI Tip: Excellent shift for evening part-timers."
                )
            }
            JobCategory.CLEANER -> {
                Tuple5(
                    "Commercial facility cleaner responsible for thorough floor mopping, trash disposal, sanitation, and restroom hygiene maintenance.",
                    "Comfortable durable pants, waterproof slip-resistant boots/shoes. Protective gloves provided.",
                    "Report to Facilities Supervisor in maintenance bay.",
                    listOf("Deep Cleaning", "Chemical Dilution", "Waste Management", "Floor Maintenance"),
                    "AI Tip: Ideal for non-technical workers seeking steady earnings."
                )
            }
            else -> {
                Tuple5(
                    "Reliable temporary daily workforce assistant for general service and operations assistance at our facility.",
                    "Neat closed shoes and dark attire.",
                    "Report to venue manager upon arrival and scan QR code for attendance.",
                    listOf("Punctuality", "Teamwork", "Customer Service"),
                    "AI Tip: Clear instructions reduce worker questions and speed up arrival."
                )
            }
        }

        return AiGeneratedJobDraft(
            title = if (cleanKeyword.contains("helper", ignoreCase = true) ||
                cleanKeyword.contains("steward", ignoreCase = true) ||
                cleanKeyword.contains("usher", ignoreCase = true) ||
                cleanKeyword.contains("cashier", ignoreCase = true)
            ) cleanKeyword else "$cleanKeyword Assistant ($zone)",
            suggestedWage = wage,
            description = description,
            dressCode = dressCode,
            instructions = instructions,
            keySkills = skills,
            tips = tips
        )
    }

    /**
     * Calculates recommended fair wage benchmark for shift in Hyderabad zones.
     */
    fun suggestFairWage(category: JobCategory, zone: String, shiftHours: Int = 8): Double {
        val baseHourly = when (category) {
            JobCategory.EVENT -> 150.0
            JobCategory.CASHIER -> 120.0
            JobCategory.CATERING -> 110.0
            JobCategory.WAREHOUSE -> 115.0
            JobCategory.HOUSEKEEPING -> 100.0
            JobCategory.KITCHEN_HELPER -> 100.0
            JobCategory.DELIVERY -> 105.0
            JobCategory.RETAIL -> 100.0
            JobCategory.CANTEEN -> 95.0
            JobCategory.ALL -> 100.0
            else -> 100.0
        }

        val zoneMultiplier = when (zone.lowercase()) {
            "uppal" -> 1.0
            "habsiguda" -> 1.05
            "tarnaka" -> 1.08
            "nacharam" -> 1.02
            else -> 1.0
        }

        val rawWage = baseHourly * shiftHours * zoneMultiplier
        // Round to nearest 50
        return (Math.round(rawWage / 50.0) * 50).toDouble()
    }

    /**
     * AI Bio enhancer for workers to maximize their hire rate.
     */
    fun enhanceWorkerBio(name: String, skills: List<String>, yearsExp: String, role: String): String {
        val skillSummary = skills.take(3).joinToString(", ")
        return "Experienced $role with $yearsExp in high-demand environments. Specializing in $skillSummary. Committed to 100% on-time attendance, zero no-show record, and courteous service with verified credentials."
    }

    private data class Tuple5<A, B, C, D, E>(
        val a: A, val b: B, val c: C, val d: D, val e: E
    )
}
