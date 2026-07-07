package com.example.rygg.core.gpx.model

enum class GpxTags(val tag: String) {
    GPX("gpx"),
    METADATA("metadata"),
    WPT("wpt"),
    RTE("rte"),
    RTEPT("rtept"),
    TRK("trk"),
    TRKSEG("trkseg"),
    TRKPT("trkpt"),
    EXTENSIONS("extensions"),
    NAME("name"),
    DESC("desc"),
    TIME("time"),
    ELE("ele"),
    SYM("sym"),
    TYPE("type"),
    CMT("cmt"),
    NUMBER("number"),
    BOUNDS("bounds")
}
