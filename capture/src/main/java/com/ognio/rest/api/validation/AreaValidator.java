package com.ognio.rest.api.validation;

import com.ognio.rest.api.data.Area;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class AreaValidator implements ConstraintValidator<AreaAnnotation, Area> {

    @Override
    public void initialize(AreaAnnotation constraintAnnotation) {

    }

    /**
     * Validate area, 'from' x,y should be lesser or equal 'to' x, y
     */
    public boolean isValid(Area object, ConstraintValidatorContext context) {
        if (!(object instanceof Area)) {
            throw new IllegalArgumentException("@Area only applies to Area");
        }

        double[] from = object.getFrom().getCoordinates();
        double[] to = object.getTo().getCoordinates();
        return from[0]<= to[0] && from[1] <= to[1] ;

    }
}