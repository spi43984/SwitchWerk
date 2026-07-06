package de.piecha.switchwerk.domain.model

enum class DeviceColor(val argb: Int?) {
    NONE(null),
    RED(0xFFF44336.toInt()),
    ORANGE(0xFFFF9800.toInt()),
    YELLOW(0xFFFDD835.toInt()),
    GREEN(0xFF4CAF50.toInt()),
    TEAL(0xFF00BCD4.toInt()),
    BLUE(0xFF006EDC.toInt()),
    PURPLE(0xFF9C27B0.toInt()),
    BROWN(0xFF795548.toInt()),
    SLATE(0xFF3F5870.toInt()),
    PINK(0xFFFF4FA3.toInt()),
    LIME(0xFFA6D608.toInt())
}
