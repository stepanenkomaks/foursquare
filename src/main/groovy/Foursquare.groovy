class Foursquare {

    static void main(String[] args) {
        def latitude = "40.748574"
        def longitude = "-73.985918"

        println "Your location is: " + FindPlaces.findPlace("40.748574", "-73.985918")
    }
}
