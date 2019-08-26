package com.ognio.rest;

import com.ognio.rest.api.CaptureController;
import com.ognio.rest.api.data.Area;
import com.ognio.rest.api.data.Feature;
import com.ognio.rest.api.data.FeatureCollection;
import com.ognio.rest.api.data.Point;
import com.ognio.rest.service.MappingService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 *  Get a location point in a certain area;
 *  Conquer a location point;
 *  Show your score;
 *
 * @author Denis B. Kulikov<br/>
 * date: 24.08.2019:21:16<br/>
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = CaptureGameRest.class)
public class MainTest {

    private static final long FIRST_CLIENT_ID = 1L;
    private static final Long SECOND_CLIENT_ID = 2L;
    private static final Area AREA = new Area(new Point(new double[]{-0.5946078, 51.5235359}), new Point(new double[]{-0.1355294, 52.6008404}));

    @Autowired
    private CaptureController captureController;
    @Autowired
    private MappingService mappingService;

    @Test
    public void test() {


        FeatureCollection points = captureController.getPoints(AREA);

        Long firstPoint = points.getFeatures().keySet().iterator().next();
        Feature firstFeature = points.getFeatures().get(firstPoint);

        assertTrue("The point should be captured by first request", captureController.conquer(FIRST_CLIENT_ID, firstFeature.getGeometry()));
        assertFalse("The point should not be captured by second request", captureController.conquer(SECOND_CLIENT_ID, firstFeature.getGeometry()));
        assertFalse("The point already captured by the same client", captureController.conquer(FIRST_CLIENT_ID, firstFeature.getGeometry()));

        assertEquals("The frist client should capture 1 point",  Long.valueOf(3), captureController.score(FIRST_CLIENT_ID));

        assertEquals("The frist client should capture 1 point",  Long.valueOf(0), captureController.score(SECOND_CLIENT_ID));

        FeatureCollection updatedPoints = captureController.getPoints(AREA);

        assertFalse("The udpated points should not be contained already captured point", updatedPoints.getFeatures().containsKey(firstPoint));

    }
}

