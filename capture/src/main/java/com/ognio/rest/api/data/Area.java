package com.ognio.rest.api.data;

import com.ognio.rest.api.validation.AreaAnnotation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Denis B. Kulikov<br/>
 * date: 25.08.2019:0:02<br/>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@AreaAnnotation(message = "'From' should be lesser then 'To'")
public class Area {
    private Point from;
    private Point to;
}
