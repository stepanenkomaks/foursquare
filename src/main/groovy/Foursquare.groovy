class Foursquare {

    static void main(String[] args) {
        def latitude = "40.748574"
        def longitude = "-73.985918"


        println "The current location is: " + FindPlace.findPlace(latitude, longitude)
    }
}
