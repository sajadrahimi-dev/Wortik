package com.wortik.app.ui.screens.explore

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsBike
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AccountTree
import androidx.compose.material.icons.rounded.AccessibilityNew
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.Architecture
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BakeryDining
import androidx.compose.material.icons.rounded.Balance
import androidx.compose.material.icons.rounded.BeachAccess
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.BusinessCenter
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CallSplit
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.Chair
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.CompareArrows
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.material.icons.rounded.DinnerDining
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.DirectionsTransit
import androidx.compose.material.icons.rounded.Diversity3
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.Egg
import androidx.compose.material.icons.rounded.Elderly
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.FamilyRestroom
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.FlutterDash
import androidx.compose.material.icons.rounded.Forest
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Handyman
import androidx.compose.material.icons.rounded.HealthAndSafety
import androidx.compose.material.icons.rounded.Hiking
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Hotel
import androidx.compose.material.icons.rounded.Icecream
import androidx.compose.material.icons.rounded.Kitchen
import androidx.compose.material.icons.rounded.Landscape
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.LocalFlorist
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.LocalPharmacy
import androidx.compose.material.icons.rounded.LocationCity
import androidx.compose.material.icons.rounded.Mood
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Park
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Pin
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Recycling
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.SetMeal
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.Shower
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.SportsSoccer
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material.icons.rounded.TheaterComedy
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.Thunderstorm
import androidx.compose.material.icons.rounded.Train
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Watch
import androidx.compose.material.icons.rounded.WavingHand
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.Whatshot
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.Work
import androidx.compose.ui.graphics.vector.ImageVector

fun categoryIconFor(categoryId: String): ImageVector = when (categoryId) {

    "numbers" -> Icons.Rounded.Pin
    "colors" -> Icons.Rounded.Palette
    "time" -> Icons.Rounded.Schedule
    "calendar" -> Icons.Rounded.CalendarMonth
    "seasons" -> Icons.Rounded.WbSunny
    "directions" -> Icons.Rounded.Explore
    "shapes" -> Icons.Rounded.Category
    "pronouns" -> Icons.AutoMirrored.Rounded.HelpOutline
    "prepositions" -> Icons.Rounded.AccountTree
    "conjunctions" -> Icons.Rounded.Link

    "house" -> Icons.Rounded.Home
    "furniture" -> Icons.Rounded.Chair
    "kitchen_tools" -> Icons.Rounded.Kitchen
    "clothing" -> Icons.Rounded.Checkroom
    "shopping" -> Icons.Rounded.ShoppingBag
    "cleaning" -> Icons.Rounded.CleaningServices
    "routine" -> Icons.Rounded.Alarm
    "accessories" -> Icons.Rounded.Watch
    "materials" -> Icons.Rounded.Layers
    "tools" -> Icons.Rounded.Handyman

    "fruits" -> Icons.Rounded.Eco
    "vegetables" -> Icons.Rounded.LocalFlorist
    "drinks" -> Icons.Rounded.LocalCafe
    "bakery" -> Icons.Rounded.BakeryDining
    "meat_fish" -> Icons.Rounded.SetMeal
    "dairy" -> Icons.Rounded.Egg
    "spices" -> Icons.Rounded.Whatshot
    "meals" -> Icons.Rounded.DinnerDining
    "restaurant" -> Icons.Rounded.Restaurant
    "sweets" -> Icons.Rounded.Cake

    "family" -> Icons.Rounded.FamilyRestroom
    "relationships" -> Icons.Rounded.Favorite
    "emotions" -> Icons.Rounded.Mood
    "personality" -> Icons.Rounded.Psychology
    "appearance" -> Icons.Rounded.Face
    "life_stages" -> Icons.Rounded.Elderly
    "greetings" -> Icons.Rounded.WavingHand
    "communication" -> Icons.Rounded.Forum
    "celebrations" -> Icons.Rounded.Celebration
    "society" -> Icons.Rounded.Diversity3

    "transport" -> Icons.Rounded.DirectionsTransit
    "airport" -> Icons.Rounded.Flight
    "train_station" -> Icons.Rounded.Train
    "hotel" -> Icons.Rounded.Hotel
    "city" -> Icons.Rounded.LocationCity
    "shops_services" -> Icons.Rounded.Storefront
    "driving" -> Icons.Rounded.DirectionsCar
    "vacation" -> Icons.Rounded.BeachAccess
    "world_borders" -> Icons.Rounded.Public
    "languages" -> Icons.Rounded.Language

    "weather" -> Icons.Rounded.Thunderstorm
    "landscape" -> Icons.Rounded.Landscape
    "pets" -> Icons.Rounded.Pets
    "wild_animals" -> Icons.Rounded.Forest
    "birds_insects" -> Icons.Rounded.FlutterDash
    "plants" -> Icons.Rounded.Park
    "space" -> Icons.Rounded.RocketLaunch
    "environment" -> Icons.Rounded.Recycling
    "elements" -> Icons.Rounded.Air
    "camping_outdoors" -> Icons.Rounded.Hiking

    "jobs" -> Icons.Rounded.Work
    "office" -> Icons.Rounded.BusinessCenter
    "school" -> Icons.Rounded.School
    "university" -> Icons.Rounded.Science
    "subjects" -> Icons.AutoMirrored.Rounded.MenuBook
    "computers" -> Icons.Rounded.Computer
    "internet" -> Icons.Rounded.Wifi
    "smartphone" -> Icons.Rounded.Smartphone
    "business" -> Icons.Rounded.TrendingUp
    "math_science" -> Icons.Rounded.Calculate

    "body_head" -> Icons.Rounded.Face
    "body_general" -> Icons.Rounded.AccessibilityNew
    "health_illness" -> Icons.Rounded.Thermostat
    "medical" -> Icons.Rounded.LocalHospital
    "sports" -> Icons.Rounded.SportsSoccer
    "fitness" -> Icons.Rounded.FitnessCenter
    "hygiene" -> Icons.Rounded.Shower
    "sleep" -> Icons.Rounded.Bedtime
    "nutrition" -> Icons.Rounded.Spa

    "music" -> Icons.Rounded.MusicNote
    "movies_tv" -> Icons.Rounded.Movie
    "books_reading" -> Icons.AutoMirrored.Rounded.MenuBook
    "art" -> Icons.Rounded.Brush
    "hobbies" -> Icons.Rounded.SportsEsports
    "photography" -> Icons.Rounded.CameraAlt
    "architecture" -> Icons.Rounded.Architecture
    "fashion" -> Icons.Rounded.Style
    "theater_stage" -> Icons.Rounded.TheaterComedy

    "politics" -> Icons.Rounded.AccountBalance
    "law" -> Icons.Rounded.Gavel
    "safety" -> Icons.Rounded.HealthAndSafety
    "religion_phil" -> Icons.Rounded.SelfImprovement
    "abstract_nouns" -> Icons.Rounded.Lightbulb
    "common_verbs_1" -> Icons.Rounded.Bolt
    "common_verbs_2" -> Icons.AutoMirrored.Rounded.DirectionsRun
    "separable_verbs" -> Icons.Rounded.CallSplit
    "adjectives_common" -> Icons.Rounded.AutoAwesome
    "opposites" -> Icons.Rounded.CompareArrows
    "feelings_verbs" -> Icons.Rounded.Psychology
    "social_verbs" -> Icons.Rounded.Groups

    else -> Icons.Rounded.AutoAwesome
}
