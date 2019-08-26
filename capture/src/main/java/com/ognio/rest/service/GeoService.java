package com.ognio.rest.service;

import com.ognio.rest.api.data.FeatureCollection;
import com.ognio.rest.api.data.Point;

/**
 * @author Denis B. Kulikov<br/>
 * date: 24.08.2019:19:40<br/>
 */
public interface GeoService {
    FeatureCollection getPoints(Point from, Point to);

    Long score(Long clientId);

    Boolean conquer(Long clientId, Point point);
}
