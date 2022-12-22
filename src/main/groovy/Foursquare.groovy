class Foursquare {
    private static def service = new FoursquareService()
    private static def scanner = new Scanner(System.in)

    static void main(String[] args) {
        def latitude, longitude
        while (true) {
            try {
                println "Enter latitude:"
                latitude = scanner.nextFloat()
                println "Enter longitude:"
                longitude = scanner.nextFloat()
                if (latitude >= -180 && latitude <= 180 && longitude >= -180 && longitude <= 180) break
                else println "These are not valid coordinates. Try again!"
            } catch (InputMismatchException exception) {
                println "This is not the right coordinate. Try again!"
                scanner.next()
            }
        }

        println service.findPlace(latitude, longitude)
    }
}
