package com.instantdelay.geobuf4j;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import mapbox.geobuf.Geobuf.Data;

public class GeometryDecoder {

    private GeometryFactory gf = new GeometryFactory();
    private double e;
    private boolean useZ;
    private int dim;

    public GeometryDecoder(int precision, boolean useZ) {
        this.e = Math.pow(10, precision);
        this.useZ = useZ;
        this.dim = useZ ? 3 : 2;
    }

    public Geometry decode(Data.Geometry g) {
        switch (g.getType()) {
        case POINT:
            return gf.createPoint(deltaDecode(g, false, 0, 1, new long[3])[0]);
        case MULTIPOINT:
            return gf.createMultiPoint(deltaDecodeAll(g));
        case LINESTRING:
            return gf.createLineString(deltaDecodeAll(g));
        case MULTILINESTRING:
            return decodeMultiLineString(g);
        case POLYGON:
            return decodePolygon(g);
        case MULTIPOLYGON:
            return decodeMultiPolygon(g);
        case GEOMETRYCOLLECTION:
            
        default:
            throw new IllegalArgumentException("Unknown geometry type: " + g.getType());
        }
        
    }

    private Polygon decodePolygon(Data.Geometry g) {
        Coordinate[][] coordArrays = deltaDecode(g, true);
        LinearRing shell = gf.createLinearRing(coordArrays[0]);
        LinearRing[] holes = new LinearRing[g.getLengthsCount() - 1];

        for (int i = 1; i < g.getLengthsCount(); i++) {
            holes[i - 1] = gf.createLinearRing(coordArrays[i]);
        }
        return gf.createPolygon(shell, holes);
    }

    private MultiPolygon decodeMultiPolygon(Data.Geometry g) {
        int coordIdx = 0;
        int lenIdx = 0;
        int numPolygons = g.getLengths(lenIdx++);
        long sum[] = new long[3];
        Polygon[] polygons = new Polygon[numPolygons];
        for (int i = 0; i < numPolygons; i++) {
            
            int numRings = g.getLengths(lenIdx++);
            
            int size = g.getLengths(lenIdx++);
            LinearRing shell = gf.createLinearRing(deltaDecode(g, true, coordIdx, size, sum));
            coordIdx += size;
            
            LinearRing[] holes = new LinearRing[numRings - 1];

            for (int h = 0; h < numRings - 1; h++) {
                size = g.getLengths(lenIdx++);
                holes[h] = gf.createLinearRing(deltaDecode(g, true, coordIdx, size, sum));
                coordIdx += size;
            }
            polygons[i] = gf.createPolygon(shell, holes);
        }
        
        return gf.createMultiPolygon(polygons);
    }

    private MultiLineString decodeMultiLineString(Data.Geometry g) {
        Coordinate[][] coordArrays = deltaDecode(g, false);
        LineString[] geoms = new LineString[g.getLengthsCount()];
        for (int i = 0; i < g.getLengthsCount(); i++) {
            geoms[i] = gf.createLineString(coordArrays[i]);
        }
        return gf.createMultiLineString(geoms);
    }

    private Coordinate[] deltaDecodeAll(Data.Geometry g) {
        int length = g.getCoordsCount() / (useZ ? 3 : 2);
        return deltaDecode(g, false, 0, length, new long[3]);
    }

    private Coordinate[][] deltaDecode(Data.Geometry g, boolean closed) {
        long x = 0, y = 0, z = 0;
        Coordinate[][] coords = new Coordinate[g.getLengthsCount()][];
        int i = 0;
        for (int j = 0; j < g.getLengthsCount(); j++) {
            coords[j] = new Coordinate[g.getLengths(j) + (closed ? 1 : 0)];
            int n = 0;
            while (n < g.getLengths(j)) {
                x += g.getCoords(i++);
                y += g.getCoords(i++);
                
                if (useZ) {
                    z += g.getCoords(i++);
                    coords[j][n++] = new Coordinate(x/e, y/e, z/e);
                }
                else {
                    coords[j][n++] = new Coordinate(x/e, y/e);
                }
            }
            if (closed) {
                coords[j][coords[j].length - 1] = coords[j][0];
            }
        }
        return coords;
    }

    private Coordinate[] deltaDecode(Data.Geometry g, boolean closed, int offset, int length, long[] sum) {
        Coordinate[] coords = new Coordinate[length + (closed ? 1 : 0)];
        int i = offset * (dim);
        int n = 0;
        while (n < length) {
            sum[0] += g.getCoords(i++);
            sum[1] += g.getCoords(i++);
            
            if (useZ) {
                sum[2] += g.getCoords(i++);
                coords[n++] = new Coordinate(sum[0]/e, sum[1]/e, sum[2]/e);
            }
            else {
                coords[n++] = new Coordinate(sum[0]/e, sum[1]/e);
            }
        }
        if (closed) {
            coords[length] = coords[0];
        }
        return coords;
    }
    
}
