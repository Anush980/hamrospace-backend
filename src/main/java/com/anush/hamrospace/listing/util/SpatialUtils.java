package com.anush.hamrospace.listing.util;

import org.springframework.stereotype.Component;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

@Component
public class SpatialUtils {

   private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    public Point createPoint(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return null;
        }
        // JTS Coordinate expects (X = Longitude, Y = Latitude)
        return GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude));
    }
    
}
