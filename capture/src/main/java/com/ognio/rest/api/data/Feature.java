package com.ognio.rest.api.data;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 *
 * {
 *          "type":"Feature",
 *          "geometry":{
 *             "type":"Point",
 *             "coordinates":[
 *                -0.1355294,
 *                51.5235359
 *             ]
 *          },
 *          "properties":{
 *             "id":"108042",
 *             "marked":true
 *          }
 *       }
 *
 * @author Denis B. Kulikov<br/>
 * date: 23.08.2019:19:29<br/>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.NAME, property = "type")
public class Feature {
    private Point geometry;

    private Map<String, String> properties;

}
