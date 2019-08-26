package com.ognio.rest.service;

import com.ognio.rest.api.data.Feature;
import com.ognio.rest.api.data.Point;
import com.ognio.rest.model.GeoFeature;
import com.ognio.rest.model.GeoPoint;
import net.sf.brunneng.jom.IMergingContext;
import net.sf.brunneng.jom.MergingContext;
import net.sf.brunneng.jom.converters.TypeConverter;
import net.sf.brunneng.jom.info.OperationContextInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Denis B. Kulikov<br/>
 * date: 24.08.2019:19:24<br/>
 */
@Service
public class MappingService {



    private final ThreadLocal<IMergingContext> contextThreadLocal = new InheritableThreadLocal<IMergingContext>(){
        @Override
        protected IMergingContext initialValue() {
            MergingContext mergingContext = new MergingContext();
            mergingContext.setTypeConverters(Arrays.asList(new GeoFeatureTypeConverter(), new FeatureTypeConverter(), new GeoPointTypeConverter(), new PointTypeConverter()));
            return mergingContext;
        }
    };

    public GeoFeature toModel(Feature point) {
        return contextThreadLocal.get().map(point, GeoFeature.class);
    }

    public Feature toApi(GeoFeature point) {
        return contextThreadLocal.get().map(point, Feature.class);
    }
    public GeoPoint toModel(Point point) {
        return contextThreadLocal.get().map(point, GeoPoint.class);
    }


    public static class FeatureTypeConverter implements TypeConverter {

        private static Logger logger = LoggerFactory.getLogger(FeatureTypeConverter.class);

        public boolean canConvert(Class fromClass, Class toClass) {
            return (fromClass.equals(Feature.class) && (toClass.equals(GeoFeature.class)));
        }

        public Object convert(Class targetClass, Object obj, OperationContextInfo contextInfo) {
            try {
                Feature feature = (Feature) obj;
                Point p = feature.getGeometry();
                Boolean marked = Boolean.valueOf(feature.getProperties().get(Point.MARKED));
                return new GeoFeature(Long.parseLong(feature.getProperties().get(Point.ID)), marked? GeoFeature.UNKNOWN_CLIENT_ID : null, new GeoPoint(p.getCoordinates()[0], p.getCoordinates()[1]));
            } catch (Exception e) {
                logger.error("Impossible convert point to geoPoint", e);
            }
            return null;
        }
    }

    public static class GeoFeatureTypeConverter implements TypeConverter {

        private static Logger logger = LoggerFactory.getLogger(GeoFeatureTypeConverter.class);

        public boolean canConvert(Class fromClass, Class toClass) {
            return (fromClass.equals(GeoFeature.class) && (toClass.equals(Feature.class)));
        }

        public Object convert(Class targetClass, Object obj, OperationContextInfo contextInfo) {
            try {
                GeoFeature p = (GeoFeature) obj;

                Map<String, String> map = new HashMap<>();
                map.put(Point.ID, Long.toString(p.getId()));
                map.put(Point.MARKED, Boolean.toString(p.getClientId()!= null));
                return new Feature(new Point(new double[]{p.getGeoPoint().getLongitude(), p.getGeoPoint().getLatitude()}), map);
            } catch (Exception e) {
                logger.error("Impossible convert GeoFeature to Point", e);
            }

            return null;

        }
    }


    public static class PointTypeConverter implements TypeConverter {

        private static Logger logger = LoggerFactory.getLogger(FeatureTypeConverter.class);

        public boolean canConvert(Class fromClass, Class toClass) {
            return (fromClass.equals(Point.class) && (toClass.equals(GeoPoint.class)));
        }

        public Object convert(Class targetClass, Object obj, OperationContextInfo contextInfo) {
            try {
                Point p = (Point) obj;
                return new GeoPoint(p.getCoordinates()[0], p.getCoordinates()[1]);
            } catch (Exception e) {
                logger.error("Impossible convert point to geoPoint", e);
            }
            return null;
        }
    }

    public static class GeoPointTypeConverter implements TypeConverter {

        private static Logger logger = LoggerFactory.getLogger(GeoFeatureTypeConverter.class);

        public boolean canConvert(Class fromClass, Class toClass) {
            return (fromClass.equals(GeoPoint.class) && (toClass.equals(Point.class)));
        }

        public Object convert(Class targetClass, Object obj, OperationContextInfo contextInfo) {
            try {
                GeoPoint p = (GeoPoint) obj;
                return new Point(new double[]{p.getLongitude(), p.getLatitude()});
            } catch (Exception e) {
                logger.error("Impossible convert GeoFeature to Point", e);
            }

            return null;

        }
    }
}
