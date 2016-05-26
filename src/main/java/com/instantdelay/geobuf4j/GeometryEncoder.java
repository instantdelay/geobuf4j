package com.instantdelay.geobuf4j;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import mapbox.geobuf.Geobuf.Data;
import mapbox.geobuf.Geobuf.Data.Geometry.Builder;
import mapbox.geobuf.Geobuf.Data.Geometry.Type;

public class GeometryEncoder {

    private double e;
    private boolean useZ;

    public GeometryEncoder(int precision, boolean useZ) {
        this.e = Math.pow(10, precision);
        this.useZ = useZ;
    }

    public Data.Geometry encode(Geometry g) {
        Builder b = Data.Geometry.newBuilder();
        switch (g.getGeometryType()) {
        case "Point":
            b.setType(Type.POINT);
            writePoint((Point)g, b);
            break;
        case "MultiPoint":
            b.setType(Type.MULTIPOINT);
            writeMultiPoint((MultiPoint) g, b);
            break;
        case "LineString":
            b.setType(Type.LINESTRING);
            writeLineString((LineString) g, b);
            break;
        case "MultiLineString":
            b.setType(Type.MULTILINESTRING);
            writeMultiLineString((MultiLineString) g, b);
            break;
        case "Polygon":
            b.setType(Type.POLYGON);
            writePolygon((Polygon) g, b, new long[] {0,0,0});
            break;
        case "MultiPolygon":
            b.setType(Type.MULTIPOLYGON);
            writeMultiPolygon((MultiPolygon) g, b);
            break;
        case "GeometryCollection":
            b.setType(Type.GEOMETRYCOLLECTION);
        default:
            throw new IllegalArgumentException("Unknown geometry type: " + g.getGeometryType());
        }
        return b.build();
    }

    private void writePoint(Point g, Builder b) {
        deltaEncode(new Coordinate[] { g.getCoordinate() }, b);
    }

    private void writeMultiPoint(MultiPoint g, Builder b) {
        Coordinate[] coords = new Coordinate[g.getNumGeometries()];
        for (int i = 0; i < g.getNumGeometries(); i++) {
            coords[i] = g.getGeometryN(i).getCoordinate();
        }
        deltaEncode(coords, b);
    }

    private void writeLineString(LineString g, Builder b) {
        deltaEncode(g.getCoordinateSequence(), b);
    }

    private void writeMultiLineString(MultiLineString g, Builder b) {
        for (int i = 0; i < g.getNumGeometries(); i++) {
            LineString ls = (LineString) g.getGeometryN(i);
            b.addLengths(ls.getNumPoints());
//            deltaEncode(ls.getCoordinateSequence(), b);
        }
        deltaEncode(g.getCoordinates(), b);
    }

    private void writeMultiPolygon(MultiPolygon g, Builder b) {
        b.addLengths(g.getNumGeometries());
        long[] last = new long[] {0,0,0};
        
        for (int i = 0; i < g.getNumGeometries(); i++) {
            b.addLengths(((Polygon)g.getGeometryN(i)).getNumInteriorRing()+1);
            writePolygon((Polygon) g.getGeometryN(i), b, last);
        }
    }

    private void writePolygon(Polygon g, Builder b, long[] last) {
        b.addLengths(g.getExteriorRing().getNumPoints() - 1);
        deltaEncode(g.getExteriorRing().getCoordinateSequence(), b, true, last);
        for (int i = 0; i < g.getNumInteriorRing(); i++) {
            LineString ring = g.getInteriorRingN(i);
            b.addLengths(ring.getNumPoints() - 1);
            deltaEncode(ring.getCoordinateSequence(), b, true, last);
        }
//        deltaEncode(g.getCoordinates(), b);
    }

    private void deltaEncode(CoordinateSequence coords, Builder b, boolean closed, long[] last) {
        int length = coords.size();
        if (closed) {
            length--;
        }
        
        for (int i = 0; i < length; i++) {
            long x = Math.round(coords.getOrdinate(i, CoordinateSequence.X) * e);
            long y = Math.round(coords.getOrdinate(i, CoordinateSequence.Y) * e);
            b.addCoords(x - last[0]);
            b.addCoords(y - last[1]);
            last[0] = x;
            last[1] = y;
            
            if (useZ) {
                long z = Math.round(coords.getOrdinate(i, CoordinateSequence.Z) * e);
                b.addCoords(z - last[2]);
                last[2] = z;
            }
        }
    }
    
    private void deltaEncode(CoordinateSequence coords, Builder b) {
        long lastX = 0, lastY = 0, lastZ = 0;
        for (int i = 0; i < coords.size(); i++) {
            long x = Math.round(coords.getOrdinate(i, CoordinateSequence.X) * e);
            long y = Math.round(coords.getOrdinate(i, CoordinateSequence.Y) * e);
            b.addCoords(x - lastX);
            b.addCoords(y - lastY);
            lastX = x;
            lastY = y;
            
            if (useZ) {
                long z = Math.round(coords.getOrdinate(i, CoordinateSequence.Z) * e);
                b.addCoords(z - lastZ);
                lastZ = z;
            }
        }
    }

    private void deltaEncode(Coordinate[] coords, Builder b) {
        long lastX = 0, lastY = 0, lastZ = 0;
        for (int i = 0; i < coords.length; i++) {
            long x = Math.round(coords[i].x * e);
            long y = Math.round(coords[i].y * e);
            b.addCoords(x - lastX);
            b.addCoords(y - lastY);
            lastX = x;
            lastY = y;
            
            if (useZ) {
                long z = Math.round(coords[i].z * e);
                b.addCoords(z - lastZ);
                lastZ = z;
            }
        }
    }

}
