package com.exelynt.booking.dto; import java.math.BigDecimal; public record ResourceResponse(Long id,String name,String description,String type,String location,BigDecimal price,boolean available) {}
