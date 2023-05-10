package com.example.pitlocator;

public class LatitudeLongitude {
        private Double lat,lng;

        private LatitudeLongitude() {}

        public LatitudeLongitude(Double lat,Double lng) {
            this.lat = lat;
            this.lng = lng;
        }

        public Double getLat() {
            return lat;
        }

        public Double getLng() {
            return lng;
        }
}
