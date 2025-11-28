package com.devinslick.homeassistantlocationproxy.data

import com.google.gson.annotations.SerializedName

/**
 * Models for Home Assistant state responses.
 */

data class HaStateResponse(
    @SerializedName("entity_id") val entity_id: String,
    @SerializedName("state") val state: String,
    @SerializedName("attributes") val attributes: HaAttributes,
    @SerializedName("last_updated") val last_updated: String
)

data class HaAttributes(
    @SerializedName("latitude") val latitude: Double?,
    @SerializedName("longitude") val longitude: Double?,
    @SerializedName("altitude") val altitude: Double?,
    @SerializedName("friendly_name") val friendly_name: String?
)
