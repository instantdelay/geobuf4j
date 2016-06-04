package geobuf4j;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.FileNotFoundException;
import java.io.InputStreamReader;

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
    public void encodeAndDecodeProducesOriginal() throws FileNotFoundException, ParseException {
        InputStreamReader reader = new InputStreamReader(getClass().getResourceAsStream("/geometries.wkt"));
        Geometry g;
        while ((g = wkt.read(reader)) != null) {
            assertThat(decoder.decode(encoder.encode(g)), is(g));
        }
    }

}
