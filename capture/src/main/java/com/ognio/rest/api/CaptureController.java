
package com.ognio.rest.api;


import com.ognio.rest.api.data.Area;
import com.ognio.rest.api.data.FeatureCollection;
import com.ognio.rest.api.data.Point;
import com.ognio.rest.model.GeoPoint;
import com.ognio.rest.service.GeoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * Get a location point in a certain area;
 * Conquer a location point;
 * Show your score;
 */
@Component
@RestController
public class CaptureController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private GeoService geoService;


    @RequestMapping(value = "/conquer/{clientId}", method = RequestMethod.POST)
    public Boolean conquer(@PathVariable(value = "clientId")Long clientId, @RequestBody Point point) {
        Boolean conquer = geoService.conquer(clientId, point);
        logger.debug("Conquer result : {}, client ID {}, {} ",conquer, clientId, point);
        return conquer;

    }

    @RequestMapping(value = "/location", method = RequestMethod.POST)
    public FeatureCollection getPoints(@Valid @RequestBody Area area){
        logger.debug("Get location with area : {}", area);
        return geoService.getPoints(area.getFrom(), area.getTo());
    }

    @RequestMapping(value = "/score/{clientId}", method = RequestMethod.GET)
    public Long score(@PathVariable(value = "clientId") Long clientId) {
        Long score = geoService.score(clientId);
        logger.debug("Request score {} for client  {}", score, clientId);
        return score;
    }

}