package geobuf4j;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.instantdelay.geobuf4j.GeometryDecoder;
import com.instantdelay.geobuf4j.GeometryEncoder;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class CoderTest {

    private static final int precision = 6;
    
    private GeometryFactory gf = new GeometryFactory(new PrecisionModel(Math.pow(10, precision)));
    private WKTReader wkt = new WKTReader(gf);
    private GeometryEncoder encoder = new GeometryEncoder(precision, false);
    private GeometryDecoder decoder = new GeometryDecoder(precision, false);

    @Test
    public void roundTripPoint() throws ParseException {
        Geometry p = wkt.read("POINT (10 10.11111111)");
        assertThat(decoder.decode(encoder.encode(p)), is(p));
    }

    @Test
    public void roundTripMultiPoint() throws ParseException {
        Geometry g = wkt.read("MULTIPOINT(0 0, 5 5, 25 3, -81 3)");
        assertThat(decoder.decode(encoder.encode(g)), is(g));
        g = wkt.read("MULTIPOINT(0 0, 0.0001 0, 0.0001 0.0001, 0 0.0001)");
        assertThat(decoder.decode(encoder.encode(g)), is(g));
    }

    @Test
    public void roundTripLineString() throws ParseException {
        Geometry g = wkt.read("LINESTRING(0 0, 5 5, 25 3, -81 3)");
        assertThat(decoder.decode(encoder.encode(g)), is(g));
    }

    @Test
    public void roundTripMultiLineString() throws ParseException {
        Geometry g = wkt.read("MULTILINESTRING((0 0, 5 5, 25 3, -81 3), (0 0, 5 5, 25 3, -81 3), (-1 -1, -1 44, 0 0, 100 3.554444))");
        assertThat(decoder.decode(encoder.encode(g)), is(g));
    }

    @Test
    public void roundTripPolygon() throws ParseException {
        Geometry g = wkt.read("POLYGON((0 0, 5 0, 5 5, 0 5, 0 0), (1 1, 1 2, 2 2, 2 1, 1 1))");
        assertThat(decoder.decode(encoder.encode(g)), is(g));
    }

    @Test
    public void roundTripMultiPolygon() throws ParseException {
        Geometry g = wkt.read("MULTIPOLYGON("
                + " ((0 0, 5 0, 5 5, 0 5, 0 0), (1 1, 1 2, 2 2, 2 1, 1 1)), "
                + " ((0 0, 5 0, 5 5, 0 5, 0 0), (1 1, 1 2, 2 2, 2 1, 1 1)), "
                + " ((0 0, 1 0, 1 1, 0 0))"
                + ")");
        assertThat(decoder.decode(encoder.encode(g)), is(g));
    }

}
