package com.ognio.rest.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Denis B. Kulikov<br/>
 * date: 23.08.2019:19:38<br/>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeoFeature {
    public static final Long UNKNOWN_CLIENT_ID = (long) -1;
    private long id;
    private volatile Long clientId;
    private GeoPoint geoPoint;

}
